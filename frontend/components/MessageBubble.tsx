'use client';

import { Message as MessageType } from '@/types';
import { format } from 'date-fns';
import { useAuth } from '@/lib/context';

interface MessageBubbleProps {
  message: MessageType;
}

export function MessageBubble({ message }: MessageBubbleProps) {
  const { user } = useAuth();
  const isOwnMessage = message.sender === user?.email;
  const isSystemMessage = message.type === 'user_joined' || message.type === 'user_left';

  if (isSystemMessage) {
    return (
      <div className="flex justify-center my-2">
        <div className="bg-gray-100 text-gray-600 px-4 py-2 rounded-full text-sm">
          {message.content}
        </div>
      </div>
    );
  }

  return (
    <div className={`flex ${isOwnMessage ? 'justify-end' : 'justify-start'} mb-4`}>
      <div className={`max-w-[70%] ${isOwnMessage ? 'order-2' : 'order-1'}`}>
        {!isOwnMessage && (
          <div className="text-sm font-semibold text-gray-700 mb-1 ml-1">
            {message.username || message.sender}
          </div>
        )}
        
        <div
          className={`px-4 py-2 rounded-2xl ${
            isOwnMessage
              ? 'bg-blue-600 text-white rounded-tr-sm'
              : 'bg-gray-100 text-gray-900 rounded-tl-sm'
          }`}
        >
          <p className="break-words">{message.content}</p>
        </div>
        
        <div className={`text-xs text-gray-500 mt-1 ${isOwnMessage ? 'text-right' : 'text-left'} ml-1`}>
          {message.timestamp && format(new Date(message.timestamp), 'HH:mm')}
        </div>
      </div>
    </div>
  );
}
