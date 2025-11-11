'use client';

import { useState, useEffect, useRef } from 'react';
import { useChat } from '@/lib/context';
import { User } from '@/types';
import MessageBubble from './MessageBubble';
import { Send, Loader2 } from 'lucide-react';

interface ChatInterfaceProps {
  selectedUser: User | null;
}

export default function ChatInterface({ selectedUser }: ChatInterfaceProps) {
  const { currentUser, messages, sendMessage, typingUsers, sendTypingIndicator } = useChat();
  const [inputMessage, setInputMessage] = useState('');
  const [isTyping, setIsTyping] = useState(false);
  const messagesEndRef = useRef<HTMLDivElement>(null);
  const typingTimeoutRef = useRef<NodeJS.Timeout | null>(null);

  const scrollToBottom = () => {
    messagesEndRef.current?.scrollIntoView({ behavior: 'smooth' });
  };

  useEffect(() => {
    scrollToBottom();
  }, [messages]);

  const handleInputChange = (e: React.ChangeEvent<HTMLTextAreaElement>) => {
    setInputMessage(e.target.value);

    if (!selectedUser || !currentUser) return;

    if (!isTyping) {
      setIsTyping(true);
      sendTypingIndicator(true, selectedUser);
    }

    if (typingTimeoutRef.current) {
      clearTimeout(typingTimeoutRef.current);
    }

    typingTimeoutRef.current = setTimeout(() => {
      setIsTyping(false);
      sendTypingIndicator(false, selectedUser);
    }, 1000);
  };

  const handleSend = () => {
    if (!inputMessage.trim() || !selectedUser || !currentUser) return;

    sendMessage(inputMessage.trim(), selectedUser);
    setInputMessage('');
    
    if (isTyping) {
      setIsTyping(false);
      sendTypingIndicator(false, selectedUser);
    }
  };

  const handleKeyPress = (e: React.KeyboardEvent) => {
    if (e.key === 'Enter' && !e.shiftKey) {
      e.preventDefault();
      handleSend();
    }
  };

  const filteredMessages = messages.filter(
    (msg) =>
      (msg.senderEmail === currentUser?.email && msg.receiverEmail === selectedUser?.email) ||
      (msg.senderEmail === selectedUser?.email && msg.receiverEmail === currentUser?.email)
  );

  const isOtherUserTyping = selectedUser && typingUsers.has(selectedUser.email);

  if (!selectedUser) {
    return (
      <div className="flex-1 flex items-center justify-center bg-gray-50">
        <div className="text-center text-gray-500">
          <svg
            className="w-24 h-24 mx-auto mb-4 text-gray-300"
            fill="none"
            stroke="currentColor"
            viewBox="0 0 24 24"
          >
            <path
              strokeLinecap="round"
              strokeLinejoin="round"
              strokeWidth={2}
              d="M8 12h.01M12 12h.01M16 12h.01M21 12c0 4.418-4.03 8-9 8a9.863 9.863 0 01-4.255-.949L3 20l1.395-3.72C3.512 15.042 3 13.574 3 12c0-4.418 4.03-8 9-8s9 3.582 9 8z"
            />
          </svg>
          <p className="text-lg font-medium">Select a user to start chatting</p>
          <p className="text-sm mt-2">Choose someone from the sidebar to begin a conversation</p>
        </div>
      </div>
    );
  }

  return (
    <div className="flex-1 flex flex-col bg-white">
      <div className="px-6 py-4 border-b border-gray-200 bg-white">
        <div className="flex items-center justify-between">
          <div>
            <h2 className="text-lg font-semibold text-gray-900">{selectedUser.username}</h2>
            <p className="text-sm text-gray-500">{selectedUser.email}</p>
          </div>
          <div className="flex items-center gap-2">
            <div
              className={`w-2 h-2 rounded-full ${
                selectedUser.status === 'online' ? 'bg-green-500' : 'bg-gray-300'
              }`}
            />
            <span className="text-sm text-gray-600">
              {selectedUser.status === 'online' ? 'Online' : 'Offline'}
            </span>
          </div>
        </div>
      </div>

      <div className="flex-1 overflow-y-auto p-6 bg-gray-50">
        {filteredMessages.length === 0 ? (
          <div className="flex items-center justify-center h-full text-gray-500">
            <div className="text-center">
              <p className="text-lg">No messages yet</p>
              <p className="text-sm mt-2">Start the conversation by sending a message</p>
            </div>
          </div>
        ) : (
          <>
            {filteredMessages.map((message, index) => (
              <MessageBubble
                key={index}
                message={message}
                isOwn={message.senderEmail === currentUser?.email}
              />
            ))}
          </>
        )}
        
        {isOtherUserTyping && (
          <div className="flex items-center gap-2 text-gray-500 text-sm mb-4">
            <Loader2 className="w-4 h-4 animate-spin" />
            <span>{selectedUser.username} is typing...</span>
          </div>
        )}
        
        <div ref={messagesEndRef} />
      </div>

      <div className="px-6 py-4 bg-white border-t border-gray-200">
        <div className="flex items-end gap-3">
          <textarea
            value={inputMessage}
            onChange={handleInputChange}
            onKeyPress={handleKeyPress}
            placeholder="Type a message..."
            rows={1}
            className="flex-1 px-4 py-3 border border-gray-300 rounded-lg focus:outline-none focus:ring-2 focus:ring-blue-500 focus:border-transparent resize-none"
            style={{ minHeight: '48px', maxHeight: '120px' }}
          />
          <button
            onClick={handleSend}
            disabled={!inputMessage.trim()}
            className="px-6 py-3 bg-blue-600 text-white rounded-lg hover:bg-blue-700 disabled:opacity-50 disabled:cursor-not-allowed transition-colors flex items-center gap-2"
          >
            <Send className="w-5 h-5" />
            <span className="font-medium">Send</span>
          </button>
        </div>
      </div>
    </div>
  );
}
