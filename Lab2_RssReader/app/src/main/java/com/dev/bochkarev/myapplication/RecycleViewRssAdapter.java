package com.dev.bochkarev.myapplication;

import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import java.util.List;

public class RecycleViewRssAdapter extends RecyclerView.Adapter<RssModelViewHolder> {
    private List<RssModel> _rssModels;

    public RecycleViewRssAdapter(List<RssModel> rssModels) {
        this._rssModels = rssModels;
    }

    @Override
    public void onBindViewHolder(RssModelViewHolder holder, int position) {
        final RssModel rssModel = _rssModels.get(position);
        ((TextView)holder.rssItemView.findViewById(R.id.titleText)).setText(rssModel._title);
        ((TextView)holder.rssItemView.findViewById(R.id.pubDate)).setText(rssModel._pubDate);
        ((TextView)holder.rssItemView.findViewById(R.id.linkText)).setText(rssModel._link);
    }

    @Override
    public RssModelViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View v = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_rss_feed, parent, false);
        RssModelViewHolder holder = new RssModelViewHolder(v);
        return holder;
    }

    @Override
    public int getItemCount() {
        if (_rssModels == null)
            return 0;
        return _rssModels.size();
    }
}
