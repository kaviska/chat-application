// WebSocket to TCP Socket Bridge
// This server bridges WebSocket connections from the browser to the Java TCP server

const WebSocket = require('ws');
const net = require('net');

const WS_PORT = 8082;
const JAVA_SERVER_HOST = 'localhost';
const JAVA_SERVER_PORT = 8081;

const wss = new WebSocket.Server({ port: WS_PORT });

console.log('╔════════════════════════════════════════╗');
console.log('║   WebSocket Bridge Server Started      ║');
console.log('╚════════════════════════════════════════╝');
console.log(`📡 WebSocket Server: ws://localhost:${WS_PORT}`);
console.log(`🔌 Java Server: ${JAVA_SERVER_HOST}:${JAVA_SERVER_PORT}`);
console.log('⏳ Waiting for connections...\n');

wss.on('connection', (ws) => {
  console.log('🔌 New WebSocket client connected');

  // Create TCP connection to Java server
  const tcpClient = new net.Socket();

  tcpClient.connect(JAVA_SERVER_PORT, JAVA_SERVER_HOST, () => {
    console.log('✅ Connected to Java server');
  });

  // Forward WebSocket messages to TCP socket
  ws.on('message', (message) => {
    try {
      const data = message.toString();
      console.log('📤 WS → TCP:', data);
      tcpClient.write(data + '\n'); // Java server expects newline-delimited messages
    } catch (error) {
      console.error('❌ Error forwarding to TCP:', error);
    }
  });

  // Forward TCP socket messages to WebSocket
  tcpClient.on('data', (data) => {
    try {
      const messages = data.toString().split('\n').filter(msg => msg.trim());
      messages.forEach(msg => {
        console.log('📥 TCP → WS:', msg);
        ws.send(msg);
      });
    } catch (error) {
      console.error('❌ Error forwarding to WebSocket:', error);
    }
  });

  // Handle disconnections
  ws.on('close', () => {
    console.log('👋 WebSocket client disconnected');
    tcpClient.end();
  });

  tcpClient.on('close', () => {
    console.log('🔌 TCP connection closed');
    ws.close();
  });

  tcpClient.on('error', (error) => {
    console.error('❌ TCP error:', error.message);
    ws.close();
  });

  ws.on('error', (error) => {
    console.error('❌ WebSocket error:', error.message);
    tcpClient.end();
  });
});

wss.on('error', (error) => {
  console.error('❌ WebSocket Server error:', error);
});

// Graceful shutdown
process.on('SIGINT', () => {
  console.log('\n🛑 Shutting down WebSocket bridge...');
  wss.close(() => {
    console.log('✅ WebSocket server closed');
    process.exit(0);
  });
});
