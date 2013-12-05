package ixa.pipe.tok;
  
  import java.util.ArrayList;
import java.util.List;
import java.util.NoSuchElementException;

  /**
   * An abstract tokenizer.  Tokenizers extending AbstractTokenizer need only
   * implement the <code>getToken()</code> method. This implementation does not
   * allow null tokens, since
   * null is used in the protected nextToken field to signify that no more
   * tokens are available.
   *
   * @author ragerri
   */

  public abstract class AbstractTokenizer<T> implements Tokenizer<T> {

    protected T nextToken; // = null;

    /**
     * Internally fetches the next token.
     *
     * @return the next token in the token stream, or null if none exists.
     */
    protected abstract T getToken();

    /**
     * Returns the next token from this Tokenizer.
     *
     * @return the next token in the token stream.
     * @throws java.util.NoSuchElementException
     *          if the token stream has no more tokens.
     */
    public T next() {
      if (nextToken == null) {
        nextToken = getToken();
      }
      T result = nextToken;
      nextToken = null;
      if (result == null) {
        throw new NoSuchElementException();
      }
      return result;
    }

    /**
     * Returns <code>true</code> if this Tokenizer has more elements.
     */
    public boolean hasNext() {
      if (nextToken == null) {
        nextToken = getToken();
      }
      return nextToken != null;
    }

    /**
     * This is an optional operation, by default not supported.
     */
    public void remove() {
      throw new UnsupportedOperationException();
    }

    /**
     * This is an optional operation, by default supported.
     *
     * @return The next token in the token stream.
     * @throws java.util.NoSuchElementException
     *          if the token stream has no more tokens.
     */
    public T peek() {
      if (nextToken == null) {
        nextToken = getToken();
      }
      if (nextToken == null) {
        throw new NoSuchElementException();
      }
      return nextToken;
    }

    /**
     * Returns text as a List of tokens.
     *
     * @return A list of all tokens remaining in the underlying Reader
     */
    public List<T> tokenize() {
      final long start = System.nanoTime();
      List<T> result = new ArrayList<T>();
      while (hasNext()) {
        result.add(next());
      }
      final long duration = System.nanoTime() - start;
      final double toksPerSecond = (double) result.size() / ((double) duration / 1000000000.0);
      System.err.printf("ixa-pipe-tok tokenized %d tokens at %.2f tokens per second.%n", result.size(), toksPerSecond);
      return result;
    }


}
