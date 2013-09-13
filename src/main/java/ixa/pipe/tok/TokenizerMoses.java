package ixa.pipe.tok;

import ixa.pipe.resources.NonPrefixBreaker;
import static ixa.pipe.resources.NonPrefixBreaker.MULTI_SPACE;
import static ixa.pipe.resources.NonPrefixBreaker.ASCII_HEX;
import static ixa.pipe.resources.NonPrefixBreaker.SPECIALS;
import static ixa.pipe.resources.NonPrefixBreaker.MULTI_DOTS;
import static ixa.pipe.resources.NonPrefixBreaker.DOTMULTI_DOT;
import static ixa.pipe.resources.NonPrefixBreaker.DOTMULTI_DOT_ANY;
import static ixa.pipe.resources.NonPrefixBreaker.NODIGIT_COMMA_NODIGIT;
import static ixa.pipe.resources.NonPrefixBreaker.DIGIT_COMMA_NODIGIT;
import static ixa.pipe.resources.NonPrefixBreaker.NODIGIT_COMMA_DIGIT;
import static ixa.pipe.resources.NonPrefixBreaker.NOALPHA_APOS_NOALPHA;
import static ixa.pipe.resources.NonPrefixBreaker.NOALPHA_DIGIT_APOS_NOALPHA;
import static ixa.pipe.resources.NonPrefixBreaker.ALPHA_APOS_NOALPHA;
import static ixa.pipe.resources.NonPrefixBreaker.ALPHA_APOS_ALPHA;
import static ixa.pipe.resources.NonPrefixBreaker.YEAR_APOS;


import java.io.InputStream;
import java.util.regex.Matcher;

public class TokenizerMoses implements TokTokenizer {

  NonPrefixBreaker nonBreaker;

  public TokenizerMoses(InputStream nonBreakingFile, String lang) {
    nonBreaker = new NonPrefixBreaker(nonBreakingFile);
  }

  public String[] tokenize(String line, String lang) { 
    String[] tokens = this.tokDetector(line, lang);
    return tokens;
  }
  
  private String[] tokDetector(String line, String lang) {
    
    //line = line.replace(line, " " + line + " ");
    // remove extra spaces and ASCII stuff
   
    Matcher multiSpace = MULTI_SPACE.matcher(line);
    Matcher asciiHex = ASCII_HEX.matcher(line);
    Matcher specials = SPECIALS.matcher(line);
    
    line = multiSpace.replaceAll(" ");
    line = asciiHex.replaceAll("");
    // separate out other special characters [^\p{Alnum}s.'`,-]
    line = specials.replaceAll(" $1 ");
    // do not separate multidots
    line = this.generateMultidots(line);
    
    // separate "," except if within numbers (5,300)
    line = NODIGIT_COMMA_NODIGIT.matcher(line).replaceAll("$1 , $2");
    // separate pre and post digit
    line = DIGIT_COMMA_NODIGIT.matcher(line).replaceAll("$1 , $2");
    line = NODIGIT_COMMA_DIGIT.matcher(line).replaceAll("$1 , $2");

    // //////////////////////////////////
    // // language dependent rules //////
    // //////////////////////////////////

    // contractions it's, l'agila
    line = this.treatContractions(line, lang);
    // non prefix breaker
    //line = nonBreaker.TokenizerNonBreaker(line);

    // clean up extraneous spaces
    line = line.replaceAll("\\s+", " ");
    line = line.trim();

    // restore multidots
    line = this.restoreMultidots(line);

    // create final array of tokens
    //System.out.println(line);
    String[] tokens = line.split(" ");

    // ensure final line break
    //if (!line.endsWith("\n")) { line = line + "\n"; }
    return tokens;
  }
  
  private String generateMultidots(String line) {
    
    line = MULTI_DOTS.matcher(line).replaceAll(" DOTMULTI$1");
    Matcher dotMultiDot = DOTMULTI_DOT.matcher(line);
    
    while (dotMultiDot.find()) {
     line = DOTMULTI_DOT_ANY.matcher(line).replaceAll(
        "DOTDOTMULTI $1");
     line = dotMultiDot.replaceAll("DOTDOTMULTI");
     // reset the matcher otherwise the while will stop after one run
     dotMultiDot.reset(line);
    }
    return line;
  }
 
  private String restoreMultidots(String line) {
    
    while (line.contains("DOTDOTMULTI")) {
      line = line.replaceAll("DOTDOTMULTI", "DOTMULTI.");
    }
    line = line.replaceAll("DOTMULTI", ".");
    return line;
  }

  private String treatContractions(String line, String lang) {
    
    if (lang.equalsIgnoreCase("en")) {
      line = NOALPHA_APOS_NOALPHA.matcher(line).replaceAll(
          "$1 ' $2");
      line = NOALPHA_DIGIT_APOS_NOALPHA.matcher(line)
          .replaceAll("$1 ' $2");
      line = ALPHA_APOS_NOALPHA.matcher(line).replaceAll(
          "$1 ' $2");
      line = ALPHA_APOS_ALPHA.matcher(line).replaceAll(
          "$1 '$2");
      line = YEAR_APOS.matcher(line).replaceAll("$1 ' $2");
    } else {
      return line;
    }
    return line;
  }

}
