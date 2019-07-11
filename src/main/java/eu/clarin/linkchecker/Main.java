package eu.clarin.linkchecker;

import eu.clarin.linkchecker.helpers.Configuration;
import eu.clarin.linkchecker.threads.CollectionThreadManager;
import org.apache.commons.cli.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;


public class Main {

    private final static Logger _logger = LoggerFactory.getLogger(Main.class);

    public static void main(String[] args) throws ParseException {

        // create Options object
        Options options = new Options();

        // add t option
        options.addOption(Option.builder("config")
                .required(true)
                .hasArg(true)
                .build());

        CommandLineParser parser = new DefaultParser();
        CommandLine cmd = parser.parse(options, args);

        if (!cmd.hasOption("config")) {

            _logger.error("Usage: Please provide the config file path as a parameter.");
            System.exit(1);

        }

        Configuration.loadConfigVariables(cmd.getOptionValue("config"));

        CollectionThreadManager manager = new CollectionThreadManager();

        manager.start();

    }

}
