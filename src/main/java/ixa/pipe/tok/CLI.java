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

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;

import net.sourceforge.argparse4j.ArgumentParsers;
import net.sourceforge.argparse4j.impl.Arguments;
import net.sourceforge.argparse4j.inf.ArgumentParser;
import net.sourceforge.argparse4j.inf.ArgumentParserException;
import net.sourceforge.argparse4j.inf.Namespace;

/**
 * ixa-pipe tokenization
 * 
 *  
 * @author ragerri
 * @version 1.0
 * 
 */

public class CLI {

  /**
   * BufferedReader (from standard input) and BufferedWriter are opened. The
   * module takes plain text from standard input and produces tokenized text by
   * sentences. The tokens are then placed into the <wf> elements of KAF
   * document. The KAF document is passed via standard output.
   * 
   * @param args
   * @throws IOException
   */

  public static void main(String[] args) throws IOException {
    
    Namespace parsedArguments = null;

    // create Argument Parser
    ArgumentParser parser = ArgumentParsers
        .newArgumentParser("ixa-pipe-tok-1.0.jar")
        .description(
            "ixa-pipe-tok-1.0 is a multilingual Tokenizer developed by IXA NLP Group.\n");

    // specify language (for language dependent treatment of apostrophes)
    parser
        .addArgument("-l", "--lang")
        .choices("de", "en", "es", "fr", "it", "nl")
        .required(true)
        .help(
            "It is REQUIRED to choose a language to perform annotation with ixa-pipe-tok.\n");
    
    parser.addArgument("-n","--normalize").choices("ancora", "en", "ptb3", "sptb3")
          .required(false).setDefault("en").help("Set normalization method; (s)ptb3 and ancora comply with " +
          		"Penn Treebank and Ancora normalizations respectively; the default option does not escape " +
          		"brackets or forward slashes. See README for more details.\n");
    
    parser.addArgument("--nokaf").action(Arguments.storeFalse())
          .help("Do not print tokens in KAF format, but plain text.\n");
    
    parser.addArgument("-o", "--outputFormat").choices("conll", "oneline")
            .setDefault("oneline")
            .required(false)
            .help("Choose between conll format (one token per line) or running tokenized text.\n");

    // input tokenized and segmented text 
    parser.addArgument("--notok").action(Arguments.storeTrue())
        .help("Build a KAF document with already tokenized text.\n");
    
    // specify whether input if a KAF/NAF file
    parser.addArgument("-k", "--kaf").action(Arguments.storeTrue())
        .help("Use this option if input is a KAF/NAF document with <raw> layer.\n");

    // specify KAF version
    parser.addArgument("--kafversion").setDefault("v1.opener")
        .help("Set kaf document version.");

    /*
     * Parse the command line arguments
     */

    // catch errors and print help
    try {
      parsedArguments = parser.parseArgs(args);
    } catch (ArgumentParserException e) {
      parser.handleError(e);
      System.out
          .println("Run java -jar ixa-pipe-tok/target/ixa-pipe-tok-1.0.jar -help for details");
      System.exit(1);
    }

    /*
     * Load language and tokenizer method parameters
     */
    String outputFormat = parsedArguments.getString("outputFormat");
    String normalize = parsedArguments.getString("normalize");
    String lang = parsedArguments.getString("lang");
    String kafVersion = parsedArguments.getString("kafversion");
    Boolean inputKafRaw = parsedArguments.getBoolean("kaf");

    BufferedReader breader = null;
    BufferedWriter bwriter = null;
    TokenFactory tokenFactory = new TokenFactory();
    KAFDocument kaf;
  
    // reading standard input, segment and tokenize
    try {
      breader = new BufferedReader(new InputStreamReader(System.in, "UTF-8"));
      bwriter = new BufferedWriter(new OutputStreamWriter(System.out, "UTF-8"));
      String text;
      

      //TODO if this option is used, get language from lang attribute in KAF 
      if (inputKafRaw) {
        // read KAF from standard input
        //kaf = KAFDocument.createFromStream(breader);
        //text = kaf.getRawText();
      } 
      else {
        // write kaf
        if (parsedArguments.getBoolean("nokaf")) {
        kaf = new KAFDocument(lang, "v1.opener");
        kaf.addLinguisticProcessor("text", "ixa-pipe-tok-" + lang, "1.0");
        
          if (parsedArguments.getBoolean("notok")) {
            Annotate annotator = new Annotate(breader);
            bwriter.write(annotator.tokenizedTextToKAF(breader,kaf));
          }
          else {
            Annotate annotator = new Annotate(breader,tokenFactory,normalize);
            bwriter.write(annotator.tokensToKAF(kaf)); 
          }
        }// kaf options end here
        
        else {
          Annotate annotator = new Annotate(breader,tokenFactory,normalize);
          if (outputFormat.equalsIgnoreCase("conll")) { 
            bwriter.write(annotator.tokensToCoNLL());
          }
          else { 
            bwriter.write(annotator.tokensToText());
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
