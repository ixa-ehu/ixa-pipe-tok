package ixa.pipe.tok;



/**
 * @author ragerri
 * @version 2013/11/25
 *
 */

public class TokenFactory {
  
  final boolean addOffsets;

  /**
   * Constructor for a new token factory which will add in the word 
   * and the begin/end position annotations.
   */
  public TokenFactory() {
    this(true);
  }

  /**
   * Constructor that allows one to choose if index annotation
   * indicating begin/end position will be included in the token.
   *
   * @param addOffsets if true, begin and end position annotations will be included (this is the default)
   */
  public TokenFactory(boolean addOffsets) {
    this.addOffsets = addOffsets;
  }

  /**
   * Constructs a Token as a String with a corresponding offsets for START and END position.
   * (Does not take substring).
   */
  public Token createToken(String tokenString, int startOffset, int length) {
    Token token = new Token();
    token.setValue(tokenString);
    if (addOffsets) { 
      token.setStartOffset(startOffset);
      token.setEndOffset(length);
    }
    return token;
  }

}
