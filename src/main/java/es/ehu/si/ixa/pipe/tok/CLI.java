/* 
 *Copyright 2014 Rodrigo Agerri

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

package es.ehu.si.ixa.pipe.tok;

import ixa.kaflib.KAFDocument;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.io.StringReader;

import net.sourceforge.argparse4j.ArgumentParsers;
import net.sourceforge.argparse4j.impl.Arguments;
import net.sourceforge.argparse4j.inf.ArgumentParser;
import net.sourceforge.argparse4j.inf.ArgumentParserException;
import net.sourceforge.argparse4j.inf.Namespace;
import net.sourceforge.argparse4j.inf.Subparser;
import net.sourceforge.argparse4j.inf.Subparsers;

import org.apache.commons.io.FileUtils;
import org.jdom2.JDOMException;

import es.ehu.si.ixa.pipe.tok.eval.TokenizerEvaluator;

/**
 * ixa-pipe-tok provides several configuration parameters:
 * 
 * <ol>
 * <li>lang: choose language to create the lang attribute in KAF header.
 * <li>tokenizer: choose between default tokenizer and WhiteSpace tokenizer.
 * <li>normalize: choose normalization method (see @link IxaPipeTokenizer).
 * <li>paragraphs: choose to print paragraph symbols.
 * <li>nokaf: do not output KAF/NAF document.
 * <li>outputFormat: if --nokaf is used, choose between oneline or conll format
 * output.
 * <li>notok: take already tokenized text as input and create a KAFDocument with
 * it.
 * <li>inputkaf: take a KAF/NAF Document as input instead of plain text file.
 * <li>kafversion: specify the KAF version as parameter.
 * <li>eval: input reference corpus to evaluate a tokenizer.
 * </ol>
 * 
 * 
 * @author ragerri
 * @version 2014-04-24
 */

public class CLI {

  /**
   * Get dynamically the version of ixa-pipe-tok by looking at the MANIFEST
   * file.
   */
  private final String version = CLI.class.getPackage()
      .getImplementationVersion();
  Namespace parsedArguments = null;

  // create Argument Parser
  ArgumentParser argParser = ArgumentParsers.newArgumentParser(
      "ixa-pipe-tok-" + version + ".jar").description(
      "ixa-pipe-tok-" + version
          + " is a multilingual tokenizer developed by the IXA NLP Group.\n");
  /**
   * Sub parser instance.
   */
  private Subparsers subParsers = argParser.addSubparsers().help(
      "sub-command help");
  /**
   * The parser that manages the tagging sub-command.
   */
  private Subparser annotateParser;
  /**
   * The parser that manages the evaluation sub-command.
   */
  private Subparser evalParser;

  public CLI() {
    annotateParser = subParsers.addParser("tok").help("Tagging CLI");
    loadAnnotateParameters();
    evalParser = subParsers.addParser("eval").help("Evaluation CLI");
    loadEvalParameters();
  }

  public static void main(String[] args) throws IOException, JDOMException {

    CLI cmdLine = new CLI();
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
   */
  public final void parseCLI(final String[] args) throws IOException, JDOMException {
    try {
      parsedArguments = argParser.parseArgs(args);
      System.err.println("CLI options: " + parsedArguments);
      if (args[0].equals("tok")) {
        annotate(System.in, System.out);
      } else if (args[0].equals("eval")) {
        eval();
      }
    } catch (ArgumentParserException e) {
      argParser.handleError(e);
      System.out.println("Run java -jar target/ixa-pipe-tok-" + version
          + ".jar (tok|eval) -help for details");
      System.exit(1);
    }
  }

  public final void annotate(final InputStream inputStream,
      final OutputStream outputStream) throws IOException, JDOMException {
    String tokenizerType = parsedArguments.getString("tokenizer");
    String outputFormat = parsedArguments.getString("outputFormat");
    String normalize = parsedArguments.getString("normalize");
    String paras = parsedArguments.getString("paragraphs");
    String lang = parsedArguments.getString("lang");
    String kafVersion = parsedArguments.getString("kafversion");
    Boolean inputKafRaw = parsedArguments.getBoolean("inputkaf");
    Boolean noTok = parsedArguments.getBoolean("notok");
    BufferedReader breader = null;
    BufferedWriter bwriter = null;

    KAFDocument kaf;
    bwriter = new BufferedWriter(new OutputStreamWriter(System.out, "UTF-8"));

    // read KAF/NAF document to tokenize raw element
    if (inputKafRaw) {
      BufferedReader kafReader = new BufferedReader(new InputStreamReader(
          System.in, "UTF-8"));
      // read KAF from standard input
      kaf = KAFDocument.createFromStream(kafReader);
      String text = kaf.getRawText();
      StringReader stringReader = new StringReader(text);
      breader = new BufferedReader(stringReader);
    }
    // read plain text from standard input and create a new
    // KAFDocument
    else {
      kaf = new KAFDocument(lang, kafVersion);
      breader = new BufferedReader(new InputStreamReader(System.in, "UTF-8"));
    }
    // tokenize in kaf
    if (parsedArguments.getBoolean("nokaf")) {

      KAFDocument.LinguisticProcessor newLp = kaf.addLinguisticProcessor("text", "ixa-pipe-tok-" + lang, version);
      newLp.setBeginTimestamp();
        Annotate annotator = new Annotate(breader, normalize, paras,
            tokenizerType);
        if (noTok) {
          annotator.tokensToKAF(breader, kaf);
        }
        else {
          annotator.tokenizedToKAF(kaf);
        }
        
      newLp.setEndTimestamp();
      bwriter.write(kaf.toString());
    }// kaf options end here

    else {
      Annotate annotator = new Annotate(breader, normalize, paras,
          tokenizerType);
      if (outputFormat.equalsIgnoreCase("conll")) {
        if (parsedArguments.getBoolean("offsets")) {
          bwriter.write(annotator.tokenizeToCoNLL());
        }// noOffset options end here
        else {
          bwriter.write(annotator.tokenizeToCoNLLOffsets());
        }
      }// conll options end here

      else {
        bwriter.write(annotator.tokenizeToText());
      }
    }// annotation options end here

    bwriter.close();
    breader.close();
  }

  private void loadAnnotateParameters() {
    // specify language (for language dependent treatment of apostrophes)
    annotateParser
        .addArgument("-l", "--lang")
        .choices("de", "en", "es", "fr", "it", "nl")
        .required(true)
        .help(
            "It is REQUIRED to choose a language to perform annotation with ixa-pipe-tok.\n");
    annotateParser
        .addArgument("-t", "--tokenizer")
        .choices("ixa", "white")
        .required(false)
        .setDefault("ixa")
        .help(
            "Choose between using the IXA pipe tokenizer (default) or a WhiteSpace tokenizer.\n");
    annotateParser
        .addArgument("-n", "--normalize")
        .choices("ancora", "default", "ptb3", "sptb3")
        .required(false)
        .setDefault("default")
        .help(
            "Set normalization method; (s)ptb3 and ancora comply with "
                + "Penn Treebank and Ancora normalizations respectively; the default option does not escape "
                + "brackets or forward slashes. See README for more details.\n");
    annotateParser
        .addArgument("-p", "--paragraphs")
        .choices("yes", "no")
        .setDefault("yes")
        .required(false)
        .help(
            "Choose to print paragraph characters in CoNLL or oneline formats.\n");
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
    annotateParser.addArgument("--notok")
        .action(Arguments.storeTrue())
        .help("Build a KAF document from an already tokenized sentence per line file.\n");
    // specify KAF version
    annotateParser.addArgument("--kafversion").setDefault("v1.naf")
        .help("Set kaf document version.\n");
  }

  public final void eval() throws IOException {
    BufferedReader breader = null;
    String testset = parsedArguments.getString("goldSet");
    String normalize = parsedArguments.getString("normalize");
    String tokenizerType = parsedArguments.getString("tokenizer");
    // tokenize standard input text
    breader = new BufferedReader(new InputStreamReader(System.in, "UTF-8"));
    Annotate annotator = new Annotate(breader, normalize, "no", tokenizerType);
    // evaluate wrt to reference set
    File reference = new File(testset);
    String references = FileUtils.readFileToString(reference);
    TokenizerEvaluator tokenizerEvaluator = annotator
        .evaluateTokenizer(references);
    System.out.println("Tokenizer Evaluator: ");
    System.out.println(tokenizerEvaluator.getFMeasure());

  }

  private void loadEvalParameters() {
    
    evalParser.addArgument("-g", "--goldSet").help(
        "Input reference file to evaluate the tokenizer.\n");
    evalParser
        .addArgument("-n", "--normalize")
        .choices("ancora", "default", "ptb3", "sptb3")
        .required(false)
        .setDefault("default")
        .help(
            "Set normalization method; (s)ptb3 and ancora comply with "
                + "Penn Treebank and Ancora normalizations respectively; the default option does not escape "
                + "brackets or forward slashes. See README for more details.\n");
    evalParser
        .addArgument("-t", "--tokenizer")
        .choices("ixa", "white")
        .required(false)
        .setDefault("ixa")
        .help(
            "Choose between using the IXA pipe tokenizer (default) or a WhiteSpace tokenizer.\n");

  }
}
