/*
 * Copyright 2014 Rodrigo Agerri

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

package es.ehu.si.ixa.pipe.tok;

import java.io.BufferedReader;
import java.io.IOException;


/**
 *  WhiteSpaceTokenizer is based on the {@link WhiteSpaceLexer} class. 
 *  This Tokenizer overrides {@link AbstractTokenizer} getToken() method 
 *  by using the {@link WhiteSpaceLexer} yylex() method.
 *  
 *  The tokenizer detects whitespaces to separate tokens. It can also detect 
 *  and mark newlines and paragraphs using the CLI option --paragraphs.
 *  
 * @author ragerri
 * @version 2014-01-30
 * 
 */
 
public class WhiteSpaceTokenizer<T> extends AbstractTokenizer<T> {

  
  private WhiteSpaceLexer jlexer;
  
  /**
   * Construct a new Tokenizer which uses the @link JFlexLexer specification
   * 
   * 
   * @param breader Reader
   * @param tokenFactory The TokenFactory that will be invoked to convert
   *        each string extracted by the @link JFlexLexer  into a @Token object
   * @param options Options to the Tokenizer (the values of the -normalize parameter)
   * 
   */
  public WhiteSpaceTokenizer(BufferedReader breader, TokenFactory tokenFactory, String options) {
    jlexer = new WhiteSpaceLexer(breader, tokenFactory, options);
  }

  /**
   * It obtains the next token. This functions performs the actual tokenization 
   * by calling the @link JFlexLexer yylex() function
   *
   * @return the next token or null if none exists.
   */
  @Override
  @SuppressWarnings("unchecked")
  public T getToken() {
    try {
      return (T) jlexer.yylex();
    } catch (IOException e) {
      // TODO Auto-generated catch block
      e.printStackTrace();
    }
    return nextToken;
  }
  
}
