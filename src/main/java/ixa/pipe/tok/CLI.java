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

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;

import net.sourceforge.argparse4j.ArgumentParsers;
import net.sourceforge.argparse4j.inf.ArgumentParser;
import net.sourceforge.argparse4j.inf.ArgumentParserException;
import net.sourceforge.argparse4j.inf.Namespace;

import org.jdom2.output.Format;
import org.jdom2.output.XMLOutputter;


/**
 * IXA-OpenNLP tokenization using Apache OpenNLP.
 * 
 * @author ragerri
 * @version 1.0
 * 
 */

public class CLI {

  /**
   * BufferedReader (from standard input) and BufferedWriter are opened. The module 
   * takes plain text from standard input and produces tokenized text by sentences. The 
   * tokens are then placed into the <wf> elements of KAF document. The KAF document 
   * is passed via standard output.
   * 
   * @param args
   * @throws IOException
   */

  public static void main(String[] args) throws IOException {
    
    Namespace parsedArguments = null;

    // create Argument Parser
    ArgumentParser parser = ArgumentParsers
        .newArgumentParser("ixa-opennlp-tok-1.0.jar")
        .description(
            "ixa-opennlp-tok-1.0 is a multilingual Tokenizer module developed by IXA NLP Group based on Apache OpenNLP.\n");

    // specify language
    parser
        .addArgument("-l", "--lang")
        .choices("en", "es")
        .required(true)
        .help(
            "It is REQUIRED to choose a language to perform annotation with IXA-OpenNLP");
    // parser.addArgument("-f","--format").choices("kaf","plain").setDefault("kaf").help("output annotation in plain native "
    // +
    // "Apache OpenNLP format or in KAF format. The default is KAF");

  
    /*
     * Parse the command line arguments
     */

    // catch errors and print help
    try {
      parsedArguments = parser.parseArgs(args);
    } catch (ArgumentParserException e) {
      parser.handleError(e);
      System.out
          .println("Run java -jar target/ixa-opennlp-tok-1.0.jar -help for details");
      System.exit(1);
    }

    /*
     * Load language and dictionary parameters and construct annotators, read
     * and write kaf
     */

      String lang = parsedArguments.getString("lang");
	  Annotate annotator = new Annotate(lang);
	  BufferedReader breader = null;
	  BufferedWriter bwriter = null;
	  KAF kaf = new KAF(lang);
	  try {
	  breader = new BufferedReader(new InputStreamReader(System.in,"UTF-8"));
      bwriter = new BufferedWriter(new OutputStreamWriter(System.out,"UTF-8"));
      String line;
      while ((line = breader.readLine()) != null) {
    	annotator.annotateTokensToKAF(line, kaf);
      }
      // add kaf header
      kaf.addlps("tokens", "ixa-opennlp-tok-"+ lang, kaf.getTimestamp(), "1.0");
      XMLOutputter xout = new XMLOutputter(Format.getPrettyFormat());
      xout.output(kaf.createKAFDoc(), bwriter);
      bwriter.close();
   }
	  catch (IOException e){ 
		  e.printStackTrace();
	  }

  }
}
