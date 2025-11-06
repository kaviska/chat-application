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
  const isSystemMessage =
    message.type === 'user_joined' || message.type === 'user_left';

  // ✅ System join/left messages
  if (isSystemMessage) {
    return (
      <div className="flex justify-center my-2">
        <div className="bg-gray-100 text-gray-600 px-4 py-2 rounded-full text-sm">
          {message.content}
        </div>
      </div>
    );
  }

  // ✅ FILE MESSAGES
  if (message.type === 'file') {
    const fileUrl = `data:${message.fileType};base64,${message.fileData}`;

    const downloadFile = () => {
      const a = document.createElement('a');
      a.href = fileUrl;
      a.download = message.fileName || 'file';
      a.click();
    };

    const isImage =
      message.fileType?.startsWith('image/') && message.fileData;

    return (
      <div
        className={`flex ${
          isOwnMessage ? 'justify-end' : 'justify-start'
        } mb-4`}
      >
        <div className={`max-w-[70%] ${isOwnMessage ? 'order-2' : 'order-1'}`}>
          {/* Sender name */}
          {!isOwnMessage && (
            <div className="text-sm font-semibold text-gray-700 mb-1 ml-1">
              {message.sender}
            </div>
          )}

          <div
            className={`p-3 rounded-2xl ${
              isOwnMessage
                ? 'bg-blue-600 text-white rounded-tr-sm'
                : 'bg-gray-100 text-gray-900 rounded-tl-sm'
            }`}
          >
            <p className="font-medium mb-2">{message.fileName}</p>

            {/* ✅ Image preview */}
            {isImage && (
              <img
                src={fileUrl}
                alt="image"
                className="rounded-lg mb-2 max-h-60 object-contain"
              />
            )}

            {/* ✅ Download button */}
            <button
              onClick={downloadFile}
              className="px-3 py-1 mt-1 text-sm bg-white text-blue-600 border border-blue-400 rounded-lg hover:bg-blue-50"
            >
              Download File
            </button>
          </div>

          {/* Timestamp */}
          <div
            className={`text-xs text-gray-500 mt-1 ${
              isOwnMessage ? 'text-right' : 'text-left'
            } ml-1`}
          >
            {message.timestamp &&
              format(new Date(message.timestamp), 'HH:mm')}
          </div>
        </div>
      </div>
    );
  }

  // ✅ NORMAL TEXT MESSAGE
  return (
    <div
      className={`flex ${
        isOwnMessage ? 'justify-end' : 'justify-start'
      } mb-4`}
    >
      <div className={`max-w-[70%] ${isOwnMessage ? 'order-2' : 'order-1'}`}>
        {!isOwnMessage && (
          <div className="text-sm font-semibold text-gray-700 mb-1 ml-1">
            {message.sender}
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

        <div
          className={`text-xs text-gray-500 mt-1 ${
            isOwnMessage ? 'text-right' : 'text-left'
          } ml-1`}
        >
          {message.timestamp && format(new Date(message.timestamp), 'HH:mm')}
        </div>
      </div>
    </div>
  );
}
