package ixa.opennlp.tok;

import java.io.InputStream;

public class Models {

  private InputStream segModel;
  private InputStream tokModel;

  public InputStream getSegModel(String cmdOption) {

    if (cmdOption.equals("en")) {
      segModel = getClass().getResourceAsStream("/en-sent.bin");
    }

    if (cmdOption.equals("es")) {
      segModel = getClass().getResourceAsStream("/es-sent.bin");
    }
    return segModel;
  }

  public InputStream getTokModel(String cmdOption) {

    if (cmdOption.equals("en")) {
      tokModel = getClass().getResourceAsStream("/en-token.bin");
    }

    if (cmdOption.equals("es")) {
      tokModel = getClass().getResourceAsStream("/es-token.bin");
    }
    return tokModel;
  }

}
