package eu.clarin.linkchecker.urlElements;

import org.apache.log4j.Logger;
import org.bson.Document;

public class URLElement {

    private String url;
    private String method;
    private String message;
    private int status;
    private String contentType;
    private String byteSize;
    private long duration;
    private long timestamp;
    private int redirectCount;
    private String collection;
    private String record;
    private String expectedMimeType;

    public URLElement(Document document) {
        this.url = document.getString("url");
        this.method = document.getString("method");
        this.message = document.getString("message");
        this.status = document.getInteger("status");
        this.contentType = document.getString("contentType");

        this.byteSize = document.getString("byteSize");
        this.duration = document.getLong("duration");
        this.timestamp = document.getLong("timestamp");
        this.collection = document.getString("collection");
        this.record = document.getString("record");
        this.redirectCount = document.getInteger("redirectCount");
        this.expectedMimeType = document.getString("expectedMimeType");
    }

    public URLElement() {
    }

    public URLElement(String url, String method, String message, int status, String contentType, String byteSize, long duration, long timestamp, String collection, int redirectCount, String record, String expectedMimeType) {
        this.url = url;
        this.method = method;
        this.message = message;
        this.status = status;
        this.contentType = contentType;
        this.byteSize = byteSize;
        this.duration = duration;
        this.timestamp = timestamp;
        this.collection = collection;
        this.redirectCount = redirectCount;
        this.record = record;
        this.expectedMimeType = expectedMimeType;
    }

    public String getUrl() {
        return url;
    }

    public void setUrl(String url) {
        this.url = url;
    }

    public String getMethod() {
        return method;
    }

    public void setMethod(String method) {
        this.method = method;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    public int getStatus() {
        return status;
    }

    public void setStatus(int status) {
        this.status = status;
    }

    public String getContentType() {
        //add space to content type, so that html tables can cut it on space to provide more horizontal space
        if(contentType!=null && !contentType.contains("; ")){
            contentType = contentType.replaceFirst(";","; ");
        }
        return contentType;
    }

    public void setContentType(String contentType) {
        this.contentType = contentType;
    }

    public String getByteSize() {
        return byteSize;
    }

    public void setByteSize(String byteSize) {
        this.byteSize = byteSize;
    }

    public long getDuration() {
        return duration;
    }

    public void setDuration(long duration) {
        this.duration = duration;
    }

    public long getTimestamp() {
        return timestamp;
    }

    public void setTimestamp(long timestamp) {
        this.timestamp = timestamp;
    }

    //if you add a new parameter dont forget to add it to this method
    public Document getMongoDocument() {
        Document document = new Document("url", url)
                .append("method", method)
                .append("message", message)
                .append("status", status)
                .append("contentType", contentType)
                .append("byteSize", byteSize)
                .append("duration", duration)
                .append("timestamp", timestamp)
                .append("redirectCount", redirectCount)
                .append("collection", collection)
                .append("record", record)
                .append("expectedMimeType",expectedMimeType);


        return document;
    }

    public String getCollection() {
        return collection;
    }

    public void setCollection(String collection) {
        this.collection = collection;
    }

    public int getRedirectCount() {
        return redirectCount;
    }

    public void setRedirectCount(int redirectCount) {
        this.redirectCount = redirectCount;
    }

    public String getRecord() {
        return record;
    }

    public void setRecord(String record) {
        this.record = record;
    }

    public String getExpectedMimeType() {
        return expectedMimeType;
    }

    public void setExpectedMimeType(String expectedMimeType) {
        this.expectedMimeType = expectedMimeType;
    }
}