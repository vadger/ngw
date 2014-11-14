package io.ngw.interceptors;

import io.ngw.ActionContext;
import io.ngw.result.Result;

public interface OnExceptionInterceptor {
  Result intercept(ActionContext context);
}
