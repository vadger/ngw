package io.ngw;

import io.undertow.server.HttpServerExchange;

import javax.inject.Singleton;
import java.util.ArrayList;
import java.util.List;

@Singleton
public class RouteResolver {
  private List<Router> routers = new ArrayList<>();

  public void addRouter(Router router) {
    router.init();
    routers.add(router);
  }

  public Class<? extends ActionHandler> resolve(HttpServerExchange exchange) {
    Class<? extends ActionHandler> actionHandlerClass = null;

    Router.Path path = new Router.Path(exchange.getRequestMethod().toString(), exchange.getRequestPath());
    for (Router router : routers) {
      actionHandlerClass = router.pathToAction.get(path);
      if (actionHandlerClass != null) return actionHandlerClass;
    }

    return null;
  }

  public String reverseResole(Class<? extends ActionHandler> handlerClazz) {
    for (Router router : routers) {
      Router.Path path = router.reversed.get(handlerClazz);
      if (path  != null) return path.path;
    }
    return null;
  }

  public void reInit() {
    for (Router router : routers) {
      router.reInit();
    }
  }
}
