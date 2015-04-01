package es.ehu.si.ixa.ixa.pipe.resources;

import java.util.regex.Pattern;

public class Normalizer {

  
  private static final String APOS = "['\u0092\u2019]|&apos]";
  private static final Pattern APOSETCETERA = Pattern.compile("APOS|[\u0091\u2018\u201B]");
  private static final Pattern REDAUX = Pattern.compile("APOS([msdMSD]|re|ve|ll)");
  
  ////////////////
  //// QUOTES ////
  ////////////////
  
  // to convert to LATEX 
  private static final Pattern SINGLEQUOTE = Pattern.compile("&apos;|'");
  private static final Pattern DOUBLEQUOTE = Pattern.compile("\"|&quot;");

  // 91,92,93,94 aren't valid unicode points, but sometimes they show
  // up from cp1252 and need to be converted 
  private static final Pattern LEFT_SINGLE_QUOTE = Pattern.compile("[\u0091\u2018\u201B\u2039]");
  private static final Pattern RIGHT_SINGLE_QUOTE = Pattern.compile("[\u0092\u2019\u203A]");
  private static final Pattern LEFT_DOUBLE_QUOTE = Pattern.compile("[\u0093\u201C\u00AB]");
  private static final Pattern RIGHT_DOUBLE_QUOTE = Pattern.compile("[\u0094\u201D\u00BB]");
  
  // to convert to ASCII 
  private static final Pattern ASCII_SINGLE_QUOTE = Pattern.compile("&apos;|[\u0091\u2018\u0092\u2019\u201A\u201B\u2039\u203A']");
  private static final Pattern ASCII_DOUBLE_QUOTE = Pattern.compile("&quot;|[\u0093\u201C\u0094\u201D\u201E\u00AB\u00BB\"]");
  
  // to convert to UNICODE 
  private static final Pattern UNICODE_LEFT_SINGLE_QUOTE = Pattern.compile("\u0091");
  private static final Pattern UNICODE_RIGHT_SINGLE_QUOTE = Pattern.compile("\u0092");
  private static final Pattern UNICODE_LEFT_DOUBLE_QUOTE = Pattern.compile("\u0093");
  private static final Pattern UNICODE_RIGHT_DOUBLE_QUOTE = Pattern.compile("\u0094");
  
  
  public String latexQuotes(String in) {
    String s1 = in;
    s1 = LEFT_SINGLE_QUOTE.matcher(s1).replaceAll("`");
    s1 = RIGHT_SINGLE_QUOTE.matcher(s1).replaceAll("'");
    s1 = LEFT_DOUBLE_QUOTE.matcher(s1).replaceAll("``");
    s1 = RIGHT_DOUBLE_QUOTE.matcher(s1).replaceAll("''");
    return s1;
  }
  
  public String asciiQuotes(String in) {
    String s1 = in;
    s1 = ASCII_SINGLE_QUOTE.matcher(s1).replaceAll("'");
    s1 = ASCII_DOUBLE_QUOTE.matcher(s1).replaceAll("\"");
    return s1;
  }
  
  
  public static String unicodeQuotes(String in, boolean probablyLeft) {
    String s1 = in;
    if (probablyLeft) {
      s1 = SINGLEQUOTE.matcher(s1).replaceAll("\u2018");
      s1 = DOUBLEQUOTE.matcher(s1).replaceAll("\u201c");
    } else {
      s1 = SINGLEQUOTE.matcher(s1).replaceAll("\u2019");
      s1 = DOUBLEQUOTE.matcher(s1).replaceAll("\u201d");
    }
    s1 = UNICODE_LEFT_SINGLE_QUOTE.matcher(s1).replaceAll("\u2018");
    s1 = UNICODE_RIGHT_SINGLE_QUOTE.matcher(s1).replaceAll("\u2019");
    s1 = UNICODE_LEFT_DOUBLE_QUOTE.matcher(s1).replaceAll("\u201c");
    s1 = UNICODE_RIGHT_DOUBLE_QUOTE.matcher(s1).replaceAll("\u201d");
    return s1;
  }
  
  public String ptb3normalize(String in) {
    String normTok = in;
    normTok = latexQuotes(normTok);
    return normTok;
  } 

}
