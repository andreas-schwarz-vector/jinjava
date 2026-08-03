package com.hubspot.jinjava.lib.filter;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import com.hubspot.jinjava.BaseInterpretingTest;
import com.hubspot.jinjava.Jinjava;
import com.hubspot.jinjava.JinjavaConfig;
import com.hubspot.jinjava.interpret.InvalidArgumentException;
import com.hubspot.jinjava.interpret.InvalidInputException;
import com.hubspot.jinjava.objects.SafeString;
import org.junit.Before;
import org.junit.Test;

public class RegexMatchFilterTest extends BaseInterpretingTest {

  RegexMatchFilter filter;

  @Before
  public void setup() {
    filter = new RegexMatchFilter();
  }

  @Test
  public void expects1Arg() {
    assertThatThrownBy(() -> filter.filter("foo", interpreter))
      .hasMessageContaining("requires 1 argument");
  }

  @Test
  public void expectsNotNullArg() {
    assertThatThrownBy(() -> filter.filter("foo", interpreter, new String[] { null }))
      .hasMessageContaining("a valid regex");
  }

  @Test
  public void itReturnsFalseOnNullInput() {
    assertThat(filter.filter(null, interpreter, "foo")).isEqualTo(false);
  }

  @Test
  public void itMatchesRegex() {
    assertThat(filter.filter("It costs $300", interpreter, "[0-9]+")).isEqualTo(true);
  }

  @Test
  public void itDoesNotMatchRegex() {
    assertThat(filter.filter("hello world", interpreter, "[0-9]+")).isEqualTo(false);
  }

  @Test
  public void itMatchesAnchoredRegex() {
    assertThat(filter.filter("hello", interpreter, "^[a-z]+$")).isEqualTo(true);
  }

  @Test
  public void itDoesNotMatchAnchoredRegex() {
    assertThat(filter.filter("hello123", interpreter, "^[a-z]+$")).isEqualTo(false);
  }

  @Test(expected = InvalidArgumentException.class)
  public void itThrowsExceptionOnInvalidRegex() {
    filter.filter("It costs $300", interpreter, "[");
  }

  @Test
  public void itMatchesRegexForSafeString() {
    assertThat(filter.filter(new SafeString("It costs $300"), interpreter, "[0-9]+"))
      .isEqualTo(true);
  }

  @Test
  public void itLimitsLongInput() {
    assertThatThrownBy(() ->
        filter.filter(
          "a".repeat(101),
          new Jinjava(JinjavaConfig.newBuilder().withMaxStringLength(10).build())
            .newInterpreter(),
          "O"
        )
      )
      .isInstanceOf(InvalidInputException.class)
      .hasMessageContaining(
        "Invalid input for 'regex_match': input with length '101' exceeds maximum allowed length of '10'"
      );
  }
}
