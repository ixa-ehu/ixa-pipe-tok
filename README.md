
ixa-pipe-tok
============

ixa-pipe-tok is a multilingual rule-based tokenizer and sentence segmenter. 
ixa-pipe-tok is part of IXA Pipeline ("is a pipeline"), a multilingual NLP pipeline developed 
by the IXA NLP Group [http://ixa2.si.ehu.es/ixa-pipes]. 

Please go to [http://ixa2.si.ehu.es/ixa-pipes] for general information about the IXA
pipeline tools but also for **official releases, including source code and binary
packages for all the tools in the IXA pipeline**.

This document is intended to be the **usage guide of ixa-pipe-tok**. If you really need to clone
and install this repository instead of using the releases provided in
[http://ixa2.si.ehu.es/ixa-pipes], please scroll down to the end of the document for
the [installation instructions](#installation).

## OVERVIEW

This module provides Multilingual Sentence Segmentation and Tokenization for a number of languages, 
such as Dutch, German, English, French, Italian and Spanish. ixa-pipe-tok is part of 
IXA Pipeline ("is a pipeline"), a multilingual NLP pipeline developed by the 
IXA NLP Group (ixa.si.ehu.es). 

**ixa-pipe-tok outputs** tokenized and segmented text in **three formats**: 

  + **NAF (default)**: KAF is used to represent tokenized text but also to
    as an interchange format between other modules in IXA pipeline
    (http://github.com/ixa-ehu). NAF is generated using Kaflib
    (http://github.com/ixa-ehu/kaflib). 
  + **Oneline**: tokenized text with one sentence per line and markers
    (\*\<P\>\*) for paragraphs, if that option is chosen. 
  + **Conll**: one token per line, two newlines per sentence and markers for
    paragraphs (\*\<P\>\*) and offsets. 

The IxaPipeTokenizer (not the WhiteSpaceTokenizer) also provides normalization functions 
to comply with annotation in corpora such as Penn Treebank for English and 
Ancora Corpus for Spanish. Most of the normalization rules have been adapted from 
the PTBTokenizer of Stanford CoreNLP version 3.2.0, and many changes have been
added to deal with other normalization requirements, such as those of the Ancora corpus. 
Specifically, apart from English Penn Treebank-compliant tokenization, 
**the IxaPipeTokenizer provides**:
  
  + **multilingual treatment of apostrophes** for Catalan, French and Italian styles 
    (l' aquila, c' est, etc.) possibly applying to other languages with the same 
    rules for splitting apostrophes. 
  + **multilingual support for non-breaking prefixes**, adding language-specific 
    non-breaking exceptions for Dutch, German, French, Italian and Spanish.
  + **Ancora normalization** in Spanish
  + **paragraph tokenization** to provide paragraph information
   
By default, the tokenizer does PTB3 normalization style except brackets and forward 
slashes (value "default" of ixa-pipe-tok -normalization parameter as described below). 
To change these options, the ixa-pipe-tok CLI currently provides four options, accessible via the 
*normalization* parameter. 
 
  + **sptb3**: Strict Penn Treebank normalization. Performs all normalizations listed below 
    except tokenizeNLs. 
  + **ptb3**: Activates all SPTB3 normalizations except: 
    + Acronym followed by a boundaryToken, the boundaryToken in this option
      is duplicated: "S.A." -> "S.A. .", whereas sptb3 does "S.A ." (only exception
      in sptb3 is "U.S." for which the last dot is duplicated. 
    + This option returns fractions such as "2 3/4" as a Token object, 
      but sptb3 separate them into two Token objects. 
  + **default**: ptb3 minus (all types of) brackets and escapeForwardSlash normalizations.
  + **ancora**: Ancora corpus based normalization. Like default, except that every 
    quote is normalized into ascii quotes. 
 
The **normalizations** (most of the rules are based on Stanford CoreNLP 3.2.0 tokenizer) 
performed by the four options above **are** (in the order in which
they appear in the IxaPipeLexer specification):
 
  + tokenizeParagraphs: creates Paragraph Tokens when more than newlines are found.
    Paragraphs are denoted by "\*\<P\>\*"
  + tokenizeNLs: create Token objects with newline characters
  + escapeForwardSlash: escape / and * 
  + normalizeBrackets: Normalize ( and ) into -LRB- and -RRB- respectively
  + normalizeOtherBrackets: Normalize {} and[] into -LCB-, -LRB- and -RCB-, -RRB-
  + latexQuotes: Normalize to \`\`, \`, '', '' for every quote (discouraged by Unicode).
  + unicodeQuotes: Normalize quotes to the range U+2018-U+201D,
    following Unicode recommendation. 
  + asciiQuotes: Normalize quote characters to ascii '' and "". The quotes preference 
    default order is latex -> Unicode -> ascii
  + sptb3Normalize: normalize fractions and Acronyms as described by the sptb3 option above.
  + ptb3Dashes: Normalize various dash characters into "--", 
  + normalizeAmpersand: Normalize the XML &amp;amp; into an ampersand
  + normalizeSpace: Turn every spaces in tokens (phone numbers, fractions
    get turned into non-breaking spaces (U+00A0).
  + normalizeFractions: Normalize fraction characters to forms like "1/2"
  + normalizeCurrency: Currency mappings into $, #, or "cents", reflecting
    the fact that nothing else appears in the PTB3 WSJ (not Euro).
  + ptb3Ldots: Normalize ellipses into ...
  + unicodeLdots: Normalize dot and optional space sequences into the Unicode 
    ellipsis character (U+2026). Dots order of application is ptb3Ldots -> UnicodeLdots.

## USING ixa-pipe-tok

ixa-pipe-tok provides 2 basic functionalities:

1. **tok**: reads a plain text or a NAF document containing a *raw* element and outputs
   tokens by sentences.
2. **eval**: functionalities to help evaluating a tokenized text with a given test set.

Each of these functionalities are accessible by adding (tok|eval) as a
subcommand to ixa-pipe-tok-$version.jar. Please read below and check the -help
parameter: 

````shell
java -jar target/ixa-pipe-tok-$version.jar (tok|eval) -help
````

### Tokenizing with ixa-pipe-tok

If you are in hurry, just execute: 

````shell
cat file.txt | java -jar $PATH/target/ixa-pipe-tok-$version.jar tok -l $lang
````

If you want to know more, please follow reading.

ixa-pipe-tok reads NAF documents (with *raw* element) or plain text files 
via standard input and outputs NAF through standard output. The NAF format specification is here:

(http://wordpress.let.vupr.nl/naf/)

There are several options to tokenize with ixa-pipe-tok: 

  + **lang**: choose language to create the lang attribute in KAF header
  + **tokenizer**: choose between the IxaPipeTokenizer and WhiteSpaceTokenizer
  + **normalize**: choose normalization method (see @link IxaPipeTokenizer)
  + **nokaf**: do not output NAF format.
  + **outputFormat**: if --nokaf is used, choose between oneline or conll format output.
    + If -o conll is chosen, it is possible to choose whether to print
      offset information (--offsets) or not. 
  + **paragraphs**: do not print paragraph markers, e.g., \*\<P\>\*;
  + **notok**: take already tokenized text as input and create a KAFDocument 
  + **inputkaf**: take a NAF document as input instead of plain text file.
  + **kafversion**: specify the NAF version as parameter

**Example**: 

````shell
cat file.txt java -jar $PATH/target/ixa-pipe-tok-$version.jar tok -l $lang
````

### Evaluation

The eval subcommand provides the following options:

  + **goldSet**: evaluate a tokenizer with respect to a tokenized gold standard. The
    input gold standard format must be *oneline* (tokenized text with one
    sentence per line) format. 
  + **tokenizer**: choose between the IxaPipeTokenizer and WhiteSpaceTokenizer
  + **normalize**: choose normalization method (see @link IxaPipeTokenizer)

**Example**:

````shell
java -jar target/ixa.pipe.tok-$version.jar eval --goldSet gold.tok
````

## JAVADOC

It is possible to generate the javadoc of the module by executing:

````shell
cd ixa-pipe-tok/
mvn javadoc:jar
````

Which will create a jar file core/target/ixa-pipe-tok-$version-javadoc.jar

## Module contents

The contents of the module are the following:

    + formatter.xml           Apache OpenNLP code formatter for Eclipse SDK
    + pom.xml                 maven pom file which deals with everything related to compilation and execution of the module
    + src/                    java source code of the module and required resources
    + Furthermore, the installation process, as described in the README.md, will generate another directory:
    target/                 it contains binary executable and other directories


## INSTALLATION

Installing the ixa-pipe-tok requires the following steps:

If you already have installed in your machine the Java 1.7+ and MAVEN 3, please go to step 3
directly. Otherwise, follow these steps:

### 1. Install JDK 1.7

If you do not install JDK 1.7 in a default location, you will probably need to configure the PATH in .bashrc or .bash_profile:

````shell
export JAVA_HOME=/yourpath/local/java7
export PATH=${JAVA_HOME}/bin:${PATH}
````

If you use tcsh you will need to specify it in your .login as follows:

````shell
setenv JAVA_HOME /usr/java/java17
setenv PATH ${JAVA_HOME}/bin:${PATH}
````

If you re-login into your shell and run the command

````shell
java -version
````

You should now see that your JDK is 1.7

### 2. Install MAVEN 3

Download MAVEN 3 from

````shell
wget http://apache.rediris.es/maven/maven-3/3.0.5/binaries/apache-maven-3.0.5-bin.tar.gz
````

Now you need to configure the PATH. For Bash Shell:

````shell
export MAVEN_HOME=/home/ragerri/local/apache-maven-3.0.5
export PATH=${MAVEN_HOME}/bin:${PATH}
````

For tcsh shell:

````shell
setenv MAVEN3_HOME ~/local/apache-maven-3.0.5
setenv PATH ${MAVEN3}/bin:{PATH}
````

If you re-login into your shell and run the command

````shell
mvn -version
````

You should see reference to the MAVEN version you have just installed plus the JDK 7 that is using.

### 3. Get module source code

If you must get the module source code from here do this:

````shell
git clone https://github.com/ixa-ehu/ixa-pipe-tok
````

### 4. Compile

````shell
cd ixa-pipe-tok
mvn clean package
````

This step will create a directory called target/ which contains various directories and files.
Most importantly, there you will find the module executable:

ixa-pipe-tok-$version.jar

This executable contains every dependency the module needs, so it is completely portable as long
as you have a JVM 1.7 installed.

To install the module in the local maven repository, usually located in ~/.m2/, execute:

````shell
mvn clean install
````

## Contact information

````shell
Rodrigo Agerri
IXA NLP Group
University of the Basque Country (UPV/EHU)
E-20018 Donostia-San Sebastián
rodrigo.agerri@ehu.es
````

