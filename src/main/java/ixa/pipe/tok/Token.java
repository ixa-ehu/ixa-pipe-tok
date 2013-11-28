package ixa.pipe.tok;

/**
 * A <code>Token</code> object acts as a Label by containing a
 * single String, which it sets or returns in response to requests.
 * 
 * @author ragerri
 * @version 2013/11/25
 *
*/

public class Token {

  
    private String str;

    /**
     * Start position of the word in the original input string
     */
    private int startOffset = -1;

    /**
     * Length of the word in the original input string
     */
    private int tokenLength = -1;


    /**
     * Create a new <code>Token</code> with a null content (i.e., str).
     */
    public Token() {
    }


    /**
     * Create a new <code>Token</code> with the given content.
     *
     * @param str The new label's content
     */
    public Token(String str) {
      this.str = str;
    }

    /**
     * Create a new <code>Token</code> with the given content.
     *
     * @param str The new label's content
     * @param startOffset Start offset in original text
     * @param tokenLength End offset in original text
     */
    public Token(String str, int startOffset, int tokenLength) {
      this.str = str;
      setStartOffset(startOffset);
      setTokenLength(tokenLength);
    }


    /**
     * Return the word value of the label (or null if none).
     *
     * @return String the word value for the label
     */
    public String value() {
      return str;
    }


    /**
     * Set the value for the label.
     *
     * @param value The value for the label
     */
    public void setValue(final String value) {
      str = value;
    }


    /**
     * Set the label from a String.
     *
     * @param str The str for the label
     */
    public void setFromString(final String str) {
      this.str = str;
    }

    @Override
    public String toString() {
      return str;
    }

    public int startOffset()
    {
      return startOffset;
    }

    public int tokenLength()
    {
      return tokenLength;
    }

    public void setStartOffset(int beginPosition)
    {
      this.startOffset = beginPosition;
    }

    public void setTokenLength(int tokLength)
    {
      this.tokenLength = tokLength;
    }


}
