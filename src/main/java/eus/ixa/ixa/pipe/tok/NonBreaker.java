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

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.HashMap;
import java.util.Map;
import java.util.Properties;
import java.util.concurrent.ConcurrentHashMap;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * This class implements exceptions for periods as sentence breakers and tokens.
 * It decides when a period induces a new sentence or a new token and when it
 * does not.
 * 
 * @author ragerri
 * 
 */
public class NonBreaker {

  // Parse nonbreaker file for non breaking exceptions
  
  public static Pattern dotSpaceNumericOnly = Pattern
      .compile("(.*)\\s+(\\#NUMERIC_ONLY\\#)");

  /**
   * Check initial punctuation in unicode.
   */
  public static Pattern initialPunct = Pattern
      .compile("[\'\"\\(\\[\\¿\\¡\u00AB\u003C\u0091\u0093\u201B\u201C\u201F\u2018\u2039&apos;&quot;]");
  /**
   * Final punctutation in unicode.
   */
  public static Pattern finalPunct = Pattern
      .compile("[\'\"\\)\\]\\%\u00BB\u003D\u0092\u0094\u201D\u203A\u2019&apos;&quot;]");

  public static Pattern alphaNumPunct = Pattern
      .compile("([\\p{Alnum}\\.\\-]*)([\'\"\\)\\]\\%\u00BB\u003D\u0092\u0094\u201D\u203A\u2019]*)(\\.+)$", Pattern.UNICODE_CHARACTER_CLASS);

  public static Pattern upperCaseAcronym = Pattern
      .compile("(\\.)[\\p{Lu}\\-]+(\\.+)$", Pattern.UNICODE_CHARACTER_CLASS);

  public static Pattern startDigits = Pattern.compile("^\\d+", Pattern.UNICODE_CHARACTER_CLASS);
  public static Pattern quoteSpaceUpperNumber = Pattern
      .compile("^( *[\'\"\\(\\[\\¿\\¡\\p{Punct}]* *[\\p{Lu}\\d])", Pattern.UNICODE_CHARACTER_CLASS);

  // SPECIAL CASES COVERED; LANGUAGE SPECIFIC RULES USING NON BREAKING
  // PREFIXES FILES
  public static Pattern WORD_DOT = Pattern.compile("^(\\S+)\\.$");
  public static Pattern LOWER = Pattern.compile("^\\p{Lower}", Pattern.UNICODE_CHARACTER_CLASS);
  
  /**
   * The nonBreakerFile to use for each language. The keys of the hash are the
   * language codes, the values the nonBreakerMap.
   */
  private static ConcurrentHashMap<String, Map<String, String>> nonBreakers =
      new ConcurrentHashMap<String, Map<String, String>>();
  
  private Map<String, String> nonBreakerMap;

  /**
   * 
   * This constructor reads nonbreaking-prefix.lang files in resources and
   * assigns "1" as value when the word does not create a break (Dr.) and "2"
   * when the word does not create a break if followed by a number (No. 1)
   * 
   * @param dictionary
   */
  public NonBreaker(Properties properties) {
    nonBreakerMap = loadNonBreaker(properties);
  }
  
  private Map<String, String> loadNonBreaker(Properties properties) {
    String lang = properties.getProperty("language");
    nonBreakers.putIfAbsent(lang, createNonBreaker(lang));
    return nonBreakers.get(lang);
  }
  
  private Map<String, String> createNonBreaker(String lang) {
    InputStream nonBreakerInputStream = getNonBreakerInputStream(lang);
    if (nonBreakerInputStream == null) {
      System.err.println("ERROR: Not nonbreaker file for language " + lang + " in src/main/resources!!");
      System.exit(1);
    }
    nonBreakerMap = new HashMap<String, String>();
    BufferedReader breader = new BufferedReader(new InputStreamReader(
        nonBreakerInputStream));
    String line;
    try {
      while ((line = breader.readLine()) != null) {
        line = line.trim();
        if (!line.startsWith("#")) {
          Matcher numonly = dotSpaceNumericOnly.matcher(line);
          if (numonly.matches()) {
            String pre = numonly.replaceAll("$1");
            nonBreakerMap.put(pre, "2");
          } else {
            nonBreakerMap.put(line, "1");
          }
        }
      }
    } catch (IOException e) {
      e.printStackTrace();
    }
    return nonBreakerMap;
  }
  
  private final InputStream getNonBreakerInputStream(String lang) {
    InputStream nonBreakerInputStream = null;
    if (lang.equalsIgnoreCase("de")) {
      nonBreakerInputStream = getClass().getResourceAsStream("/de-nonbreaker.txt");
    } else if (lang.equalsIgnoreCase("en")) {
      nonBreakerInputStream = getClass().getResourceAsStream("/en-nonbreaker.txt");
    } else if (lang.equalsIgnoreCase("es")) {
      nonBreakerInputStream = getClass().getResourceAsStream("/es-nonbreaker.txt");
    } else if (lang.equalsIgnoreCase("eu")) {
      nonBreakerInputStream = getClass().getResourceAsStream("/eu-nonbreaker.txt");
    } else if (lang.equalsIgnoreCase("fr")) {
      nonBreakerInputStream = getClass().getResourceAsStream("/fr-nonbreaker.txt");
    } else if (lang.equalsIgnoreCase("gl")) {
      nonBreakerInputStream = getClass().getResourceAsStream("/gl-nonbreaker.txt");
    } else if (lang.equalsIgnoreCase("it")) {
      nonBreakerInputStream = getClass().getResourceAsStream("/it-nonbreaker.txt");
    } else if (lang.equalsIgnoreCase("nl")) {
      nonBreakerInputStream = getClass().getResourceAsStream("/nl-nonbreaker.txt");
    }
    return nonBreakerInputStream;
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

      Matcher finalPunctMatcher = finalPunct.matcher(words[i]);
      Matcher alphanumPunctMatcher = alphaNumPunct.matcher(words[i]);
      Matcher upperAcro = upperCaseAcronym.matcher(words[i]);
      Matcher upper = quoteSpaceUpperNumber.matcher(words[i + 1]);
      Matcher startDigitsMatcher = startDigits.matcher(words[i + 1]);

      if (alphanumPunctMatcher.find()) {
        String prefix = alphanumPunctMatcher.replaceAll("$1");
        if (words[i].contains(prefix) && nonBreakerMap.containsKey(prefix)
            && (nonBreakerMap.get(prefix) == "1") && !finalPunctMatcher.find()) {
          // not breaking
        }

        else if (upperAcro.find()) {
          // non-breaking, upper case acronym
        }

        // the next word has a bunch of initial quotes, maybe a space,
        // then either upper case or a number
        else if (upper.find()) {

          // literal implementation from unless in perl:
          if (!(words[i].contains(prefix) && nonBreakerMap.containsKey(prefix)
              && (nonBreakerMap.get(prefix) == "2") && !finalPunctMatcher.find() && startDigitsMatcher
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

  /**
   * It decides when periods do not need to be tokenized
   * 
 * @param line
 * @return line
 */
public String TokenizerNonBreaker(String line) {
    StringBuilder sb = new StringBuilder();
    String segmentedText = "";
    int i;
    String[] words = line.split(" ");

    for (i = 0; i < words.length; i++) {
      Matcher wordDot = WORD_DOT.matcher(words[i]);

      // find anything non-whitespace finishing with a period
      if (wordDot.find()) {

        String prefix = wordDot.replaceAll("$1");

        if ((prefix.contains(".") && prefix.matches("\\p{Alpha}+"))
            || (nonBreakerMap.containsKey(prefix) && nonBreakerMap.get(prefix) == "1")
            || (i < (words.length - 1) && LOWER.matcher(words[i + 1]).find())) {
          // do not tokenize
        } else if ((nonBreakerMap.containsKey(prefix) && nonBreakerMap.get(prefix) == "2")
            && (i < (words.length - 1) && startDigits.matcher(words[i + 1])
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
