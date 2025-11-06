"use client";

import { useState, FormEvent } from "react";
import { Send, Smile } from "lucide-react";

interface MessageInputProps {
  onSendMessage: (content: string) => void;
  onSendFile: (file: File) => void;
  disabled?: boolean;
}

export function MessageInput({ onSendMessage, onSendFile, disabled }: MessageInputProps) {
  const [message, setMessage] = useState("");

  const handleSubmit = (e: FormEvent) => {
    e.preventDefault();

    if (message.trim() && !disabled) {
      onSendMessage(message.trim());
      setMessage("");
    }
  };

  return (
    <form
      onSubmit={handleSubmit}
      className="border-t border-gray-200 p-4 bg-white"
    >
      <div className="flex items-center gap-2">
        <button
          type="button"
          className="p-2 hover:bg-gray-100 rounded-full transition-colors"
          title="Add emoji"
        >
          <Smile className="w-6 h-6 text-gray-500" />
        </button>

        <input
          type="text"
          value={message}
          onChange={(e) => setMessage(e.target.value)}
          placeholder="Type a message..."
          disabled={disabled}
          className="flex-1 px-4 py-2 border border-gray-300 rounded-full focus:outline-none focus:ring-2 focus:ring-blue-500 focus:border-transparent disabled:bg-gray-100 disabled:cursor-not-allowed"
        />

        {/* ✅ File uploader */}
        <input
          type="file"
          onChange={(e) => {
            const file = e.target.files?.[0];
            if (file) onSendFile(file);
          }}
        />

        <button
          type="submit"
          disabled={!message.trim() || disabled}
          className="p-2 bg-blue-600 hover:bg-blue-700 rounded-full transition-colors disabled:bg-gray-300 disabled:cursor-not-allowed"
          title="Send message"
        >
          <Send className="w-6 h-6 text-white" />
        </button>
      </div>
    </form>
  );
}
