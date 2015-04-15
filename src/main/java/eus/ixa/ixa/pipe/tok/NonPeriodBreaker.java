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

  public static String SECTION = "\u00A7";
  public static Pattern section = Pattern.compile(SECTION);
  /**
   * Segment everything not segmented in the SentenceSegmenter.
   */
  public static Pattern segmentAll = Pattern.compile("([\\p{Alnum}\\.-]*" + RuleBasedSegmenter.FINAL_PUNCT + "*[\\.]+)[\\ ]*(" + RuleBasedSegmenter.INITIAL_PUNCT + "*[\\ ]*[\\p{Lu}\\p{Digit}])", Pattern.UNICODE_CHARACTER_CLASS);
  /**
   * Do not split dot after these words if followed by number.
   */
  public static String NON_BREAKER_DIGITS = "(al|[Aa]rt|[Nn]o|[Nn]r|p|pp|[Pp]ág)";
  /**
   * Re-attach segmented dots after non breaker digits.
   */
  public static Pattern nonBreakerDigits = Pattern.compile("(" + NON_BREAKER_DIGITS + "[\\ ]*[\\.-]*)" + SECTION + "([\\ ]*\\p{Digit})", Pattern.UNICODE_CHARACTER_CLASS);
  /**
   * General acronyms.
   */
  public static Pattern acronym = Pattern.compile("(\\p{Alpha})(\\.(\u00A7)[\\ ]*\\p{Alpha})+([\\.])", Pattern.UNICODE_CHARACTER_CLASS);
  /**
   * Do not segment numbers like 11.1.
   */
  public static Pattern numbers = Pattern.compile("(\\p{Digit}+[\\.])[\\ ]*[\u00A7][\\ ]*(\\p{Digit}+)", Pattern.UNICODE_CHARACTER_CLASS);
  /**
   * Any non white space followed by a period.
   */
  public static Pattern wordDot = Pattern.compile("^(\\S+)\\.$");
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
   * This constructor reads some non breaking prefixes files in resources
   * to create exceptions of segmentation and tokenization.
   * 
   * @param properties
   *          the options
   */
  public NonPeriodBreaker(Properties properties) {
    loadNonBreaker(properties);
  }

  private void loadNonBreaker(Properties properties) {
    String lang = properties.getProperty("language");
    if (NON_BREAKER == null) {
      createNonBreaker(lang);
    }
  }

  private void createNonBreaker(String lang) {
    List<String> nonBreakerList = new ArrayList<String>();
    
    InputStream nonBreakerInputStream = getNonBreakerInputStream(lang);
    if (nonBreakerInputStream == null) {
      System.err.println("ERROR: Not nonbreaker file for language " + lang
          + " in src/main/resources!!");
      System.exit(1);
    }
    BufferedReader breader = new BufferedReader(new InputStreamReader(
        nonBreakerInputStream));
    String line;
    try {
      while ((line = breader.readLine()) != null) {
        line = line.trim();
        if (!line.startsWith("#")) {
          nonBreakerList.add(line);
          }
        }
    } catch (IOException e) {
      e.printStackTrace();
    }
    NON_BREAKER = StringUtils.createDisjunctRegexFromList(nonBreakerList);
  }

  private final InputStream getNonBreakerInputStream(String lang) {
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
   
    //split everything not segmented in the SentenceSegmenter
    line = segmentAll.matcher(line).replaceAll("$1\u00A7$2");
   
    //re-attached dots followed by numbers
    line = nonBreakerDigits.matcher(line).replaceAll("$1$3");
    //re-attached segmented dots preceded by a word in the non breaker list
    Pattern nonBreaker = Pattern.compile("([\\ ](" + NON_BREAKER + ")[\\ ]*[\\.]*)[\\ ]*" + SECTION);
    line = nonBreaker.matcher(line).replaceAll("$1");
    //acronyms
    line = deSegmentAcronyms(line);
    //de-segment 11.1. numbers
    line = numbers.matcher(line).replaceAll("$1$2");
    //split any remaining section mark
    line = section.matcher(line).replaceAll("\n");
    return line;
  }
  
  /**
   * Removes wrongly introduce SECTION marks in acronyms.
   * @param line the text
   * @return the segmented text
   */
  public static String deSegmentAcronyms(String line) {
    Matcher linkMatcher = acronym.matcher(line);
    StringBuffer sb = new StringBuffer();
    while (linkMatcher.find()) {
      linkMatcher.appendReplacement(sb, linkMatcher.group().replaceAll(SECTION, ""));
    }
    linkMatcher.appendTail(sb);
    line = sb.toString();
    return line;
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
    StringBuilder sb = new StringBuilder();
    String tokenizedText = "";
    int i;
    String[] words = line.split(" ");
    
    for (i = 0; i < words.length; i++) {
      Matcher wordDotMatcher = wordDot.matcher(words[i]);
      
      // find anything non-whitespace finishing with a period
      if (wordDotMatcher.find()) {
        String prefix = wordDotMatcher.replaceAll("$1");
        
        if ((prefix.contains(".") && prefix.matches("\\p{Alpha}+"))
            || (prefix.matches("(" + NON_BREAKER + ")"))
            || (i < (words.length - 1) && (startLower.matcher(words[i + 1]).find() || startPunct.matcher(words[i + 1]).find()))) {
          // do not tokenize if (word contains a period and is alphabetic) OR word is a non breaker OR (word is a non breaker and next is (lowercase or starts with punctuation that is end of sentence marker))
        } else if (prefix.matches(NON_BREAKER_DIGITS) && (i < (words.length - 1) && startDigit.matcher(words[i + 1]).find())) {
          // do not tokenize if word is a nonbreaker digit AND next word starts with a digit 
        } else {
          words[i] = prefix + " .";
        }
      }
      sb.append(words[i]).append(" ");
      tokenizedText = sb.toString();
    }
    return tokenizedText;
  }

}
