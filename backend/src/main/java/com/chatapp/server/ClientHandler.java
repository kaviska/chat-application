package com.chatapp.server;

import com.chatapp.auth.UserAuthService;
import com.chatapp.database.DatabaseManager;
import com.chatapp.database.MessageRepository;
import com.chatapp.model.Message;
import com.chatapp.model.User;
import com.google.gson.Gson;
import com.google.gson.JsonObject;

import java.io.*;
import java.net.Socket;
import java.util.List;

public class ClientHandler implements Runnable {
    private final Socket clientSocket;
    private final MainServer server;
    private PrintWriter out;
    private BufferedReader in;
    private String userEmail;
    private String username;
    private final UserAuthService authService;
    private final MessageRepository messageRepository;
    private final Gson gson;

    public ClientHandler(Socket socket, MainServer server) {
        this.clientSocket = socket;
        this.server = server;
        this.authService = new UserAuthService();
        this.messageRepository = new MessageRepository();
        this.gson = new Gson();
    }

    @Override
    public void run() {
        try {
            in = new BufferedReader(new InputStreamReader(clientSocket.getInputStream()));
            out = new PrintWriter(clientSocket.getOutputStream(), true);

            String inputLine;
            while ((inputLine = in.readLine()) != null) {
                System.out.println("📨 Received: " + inputLine);
                handleMessage(inputLine);
            }

        } catch (IOException e) {
            System.err.println("❌ Client disconnected: " + userEmail);
        } finally {
            cleanup();
        }
    }

    private void handleMessage(String jsonMessage) {
        try {
            Message message = Message.fromJson(jsonMessage);

            switch (message.getType()) {

                case "register":
                    handleRegister(message);
                    break;

                case "login":
                    handleLogin(message);
                    break;

                case "message":
                    handlePublicMessage(message);
                    break;

                case "file":
                    handleFileMessage(jsonMessage);
                    break;

                case "private_message":
                    handlePrivateMessage(message);
                    break;

                case "get_users":
                    handleGetUsers();
                    break;

                case "get_history":
                    handleGetHistory(message);
                    break;

                case "typing":
                    handleTyping(message);
                    break;

                case "logout":
                    handleLogout();
                    break;

                default:
                    sendError("Unknown message type: " + message.getType());
            }

        } catch (Exception e) {
            e.printStackTrace();
            sendError("Invalid message format");
        }
    }

    // ✅ FILE MESSAGE HANDLER (NEW)
    private void handleFileMessage(String jsonMessage) {
        System.out.println("📦 File shared, broadcasting to all clients…");
        server.broadcast(jsonMessage, null);
    }

    private void handleRegister(Message message) {
        JsonObject content = gson.fromJson(message.getContent(), JsonObject.class);
        String email = content.get("email").getAsString();
        String password = content.get("password").getAsString();
        String username = content.get("username").getAsString();

        boolean success = authService.register(email, password, username);

        Message response = new Message();
        response.setType("register_response");

        if (success) {
            response.setContent("{\"success\": true, \"message\": \"Registration successful\"}");
            System.out.println("✅ User registered: " + email);
        } else {
            response.setContent("{\"success\": false, \"message\": \"Registration failed. Email may already exist.\"}");
        }

        sendMessage(response.toJson());
    }

    private void handleLogin(Message message) {
        JsonObject content = gson.fromJson(message.getContent(), JsonObject.class);
        String email = content.get("email").getAsString();
        String password = content.get("password").getAsString();

        User user = authService.login(email, password);

        Message response = new Message();
        response.setType("login_response");

        if (user != null) {
            this.userEmail = email;
            this.username = user.getUsername();
            server.addClient(email, this);

            JsonObject responseContent = new JsonObject();
            responseContent.addProperty("success", true);
            responseContent.addProperty("email", user.getEmail());
            responseContent.addProperty("username", user.getUsername());
            responseContent.addProperty("message", "Login successful");
            response.setContent(responseContent.toString());

            sendMessage(response.toJson());
            System.out.println("✅ User logged in: " + email);

            broadcastUserJoined();
            sendRecentMessages();

        } else {
            response.setContent("{\"success\": false, \"message\": \"Invalid credentials\"}");
            sendMessage(response.toJson());
        }
    }

    private void handlePublicMessage(Message message) {
        String senderEmail = message.getSender();

        if (userEmail == null && senderEmail == null) {
            sendError("Not authenticated");
            return;
        }

        String effectiveSender = userEmail != null ? userEmail : senderEmail;
        String effectiveUsername = username;

        if (effectiveUsername == null && effectiveSender != null) {
            User user = authService.getUserByEmail(effectiveSender);
            if (user != null) {
                effectiveUsername = user.getUsername();
            }
        }

        System.out.println("📝 Public message from " + effectiveUsername + ": " + message.getContent());

        messageRepository.saveMessage(effectiveSender, null, message.getContent());

        Message broadcastMsg = new Message();
        broadcastMsg.setType("message");
        broadcastMsg.setSender(effectiveSender);
        broadcastMsg.setUsername(effectiveUsername);
        broadcastMsg.setContent(message.getContent());
        broadcastMsg.setTimestamp(System.currentTimeMillis());

        server.broadcast(broadcastMsg.toJson(), null);
    }

    private void handlePrivateMessage(Message message) {
        if (userEmail == null) {
            sendError("Not authenticated");
            return;
        }

        String receiver = message.getReceiver();

        messageRepository.saveMessage(userEmail, receiver, message.getContent());

        Message privateMsg = new Message();
        privateMsg.setType("private_message");
        privateMsg.setSender(userEmail);
        privateMsg.setUsername(username);
        privateMsg.setReceiver(receiver);
        privateMsg.setContent(message.getContent());
        privateMsg.setTimestamp(System.currentTimeMillis());

        server.sendToUser(receiver, privateMsg.toJson());
        sendMessage(privateMsg.toJson());
    }

    private void handleGetUsers() {
        List<User> onlineUsers = authService.getOnlineUsers();

        Message response = new Message();
        response.setType("user_list");
        response.setContent(gson.toJson(onlineUsers));

        sendMessage(response.toJson());
    }

    private void handleGetHistory(Message message) {
        List<Message> history = messageRepository.getRecentPublicMessages(50);

        Message response = new Message();
        response.setType("history");
        response.setContent(gson.toJson(history));

        sendMessage(response.toJson());
    }

    private void handleTyping(Message message) {
        if (userEmail == null)
            return;

        Message typingMsg = new Message();
        typingMsg.setType("typing");
        typingMsg.setSender(userEmail);
        typingMsg.setUsername(username);
        typingMsg.setContent(message.getContent());

        server.broadcast(typingMsg.toJson(), userEmail);
    }

    private void handleLogout() {
        cleanup();
    }

    private void sendRecentMessages() {
        List<Message> recentMessages = messageRepository.getRecentPublicMessages(50);

        Message response = new Message();
        response.setType("history");
        response.setContent(gson.toJson(recentMessages));

        sendMessage(response.toJson());
    }

    private void broadcastUserJoined() {
        Message statusMsg = new Message();
        statusMsg.setType("user_joined");
        statusMsg.setSender(userEmail);
        statusMsg.setUsername(username);
        statusMsg.setContent(username + " joined the chat");

        server.broadcast(statusMsg.toJson(), userEmail);
        handleGetUsers();
        server.broadcastUserList();
    }

    private void broadcastUserLeft() {
        Message statusMsg = new Message();
        statusMsg.setType("user_left");
        statusMsg.setSender(userEmail);
        statusMsg.setUsername(username);
        statusMsg.setContent(username + " left the chat");

        server.broadcast(statusMsg.toJson(), null);
        server.broadcastUserList();
    }

    public void sendMessage(String message) {
        if (out != null) {
            out.println(message);
        }
    }

    private void sendError(String errorMessage) {
        Message error = new Message();
        error.setType("error");
        error.setContent(errorMessage);
        sendMessage(error.toJson());
    }

    private void cleanup() {
        try {
            if (userEmail != null) {
                authService.updateUserStatus(userEmail, "offline");
                server.removeClient(userEmail);
                broadcastUserLeft();
            }

            if (in != null)
                in.close();
            if (out != null)
                out.close();
            if (clientSocket != null)
                clientSocket.close();

        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public String getUserEmail() {
        return userEmail;
    }

    public String getUsername() {
        return username;
    }
}
