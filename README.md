# Real-Time Chat Application

A full-stack real-time chat application with **Java backend** (Socket server + MySQL) and **Next.js frontend**.

## 🚀 Features

- ✅ **User Authentication** - Email/password registration and login with BCrypt encryption
- ✅ **Real-Time Messaging** - Instant message delivery using WebSocket/TCP sockets
- ✅ **Group Chat** - Public chat room where all users can communicate
- ✅ **Online Users** - See who's currently online
- ✅ **Message History** - All messages stored in MySQL database
- ✅ **Modern UI** - Clean, responsive interface built with Next.js and Tailwind CSS
- ✅ **Multi-threaded Server** - Java backend handles multiple concurrent connections

## 📁 Project Structure

```
network-assignment/
├── backend/              # Java server
│   ├── src/
│   │   └── main/
│   │       ├── java/com/chatapp/
│   │       │   ├── server/
│   │       │   │   ├── MainServer.java
│   │       │   │   └── ClientHandler.java
│   │       │   ├── auth/
│   │       │   │   └── UserAuthService.java
│   │       │   ├── database/
│   │       │   │   ├── DatabaseManager.java
│   │       │   │   └── MessageRepository.java
│   │       │   └── model/
│   │       │       ├── User.java
│   │       │       └── Message.java
│   │       └── resources/
│   │           ├── schema.sql
│   │           └── db_config.properties
│   ├── websocket-bridge.js
│   ├── package.json
│   └── pom.xml
│
└── frontend/             # Next.js client
    ├── app/
    │   ├── login/
    │   ├── register/
    │   ├── chat/
    │   └── layout.tsx
    ├── components/
    │   ├── MessageBubble.tsx
    │   ├── MessageInput.tsx
    │   └── UserList.tsx
    ├── lib/
    │   ├── context.tsx
    │   └── socket.ts
    └── types/
        └── index.ts
```

## 🛠️ Technologies

### Backend
- **Java 17** - Programming language
- **MySQL** - Database for users and messages
- **JDBC** - Database connectivity
- **Gson** - JSON serialization
- **BCrypt** - Password hashing
- **Java Sockets** - Network communication
- **Node.js + ws** - WebSocket bridge

### Frontend
- **Next.js 14** - React framework
- **TypeScript** - Type safety
- **Tailwind CSS** - Styling
- **Lucide React** - Icons
- **WebSocket API** - Real-time communication

## 📋 Prerequisites

1. **Java Development Kit (JDK) 17+**
   - Download: https://www.oracle.com/java/technologies/downloads/

2. **Maven**
   - Download: https://maven.apache.org/download.cgi

3. **MySQL Server** (XAMPP recommended)
   - Download: https://www.apachefriends.org/

4. **Node.js 18+**
   - Download: https://nodejs.org/

## 🚀 Installation & Setup

### 1. Database Setup

1. Start XAMPP and start MySQL service
2. Open phpMyAdmin (http://localhost/phpmyadmin)
3. Create database and run schema:

```sql
-- Or run the SQL file directly
mysql -u root < backend/src/main/resources/schema.sql
```

4. Configure database connection in `backend/src/main/resources/db_config.properties`:

```properties
db.url=jdbc:mysql://localhost:3306/chat_app?useSSL=false&serverTimezone=UTC
db.username=root
db.password=YOUR_MYSQL_PASSWORD
```

### 2. Backend Setup

```powershell
# Navigate to backend
cd backend

# Install dependencies and build
mvn clean package

# Install Node.js dependencies for WebSocket bridge
npm install

# Run WebSocket bridge (Terminal 1)
npm start

# Run Java server (Terminal 2)
java -jar target/chat-server-1.0-SNAPSHOT-jar-with-dependencies.jar
```

### 3. Frontend Setup

```powershell
# Navigate to frontend
cd frontend

# Install dependencies
npm install

# Run development server
npm run dev
```

### 4. Access the Application

- **Frontend**: http://localhost:3000
- **WebSocket Bridge**: ws://localhost:8082
- **Java Server**: localhost:8081

## 🎯 Usage

1. **Register** a new account at http://localhost:3000/register
2. **Login** with your credentials
3. **Start chatting** in the group chat room
4. **See online users** in the sidebar
5. **Send messages** in real-time

## 📡 Message Protocol

All messages use JSON format:

```json
{
  "type": "message | login | register | user_list | ...",
  "sender": "user@example.com",
  "receiver": "optional@example.com",
  "content": "message content",
  "username": "John Doe",
  "timestamp": 1234567890
}
```

### Message Types:
- `register` - User registration
- `login` - User authentication
- `message` - Public chat message
- `private_message` - Direct message to specific user
- `user_list` - Request/receive online users
- `history` - Retrieve message history
- `user_joined` - User joined notification
- `user_left` - User left notification
- `typing` - Typing indicator

## 🗄️ Database Schema

### Users Table
```sql
CREATE TABLE users (
  id INT PRIMARY KEY AUTO_INCREMENT,
  email VARCHAR(255) UNIQUE NOT NULL,
  password VARCHAR(255) NOT NULL,
  username VARCHAR(255) NOT NULL,
  status ENUM('online', 'offline') DEFAULT 'offline'
);
```

### Messages Table
```sql
CREATE TABLE messages (
  id INT PRIMARY KEY AUTO_INCREMENT,
  sender VARCHAR(255) NOT NULL,
  receiver VARCHAR(255) NULL,
  message TEXT NOT NULL,
  timestamp DATETIME DEFAULT CURRENT_TIMESTAMP
);
```

## 🔐 Security Features

- ✅ **Password Hashing** - BCrypt with salt
- ✅ **Input Validation** - Email format, password length
- ✅ **SQL Injection Prevention** - Prepared statements
- ✅ **Session Management** - User status tracking

## 🚦 Troubleshooting

### Backend won't start
- ✅ Check MySQL is running (XAMPP)
- ✅ Verify database `chat_app` exists
- ✅ Check `db_config.properties` credentials

### Frontend can't connect
- ✅ Ensure WebSocket bridge is running (port 8082)
- ✅ Ensure Java server is running (port 8081)
- ✅ Check browser console for errors

### Build errors
```powershell
# Clean and rebuild
mvn clean install

# Or skip tests
mvn clean package -DskipTests
```

## 📸 Screenshots

### Login Page
Modern authentication interface with form validation.

### Chat Interface
Real-time messaging with online user list and message history.

## 🎓 Learning Outcomes

This project demonstrates:
- Multi-threaded Java server programming
- Socket programming and network protocols
- Database design and JDBC connectivity
- Authentication and password security
- WebSocket communication
- Modern frontend development with Next.js
- Full-stack application architecture

## 📝 License

This project is created for educational purposes.

## 👨‍💻 Author

Built as a network programming assignment demonstrating real-time communication systems.

## 🙏 Acknowledgments

- Java Socket Programming
- Next.js Documentation
- MySQL Documentation
- BCrypt Password Hashing
