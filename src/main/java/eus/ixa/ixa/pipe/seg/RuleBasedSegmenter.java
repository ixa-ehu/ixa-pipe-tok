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

import eus.ixa.ixa.pipe.tok.NonBreaker;

public class RuleBasedSegmenter implements SentenceSegmenter {
 
  /**
   * The constant representing every line break in the original input text.
   */
  public static final String LINE_BREAK = "<JAR>";
  /**
   * Constant representing a paragraph (a doubleLine) in the original input text.
   */
  public static final String PARAGRAPH = "<P>";
  /**
   * Constant representing a double space in the original input text.
   */
  public static final String DOUBLE_SPACE = "<KAR>";
  /**
   * Line break pattern.
   */
  public static Pattern lineBreak = Pattern.compile("<JAR>");
  /**
   * Two lines.
   */
  public static Pattern doubleLine = Pattern.compile("<JAR><JAR>");
  /**
   * Multi space pattern.
   */
  public static Pattern doubleSpace = Pattern.compile("\\s\\s");
  /**
   * If space paragraph mark and lowercase then it is a spurious paragraph.
   */
  //TODO extend to other expressions different from lower?
  public static Pattern spuriousParagraph = Pattern.compile("(<P>)(\\p{Lower})", Pattern.UNICODE_CHARACTER_CLASS);
  
  /**
   * Non-period end of sentence markers (?!) followed by sentence starters.
   */
  public static Pattern noPeriodEnd = Pattern
      .compile("([?!])([<JAR><KAR><P>\\s]+)([\'\"\\(\\[\\¿\\¡\u00AB\u2018\u201B\u201C\u201F\u2039]*[\\p{Lu}])", Pattern.UNICODE_CHARACTER_CLASS);
  /**
   * Multi-dots followed by sentence starters.
   */
  public static Pattern multiDotsStarters = Pattern
      .compile("(\\.[\\.]+)([<JAR><KAR><P>\\s]+)([\'\"\\(\\[\\¿\\¡\u00AB\u2018\u201B\u201C\u201F\u2039]*[\\p{Lu}])", Pattern.UNICODE_CHARACTER_CLASS);
  /**
   * Wrongly introduced periods; Centraal.There.
   */
  public static Pattern wrongPeriods = Pattern.
      compile("(\\w+[\\.]+)([\'\"\\(\\[\\¿\\¡\u00AB\u2018\u201B\u201C\u201F\u2039]*[\\p{Lu}])", Pattern.UNICODE_CHARACTER_CLASS);
  
  /**
   * Some sort of punctuation inside a quote or parenthetical followed by a possible
   * sentence starter punctuation and upper case.
   */
  public static Pattern endInsideQuotes = Pattern
      .compile("([?!\\.][\\ ]*[\'\"\\)\\]\\%\u00BB\u2019\u201D\u203A]+)([<JAR><KAR><P>\\s]+)([\'\"\\(\\[\\¿\\¡\u00AB\u2018\u201B\u201C\u201F\u2039]*[\\ ]*[\\p{Lu}])");

  /**
   *  End with some sort of punctuation and followed by a sentence starter punctuation
   *  and upper case.
   */
  public static Pattern punctUpper = Pattern
      .compile("([?!\\.])([<JAR><KAR><P>\\s]+)([\'\"\\(\\[\\¿\\¡\u00AB\u2018\u201B\u201C\u201F\u2039]+[\\ ]*[\\p{Lu}])");
  /**
   * End of sentence punctuation, spaces and link.
   */
  public static Pattern endPunctLink = Pattern.compile("([?!\\.])([<JAR><KAR><P>\\s]+)(http.+|www+)");

  

  /**
   * The nonbreaker.
   */
  private NonBreaker nonBreaker;

  public RuleBasedSegmenter(Properties properties) {
    if (nonBreaker == null) {
      nonBreaker = new NonBreaker(properties);
    }
  }
  
  public String[] segmentSentence(String line) {
    String[] sentences = sentenceSplitter(line);
    return sentences;
  }

  private String[] sentenceSplitter(String text) {
    
    //text = spuriousParagraph.matcher(text).replaceAll("$1 $2");
    // non-period end of sentence markers (?!) followed by sentence starters.
    text = noPeriodEnd.matcher(text).replaceAll("$1$2\n$3");
    // multi-dots followed by sentence starters
    text = multiDotsStarters.matcher(text).replaceAll("$1$2\n$2");
    text = wrongPeriods.matcher(text).replaceAll("$1\n$2");
    // end of sentence inside quotes or brackets
    text = endInsideQuotes.matcher(text).replaceAll("$1\n$2");
    // add breaks for sentences that end with some sort of punctuation are
    // followed by a sentence starter punctuation and upper case
    text = punctUpper.matcher(text).replaceAll("$1\n$2");
    text = endPunctLink.matcher(text).replaceAll("$1\n$2");

    // non prefix breaker detects exceptions to sentence breaks
    text = nonBreaker.SegmenterNonBreaker(text);
    String[] sentences = text.split("\n");
    return sentences;
  }
  
  public String buildText(String text) {
    //<JAR><JAR> to <P>
    text = doubleLine.matcher(text).replaceAll(RuleBasedSegmenter.PARAGRAPH);
    //<JAR> to " "
    text = lineBreak.matcher(text).replaceAll(" ");
    //"\\s\\s" to <KAR>
    text = doubleSpace.matcher(text).replaceAll(RuleBasedSegmenter.DOUBLE_SPACE);
    return text;
  }

}
