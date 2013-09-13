package ixa.pipe.resources;

public class Formats {

  public String cleanWeirdChars(String line) {
    line = line.replaceAll("’", "'");
    line = line.replaceAll("’", "'");
    line = line.replaceAll("‘", "'");
    line = line.replaceAll("“", "\"");
    line = line.replaceAll("”", "\"");
    line = line.replaceAll("`", "'");
    line = line.replace("''","\"");
    return line;

  }

}
