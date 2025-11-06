class SocketClient {
  private socket: WebSocket | null = null;
  private reconnectAttempts = 0;
  private maxReconnectAttempts = 5;
  private reconnectDelay = 3000;
  private messageHandlers: Map<string, (data: any) => void> = new Map();

  connect(serverUrl: string): Promise<void> {
    return new Promise((resolve, reject) => {
      try {
        this.socket = new WebSocket(serverUrl);

        this.socket.onopen = () => {
          console.log("✅ WebSocket connected");
          this.reconnectAttempts = 0;
          resolve();
        };

        this.socket.onmessage = (event) => {
          try {
            const message = JSON.parse(event.data);
            const handler = this.messageHandlers.get(message.type);
            if (handler) {
              handler(message);
            }

            // Call the generic 'message' handler for all messages
            const genericHandler = this.messageHandlers.get("*");
            if (genericHandler) {
              genericHandler(message);
            }
          } catch (error) {
            console.error("Error parsing message:", error);
          }
        };

        this.socket.onerror = (error) => {
          console.error("❌ WebSocket error:", error);
          reject(error);
        };

        this.socket.onclose = () => {
          console.log("🔌 WebSocket disconnected");
          this.attemptReconnect(serverUrl);
        };
      } catch (error) {
        reject(error);
      }
    });
  }

  private attemptReconnect(serverUrl: string) {
    if (this.reconnectAttempts < this.maxReconnectAttempts) {
      this.reconnectAttempts++;
      console.log(
        `🔄 Reconnecting... (Attempt ${this.reconnectAttempts}/${this.maxReconnectAttempts})`
      );

      setTimeout(() => {
        this.connect(serverUrl);
      }, this.reconnectDelay);
    } else {
      console.error("❌ Max reconnection attempts reached");
    }
  }

  send(message: any): void {
    if (this.socket && this.socket.readyState === WebSocket.OPEN) {
      this.socket.send(JSON.stringify(message));
    } else {
      console.error("❌ Socket is not connected");
    }
  }

  on(messageType: string, handler: (data: any) => void): void {
    this.messageHandlers.set(messageType, handler);
  }

  off(messageType: string): void {
    this.messageHandlers.delete(messageType);
  }

  disconnect(): void {
    if (this.socket) {
      this.socket.close();
      this.socket = null;
    }
  }

  isConnected(): boolean {
    return this.socket !== null && this.socket.readyState === WebSocket.OPEN;
  }

  async sendFile(file: File, sender: string) {
    const arrayBuffer = await file.arrayBuffer();

    // ✅ Convert ArrayBuffer → Base64 (browser safe)
    const base64 = btoa(String.fromCharCode(...new Uint8Array(arrayBuffer)));

    this.send({
      type: "file",
      sender,
      fileName: file.name,
      fileType: file.type,
      fileSize: file.size,
      fileData: base64, // ✅ FIXED → matches Message interface
    });
  }
}

export const socketClient = new SocketClient();
