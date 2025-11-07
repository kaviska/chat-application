package com.chatapp.server;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.InetSocketAddress;
import java.net.Socket;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import org.java_websocket.WebSocket;
import org.java_websocket.handshake.ClientHandshake;
import org.java_websocket.server.WebSocketServer;

/**
 * WebSocket Bridge Server that bridges WebSocket connections to the TCP chat server
 */
public class WebSocketBridge extends WebSocketServer {
    private static final int WS_PORT = 8082;
    private static final String JAVA_SERVER_HOST = "localhost";
    private static final int JAVA_SERVER_PORT = 8081;
    
    private final ConcurrentHashMap<WebSocket, Socket> wsToTcpMap;
    private final ConcurrentHashMap<WebSocket, BufferedReader> wsToReaderMap;
    private final ConcurrentHashMap<WebSocket, PrintWriter> wsToWriterMap;
    private final ExecutorService executorService;

    public WebSocketBridge() {
        super(new InetSocketAddress(WS_PORT));
        this.wsToTcpMap = new ConcurrentHashMap<>();
        this.wsToReaderMap = new ConcurrentHashMap<>();
        this.wsToWriterMap = new ConcurrentHashMap<>();
        this.executorService = Executors.newCachedThreadPool();
        
        System.out.println("╔════════════════════════════════════════╗");
        System.out.println("║   WebSocket Bridge Server Started      ║");
        System.out.println("╚════════════════════════════════════════╝");
        System.out.println("📡 WebSocket Server: ws://localhost:" + WS_PORT);
        System.out.println("🔌 Java Server: " + JAVA_SERVER_HOST + ":" + JAVA_SERVER_PORT);
        System.out.println("⏳ Waiting for connections...\n");
    }

    @Override
    public void onOpen(WebSocket conn, ClientHandshake handshake) {
        System.out.println("🔌 New WebSocket client connected: " + conn.getRemoteSocketAddress());
        
        // Create TCP connection to Java server in a separate thread
        executorService.submit(() -> connectToTcpServer(conn));
    }

    private void connectToTcpServer(WebSocket ws) {
        try {
            Socket tcpSocket = new Socket(JAVA_SERVER_HOST, JAVA_SERVER_PORT);
            BufferedReader reader = new BufferedReader(new InputStreamReader(tcpSocket.getInputStream()));
            PrintWriter writer = new PrintWriter(tcpSocket.getOutputStream(), true);
            
            wsToTcpMap.put(ws, tcpSocket);
            wsToReaderMap.put(ws, reader);
            wsToWriterMap.put(ws, writer);
            
            System.out.println("✅ Connected to Java server for WebSocket: " + ws.getRemoteSocketAddress());
            
            // Start listening for messages from TCP server
            executorService.submit(() -> listenToTcpServer(ws, reader));
            
        } catch (IOException e) {
            System.err.println("❌ Failed to connect to TCP server: " + e.getMessage());
            ws.close();
        }
    }

    private void listenToTcpServer(WebSocket ws, BufferedReader reader) {
        try {
            String message;
            while ((message = reader.readLine()) != null && ws.isOpen()) {
                System.out.println("📥 TCP → WS: " + message);
                ws.send(message);
            }
        } catch (IOException e) {
            if (ws.isOpen()) {
                System.err.println("❌ Error reading from TCP server: " + e.getMessage());
                ws.close();
            }
        }
    }

    @Override
    public void onMessage(WebSocket conn, String message) {
        try {
            System.out.println("📤 WS → TCP: " + message);
            PrintWriter writer = wsToWriterMap.get(conn);
            if (writer != null) {
                writer.println(message);
            } else {
                System.err.println("❌ No TCP connection found for WebSocket");
            }
        } catch (Exception e) {
            System.err.println("❌ Error forwarding message to TCP: " + e.getMessage());
        }
    }

    @Override
    public void onClose(WebSocket conn, int code, String reason, boolean remote) {
        System.out.println("👋 WebSocket client disconnected: " + conn.getRemoteSocketAddress());
        cleanupConnection(conn);
    }

    @Override
    public void onError(WebSocket conn, Exception ex) {
        System.err.println("❌ WebSocket error: " + ex.getMessage());
        if (conn != null) {
            cleanupConnection(conn);
        }
    }

    private void cleanupConnection(WebSocket ws) {
        try {
            // Close TCP connection
            Socket tcpSocket = wsToTcpMap.remove(ws);
            if (tcpSocket != null && !tcpSocket.isClosed()) {
                tcpSocket.close();
            }
            
            // Close reader
            BufferedReader reader = wsToReaderMap.remove(ws);
            if (reader != null) {
                reader.close();
            }
            
            // Close writer
            PrintWriter writer = wsToWriterMap.remove(ws);
            if (writer != null) {
                writer.close();
            }
            
            System.out.println("🔌 TCP connection cleaned up for WebSocket");
        } catch (IOException e) {
            System.err.println("❌ Error cleaning up connection: " + e.getMessage());
        }
    }

    @Override
    public void onStart() {
        System.out.println("🚀 WebSocket server started successfully!");
        setConnectionLostTimeout(0);
        setConnectionLostTimeout(100);
    }

    public void shutdownBridge() {
        System.out.println("\n🛑 Shutting down WebSocket bridge...");
        
        // Clean up all connections
        wsToTcpMap.keySet().forEach(this::cleanupConnection);
        
        // Shutdown executor service
        executorService.shutdown();
        
        try {
            this.stop();
            System.out.println("✅ WebSocket server closed");
        } catch (InterruptedException e) {
            System.err.println("❌ Error stopping WebSocket server: " + e.getMessage());
        }
    }

    public static void main(String[] args) {
        WebSocketBridge server = new WebSocketBridge();
        server.start();

        // Graceful shutdown hook
        Runtime.getRuntime().addShutdownHook(new Thread(server::shutdownBridge));

        // Keep the main thread alive
        try {
            Thread.currentThread().join();
        } catch (InterruptedException e) {
            System.err.println("Main thread interrupted");
        }
    }
}