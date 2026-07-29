package com.hubspot.jinjava.lib.filter;

import static com.hubspot.jinjava.lib.filter.ReplaceFilter.checkLength;

import com.google.re2j.Matcher;
import com.google.re2j.Pattern;
import com.google.re2j.PatternSyntaxException;
import com.hubspot.jinjava.doc.annotations.JinjavaDoc;
import com.hubspot.jinjava.doc.annotations.JinjavaParam;
import com.hubspot.jinjava.doc.annotations.JinjavaSnippet;
import com.hubspot.jinjava.interpret.InvalidArgumentException;
import com.hubspot.jinjava.interpret.InvalidInputException;
import com.hubspot.jinjava.interpret.InvalidReason;
import com.hubspot.jinjava.interpret.JinjavaInterpreter;
import com.hubspot.jinjava.interpret.TemplateSyntaxException;
import com.hubspot.jinjava.objects.SafeString;

@JinjavaDoc(
  value = "Returns true if the value matches the given regular expression (Java RE2 syntax), " +
  "false otherwise. Uses partial matching (the pattern can match anywhere in the string). " +
  "Anchor with ^...$ for full-string matching.",
  input = @JinjavaParam(
    value = "s",
    desc = "Base string to test against",
    required = true
  ),
  params = {
    @JinjavaParam(
      value = "regex",
      desc = "The regular expression to match against the string",
      required = true
    ),
  },
  snippets = {
    @JinjavaSnippet(
      code = "{% if \"It costs $300\"|matches_regex(\"[0-9]+\") %}\n" +
      "    Contains a number\n" +
      "{% endif %}"
    ),
    @JinjavaSnippet(
      desc = "Anchor the pattern to match the entire string",
      code = "{% if \"hello\"|matches_regex(\"^[a-z]+$\") %}\n" +
      "    All lowercase letters\n" +
      "{% endif %}"
    ),
  }
)
public class MatchesRegexFilter implements Filter {

  @Override
  public String getName() {
    return "matches_regex";
  }

  @Override
  public Object filter(Object var, JinjavaInterpreter interpreter, String... args) {
    if (args.length < 1) {
      throw new TemplateSyntaxException(
        interpreter,
        getName(),
        "requires 1 argument (regex string)"
      );
    }

    if (args[0] == null) {
      throw new TemplateSyntaxException(
        interpreter,
        getName(),
        "requires a valid regex param (not null)"
      );
    }

    if (var == null) {
      return false;
    }

    String s;
    if (var instanceof String) {
      s = (String) var;
    } else if (var instanceof SafeString) {
      s = ((SafeString) var).getValue();
    } else {
      throw new InvalidInputException(interpreter, this, InvalidReason.STRING);
    }

    // Minor optimization, avoid checking short strings
    if (s.length() > 100) {
      checkLength(interpreter, s, this);
    }

    String regex = args[0];

    try {
      Pattern p = Pattern.compile(regex);
      Matcher matcher = p.matcher(s);

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
