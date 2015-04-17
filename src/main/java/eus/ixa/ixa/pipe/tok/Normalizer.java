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

import java.util.regex.Pattern;

/**
 * Normalizer class for converting punctuation mostly following various
 * corpora conventions such as Penn TreeBank and Ancora.
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
  public static final Pattern startLeftDoubleQuote = Pattern.compile("^[\u00AB\u0093\u201C\"]");
  public static final Pattern startParaLeftDoubleQuote = Pattern.compile("([\u00B6\u00B6] *)\"");
  public static final Pattern insideLeftDoubleQuote = Pattern.compile("([ \\(\\[\\{])\"");
  public static final Pattern rightDoubleQuote = Pattern.compile("[\u00BB\u0094\u201D]");
  public static final Pattern singleAsciiQuote = Pattern.compile("'|\u0027");
  public static final Pattern doubleAsciiQuote = Pattern.compile("\"");
  public static final String TO_ASCII_SINGLE_QUOTE = "[\u0027\u0091\u0092\u2019\u201A\u201B\u203A\u2018\u2039']";
  public static final Pattern toAsciiSingleQuote = Pattern.compile("TO_ASCII_SINGLE_QUOTE");
  public static final Pattern toAsciiDoubleQuote = Pattern.compile("[\u00AB\u00BB\u0093\u0094\u201C\u201D\u201E\"]");

  /**
   * Convert several strings to their unicode counterparts according
   * to language convention.
   * @param line the string to be normalized
   * @param lang the language
   * @return the normalized string
   */
  public static String convertNonCanonicalStrings(String line, String lang) {
    //System.err.println((int)'’');
    line = apostrophe.matcher(line).replaceAll("'");
    line = ellipsis.matcher(line).replaceAll(THREE_DOTS);
    line = longDash.matcher(line).replaceAll("--");
    if (lang.equalsIgnoreCase("en")) {
      line = oneFourth.matcher(line).replaceAll("1\\\\/4");
      line = oneThird.matcher(line).replaceAll("1\\\\/3");
      line = oneHalf.matcher(line).replaceAll("1\\\\/2");
      line = threeQuarters.matcher(line).replaceAll("3\\\\/4");
      line = sterling.matcher(line).replaceAll("#");
    }
    line = oneFourth.matcher(line).replaceAll("1/4");
    line = oneThird.matcher(line).replaceAll("1/3");
    line = oneHalf.matcher(line).replaceAll("1/2");
    line = twoThirds.matcher(line).replaceAll("2/3");
    line = threeQuarters.matcher(line).replaceAll("3/4");
    line = cents.matcher(line).replaceAll("cents");
    return line;
  }

  /**
   * Normalizing quotes is important for POS and parsing. This
   * function normalizes quotes following corpora conventions for
   * each language.
   * @param line the string to be normalized
   * @param lang the language
   * @return the normalized string
   */
  public static String normalizeQuotes(String line, String lang) {

    if (lang.equalsIgnoreCase("en")) {
      line = leftSingleQuote.matcher(line).replaceAll("`");
      line = rightSingleQuote.matcher(line).replaceAll("'");
      line = leftDoubleQuote.matcher(line).replaceAll("``");
      line = rightDoubleQuote.matcher(line).replaceAll("''");
      //TODO double quotes to latex quotes
      line = startLeftDoubleQuote.matcher(line).replaceAll("``");
      line = startParaLeftDoubleQuote.matcher(line).replaceAll("$1``");
      //line = insideLeftDoubleQuote.matcher(line).replaceAll("$1``");
    } else if (lang.equalsIgnoreCase("de") || lang.equalsIgnoreCase("es") || lang.equalsIgnoreCase("eu")
        || lang.equalsIgnoreCase("fr") || lang.equalsIgnoreCase("gl")
        || lang.equalsIgnoreCase("it") || lang.equalsIgnoreCase("nl")) {
      line = toAsciiSingleQuote.matcher(line).replaceAll("'");
      line = toAsciiDoubleQuote.matcher(line).replaceAll("\"");
    }
    return line;
  }

}
