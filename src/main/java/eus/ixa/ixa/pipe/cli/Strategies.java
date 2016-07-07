package eus.ixa.ixa.pipe.cli;

public class Strategies {
    public static Strategy parse(String strategyString) {
        if (strategyString.equalsIgnoreCase("tok")) {
            return Strategy.TOKENIZE;
        } else if (strategyString.equals("server")) {
            return Strategy.SERVER;
        } else if (strategyString.equals("client")) {
            return Strategy.CLIENT;
        } else {
            return Strategy.UNKNOWN;
        }
    }
}
