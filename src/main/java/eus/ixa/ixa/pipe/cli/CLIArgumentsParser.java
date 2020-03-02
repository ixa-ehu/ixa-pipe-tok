package eus.ixa.ixa.pipe.cli;

import net.sourceforge.argparse4j.ArgumentParsers;
import net.sourceforge.argparse4j.impl.Arguments;
import net.sourceforge.argparse4j.inf.*;

public class CLIArgumentsParser {
  // Software version
  private final String version;

  // create internal argument parser
  private final ArgumentParser argParser;
  /**
   * The parser that manages the tagging sub-command.
   */
  private final Subparser annotateParser;
  /**
   * Parser to start TCP socket for server-client functionality.
   */
  private final Subparser serverParser;
  /**
   * Sends queries to the serverParser for annotation.
   */
  private final Subparser clientParser;

  public CLIArgumentsParser(String version) {
    this.version = version;

    String argumentParserName = "ixa-pipe-tok-" + version + ".jar";
    String argumentParserDescription = "ixa-pipe-tok-" + version
        + " is a multilingual tokenizer developed by the IXA NLP Group.\n";
    this.argParser = ArgumentParsers.newArgumentParser(argumentParserName)
        .description(argumentParserDescription);
    Subparsers subParsers = this.argParser.addSubparsers();
    this.annotateParser = createAnnotateArgumentsParser(subParsers);
    this.serverParser = createServerArgumentsParser(subParsers);
    this.clientParser = createClientArgumentsParser(subParsers);
  }

  public Parameters parse(String[] args) throws ArgumentParserException {
    Strategy strategy = Strategies.parse(args[0]);
    Namespace namespace;
    switch (strategy) {
    case TOKENIZE:
      namespace = this.argParser.parseArgs(args);
      return new Parameters(args[0], strategy, namespace);
    case SERVER:
      namespace = this.argParser.parseArgs(args);
      return new Parameters(args[0], strategy, namespace);
    case CLIENT:
      namespace = this.argParser.parseArgs(args);
      return new Parameters(args[0], strategy, namespace);
    default:
      return new Parameters(args[0], Strategy.UNKNOWN);
    }
  }

  public void handleError(ArgumentParserException e) {
    this.argParser.handleError(e);
  }

  private Subparser createAnnotateArgumentsParser(Subparsers subParsers) {
    Subparser result = subParsers.addParser("tok").help("Tagging CLI");

    // specify language (for language dependent treatment of apostrophes)
    result.addArgument("-l", "--lang")
        .choices("ca", "de", "en", "es", "eu", "fr", "gl", "it", "nl", "ru").required(true)
        .help(
            "It is REQUIRED to choose a language to perform annotation with ixa-pipe-tok.\n");
    result.addArgument("-n", "--normalize")
        .choices("alpino", "ancora", "ctag", "default", "ptb", "tiger",
            "tutpenn")
        .required(false).setDefault("default").help(
            "Set normalization method according to corpus; the default option does not escape "
                + "brackets or forward slashes. See README for more details.\n");
    result.addArgument("-u", "--untokenizable").choices("yes", "no")
        .setDefault("no").required(false)
        .help("Print untokenizable characters.\n");
    result.addArgument("-o", "--outputFormat")
        .choices("conll", "oneline", "naf").setDefault("naf").required(false)
        .help("Choose output format; it defaults to NAF.\n");
    result.addArgument("--offsets").action(Arguments.storeFalse()).help(
        "Do not print offset and lenght information of tokens in CoNLL format.\n");
    result.addArgument("--inputkaf").action(Arguments.storeTrue()).help(
        "Use this option if input is a KAF/NAF document with <raw> layer.\n");
    result.addArgument("--notok").action(Arguments.storeTrue()).help(
        "Build a KAF document from an already tokenized sentence per line file.\n");
    result.addArgument("--noseg").action(Arguments.storeTrue())
        .help("Tokenize without segmenting sentences.\n");
    result.addArgument("--hardParagraph").choices("yes", "no").setDefault("no")
        .required(false).help("Do not segment paragraphs. Ever.\n");
    result.addArgument("--kafversion").setDefault("v1.naf")
        .help("Set kaf document version.\n");
    return result;
  }

  private Subparser createServerArgumentsParser(Subparsers subParsers) {
    Subparser result = subParsers.addParser("server")
        .help("Start TCP socket server");
    result.addArgument("-p", "--port").required(true)
        .help("Port to be assigned to the server.\n");
    // specify language (for language dependent treatment of apostrophes)
    result.addArgument("-l", "--lang")
        .choices("ca" ,"de", "en", "es", "eu", "fr", "gl", "it", "nl").required(true)
        .help(
            "It is REQUIRED to choose a language to perform annotation with ixa-pipe-tok.\n");
    result.addArgument("-n", "--normalize")
        .choices("alpino", "ancora", "ctag", "default", "ptb", "tiger",
            "tutpenn")
        .required(false).setDefault("default").help(
            "Set normalization method according to corpus; the default option does not escape "
                + "brackets or forward slashes. See README for more details.\n");
    result.addArgument("-u", "--untokenizable").choices("yes", "no")
        .setDefault("no").required(false)
        .help("Print untokenizable characters.\n");
    result.addArgument("-o", "--outputFormat")
        .choices("conll", "oneline", "naf").setDefault("naf").required(false)
        .help("Choose output format; it defaults to NAF.\n");
    result.addArgument("--offsets").action(Arguments.storeFalse()).help(
        "Do not print offset and lenght information of tokens in CoNLL format.\n");
    result.addArgument("--inputkaf").action(Arguments.storeTrue()).help(
        "Use this option if input is a KAF/NAF document with <raw> layer.\n");
    result.addArgument("--notok").action(Arguments.storeTrue()).help(
        "Build a KAF document from an already tokenized sentence per line file.\n");
    result.addArgument("--hardParagraph").choices("yes", "no").setDefault("no")
        .required(false).help("Do not segment paragraphs. Ever.\n");
    result.addArgument("--kafversion").setDefault("v1.naf")
        .help("Set kaf document version.\n");

    return result;
  }

  private Subparser createClientArgumentsParser(Subparsers subParsers) {
    Subparser result = subParsers.addParser("client")
        .help("Send queries to the TCP socket server");

    result.addArgument("-p", "--port").required(true)
        .help("Port of the TCP server.\n");
    result.addArgument("--host").required(false).setDefault("localhost")
        .help("Hostname or IP where the TCP server is running.\n");

    return result;
  }
}
