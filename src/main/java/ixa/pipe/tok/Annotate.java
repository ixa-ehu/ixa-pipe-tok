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
import ixa.pipe.tok.eval.TokenizerEvaluator;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.StringReader;
import java.util.List;

/**
 * This class provides the annotation functions to output the tokenized text
 * into:
 * <ol>
 * <li>A list of <WF> elements inside a KAF document (DEFAULT)
 * <li>As running tokenized and segmented text
 * <li>CoNLL format, namely, one token per line and two newlines for each
 * sentence.
 * <li>Evaluate the tokenizer against a reference text.
 * </ol>
 * 
 * All these options are configurable by using the --nokaf boolean parameter and
 * the -outputFormat parameter of the CLI.
 * 
 * @author ragerri
 * @version 2013-01-31
 * 
 */
public class Annotate {

  private Tokenizer<Token> tokenizer;
  private Segmenter segmenter;
  private TokenFactory tokenFactory;

  // counters for paragraphs and sentences
  int noParas = 1;
  int noSents = 0;

  /**
   * Constructs an annotator taking into account the normalization options and
   * paragraph options.
   * 
   * @param breader
   * @param normalize
   * @param options
   * @param tokenizerType
   */
  public Annotate(BufferedReader breader, String normalize, String options,
      String tokenizerType) {
    this.tokenFactory = new TokenFactory();
    if (tokenizerType.equalsIgnoreCase("white")) {
      tokenizer = new WhiteSpaceTokenizer<Token>(breader, tokenFactory, options);
    } else {
      tokenizer = new IxaPipeTokenizer<Token>(breader, tokenFactory, normalize,
          options);
    }
    segmenter = new Segmenter();

  }

  /**
   * Tokenize, segment and creates the WF elements into a KAF document: wf,
   * sent, para, offset and length attributes are provided.
   * 
   * @param kaf
   * @return KAFDocument kaf containing WF with tokens
   */
  public String tokensToKAF(KAFDocument kaf) {
    //TODO post-process <P> and then lowercase word
    List<Token> tokens = tokenizer.tokenize();
    List<List<Token>> sentences = segmenter.segment(tokens);
    for (List<Token> sentence : sentences) {
      // initialize sentence counter
      noSents = noSents + 1;
      for (Token token : sentence) {
        if (token.value().equals(IxaPipeLexer.PARAGRAPH_TOKEN)) {
          noParas++;
        } else {
          WF wf = kaf.newWF(token.value(), token.startOffset());
          wf.setPara(noParas);
          wf.setSent(noSents);
        }
      }
    }
    return kaf.toString();
  }

  /**
   * Tokenizes and segments input text. Outputs tokenized text in conll format:
   * one token per sentence and two newlines to divide sentences.
   * 
   * @return String tokenized text
   */
  public String tokensToCoNLL() {
    StringBuilder sb = new StringBuilder();
    List<Token> tokens = tokenizer.tokenize();
    List<List<Token>> sentences = segmenter.segment(tokens);
    for (List<Token> sentence : sentences) {
      for (Token token : sentence) {
        sb.append(token.value().trim()).append("\n");
      }
      sb.append("\n");
    }
    return sb.toString();
  }

  /**
   * Tokenizes and segments input text. Outputs tokenized text in conll format:
   * one token per sentence and two newlines to divide sentences plus offsets
   * and lenght information about tokens.
   * 
   * @return String tokenized text
   */
  public String tokensToCoNLLOffsets() {
    StringBuilder sb = new StringBuilder();
    List<Token> tokens = tokenizer.tokenize();
    List<List<Token>> sentences = segmenter.segment(tokens);
    for (List<Token> sentence : sentences) {
      for (Token token : sentence) {
          sb.append(token.value().trim()).append(" ").append(token.startOffset())
              .append(" ").append(token.tokenLength()).append("\n");
        }
      sb.append("\n");
    }
    return sb.toString();
  }

  /**
   * Tokenize and Segment input text. Outputs tokens in running text format one
   * sentence per line.
   * 
   * @return String tokenized text
   */
  public String tokensToText() {
    List<Token> tokens = tokenizer.tokenize();
    List<List<Token>> sentences = segmenter.segment(tokens);
    StringBuilder sb = new StringBuilder();
    for (List<Token> sentence : sentences) {
      for (Token token : sentence) {
        if (token.value().equals(IxaPipeLexer.PARAGRAPH_TOKEN)) {
          sb.append(token.value()).append("\n");
        }
        else {
          sb.append(token.value().trim()).append(" ");
        }
      }
      sb.append("\n");
    }
    return sb.toString().trim();
  }

  /**
   * This function takes a reference tokenized text, performs 
   * tokenization on some input raw text and builds a 
   * @link TokenizerEvaluator to compare the reference tokenization
   * againts the predicted tokenization. 
   * 
   * This function is used in the CLI to obtain the F score of 
   * a tokenizer via the --eval parameter. 
   * 
   * @param referenceText the reference tokenized text
   * @return a Tokenizer Evaluator 
   * @throws IOException
   */
  public TokenizerEvaluator evaluateTokenizer(String referenceText)
      throws IOException {
    // tokenize input text
    List<Token> tokens = tokenizer.tokenize();
    // construct whitespace tokenizer to obtain the Token objects from reference
    // text
    
    StringReader stringReader = new StringReader(referenceText);
    BufferedReader refReader = new BufferedReader(stringReader);
    Tokenizer<Token> whiteSpacer = new WhiteSpaceTokenizer<Token>(refReader,
        tokenFactory, "no");
    // createn Token objects out from the reference text
    List<Token> references = whiteSpacer.tokenize();
    // evaluate
    TokenizerEvaluator tokenizerEvaluator = new TokenizerEvaluator();
    tokenizerEvaluator.evaluate(references, tokens);
    return tokenizerEvaluator;
  }

}
