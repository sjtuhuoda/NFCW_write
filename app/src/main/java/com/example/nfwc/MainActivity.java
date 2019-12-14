package com.example.nfwc;

import androidx.appcompat.app.AppCompatActivity;

import android.annotation.SuppressLint;
import android.annotation.TargetApi;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.nfc.FormatException;
import android.nfc.NdefMessage;
import android.nfc.NdefRecord;
import android.nfc.NfcAdapter;
import android.nfc.NfcManager;
import android.nfc.Tag;
import android.nfc.tech.MifareClassic;
import android.nfc.tech.Ndef;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.os.Parcelable;
import android.provider.Settings;
import android.text.TextUtils;
import android.util.Log;
import android.view.WindowManager;
import android.widget.TextView;
import android.widget.Toast;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.nio.charset.Charset;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

public class MainActivity extends AppCompatActivity {

    /** Called when the activity is first created. */
    private NfcAdapter mNfcAdapter;
    private PendingIntent mPendingIntent;
    private String[][] techList;
    private IntentFilter[] intentFilters;
    private Tag tag;
    private TextView report;
    private String message = "";
    private int sdkVersion;
    public static final String TXT_FILEPATH = Environment.getExternalStorageDirectory().getPath() + "/files/";
    /**
     * 开始的生命周期:onCreate-->onStart-->OnResume
     * 从此页面跳转至详细页面：onPause-->OnStop , OnRestart-->OnStart -->OnResume
     * nfc onPause --> onNewIntent -- >onResume
     * 从其他页面返回  onNewIntent-->OnRestart-->OnStart -->OnResume
     */
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        //屏幕变暗—>黑，但不锁屏
        //	getWindow().addFlags(WindowManager.LayoutParams.FLAG_SHOW_WHEN_LOCKED);
        getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);

        setContentView(R.layout.activity_main);
        report=(TextView)findViewById(R.id.textView);
        report.setText(String.format("%s", message));


        // 支持nfc tag所有的标准
        techList = new String[][] { new String[] { android.nfc.tech.NfcV.class.getName() }, new String[] { android.nfc.tech.NfcF.class.getName() }, new String[] { android.nfc.tech.NfcA.class.getName() }, new String[] { android.nfc.tech.NfcB.class.getName() }, new String[] { android.nfc.tech.Ndef.class.getName() }, new String[] { android.nfc.tech.NdefFormatable.class.getName() }, new String[] { android.nfc.tech.MifareClassic.class.getName() }, new String[] { android.nfc.tech.MifareUltralight.class.getName() } };
        // 当任意tag被检测到时，你将收到TAG_DISCOVERED intent。因此请注意你应该只处理你想要的Intent。
        intentFilters = new IntentFilter[] { new IntentFilter(NfcAdapter.ACTION_TECH_DISCOVERED), new IntentFilter(NfcAdapter.ACTION_TAG_DISCOVERED), new IntentFilter(NfcAdapter.ACTION_NDEF_DISCOVERED) };
        mPendingIntent = PendingIntent.getActivity(this, 0, new Intent(this, getClass()).addFlags(Intent.FLAG_ACTIVITY_SINGLE_TOP), 0);

        openNfcManager();

        sdkVersion = Build.VERSION.SDK_INT;

    }

    private void openNfcManager(){
        NfcManager manager = (NfcManager) getSystemService(Context.NFC_SERVICE);
        mNfcAdapter = manager.getDefaultAdapter();
        // 实例化NFC设备
        //	mNfcAdapter = NfcAdapter.getDefaultAdapter(this);
        if (mNfcAdapter == null) {
            Toast.makeText(this, "您的设备不支持NFC", Toast.LENGTH_SHORT).show();
        } else if (mNfcAdapter != null) {
            if (!mNfcAdapter.isEnabled()) {
                Toast.makeText(this, "请在系统设置中先启用NFC功能", Toast.LENGTH_SHORT).show();
                startActivity(new Intent(Settings.ACTION_NFC_SETTINGS));
            } else {
                Toast.makeText(this, "启动NFC注册成功......", Toast.LENGTH_SHORT).show();
            }

        }
    }


    // 当窗口的创建模式是singleTop或singleTask时调用，用于取代onCreate方法
    // 当NFC标签靠近手机，建立连接后调用
    @SuppressLint("NewApi")
    @Override
    public void onNewIntent( Intent intent) {

        //nfc标签靠近手机，建立连接后调用
        if(sdkVersion <  Build.VERSION_CODES.KITKAT){ //19 4.4
            tagShowMethod(intent);
        }

    }

    @Override
    public void onResume() {
        super.onResume();

        if(mNfcAdapter != null){
            // 使用前台发布系统
            mNfcAdapter.enableForegroundDispatch(this, mPendingIntent, intentFilters, techList);
            enableReaderMode();
        }

    }

    @TargetApi(19)
    private void enableReaderMode() {
        if(sdkVersion < 19){return;}

        if(sdkVersion >= Build.VERSION_CODES.KITKAT){ //19 4.4
            int READER_FLAGS = NfcAdapter.FLAG_READER_NFC_A | NfcAdapter.FLAG_READER_NO_PLATFORM_SOUNDS;
            //	| NfcAdapter.FLAG_READER_NO_PLATFORM_SOUNDS;
            Bundle options = new Bundle();
            options.putInt(NfcAdapter.EXTRA_READER_PRESENCE_CHECK_DELAY, 50);// 延迟对卡片的检测

            if (mNfcAdapter != null) {
                mNfcAdapter.enableReaderMode(MainActivity.this, new NfcAdapter.ReaderCallback() {
                    @Override
                    public void onTagDiscovered(Tag tag) {
                        //这里不是主线程,不能直接让textview设置内容，
                        //api19 不从onNewIntent走，而是直接跳到这个回调函数里

                        tagShowMethodAPI19(tag);
                    }
                },READER_FLAGS, null);
            }
        }
    }

    @Override
    protected void onStart() {
        super.onStart();
    }

    @Override
    protected void onRestart() {
        super.onRestart();
    }

    @Override
    protected void onPause() {
        super.onPause();
        if(mNfcAdapter != null){
            mNfcAdapter.disableForegroundDispatch(this);
            disableReaderMode();
        }
    }

    @TargetApi(19)
    private void disableReaderMode() {

        if(sdkVersion < 19){return;}
        mNfcAdapter.disableReaderMode(this);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        mNfcAdapter = null;
        this.finish();

    }

    @Override
    protected void onStop() {
        super.onStop();
    }

    private void tagShowMethod(final Intent intent){

        // 获取Tag对象后读取里面的信息
        String tagInfo = getTagInfo(intent);
        Log.e("nfc", "tagInfo =" + tagInfo);
        if (!TextUtils.isEmpty(tagInfo)) {

            Date curDate = new Date();
            String dateStr = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss.SSS", Locale.CHINA).format(curDate);
            writeToTxt(tagInfo, dateStr);
        }
    }

    private void tagShowMethodAPI19(final Tag tag){
        // 获取Tag对象后读取里面的信息
        String tagInfo = getTagInfoAPI19(tag);
        message = tagInfo;
        Log.e("nfc", "tagInfo === api19 ==" + tagInfo);
//        if (!TextUtils.isEmpty(tagInfo)) {
//
//            Date curDate = new Date();
//            String dateStr = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss SSS", Locale.CHINA).format(curDate);
//            writeToTxt(tagInfo, dateStr);
//
//        }
    }


    private void writeToTxt( String recordNo,  String dateStr) {
        String fileNamePrefix = new SimpleDateFormat("yyyy-MM-dd", Locale.CHINA).format(new Date());
        String fileName = fileNamePrefix + ".txt";
        String s = recordNo + "," + dateStr.substring(11, dateStr.length());
        // 创建文件的对象,必须指向创建文件的路径
        FileOutputStream fos = null;
        try {
            File file = new File(TXT_FILEPATH + fileName);
            Toast.makeText(this, "请在系统设置中先启用NFC功能", Toast.LENGTH_SHORT).show();
            // 如果文件不存在，则创建文件
            if (!file.exists()) {
                if (!file.getParentFile().exists()) {
                    file.getParentFile().mkdirs();
                }
                file.createNewFile();
            } else {
                // 文件中有内容写入前加入回车符
                // FileOutputStream无法写入回车
                s = "\r\n" + s;
            }
            fos = new FileOutputStream(file, true);
            byte[] usemms = s.getBytes();
            fos.write(usemms, 0, usemms.length);
        } catch (Exception e) {
            e.printStackTrace();
        }finally {
            try {
                fos.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    private String getTagInfo(Intent intent) {
        tag = intent.getParcelableExtra(NfcAdapter.EXTRA_TAG);
        String tagInfo = "";
        // nfc tag支持哪几种格式
        if (null != tag) {
            String[] techListStrings = tag.getTechList();

            for (String tech : techListStrings) {
                if (tech == android.nfc.tech.Ndef.class.getName()) {
                    // 解析Ndef格式
                    tagInfo = parseNDEFMsg(intent);
                } else if (tech == android.nfc.tech.NfcV.class.getName()) {
                    // 解析NfcV格式
//                    tagInfo = parseNFCVMsg();
                } else if (tech == android.nfc.tech.NfcA.class.getName()) {
                    // 解析NfcA格式
                } else if (tech == android.nfc.tech.MifareClassic.class.getName()) {
                    // 解析MifareClassic格式
                    tagInfo = parseMifareClassicMsg();
                }
            }

        }
        return tagInfo;
    }

    private String getTagInfoAPI19(Tag tag) {
        //	tag = intent.getParcelableExtra(NfcAdapter.EXTRA_TAG);
        String tagInfo = "";
        // nfc tag支持哪几种格式
        if (null != tag) {
            String[] techListStrings = tag.getTechList();

            for (String tech : techListStrings) {
                if (tech == android.nfc.tech.Ndef.class.getName()) {
                    // 解析Ndef格式
                    tagInfo = parseNDEFMsgAPI19(tag);
                } else if (tech == android.nfc.tech.NfcV.class.getName()) {
                    // 解析NfcV格式
//                    tagInfo = parseNFCVMsg();
                } else if (tech == android.nfc.tech.NfcA.class.getName()) {
                    // 解析NfcA格式
                } else if (tech == android.nfc.tech.MifareClassic.class.getName()) {
                    // 解析MifareClassic格式
                    tagInfo = parseMifareClassicMsg();
                }
            }
        }
        return tagInfo;
    }


    private synchronized String parseNDEFMsg(Intent intent) {

        String tagText = "";
        Parcelable[] rawMsgs = intent.getParcelableArrayExtra(NfcAdapter.EXTRA_NDEF_MESSAGES);
        // 创建NdefMessage对象和NdefRecord对象
        if (rawMsgs != null && rawMsgs.length > 0) {
            NdefMessage ndefMessage = (NdefMessage) rawMsgs[0];
            NdefRecord mNdefRecord = null;
            if (null != ndefMessage) {
                mNdefRecord = ndefMessage.getRecords()[0];
            }
            if (null != mNdefRecord) {
                try {
                    tagText = new String(mNdefRecord.getPayload(), "UTF-8").trim();
                } catch (UnsupportedEncodingException e) {
                    e.printStackTrace();
                }
            }
        }
        return tagText;
    }

    private synchronized String parseNDEFMsgAPI19(Tag tag) {
        String tagText = "";
        Ndef ndef = Ndef.get(tag);

        try {
            if(ndef == null){
                return tagText;
            }
            ndef.connect();
            NdefMessage ndefMessage  = ndef.getNdefMessage();
            NdefRecord mNdefRecord = null;
            if (null != ndefMessage) {
                mNdefRecord = ndefMessage.getRecords()[0];
            }
            if (null != mNdefRecord) {
                tagText = new String(mNdefRecord.getPayload(), "UTF-8").trim();
            }
        } catch (Exception e) {
            e.printStackTrace();
        }finally{
            if(ndef != null){
                try {
                    ndef.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }
        return tagText;
    }

    public void writeNdefTag(Intent in) {
        Tag tag = in.getParcelableExtra(NfcAdapter.EXTRA_TAG);
        Ndef ndef = Ndef.get(tag);
        try {
            // 这一句别丢了，读nfc标签的时候不需要这句，因为那时数据直接就在intent中。
            ndef.connect();
            // 构造一个合适的NdefMessage。你可以看到代码里用了NdefRecord数组，只不过这个数组里只有一个record
            NdefMessage ndefMsg = new NdefMessage(new NdefRecord[] { createTextRecord("Test-10112") });
            ndef.writeNdefMessage(ndefMsg);
        } catch (IOException e) {
            e.printStackTrace();
        } catch (FormatException e) {
            e.printStackTrace();
        }
    }

    // 创建一个封装要写入的文本的NdefRecord对象
    public NdefRecord createTextRecord(String text) {
        // 生成语言编码的字节数组，中文编码
        byte[] langBytes = Locale.CHINA.getLanguage().getBytes(Charset.forName("US-ASCII"));
        // 将要写入的文本以UTF_8格式进行编码
        Charset utfEncoding = Charset.forName("UTF-8");
        // 由于已经确定文本的格式编码为UTF-8，所以直接将payload的第1个字节的第7位设为0
        byte[] textBytes = text.getBytes(utfEncoding);
        int utfBit = 0;
        // 定义和初始化状态字节
        char status = (char) (utfBit + langBytes.length);
        // 创建存储payload的字节数组
        byte[] data = new byte[1 + langBytes.length + textBytes.length];
        // 设置状态字节
        data[0] = (byte) status;
        // 设置语言编码
        System.arraycopy(langBytes, 0, data, 1, langBytes.length);
        // 设置实际要写入的文本
        System.arraycopy(textBytes, 0, data, 1 + langBytes.length, textBytes.length);
        // 根据前面设置的payload创建NdefRecord对象
        NdefRecord record = new NdefRecord(NdefRecord.TNF_MIME_MEDIA, NdefRecord.RTD_TEXT, new byte[0], data);
        return record;
    }

    private String parseMifareClassicMsg() {
        String tagInfo = "";
        MifareClassic mc = MifareClassic.get(tag);// 通过intent拿到EXTRA_TAG并转化成MirareClassic格式。
        int bCount = 0;
        int bIndex = 0;
        try {
            mc.connect();

            // 1K: 16个分区(sector)，每个分区4个块(block)，每个块(block) 16个byte数据
            // 2K: 32个分区，每个分区4个块(block)，每个块(block) 16个byte数据
            // 4K: 64个分区，每个分区4个块(block)，每个块(block) 16个byte数据
            // 获得sector总数
            int sectorCount = mc.getSectorCount();
            System.out.println(sectorCount);
            for (int i = 0; i < sectorCount; i++) {
                // 尝试去获得每个sector的认证，只有认证通过才能访问
                boolean auth = mc.authenticateSectorWithKeyA(i, MifareClassic.KEY_DEFAULT);
                if (auth) {
                    // 这句其实不是必须的，因为每个sector中本来就只有4个block
                    bCount = mc.getBlockCountInSector(i);
                    // 我们可以得到每一个sector中的第一个block的编号
                    bIndex = mc.sectorToBlock(i);
                    for (int j = 0; j < bCount; j++) {// 循环四次拿出一个sector中所有的block
                        // 每次循环bIndex会去++，然后可以得出每一个block的数据。这些数据是字节码，所以你还有一个翻译的工作要做。
                        byte[] data = mc.readBlock(bIndex);
                        tagInfo += data.toString();
                        System.out.println(tagInfo);
                        bIndex++;
                    }
                } else {
                    System.out.println(i);
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
        }

        return tagInfo;
    }
}