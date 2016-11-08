
ixa-pipe-tok
============

ixa-pipe-tok is a multilingual rule-based tokenizer and sentence segmenter.
ixa-pipe-tok is part of IXA pipes, a set of multilingual NLP tools developed
by the IXA NLP Group [http://ixa2.si.ehu.es/ixa-pipes]. **Current version is 1.8.4.**

Please go to [http://ixa2.si.ehu.es/ixa-pipes] for general information about the IXA
pipes tools but also for **official releases, including source code and binary
packages for all the tools in the IXA pipes toolkit**.

This document is intended to be the **usage guide of ixa-pipe-tok**. If you really need to clone
and install this repository instead of using the releases provided in
[http://ixa2.si.ehu.es/ixa-pipes], please scroll down to the end of the document for
the [installation instructions](#installation).

**NOTICE!!**: ixa-pipe-tok is now in [Maven Central](http://search.maven.org/)
for easy access to its API.

## TABLE OF CONTENTS

1. [Overview of ixa-pipe-tok](#overview)
2. [Usage of ixa-pipe-tok](#cli-usage)
  + [Tokenization](#tokenizing)
3. [API via Maven Dependency](#api)
4. [Git installation](#installation)

## OVERVIEW

This module provides Multilingual Sentence Segmentation and Tokenization for a number of languages,
such as Basque, Dutch, German, English, French, Galician, Italian and Spanish.
**ixa-pipe-tok outputs** tokenized and segmented text in **three formats**:

  + **NAF (default)**: NAF is used to represent tokenized text but also to
    as an interchange format between other ixa pipes tools
    (http://github.com/ixa-ehu). NAF is generated using Kaflib
    (http://github.com/ixa-ehu/kaflib).
  + **Oneline**: tokenized text with one sentence per line and markers
    (\*\<P\>\*) for paragraphs, if that option is chosen.
  + **Conll**: one token per line, two newlines per sentence and markers for
    paragraphs (\*\<P\>\*) and offsets, if that option is chosen.

ixa-pipe-tok also provides normalization functions to comply with annotation in corpora such as Penn Treebank for English and Ancora Corpus for Spanish, among others.
  + **multilingual treatment of apostrophes** for Catalan, French and Italian styles
    (l' aquila, c' est, etc.) possibly applying to other languages with the same
    rules for splitting apostrophes.
  + **language-specific non-breaking exceptions** for Basque, Dutch, German, English, French, Galician, Italian and Spanish.
  + **Normalization** following several corpora conventions.
  + **paragraph tokenization**.

## CLI-USAGE

ixa-pipe-tok provides 3 basic functionalities:

1. **tok**: reads a plain text or a NAF document containing a *raw* element and outputs
   tokens by sentences.
2. **server**: starts a TCP service loading the model and required resources.
3. **client**: sends a NAF document to a running TCP server.

Each of these functionalities are accessible by adding (tok|server|client) as a
subcommand to ixa-pipe-tok-1.8.4.jar. Please read below and check the -help
parameter. For example:

````shell
java -jar target/ixa-pipe-tok-1.8.5-exec.jar tok -help
````

### Tokenizing

If you are in hurry, [Download](http://ixa2.si.ehu.es/ixa-pipes/models/guardian.txt) or create a plain text file and use it like this:

````shell
cat guardian.txt | java -jar target/ixa-pipe-tok-1.8.5-exec.jar tok -l en
````

If you want to know more, please follow reading.

ixa-pipe-tok reads NAF documents (with *raw* element) or plain text files
via standard input and outputs NAF through standard output. The NAF format specification is here:

(http://wordpress.let.vupr.nl/naf/)

There are several options to tokenize with ixa-pipe-tok:

  + **lang**: choose language to create the lang attribute in KAF header.
  + **normalize**: choose normalization method (see Normalizer class).
  + **nokaf**: do not output NAF format.
  + **hardParagraph**: do not detect spurious paragraphs.
  + **outputFormat**: if --nokaf is used, choose between oneline or conll format output.
    + If -o conll is chosen, it is possible to choose whether to print
      offset information (--offsets) or not.
  + **notok**: take an already tokenized text as input and create a KAFDocument
  + **inputkaf**: take a NAF document as input instead of plain text file.
  + **kafversion**: specify the NAF version as parameter

**Example**:

````shell
cat guardian.txt | java -jar target/ixa-pipe-tok-1.8.5-exec.jar tok -l en
````

## API

The easiest way to use ixa-pipe-tok programatically is via Apache Maven. Add
this dependency to your pom.xml:

````shell
<dependency>
    <groupId>eus.ixa</groupId>
    <artifactId>ixa-pipe-tok</artifactId>
    <version>1.8.3</version>
</dependency>
````

## JAVADOC

The javadoc of the module is located here:

````shell
ixa-pipe-tok/target/ixa-pipe-tok-$version-javadoc.jar
````

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

### 1. Install JDK 1.7 or JDK 1.8

If you do not install JDK 1.7+ in a default location, you will probably need to configure the PATH in .bashrc or .bash_profile:

````shell
export JAVA_HOME=$pwd/java8
export PATH=${JAVA_HOME}/bin:${PATH}
````
Replacing $pwd with the full path given by typing the **pwd** inside the java directory.

If you use tcsh you will need to specify it in your .login as follows:

````shell
setenv JAVA_HOME $pwd/java8
setenv PATH ${JAVA_HOME}/bin:${PATH}
````

If you re-login into your shell and run the command

````shell
java -version
````

You should now see that your JDK is 1.7+

### 2. Install MAVEN 3

Download MAVEN 3 from

````shell
wget http://apache.rediris.es/maven/maven-3/3.0.5/binaries/apache-maven-3.0.5-bin.tar.gz
````

Now you need to configure the PATH. For Bash Shell:

````shell
export MAVEN_HOME=$pwd/apache-maven-3.0.5
export PATH=${MAVEN_HOME}/bin:${PATH}
````
Replacing $pwd with the full path given by typing the **pwd** inside the apache maven directory.

For tcsh shell:

````shell
setenv MAVEN3_HOME $pwd/apache-maven-3.0.5
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

ixa-pipe-tok-1.8.4-exec.jar

This executable contains every dependency the module needs, so it is completely portable as long
as you have a JVM 1.7 or newer installed.

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
rodrigo.agerri@ehu.eus
````
