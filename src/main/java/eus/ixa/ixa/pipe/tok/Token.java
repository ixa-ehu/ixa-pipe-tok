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
 * A <code>Token</code> object contains a single String, a startOffset and the
 * length of the String. These attributes are set or returned in response to
 * requests.
 * 
 * @author ragerri
 * @version 2015-04-14
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

  public Token(final String str) {
    token = str;
  }

  /**
   * Creates a new <code>Token</code> with the given content.
   * 
   * @param str
   *          The new token's value
   * @param startOffset
   *          Start offset in original text
   * @param tokenLength
   *          End offset in original text
   */
  public Token(final String str, final int startOffset, final int tokenLength) {
    token = str;
    setStartOffset(startOffset);
    setTokenLength(tokenLength);
  }

  public String getTokenValue() {
    return token;
  }

  /**
   * Set the value for the token.
   * 
   * @param value
   *          The value for the token
   */
  public void setTokenValue(final String value) {
    token = value;
  }

  @Override
  public String toString() {
    return token;
  }

  /**
   * Get the token starting offset.
   * @return the offset
   */
  public int startOffset() {
    return startOffset;
  }

  /**
   * Get the token length.
   * @return the length
   */
  public int tokenLength() {
    return tokenLength;
  }

  /**
   * Set the token offset.
   * @param beginPosition the startOffset
   */
  public void setStartOffset(final int beginPosition) {
    startOffset = beginPosition;
  }

  /**
   * Set the length of the token.
   * @param tokLength the length
   */
  public void setTokenLength(final int tokLength) {
    tokenLength = tokLength;
  }

}
