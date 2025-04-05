import { netConnect, printText } from 'rn-xprinter';
import { Text, View, StyleSheet } from 'react-native';
import { useState, useEffect } from 'react';

export default function App() {
  const [result, setResult] = useState<number | undefined>();

  useEffect(() => {
    netConnect('192.168.1.103')
      .then(() => {
        printText('Test');
      })
      .catch((e) => {
        setResult(e);
      });
  }, []);

  return (
    <View style={styles.container}>
      <Text>Result: {result}</Text>
    </View>
  );
}

const styles = StyleSheet.create({
  container: {
    flex: 1,
    alignItems: 'center',
    justifyContent: 'center',
  },
});
