package ixa.pipe.seg;

import ixa.pipe.resources.NonPrefixBreaker;

import java.io.InputStream;
import java.util.regex.Matcher;
import static ixa.pipe.resources.NonPrefixBreaker.MULTI_SPACE;
import static ixa.pipe.resources.NonPrefixBreaker.NOPERIOD_END;
import static ixa.pipe.resources.NonPrefixBreaker.MULTI_DOTS_STARTERS;
import static ixa.pipe.resources.NonPrefixBreaker.END_INSIDE_QUOTES;
import static ixa.pipe.resources.NonPrefixBreaker.PUNCT_UPPER;

/**
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
   * Rule-based sentence segmenter implements SentenceSegmenter method
   * and calls to the sentenceSplitter function to do the actual segmentation
   * Each line is a paragraph of the original input text
   * 
   * @param line
   * @return an array of segmented sentences, each element in the array corresponds
   * to a sentence
   */
  public String[] segmentSentence(String line) {
    String[] sentences = this.sentenceSplitter(line);
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
   * @return an array of segmented senteces, each element in the array corresponds
   * to a sentence
   */
  private String[] sentenceSplitter(String line) {  
    // clean extra spaces
    String text = line.trim();
    Matcher m = MULTI_SPACE.matcher(text);
    text = m.replaceAll(" ");
    //text = text.replaceAll("(\\.)\\s+([A-Z])","$1\n$2");

    // non-period end of sentence markers (?!) followed by sentence starters.
    text = NOPERIOD_END.matcher(text).replaceAll("$1\n$2");
    // multi-dots followed by sentence starters
    text = MULTI_DOTS_STARTERS.matcher(text).replaceAll("$1\n$2");
    // end of sentence inside quotes or brackets
    text = END_INSIDE_QUOTES.matcher(text).replaceAll("$1\n$2");
    // add breaks for sentences that end with some sort of punctuation are
    // followed by a sentence starter punctuation and upper case
    text = PUNCT_UPPER.matcher(text).replaceAll("$1\n$2");

    // //////////////////////////////////
    // // language dependent rules //////
    // //////////////////////////////////

    // non prefix breaker
    text = nonBreaker.SegmenterNonBreaker(text);
    //System.out.println(text);
    String[] sentences = text.split("\n");
    return sentences;
  }

}
