package eu.clarin.cmdi.linkchecker;

import eu.clarin.cmdi.linkchecker.threads.CollectionThreadManager;
import eu.clarin.cmdi.linkchecker.helpers.Configuration;
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
        try {
            manager.start();
        } catch (Exception e) {
            _logger.error("There has been an error and linkchecker is down. Here is the reason: ",e);
        }


    }

}
