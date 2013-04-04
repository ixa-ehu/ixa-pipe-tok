package ixa.opennlp.tok;

import java.io.IOException;
import java.io.InputStream;

import opennlp.tools.tokenize.TokenizerME;
import opennlp.tools.tokenize.TokenizerModel;

/**
 * A simple tokenizer based on Apache OpenNLP.
 * 
 * Model trained by IXA NLP Group
 * 
 * @author ragerri
 * 
 */
public class TokTokenizer {

  private TokenizerModel tokModel;
  private TokenizerME tokDetector;

  /**
   * This constructor loads a tokenization model, it initializes and creates a
   * tokDetector using such a model.
   */
  public TokTokenizer(InputStream trainedModel) {

    // InputStream trainedModel =
    // getClass().getResourceAsStream("/en-token.bin");

    try {
      tokModel = new TokenizerModel(trainedModel);

    } catch (IOException e) {
      e.printStackTrace();
    } finally {
      if (trainedModel != null) {
        try {
          trainedModel.close();
        } catch (IOException e) {
        }
      }
    }

    tokDetector = new TokenizerME(tokModel);

  }

  /**
   * @param sentence
   * @return an array of tokenized tokens
   */
  public String[] toker(String sentence) {
    String tokens[] = tokDetector.tokenize(sentence);
    return tokens;

  }
}
