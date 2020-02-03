import 'package:flutter/services.dart';
import 'package:flutter_test/flutter_test.dart';
import 'package:ongbau_printer_internal/ongbau_printer_internal.dart';

void main() {
  const MethodChannel channel = MethodChannel('ongbau_printer_internal');

  TestWidgetsFlutterBinding.ensureInitialized();

  setUp(() {
    channel.setMockMethodCallHandler((MethodCall methodCall) async {
      return '42';
    });
  });

  tearDown(() {
    channel.setMockMethodCallHandler(null);
  });

  test('getPlatformVersion', () async {
    expect(await OngbauPrinterInternal.platformVersion, '42');
  });
}
