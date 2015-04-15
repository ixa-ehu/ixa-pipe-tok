package eus.ixa.ixa.pipe.tok;

import java.io.BufferedReader;
import java.io.IOException;

import eus.ixa.ixa.pipe.seg.RuleBasedSegmenter;

public class InputOuput {

  private InputOuput() {
    
  }
  
  public static String readText(BufferedReader breader) {
    String line;
    StringBuilder sb = new StringBuilder();
    try {
      while ((line = breader.readLine()) != null) {
        sb.append(line).append(RuleBasedSegmenter.LINE_BREAK);
      }
    } catch (IOException e) {
      e.printStackTrace();
    }
    String text = sb.toString();
    return text;
  }

}
