package com.hubspot.jinjava.interpret;

import static org.assertj.core.api.Assertions.assertThat;

import com.google.common.collect.ImmutableMap;
import org.junit.Test;
import tools.jackson.databind.ObjectMapper;

public class NullValueTest {

  @Test
  public void itSerializesUnderlyingValue() {
    LazyExpression expression = LazyExpression.of(
      () -> ImmutableMap.of("test", "hello", "test2", NullValue.instance()),
      "{}"
    );
    Object evaluated = expression.get();
    assertThat(evaluated).isNotNull();
    assertThat(new ObjectMapper().writeValueAsString(expression))
      .isEqualTo("{\"test\":\"hello\",\"test2\":null}");
  }
}
