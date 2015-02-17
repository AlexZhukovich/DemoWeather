package com.alexzh.demoweather;

import android.content.Context;
import android.database.Cursor;
import android.database.DataSetObserver;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.AsyncTask;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;
import com.alexzh.demoweather.data.WeatherContract;

import java.io.InputStream;

public class WeatherTimeAdapter extends RecyclerView.Adapter<WeatherTimeAdapter.ViewHolder> {

    private static Context mContext;
    private static Cursor mCursor;
    private boolean mDataValid;
    private int mRowIdColumn;
    private DataSetObserver mDataSetObserver;

    public WeatherTimeAdapter(Context context, Cursor cursor) {
        mContext = context;
        mCursor = cursor;
        mDataValid = mCursor != null;
        mRowIdColumn = mDataValid ? mCursor.getColumnIndex(WeatherContract.WeatherEntry._ID) : -1;
        mDataSetObserver = new NotifyingDataSetObserver();
        if (mCursor != null)
            mCursor.registerDataSetObserver(mDataSetObserver);
    }

    public Cursor getCursor() {
        return mCursor;
    }

    @Override
    public int getItemCount() {
        if (mDataValid && mCursor != null) {
            return mCursor.getCount();
        }
        return 0;
    }

    @Override
    public long getItemId(int position) {
        if (mDataValid && mCursor != null && mCursor.moveToPosition(position))
            return mCursor.getLong(mRowIdColumn);
        return 0;
    }

    @Override
    public void setHasStableIds(boolean hasStableIds) {
        super.setHasStableIds(hasStableIds);
    }

    @Override
    public ViewHolder onCreateViewHolder(ViewGroup viewGroup, int viewType) {
        View mView = LayoutInflater.from(viewGroup.getContext()).inflate(
                R.layout.item_detailed_weather,
                viewGroup,
                false);

        return new ViewHolder(mView);
    }


    @Override
    public void onBindViewHolder(ViewHolder holder, int position) {
        mCursor.moveToPosition(position);
        String date = mCursor.getString(DetailFragment.COL_DATE);
        double maxTemp = mCursor.getDouble(DetailFragment.COL_MAX_TEMP);
        double minTemp = mCursor.getDouble(DetailFragment.COL_MIN_TEMP);
        double humidity = mCursor.getDouble(DetailFragment.COL_HUMIDITY);
        double pressure = mCursor.getDouble(DetailFragment.COL_PRESSURE);
        double wind = mCursor.getDouble(DetailFragment.COL_WIND);
        String description = mCursor.getString(DetailFragment.COL_DESCRIPTION);

        holder.dayTextView.setText(Utility.getFriendlyDayString(mContext, date));
        holder.dateTextView.setText(Utility.getFormattedMonthDay(date)
                + " (" + Utility.getTime(date) + ")");
        holder.maxTempTextView.setText(Utility.formatTemperature(mContext, maxTemp,
                Utility.isMetric(mContext)));
        holder.minTempTextView.setText(Utility.formatTemperature(mContext,minTemp,
                Utility.isMetric(mContext)));
        holder.humidityTextView.setText(Utility.formatHumidity(mContext, humidity));
        holder.pressureTextView.setText(Utility.formatPressure(mContext, pressure));
        holder.windTextView.setText(Utility.formatWindSpeed(mContext, wind));

        String url = Utility.getWeatherImageUrl(description);
        if (url != null)
            new DownloadImage(holder.descriptionImageView).execute(url);
    }

    public static class ViewHolder extends RecyclerView.ViewHolder {
        private TextView dayTextView;
        private TextView dateTextView;
        private TextView maxTempTextView;
        private TextView minTempTextView;
        private TextView humidityTextView;
        private TextView pressureTextView;
        private TextView windTextView;
        private ImageView descriptionImageView;

        public ViewHolder(View itemView) {
            super(itemView);

            dayTextView = (TextView) itemView.findViewById(R.id.detail_day_textview);
            dateTextView = (TextView) itemView.findViewById(R.id.detail_date_textview);
            maxTempTextView = (TextView) itemView.findViewById(R.id.detail_high_textview);
            minTempTextView = (TextView) itemView.findViewById(R.id.detail_low_textview);
            humidityTextView = (TextView) itemView.findViewById(R.id.detail_humidity_textview);
            pressureTextView = (TextView) itemView.findViewById(R.id.detail_pressure_textview);
            windTextView = (TextView) itemView.findViewById(R.id.detail_wind_textview);
            descriptionImageView = (ImageView) itemView.findViewById(R.id.detail_imageview);

            itemView.setTag(itemView);
        }
    }

    public Cursor swapCursor(Cursor newCursor) {
        if (newCursor == mCursor) {
            return null;
        }

        final Cursor oldCursor = mCursor;
        if (oldCursor != null && mDataSetObserver != null) {
            oldCursor.unregisterDataSetObserver(mDataSetObserver);
        }

        mCursor = newCursor;
        if (mCursor != null) {
            if (mDataSetObserver != null) {
                mCursor.registerDataSetObserver(mDataSetObserver);
            }
            mRowIdColumn = newCursor.getColumnIndexOrThrow(WeatherContract.WeatherEntry._ID);
            mDataValid = true;
            notifyDataSetChanged();
        } else {
            mRowIdColumn = -1;
            mDataValid = false;
            notifyDataSetChanged();
        }
        return oldCursor;
    }

    private class NotifyingDataSetObserver extends DataSetObserver {

        @Override
        public void onChanged() {
            super.onChanged();
            mDataValid = true;
            notifyDataSetChanged();
        }

        @Override
        public void onInvalidated() {
            super.onInvalidated();
            mDataValid = false;
            notifyDataSetChanged();
        }
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