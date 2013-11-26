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

package ixa.pipe.tok;

import static ixa.pipe.resources.NonPrefixBreaker.ALPHA_APOS_ALPHA;
import static ixa.pipe.resources.NonPrefixBreaker.ALPHA_APOS_NOALPHA;
import static ixa.pipe.resources.NonPrefixBreaker.ASCII_HEX;
import static ixa.pipe.resources.NonPrefixBreaker.DASH;
import static ixa.pipe.resources.NonPrefixBreaker.DASH_LU;
import static ixa.pipe.resources.NonPrefixBreaker.DIGIT_COMMA_NODIGIT;
import static ixa.pipe.resources.NonPrefixBreaker.DOTMULTI_DOT;
import static ixa.pipe.resources.NonPrefixBreaker.DOTMULTI_DOT_ANY;
import static ixa.pipe.resources.NonPrefixBreaker.MULTI_DOTS;
import static ixa.pipe.resources.NonPrefixBreaker.MULTI_SPACE;
import static ixa.pipe.resources.NonPrefixBreaker.NOALPHA_APOS_NOALPHA;
import static ixa.pipe.resources.NonPrefixBreaker.NOALPHA_DIGIT_APOS_ALPHA;
import static ixa.pipe.resources.NonPrefixBreaker.NODIGIT_COMMA_DIGIT;
import static ixa.pipe.resources.NonPrefixBreaker.NODIGIT_COMMA_NODIGIT;
import static ixa.pipe.resources.NonPrefixBreaker.QEXC;
import static ixa.pipe.resources.NonPrefixBreaker.SPECIALS;
import static ixa.pipe.resources.NonPrefixBreaker.YEAR_APOS;
import ixa.pipe.resources.NonPrefixBreaker;

import java.io.BufferedReader;
import java.io.IOException;
import java.util.List;
import java.util.regex.Matcher;



public class IXATokenizer<T> extends AbstractTokenizer<T> {

  NonPrefixBreaker nonBreaker;
  
  private JFlexTokenizer jlexer;
  
  
  // TODO Americanize
  public IXATokenizer(BufferedReader breader, TokenFactory tokenFactory, String options) {
    jlexer = new JFlexTokenizer(breader, tokenFactory, options);
  }

  /**
   * Internally fetches the next token.
   *
   * @return the next token in the token stream, or null if none exists.
   */
  @Override
  @SuppressWarnings("unchecked")
  protected T getToken() {
    try {
      return (T) jlexer.yylex();
    } catch (IOException e) {
      // TODO Auto-generated catch block
      e.printStackTrace();
    }
    return nextToken;
  }
  
  /**
   * Main tokenizer function. It applies the tokenizing rules and treats with
   * language-dependent periods plus url links.
   * 
   * @param line
   * @param lang
   * @return String[] containing where each member is a token of the input
   *         sentence
   */
  private String[] tokDetector1(String line, String lang) {

    // remove extra spaces and ASCII stuff
    line = " " + line + " ";
    line = MULTI_SPACE.matcher(line).replaceAll(" ");
    line = ASCII_HEX.matcher(line).replaceAll("");
    // separate question and exclamation marks
    line = QEXC.matcher(line).replaceAll(" $1 ");
    // separate dash if before an upper case character 
    line = DASH_LU.matcher(line).replaceAll("$1 $2");
    // separate dash if before or after space
    line = DASH.matcher(line).replaceAll(" $1 ");
    // separate out other special characters [^\p{Alnum}s.'`,-?!]
    line = SPECIALS.matcher(line).replaceAll(" $1 ");

    // do not separate multidots
    line = generateMultidots(line);

    // separate "," except if within numbers (5,300)
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

    // clean up extraneous spaces
    line = line.replaceAll("\\s+", " ");
    line = line.trim();

    // restore multidots
    line = restoreMultidots(line);

    // urls 

    // create final array of tokens
    //System.out.println(line);
    String[] tokens = line.split(" ");

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

}
