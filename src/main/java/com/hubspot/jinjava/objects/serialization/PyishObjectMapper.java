package com.hubspot.jinjava.objects.serialization;

import com.google.common.annotations.Beta;
import com.hubspot.jinjava.interpret.DeferredValueException;
import com.hubspot.jinjava.interpret.JinjavaInterpreter;
import com.hubspot.jinjava.interpret.OutputTooBigException;
import com.hubspot.jinjava.util.WhitespaceUtils;
import java.io.CharArrayWriter;
import java.io.Writer;
import java.util.Objects;
import java.util.Optional;
import java.util.concurrent.atomic.AtomicInteger;
import tools.jackson.core.JacksonException;
import tools.jackson.core.JsonGenerator;
import tools.jackson.databind.DatabindException;
import tools.jackson.databind.ObjectWriter;
import tools.jackson.databind.PropertyNamingStrategies;
import tools.jackson.databind.SerializationContext;
import tools.jackson.databind.ValueSerializer;
import tools.jackson.databind.json.JsonMapper;
import tools.jackson.databind.module.SimpleModule;

@Beta
public class PyishObjectMapper {

  public static final ObjectWriter PYISH_OBJECT_WRITER;
  public static final ObjectWriter SNAKE_CASE_PYISH_OBJECT_WRITER;
  public static final String ALLOW_SNAKE_CASE_ATTRIBUTE = "allowSnakeCase";

  static {
    PYISH_OBJECT_WRITER =
      getPyishObjectMapperBuilder()
        .build()
        .writer()
        .with(PyishPrettyPrinter.INSTANCE)
        .with(PyishCharacterEscapes.INSTANCE);

    SNAKE_CASE_PYISH_OBJECT_WRITER =
      getPyishObjectMapperBuilder()
        .propertyNamingStrategy(PropertyNamingStrategies.SNAKE_CASE)
        .build()
        .writer()
        .with(PyishPrettyPrinter.INSTANCE)
        .with(PyishCharacterEscapes.INSTANCE);
  }

  private static JsonMapper.Builder getPyishObjectMapperBuilder() {
    return JinjavaMapperDefaults
      .applyTo(
        JsonMapper.builder(
          JinjavaMapperDefaults.jsonFactoryBuilder().quoteChar('\'').build()
        )
      )
      .addModule(
        new SimpleModule()
          .setSerializerModifier(PyishBeanSerializerModifier.INSTANCE)
          .addSerializer(PyishSerializable.class, PyishSerializer.INSTANCE)
          .setDefaultNullKeySerializer(new NullKeySerializer())
      );
  }

  public static String getAsUnquotedPyishString(Object val) {
    if (val == null) {
      return "";
    }
    if (val instanceof String || val instanceof Number || val instanceof Boolean) {
      return val.toString();
    }
    return WhitespaceUtils.unquoteAndUnescape(getAsPyishString(val, true));
  }

  public static String getAsPyishString(Object val) {
    return getAsPyishString(val, false);
  }

  private static String getAsPyishString(Object val, boolean forOutput) {
    try {
      return getAsPyishStringOrThrow(val, forOutput);
    } catch (JacksonException e) {
      handleLengthLimitingException(e);
      handleDeferredValueException(e);
      return Objects.toString(val, "");
    }
  }

  private static void handleDeferredValueException(JacksonException e) {
    Throwable unwrapped = unwrap(e);
    if (unwrapped instanceof DeferredValueException) {
      throw (DeferredValueException) unwrapped;
    }
  }

  public static void handleLengthLimitingException(JacksonException e) {
    Throwable unwrapped = unwrap(e);
    if (unwrapped instanceof LengthLimitingJsonProcessingException) {
      throw new OutputTooBigException(
        ((LengthLimitingJsonProcessingException) unwrapped).getMaxSize(),
        ((LengthLimitingJsonProcessingException) unwrapped).getAttemptedSize()
      );
    } else if (unwrapped instanceof OutputTooBigException) {
      throw (OutputTooBigException) unwrapped;
    }
  }

  /**
   * Jackson wraps any non-Jackson exception thrown while serializing a bean property
   * in a {@link DatabindException}, so the original cause has to be peeled off before
   * it can be inspected.
   */
  private static Throwable unwrap(JacksonException e) {
    return e instanceof DatabindException ? e.getCause() : e;
  }

  public static String getAsPyishStringOrThrow(Object val) {
    return getAsPyishStringOrThrow(val, false);
  }

  public static String getAsPyishStringOrThrow(Object val, boolean forOutput) {
    boolean useSnakeCaseMappingOverride = JinjavaInterpreter
      .getCurrentMaybe()
      .map(interpreter ->
        interpreter.getConfig().getLegacyOverrides().isUseSnakeCasePropertyNaming()
      )
      .orElse(false);
    ObjectWriter objectWriter = useSnakeCaseMappingOverride
      ? SNAKE_CASE_PYISH_OBJECT_WRITER
      : PYISH_OBJECT_WRITER;
    Writer writer;
    Optional<Long> maxOutputSize = JinjavaInterpreter
      .getCurrentMaybe()
      .map(interpreter -> interpreter.getConfig().getMaxOutputSize())
      .filter(max -> max > 0);
    if (maxOutputSize.isPresent()) {
      AtomicInteger remainingLength = new AtomicInteger(
        (int) Math.min(Integer.MAX_VALUE, maxOutputSize.get())
      );
      objectWriter =
        objectWriter.withAttribute(
          LengthLimitingWriter.REMAINING_LENGTH_ATTRIBUTE,
          remainingLength
        );
      writer = new LengthLimitingWriter(new CharArrayWriter(), remainingLength);
    } else {
      writer = new CharArrayWriter();
    }
    if (!useSnakeCaseMappingOverride) {
      objectWriter = objectWriter.withAttribute(ALLOW_SNAKE_CASE_ATTRIBUTE, !forOutput);
    }
    objectWriter.writeValue(writer, val);

    return writer.toString();
  }

  public static class NullKeySerializer extends ValueSerializer<Object> {

    @Override
    public void serialize(
      Object o,
      JsonGenerator jsonGenerator,
      SerializationContext context
    ) {
      jsonGenerator.writeName("");
    }
  }
}
