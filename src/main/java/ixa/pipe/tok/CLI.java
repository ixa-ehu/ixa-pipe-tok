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
import ixa.pipe.resources.Resources;
import ixa.pipe.seg.SegmenterMoses;
import ixa.pipe.seg.SentenceSegmenter;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.IOException;
import java.io.InputStream;
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
 * This module implements a rule-based loosely inspired by the moses tokenizer
 * (https://github.com/moses-smt/mosesdecoder)
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
            "ixa-pipe-tok-1.0 is a multilingual Tokenizer module developed by IXA NLP Group.\n");

    // specify language
    parser
        .addArgument("-l", "--lang")
        .choices("en", "es")
        .required(true)
        .help(
            "It is REQUIRED to choose a language to perform annotation with ixa-pipe-tok");
    
    // input tokenized and segmented text 
    parser.addArgument("--notok").action(Arguments.storeTrue())
        .help("Build KAF with already tokenized and segmented text");
    
    // specify whether input if a KAF/NAF file
    parser.addArgument("-k", "--kaf").action(Arguments.storeTrue())
        .help("Use this option if input is a KAF/NAF document with <raw> layer.");

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
     * Load language and tokenizer method parameters and construct annotators,
     * read and write kaf
     */

    String lang = parsedArguments.getString("lang");
    String kafVersion = parsedArguments.getString("kafversion");
    Boolean inputKafRaw = parsedArguments.getBoolean("kaf");

    TokenFactory tokenFactory = new TokenFactory();
    String options= "";
    BufferedReader breader = null;
    BufferedWriter bwriter = null;
    KAFDocument kaf;

    // choosing tokenizer and resources by language
  
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
        
        IXATokenizer<Token> tokenizer = new IXATokenizer<Token>(breader,tokenFactory,options);
        while (tokenizer.hasNext()) { 
          Token token = tokenizer.next();
          System.out.println(token.value());
        }
             
      }
    } catch (IOException e) {
      e.printStackTrace();
    }

  }
}
