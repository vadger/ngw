package io.ngw.result;

import java.io.InputStream;

public class ForwardInterceptionResult implements Result {
  @Override
  public InputStream flush() {
    return null;
  }

  @Override
  public String getContentType() {
    return null;
  }
}
