package eus.ixa.ixa.pipe.tok;

import java.util.List;
import java.util.regex.Pattern;

public class StringUtils {
  
  public static Pattern doubleBar = Pattern.compile("\\|\\|");

  private StringUtils() {
    
  }
  
  public static String getStringFromTokens(final String[] tokens) {
    StringBuilder sb = new StringBuilder();
    for (String tok : tokens) {
      sb.append(tok).append(" ");
    }
    if (sb.length() > 0) {
       sb.setLength(sb.length() - 1);
    }
    return sb.toString();
  }
  
  public static String getStringFromTokens(final List<Token> tokens) {
    StringBuilder sb = new StringBuilder();
    for (Token tok : tokens) {
      sb.append(tok.getTokenValue()).append(" ");
    }
    if (sb.length() > 0) {
       sb.setLength(sb.length() - 1);
    }
    return sb.toString();
  }
  
  public static String createDisjunctRegexFromList(List<String> words) {
    StringBuilder sb = new StringBuilder();
    for (String word : words) {
      sb.append(word).append("|");
    }
    String regExp = sb.toString();
    regExp = doubleBar.matcher(regExp).replaceAll("\\|");
    String result = regExp.substring(1, (regExp.length() - 1));
    return result;
  }
}
