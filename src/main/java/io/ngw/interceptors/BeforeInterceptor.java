package io.ngw.interceptors;

import io.ngw.ActionContext;
import io.ngw.result.Result;

public interface BeforeInterceptor {
  Result intercept(ActionContext context);
}
