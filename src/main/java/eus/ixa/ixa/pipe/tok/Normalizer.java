/*
 * Copyright 2015 Rodrigo Agerri

 Licensed under the Apache License, Version 2.0 (the "License");
 you may not use this file except in compliance with the License.
 You may obtain a copy of the License at

 http://www.apache.org/licenses/LICENSE-2.0

 Unless required by applicable law or agreed to in writing, software
 distributed under the License is distributed on an "AS IS" BASIS,
 WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 See the License for the specific language governing permissions and
 limitations under the License.
 */

package eus.ixa.ixa.pipe.tok;

import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Normalizer class for converting punctuation mostly following various corpora
 * conventions such as Penn TreeBank, Ancora, Tutpenn, Tiger and CTAG.
 * @author ragerri
 * @version 2015-04-13
 */
public class Normalizer {

  /**
   * This class is to be used statically.
   */
  private Normalizer() {
  }

  public static final String THREE_DOTS = "...";
  public static final Pattern ellipsis = Pattern.compile("[\u2026\u8230]");
  public static final Pattern longDash = Pattern.compile("–|[\u2014\u8212]");

  public static final Pattern oneFourth = Pattern.compile("\u00BC");
  public static final Pattern oneThird = Pattern.compile("\u2153");
  public static final Pattern oneHalf = Pattern.compile("\u00BD");
  public static final Pattern twoThirds = Pattern.compile("\u2154");
  public static final Pattern threeQuarters = Pattern.compile("\u00BE");

  private static final Pattern cents = Pattern.compile("\u00A2");
  private static final Pattern sterling = Pattern.compile("\u00A3");

  public static final Pattern apostrophe = Pattern.compile("['\u0027\u0092\u2019]");
  public static final Pattern leftSingleQuote = Pattern.compile("[\u0091\u201B\u2018\u2039]");
  public static final Pattern rightSingleQuote = Pattern.compile("[\u0027\u0092\u203A\u2019]");
  public static final Pattern leftDoubleQuote = Pattern.compile("[\u00AB\u0093\u201C]");
  public static final Pattern rightDoubleQuote = Pattern.compile("[\u00BB\u0094\u201D]");
  public static final Pattern singleAsciiQuote = Pattern.compile("'|\u0027");
  public static final Pattern invertSingleAsciiQuote = Pattern.compile("([\\p{Alpha}])([^\\p{Space}])", Pattern.UNICODE_CHARACTER_CLASS);
  public static final Pattern doubleAsciiQuote = Pattern.compile("\"");
  public static final Pattern doubleAsciiQuoteAlphaNumeric = Pattern.compile("([\\p{Alpha}\\p{Digit}$])", Pattern.UNICODE_CHARACTER_CLASS);

  public static final String TO_ASCII_SINGLE_QUOTE = "[\u0027\u0091\u0092\u2019\u201A\u201B\u203A\u2018\u2039]";
  public static final Pattern toAsciiSingleQuote = Pattern.compile("[\u0027\u0091\u0092\u2019\u201A\u201B\u203A\u2018\u2039]");
  public static final Pattern toAsciiDoubleQuote = Pattern.compile("[\u00AB\u00BB\u0093\u0094\u201C\u201D\u201E\"]");

  /**
   * Converts non-unicode and other strings into their unicode
   * counterparts.
   * @param sentence the list of tokens
   * @param lang the language
   */
  public static void convertNonCanonicalStrings(final List<Token> sentence,
      final String lang) {
    // System.err.println((int)'’');
    for (final Token token : sentence) {
      token.setTokenValue(apostrophe.matcher(token.getTokenValue()).replaceAll(
          "'"));
      token.setTokenValue(ellipsis.matcher(token.getTokenValue()).replaceAll(
          THREE_DOTS));
      token.setTokenValue(longDash.matcher(token.getTokenValue()).replaceAll(
          "--"));
      if (lang.equalsIgnoreCase("en")) {
        token.setTokenValue(oneFourth.matcher(token.getTokenValue())
            .replaceAll("1\\\\/4"));
        token.setTokenValue(oneThird.matcher(token.getTokenValue()).replaceAll(
            "1\\\\/3"));
        token.setTokenValue(oneHalf.matcher(token.getTokenValue()).replaceAll(
            "1\\\\/2"));
        token.setTokenValue(threeQuarters.matcher(token.getTokenValue())
            .replaceAll("3\\\\/4"));
        token.setTokenValue(sterling.matcher(token.getTokenValue()).replaceAll(
            "#"));
      }
      token.setTokenValue(oneFourth.matcher(token.getTokenValue()).replaceAll(
          "1/4"));
      token.setTokenValue(oneThird.matcher(token.getTokenValue()).replaceAll(
          "1/3"));
      token.setTokenValue(oneHalf.matcher(token.getTokenValue()).replaceAll(
          "1/2"));
      token.setTokenValue(twoThirds.matcher(token.getTokenValue()).replaceAll(
          "2/3"));
      token.setTokenValue(threeQuarters.matcher(token.getTokenValue())
          .replaceAll("3/4"));
      token.setTokenValue(cents.matcher(token.getTokenValue()).replaceAll(
          "cents"));
    }
  }

  /**
   * Normalizes non-ambiguous quotes according to language and corpus.
   * @param sentence the list of tokens
   * @param lang the language
   */
  public static void normalizeQuotes(final List<Token> sentence,
      final String lang) {

    for (final Token token : sentence) {
      if (lang.equalsIgnoreCase("en")) {
        token.setTokenValue(leftSingleQuote.matcher(token.getTokenValue())
            .replaceAll("`"));
        token.setTokenValue(rightSingleQuote.matcher(token.getTokenValue())
            .replaceAll("'"));
        token.setTokenValue(leftDoubleQuote.matcher(token.getTokenValue())
            .replaceAll("``"));
        token.setTokenValue(rightDoubleQuote.matcher(token.getTokenValue())
            .replaceAll("''"));
      } else if (lang.equalsIgnoreCase("de") || lang.equalsIgnoreCase("es")
          || lang.equalsIgnoreCase("eu") || lang.equalsIgnoreCase("fr")
          || lang.equalsIgnoreCase("gl") || lang.equalsIgnoreCase("it")
          || lang.equalsIgnoreCase("nl")) {
        token.setTokenValue(toAsciiSingleQuote.matcher(token.getTokenValue())
            .replaceAll("'"));
        token.setTokenValue(toAsciiDoubleQuote.matcher(token.getTokenValue())
            .replaceAll("\""));
      }
    }
  }

  /**
   * Normalizes double and ambiguous quotes according to language
   * and corpus.
   * @param sentence the list of tokens
   * @param lang the language
   */
  public static void normalizeDoubleQuotes(final List<Token> sentence,
      final String lang) {

    boolean isLeft = true;
    for (int i = 0; i < sentence.size(); i++) {
      if (lang.equalsIgnoreCase("en")) {
        final Matcher doubleAsciiQuoteMatcher = doubleAsciiQuote
            .matcher(sentence.get(i).getTokenValue());
        final Matcher singleAsciiQuoteMatcher = singleAsciiQuote
            .matcher(sentence.get(i).getTokenValue());
        // if current token is "
        if (doubleAsciiQuoteMatcher.find()) {
          if (isLeft
              && i < sentence.size() - 1
              && doubleAsciiQuoteAlphaNumeric.matcher(
                  sentence.get(i + 1).getTokenValue()).find()) {
            sentence.get(i).setTokenValue("``");
            isLeft = false;
          } else if (!isLeft) {
            sentence.get(i).setTokenValue("''");
            isLeft = true;
          }
        } else if (singleAsciiQuoteMatcher.find()) {
          if (i < sentence.size() - 2
              && sentence.get(i + 1).getTokenValue().matches("[A-Za-z]")
              && sentence.get(i + 2).getTokenValue()
                  .matches("[^ \t\n\r\u00A0\u00B6]")) {
            sentence.get(i).setTokenValue("`");
          }
        }
      }
    }
  }

}
