package com.dev.bochkarev.myapplication;

import android.os.AsyncTask;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.text.TextUtils;
import android.util.Log;
import android.util.Xml;
import android.view.View;
import android.widget.Button;
import android.widget.Toast;

import org.xmlpull.v1.XmlPullParser;
import org.xmlpull.v1.XmlPullParserException;

import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;

public class MainActivity extends AppCompatActivity{

    private SwipeRefreshLayout _swipeLayout;
    private Button _loadRssButton;
    private List<RssModel> _rssModels;
    private RecyclerView _rssRecycleView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        _swipeLayout = (SwipeRefreshLayout) findViewById(R.id.swipeLayout);
        _loadRssButton = (Button) findViewById(R.id.load_rss_button);
        _rssRecycleView = (RecyclerView) findViewById(R.id.rssRecycleVIew);

        LinearLayoutManager llm = new LinearLayoutManager(this);
        llm.setOrientation(LinearLayoutManager.VERTICAL);
        _rssRecycleView.setLayoutManager(llm);
        _rssRecycleView.setAdapter( new RecycleViewRssAdapter(_rssModels) );

        _loadRssButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                new RssLoader().execute((Void) null);
            }
        });
        _swipeLayout.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
            @Override
            public void onRefresh() {
                new RssLoader().execute((Void) null);
            }
        });
    }

    public List<RssModel> parseFeed(InputStream inputStream) throws XmlPullParserException, IOException {
        String title = null;
        String link = null;
        String pubDate = null;
        boolean isItem = false;
        List<RssModel> items = new ArrayList<>();

        try {
            XmlPullParser xmlPullParser = Xml.newPullParser();
            xmlPullParser.setFeature(XmlPullParser.FEATURE_PROCESS_NAMESPACES, false);
            xmlPullParser.setInput(inputStream, null);

            xmlPullParser.nextTag();
            while (xmlPullParser.next() != XmlPullParser.END_DOCUMENT) {
                int eventType = xmlPullParser.getEventType();

                String name = xmlPullParser.getName();
                if(name == null)
                    continue;

                if(eventType == XmlPullParser.END_TAG) {
                    if(name.equalsIgnoreCase("item")) {
                        isItem = false;
                    }
                    continue;
                }

                if (eventType == XmlPullParser.START_TAG) {
                    if(name.equalsIgnoreCase("item")) {
                        isItem = true;
                        continue;
                    }
                }

                //Log.d("MainActivity", "Parsing name ==> " + name);
                String result = "";
                if (xmlPullParser.next() == XmlPullParser.TEXT) {
                    result = xmlPullParser.getText();
                    xmlPullParser.nextTag();
                }

                if (name.equalsIgnoreCase("title")) {
                    title = result;
                } else if (name.equalsIgnoreCase("link")) {
                    link = result;
                } else if (name.equalsIgnoreCase("pubDate")) {
                    pubDate = result;
                }

                Log.d("MainActivity", "Parsing name ==> " + result);
                if (title != null && link != null && pubDate != null) {
                    if(isItem) {
                        RssModel item = new RssModel(title, link, pubDate);
                        items.add(item);
                    }
                    else {
                        /*
                        mFeedTitle = title;
                        mFeedLink = link;
                        mFeedDescription = pubDate;*/
                    }

                    title = null;
                    link = null;
                    pubDate = null;
                    isItem = false;
                }
            }

            return items;
        } finally {
            inputStream.close();
        }
    }


    private class RssLoader extends AsyncTask<Void, Void, Boolean> {
        @Override
        protected Boolean doInBackground(Void... params) {
            if ("http://feeds.bbci.co.uk/news/business/rss.xml".isEmpty())
                return false;

            try {
                URL url = new URL("http://feeds.bbci.co.uk/news/business/rss.xml");
                InputStream inputStream = url.openConnection().getInputStream();
                _rssModels = parseFeed(inputStream);
                return true;
            } catch (IOException e) {
                Log.e("MainActivity", "IO error", e);
            } catch (XmlPullParserException e) {
                Log.e("MainActivity", "XML parse error", e);
            }
            return false;
        }

        @Override
        protected void onPreExecute() {
            //_swipeLayout.setRefreshing(true);
            Toast.makeText(MainActivity.this,
                    "Trying to load Yabdex Games RSS",
                    Toast.LENGTH_LONG).show();
        }

        @Override
        protected void onPostExecute(Boolean success) {
            //_swipeLayout.setRefreshing(false);

            if (success) {
                _rssRecycleView.setAdapter(new RecycleViewRssAdapter(_rssModels));
            } else {
                Toast.makeText(MainActivity.this,
                        "Yandex Games RSS are not available now. Check your internet connection",
                        Toast.LENGTH_LONG).show();
            }
        }
    }
}
