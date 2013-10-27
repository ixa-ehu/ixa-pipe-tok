IXA-pipe-tok
===============

This module provides Sentence Segmentation and Tokenization for English and Spanish via a
rule-based approach originally inspired by the Moses (https://github.com/moses-smt/mosesdecoder)
tokenizer but with several additions and modifications.

ixa-pipe-tok is part of IXA-Pipeline ("is a pipeline"), a multilingual NLP pipeline developed by the IXA NLP Group (ixa.si.ehu.es).


Contents
========

The contents of the module are the following:

    + formatter.xml           Apache OpenNLP code formatter for Eclipse SDK
    + pom.xml                 maven pom file which deals with everything related to compilation and execution of the module
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
git clone git@github.com:ixa-ehu/ixa-pipe-tok.git
````

move into main directory:

````shell
cd ixa-pipe-tok
````
compile module:

````shell
mvn clean package
````

This step will create a directory called target/ which contains various directories and files.
Most importantly, there you will find the module executable:

ixa-pipe-tok-1.0.jar

This executable contains every dependency the module needs, so it is completely portable as long
as you have a JVM 1.6 installed.

To install the module as in the maven's user local repository, located in ~/.m2/repository, do this:

````shell
mvn clean install
````

4. Using ixa-pipe-tok
========================

The program accepts standard input and outputs tokenized text in KAF:

https://github.com/opener-project/kaf/wiki/KAF-structure-overview

To run the program execute:

````shell
cat file.txt | java -jar $PATH/target/ixa-pipe-tok-1.0.jar -l $lang
````

For a summary of options available, run:

````shell
java -jar $PATH/target/ixa-pipe-tok-1.0.jar -help
````

GENERATING JAVADOC
==================

You can also generate the javadoc of the module by executing:

````shell
mvn javadoc:jar
````

Which will create a jar file core/target/ixa-pipe-tok-1.0-javadoc.jar


Contact information
===================

````shell
Rodrigo Agerri
IXA NLP Group
University of the Basque Country (UPV/EHU)
E-20018 Donostia-San Sebastián
rodrigo.agerri@ehu.es
````

