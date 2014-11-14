package io.ngw.binders;

import javax.inject.Singleton;
import java.util.HashMap;
import java.util.Map;

@Singleton
public class BindersProvider {
  private Map<Class<?>, Binder> binders = new HashMap<>();

  public void addBinder(Class<?> bindType, Binder binder) {
    binders.put(bindType, binder);
  }

  public Binder getBinder(Class<?> type) {
    return binders.get(type);
  }
}
