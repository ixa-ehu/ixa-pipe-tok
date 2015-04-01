/*
 * Copyright 2013 Rodrigo Agerri

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

package es.ehu.si.ixa.ixa.pipe.tok;

import static es.ehu.si.ixa.ixa.pipe.resources.NonPrefixBreaker.ALPHA_APOS_ALPHA;
import static es.ehu.si.ixa.ixa.pipe.resources.NonPrefixBreaker.ALPHA_APOS_NOALPHA;
import static es.ehu.si.ixa.ixa.pipe.resources.NonPrefixBreaker.ASCII_HEX;
import static es.ehu.si.ixa.ixa.pipe.resources.NonPrefixBreaker.DASH;
import static es.ehu.si.ixa.ixa.pipe.resources.NonPrefixBreaker.DIGIT_COMMA_NODIGIT;
import static es.ehu.si.ixa.ixa.pipe.resources.NonPrefixBreaker.DOTMULTI_DOT;
import static es.ehu.si.ixa.ixa.pipe.resources.NonPrefixBreaker.DOTMULTI_DOT_ANY;
import static es.ehu.si.ixa.ixa.pipe.resources.NonPrefixBreaker.LINK;
import static es.ehu.si.ixa.ixa.pipe.resources.NonPrefixBreaker.MULTI_DOTS;
import static es.ehu.si.ixa.ixa.pipe.resources.NonPrefixBreaker.MULTI_SPACE;
import static es.ehu.si.ixa.ixa.pipe.resources.NonPrefixBreaker.NOALPHA_APOS_NOALPHA;
import static es.ehu.si.ixa.ixa.pipe.resources.NonPrefixBreaker.NOALPHA_DIGIT_APOS_ALPHA;
import static es.ehu.si.ixa.ixa.pipe.resources.NonPrefixBreaker.NODIGIT_COMMA_DIGIT;
import static es.ehu.si.ixa.ixa.pipe.resources.NonPrefixBreaker.NODIGIT_COMMA_NODIGIT;
import static es.ehu.si.ixa.ixa.pipe.resources.NonPrefixBreaker.QEXC;
import static es.ehu.si.ixa.ixa.pipe.resources.NonPrefixBreaker.SPECIALS;
import static es.ehu.si.ixa.ixa.pipe.resources.NonPrefixBreaker.YEAR_APOS;
import static es.ehu.si.ixa.ixa.pipe.resources.NonPrefixBreaker.LEFT_QUOTES;
import static es.ehu.si.ixa.ixa.pipe.resources.NonPrefixBreaker.RIGHT_QUOTES;

import java.io.InputStream;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import es.ehu.si.ixa.ixa.pipe.resources.NonPrefixBreaker;

public class RuleBasedTokenizer implements Tokenizer {
  
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
  
  

  NonPrefixBreaker nonBreaker;

  public RuleBasedTokenizer(InputStream nonBreakingFile, String lang) {
    nonBreaker = new NonPrefixBreaker(nonBreakingFile);
  }

  public String[] tokenize(String line, String lang) {
    String[] tokens = tokDetector(line, lang);
    return tokens;
  }

  /**
   * Main tokenizer function. It applies the tokenizing rules and treats with
   * language-dependent periods plus url links. Idea based on Moses tokenizer
   * 
   * 
   * @param line
   * @param lang
   * @return String[] containing where each member is a token of the input
   *         sentence
   */
  private String[] tokDetector(String line, String lang) {

    // remove extra spaces and ASCII stuff
    line = " " + line + " ";
    line = MULTI_SPACE.matcher(line).replaceAll(" ");
    line = normalizeQuotes(line, lang);
    line = ASCII_HEX.matcher(line).replaceAll("");
    // separate question and exclamation marks
    line = QEXC.matcher(line).replaceAll(" $1 ");
    // separate dash if before an upper case character 
    //line = DASH_LU.matcher(line).replaceAll("$1 $2");
    // separate dash if before or after space
    line = DASH.matcher(line).replaceAll(" $1 ");
    // separate out other special characters [^\p{Alnum}s.'`,-?!]
    line = SPECIALS.matcher(line).replaceAll(" $1 ");

    // do not separate multidots
    line = generateMultidots(line);

    // separate "," except if within numbers (1,200)
    line = NODIGIT_COMMA_NODIGIT.matcher(line).replaceAll("$1 , $2");
    // separate pre and post digit
    line = DIGIT_COMMA_NODIGIT.matcher(line).replaceAll("$1 , $2");
    line = NODIGIT_COMMA_DIGIT.matcher(line).replaceAll("$1 , $2");

    // //////////////////////////////////
    // // language dependent rules //////
    // //////////////////////////////////

    // contractions it's, l'agila
    line = treatContractions(line, lang);
    // non prefix breaker
    line = nonBreaker.TokenizerNonBreaker(line);

    // clean up extra spaces
    line = line.replaceAll("\\s+", " ");
    line = line.trim();

    // restore multidots
    line = restoreMultidots(line);

    // urls 
    line = detokenizeURLs(line);

    // create final array of tokens
    //System.out.println(line);
    String[] tokens = line.split(" ");

    // ensure final line break
    // if (!line.endsWith("\n")) { line = line + "\n"; }
    return tokens;
  }

  /**
   * 
   * This function normalizes multi-period expressions (...) to make
   * tokenization easier; it also keeps multidots together
   * 
   * @param line
   * @return string
   */
  private String generateMultidots(String line) {

    line = MULTI_DOTS.matcher(line).replaceAll(" DOTMULTI$1 ");
    Matcher dotMultiDot = DOTMULTI_DOT.matcher(line);

    while (dotMultiDot.find()) {
      line = DOTMULTI_DOT_ANY.matcher(line).replaceAll("DOTDOTMULTI $1");
      line = dotMultiDot.replaceAll("DOTDOTMULTI");
      // reset the matcher otherwise the while will stop after one run
      dotMultiDot.reset(line);
    }
    return line;
  }

  /**
   * restores the normalized multidots to its original state and it tokenizes
   * them
   * 
   * @param line
   * @return tokenized multidots
   */
  private String restoreMultidots(String line) {

    while (line.contains("DOTDOTMULTI")) {
      line = line.replaceAll("DOTDOTMULTI", "DOTMULTI.");
    }
    line = line.replaceAll("DOTMULTI", ".");
    return line;
  }

  /**
   * 
   * Using nonprefix_breaker.$lang files it tokenizes single quotes based on the
   * input language
   * 
   * @param line
   * @param lang
   * @return tokenized sinqle quotes expressions
   */
  private String treatContractions(String line, String lang) {

    if (lang.equalsIgnoreCase("en")) {
      line = NOALPHA_APOS_NOALPHA.matcher(line).replaceAll("$1 ' $2");
      line = NOALPHA_DIGIT_APOS_ALPHA.matcher(line).replaceAll("$1 ' $2");
      line = ALPHA_APOS_NOALPHA.matcher(line).replaceAll("$1 ' $2");
      line = ALPHA_APOS_ALPHA.matcher(line).replaceAll("$1 '$2");
      line = YEAR_APOS.matcher(line).replaceAll("$1 ' $2");
    } else {
      line = line.replaceAll("'", "' ");
    }
    return line;
  }
  
  /**
   * 
   * Using nonprefix_breaker.$lang files it tokenizes single quotes based on the
   * input language
   * 
   * @param line
   * @param lang
   * @return tokenized sinqle quotes expressions
   */
  private String normalizeQuotes(String line, String lang) {

    if (lang.equalsIgnoreCase("en")) {
      line = LEFT_QUOTES.matcher(line).replaceAll("`` $1");
      line = RIGHT_QUOTES.matcher(line).replaceAll("$1 ''");
    } else {
      line = line.replaceAll("'", "' ");
    }
    return line;
  }

  /**
   * It detects (wrongly tokenized) URLs and de-tokenizes them
   * 
   * @param line
   * @param lang
   * @return detokenized URL
   */
  private String detokenizeURLs(String line) {
    Matcher link = LINK.matcher(line);
    StringBuffer sb = new StringBuffer();
    while (link.find()) {
      link.appendReplacement(sb, link.group().replaceAll("\\s", ""));
    }
    link.appendTail(sb);
    line = sb.toString();
    return line;
  }

}
