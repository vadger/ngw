package io.ngw;

import java.util.HashMap;
import java.util.Map;

public abstract class Router {
  final Map<Path, Class<? extends ActionHandler>> pathToAction = new HashMap<>();
  final Map<Class<? extends ActionHandler>, Path> reversed = new HashMap<>();

  public abstract void init();

  public final void reInit() {
    pathToAction.clear();
    reversed.clear();
    init();
  }

  protected void get(String path, Class<? extends ActionHandler> handler) {
    Path get = new Path("GET", path);
    pathToAction.put(get, handler);
    reversed.put(handler, get);
  }

  protected void post(String path, Class<? extends ActionHandler> handler) {
    Path post = new Path("POST", path);
    pathToAction.put(post, handler);
    reversed.put(handler, post);
  }

  public static class Path {
    public String method;
    public String path;

    public Path(String method, String path) {
      this.method = method;
      this.path = path;
    }

    @Override
    public boolean equals(Object o) {
      if (this == o) return true;
      if (o == null || getClass() != o.getClass()) return false;

      Path path1 = (Path) o;

      if (!method.equals(path1.method)) return false;
      if (!path.equals(path1.path)) return false;

      return true;
    }

    @Override
    public int hashCode() {
      int result = method.hashCode();
      result = 31 * result + path.hashCode();
      return result;
    }
  }
}
