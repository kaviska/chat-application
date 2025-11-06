'use client';

import { Message as MessageType, FileContent } from '@/types';
import { format } from 'date-fns';
import { useAuth } from '@/lib/context';

interface MessageBubbleProps {
  message: MessageType;
}

// Type guard for file content
const isFileContent = (content: any): content is FileContent =>
  content &&
  typeof content === 'object' &&
  'filename' in content &&
  'type' in content &&
  'data' in content;

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
          {typeof message.content === 'string'
            ? message.content
            : isFileContent(message.content)
            ? `${message.content.filename} was shared`
            : JSON.stringify(message.content)}
        </div>
      </div>
    );
  }

  // ✅ FILE MESSAGES
  if (message.type === 'file') {
    const fileContent = isFileContent(message.content) 
      ? message.content 
      : typeof message.content === 'string'
      ? JSON.parse(message.content)
      : null;

    if (!fileContent) {
      console.error('Invalid file message:', message);
      return null;
    }

    const fileUrl = fileContent.data;

    const downloadFile = () => {
      const a = document.createElement('a');
      a.href = fileUrl;
      a.download = fileContent.filename;
      a.click();
    };

    const isImage = fileContent.type.startsWith('image/') && fileContent.data;

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
            <p className="font-medium mb-2">{fileContent.filename}</p>

            {/* ✅ Image preview */}
            {isImage && (
              <img
                src={fileUrl}
                alt={fileContent.filename}
                className="rounded-lg mb-2 max-h-60 object-contain"
              />
            )}

            {/* ✅ Download button */}
            <button
              onClick={downloadFile}
              className={`px-3 py-1 mt-1 text-sm rounded-lg hover:bg-opacity-90 ${
                isOwnMessage
                  ? 'bg-white text-blue-600 border border-white hover:bg-white'
                  : 'bg-blue-600 text-white border border-blue-400 hover:bg-blue-700'
              }`}
            >
              Download {fileContent.filename}
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
      className={`flex ${isOwnMessage ? 'justify-end' : 'justify-start'} mb-4`}
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
          <p className="break-words">
            {typeof message.content === 'string'
              ? message.content
              : isFileContent(message.content)
              ? message.content.filename
              : JSON.stringify(message.content)}
          </p>
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
