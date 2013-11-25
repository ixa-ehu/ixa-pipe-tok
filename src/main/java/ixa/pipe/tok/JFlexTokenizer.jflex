package ixa.pipe.tok;

/* --------------------------Usercode Section------------------------ */

import ixa.pipe.resources.NonPrefixBreaker;
import java.io.InputStream;
import java.util.regex.Matcher;

	
	/* -----------------Options and Declarations Section----------------- */

%%

/* 
 * The name of the class; JFlex will write code to JFlexTokenizer.java  
 */

%class JFlexTokenizer
%unicode

/* 
 * The function to get the next token
 */

%type Object
%caseless
%char
%state strictTB3 TB3 EN ANCORA


/* 
 * Member variables and functions
 */

%{

 private TokenFactory tokenFactory;


 private Object tokenMaker() { 
     String tokenText = yytext();
     return tokenMaker(tokenString);
}

 private Object tokenMaker(String tokenString) { 
  tokenFactory.maketoken(tokenString, yychar(), yylength());
  }



%}
