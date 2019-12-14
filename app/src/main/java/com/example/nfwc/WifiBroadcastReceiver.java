package com.example.nfwc;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.net.wifi.ScanResult;
import android.net.wifi.WifiConfiguration;
import android.net.wifi.WifiInfo;
import android.net.wifi.WifiManager;
import android.net.ConnectivityManager;
import android.util.Log;

import java.util.List;

public class WifiBroadcastReceiver extends BroadcastReceiver {
    private WifiManager mWifiManager;
    private Context receiverContext;
    private WifiStateChangeListener wifiStateChangeListener;
    public List<ScanResult> scanResults=null;

    public WifiBroadcastReceiver(Context context,WifiManager wifiManager){
        receiverContext=context;
        mWifiManager=wifiManager;
        IntentFilter filter=new IntentFilter();
        filter.addAction(WifiManager.WIFI_STATE_CHANGED_ACTION);//wifi开关变化广播
        filter.addAction(WifiManager.SCAN_RESULTS_AVAILABLE_ACTION);//热点扫描结果通知广播
        filter.addAction(WifiManager.SUPPLICANT_STATE_CHANGED_ACTION);//—热点连接结果通知广播
        filter.addAction(WifiManager.NETWORK_STATE_CHANGED_ACTION);  //—网络状态变化广播（与上一广播协同完成连接过程通知）
        filter.addAction(ConnectivityManager.CONNECTIVITY_ACTION);
        receiverContext.registerReceiver(this,filter);
    }

    @Override
    public void onReceive(Context context, Intent intent) {
        String action = intent.getAction();
        if (null != wifiStateChangeListener) {
            wifiStateChangeListener.onWifiChange(intent);
        }
    }

    public void unRegister() {
        receiverContext.unregisterReceiver(this);
    }

    public void setWifiStateChangeListener(WifiStateChangeListener wifiStateChangeListener) {
        this.wifiStateChangeListener = wifiStateChangeListener;
    }

    public interface WifiStateChangeListener {
        void onWifiChange(Intent action);
    }

    public void onWifiChange(Intent action){
        switch (action.getAction()){
            case WifiManager.WIFI_STATE_CHANGED_ACTION:
                checkWifiState(action);
                break;
            case WifiManager.SCAN_RESULTS_AVAILABLE_ACTION:
                getScanResult();
                break;
            default:
                break;
        }
    }

    private void checkWifiState(Intent intent){
        //检测当前WiFi状态
        int wifiState=intent.getIntExtra(WifiManager.EXTRA_WIFI_STATE, WifiManager.WIFI_STATE_DISABLED);
        switch (wifiState){
            case WifiManager.WIFI_STATE_DISABLED:
                OpenWifi();
                break;
            case WifiManager.WIFI_STATE_ENABLED:
                startScan();
                break;
            case WifiManager.WIFI_STATE_DISABLING:
                break;
            case WifiManager.WIFI_STATE_ENABLING:
                break;
            default:
                break;
        }
    }

    private void OpenWifi(){
        if(!mWifiManager.isWifiEnabled()){
            Log.d("wifi", "try to open wifi");
            mWifiManager.setWifiEnabled(true);
        }
        else{
            Log.d("wifi", "wifi has been opened");
        }
    }

    private void startScan(){
        mWifiManager.startScan();
    }

    private void getScanResult(){
        scanResults.clear();
        scanResults=mWifiManager.getScanResults();
    }
}