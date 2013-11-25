
import java.util.ArrayList;
import java.util.List;

%%

%public
%class Tokenizer
%standalone
%caseless 
%unicode

%{

public static final String openbracket = "-LRB-";
public static final String closebracket = "-RRB-";
public static final String openbrace = "-LCB-";
public static final String closebrace = "-RCB-";

String tok;
List<String> tokenList;

private String getToken() { 
    String tok = yytext();
    tokenList.add(tok);
    return tok;
}

private void printTokens() {
    getToken();
    for (String elem : tokenList) { 
System.out.println(elem);
    }
}
%}

/* Lexical Rules */

%%


\{      {  System.out.print(openbrace + " "); }

\(      { System.out.print(openbracket + " "); }

\}      { System.out.print(getToken() + " "); } 

\)      { System.out.print(" " + yytext()); } 
