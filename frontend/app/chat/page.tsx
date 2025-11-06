"use client";

import { useEffect, useRef, useState } from "react";
import { useRouter } from "next/navigation";
import { useAuth } from "@/lib/context";
import { socketClient } from "@/lib/socket";
import { MessageBubble } from "@/components/MessageBubble";
import { MessageInput } from "@/components/MessageInput";
import { UserList } from "@/components/UserList";
import { LogOut, MessageCircle } from "lucide-react";
import { User, Message, StoredFile, FileContent } from "@/types";

export default function ChatPage() {
  const { user, isAuthenticated, logout } = useAuth();
  const router = useRouter();
  const [messages, setMessages] = useState<Message[]>([]);
  const [onlineUsers, setOnlineUsers] = useState<User[]>([]);
  const [isConnected, setIsConnected] = useState(false);
  const [storedFiles, setStoredFiles] = useState<StoredFile[]>([]);
  const messagesEndRef = useRef<HTMLDivElement>(null);

  useEffect(() => {
    if (!isAuthenticated || !user) {
      router.push("/login");
      return;
    }

    const connectToServer = async () => {
      try {
        await socketClient.connect("ws://localhost:8082");
        setIsConnected(true);

        // Request stored files (include sender so server can return user-specific files)
        socketClient.send({ type: "get_files", sender: user.email });
        // Re-request files shortly after connect in case the server-side history or
        // authentication sequence causes the first request to return nothing.
        setTimeout(() => {
          socketClient.send({ type: "get_files", sender: user.email });
        }, 1500);

        socketClient.on("*", (message: Message) => {
          switch (message.type) {
            // ✅ Normal text messages
            case "message":
            case "private_message":
            case "user_joined":
            case "user_left":
              setMessages((prev) => [...prev, message]);
              break;

            // ✅ File message
            case "file":
              const fileContent = message.content as FileContent;
              setMessages((prev) => [
                ...prev,
                {
                  ...message,
                  content: fileContent
                },
              ]);
              break;

            // ✅ Files list response
            case "files_list": {
              const files = message.files || [];
              console.log('📥 Received files_list:', files.length, files);
              setStoredFiles(files);

              // Convert stored files to file messages and merge with existing messages (dedupe)
              const fileMessages: Message[] = files.map((file: StoredFile) => ({
                type: "file",
                sender: file.sender,
                receiver: file.receiver,
                timestamp: file.timestamp,
                content: {
                  filename: file.filename,
                  type: file.fileType,
                  data: file.data,
                } as FileContent,
              } as Message));

              setMessages((prev) => {
                const combined = [...prev, ...fileMessages];
                const seen = new Set<string>();
                const deduped: Message[] = [];

                for (const m of combined) {
                  const key = `${m.type}-${m.timestamp}-${m.sender}-${typeof m.content === 'object' && 'filename' in m.content ? (m.content as FileContent).filename : m.content}`;
                  if (!seen.has(key)) {
                    seen.add(key);
                    deduped.push(m);
                  }
                }

                // Sort by timestamp asc so history/order is preserved
                deduped.sort((a, b) => (a.timestamp || 0) - (b.timestamp || 0));
                return deduped;
              });

              break;
            }

            // ✅ Online users list
            case "user_list":
              try {
                const users = typeof message.content === 'string' 
                  ? JSON.parse(message.content)
                  : message.content;
                setOnlineUsers(users);
              } catch {
                console.error("Invalid user list format");
              }
              break;

            // ✅ History
            case "history":
              try {
                const history = typeof message.content === 'string'
                  ? JSON.parse(message.content)
                  : message.content;

                // Merge history with any stored files we already received
                const fileMessagesFromStored = storedFiles.map((file) => ({
                  type: 'file',
                  sender: file.sender,
                  receiver: file.receiver,
                  timestamp: file.timestamp,
                  content: {
                    filename: file.filename,
                    type: file.fileType,
                    data: file.data,
                  } as FileContent,
                } as Message));

                const combined = Array.isArray(history) ? [...history, ...fileMessagesFromStored] : fileMessagesFromStored;

                // Dedupe combined messages
                const seen = new Set<string>();
                const deduped: Message[] = [];
                for (const m of combined) {
                  const key = `${m.type}-${m.timestamp}-${m.sender}-${typeof m.content === 'object' && 'filename' in m.content ? (m.content as FileContent).filename : m.content}`;
                  if (!seen.has(key)) {
                    seen.add(key);
                    deduped.push(m);
                  }
                }

                deduped.sort((a, b) => (a.timestamp || 0) - (b.timestamp || 0));
                setMessages(deduped);
              } catch (err) {
                console.error("Invalid history message format", err);
              }
              break;
          }
        });

        // request chat history
        socketClient.send({
          type: "get_history",
          sender: user.email,
          content: "",
        });

        // request online user list
        socketClient.send({
          type: "get_users",
          sender: user.email,
          content: "",
        });
      } catch (error) {
        console.error("Failed to connect:", error);
        setIsConnected(false);
      }
    };

    connectToServer();

    return () => {
      socketClient.disconnect();
    };
  }, [isAuthenticated, user, router]);

  // ✅ Auto-scroll
  useEffect(() => {
    messagesEndRef.current?.scrollIntoView({ behavior: "smooth" });
  }, [messages]);

  const handleSendMessage = (content: string) => {
    if (!isConnected || !user) return;

    socketClient.send({
      type: "message",
      sender: user.email,
      content,
    });
  };

  const handleSendFile = async (file: File) => {
    if (!isConnected || !user) return;

    await socketClient.sendFile(file, user.email);
  };

  const handleLogout = () => {
    socketClient.send({
      type: "logout",
      sender: user?.email,
      content: "",
    });
    socketClient.disconnect();
    logout();
    router.push("/login");
  };

  if (!isAuthenticated) return null;

  return (
    <div className="h-screen flex flex-col bg-gray-50">
      {/* HEADER */}
      <header className="bg-white border-b border-gray-200 px-6 py-4 flex items-center justify-between">
        <div className="flex items-center gap-3">
          <div className="bg-blue-600 p-2 rounded-lg">
            <MessageCircle className="w-6 h-6 text-white" />
          </div>
          <div>
            <h1 className="text-xl font-bold text-gray-900">
              Chat Application
            </h1>
            <p className="text-sm text-gray-500">
              {isConnected ? (
                <span className="text-green-600">● Connected</span>
              ) : (
                <span className="text-red-600">● Disconnected</span>
              )}
            </p>
          </div>
        </div>

        <div className="flex items-center gap-4">
          <div className="text-right">
            <div className="font-semibold text-gray-900">{user?.username}</div>
            <div className="text-sm text-gray-500">{user?.email}</div>
          </div>

          <button
            onClick={handleLogout}
            className="p-2 hover:bg-gray-100 rounded-lg transition-colors"
            title="Logout"
          >
            <LogOut className="w-5 h-5 text-gray-600" />
          </button>
        </div>
      </header>

      {/* MAIN CONTENT */}
      <div className="flex-1 flex overflow-hidden">
        {/* CHAT AREA */}
        <div className="flex-1 flex flex-col">
          <div className="flex-1 overflow-y-auto p-6 bg-white">
            {messages.length === 0 ? (
              <div className="h-full flex items-center justify-center text-gray-500">
                <div className="text-center">
                  <MessageCircle className="w-16 h-16 mx-auto mb-4 text-gray-300" />
                  <p className="text-lg">No messages yet</p>
                  <p className="text-sm">Start chatting with others!</p>
                </div>
              </div>
            ) : (
              <>
                {messages.map((message, index) => (
                  <MessageBubble key={index} message={message} />
                ))}
                <div ref={messagesEndRef} />
              </>
            )}
          </div>

          {/* INPUT AREA */}
          <MessageInput
            onSendMessage={handleSendMessage}
            onSendFile={handleSendFile}
            disabled={!isConnected}
          />
        </div>

        <UserList users={onlineUsers} currentUserEmail={user?.email} />
      </div>
    </div>
  );
}
