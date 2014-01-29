/* 
 *Copyright 2013 Rodrigo Agerri

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
import java.io.FileReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.io.StringReader;
import java.util.List;

import org.apache.commons.io.FileUtils;

import net.sourceforge.argparse4j.ArgumentParsers;
import net.sourceforge.argparse4j.impl.Arguments;
import net.sourceforge.argparse4j.inf.ArgumentParser;
import net.sourceforge.argparse4j.inf.ArgumentParserException;
import net.sourceforge.argparse4j.inf.Namespace;

/**
 * ixa-pipe-tok provides several configuration parameters:
 * 
 * <ol>
 * <li>lang: choose language to create the lang attribute in KAF header
 * <li>normalize: choose normalization method (see @link JFlexLexerTokenizer)
 * <li>nokaf: do not output KAF/NAF document.
 * <li>outputFormat: if --nokaf is used, choose between oneline or conll format
 *     output.
 * <li>notok: take already tokenized text as input and create a KAFDocument with
 *     it.
 * <li>inputkaf: take a KAF/NAF Document as input instead of plain text file.
 * <li>kafversion: specify the KAF version as parameter.
 * <li>eval: input reference corpus and raw corpus to evaluate the tokenizer. 
 * </ol>
 * 
 * 
 * @author ragerri
 * @version 2013-18-12
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
        .newArgumentParser("ixa-pipe-tok-1.3.jar")
        .description(
            "ixa-pipe-tok-1.3 is a multilingual Tokenizer developed by IXA NLP Group.\n");

    // specify language (for language dependent treatment of apostrophes)
    parser
        .addArgument("-l", "--lang")
        .choices("de", "en", "es", "fr", "it", "nl")
        .required(true)
        .help(
            "It is REQUIRED to choose a language to perform annotation with ixa-pipe-tok.\n");

    parser
        .addArgument("-n", "--normalize")
        .choices("ancora", "default", "ptb3", "sptb3")
        .required(false)
        .setDefault("default")
        .help(
            "Set normalization method; (s)ptb3 and ancora comply with "
                + "Penn Treebank and Ancora normalizations respectively; the default option does not escape "
                + "brackets or forward slashes. See README for more details.\n");

    parser.addArgument("--nokaf").action(Arguments.storeFalse())
        .help("Do not print tokens in KAF format, but plain text.\n");

    parser
        .addArgument("-o", "--outputFormat")
        .choices("conll", "oneline")
        .setDefault("oneline")
        .required(false)
        .help(
            "Choose between conll format (one token per line) or running tokenized text.\n");
    
    parser.addArgument("--noparas").action(Arguments.storeFalse())
        .help("Do not print paragraph characters in CoNLL or oneline formats");
    
    parser.addArgument("--offsets").action(Arguments.storeFalse())
        .help("Do not print offset and lenght information of tokens in CoNLL format");

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
        .help("Set kaf document version.");
    
    parser.addArgument("-e","--eval").nargs(1).help("Input reference file to evaluate the tokenizer");

    /*
     * Parse the command line arguments
     */

    // catch errors and print help
    try {
      parsedArguments = parser.parseArgs(args);
    } catch (ArgumentParserException e) {
      parser.handleError(e);
      System.out
          .println("Run java -jar ixa-pipe-tok/target/ixa-pipe-tok-$version.jar -help for details");
      System.exit(1);
    }

    /*
     * Load language and tokenizer method parameters
     */
    String outputFormat = parsedArguments.getString("outputFormat");
    String normalize = parsedArguments.getString("normalize");
    String lang = parsedArguments.getString("lang");
    String kafVersion = parsedArguments.getString("kafversion");
    Boolean inputKafRaw = parsedArguments.getBoolean("inputkaf");

    BufferedReader breader = null;
    BufferedWriter bwriter = null;
    TokenFactory tokenFactory = new TokenFactory();
    
    KAFDocument kaf;

    // reading standard input, segment and tokenize
    try {
      
      if (parsedArguments.getList("eval").isEmpty() == false) {
        breader = new BufferedReader(new InputStreamReader(System.in, "UTF-8"));
        File reference = new File(parsedArguments.<String>getList("eval").get(0));
        List<String> references = FileUtils.readLines(reference);
        Annotate annotator = new Annotate(breader,tokenFactory,normalize);
        TokenizerEvaluator tokenizerEvaluator = annotator.evaluateTokenizer(references);
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
      else {// read plain text from standard input and create a new
            // KAFDocument
        kaf = new KAFDocument(lang, kafVersion);
        breader = new BufferedReader(new InputStreamReader(System.in, "UTF-8"));
      }
      // tokenize in kaf
      if (parsedArguments.getBoolean("nokaf")) {
        
        kaf.addLinguisticProcessor("text", "ixa-pipe-tok-" + lang, "1.3");
        if (parsedArguments.getBoolean("notok")) {
          Annotate annotator = new Annotate(breader);
          bwriter.write(annotator.tokenizedTextToKAF(breader, kaf));
        } else {
          Annotate annotator = new Annotate(breader, tokenFactory, normalize);
          bwriter.write(annotator.tokensToKAF(kaf));
        }
      }// kaf options end here

      else {
        Annotate annotator = new Annotate(breader, tokenFactory, normalize);
        if (outputFormat.equalsIgnoreCase("conll")) {
          if (parsedArguments.getBoolean("offsets")) {
            if (parsedArguments.getBoolean("noparas")) { 
              bwriter.write(annotator.tokensToCoNLL());
            }
            else { 
              String outputText = annotator.tokensToCoNLL().replaceAll("\\*\\<P\\>\\*\\s+","");
              bwriter.write(outputText);
            }
          }//noOffset options end here
          
          if (parsedArguments.getBoolean("noparas")) {
            bwriter.write(annotator.tokensToCoNLLOffsets());
          }
          else { 
            String outputText = annotator.tokensToCoNLLOffsets().replaceAll("\\*\\<P\\>\\*\\s+\\d+\\s+\\d+\n","");
            bwriter.write(outputText);
          }
        }//conll options end here
        
        else {
          if (parsedArguments.getBoolean("noparas")) { 
            bwriter.write(annotator.tokensToText());
          }
          else { 
            String outputText = annotator.tokensToText().replaceAll("\\*\\<P\\>\\*", "");
            bwriter.write(outputText);
          }
        }
      }// annotation options end here

      bwriter.close();
      breader.close();
    } catch (IOException e) {
      e.printStackTrace();
    }
  }
}
