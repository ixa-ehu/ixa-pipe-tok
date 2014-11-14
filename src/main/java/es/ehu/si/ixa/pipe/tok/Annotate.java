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

package es.ehu.si.ixa.pipe.tok;

import ixa.kaflib.KAFDocument;
import ixa.kaflib.WF;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.Reader;
import java.io.StringReader;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Properties;

import org.apache.commons.io.IOUtils;

import es.ehu.si.ixa.pipe.tok.eval.TokenizerEvaluator;

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
 * @version 2013-03-31
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
  public Annotate(BufferedReader breader, Properties properties) {
    this.tokenFactory = new TokenFactory();
    String tokenizerType = properties.getProperty("tokenizer");
    if (tokenizerType.equalsIgnoreCase("white")) {
      tokenizer = new WhiteSpaceTokenizer<Token>(breader, tokenFactory, properties);
    } else {
      tokenizer = new IxaPipeTokenizer<Token>(breader, tokenFactory, properties);
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
  public void tokenizedToKAF(KAFDocument kaf) {
    List<Token> tokens = tokenizer.tokenize();
    // remove paragraphs followed by lowercase words
    List<Integer> spuriousParas = getSpuriousParas(tokens);
    removeSpuriousParas(tokens,spuriousParas);
    //segment
    List<List<Token>> sentences = segmenter.segment(tokens);
    for (List<Token> sentence : sentences) {
      noSents = noSents + 1;
      for (Token token : sentence) {
        if (token.value().equals(IxaPipeLexer.PARAGRAPH_TOKEN)) {
          ++noParas;
          //TODO sentences without end markers;
          //crap rule
          while (noParas > noSents) {
            ++noSents;
          }
        } else {
          WF wf = kaf.newWF(token.value(), token.startOffset());
          wf.setPara(noParas);
          wf.setSent(noSents);
        }
      }
    }
  }

  /**
   * Tokenizes and segments input text. Outputs tokenized text in conll format:
   * one token per sentence and two newlines to divide sentences.
   * 
   * @return String tokenized text
   */
  public String tokenizeToCoNLL() {
    StringBuilder sb = new StringBuilder();
    List<Token> tokens = tokenizer.tokenize();
    // remove paragraphs followed by lowercase words
    List<Integer> spuriousParas = getSpuriousParas(tokens);
    removeSpuriousParas(tokens,spuriousParas);
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
  public String tokenizeToCoNLLOffsets() {
    StringBuilder sb = new StringBuilder();
    List<Token> tokens = tokenizer.tokenize();
    // remove paragraphs followed by lowercase words
    List<Integer> spuriousParas = getSpuriousParas(tokens);
    removeSpuriousParas(tokens,spuriousParas);
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
  public String tokenizeToText() {
    List<Token> tokens = tokenizer.tokenize();
    // remove paragraphs followed by lowercase words
    List<Integer> spuriousParas = getSpuriousParas(tokens);
    removeSpuriousParas(tokens,spuriousParas);
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
   * @return a tokenizer Evaluator 
   * @throws IOException
   */
  public TokenizerEvaluator evaluateTokenizer(String referenceText)
      throws IOException {
    // tokenize input text
    List<Token> tokens = tokenizer.tokenize();
    // construct whitespace tokenizer to obtain the Token objects from reference
    // text
    Properties properties = new Properties();
    properties.setProperty("paragraphs", "no");
    StringReader stringReader = new StringReader(referenceText);
    BufferedReader refReader = new BufferedReader(stringReader);
    Tokenizer<Token> whiteSpacer = new WhiteSpaceTokenizer<Token>(refReader,
        tokenFactory, properties);
    // create Token objects out from the reference text
    List<Token> references = whiteSpacer.tokenize();
    // evaluate
    TokenizerEvaluator tokenizerEvaluator = new TokenizerEvaluator();
    tokenizerEvaluator.evaluate(references, tokens);
    return tokenizerEvaluator;
  }
  
  private static List<Integer> getSpuriousParas(List<Token> tokens) {
    List<Integer> spuriousTokens = new ArrayList<Integer>();
    for (int i = 1; i < (tokens.size() -1); ++i) {
      if (tokens.get(i).value().equals(IxaPipeLexer.PARAGRAPH_TOKEN) && 
          ( tokens.get(i+1).value().matches("[a-z]+") || 
              tokens.get(i+1).value().equals(IxaPipeLexer.PARAGRAPH_TOKEN))) {
        spuriousTokens.add(i);
      }
    }
    return spuriousTokens;
  }
  
  private static void removeSpuriousParas(List<Token> tokens, List<Integer> spuriousParas) {
    Collections.sort(spuriousParas, Collections.reverseOrder());
    for (int paraIndex : spuriousParas) {
      tokens.remove(paraIndex);
    }
  }
  
  public void tokensToKAF(Reader breader, KAFDocument kaf) throws IOException {
    List<String> sentences = IOUtils.readLines(breader);
    for (String sentence : sentences) {
      noSents = noSents + 1;
      String[] tokens = sentence.split(" ");
      for (String token : tokens) {
        if (token.equals(IxaPipeLexer.PARAGRAPH_TOKEN)) {
          ++noParas;
          //TODO sentences without end markers;
          //crap rule
          while (noParas > noSents) {
            ++noSents;
          }
        } else {
          //TODO add offset
          WF wf = kaf.newWF(token);
          wf.setPara(noParas);
          wf.setSent(noSents);
        }
      }
    }
  }

}
