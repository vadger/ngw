package io.ngw.binders;

public class LongBinder implements Binder<Long> {
  @Override
  public Long bind(String value) {
    return Long.valueOf(value);
  }
}
