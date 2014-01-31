/* 
 *Copyright 2014 Rodrigo Agerri

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

package ixa.pipe.tok.eval;

import ixa.pipe.tok.Token;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import org.apache.commons.io.FileUtils;

/**
 * The {@link TokenizerEvaluator} measures the performance of a 
 * tokenizer wrt to some reference {@link Token}s.
 * 
 */
public class TokenizerEvaluator {

  private FMeasure fmeasure = new FMeasure();
  public static final boolean DEBUG = false;

  private List<List<String>> referenceTokens(List<Token> referenceList) {

    List<List<String>> references = new ArrayList<List<String>>();
    for (int i=0; i < referenceList.size(); i++) {
      List<String> reference = Arrays.asList(new String[]{Integer.toString(i), referenceList.get(i).value()});
      references.add(reference);
      
      if (DEBUG) {
        StringBuilder sb = new StringBuilder();
        sb.append(referenceList.get(i).value()).append(" ").append(reference).append("\n");
        try {
          FileUtils.writeStringToFile(new File("reference-tokens.log"), sb.toString(), true);
        } catch (IOException e) {
          // TODO Auto-generated catch block
          e.printStackTrace();
        }
      }
    }
    return references;
  }

  private List<List<String>> predictionTokens(List<Token> predictionList) {

    List<List<String>> predictions= new ArrayList<List<String>>();
    for (int j=0; j < predictionList.size(); j++) { 
      List<String> prediction = Arrays.asList(new String[]{Integer.toString(j), predictionList.get(j).value()});
      predictions.add(prediction);
      
      if (DEBUG) {
        StringBuilder sb = new StringBuilder();
        sb.append(predictionList.get(j).value()).append(" ").append(prediction).append("\n");
        try {
          FileUtils.writeStringToFile(new File("prediction-tokens.log"), sb.toString(), true);
        } catch (IOException e) {
          // TODO Auto-generated catch block
          e.printStackTrace();
        }
      }
    }
    return predictions;
  }

  /**
   * Evaluates the given reference Token list wrt to the predicted
   * Token list.
   * 
   * The implementation has to update the score after every invocation.
   * 
   * @param reference
   *          the reference sample.
   * 
   * @return the predicted sample
   */

  public void evaluate(List<Token> referenceList,
      List<Token> predictedList) {
    
    List<List<String>> references = referenceTokens(referenceList);
    List<List<String>> predictions = predictionTokens(predictedList);
    fmeasure.updateScores(references, predictions);

  }

  public FMeasure getFMeasure() {
    return fmeasure;
  }
}
