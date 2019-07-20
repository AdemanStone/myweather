package com.qst.myweather;


import android.content.Context;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;


//获取网络状态
public class NetUtil {
    public static final int NETWORK_NONE = 1;
    public static final int NETWORK_WIFI = 2;
    public static final int NETWORK_MOBILE = 3;

    public static int getNetworkState(Context context) {
        ConnectivityManager cM = (ConnectivityManager)context.
                getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo networkInfo = cM.getActiveNetworkInfo();

        if (networkInfo == null)
            return NETWORK_NONE;

        int nType = networkInfo.getType();
        if (nType == ConnectivityManager.TYPE_WIFI)
            return NETWORK_WIFI;
        else if (nType == ConnectivityManager.TYPE_MOBILE)
            return NETWORK_MOBILE;

        return NETWORK_NONE;
    }
}
