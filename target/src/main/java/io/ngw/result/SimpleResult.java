package io.ngw.result;

import io.ngw.result.Result;

import java.io.ByteArrayInputStream;
import java.io.InputStream;
import java.io.UnsupportedEncodingException;

public class SimpleResult implements Result {

  private String content;

  public SimpleResult(String content) {
    this.content = content;
  }

  @Override
  public InputStream flush() {
    try {
      return new ByteArrayInputStream(content.getBytes("utf8"));
    } catch (UnsupportedEncodingException e) {
      throw new RuntimeException(e);
    }
  }

  @Override
  public String getContentType() {
    return "text/html";
  }
}
