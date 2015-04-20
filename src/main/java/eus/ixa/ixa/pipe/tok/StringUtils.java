package eus.ixa.ixa.pipe.tok;

import java.io.BufferedReader;
import java.io.IOException;
import java.util.List;
import java.util.regex.Pattern;

import eus.ixa.ixa.pipe.seg.RuleBasedSegmenter;

public class StringUtils {

  public static Pattern doubleBar = Pattern.compile("\\|\\|");

  private StringUtils() {

  }

  public static String getStringFromTokens(final String[] tokens) {
    final StringBuilder sb = new StringBuilder();
    for (final String tok : tokens) {
      sb.append(tok).append(" ");
    }
    if (sb.length() > 0) {
      sb.setLength(sb.length() - 1);
    }
    return sb.toString();
  }

  public static String getStringFromTokens(final List<Token> tokens) {
    final StringBuilder sb = new StringBuilder();
    for (final Token tok : tokens) {
      sb.append(tok.getTokenValue()).append(" ");
    }
    if (sb.length() > 0) {
      sb.setLength(sb.length() - 1);
    }
    return sb.toString();
  }

  public static String createDisjunctRegexFromList(final List<String> words) {
    final StringBuilder sb = new StringBuilder();
    for (final String word : words) {
      sb.append(word).append("|");
    }
    String regExp = sb.toString();
    regExp = doubleBar.matcher(regExp).replaceAll("\\|");
    regExp = regExp.replaceAll("\\.", "\\\\.");
    final String result = regExp.substring(1, regExp.length() - 1);
    return result;
  }
  
  /**
   * Reads standard input text from the BufferedReader and
   * adds a line break mark for every line. The output of
   * this functions is then further processed by methods
   * called in the constructors of the SentenceSegmenter and
   * Tokenizer.
   * @param breader the buffered reader
   * @return the input text in a string object
   */
  public static String readText(final BufferedReader breader) {
    String line;
    final StringBuilder sb = new StringBuilder();
    try {
      while ((line = breader.readLine()) != null) {
        sb.append(line).append(RuleBasedSegmenter.LINE_BREAK);
      }
    } catch (final IOException e) {
      e.printStackTrace();
    }
    final String text = sb.toString();
    return text;
  }
}
