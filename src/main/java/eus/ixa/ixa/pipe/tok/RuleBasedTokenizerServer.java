/*
 *  Copyright 2015 Rodrigo Agerri

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

import ixa.kaflib.KAFDocument;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.io.StringReader;
import java.io.UnsupportedEncodingException;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.Properties;

import org.jdom2.JDOMException;

public class RuleBasedTokenizerServer {
  
  /**
   * Get dynamically the version of ixa-pipe-tok by looking at the MANIFEST
   * file.
   */
  private final String version = CLI.class.getPackage().getImplementationVersion();
  /**
   * Get the git commit of the ixa-pipe-tok compiled by looking at the MANIFEST
   * file.
   */
  private final String commit = CLI.class.getPackage().getSpecificationVersion();
  
  /**
   * Construct a RuleBasedTokenizer server.
   * 
   * @param properties
   *          the properties
   */
  public RuleBasedTokenizerServer(Properties properties) {

    Integer port = Integer.parseInt(properties.getProperty("port"));
    String result;
    ServerSocket socketServer = null;
    Socket activeSocket;
    BufferedReader inFromClient = null;
    BufferedWriter outToClient = null;

    try {
      System.out.println("-> Trying to listen port... " + port);
      socketServer = new ServerSocket(port);
      System.out.println("-> Connected and listening to port " + port);
      while (true) {
        try {
          activeSocket = socketServer.accept();
          inFromClient = new BufferedReader(new InputStreamReader(activeSocket.getInputStream(), "UTF-8"));
          outToClient = new BufferedWriter(new OutputStreamWriter(activeSocket.getOutputStream(), "UTF-8"));
          //get data from client
          String stringFromClient = getClientData(inFromClient);
          // annotate
          result = getAnnotations(properties, stringFromClient);
        } catch (JDOMException e) {
          result = "\n-> ERROR: Badly formatted NAF document!!\n";
          sendDataToClient(outToClient, result);
          continue;
        } catch (UnsupportedEncodingException e) {
          result = "\n-> ERROR: Encoding not valid UTF-8!!\n";
          sendDataToClient(outToClient, result);
          continue;
        } catch (IOException e) {
          result = "\n -> ERROR: Input data not correct!!\n";
          sendDataToClient(outToClient, result);
          continue;
        }
        //send data to server after all exceptions and close the outToClient
        sendDataToClient(outToClient, result);
        //close the resources
        inFromClient.close();
        activeSocket.close();
      } //end of processing block
    } catch (IOException e) {
      e.printStackTrace();
      System.err.println("-> IOException due to failing to create the TCP socket or to wrongly provided model path.");
    } finally {
      System.out.println("closing tcp socket...");
      try {
        socketServer.close();
      } catch (IOException e) {
        e.printStackTrace();
      }
    }
  }
  
  /**
   * Read data from the client and output to a String.
   * @param inFromClient the client inputstream
   * @return the string from the client
   */
  private String getClientData(BufferedReader inFromClient) {
    StringBuilder stringFromClient = new StringBuilder();
    try {
      String line;
      while ((line = inFromClient.readLine()) != null) {
        if (line.matches("<ENDOFDOCUMENT>")) {
          break;
        }
        stringFromClient.append(line).append("\n");
        if (line.matches("</NAF>")) {
          break;
        }
      }
    }catch (IOException e) {
      e.printStackTrace();
    }
    return stringFromClient.toString();
  }
  
  /**
   * Send data back to server after annotation.
   * @param outToClient the outputstream to the client
   * @param kafToString the string to be processed
   * @throws IOException if io error
   */
  private void sendDataToClient(BufferedWriter outToClient, String kafToString) throws IOException {
    outToClient.write(kafToString);
    outToClient.close();
  }
  
  /**
   * Get tokens.
   * @param properties the options
   * @param stringFromClient the original string
   * @return the tokenized string
   * @throws IOException if io problems
   * @throws JDOMException if NAF problems
   */
  private String getAnnotations(Properties properties, String stringFromClient) throws IOException, JDOMException {
    
    BufferedReader breader;
    KAFDocument kaf;
    String kafString = null;
    String lang = properties.getProperty("language");
    String outputFormat = properties.getProperty("outputFormat");
    Boolean inputKafRaw = Boolean.valueOf(properties.getProperty("inputkaf"));
    Boolean noTok = Boolean.valueOf(properties.getProperty("notok"));
    String kafVersion = properties.getProperty("kafversion");
    Boolean offsets = Boolean.valueOf(properties.getProperty("offsets"));
    if (noTok) {
      final BufferedReader noTokReader = new BufferedReader(new StringReader(stringFromClient));
      kaf = new KAFDocument(lang, kafVersion);
      final KAFDocument.LinguisticProcessor newLp = kaf.addLinguisticProcessor(
          "text", "ixa-pipe-tok-notok-" + lang, version + "-" + commit);
      newLp.setBeginTimestamp();
      Annotate.tokensToKAF(noTokReader, kaf);
      newLp.setEndTimestamp();
      kafString = kaf.toString();
      noTokReader.close();
    } else {
      if (inputKafRaw) {
        final BufferedReader kafReader = new BufferedReader(new StringReader(stringFromClient));
        kaf = KAFDocument.createFromStream(kafReader);
        final String text = kaf.getRawText();
        final StringReader stringReader = new StringReader(text);
        breader = new BufferedReader(stringReader);
      } else {
        kaf = new KAFDocument(lang, kafVersion);
        breader = new BufferedReader(new StringReader(stringFromClient));
      }
      final Annotate annotator = new Annotate(breader, properties);
      if (outputFormat.equalsIgnoreCase("conll")) {
        if (offsets) {
          kafString = annotator.tokenizeToCoNLL();
        } else {
          kafString = annotator.tokenizeToCoNLLOffsets();
        }
      } else if (outputFormat.equalsIgnoreCase("oneline")) {
        kafString = annotator.tokenizeToText();
      } else {
        final KAFDocument.LinguisticProcessor newLp = kaf
            .addLinguisticProcessor("text", "ixa-pipe-tok-" + lang, version
                + "-" + commit);
        newLp.setBeginTimestamp();
        annotator.tokenizeToKAF(kaf);
        newLp.setEndTimestamp();
        kafString = kaf.toString();
      }
      breader.close();
    }
    return kafString;
  }

}

