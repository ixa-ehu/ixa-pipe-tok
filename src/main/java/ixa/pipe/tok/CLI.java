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

package ixa.pipe.tok;

import ixa.kaflib.KAFDocument;
import ixa.pipe.tok.eval.TokenizerEvaluator;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.io.StringReader;

import net.sourceforge.argparse4j.ArgumentParsers;
import net.sourceforge.argparse4j.impl.Arguments;
import net.sourceforge.argparse4j.inf.ArgumentParser;
import net.sourceforge.argparse4j.inf.ArgumentParserException;
import net.sourceforge.argparse4j.inf.Namespace;

import org.apache.commons.io.FileUtils;

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
 * it. This options uses the WhiteSpace tokenizer. 
 * <li>inputkaf: take a KAF/NAF Document as input instead of plain text file.
 * <li>kafversion: specify the KAF version as parameter.
 * <li>eval: input reference corpus to evaluate a tokenizer. 
 * </ol>
 * 
 * 
 * @author ragerri
 * @version 2014-01-31
 */

public class CLI {

  /**
   * BufferedReader (from standard input) and BufferedWriter are opened. The
   * module takes plain text or KAF from standard input and produces tokenized
   * text by sentences. The tokens are then placed into the <wf> elements of KAF
   * document. The KAF document is passed via standard output.
   * 
   * @param args
   * @throws IOException
   */

  public static void main(String[] args) throws IOException {

    Namespace parsedArguments = null;

    // create Argument Parser
    ArgumentParser parser = ArgumentParsers
        .newArgumentParser("ixa-pipe-tok-1.5.jar")
        .description(
            "ixa-pipe-tok-1.5 is a multilingual Tokenizer developed by IXA NLP Group.\n");

    // specify language (for language dependent treatment of apostrophes)
    parser
        .addArgument("-l", "--lang")
        .choices("de", "en", "es", "fr", "it", "nl")
        .required(true)
        .help(
            "It is REQUIRED to choose a language to perform annotation with ixa-pipe-tok.\n");

    parser
        .addArgument("-t", "--tokenizer")
        .choices("ixa", "white")
        .required(false)
        .setDefault("ixa")
        .help(
            "Choose between using the IXA pipe tokenizer (default) or a WhiteSpace tokenizer.\n");

    parser
        .addArgument("-n", "--normalize")
        .choices("ancora", "default", "ptb3", "sptb3")
        .required(false)
        .setDefault("default")
        .help(
            "Set normalization method; (s)ptb3 and ancora comply with "
                + "Penn Treebank and Ancora normalizations respectively; the default option does not escape "
                + "brackets or forward slashes. See README for more details.\n");

    parser
        .addArgument("-p", "--paragraphs")
        .choices("yes", "no")
        .setDefault("yes")
        .required(false)
        .help(
            "Choose to print paragraph characters in CoNLL or oneline formats.\n");

    parser.addArgument("--nokaf").action(Arguments.storeFalse())
        .help("Do not print tokens in KAF format, but plain text.\n");

    parser
        .addArgument("-o", "--outputFormat")
        .choices("conll", "oneline")
        .setDefault("oneline")
        .required(false)
        .help(
            "Choose between conll format (one token per line) or running tokenized text.\n");

    parser
        .addArgument("--offsets")
        .action(Arguments.storeFalse())
        .help(
            "Do not print offset and lenght information of tokens in CoNLL format.\n");

    // input tokenized and segmented text
    parser.addArgument("--notok").action(Arguments.storeTrue())
        .help("Build a KAF document with already tokenized text.\n");

    // specify whether input if a KAF/NAF file
    parser
        .addArgument("--inputkaf")
        .action(Arguments.storeTrue())
        .help(
            "Use this option if input is a KAF/NAF document with <raw> layer.\n");

    // specify KAF version
    parser.addArgument("--kafversion").setDefault("v1.opener")
        .help("Set kaf document version.\n");

    parser.addArgument("-e", "--eval").help(
        "Input reference file to evaluate the tokenizer.\n");

    /*
     * Parse the command line arguments
     */

    // catch errors and print help
    try {
      parsedArguments = parser.parseArgs(args);
      //System.err.println(parser.parseArgs(args));
    } catch (ArgumentParserException e) {
      parser.handleError(e);
      System.out
          .println("Run java -jar ixa-pipe-tok/target/ixa-pipe-tok-$version.jar -help for details.\n");
      System.exit(1);
    }

    /*
     * Load language and tokenizer method parameters
     */
    String tokenizerType = parsedArguments.getString("tokenizer");
    String outputFormat = parsedArguments.getString("outputFormat");
    String normalize = parsedArguments.getString("normalize");
    String paras = parsedArguments.getString("paragraphs");
    String lang = parsedArguments.getString("lang");
    String kafVersion = parsedArguments.getString("kafversion");
    Boolean inputKafRaw = parsedArguments.getBoolean("inputkaf");

    BufferedReader breader = null;
    BufferedWriter bwriter = null;

    KAFDocument kaf;

    try {

      if (parsedArguments.getString("eval") != null) {
        // tokenize standard input text
        breader = new BufferedReader(new InputStreamReader(System.in, "UTF-8"));
        Annotate annotator = new Annotate(breader, normalize, "no",
            tokenizerType);
        // evaluate wrt to reference set
        File reference = new File(parsedArguments.getString("eval"));
        String references = FileUtils.readFileToString(reference);
        TokenizerEvaluator tokenizerEvaluator = annotator
            .evaluateTokenizer(references);
        System.out.println("Tokenizer Evaluator: ");
        System.out.println(tokenizerEvaluator.getFMeasure());
      }

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

        kaf.addLinguisticProcessor("text", "ixa-pipe-tok-" + lang, "1.5");
        if (parsedArguments.getBoolean("notok")) {
          Annotate annotator = new Annotate(breader, normalize, paras, "white");
          bwriter.write(annotator.tokensToKAF(kaf));
        } else {
          Annotate annotator = new Annotate(breader, normalize, paras, tokenizerType);
          bwriter.write(annotator.tokensToKAF(kaf));
        }
      }// kaf options end here

      else {
        Annotate annotator = new Annotate(breader, normalize, paras,
            tokenizerType);
        if (outputFormat.equalsIgnoreCase("conll")) {
          if (parsedArguments.getBoolean("offsets")) {
            bwriter.write(annotator.tokensToCoNLL());
          }// noOffset options end here
          else {
            bwriter.write(annotator.tokensToCoNLLOffsets());
          }
        }// conll options end here

        else {
          bwriter.write(annotator.tokensToText());
        }
      }// annotation options end here

      bwriter.close();
      breader.close();
    } catch (IOException e) {
      e.printStackTrace();
    }
  }
}
