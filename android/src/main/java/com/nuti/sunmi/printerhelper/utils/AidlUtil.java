package com.nuti.sunmi.printerhelper.utils;

import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.os.Build;
import android.os.IBinder;
import android.os.RemoteException;
import android.util.Log;
import android.widget.Toast;

import androidx.annotation.RequiresApi;

import com.nuti.sunmi.printerhelper.bean.TableItem;

import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;


import io.flutter.plugin.common.MethodChannel;
import woyou.aidlservice.jiuiv5.ICallback;
import woyou.aidlservice.jiuiv5.IWoyouService;


public class AidlUtil {
    public static String TAG = "AidlUtil";


    private static final String SERVICE＿PACKAGE = "woyou.aidlservice.jiuiv5";
    private static final String SERVICE＿ACTION = "woyou.aidlservice.jiuiv5.IWoyouService";

    private IWoyouService woyouService;
    private static AidlUtil mAidlUtil;
    private Context context;




    interface OnBindCallBack {
        void disconnected(ComponentName componentName);
        void connected(ComponentName componentName);
    }

    private OnBindCallBack mOnBindCallBack;

    void setOnBindCallBack(OnBindCallBack callBack){
        this.mOnBindCallBack = callBack;
    }

    private AidlUtil(Context context) {
        this.context = context.getApplicationContext();
    }

    public static AidlUtil getInstance(Context ctx) {
        if (mAidlUtil == null) {
            synchronized (AidlUtil.class) {
                if (mAidlUtil == null) {
                    mAidlUtil = new AidlUtil(ctx);
                }
            }
        }
        return mAidlUtil;
    }

    /**
     */
    @RequiresApi(api = Build.VERSION_CODES.DONUT)
    public void connectPrinterService() {
        Intent intent = new Intent();
        intent.setPackage(SERVICE＿PACKAGE);
        intent.setAction(SERVICE＿ACTION);
        context.getApplicationContext().startService(intent);
        context.getApplicationContext().bindService(intent, connService, Context.BIND_AUTO_CREATE);
    }


    public void disconnectPrinterService() {
        if (woyouService != null) {
            context.getApplicationContext().unbindService(connService);
            woyouService = null;
        }
    }

    public boolean isConnect() {
        return woyouService != null;
    }

    private ServiceConnection connService = new ServiceConnection() {

        @Override
        public void onServiceDisconnected(ComponentName name) {
            woyouService = null;
            if (mOnBindCallBack != null){
                mOnBindCallBack.disconnected(name);
            }
        }

        @Override
        public void onServiceConnected(ComponentName name, IBinder service) {
            woyouService = IWoyouService.Stub.asInterface(service);
            if (mOnBindCallBack != null){
                mOnBindCallBack.connected(name);
            }
        }
    };


    public void sendRawData(byte[] data) {
        if (woyouService == null) {
            Log.e(TAG, "sendRawData: woyouService is null");
            return;
        }

        try {
            woyouService.sendRAWData(data, null);
        } catch (RemoteException e) {
            e.printStackTrace();
        }
    }



    public ICallback generateCB(final PrinterCallback printerCallback){
        return new ICallback.Stub(){


            @Override
            public void onRunResult(boolean isSuccess) throws RemoteException {

            }

            @Override
            public void onReturnString(String result) throws RemoteException {
                printerCallback.onReturnString(result);
            }

            @Override
            public void onRaiseException(int code, String msg) throws RemoteException {

            }

            @Override
            public void onPrintResult(int code, String msg) throws RemoteException {

            }
        };
    }


    private int[] darkness = new int[]{0x0600, 0x0500, 0x0400, 0x0300, 0x0200, 0x0100, 0,
            0xffff, 0xfeff, 0xfdff, 0xfcff, 0xfbff, 0xfaff};



    public void setDarkness(int index) {
        if (woyouService == null) {
            Log.e(TAG, "setDarkness: woyouService is null");
            return;
        }

        int k = darkness[index];
        try {
            woyouService.sendRAWData(ESCUtil.setPrinterDarkness(k), null);
            woyouService.printerSelfChecking(null);
        } catch (RemoteException e) {
            e.printStackTrace();
        }
    }


    public List<String> getPrinterInfo(PrinterCallback printerCallback1, PrinterCallback printerCallback2) {
        if (woyouService == null) {
            Log.e(TAG, "getPrinterInfo: woyouService is null");
            return null;
        }

        List<String> info = new ArrayList<>();
        try {

            woyouService.getPrintedLength(generateCB(printerCallback1));
            woyouService.getPrinterFactory(generateCB(printerCallback2));
            info.add(woyouService.getPrinterSerialNo());
            info.add(woyouService.getPrinterModal());
            info.add(woyouService.getPrinterVersion());

            info.add(printerCallback1.getResult());
            info.add(printerCallback2.getResult());
            info.add(woyouService.getServiceVersion());
            PackageManager packageManager = context.getPackageManager();
            try {
                PackageInfo packageInfo = packageManager.getPackageInfo(SERVICE＿PACKAGE, 0);
                if(packageInfo != null){
                    info.add(packageInfo.versionName);
                    info.add(packageInfo.versionCode+"");
                }else{
                    info.add("");info.add("");
                }
            } catch (PackageManager.NameNotFoundException e) {
                e.printStackTrace();
            }

        } catch (Exception e) {
            e.printStackTrace();
        }
        return info;
    }


    public void initPrinter() {
        if (woyouService == null) {
            Log.e(TAG, "initPrinter: woyouService is null");
            return;
        }

        try {
            woyouService.printerInit(null);
        } catch (RemoteException e) {
            e.printStackTrace();
        }
    }


    public void printQr(String data, int modulesize, int errorlevel) {
        if (woyouService == null) {
            Log.e(TAG, "printQr: woyouService is null");
            return;
        }


        try {
			woyouService.setAlignment(1, null);
            woyouService.printQRCode(data, modulesize, errorlevel, null);
            woyouService.lineWrap(3, null);
        } catch (RemoteException e) {
            e.printStackTrace();
        }
    }


    public void printBarCode(String data, int symbology, int height, int width, int textposition) {
        if (woyouService == null) {
            Log.e(TAG, "printBarCode: woyouService is null");
            return;
        }


        try {
            woyouService.printBarCode(data, symbology, height, width, textposition, null);
            woyouService.lineWrap(3, null);
        } catch (RemoteException e) {
            e.printStackTrace();
        }
    }


    /**
     * HoaNT 21.01.2020
     */
    public void printLeftRight(String strLeft, String strRight, int size, MethodChannel.Result result) {
        if (woyouService == null) {
            Log.e(TAG, "pringText: woyouService is null");
            return;
        }

        try {
            String line = String.format("%-15s %15s %n", strLeft, strRight);
            woyouService.printTextWithFont(line, null, size, null);
            woyouService.lineWrap(1, null);

            result.success(true);
        } catch (RemoteException e) {
            e.printStackTrace();
            result.error("write_error", "printCustom", e.getLocalizedMessage());
        }
    }


    public void printCustom(String message, int size, int align, MethodChannel.Result result) {
        if (woyouService == null) {
            Log.e(TAG, "pringText: woyouService is null");
            return;
        }

        try {
            switch (align) {
                case 0:
                    // left align
                    woyouService.sendRAWData(ESCUtil.alignLeft(), null);
                    break;
                case 1:
                    // center align
                    woyouService.sendRAWData(ESCUtil.alignCenter(), null);
                    break;
                case 2:
                    // right align
                    woyouService.sendRAWData(ESCUtil.alignRight(), null);
                    break;
            }

            woyouService.printTextWithFont(message, null , size,null);
            woyouService.lineWrap(1, null);

            result.success(true);
        } catch (RemoteException e) {
            e.printStackTrace();
            result.error("write_error", "printCustom", e.getLocalizedMessage());
        }
    }


    public void printText(String content, float size, boolean isBold) {
        if (woyouService == null) {
            Log.e(TAG, "pringText: woyouService is null");
            return;
        }

        try {
            if (isBold) {
                woyouService.sendRAWData(ESCUtil.boldOn(), null);
            } else {
                woyouService.sendRAWData(ESCUtil.boldOff(), null);
            }

            woyouService.printTextWithFont(content, null, size, null);
            woyouService.lineWrap(3, null);
        } catch (RemoteException e) {
            e.printStackTrace();
        }
    }


    public void printBitmap(Bitmap bitmap) {
        if (woyouService == null) {
            Log.e(TAG, "printBitmap: woyouService is null");
            return;
        }

        try {
            woyouService.setAlignment(1, null);
            woyouService.printBitmap(bitmap, null);
            woyouService.lineWrap(3, null);
        } catch (RemoteException e) {
            e.printStackTrace();
        }
    }



    public void printBitmap(Bitmap bitmap, int orientation) {
        if (woyouService == null) {
            Toast.makeText(context,"服务已断开！",Toast.LENGTH_LONG).show();
            return;
        }

        try {
            if(orientation == 0){
                woyouService.printBitmap(bitmap, null);
                woyouService.printText("Sắp xếp theo hàng ngang\n", null);
                woyouService.printBitmap(bitmap, null);
                woyouService.printText("Sắp xếp theo hàng ngang\n", null);
            }else{
                woyouService.printBitmap(bitmap, null);
                woyouService.printText("\nSắp xếp theo chiều dọc\n", null);
                woyouService.printBitmap(bitmap, null);
                woyouService.printText("\nSắp xếp theo chiều dọc\n", null);
            }
            woyouService.lineWrap(3, null);
        } catch (RemoteException e) {
            e.printStackTrace();
        }
    }


    public void printTable(LinkedList<TableItem> list) {
        if (woyouService == null) {
            Log.e(TAG, "printTable: woyouService is null");
            return;
        }

        try {
            for (TableItem tableItem : list) {
                Log.i("kaltin", "printTable: "+tableItem.getText()[0]+tableItem.getText()[1]+tableItem.getText()[2]);
                woyouService.printColumnsString(tableItem.getText(), tableItem.getWidth(), tableItem.getAlign(), null);
            }
            woyouService.lineWrap(3, null);
        } catch (RemoteException e) {
            e.printStackTrace();
        }
    }


    public void printNewLine(){
        if (woyouService == null) {
            Log.e(TAG, "print3Line: woyouService is null");
            return;
        }

        try {
            woyouService.lineWrap(1, null);
        } catch (RemoteException e) {
            e.printStackTrace();
        }
    }

    public void print3Line(){
        if (woyouService == null) {
            Log.e(TAG, "print3Line: woyouService is null");
            return;
        }

        try {
            woyouService.lineWrap(3, null);
        } catch (RemoteException e) {
            e.printStackTrace();
        }
    }


    public void sendRawDatabyBuffer(byte[] data, ICallback iCallback){
        if (woyouService == null) {
            Log.e(TAG, "sendRawDatabyBuffer: woyouService is null");
            return;
        }

        try {
            woyouService.enterPrinterBuffer(true);
            woyouService.sendRAWData(data, iCallback);
            woyouService.exitPrinterBufferWithCallback(true, iCallback);
        } catch (RemoteException e) {
            e.printStackTrace();
        }
    }



    private byte[] fontSize(int size){
        // Print config "mode"
        byte[] cc = new byte[] { 0x1B, 0x21, 0x03 }; // 0- normal size text
        // byte[] cc1 = new byte[]{0x1B,0x21,0x00}; // 0- normal size text
        byte[] bb = new byte[] { 0x1B, 0x21, 0x08 }; // 1- only bold text
        byte[] bb2 = new byte[] { 0x1B, 0x21, 0x20 }; // 2- bold with medium text
        byte[] bb3 = new byte[] { 0x1B, 0x21, 0x10 }; // 3- bold with large text
        byte[] bb4 = new byte[] { 0x1B, 0x21, 0x30 }; // 4- strong text


        try {
            if (size == 0){
                return cc;
            }
            else if (size == 1){
                return bb;
            }
            else if (size == 2){
                return bb2;
            }
            else if (size == 3){
                return bb3;
            }
            else if (size == 4){
                return bb4;
            }
            else {
                return cc;
            }
        } catch (Exception e){
            e.getLocalizedMessage();
        }

        return cc;
    }
}
