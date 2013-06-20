/*
 * Copyright 2013 Rodrigo Agerri

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


package ixa.pipe.tok;

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
public class TokenizerOpenNLP implements TokTokenizer {

  private TokenizerModel tokModel;
  private TokenizerME tokDetector;

  /**
   * This constructor loads a tokenization model, it initializes and creates a
   * tokDetector using such a model.
   */
  public TokenizerOpenNLP(InputStream trainedModel) {

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
  public String[] tokenize(String sentence) {
    String tokens[] = tokDetector.tokenize(sentence);
    return tokens;

  }
}
