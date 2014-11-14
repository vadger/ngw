package io.ngw.binders;

public class StringBinder implements Binder<String> {
  @Override
  public String bind(String value) {
    return value;
  }

  @Override
  public String unbind(String value) {
    return value;
  }
}
