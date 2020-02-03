import 'dart:async';

import 'package:flutter/services.dart';


class OngbauPrinterInternal {
  static const int STATE_OFF = 10;
  static const int STATE_TURNING_ON = 11;
  static const int STATE_ON = 12;
  static const int STATE_TURNING_OFF = 13;
  static const int STATE_BLE_TURNING_ON = 14;
  static const int STATE_BLE_ON = 15;
  static const int STATE_BLE_TURNING_OFF = 16;
  static const int ERROR = -1;
  static const int CONNECTED = 1;
  static const int DISCONNECTED = 0;

  static const String namespace = 'ongbau_printer_internal';
  static const MethodChannel _channel = const MethodChannel('$namespace/methods');
  static const EventChannel _stateChannel = const EventChannel('$namespace/state');

  static const String type_device = "internal";


  final StreamController<MethodCall> _methodStreamController = new StreamController.broadcast();

  OngbauPrinterInternal._() {
    // ignore: missing_return
    _channel.setMethodCallHandler((MethodCall call) {_methodStreamController.add(call);});
  }

  static OngbauPrinterInternal _instance = new OngbauPrinterInternal._();

  static OngbauPrinterInternal get instance => _instance;



  static Future<String> get platformVersion async {
    final String version = await _channel.invokeMethod('getPlatformVersion');
    return version;
  }


  Stream<int> onStateChanged() => _stateChannel.receiveBroadcastStream().map((buffer) => buffer);

  Future<bool> get isConnected  => _channel.invokeMethod('isConnected', {'typeDevice': type_device});

  Future<dynamic> connect() => _channel.invokeMethod('connect', {'typeDevice': type_device});

  Future<dynamic> disconnect() => _channel.invokeMethod('disconnect', {'typeDevice': type_device});

  Future<dynamic> printLeftRight(String string1,String string2,int size) =>
      _channel.invokeMethod('printLeftRight', {'typeDevice': type_device, 'string1': string1, 'string2': string2,'size': size });

  Future<dynamic> printCustom(String message, int size, int align) =>
      _channel.invokeMethod('printCustom', {'typeDevice': type_device, 'message': message, 'size': size, 'align': align});

  Future<dynamic> printNewLine() => _channel.invokeMethod('printNewLine', {'typeDevice': type_device});

  Future<dynamic> paperCut() => _channel.invokeMethod('paperCut', {'typeDevice': type_device});

  Future<dynamic> printImage(String pathImage) =>
      _channel.invokeMethod('printImage', {'typeDevice': type_device, 'pathImage': pathImage});

  Future<dynamic> printQRcode(String textToQR, int width, int height, int align) =>
      _channel.invokeMethod('printQRcode', {'typeDevice': type_device,'textToQR': textToQR, 'width': width, 'height': height, 'align': align});


}
