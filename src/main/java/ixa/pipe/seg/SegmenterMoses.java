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

import ixa.pipe.resources.NonPrefixBreaker;

import java.io.InputStream;
import java.util.regex.Matcher;
import static ixa.pipe.resources.NonPrefixBreaker.MULTI_SPACE;
import static ixa.pipe.resources.NonPrefixBreaker.NOPERIOD_END;
import static ixa.pipe.resources.NonPrefixBreaker.MULTI_DOTS_STARTERS;
import static ixa.pipe.resources.NonPrefixBreaker.WRONG_PERIODS;
import static ixa.pipe.resources.NonPrefixBreaker.END_INSIDE_QUOTES;
import static ixa.pipe.resources.NonPrefixBreaker.PUNCT_UPPER;

/**
 * 
 * Sentence segmenter loosely inspired by the moses decoder sentence segmenter
 * https://github.com/moses-smt/mosesdecoder
 * 
 * @author ragerri
 * 
 */
public class SegmenterMoses implements SentenceSegmenter {

  NonPrefixBreaker nonBreaker;

  public SegmenterMoses(InputStream nonBreakingFile) {
    nonBreaker = new NonPrefixBreaker(nonBreakingFile);

  }

  /**
   * 
   * Rule-based sentence segmenter implements SentenceSegmenter method and calls
   * to the sentenceSplitter function to do the actual segmentation Each line is
   * a paragraph of the original input text
   * 
   * @param line
   * @return an array of segmented sentences, each element in the array
   *         corresponds to a sentence
   */
  public String[] segmentSentence(String line) {
    String[] sentences = sentenceSplitter(line);
    return sentences;
  }

  /**
   * 
   * Rule-based sentence segmenter loosely inspired by moses segmenter
   * https://github.com/moses-smt/mosesdecoder
   * 
   * Each line is a paragraph of the original input text
   * 
   * @param line
   * @return an array of segmented sentences, each element in the array
   *         corresponds to a sentence
   */
  private String[] sentenceSplitter(String line) {
    // clean extra spaces
    String text = line.trim();
    Matcher m = MULTI_SPACE.matcher(text);
    text = m.replaceAll(" ");
    // replace '' for "
    text = text.replaceAll("''","\"");
    text = text.replaceAll("``","\"");

    // non-period end of sentence markers (?!) followed by sentence starters.
    text = NOPERIOD_END.matcher(text).replaceAll("$1\n$2");
    // multi-dots followed by sentence starters
    text = MULTI_DOTS_STARTERS.matcher(text).replaceAll("$1\n$2");
    text = WRONG_PERIODS.matcher(text).replaceAll("$1\n$2");
    // end of sentence inside quotes or brackets
    text = END_INSIDE_QUOTES.matcher(text).replaceAll("$1\n$2");
    // add breaks for sentences that end with some sort of punctuation are
    // followed by a sentence starter punctuation and upper case
    text = PUNCT_UPPER.matcher(text).replaceAll("$1\n$2");

    // //////////////////////////////////
    // // language dependent rules //////
    // //////////////////////////////////

    // non prefix breaker detects exceptions to sentence breaks
    text = nonBreaker.SegmenterNonBreaker(text);
    String[] sentences = text.split("\n");
    return sentences;
  }

}
