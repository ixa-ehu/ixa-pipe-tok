package ixa.pipe.resources;

import java.util.regex.Pattern;

public class Normalizer {

  
  private static final String APOS = "['\u0092\u2019]|&apos]";
  private static final Pattern REDAUX = Pattern.compile("APOS([msdMSD]|re|ve|ll)");
  
  ////////////////
  //// QUOTES ////
  ////////////////
  
  // to convert to LATEX 
  private static final Pattern singleQuote = Pattern.compile("&apos;|'");
  private static final Pattern doubleQuote = Pattern.compile("\"|&quot;");

  // 91,92,93,94 aren't valid unicode points, but sometimes they show
  // up from cp1252 and need to be converted 
  private static final Pattern leftSingleQuote = Pattern.compile("[\u0091\u2018\u201B\u2039]");
  private static final Pattern rightSingleQuote = Pattern.compile("[\u0092\u2019\u203A]");
  private static final Pattern leftDoubleQuote = Pattern.compile("[\u0093\u201C\u00AB]");
  private static final Pattern rightDoubleQuote = Pattern.compile("[\u0094\u201D\u00BB]");
  
  // to convert to ASCII 
  private static final Pattern asciiSingleQuote = Pattern.compile("&apos;|[\u0091\u2018\u0092\u2019\u201A\u201B\u2039\u203A']");
  private static final Pattern asciiDoubleQuote = Pattern.compile("&quot;|[\u0093\u201C\u0094\u201D\u201E\u00AB\u00BB\"]");
  
  // to convert to UNICODE 
  private static final Pattern unicodeLeftSingleQuote = Pattern.compile("\u0091");
  private static final Pattern unicodeRightSingleQuote = Pattern.compile("\u0092");
  private static final Pattern unicodeLeftDoubleQuote = Pattern.compile("\u0093");
  private static final Pattern unicodeRightDoubleQuote = Pattern.compile("\u0094");

  
  public String latexQuotes(String in, boolean probablyLeft) {
    String s1 = in;
    if (probablyLeft) {
      s1 = singleQuote.matcher(s1).replaceAll("`");
      s1 = doubleQuote.matcher(s1).replaceAll("``");
    } else {
      s1 = singleQuote.matcher(s1).replaceAll("'");
      s1 = doubleQuote.matcher(s1).replaceAll("''");
    }
    s1 = leftSingleQuote.matcher(s1).replaceAll("`");
    s1 = rightSingleQuote.matcher(s1).replaceAll("'");
    s1 = leftDoubleQuote.matcher(s1).replaceAll("``");
    s1 = rightDoubleQuote.matcher(s1).replaceAll("''");
    return s1;
  }
  
  public String asciiQuotes(String in) {
    String s1 = in;
    s1 = asciiSingleQuote.matcher(s1).replaceAll("'");
    s1 = asciiDoubleQuote.matcher(s1).replaceAll("\"");
    return s1;
  }
  
  
  public static String unicodeQuotes(String in, boolean probablyLeft) {
    String s1 = in;
    if (probablyLeft) {
      s1 = singleQuote.matcher(s1).replaceAll("\u2018");
      s1 = doubleQuote.matcher(s1).replaceAll("\u201c");
    } else {
      s1 = singleQuote.matcher(s1).replaceAll("\u2019");
      s1 = doubleQuote.matcher(s1).replaceAll("\u201d");
    }
    s1 = unicodeLeftSingleQuote.matcher(s1).replaceAll("\u2018");
    s1 = unicodeRightSingleQuote.matcher(s1).replaceAll("\u2019");
    s1 = unicodeLeftDoubleQuote.matcher(s1).replaceAll("\u201c");
    s1 = unicodeRightDoubleQuote.matcher(s1).replaceAll("\u201d");
    return s1;
  }
  
  public String ptb3normalize(String in) {
    String normTok = in;
    //normTok = latexQuotes(normTok);
    return normTok;
  } 

  
  public String cleanWeirdChars(String line) {
    line = line.replaceAll("’", "'");
    line = line.replaceAll("’", "'");
    line = line.replaceAll("‘", "'");
    line = line.replaceAll("“", "\"");
    line = line.replaceAll("”", "\"");
    return line;

  }

}
