/*
 *Copyright 2013 Rodrigo Agerri

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

package ixa.pipe.resources;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.HashMap;
import java.util.Map.Entry;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * This class implements exceptions for periods as sentence breakers. It decides
 * when a period induces a new sentence or a new token or when it does not.
 * 
 * @author ragerri
 * 
 */
public class NonPrefixBreaker {

  // Parse nonbreaking_prefix.$lang file for non breaking exceptions

  public static Pattern DOT_SPACE_NUMERIC_ONLY = Pattern
      .compile("(.*)\\s+(\\#NUMERIC_ONLY\\#)");

  // useful patterns existing in Perl not in Java
  // using unicode java code for these characters »’”› and its counterparts
  public static Pattern INITIAL_PUNCT = Pattern
      .compile("[\'\"\\(\\[\\¿\\¡\u00AB\u2018\u201B\u201C\u201F\u2039]");
  public static Pattern FINAL_PUNCT = Pattern
      .compile("[\'\"\\)\\]\\%\u00BB\u2019\u201D\u203A]");

  // //////////////////////////
  // // Segmenter Patterns ////
  // //////////////////////////

  // non-period end of sentence markers (?!) followed by sentence starters.
  public static Pattern NOPERIOD_END = Pattern
      .compile("([?!])\\s+([\'\"\\(\\[\\¿\\¡\u00AB\u2018\u201B\u201C\u201F\u2039]*[\\p{Lu}])");

  // multi-dots followed by sentence starters
  public static Pattern MULTI_DOTS_STARTERS = Pattern
      .compile("(\\.[\\.]+)\\s+([\'\"\\(\\[\\¿\\¡\u00AB\u2018\u201B\u201C\u201F\u2039]*[\\p{Lu}])");

  // some sort of punctuation inside a quote or parenthetical followed
  // by a possible sentence starter punctuation and upper case
  // public static Pattern END_INSIDE_QUOTES = Pattern
  // .compile("([?!\\.][\\ ]*[\'\"\\)\\]\\p{Punct}]+)\\s+([\'\"\\(\\[\\¿\\¡\\p{IsPunct}]*[\\ ]*[\\p{Lu}])");
  public static Pattern END_INSIDE_QUOTES = Pattern
      .compile("([?!\\.][\\ ]*[\'\"\\)\\]\\%\u00BB\u2019\u201D\u203A]+)\\s+([\'\"\\(\\[\\¿\\¡\u00AB\u2018\u201B\u201C\u201F\u2039]*[\\ ]*[\\p{Lu}])");

  // end with some sort of punctuation and followed by a sentence
  // starter punctuation and upper case
  public static Pattern PUNCT_UPPER = Pattern
      .compile("([?!\\.])\\s+([\'\"\\(\\[\\¿\\¡\u00AB\u2018\u201B\u201C\u201F\u2039]+[\\ ]*[\\p{Lu}])");

  // SPECIAL PUNCTUATION CASES COVERED. These patterns check for remaining
  // periods in the nonbreakingprefixes functions

  public static Pattern ALPHANUM_PUNCT = Pattern
      .compile("([\\p{Alnum}\\.\\-]*)([\'\"\\)\\]\\%\u00BB\u2019\u201D\u203A]*)(\\.+)$");

  public static Pattern UPPER_CASE_ACRONYM = Pattern
      .compile("(\\.)[\\p{Lu}\\-]+(\\.+)$");

  public static Pattern START_DIGITS = Pattern.compile("^\\d+");
  public static Pattern QUOTE_SPACE_UPPER_NUMBER = Pattern
      .compile("^( *[\'\"\\(\\[\\¿\\¡\\p{Punct}]* *[\\p{Lu}\\d])");

  // //////////////////////////
  // // Tokenizer Patterns ////
  // //////////////////////////

  public static Pattern MULTI_SPACE = Pattern.compile("\\s+");
  // every control character not "printable"
  public static Pattern ASCII_HEX = Pattern.compile("[^\\x20-\\x7E]");
  public static Pattern SPECIALS = Pattern
      .compile("([^\\p{Alnum}\\s\\.\'\\`\\,\\-\\¿\\?\\¡\\!])");
  // question and exclamation marks (do not separate if multiple)
  public static Pattern QEXC = Pattern.compile("([\\¿\\?\\¡\\!]+)");

  // tokenize dash only when before or after a space
  public static Pattern DASH = Pattern.compile("( \\-|\\- )");

  // multidots
  public static Pattern MULTI_DOTS = Pattern.compile("\\.([\\.]+)");
  public static Pattern DOTMULTI_DOT = Pattern.compile("DOTMULTI\\.");
  public static Pattern DOTMULTI_DOT_ANY = Pattern
      .compile("DOTMULTI\\.([^\\.])");

  // commas and digits
  public static Pattern NODIGIT_COMMA_NODIGIT = Pattern
      .compile("([^\\d])[,]([^\\d])");
  // separate "," pre and post number
  public static Pattern DIGIT_COMMA_NODIGIT = Pattern
      .compile("([\\d])[,]([^\\d])");
  public static Pattern NODIGIT_COMMA_DIGIT = Pattern
      .compile("([^\\d])[,](\\d)");

  // SPECIAL CASES COVERED; LANGUAGE SPECIFIC RULES USING NON BREAKING
  // PREFIXES FILES
  public static Pattern WORD_DOT = Pattern.compile("^(\\S+)\\.$");
  public static Pattern LOWER = Pattern.compile("^\\p{Lower}");

  // links
  public static Pattern LINK = Pattern
      .compile("http\\s:\\s/\\s/\\s[\\s-A-Za-z0-9+&@#/%?=~_()|!:,.;]*[-A-Za-z0-9+&@#/%=~_(|]");

  // english contractions patterns
  public static Pattern NOALPHA_APOS_NOALPHA = Pattern
      .compile("([^\\p{Alpha}])[']([^\\p{Alpha}])");
  public static Pattern NOALPHA_DIGIT_APOS_ALPHA = Pattern
      .compile("([^\\p{Alpha}\\d])['](\\p{Alpha})");
  public static Pattern ALPHA_APOS_NOALPHA = Pattern
      .compile("([\\p{Alpha}])[']([^\\p{Alpha}])");
  public static Pattern ALPHA_APOS_ALPHA = Pattern
      .compile("([\\p{Alpha}])[']([\\p{Alpha}])");
  // special case for "1990's"
  public static Pattern YEAR_APOS = Pattern.compile("([\\d])[']([s])");

  private HashMap<String, String> dictMap;

  /**
   * 
   * This constructor reads nonbreaking-prefix.lang files in resources and
   * assigns "1" as value when the word does not create a break (Dr.) and "2"
   * when the word does not create a break if followed by a number (No. 1)
   * 
   * @param dictionary
   */
  public NonPrefixBreaker(InputStream dictionary) {
    dictMap = new HashMap<String, String>();
    BufferedReader breader = new BufferedReader(new InputStreamReader(
        dictionary));
    String line;
    try {
      while ((line = breader.readLine()) != null) {
        line = line.trim();
        if (!line.startsWith("#")) {
          Matcher numonly = DOT_SPACE_NUMERIC_ONLY.matcher(line);
          if (numonly.matches()) {
            String pre = numonly.replaceAll("$1");
            dictMap.put(pre, "2");
          } else {
            dictMap.put(line, "1");
          }
        }
      }
    } catch (IOException e) {
      e.printStackTrace();
    }
  }

  /**
   * This function implements exceptions for periods as sentence breakers. It
   * decides when a period induces a new sentence or not.
   * 
   * @param paragraph
   * @return segmented text (with newlines included)
   */
  public String SegmenterNonBreaker(String line) {

    StringBuilder sb = new StringBuilder();
    String segmentedText = "";
    int i;
    String[] words = line.split(" ");

    for (i = 0; i < (words.length - 1); i++) {

      Matcher finalPunct = FINAL_PUNCT.matcher(words[i]);
      Matcher alphanum = ALPHANUM_PUNCT.matcher(words[i]);
      Matcher upperAcro = UPPER_CASE_ACRONYM.matcher(words[i]);
      Matcher upper = QUOTE_SPACE_UPPER_NUMBER.matcher(words[i + 1]);
      Matcher startDigits = START_DIGITS.matcher(words[i + 1]);

      if (alphanum.find()) {
        String prefix = alphanum.replaceAll("$1");
        if (words[i].contains(prefix) && dictMap.containsKey(prefix)
            && (dictMap.get(prefix) == "1") && !finalPunct.find()) {
          // not breaking
        }

        else if (upperAcro.find()) {
          // non-breaking, upper case acronym
        }

        // the next word has a bunch of initial quotes, maybe a space,
        // then either upper case or a number
        else if (upper.find()) {

          // literal implementation from unless in perl:
          if (!(words[i].contains(prefix) && dictMap.containsKey(prefix)
              && (dictMap.get(prefix) == "2") && !finalPunct.find() && startDigits
                .find())) {
            words[i] = words[i] + "\n";
          }
          // equivalent if-then applying De Morgan theorem:
          /*
           * if (!words[i].contains(prefix) || !dictMap.containsKey(prefix) ||
           * (dictMap.get(prefix) != "2") || finalPunct.find() ||
           * !startDigits.find()) { words[i] = words[i] + "\n"; }
           */

          // we always add a return for these unless we have a numeric
          // non-breaker and a number start
        }
      }
      sb.append(words[i]).append(" ");
      segmentedText = sb.toString();
    }
    // add last index of words array removed for easy look ahead
    segmentedText = segmentedText + words[i];
    return segmentedText;
  }

  public String TokenizerNonBreaker(String line) {
    StringBuilder sb = new StringBuilder();
    String segmentedText = "";
    int i;
    String[] words = line.split(" ");

    for (i = 0; i < words.length; i++) {
      Matcher wordDot = WORD_DOT.matcher(words[i]);

      if (wordDot.find()) {

        String prefix = wordDot.replaceAll("$1");

        if ((prefix.contains(".") && prefix.matches("\\p{Alpha}+"))
            || (dictMap.containsKey(prefix) && dictMap.get(prefix) == "1")
            || (i < (words.length - 1) && LOWER.matcher(words[i + 1]).find())) {
          // do not tokenize
        } else if ((dictMap.containsKey(prefix) && dictMap.get(prefix) == "2")
            && (i < (words.length - 1) && START_DIGITS.matcher(words[i + 1])
                .find())) {
          // do not tokenize
        } else {
          words[i] = prefix + " .";
        }
      }
      sb.append(words[i]).append(" ");
      segmentedText = sb.toString();
    }
    return segmentedText;
  }

}
