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

import java.io.BufferedReader;
import java.io.IOException;
import java.util.List;


/**
 *  IXATokenizer is based on the {@link JFlexTokenizer} class.  Here IXATokenizer 
 *  overrides {@link AbstractTokenizer} getToken() method calling the {@link JFlexTokenizer} yylex() 
 *  method instead. 
 *  
 *  Most of the rules taken/adapted from PTBLexer class of Stanford CoreNLP 3.2.0.
 *  
 * This tokenizer provides options to configure:
 * - language for language-dependent apostrophe treatment and
 * - normalization method. 
 * 
 *  By default, the tokenizer does PTB3 normalization style except brackets, forward 
 *  slashes and duplication of dots after acronym at sentence end (option "en" below).  
 *  
 *  To change these options, the CLI currently provides four parameters: 
 * 
 * <ol>
 * <li>sptb3: Strict Penn Treebank normalization. 
 * IXATokenizer ptb3 option below deviates from strict PTB3
 * WSJ tokenization in two cases: (i) When an acronym is followed by a
 * sentence end, such as "Corp." at the end of a sentence, the PTB3
 * has tokens of "Corp" and ".", while by default IXATokenizer duplicates
 * the period returning tokens of "Corp." and "."; and (ii) IXATokenizer
 * will return numbers with a whole number and a fractional part like
 * "5 7/8" as a single token (with a non-breaking space in the middle),
 * while the PTB3 separates them into two tokens "5" and "7/8".
 * (Exception: for "U.S." the treebank does have the two tokens
 * "U.S." and "." like our default; sptb3 does that too.) 
 * <li> ptb3: Activates all traditional PTB3 normalizations. These are:
 * <li> en: ptb3 minus brackets and forward slash normalizations. The DEFAULT.
 * <li> Ancora: Ancora corpus based normalization.
 * </ol> 
 * 
 * Each of the four options either activate or switch off the following specific
 * normalizations: 
 * <ol>
 * <li>americanize: Rewrite British English spellings using American English
 * <li>normalizeSpace: Whether any spaces in tokens (phone numbers, fractions
 *     get turned into U+00A0 (non-breaking space).  It's dangerous to turn
 *     this off for most as it is usually assumed that there are not spaces in tokens.
 * <li>normalizeAmpersand: Normalize the XML &amp;amp; intoto an ampersand
 * <li>normalizeCurrency: Clumsy currency mappings into $, #, or "cents", reflecting
 *     the fact that nothing else appears in the old PTB3 WSJ (not Euro). 
 * <li>normalizeFractions: Normalize fraction characters to spelled out letter forms like "1/2"
 * <li>normalizeBrackets: Normalized round brackets to -LRB- and -RRB- 
 * <li>normalizeOtherBrackets: Normalize other common bracket characters
 *     to -LCB-, -LRB-, -RCB-, -RRB-
 * <li>asciiQuotes: Normalize quote characters to ascii ' and "
 * <li>latexQuotes: Normalize to ``, `, ', '' for every quote, as in Latex
 *     and the PTB3 WSJ (though this is now discouraged by Unicode).
 *     If true, this takes precedence over the setting of unicodeQuotes;
 *     if both are false, no mapping is done.
 * <li>unicodeQuotes: Whether to normalized quotes to the range U+2018 to U+201D,
 *     the preferred Unicode encoding of single and double quotes.
 * <li>ptb3Ldots: Whether to map ellipses to ..., the old PTB3 WSJ coding
 *     of an ellipsis. If true, this takes precedence over the setting of
 *     unicodeLdots; if both are false, no normalization is performed. .
 * <li>unicodeLdots: Whether to map dot and optional space sequences to
 *     U+2026, the Unicode ellipsis character
 * <li>ptb3Dashes: Whether to turn various dash characters into "--",
 *     the dominant encoding of dashes in the PTB3 WSJ
 * <li>escapeForwardSlash: Whether to put a backslash escape in front
 *     of / and * as the old PTB3 WSJ does.
 * </ol>
 *
 * @param breader The Reader to tokenize text from
 * @param tokenFactory The TokenFactory that will be invoked to convert
 *    each substring extracted by the lexer into a @Token
 * @param options Options to the tokenizer
 * 
 * @author ragerri
 * @version 2013/11/27
 * 
 * */
 
public class IXATokenizer<T> extends AbstractTokenizer<T> {

  
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
  
}
