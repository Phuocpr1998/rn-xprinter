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

## Usage

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
