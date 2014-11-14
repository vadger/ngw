package io.ngw;

import io.ngw.result.Result;

import java.util.ArrayList;
import java.util.List;

public abstract class ActionHandler {

  private ActionContext context;

  private List<Runnable> beforeActionEvents = new ArrayList<>();

  protected void addBeforeActionEvent(Runnable runnable) {
    beforeActionEvents.add(runnable);
  }

  public ActionContext getContext() {
    return context;
  }

  public void setContext(ActionContext context) {
    this.context = context;
  }

  public void onBeforeActionHandling() {
    for (Runnable beforeActionEvent : beforeActionEvents) {
      beforeActionEvent.run();
    }
  }

  public abstract Result handle();
}
