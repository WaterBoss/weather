package com.example.qcy.weather;

import android.app.Activity;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ScrollView;
import android.widget.TextView;
import android.widget.Toast;

import com.bumptech.glide.Glide;
import com.example.qcy.com.example.qcy.util.AllUtil;
import com.example.qcy.com.example.qcy.util.MyApplication;
import com.example.qcy.javabean.Forecast;
import com.example.qcy.javabean.Weather;
import com.example.qcy.net.UtilityNet;
import com.example.qcy.net.UtilityNet.MCallBack;

/**
 * 天气详情页面
 */
public class WeatherActivity extends BaseActivity {

    private static final String TAG = "WeatherActivity";
    private ScrollView mWeatherLayout;
    private TextView mTitleCity;
    private TextView mTitleUpadateTime;
    private TextView mDegreeText;
    private TextView mWeatherInfo;
    private LinearLayout mForeCast;
    private TextView mApiText;
    private TextView mPm25Text;
    private TextView mComforText;
    private TextView mCarWashText;
    private TextView mSportText;
    private ImageView mBackgroundImage;

    //打开WeatherActivity页面
    public static void startActivity(Activity activity, String parmaStr) {
        Intent intent = new Intent(activity, WeatherActivity.class);
        intent.putExtra("weather_id", parmaStr);
        activity.startActivity(intent);
    }


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_weather);
        MyApplication.getInstance().setActivity(this);
        mBackgroundImage = (ImageView) findViewById(R.id.background_imagev);
        mWeatherLayout = (ScrollView) findViewById(R.id.weather_scroll);
        mTitleCity = (TextView) findViewById(R.id.title_city);
        mTitleUpadateTime = (TextView) findViewById(R.id.update_time);
        mDegreeText = (TextView) findViewById(R.id.wendu_tv);
        mWeatherInfo = (TextView) findViewById(R.id.weather_tv);
        mForeCast = (LinearLayout) findViewById(R.id.forecast_layout);
        mApiText = (TextView) findViewById(R.id.api_tv);
        mPm25Text = (TextView) findViewById(R.id.pm25_tv);
        mComforText = (TextView) findViewById(R.id.comfort_tv);
        mCarWashText = (TextView) findViewById(R.id.car_wash_tv);
        mSportText = (TextView) findViewById(R.id.sport_tv);
        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(this);
        String bingPic = prefs.getString("bing_pic", null);
        if (!TextUtils.isEmpty(bingPic)) {
            Glide.with(this).load(bingPic).into(mBackgroundImage);
        } else {
            LoadPicCallBack loadPicCallBack = new LoadPicCallBack();
            String address = "http://guolin.tech/api/bing_pic";
            UtilityNet.loadBingPic(loadPicCallBack, address);
        }
        String weatheInfo = prefs.getString("weather", null);
        if (!TextUtils.isEmpty(weatheInfo)) {
            //有本地缓存数据,显示天气信息
            Weather weather = UtilityNet.handleWeatherResponse(weatheInfo);
            showWeatherInfo(weather);
            AllUtil.logUtil(TAG, AllUtil.DUBUG_LEVER, "加载缓存数据");
        } else {
            //本地缓存数据为空，向服务端获取数据，再显示数据
            AllUtil.logUtil(TAG, AllUtil.DUBUG_LEVER, "加载服务端数据");
//            String weatherId = getIntent().getStringExtra("weather_id");
            MCallBack mCallBack = new MCallBacK();
            String weatherId = "CN101190401";
            UtilityNet.requestWeather(weatherId, mCallBack);
        }
    }

    @Override
    protected void onStart() {
        super.onStart();
    }

    /**
     * 显示天气信息到界面上
     *
     * @param weather
     */
    private void showWeatherInfo(Weather weather) {
        if (weather == null) {
            return;
        }
        mTitleCity.setText(weather.basic.cityName);
        mTitleUpadateTime.setText(weather.basic.update.updateTime.split("")[1]);
        mDegreeText.setText(weather.now.temperature + "0C");
        mWeatherInfo.setText(weather.now.more.info);
        mForeCast.removeAllViews();
        mForeCast.setVisibility(View.GONE);
        for (Forecast forecast : weather.forecastList) {
            View forecastView = LayoutInflater.from(this).inflate(R.layout.forecast_item, mForeCast, true);
            TextView dataText = (TextView) forecastView.findViewById(R.id.data_tv);
            dataText.setText(forecast.date);
            TextView infoText = (TextView) forecastView.findViewById(R.id.info_tv);
            infoText.setText(forecast.more.info);
            TextView maxText = (TextView) findViewById(R.id.max_tv);
            maxText.setText(forecast.temperature.max);
            TextView minText = (TextView) findViewById(R.id.min_tv);
            minText.setText(forecast.temperature.min);
        }
        if (weather.aqi != null) {
            mApiText.setText(weather.aqi.city.api);
            mPm25Text.setText(weather.aqi.city.pm25);
        }
        String comforStr = "舒适度" + weather.suggestion.comfort.info;
        String carWashStr = "洗车指数" + weather.suggestion.carWash.info;
        String sportStr = "运动建议" + weather.suggestion.sport.info;
        mComforText.setText(comforStr);
        mCarWashText.setText(carWashStr);
        mSportText.setText(sportStr);
        if (!weather.forecastList.isEmpty()) {
            mForeCast.setVisibility(View.VISIBLE);
        }
    }

    private class MCallBacK implements MCallBack {
        @Override
        public void beginQuery() {
            Activity activity = WeatherActivity.this.getParent();
//            AllUtil.showProgressDialog(activity);
        }

        @Override
        public void response() {
            WeatherActivity.this.runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    AllUtil.closeProgressDialog();
                }
            });
        }

        @Override
        public void failure() {
            WeatherActivity.this.runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    Toast.makeText(WeatherActivity.this, "获取天气信息失败！", Toast.LENGTH_SHORT);
                    AllUtil.closeProgressDialog();
                }
            });
        }

        @Override
        public void endQuery() {
        }

        @Override
        public void setMCurrentLever(int i) {

        }

        @Override
        public void response(final Object parma) {
            WeatherActivity.this.runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    Weather weather = (Weather) parma;
                    showWeatherInfo(weather);
                    AllUtil.closeProgressDialog();
                }
            });
        }
    }

    private class LoadPicCallBack implements MCallBack {
        @Override
        public void beginQuery() {

        }

        @Override
        public void response() {
        }

        @Override
        public void failure() {


        }

        @Override
        public void endQuery() {

        }

        @Override
        public void setMCurrentLever(int i) {

        }

        @Override
        public void response(final Object parma) {
            WeatherActivity.this.runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    Glide.with(WeatherActivity.this).load(parma).into(mBackgroundImage);
                }
            });
        }
    }
}
