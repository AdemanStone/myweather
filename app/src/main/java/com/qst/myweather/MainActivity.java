package com.qst.myweather;

import android.Manifest;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.support.annotation.Nullable;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.amap.api.location.AMapLocation;
import com.amap.api.location.AMapLocationClient;
import com.amap.api.location.AMapLocationClientOption;
import com.amap.api.location.AMapLocationListener;

import org.xmlpull.v1.XmlPullParser;
import org.xmlpull.v1.XmlPullParserFactory;

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.StringReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.Calendar;
import java.util.List;

import static android.content.pm.PackageManager.PERMISSION_GRANTED;

public class MainActivity extends AppCompatActivity implements View.OnClickListener {

    private static final int UPDATE_TODAY_WEATHER = 1;
    public static String changeCityCode = "101210701";
    private List<City> cityList;
    //声明AMapLocationClient类对象
    public AMapLocationClient mLocationClient = null;
    //声明定位回调监听器
    public AMapLocationListener mLocationListener = new MyAMapLocationListener();
    //声明AMapLocationClientOption对象
    public AMapLocationClientOption mLocationOption = null;
    private String cc;

    private Handler mHandler = new Handler() {
        @Override
        public void handleMessage(Message msg) {
            switch (msg.what) {
                case UPDATE_TODAY_WEATHER:
                    updateTodayWeather((TodayWeather) msg.obj);
                    break;
                default:
                    break;
            }
        }
    };

    private void updateTodayWeather(TodayWeather todayWeather) {
        tvCity.setText(todayWeather.getCity() + "天气");
        tvCityName.setText(todayWeather.getCity());
        tvTime.setText("今日" + todayWeather.getUpdatetime() + "发布");
        tvHumudity.setText("湿度：" + todayWeather.getShidu());
        tvClimate.setText(todayWeather.getType());
        tvPmData.setText(todayWeather.getPm25());
        tvPmQuality.setText(todayWeather.getQuality());
        Calendar calendar = Calendar.getInstance();
        tvWeek.setText((calendar.get(Calendar.MONTH) + 1) + "月" + todayWeather.getDate());
        tvWind.setText("风力等级：" + todayWeather.getFengxiang() + todayWeather.getFengli());
        String str = todayWeather.getLow().replace("低温 ", "") +
                "~" + todayWeather.getHigh().replace("高温", "");
        tvTemperature.setText(str);
        if (todayWeather.getType().length() < 3) {
            Icon_Set(todayWeather.getType());
        } else {
            if (todayWeather.getType().indexOf("转") != -1) {
                String twz = todayWeather.getType().substring(0, todayWeather.getType().indexOf("转"));
                Icon_Set(twz);
            } else if (todayWeather.getType().indexOf("到") != -1) {
                String twd = todayWeather.getType().substring(0, todayWeather.getType().indexOf("到"));
                Icon_Set(twd);
            }
        }
    }

    private void Icon_Set(String Weather_Type) {
        if (Weather_Type.equals("晴")) {
            imgWeather.setImageDrawable(getResources().getDrawable(R.drawable.sunny));
            back.setBackground(getResources().getDrawable(R.drawable.sunny2));
        } else if (Weather_Type.equals("多云")) {
            imgWeather.setImageDrawable(getResources().getDrawable(R.drawable.cloudy));
            back.setBackground(getResources().getDrawable(R.drawable.sunnyback));
        } else if (Weather_Type.equals("大雨")) {
            imgWeather.setImageDrawable(getResources().getDrawable(R.drawable.raining));
            back.setBackground(getResources().getDrawable(R.drawable.sunny3));
        } else if (Weather_Type.equals("阴")) {
            imgWeather.setImageDrawable(getResources().getDrawable(R.drawable.overcast));
            back.setBackground(getResources().getDrawable(R.drawable.sunny3));
        } else if (Weather_Type.equals("小雨")) {
            imgWeather.setImageDrawable(getResources().getDrawable(R.drawable.smallrain));
            back.setBackground(getResources().getDrawable(R.drawable.sunny3));
        } else if (Weather_Type.equals("中雨")) {
            imgWeather.setImageDrawable(getResources().getDrawable(R.drawable.raining));
            back.setBackground(getResources().getDrawable(R.drawable.sunny3));
        } else if (Weather_Type.equals("雷阵雨")) {
            imgWeather.setImageDrawable(getResources().getDrawable(R.drawable.thundershower));
            back.setBackground(getResources().getDrawable(R.drawable.sunny3));
        } else {
            imgWeather.setImageDrawable(getResources().getDrawable(R.drawable.defaulticon));
            back.setBackground(getResources().getDrawable(R.drawable.sunny3));
        }
    }

    private ImageView btnUpdate;
    private ImageView findLocation;
    private TextView tvCityName;
    private TextView tvCity;
    private TextView tvTime;
    private TextView tvHumudity;
    private TextView tvWeek;
    private TextView tvPmData;
    private TextView tvPmQuality;
    private TextView tvTemperature;
    private TextView tvClimate;
    private TextView tvWind;
    private ImageView imgPm;
    private ImageView imgWeather;
    private ImageView imgSelectCity;
    private LinearLayout back;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        btnUpdate = (ImageView) findViewById(R.id.title_update_btn);
        btnUpdate.setOnClickListener(this);
        imgSelectCity = (ImageView) findViewById(R.id.title_city_manager);
        imgSelectCity.setOnClickListener(this);
        findLocation = (ImageView)findViewById(R.id.title_location);
        findLocation.setOnClickListener(this);
        back = (LinearLayout) findViewById(R.id.back);

        checkNetState();
        InitView();
        Location_Init();

    }

    private TodayWeather parseXML(String xmlData) {
        TodayWeather todayWeather = null;

        int fengXiangCount = 0;
        int fengLiCount = 0;
        int dateCount = 0;
        int highCount = 0;
        int lowCount = 0;
        int typeCount = 0;

        try {
            XmlPullParserFactory fac = XmlPullParserFactory.newInstance();
            XmlPullParser xmlPullParser = fac.newPullParser();
            xmlPullParser.setInput(new StringReader(xmlData));
            int eventType = xmlPullParser.getEventType();
            Log.d("Myweather", "parseXML");

            while (eventType != XmlPullParser.END_DOCUMENT) {
                switch (eventType) {
                    case XmlPullParser.START_DOCUMENT:
                        break;
                    case XmlPullParser.START_TAG:
                        if (xmlPullParser.getName().equals("resp")) {
                            todayWeather = new TodayWeather();
                        }
                        if (todayWeather != null) {
                            if (xmlPullParser.getName().equals("city")) {
                                eventType = xmlPullParser.next();
                                todayWeather.setCity(xmlPullParser.getText());
                                Log.d("Myweather", "city" + xmlPullParser.getText());
                            } else if (xmlPullParser.getName().equals("updatetime")) {
                                eventType = xmlPullParser.next();
                                todayWeather.setUpdatetime(xmlPullParser.getText());
                                Log.d("Myweather", "updatetime" + xmlPullParser.getText());
                            } else if (xmlPullParser.getName().equals("shidu")) {
                                eventType = xmlPullParser.next();
                                todayWeather.setShidu(xmlPullParser.getText());
                                Log.d("Myweather", "湿度" + xmlPullParser.getText());
                            } else if (xmlPullParser.getName().equals("wendu")) {
                                eventType = xmlPullParser.next();
                                todayWeather.setWendu(xmlPullParser.getText());
                                Log.d("Myweather", "温度" + xmlPullParser.getText());
                            } else if (xmlPullParser.getName().equals("pm25")) {
                                eventType = xmlPullParser.next();
                                todayWeather.setPm25(xmlPullParser.getText());
                                Log.d("Myweather", "pm2.5" + xmlPullParser.getText());
                            } else if (xmlPullParser.getName().equals("quality")) {
                                eventType = xmlPullParser.next();
                                todayWeather.setQuality(xmlPullParser.getText());
                                Log.d("Myweather", "空气质量" + xmlPullParser.getText());
                            } else if ((xmlPullParser.getName().equals("fengxiang")) && fengXiangCount == 0) {
                                eventType = xmlPullParser.next();
                                todayWeather.setFengxiang(xmlPullParser.getText());
                                fengXiangCount++;
                                Log.d("Myweather", "风向" + xmlPullParser.getText());
                            } else if ((xmlPullParser.getName().equals("fengli")) && fengLiCount == 0) {
                                eventType = xmlPullParser.next();
                                todayWeather.setFengli(xmlPullParser.getText());
                                fengLiCount++;
                                Log.d("Myweather", "风力" + xmlPullParser.getText());
                            } else if ((xmlPullParser.getName().equals("date")) && dateCount == 0) {
                                eventType = xmlPullParser.next();
                                todayWeather.setDate(xmlPullParser.getText());
                                dateCount++;
                                Log.d("Myweather", "日期" + xmlPullParser.getText());
                            } else if ((xmlPullParser.getName().equals("high")) && highCount == 0) {
                                eventType = xmlPullParser.next();
                                todayWeather.setHigh(xmlPullParser.getText());
                                highCount++;
                                Log.d("Myweather", "最高气温" + xmlPullParser.getText());
                            } else if ((xmlPullParser.getName().equals("low")) && lowCount == 0) {
                                eventType = xmlPullParser.next();
                                todayWeather.setLow(xmlPullParser.getText());
                                lowCount++;
                                Log.d("Myweather", "最低气温" + xmlPullParser.getText());
                            } else if ((xmlPullParser.getName().equals("type")) && typeCount == 0) {
                                eventType = xmlPullParser.next();
                                todayWeather.setType(xmlPullParser.getText());
                                typeCount++;
                                Log.d("Myweather", "天气情况" + xmlPullParser.getText());
                            }
                        }
                        break;
                    case XmlPullParser.END_TAG:
                        break;
                }
                eventType = xmlPullParser.next();
            }

        } catch (Exception e) {
            e.printStackTrace();
        }
        return todayWeather;
    }

    private void InitView() {
        tvCityName = (TextView) findViewById(R.id.title_city_name);
        tvCity = (TextView) findViewById(R.id.city);
        tvTime = (TextView) findViewById(R.id.time);
        tvHumudity = (TextView) findViewById(R.id.humidity);
        tvWeek = (TextView) findViewById(R.id.week_today);
        tvPmData = (TextView) findViewById(R.id.pm2_5_data);
        tvPmQuality = (TextView) findViewById(R.id.pm2_5_quality);
        tvTemperature = (TextView) findViewById(R.id.temperature);
        tvClimate = (TextView) findViewById(R.id.climate);
        tvWind = (TextView) findViewById(R.id.wind);
        imgPm = (ImageView) findViewById(R.id.pm2_5_img);
        imgWeather = (ImageView) findViewById(R.id.weather_img);
        cityList = ((MyApplication)getApplication()).getCityList();


        tvCityName.setText("N/A");
        tvCity.setText("N/A");
        tvTime.setText("N/A");
        tvHumudity.setText("N/A");
        tvWeek.setText("N/A");
        tvPmData.setText("N/A");
        tvPmQuality.setText("N/A");
        tvTemperature.setText("N/A");
        tvClimate.setText("N/A");
        tvWind.setText("N/A");

        SharedPreferences sharedPreferences = getSharedPreferences("config", MODE_PRIVATE);
        String cityCode = sharedPreferences.getString("main_city_code", "101210701");
        if (NetUtil.getNetworkState(this) != NetUtil.NETWORK_NONE) {
            GetLocationCode();
            if (cc == null) {
                queryWeatherCode(cityCode);
                Toast.makeText(this, "自动定位失败显示默认城市温州", Toast.LENGTH_LONG).show();
            }
            else {
                cityCode = sharedPreferences.getString("main_city_code", changeCityCode);
                queryWeatherCode(cityCode);
                Toast.makeText(this, "定位成功当前位置为" + cc, Toast.LENGTH_LONG).show();
            }
        }
        else {
            Toast.makeText(this, "网络连接失败请检查网络", Toast.LENGTH_LONG).show();
        }
    }

    private void checkNetState() {
        if (NetUtil.getNetworkState(this) != NetUtil.NETWORK_NONE) {
            Log.d("Myweather", "网络状态正常");
        } else {
            Log.d("Myweather", "网络连接失败");
            Toast.makeText(MainActivity.this, "网络连接失败", Toast.LENGTH_LONG).show();
        }
    }

    @Override
    public void onClick(View v) {
        if (v.getId() == R.id.title_update_btn) {
            SharedPreferences sharedPreferences = getSharedPreferences("config", MODE_PRIVATE);
            String cityCode = sharedPreferences.getString("main_city_code", changeCityCode);

            if (NetUtil.getNetworkState(this) != NetUtil.NETWORK_NONE) {
                Log.d("Myweather", "网络连接正常");
                queryWeatherCode(cityCode);
                Toast.makeText(MainActivity.this, "更新成功", Toast.LENGTH_LONG).show();
            } else {
                Log.d("Myweather", "网络连接失败");
                Toast.makeText(MainActivity.this, "网络连接失败", Toast.LENGTH_LONG).show();
            }
        } else if (v.getId() == R.id.title_city_manager) {
            Intent intent = new Intent(this, SelectCityActivity.class);
            startActivityForResult(intent, 0);
        } else if (v.getId() == R.id.title_location){
            GetLocationCode();
            if (cc != null) {
                SharedPreferences sharedPreferences = getSharedPreferences("config", MODE_PRIVATE);
                String cityCode = sharedPreferences.getString("main_city_code", changeCityCode);
                queryWeatherCode(cityCode);
                Toast.makeText(this, "定位成功当前位置为" + cc, Toast.LENGTH_LONG).show();
            }
            else
                Toast.makeText(this, "定位失败请重试", Toast.LENGTH_LONG).show();
        }

    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        String sCityName = data.getExtras().getString("cityname");
        String sCityCode = data.getExtras().getString("citycode");

        queryWeatherCode(sCityCode);
        changeCityCode = sCityCode;
        tvCityName.setText(sCityName);
    }

    private void queryWeatherCode(String cityCode) {
        final String address = "http://wthrcdn.etouch.cn/WeatherApi?citykey=" + cityCode;

        Log.d("Myweather", address);

        new Thread(new Runnable() {
            @Override
            public void run() {
                HttpURLConnection con = null;

                TodayWeather todayWeather = null;
                try {
                    URL url = new URL(address);
                    con = (HttpURLConnection) url.openConnection();
                    con.setRequestMethod("GET");
                    con.setConnectTimeout(8000);
                    con.setReadTimeout(8000);
                    InputStream in = con.getInputStream();
                    BufferedReader reader = new BufferedReader(new InputStreamReader(in));
                    StringBuilder response = new StringBuilder();
                    String str;
                    while ((str = reader.readLine()) != null) {
                        response.append(str);
                        Log.d("Myweather", str);
                    }

                    String responseStr = response.toString();
                    Log.d("Myweather", responseStr);
                    todayWeather = parseXML(responseStr);
                    if (todayWeather != null) {
                        Log.d("Myweather", todayWeather.toString());
                    }
                    Message msg = new Message();
                    msg.what = UPDATE_TODAY_WEATHER;
                    msg.obj = todayWeather;
                    mHandler.sendMessage(msg);

                } catch (Exception e) {
                    e.printStackTrace();
                } finally {
                    con.disconnect();
                }
            }
        }).start();
    }

    private void Location_Init() {
        //初始化定位
        mLocationClient = new AMapLocationClient(getApplicationContext());
        //设置定位回调监听
        mLocationClient.setLocationListener(mLocationListener);
        //初始化AMapLocationClientOption对象
        mLocationOption = new AMapLocationClientOption();
        //设置定位模式为低功耗模式
        mLocationOption.setLocationMode(AMapLocationClientOption.AMapLocationMode.Battery_Saving);
        //获取一次定位结果：
        //该方法默认为false。
        mLocationOption.setOnceLocation(false);
        mLocationOption.setInterval(2000);

        //获取最近3s内精度最高的一次定位结果：
        //设置setOnceLocationLatest(boolean b)接口为true，启动定位时SDK会返回最近3s内精度最高的一次定位结果。如果设置其为true，setOnceLocation(boolean b)接口也会被设置为true，反之不会，默认为false。
        mLocationOption.setOnceLocationLatest(true);
        //设置是否返回地址信息（默认返回地址信息）
        mLocationOption.setNeedAddress(true);
        //设置是否允许模拟位置,默认为false，不允许模拟位置
        mLocationOption.setMockEnable(false);
        //关闭缓存机制
        mLocationOption.setLocationCacheEnable(false);
        //给定位客户端对象设置定位参数
        mLocationClient.setLocationOption(mLocationOption);
        //启动定位
        mLocationClient.startLocation();

    }

    private class MyAMapLocationListener implements AMapLocationListener {

        @Override
        public void onLocationChanged(AMapLocation aMapLocation) {
            if (aMapLocation != null) {
                if (aMapLocation.getErrorCode() == 0 && aMapLocation.getCity()!=null) {
                    cc = aMapLocation.getCity();
                    if (cc.indexOf("市") != -1)
                        cc = cc.replace("市", "");
                    Log.e("位置：", aMapLocation.getAddress());
                } else {
                    //定位失败时，可通过ErrCode（错误码）信息来确定失败的原因，errInfo是错误信息，详见错误码表。
                    Log.e("AmapError", "location Error, ErrCode:"
                            + aMapLocation.getErrorCode() + ", errInfo:"
                            + aMapLocation.getErrorInfo());
                }
            }
        }
    }

    private void GetLocationCode(){
        for (City city:cityList){
            String cityname = city.getCity();
            if (cityname.equals(cc)){
                changeCityCode = city.getNumber().toString();
                break;
            }
        }
    }
}

