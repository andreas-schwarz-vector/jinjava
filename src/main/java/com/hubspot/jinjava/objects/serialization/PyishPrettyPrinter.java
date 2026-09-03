package com.hubspot.jinjava.objects.serialization;

import com.google.common.annotations.Beta;
import tools.jackson.core.JsonGenerator;
import tools.jackson.core.util.DefaultPrettyPrinter;

@Beta
public class PyishPrettyPrinter extends DefaultPrettyPrinter {

  public static final PyishPrettyPrinter INSTANCE = new PyishPrettyPrinter();

  @Override
  public DefaultPrettyPrinter createInstance() {
    return INSTANCE;
  }

  private PyishPrettyPrinter() {
    _objectIndenter = FixedSpaceIndenter.instance();
  }

  @Override
  public void beforeArrayValues(JsonGenerator jg) {}

  @Override
  public void writeEndArray(JsonGenerator jg, int nrOfValues) {
    if (!this._arrayIndenter.isInline()) {
      --this._nesting;
    }
    jg.writeRaw(']');
  }

  @Override
  public void writeObjectNameValueSeparator(JsonGenerator jg) {
    jg.writeRaw(": ");
  }

  @Override
  public void beforeObjectEntries(JsonGenerator jg) {}

  @Override
  public void writeEndObject(JsonGenerator jg, int nrOfEntries) {
    if (!this._objectIndenter.isInline()) {
      --this._nesting;
    }
    jg.writeRaw("} ");
  }
}
