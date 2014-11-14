package io.ngw.result;

import java.io.InputStream;

public interface Result {
  InputStream flush();

  String getContentType();
}
