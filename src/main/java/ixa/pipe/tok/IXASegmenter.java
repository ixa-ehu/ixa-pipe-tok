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

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/* 
 * Sentence segmenter 
 * 
 * @author ragerri
 * 
 */
public class IXASegmenter {

  private static final boolean DEBUG = false;

  /**
   * List of regex Pattern that are sentence boundaries to be discarded.
   */
  private List<Pattern> sentenceBoundaryToDiscard;
  private final Pattern sentenceRegionBeginPattern;
  private final Pattern sentenceRegionEndPattern;

  public static final Set<String> DEFAULT_BOUNDARY_FOLLOWERS = new HashSet<String>(
      Arrays.asList(")", "]", "\"", "\'", "''", "-RRB-", "-RSB-", "-RCB-"));
  public static final Set<String> DEFAULT_SENTENCE_BOUNDARIES_TO_DISCARD = new HashSet<String>(
      Arrays.asList("\n", JFlexTokenizer.NEWLINE_TOKEN));

  /**
   * Set of tokens (Strings) that qualify as tokens that can follow what
   * normally counts as an end of sentence token, and which are attributed to
   * the preceding sentence. For example ")" coming after a period.
   */
  private final Set<String> sentenceBoundaryFollowers;

  /**
   * Regex for tokens (Strings) that qualify as sentence-final tokens.
   */
  private final Pattern sentenceBoundaryTokenPattern;

  private boolean allowEmptySentences = false;

  /**
   * Create a {@code IXASegmenter} using a sensible default list of
   * tokens to split on for English/Latin writing systems. The default set is:
   * {".","?","!"} and any combination of ! or ?, as in !!!?!?!?!!!?!!?!!!.
   */
  public IXASegmenter() {
    this("\\.|[!?]+");
  }

  /**
   * Flexibly set the set of acceptable sentence boundary tokens, but with a
   * default set of allowed boundary following tokens and sentence boundary to
   * discard tokens (based on English and Penn Treebank encoding). The allowed
   * set of boundary followers is: {")","]","\"","\'", "''", "-RRB-", "-RSB-",
   * "-RCB-"}. The default set of discarded separator tokens includes the
   * newline tokens used by WhitespaceLexer and PTBLexer.
   * 
   * @param boundaryTokenRegex
   *          The set of boundary tokens
   */
  public IXASegmenter(String boundaryTokenRegex) {
    this(boundaryTokenRegex, DEFAULT_BOUNDARY_FOLLOWERS,
        DEFAULT_SENTENCE_BOUNDARIES_TO_DISCARD);
  }

  /**
   * Flexibly set the set of acceptable sentence boundary tokens, the set of
   * tokens commonly following sentence boundaries, and also the set of tokens
   * that are sentences boundaries that should be discarded.
   */
  public IXASegmenter(String boundaryTokenRegex, Set<String> boundaryFollowers,
      Set<String> boundaryToDiscard) {
    this(boundaryTokenRegex, boundaryFollowers, boundaryToDiscard, null, null);
  }

  /**
   * Flexibly set a pattern that matches acceptable sentence boundaries, the set
   * of tokens commonly following sentence boundaries, and also the set of
   * tokens that are sentence boundaries that should be discarded. This is
   * private because it is a dangerous constructor. It's not clear what the
   * semantics should be if there are both boundary token sets, and patterns to
   * match.
   */
  private IXASegmenter(String boundaryTokenRegex,
      Set<String> boundaryFollowers, Set<String> boundaryToDiscard,
      Pattern regionBeginPattern, Pattern regionEndPattern) {
    
    sentenceBoundaryTokenPattern = Pattern.compile(boundaryTokenRegex);
    sentenceBoundaryFollowers = boundaryFollowers;
    setSentenceBoundaryToDiscard(boundaryToDiscard);
    sentenceRegionBeginPattern = regionBeginPattern;
    sentenceRegionEndPattern = regionEndPattern;
    if (DEBUG) {
      System.err.println("WordToSentenceProcessor: boundaryTokens="
          + boundaryTokenRegex);
      System.err.println("  boundaryFollowers=" + boundaryFollowers);
      System.err.println("  boundaryToDiscard=" + boundaryToDiscard);
    }
  }

  public void setSentenceBoundaryToDiscard(Set<String> regexSet) {
    sentenceBoundaryToDiscard = new ArrayList<Pattern>(regexSet.size());
    for (String s : regexSet) {
      sentenceBoundaryToDiscard.add(Pattern.compile(Pattern.quote(s)));
    }
  }

  private boolean matchesSentenceBoundaryToDiscard(String word) {
    for (Pattern p : sentenceBoundaryToDiscard) {
      Matcher m = p.matcher(word);
      if (m.matches()) {
        return true;
      }
    }
    return false;
  }

  public boolean allowEmptySentences() {
    return allowEmptySentences;
  }

  /**
   * Returns a List of Lists where each element is built from a run of Words in
   * the input Document. Specifically, reads through each word in the input
   * document and breaks off a sentence after finding a valid sentence boundary
   * token or end of file. Note that for this to work, the words in the input
   * document must have been tokenized with a tokenizer that makes sentence
   * boundary tokens their own tokens (e.g., {@link IXATokenizer}).
   * 
   * @param words
   *          A list of already tokenized words (must implement HasWord or be a
   *          String)
   * @return A list of Sentence
   * @see #IXASegmenter(String, Set, Set, Pattern, Pattern)
   */
  public List<List<Token>> wordsToSentences(List<Token> tokens) {
    List<List<Token>> sentences = new ArrayList<List<Token>>();
    List<Token> currentSentence = new ArrayList<Token>();
    List<Token> lastSentence = null;
    boolean insideRegion = false;
    for (Token token : tokens) {
      String word = token.value();
      boolean forcedEnd = false;

      if (DEBUG) {
        System.err.println("Word is " + word);
      }
      if (sentenceRegionBeginPattern != null && !insideRegion) {
        if (sentenceRegionBeginPattern.matcher(word).matches()) {
          insideRegion = true;
        }
        if (DEBUG) {
          System.err.println("  outside region");
        }
        continue;
      }
      if (sentenceBoundaryFollowers.contains(word) && lastSentence != null
          && currentSentence.isEmpty()) {
        lastSentence.add(token);
        if (DEBUG) {
          System.err.println("  added to last");
        }
      } else {
        boolean newSent = false;
        if (matchesSentenceBoundaryToDiscard(word)) {
          newSent = true;
        } else if (sentenceRegionEndPattern != null
            && sentenceRegionEndPattern.matcher(word).matches()) {
          insideRegion = false;
          newSent = true;
        } else if (sentenceBoundaryTokenPattern.matcher(word).matches()) {
          currentSentence.add(token);
          if (DEBUG) {
            System.err.println("  is sentence boundary; added to current");
          }
          newSent = true;
        } else if (forcedEnd) {
          currentSentence.add(token);
          newSent = true;
          if (DEBUG) {
            System.err.println("  annotated to be the end of a sentence");
          }
        } else {
          currentSentence.add(token);
          if (DEBUG) {
            System.err.println("  added to current");
          }
        }
        if (newSent && (!currentSentence.isEmpty() || allowEmptySentences())) {
          if (DEBUG) {
            System.err.println("  beginning new sentence");
          }
          sentences.add(currentSentence);
          // adds this sentence now that it's complete
          lastSentence = currentSentence;
          currentSentence = new ArrayList<Token>(); // clears the current
                                                    // sentence
        }
      }
    }

    // add any words at the end, even if there isn't a sentence
    // terminator at the end of file
    if (!currentSentence.isEmpty()) {
      sentences.add(currentSentence); // adds last sentence
    }
    return sentences;
  }

}
