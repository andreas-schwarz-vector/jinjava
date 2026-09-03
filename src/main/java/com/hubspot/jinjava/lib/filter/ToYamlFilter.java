package com.hubspot.jinjava.lib.filter;

import com.hubspot.jinjava.doc.annotations.JinjavaDoc;
import com.hubspot.jinjava.doc.annotations.JinjavaParam;
import com.hubspot.jinjava.doc.annotations.JinjavaSnippet;
import com.hubspot.jinjava.interpret.InvalidInputException;
import com.hubspot.jinjava.interpret.InvalidReason;
import com.hubspot.jinjava.interpret.JinjavaInterpreter;
import com.hubspot.jinjava.objects.serialization.JinjavaMapperDefaults;
import tools.jackson.core.JacksonException;
import tools.jackson.dataformat.yaml.YAMLMapper;
import tools.jackson.dataformat.yaml.YAMLWriteFeature;

@JinjavaDoc(
  value = "Writes object as a YAML string",
  input = @JinjavaParam(
    value = "object",
    desc = "Object to write to YAML",
    required = true
  ),
  snippets = { @JinjavaSnippet(code = "{{object|toyaml}}") }
)
public class ToYamlFilter implements Filter {

  private static final YAMLMapper OBJECT_MAPPER = JinjavaMapperDefaults
    .applyTo(YAMLMapper.builder())
    .disable(YAMLWriteFeature.WRITE_DOC_START_MARKER)
    .build();

  @Override
  public Object filter(Object var, JinjavaInterpreter interpreter, String... args) {
    try {
      return OBJECT_MAPPER.writeValueAsString(var);
    } catch (JacksonException e) {
      throw new InvalidInputException(interpreter, this, InvalidReason.JSON_WRITE);
    }
  }

  @Override
  public String getName() {
    return "toyaml";
  }
}
