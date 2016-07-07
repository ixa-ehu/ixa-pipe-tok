/*
 * Copyright 2016 Rodrigo Agerri

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

import java.io.BufferedReader;
import java.io.IOException;
import java.io.Reader;
import java.util.List;
import java.util.Properties;

import com.google.common.io.CharStreams;

import eus.ixa.ixa.pipe.ml.tok.RuleBasedSegmenter;
import eus.ixa.ixa.pipe.ml.tok.RuleBasedTokenizer;
import eus.ixa.ixa.pipe.ml.tok.Token;
import eus.ixa.ixa.pipe.ml.tok.Tokenizer;

/**
 * This class provides the annotation functions to output the tokenized text
 * into:
 * <ol>
 * <li>A list of WF elements inside a NAF document (DEFAULT)
 * <li>As running tokenized and segmented text
 * <li>CoNLL format, namely, one token per line and two newlines for each
 * sentence.
 * </ol>
 * All these options are configurable by using the parameters of the CLI.
 * @author ragerri
 * @version 2016-04-20
 */
public class Annotate {

  /**
   * The tokenizer.
   */
  private final Tokenizer toker;
  /**
   * The sentence splitter.
   */
  private final RuleBasedSegmenter segmenter;

  /**
   * Build an annotator from the reader and the properties object.
   * @param breader the reader
   * @param properties the properties
   */
  public Annotate(final BufferedReader breader, final Properties properties) {
    String text = RuleBasedSegmenter.readText(breader);
    segmenter = new RuleBasedSegmenter(text, properties);
    toker = new RuleBasedTokenizer(text, properties);
  }

  /**
   * Tokenize document to NAF.
   * @param kaf
   *          the incoming naf document
   * @throws IOException
   *           if io problems
   */
  public void tokenizeToKAF(final KAFDocument kaf) throws IOException {

    int noSents = 0;
    int noParas = 1;

    final String[] sentences = segmenter.segmentSentence();
    final List<List<Token>> tokens = toker.tokenize(sentences);
    for (final List<Token> tokenizedSentence : tokens) {
      noSents = noSents + 1;
      for (final Token token : tokenizedSentence) {
        if (token.getTokenValue().equals(RuleBasedSegmenter.PARAGRAPH)) {
          ++noParas;
          // TODO debug this
          if (noSents < noParas) {
            ++noSents;
          }
        } else {
          final WF wf = kaf.newWF(token.startOffset(), token.getTokenValue(),
              noSents);
          wf.setLength(token.tokenLength());
          wf.setPara(noParas);
        }
      }
    }
  }

  /**
   * Tokenizes and segments input text. Outputs tokenized text in conll format:
   * one token per sentence and two newlines to divide sentences.
   * @return String tokenized text
   */
  public String tokenizeToCoNLL() {

    final StringBuilder sb = new StringBuilder();
    final String[] sentences = segmenter.segmentSentence();
    final List<List<Token>> tokens = toker.tokenize(sentences);
    for (final List<Token> tokSentence : tokens) {
      for (final Token token : tokSentence) {
        String tokenValue = token.getTokenValue();
        if (tokenValue.equals(RuleBasedSegmenter.PARAGRAPH)) {
          tokenValue = "*<P>*";
        }
        sb.append(tokenValue.trim()).append("\n");
      }
      sb.append("\n");
    }
    return sb.toString();
  }

  /**
   * Tokenizes and segments input text. Outputs tokenized text in conll format:
   * one token per sentence and two newlines to divide sentences plus offsets
   * and lenght information about tokens.
   * @return String tokenized text
   */
  public String tokenizeToCoNLLOffsets() {

    final StringBuilder sb = new StringBuilder();
    final String[] sentences = segmenter.segmentSentence();
    final List<List<Token>> tokens = toker.tokenize(sentences);
    for (final List<Token> tokSentence : tokens) {
      for (final Token token : tokSentence) {
        String tokenValue = token.getTokenValue();
        if (tokenValue.equals(RuleBasedSegmenter.PARAGRAPH)) {
          tokenValue = "*<P>*";
        }
        sb.append(tokenValue.trim()).append(" ").append(token.startOffset())
            .append(" ").append(token.tokenLength()).append("\n");
      }
      sb.append("\n");
    }
    return sb.toString();
  }

  /**
   * Tokenize and Segment input text. Outputs tokens in running text format one
   * sentence per line.
   * @return String tokenized text
   */
  public String tokenizeToText() {

    final StringBuilder sb = new StringBuilder();
    final String[] sentences = segmenter.segmentSentence();
    final List<List<Token>> tokens = toker.tokenize(sentences);
    for (final List<Token> tokSentence : tokens) {
      for (final Token token : tokSentence) {
        String tokenValue = token.getTokenValue();
        if (tokenValue.equals(RuleBasedSegmenter.PARAGRAPH)) {
          sb.append("*<P>*").append("\n");
        } else {
          sb.append(tokenValue.trim()).append(" ");
        }
      }
      sb.append("\n");
    }
    return sb.toString().trim();
  }

  /**
   * Read already tokenized text (one sentence per line) and builds a NAF
   * document.
   * @param breader
   *          the reader
   * @param kaf
   *          the naf document
   * @throws IOException
   *           if io problems
   */
  public static void tokensToKAF(final Reader breader, final KAFDocument kaf)
      throws IOException {
    int noSents = 0;
    int noParas = 1;
    final List<String> sentences = CharStreams.readLines(breader);
    for (final String sentence : sentences) {
      noSents = noSents + 1;
      final String[] tokens = sentence.split(" ");
      for (final String token : tokens) {
        if (token.equals(RuleBasedSegmenter.PARAGRAPH)) {
          ++noParas;
          // TODO sentences without end markers;
          // crap rule
          while (noParas > noSents) {
            ++noSents;
          }
        } else {
          // TODO add offset
          final WF wf = kaf.newWF(0, token, noSents);
          wf.setPara(noParas);
          // wf.setSent(noSents);
        }
      }
    }
  }

}
