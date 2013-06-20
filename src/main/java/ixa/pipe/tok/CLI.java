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
import ixa.pipe.resources.Formats;
import ixa.pipe.resources.Resources;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;


import net.sourceforge.argparse4j.ArgumentParsers;
import net.sourceforge.argparse4j.inf.ArgumentParser;
import net.sourceforge.argparse4j.inf.ArgumentParserException;
import net.sourceforge.argparse4j.inf.Namespace;


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
        .newArgumentParser("ixa-pipe-tok-1.0.jar")
        .description(
            "ixa-pipe-tok-1.0 is a multilingual Tokenizer module developed by IXA NLP Group based on Apache OpenNLP.\n");

    // specify language
    parser
        .addArgument("-l", "--lang")
        .choices("en", "es")
        .required(true)
        .help(
            "It is REQUIRED to choose a language to perform annotation with IXA-Pipeline");
    
// specify tokenization method
    
    parser.addArgument("-m","--method").choices("moses","ml").setDefault("moses").help("Tokenization method." +
    		"Choose 'moses' for a re-implementation of the Moses MT system tokenizer (this is the default) " +
    		"; 'ml' for OpenNLP trained statistical models. ");
    
      
    /*
     * Parse the command line arguments
     */

    // catch errors and print help
    try {
      parsedArguments = parser.parseArgs(args);
    } catch (ArgumentParserException e) {
      parser.handleError(e);
      System.out
          .println("Run java -jar target/ixa-pipe-tok-1.0.jar -help for details");
      System.exit(1);
    }

    /*
     * Load language and tokenizer method parameters and construct annotators, read
     * and write kaf
     */

      String lang = parsedArguments.getString("lang");
      String method = parsedArguments.getString("method");
      
      Formats formatter = new Formats();
	  Annotate annotator = new Annotate(lang);
	  BufferedReader breader = null;
	  BufferedWriter bwriter = null;
	  KAFDocument kaf = new KAFDocument(lang,"v1.opener");
	  
	  // choosing tokenizer and language 
	  
	  Resources resourceRetriever = new Resources();
	  TokTokenizer tokenizer = null;
	  
	  if (method.equalsIgnoreCase("ml")) { 
    	  InputStream tokModel = resourceRetriever.getTokModel(lang);
    	  tokenizer = new TokenizerOpenNLP(tokModel);
      }
      
      else { 
    	  tokenizer = new TokenizerMoses();
      }
	  
	  // reading standard input and tokenize
	  try {
	  breader = new BufferedReader(new InputStreamReader(System.in,"UTF-8"));
      bwriter = new BufferedWriter(new OutputStreamWriter(System.out,"UTF-8"));
            
      String line;
      while ((line = breader.readLine()) != null) {
    	line = formatter.cleanWeirdChars(line);  
    	annotator.annotateTokensToKAF(line, tokenizer, kaf);
      }
      
      // write kaf document
      kaf.addLinguisticProcessor("text","ixa-pipe-tok-"+lang,"1.0");
      bwriter.write(kaf.toString());
      bwriter.close();
      
   }
	  catch (IOException e){ 
		  e.printStackTrace();
	  }

  }
}
