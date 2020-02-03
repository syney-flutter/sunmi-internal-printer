import 'dart:async';
import 'dart:io';
import 'dart:typed_data';

import 'package:flutter/material.dart';
import 'package:flutter/services.dart';
import 'package:ongbau_printer_internal/ongbau_printer_internal.dart';
import 'package:ongbau_printer_internal_example/testprint.dart';


class InternalPrinterWidget extends StatefulWidget {
    @override
    _InternalPrinterState createState() => new _InternalPrinterState();
}


class _InternalPrinterState extends State<InternalPrinterWidget> {

    static final String TAG = "InternalPrinterState";


    OngbauPrinterInternal mInternalPrinter = OngbauPrinterInternal.instance;
    bool _connected = false;


    String pathImage;
    TestPrint testPrint;

    @override
    void initState() {
        super.initState();
        initPlatformState();
        testPrint= TestPrint();
    }


    @override
    void dispose() {
        _disconnect();
        super.dispose();
    }



    Future<void> initPlatformState() async {
        bool isConnected = await mInternalPrinter.isConnected;

        if (!isConnected){
            var result = await mInternalPrinter.connect();
            if (result == bool){
                isConnected = true;
            } else {
                isConnected = false;
            }
        }

        debugPrint('$TAG: initPlatformState : isConnected = $isConnected');

        if (!mounted) return;

        if(isConnected) {
            setState(() {
                _connected = true;
            });
        }
    }

    @override
    Widget build(BuildContext context) {
        return MaterialApp(
            home: Scaffold(
                appBar: AppBar(
                    title: Text('Blue Thermal Printer'),
                ),
                body: Container(
                    child: Padding(
                        padding: const EdgeInsets.all(8.0),
                        child: ListView(
                            children: <Widget>[
                                Row(
                                    crossAxisAlignment: CrossAxisAlignment.center,
                                    mainAxisAlignment: MainAxisAlignment.start,
                                    children: <Widget>[
                                        SizedBox(width: 10,),
                                        Text(
                                            'Device:',
                                            style: TextStyle(
                                                fontWeight: FontWeight.bold,
                                            ),
                                        ),
                                        SizedBox(width: 30,),
                                    ],
                                ),
                                SizedBox(height: 10,),
                                Row(
                                    crossAxisAlignment: CrossAxisAlignment.center,
                                    mainAxisAlignment: MainAxisAlignment.end,
                                    children: <Widget>[
                                        RaisedButton(
                                            color: Colors.brown,
                                            onPressed:(){
                                                initPlatformState();
                                            },
                                            child: Text('Refresh', style: TextStyle(color: Colors.white),),
                                        ),
                                        SizedBox(width: 20,),
                                        RaisedButton(
                                            color: _connected ?Colors.red:Colors.green,
                                            onPressed:
                                            _connected ? _disconnect : _connect,
                                            child: Text(_connected ? 'Disconnect' : 'Connect', style: TextStyle(color: Colors.white),),
                                        ),
                                    ],
                                ),
                                Padding(
                                    padding: const EdgeInsets.only(left: 10.0, right: 10.0, top: 50),
                                    child:  RaisedButton(
                                        color: Colors.brown,
                                        onPressed:(){
                                            testPrint.sample(pathImage);
                                        },
                                        child: Text('PRINT TEST', style: TextStyle(color: Colors.white)),
                                    ),
                                ),
                            ],
                        ),
                    ),
                ),
            ),
        );
    }

    void _connect() async {
        bool result = await mInternalPrinter.isConnected;
        if (!result){
            var resultConnect = await mInternalPrinter.connect();
            if (resultConnect.runtimeType == bool){
                setState(() => _connected = true);
            } else {
                setState(() => _connected = false);
            }
        } else {
            setState(() => _connected = true);
        }

        debugPrint('$TAG: _connect: _connected = $_connected');
    }


    void _disconnect() async {
        var result = await mInternalPrinter.disconnect();
        if (result.runtimeType == bool){
            setState(() => _connected = false);
        } else {
            setState(() => _connected = true);
        }

        debugPrint('$TAG: _disconnect: _connected = $_connected');
    }

//write to app path
    Future<void> writeToFile(ByteData data, String path) {
        final buffer = data.buffer;
        return new File(path).writeAsBytes(
            buffer.asUint8List(data.offsetInBytes, data.lengthInBytes));
    }



    Future show(
        String message, {
            Duration duration: const Duration(seconds: 3),
        }) async {
        await new Future.delayed(new Duration(milliseconds: 100));
        Scaffold.of(context).showSnackBar(
            new SnackBar(
                content: new Text(
                    message,
                    style: new TextStyle(
                        color: Colors.white,
                    ),
                ),
                duration: duration,
            ),
        );
    }
}