import XPrinter, { getUsbDevices, getSerialDevices } from 'rn-xprinter';
import { Text, View, StyleSheet, ScrollView, Button, Alert } from 'react-native';
import { useState, useEffect } from 'react';

export default function App() {
  const [results, setResults] = useState<string[]>([]);
  const [printers, setPrinters] = useState<{ [key: string]: XPrinter }>({});

  const addResult = (message: string) => {
    setResults(prev => [...prev, `${new Date().toLocaleTimeString()}: ${message}`]);
  };

  const createPrinterInstance = (name: string) => {
    if (printers[name]) {
      addResult(`Printer ${name} already exists`);
      return;
    }

    const printer = new XPrinter();
    setPrinters(prev => ({ ...prev, [name]: printer }));
    addResult(`Created printer instance: ${name} (ID: ${printer.getInstanceId()})`);
  };

  const connectToPrinter = async (printerName: string, ip: string) => {
    const printer = printers[printerName];
    if (!printer) {
      addResult(`Printer ${printerName} not found. Create it first.`);
      return;
    }

    try {
      await printer.netConnect(ip);
      addResult(`${printerName} connected to ${ip}`);
    } catch (error) {
      addResult(`${printerName} connection failed: ${error}`);
    }
  };

  const printWithPrinter = (printerName: string, text: string) => {
    const printer = printers[printerName];
    if (!printer) {
      addResult(`Printer ${printerName} not found`);
      return;
    }

    try {
      printer.printText(text);
      addResult(`${printerName} printed: "${text}"`);
    } catch (error) {
      addResult(`${printerName} print failed: ${error}`);
    }
  };

  const disposePrinter = (printerName: string) => {
    const printer = printers[printerName];
    if (!printer) {
      addResult(`Printer ${printerName} not found`);
      return;
    }

    printer.dispose();
    setPrinters(prev => {
      const newPrinters = { ...prev };
      delete newPrinters[printerName];
      return newPrinters;
    });
    addResult(`Disposed printer: ${printerName}`);
  };

  const testMultiplePrinters = async () => {
    // Create multiple printer instances
    createPrinterInstance('Printer1');
    createPrinterInstance('Printer2');

    // Simulate connecting to different printers
    await connectToPrinter('Printer1', '192.168.1.100');
    await connectToPrinter('Printer2', '192.168.1.101');

    // Print different content on each printer
    printWithPrinter('Printer1', 'Hello from Printer 1!');
    printWithPrinter('Printer2', 'Hello from Printer 2!');
  };

  const getDevices = async () => {
    try {
      const usbDevices = await getUsbDevices();
      const serialDevices = await getSerialDevices();
      addResult(`USB Devices: ${JSON.stringify(usbDevices)}`);
      addResult(`Serial Devices: ${JSON.stringify(serialDevices)}`);
    } catch (error) {
      addResult(`Get devices failed: ${error}`);
    }
  };

  useEffect(() => {
    addResult('XPrinter Multi-Instance Example Started');
    addResult('This example demonstrates multiple printer instances');
  }, []);

  return (
    <ScrollView style={styles.container}>
      <View style={styles.header}>
        <Text style={styles.title}>XPrinter Multi-Instance Example</Text>
        <Text style={styles.subtitle}>
          Active Instances: {Object.keys(printers).length}
        </Text>
      </View>

      <View style={styles.buttonContainer}>
        <Button
          title="Create Printer1"
          onPress={() => createPrinterInstance('Printer1')}
        />
        <Button
          title="Create Printer2"
          onPress={() => createPrinterInstance('Printer2')}
        />
        <Button
          title="Test Multiple Printers"
          onPress={testMultiplePrinters}
        />
        <Button
          title="Get Devices"
          onPress={getDevices}
        />
      </View>

      <View style={styles.buttonContainer}>
        <Button
          title="Connect P1 (192.168.1.100)"
          onPress={() => connectToPrinter('Printer1', '192.168.1.100')}
        />
        <Button
          title="Connect P2 (192.168.1.101)"
          onPress={() => connectToPrinter('Printer2', '192.168.1.101')}
        />
      </View>

      <View style={styles.buttonContainer}>
        <Button
          title="Print with P1"
          onPress={() => printWithPrinter('Printer1', 'Test from P1')}
        />
        <Button
          title="Print with P2"
          onPress={() => printWithPrinter('Printer2', 'Test from P2')}
        />
      </View>

      <View style={styles.buttonContainer}>
        <Button
          title="Dispose Printer1"
          onPress={() => disposePrinter('Printer1')}
          color="red"
        />
        <Button
          title="Dispose Printer2"
          onPress={() => disposePrinter('Printer2')}
          color="red"
        />
      </View>

      <View style={styles.logContainer}>
        <Text style={styles.logTitle}>Activity Log:</Text>
        {results.map((result, index) => (
          <Text key={index} style={styles.logText}>
            {result}
          </Text>
        ))}
      </View>
    </ScrollView>
  );
}

const styles = StyleSheet.create({
  container: {
    flex: 1,
    backgroundColor: '#f5f5f5',
  },
  header: {
    alignItems: 'center',
    paddingVertical: 20,
    backgroundColor: '#fff',
    marginBottom: 10,
  },
  title: {
    fontSize: 20,
    fontWeight: 'bold',
    color: '#333',
  },
  subtitle: {
    fontSize: 14,
    color: '#666',
    marginTop: 5,
  },
  buttonContainer: {
    flexDirection: 'row',
    justifyContent: 'space-around',
    marginVertical: 10,
    paddingHorizontal: 10,
  },
  logContainer: {
    margin: 10,
    padding: 10,
    backgroundColor: '#fff',
    borderRadius: 5,
  },
  logTitle: {
    fontSize: 16,
    fontWeight: 'bold',
    marginBottom: 10,
    color: '#333',
  },
  logText: {
    fontSize: 12,
    color: '#666',
    marginBottom: 5,
    fontFamily: 'monospace',
  },
});
