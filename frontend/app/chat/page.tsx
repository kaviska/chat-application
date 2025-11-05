'use client';

import { useEffect, useRef, useState } from 'react';
import { useRouter } from 'next/navigation';
import { useAuth } from '@/lib/context';
import { socketClient } from '@/lib/socket';
import { MessageBubble } from '@/components/MessageBubble';
import { MessageInput } from '@/components/MessageInput';
import { UserList } from '@/components/UserList';
import { LogOut, MessageCircle } from 'lucide-react';
import { User, Message } from '@/types';

export default function ChatPage() {
  const { user, isAuthenticated, logout } = useAuth();
  const router = useRouter();
  const [messages, setMessages] = useState<Message[]>([]);
  const [onlineUsers, setOnlineUsers] = useState<User[]>([]);
  const [isConnected, setIsConnected] = useState(false);
  const messagesEndRef = useRef<HTMLDivElement>(null);

  useEffect(() => {
    if (!isAuthenticated || !user) {
      router.push('/login');
      return;
    }

    // Connect to server
    const connectToServer = async () => {
      try {
        await socketClient.connect('ws://localhost:8082');
        setIsConnected(true);

        // Setup message handlers
        socketClient.on('*', (message: Message) => {
          switch (message.type) {
            case 'message':
            case 'private_message':
            case 'user_joined':
            case 'user_left':
              setMessages((prev) => [...prev, message]);
              break;
            
            case 'user_list':
              const users = JSON.parse(message.content);
              setOnlineUsers(users);
              break;
            
            case 'history':
              const history = JSON.parse(message.content);
              setMessages(history);
              break;
          }
        });

        // If already logged in via localStorage, just get history
        socketClient.send({
          type: 'get_history',
          sender: user.email,
          content: '',
        });

        socketClient.send({
          type: 'get_users',
          sender: user.email,
          content: '',
        });

      } catch (error) {
        console.error('Failed to connect:', error);
        setIsConnected(false);
      }
    };

    connectToServer();

    return () => {
      socketClient.disconnect();
    };
  }, [isAuthenticated, user, router]);

  // Auto-scroll to bottom when new messages arrive
  useEffect(() => {
    messagesEndRef.current?.scrollIntoView({ behavior: 'smooth' });
  }, [messages]);

  const handleSendMessage = (content: string) => {
    if (!isConnected || !user) return;

    socketClient.send({
      type: 'message',
      sender: user.email,
      content,
    });
  };

  const handleLogout = () => {
    socketClient.send({
      type: 'logout',
      sender: user?.email,
      content: '',
    });
    socketClient.disconnect();
    logout();
    router.push('/login');
  };

  if (!isAuthenticated) {
    return null;
  }

  return (
    <div className="h-screen flex flex-col bg-gray-50">
      {/* Header */}
      <header className="bg-white border-b border-gray-200 px-6 py-4 flex items-center justify-between">
        <div className="flex items-center gap-3">
          <div className="bg-blue-600 p-2 rounded-lg">
            <MessageCircle className="w-6 h-6 text-white" />
          </div>
          <div>
            <h1 className="text-xl font-bold text-gray-900">Chat Application</h1>
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

      {/* Main Content */}
      <div className="flex-1 flex overflow-hidden">
        {/* Chat Area */}
        <div className="flex-1 flex flex-col">
          {/* Messages */}
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

          {/* Message Input */}
          <MessageInput
            onSendMessage={handleSendMessage}
            disabled={!isConnected}
          />
        </div>

        {/* User List Sidebar */}
        <UserList users={onlineUsers} currentUserEmail={user?.email} />
      </div>
    </div>
  );
}
