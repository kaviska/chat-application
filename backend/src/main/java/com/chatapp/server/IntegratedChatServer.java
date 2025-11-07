package com.chatapp.server;

import com.chatapp.database.DatabaseManager;

/**
 * Integrated Chat Server that runs both TCP and WebSocket servers
 */
public class IntegratedChatServer {
    private MainServer tcpServer;
    private WebSocketBridge webSocketBridge;
    private Thread tcpServerThread;
    private Thread webSocketThread;

    public void start() {
        System.out.println("╔═══════════════════════════════════════════╗");
        System.out.println("║        Starting Integrated Chat Server    ║");
        System.out.println("╚═══════════════════════════════════════════╝");
        
        // Test database connection first
        DatabaseManager dbManager = DatabaseManager.getInstance();
        if (!dbManager.testConnection()) {
            System.err.println("❌ Failed to connect to database. Please check your MySQL configuration.");
            System.err.println("Make sure:");
            System.err.println("1. MySQL server is running (XAMPP started)");
            System.err.println("2. Database 'chat_app' exists");
            System.err.println("3. Run the schema.sql file to create tables");
            return;
        }
        
        // Start TCP server in separate thread
        tcpServer = new MainServer();
        tcpServerThread = new Thread(() -> {
            tcpServer.start();
        });
        tcpServerThread.start();
        
        // Wait a moment for TCP server to start
        try {
            Thread.sleep(2000);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }
        
        // Start WebSocket bridge in separate thread
        webSocketBridge = new WebSocketBridge();
        webSocketThread = new Thread(() -> {
            webSocketBridge.start();
            try {
                Thread.currentThread().join();
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
            }
        });
        webSocketThread.start();
        
        System.out.println("\n🎉 Both servers are running!");
        System.out.println("📡 TCP Server: localhost:8081");
        System.out.println("🌐 WebSocket Server: ws://localhost:8082");
        System.out.println("💡 Frontend can now connect to ws://localhost:8082");
        
        // Setup shutdown hook
        Runtime.getRuntime().addShutdownHook(new Thread(this::shutdown));
    }

    public void shutdown() {
        System.out.println("\n🛑 Shutting down Integrated Chat Server...");
        
        if (webSocketBridge != null) {
            webSocketBridge.shutdownBridge();
        }
        
        if (tcpServer != null) {
            tcpServer.shutdown();
        }
        
        // Interrupt threads
        if (webSocketThread != null) {
            webSocketThread.interrupt();
        }
        
        if (tcpServerThread != null) {
            tcpServerThread.interrupt();
        }
        
        System.out.println("✅ Integrated Chat Server shutdown complete");
    }

    public static void main(String[] args) {
        IntegratedChatServer server = new IntegratedChatServer();
        server.start();
        
        // Keep main thread alive
        try {
            Thread.currentThread().join();
        } catch (InterruptedException e) {
            System.err.println("Main thread interrupted");
        }
    }
}