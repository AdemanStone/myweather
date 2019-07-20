package com.qst.myweather;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextUtils;
import android.text.TextWatcher;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import java.util.ArrayList;
import java.util.List;

public class SelectCityActivity extends AppCompatActivity implements View.OnClickListener{

    private ImageView titleBack;
    private ListView myList;
    private TextView tvSelectCityName;
    private TextView tvSelectCityCode;
    private List<City> cityList;
    private ArrayList<String> cityName;
    private ArrayAdapter<String> myAdapter;
    private ClearEditText mClearText;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.select_city);

        initViews();
        processListener();

    }

    @Override
    public void onClick(View v) {
        if (v.getId() == R.id.title_back){

            if (tvSelectCityName.getText().length() == 0 || tvSelectCityCode.getText().length() == 0) {
                Toast.makeText(this, "请选择城市", Toast.LENGTH_LONG).show();
                return;
            }

            Intent intent = getIntent();
            intent.putExtra("cityname", tvSelectCityName.getText().toString());
            intent.putExtra("citycode", tvSelectCityCode.getText().toString());
            setResult(0, intent);
            finish();
        }
    }

    private void initViews(){
        titleBack = (ImageView)findViewById(R.id.title_back);
        myList = (ListView)findViewById(R.id.city_list);
        tvSelectCityName = (TextView)findViewById(R.id.tvSelectCityName);
        tvSelectCityCode = (TextView)findViewById(R.id.tvSelectCityCode);
        mClearText = (ClearEditText)findViewById(R.id.cetSearchCity);
        cityList = ((MyApplication)getApplication()).getCityList();
        titleBack.setOnClickListener(this);
    }

    private void processListener(){
        prepareList();
        //列表单击事件

        listenListClick();

        listClearEdit();

    }

    private void listClearEdit() {
        mClearText.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {

            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                filterCity(s.toString());
                myList.setAdapter(myAdapter);

            }

            @Override
            public void afterTextChanged(Editable s) {

            }
        });

    }

    private void filterCity(String filterString){
        cityName.clear();
        if (TextUtils.isEmpty(filterString)){
            for (City city:cityList){
                cityName.add(city.getCity() + "  城市代码：" + city.getNumber());
            }
        }
        else {
            for (City city:cityList){
                if (city.getCity().indexOf(filterString) != -1){
                    cityName.add(city.getCity() + "  城市代码：" + city.getNumber());
                }
            }
            myAdapter.notifyDataSetChanged();
        }
    }

    private void prepareList(){
        cityName = new ArrayList<String>();
        for (City city:cityList){
            cityName.add(city.getCity() + "  城市代码：" + city.getNumber());
        }

        myAdapter = new ArrayAdapter<String>(this, android.R.layout.simple_list_item_1, cityName);
        myList.setAdapter(myAdapter);
    }

    private void listenListClick(){
        myList.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                String sSelect[] = cityName.get(position).split("  城市代码：");
                Log.i("Myweather", sSelect.toString());
                tvSelectCityName.setText(sSelect[0]);
                tvSelectCityCode.setText(sSelect[1]);
            }
        });
    }
}
