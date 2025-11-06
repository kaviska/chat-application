export interface User {
  id?: number;
  email: string;
  username: string;
  status?: 'online' | 'offline';
}

export interface Message {
  type:
    | 'message'
    | 'private_message'
    | 'login_response'
    | 'register_response'
    | 'user_list'
    | 'user_joined'
    | 'user_left'
    | 'typing'
    | 'history'
    | 'error'
    | 'file'
    | 'files_list';

  sender?: string;
  receiver?: string;
  content: string | FileContent;
  username?: string;
  timestamp?: number;
  files?: StoredFile[];
}

export interface FileContent {
  filename: string;
  type: string;
  data: string; // base64 file
}

export interface StoredFile {
  id: number;
  filename: string;
  fileType: string;
  data: string;
  sender: string;
  receiver?: string;
  timestamp: number;
}


export interface AuthResponse {
  success: boolean;
  email?: string;
  username?: string;
  message: string;
}
