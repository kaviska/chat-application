package com.chatapp.server;

import com.chatapp.auth.UserAuthService;
import com.chatapp.database.DatabaseManager;
import com.chatapp.model.Message;
import com.chatapp.model.User;
import com.google.gson.Gson;

import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class MainServer {
    private static final int PORT = 8081;
    private final Map<String, ClientHandler> connectedClients;
    private final ExecutorService threadPool;
    private ServerSocket serverSocket;
    private final UserAuthService authService;
    private final Gson gson;
    private volatile boolean running = false;

    public MainServer() {
        this.connectedClients = new ConcurrentHashMap<>();
        this.threadPool = Executors.newCachedThreadPool();
        this.authService = new UserAuthService();
        this.gson = new Gson();
    }

    public void start() {
        try {
            // Test database connection
            DatabaseManager dbManager = DatabaseManager.getInstance();
            if (!dbManager.testConnection()) {
                System.err.println("❌ Failed to connect to database. Please check your MySQL configuration.");
                System.err.println("Make sure:");
                System.err.println("1. MySQL server is running (XAMPP started)");
                System.err.println("2. Database 'chat_app' exists");
                System.err.println("3. Run the schema.sql file to create tables");
                return;
            }

            serverSocket = new ServerSocket(PORT);
            running = true;

            System.out.println("╔════════════════════════════════════════╗");
            System.out.println("║   🚀 Chat Server Started Successfully  ║");
            System.out.println("╚════════════════════════════════════════╝");
            System.out.println("📡 Server listening on port: " + PORT);
            System.out.println("💾 Database: Connected");
            System.out.println("🔐 Authentication: Enabled");
            System.out.println("⏳ Waiting for clients...\n");

            // Accept client connections
            while (running) {
                try {
                    Socket clientSocket = serverSocket.accept();
                    System.out.println("🔌 New client connected: " + clientSocket.getInetAddress());

                    ClientHandler clientHandler = new ClientHandler(clientSocket, this);
                    threadPool.execute(clientHandler);

                } catch (IOException e) {
                    if (running) {
                        System.err.println("❌ Error accepting client connection: " + e.getMessage());
                    }
                }
            }

        } catch (IOException e) {
            System.err.println("❌ Could not start server on port " + PORT);
            e.printStackTrace();
        } finally {
            shutdown();
        }
    }

    /**
     * Add a client to the connected clients map
     */
    public void addClient(String email, ClientHandler handler) {
        connectedClients.put(email, handler);
        System.out.println("👤 Client added: " + email + " (Total: " + connectedClients.size() + ")");
    }

    /**
     * Remove a client from connected clients
     */
    public void removeClient(String email) {
        connectedClients.remove(email);
        System.out.println("👋 Client removed: " + email + " (Total: " + connectedClients.size() + ")");
    }

    /**
     * Broadcast message to all connected clients except the sender
     */
    public void broadcast(String message, String senderEmail) {
        for (Map.Entry<String, ClientHandler> entry : connectedClients.entrySet()) {
            if (senderEmail == null || !entry.getKey().equals(senderEmail)) {
                entry.getValue().sendMessage(message);
            }
        }
    }

    /**
     * Send message to a specific user
     */
    public void sendToUser(String email, String message) {
        ClientHandler handler = connectedClients.get(email);
        if (handler != null) {
            handler.sendMessage(message);
        } else {
            System.err.println("❌ User not found: " + email);
        }
    }

    /**
     * Broadcast updated user list to all clients
     */
    public void broadcastUserList() {
        List<User> onlineUsers = authService.getOnlineUsers();
        
        Message userListMsg = new Message();
        userListMsg.setType("user_list");
        userListMsg.setContent(gson.toJson(onlineUsers));
        
        broadcast(userListMsg.toJson(), null);
    }

    /**
     * Get number of connected clients
     */
    public int getConnectedClientsCount() {
        return connectedClients.size();
    }

    /**
     * Shutdown server gracefully
     */
    public void shutdown() {
        running = false;
        
        System.out.println("\n🛑 Shutting down server...");

        // Close all client connections
        for (ClientHandler handler : connectedClients.values()) {
            try {
                if (handler.getUserEmail() != null) {
                    authService.updateUserStatus(handler.getUserEmail(), "offline");
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        }

        connectedClients.clear();

        // Shutdown thread pool
        threadPool.shutdown();

        // Close server socket
        try {
            if (serverSocket != null && !serverSocket.isClosed()) {
                serverSocket.close();
            }
        } catch (IOException e) {
            e.printStackTrace();
        }

        System.out.println("✅ Server shutdown complete");
    }

    public static void main(String[] args) {
        MainServer server = new MainServer();
        
        // Add shutdown hook for graceful shutdown
        Runtime.getRuntime().addShutdownHook(new Thread(() -> {
            System.out.println("\n⚠️  Shutdown signal received");
            server.shutdown();
        }));

        server.start();
    }
}
