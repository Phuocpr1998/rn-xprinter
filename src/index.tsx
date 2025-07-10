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

// Generate unique instance IDs
let instanceCounter = 0;
const generateInstanceId = (): string => {
  return `xprinter_instance_${++instanceCounter}_${Date.now()}`;
};

export class XPrinter {
  private instanceId: string;

  constructor() {
    this.instanceId = generateInstanceId();
    // Initialize the instance in the native module
    RnXprinter.createInstance(this.instanceId);
  }

  async netConnect(ip: string): Promise<any> {
    return RnXprinter.netConnect(this.instanceId, ip);
  }

  async serialConnect(serialPort: string): Promise<any> {
    return RnXprinter.serialConnect(this.instanceId, serialPort);
  }

  async usbConnect(device: string): Promise<any> {
    return RnXprinter.usbConnect(this.instanceId, device);
  }

  printQRCode(content: string): void {
    return RnXprinter.printQRCode(this.instanceId, content);
  }

  printText(content: string): void {
    return RnXprinter.printText(this.instanceId, content);
  }

  printBarcode(data: string, codeType: number): void {
    return RnXprinter.printBarcode(this.instanceId, data, codeType);
  }

  printBitmap(
    bitmapData: string,
    alignment: number,
    width: number,
    model: number
  ): void {
    return RnXprinter.printBitmap(this.instanceId, bitmapData, alignment, width, model);
  }

  tsplPrintBitmap(sWidth: number, sHeight: number, bitmapData: string, width: number): void {
    return RnXprinter.tsplPrintBitmap(this.instanceId, sWidth, sHeight, bitmapData, width);
  }

  closeConnection(): void {
    return RnXprinter.closeConnection(this.instanceId);
  }

  setCharSet(charSet: string): void {
    return RnXprinter.setCharSet(this.instanceId, charSet);
  }

  printPageModelData(): void {
    return RnXprinter.printPageModelData(this.instanceId);
  }

  tsplPrintTest(): void {
    return RnXprinter.tsplPrintTest(this.instanceId);
  }

  zplPrintTest(): void {
    return RnXprinter.zplPrintTest(this.instanceId);
  }

  cpclPrintTest(): void {
    return RnXprinter.cpclPrintTest(this.instanceId);
  }

  tsplFormFeed(sWidth: number, sHeight: number): void {
    return RnXprinter.tsplFormFeed(this.instanceId, sWidth, sHeight);
  }

  // Instance cleanup method
  dispose(): void {
    this.closeConnection();
    RnXprinter.destroyInstance(this.instanceId);
  }

  // Get the instance ID (useful for debugging)
  getInstanceId(): string {
    return this.instanceId;
  }
}

// Static utility methods (not instance-specific)
export async function getUsbDevices(): Promise<any> {
  return RnXprinter.getUsbDevices();
}

export async function getSerialDevices(): Promise<string> {
  return RnXprinter.getSerialDevices();
}

// Backward compatibility - export the class as default
export default XPrinter;

// For users who want to use the old approach (deprecated)
// These will be removed in future versions
export function netConnect(ip: string): Promise<any> {
  console.warn('netConnect is deprecated. Use new XPrinter().netConnect() instead.');
  return RnXprinter.netConnect('legacy', ip);
}

export function serialConnect(serialPort: string): Promise<any> {
  console.warn('serialConnect is deprecated. Use new XPrinter().serialConnect() instead.');
  return RnXprinter.serialConnect('legacy', serialPort);
}

export function usbConnect(device: string): Promise<any> {
  console.warn('usbConnect is deprecated. Use new XPrinter().usbConnect() instead.');
  return RnXprinter.usbConnect('legacy', device);
}

export function printQRCode(content: string): void {
  console.warn('printQRCode is deprecated. Use new XPrinter().printQRCode() instead.');
  return RnXprinter.printQRCode('legacy', content);
}

export function printText(content: string): void {
  console.warn('printText is deprecated. Use new XPrinter().printText() instead.');
  return RnXprinter.printText('legacy', content);
}

export function printBarcode(data: string, codeType: number): void {
  console.warn('printBarcode is deprecated. Use new XPrinter().printBarcode() instead.');
  return RnXprinter.printBarcode('legacy', data, codeType);
}

export function printBitmap(
  bitmapData: string,
  alignment: number,
  width: number,
  model: number
): void {
  console.warn('printBitmap is deprecated. Use new XPrinter().printBitmap() instead.');
  return RnXprinter.printBitmap('legacy', bitmapData, alignment, width, model);
}

export function tsplPrintBitmap(sWidth: number, sHeight: number, bitmapData: string, width: number): void {
  console.warn('tsplPrintBitmap is deprecated. Use new XPrinter().tsplPrintBitmap() instead.');
  return RnXprinter.tsplPrintBitmap('legacy', sWidth, sHeight, bitmapData, width);
}

export function closeConnection(): void {
  console.warn('closeConnection is deprecated. Use new XPrinter().closeConnection() instead.');
  return RnXprinter.closeConnection('legacy');
}

export function setCharSet(charSet: string): void {
  console.warn('setCharSet is deprecated. Use new XPrinter().setCharSet() instead.');
  return RnXprinter.setCharSet('legacy', charSet);
}

export function printPageModelData(): void {
  console.warn('printPageModelData is deprecated. Use new XPrinter().printPageModelData() instead.');
  return RnXprinter.printPageModelData('legacy');
}

export function tsplPrintTest(): void {
  console.warn('tsplPrintTest is deprecated. Use new XPrinter().tsplPrintTest() instead.');
  return RnXprinter.tsplPrintTest('legacy');
}

export function zplPrintTest(): void {
  console.warn('zplPrintTest is deprecated. Use new XPrinter().zplPrintTest() instead.');
  return RnXprinter.zplPrintTest('legacy');
}

export function cpclPrintTest(): void {
  console.warn('cpclPrintTest is deprecated. Use new XPrinter().cpclPrintTest() instead.');
  return RnXprinter.cpclPrintTest('legacy');
}

export function tsplFormFeed(sWidth: number, sHeight: number): void {
  console.warn('tsplFormFeed is deprecated. Use new XPrinter().tsplFormFeed() instead.');
  return RnXprinter.tsplFormFeed('legacy', sWidth, sHeight);
}


