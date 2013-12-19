IXA-pipe-tok
===============

This module provides Multilingual Sentence Segmentation and Tokenization for a number of languages, 
such as Dutch, German, English, French, Italian and Spanish. ixa-pipe-tok is part of 
IXA Pipeline ("is a pipeline"), a multilingual NLP pipeline developed by the 
IXA NLP Group (ixa.si.ehu.es). 

ixa-pipe-tok outputs tokenized and segmented text in three formats: 

    + KAF/NAF (default): KAF is used to represent tokenized text but also to
      as an interchange format between other modules in IXA pipeline
      (http://github.com/ixa-ehu)
    + Running text: tokenized text with one sentence per line and markers
      (\*<P>\*) for paragraphs. 
    + Conll: one token per line, two newlines per sentence and markers for
      paragraphs (\*<P>\*). 

ixa-pipe-tok provides several configuration parameters:

    + lang: choose language to create the lang attribute in KAF header
    + normalize: choose normalization method (see @link JFlexLexerTokenizer)
    + nokaf: do not output KAF/NAF document.
    + outputFormat: if --nokaf is used, choose between oneline or conll format output.
    + notok: take already tokenized text as input and create a KAFDocument with
    + kaf: take a KAFDocument as input instead of plain text file.
    + kafversion: specify the KAF version as parameter
  

This Tokenizer also provides normalization functions 
to comply with annotation in corpora such as Penn Treebank for English and 
Ancora Corpus for Spanish. Most of the normalization rules have been adapted from 
the PTBTokenizer of Stanford CoreNLP version 3.2.0, and many changes have been
added to deal with other normalization requirements, such as those of the Ancora corpus. 
Specifically, apart from English Penn Treebank-compliant tokenization, 
this Tokenizer provides:
  
    + multilingual treatment of apostrophes for Catalan, French and Italian styles 
      (l' aquila, c' est, etc.) possibly applying to other languages with the same 
      rules for splitting apostrophes. 
    + multilingual support for non-breaking prefixes, adding language-specific 
      non-breaking exceptions for Dutch, German, French, Italian and Spanish.
    + normalization following Ancora corpus in Spanish
    + paragraph tokenization to provide paragraph information
   
By default, the tokenizer does PTB3 normalization style except brackets and forward 
slashes (value "default" of ixa-pipe-tok -normalization parameter as described below). 
To change these options, the ixa-pipe-tok CLI currently provides four options, accessible via the 
"-normalization" parameter. 
 
    + sptb3: Strict Penn Treebank normalization. Performs all normalizations listed below 
      except tokenizeNLs. 
    + ptb3: Activates all SPTB3 normalizations except: 
        + Acronym followed by a boundaryToken, the boundaryToken in this option
         is duplicated: "S.A." -> "S.A. .", whereas sptb3 does "S.A ." (only exception
         in sptb3 is "U.S." for which the last dot is duplicated. 
        + This option returns fractions such as "2 3/4" as a Token object, 
          but sptb3 separate them into two Token objects. 
        + default: ptb3 minus (all types of) brackets and escapeForwardSlash normalizations.
        + ancora: Ancora corpus based normalization. Like default, except that every 
          quote is normalized into ascii quotes. 
 
The normalizations performed by the four options above are (in the order in which
they appear in the JFlexLexer specification):
 
     + tokenizeNLs: create Token objects with newline characters
     + escapeForwardSlash: escape / and * -> \/ \*
     + normalizeBrackets: Normalize ( and ) into -LRB- and -RRB- respectively
     + normalizeOtherBrackets: Normalize {} and[] into -LCB-, -LRB- and -RCB-, -RRB-
     + latexQuotes: Normalize to ``, `, ', '' for every quote (discouraged by Unicode).
     + unicodeQuotes: Normalize quotes to the range U+2018-U+201D,
       following Unicode recommendation. 
     + asciiQuotes: Normalize quote characters to ascii ' and ". The quotes preference 
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
     + tokenizeParagraphs: creates Paragraph Tokens when more than newlines are found.
       Paragraphs are denoted by "*<P>*"

Contents
========

The contents of the module are the following:

    + formatter.xml           Apache OpenNLP code formatter for Eclipse SDK
    + pom.xml                 maven pom file which deals with everything related to compilation and execution of the module. 
                              Use this default for using ixa-pipe-tok with KAF format
    + pom-naf.xml             maven pom file to use ixa-pipe-tok with NAF format
    + src/                    java source code of the module
    + Furthermore, the installation process, as described in the README.md, will generate another directory:
      target/                 it contains binary executable and other directories


INSTALLATION
============

Installing the ixa-pipe-tok requires the following steps:

If you already have installed in your machine JDK7 and MAVEN 3, please go to step 3
directly. Otherwise, follow these steps:

1. Install JDK 1.7
-------------------

Openjdk7 packages for your Operative System are OK. If you do not install JDK 1.7 
in a default location, you will probably need to configure the PATH in .bashrc or .bash_profile:

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

You should now see that your jdk is 1.7

2. Install MAVEN 3
------------------

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

You should see reference to the MAVEN version you have just installed plus the JDK 6 that is using.

3. Install ixa-pipe-tok
-----------------------

````shell
git clone https://github.com/ixa-ehu/ixa-pipe-tok.git
````

move into main directory:

````shell
cd ixa-pipe-tok
````
compile module to output and read KAF Documents:

````shell
mvn clean package
````

For NAF Documents, use the optional pom-naf.xml distributed: 

````shell
mvn -f pom-naf.xml clean package
````

These steps will create a directory called target/ which contains various directories and files.
Most importantly, there you will find the module executable:

ixa-pipe-tok-$version.jar

This executable contains every dependency the module needs, so it is completely portable as long
as you have a JVM 1.7 installed (Oracle or OpenJDK versions tested). 

To install the module in the maven's user local repository, located in ~/.m2/repository, do this:

````shell
mvn clean install
````

4. Using ixa-pipe-tok
---------------------

The program accepts plain text or KAF/NAF via standard input and outputs tokenized text
in three formats, as explained above in this README. For KAF and NAF
specifications, please check: 

https://github.com/opener-project/kaf/wiki/KAF-structure-overview

and 

https://github.com/ixa-ehu/naf

To run the program execute:

````shell
cat file.(txt|kaf|naf) | java -jar $PATH/target/ixa-pipe-tok-1.3.jar -l $lang
````

For a summary of options available, run:

````shell
java -jar $PATH/target/ixa-pipe-tok-1.3.jar -help
````

GENERATING JAVADOC
==================

You can also generate the javadoc of the module by executing:

````shell
mvn javadoc:jar
````

Which will create a jar file core/target/ixa-pipe-tok-1.3-javadoc.jar


Contact information
===================

````shell
Rodrigo Agerri
IXA NLP Group
University of the Basque Country (UPV/EHU)
E-20018 Donostia-San Sebastián
rodrigo.agerri@ehu.es
````

