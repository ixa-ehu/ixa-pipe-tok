package ixa.pipe.tok;

import ixa.pipe.resources.NonPrefixBreaker;

import java.io.InputStream;
import java.util.regex.Matcher;


public class TokenizerMoses implements TokTokenizer {
	
	NonPrefixBreaker nonBreaker;
	
	public TokenizerMoses(InputStream nonBreakingFile) { 
		nonBreaker = new NonPrefixBreaker(nonBreakingFile);
	}
	
	/*public String treatContractions(String line) {
		if (lang.equalsIgnoreCase("en")) { 
			line = NonPrefixBreaker.NOALPHA_APOS_NOALPHA.matcher(line).replaceAll("$1 ' $2");
			line = NonPrefixBreaker.NOALPHA_DIGIT_APOS_NOALPHA.matcher(line).replaceAll("$1 ' $2");
			line = NonPrefixBreaker.ALPHA_APOS_NOALPHA.matcher(line).replaceAll("$1 ' $2");
			line = NonPrefixBreaker.ALPHA_APOS_ALPHA.matcher(line).replaceAll("$1 '$2");
			line = NonPrefixBreaker.YEAR_APOS.matcher(line).replaceAll("$1 ' $2");
		}
		else { 
			return line;
		}
		return line;
	}*/
	
	


	public String [] tokenize(String line) {
		line = line.replace(line," " + line + " ");
		Matcher m = NonPrefixBreaker.MULTI_SPACE.matcher(line);
		line = m.replaceAll(" ");
		line = NonPrefixBreaker.ASCII_DECIMALS.matcher(line).replaceAll("");
		line = NonPrefixBreaker.SPECIALS.matcher(line).replaceAll(" $1 ");
		line = NonPrefixBreaker.MULTI_DOTS.matcher(line).replaceAll("DOTMULTI $1");
		Matcher multiMatcher = NonPrefixBreaker.DOT_MULTI.matcher(line);
		while (multiMatcher.find()) { 
			line = NonPrefixBreaker.MULTI_MULTI_DOTS.matcher(line).replaceAll("DOTDOTMULTI $1");
			line = NonPrefixBreaker.DOT_MULTI.matcher(line).replaceAll("DOTDOTMULTI");
		}
		line = NonPrefixBreaker.NODIGIT_COMMA_NODIGIT.matcher(line).replaceAll("$1 , $2");
		line = NonPrefixBreaker.DIGIT_COMMA_NODIGIT.matcher(line).replaceAll("$1 , $2");
		line = NonPrefixBreaker.NODIGIT_COMMA_DIGIT.matcher(line).replaceAll("$1 , $2");
		line = NonPrefixBreaker.DOUBLE_QUOTES.matcher(line).replaceAll("\"");

		////////////////////////////////////
		//// language dependent rules //////
		////////////////////////////////////

		// contractions it's, l'agila
		//line = this.treatContractions(line,lang);
		// non prefix breaker
		//line = nonBreaker.TokenizerNonBreaker(line);

		// clean up extraneous spaces
		line = line.replaceAll("\\s+", " ");
		line = line.trim();

		// restore multidots

		while (line.matches("DOTDOTMULTI"))
		{
			line = line.replaceAll("DOTDOTMULTI","DOTMULTI.");
		}
		line = line.replaceAll("DOTMULTI",".");

		// create final array of tokens
		//System.out.println(line);
		String [] tokens = line.split(" ");

		//ensure final line break
		/*if (!line.endsWith("\n")) { 
      line = line + "\n";
    }*/
		return tokens;
	}


}
