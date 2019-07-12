package eu.clarin.cmdi.linkchecker.threads;

import eu.clarin.cmdi.linkchecker.helpers.Configuration;
import eu.clarin.cmdi.rasa.helpers.impl.ACDHCheckedLinkFilter;
import eu.clarin.cmdi.rasa.helpers.impl.ACDHLinkToBeCheckedFilter;
import eu.clarin.cmdi.rasa.links.CheckedLink;
import eu.clarin.cmdi.rasa.links.LinkToBeChecked;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import static eu.clarin.cmdi.linkchecker.helpers.Configuration.checkedLinkResource;
import static eu.clarin.cmdi.linkchecker.helpers.Configuration.linkToBeCheckedResource;
import static java.util.concurrent.TimeUnit.MINUTES;

//manage all the threads
//also calculate average values:
//  url per minute
//  url per hour
//  url per day
//...
public class CollectionThreadManager {


    private final static Logger _logger = LoggerFactory.getLogger(CollectionThreadManager.class);

    public void start() {

        //outputs currently running collection threads
        new StatusThread().start();

        //starts all collection threads based on linksToBeChecked
        new Thread(this::startCollectionThreads).start();

        Runnable threadRefiller = this::refillCollectionThreads;

        //every day at 1 am
        long oneAM = LocalDateTime.now().until(LocalDate.now().atTime(1, 0), ChronoUnit.MINUTES);
        ScheduledExecutorService scheduler = Executors.newScheduledThreadPool(1);
        scheduler.scheduleAtFixedRate(threadRefiller, oneAM, TimeUnit.DAYS.toMinutes(1), MINUTES);


    }

    //if the collectionThread is not running, it means it is finished.
    //So we copy all urls of this collection from linksChecked back to linksToBeChecked
    //So every 24 hours we restart finished collection threads.
    private void refillCollectionThreads() {

        _logger.info("Refilling collections...");

        for (String collection : checkedLinkResource.getCollectionNames()) {

            CollectionThread collectionThread = getCollectionThreadByName(collection);

            //if collection thread isn't running, copy all links back to linksToBeChecked
            if (collectionThread == null) {
                _logger.info("Collection: " + collection + " is not running. Copying links to linksToBeChecked and starting a thread...");
                refillLinksToBeChecked(collection);
                CollectionThread t = startCollectionThread(collection);


                Stream<LinkToBeChecked> linksToBeChecked = linkToBeCheckedResource.get(Optional.of(new ACDHLinkToBeCheckedFilter(collection)));

                linksToBeChecked.forEach(linkToBeChecked -> {
                    if (!t.urlQueue.contains(linkToBeChecked)) {
                        t.urlQueue.add(linkToBeChecked);
//                            _logger.info("Added url to collection thread: " + collection);
                    }
                });

                _logger.info("Successfully restarted collection thread: " + collection);

            }

        }


    }

    private void refillLinksToBeChecked(String collection) {

        Stream<CheckedLink> checkedLinks = checkedLinkResource.get(Optional.of(new ACDHCheckedLinkFilter(collection)));

        checkedLinks.forEach(checkedLink -> {
            LinkToBeChecked linkToBeChecked = new LinkToBeChecked(checkedLink);
            linkToBeCheckedResource.save(linkToBeChecked);
        });

    }

    private CollectionThread startCollectionThread(String collection) {
        CollectionThread t;
        //handle specific crawl delay if any
        long crawlDelay;
        if (Configuration.CRAWLDELAYMAP.containsKey(collection)) {
            crawlDelay = Configuration.CRAWLDELAYMAP.get(collection);
            _logger.info("Crawl delay set to: " + crawlDelay + " for collection " + collection);
        } else {
            //should be 0
            crawlDelay = Configuration.CRAWLDELAY;
        }

        t = new CollectionThread(collection, crawlDelay);
        t.start();

        _logger.info("Started collection thread: " + collection);

        return t;
    }


    private void startCollectionThreads() {

        List<String> collections = checkedLinkResource.getCollectionNames();

        for (String collection : collections) {

            Stream<LinkToBeChecked> linksToBeChecked = linkToBeCheckedResource.get(Optional.of(new ACDHLinkToBeCheckedFilter(collection)));

            CollectionThread t = getCollectionThreadByName(collection);

            //start new thread if it doesn't exist already
            if (t == null) {
                t = startCollectionThread(collection);
            }

            for (LinkToBeChecked linkToBeChecked : linksToBeChecked.collect(Collectors.toList())) {
                if (!t.urlQueue.contains(linkToBeChecked)) {
                    t.urlQueue.add(linkToBeChecked);
                    //_logger.info("Added url to collection thread: " + collection);
                }
            }

        }

        _logger.info("Added all links to respective threads.");

    }

    private static CollectionThread getCollectionThreadByName(String threadName) {
        for (Thread t : Thread.getAllStackTraces().keySet()) {
            if (t.getName().equals(threadName) && t.getClass().equals(CollectionThread.class)) {
                return (CollectionThread) t;
            }
        }
        return null;
    }
}
