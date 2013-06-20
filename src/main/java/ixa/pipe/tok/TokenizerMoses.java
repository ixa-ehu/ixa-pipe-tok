package ixa.pipe.tok;

import ixa.pipe.resources.NonBreakingPrefixes;

import java.util.regex.Matcher;
import java.util.regex.Pattern;


public class TokenizerMoses implements TokTokenizer {

  private static Pattern multiSpace = Pattern.compile("\\s+");
  private static Pattern asciiDecimals = Pattern.compile("[000-037]");
  private static Pattern specials = Pattern.compile("([^\\p{Alnum}\\s\\.\'\\`\\,\\-])");
  private static Pattern multiDots = Pattern.compile("\\.([\\.]+)");
  private static Pattern dotMulti = Pattern.compile("DOTMULTI\\.");
  private static Pattern multimultiDots = Pattern.compile("DOTMULTI\\.([^\\.])");
  private static Pattern noNumberCommaNoNumber = Pattern.compile("([^\\d])[,]([^\\d])");
  private static Pattern numberCommaNoNumber = Pattern.compile("([\\d])[,]([^\\d])");
  private static Pattern noNumberCommaNumber = Pattern.compile("([^\\d])[,](\\d)");
  private static Pattern doubleQuotes = Pattern.compile("\'\'");
  private static Pattern wordDot = Pattern.compile("^(\\S+)\\.$");
  
  // english contractions patterns 
  private static Pattern noAlphaAposNoAlpha = Pattern.compile("([^a-zA-Z])[']([^a-zA-Z])");
  private static Pattern noAlphaNoNumber= Pattern.compile("([^A-Za-z]\\d])[\']([a-zA-Z])");
  private static Pattern alphaAposNoAlpha = Pattern.compile("([a-zA-Z])[']([^A-Za-z])");
  private static Pattern alphaAposAlpha = Pattern.compile("([a-zA-Z])[']([a-zA-Z])");
  // special case for "1990's"
  private static Pattern yearApos = Pattern.compile("([\\d])[']([s])");
  
  NonBreakingPrefixes dict = new NonBreakingPrefixes();
  
  

  public String englishContractions(String line) {
	line = noAlphaAposNoAlpha.matcher(line).replaceAll("$1 ' $2");
	line = noAlphaNoNumber.matcher(line).replaceAll("$1 ' $2");
	line = alphaAposNoAlpha.matcher(line).replaceAll("$1 ' $2");
	line = alphaAposAlpha.matcher(line).replaceAll("$1 '$2");
	line = yearApos.matcher(line).replaceAll("$1 ' $2");
	return line;
  }

  public String nonPrefixBreaker(String line) {
		String[] words = line.split(" ");
	    StringBuilder sb = new StringBuilder();
		for (int i=0; i < words.length; i++) { 
			String word = words[i];
		
			if (word.matches("^(\\S+)\\.$")) {
				String pre = wordDot.matcher(word).replaceAll("$1");
			
				if ((!pre.equalsIgnoreCase(".") && pre.matches("[A-Za-z]")) || 
						dict.titles.contains(pre) || (i < (words.length)-1 && 
						words[i+1].matches("[^a-z]")))
				{
					return word;
				}
				
				else if (dict.numericOnly.contains(pre) && (i < (words.length)-1) && 
						words[i+1].matches("^[0-9]+")) { 
				  return word;
				}
				else { 
					word = pre + " .";
				}
			}
			sb.append(word).append(" ");
			line = sb.toString();
		}
		return line;
	}
  
  
  public String [] tokenize(String line) {
    line = line.replace(line," " + line + " ");
    Matcher m = multiSpace.matcher(line);
    line = m.replaceAll(" ");
    line = asciiDecimals.matcher(line).replaceAll("");
    line = specials.matcher(line).replaceAll(" $1 ");
    line = multiDots.matcher(line).replaceAll("DOTMULTI $1");
    Matcher multiMatcher = dotMulti.matcher(line);
    while (multiMatcher.find()) { 
    	line = multimultiDots.matcher(line).replaceAll("DOTDOTMULTI $1");
    	line = dotMulti.matcher(line).replaceAll("DOTDOTMULTI");
    }
    line = noNumberCommaNoNumber.matcher(line).replaceAll("$1 , $2");
    line = numberCommaNoNumber.matcher(line).replaceAll("$1 , $2");
    line = noNumberCommaNumber.matcher(line).replaceAll("$1 , $2");
    line = doubleQuotes.matcher(line).replaceAll("\"");
    
    // english contractions
    line = this.englishContractions(line);
    // non prefix breaker
    line = this.nonPrefixBreaker(line);
    
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
    System.out.println(line);
    String [] tokens = line.split(" ");
    
    //ensure final line break
    /*if (!line.endsWith("\n")) { 
      line = line + "\n";
    }*/
    return tokens;
  }
  
  
}
