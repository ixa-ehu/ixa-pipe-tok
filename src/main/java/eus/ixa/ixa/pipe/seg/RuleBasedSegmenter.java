/*
 *Copyright 2015 Rodrigo Agerri

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

package eus.ixa.ixa.pipe.seg;


import java.io.InputStream;
import java.util.regex.Matcher;

import eus.ixa.ixa.pipe.tok.NonPrefixBreaker;
import static eus.ixa.ixa.pipe.tok.NonPrefixBreaker.END_INSIDE_QUOTES;
import static eus.ixa.ixa.pipe.tok.NonPrefixBreaker.END_PUNCT_LINK;
import static eus.ixa.ixa.pipe.tok.NonPrefixBreaker.MULTI_DOTS_STARTERS;
import static eus.ixa.ixa.pipe.tok.NonPrefixBreaker.MULTI_SPACE;
import static eus.ixa.ixa.pipe.tok.NonPrefixBreaker.NOPERIOD_END;
import static eus.ixa.ixa.pipe.tok.NonPrefixBreaker.PUNCT_UPPER;
import static eus.ixa.ixa.pipe.tok.NonPrefixBreaker.WRONG_PERIODS;

public class RuleBasedSegmenter implements SentenceSegmenter {
  public static final String LINE_BREAK="<JAR>";
  public static final String PARAGRAPH = "<P>";

  NonPrefixBreaker nonBreaker;

  public RuleBasedSegmenter(InputStream nonBreakingFile) {
    nonBreaker = new NonPrefixBreaker(nonBreakingFile);

  }

  public String[] segmentSentence(String line) {
    String[] sentences = sentenceSplitter(line);
    return sentences;
  }

  private String[] sentenceSplitter(String line) {
    // clean extra spaces
    String text = line.trim();
    Matcher m = MULTI_SPACE.matcher(text);
    text = m.replaceAll(" ");

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
    text = END_PUNCT_LINK.matcher(text).replaceAll("$1\n$2");

    // non prefix breaker detects exceptions to sentence breaks
    text = nonBreaker.SegmenterNonBreaker(text);
    String[] sentences = text.split("\n");
    return sentences;
  }

}
