package eus.ixa.ixa.pipe.seg;

import java.io.IOException;
import java.io.Reader;

/**
 * BufferedReader with readLine function that also outputs
 * \r and \n newline characters.
 *
 * @version 2014-12-01
 *
 */
public class LineTerminatorReader {
  private Reader delegate;
  private StringBuilder sb;
  private int nextCh = SOL;
  private int readChars;

  private static final int SOL = -10; // Start Of Line

  /**
   * Construct a LineTerminatorReader from a Reader.
   * @param delegate the Reader
   */
  public LineTerminatorReader(Reader delegate) {
    this.delegate = delegate;
    sb = new StringBuilder();
  }

  /**
   * Reads all chars of a line, returning also line ending characters.
   * @return the line text
   */
  public String readLine() throws IOException {
    String res = null;
    sb.setLength(0);
    int ch = (char) -10;

    if (nextCh == -1) {
      res = null;
    } else {

      boolean newLine = false;
      boolean eof = false;
      while (!newLine && !eof) {
        if (nextCh != SOL) {
          sb.append((char) nextCh);
        }
        nextCh = SOL;
        ch = delegate.read();
        switch (ch) {
        case '\r':
          // check for double newline char
          nextCh = delegate.read();
          if (nextCh == '\n') {
            // double line found
            sb.append("\r\n");
            newLine = true;
            nextCh = SOL;
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
          eof = true;
          nextCh = -1;
          break;
        default:
          if (ch != -1)
            sb.append((char) ch);
        }
      }
      res = sb.toString();
      readChars += res.length();
    }
    return res;
  }

  public void close() throws IOException {
    delegate.close();
  }

  public int readChars() {
    return readChars;
  }
}
