"use client";

import { useEffect, useRef, useState } from 'react';
import { useRouter } from 'next/navigation';
import { useChat } from '@/lib/context';
import { wsService } from '@/lib/socket';
import MessageBubble from '@/components/MessageBubble';
import { MessageInput } from '@/components/MessageInput';
import UserList from '@/components/UserList';
import { LogOut, MessageCircle } from 'lucide-react';
import { User, Message } from '@/types';

export default function ChatPage() {
  const { currentUser: user, logout } = useChat();
  const router = useRouter();
  const [messages, setMessages] = useState<Message[]>([]);
  const [onlineUsers, setOnlineUsers] = useState<User[]>([]);
  const [isConnected, setIsConnected] = useState(false);
  const messagesEndRef = useRef<HTMLDivElement>(null);
  const [selectedUser, setSelectedUser] = useState<User | null>(null);
  const isAuthenticated = !!user;

  useEffect(() => {
    if (!isAuthenticated || !user) {
      router.push('/login');
      return;
    }

    // Connect to server
    const connectToServer = async () => {
      try {
        await wsService.connect();
        setIsConnected(true);

        // Setup message handlers
        wsService.onMessage((message: any) => {
          switch (message.type) {
            case 'message':
            case 'private_message':
            case 'user_joined':
            case 'user_left':
              // If server sends Message-shaped payloads, try to normalize
              setMessages((prev) => [...prev, message]);
              break;

            case 'user_list':
              try {
                const users = Array.isArray(message.users) ? message.users : JSON.parse(message.content || '[]');
                setOnlineUsers(users);
              } catch (e) {
                console.error('Failed to parse user_list', e);
              }
              break;

            case 'history':
            case 'conversation_history':
              try {
                const history = Array.isArray(message.messages) ? message.messages : JSON.parse(message.content || '[]');
                setMessages(history);
              } catch (e) {
                console.error('Failed to parse history', e);
              }
              break;
          }
        });

        // Request history and users
        wsService.send({ type: 'get_history', senderEmail: user.email, content: '' });
        wsService.send({ type: 'get_users', senderEmail: user.email, content: '' });

      } catch (error) {
        console.error('Failed to connect:', error);
        setIsConnected(false);
      }
    };

    connectToServer();

    return () => {
      wsService.disconnect();
    };
  }, [isAuthenticated, user, router]);

  // Auto-scroll to bottom when new messages arrive
  useEffect(() => {
    messagesEndRef.current?.scrollIntoView({ behavior: 'smooth' });
  }, [messages]);

  const handleSendMessage = (content: string) => {
    if (!isConnected || !user) return;

    if (selectedUser) {
      wsService.send({ type: 'private_message', senderEmail: user.email, receiverEmail: selectedUser.email, content });
    } else {
      wsService.send({ type: 'message', senderEmail: user.email, content });
    }
  };

  const handleLogout = () => {
    wsService.send({ type: 'logout', senderEmail: user?.email, content: '' });
    wsService.disconnect();
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
                      <MessageBubble key={index} message={message} isOwn={message.senderEmail === user?.email} />
                    ))}
                <div ref={messagesEndRef} />
              </>
            )}
          </div>

          {/* Message Input */}
          <MessageInput onSendMessage={handleSendMessage} disabled={!isConnected} />
        </div>

        {/* User List Sidebar */}
        <UserList
          users={onlineUsers}
          selectedUser={selectedUser}
          onSelectUser={(u) => {
            setSelectedUser(u);
            // request conversation with selected user
            wsService.send({ type: 'get_conversation', otherUserEmail: u.email });
          }}
          title="Online Users"
        />
      </div>
    </div>
  );
}
