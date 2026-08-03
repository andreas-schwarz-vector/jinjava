package com.hubspot.jinjava.lib.exptest;

import static org.assertj.core.api.Assertions.assertThat;

import com.google.common.collect.ImmutableMap;
import com.hubspot.jinjava.BaseJinjavaTest;
import com.hubspot.jinjava.objects.SafeString;
import java.util.Arrays;
import org.junit.Test;

public class IsStringSearchExpTestTest extends BaseJinjavaTest {

  private static final String MATCHING_TEMPLATE = "{{ var is search arg }}";

  @Test
  public void itReturnsTrueForMatchingRegex() {
    assertThat(
      jinjava.render(
        MATCHING_TEMPLATE,
        ImmutableMap.of("var", "It costs $300", "arg", "[0-9]+")
      )
    )
      .isEqualTo("true");
  }

  @Test
  public void itReturnsFalseForNonMatchingRegex() {
    assertThat(
      jinjava.render(
        MATCHING_TEMPLATE,
        ImmutableMap.of("var", "hello world", "arg", "[0-9]+")
      )
    )
      .isEqualTo("false");
  }

  @Test
  public void itReturnsFalseForNull() {
    assertThat(jinjava.render(MATCHING_TEMPLATE, ImmutableMap.of("var", "testing")))
      .isEqualTo("false");
  }

  @Test
  public void itWorksForSafeString() {
    assertThat(
      jinjava.render(
        MATCHING_TEMPLATE,
        ImmutableMap.of("var", "testing", "arg", new SafeString("^test"))
      )
    )
      .isEqualTo("true");
  }

  @Test
  public void itWorksWithSelectattr() {
    String template =
      "{% for item in items|selectattr('name', 'search', '^foo') %}{{ item.name }},{% endfor %}";
    assertThat(
      jinjava.render(
        template,
        ImmutableMap.of(
          "items",
          Arrays.asList(
            ImmutableMap.of("name", "foobar"),
            ImmutableMap.of("name", "baz"),
            ImmutableMap.of("name", "foobaz")
          )
        )
      )
    )
      .isEqualTo("foobar,foobaz,");
  }
}
