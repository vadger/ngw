package io.ngw.interceptors;

import io.ngw.ActionContext;

public interface AfterInterceptor {
  void intercept(ActionContext context);
}
