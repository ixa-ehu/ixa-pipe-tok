/*
 *Copyright 2016, 2018 Rodrigo Agerri

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

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.io.StringReader;
import java.io.UnsupportedEncodingException;
import java.net.Socket;
import java.net.UnknownHostException;
import java.util.Properties;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.jdom2.JDOMException;
import eus.ixa.ixa.pipe.cli.CLIArgumentsParser;
import eus.ixa.ixa.pipe.cli.Parameters;
import eus.ixa.ixa.pipe.cli.Strategy;
import ixa.kaflib.KAFDocument;
import net.sourceforge.argparse4j.inf.ArgumentParserException;

/**
 * ixa-pipe-tok uses the tokenizer API from ixa-pipe-ml. This pipe provides
 * several configuration parameters:
 * <ol>
 * <li>lang: choose language to create the lang attribute in KAF header.
 * <li>normalize: choose normalization method.
 * <li>outputFormat: choose between oneline, conll or NAF as output.
 * <li>untokenizable: print untokenizable (\uFFFD) characters.
 * <li>notok: take already tokenized text as input and create a KAFDocument with
 * it.
 * <li>noseg: tokenize but do not segment sentences.
 * <li>inputkaf: take a NAF Document as input instead of plain text file.
 * <li>kafversion: specify the NAF version as parameter.
 * <li>hardParagraph: never break paragraphs.
 * </ol>
 *
 * @author ragerri
 * @version 2016-04-20
 */
public class CLI {

  private static final Logger LOG = LogManager.getLogger(CLI.class);

  /**
   * Get dynamically the version of ixa-pipe-tok by looking at the MANIFEST
   * file.
   */
  private final static String VERSION = CLI.class.getPackage()
      .getImplementationVersion();
  /**
   * Get the commit of ixa-pipe-tok by looking at the MANIFEST file.
   */
  private final String COMMIT_SHA = CLI.class.getPackage()
      .getSpecificationVersion();

  // create the CLI arguments parser
  private final CLIArgumentsParser cliArgumentsParser;

  public CLI() {
    cliArgumentsParser = new CLIArgumentsParser(VERSION);
  }

  public static void main(final String[] args)
      throws IOException, JDOMException {
    final CLI cmdLine = new CLI();
    cmdLine.run(args);
  }

  /**
   * Run the appropriate command based on the command line parameters.
   *
   * @param args
   *          the arguments passed through the CLI
   * @throws IOException
   *           exception if problems with the incoming data
   * @throws JDOMException
   *           a xml exception
   */
  public final void run(final String[] args) throws IOException, JDOMException {
    try {
      Parameters parameters = cliArgumentsParser.parse(args);
      if (parameters.getStrategy() == Strategy.TOKENIZE) {
        annotate(parameters);
      } else if (parameters.getStrategy() == Strategy.SERVER) {
        server(parameters);
      } else if (parameters.getStrategy() == Strategy.CLIENT) {
        client(parameters);
      } else {
        System.out.println(String.format(
            "Invalid sub-command [%s]. Sub-commands accepted are: (tok|server|client)",
            parameters.getStrategyString()));
      }
    } catch (final ArgumentParserException e) {
      cliArgumentsParser.handleError(e);
      System.out.println("Run java -jar target/ixa-pipe-tok-" + VERSION
          + ".jar (tok|server|client) -help for details");
      System.exit(1);
    }
  }

  public final void annotate(Parameters parameters)
      throws IOException, JDOMException {
    final String outputFormat = parameters.getOutputFormat();
    final String lang = parameters.getLanguage();
    final String kafVersion = parameters.getKafVersion();
    final Boolean inputKafRaw = parameters.getInputRawKaf();
    final Boolean noTok = parameters.getNoTok();
    final Properties properties = parameters.getAnnotateProperties();

    BufferedReader breader = null;
    final BufferedWriter bwriter = new BufferedWriter(
        new OutputStreamWriter(System.out, "UTF-8"));
    KAFDocument kaf;

    if (noTok) {
      final BufferedReader noTokReader = new BufferedReader(
          new InputStreamReader(System.in, "UTF-8"));
      kaf = new KAFDocument(lang, kafVersion);
      final KAFDocument.LinguisticProcessor newLp = kaf.addLinguisticProcessor(
          "text", "ixa-pipe-tok-notok-" + lang, VERSION + "-" + COMMIT_SHA);
      newLp.setBeginTimestamp();
      Annotate.tokensToKAF(noTokReader, kaf);
      newLp.setEndTimestamp();
      bwriter.write(kaf.toString());
      noTokReader.close();
    } else {
      if (inputKafRaw) {
        final BufferedReader kafReader = new BufferedReader(
            new InputStreamReader(System.in, "UTF-8"));
        // read KAF from standard input
        kaf = KAFDocument.createFromStream(kafReader);
        final String text = kaf.getRawText();
        final StringReader stringReader = new StringReader(text);
        breader = new BufferedReader(stringReader);
      } else {
        kaf = new KAFDocument(lang, kafVersion);
        breader = new BufferedReader(new InputStreamReader(System.in, "UTF-8"));
      }
      final Annotate annotator = new Annotate(breader, properties);
      if (outputFormat.equalsIgnoreCase("conll")) {
        if (parameters.getOffsets()) {
          bwriter.write(annotator.tokenizeToCoNLL());
        } else {
          bwriter.write(annotator.tokenizeToCoNLLOffsets());
        }
      } else if (outputFormat.equalsIgnoreCase("oneline")) {
        bwriter.write(annotator.tokenizeToText());
      } else {
        final KAFDocument.LinguisticProcessor newLp = kaf
            .addLinguisticProcessor("text", "ixa-pipe-tok-" + lang,
                VERSION + "-" + COMMIT_SHA);
        newLp.setBeginTimestamp();
        annotator.tokenizeToKAF(kaf);
        newLp.setEndTimestamp();
        bwriter.write(kaf.toString());
      }
      breader.close();
    }
    bwriter.close();
  }

  /**
   * Set up the TCP socket for annotation.
   * 
   * @param parameters the parameters
   */
  public final void server(Parameters parameters) {
    Properties serverProperties = parameters.getServerProperties();
    new RuleBasedTokenizerServer(serverProperties);
  }

  /**
   * The client to query the TCP server for annotation.
   * 
   * @param parameters the parameters
   */
  public final void client(Parameters parameters) {
    String host = parameters.getHost();
    String port = parameters.getPort();
    try (Socket socketClient = new Socket(host, Integer.parseInt(port));
        BufferedReader inFromUser = new BufferedReader(
            new InputStreamReader(System.in, "UTF-8"));
        BufferedWriter outToUser = new BufferedWriter(
            new OutputStreamWriter(System.out, "UTF-8"));
        BufferedWriter outToServer = new BufferedWriter(
            new OutputStreamWriter(socketClient.getOutputStream(), "UTF-8"));
        BufferedReader inFromServer = new BufferedReader(
            new InputStreamReader(socketClient.getInputStream(), "UTF-8"));) {

      // send data to server socket
      StringBuilder inText = new StringBuilder();
      String line;
      while ((line = inFromUser.readLine()) != null) {
        inText.append(line).append("\n");
      }
      inText.append("<ENDOFDOCUMENT>").append("\n");
      outToServer.write(inText.toString());
      outToServer.flush();

      // get data from server
      StringBuilder sb = new StringBuilder();
      String kafString;
      while ((kafString = inFromServer.readLine()) != null) {
        sb.append(kafString).append("\n");
      }
      outToUser.write(sb.toString());
    } catch (UnsupportedEncodingException e) {
      // this cannot happen but...
      throw new AssertionError("UTF-8 not supported");
    } catch (UnknownHostException e) {
      LOG.error("ERROR: Unknown hostname or IP address!");
      System.exit(1);
    } catch (NumberFormatException e) {
      LOG.error("Port number not correct!");
      System.exit(1);
    } catch (IOException e) {
      LOG.error("Exception", e);
    }
  }
}
