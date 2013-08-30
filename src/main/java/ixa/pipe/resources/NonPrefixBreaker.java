package ixa.pipe.resources;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.HashMap;
import java.util.regex.Pattern;

public class NonPrefixBreaker {

  // loading NonBreakingPrefixes file
  public static String DOT_SPACE_NUMERIC_ONLY = "(\\.*)[\\s]+(#NUMERIC_ONLY#)";

  // Segmenter Patterns

  // non-period end of sentence markers (?!) followed by sentence starters.
  public static Pattern NOPERIOD_END = Pattern
      .compile("([?!])\\s+([\'\"\\(\\[\\¿\\¡\\p{Punct}]*[\\p{Upper}])");

  // multi-dots followed by sentence starters
  public static Pattern MULTI_DOTS_STARTERS = Pattern
      .compile("(\\.[\\.]+)\\s+([\'\"\\(\\[\\¿\\¡\\p{Punct}]*[\\p{Upper}])");

  // add breaks for sentences that end with some sort of punctuation inside a
  // quote or parenthetical and are
  // followed by a possible sentence starter punctuation and upper case
  public static Pattern END_INSIDE_QUOTES = Pattern
      .compile("([?!\\.][\\ ]*[\'\"\\)\\]\\p{Punct}]+)\\s+([\'\"\\(\\[\\¿\\¡\\p{Punct}]*[\\ ]*[\\p{Upper}])");

  // add breaks for sentences that end with some sort of punctuation are
  // followed
  // by a sentence starter punctuation and upper case
  public static Pattern NOPERIOD_END_SPACE = Pattern
      .compile("([?!])\\s+([\'\"\\(\\[\\¿\\¡\\p{Punct}]+[\\ ]*[\\p{Upper}])");
  public static Pattern ALPHANUM_PUNCT = Pattern
      .compile("([\\p{Alnum}\\.\\-]*)([\'\"\\)\\]\\%\\p{Punct}]*)(\\.+)");
  public static String STRING_ALPHANUM_PUNCT = "([\\p{Alnum}\\.\\-]*)([\'\"\\)\\]\\%\\p{Punct}]*)(\\.+)";

  // / Tokenizer Patterns

  public static Pattern MULTI_SPACE = Pattern.compile("\\s+");
  public static Pattern ASCII_DECIMALS = Pattern.compile("[000-037]");
  public static Pattern SPECIALS = Pattern
      .compile("([^\\p{Alnum}\\s\\.\'\\`\\,\\-])");
  public static Pattern MULTI_DOTS = Pattern.compile("\\.([\\.]+)");
  public static Pattern DOT_MULTI = Pattern.compile("DOTMULTI\\.");
  public static Pattern MULTI_MULTI_DOTS = Pattern
      .compile("DOTMULTI\\.([^\\.])");
  public static Pattern NODIGIT_COMMA_NODIGIT = Pattern
      .compile("([^\\d])[,]([^\\d])");
  public static Pattern DIGIT_COMMA_NODIGIT = Pattern
      .compile("([\\d])[,]([^\\d])");
  public static Pattern NODIGIT_COMMA_DIGIT = Pattern
      .compile("([^\\d])[,](\\d)");
  public static Pattern DOUBLE_QUOTES = Pattern.compile("\'\'");
  public static Pattern WORD_DOT = Pattern.compile("^(\\S+)\\.$");

  // english contractions patterns
  public static Pattern NOALPHA_APOS_NOALPHA = Pattern
      .compile("([^a-zA-Z])[']([^a-zA-Z])");
  public static Pattern NOALPHA_DIGIT_APOS_NOALPHA = Pattern
      .compile("([^A-Za-z]\\d])[\']([a-zA-Z])");
  public static Pattern ALPHA_APOS_NOALPHA = Pattern
      .compile("([a-zA-Z])[']([^A-Za-z])");
  public static Pattern ALPHA_APOS_ALPHA = Pattern
      .compile("([a-zA-Z])[']([a-zA-Z])");
  // special case for "1990's"
  public static Pattern YEAR_APOS = Pattern.compile("([\\d])[']([s])");

  private HashMap<String, String> dictMap;

  public NonPrefixBreaker(InputStream dictionary) {
    dictMap = new HashMap<String, String>();
    BufferedReader breader = new BufferedReader(new InputStreamReader(
        dictionary));
    String line;
    try {
      while ((line = breader.readLine()) != null) {
        line = line.trim();
        if (!line.startsWith("#")) {
          if (line.matches(DOT_SPACE_NUMERIC_ONLY)) {
            String pre = line.replaceAll(DOT_SPACE_NUMERIC_ONLY, "$1");
            dictMap.put(pre, "2");
          } else {
            dictMap.put(line, "1");

          }
        }
      }
    } catch (IOException e) {
      // TODO Auto-generated catch block
      e.printStackTrace();
    }
  }

  public String SegmenterNonBreaker(String line) {
    String segmentedText = null;
    String[] words = line.split(" ");
    StringBuilder sb = new StringBuilder();
    for (int i = 0; i < words.length; i++) {
      String word = words[i];

      if (word.matches(STRING_ALPHANUM_PUNCT)) {
        String pre = ALPHANUM_PUNCT.matcher(word).replaceAll("$1");
        String startPunct = ALPHANUM_PUNCT.matcher(word).replaceAll("$2");

        if (word.matches(pre) && dictMap.containsKey(pre)
            && (dictMap.get(pre) == "1") && !word.matches(startPunct)) {
          // not breaking
          return word;
        } else if (word.matches("(\\.)[\\p{Upper}\\-]+(\\.+)$")) {
          // non-breaking, upper case acronym
          return word;
        } else if (word
            .matches("^([ ]*[\'\"\\(\\[\\¿\\¡\\p{Punct}]*[ ]*[\\p{Upper}0-9])")) {
          // the next word has a bunch of initial quotes, maybe a space,
          // then either upper case or a number
          while (word.matches(pre) && dictMap.containsKey(pre)
              && (dictMap.get(pre) == "2") && !word.matches(startPunct)
              && words[i + 1].matches("^[0-9]+")) {
            word = word + "\n";
          }
          // we always add a return for these unless we have a numeric
          // non-breaker and a number start
        }
      }
      sb.append(word).append(" ");
      segmentedText = sb.toString();
    }
    return segmentedText;
  }

  /*
   * public String nonPrefixBreaker(String line) {
   * 
   * String[] words = line.split(" "); StringBuilder sb = new StringBuilder();
   * for (int i=0; i < words.length; i++) { String word = words[i];
   * 
   * if (word.matches("^(\\S+)\\.$")) { String pre =
   * WORD_DOT.matcher(word).replaceAll("$1");
   * 
   * if ((!pre.equalsIgnoreCase(".") && pre.matches("[A-Za-z]")) ||
   * //this.nonNumeric.contains(pre) || (i < (words.length)-1 &&
   * words[i+1].matches("[^a-z]"))) { return word; } else if
   * (this.numericOnly.contains(pre) && (i < (words.length)-1) &&
   * words[i+1].matches("^[0-9]+")) { return word; } else { word = pre + " ."; }
   * } sb.append(word).append(" "); line = sb.toString(); } return line; }
   */

}
