package io.ngw.result;

import io.ngw.ActionHandler;

import java.io.InputStream;

public class ForwardResult implements Result {

  private ActionHandler handler;

  public ForwardResult(ActionHandler handler) {
    this.handler = handler;
  }

  @Override
  public InputStream flush() {
    return null;
  }

  @Override
  public String getContentType() {
    return null;
  }

  public ActionHandler getHandler() {
    return handler;
  }
}
