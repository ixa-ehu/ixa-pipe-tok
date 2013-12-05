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
  private IXASegmenter segmenter;

  // counters
  int noSents = 0;
  int offsetCounter = 0;
  int current_index = 0;
  int previous_index = 0;

  public Annotate(BufferedReader breader, TokenFactory tokenFactory,
      String normalize) {
    tokenizer = new IXATokenizer<Token>(breader, tokenFactory, normalize);
    segmenter = new IXASegmenter();
  }

  public Annotate(BufferedReader breader) {

  }

  public String tokensToKAF(KAFDocument kaf) {

    List<Token> tokens = tokenizer.tokenize();
    List<List<Token>> sentences = segmenter.wordsToSentences(tokens);
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

  public String tokensToCoNLL() {
    StringBuilder sb = new StringBuilder();
    List<Token> tokens = tokenizer.tokenize();
    List<List<Token>> sentences = segmenter.wordsToSentences(tokens);

    for (List<Token> sentence : sentences) {
      for (Token token : sentence) {
        sb.append(token.value()).append(" ").append(token.startOffset())
            .append(" ").append(token.tokenLength()).append("\n");
      }
      sb.append("\n");
    }
    return sb.toString();
  }

  public String tokensToText() {
    List<Token> tokens = tokenizer.tokenize();
    List<List<Token>> sentences = segmenter.wordsToSentences(tokens);
    StringBuilder sb = new StringBuilder();

    for (List<Token> sentence : sentences) {

      for (Token token : sentence) {
        sb.append(token.value()).append(" ");
      }
      sb.append("\n");
    }
    return sb.toString();
  }

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

  public String tokenizedTextToKAF(BufferedReader breader, KAFDocument kaf)
      throws IOException {

    String text = buildText(breader);
    // this creates the actual sentences to be passed to the sentence detector
    String[] sentences = text.split("<P>");

    for (String sent : sentences) {
      // clean extra spaces
      sent = sent.trim();
      sent = sent.replaceAll("\\s+", " ");

      String[] tokens = sent.split(" ");
      // get sentence counter
      noSents = noSents + 1;

      for (int i = 0; i < tokens.length; i++) {
        // get offsets
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
