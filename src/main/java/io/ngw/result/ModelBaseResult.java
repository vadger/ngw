package io.ngw.result;

import io.ngw.ActionHandler;
import io.ngw.In;
import io.ngw.Out;
import io.ngw.result.Result;

import java.lang.reflect.Field;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static io.ngw.Utils.getAllFields;
import static java.util.stream.Collectors.toList;

public abstract class ModelBaseResult implements Result {
  protected Map<String, Object> model = new HashMap<>();

  protected ModelBaseResult(ActionHandler handler) {
    model.put("actionHandler", handler);
    model.put("actionContext", handler.getContext());
    getAllInOutFields(handler).forEach(field -> {
      field.setAccessible(true);
      try {
        model.put(field.getName(), field.get(handler));
      } catch (IllegalAccessException e) {
        throw new RuntimeException(e);
      }
    });
  }

  public void addToModel(String key, Object value) {
    model.put(key, value);
  }

  public Object getObject(String key) {
    return model.get(key);
  }

  private List<Field> getAllInOutFields(ActionHandler handler) {
    return getAllFields(handler)
        .filter(field -> field.isAnnotationPresent(In.class) || field.isAnnotationPresent(Out.class))
        .collect(toList());
  }
}
