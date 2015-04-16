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

import java.util.ArrayList;
import java.util.List;
import java.util.Properties;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import eus.ixa.ixa.pipe.seg.RuleBasedSegmenter;

/**
 * This class provides a multilingual rule based tokenizer. It also
 * provides normalization based on several corpora conventions such as
 * Penn Treebank and Ancora.
 * 
 * @author ragerri
 * @version 2015-04-14
 *
 */
public class RuleBasedTokenizer implements Tokenizer {

public static Pattern doubleSpaces = Pattern.compile("[\\  ]+");
 /**
 * Non printable control characters.
 */
public static Pattern asciiHex = Pattern.compile("[\\x00-\\x19]");
 /**
 * Tokenize everything but these characters.
 */
public static Pattern specials = Pattern
     .compile("([^\\p{Alnum}\\p{Space}\\.\\-\\¿\\?\\¡\\!'`,/\u0027\u0091\u0092\u2019\u201A\u201B\u203A\u2018\u2039\'])", Pattern.UNICODE_CHARACTER_CLASS);
 /**
 * Question and exclamation marks (do not separate if multiple).
 */
public static Pattern qexc = Pattern.compile("([\\¿\\?\\¡\\!]+)");
 /**
 * Dash preceded or followed by space.
 */
public static Pattern spaceDashSpace = Pattern.compile("( [\\-/]+|[\\-/]+ )");
 /**
 * Multidots.
 */
public static Pattern multiDots = Pattern.compile("\\.([\\.]+)");
 /**
 * Multi dot pattern and extra dot.
 */
public static Pattern dotmultiDot = Pattern.compile("DOTMULTI\\.");
 /**
 * Dot multi pattern followed by anything.
 */
public static Pattern dotmultiDotAny = Pattern.compile("DOTMULTI\\.([^\\.])");
 /**
 * No digit comma and no digit.
 */
public static Pattern noDigitComma = Pattern.compile("([^\\p{Digit}])(,)", Pattern.UNICODE_CHARACTER_CLASS);
public static Pattern commaNoDigit= Pattern.compile("(,)([^\\p{Digit}])", Pattern.UNICODE_CHARACTER_CLASS);
 /**
 * Digit comma and non digit.
 */
public static Pattern digitCommaNoDigit = Pattern.compile("([\\p{Digit}])(,)([^\\p{Digit}])", Pattern.UNICODE_CHARACTER_CLASS);
 /**
 * Non digit comma and digit.
 */
public static Pattern noDigitCommaDigit = Pattern.compile("([^\\p{Digit}])(,)(\\p{Digit})", Pattern.UNICODE_CHARACTER_CLASS);
/**
 * Detect wrongly tokenized links.
 */
public static Pattern wrongLink = Pattern
    .compile("((http|ftp)\\s:\\s//\\s*[\\p{Alpha}\\p{Digit}+&@#/%?=~_|!:,.;]+[-\\p{Alpha}\\p{Digit}\\+&@#/%=~_\\(|])", Pattern.UNICODE_CHARACTER_CLASS);

/**
 * No alphabetic apostrophe and no alphabetic.
 */
public static Pattern noAlphaAposNoAlpha = Pattern
    .compile("([^\\p{Alpha}])(" + Normalizer.TO_ASCII_SINGLE_QUOTE + ")([^\\p{Alpha})])", Pattern.UNICODE_CHARACTER_CLASS);
/**
 * Non alpha, digit, apostrophe and alpha.
 */
public static Pattern noAlphaDigitAposAlpha = Pattern
    .compile("([^\\p{Alpha}\\d])(" + Normalizer.TO_ASCII_SINGLE_QUOTE + ")(\\p{Alpha})", Pattern.UNICODE_CHARACTER_CLASS);
/**
 * Alphabetic apostrophe and non alpha.
 */
public static Pattern alphaAposNonAlpha = Pattern
    .compile("(\\p{Alpha})(" + Normalizer.TO_ASCII_SINGLE_QUOTE + ")([^\\p{Alpha}])", Pattern.UNICODE_CHARACTER_CLASS);
/**
 * Alphabetic apostrophe and alphabetic. Mostly for romance languages separation.
 */
public static Pattern AlphaAposAlpha = Pattern
    .compile("(\\p{Alpha})(" + Normalizer.TO_ASCII_SINGLE_QUOTE + ")(\\p{Alpha})", Pattern.UNICODE_CHARACTER_CLASS);
/**
 * Split English apostrophes. 
 */
public static Pattern englishApos = Pattern.compile("(\\p{Alpha})(" + Normalizer.TO_ASCII_SINGLE_QUOTE + ")([msdMSD]|re|ve|ll)", Pattern.UNICODE_CHARACTER_CLASS);
/**
 * Digit apostrophe and s (for 1990's).
 */
public static Pattern yearApos = Pattern.compile("([\\p{Digit}])(" + Normalizer.TO_ASCII_SINGLE_QUOTE + ")([s])", Pattern.UNICODE_CHARACTER_CLASS);
public static Pattern otherApos = Pattern.compile("(" + Normalizer.TO_ASCII_SINGLE_QUOTE + ")");

public static Pattern detokenParagraphs =  Pattern.compile("(\u00B6)[\\ ]*(\u00B6)", Pattern.UNICODE_CHARACTER_CLASS);

private static boolean DEBUG = false;

  private TokenFactory tokenFactory;
  private NonPeriodBreaker nonBreaker;
  private String lang;
  private String originalText;

  /**
   * Construct a rule based tokenizer.
   * @param text the text used for offset calculation
   * @param properties the options
   */
  public RuleBasedTokenizer(String text, Properties properties) {
    this.lang = properties.getProperty("language");
    nonBreaker = new NonPeriodBreaker(properties);
    tokenFactory = new TokenFactory();
    //TODO improve this
    originalText = RuleBasedSegmenter.buildText(text);    
  }
  

  /* (non-Javadoc)
   * @see eus.ixa.ixa.pipe.tok.Tokenizer#tokenize(java.lang.String[])
   */
  public List<List<Token>> tokenize(String[] sentences) {
    int prevIndex = 0;
    int curIndex = 0;
    List<List<Token>> result = new ArrayList<List<Token>>();
    //TODO improve this
    String offsetText = originalText;
    for (String sentence : sentences) {
      if (DEBUG) {
        System.err.println("-> Segmented:" + sentence);
      }
      List<Token> tokens = new ArrayList<Token>();
      String[] curTokens = getTokens(sentence);
      for (int i = 0; i < curTokens.length; i++) {
        curIndex = offsetText.indexOf(curTokens[i], prevIndex);
        Token curToken = tokenFactory.createToken(curTokens[i], curIndex, curTokens[i].length());
        if (DEBUG) {
        System.err.println("-> Token:" + curTokens[i] + " curIndex: " + curIndex + " prev: "  + prevIndex);
        }
        if (curToken.tokenLength() != 0) {
          tokens.add(curToken);
        }
        prevIndex = curIndex + curToken.tokenLength();
      }
      String language = lang;
      normalizeTokens(tokens, language);
      result.add(tokens);
    }
    return result;
  }
  
  /**
   * Actual tokenization function.
   * @param line the sentence to be tokenized
   * @return an array containing the tokens for the sentence
   */
  private String[] getTokens(String line) {

    //these are fine because they do not affect offsets
    line = line.trim();
    line = doubleSpaces.matcher(line).replaceAll(" ");
    // remove ASCII stuff
    line = asciiHex.matcher(line).replaceAll(" ");
    // separate question and exclamation marks
    line = qexc.matcher(line).replaceAll(" $1 ");
    // separate dash if before or after space
    line = spaceDashSpace.matcher(line).replaceAll(" $1 ");
    // separate out other special characters [^\p{Alnum}s.'`,-?!/]
    line = specials.matcher(line).replaceAll(" $1 ");

    // do not separate multidots
    line = generateMultidots(line);

    // separate "," except if within numbers (1,200)
    line = noDigitComma.matcher(line).replaceAll("$1 $2");
    line = commaNoDigit.matcher(line).replaceAll("$1 $2");
    // separate pre and post digit
    line = digitCommaNoDigit.matcher(line).replaceAll("$1 $2 $3");
    line = noDigitCommaDigit.matcher(line).replaceAll("$1 $2 $3");

    // contractions it's, l'agila, c'est
    line = treatContractions(line);
    //TODO future.'?
    // non breaker
    line = nonBreaker.TokenizerNonBreaker(line);

    // restore multidots
    line = restoreMultidots(line);
    // urls
    //TODO normalize URLs after tokenization for offsets
    line = detokenizeURLs(line);
    
    //these are fine because they do not affect offsets
    line = line.trim();
    line = doubleSpaces.matcher(line).replaceAll(" ");
    line = detokenParagraphs.matcher(line).replaceAll("$1$2");
    
    if (DEBUG) {
      System.out.println("->Tokens:" + line);
    }
    String[] tokens = line.split(" ");
    
    return tokens;
  }
  
  /**
   * This function normalizes multi-period expressions (...) to make
   * tokenization easier.
   * 
   * @param line
   * @return string
   */
  private String generateMultidots(String line) {

    line = multiDots.matcher(line).replaceAll(" DOTMULTI$1 ");
    Matcher dotMultiDot = dotmultiDot.matcher(line);

    while (dotMultiDot.find()) {
      line = dotmultiDotAny.matcher(line).replaceAll("DOTDOTMULTI $1");
      line = dotMultiDot.replaceAll("DOTDOTMULTI");
      // reset the matcher otherwise the while will stop after one run
      dotMultiDot.reset(line);
    }
    return line;
  }

  /**
   * Restores the normalized multidots to its original state and it tokenizes
   * them.
   * 
   * @param line
   * @return the tokenized multidots
   */
  private String restoreMultidots(String line) {

    while (line.contains("DOTDOTMULTI")) {
      line = line.replaceAll("DOTDOTMULTI", "DOTMULTI.");
    }
    line = line.replaceAll("DOTMULTI", ".");
    return line;
  }

  private String treatContractions(String line) {
    
      line = noAlphaAposNoAlpha.matcher(line).replaceAll("$1 $2 $3");
      line = noAlphaDigitAposAlpha.matcher(line).replaceAll("$1 $2 $3");
      line = alphaAposNonAlpha.matcher(line).replaceAll("$1 $2 $3");
      line = englishApos.matcher(line).replaceAll("$1 $2$3");
      line = yearApos.matcher(line).replaceAll("$1 $2$3");
      // romance tokenization of apostrophes c' l'
      line = AlphaAposAlpha.matcher(line).replaceAll("$1$2 $3");
    return line;
  }

  private String detokenizeURLs(String line) {
    Matcher linkMatcher = wrongLink.matcher(line);
    StringBuffer sb = new StringBuffer();
    while (linkMatcher.find()) {
      linkMatcher.appendReplacement(sb, linkMatcher.group().replaceAll("\\s", ""));
    }
    linkMatcher.appendTail(sb);
    line = sb.append(" ").toString();
    return line;
  }
  
  /**
   * Set as value of the token its normalized counterpart. Normalization
   * is done following languages and corpora (Penn TreeBank, Ancora, Tiger, Tutpenn, etc.)
   * conventions.
   * @param tokens the tokens
   */
  public static void normalizeTokens(List<Token> tokens, String lang) {
    String tokenizedSentence = StringUtils.getStringFromTokens(tokens);
    tokenizedSentence = Normalizer.convertNonCanonicalStrings(tokenizedSentence, lang);
    //TODO work to do in English with double ascii quotes
    tokenizedSentence= Normalizer.normalizeQuotes(tokenizedSentence, lang);
    String[] normalizedTokens = tokenizedSentence.split(" ");
    for (int i = 0; i < tokens.size(); i++) {
      tokens.get(i).setTokenValue(normalizedTokens[i]);
    }
  }
}
