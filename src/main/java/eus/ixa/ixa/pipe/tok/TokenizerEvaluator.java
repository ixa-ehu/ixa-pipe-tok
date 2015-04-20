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

package eus.ixa.ixa.pipe.tok;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import com.google.common.base.Charsets;
import com.google.common.io.Files;

/**
 * The {@link TokenizerEvaluator} measures the performance of a tokenizer wrt to
 * some reference {@link Token}s.
 * 
 */
public class TokenizerEvaluator {

  private final FMeasure fmeasure = new FMeasure();
  public static final boolean DEBUG = true;

  private List<List<String>> referenceTokens(final List<Token> referenceList) {

    final List<List<String>> references = new ArrayList<List<String>>();
    for (int i = 0; i < referenceList.size(); i++) {
      final List<String> reference = Arrays.asList(new String[] {
          Integer.toString(i), referenceList.get(i).getTokenValue() });
      references.add(reference);

      if (DEBUG) {
        final StringBuilder sb = new StringBuilder();
        sb.append(referenceList.get(i).getTokenValue()).append(" ")
            .append(reference).append("\n");
        try {
          Files.append(sb.toString(), new File("reference-tokens.log"),
              Charsets.UTF_8);
        } catch (final IOException e) {
          // TODO Auto-generated catch block
          e.printStackTrace();
        }
      }
    }
    return references;
  }

  private List<List<String>> predictionTokens(final List<Token> predictionList) {

    final List<List<String>> predictions = new ArrayList<List<String>>();
    for (int j = 0; j < predictionList.size(); j++) {
      final List<String> prediction = Arrays.asList(new String[] {
          Integer.toString(j), predictionList.get(j).getTokenValue() });
      predictions.add(prediction);

      if (DEBUG) {
        final StringBuilder sb = new StringBuilder();
        sb.append(predictionList.get(j).getTokenValue()).append(" ")
            .append(prediction).append("\n");
        try {
          Files.append(sb.toString(), new File("prediction-tokens.log"),
              Charsets.UTF_8);
        } catch (final IOException e) {
          // TODO Auto-generated catch block
          e.printStackTrace();
        }
      }
    }
    return predictions;
  }

  /**
   * Evaluates the given reference Token list wrt to the predicted Token list.
   * 
   * The implementation has to update the score after every invocation.
   * 
   * @param referenceList
   *          the reference sample.
   * @param predictedList
   *          the predictedList
   */

  public void evaluate(final List<Token> referenceList,
      final List<Token> predictedList) {

    final List<List<String>> references = referenceTokens(referenceList);
    final List<List<String>> predictions = predictionTokens(predictedList);
    fmeasure.updateScores(references, predictions);

  }

  public FMeasure getFMeasure() {
    return fmeasure;
  }
}
