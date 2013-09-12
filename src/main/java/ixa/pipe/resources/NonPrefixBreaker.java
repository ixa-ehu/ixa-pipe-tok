package ixa.pipe.resources;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.HashMap;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class NonPrefixBreaker {
  
  // Parse nonbreaking_prefix.$lang file for non breaking exceptions
  public static Pattern DOT_SPACE_NUMERIC_ONLY = Pattern.
      compile("(.*)\\s+(\\#NUMERIC_ONLY\\#)");

  // useful patterns existing in Perl not in Java
  //using unicode java code for these characters »’”› and its counterparts
  public static Pattern INITIAL_PUNCT = Pattern.compile("[\'\"\\(\\[\\¿\\¡\u00AB\u2018\u201B\u201C\u201F\u2039]");
  public static Pattern FINAL_PUNCT = Pattern.compile("[\'\"\\)\\]\\%\u00BB\u2019\u201D\u203A]");
  
  // Segmenter Patterns

  // non-period end of sentence markers (?!) followed by sentence starters.
  public static Pattern NOPERIOD_END = Pattern
      .compile("([?!])\\s+([\'\"\\(\\[\\¿\\¡\u00AB\u2018\u201B\u201C\u201F\u2039]*[\\p{Lu}])");
  
  // multi-dots followed by sentence starters
  public static Pattern MULTI_DOTS_STARTERS = Pattern
      .compile("(\\.[\\.]+)\\s+([\'\"\\(\\[\\¿\\¡\u00AB\u2018\u201B\u201C\u201F\u2039]*[\\p{Lu}])");

  // some sort of punctuation inside a quote or parenthetical followed 
  // by a possible sentence starter punctuation and upper case
  //public static Pattern END_INSIDE_QUOTES = Pattern
  //    .compile("([?!\\.][\\ ]*[\'\"\\)\\]\\p{Punct}]+)\\s+([\'\"\\(\\[\\¿\\¡\\p{IsPunct}]*[\\ ]*[\\p{Lu}])");
  public static Pattern END_INSIDE_QUOTES = Pattern
      .compile("([?!\\.][\\ ]*[\'\"\\)\\]\\%\u00BB\u2019\u201D\u203A]+)\\s+([\'\"\\(\\[\\¿\\¡\u00AB\u2018\u201B\u201C\u201F\u2039]*[\\ ]*[\\p{Lu}])");
  
  // end with some sort of punctuation and followed by a sentence 
  // starter punctuation and upper case 
  public static Pattern PUNCT_UPPER = Pattern
      .compile("([?!\\.])\\s+([\'\"\\(\\[\\¿\\¡\u00AB\u2018\u201B\u201C\u201F\u2039]+[\\ ]*[\\p{Lu}])");
  
  // SPECIAL PUNCTUATION CASES COVERED. CHECK FOR REMAINING PERIODS
    
  public static Pattern ALPHANUM_PUNCT = Pattern
      .compile("([\\p{Alnum}\\.\\-]*)([\'\"\\)\\]\\%\u00BB\u2019\u201D\u203A]*)(\\.+)$");
  
  public static Pattern UPPER_CASE_ACRONYM = Pattern.compile("(\\.)[\\p{Lu}\\-]+(\\.+)$");
  
  public static Pattern START_DIGITS = Pattern.compile("^\\d+");
  public static Pattern QUOTE_SPACE_UPPER_NUMBER = Pattern.
      compile("^( *[\'\"\\(\\[\\¿\\¡\\p{Punct}]* *[\\p{Lu}\\d])");
  

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
          Matcher numonly = DOT_SPACE_NUMERIC_ONLY.matcher(line);
          if (numonly.matches()) {
            String pre = numonly.replaceAll("$1");
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
    int i;
    StringBuilder sb = new StringBuilder();
    for (i = 0; i < (words.length-1); i++) {
      
      Matcher finalPunct = FINAL_PUNCT.matcher(words[i]);
      Matcher alphanum = ALPHANUM_PUNCT.matcher(words[i]);
      Matcher upperAcro = UPPER_CASE_ACRONYM.matcher(words[i]);
      Matcher upper = QUOTE_SPACE_UPPER_NUMBER.matcher(words[i+1]);
      Matcher startDigits = START_DIGITS.matcher(words[i+1]);
      
      if (alphanum.find()) {
        String prefix = alphanum.replaceAll("$1");
        if (words[i].contains(prefix) && dictMap.containsKey(prefix)
           && (dictMap.get(prefix) == "1") && !finalPunct.find()) {
          // not breaking 
          //return words[i];
        }
        
        else if (upperAcro.find()) {
          // non-breaking, upper case acronym
          //return words[i];
        }
        
        // the next word has a bunch of initial quotes, maybe a space,
        // then either upper case or a number
        else if (upper.find()) {

          // literal implementation from unless in perl:
          if (!(words[i].contains(prefix) && dictMap.containsKey(prefix) && (dictMap.get(prefix) == "2") && 
              !finalPunct.find() && startDigits.find())) {
            words[i] = words[i] + "\n";
          }
          // equivalent if-then applying De Morgan theorem:
          /*if (!words[i].contains(prefix) || !dictMap.containsKey(prefix)
              || (dictMap.get(prefix) != "2") || finalPunct.find()
              || !startDigits.find()) {
            words[i] = words[i] + "\n";
          }*/
          
          // we always add a return for these unless we have a numeric
          // non-breaker and a number start
        }
      }
      sb.append(words[i]).append(" ");
      segmentedText = sb.toString();
    }
    // add last index of words array removed for easy look ahead
    segmentedText = segmentedText + words[i];
    return segmentedText;
  }
}
