package io.ngw;

import com.google.inject.Guice;
import com.google.inject.Injector;
import com.google.inject.Module;
import io.ngw.binders.*;
import io.ngw.dev.AppCodeChangedPlugin;
import io.undertow.Undertow;
import io.undertow.server.handlers.form.EagerFormParsingHandler;
import org.springsource.loaded.agent.SpringLoadedPreProcessor;

import javax.inject.Inject;
import java.math.BigDecimal;

import static java.util.Arrays.copyOf;

public class Server {

  private Undertow server;
  private Injector injector;

  @Inject BindersProvider bindersProvider;
  @Inject RouteResolver routeResolver;

  public Server() {
    try {
      injector = Guice.createInjector((Module)Class.forName("config.Module").newInstance());
      injector.injectMembers(this);
      initBinders();
      registerClassReloadPlugin();
    } catch (Exception e) {
      throw new RuntimeException(e);
    }
    CoreHttpHandler coreHandler = new CoreHttpHandler(this, routeResolver, injector);
    EagerFormParsingHandler eagerFormParsingHandler = new EagerFormParsingHandler();
    eagerFormParsingHandler.setNext(coreHandler);
    server = Undertow.builder()
        .addHttpListener(8080, "localhost")
        .setHandler(eagerFormParsingHandler).build();
  }

  private void registerClassReloadPlugin() {
    AppCodeChangedPlugin springLoadedPlugin = new AppCodeChangedPlugin();
    springLoadedPlugin.registeredRouteResolver = routeResolver;
    SpringLoadedPreProcessor.registerGlobalPlugin(springLoadedPlugin);
  }

  private void initBinders() {
    bindersProvider.addBinder(String.class, new StringBinder());
    bindersProvider.addBinder(Long.class, new LongBinder());
    bindersProvider.addBinder(BigDecimal.class, new BigDecimalBinder());
  }

  public void start() {
    server.start();
  }

  public RouteResolver getRouteResolver() {
    return routeResolver;
  }

}
