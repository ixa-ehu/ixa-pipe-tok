/*
 *Copyright 2013 Rodrigo Agerri

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

package ixa.pipe.seg;

import java.io.IOException;
import java.io.InputStream;

import opennlp.tools.sentdetect.SentenceDetector;
import opennlp.tools.sentdetect.SentenceDetectorME;
import opennlp.tools.sentdetect.SentenceModel;
import opennlp.tools.util.Span;

/**
 * Sentence Segmentation using Apache OpenNLP Model trained by IXA NLP Group
 * 
 * @author ragerri
 * 
 */
public class Segmenter {

  private SentenceModel segModel;
  private SentenceDetector sentDetector;

  /**
   * The Segmenter constructor loads an Apache OpenNLP sentence segmentation
   * model, it initializes and sentenceDetector using such a model.
   * 
   * @param cmd
   */

  public Segmenter(InputStream trainedModel) {

    // InputStream trainedModel =
    // getClass().getResourceAsStream("/en-sent.bin");
    try {
      segModel = new SentenceModel(trainedModel);

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

    sentDetector = new SentenceDetectorME(segModel);

  }

  /**
   * @param line
   *          a string
   * @return an array of segmented sentences
   */
  public String[] segmentSentence(String line) {
    String sentences[] = sentDetector.sentDetect(line);
    return sentences;

  }
  
  public Span[] segmentPosSentence(String line) { 
    Span sentences[] = sentDetector.sentPosDetect(line);
    return sentences;
  }
 
}
