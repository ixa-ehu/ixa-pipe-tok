package ixa.pipe.tok;

/* --------------------------Usercode Section------------------------ */


import java.io.Reader;
import java.util.logging.Logger;
import java.util.regex.Pattern;

	
/* -----------------Options and Declarations Section----------------- */

%%

%class JFlexLexer
%unicode
%type Token
%caseless
%char
%state SPTB3 PTB3 ANCORA
/* 
 * Member variables and functions
 */

%{

  private TokenFactory tokenFactory;
  private static final Logger LOGGER = Logger.getLogger(JFlexLexer.class.getName());
  private boolean seenUntokenizableCharacter;
  private enum UntokenizableOptions { NONE_DELETE, FIRST_DELETE, ALL_DELETE, NONE_KEEP, FIRST_KEEP, ALL_KEEP }
  private UntokenizableOptions untokenizable = UntokenizableOptions.FIRST_DELETE;
  
  
  /////////////////
  //// OPTIONS ////
  /////////////////
  
  
  /* Flags begin with ptb3Normalize minus americanize, brackets and forward slash escaping */
  
  //private boolean americanize = false;
  private boolean tokenizeNLs;
  private boolean escapeForwardSlash;
  private boolean normalizeBrackets;
  private boolean normalizeOtherBrackets;
  private boolean latexQuotes;
  private boolean unicodeQuotes;
  private boolean asciiQuotes;
  private boolean sptb3Normalize;
  private boolean ptb3Dashes = true;
  private boolean normalizeAmpersand = true;
  private boolean normalizeSpace = true;
  private boolean normalizeFractions = true;
  private boolean normalizeCurrency = true;
  private boolean ptb3Ldots = true;
  private boolean unicodeLdots = true;
  private boolean tokenizeParagraphs = true;
 
  public JFlexLexer(Reader breader, TokenFactory tokenFactory, String options) {
    this(breader);
    this.tokenFactory = tokenFactory;
    if (options == null) {
      options = "";
    }
    else if (options.equalsIgnoreCase("default")) {
         //americanize = false;
          escapeForwardSlash = false;
          normalizeBrackets = false;
          normalizeOtherBrackets = false;
          latexQuotes = true;
          unicodeQuotes = true;
          asciiQuotes = true;
          sptb3Normalize = false;
    }
    else if (options.equalsIgnoreCase("ptb3")) {
        //americanize = true;
        escapeForwardSlash = true;
        normalizeBrackets = true;
        normalizeOtherBrackets = true;
        latexQuotes = true;
        unicodeQuotes = true;
        asciiQuotes = true;
        sptb3Normalize = false;
      }  
    else if (options.equalsIgnoreCase("sptb3")) {
        escapeForwardSlash = true;
        normalizeBrackets = true;
        normalizeOtherBrackets = true;
        latexQuotes = true;
        unicodeQuotes = true;
        asciiQuotes = true;
        sptb3Normalize = true;
      }
    else if (options.equalsIgnoreCase("ancora")) { 
        escapeForwardSlash = false;
        normalizeBrackets = false;
        normalizeOtherBrackets = false;
        latexQuotes = false;
        unicodeQuotes = false;
        asciiQuotes = true;
        sptb3Normalize = false;
    }
    if (sptb3Normalize) {
      yybegin(SPTB3);
    }
    else {
      yybegin(PTB3);
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
  
  /////////////////////////////
  //// HYPHENS and ESCAPES ////
  /////////////////////////////
  
  private static String normalizeSoftHyphen(String in) {
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

  private Token normalizeQuotes(String token, boolean probablyLeft) {
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
  //// ELLIPSIS ////
  //////////////////
  
  public static final String NEWLINE_TOKEN = "*NL*";
  public static final String PARAGRAPH_TOKEN = "*<P>*";
  public static final String ptbMultiDots = "...";
  public static final String unicodeMultiDots = "\u2026";
  
  private Token normalizeMultiDots(final String token) {
    if (ptb3Ldots) {
      return makeToken(ptbMultiDots);
    } else if (unicodeLdots) {
      return makeToken(unicodeMultiDots);
    } else {
      return makeToken(token);
    }
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
    Token token;
    if (tokenString.equalsIgnoreCase("*NL*")) {
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

/* \u3000 is ideographic space */
SPACE = [ \t\u0020\u00A0\u2000-\u200A\u3000]
SPACES = {SPACE}+
NEWLINE = \r|\r?\n|\u2028|\u2029|\u000B|\u000C|\u0085
SPACENL = ({SPACE}|{NEWLINE})
SPACENLS = {SPACENL}+
SENTEND = {SPACENL}({SPACENL}|[:uppercase:])


/*---- APOSTROPHES and PUNCTUATION ----*/

APOS = ['\u0092\u2019]|&apos;
/* Includes extra ones that may appear inside a word, rightly or wrongly */
ALL_APOS = {APOS}|[\u0091\u2018\u201B]
SPECIAL_APOS_AUX = n{ALL_APOS}t
SPECIAL_WEBS = &[aeiouAEIOU](acute|grave|uml);
INTRA_SENT_PUNCT = [,;:\u3001]
DOUBLE_QUOTE = \"|&quot;
LESS_THAN = <|&lt;
GREATER_THAN = >|&gt;
AMP = &amp;  
DASHES = &(MD|mdash|ndash);|[\u0096\u0097\u2013\u2014\u2015]
SPECIAL_PUNCT = &(HT|TL|UR|LR|QC|QL|QR|odq|cdq|#[0-9]+);
HYPHEN = [-_\u058A\u2010\u2011]
LDOTS = \.{3,5}|(\.[ \u00A0]){2,4}\.|[\u0085\u2026]
ASTERISK = \*+|(\\\*){1,3}
ATS = @+
HASHES = #+
UNDERSCORES = _+
OTHER_PUNCT = {ATS}|{HASHES}|{UNDERSCORES}
HYPHENS = \-+
FAKEDUCKFEET = <<|>>
QUOTES = {APOS}|''|[`\u2018\u2019\u201A\u201B\u201C\u201D\u0091\u0092\u0093\u0094\u201E\u201F\u2039\u203A\u00AB\u00BB]{1,2}


/* ---- WORDS; note that U+0237-U+024F (dotless j) isn't in [:letter:] ----*/

LETTER = ([:letter:]|{SPECIAL_WEBS}|[\u00AD\u0237-\u024F\u02C2-\u02C5\u02D2-\u02DF\u02E5-\u02FF\u0300-\u036F\u0370-\u037D\u0384\u0385\u03CF\u03F6\u03FC-\u03FF\u0483-\u0487\u04CF\u04F6-\u04FF\u0510-\u0525\u055A-\u055F\u0591-\u05BD\u05BF\u05C1\u05C2\u05C4\u05C5\u05C7\u0615-\u061A\u063B-\u063F\u064B-\u065E\u0670\u06D6-\u06EF\u06FA-\u06FF\u070F\u0711\u0730-\u074F\u0750-\u077F\u07A6-\u07B1\u07CA-\u07F5\u07FA\u0900-\u0903\u093C\u093E-\u094E\u0951-\u0955\u0962-\u0963\u0981-\u0983\u09BC-\u09C4\u09C7\u09C8\u09CB-\u09CD\u09D7\u09E2\u09E3\u0A01-\u0A03\u0A3C\u0A3E-\u0A4F\u0A81-\u0A83\u0ABC-\u0ACF\u0B82\u0BBE-\u0BC2\u0BC6-\u0BC8\u0BCA-\u0BCD\u0C01-\u0C03\u0C3E-\u0C56\u0D3E-\u0D44\u0D46-\u0D48\u0E30-\u0E3A\u0E47-\u0E4E\u0EB1-\u0EBC\u0EC8-\u0ECD])
WORD = {LETTER}+([.!?]{LETTER}+)*

/* -- French, Italian APOS tokenization c' l' m' -- */
DET_APOS = ([A-Za-z]|[Qq]u){ALL_APOS}
/* -- English APOS tokenization 's 'm 'd 're 've 'll --*/
APOS_AUX = {APOS}([msdMSD]|re|ve|ll)

/* \u00AD is soft hyphen */
SPECIAL_WORD = [A-Za-z\u00AD]*[A-MO-Za-mo-z](\u00AD)*
WORD_APOS = {APOS}n{APOS}?|[lLdDjJ]{APOS}|Dunkin{APOS}|somethin{APOS}|ol{APOS}|{APOS}em|[A-HJ-XZn]{ALL_APOS}[:letter:]{2}[:letter:]*|{APOS}[2-9]0s|{APOS}till?|[:letter:][:letter:]*[aeiouyAEIOUY]{ALL_APOS}[aeiouA-Z][:letter:]*|{APOS}cause|cont'd\.?|'twas|nor'easter|c'mon|e'er|s'mores|ev'ry|li'l|nat'l
Y_APOS_WORD = y{APOS}
NORMALIZED_PREFIXES = -(RRB|LRB|RCB|LCB|RSB|LSB)-|C\.D\.s|pro-|anti-|S(&|&amp;)P-500|S(&|&amp;)Ls|Cap{APOS}n|c{APOS}est
APOS_DIGIT_DIGIT = {APOS}[0-9][0-9]

HYPHEN_WORDS = [A-Za-z0-9]+(-[A-Za-z]+){0,2}(\\?\/[A-Za-z0-9]+(-[A-Za-z]+){0,2}){1,2}
OTHER_HYPHEN_WORDS = ([dDoOlL]{ALL_APOS}([:letter:]|[:digit:]))?([:letter:]|[:digit:])+({HYPHEN}([dDoOlL]{ALL_APOS}([:letter:]|[:digit:]))?([:letter:]|[:digit:])+)*
WORD_AMP = [A-Z]+(([+&]|{AMP})[A-Z]+)+
WORD_HYPHEN_ACRONYM = [A-Za-z0-9][A-Za-z0-9.,\u00AD]*(-([A-Za-z0-9\u00AD]+|{ACRONYM}\.))+


/*---- URLS ----*/
STRICT_URL = https?:\/\/[^ \t\n\f\r\"<>|()]+[^ \t\n\f\r\"<>|.!?(){},-]
APPROX_URL = (https?:\/\/)*((www\.([^ \t\n\f\r\"<>|.!?(){},]+\.)+[a-zA-Z]{2,4})|(([^ \t\n\f\r\"`'<>|.!?(){},-_$]+\.)+(com|net|org|edu|es|fr|uk|it|nl|de|eu|cat)))(\/[^ \t\n\f\r\"<>|()]+[^ \t\n\f\r\"<>|.!?(){},-])?
EMAIL = [a-zA-Z0-9][^ \t\n\f\r\"<>|()\u00A0]*@([^ \t\n\f\r\"<>|().\u00A0]+\.)*([^ \t\n\f\r\"<>|().\u00A0]+)
/* Technically, names should be capped at 15 characters, but this is not 
useful with not complying ones*/
TWITTER_NAME = @[a-zA-Z_][a-zA-Z_0-9]*
TWITTER_CATEGORY = #{WORD}
TWITTER = {TWITTER_NAME}|{TWITTER_CATEGORY}
/* Smileys (based on Chris Potts' publicly available tutorial, but without "8)", "do:" or "):" plus simple Asian smileys 
 */
SMILEY = [<>]?[:;=][\-o\*']?[\(\)DPdpO\\{@\|\[\]]
ASIANSMILEY = [\^x=~<>]\.\[\^x=~<>]|[\-\^x=~<>']_[\-\^x=~<>']|\([\-\^x=~<>'][_.]?[\-\^x=~<>']\)


/*---- NUMBERS and DATES ----*/
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
/* U+2200-U+2BFF has a lot of the various mathematical, etc. symbol ranges */
MISC_SYMBOL = [+%&~\^|\\¦\u00A7¨\u00A9\u00AC\u00AE¯\u00B0-\u00B3\u00B4-\u00BA\u00D7\u00F7\u0387\u05BE\u05C0\u05C3\u05C6\u05F3\u05F4\u0600-\u0603\u0606-\u060A\u060C\u0614\u061B\u061E\u066A\u066D\u0703-\u070D\u07F6\u07F7\u07F8\u0964\u0965\u0E4F\u1FBD\u2016\u2017\u2020-\u2023\u2030-\u2038\u203B\u203E-\u2042\u2044\u207A-\u207F\u208A-\u208E\u2100-\u214F\u2190-\u21FF\u2200-\u2BFF\u3012\u30FB\uFF01-\uFF0F\uFF1A-\uFF20\uFF3B-\uFF40\uFF5B-\uFF65\uFF65]
/* \uFF65 is Halfwidth katakana middle dot; \u30FB is Katakana middle dot */
/* Math and other symbols that stand alone: °²× ∀ */
// bullet chars: 2219, 00b7, 2022, 2024


///////////////////////////////
//// NON BREAKING PREFIXES ////
//////////////////////////////

/* -- for end of sentence acronyms --*/

ACRO_NEXT_WORD_EN = [A]bout|[A]ccording|[A]dditionally|[A]fter|[A]n|[A]|[A]s|[A]t|[B]ut|[E]arlier|[H]e|[H]er|[H]ere|[H]owever|[I]f|[I]n|[I]t|[L]ast|[M]any|[M]ore|[M]r\.|[M]s\.|[N]ow|[O]nce|[O]ne|[O]ther|[O]ur|[S]he|[S]ince|[S]o|[S]ome|[S]uch|[T]hat|[T]he|[T]heir|[T]hen|[T]here|[T]hese|[T]hey|[T]his|[W]e|[W]hen|[W]hile|[W]hat|[Y]et|[Y]ou

ACRO_NEXT_WORD_ES = [A]lgun[ao]s?|[A]lgún|[A]mb[ao]s?|[B]astantes|[C]ada|[C]onmigo|[C]ontigo|[C]ualesquier|[C]ualquier|[C]uant[ao]s?|[É]l|[E]ll[ao]s?|[D]emasiad[ao]s?|[L]l[aeo]s?|[M][eí]|[M]í[ao]s?|[M]uch[ao]s?|[N]ada|[N]adie|[N]aide|[N]ingun[ao]s?|[N]os|[N]nosotr[ao]s?|[N]nuestr[ao]s?|[O]s|[O]tr[ao]s?|[P]oc[ao]s?|[Q]uienesquiera|[Q]uienquiera|[S]end[ao]s?|[S]uy[ao]s?|[T][eiú]|[T]uy[ao]s?|[T]ant[ao]s?|[T]od[ao]s?|[U]n[ao]s?|[U]n|[V]ari[ao]s|[U]sted|[U]stedes|[V]osotr[ao]s?|[V]uestr[ao]s?|[V]os|[Y]o|[S]obre|[S]egún|[A]demás|[A]dicionalmente|[D]espués|[L]uego|[A]ntes|[A]nteriormente|[A]l|[E]n|[A]quí|[S]in?|[P]ero|[Ú]ltimamente|[D]esde|[T]an|[M]ientras|[A]ún|[A]unque

ACRO_NEXT_WORD = {ACRO_NEXT_WORD_EN}|{ACRO_NEXT_WORD_ES}

/* Contains also acronyms with lower words */
ACRONYM = [A-Za-z](\.[A-Za-z])+|(Canada|Sino|Korean|EU|Japan|non)-U\.S|U\.S\.-(U\.K|U\.S\.S\.R)
ACRONYMS = ({ACRONYM})\.

/* ONLY BEFORE NUMBERS 
 * Unless they also appear in ABBREV_DATES or SPECIAL_ABBREV_PREFIX.
 * est. is "estimated" -- common in some financial contexts. ext. is extension, ca. is circa.
 * Maybe also "op." for "op. cit." but also get a photo op. 
 */

ABBREV_NUMBER = (al|ca|figs?|prop|nos?|Nrs?|art|bldg|prop|pp|op)\.

////////////////////////////////////////////////
//// DATES AND COMPANIES + LOWERCASE  WORDS ////
////////////////////////////////////////////////


/* ABBREV_DATES: FOLLOWED BY LOWER CASE WORDS 
 * If followed by uppercase, a sentence boundary is assumed.
 */

ABBREV_MONTH_DE = Jän|März|Mai|Okt|Dez
ABBREV_MONTH_EN = Jan|Feb|Mar|Apr|Jun|Jul|Aug|Sep|Sept|Oct|Nov|Dec
ABBREV_MONTH_ES = Ene|Febr|May|Abr|Ag|Dic
ABBREV_MONTH_FR = janv|févr|mars|avril|juin|juil|août|déc
ABBREV_MONTH_IT = genn|febbr|magg|giugno|luglio|sett|ott
ABBREV_MONTH_NL = maart|mei|juni|juli|okt
ABBREV_MONTH = {ABBREV_MONTH_DE}|{ABBREV_MONTH_EN}|{ABBREV_MONTH_ES}|{ABBREV_MONTH_FR}|{ABBREV_MONTH_IT}|{ABBREV_MONTH_NL}


ABBREV_DAYS_DE = So|Mo|Di|Mi|Do|Fr|Sa
ABBREV_DAYS_EN = Mon|Tue|Tues|Wed|Thu|Thurs|Fri|Sat|Sun
ABBREV_DAYS_ES = Lun|Mar|Miér|Jue|Vier|Sáb|Dom
ABBREV_DAYS_FR = lun|mer|jeu|ven|sam|dim
ABBREV_DAYS_IT = mar|gio|ven|sab
ABBREV_DAYS_NL = ma|woe|vrij|za|zo|wo|vr 

ABBREV_DAYS = {ABBREV_DAYS_DE}|{ABBREV_DAYS_EN}|{ABBREV_DAYS_ES}|{ABBREV_DAYS_FR}|{ABBREV_DAYS_IT}|{ABBREV_DAYS_NL}

ABBREV_STATE = Ala|Ariz|[A]rk|Calif|Colo|Conn|Dak|Del|Fla|Ga|[I]ll|Ind|Kans?|Ky|La|[M]ass|Md|Mich|Minn|[M]iss|Mo|Mont|Neb|Nev|Okla|[O]re|Pa|Penn|Tenn|Tex|Va|Vt|[W]ash|Wisc?|Wyo

/* Bhd is Malaysian companies! Rt. is Hungarian? */
/* Special case: Change the class of Pty when followed by Ltd to not sentence break (in main code below)... */
ABBREV_COMP = Inc|Cos?|Corp|Pp?t[ye]s?|Ltd|Plc|Rt|Bancorp|Dept|Bhd|Assn|Univ|Intl|Sys|Dep|Fac|Coop|Soc

/* Don't included fl. oz. since Oz turns up too much in caseless tokenizer. ft now allows upper after it for "Fort" use. */
ABBREV_NUM = Ph|tel|est|ext|sq
ABBREV_AFTER_PERSON = Jr|Sr|Bros|(Ed|Ph)\.D|Blvd|Rd|Esq
ABBREV_DATES = ({ABBREV_MONTH}|{ABBREV_DAYS}|{ABBREV_STATE}|{ABBREV_COMP}|{ABBREV_NUM}|{ABBREV_AFTER_PERSON}|etc|al|seq)\.

///////////////////////
//// PERSON TITLES ////
///////////////////////

ABBREV_PREFIX_EN = Adj|Adm|Adv|Assoc|Asst|Attys?|Ave|Bart|Bldg|Brig|Bros|Capt|Col|Co?mdr|Con|Corp|Cpl|Det|DR|Drs?|Ens|Ft|Gen|Govs?|Hon|Hr|Hosp|Insp|Lieut|Lt|Maj|Messrs|[M]iss|Mlle|Mme|MM|MR|MRS|MS|Mr|Mrs|Msgr|Ms|Msgr|Mt|Op|Ord|Ph|Pfc|Pres|Profs?|Pvt|Reps?|Res|Rev|Sens?|Sfc|Sgt|Sr|Ste?|Spc|Supts?|Surg

ABBREV_PREFIX_ES = Apdo|Av|Bco|CC\.AA|Da|Dep|Dn|Dr|Dra|EE\.UU|Excmo|FF\.CC|Fil|Gral|Let|Lic|Prof|Pts|Rte|Sr|Sra|Srta|Sta

ABBREV_PREFIX_DE= Dkr

ABBREV_PREFIX_FR= Msr|Mgr

ABBREV_PREFIX_NL=Lt|maj|Mej|mevr|Mme|Mw|plv

ABBREV_PREFIX = {ABBREV_PREFIX_DE}|{ABBREV_PREFIX_EN}|{ABBREV_PREFIX_FR}|{ABBREV_PREFIX_ES}|{ABBREV_PREFIX_NL}

/* SPECIAL_ABBREV_PREFIX are list of titles. 
 * These are often followed by upper-case names, but do not indicate sentence breaks
 */
SPECIAL_ABBREV_COMP = Invt|Elec|Natl|M[ft]g
ABBREV_UPPER = [A-Za-z]|{ABBREV_PREFIX}|vs|Alex|Wm|Jos|Cie|a\.k\.a|cf|TREAS|{ACRONYM}|{SPECIAL_ABBREV_COMP}
SPECIAL_ABBREV_PREFIX = {ABBREV_UPPER}\.

/* phone numbers. keep multi dots pattern separate, so not confused with decimal numbers. */
PHONE = (\([0-9]{2,3}\)[ \u00A0]?|(\+\+?)?([0-9]{2,4}[\- \u00A0])?[0-9]{2,4}[\- \u00A0])[0-9]{3,4}[\- \u00A0]?[0-9]{3,5}|((\+\+?)?[0-9]{2,4}\.)?[0-9]{2,4}\.[0-9]{3,4}\.[0-9]{3,5}


/* ------------------------Lexical Rules Section---------------------- */

%%

/* ptb3 normalized ampersand */
{AMP}                       { return normalizeAmpNext(); }

/* ptb3 normalized dashes */
{DASHES}                    { if (ptb3Dashes) {
                    	        return makeToken(ptbDash); 
                                }
                  		else {
                    		return makeToken();
                   		}
                	    }
                
/* special punctuation */
{SPECIAL_PUNCT}       	    { return makeToken(); }

/* special words slang */

cannot                      { yypushback(3) ; return makeToken(); }
gonna|gotta|lemme|gimme|wanna   { yypushback(2) ; return makeToken(); }

/*---- WORDS  including they're and n't and so on ----*/
{WORD}/{APOS_AUX}           {   String origTxt = yytext();
                                String tmp = normalizeSoftHyphen(origTxt);
                                return makeToken(tmp);
                            }
                        	
{SPECIAL_WORD}/{SPECIAL_APOS_AUX}   {   String origTxt = yytext();
                                        String normString = normalizeSoftHyphen(origTxt);
                          	        return makeToken(normString); 
                          	    }

{WORD}                      {   String origTxt = yytext();
                                String normString = normalizeSoftHyphen(origTxt);
                                return makeToken(normString);
                            }

{WORD_APOS}                 { return makeToken(); }

{Y_APOS_WORD}/[:letter:]    { return makeToken(); }

/*---- URLs ----*/
{STRICT_URL}                { String txt = yytext();
                                if (escapeForwardSlash) {
                                    txt = escape(txt, '/');
                                    txt = escape(txt, '*');
                                }
                                return makeToken(txt); 
                            }

{APPROX_URL}                { String txt = yytext();
                                if (escapeForwardSlash) {
                                    txt = escape(txt, '/');
                                    txt = escape(txt, '*');
                                }
                                return makeToken(txt); 
                            }

{EMAIL}                     { return makeToken(); }
{TWITTER}                   { return makeToken(); }

/*---- APOSTROPHES BREAKING ----*/

/*French and Italian style*/
[^A-Za-z]{DET_APOS}                  { return normalizeQuotes(yytext(), false); }

/*English*/
{APOS_AUX}/[^A-Za-z]        { return normalizeQuotes(yytext(), false); }
{SPECIAL_APOS_AUX}          { return normalizeQuotes(yytext(), false); }

/*---- DATES and NUMERS ----*/

{DATE}                      { String txt = yytext();
                                if (escapeForwardSlash) {
                                    txt = escape(txt, '/');
                                }
                                return makeToken(txt);
                            }
{NUMBER}                    {   String origTxt = yytext();
                                String normString = normalizeSoftHyphen(origTxt); 
                                return makeToken(normString); 
			    }
{SUBSUPNUM}                 { return makeToken(); }


/*---- Ancora state for normalization ----*/

<ANCORA>{FRACTION} 	    { String txt = yytext();
                  	        if (escapeForwardSlash) {
                   		txt = escape(txt, '/');
                  		}
                  		if (normalizeSpace) {
                  		// change space to non-breaking space
                   		txt = txt.replace(' ', '\u00A0'); 
                                }
                  		return makeToken(txt);
               		    }

/*---- Treebank 3 state for normalization ----*/

<PTB3>{FRACTION} 	    { String txt = yytext();
                  	        if (escapeForwardSlash) {
                   		txt = escape(txt, '/');
                  		}
                  		if (normalizeSpace) {
                  		// change space to non-breaking space
                   		txt = txt.replace(' ', '\u00A0'); 
                                }
                  		return makeToken(txt);
               		    }

/*---- Strict Treebank 3 state for normalization ----*/

<SPTB3>{FRACTION_TB3}   { String txt = yytext();
                  	        if (escapeForwardSlash) {
                    	            txt = escape(txt, '/');
                  		}
                  		if (normalizeSpace) {
                  		// change space to non-breaking space
                    		txt = txt.replace(' ', '\u00A0'); 
                  		}
                  		return makeToken(txt);
                	    }

{OTHER_FRACTION}            { return normalizeFractions(yytext()); }

{NORMALIZED_PREFIXES}       { return normalizeAmpNext(); }

{HYPHEN_WORDS}              {   if (escapeForwardSlash) {
								String txt = escape(yytext(), '/');
									return makeToken(txt);
                                }
                                else {
                                return makeToken();
                                }
                            }

{DOLLAR}                    { return makeToken(); }
{OTHER_CURRENCIES}          {   if (normalizeCurrency) {
			        				String normString = normalizeCurrency(yytext());
			        				return makeToken(normString);
                                } 
                                else {
                                return makeToken();
                                }
                            }
                          
                            
/* -------- NON BREAKING PREFIXES ------------*/

/* Any acronym can be treated as sentence final iff followed by this list 
* of words (pronouns, determiners, and prepositions, etc.). "U.S." is the single 
* big source of errors.  Character classes make this rule case sensitive! (This is needed!!) 
*/
{ACRONYMS}/({SPACENLS})({ACRO_NEXT_WORD}){SPACENL} {
                          // try to work around an apparent jflex bug where it
                          // gets a space at the token end by getting
                          // wrong the length of the trailing context.
                          while (yylength() > 0) {
                            char last = yycharat(yylength()-1);
                            if (last == ' ' || last == '\t' || (last >= '\n' && last <= '\r' || last == '\u0085')) {
                              yypushback(1);
                            } else {
                              break;
                            }
                          }
                          String s;
                          if (sptb3Normalize && ! "U.S.".equals(yytext())) {
                            yypushback(1); // return a period for next time
                            s = yytext();
                          } else {
                            s = yytext();
                            yypushback(1); // return a period for next time
                          }
                          return makeToken(s);
                        }

/* Special case to get ca., fig. or Prop. before numbers */
{ABBREV_NUMBER}/{SPACENL}?[:digit:]   {
                          // try to work around an apparent jflex bug where it
                          // gets a space at the token end by getting
                          // wrong the length of the trailing context.
                          while (yylength() > 0) {
                            char last = yycharat(yylength()-1);
                            if (last == ' ' || last == '\t' || (last >= '\n' && last <= '\r' || last == '\u0085')) {
                              yypushback(1);
                            } else {
                              break;
                            }
                          }
			  				return makeToken();
						}
						
/* Special case to get pty. ltd. or pty limited. 
 * Also added "Co." since someone complained, but usually a comma after it. 
 */
(pt[eyEY]|co)\./{SPACE}(ltd|lim)  { return makeToken(); }

{ABBREV_DATES}/{SENTEND}   {    String s;
                          	if (sptb3Normalize && ! "U.S.".equals(yytext())) {
                                yypushback(1); // return a period for next time
                                s = yytext();
                                } 
                                else {
                                    s = yytext();
                                    yypushback(1); // return a period for next time
                                }
                          	return makeToken(s); 
                            }


{ABBREV_DATES}/[^][^]        { return makeToken(); }

/* this one should only match at the end of file
 * since the previous one matches even newlines
*/
{ABBREV_DATES}               { String s;
                              if (sptb3Normalize && ! "U.S.".equals(yytext())) {
                              yypushback(1); // return a period for next time
                              s = yytext();
                              } else {
                                s = yytext();
                                yypushback(1); // return a period for next time
                              }
                              return makeToken(s);
                             }

{SPECIAL_ABBREV_PREFIX}     { return makeToken(); }
{ABBREV_UPPER}/{SPACE}      { return makeToken(); }

{ACRONYM}/{SPACENL}         { return makeToken(); }
{APOS_DIGIT_DIGIT}/{SPACENL} { return makeToken(); }

{WORD}\./{INTRA_SENT_PUNCT} {   String origTxt = yytext();
                                String normString = normalizeSoftHyphen(origTxt);
				return makeToken(normString); 
                            }

{PHONE}                 	{ String txt = yytext();
                          		if (normalizeSpace) {
                            	txt = txt.replace(' ', '\u00A0'); // change space to non-breaking space
                          	}
                          		if (normalizeBrackets) {
                            		txt = LEFT_PAREN_PATTERN.matcher(txt).replaceAll(openRB);
                            		txt = RIGHT_PAREN_PATTERN.matcher(txt).replaceAll(closeRB);
                          		}
                          	return makeToken(txt);
                        	}

/*---- QUOTES ----*/ 
{DOUBLE_QUOTE}/[A-Za-z0-9$]  { return normalizeQuotes(yytext(), true); }
{DOUBLE_QUOTE}               { return normalizeQuotes(yytext(), false); }

{LESS_THAN}              	{ return makeToken("<"); }
{GREATER_THAN}           	{ return makeToken(">"); }

{SMILEY}/[^A-Za-z] 			{ 	String txt = yytext();
                  				if (normalizeBrackets) {
                    				txt = LEFT_PAREN_PATTERN.matcher(txt).replaceAll(openRB);
                    				txt = RIGHT_PAREN_PATTERN.matcher(txt).replaceAll(closeRB);
                  				}
                  				return makeToken(txt);
                			}
                			
{ASIANSMILEY}        		{ 	String txt = yytext();
                  				if (normalizeBrackets) {
                    			txt = LEFT_PAREN_PATTERN.matcher(txt).replaceAll(openRB);
                    			txt = RIGHT_PAREN_PATTERN.matcher(txt).replaceAll(closeRB);
                  				}
                  				return makeToken(txt);
                			}
                			
/*---- BRACKETS ----*/ 

\{                          {   if (normalizeOtherBrackets) {
                    	            return makeToken(openCB); 
                            }
                  	        else {
                    		    return makeToken();
                  			}
                	    	}
\}              		
			    			{   if (normalizeOtherBrackets) {
                    		        return makeToken(closeCB); 
                                    }
                  	    	    else {
                    		        return makeToken();
                  		    	}
                	    	}
                
\[                          {   if (normalizeOtherBrackets) {
                                    return makeToken(openSB); 
                                }
                                else {
                                    return makeToken();
                                }
                            }       
\]                          {   if (normalizeOtherBrackets) {
                                    return makeToken(closeSB); 
                                }
                                else {
                                    return makeToken();
                                }                    
                            }
\(                          {   if (normalizeBrackets) {
                                    return makeToken(openRB); 
                                }
                                else {
                                    return makeToken();
                                }
                            }
\)                          {   if (normalizeBrackets) {
                                    return makeToken(closeRB); 
                                }
                                else {
                                    return makeToken();
                                }
                            }

{HYPHENS}       			{ 	if (yylength() >= 3 && yylength() <= 4 && ptb3Dashes) {
                    			return makeToken(ptbDash);
                  				} else {
                    			return makeToken();
                  				}
                			}
                			
{LDOTS}         			{ return normalizeMultiDots(yytext()); }

{OTHER_PUNCT}      			{ return makeToken(); }
{ASTERISK}         			{ 	if (escapeForwardSlash) {
                    			return makeToken(escape(yytext(), '*')); }
                  				else {
                    			return makeToken();
                  				}
                			}

/*---- START and END Sentence ----*/
                			
{INTRA_SENT_PUNCT}       	{ return makeToken(); }
[?!]+          	 			{ return makeToken(); }

[.¡¿\u037E\u0589\u061F\u06D4\u0700-\u0702\u07FA\u3002]  { return makeToken(); }
=               			{ return makeToken(); }
\/              			{ 	if (escapeForwardSlash) {
                    			return makeToken(escape(yytext(), '/')); }
                  				else {
                    			return makeToken();
                  				}
                			}

/*---- OTHER NONBREAKING WORDS with ACRONYMS and HYPHENS ----*/                			
{WORD_AMP}\./{INTRA_SENT_PUNCT}	{ return makeToken(normalizeSoftHyphen(yytext())); }
{WORD_AMP}        			{ return makeToken(normalizeSoftHyphen(yytext())); }
{OTHER_HYPHEN_WORDS}\./{INTRA_SENT_PUNCT}	{ return makeToken(); }
{OTHER_HYPHEN_WORDS}        { return makeToken(); }
{WORD_HYPHEN_ACRONYM}\./{INTRA_SENT_PUNCT}	{ return normalizeAmpNext(); }
{WORD_HYPHEN_ACRONYM}       { return normalizeAmpNext(); }

/*---- QUOTES ----*/
/* invert quote - often but not always right */
'/[A-Za-z][^ \t\n\r\u00A0] 	{ return normalizeQuotes(yytext(), true); }
                                         
{APOS_AUX}        			{   return normalizeQuotes(yytext(), false); }
{QUOTES}        			{   return normalizeQuotes(yytext(), false); }
{FAKEDUCKFEET}  			{   return makeToken(); }
{MISC_SYMBOL}    			{   return makeToken(); }

{NEWLINE}/{NEWLINE}+                    {   if (tokenizeParagraphs) { 
                                                return makeToken(PARAGRAPH_TOKEN);
                                            }
                                        } 

{NEWLINE}      				{   if (tokenizeNLs) {
                      			        return makeToken(NEWLINE_TOKEN); 
                			    }
                			}							


/*---- skip non printable characters ----*/

[\\x00-\\x19]|{SPACES}		{ }

/*---- warn about other non tokenized characters ----*/

.       { String str = yytext();
          int first = str.charAt(0);
          String msg = String.format("Untokenizable: %s (U+%s, decimal: %s)", yytext(), Integer.toHexString(first).toUpperCase(), Integer.toString(first));
          switch (untokenizable) {
            case NONE_DELETE:
              break;
            case FIRST_DELETE:
              if ( ! this.seenUntokenizableCharacter) {
                LOGGER.warning(msg);
                this.seenUntokenizableCharacter = true;
              }
              break;
            case ALL_DELETE:
              LOGGER.warning(msg);
              this.seenUntokenizableCharacter = true;
              break;
            case NONE_KEEP:
              return makeToken();
            case FIRST_KEEP:
              if ( ! this.seenUntokenizableCharacter) {
                LOGGER.warning(msg);
                this.seenUntokenizableCharacter = true;
              }
              return makeToken();
            case ALL_KEEP:
              LOGGER.warning(msg);
              this.seenUntokenizableCharacter = true;
              return makeToken();
          }
        }
<<EOF>> 					{ return null; }

/*skip everything else*/
/*.|\n 			{ } */


