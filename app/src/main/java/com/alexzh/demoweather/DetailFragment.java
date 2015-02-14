package com.alexzh.demoweather;

import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.view.MenuItemCompat;
import android.support.v7.widget.ShareActionProvider;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import java.io.InputStream;

public class DetailFragment extends Fragment {
    public static final String DAY_KEY = "day";
    public static final String DATE_KEY = "date";
    public static final String HIGH_KEY = "high";
    public static final String LOW_KEY = "low";
    public static final String WEATHER_KEY = "weather";

    private ShareActionProvider mShareActionProvider;
    private static final String SHARE_HASHTAG = " #DemoWeather";

    public DetailFragment() {
        setHasOptionsMenu(true);
    }

    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        inflater.inflate(R.menu.menu_datail_fragment, menu);

        MenuItem menuItem = menu.findItem(R.id.action_share);
        mShareActionProvider = (ShareActionProvider) MenuItemCompat.getActionProvider(menuItem);
        mShareActionProvider.setShareIntent(createShareForecastIntent());
    }

    private Intent createShareForecastIntent() {
        Intent shareIntent = new Intent(Intent.ACTION_SEND);
        shareIntent.setType("text/plain");
        shareIntent.putExtra(Intent.EXTRA_TEXT, SHARE_HASHTAG);
        return shareIntent;
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View rootView = inflater.inflate(R.layout.fragment_detail, container, false);

        TextView dayTextView = (TextView) rootView.findViewById(R.id.detail_day_textview);
        TextView dateTextView = (TextView) rootView.findViewById(R.id.detail_date_textview);
        TextView highTextView = (TextView) rootView.findViewById(R.id.detail_high_textview);
        TextView lowTextView = (TextView) rootView.findViewById(R.id.detail_low_textview);
        ImageView weatherImageView = (ImageView) rootView.findViewById(R.id.detail_imageview);

        if (getActivity().getIntent().getExtras() != null) {
            dayTextView.setText(getActivity().getIntent().getStringExtra(DAY_KEY));
            dateTextView.setText(getActivity().getIntent().getStringExtra(DATE_KEY));
            highTextView.setText(getActivity().getIntent().getStringExtra(HIGH_KEY));
            lowTextView.setText(getActivity().getIntent().getStringExtra(LOW_KEY));

            String url = Utility.getWeatherImageUrl(getActivity().getIntent().getStringExtra(WEATHER_KEY));
            if (url != null)
                new DownloadImage(weatherImageView).execute(url);
        }
        return rootView;
    }


    private class DownloadImage extends AsyncTask<String, Void, Bitmap> {
        ImageView mImage;

        public DownloadImage(ImageView image) {
            this.mImage = image;
        }

        protected Bitmap doInBackground(String... urls) {
            String url = urls[0];
            Bitmap mIcon = null;
            try {
                InputStream in = new java.net.URL(url).openStream();
                mIcon = BitmapFactory.decodeStream(in);
            } catch (Exception e) {
                Log.e("Error", e.getMessage());
                e.printStackTrace();
            }
            return mIcon;
        }

        protected void onPostExecute(Bitmap result) {
            mImage.setImageBitmap(result);
        }
    }
}