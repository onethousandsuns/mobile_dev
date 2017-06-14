package com.dev.bochkarev.myapplication;

import android.os.AsyncTask;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.util.Xml;
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
    private List<RssModel> _rssModels;
    private RecyclerView _rssRecycleView;

    private InputStream _inputStream;
    private XmlPullParser _xmlPullParser;
    private LinearLayoutManager _linearLayoutManager;

    private static int NEWS_SCROOL_SPEED = 10;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        Toast.makeText(MainActivity.this,
                R.string.swipe_down,
                Toast.LENGTH_LONG).show();

        _inputStream = null;
        _xmlPullParser = null;

        _swipeLayout = (SwipeRefreshLayout) findViewById(R.id.swipeLayout);
        _rssRecycleView = (RecyclerView) findViewById(R.id.rssRecycleVIew);

        _linearLayoutManager = new LinearLayoutManager(this);
        _linearLayoutManager.setOrientation(LinearLayoutManager.VERTICAL);
        _rssRecycleView.setLayoutManager(_linearLayoutManager);
        _rssRecycleView.setAdapter( new RecycleViewRssAdapter(_rssModels) );

        setOnScrollListener();

        _swipeLayout.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
            @Override
            public void onRefresh() {
                _rssRecycleView.setRecycledViewPool(new RecyclerView.RecycledViewPool());
                _rssRecycleView.clearOnScrollListeners();

                new restartRssFeedTask().execute();
            }
        });
    }

    private class restartRssFeedTask extends AsyncTask<Void , Void, Boolean> {
        protected Boolean doInBackground(Void... params) {
            try{
                if (_rssModels != null){
                    _rssModels.clear();
                }
                if (_inputStream != null){
                    try {
                        _inputStream.close();
                        _inputStream = null;
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                }
                if (_xmlPullParser != null){
                    _xmlPullParser = null;
                }
                setOnScrollListener();
                Log.d("RefreshListener", "User refreshed rss feed");
                loadNextDataFromApi();
                return true;
            } catch (Exception e){
                return false;
            }
        }

        @Override
        protected void onPreExecute() {
            Toast.makeText(MainActivity.this,
                    "Trying to refresh RSS feed ",
                    Toast.LENGTH_SHORT).show();
        }

        @Override
        protected void onPostExecute(Boolean success) {
            if (success) {
                Toast.makeText(MainActivity.this,
                        R.string.rss_success,
                        Toast.LENGTH_SHORT).show();
            } else {
                Toast.makeText(MainActivity.this,
                        R.string.rss_refresh_error,
                        Toast.LENGTH_LONG).show();
            }
        }
    }

    private void loadNextDataFromApi() {
        new RssLoader().execute((Void) null);
    }

    private void setOnScrollListener()
    {
        _rssRecycleView.setOnScrollListener(new EndlessRecyclerOnScrollListener(_linearLayoutManager) {
            @Override
            public void onLoadMore(int current_page) {
                loadNextDataFromApi();
            }
        });
    }

    public List<RssModel> getNextNewsFromRssFeed() throws XmlPullParserException, IOException {
        String title = null;
        String link = null;
        String pubDate = null;
        boolean isItem = false;
        List<RssModel> items = new ArrayList<>();
        int currentNews = 0;

        if (_xmlPullParser == null) {
            _xmlPullParser = Xml.newPullParser();
            _xmlPullParser.setFeature(XmlPullParser.FEATURE_PROCESS_NAMESPACES, false);
            _xmlPullParser.setInput(_inputStream, null);
        }

        _xmlPullParser.nextTag();

        while (currentNews < NEWS_SCROOL_SPEED) {
            if (_xmlPullParser.next() == XmlPullParser.END_DOCUMENT) {
                _inputStream.close();
                Log.d("NewsLoader", "Loaded news count: " + items.size());
                Log.d("NewsLoader", "Reached end of RSS file");
                return items;
            }

            int eventType = _xmlPullParser.getEventType();

            String name = _xmlPullParser.getName();
            if (name == null)
                continue;

            if (eventType == XmlPullParser.END_TAG) {
                if (name.equalsIgnoreCase("item")) {
                    isItem = false;
                }
                continue;
            }

            if (eventType == XmlPullParser.START_TAG) {
                if (name.equalsIgnoreCase("item")) {
                    isItem = true;
                    continue;
                }
            }

            String result = "";
            if (_xmlPullParser.next() == XmlPullParser.TEXT) {
                result = _xmlPullParser.getText();
                _xmlPullParser.nextTag();
            }

            if (name.equalsIgnoreCase("title")) {
                title = result;
            } else if (name.equalsIgnoreCase("link")) {
                link = result;
            } else if (name.equalsIgnoreCase("pubDate")) {
                pubDate = result;
            }

            if (title != null && link != null && pubDate != null) {
                if (isItem) {
                    RssModel item = new RssModel(title, link, pubDate);
                    items.add(item);
                    currentNews++;
                }

                title = null;
                link = null;
                pubDate = null;
                isItem = false;
            }
        }
        Log.d("NewsLoader", "Loaded news count: " + items.size());
        return items;
    }


    private class RssLoader extends AsyncTask<Void, Void, Boolean> {
        @Override
        protected Boolean doInBackground(Void... params) {
            try {
                if (_inputStream == null)
                {
                    URL url = new URL("http://feeds.bbci.co.uk/news/business/rss.xml");
                    _inputStream = url.openConnection().getInputStream();
                }
                if (_rssModels == null){
                    _rssModels = getNextNewsFromRssFeed();
                } else {
                    _rssModels.addAll(getNextNewsFromRssFeed());
                }
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
            _swipeLayout.setRefreshing(true);
        }

        @Override
        protected void onPostExecute(Boolean success) {
            _swipeLayout.setRefreshing(false);

            if (success) {
                _rssRecycleView.setAdapter(new RecycleViewRssAdapter(_rssModels));
            } else {
                Toast.makeText(MainActivity.this,
                        R.string.check_connection,
                        Toast.LENGTH_LONG).show();
            }
        }
    }
}
