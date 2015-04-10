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

public class RuleBasedTokenizer implements Tokenizer {

 /**
 * Non printable control characters.
 */
public static Pattern asciiHex = Pattern.compile("[\\x00-\\x19]");
 /**
 * Tokenize everything but these characters.
 */
public static Pattern specials = Pattern
     .compile("([^\\p{Alnum}\\p{Space}\\.\\-\\¿\\?\\¡\\!'`,/])", Pattern.UNICODE_CHARACTER_CLASS);
 /**
 * question and exclamation marks (do not separate if multiple).
 */
public static Pattern qexc = Pattern.compile("([\\¿\\?\\¡\\!]+)");
 /**
 * Dash preceded or followed by space.
 */
public static Pattern spaceDashSpace = Pattern.compile("([<P> ]+[\\-/]+|[\\-/]+[<P> ]+)");
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
public static Pattern dotmultiDotAny = Pattern
     .compile("DOTMULTI\\.([^\\.])");
 /**
 * No digit comma and no digit.
 */
public static Pattern noDigitCommaNoDigit = Pattern
     .compile("([^\\d])[,]([^\\d])");
 /**
 * Digit comma and non digit.
 */
public static Pattern digitCommaNoDigit = Pattern
     .compile("([\\d])[,]([^\\d])");
 /**
 * Non digit comma and digit.
 */
public static Pattern noDigitCommaDigit = Pattern
     .compile("([^\\d])[,](\\d)");
/**
 * Detect wrongly tokenized links.
 */
public static Pattern link = Pattern
    .compile("((http|ftp)\\s:\\s//[\\s-A-Za-z0-9+&@#/%?=~_|!:,.;]*[-A-Za-z0-9+&@#/%=~_(|])");

/**
 * No alphabetic apostrophe and no alphabetic.
 */
public static Pattern noAlphaAposNoAlpha = Pattern
    .compile("([^\\p{Alpha}])['](^[\\p{Alpha}')])", Pattern.UNICODE_CHARACTER_CLASS);
/**
 * Non alpha, digit, apostrophe and alpha.
 */
public static Pattern noAlphaDigitAposAlpha = Pattern
    .compile("([^\\p{Alpha}\\d])['](\\p{Alpha})", Pattern.UNICODE_CHARACTER_CLASS);
/**
 * Alphabetic apostrophe and non alpha.
 */
public static Pattern alphaAposNonAlpha = Pattern
    .compile("(\\p{Alpha})[']([^\\p{Alpha}])", Pattern.UNICODE_CHARACTER_CLASS);
/**
 * Alphabetic apostrophe and alphabetic. Mostly for romance languages separation.
 */
public static Pattern AlphaAposAlpha = Pattern
    .compile("(\\p{Alpha})['](\\p{Alpha})", Pattern.UNICODE_CHARACTER_CLASS);

/**
 * Split English apostrophes. 
 */
public static Pattern englishApos = Pattern.compile("(\\p{Alpha})[']([msdMSD]|re|ve|ll)", Pattern.UNICODE_CHARACTER_CLASS);
/**
 * Digit apostrophe and s (for 1990's).
 */
public static Pattern yearApos = Pattern.compile("([\\d])[']([s])");
/**
 * Re-tokenize wrongly split paragraphs.
 */
public static Pattern tokParagraph = Pattern.compile("<\\s(P)\\s>");
/**
 * Offset counter.
 */
int offsetCounter = 0;
/**
 * Current index.
 */
int curIndex = 0;
/**
 * Previous index.
 */
int prevIndex = 0;

  private TokenFactory tokenFactory;
  private NonBreaker nonBreaker;
  private String lang;

  public RuleBasedTokenizer(Properties properties) {
    lang = properties.getProperty("language");
    nonBreaker = new NonBreaker(properties);
    tokenFactory = new TokenFactory();
  }

  public List<List<Token>> tokenize(String[] sentences) {
    List<List<Token>> result = new ArrayList<List<Token>>();
    List<Token> tokens = new ArrayList<Token>();
    
    for (String sentence : sentences) {
      System.err.println("-> Segmented:" + sentence);
      String[] curTokens = getTokens(sentence);
      for (int i = 0; i < curTokens.length; i++) {
        curIndex = sentence.indexOf(curTokens[i], prevIndex);
        int offset = curIndex + offsetCounter;
        Token curToken = makeToken(curTokens[i], offset);
        tokens.add(curToken);
        prevIndex = curIndex + curToken.tokenLength();
      }
      offsetCounter = offsetCounter + sentence.length();
    }
    result.add(tokens);
    return result;
  }

  private String[] getTokens(String line) {

    // remove ASCII stuff
    line = asciiHex.matcher(line).replaceAll(" ");
    //normalize following language and corpus conventions
    //line = Normalizer.convertNonCanonicalStrings(line);
    //line = Normalizer.normalizeQuotes(line, lang);
    // separate question and exclamation marks
    line = qexc.matcher(line).replaceAll(" $1 ");
    // separate dash if before or after space
    line = spaceDashSpace.matcher(line).replaceAll(" $1 ");
    // separate out other special characters [^\p{Alnum}s.'`,-?!/]
    line = specials.matcher(line).replaceAll(" $1 ");

    // do not separate multidots
    line = generateMultidots(line);

    // separate "," except if within numbers (1,200)
    line = noDigitCommaNoDigit.matcher(line).replaceAll("$1 , $2");
    // separate pre and post digit
    line = digitCommaNoDigit.matcher(line).replaceAll("$1 , $2");
    line = noDigitCommaDigit.matcher(line).replaceAll("$1 , $2");

    // contractions it's, l'agila
    line = treatContractions(line);
    // non breaker
    line = nonBreaker.TokenizerNonBreaker(line);

    // clean up extra spaces
    line = line.replaceAll("\\s+", " ");
    line = line.trim();

    // restore multidots
    line = restoreMultidots(line);
    // urls 
    line = detokenizeURLs(line);
    //restore paragraph marks
    line = detokenizeParagraphs(line);
    
    // create final array of tokens
    //System.out.println("->Tokens:" + line);
    String[] tokens = line.split(" ");
    
    // ensure final line break
    // if (!line.endsWith("\n")) { line = line + "\n"; }
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
    
      line = noAlphaAposNoAlpha.matcher(line).replaceAll("$1 ' $2");
      line = noAlphaDigitAposAlpha.matcher(line).replaceAll("$1 ' $2");
      line = alphaAposNonAlpha.matcher(line).replaceAll("$1 ' $2");
      line = englishApos.matcher(line).replaceAll("$1 '$2");
      line = yearApos.matcher(line).replaceAll("$1 '$2");
      // romance tokenization of apostrophes c' l'
      line = AlphaAposAlpha.matcher(line).replaceAll("$1' $2");
    return line;
  }

  private String detokenizeURLs(String line) {
    Matcher linkMatcher = link.matcher(line);
    StringBuffer sb = new StringBuffer();
    while (linkMatcher.find()) {
      linkMatcher.appendReplacement(sb, linkMatcher.group().replaceAll("\\s", ""));
    }
    linkMatcher.appendTail(sb);
    line = sb.toString();
    return line;
  }
  
  private String detokenizeParagraphs(String line) {
    Matcher paragraphMatcher = tokParagraph.matcher(line);
    StringBuffer sb = new StringBuffer();
    while (paragraphMatcher.find()) {
      paragraphMatcher.appendReplacement(sb, paragraphMatcher.group().replaceAll("\\s", ""));
    }
    paragraphMatcher.appendTail(sb);
    line = sb.toString();
    return line;
  }

  private Token makeToken(String tokenString, int offset) {
    Token token;
    if (tokenString.equalsIgnoreCase(RuleBasedSegmenter.LINE_BREAK) || tokenString.equalsIgnoreCase(RuleBasedSegmenter.PARAGRAPH)) {
      token = tokenFactory.createToken(tokenString, offset, 1);
    }
    else {
      token = tokenFactory.createToken(tokenString, offset, tokenString.length());
    }
    return token;
  }


}
