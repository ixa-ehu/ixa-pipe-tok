package eus.ixa.ixa.pipe.cli;

import static org.junit.Assert.*;
import org.junit.Test;

public class CLIArgumentsParserTest {
  private CLIArgumentsParser argumentsParser = new CLIArgumentsParser(
      "test-version");

  @Test
  public void testParseUnknownStrategy() throws Exception {
    String[] args = { "unknown-strategy" };
    Parameters parameters = argumentsParser.parse(args);

    assertEquals("unknown-strategy", parameters.getStrategyString());
    assertEquals(Strategy.UNKNOWN, parameters.getStrategy());
  }

  @Test
  public void testParseTokStrategy() throws Exception {
    String[] args = { "tok", "-l", "en" };
    Parameters parameters = argumentsParser.parse(args);

    assertEquals(Strategy.TOKENIZE, parameters.getStrategy());
    assertEquals("en", parameters.getLanguage());

    args = "tok -l en -n tutpenn".split("\\s");
    parameters = argumentsParser.parse(args);

    assertEquals(Strategy.TOKENIZE, parameters.getStrategy());
    assertEquals("en", parameters.getLanguage());
    assertEquals("tutpenn", parameters.getNormalize());
  }

  @Test
  public void testParseServerStrategy() throws Exception {
    String[] args = "server -l en -p 8001".split("\\s");
    Parameters parameters = argumentsParser.parse(args);

    assertEquals(Strategy.SERVER, parameters.getStrategy());
    assertEquals("en", parameters.getLanguage());
    assertEquals("8001", parameters.getPort());
  }

  @Test
  public void testParseClientStrategy() throws Exception {
    String[] args = "client -p 8001".split("\\s");
    Parameters parameters = argumentsParser.parse(args);

    assertEquals(Strategy.CLIENT, parameters.getStrategy());
    assertEquals("8001", parameters.getPort());
  }
}
