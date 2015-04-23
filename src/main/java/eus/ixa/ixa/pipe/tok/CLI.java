/*
 *Copyright 2015 Rodrigo Agerri

   Licensed under the Apache License, Version 2.0 (the "License");
   you may not use this file except in compliance with the License.
   You may obtain a copy of the License at

       http://www.apache.org/licenses/LICENSE-2.0

   Unless required by applicable law or agreed to in writing, software
   distributed under the License is distributed on an "AS IS" BASIS,
   WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
   See the License for the specific language governing permissions and
   limitations under the License.
 */

package eus.ixa.ixa.pipe.tok;

import ixa.kaflib.KAFDocument;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.io.StringReader;
import java.util.Properties;

import net.sourceforge.argparse4j.ArgumentParsers;
import net.sourceforge.argparse4j.impl.Arguments;
import net.sourceforge.argparse4j.inf.ArgumentParser;
import net.sourceforge.argparse4j.inf.ArgumentParserException;
import net.sourceforge.argparse4j.inf.Namespace;
import net.sourceforge.argparse4j.inf.Subparser;
import net.sourceforge.argparse4j.inf.Subparsers;

import org.jdom2.JDOMException;

/**
 * ixa-pipe-tok provides several configuration parameters:
 * 
 * <ol>
 * <li>lang: choose language to create the lang attribute in KAF header.
 * <li>tokenizer: choose tokenizer type.
 * <li>normalize: choose normalization method.
 * <li>nokaf: do not output NAF document.
 * <li>outputFormat: if --nokaf is used, choose between oneline or conll format
 * output.
 * <li>notok: take already tokenized text as input and create a KAFDocument with
 * it.
 * <li>inputkaf: take a NAF Document as input instead of plain text file.
 * <li>kafversion: specify the NAF version as parameter.
 * <li>eval: input reference corpus to evaluate a tokenizer.
 * </ol>
 * 
 * 
 * @author ragerri
 * @version 2015-04-08
 */

public class CLI {

  /**
   * Get dynamically the version of ixa-pipe-tok by looking at the MANIFEST
   * file.
   */
  private final String version = CLI.class.getPackage()
      .getImplementationVersion();
  /**
   * Get the commit of ixa-pipe-tok by looking at the MANIFEST file.
   */
  private final String commit = CLI.class.getPackage()
      .getSpecificationVersion();
  Namespace parsedArguments = null;

  // create Argument Parser
  ArgumentParser argParser = ArgumentParsers.newArgumentParser(
      "ixa-pipe-tok-" + version + ".jar").description(
      "ixa-pipe-tok-" + version
          + " is a multilingual tokenizer developed by the IXA NLP Group.\n");
  /**
   * Sub parser instance.
   */
  private final Subparsers subParsers = argParser.addSubparsers().help(
      "sub-command help");
  /**
   * The parser that manages the tagging sub-command.
   */
  private final Subparser annotateParser;

  public CLI() {
    annotateParser = subParsers.addParser("tok").help("Tagging CLI");
    loadAnnotateParameters();
  }

  public static void main(final String[] args) throws IOException,
      JDOMException {

    final CLI cmdLine = new CLI();
    cmdLine.parseCLI(args);
  }

  /**
   * Parse the command interface parameters with the argParser.
   * 
   * @param args
   *          the arguments passed through the CLI
   * @throws IOException
   *           exception if problems with the incoming data
   * @throws JDOMException
   *           a xml exception
   */
  public final void parseCLI(final String[] args) throws IOException,
      JDOMException {
    try {
      parsedArguments = argParser.parseArgs(args);
      System.err.println("CLI options: " + parsedArguments);
      if (args[0].equals("tok")) {
        annotate(System.in, System.out);
      }
    } catch (final ArgumentParserException e) {
      argParser.handleError(e);
      System.out.println("Run java -jar target/ixa-pipe-tok-" + version
          + ".jar tok -help for details");
      System.exit(1);
    }
  }

  public final void annotate(final InputStream inputStream,
      final OutputStream outputStream) throws IOException, JDOMException {
    final String tokenizerType = parsedArguments.getString("tokenizer");
    final String outputFormat = parsedArguments.getString("outputFormat");
    final String normalize = parsedArguments.getString("normalize");
    final String lang = parsedArguments.getString("lang");
    final String kafVersion = parsedArguments.getString("kafversion");
    final Boolean inputKafRaw = parsedArguments.getBoolean("inputkaf");
    final Boolean noTok = parsedArguments.getBoolean("notok");
    final Properties properties = setAnnotateProperties(lang, tokenizerType,
        normalize);

    BufferedReader breader = null;
    final BufferedWriter bwriter = new BufferedWriter(new OutputStreamWriter(
        System.out, "UTF-8"));
    KAFDocument kaf;

    if (noTok) {
      final BufferedReader noTokReader = new BufferedReader(
          new InputStreamReader(System.in, "UTF-8"));
      kaf = new KAFDocument(lang, kafVersion);
      final KAFDocument.LinguisticProcessor newLp = kaf.addLinguisticProcessor(
          "text", "ixa-pipe-tok-notok-" + lang, version + "-" + commit);
      newLp.setBeginTimestamp();
      Annotate.tokensToKAF(noTokReader, kaf);
      newLp.setEndTimestamp();
      bwriter.write(kaf.toString());
    } else {
      if (inputKafRaw) {
        final BufferedReader kafReader = new BufferedReader(
            new InputStreamReader(System.in, "UTF-8"));
        // read KAF from standard input
        kaf = KAFDocument.createFromStream(kafReader);
        final String text = kaf.getRawText();
        final StringReader stringReader = new StringReader(text);
        breader = new BufferedReader(stringReader);
      } else {
        kaf = new KAFDocument(lang, kafVersion);
        breader = new BufferedReader(new InputStreamReader(System.in, "UTF-8"));
      } // tokenize in naf
      if (parsedArguments.getBoolean("nokaf")) {
        final KAFDocument.LinguisticProcessor newLp = kaf
            .addLinguisticProcessor("text", "ixa-pipe-tok-" + lang, version
                + "-" + commit);
        newLp.setBeginTimestamp();
        final Annotate annotator = new Annotate(breader, properties);
        annotator.tokenizeToKAF(kaf);
        newLp.setEndTimestamp();
        bwriter.write(kaf.toString());
      } else {
        final Annotate annotator = new Annotate(breader, properties);
        if (outputFormat.equalsIgnoreCase("conll")) {
          if (parsedArguments.getBoolean("offsets")) {
            bwriter.write(annotator.tokenizeToCoNLL());
          } else {
            bwriter.write(annotator.tokenizeToCoNLLOffsets());
          }
        } else {
          bwriter.write(annotator.tokenizeToText());
        }
      }
      breader.close();
    }
    bwriter.close();
  }

  private void loadAnnotateParameters() {
    // specify language (for language dependent treatment of apostrophes)
    annotateParser
        .addArgument("-l", "--lang")
        .choices("de", "en", "es", "eu", "fr", "gl", "it", "nl")
        .required(true)
        .help(
            "It is REQUIRED to choose a language to perform annotation with ixa-pipe-tok.\n");
    annotateParser.addArgument("-t", "--tokenizer").choices("rule")
        .required(false).setDefault("rule")
        .help("Choose the tokenizer type.\n");
    annotateParser
        .addArgument("-n", "--normalize")
        .choices("alpino", "ancora", "ctag", "default", "ptb", "tiger",
            "tutpenn")
        .required(false)
        .setDefault("default")
        .help(
            "Set normalization method according to corpus; the default option does not escape "
                + "brackets or forward slashes. See README for more details.\n");
    annotateParser.addArgument("--nokaf").action(Arguments.storeFalse())
        .help("Do not print tokens in KAF format, but plain text.\n");
    annotateParser
        .addArgument("-o", "--outputFormat")
        .choices("conll", "oneline")
        .setDefault("oneline")
        .required(false)
        .help(
            "Choose between conll format (one token per line) or running tokenized text.\n");
    annotateParser
        .addArgument("--offsets")
        .action(Arguments.storeFalse())
        .help(
            "Do not print offset and lenght information of tokens in CoNLL format.\n");
    // specify whether input if a KAF/NAF file
    annotateParser
        .addArgument("--inputkaf")
        .action(Arguments.storeTrue())
        .help(
            "Use this option if input is a KAF/NAF document with <raw> layer.\n");
    annotateParser
        .addArgument("--notok")
        .action(Arguments.storeTrue())
        .help(
            "Build a KAF document from an already tokenized sentence per line file.\n");
    // specify KAF version
    annotateParser.addArgument("--kafversion").setDefault("v1.naf")
        .help("Set kaf document version.\n");
  }

  private Properties setAnnotateProperties(final String lang,
      final String tokenizer, final String normalize) {
    final Properties annotateProperties = new Properties();
    annotateProperties.setProperty("language", lang);
    annotateProperties.setProperty("tokenizer", tokenizer);
    annotateProperties.setProperty("normalize", normalize);
    return annotateProperties;
  }

}
