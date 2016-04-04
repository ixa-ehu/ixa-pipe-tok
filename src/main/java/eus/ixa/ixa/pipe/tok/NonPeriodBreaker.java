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
import java.util.ArrayList;
import java.util.List;
import java.util.Properties;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import eus.ixa.ixa.pipe.seg.RuleBasedSegmenter;

/**
 * This class implements exceptions for periods as sentence breakers and tokens.
 * It decides when a period induces a new sentence or a new token and when it
 * does not.
 * 
 * @author ragerri
 * @version 2015-04-04
 */
public class NonPeriodBreaker {

  /**
   * Non segmented words, candidates for sentence breaking.
   */
  public static Pattern nonSegmentedWords = Pattern.compile("([\\p{Alnum}\\.\\-]*)(" +  RuleBasedSegmenter.FINAL_PUNCT + "*)(\\.+)$", Pattern.UNICODE_CHARACTER_CLASS);
  /**
   * Next word wrt to the candidate to indicate sentence breaker.
   */
  public static Pattern nextCandidateWord = Pattern.compile("([\\ ]*" + RuleBasedSegmenter.INITIAL_PUNCT + "*[\\ ]*[\\p{Lu}\\p{Digit}])", Pattern.UNICODE_CHARACTER_CLASS);
  /**
   * Do not split dot after these words if followed by number.
   */
  public static String NON_BREAKER_DIGITS = "(al|[Aa]rt|ca|figs?|[Nn]os?|[Nn]rs?|op|p|pp|[Pp]ág)";
  /**
   * General acronyms.
   */
  public static Pattern acronym = Pattern.compile("(\\.)[\\p{Lu}\\-]+([\\.]+)$", Pattern.UNICODE_CHARACTER_CLASS);
  /**
   * Do not segment numbers like 11.1.
   */
  public static Pattern numbers = Pattern.compile("(\\p{Digit}+[\\.])[\\ ]*(\\p{Digit}+)", Pattern.UNICODE_CHARACTER_CLASS);
  /**
   * Any non white space followed by a period.
   */
  public static Pattern wordDot = Pattern.compile("^(\\S+)\\.$");
  /**
   * Any alphabetic character.
   */
  public static Pattern alphabetic = Pattern.compile("\\p{Alpha}", Pattern.UNICODE_CHARACTER_CLASS);
  /**
   * Starts with a lowercase.
   */
  public static Pattern startLower = Pattern.compile("^\\p{Lower}+", Pattern.UNICODE_CHARACTER_CLASS);
  /**
   * Starts with punctuation that is not beginning of sentence marker.
   */
  public static Pattern startPunct = Pattern.compile("^[\\!#\\$%&\\(\\)\\*\\+,-\\/:;=>\\?@\\[\\\\\\]\\^\\{\\|\\}~]");
  /**
   * Starts with a digit.
   */
  public static Pattern startDigit = Pattern.compile("^\\p{Digit}+", Pattern.UNICODE_CHARACTER_CLASS);
  /**
   * Non breaker prefix read from the files in resources.
   */
  private String NON_BREAKER = null;

  /**
   * 
   * This constructor reads some non breaking prefixes files in resources to
   * create exceptions of segmentation and tokenization.
   * 
   * @param properties
   *          the options
   */
  public NonPeriodBreaker(final Properties properties) {
    loadNonBreaker(properties);
  }

  private void loadNonBreaker(final Properties properties) {
    final String lang = properties.getProperty("language");
    if (NON_BREAKER == null) {
      createNonBreaker(lang);
    }
  }

  private void createNonBreaker(final String lang) {
    final List<String> nonBreakerList = new ArrayList<String>();

    final InputStream nonBreakerInputStream = getNonBreakerInputStream(lang);
    if (nonBreakerInputStream == null) {
      System.err.println("ERROR: Not nonbreaker file for language " + lang
          + " in src/main/resources!!");
      System.exit(1);
    }
    final BufferedReader breader = new BufferedReader(new InputStreamReader(
        nonBreakerInputStream));
    String line;
    try {
      while ((line = breader.readLine()) != null) {
        line = line.trim();
        if (!line.startsWith("#")) {
          nonBreakerList.add(line);
        }
      }
    } catch (final IOException e) {
      e.printStackTrace();
    }
    NON_BREAKER = StringUtils.createDisjunctRegexFromList(nonBreakerList);
  }

  private final InputStream getNonBreakerInputStream(final String lang) {
    InputStream nonBreakerInputStream = null;
    if (lang.equalsIgnoreCase("de")) {
      nonBreakerInputStream = getClass().getResourceAsStream(
          "/de-nonbreaker.txt");
    } else if (lang.equalsIgnoreCase("en")) {
      nonBreakerInputStream = getClass().getResourceAsStream(
          "/en-nonbreaker.txt");
    } else if (lang.equalsIgnoreCase("es")) {
      nonBreakerInputStream = getClass().getResourceAsStream(
          "/es-nonbreaker.txt");
    } else if (lang.equalsIgnoreCase("eu")) {
      nonBreakerInputStream = getClass().getResourceAsStream(
          "/eu-nonbreaker.txt");
    } else if (lang.equalsIgnoreCase("fr")) {
      nonBreakerInputStream = getClass().getResourceAsStream(
          "/fr-nonbreaker.txt");
    } else if (lang.equalsIgnoreCase("gl")) {
      nonBreakerInputStream = getClass().getResourceAsStream(
          "/gl-nonbreaker.txt");
    } else if (lang.equalsIgnoreCase("it")) {
      nonBreakerInputStream = getClass().getResourceAsStream(
          "/it-nonbreaker.txt");
    } else if (lang.equalsIgnoreCase("nl")) {
      nonBreakerInputStream = getClass().getResourceAsStream(
          "/nl-nonbreaker.txt");
    }
    return nonBreakerInputStream;
  }

  /**
   * This function implements exceptions for periods as sentence breakers. It
   * decides when a period induces a new sentence or not.
   * 
   * @param line
   *          the text to be processed
   * @return segmented text (with newlines included)
   */
  public String SegmenterNonBreaker(String line) {

    // these are fine because they do not affect offsets
    line = line.trim();
    line = RuleBasedTokenizer.doubleSpaces.matcher(line).replaceAll(" ");
    final StringBuilder sb = new StringBuilder();
    String segmentedText = "";
    int i;
    final String[] words = line.split(" ");
    //iterate over the words
    for (i = 0; i < (words.length - 1); i++) {
      Matcher nonSegmentedWordMatcher = nonSegmentedWords.matcher(words[i]);
      //System.err.println("-> IF 01");
      //candidate word to be segmented found:
      if (nonSegmentedWordMatcher.find()) {
        String curWord = nonSegmentedWordMatcher.replaceAll("$1");
        String finalPunct = nonSegmentedWordMatcher.replaceAll("$2");
        if (!curWord.isEmpty() && curWord.matches("(" + NON_BREAKER + ")")
            && finalPunct.isEmpty()) {
          // if current word is not empty and is a no breaker and there is not
          // final punctuation
        } else if (acronym.matcher(words[i]).find()) {
          // if acronym
        } else if (nextCandidateWord.matcher(words[i + 1]).find()) {
          // if next word contains initial punctuation and then uppercase or
          // digit do:
          if (!(!curWord.isEmpty() && curWord.matches(NON_BREAKER_DIGITS)
              && (finalPunct.isEmpty()) && (startDigit.matcher(words[i + 1])
              .find()))) {
            // segment unless current word is a non breaker digit and next word
            // is not final punctuation or does not start with a number
            words[i] = words[i] + "\n";
          }
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
   * It decides when periods do not need to be tokenized.
   * 
   * @param line
   *          the sentence to be tokenized
   * @return line
   */
  public String TokenizerNonBreaker(String line) {

    // these are fine because they do not affect offsets
    line = line.trim();
    line = RuleBasedTokenizer.doubleSpaces.matcher(line).replaceAll(" ");
    final StringBuilder sb = new StringBuilder();
    String tokenizedText = "";
    int i;
    final String[] words = line.split(" ");

    for (i = 0; i < words.length; i++) {
      final Matcher wordDotMatcher = wordDot.matcher(words[i]);

      // find anything non-whitespace finishing with a period
      if (wordDotMatcher.find()) {
        final String curWord = wordDotMatcher.replaceAll("$1");

        if ((curWord.contains(".")
            && alphabetic.matcher(curWord).find())
            || curWord.matches("(" + NON_BREAKER + ")")
            || (i < words.length - 1
            && (startLower.matcher(words[i + 1]).find() || startPunct.matcher(
                words[i + 1]).find()))) {
          // do not tokenize if (word contains a period and is alphabetic) OR
          // word is a non breaker OR (word is a non breaker and next is
          // (lowercase or starts with punctuation that is end of sentence
          // marker))
        } else if (curWord.matches(NON_BREAKER_DIGITS) && i < words.length - 1
            && startDigit.matcher(words[i + 1]).find()) {
          // do not tokenize if word is a nonbreaker digit AND next word starts
          // with a digit
        } else {
          words[i] = curWord + " .";
        }
      }
      sb.append(words[i]).append(" ");
      tokenizedText = sb.toString();
    }
    return tokenizedText;
  }

}
