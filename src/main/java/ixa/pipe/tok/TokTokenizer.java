package ixa.pipe.tok;

public interface TokTokenizer {

  /**
   * Takes a sentence (already segmented) and performs tokenization.
   * 
   * @param String
   *          segmented sentence
   * @param lang
   * @return String[] where each element is a token
   */
  public String[] tokenize(String sentence, String lang);

}
