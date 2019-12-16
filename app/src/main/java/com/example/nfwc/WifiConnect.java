package com.example.nfwc;

import java.util.LinkedList;
import java.util.List;
import java.util.Set;

import android.Manifest;
import android.annotation.SuppressLint;
import android.content.DialogInterface;
import android.content.pm.PackageManager;
import android.net.wifi.ScanResult;
import android.net.wifi.WifiConfiguration;
import android.net.wifi.WifiInfo;
import android.net.wifi.WifiManager;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;

public class WifiConnect {
    private Context mContext;
    private WifiManager mWifiManager;
    private List<ScanResult> scanResults=null;
    private WifiBroadcastReceiver receiver;
    private static final int MY_PERMISSIONS_REQUEST_FINE_LOCATION=0;
    private static final int MY_PERMISSIONS_REQUEST_COARSE_LOCATION=1;

    public WifiConnect(Context context){
        mContext=context;
        mWifiManager = context==null ? null : (WifiManager)context.getApplicationContext().getSystemService(context.WIFI_SERVICE);
        //receiver=new WifiBroadcastReceiver(mContext,mWifiManager);
        OpenWifi();
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

    public String Connect(String targetSSID,String password){
        targetSSID="\""+targetSSID+"\"";
        password="\""+password+"\"";
        try {
            Thread.sleep(5000);
        }catch (Exception e){e.printStackTrace();}
        scanResults=receiver.scanResults;
        ScanResult targetWifi=findWifiBySSID(targetSSID);
        int netId=findInConfiguration(targetWifi);
        WifiInfo info=mWifiManager.getConnectionInfo();
        if(info!=null){
            if(info.getSSID().equals(targetSSID)){
                return "This wifi is connecting";
            }
            else{
                mWifiManager.disconnect();
            }
        }

        //没找到对应wifi
        if(targetWifi==null){
            return "There is no wifi named "+targetSSID;
        }

        //找到对应wifi，但未连接过，构造config
        if(netId!=-1){
            mWifiManager.enableNetwork(netId,true);
            return "Connected";
        }
        else{
            WifiConfiguration config=createWifiConfig(targetWifi,password);
            mWifiManager.addNetwork(config);
            List<WifiConfiguration> tmp=mWifiManager.getConfiguredNetworks();
            for(WifiConfiguration i:tmp){
                if(!TextUtils.isEmpty(i.SSID)&&TextUtils.equals(i.SSID,targetWifi.SSID)){
                    mWifiManager.enableNetwork(i.networkId,true);
                    break;
                }
            }
            return "Connected new";
        }

    }

    //Search wifi by SSID in scanResults
    private ScanResult findWifiBySSID(String targetSSID){
        ScanResult targetWifi=null;

        for(ScanResult tempScanResult:scanResults){
            if(tempScanResult.SSID.equals(targetSSID)){
                targetWifi=tempScanResult;
            }
        }
        return targetWifi;
    }

    //Judge whether this wifi has been configured before
    //Return netId or -1
    private int findInConfiguration(ScanResult targetWifi){
        int netId=-1;
        List<WifiConfiguration> tempList=mWifiManager.getConfiguredNetworks();

        if(targetWifi==null){
            return -1;
        }

        for(WifiConfiguration c:tempList){
            if(c.SSID.equals(targetWifi.SSID)){
                netId=c.networkId;
            }
        }
        return netId;
    }

    //Get the SSID of connecting wifi
    private String getCurrentSSID(){
        return "";
    }

    private WifiConfiguration createWifiConfig(ScanResult scanResult,String password){
        WifiConfiguration config=new WifiConfiguration();
        Log.d("create config",scanResult.SSID);
        config.SSID=scanResult.SSID;
        Log.d("create config",config.SSID);
        String capabilities=scanResult.capabilities;

        if(!TextUtils.isEmpty(capabilities)){
            if(capabilities.contains("WPA")||capabilities.contains("wpa")){
                config.preSharedKey=password;
            }
            else if(capabilities.contains("WEP")||capabilities.contains("wep")){
                config.wepKeys[0]=password;
                config.wepTxKeyIndex=0;
                config.allowedKeyManagement.set(WifiConfiguration.KeyMgmt.NONE);
                config.allowedGroupCiphers.set(WifiConfiguration.GroupCipher.WEP40);
            }
            else{
                config.allowedKeyManagement.set(WifiConfiguration.KeyMgmt.NONE);
            }
        }
        return config;
    }

}
