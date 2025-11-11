# Admin-Member Real-Time Chat Application

A complete WebSocket-based chat application where **Admins** can communicate with **Members** in real-time.

## 🎯 Features

### Admin Features
✅ View all registered members  
✅ Send direct messages to any member  
✅ See member online/offline status  
✅ Real-time message delivery  
✅ Typing indicators  
✅ Message history  

### Member Features
✅ Register new account  
✅ Login to member dashboard  
✅ View all admins  
✅ Send direct messages to admins  
✅ Real-time message delivery  
✅ Typing indicators  
✅ Message history  

## 📁 Project Structure

```
chat-app/
├── backend/
│   ├── src/main/
│   │   ├── java/com/chatapp/
│   │   │   ├── server/
│   │   │   │   ├── MainServer.java           # Main WebSocket server
│   │   │   │   └── ClientHandler.java        # Handles individual clients
│   │   │   ├── auth/
│   │   │   │   └── UserAuthService.java      # Authentication logic
│   │   │   ├── database/
│   │   │   │   ├── DatabaseManager.java      # DB connection management
│   │   │   │   └── MessageRepository.java    # Message CRUD operations
│   │   │   └── model/
│   │   │       ├── User.java                 # User model
│   │   │       └── Message.java              # Message model
│   │   └── resources/
│   │       ├── schema.sql                    # Database schema
│   │       └── db_config.properties          # DB configuration
│   ├── websocket-bridge.js                   # WebSocket <-> TCP bridge
│   ├── pom.xml                                # Maven configuration
│   └── package.json                           # Node.js configuration
│
└── frontend/                                  # Next.js frontend
    ├── app/
    │   ├── page.tsx                          # Landing/login selector
    │   ├── admin/
    │   │   └── page.tsx                      # Admin dashboard
    │   ├── member/
    │   │   └── page.tsx                      # Member dashboard
    │   ├── login/
    │   │   └── page.tsx                      # Member login
    │   └── register/
    │       └── page.tsx                      # Member registration
    ├── components/
    │   ├── ChatInterface.tsx                 # Main chat UI
    │   ├── UserList.tsx                      # User sidebar
    │   └── MessageBubble.tsx                 # Message component
    ├── lib/
    │   ├── socket.ts                         # WebSocket service
    │   └── context.tsx                       # Global state management
    └── types/
        └── index.ts                          # TypeScript types
```

## 🛠 Technologies

### Backend
- **Java 17** - Main programming language
- **MySQL** - Database for users and messages
- **BCrypt** - Password hashing
- **Gson** - JSON serialization
- **Java Sockets** - TCP communication
- **Node.js + ws** - WebSocket bridge

### Frontend
- **Next.js 14** - React framework
- **TypeScript** - Type safety
- **Tailwind CSS** - Styling
- **WebSocket API** - Real-time communication

## 📋 Prerequisites

1. **Java Development Kit (JDK) 17+**
   ```bash
   java -version
   ```

2. **Maven**
   ```bash
   mvn -version
   ```

3. **MySQL Server** (XAMPP recommended)
   - Download: https://www.apachefriends.org/

4. **Node.js 18+**
   ```bash
   node -version
   npm -version
   ```

## 🚀 Installation & Setup

### 1. Database Setup

1. Start XAMPP and start MySQL service
2. Open phpMyAdmin (http://localhost/phpmyadmin)
3. Run the schema file:

```bash
# From backend directory
mysql -u root -p < src/main/resources/schema.sql
```

Or manually create the database and run the SQL from `schema.sql`

4. Update database credentials in `backend/src/main/resources/db_config.properties`:

```properties
db.url=jdbc:mysql://localhost:3306/chat_app?useSSL=false&serverTimezone=UTC&allowPublicKeyRetrieval=true
db.username=root
db.password=YOUR_MYSQL_PASSWORD
```

### 2. Backend Setup

```bash
# Navigate to backend directory
cd backend

# Install Maven dependencies and compile
mvn clean compile

# Copy dependencies
mvn dependency:copy-dependencies -DoutputDirectory=target/dependency

# Install Node.js dependencies for WebSocket bridge
npm install
```

### 3. Frontend Setup

```bash
# Navigate to frontend directory
cd frontend

# Install dependencies
npm install
```

## ▶️ Running the Application

You need to run **3 separate terminals**:

### Terminal 1: Java Server
```bash
cd backend
java -cp "target/classes;target/dependency/*" com.chatapp.server.MainServer
```

Or on macOS/Linux:
```bash
cd backend
java -cp "target/classes:target/dependency/*" com.chatapp.server.MainServer
```

**Expected output:**
```
✅ Chat Server started on port 8081
Waiting for connections...
```

### Terminal 2: WebSocket Bridge
```bash
cd backend
npm start
```

**Expected output:**
```
✅ WebSocket Bridge started on port 8082
Connecting to Java server at localhost:8081
```

### Terminal 3: Frontend
```bash
cd frontend
npm run dev
```

**Expected output:**
```
✓ Ready in 2.5s
- Local: http://localhost:3000
```

## 🎯 Usage

### Access the Application

1. **Landing Page**: http://localhost:3000
2. **Admin Login**: Click "Admin Login"
3. **Member Login/Register**: Click "Member Login" or "Register"

### Default Credentials

**Admin:**
- Email: `admin@chat.com`
- Password: `admin123`

**Member:**
- Email: `member@chat.com`
- Password: `admin123`

### Testing the Application

1. **Register a new member:**
   - Go to http://localhost:3000/register
   - Fill in details and register
   - Login with the new credentials

2. **Admin Dashboard:**
   - Login as admin
   - See all members in the sidebar
   - Click on a member to start chatting
   - Send messages in real-time

3. **Member Dashboard:**
   - Login as member
   - See all admins in the sidebar
   - Click on an admin to start chatting
   - Send messages in real-time

4. **Test Real-Time Features:**
   - Open two browser windows
   - Login as admin in one, member in another
   - Send messages back and forth
   - See typing indicators
   - Check online/offline status

## 📡 WebSocket Protocol

All messages use JSON format:

### Login (Admin)
```json
{
  "type": "login_admin",
  "email": "admin@chat.com",
  "password": "admin123"
}
```

### Login (Member)
```json
{
  "type": "login_member",
  "email": "member@chat.com",
  "password": "password123"
}
```

### Register (Member)
```json
{
  "type": "register_member",
  "email": "new@member.com",
  "password": "password123",
  "username": "New Member"
}
```

### Send Message
```json
{
  "type": "chat_message",
  "receiverEmail": "receiver@email.com",
  "receiverType": "admin",
  "content": "Hello!"
}
```

### Get User List
```json
{
  "type": "get_users"
}
```

### Get Conversation History
```json
{
  "type": "get_conversation",
  "otherUserEmail": "other@email.com",
  "otherUserType": "member"
}
```

### Typing Indicator
```json
{
  "type": "typing",
  "receiverEmail": "receiver@email.com",
  "receiverType": "admin",
  "isTyping": true
}
```

## 🗄 Database Schema

### Admins Table
```sql
CREATE TABLE admins (
    id INT PRIMARY KEY AUTO_INCREMENT,
    email VARCHAR(255) UNIQUE NOT NULL,
    password VARCHAR(255) NOT NULL,
    username VARCHAR(255) NOT NULL,
    status ENUM('online', 'offline') DEFAULT 'offline',
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);
```

### Members Table
```sql
CREATE TABLE members (
    id INT PRIMARY KEY AUTO_INCREMENT,
    email VARCHAR(255) UNIQUE NOT NULL,
    password VARCHAR(255) NOT NULL,
    username VARCHAR(255) NOT NULL,
    status ENUM('online', 'offline') DEFAULT 'offline',
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);
```

### Messages Table
```sql
CREATE TABLE messages (
    id INT PRIMARY KEY AUTO_INCREMENT,
    sender_email VARCHAR(255) NOT NULL,
    sender_type ENUM('admin', 'member') NOT NULL,
    receiver_email VARCHAR(255) NOT NULL,
    receiver_type ENUM('admin', 'member') NOT NULL,
    message TEXT NOT NULL,
    timestamp DATETIME DEFAULT CURRENT_TIMESTAMP,
    is_read BOOLEAN DEFAULT FALSE
);
```

## 🔐 Security Features

✅ **Password Hashing** - BCrypt with salt  
✅ **Input Validation** - Email format, password length  
✅ **SQL Injection Prevention** - Prepared statements  
✅ **Session Management** - User status tracking  
✅ **Separate User Types** - Admin and member isolation  

## 🚦 Troubleshooting

### Backend won't start
- ✅ Check MySQL is running (XAMPP)
- ✅ Verify database `chat_app` exists
- ✅ Check `db_config.properties` credentials
- ✅ Ensure port 8081 is not in use

### WebSocket bridge error
- ✅ Ensure Java server is running first
- ✅ Check port 8082 is not in use
- ✅ Verify Node.js and npm are installed

### Frontend can't connect
- ✅ Ensure WebSocket bridge is running (port 8082)
- ✅ Ensure Java server is running (port 8081)
- ✅ Check browser console for errors
- ✅ Clear browser cache and reload

### Maven build errors
```bash
# Clean and rebuild
mvn clean install

# Or skip tests
mvn clean package -DskipTests
```

### Port already in use
```bash
# Windows - Kill process on port 8081
netstat -ano | findstr :8081
taskkill /PID <PID> /F

# macOS/Linux - Kill process on port 8081
lsof -ti:8081 | xargs kill -9
```

## 📸 Features Overview

### Admin Dashboard
- View all members
- Online/offline indicators
- Real-time messaging
- Message history
- Typing indicators

### Member Dashboard
- View all admins
- Online/offline indicators
- Real-time messaging
- Message history
- Typing indicators
- Registration system

## 🔄 Architecture

```
Browser (Frontend)
    ↕ WebSocket
WebSocket Bridge (Node.js)
    ↕ TCP Socket
Java Server (Backend)
    ↕ JDBC
MySQL Database
```

## 📝 API Endpoints Summary

| Message Type | Direction | Description |
|-------------|-----------|-------------|
| `login_admin` | Client → Server | Admin authentication |
| `login_member` | Client → Server | Member authentication |
| `register_member` | Client → Server | New member registration |
| `chat_message` | Client ↔ Server | Send/receive messages |
| `get_users` | Client → Server | Request user list |
| `user_list` | Server → Client | Response with users |
| `get_conversation` | Client → Server | Load chat history |
| `conversation_history` | Server → Client | Chat history response |
| `typing` | Client ↔ Server | Typing indicators |
| `user_joined` | Server → Client | User online notification |
| `user_left` | Server → Client | User offline notification |

## 🎓 Learning Outcomes

This project demonstrates:
- Multi-threaded Java server programming
- WebSocket and TCP socket communication
- Database design and JDBC connectivity
- User authentication and password security
- Real-time bidirectional communication
- Modern frontend development with Next.js
- Full-stack application architecture
- State management in React

## 📄 License

This project is created for educational purposes.

## 👨‍💻 Support

For issues or questions, please check:
1. All services are running (Java, WebSocket bridge, Frontend)
2. Database is properly configured
3. Ports are not blocked or in use
4. Browser console for frontend errors
5. Server logs for backend errors

---

**Happy Chatting! 💬**
