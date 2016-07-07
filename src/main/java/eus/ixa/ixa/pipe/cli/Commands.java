package eus.ixa.ixa.pipe.cli;

public class Commands {
    public static Command parse(String subcommandString) {
        if (subcommandString.equalsIgnoreCase("tok")) {
            return Command.TOKENIZE;
        } else if (subcommandString.equals("server")) {
            return Command.SERVER;
        } else if (subcommandString.equals("client")) {
            return Command.SERVER;
        } else {
            return Command.UNKNOWN;
        }
    }
}
