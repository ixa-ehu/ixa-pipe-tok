package ixa.pipe.tok.eval;

import ixa.pipe.tok.Token;
import ixa.pipe.tok.Tokenizer;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import org.apache.commons.io.FileUtils;

/**
 * The {@link TokenizerEvaluator} measures the performance of
 * the given {@link Tokenizer} with the provided reference
 * {@link TokenSample}s.
 *
 * @see Evaluator
 * @see Tokenizer
 * @see TokenSample
 */
public class TokenizerEvaluator {

  private FMeasure fmeasure = new FMeasure();
  public static final boolean DEBUG = true;
  
  /**
   * Initializes the current instance with the
   * given {@link Tokenizer}.
   *
   * @param tokenizer the {@link Tokenizer} to evaluate.
   * @param listeners evaluation sample listeners
   */
  public TokenizerEvaluator() {
  }
  
  private Integer[] referenceTokens(List<String> referenceText) {
    
    List<Integer> referenceLengths = new ArrayList<Integer>();
    for (String ref : referenceText) {
      if (!ref.isEmpty()) { 
      referenceLengths.add(ref.split(" ")[0].length());
      if (DEBUG) {
        StringBuilder sb = new StringBuilder();
        sb.append(ref.split(" ")[0]).append(" ").append(ref.split(" ")[0].length()).append("\n");
        try {
          FileUtils.write(new File("reference-tokens.log"), sb.toString(), true);
        } catch (IOException e) {
          // TODO Auto-generated catch block
          e.printStackTrace();
        }
      }
      }
    }
    return referenceLengths.toArray(new Integer[referenceLengths.size()]);
  }
  
  private Integer[] predictionTokens(List<Token> tokenizedList) { 
    
    List<Integer> predictionLengths = new ArrayList<Integer>();
    
    for (Token tok : tokenizedList) {
      if ((!tok.value().equals("*<P>*") || !tok.value().equals("*NL*")) && tok.value().matches("\\S+")) { 
        predictionLengths.add(tok.tokenLength());
        if (DEBUG) {
          StringBuilder sb = new StringBuilder();
          sb.append(tok.value()).append(" ").append(tok.tokenLength()).append("\n");
          try {
            FileUtils.write(new File("prediction-tokens.log"), sb.toString(), true);
          } catch (IOException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
          }
        }
      }
    }
    return predictionLengths.toArray(new Integer[predictionLengths.size()]);
  }
  
  
  /**
   * Evaluates the given reference sample object.
   * 
   * The implementation has to update the score after every invocation.
   *
   * @param reference the reference sample.
   * 
   * @return the predicted sample
   */
  //protected T processSample(T reference) {
    // should be overridden by subclass... in the future we will make it abstract.
  //  return null;
  //}
  
  public Integer[] evaluate(List<String> referenceText, List<Token> tokenizedList)  {
    Integer[] references = referenceTokens(referenceText);
    Integer[] predictions = predictionTokens(tokenizedList);
    fmeasure.updateScores(references, predictions);
    return predictions;
    
  }
  
  public FMeasure getFMeasure() {
    return fmeasure;
  }
}
