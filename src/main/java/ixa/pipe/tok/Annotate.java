/*
 * Copyright 2013 Rodrigo Agerri

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
import ixa.kaflib.WF;

import java.io.BufferedReader;
import java.io.IOException;
import java.util.List;

public class Annotate {

  private IXATokenizer<Token> tokenizer;
  
  public Annotate (BufferedReader breader, TokenFactory tokenFactory, String normalize) { 
    tokenizer = new IXATokenizer<Token>(breader, tokenFactory, normalize);
  }
  
  /**
   * This function takes the original input text and cleans extra newlines and
   * spaces creating the input text for the sentence segmenter and the tokenizer
   * 
   * @param String
   *          text
   * @return String text
   */

  public String buildText(String text) {
    text = text.replaceAll("(<JA><JA>)+", "<P>");
    text = text.replaceAll("<JA>", " ");
    text = text.replaceAll("\\s+", " ");
    text = text.trim();
    return text;
  }

  /**
   * This method performs Sentence Detection and Tokenization to produce
   * tokenized text by sentences represented in KAF format.
   * 
   * For every line of text the method receives, it creates an array of
   * segmented sentences, and an array of Tokens.
   * 
   * It fills the kaf object with the word forms element <wf> corresponding to
   * each of the tokens.
   * 
   * @param String
   *          text
   * @param Segmenter
   *          sentDetector
   * @param Tokenizer
   *          toker
   * @param KAF
   *          object. This object is used to take the output data and convert it
   *          to KAF, returning an XML document in a string.
   */

  private List<Token> toker(KAFDocument kaf)
      throws IOException {

    int noSents = 0;
    List<Token> tokens = tokenizer.tokenize();
    
    for (Token token : tokens) {
      System.out.println(token.value() + " " + token.startOffset() + " " + token.tokenLength());
      WF wf = kaf.newWF(token.value(), token.startOffset());
      wf.setSent(noSents);
    }
  }
  
  /**
   * This method performs Sentence Detection and Tokenization to produce
   * tokenized text by sentences represented in KAF format.
   * 
   * For every line of text the method receives, it creates an array of
   * segmented sentences, and an array of Tokens.
   * 
   * It fills the kaf object with the word forms element <wf> corresponding to
   * each of the tokens.
   * 
   * @param String
   *          text
   * @param Segmenter
   *          sentDetector
   * @param Tokenizer
   *          toker
   * @param KAF
   *          object. This object is used to take the output data and convert it
   *          to KAF, returning an XML document in a string.
   */

  int tokSents = 0;
  int tokOffsetCounter = 0;
  int tokCurrent_index = 0;
  int tokPrevious_index = 0;
  
  public void tokenizedTextToKAF(String text, String lang, Tokenizer toker, KAFDocument kaf)
      throws IOException {

    // this creates the actual sentences to be passed to the sentence detector
    String[] sentences = text.split("<JA>");

    for (String sent : sentences) {
        // clean extra spaces
        sent = sent.trim();
        sent = sent.replaceAll("\\s+", " ");
        // System.out.println(sent);

        // "tokenize" an already tokenized sentence
        //String[] tokens = toker.tokenize(sent, lang);
        String[] tokens = sent.split(" ");
        // get sentence counter
        tokSents = tokSents + 1;
       
        for (int i = 0; i < tokens.length; i++) {
          // get offsets
          tokCurrent_index = sent.indexOf(tokens[i], tokPrevious_index);
          int offset = tokOffsetCounter + tokCurrent_index;
          WF wf = kaf.newWF(tokens[i], offset);
          wf.setSent(tokSents);
          tokPrevious_index = tokCurrent_index + tokens[i].length();
        }
        tokOffsetCounter += sent.length();
      }
      
  }
}
