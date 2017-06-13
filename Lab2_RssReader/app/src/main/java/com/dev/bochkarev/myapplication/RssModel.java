package com.dev.bochkarev.myapplication;

public class RssModel {
    public String _title;
    public String _link;
//    public String _description;
    public String _pubDate;

    public RssModel(String title, String link, String pubDate) {
        this._title = title;
        this._link = link;
//        this._description = description;
        this._pubDate = pubDate;
    }
}
