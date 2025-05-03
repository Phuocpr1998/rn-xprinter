import { NativeModules, Platform } from 'react-native';

const LINKING_ERROR =
  `The package 'rn-xprinter' doesn't seem to be linked. Make sure: \n\n` +
  Platform.select({ ios: "- You have run 'pod install'\n", default: '' }) +
  '- You rebuilt the app after installing the package\n' +
  '- You are not using Expo Go\n';

const RnXprinter = NativeModules.RnXprinter
  ? NativeModules.RnXprinter
  : new Proxy(
    {},
    {
      get() {
        throw new Error(LINKING_ERROR);
      },
    }
  );

export function netConnect(ip: string): Promise<any> {
  return RnXprinter.netConnect(ip);
}

export function printQRCode(content: string): void {
  return RnXprinter.printQRCode(content);
}

export function printText(content: string): void {
  return RnXprinter.printText(content);
}

export function printBarcode(data: string, codeType: number): void {
  return RnXprinter.printBarcode(data, codeType);
}

export function printBitmap(
  bitmapData: string,
  alignment: number,
  width: number,
  model: number
): void {
  return RnXprinter.printBitmap(bitmapData, alignment, width, model);
}

export function closeConnection(): void {
  return RnXprinter.closeConnection();
}

export function setCharSet(charSet: String): void {
  return RnXprinter.setCharSet(charSet);
}


export function printPageModelData(): void {
  return RnXprinter.printPageModelData();
}


export function tsplPrintTest(): void {
  return RnXprinter.tsplPrintTest();
}


export function zplPrintTest(): void {
  return RnXprinter.zplPrintTest();
}

export function cpclPrintTest(): void {
  return RnXprinter.cpclPrintTest();
}


export function getUsbDevices(): Promise<any> {
  return RnXprinter.getUsbDevices();
}

