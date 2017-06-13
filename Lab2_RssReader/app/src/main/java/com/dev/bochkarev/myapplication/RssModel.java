package com.dev.bochkarev.myapplication;

public class RssModel {
    public String _title;
    public String _link;
    public String _pubDate;

    public RssModel(String title, String link, String pubDate) {
        this._title = title;
        this._link = link;
        this._pubDate = pubDate;
    }
}
