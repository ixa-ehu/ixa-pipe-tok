/*
 *Copyright 2013 Rodrigo Agerri

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

/**
 * This class provides the functionality to create {@link Token} objects. E.g., the 
 * {@link IxaPipeTokenizer} uses the createToken function of this class to create
 * every {@link Token}
 * 
 * @author ragerri
 * @version 2013-11-25
 * 
 */

public class TokenFactory {

  final boolean addOffsets;

  /**
   * Constructor for a new token factory which will add in the word and the
   * begin/end position annotations.
   */
  public TokenFactory() {
    this(true);
  }

  /**
   * Constructor that allows one to choose if index annotation indicating
   * begin/end position will be included in the token.
   * 
   * @param addOffsets
   *          if true, offSet annotations will be included (this is the default)
   */
  public TokenFactory(boolean addOffsets) {
    this.addOffsets = addOffsets;
  }

  
  /**
   * Constructs a Token as a String with corresponding offsets and length from
   * which to calculate start and end position of the Token. (Does not take
   * substring).
   * 
   * @param tokenString string to be added to a Token object
   * @param offset the starting offset of the Token
   * @param length of the string
   * @return a new Token object
   * 
   */
  public Token createToken(String tokenString, int offset, int length) {
    Token token = new Token();
    token.setValue(tokenString);
    if (addOffsets) {
      token.setStartOffset(offset);
      token.setTokenLength(length);
    }
    return token;
  }

}
