export interface User {
  id?: number;
  email: string;
  username: string;
  status?: 'online' | 'offline';
}

export interface Message {
  type: 'message' | 'private_message' | 'login_response' | 'register_response' | 'user_list' | 'user_joined' | 'user_left' | 'typing' | 'history' | 'error';
  sender?: string;
  receiver?: string;
  content: string;
  username?: string;
  timestamp?: number;
}

export interface AuthResponse {
  success: boolean;
  email?: string;
  username?: string;
  message: string;
}
