package io.ngw.result;

import freemarker.ext.beans.BeansWrapper;
import freemarker.template.Configuration;
import freemarker.template.Template;
import freemarker.template.TemplateException;
import io.ngw.ActionHandler;

import java.io.*;

public class FreemarkerResult extends ModelBaseResult {

  private String contentType = "text/html";
  private String templateFile;

  private static Configuration configuration = initConfiguration();

  private static Configuration initConfiguration() {
    Configuration conf = new Configuration();
    conf.setDefaultEncoding("UTF-8");
    BeansWrapper objectWrapper = new BeansWrapper();
    objectWrapper.setExposeFields(true);
    conf.setObjectWrapper(objectWrapper);
    conf.setTemplateLoader(new HtmlTemplateLoader());
    return conf;
  }

  public FreemarkerResult(ActionHandler handler, String templateFile) {
    super(handler);
    this.templateFile = templateFile;
  }

  public FreemarkerResult(ActionHandler handler) {
    super(handler);
    Class<? extends ActionHandler> handlerClass = handler.getClass();
    this.templateFile = handlerClass.getPackage().getName().replace("handlers.", "").replaceAll("\\.", "/")
        + "/" + handlerClass.getSimpleName().substring(0, 1).toLowerCase() + handlerClass.getSimpleName().substring(1)
        + ".ftl";
  }

  public FreemarkerResult(ActionHandler handler, String templateFile, String contentType) {
    this(handler, templateFile);
    this.contentType = contentType;
  }

  @Override
  public InputStream flush() {
    try {
      Template template = configuration.getTemplate(templateFile);
      template.setEncoding("UTF-8");
      ByteArrayOutputStream out = new ByteArrayOutputStream();
      OutputStreamWriter writer = new OutputStreamWriter(out);
      template.process(model,  writer);
      writer.close();
      return new ByteArrayInputStream(out.toByteArray());
    } catch (IOException | TemplateException e) {
      throw new RuntimeException(e);
    }
  }

  @Override
  public String getContentType() {
    return contentType;
  }
}
