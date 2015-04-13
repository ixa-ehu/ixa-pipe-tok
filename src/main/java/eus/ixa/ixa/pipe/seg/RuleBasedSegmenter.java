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

import java.io.BufferedReader;
import java.io.IOException;
import java.util.Properties;
import java.util.regex.Pattern;

import eus.ixa.ixa.pipe.tok.NonBreaker;

public class RuleBasedSegmenter implements SentenceSegmenter {
 
  /**
   * The constant representing every line break in the original input text.
   */
  public static final String LINE_BREAK = "<JAR>";
  /**
   * Constant representing a paragraph (a doubleLine) in the original input text.
   */
  public static final String PARAGRAPH = "\u00B6";
  /**
   * Line break pattern.
   */
  public static Pattern lineBreak = Pattern.compile("(<JAR>)");
  /**
   * Two lines.
   */
  public static Pattern doubleLine = Pattern.compile("(<JAR><JAR>)");
  /**
   * If space paragraph mark and lowercase then it is a spurious paragraph.
   */
  //TODO extend to other expressions different from lower?
  public static Pattern spuriousParagraph = Pattern.compile("(\u00B6)(\\p{Space}*\\p{Lower})", Pattern.UNICODE_CHARACTER_CLASS);
  /**
   * Non-period end of sentence markers (?!), one or more spaces, sentence starters.
   */
  public static Pattern noPeriodSpaceEnd = Pattern
      .compile("([?!])[\\ ]+([\'\"\\(\\[\\¿\\¡\u00AB\u003C\u0091\u0093\u201B\u201C\u201F\u2018\u2039]*[\\p{Lu}])", Pattern.UNICODE_CHARACTER_CLASS);
  /**
   * Non-period end of sentence markers (?!), paragraph mark, sentence starters.
   */
  public static Pattern noPeriodParaEnd = Pattern
      .compile("([?!])(\u00B6)+([\'\"\\(\\[\\¿\\¡\u00AB\u003C\u0091\u0093\u201B\u201C\u201F\u2018\u2039]*[\\p{Lu}])", Pattern.UNICODE_CHARACTER_CLASS);
  /**
   * Multi-dots followed by sentence starters.
   */
  public static Pattern multiDotsSpaceStarters = Pattern
      .compile("(\\.[\\.]+)[\\ ]+([\'\"\\(\\[\\¿\\¡\u00AB\u003C\u0091\u0093\u201B\u201C\u201F\u2018\u2039]*[\\p{Lu}])", Pattern.UNICODE_CHARACTER_CLASS);
  /**
   * Multi-dots followed by sentence starters.
   */
  public static Pattern multiDotsParaStarters = Pattern
      .compile("(\\.[\\.]+)(\u00B6)+([\'\"\\(\\[\\¿\\¡\u00AB\u003C\u0091\u0093\u201B\u201C\u201F\u2018\u2039]*[\\p{Lu}])", Pattern.UNICODE_CHARACTER_CLASS);
  /**
   * End of sentence marker, maybe a space, punctuation (quotes, brackets), space, maybe some more punctuation, maybe some space and uppercase.
   */
  public static Pattern endInsideQuotesSpace = Pattern
      .compile("([?!\\.][\\ ]*[\'\"\\)\\]\\%\u00BB\u003D\u0092\u0094\u201D\u203A\u2019]+)[\\ ]+([\'\"\\(\\[\\¿\\¡\u00AB\u003C\u0091\u0093\u201B\u201C\u201F\u2018\u2039]*[\\ ]*[\\p{Lu}])", Pattern.UNICODE_CHARACTER_CLASS);
  /**
   * End of sentence marker, maybe a space, punctuation (quotes, brackets), space, maybe some more punctuation, maybe some space and uppercase.
   */
  public static Pattern endInsideQuotesPara = Pattern
      .compile("([?!\\.](\u00B6)*[\'\"\\)\\]\\%\u00BB\u003D\u0092\u0094\u201D\u203A\u2019]+)(\u00B6)+([\'\"\\(\\[\\¿\\¡\u00AB\u003C\u0091\u0093\u201B\u201C\u201F\u2018\u2039]*(\u00B6)*[\\p{Lu}])", Pattern.UNICODE_CHARACTER_CLASS);
  /**
   *  End with some sort of punctuation and followed by a sentence starter punctuation
   *  and upper case.
   */
  public static Pattern punctSpaceUpper = Pattern
      .compile("([?!\\.])[\\ ]+([\'\"\\(\\[\\¿\\¡\u00AB\u003C\u0091\u0093\u201B\u201C\u201F\u2018\u2039]+[\\ ]*[\\p{Lu}])", Pattern.UNICODE_CHARACTER_CLASS);
  /**
   *  End with some sort of punctuation and followed by a sentence starter punctuation
   *  and upper case.
   */
  public static Pattern punctParaUpper = Pattern
      .compile("([?!\\.])(\u00B6)+([\'\"\\(\\[\\¿\\¡\u00AB\u003C\u0091\u0093\u201B\u201C\u201F\u2018\u2039]+(\u00B6)*[\\p{Lu}])", Pattern.UNICODE_CHARACTER_CLASS);
  /**
   * Wrongly introduced periods; Centraal.There.
   */
  public static Pattern wrongPeriods = Pattern.
      compile("(\\w+[\\.]+)([\'\"\\(\\[\\¿\\¡\u00AB\u003C\u0091\u0093\u201B\u201C\u201F\u2018\u2039]*[\\p{Lu}])", Pattern.UNICODE_CHARACTER_CLASS);
  
  public static Pattern conventionalPara = Pattern
      .compile("([?!\\.])(\u00B6)+([\'\"\\(\\[\\¿\\¡\u00AB\u003C\u0091\u0093\u201B\u201C\u201F\u2018\u2039]*[\\p{Lu}])", Pattern.UNICODE_CHARACTER_CLASS);
  /**
  
  /**
   * End of sentence punctuation, spaces and link.
   */
  public static Pattern endPunctLinkSpace = Pattern.compile("([?!\\.])\\s+(http.+)");
  /**
   * End of sentence punctuation, spaces and link.
   */
  public static Pattern endPunctLinkPara = Pattern.compile("([?!\\.])(\u00B6)+(http.+|www.+|ftp.+)", Pattern.UNICODE_CHARACTER_CLASS);
  
  private static Boolean DEBUG = false;

  /**
   * The nonbreaker decides when to split strings followed by periods.
   */
  private NonBreaker nonBreaker;
  private BufferedReader breader;

  public RuleBasedSegmenter(BufferedReader reader, Properties properties) {
    if (nonBreaker == null) {
      nonBreaker = new NonBreaker(properties);
    }
    this.breader = reader;
  }
  
  public String[] segmentSentence() {
    String text = buildText();
    if (DEBUG) {
      System.err.println("->Build:" + text);
    }
    String[] sentences = segment(text);
    return sentences;
  }

  private String[] segment(String text) {
    
    //remove spurious paragraphs
    text = spuriousParagraph.matcher(text).replaceAll(" $2");
    // non-period end of sentence markers (?!) followed by sentence starters.
    text = noPeriodSpaceEnd.matcher(text).replaceAll("$1\n$2");
    // multi-dots followed by sentence starters
    text = multiDotsSpaceStarters.matcher(text).replaceAll("$1\n$2");
    // end of sentence inside quotes or brackets
    text = endInsideQuotesSpace.matcher(text).replaceAll("$1\n$2");
    text = endInsideQuotesPara.matcher(text).replaceAll("$1\n$3$4");
    text = punctSpaceUpper.matcher(text).replaceAll("$1\n$2");
    text = wrongPeriods.matcher(text).replaceAll("$1\n$2");
    //Segmented sentence appears empty when group is not properly specified (e.g., maybe $3 is just a blank).CAREFUL!!
    //TODO this does not work
    text = endPunctLinkSpace.matcher(text).replaceAll("$1\n$2");
    
    //TODO break the rest of the paragraphs
    //TODO do this properly in the nonbreaker
    text = conventionalPara.matcher(text).replaceAll("$1\n$2$3");
    // non prefix breaker detects exceptions to sentence breaks
    text = nonBreaker.SegmenterNonBreaker(text);
   
    String[] sentences = text.split("\n");
    return sentences;
  }
  
  /**
   * Build the text from the Reader. Adds "JAR" for line terminations and
   * "\u00B6" whenever two newlines are found together.
   * @return the string representing the text
   */
  public String buildText() {
    String line;
    StringBuilder sb = new StringBuilder();
    try {
      while ((line = breader.readLine()) != null) {
        sb.append(line).append(LINE_BREAK);
      }
    } catch (IOException e) {
      e.printStackTrace();
    }
    String text = sb.toString();
    //<JAR><JAR> to paragraph mark in unicode
    text = doubleLine.matcher(text).replaceAll(PARAGRAPH);
    //<JAR> to " "
    text = lineBreak.matcher(text).replaceAll(" ");
    return text;
  }

}
