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


public class Normalizer {

  private Normalizer() {
  }

  private static final Pattern WEIRD_DOTS = Pattern.compile("…");
  private static final Pattern WEIRD_LEFT_QUOTE = Pattern.compile("‘");
  private static final Pattern WEIRD_RIGTH_QUOTE = Pattern.compile("’");
  private static final Pattern LONG_DASH = Pattern.compile("—");
  //normalize quotes
  public static Pattern RIGHT_QUOTES = Pattern.compile("([\\p{Alnum}\\p{Punct}])[\"]", Pattern.UNICODE_CHARACTER_CLASS);
  public static Pattern LEFT_QUOTES = Pattern.compile("[\"]([\\p{Alnum}])", Pattern.UNICODE_CHARACTER_CLASS);

  public static String convertNonCanonicalStrings(String line) {
    line = WEIRD_DOTS.matcher(line).replaceAll("...");
    line = WEIRD_LEFT_QUOTE.matcher(line).replaceAll("`");
    line = WEIRD_RIGTH_QUOTE.matcher(line).replaceAll("'");
    line = LONG_DASH.matcher(line).replaceAll("--");
    return line;
  }

  public static String normalizeQuotes(String line, String lang) {

    if (lang.equalsIgnoreCase("en")) {
      line = LEFT_QUOTES.matcher(line).replaceAll("`` $1");
      line = RIGHT_QUOTES.matcher(line).replaceAll("$1 ''");
    }
    return line;
  }

}
