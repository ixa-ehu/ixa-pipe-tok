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
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Properties;
import java.util.concurrent.ConcurrentHashMap;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import eus.ixa.ixa.pipe.seg.RuleBasedSegmenter;

/**
 * This class implements exceptions for periods as sentence breakers and tokens.
 * It decides when a period induces a new sentence or a new token and when it
 * does not.
 * 
 * @author ragerri
 * 
 */
public class NonBreaker {

  private static String SECTION = "\u00A7";
  public static Pattern section = Pattern.compile(SECTION);
  private static String NON_BREAKER = null;
  
  /**
   * Segment everything not segmented with the RuleBasedSegmenter.
   */
  public static Pattern segmentAll = Pattern.compile("([\\p{Alnum}\\.-]*" + RuleBasedSegmenter.FINAL_PUNCT + "*[\\.]+)([\\ ]*" + RuleBasedSegmenter.INITIAL_PUNCT + "*[\\ ]*[\\p{Lu}\\p{Digit}])", Pattern.UNICODE_CHARACTER_CLASS);
  
  /**
   * Do not split dot after this words if followed by number.
   */
  public static String NON_BREAKER_DIGITS = "(al|art|no|pp)";
  /**
   * Re-attach segmented dots after non breaker digits.
   */
  public static Pattern nonBreakerDigits = Pattern.compile("((al|art|no|pp)[\\ ]*[\\.-]*)" + SECTION + "([\\ ]*\\p{Digit})", Pattern.UNICODE_CHARACTER_CLASS);

  /**
   * 
   * This constructor reads nonbreaking-prefix.lang files in resources and
   * assigns "1" as value when the word does not create a break (Dr.) and "2"
   * when the word does not create a break if followed by a number (No. 1)
   * 
   * @param properties
   *          the options
   */
  public NonBreaker(Properties properties) {
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
   
    //split everything not segmented in the segmenter
    line = segmentAll.matcher(line).replaceAll("$1\u00A7$2");
   
    //re-attached dots followed by numbers
    line = nonBreakerDigits.matcher(line).replaceAll("$1$3");
    //TODO two first conditions
    
    //split every section mark introduced
    line = section.matcher(line).replaceAll("\n");
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
    line = line;
    return line;
  }

}
