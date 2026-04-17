package com.ais.cafeteria.pos.models;

public class AisNewsItem {

    private final String tag;
    private final String title;
    private final String summary;
    private final String publishedDate;
    private final String articleUrl;

    public AisNewsItem(String tag, String title, String summary, String publishedDate, String articleUrl) {
        this.tag = tag;
        this.title = title;
        this.summary = summary;
        this.publishedDate = publishedDate;
        this.articleUrl = articleUrl;
    }

    public String getTag() {
        return tag;
    }

    public String getTitle() {
        return title;
    }

    public String getSummary() {
        return summary;
    }

    public String getPublishedDate() {
        return publishedDate;
    }

    public String getArticleUrl() {
        return articleUrl;
    }
}
