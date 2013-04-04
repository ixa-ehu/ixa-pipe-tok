IXA-OpenNLP-tok
===============

This module provides a simple wrapper that uses Apache OpenNLP
programatically to perform Segmentation and Tokenization.
Models have been trained by the IXA NLP Group (ixa.si.ehu.es).


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

Installing the ixa-opennlp-tok requires the following steps:

If you already have installed in your machine JDK6 and MAVEN 3, please go to step 3
directly. Otherwise, follow these steps:

1. Install JDK 1.6
-------------------

If you do not install JDK 1.6 in a default location, you will probably need to configure the PATH in .bashrc or .bash_profile:

````shell
export JAVA_HOME=/yourpath/local/java6
export PATH=${JAVA_HOME}/bin:${PATH}
````

If you use tcsh you will need to specify it in your .login as follows:

````shell
setenv JAVA_HOME /usr/java/java16
setenv PATH ${JAVA_HOME}/bin:${PATH}
````

If you re-login into your shell and run the command

````shell
java -version
````

You should now see that your jdk is 1.6

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

3. Get module from bitbucket
----------------------------

````shell
hg clone ssh://hg@bitbucket.org/ragerri/ixa-opennlp-tok
````

4. Move into main directory
---------------------------

````shell
cd ixa-opennlp-tok
````

5. Copy models to resources
---------------------------

You need to copy four models to ixa-opennlp-tok/src/main/resources/ for the module to work:

1. en-sent.bin and en-token.bin for English Segmentation and Tokenization.
2. es-sent.bin and es-token.bin for Spanish Segmentation and Tokenization.

Download the models from

````shell
http://ixa2.si.ehu.es/ragerri/ixa-opennlp-models/
````

If you change the name of the models you will need to modify also the source code in Models.java

````shell
cp en-sent.bin $repo/src/main/resources/
cp es-sent.bin $repo/src/main/resources/
cp en-token.bin $repo/src/main/resources/
cp es-token.bin $repo/src/main/resources/
````


6. Install module using maven
-----------------------------

````shell
mvn clean package
````

This step will create a directory called target/ which contains various directories and files.
Most importantly, there you will find the module executable:

ixa-opennlp-tok-1.0.jar

This executable contains every dependency the module needs, so it is completely portable as long
as you have a JVM 1.6 installed.

To install the module as in the maven's user local repository, located in ~/.m2/repository, do this:

````shell
mvn clean install
````

7. USING ixa-opennlp-tok
========================

The program accepts standard input and outputs tokenized text in KAF.

To run the program execute:

````shell
cat file.txt | java -jar $PATH/target/ixa-opennlp-tok-1.0.jar -l $lang
````

GENERATING JAVADOC
==================

You can also generate the javadoc of the module by executing:

````shell
mvn javadoc:jar
````

Which will create a jar file core/target/ixa-opennlp-tok-1.0-javadoc.jar


Contact information
===================

````shell
Rodrigo Agerri
IXA NLP Group
University of the Basque Country (UPV/EHU)
E-20018 Donostia-San Sebastián
rodrigo.agerri@ehu.es
````
