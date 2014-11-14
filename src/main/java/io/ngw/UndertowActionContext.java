package io.ngw;

import com.google.inject.Injector;
import io.undertow.server.HttpServerExchange;
import io.undertow.server.handlers.form.FormData;
import io.undertow.server.handlers.form.FormDataParser;
import io.undertow.util.HttpString;

import java.util.*;
import java.util.stream.Collectors;

public class UndertowActionContext implements ActionContext {

  private static final HttpString POST = new HttpString("POST");
  private Map<String, String[]> requestParameters;


  private HttpServerExchange exchange;
  private Injector injector;
  private Class<? extends ActionHandler> handlerClass;

  public UndertowActionContext(HttpServerExchange exchange, Injector injector, Class<? extends ActionHandler> handlerClass) {
    this.exchange = exchange;
    this.injector = injector;
    this.handlerClass = handlerClass;
    this.requestParameters = prepareRequestParameters();
  }

  private Map<String, String[]> prepareRequestParameters() {
    Map<String, String[]> result = new HashMap<>();
    for (Map.Entry<String, Deque<String>> entry : exchange.getQueryParameters().entrySet()) {
      Deque<String> params = entry.getValue();
      if (params != null) {
        result.put(entry.getKey(), params.toArray(new String[params.size()]));
      }
    }
    FormData formData = exchange.getAttachment(FormDataParser.FORM_DATA);
    if (formData != null) {
      for (String key : formData) {
        Deque<FormData.FormValue> formValues = formData.get(key);
        List<String> values = formValues.stream().filter(fv -> !fv.isFile()).map(FormData.FormValue::getValue).collect(Collectors.toList());
        result.put(key, values.toArray(new String[values.size()]));
      }
    }
    return result;
  }

  @Override
  public Injector getInjector() {
    return injector;
  }

  @Override
  public String[] getRequestParameters(String name) {
    return requestParameters.get(name);
  }

  @Override
  public Class<? extends ActionHandler> getHandlerClass() {
    return handlerClass;
  }
}
