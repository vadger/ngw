package io.ngw;


import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Stream;

import static java.util.Arrays.asList;
import static java.util.stream.Collectors.toList;

public final class Utils {
  private Utils() {
  }

  public static Stream<Field> getAllFields(Object object) {
    List<Field> result = new ArrayList<>();
    Class<?> clazz = object.getClass();
    while (clazz != null) {
      result.addAll(asList(clazz.getDeclaredFields()));
      clazz = clazz.getSuperclass();
    }
    return result.stream();
  }
}
