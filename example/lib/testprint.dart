import 'package:ongbau_printer_internal/ongbau_printer_internal.dart';

class TestPrint {
    OngbauPrinterInternal internalPrinter = OngbauPrinterInternal.instance;

   sample(String pathImage) async {
    //SIZE
    // 0- normal size text
    // 1- only bold text
    // 2- bold with medium text
    // 3- bold with large text
    //ALIGN
    // 0- ESC_ALIGN_LEFT
    // 1- ESC_ALIGN_CENTER
    // 2- ESC_ALIGN_RIGHT

       internalPrinter.isConnected.then((isConnected) {
      if (isConnected) {
          internalPrinter.printNewLine();
          //internalPrinter.printLeftRight("Trái 0", "Phải 1", 0);
          internalPrinter.printCustom("In trái", 0, 0, false);
          internalPrinter.printCustom("In phải", 0, 2, false);
          internalPrinter.printCustom("In giữa", 0, 1, false);
          internalPrinter.printNewLine();
      }
    });
  }
}