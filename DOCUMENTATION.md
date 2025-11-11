# 🎓 Project Documentation

## Architecture Overview

This is a **full-stack real-time chat application** demonstrating client-server architecture with the following components:

```
┌─────────────┐         ┌──────────────────┐         ┌─────────────┐
│   Browser   │ ◄─WS──► │  WebSocket Bridge│ ◄─TCP─► │ Java Server │
│  (Next.js)  │         │    (Node.js)     │         │  (Port 8081)│
└─────────────┘         └──────────────────┘         └──────┬──────┘
                         Port 8082                           │
                                                             │
                                                     ┌───────▼──────┐
                                                     │    MySQL     │
                                                     │   Database   │
                                                     └──────────────┘
```

## Component Details

### 1. Java Backend Server (Port 8081)

**Purpose:** Handles all business logic, authentication, and data persistence

**Key Components:**

- **MainServer.java** - Main server with ServerSocket accepting connections
- **ClientHandler.java** - Manages individual client connections (one thread per client)
- **UserAuthService.java** - Handles registration, login, password hashing
- **DatabaseManager.java** - Singleton pattern for DB connection management
- **MessageRepository.java** - CRUD operations for messages

**Technologies:**
- Java 17 (multithreading, socket programming)
- JDBC for database operations
- BCrypt for password security
- Gson for JSON serialization
- Maven for dependency management

### 2. WebSocket Bridge (Port 8082)

**Purpose:** Bridges WebSocket (browser) to TCP Socket (Java server)

**Why needed?**
- Browsers can only use WebSocket protocol
- Java server uses TCP Socket
- This bridge translates between the two

**Implementation:**
```javascript
WebSocket ◄──► Node.js ◄──► TCP Socket
(Browser)      (Bridge)      (Java)
```

### 3. Next.js Frontend (Port 3000)

**Purpose:** User interface and real-time messaging client

**Key Features:**
- React components for UI
- WebSocket client for real-time communication
- Context API for state management
- Tailwind CSS for styling

**Pages:**
- `/` - Landing page
- `/login` - User authentication
- `/register` - User registration
- `/chat` - Main chat interface

## Communication Protocol

### Message Flow

1. **User sends message:**
   ```
   Browser → WebSocket → Bridge → TCP → Java Server
   ```

2. **Server broadcasts:**
   ```
   Java Server → TCP → Bridge → WebSocket → All Browsers
   ```

### Message Format (JSON)

```json
{
  "type": "message_type",
  "sender": "user@example.com",
  "receiver": "optional@example.com",
  "content": "message content or JSON data",
  "username": "Display Name",
  "timestamp": 1234567890
}
```

### Message Types

| Type | Direction | Purpose |
|------|-----------|---------|
| `register` | Client → Server | Create new account |
| `login` | Client → Server | Authenticate user |
| `message` | Both | Public chat message |
| `private_message` | Both | Direct message |
| `user_list` | Both | Request/send online users |
| `history` | Both | Get message history |
| `user_joined` | Server → Client | User joined notification |
| `user_left` | Server → Client | User left notification |

## Database Design

### Users Table
```sql
CREATE TABLE users (
    id INT PRIMARY KEY AUTO_INCREMENT,
    email VARCHAR(255) UNIQUE NOT NULL,      -- Login identifier
    password VARCHAR(255) NOT NULL,          -- BCrypt hashed
    username VARCHAR(255) NOT NULL,          -- Display name
    status ENUM('online', 'offline'),        -- Current status
    created_at DATETIME DEFAULT NOW(),
    last_seen DATETIME DEFAULT NOW()
);
```

**Indexes:**
- PRIMARY KEY on `id`
- UNIQUE KEY on `email`

### Messages Table
```sql
CREATE TABLE messages (
    id INT PRIMARY KEY AUTO_INCREMENT,
    sender VARCHAR(255) NOT NULL,            -- User email
    receiver VARCHAR(255) NULL,              -- NULL = public message
    message TEXT NOT NULL,                   -- Message content
    timestamp DATETIME DEFAULT NOW(),        -- When sent
    is_read BOOLEAN DEFAULT FALSE,           -- Read status
    FOREIGN KEY (sender) REFERENCES users(email),
    FOREIGN KEY (receiver) REFERENCES users(email)
);
```

**Indexes:**
- PRIMARY KEY on `id`
- INDEX on `sender`
- INDEX on `receiver`
- INDEX on `timestamp`

## Security Implementation

### 1. Password Security
```java
// Registration - Hash password
String hashedPassword = BCrypt.hashpw(password, BCrypt.gensalt());

// Login - Verify password
if (BCrypt.checkpw(password, hashedPassword)) {
    // Password correct
}
```

**Why BCrypt?**
- Adaptive hashing (slow by design)
- Built-in salt
- Industry standard

### 2. SQL Injection Prevention
```java
// ❌ Bad - SQL Injection vulnerable
String query = "SELECT * FROM users WHERE email = '" + email + "'";

// ✅ Good - Prepared statement
PreparedStatement pstmt = conn.prepareStatement(
    "SELECT * FROM users WHERE email = ?"
);
pstmt.setString(1, email);
```

### 3. Input Validation
- Email format validation
- Password length requirements (min 6 chars)
- Username presence check
- Message content sanitization

## Advanced Features You Can Add

### 1. Private Messaging
Already supported in backend! Just need UI:

```java
// Send private message
Message pm = new Message();
pm.setType("private_message");
pm.setSender(myEmail);
pm.setReceiver(friendEmail);
pm.setContent("Hello!");
socket.send(pm.toJson());
```

### 2. Typing Indicators
```javascript
// Frontend
input.addEventListener('input', () => {
  socket.send({
    type: 'typing',
    content: 'typing'
  });
});

// Backend broadcasts to others
```

### 3. File Upload
Add to ClientHandler.java:
```java
case "file_upload":
    // Receive file bytes
    // Store in database or filesystem
    // Broadcast file availability
```

### 4. Message Reactions
Extend Message model:
```java
class Message {
    private Map<String, List<String>> reactions; // emoji -> list of users
}
```

### 5. Chat Rooms
Create ChatRoom class:
```java
class ChatRoom {
    private String id;
    private String name;
    private Set<String> members;
    private List<Message> messages;
}
```

## Performance Considerations

### Current Architecture
- ✅ Multi-threaded server (one thread per client)
- ✅ Connection pooling not needed (small scale)
- ✅ In-memory user list (fast access)
- ✅ Database indexes for fast queries

### For Production Scale
1. **Use thread pool instead of thread-per-client**
   ```java
   ExecutorService pool = Executors.newFixedThreadPool(100);
   ```

2. **Add message queue (Redis)**
   - Decouple message processing
   - Handle spikes in traffic

3. **Load balancer**
   - Multiple server instances
   - Distribute connections

4. **WebSocket directly from Java**
   - Use Java-WebSocket library
   - Remove Node.js bridge

## Testing Guide

### Unit Tests (Add to project)
```java
@Test
public void testUserRegistration() {
    UserAuthService auth = new UserAuthService();
    boolean result = auth.register(
        "test@test.com",
        "password123",
        "Test User"
    );
    assertTrue(result);
}
```

### Integration Tests
1. **Database Connection**
   - Can connect to MySQL?
   - Can execute queries?

2. **Socket Communication**
   - Can client connect?
   - Can messages be sent?

3. **Authentication Flow**
   - Register → Success
   - Login → Success
   - Wrong password → Fail

### Manual Testing
```bash
# Test with telnet
telnet localhost 8081

# Send JSON message
{"type":"register","content":"{\"email\":\"test@test.com\",\"password\":\"pass\",\"username\":\"Test\"}"}
```

## Code Quality

### Best Practices Used

1. **Separation of Concerns**
   - Server logic ≠ Database logic ≠ Auth logic
   - Each class has single responsibility

2. **Design Patterns**
   - Singleton (DatabaseManager)
   - Factory (Message creation)
   - Observer (Message broadcasting)

3. **Resource Management**
   ```java
   try (Connection conn = db.getConnection();
        PreparedStatement pstmt = conn.prepareStatement(sql)) {
       // Auto-closes resources
   }
   ```

4. **Error Handling**
   - Try-catch blocks
   - Meaningful error messages
   - Graceful degradation

## Troubleshooting

### Common Issues

**"Port already in use"**
```powershell
# Windows
netstat -ano | findstr :8081
taskkill /PID <PID> /F
```

**"Database connection failed"**
- Check MySQL is running
- Verify credentials in db_config.properties
- Ensure database exists

**"WebSocket connection refused"**
- Start servers in order: Bridge → Java → Frontend
- Check all ports are available

## Learning Resources

### Java Socket Programming
- Oracle Socket Tutorial
- Java Network Programming (book)

### WebSocket
- MDN WebSocket API
- Socket.io documentation

### Database
- JDBC Tutorial
- MySQL optimization

### Security
- OWASP Top 10
- Password hashing best practices

## Future Enhancements

- [ ] JWT token authentication
- [ ] Message encryption (end-to-end)
- [ ] Voice/Video calling
- [ ] File sharing
- [ ] Group chats/rooms
- [ ] Message search
- [ ] User profiles with avatars
- [ ] Push notifications
- [ ] Mobile app (React Native)
- [ ] Docker containerization

## Contributing

This is an educational project. Feel free to:
1. Fork the repository
2. Add features
3. Improve documentation
4. Share with others learning networking

## Questions?

Common questions answered:

**Q: Why not use Spring Boot?**
A: This demonstrates raw socket programming concepts.

**Q: Why Node.js bridge?**
A: Browsers require WebSocket; Java server uses TCP. Bridge translates.

**Q: Can I use this in production?**
A: Add security hardening, testing, monitoring first.

**Q: How many users can it handle?**
A: Current: ~100 concurrent. With optimization: 1000+

---

**Happy Coding! 🚀**
