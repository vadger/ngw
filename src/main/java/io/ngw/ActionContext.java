package io.ngw;

import com.google.inject.Injector;

public interface ActionContext {
  Injector getInjector();

  default String getRequestParameter(String name) {
    String[] requestParameters = getRequestParameters(name);
    return requestParameters != null && requestParameters.length > 0  ? requestParameters[0] : null;
  }

  String[] getRequestParameters(String name);

  Class<? extends ActionHandler> getHandlerClass();
}
