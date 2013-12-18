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

/**
 * This class provides the annotation functions to output 
 * the tokenized text into: 
 * <ol>
 *   <li> A list of <WF> elements inside a KAF document (DEFAULT)
 *   <li> As running tokenized and segemented text
 *   <li> CoNLL format, namely, one token per line and two newlines for each 
 *        sentence. 
 *   <li> It also provides a tokenizedTextToKAF method which takes already
 *        tokenized text as input and formats it into KAF WF elements. 
 * </ol> 
 * 
 * All these four options are configurable by using the --nokaf boolean
 * parameter and the -outputFormat parameter of the CLI. 
 * 
 * @author ragerri
 * @version 2013-18-12
 *
 */
public class Annotate {

  private JFlexLexerTokenizer<Token> tokenizer;
  private Segmenter segmenter;

  // counters
  int noSents = 0;
  int offsetCounter = 0;
  int current_index = 0;
  int previous_index = 0;

  /**
   * Constructs an annotator taking into account the normalization options
   * to be applied. 
   * 
   * @param breader
   * @param tokenFactory
   * @param normalize
   */
  public Annotate(BufferedReader breader, TokenFactory tokenFactory,
      String normalize) {
    tokenizer = new JFlexLexerTokenizer<Token>(breader, tokenFactory, normalize);
    segmenter = new Segmenter();
  }

  /**
   * Constructs an annotator with no options. This is to use the 
   * tokenizedTextToKAF method 
   * 
   * @param breader
   */
  public Annotate(BufferedReader breader) {

  }

  /**
   * Tokenize, segment and creates the WF elements into 
   * a KAF document
   * 
   * @param kaf
   * @return KAFDocument kaf containing WF with tokens
   */
  public String tokensToKAF(KAFDocument kaf) {

    List<Token> tokens = tokenizer.tokenize();
    List<List<Token>> sentences = segmenter.segment(tokens);
    for (List<Token> sentence : sentences) {

      // initiate sentence counter
      noSents = noSents + 1;
      for (Token token : sentence) {
        WF wf = kaf.newWF(token.value(), token.startOffset());
        wf.setSent(noSents);
      }
    }
    return kaf.toString();
  }

  /**
   * Tokenizes and segments input text. Outputs tokenized text 
   * in conll format: one token per sentence and two newlines to 
   * divide sentences. 
   * 
   * @return String tokenized text
   */
  public String tokensToCoNLL() {
    StringBuilder sb = new StringBuilder();
    List<Token> tokens = tokenizer.tokenize();
    List<List<Token>> sentences = segmenter.segment(tokens);

    for (List<Token> sentence : sentences) {
      for (Token token : sentence) {
        sb.append(token.value()).append(" ").append(token.startOffset())
            .append(" ").append(token.tokenLength()).append("\n");
      }
      sb.append("\n");
    }
    return sb.toString();
  }

  /**
   * Tokenize and Segment input text. Outputs tokens in running text 
   * format one sentence per line. 
   * 
   * @return String tokenized text
   */
  public String tokensToText() {
   
    List<Token> tokens = tokenizer.tokenize();
    List<List<Token>> sentences = segmenter.segment(tokens);
    StringBuilder sb = new StringBuilder();

    for (List<Token> sentence : sentences) {

      for (Token token : sentence) {
        sb.append(token.value()).append(" ");
      }
      sb.append("\n");
    }
    return sb.toString();
  }

  /**
   * Super-horrible method to create sentences from tokenized text. 
   * Used in the tokenizedTextToKAF to create a KAFDocument with WF 
   * elements out of already tokenized text. 
   * 
   * @param breader
   * @return String text to be tokenized
   * @throws IOException
   */
  private String buildText(BufferedReader breader) throws IOException {

    StringBuilder sb = new StringBuilder();
    String text;
    String line;
    while ((line = breader.readLine()) != null) {
      sb.append(line).append("<JA>");
    }
    text = sb.toString();
    text = text.replaceAll("(<JA><JA>)+", "<P>");
    text = text.replaceAll("<JA>", " ");
    text = text.replaceAll("\\s+", " ");
    text = text.trim();
    return text;
  }

  /**
   * Takes already tokenized text as input and creates a KAFDocument
   * with WF holding the tokens. 
   * 
   * @param breader
   * @param kaf
   * @return String holding a KAFDocument with WF elements
   * @throws IOException
   */
  public String tokenizedTextToKAF(BufferedReader breader, KAFDocument kaf)
      throws IOException {

    String text = buildText(breader);
    // this creates the actual sentences to be passed to the sentence detector
    String[] sentences = text.split("<P>");

    for (String sent : sentences) {
      // clean extra spaces
      sent = sent.trim();
      sent = sent.replaceAll("\\s+", " ");

      // "tokenize" 
      String[] tokens = sent.split(" ");
      // get sentence counter
      noSents = noSents + 1;

      for (int i = 0; i < tokens.length; i++) {
        // get offsets; offsets here will not be the original document
        current_index = sent.indexOf(tokens[i], previous_index);
        int offset = offsetCounter + current_index;
        WF wf = kaf.newWF(tokens[i], offset);
        wf.setSent(noSents);
        previous_index = current_index + tokens[i].length();
      }
      offsetCounter += sent.length();
    }
    return kaf.toString();
  }
}
