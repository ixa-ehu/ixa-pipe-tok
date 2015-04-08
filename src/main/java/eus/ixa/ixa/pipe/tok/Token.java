/*
 *Copyright 2015 Rodrigo Agerri

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
package eus.ixa.ixa.pipe.tok;


/**
 * A <code>Token</code> object contains a single String, a startOffset and 
 * the length of the String. These attributes are set or returned
 * in response to requests.
 * 
 * @author ragerri
 * @version 2013-11-25
 * 
 */

public class Token {

  private String token;

  /**
   * Start position of the word in the original input string
   */
  private int startOffset = -1;

  /**
   * Length of the word in the original input string
   */
  private int tokenLength = -1;
  
  /**
   * Create a new token with a null content.
   */
  public Token() {
  }

  public Token(String str) {
    this.token = str;
  }

  /**
   * Creates a new <code>Token</code> with the given content.
   * 
   * @param str
   *          The new label's content
   * @param startOffset
   *          Start offset in original text
   * @param tokenLength
   *          End offset in original text
   */
  public Token(String str, int startOffset, int tokenLength) {
    this.token = str;
    setStartOffset(startOffset);
    setTokenLength(tokenLength);
  }

  public String getTokenValue() {
    return token;
  }

  /**
   * Set the value for the label.
   * 
   * @param value
   *          The value for the label
   */
  public void setTokenValue(final String value) {
    token = value;
  }

  @Override
  public String toString() {
    return token;
  }

  public int startOffset() {
    return startOffset;
  }

  public int tokenLength() {
    return tokenLength;
  }

  public void setStartOffset(int beginPosition) {
    this.startOffset = beginPosition;
  }

  public void setTokenLength(int tokLength) {
    this.tokenLength = tokLength;
  }

}

