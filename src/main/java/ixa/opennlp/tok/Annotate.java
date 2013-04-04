/**
 * 
 */
package ixa.opennlp.tok;

import java.io.IOException;
import java.io.InputStream;

/**
 * @author ragerri
 * 
 */
public class Annotate {

  private Segmenter sentDetector;
  private TokTokenizer toker;
  

  public Annotate(String cmdOption) {
    Models modelRetriever = new Models();
    InputStream segModel = modelRetriever.getSegModel(cmdOption);
    sentDetector = new Segmenter(segModel);
    InputStream tokModel = modelRetriever.getTokModel(cmdOption);
    toker = new TokTokenizer(tokModel);
  }

     
    /**
   * This method uses the Apache OpenNLP Sentence Detector and Tokenizer to produce
   * tokenized text by sentences.
   * 
   * For every line of text the method receives, it creates an array of
   * segmented sentences, and an array of Tokens. 
   * 
   * It fills the kaf object with the word forms element
   * <wf> corresponding to each of the tokens.
   * 
   * @param line of string
   * @param KAF bject. This object is used to take the output data and convert it
   *          to KAF, returning an XML document in a string.
   */

  int noSents = 0;
   
  public void annotateTokensToKAF(String line, KAF kaf) throws IOException {
	  
    String sentences[] = sentDetector.segmentSentence(line);
    
    // get linguistic annotations
     for (String sent : sentences) {
     
      String tokens[] = toker.toker(sent);
  
      // get sentence counter
      noSents = noSents + 1;
      String sid = Integer.toString(noSents);
      
      // Add tokens in the sentence to kaf object
      int numTokensInKaf = kaf.getNumWfs();
      int nextTokenInd = numTokensInKaf + 1;
      for (int i = 0; i < tokens.length; i++) {
        String id = "w" + Integer.toString(nextTokenInd++);
        String tokenStr = tokens[i];
        kaf.addWf(id, sid, tokenStr);
      }
   }
    
  }

  

}
