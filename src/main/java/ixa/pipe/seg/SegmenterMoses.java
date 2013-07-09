package ixa.pipe.seg;

import ixa.pipe.resources.NonPrefixBreaker;

import java.io.InputStream;
import java.util.regex.Matcher;
import static ixa.pipe.resources.NonPrefixBreaker.MULTI_SPACE;
import static ixa.pipe.resources.NonPrefixBreaker.NOPERIOD_END;
import static ixa.pipe.resources.NonPrefixBreaker.MULTI_DOTS_STARTERS;
import static ixa.pipe.resources.NonPrefixBreaker.END_INSIDE_QUOTES;
import static ixa.pipe.resources.NonPrefixBreaker.NOPERIOD_END_SPACE;
import static ixa.pipe.resources.NonPrefixBreaker.STRING_ALPHANUM_PUNCT;
import static ixa.pipe.resources.NonPrefixBreaker.ALPHANUM_PUNCT;

 
public class SegmenterMoses implements SentenceSegmenter {

	NonPrefixBreaker nonBreaker;

	public SegmenterMoses(InputStream nonBreakingFile) {
		nonBreaker = new NonPrefixBreaker(nonBreakingFile);
		
	}
	

	public String [] segmentSentence(String text) {
		Matcher m = MULTI_SPACE.matcher(text);
		text = m.replaceAll(" ");
		// clean up extraneous spaces
		text = text.trim();
		text = text.replaceAll("\\s+", " ");
		//text = text.replaceAll("(\\.)\\s+([A-Z])","$1\n$2");
		
		// this is one paragraph 
		
		
		// non-period end of sentence markers (?!) followed by sentence starters.
		text = NOPERIOD_END.matcher(text).replaceAll("$1\n$2");
		// multi-dots followed by sentence starters
		text = MULTI_DOTS_STARTERS.matcher(text).replaceAll("$1\n$2");
		// end of sentence inside quotes or brackets
		text = END_INSIDE_QUOTES.matcher(text).replaceAll("$1\n$2");
		// add breaks for sentences that end with some sort of punctuation are followed 
		// by a sentence starter punctuation and upper case
		text = NOPERIOD_END_SPACE.matcher(text).replaceAll("$1\n$2");
		
		////////////////////////////////////
		//// language dependent rules //////
		////////////////////////////////////

		// non prefix breaker
		text = nonBreaker.SegmenterNonBreaker(text);

		// create final array of tokens
		System.out.println(text);
		String [] tokens = text.split(" ");
		
		return tokens;
	}
	
}
