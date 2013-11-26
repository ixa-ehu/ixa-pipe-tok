package ixa.pipe.tok;

/* --------------------------Usercode Section------------------------ */

import ixa.pipe.resources.NonPrefixBreaker;
import java.io.Reader;
import java.util.logging.Logger;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Properties;
import java.util.Set;
import java.util.regex.Pattern;

	
/* -----------------Options and Declarations Section----------------- */

%%

%class JFlexTokenizer
%unicode
%type Token
%caseless
%char
%state strictTB3 TB3 EN ANCORA


/* 
 * Member variables and functions
 */

%{

  private TokenFactory tokenFactory;
  
   /* Flags begin with historical ptb3Escaping behavior */
  
  private boolean tokenizeNLs;
  private boolean americanize = true;
  private boolean ptb3Dashes = true;
  private boolean normalizeAmpersand = true;
  private boolean escapeForwardSlash = true;
  private boolean normalizeSpace = true;
  private boolean normalizeFractions = true;
   
  private boolean normalizeCurrency = true;
 
  private boolean normalizeParentheses = true;
  private boolean normalizeOtherBrackets = true;
 
  private boolean latexQuotes = true;
  private boolean unicodeQuotes;
  private boolean asciiQuotes;
  private boolean ptb3Ellipsis = true;
  private boolean unicodeEllipsis;
  
  
  private boolean strictTreebank3 = false;
  
  
  public JFlexTokenizer(Reader breader, TokenFactory tokenFactory, String options) {
    this(breader);
    this.tokenFactory = tokenFactory;
    if (options == null) {
      options = "";
    }
  }
  
  ////////////////////
  //// AMPERSANDS ////
  ////////////////////
  
  private static final Pattern AMP_PATTERN = Pattern.compile("(?i:&amp;)");
   
  private Token normalizeAmpNext() {
    final String txt = yytext();
    if (normalizeAmpersand) {
      return makeToken(normalizeAmp(txt));
    } else {
      return makeToken();
    }
  }
  
  private static String normalizeAmp(final String in) {
    return AMP_PATTERN.matcher(in).replaceAll("&");
  }
  
  private static String removeSoftHyphen(String in) {
    // \u00AD remove the soft hyphen character, except those used for line-breaking
    if (in.indexOf('\u00AD') < 0) {
      return in;
    }
    int length = in.length();
    StringBuilder out = new StringBuilder(length - 1);
    for (int i = 0; i < length; i++) {
      char ch = in.charAt(i);
      if (ch != '\u00AD') {
        out.append(ch);
      }
    }
    if (out.length() == 0) {
      out.append('-'); // don't create an empty token
    }
    return out.toString();
  }
  
  /** This escapes a character with a backslash, but doesn't do it
   *  if the character is already preceded by a backslash.
   */
  private static String escape(String s, char c) {
    int i = s.indexOf(c);
    while (i != -1) {
      if (i == 0 || s.charAt(i - 1) != '\\') {
        s = s.substring(0, i) + '\\' + s.substring(i);
        i = s.indexOf(c, i + 2);
      } else {
        i = s.indexOf(c, i + 1);
      }
    }
    return s;
  }
  
  
  ////////////////
  //// QUOTES ////
  ////////////////
  
  private static final Pattern singleQuote = Pattern.compile("&apos;|'");
  private static final Pattern doubleQuote = Pattern.compile("\"|&quot;");

  // 91,92,93,94 aren't valid unicode points, but sometimes they show
  // up from cp1252 and need to be translated
  private static final Pattern leftSingleQuote = Pattern.compile("[\u0091\u2018\u201B\u2039]");
  private static final Pattern rightSingleQuote = Pattern.compile("[\u0092\u2019\u203A]");
  private static final Pattern leftDoubleQuote = Pattern.compile("[\u0093\u201C\u00AB]");
  private static final Pattern rightDoubleQuote = Pattern.compile("[\u0094\u201D\u00BB]");

  private static String latexQuotes(String in, boolean probablyLeft) {
    String s1 = in;
    if (probablyLeft) {
      s1 = singleQuote.matcher(s1).replaceAll("`");
      s1 = doubleQuote.matcher(s1).replaceAll("``");
    } else {
      s1 = singleQuote.matcher(s1).replaceAll("'");
      s1 = doubleQuote.matcher(s1).replaceAll("''");
    }
    s1 = leftSingleQuote.matcher(s1).replaceAll("`");
    s1 = rightSingleQuote.matcher(s1).replaceAll("'");
    s1 = leftDoubleQuote.matcher(s1).replaceAll("``");
    s1 = rightDoubleQuote.matcher(s1).replaceAll("''");
    return s1;
  }

  private static final Pattern asciiSingleQuote = Pattern.compile("&apos;|[\u0091\u2018\u0092\u2019\u201A\u201B\u2039\u203A']");
  private static final Pattern asciiDoubleQuote = Pattern.compile("&quot;|[\u0093\u201C\u0094\u201D\u201E\u00AB\u00BB\"]");

  private static String asciiQuotes(String in) {
    String s1 = in;
    s1 = asciiSingleQuote.matcher(s1).replaceAll("'");
    s1 = asciiDoubleQuote.matcher(s1).replaceAll("\"");
    return s1;
  }

  private static final Pattern unicodeLeftSingleQuote = Pattern.compile("\u0091");
  private static final Pattern unicodeRightSingleQuote = Pattern.compile("\u0092");
  private static final Pattern unicodeLeftDoubleQuote = Pattern.compile("\u0093");
  private static final Pattern unicodeRightDoubleQuote = Pattern.compile("\u0094");

  private static String unicodeQuotes(String in, boolean probablyLeft) {
    String s1 = in;
    if (probablyLeft) {
      s1 = singleQuote.matcher(s1).replaceAll("\u2018");
      s1 = doubleQuote.matcher(s1).replaceAll("\u201c");
    } else {
      s1 = singleQuote.matcher(s1).replaceAll("\u2019");
      s1 = doubleQuote.matcher(s1).replaceAll("\u201d");
    }
    s1 = unicodeLeftSingleQuote.matcher(s1).replaceAll("\u2018");
    s1 = unicodeRightSingleQuote.matcher(s1).replaceAll("\u2019");
    s1 = unicodeLeftDoubleQuote.matcher(s1).replaceAll("\u201c");
    s1 = unicodeRightDoubleQuote.matcher(s1).replaceAll("\u201d");
    return s1;
  }

  private Token handleQuotes(String token, boolean probablyLeft) {
    String normToken;
    if (latexQuotes) {
      normToken = latexQuotes(token, probablyLeft);
    } else if (unicodeQuotes) {
      normToken = unicodeQuotes(token, probablyLeft);
    } else if (asciiQuotes) {
      normToken = asciiQuotes(token);
    } else {
      normToken = token;
    }
    return makeToken(normToken);
  }
  
  ///////////////////
  //// FRACTIONS ////
  ///////////////////
  
  private static final Pattern LEFT_PAREN_PATTERN = Pattern.compile("\\(");
  private static final Pattern RIGHT_PAREN_PATTERN = Pattern.compile("\\)");

  private static final Pattern ONE_FOURTH_PATTERN = Pattern.compile("\u00BC");
  private static final Pattern ONE_HALF_PATTERN = Pattern.compile("\u00BD");
  private static final Pattern THREE_FOURTHS_PATTERN = Pattern.compile("\u00BE");
  private static final Pattern ONE_THIRD_PATTERN = Pattern.compile("\u2153");
  private static final Pattern TWO_THIRDS_PATTERN = Pattern.compile("\u2154");

  private Token normalizeFractions(final String in) {
    String out = in;
    if (normalizeFractions) {
      if (escapeForwardSlash) {
        out = ONE_FOURTH_PATTERN.matcher(out).replaceAll("1\\\\/4");
        out = ONE_HALF_PATTERN.matcher(out).replaceAll("1\\\\/2");
        out = THREE_FOURTHS_PATTERN.matcher(out).replaceAll("3\\\\/4");
        out = ONE_THIRD_PATTERN.matcher(out).replaceAll("1\\\\/3");
        out = TWO_THIRDS_PATTERN.matcher(out).replaceAll("2\\\\/3");
     } else {
        out = ONE_FOURTH_PATTERN.matcher(out).replaceAll("1/4");
        out = ONE_HALF_PATTERN.matcher(out).replaceAll("1/2");
        out = THREE_FOURTHS_PATTERN.matcher(out).replaceAll("3/4");
        out = ONE_THIRD_PATTERN.matcher(out).replaceAll("1/3");
        out = TWO_THIRDS_PATTERN.matcher(out).replaceAll("2/3");
      }
    }
    return makeToken(out);
  }
  
  //////////////////
  //// CURRENCY ////
  //////////////////
  
  private static final Pattern CENTS_PATTERN = Pattern.compile("\u00A2");
  private static final Pattern POUND_PATTERN = Pattern.compile("\u00A3");
  private static final Pattern GENERIC_CURRENCY_PATTERN = Pattern.compile("[\u0080\u00A4\u20A0\u20AC]");

  private static String normalizeCurrency(String in) {
    String s1 = in;
    s1 = CENTS_PATTERN.matcher(s1).replaceAll("cents");
    // historically used for pound in PTB3
    s1 = POUND_PATTERN.matcher(s1).replaceAll("#");  
    // not good translation for Euro €
    s1 = GENERIC_CURRENCY_PATTERN.matcher(s1).replaceAll("\\$");  
    return s1;
  }
  
   //////////////////
  //// BRACKETS ////
  //////////////////
  
  public static final String openRB = "-LRB-";
  public static final String closeRB = "-RRB-";
  public static final String openCB = "-LCB-";
  public static final String closeCB = "-RCB-";
  public static final String openSB = "-LSB-";
  public static final String closeSB = "-RSB-";
  public static final String ptbDash = "--";
  
  ////////////////////////
  //// MAIN FUNCTIONS ////
  ////////////////////////
  
  
  private Token makeToken() { 
    String tokenString = yytext();
    return makeToken(tokenString);
  }

  private Token makeToken(String tokenString) { 
    return tokenFactory.createToken(tokenString, yychar, yylength());
  }
  
 

%}


  ////////////////
  //// MACROS ////
  ///////////////

AMP = &amp;  
DASHES = &(MD|mdash|ndash);|[\u0096\u0097\u2013\u2014\u2015]
SPECIAL_PUNCT = &(HT|TL|UR|LR|QC|QL|QR|odq|cdq|#[0-9]+);

/* APOSTROPHES */

APOS = ['\u0092\u2019]|&apos;
/* Includes extra ones that may appear inside a word, rightly or wrongly */
ALL_APOS = {APOS}|[\u0091\u2018\u201B]
SPECIAL_APOS_AUX = n{ALL_APOS}t
SPECIAL_WEBS = &[aeiouAEIOU](acute|grave|uml);

/* WORDS; note that U+0237-U+024F (dotless j) isn't in [:letter:] */

LETTER = ([:letter:]|{SPECIAL_WEBS}|[\u00AD\u0237-\u024F\u02C2-\u02C5\u02D2-\u02DF\u02E5-\u02FF\u0300-\u036F\u0370-\u037D\u0384\u0385\u03CF\u03F6\u03FC-\u03FF\u0483-\u0487\u04CF\u04F6-\u04FF\u0510-\u0525\u055A-\u055F\u0591-\u05BD\u05BF\u05C1\u05C2\u05C4\u05C5\u05C7\u0615-\u061A\u063B-\u063F\u064B-\u065E\u0670\u06D6-\u06EF\u06FA-\u06FF\u070F\u0711\u0730-\u074F\u0750-\u077F\u07A6-\u07B1\u07CA-\u07F5\u07FA\u0900-\u0903\u093C\u093E-\u094E\u0951-\u0955\u0962-\u0963\u0981-\u0983\u09BC-\u09C4\u09C7\u09C8\u09CB-\u09CD\u09D7\u09E2\u09E3\u0A01-\u0A03\u0A3C\u0A3E-\u0A4F\u0A81-\u0A83\u0ABC-\u0ACF\u0B82\u0BBE-\u0BC2\u0BC6-\u0BC8\u0BCA-\u0BCD\u0C01-\u0C03\u0C3E-\u0C56\u0D3E-\u0D44\u0D46-\u0D48\u0E30-\u0E3A\u0E47-\u0E4E\u0EB1-\u0EBC\u0EC8-\u0ECD])
WORD = {LETTER}+([.!?]{LETTER}+)*
APOS_AUX = {APOS}([msdMSD]|re|ve|ll)
/* \u00AD is soft hyphen */
SPECIAL_WORD = [A-Za-z\u00AD]*[A-MO-Za-mo-z](\u00AD)*
WORD_APOS = {APOS}n{APOS}?|[lLdDjJ]{APOS}|Dunkin{APOS}|somethin{APOS}|ol{APOS}|{APOS}em|[A-HJ-XZn]{ALL_APOS}[:letter:]{2}[:letter:]*|{APOS}[2-9]0s|{APOS}till?|[:letter:][:letter:]*[aeiouyAEIOUY]{ALL_APOS}[aeiouA-Z][:letter:]*|{APOS}cause|cont'd\.?|'twas|nor'easter|c'mon|e'er|s'mores|ev'ry|li'l|nat'l
Y_APOS_WORD = y{APOS}
/* Cap'n for captain, c'est for french */
NORMALIZED_PREFIXES = -(RRB|LRB|RCB|LCB|RSB|LSB)-|C\.D\.s|pro-|anti-|S(&|&amp;)P-500|S(&|&amp;)Ls|Cap{APOS}n|c{APOS}est
APOS_DIGIT_DIGIT = {APOS}[0-9][0-9]
HYPHEN_WORDS = [A-Za-z0-9]+(-[A-Za-z]+){0,2}(\\?\/[A-Za-z0-9]+(-[A-Za-z]+){0,2}){1,2}


/* URLS */
STRICT_URL = https?:\/\/[^ \t\n\f\r\"<>|()]+[^ \t\n\f\r\"<>|.!?(){},-]
APPROX_URL = ((www\.([^ \t\n\f\r\"<>|.!?(){},]+\.)+[a-zA-Z]{2,4})|(([^ \t\n\f\r\"`'<>|.!?(){},-_$]+\.)+(com|net|org|edu)))(\/[^ \t\n\f\r\"<>|()]+[^ \t\n\f\r\"<>|.!?(){},-])?
EMAIL = [a-zA-Z0-9][^ \t\n\f\r\"<>|()\u00A0]*@([^ \t\n\f\r\"<>|().\u00A0]+\.)*([^ \t\n\f\r\"<>|().\u00A0]+)
/* Technically, names should be capped at 15 characters.  However, then
   you get into weirdness with what happens to the rest of the characters. */
TWITTER_NAME = @[a-zA-Z_][a-zA-Z_0-9]*
TWITTER_CATEGORY = #{WORD}
TWITTER = {TWITTER_NAME}|{TWITTER_CATEGORY}

/* NUMBERS and DATES */
DIGIT = [:digit:]|[\u07C0-\u07C9]
DATE = {DIGIT}{1,2}[\-\/]{DIGIT}{1,2}[\-\/]{DIGIT}{2,4}
NUM = {DIGIT}+|{DIGIT}*([.:,\u00AD\u066B\u066C]{DIGIT}+)+
NUMBER = [\-+]?{NUM}
SUBSUPNUM = [\u207A\u207B\u208A\u208B]?([\u2070\u00B9\u00B2\u00B3\u2074-\u2079]+|[\u2080-\u2089]+)
FRACTION = ({DIGIT}{1,4}[- \u00A0])?{DIGIT}{1,4}(\\?\/|\u2044){DIGIT}{1,4}
FRACTION_TB3 = ({DIGIT}{1,4}-)?{DIGIT}{1,4}(\\?\/|\u2044){DIGIT}{1,4}
OTHER_FRACTION = [\u00BC\u00BD\u00BE\u2153-\u215E]
DOLLAR = ([A-Z]*\$|#)
/* These are cent and pound sign, euro and euro, and Yen, Lira */
OTHER_CURRENCIES= [\u00A2\u00A3\u00A4\u00A5\u0080\u20A0\u20AC\u060B\u0E3F\u20A4\uFFE0\uFFE1\uFFE5\uFFE6]

/* ------------------------Lexical Rules Section---------------------- */

%%

/* ptb3 normalized ampersand */
{AMP}                 	{ return normalizeAmpNext(); }

/* ptb3 normalized dashes */
{DASHES}				{ if (ptb3Dashes) {
                    		return makeToken(ptbDash); }
                  		else {
                    		return makeToken();
                   		}
                		}
                
/* special punctuation */
{SPECIAL_PUNCT}       		{ return makeToken(); }

/* special words slang */

cannot          		{ yypushback(3) ; return makeToken(); }
gonna|gotta|lemme|gimme|wanna 	{ yypushback(2) ; return makeToken(); }

/* WORDS  including they're and n't and so on */
{WORD}/{APOS_AUX}         	{String normString = removeSoftHyphen(yytext());
                          	 return makeToken(normString);
                        	}
                        	
{SPECIAL_WORD}/{SPECIAL_APOS_AUX}       {  String normString = removeSoftHyphen(yytext());
                          				   return makeToken(normString); 
                          				}

{WORD}                  { String normString = removeSoftHyphen(yytext());
                          return makeToken(normString);
                        }

{WORD_APOS}               { return makeToken(); }

{Y_APOS_WORD}/[:letter:]   { return makeToken(); }

/* URLs */
{STRICT_URL}            { String txt = yytext();
                          if (escapeForwardSlash) {
                            txt = escape(txt, '/');
                            txt = escape(txt, '*');
                          }
                          return makeToken(txt); 
                        }

{APPROX_URL}            { String txt = yytext();
                          if (escapeForwardSlash) {
                            txt = escape(txt, '/');
                            txt = escape(txt, '*');
                          }
                          return makeToken(txt); 
                        }

{EMAIL}                 { return makeToken(); }
{TWITTER}               { return makeToken(); }

/* QUOTES */

{APOS_AUX}/[^A-Za-z]    { return handleQuotes(yytext(), false);
                        }
{SPECIAL_APOS_AUX}      { return handleQuotes(yytext(), false);
                        }

/* DATES and NUMERS */

{DATE}                  { String txt = yytext();
                          if (escapeForwardSlash) {
                            txt = escape(txt, '/');
                          }
                          return makeToken(txt);
                        }
{NUMBER}                { String normString = removeSoftHyphen(yytext());
						  return makeToken(normString); 
						}
{SUBSUPNUM}             { return makeToken(); }


/* Treebank 3 state for normalization */

<TB3>{FRACTION} 		{ String txt = yytext();
                  		 	if (escapeForwardSlash) {
                   		  	txt = escape(txt, '/');
                  		  	}
                  			if (normalizeSpace) {
                  			// change space to non-breaking space
                   			txt = txt.replace(' ', '\u00A0'); 

                  			}
                  			return makeToken(txt);
               			}

/* Strict Treebank 3 state for normalization */

<strictTB3>{FRACTION_TB3} { String txt = yytext();
                  			if (escapeForwardSlash) {
                    		txt = escape(txt, '/');
                  			}
                  			if (normalizeSpace) {
                  			// change space to non-breaking space
                    		txt = txt.replace(' ', '\u00A0'); 
                  			}
                  			return makeToken(txt);
                		  }

{OTHER_FRACTION}          { return normalizeFractions(yytext()); }

{NORMALIZED_PREFIXES}    { return normalizeAmpNext(); }

{HYPHEN_WORDS}          { if (escapeForwardSlash) {
							String txt = escape(yytext(), '/');
							return makeToken(txt);
                          } else {
                            return makeToken();
                          }
                        }

{DOLLAR}               { return makeToken(); }
{OTHER_CURRENCIES}     { if (normalizeCurrency) {
						    String normString = normalizeCurrency(yytext());
						    return makeToken(normString);
                          }
                          else {
                            return makeToken();
                          }
                        }
/* NON BREAKING PREFIXES 


\{              		{ if (normalizeOtherBrackets) {
                    		return makeToken(openCB); }
                  		  else {
                    		return makeToken();
                  		  }
                		}
\}              		
						{ if (normalizeOtherBrackets) {
                    		return makeToken(closeCB); }
                  		else {
                    		return makeToken();
                  		}
                		}
                
\[              { if (normalizeOtherBrackets) {
                    return makeToken(openSB); }
                  else {
                    return makeToken();
                  }
                }
\]              { if (normalizeOtherBrackets) {
                    return makeToken(closeSB); }
                  else {
                    return makeToken();
                  }
                }
\(              { if (normalizeParentheses) {
                    return makeToken(openRB); }
                  else {
                    return makeToken();
                  }
                }
\)              { if (normalizeParentheses) {
                    return makeToken(closeRB); 
                  }
                  else {
                    return makeToken();
                  }
                }

.|\n 			{ /* skip everything else */ }

   

<<EOF>> { return null; }