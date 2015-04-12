package eus.ixa.ixa.pipe.seg;

import java.io.IOException;
import java.io.Reader;

/**
 * BufferedReader with readLine function that also outputs
 * \r and \n newline characters.
 */
public class NewLineBufferedReader {
  private Reader delegate;
  private StringBuilder sb;
  private int nextCharacter = startOfLine;
  private int readChars;

  private static final int startOfLine = -10; // Start Of Line

  public NewLineBufferedReader(Reader delegate) {
    this.delegate = delegate;
    sb = new StringBuilder();
  }

  /**
   * Readline with newlines.
   * @return the string
   * @throws IOException the io exception
   */
  public String readLine() throws IOException {
    String result = null;
    sb.setLength(0);
    int curChar = (char) -10;

    if (nextCharacter == -1) {
      result = null;
    } else {

      boolean newLine = false;
      boolean endOfFile = false;
      while (!newLine && !endOfFile) {
        if (nextCharacter != startOfLine) {
          sb.append((char) nextCharacter);
        }
        nextCharacter = startOfLine;
        curChar = delegate.read();
        switch (curChar) {
        case '\r':
          // check for double newline char
          nextCharacter = delegate.read();
          if (nextCharacter == '\n') {
            // double line found
            sb.append("\r\n");
            newLine = true;
            nextCharacter = startOfLine;
          } else {
            sb.append("\r");
            newLine = true;
          }
          break;
        case '\n':
          sb.append("\n");
          newLine = true;
          break;
        case -1:
          endOfFile = true;
          nextCharacter = -1;
          break;
        default:
          if (curChar != -1)
            sb.append((char) curChar);
        }
      }
      result = sb.toString();
      readChars += result.length();
    }
    return result;
  }

  public void close() throws IOException {
    delegate.close();
  }

  public int readChars() {
    return readChars;
  }
}
