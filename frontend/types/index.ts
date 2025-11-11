export interface User {
  id?: number;
  email: string;
  username: string;
  status: 'online' | 'offline';
  userType: 'admin' | 'member';
}

export interface Message {
  id?: number;
  senderEmail: string;
  senderType: 'admin' | 'member';
  senderUsername?: string;
  receiverEmail: string;
  receiverType: 'admin' | 'member';
  content: string;
  timestamp: string;
  isRead?: boolean;
}

export interface ChatMessage {
  type: string;
  senderEmail?: string;
  senderType?: string;
  senderUsername?: string;
  receiverEmail?: string;
  receiverType?: string;
  content?: string;
  timestamp?: string;
  email?: string;
  username?: string;
  userType?: string;
  users?: User[];
  messages?: Message[];
  message?: string;
  isTyping?: boolean;
  otherUserEmail?: string;
  otherUserType?: string;
  password?: string;
}
