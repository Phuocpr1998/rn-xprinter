# React Native XPrinter

A React Native module for XPrinter devices that provides various printing capabilities including text, QR codes, barcodes, and bitmap printing.

## Installation

```bash
npm install rn-xprinter
# or
yarn add rn-xprinter
```

### iOS Setup
```bash
cd ios && pod install
```

## Features

- Network printer connection
- USB device detection
- Text printing
- QR Code printing
- Barcode printing
- Bitmap printing
- Character set configuration
- Multiple printer language support (TSPL, ZPL, CPCL)
- **Multiple printer instances support (NEW!)**

## Usage

> ⚡ **New Instance-Based API (Recommended)**
> 
> The library now supports multiple printer instances! This allows you to connect to and manage multiple printers simultaneously.

### Creating Printer Instances

```typescript
import XPrinter from 'rn-xprinter';

// Create printer instances
const printer1 = new XPrinter();
const printer2 = new XPrinter();

// Each instance has a unique ID
console.log(printer1.getInstanceId()); // e.g., "xprinter_instance_1_1699123456789"
console.log(printer2.getInstanceId()); // e.g., "xprinter_instance_2_1699123456790"
```

### Connect to Multiple Printers

```typescript
// Connect each instance to different printers
await printer1.netConnect('192.168.1.100');
await printer2.netConnect('192.168.1.101');

// Or use USB/Serial connections
await printer1.usbConnect('USB001');
await printer2.serialConnect('/dev/ttyUSB0');
```

### Print with Specific Instances

```typescript
// Print different content on each printer
printer1.printText('Hello from Printer 1!');
printer2.printText('Hello from Printer 2!');

// Print QR codes
printer1.printQRCode('https://printer1.example.com');
printer2.printQRCode('https://printer2.example.com');

// Print barcodes
printer1.printBarcode('123456789', 0);
printer2.printBarcode('987654321', 1);
```

### Instance Management

```typescript
// Close specific printer connection
printer1.closeConnection();

// Dispose of instance (recommended when done)
printer1.dispose(); // This closes connection and cleans up resources

// Get static device information (not instance-specific)
import { getUsbDevices, getSerialDevices } from 'rn-xprinter';
const usbDevices = await getUsbDevices();
const serialDevices = await getSerialDevices();
```

### Complete Example

```typescript
import XPrinter, { getUsbDevices } from 'rn-xprinter';

const setupPrinters = async () => {
  // Create instances
  const mainPrinter = new XPrinter();
  const backupPrinter = new XPrinter();
  
  try {
    // Connect to different printers
    await mainPrinter.netConnect('192.168.1.100');
    await backupPrinter.netConnect('192.168.1.101');
    
    // Print receipts simultaneously
    mainPrinter.printText('Receipt #001 - Main Printer');
    backupPrinter.printText('Receipt #002 - Backup Printer');
    
    // Print QR codes with different content
    mainPrinter.printQRCode('https://main-receipt.com/001');
    backupPrinter.printQRCode('https://backup-receipt.com/002');
    
  } catch (error) {
    console.error('Printer setup failed:', error);
  } finally {
    // Clean up when done
    mainPrinter.dispose();
    backupPrinter.dispose();
  }
};
```

---

## Legacy API (Deprecated)

> ⚠️ **Deprecated**: The following global functions are deprecated and will be removed in future versions. Please use the instance-based API above.

### Connect to Printer

```typescript
import { netConnect, getUsbDevices } from 'rn-xprinter';

// Connect to network printer
await netConnect('192.168.1.100');

// Get USB devices
const devices = await getUsbDevices();
```

### Print Text

```typescript
import { printText, setCharSet } from 'rn-xprinter';

// Set character set (optional)
setCharSet('UTF-8');

// Print text
printText('Hello World!');
```

### Print QR Code

```typescript
import { printQRCode } from 'rn-xprinter';

printQRCode('https://example.com');
```

### Print Barcode

```typescript
import { printBarcode } from 'rn-xprinter';

// Print barcode with specified code type
printBarcode('123456789', 0); // 0 is the code type
```

### Print Bitmap

```typescript
import { printBitmap, tsplPrintBitmap } from 'rn-xprinter';

// Print bitmap with alignment and width
printBitmap(bitmapData, 0, 384, 0);

// TSPL bitmap printing
tsplPrintBitmap(384, 200, bitmapData, 384);
```

### Printer Language Tests

```typescript
import { tsplPrintTest, zplPrintTest, cpclPrintTest } from 'rn-xprinter';

// Test different printer languages
tsplPrintTest();
zplPrintTest();
cpclPrintTest();
```

### Close Connection

```typescript
import { closeConnection } from 'rn-xprinter';

closeConnection();
```

## API Reference

### Connection Methods
- `netConnect(ip: string): Promise<any>` - Connect to network printer
- `getUsbDevices(): Promise<any>` - Get list of USB devices
- `closeConnection(): void` - Close printer connection

### Printing Methods
- `printText(content: string): void` - Print text
- `printQRCode(content: string): void` - Print QR code
- `printBarcode(data: string, codeType: number): void` - Print barcode
- `printBitmap(bitmapData: string, alignment: number, width: number, model: number): void` - Print bitmap
- `tsplPrintBitmap(sWidth: number, sHeight: number, bitmapData: String, width: number): void` - TSPL bitmap printing
- `tsplFormFeed(sWidth: number, sHeight: number): void` - Form feed for TSPL printer with specified width and height

### Configuration Methods
- `setCharSet(charSet: String): void` - Set character set
- `printPageModelData(): void` - Print page model data

### Test Methods
- `tsplPrintTest(): void` - Test TSPL printing
- `zplPrintTest(): void` - Test ZPL printing
- `cpclPrintTest(): void` - Test CPCL printing

## Error Handling

The module will throw an error if it's not properly linked. Make sure to:
- Run `pod install` for iOS
- Rebuild the app after installing the package
- Not use Expo Go

## License

MIT

---

Made with [create-react-native-library](https://github.com/callstack/react-native-builder-bob)
