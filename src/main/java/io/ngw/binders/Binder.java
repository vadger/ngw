package io.ngw.binders;

public interface Binder<T> {
  T bind(String value);

  default String unbind(T value) {
    return value.toString();
  }
}
