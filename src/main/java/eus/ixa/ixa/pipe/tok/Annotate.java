/*
 * Copyright 2015 Rodrigo Agerri

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

package eus.ixa.ixa.pipe.tok;

import ixa.kaflib.KAFDocument;
import ixa.kaflib.WF;

import java.io.IOException;
import java.util.List;
import java.util.Properties;

import eus.ixa.ixa.pipe.seg.RuleBasedSegmenter;

/**
 * This class provides the annotation functions to output the tokenized text
 * into:
 * <ol>
 * <li>A list of <WF> elements inside a NAF document (DEFAULT)
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
 * @version 2015-04-08
 * 
 */
public class Annotate {

  /**
   * The tokenizer.
   */
  private Tokenizer toker;
  /**
   * The sentence splitter.
   */
  private RuleBasedSegmenter segmenter;
  /**
   * Sentence counter.
   */
  int noSents = 0;
  /**
   * Paragraph counter.
   */
  int noParas = 1;
  /**
   * Offset counter.
   */
  int offsetCounter = 0;
  /**
   * Current index.
   */
  int curIndex = 0;
  /**
   * Previous index.
   */
  int prevIndex = 0;

  public Annotate(Properties properties) {
    segmenter = new RuleBasedSegmenter(properties);
    toker = new RuleBasedTokenizer(properties);
  }

  public void tokenizeToKAF(String text, KAFDocument kaf) throws IOException {

      String[] sentences = segmenter.segmentSentence(text);
      List<List<Token>> tokens = toker.tokenize(sentences);
      for (List<Token> tokSentence: tokens) {
        noSents = noSents + 1;
        for (Token token : tokSentence) {
          if (token.getTokenValue().equals(RuleBasedSegmenter.PARAGRAPH)) {
            ++noParas;
          } else {
            WF wf = kaf.newWF(token.getTokenValue(), token.startOffset(), noSents);
            wf.setPara(noParas);
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
  public String tokenizeToCoNLL(String text) {

    StringBuilder sb = new StringBuilder();
    String[] sentences = segmenter.segmentSentence(text);
    List<List<Token>> tokens = toker.tokenize(sentences);
    for (List<Token> tokSentence: tokens) {
      for (Token token : tokSentence) {
        sb.append(token.getTokenValue().trim()).append("\n");
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
  public String tokenizeToCoNLLOffsets(String text) {

    StringBuilder sb = new StringBuilder();
    String[] sentences = segmenter.segmentSentence(text);
    List<List<Token>> tokens = toker.tokenize(sentences);
    for (List<Token> tokSentence: tokens) {
      for (Token token : tokSentence) {
        sb.append(token.getTokenValue().trim()).append(" ").append(token.startOffset())
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
  public String tokenizeToText(String text) {

    StringBuilder sb = new StringBuilder();
    String[] sentences = segmenter.segmentSentence(text);
    List<List<Token>> tokens = toker.tokenize(sentences);
    for (List<Token> tokSentence: tokens) {
      for (Token token : tokSentence) {
          if (token.getTokenValue().equals(RuleBasedSegmenter.PARAGRAPH)) {
            sb.append(token.getTokenValue()).append("\n");
          }
          else {
            sb.append(token.getTokenValue().trim()).append(" ");
          }
        }
        sb.append("\n");
    }
    return sb.toString().trim();
  }

  public void tokensToKAF(String text, KAFDocument kaf) throws IOException {

    String[] sentences = text.split(RuleBasedSegmenter.LINE_BREAK);
    for (String sent : sentences) {
      sent = sent.trim();
      sent = sent.replaceAll("\\s+", " ");
      String[] tokens = sent.split(" ");

      noSents = noSents + 1;
      for (int i = 0; i < tokens.length; i++) {
        curIndex = sent.indexOf(tokens[i], prevIndex);
        int offset = offsetCounter + curIndex;
        WF wf = kaf.newWF(tokens[i], offset);
        wf.setSent(noSents);
        prevIndex = curIndex + tokens[i].length();
      }
      offsetCounter += sent.length();
    }
  }
  
}
