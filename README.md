# rn-xprinter

React Native SDK for connecting and printing with **XPrinter** devices over a network.

## Features

- Connect to XPrinter via IP
- Print text, QR codes, barcodes, and bitmap images
- Customize alignment, width, model
- Set character set for text encoding
- Close printer connection

## Installation

```sh
npm install rn-xprinter
```

## Usage

```js
import {
  netConnect,
  printText,
  printQRCode,
  printBarcode,
  printBitmap,
  closeConnection,
  setCharSet,
} from 'rn-xprinter';

await netConnect('192.168.0.100'); // connect to printer

printText('Hello, printer!');
printQRCode('https://example.com');
printBarcode('123456789012', 73); // e.g., 73 for CODE128
printBitmap('base64 of bitmap', 1, 300, 0); // center aligned bitmap
setCharSet('UTF-8'); // optional charset setting

closeConnection(); // close printer connection
```

## API

### `netConnect(ip: string): Promise<any>`

Connect to the printer over the network.

- `ip`: IP address of the XPrinter device  
**Returns**: a Promise resolving when connected

---

### `printText(content: string): void`

Prints plain text.

- `content`: The string content to print

---

### `printQRCode(content: string): void`

Prints a QR Code.

- `content`: The content encoded into the QR Code

---

### `printBarcode(data: string, codeType: number): void`

Prints a 1D barcode.

- `data`: The barcode data string
- `codeType`: Integer type of barcode (e.g., 65 for UPC-A, 73 for CODE128)

---

### `printBitmap(bitmapData: string, alignment: number, width: number, model: number): void`

Prints an image from a bitmap data

- `bitmapData`: Base64 of image (e.g., PNG/JPG)
- `alignment`: 0 = left, 1 = center, 2 = right
- `width`: Image width in pixels
- `model`: Reserved or mode selector (printer-specific)

---

### `tsplPrintBitmap(sWidth: number, sHeight: number, bitmapData: String, width: number): void `

Prints an image from a bitmap data

- `sWidth`: mm
- `sHeight`: mm
- `bitmapData`: Base64 of image (e.g., PNG/JPG)
- `width`: Image width in pixels

---

### `setCharSet(charSet: string): void`

Set the character encoding used when printing text (e.g., for UTF-8, Vietnamese, etc).

- `charSet`: Character set string (e.g., `"UTF-8"`, `"GBK"`)

---

### `closeConnection(): void`

Close the printer connection.

---

### `printPageModelData(): void`

Prints data in page mode.

---

### `tsplPrintTest(): void`

Sends a TSPL test print command to the printer.

---

### `zplPrintTest(): void`

Sends a ZPL test print command to the printer.

---

### `cpclPrintTest(): void`

Sends a CPCL test print command to the printer.

---

### `getUsbDevices(): Promise<any>`

Retrieves a list of connected USB devices.

**Returns**: a Promise resolving to the list of USB devices.

## Contributing

See the [contributing guide](CONTRIBUTING.md) to learn how to contribute to the repository and the development workflow.

## License

MIT

---

Made with [create-react-native-library](https://github.com/callstack/react-native-builder-bob)
