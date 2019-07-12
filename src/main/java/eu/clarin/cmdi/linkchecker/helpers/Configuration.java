package eu.clarin.cmdi.linkchecker.helpers;

import com.mongodb.client.MongoClient;
import com.mongodb.client.MongoClients;
import com.mongodb.client.MongoDatabase;
import eu.clarin.cmdi.rasa.helpers.RasaFactory;
import eu.clarin.cmdi.rasa.helpers.impl.ACDHRasaFactory;
import eu.clarin.cmdi.rasa.linkResources.CheckedLinkResource;
import eu.clarin.cmdi.rasa.linkResources.LinkToBeCheckedResource;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.FileInputStream;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import java.util.Properties;

public class Configuration {

    private static Properties properties = new Properties();
    private final static Logger _logger = LoggerFactory.getLogger(Configuration.class);

    public static int TIMEOUT;
    public static int REDIRECT_FOLLOW_LIMIT;
    public static String USERAGENT;
    public static long CRAWLDELAY;
    public static Map<String, Long> CRAWLDELAYMAP = new HashMap<>();

    public static CheckedLinkResource checkedLinkResource;
    public static LinkToBeCheckedResource linkToBeCheckedResource;

    public static void loadConfigVariables(String configPath) {
        try {
            properties.load(new FileInputStream(configPath));
        } catch (IOException e) {

            _logger.error("Can't load properties file: " + e.getMessage());
            System.exit(1);
        }

        TIMEOUT = Integer.parseInt(properties.getProperty("TIMEOUT"));
        REDIRECT_FOLLOW_LIMIT = Integer.parseInt(properties.getProperty("REDIRECT_FOLLOW_LIMIT"));
        USERAGENT = properties.getProperty("USERAGENT");
        CRAWLDELAY = Long.parseLong(properties.getProperty("CRAWLDELAY"));

        String crawlDelayList = properties.getProperty("CRAWLDELAYLIST");
        for (String crawlDelayEntry : crawlDelayList.split(",")) {
            String collection = crawlDelayEntry.split("=")[0];
            long delay = Long.parseLong(crawlDelayEntry.split("=")[1]);
            CRAWLDELAYMAP.put(collection, delay);
        }


        RasaFactory factory = new ACDHRasaFactory(properties.getProperty("DATABASE_NAME"),properties.getProperty("DATABASE_URI"));
        checkedLinkResource = factory.getCheckedLinkResource();
        linkToBeCheckedResource = factory.getLinkToBeCheckedResource();

    }

}
