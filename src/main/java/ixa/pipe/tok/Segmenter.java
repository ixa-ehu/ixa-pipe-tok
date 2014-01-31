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

/**
 * Sentence segmenter
 * 
 * Splits tokenized text into sentences if boundary {@link Token} are themselves
 * tokens. {@link Token}
 * 
 * @author ragerri
 * @version 2013-18-12
 * 
 */
public class Segmenter {

  private static final boolean DEBUG = false;

  /**
   * List of regex Pattern that are sentence boundaries to be discarded;
   * newlines by default.
   */
  private List<Pattern> sentenceBoundariesToDiscard;

  /**
   * Set of tokens such as DEFAULT_BOUNDARY_FOLLOWERS which come after a
   * canonical sentence boundary {".","?","!"}
   */
  private final Set<String> sentenceBoundaryFollowers;

  /**
   * Pattern for sentence boundary tokens, eg., {".","?","!"}
   */
  private final Pattern boundaryTokens;

  /**
   * For ending sentences with [\")\]\}]\.[!?]+
   */
  public static final Set<String> DEFAULT_BOUNDARY_FOLLOWERS = new HashSet<String>(
      Arrays.asList(")", "]", "''", "-RRB-", "-RSB-", "-RCB-"));
  /**
   * Do not keep newline tokens
   */
  public static final Set<String> DEFAULT_SENTENCE_BOUNDARIES_TO_DISCARD = new HashSet<String>(
      Arrays.asList("\n", IxaPipeLexer.NEWLINE_TOKEN));
  
  private boolean allowEmptySentences = false;

  /**
   * Constructs a {@code Segmenter} using a default list of tokens to split:
   * {".","?","!"}
   */
  public Segmenter() {
    this("\\.|[!?]+");
  }

  /**
   * Flexibly set the set of acceptable sentence boundary tokens, but including
   * a default set boundary follower tokens ( {")","]","\"","\'", "''", "-RRB-",
   * "-RSB-", "-RCB-"} ) and sentence boundary to discard tokens (newlines).
   * 
   * @param boundaryTokenRegex
   *          The set of boundary tokens
   */
  public Segmenter(String boundaryTokenRegex) {
    this(boundaryTokenRegex, DEFAULT_BOUNDARY_FOLLOWERS,
        DEFAULT_SENTENCE_BOUNDARIES_TO_DISCARD);
  }

  /**
   * Flexibly sets the set of acceptable sentence boundary tokens, the set of
   * tokens commonly following sentence boundaries such as ")}\"" and also the
   * set of tokens that are sentences boundaries that should be discarded (such
   * as newlines).
   */
  public Segmenter(String boundaryTokenRegex, Set<String> boundaryFollowers,
      Set<String> boundariesToDiscard) {

    boundaryTokens = Pattern.compile(boundaryTokenRegex);
    sentenceBoundaryFollowers = boundaryFollowers;
    setSentenceBoundariesToDiscard(boundariesToDiscard);
    
    if (DEBUG) {
      System.err.println("segmenter: boundaryTokens=" + boundaryTokenRegex);
      System.err.println("  boundaryFollowers=" + boundaryFollowers);
      System.err.println("  boundaryToDiscard=" + boundariesToDiscard);
    }
  }

  public void setSentenceBoundariesToDiscard(Set<String> regexSet) {
    sentenceBoundariesToDiscard = new ArrayList<Pattern>(regexSet.size());
    for (String s : regexSet) {
      sentenceBoundariesToDiscard.add(Pattern.compile(Pattern.quote(s)));
    }
  }

  private boolean matchesSentenceBoundaryToDiscard(String word) {
    for (Pattern p : sentenceBoundariesToDiscard) {
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
   * Splits tokenized text into sentences and it returns a List of Sentences.
   * Each sentence is itself a List<Token>. This function reads a List<Token>
   * and creates a sublist whenever a boundaryToken is found. It adds tokens to a
   * currentList until a boundaryToken is found. Then the current list is added
   * to the List<List<Token>> segmentedSentences and a new sublist is created
   * to add the following tokens until a new boundaryToken is found. The
   * exception is when a boundaryFollower is found that is added directly to the
   * previous Sentence, if not empty. This function requires that the input be a
   * List of Tokens including boundary Tokens (e.g., {@link IxaPipeTokenizer}
   * ).
   * 
   * @param tokens a list of Token objects
   * @return A list of Sentences, which is a List of Lists
   * @see #Segmenter(String, Set, Set)
   */
  public List<List<Token>> segment(List<Token> tokens) {

    List<List<Token>> segmentedSentences = new ArrayList<List<Token>>();
    List<Token> currentSentence = new ArrayList<Token>();
    List<Token> previousSentence = null;

    for (Token token : tokens) {
      String word = token.value();
      boolean forcedEnd = false;

      if (DEBUG) {
        System.err.println("Word is " + word);
      }
      if (sentenceBoundaryFollowers.contains(word) && previousSentence != null
          && currentSentence.isEmpty()) {
        previousSentence.add(token);
        if (DEBUG) {
          System.err.println("  added to last");
        }
      } else {
        boolean newSent = false;
        if (matchesSentenceBoundaryToDiscard(word)) {
          newSent = true;
        } else if (boundaryTokens.matcher(word).matches()) {
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
          segmentedSentences.add(currentSentence);
          // adds this sentence now that it's complete
          previousSentence = currentSentence;
          currentSentence = new ArrayList<Token>(); // clears the current
                                                    // sentence
        }
      }
    }
    // add any words at the end, even if there isn't a sentence
    // terminator at the end of file
    if (!currentSentence.isEmpty()) {
      segmentedSentences.add(currentSentence); // adds last sentence
    }
    return segmentedSentences;
  }

}
