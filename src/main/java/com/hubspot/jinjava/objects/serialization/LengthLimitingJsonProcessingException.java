package com.hubspot.jinjava.objects.serialization;

import com.google.common.annotations.Beta;
import tools.jackson.core.JacksonException;

/**
 * Note: as of jinjava 3.0 this extends Jackson 3's {@link JacksonException}, which is a
 * {@code RuntimeException}. It used to extend Jackson 2's {@code JsonProcessingException},
 * an {@code IOException}, so {@code catch (IOException)} no longer catches it.
 */
@Beta
public class LengthLimitingJsonProcessingException extends JacksonException {

  private final int maxSize;
  private final int attemptedSize;

  protected LengthLimitingJsonProcessingException(int maxSize, int attemptedSize) {
    super(
      String.format(
        "Max length of %d chars reached when serializing. %d chars attempted.",
        maxSize,
        attemptedSize
      )
    );
    this.maxSize = maxSize;
    this.attemptedSize = attemptedSize;
  }

  public int getAttemptedSize() {
    return attemptedSize;
  }

  public int getMaxSize() {
    return maxSize;
  }
}
