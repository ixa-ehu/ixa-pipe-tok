package ixa.pipe.tok;

/* --------------------------Usercode Section------------------------ */

import ixa.pipe.resources.NonPrefixBreaker;
import java.io.InputStream;
import java.util.regex.Matcher;

	
	/* -----------------Options and Declarations Section----------------- */

%%

/* The name of the class; JFlex will write code to JFlexTokenizer.java  
 * /
%class JFlexTokenizer
%unicode 
/* The function to get the next token
 * /
%function next
%type char
%caseless
%state YyStrictlyTreebank3 YyTraditionalTreebank3
