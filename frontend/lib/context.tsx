'use client';

import React, { createContext, useContext, useState, useEffect, ReactNode } from 'react';
import { User, Message } from '@/types';

interface AuthContextType {
  user: User | null;
  isAuthenticated: boolean;
  login: (email: string, username: string) => void;
  logout: () => void;
}
// comment

interface ChatContextType {
  messages: Message[];
  onlineUsers: User[];
  socket: WebSocket | null;
  isConnected: boolean;
  addMessage: (message: Message) => void;
  setOnlineUsers: (users: User[]) => void;
  connectSocket: (email: string) => void;
  disconnectSocket: () => void;
  sendMessage: (content: string) => void;
  sendPrivateMessage: (receiver: string, content: string) => void;
}

const AuthContext = createContext<AuthContextType | undefined>(undefined);
const ChatContext = createContext<ChatContextType | undefined>(undefined);

export function AuthProvider({ children }: { children: ReactNode }) {
  const [user, setUser] = useState<User | null>(null);
  const [isAuthenticated, setIsAuthenticated] = useState(false);

  useEffect(() => {
    // Check if user is stored in localStorage
    const storedUser = localStorage.getItem('user');
    if (storedUser) {
      const parsedUser = JSON.parse(storedUser);
      setUser(parsedUser);
      setIsAuthenticated(true);
    }
  }, []);

  const login = (email: string, username: string) => {
    const newUser: User = { email, username, status: 'online' };
    setUser(newUser);
    setIsAuthenticated(true);
    localStorage.setItem('user', JSON.stringify(newUser));
  };

  const logout = () => {
    setUser(null);
    setIsAuthenticated(false);
    localStorage.removeItem('user');
  };

  return (
    <AuthContext.Provider value={{ user, isAuthenticated, login, logout }}>
      {children}
    </AuthContext.Provider>
  );
}

export function ChatProvider({ children }: { children: ReactNode }) {
  const [messages, setMessages] = useState<Message[]>([]);
  const [onlineUsers, setOnlineUsers] = useState<User[]>([]);
  const [socket, setSocket] = useState<WebSocket | null>(null);
  const [isConnected, setIsConnected] = useState(false);

  const addMessage = (message: Message) => {
    setMessages((prev) => [...prev, message]);
  };

  const connectSocket = (email: string) => {
    const ws = new WebSocket('ws://localhost:8082'); // We'll create a WebSocket bridge

    ws.onopen = () => {
      console.log('✅ Connected to server');
      setIsConnected(true);
    };

    ws.onmessage = (event) => {
      const message: Message = JSON.parse(event.data);
      
      switch (message.type) {
        case 'message':
        case 'private_message':
          addMessage(message);
          break;
        case 'user_list':
          const users = JSON.parse(message.content);
          setOnlineUsers(users);
          break;
        case 'history':
          const history = JSON.parse(message.content);
          setMessages(history);
          break;
        case 'user_joined':
        case 'user_left':
          addMessage(message);
          break;
      }
    };

    ws.onerror = (error) => {
      console.error('❌ WebSocket error:', error);
      setIsConnected(false);
    };

    ws.onclose = () => {
      console.log('🔌 Disconnected from server');
      setIsConnected(false);
    };

    setSocket(ws);
  };

  const disconnectSocket = () => {
    if (socket) {
      socket.close();
      setSocket(null);
      setIsConnected(false);
    }
  };

  const sendMessage = (content: string) => {
    if (socket && isConnected) {
      const message: Message = {
        type: 'message',
        content,
      };
      socket.send(JSON.stringify(message));
    }
  };

  const sendPrivateMessage = (receiver: string, content: string) => {
    if (socket && isConnected) {
      const message: Message = {
        type: 'private_message',
        receiver,
        content,
      };
      socket.send(JSON.stringify(message));
    }
  };

  return (
    <ChatContext.Provider
      value={{
        messages,
        onlineUsers,
        socket,
        isConnected,
        addMessage,
        setOnlineUsers,
        connectSocket,
        disconnectSocket,
        sendMessage,
        sendPrivateMessage,
      }}
    >
      {children}
    </ChatContext.Provider>
  );
}

export function useAuth() {
  const context = useContext(AuthContext);
  if (!context) {
    throw new Error('useAuth must be used within AuthProvider');
  }
  return context;
}

export function useChat() {
  const context = useContext(ChatContext);
  if (!context) {
    throw new Error('useChat must be used within ChatProvider');
  }
  return context;
}
