package com.chatapp.server;

import com.chatapp.auth.UserAuthService;
import com.chatapp.database.DatabaseManager;
import com.chatapp.database.MessageRepository;
import com.chatapp.database.FileRepository;
import com.chatapp.model.Message;
import com.chatapp.model.User;
import com.chatapp.model.File;
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
    private final FileRepository fileRepository;
    private final Gson gson;

    public ClientHandler(Socket socket, MainServer server) {
        this.clientSocket = socket;
        this.server = server;
        this.authService = new UserAuthService();
        DatabaseManager dbManager = DatabaseManager.getInstance();
        this.messageRepository = new MessageRepository();
        this.fileRepository = new FileRepository(dbManager);
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
            System.out.println("📨 Handling message type: " + message.getType());

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
                    handleFileMessage(message);
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

                case "get_files":
                    sendFilesList();
                    break;

                default:
                    sendError("Unknown message type: " + message.getType());
            }

        } catch (Exception e) {
            e.printStackTrace();
            sendError("Invalid message format");
        }
    }

    private void handleFileMessage(Message message) {
        try {
            System.out.println("💾 Handling file message...");
            System.out.println("Message content: " + message.getContent());

            Object rawContent = message.getContent();
            JsonObject content;

            try {
                if (rawContent instanceof JsonObject) {
                    content = (JsonObject) rawContent;
                } else if (rawContent instanceof com.google.gson.internal.LinkedTreeMap) {
                    // Convert LinkedTreeMap to JsonObject
                    String jsonStr = gson.toJson(rawContent);
                    content = gson.fromJson(jsonStr, JsonObject.class);
                } else if (rawContent instanceof String) {
                    content = gson.fromJson((String) rawContent, JsonObject.class);
                } else {
                    throw new Exception(
                            "Invalid file message format: unexpected content type: " + rawContent.getClass().getName());
                }
            } catch (Exception e) {
                System.err.println("Failed to parse file content: " + e.getMessage());
                System.err.println(
                        "Raw content type: " + (rawContent != null ? rawContent.getClass().getName() : "null"));
                System.err.println("Raw content value: " + rawContent);
                throw e;
            }

            if (content == null) {
                throw new Exception("Invalid file message format: failed to parse content as JSON object");
            }

            String filename = content.get("filename").getAsString();
            String fileData = content.get("data").getAsString();
            String fileType = content.get("type").getAsString();

            // Validate and decode base64 file data
            byte[] decodedData;
            if (fileData.contains(",")) {
                // Handle data URLs (e.g., "data:image/png;base64,ABC123...")
                String[] parts = fileData.split(",");
                if (parts.length < 2 || parts[1].isEmpty()) {
                    throw new Exception("Invalid or empty file data");
                }
                decodedData = java.util.Base64.getDecoder().decode(parts[1]);
            } else {
                if (fileData.isEmpty()) {
                    throw new Exception("Empty file data");
                }
                // Handle raw base64 data
                decodedData = java.util.Base64.getDecoder().decode(fileData);
            }

            // Determine sender: prefer authenticated userEmail, fall back to message.sender
            String sender = (userEmail != null && !userEmail.isEmpty()) ? userEmail
                    : (message.getSender() != null && !message.getSender().isEmpty() ? message.getSender() : null);

            if (sender == null || sender.isEmpty()) {
                throw new Exception("User must be authenticated to share files");
            }

            // Create and save file object (set receiver to null for group sharing)
            File file = new File(filename, decodedData, fileType, sender, null);
            System.out.println("📦 Creating file: " + filename + " (Type: " + fileType + ", Size: " + decodedData.length
                    + " bytes)");
            fileRepository.saveFile(file);

            // Always broadcast the file message to all clients for group sharing
            message.setReceiver(null); // Ensure receiver is null for broadcasting
            server.broadcast(message.toJson(), null);
            System.out.println("📢 Broadcasting file to all users: " + filename);

            System.out.println("📦 File saved and shared: " + filename);

        } catch (Exception e) {
            e.printStackTrace();
            sendError("Failed to process file: " + e.getMessage());
        }
    }

    private void sendFilesList() {
        try {
            List<File> files = fileRepository.getFilesForUser(userEmail);
            JsonObject response = new JsonObject();
            response.addProperty("type", "files_list");

            com.google.gson.JsonArray fileArray = new com.google.gson.JsonArray();
            for (File file : files) {
                JsonObject fileObj = new JsonObject();
                fileObj.addProperty("id", file.getId());
                fileObj.addProperty("filename", file.getFilename());
                fileObj.addProperty("fileType", file.getFileType());
                fileObj.addProperty("sender", file.getSender());
                fileObj.addProperty("receiver", file.getReceiver());
                fileObj.addProperty("timestamp", file.getTimestamp().getTime());

                // Convert file data to base64
                String base64Data = "data:" + file.getFileType() + ";base64," +
                        java.util.Base64.getEncoder().encodeToString(file.getFileData());
                fileObj.addProperty("data", base64Data);

                fileArray.add(fileObj);
            }

            response.add("files", fileArray);
            sendMessage(response.toString());

        } catch (Exception e) {
            e.printStackTrace();
            sendError("Failed to retrieve files: " + e.getMessage());
        }
    }

    private void handleRegister(Message message) {
        String contentStr = message.getContentAsString();
        JsonObject content = gson.fromJson(contentStr, JsonObject.class);
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
        String contentStr = message.getContentAsString();
        JsonObject content = gson.fromJson(contentStr, JsonObject.class);
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

        String content = message.getContentAsString();
        System.out.println("📝 Public message from " + effectiveUsername + ": " + content);

        messageRepository.saveMessage(effectiveSender, null, content);

        Message broadcastMsg = new Message();
        broadcastMsg.setType("message");
        broadcastMsg.setSender(effectiveSender);
        broadcastMsg.setUsername(effectiveUsername);
        broadcastMsg.setContent(content);
        broadcastMsg.setTimestamp(System.currentTimeMillis());

        server.broadcast(broadcastMsg.toJson(), null);
    }

    private void handlePrivateMessage(Message message) {
        if (userEmail == null) {
            sendError("Not authenticated");
            return;
        }

        String receiver = message.getReceiver();

        String content = message.getContentAsString();
        messageRepository.saveMessage(userEmail, receiver, content);

        Message privateMsg = new Message();
        privateMsg.setType("private_message");
        privateMsg.setSender(userEmail);
        privateMsg.setUsername(username);
        privateMsg.setReceiver(receiver);
        privateMsg.setContent(content);
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
