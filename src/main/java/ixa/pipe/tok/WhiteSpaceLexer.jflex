package ixa.pipe.tok;

/* --------------------------Usercode Section------------------------ */


import java.io.Reader;
import java.util.logging.Logger;
import java.util.regex.Pattern;

	
/* -----------------Options and Declarations Section----------------- */

%%

%class WhiteSpaceLexer
%unicode
%type Token
%caseless
%char
 
/* 
 * Member variables and functions
 */

%{

  private TokenFactory tokenFactory;
  private static final Logger LOGGER = Logger.getLogger(WhiteSpaceLexer.class.getName());
  
  
  /////////////////
  //// OPTIONS ////
  /////////////////
  
  
  /* Flags begin with ptb3Normalize minus americanize, brackets and forward slash escaping */
  
  //private boolean americanize = false;
  private boolean tokenizeNLs;
  private boolean tokenizeParagraphs;
 
  public WhiteSpaceLexer(Reader breader, TokenFactory tokenFactory, String options) {
    this(breader);
    this.tokenFactory = tokenFactory;
    if (options == null) {
      options = "";
    }
    else if (options.equalsIgnoreCase("yes")) {
        tokenizeNLs = true;
        tokenizeParagraphs = true;
    }  
  }
  
  //////////////////
  //// NEWLINES ////
  //////////////////
  
  public static final String NEWLINE_TOKEN = "*NL*";
  public static final String PARAGRAPH_TOKEN = "*<P>*";
  
  ////////////////////////
  //// MAIN FUNCTIONS ////
  ////////////////////////
  
  
  private Token makeToken() { 
    String tokenString = yytext();
    return makeToken(tokenString);
  }

  private Token makeToken(String tokenString) {
    Token token;
    if (tokenString.equalsIgnoreCase("*NL*") || tokenString.equalsIgnoreCase("*<P>*")) {
      token = tokenFactory.createToken(tokenString, yychar, 1);
    }
    else { 
      token = tokenFactory.createToken(tokenString, yychar, yylength());
    }
    return token;
  }

%}

  ////////////////
  //// MACROS ////
  ///////////////

/*---- SPACES ----*/ 

PARAGRAPH = [\n\u2028\u2029\u0085]{1,123}
NEWLINE = \r|\r?\n|\u2028|\u2029|\u0085
OTHER_NEWLINE = [\u000B\u000C]
SPACE = [ \t\u00A0\u1680\u180E\u2002-\u200B\u202F\u205F\u2060\u3000]
SPACES = {SPACE}+



TEXT = [^ \t\u00A0\u1680\u180E\u2002-\u200B\u202F\u205F\u2060\u3000\r\n\u0085\u2028\u2029\u000B\u000C]+

/* ------------------------Lexical Rules Section---------------------- */

%%


{PARAGRAPH}                             {   if (tokenizeParagraphs) { 
                                                return makeToken(PARAGRAPH_TOKEN);
                                            }
                                        } 

{NEWLINE}      				{   if (tokenizeNLs) {
                			        return makeToken(NEWLINE_TOKEN); 
                                        }
}
{OTHER_NEWLINE} 			{   if (tokenizeNLs) {
                        		        return makeToken(NEWLINE_TOKEN); 
                                            }
                                        }

{SPACES}                                {
                                        }

{TEXT}                                  {   return makeToken(); 
                                        }

<<EOF>>     				{ return null; }
