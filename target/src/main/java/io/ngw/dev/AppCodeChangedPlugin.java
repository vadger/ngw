package io.ngw.dev;

import io.ngw.RouteResolver;
import io.ngw.Router;
import org.springsource.loaded.ReloadEventProcessorPlugin;

public class AppCodeChangedPlugin implements ReloadEventProcessorPlugin {

  public RouteResolver registeredRouteResolver;

  @Override
  public boolean shouldRerunStaticInitializer(String s, Class<?> aClass, String s2) {
    return false;
  }

  @Override
  public void reloadEvent(String typeName, Class<?> clazz, String encodedTimestamp) {
    if (Router.class.isAssignableFrom(clazz)) {
      registeredRouteResolver.reInit();
    }
  }
}
