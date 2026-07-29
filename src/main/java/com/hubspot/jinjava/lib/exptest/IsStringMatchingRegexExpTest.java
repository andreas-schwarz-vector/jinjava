package com.hubspot.jinjava.lib.exptest;

import com.google.re2j.Matcher;
import com.google.re2j.Pattern;
import com.google.re2j.PatternSyntaxException;
import com.hubspot.jinjava.doc.annotations.JinjavaDoc;
import com.hubspot.jinjava.doc.annotations.JinjavaParam;
import com.hubspot.jinjava.doc.annotations.JinjavaSnippet;
import com.hubspot.jinjava.interpret.InvalidArgumentException;
import com.hubspot.jinjava.interpret.InvalidReason;
import com.hubspot.jinjava.interpret.JinjavaInterpreter;
import com.hubspot.jinjava.interpret.TemplateSyntaxException;

@JinjavaDoc(
  value = "Return true if object is a string which matches a specified regular expression " +
  "(Java RE2 syntax). Uses partial matching (the pattern can match anywhere in the string). " +
  "Anchor with ^...$ for full-string matching.",
  input = @JinjavaParam(value = "string", type = "string", required = true),
  params = @JinjavaParam(
    value = "regex",
    type = "string",
    desc = "The regular expression to match against the string",
    required = true
  ),
  snippets = {
    @JinjavaSnippet(
      code = "{% if variable is string_matching_regex '[0-9]+' %}\n" +
      "      <!--code to render if variable matches regex -->\n" +
      "{% endif %}"
    ),
    @JinjavaSnippet(
      desc = "Use with selectattr to filter a list by regex",
      code = "{{ items|selectattr('name', 'string_matching_regex', '^foo') }}"
    ),
  }
)
public class IsStringMatchingRegexExpTest extends IsStringExpTest {

  @Override
  public String getName() {
    return super.getName() + "_matching_regex";
  }

  @Override
  public boolean evaluate(Object var, JinjavaInterpreter interpreter, Object... args) {
    if (!super.evaluate(var, interpreter, args)) {
      return false;
    }

    if (args.length == 0) {
      throw new TemplateSyntaxException(
        interpreter,
        getName(),
        "requires 1 argument (regex string)"
      );
    }

    if (args[0] == null) {
      return false;
    }

    String regex = args[0].toString();

    try {
      Pattern p = Pattern.compile(regex);
      Matcher matcher = p.matcher((String) var);

      return matcher.find();
    } catch (PatternSyntaxException e) {
      throw new InvalidArgumentException(
        interpreter,
        this,
        InvalidReason.REGEX,
        0,
        regex
      );
    }
  }
}
