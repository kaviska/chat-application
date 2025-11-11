'use client';

import React, { createContext, useContext, useState, useEffect, ReactNode } from 'react';
import { User, Message, ChatMessage } from '@/types';
import { wsService } from './socket';

interface ChatContextType {
  currentUser: User | null;
  setCurrentUser: (user: User | null) => void;
  users: User[];
  setUsers: (users: User[]) => void;
  messages: Message[];
  addMessage: (message: Message) => void;
  selectedUser: User | null;
  setSelectedUser: (user: User | null) => void;
  isConnected: boolean;
  login: (email: string, password: string, userType: 'admin' | 'member') => Promise<void>;
  register: (email: string, password: string, username: string) => Promise<void>;
  logout: () => void;
  sendMessage: (content: string, receiver: User) => void;
  loadConversation: (otherUser: User) => void;
  typingUsers: Set<string>;
  sendTypingIndicator: (isTyping: boolean, receiver: User) => void;
}

const ChatContext = createContext<ChatContextType | undefined>(undefined);

export function ChatProvider({ children }: { children: ReactNode }) {
  const [currentUser, setCurrentUser] = useState<User | null>(null);
  const [users, setUsers] = useState<User[]>([]);
  const [messages, setMessages] = useState<Message[]>([]);
  const [selectedUser, setSelectedUser] = useState<User | null>(null);
  const [isConnected, setIsConnected] = useState(false);
  const [typingUsers, setTypingUsers] = useState<Set<string>>(new Set());

  useEffect(() => {
    // Connect to WebSocket on mount
    wsService.connect()
      .then(() => {
        setIsConnected(true);
      })
      .catch(err => {
        console.error('Failed to connect to WebSocket:', err);
        setIsConnected(false);
      });

    // Set up message handler
    const handleMessage = (message: ChatMessage) => {
      console.log('Context received message:', message);

      switch (message.type) {
        case 'login_success':
          if (message.email && message.username && message.userType) {
            const user: User = {
              email: message.email,
              username: message.username,
              status: 'online',
              userType: message.userType as 'admin' | 'member'
            };
            setCurrentUser(user);
            
            // Store in localStorage
            localStorage.setItem('currentUser', JSON.stringify(user));
            
            // Request user list
            wsService.send({ type: 'get_users' });
          }
          break;

        case 'register_success':
          alert(message.message || 'Registration successful! Please login.');
          break;

        case 'user_list':
          if (message.users) {
            setUsers(message.users);
          }
          break;

        case 'chat_message':
        case 'message_sent':
          if (message.senderEmail && message.senderType && 
              message.receiverEmail && message.receiverType && message.content) {
            const newMessage: Message = {
              senderEmail: message.senderEmail,
              senderType: message.senderType as 'admin' | 'member',
              senderUsername: message.senderUsername,
              receiverEmail: message.receiverEmail,
              receiverType: message.receiverType as 'admin' | 'member',
              content: message.content,
              timestamp: message.timestamp || new Date().toISOString()
            };
            setMessages(prev => [...prev, newMessage]);
          }
          break;

        case 'conversation_history':
          if (message.messages) {
            setMessages(message.messages);
          }
          break;

        case 'user_joined':
        case 'user_left':
          // Refresh user list
          wsService.send({ type: 'get_users' });
          break;

        case 'typing':
          if (message.senderEmail && message.isTyping !== undefined) {
            setTypingUsers(prev => {
              const newSet = new Set(prev);
              if (message.isTyping) {
                newSet.add(message.senderEmail!);
              } else {
                newSet.delete(message.senderEmail!);
              }
              return newSet;
            });

            // Clear typing indicator after 3 seconds
            if (message.isTyping) {
              setTimeout(() => {
                setTypingUsers(prev => {
                  const newSet = new Set(prev);
                  newSet.delete(message.senderEmail!);
                  return newSet;
                });
              }, 3000);
            }
          }
          break;

        case 'error':
          console.error('Server error:', message.message);
          alert(message.message || 'An error occurred');
          break;
      }
    };

    wsService.onMessage(handleMessage);

    // Check for existing session
    const savedUser = localStorage.getItem('currentUser');
    if (savedUser) {
      try {
        const user = JSON.parse(savedUser);
        setCurrentUser(user);
      } catch (err) {
        console.error('Error parsing saved user:', err);
      }
    }

    return () => {
      wsService.removeMessageHandler(handleMessage);
    };
  }, []);

  const login = async (email: string, password: string, userType: 'admin' | 'member') => {
    const messageType = userType === 'admin' ? 'login_admin' : 'login_member';
    wsService.send({
      type: messageType,
      email,
      password
    });
  };

  const register = async (email: string, password: string, username: string) => {
    wsService.send({
      type: 'register_member',
      email,
      password,
      username
    });
  };

  const logout = () => {
    setCurrentUser(null);
    setUsers([]);
    setMessages([]);
    setSelectedUser(null);
    localStorage.removeItem('currentUser');
    wsService.disconnect();
    window.location.href = '/';
  };

  const sendMessage = (content: string, receiver: User) => {
    if (!currentUser) return;

    wsService.send({
      type: 'chat_message',
      receiverEmail: receiver.email,
      receiverType: receiver.userType,
      content
    });
  };

  const loadConversation = (otherUser: User) => {
    setSelectedUser(otherUser);
    setMessages([]); // Clear messages while loading
    
    wsService.send({
      type: 'get_conversation',
      otherUserEmail: otherUser.email,
      otherUserType: otherUser.userType
    });
  };

  const addMessage = (message: Message) => {
    setMessages(prev => [...prev, message]);
  };

  const sendTypingIndicator = (isTyping: boolean, receiver: User) => {
    if (!currentUser) return;

    wsService.send({
      type: 'typing',
      receiverEmail: receiver.email,
      receiverType: receiver.userType,
      isTyping
    });
  };

  return (
    <ChatContext.Provider
      value={{
        currentUser,
        setCurrentUser,
        users,
        setUsers,
        messages,
        addMessage,
        selectedUser,
        setSelectedUser,
        isConnected,
        login,
        register,
        logout,
        sendMessage,
        loadConversation,
        typingUsers,
        sendTypingIndicator
      }}
    >
      {children}
    </ChatContext.Provider>
  );
}

export function useChat() {
  const context = useContext(ChatContext);
  if (context === undefined) {
    throw new Error('useChat must be used within a ChatProvider');
  }
  return context;
}
