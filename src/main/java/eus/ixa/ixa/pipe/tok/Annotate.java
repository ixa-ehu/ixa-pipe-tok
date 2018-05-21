/*
 * Copyright 2016, 2018 Rodrigo Agerri

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
import java.util.ArrayList;
import java.util.List;
import java.util.Properties;

import com.google.common.io.CharStreams;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
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
 * 
 * @author ragerri
 * @version 2016-04-20
 */
public class Annotate {

  private static String DELIMITER = " ";
  private static String LINE_BREAK = "\n";
  private static String DEFAULT_TOKEN_VALUE = "*<P>*";

  private static final Logger LOG = LogManager.getLogger(Annotate.class);

  /**
   * The tokenizer.
   */
  private final Tokenizer tokenizer;
  /**
   * The sentence splitter.
   */
  private final RuleBasedSegmenter segmenter;
  private List<String> text;
  private boolean isNoSeg;

  /**
   * Build an annotator from the reader and the properties object.
   * 
   * @param breader
   *          the reader
   * @param properties
   *          the properties
   */
  public Annotate(final BufferedReader breader, final Properties properties) {
    isNoSeg = Boolean.valueOf(properties.getProperty("noseg"));
    if (isNoSeg) {
      text = buildSegmentedSentences(breader);
    }
    String textSegment = RuleBasedSegmenter.readText(breader);
    segmenter = new RuleBasedSegmenter(textSegment, properties);
    tokenizer = new RuleBasedTokenizer(textSegment, properties);
  }

  /**
   * Reads standard input text from the BufferedReader.
   * 
   * @param breader
   *          the buffered reader
   * @return the input text in a string object
   */
  // TODO move to ixa-pipe-ml
  private static List<String> buildSegmentedSentences(final BufferedReader breader) {
    String line;
    List<String> sentences = new ArrayList<>();
    try {
      while ((line = breader.readLine()) != null) {
        sentences.add(line);
      }
    } catch (final IOException e) {
      LOG.error("IOException", e);
    }
    return sentences;
  }

  /**
   * Tokenize document to NAF.
   * 
   * @param kaf
   *          the incoming naf document
   * @throws IOException
   *           if io problems
   */
  public void tokenizeToKAF(final KAFDocument kaf) throws IOException {

    int noSents = 0;
    int noParas = 1;
    
    List<List<Token>> tokens;

    if (isNoSeg) {
      String[] sentences = text.toArray(new String[text.size()]);
      tokens = tokenizer.tokenize(sentences);
    } else {
      final String[] sentences = segmenter.segmentSentence();
      tokens = tokenizer.tokenize(sentences);
    }
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
   * 
   * @return String tokenized text
   */
  public String tokenizeToCoNLL() {

    final StringBuilder sb = new StringBuilder();
    final String[] sentences = segmenter.segmentSentence();
    final List<List<Token>> tokens = tokenizer.tokenize(sentences);
    for (final List<Token> tokSentence : tokens) {
      for (final Token token : tokSentence) {
        String tokenValue = token.getTokenValue();
        if (tokenValue.equals(RuleBasedSegmenter.PARAGRAPH)) {
          tokenValue = DEFAULT_TOKEN_VALUE;
        }
        sb.append(tokenValue.trim()).append(LINE_BREAK);
      }
      sb.append(LINE_BREAK);
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

    final StringBuilder sb = new StringBuilder();
    final String[] sentences = segmenter.segmentSentence();
    final List<List<Token>> tokens = tokenizer.tokenize(sentences);
    for (final List<Token> tokSentence : tokens) {
      for (final Token token : tokSentence) {
        String tokenValue = token.getTokenValue();
        if (tokenValue.equals(RuleBasedSegmenter.PARAGRAPH)) {
          tokenValue = DEFAULT_TOKEN_VALUE;
        }
        sb.append(tokenValue.trim()).append(DELIMITER).append(token.startOffset())
            .append(DELIMITER).append(token.tokenLength()).append(LINE_BREAK);
      }
      sb.append(LINE_BREAK);
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

    final StringBuilder sb = new StringBuilder();
    if (isNoSeg) {
      String[] sentences = text.toArray(new String[text.size()]);
      final List<List<Token>> tokens = tokenizer.tokenize(sentences);
      for (final List<Token> tokSentence : tokens) {
        for (final Token tok : tokSentence) {
          String tokenValue = tok.getTokenValue();
          sb.append(tokenValue.trim()).append(DELIMITER);
        }
        sb.append(LINE_BREAK);
      }
    } else {
      final String[] sentences = segmenter.segmentSentence();
      final List<List<Token>> tokens = tokenizer.tokenize(sentences);
      for (final List<Token> tokSentence : tokens) {
        for (final Token token : tokSentence) {
          String tokenValue = token.getTokenValue();
          if (tokenValue.equals(RuleBasedSegmenter.PARAGRAPH)) {
            sb.append(DEFAULT_TOKEN_VALUE).append(LINE_BREAK);
          } else {
            sb.append(tokenValue.trim()).append(DELIMITER);
          }
        }
        sb.append(LINE_BREAK);
      }
    }
    return sb.toString().trim();
  }

  /**
   * Read already tokenized text (one sentence per line) and builds a NAF
   * document.
   * 
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
      final String[] tokens = sentence.split(DELIMITER);
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
