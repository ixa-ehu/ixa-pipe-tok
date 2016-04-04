/*
 *Copyright 2015 Rodrigo Agerri

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

package eus.ixa.ixa.pipe.seg;

import java.util.Properties;
import java.util.regex.Pattern;

import eus.ixa.ixa.pipe.tok.NonPeriodBreaker;
import eus.ixa.ixa.pipe.tok.RuleBasedTokenizer;

/**
 * Rule based SentenceSegmenter. It also removes possible spurious paragraphs
 * and newlines. Exceptions are managed by the NonPeriodBreaker class.
 * 
 * @author ragerri
 * @version 2015-04-14
 */
public class RuleBasedSegmenter implements SentenceSegmenter {

  /**
   * The constant representing every line break in the original input text.
   */
  public static final String LINE_BREAK = "<JAR>";
  /**
   * Constant representing a paragraph (a doubleLine) in the original input
   * text.
   */
  public static final String PARAGRAPH = "\u00B6\u00B6";
  /**
   * Line break pattern.
   */
  public static Pattern lineBreak = Pattern.compile("<JAR>");
  /**
   * Two lines.
   */
  public static Pattern doubleLineBreak = Pattern.compile("(<JAR><JAR>)");
  /**
   * Paragraph pattern.
   */
  public static Pattern paragraph = Pattern.compile("(" + PARAGRAPH + ")");
  /**
   * Initial punctuation in unicode.
   */
  public static String INITIAL_PUNCT = "[\u0023\'\"\\¿\\¡\u00AB\u003C\u0091\u0093\u201B\u201C\u201F\u2018\u2039]";
  /**
   * Final punctuation in unicode.
   */
  public static String FINAL_PUNCT = "[\'\"\\)\\]\\%\u00BB\u003D\u0092\u0094\u201D\u203A\u2019]";
  /**
   * End of sentence markers, paragraph mark and link.
   */
  public static Pattern endPunctLinkPara = Pattern
      .compile("([?!\\.])[\\ ]*(\u00B6\u00B6)+[\\ ]*(http|www|ftp)");
  /**
   * End of sentence marker, one or more paragraph marks, maybe some starting
   * punctuation, uppercase.
   */
  public static Pattern conventionalPara = Pattern.compile(
      "([?!\\.])[\\ ]*(\u00B6\u00B6)+[\\ ]*(" + INITIAL_PUNCT + "*[\\p{Lu}])",
      Pattern.UNICODE_CHARACTER_CLASS);
  /**
   * End of sentence marker, maybe one or more paragraph marks, final punctuation (quotes, brackets),
   * two or more paragraph marks, maybe some initial punctuation, maybe some space and uppercase.
   */
  public static Pattern endInsideQuotesPara = Pattern.compile(
      "([?!\\.](\u00B6)*" + FINAL_PUNCT + "+)(\u00B6\u00B6)+(" + INITIAL_PUNCT
          + "*(\u00B6\u00B6)*[\\p{Lu}])", Pattern.UNICODE_CHARACTER_CLASS);
  /**
   * Multi-dots, paragraph mark, sentence starters and uppercase.
   */
  public static Pattern multiDotsParaStarters = Pattern.compile(
      "(\\.[\\.]+)(\u00B6\u00B6)+(" + INITIAL_PUNCT + "*[\\p{Lu}])",
      Pattern.UNICODE_CHARACTER_CLASS);
  /**
   * If paragraph mark, maybe some space and lowercase or punctuation (not start
   * of sentence markers) then it is a spurious paragraph.
   */
  public static Pattern spuriousParagraph = Pattern
      .compile(
          "(\u00B6\u00B6)+\\s*([\\p{Lower}\\!#\\$%&\\(\\)\\*\\+,-\\/:;=>\\?@\\[\\\\\\]\\^\\{\\|\\}~])",
          Pattern.UNICODE_CHARACTER_CLASS);
  /**
   * Alphanumeric, maybe a space, paragraph mark, maybe a space, and lowercase
   * letter or digit.
   */
  public static Pattern alphaNumParaLowerNum = Pattern.compile(
      "(\\p{Alnum})\\s*(\u00B6\u00B6)+\\s*([\\p{Lower}\\p{Digit}])",
      Pattern.UNICODE_CHARACTER_CLASS);
  /**
   * Non-period end of sentence markers (?!), one or more spaces, sentence
   * starters.
   */
  public static Pattern noPeriodSpaceEnd = Pattern.compile("([?!])[\\ ]+("
      + INITIAL_PUNCT + "*[\\p{Lu}])", Pattern.UNICODE_CHARACTER_CLASS);
  /**
   * Multi-dots, space, sentence starters and uppercase.
   */
  public static Pattern multiDotsSpaceStarters = Pattern.compile(
      "(\\.[\\.]+)[\\ ]+(" + INITIAL_PUNCT + "*[\\p{Lu}])",
      Pattern.UNICODE_CHARACTER_CLASS);
  /**
   * End of sentence marker, maybe a space, punctuation (quotes, brackets),
   * space, maybe some more punctuation, maybe some space and uppercase.
   */
  public static Pattern endInsideQuotesSpace = Pattern.compile("([?!\\.][\\ ]*"
      + FINAL_PUNCT + "+)[\\ ]+(" + INITIAL_PUNCT + "*[\\ ]*[\\p{Lu}])",
      Pattern.UNICODE_CHARACTER_CLASS);
  /**
   * End of sentence marker, sentence starter punctuation and upper case.
   */
  public static Pattern punctSpaceUpper = Pattern.compile("([?!\\.])[\\ ]+("
      + INITIAL_PUNCT + "+[\\ ]*[\\p{Lu}])", Pattern.UNICODE_CHARACTER_CLASS);
  /**
   * - ( C
   */
  public static Pattern punctSpaceMultiPunct = Pattern.compile("([?!\\.])[\\ ]+([\\-]+[\\ ]*[\\(]*\\p{Lu})", Pattern.UNICODE_CHARACTER_CLASS);

  /**
   * End of sentence punctuation, maybe spaces and link.
   */
  public static Pattern endPunctLinkSpace = Pattern
      .compile("([?!\\.])[\\ ]*(http|www|ftp)");

  private static Boolean DEBUG = false;

  /**
   * The nonbreaker decides when to split strings followed by periods.
   */
  private NonPeriodBreaker nonBreaker;
  private final String text;
  private boolean isHardParagraph = false;

  /**
   * Construct a RuleBasedSegmenter from a BufferedReader and the properties.
   * 
   * @param originalText
   *          the text to be segmented
   * @param properties
   *          the properties
   */
  public RuleBasedSegmenter(final String originalText,
      final Properties properties) {
    String hardParagraph = properties.getProperty("hardParagraph");
    if (hardParagraph.equalsIgnoreCase("yes")) {
      isHardParagraph = true;
    }
    if (nonBreaker == null) {
      nonBreaker = new NonPeriodBreaker(properties);
    }
    // TODO improve this, when should we load the text?
    text = buildText(originalText);
  }

  /*
   * (non-Javadoc)
   * 
   * @see eus.ixa.ixa.pipe.seg.SentenceSegmenter#segmentSentence()
   */
  public String[] segmentSentence() {
    if (DEBUG) {
      System.err.println("-> Build:" + text);
    }
    final String[] sentences = segment(text);
    return sentences;
  }

  /**
   * Segments sentences and calls the NonPeriodBreaker for exceptions.
   * 
   * @param text
   *          the text be segmented
   * @return the sentences
   */
  private String[] segment(final String builtText) {

    // these are fine because they do not affect offsets
    String line = builtText.trim();
    line = RuleBasedTokenizer.doubleSpaces.matcher(line).replaceAll(" ");
    
    if (isHardParagraph) {
      //convert every (spurious) paragraph in newlines and keep them
      line = paragraph.matcher(line).replaceAll("\n$1");
    } else {
      // end of sentence markers, paragraph mark and beginning of link
      line = endPunctLinkPara.matcher(line).replaceAll("$1\n$2$3");
      line = conventionalPara.matcher(line).replaceAll("$1\n$2$3");
      line = endInsideQuotesPara.matcher(line).replaceAll("$1\n$3$4");
      line = multiDotsParaStarters.matcher(line).replaceAll("$1\n$2$3");
      // remove spurious paragraphs
      line = alphaNumParaLowerNum.matcher(line).replaceAll("$1 $3");
      line = spuriousParagraph.matcher(line).replaceAll(" $2");
    }
    // non-period end of sentence markers (?!) followed by sentence starters.
    line = noPeriodSpaceEnd.matcher(line).replaceAll("$1\n$2");
    // multi-dots followed by sentence starters
    line = multiDotsSpaceStarters.matcher(line).replaceAll("$1\n$2");
    // end of sentence inside quotes or brackets
    line = endInsideQuotesSpace.matcher(line).replaceAll("$1\n$2");
    // end of sentence marker, sentence starter punctuation and upper case.
    line = punctSpaceUpper.matcher(line).replaceAll("$1\n$2");
    // end of sentence markers, maybe space or paragraph mark and beginning of
    // link
    line = endPunctLinkSpace.matcher(line).replaceAll("$1\n$2");
    
    //special case of multi-punctuation
    line = punctSpaceMultiPunct.matcher(line).replaceAll("$1\n$2");

    //System.err.println("-> NonBreaker.....");
    // non breaker segments everything else with some exceptions
    line = nonBreaker.SegmenterNonBreaker(line);
    //System.err.println("-> Segmentation DONE!");
    final String[] sentences = line.split("\n");
    return sentences;
  }

  public static String buildText(String text) {
    // <JAR><JAR> to PARAGRAPH mark in unicode
    text = RuleBasedSegmenter.doubleLineBreak.matcher(text).replaceAll(
        PARAGRAPH);
    // <JAR> to " "
    text = RuleBasedSegmenter.lineBreak.matcher(text).replaceAll(" ");
    return text;
  }

}
