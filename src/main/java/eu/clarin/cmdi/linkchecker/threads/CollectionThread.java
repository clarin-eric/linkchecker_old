package eu.clarin.cmdi.linkchecker.threads;

import eu.clarin.cmdi.linkchecker.httpLinkChecker.HTTPLinkChecker;
import eu.clarin.cmdi.rasa.links.CheckedLink;
import eu.clarin.cmdi.rasa.links.LinkToBeChecked;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.util.concurrent.ConcurrentLinkedQueue;

import static eu.clarin.cmdi.linkchecker.helpers.Configuration.checkedLinkResource;

public class CollectionThread extends Thread {

    private final static Logger _logger = LoggerFactory.getLogger(CollectionThread.class);

    public ConcurrentLinkedQueue<LinkToBeChecked> urlQueue = new ConcurrentLinkedQueue<>();

    private long CRAWLDELAY;

    public CollectionThread(String name, long CRAWLDELAY) {
        super(name);
        this.CRAWLDELAY = CRAWLDELAY;
    }

    @Override
    public void run() {


        //before starting the url check, wait 60 seconds for the queue of this thread to get populated by the httplinkchecker
        //i do this because if we don't wait and let the thread run for only one url, i'm afraid the thread
        // will be closed after one url check and it will be necessary to create a new thread for each url, which
        //is not the aim of this multithreading.
        _logger.info("Waiting 1 minute for url queues to be filled for collection " + getName() + "...");
        synchronized (this) {
            try {
                wait(60000);
            } catch (InterruptedException e) {
                _logger.error("Waiting for thread " + getName() + " interrupted");
            }
        }
        _logger.info("Done waiting for " + getName() + ".");

        long startTime;

        //name of the thread is also name of the collection
        String collection = getName();

        LinkToBeChecked linkToBeChecked;

        HTTPLinkChecker httpLinkChecker = new HTTPLinkChecker();
        while ((linkToBeChecked = urlQueue.poll()) != null) {

            CheckedLink checkedLink;

            String url = linkToBeChecked.getUrl();

            try {


                checkedLink = httpLinkChecker.checkLink(url, 0, 0, url);

                startTime = System.currentTimeMillis();
                checkedLink.setCollection(collection);
                checkedLink.setRecord(linkToBeChecked.getRecord());
                checkedLink.setExpectedMimeType(linkToBeChecked.getExpectedMimeType() == null ? "" : linkToBeChecked.getExpectedMimeType());


                //better not to have it in log file, because it makes it unreadable
//                _logger.info("Successfully checked link: "+ url);

            } catch (IOException | IllegalArgumentException e) {
                startTime = System.currentTimeMillis();

                //better not to have it in log file
//                _logger.error("There is an error with the URL: " + url + " . It is not being checked.");

                checkedLink = new CheckedLink(url, "Not checked", e.getMessage() + " for URL: " + url, 0,
                        "Not specified", "0", 0, System.currentTimeMillis(), collection, 0, null, "");

                checkedLink.setCollection(collection);
                checkedLink.setRecord(linkToBeChecked.getRecord());

            }

            Boolean saved = checkedLinkResource.save(checkedLink);
            if (!saved) {
                _logger.error("Could not save url: " + checkedLink.getUrl());
            }

            long estimatedTime = System.currentTimeMillis() - startTime;


            //I measure the time it takes to handle the mongodb operations.
            //If it takes longer than the crawldelay, we go to the next url.
            //If not, then we wait CRAWLDELAY-estimatedTime.
            //This is done, so that we don't lose
            //extra time with database operations.
            if (estimatedTime < CRAWLDELAY) {
                try {
                    sleep(CRAWLDELAY - estimatedTime);
                } catch (InterruptedException e) {
                    //do nothing, shouldn't get interrupted
                }
            }


        }


    }

}
