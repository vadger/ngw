package io.ngw.result;

import com.google.common.base.Joiner;
import com.google.common.base.Strings;
import io.ngw.*;
import io.ngw.binders.Binder;
import io.ngw.binders.BindersProvider;

import java.io.InputStream;
import java.io.UnsupportedEncodingException;
import java.lang.reflect.Field;
import java.net.URLEncoder;
import java.util.stream.Stream;

public class RedirectResult implements Result {

  private ActionHandler handlerTemplate;
  private ActionContext context;

  public RedirectResult(Class<? extends ActionHandler> handlerClazz, ActionContext context) {
    try {
      handlerTemplate = handlerClazz.newInstance();
    } catch (InstantiationException | IllegalAccessException e) {
      throw new RuntimeException(e);
    }
    this.context = context;
  }

  public RedirectResult(final ActionHandler handler, ActionContext context) {
    handlerTemplate = handler;
    this.context = context;
  }

  private String getEncodedFieldValue(Field f, ActionHandler handler, BindersProvider bindersProvider) {
    f.setAccessible(true);
    try {
      Object value = f.get(handler);
      // TODO collections and arrays should be handled separately
      Binder binder = bindersProvider.getBinder(f.getType());
      return value != null ? URLEncoder.encode(binder != null ? binder.unbind(value) : value.toString(), "utf-8") : "";
    } catch (IllegalAccessException | UnsupportedEncodingException e) {
      throw new RuntimeException(e);
    }
  }

  @Override
  public InputStream flush() {
    return null;
  }

  @Override
  public String getContentType() {
    return null;
  }

  public String getLocation() {
    BindersProvider bindersProvider = context.getInjector().getInstance(BindersProvider.class);
    StringBuilder location = new StringBuilder(context.getInjector().getInstance(RouteResolver.class).reverse(handlerTemplate.getClass()));

    Stream<String> keyValue = Utils.getAllFields(handlerTemplate)
        .filter(f -> f.isAnnotationPresent(In.class))
        .map(f -> f.getName() + "=" + getEncodedFieldValue(f, handlerTemplate, bindersProvider));
    String params = Joiner.on("&").join(keyValue.toArray());
    if (!Strings.isNullOrEmpty(params)) location.append("?").append(params);

    return location.toString();
  }
}
