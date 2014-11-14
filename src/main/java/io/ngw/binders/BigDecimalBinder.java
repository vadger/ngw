package io.ngw.binders;

import java.math.BigDecimal;

public class BigDecimalBinder implements Binder<BigDecimal> {
  @Override
  public BigDecimal bind(String value) {
    return new BigDecimal(value);
  }
}
