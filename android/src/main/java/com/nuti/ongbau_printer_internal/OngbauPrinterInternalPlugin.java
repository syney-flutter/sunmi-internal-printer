package com.nuti.ongbau_printer_internal;

import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Handler;
import android.os.Looper;
import android.util.Log;

import androidx.annotation.NonNull;

import com.nuti.sunmi.printerhelper.utils.AidlUtil;

import java.util.Map;

import io.flutter.embedding.engine.plugins.FlutterPlugin;
import io.flutter.plugin.common.BinaryMessenger;
import io.flutter.plugin.common.EventChannel;
import io.flutter.plugin.common.MethodCall;
import io.flutter.plugin.common.MethodChannel;
import io.flutter.plugin.common.MethodChannel.MethodCallHandler;
import io.flutter.plugin.common.MethodChannel.Result;
import io.flutter.plugin.common.PluginRegistry;


/** OngbauPrinterInternalPlugin */
public class OngbauPrinterInternalPlugin implements FlutterPlugin, MethodCallHandler {

  private static final String TAG = "OngbauPrinterPlugin";
  private static final String NAMESPACE = "ongbau_printer_internal";


  private static AidlUtil mAidlUtil;
  private EventChannel.EventSink statusSink, readSink;

  private Context mContext;


  public OngbauPrinterInternalPlugin(){
  }

  public void setContext(Context context){
    this.mContext = context;
  }

  private void setupChanel(BinaryMessenger binaryMessenger){
    if (mContext == null) {
      Log.d(TAG, "setupChanel: Context is null");
    } else {
      Log.d(TAG, "setupChanel: Context initialized");
    }

    final MethodChannel channel = new MethodChannel(binaryMessenger, NAMESPACE + "/methods");
    channel.setMethodCallHandler(this);


    EventChannel stateChannel = new EventChannel(binaryMessenger, NAMESPACE + "/state");
    stateChannel.setStreamHandler(stateStreamHandler);
  }

  @Override
  public void onAttachedToEngine(@NonNull FlutterPluginBinding flutterPluginBinding) {
    Log.d(TAG, "onAttachedToEngine called");
    if (flutterPluginBinding.getApplicationContext() != null) {
      mContext = flutterPluginBinding.getApplicationContext();
    }

    setupChanel(flutterPluginBinding.getBinaryMessenger());
  }


  public static void registerWith(PluginRegistry.Registrar registrar) {
    Log.d(TAG, "registerWith called");
    OngbauPrinterInternalPlugin ongbauPrinterInternalPlugin = new OngbauPrinterInternalPlugin();
    ongbauPrinterInternalPlugin.setContext(registrar.activity().getApplicationContext());
    ongbauPrinterInternalPlugin.setupChanel(registrar.messenger());
  }


  @Override
  public void onDetachedFromEngine(@NonNull FlutterPluginBinding binding) {

  }


  @Override
  public void onMethodCall(@NonNull MethodCall call, @NonNull Result result) {
    Log.d(TAG, "onMethodCall called");

    if (call.method.equals("getPlatformVersion")) {
      result.success("Android " + android.os.Build.VERSION.RELEASE);
    } else {
      onMethodCallInternal(call, result);
    }
  }


  /**
   * PRINTER INTERNAL
   * @param call
   * @param result
   */
  private void onMethodCallInternal(MethodCall call, Result result) {
    if (mContext == null){
      Log.d(TAG, "onMethodCallInternal: Context is null");
      result.error("error", "Connect is null", null);
      return;
    }

    final Map<String, Object> arguments = call.arguments();

    switch (call.method) {
      case "isConnected":
        result.success(mAidlUtil != null && mAidlUtil.isConnect());
        break;

      case "connect":
        try {
          if (mAidlUtil == null) {
            mAidlUtil = AidlUtil.getInstance(mContext);
          }
          mAidlUtil.connectPrinterService();
          result.success(true);
        } catch (Exception e){
          e.getLocalizedMessage();
          result.error("error_connect", "Connect Exception", e.getLocalizedMessage());
        }
        break;

      case "disconnect":
        try{
          mAidlUtil.disconnectPrinterService();
          result.success(true);
        } catch (Exception e){
          e.getLocalizedMessage();
          result.error("error_disconnect", "Disconnect Exception", e.getLocalizedMessage());
        }
        break;

      case "printLeftRight":
        try {
          String string1 = (String) arguments.get("string1");
          String string2 = (String) arguments.get("string2");
          int size = (int) arguments.get("size");

          mAidlUtil.printLeftRight(string1, string2, size, result);
          result.success(true);
        } catch (Exception e){
          result.error("error_printLeftRight", e.getLocalizedMessage(), null);
        }
        break;

      case "printCustom":
        try {
          String message = (String) arguments.get("message");
          int size = (int) arguments.get("size");
          int align = (int) arguments.get("align");
          boolean isBold = (boolean) arguments.get("isBold");

          mAidlUtil.printCustom(message, size, align, isBold, result);

          result.success(true);
        } catch (Exception e){
          result.error("error_printCustom", e.getLocalizedMessage(), null);
        }
        break;

      case "printNewLine":
        try {
          mAidlUtil.printNewLine();
          result.success(true);
        } catch (Exception e){
          result.error("error_printNewLine", e.getLocalizedMessage(), null);
        }
        break;


      case "printText":
        try {
          String message = (String) arguments.get("message");
          int size = (int) arguments.get("size");
          boolean isBold = (boolean) arguments.get("isBold");

          mAidlUtil.printText(message, size, isBold);

          result.success(true);
        } catch (Exception e){
          result.error("error_printNewLine", e.getLocalizedMessage(), null);
        }
        break;

      case "printImage":
        try {
          String pathImage = (String) arguments.get("pathImage");
          result.success(true);
        } catch (Exception e){
          result.error("error_printImage", e.getLocalizedMessage(), null);
        }

        break;
      default:
        result.notImplemented();
        break;
    }
  }


  // MethodChannel.Result wrapper that responds on the platform thread.
  private static class MethodResultWrapper implements Result {
    private Result methodResult;
    private Handler handler;

    MethodResultWrapper(Result result) {
      methodResult = result;
      handler = new Handler(Looper.getMainLooper());
    }

    @Override
    public void success(final Object result) {
      handler.post(new Runnable() {
        @Override
        public void run() {
          methodResult.success(result);
        }
      });
    }

    @Override
    public void error(final String errorCode, final String errorMessage, final Object errorDetails) {
      handler.post(new Runnable() {
        @Override
        public void run() {
          methodResult.error(errorCode, errorMessage, errorDetails);
        }
      });
    }

    @Override
    public void notImplemented() {
      handler.post(new Runnable() {
        @Override
        public void run() {
          methodResult.notImplemented();
        }
      });
    }
  }





  private final EventChannel.StreamHandler stateStreamHandler = new EventChannel.StreamHandler() {

    private final BroadcastReceiver mReceiver = new BroadcastReceiver() {
      @Override
      public void onReceive(Context context, Intent intent) {
        final String action = intent.getAction();

        Log.d(TAG, action);

        if (BluetoothAdapter.ACTION_STATE_CHANGED.equals(action)) {
          statusSink.success(intent.getIntExtra(BluetoothAdapter.EXTRA_STATE, -1));
        } else if (BluetoothDevice.ACTION_ACL_CONNECTED.equals(action)) {
          statusSink.success(1);
        } else if (BluetoothDevice.ACTION_ACL_DISCONNECTED.equals(action)) {
          statusSink.success(0);
        }
      }
    };

    @Override
    public void onListen(Object o, EventChannel.EventSink eventSink) {
      statusSink = eventSink;

      if (mContext != null) {
        mContext.registerReceiver(mReceiver, new IntentFilter(BluetoothAdapter.ACTION_STATE_CHANGED));
        mContext.registerReceiver(mReceiver, new IntentFilter(BluetoothDevice.ACTION_ACL_CONNECTED));
        mContext.registerReceiver(mReceiver, new IntentFilter(BluetoothDevice.ACTION_ACL_DISCONNECTED));
      }
    }

    @Override
    public void onCancel(Object o) {
      statusSink = null;
      if (mContext != null) {
        mContext.unregisterReceiver(mReceiver);
      }
    }
  };





  private final EventChannel.StreamHandler readResultsHandler = new EventChannel.StreamHandler() {
    @Override
    public void onListen(Object o, EventChannel.EventSink eventSink) {
      readSink = eventSink;
    }

    @Override
    public void onCancel(Object o) {
      readSink = null;
    }
  };

}
