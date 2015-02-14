package com.alexzh.demoweather;

import android.content.Context;
import android.content.Intent;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import java.util.List;

public class WeatherAdapter extends RecyclerView.Adapter<WeatherAdapter.ViewHolder> {

    private static Context mContext;
    private static List<String> mList;

    public WeatherAdapter(Context context, List<String> list) {
            mContext = context;
            mList = list;
    }

    public void clear() {
        mList.clear();
        notifyDataSetChanged();
    }

    public void add(String value) {
        mList.add(value);
        notifyDataSetChanged();
    }

    @Override
    public ViewHolder onCreateViewHolder(ViewGroup viewGroup, int viewType) {
            View mView = LayoutInflater.from(viewGroup.getContext()).inflate(
            R.layout.item_weather,
            viewGroup,
            false);

            return new ViewHolder(mView);
            }


    @Override
    public void onBindViewHolder(ViewHolder holder, int position) {
            holder.nameTextView.setText(mList.get(position));
    }

    @Override
    public int getItemCount() {
            return mList.size();
    }

    public static class ViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener {
        private TextView nameTextView;

        public ViewHolder(View itemView) {
            super(itemView);

            nameTextView = (TextView) itemView.findViewById(R.id.textView);
            itemView.setOnClickListener(this);
            itemView.setTag(itemView);
        }

        @Override
        public void onClick(View v) {
            int endDay = mList.get(getPosition()).indexOf(",");
            Intent detailedIntent = new Intent(mContext, DetailActivity.class);
            detailedIntent.putExtra(DetailFragment.DAY_KEY,
                    mList.get(getPosition()).substring(0, endDay));
            int endDate = mList.get(getPosition()).indexOf("-");
            detailedIntent.putExtra(DetailFragment.DATE_KEY,
                    mList.get(getPosition()).substring(endDay + 2, endDate - 1));
            int endWeather = mList.get(getPosition()).indexOf("(");
            detailedIntent.putExtra(DetailFragment.WEATHER_KEY,
                    mList.get(getPosition()).substring(endDate + 2, endWeather - 1));
            int endHigh = mList.get(getPosition()).indexOf("/");
            detailedIntent.putExtra(DetailFragment.HIGH_KEY,
                    mList.get(getPosition()).substring(endWeather + 2, endHigh));
            int endLow = mList.get(getPosition()).indexOf(")");
            detailedIntent.putExtra(DetailFragment.LOW_KEY,
                    mList.get(getPosition()).substring(endHigh + 2, endLow - 1));

            mContext.startActivity(detailedIntent);

        }
    }
}