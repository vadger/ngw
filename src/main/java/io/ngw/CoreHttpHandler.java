package io.ngw;

import com.google.inject.Injector;
import io.ngw.binders.Binder;
import io.ngw.interceptors.Before;
import io.ngw.interceptors.BeforeInterceptor;
import io.ngw.result.ForwardInterceptionResult;
import io.ngw.result.ForwardResult;
import io.ngw.result.RedirectResult;
import io.ngw.result.Result;
import io.undertow.UndertowLogger;
import io.undertow.io.IoCallback;
import io.undertow.io.Sender;
import io.undertow.server.HttpHandler;
import io.undertow.server.HttpServerExchange;
import io.undertow.server.handlers.RedirectHandler;
import io.undertow.util.Headers;
import org.xnio.IoUtils;

import java.io.IOException;
import java.io.InputStream;
import java.nio.ByteBuffer;
import java.util.ArrayList;
import java.util.List;
import java.util.Stack;

import static io.ngw.Utils.getAllFields;
import static java.util.Arrays.asList;

class CoreHttpHandler implements HttpHandler {

  private Server server;
  private RouteResolver routeResolver;
  private Injector injector;

  CoreHttpHandler(Server server, RouteResolver routeResolver, Injector injector) {
    this.server = server;
    this.routeResolver = routeResolver;
    this.injector = injector;
  }

  @Override
  public void handleRequest(final HttpServerExchange exchange) throws Exception {
    Class<? extends ActionHandler> handlerClass = routeResolver.resolve(exchange);
    if (handlerClass == null) {
      exchange.setResponseCode(404);
      exchange.getResponseSender().send("Not found");
      exchange.getResponseSender().close();
      return;
    }
    UndertowActionContext context = new UndertowActionContext(exchange, injector, handlerClass);
    Result result = handleBeforeInterceptors(handlerClass, context);

    if (result instanceof ForwardInterceptionResult) {
      ActionHandler handler = prepareHandler(exchange, handlerClass, context);
      result = handler.handle();
      result = handleForwardResultIfNeeded(result, exchange, context);
    }

    if (result instanceof RedirectResult) {
      new RedirectHandler(((RedirectResult)result).getLocation()).handleRequest(exchange);
      return;
    }

    exchange.getResponseHeaders().add(Headers.CONTENT_TYPE, result.getContentType());
    final long startTime = System.currentTimeMillis();
    sendResponse(result, exchange, new IoCallback() {
      @Override
      public void onComplete(HttpServerExchange exchange, Sender sender) {
        System.out.println("Response sent in " + (System.currentTimeMillis() - startTime) + "ms");
        sender.close();
      }

      @Override
      public void onException(HttpServerExchange exchange, Sender sender, IOException exception) {

      }
    });
  }

  private Result handleForwardResultIfNeeded(Result result, HttpServerExchange exchange, UndertowActionContext context) {
    if (result instanceof ForwardResult) {
      ForwardResult forwardResult = (ForwardResult) result;
      ActionHandler handler = forwardResult.getHandler();
      prepareHandler(exchange, handler.getClass(), context);
      return handleForwardResultIfNeeded(handler.handle(), exchange, context);
    }
    return result;
  }

  private ActionHandler prepareHandler(HttpServerExchange exchange, Class<? extends ActionHandler> handlerClass, UndertowActionContext context) {
    ActionHandler handler = null;
    try {
      handler = handlerClass.newInstance();
    } catch (InstantiationException | IllegalAccessException e) {
      throw new RuntimeException(e);
    }
    handler.setContext(context);
    injector.injectMembers(handler);
    bindParameters(handler, exchange);
    handler.onBeforeActionHandling();
    return handler;
  }

  private Result handleBeforeInterceptors(Class<? extends ActionHandler> handlerClass, ActionContext context) {
    Stack<Class> inheritanceStack = getInheritanceStack(handlerClass);
    List<Class<? extends BeforeInterceptor>> interceptors = new ArrayList<>();

    while (!inheritanceStack.isEmpty()) {
      Before before = (Before) inheritanceStack.pop().getAnnotation(Before.class);
      if (before != null) interceptors.addAll(asList(before.value()));
    }

    Result result = new ForwardInterceptionResult();

    for (Class<? extends BeforeInterceptor> interceptor : interceptors) {
      result = injector.getInstance(interceptor).intercept(context);
      if (!(result instanceof ForwardInterceptionResult)) return result;
    }

    return result;
  }

  private Stack<Class> getInheritanceStack(Class<? extends ActionHandler> handlerClass) {
    Stack<Class> inheritanceStack = new Stack<>();
    Class current = handlerClass;
    while (!current.equals(Object.class)) {
      inheritanceStack.push(current);
      current = current.getSuperclass();
    }
    return inheritanceStack;
  }

  private void bindParameters(ActionHandler handler, HttpServerExchange exchange) {
    getAllFields(handler)
        .filter(field -> field.isAnnotationPresent(In.class))
        .forEach(field -> {
          try {
            String value = handler.getContext().getRequestParameter(field.getName());
            if (value != null) {
              Binder binder = server.bindersProvider.getBinder(field.getType());
              if (binder != null) {
                field.setAccessible(true);
                field.set(handler, binder.bind(value));
              }
            }
          } catch (Exception e) {
            // ignore
          }
        });
  }

  private void sendResponse(final Result result, final HttpServerExchange exchange, final IoCallback completionCallback) {
    final Sender sender = exchange.getResponseSender();
    // Copied from URL resource
    class ServerTask implements Runnable, IoCallback {

      private InputStream inputStream;
      private byte[] buffer;

      @Override
      public void run() {
        if (inputStream == null) {
          inputStream = result.flush();
          buffer = new byte[2048];//TODO: we should be pooling these
        }
        try {
          int res = inputStream.read(buffer);
          if (res == -1) {
            //we are done, just return
            IoUtils.safeClose(inputStream);
            completionCallback.onComplete(exchange, sender);
            return;
          }
          sender.send(ByteBuffer.wrap(buffer, 0, res), this);
        } catch (IOException e) {
          onException(exchange, sender, e);
        }

      }

      @Override
      public void onComplete(final HttpServerExchange exchange, final Sender sender) {
        if (exchange.isInIoThread()) {
          exchange.dispatch(this);
        } else {
          run();
        }
      }

      @Override
      public void onException(final HttpServerExchange exchange, final Sender sender, final IOException exception) {
        UndertowLogger.REQUEST_IO_LOGGER.ioException(exception);
        IoUtils.safeClose(inputStream);
        if (!exchange.isResponseStarted()) {
          exchange.setResponseCode(500);
        }
        completionCallback.onException(exchange, sender, exception);
      }
    }

    ServerTask serveTask = new ServerTask();
    if (exchange.isInIoThread()) {
      exchange.dispatch(serveTask);
    } else {
      serveTask.run();
    }
  }
}
