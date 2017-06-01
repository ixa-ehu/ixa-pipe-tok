package eus.ixa.ixa.pipe.cli;

import net.sourceforge.argparse4j.inf.Namespace;

import java.util.Properties;

public class Parameters {
  private final Namespace namespace;
  private final String strategyString;
  private final Strategy strategy;

  public Parameters(String strategyString, Strategy strategy,
      Namespace namespace) {
    this.strategyString = strategyString;
    this.strategy = strategy;
    this.namespace = namespace;
  }

  public Parameters(String strategyString, Strategy strategy) {
    this(strategyString, strategy, null);
  }

  public String getStrategyString() {
    return this.strategyString;
  }

  public Strategy getStrategy() {
    return this.strategy;
  }

  public String getOutputFormat() {
    return this.namespace.getString("outputFormat");
  }

  public String getNormalize() {
    return this.namespace.getString("normalize");
  }

  public String getLanguage() {
    return this.namespace.getString("lang");
  }

  public String getUntokenizable() {
    return this.namespace.getString("untokenizable");
  }

  public String getKafVersion() {
    return this.namespace.getString("kafversion");
  }

  public boolean getInputRawKaf() {
    return namespace.getBoolean("inputkaf");
  }

  public boolean getNoTok() {
    return namespace.getBoolean("notok");
  }

  public boolean getNoSeg() {
    return namespace.getBoolean("noseg");
  }

  public String getHardParagraph() {
    return namespace.getString("hardParagraph");
  }

  public boolean getOffsets() {
    return namespace.getBoolean("offsets");
  }

  public String getPort() {
    return namespace.getString("port");
  }

  public String getHost() {
    return namespace.getString("host");
  }

  public Properties getAnnotateProperties() {
    Properties annotateProperties = new Properties();
    annotateProperties.setProperty("language", getLanguage());
    annotateProperties.setProperty("normalize", getNormalize());
    annotateProperties.setProperty("untokenizable", getUntokenizable());
    annotateProperties.setProperty("hardParagraph", getHardParagraph());
    annotateProperties.setProperty("noseg", String.valueOf(getNoSeg()));

    return annotateProperties;
  }

  public Properties getServerProperties() {
    final Properties serverProperties = new Properties();
    serverProperties.setProperty("port", getPort());
    serverProperties.setProperty("language", getLanguage());
    serverProperties.setProperty("normalize", getNormalize());
    serverProperties.setProperty("untokenizable", getUntokenizable());
    serverProperties.setProperty("kafversion", getKafVersion());
    serverProperties.setProperty("inputkaf", String.valueOf(getInputRawKaf()));
    serverProperties.setProperty("notok", String.valueOf(getNoTok()));
    serverProperties.setProperty("outputFormat", getOutputFormat());
    serverProperties.setProperty("offsets", String.valueOf(getOffsets()));
    serverProperties.setProperty("hardParagraph", getHardParagraph());

    return serverProperties;
  }
}
