package eus.ixa.ixa.pipe.tok;

import java.util.List;

public class StringUtils {

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
}
