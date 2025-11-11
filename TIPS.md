# 💡 Tips & Tricks

## Development Tips

### Hot Reload During Development

**Frontend (Next.js):**
- ✅ Already has hot reload - just save files and see changes instantly
- No need to restart the dev server

**Backend (Java):**
- ❌ No hot reload - must rebuild after changes
- Quick rebuild: `mvn compile` (faster than full package)
- Or use IntelliJ IDEA with auto-reload

### Debugging

**Backend Debugging:**
```java
// Add debug prints
System.out.println("🔍 Debug: " + variable);

// Or use proper logging
Logger logger = Logger.getLogger(MainServer.class.getName());
logger.info("User connected: " + email);
```

**Frontend Debugging:**
```javascript
// Browser console
console.log('Message received:', message);

// React DevTools
// Install Chrome extension: React Developer Tools
```

**Database Debugging:**
```sql
-- Check what's in the database
SELECT * FROM users;
SELECT * FROM messages ORDER BY timestamp DESC LIMIT 20;

-- Check online users
SELECT email, username, status FROM users WHERE status = 'online';
```

## Common Modifications

### Change Server Port

**Java Server:**
```java
// In MainServer.java, line 17
private static final int PORT = 8081; // Change to your port
```

**WebSocket Bridge:**
```javascript
// In websocket-bridge.js, line 4-5
const WS_PORT = 8082;           // Change WebSocket port
const JAVA_SERVER_PORT = 8081;  // Must match Java server
```

**Frontend:**
```typescript
// In lib/socket.ts and lib/context.tsx
const ws = new WebSocket('ws://localhost:8082'); // Change to your bridge port
```

### Change Database Configuration

Edit `backend/src/main/resources/db_config.properties`:
```properties
db.url=jdbc:mysql://localhost:3306/chat_app
db.username=root
db.password=YOUR_PASSWORD  # Change this
```

### Customize UI Colors

Edit `frontend/app/globals.css` or Tailwind classes:
```css
/* Primary color */
.bg-blue-600 → .bg-purple-600

/* Gradients */
from-blue-50 via-white to-purple-50 → your colors
```

## Performance Optimization

### Database Indexes
Already added! But if you add new queries:
```sql
-- Add index on frequently queried columns
CREATE INDEX idx_column_name ON table_name(column_name);
```

### Limit Message History
```java
// In MessageRepository.java
// Change limit in getRecentPublicMessages()
public List<Message> getRecentPublicMessages(int limit) {
    // Default is 50, increase for more history
}
```

### Thread Pool (for production)
```java
// In MainServer.java
// Replace thread-per-client with pool
private final ExecutorService threadPool = 
    Executors.newFixedThreadPool(50); // Max 50 threads
```

## Testing Tips

### Test Registration
```powershell
# Using curl
curl -X POST http://localhost:8082 \
  -H "Content-Type: application/json" \
  -d '{"type":"register","content":"{\"email\":\"test@test.com\",\"password\":\"pass123\",\"username\":\"Test\"}"}'
```

### Test Database Connection
```sql
-- In MySQL command line or phpMyAdmin
USE chat_app;
SHOW TABLES;
DESCRIBE users;
DESCRIBE messages;
```

### Test Multiple Users
1. Open http://localhost:3000 in Chrome
2. Open http://localhost:3000 in Incognito mode
3. Open http://localhost:3000 in Firefox
4. Register different users in each
5. Chat and see real-time updates

## Useful MySQL Commands

```sql
-- Clear all messages
TRUNCATE TABLE messages;

-- Delete a user
DELETE FROM users WHERE email = 'test@example.com';

-- Reset auto-increment
ALTER TABLE users AUTO_INCREMENT = 1;

-- See all users
SELECT id, email, username, status FROM users;

-- Count messages
SELECT COUNT(*) FROM messages;

-- Recent activity
SELECT 
    u.username,
    COUNT(m.id) as message_count
FROM users u
LEFT JOIN messages m ON u.email = m.sender
GROUP BY u.email
ORDER BY message_count DESC;
```

## Git Commands (Version Control)

```bash
# Initialize git repository
git init

# Add all files
git add .

# Commit
git commit -m "Initial commit: Full-stack chat application"

# Create repository on GitHub, then:
git remote add origin https://github.com/yourusername/chat-app.git
git branch -M main
git push -u origin main
```

## Environment Variables (Production)

Instead of hardcoding, use environment variables:

**Backend:**
```java
// Read from environment
String dbPassword = System.getenv("DB_PASSWORD");
String dbUrl = System.getenv("DB_URL");
```

**Frontend:**
Create `.env.local`:
```
NEXT_PUBLIC_WS_URL=ws://localhost:8082
```

Use in code:
```typescript
const wsUrl = process.env.NEXT_PUBLIC_WS_URL || 'ws://localhost:8082';
```

## Docker Deployment (Advanced)

**Dockerfile for Backend:**
```dockerfile
FROM openjdk:17-slim
COPY target/chat-server-1.0-SNAPSHOT-jar-with-dependencies.jar app.jar
EXPOSE 8081
CMD ["java", "-jar", "app.jar"]
```

**Dockerfile for Frontend:**
```dockerfile
FROM node:18-alpine
WORKDIR /app
COPY package*.json ./
RUN npm install
COPY . .
RUN npm run build
EXPOSE 3000
CMD ["npm", "start"]
```

## Monitoring & Logging

### Add Logging to Java
```java
import java.util.logging.*;

public class MainServer {
    private static final Logger logger = 
        Logger.getLogger(MainServer.class.getName());
    
    public void start() {
        logger.info("Server starting...");
        logger.warning("Something unusual happened");
        logger.severe("Critical error!");
    }
}
```

### Frontend Error Tracking
```typescript
// Add to app/layout.tsx
window.addEventListener('error', (event) => {
  console.error('Global error:', event.error);
  // Send to error tracking service (Sentry, LogRocket, etc.)
});
```

## Code Snippets

### Add Emoji Picker
```bash
npm install emoji-picker-react
```

```typescript
import EmojiPicker from 'emoji-picker-react';

// In MessageInput component
const [showEmojiPicker, setShowEmojiPicker] = useState(false);

// Add emoji to message
const handleEmojiClick = (emojiObject: any) => {
  setMessage(prev => prev + emojiObject.emoji);
};
```

### Add Message Timestamps
```typescript
import { formatDistanceToNow } from 'date-fns';

// In MessageBubble
<span>
  {formatDistanceToNow(new Date(message.timestamp), { addSuffix: true })}
</span>
```

### Add Sound Notifications
```typescript
// Play sound when message received
const playNotificationSound = () => {
  const audio = new Audio('/notification.mp3');
  audio.play();
};

// In message handler
if (message.sender !== user.email) {
  playNotificationSound();
}
```

## Keyboard Shortcuts

### Development
- `Ctrl + C` - Stop server
- `Ctrl + Shift + R` - Hard reload browser
- `F12` - Open DevTools
- `Ctrl + Shift + I` - Inspect element

### VS Code
- `Ctrl + P` - Quick file open
- `Ctrl + Shift + F` - Search in files
- `Ctrl + B` - Toggle sidebar
- `Ctrl + `` ` `` - Toggle terminal

## Security Checklist

Before deploying:
- [ ] Change default database password
- [ ] Use environment variables for secrets
- [ ] Enable HTTPS (SSL certificates)
- [ ] Add rate limiting
- [ ] Implement CSRF protection
- [ ] Add input sanitization
- [ ] Enable CORS properly
- [ ] Use prepared statements (already done ✅)
- [ ] Hash passwords (already done ✅)
- [ ] Validate all inputs (already done ✅)

## Backup & Recovery

### Backup Database
```bash
# Backup
mysqldump -u root -p chat_app > backup.sql

# Restore
mysql -u root -p chat_app < backup.sql
```

### Backup Code
```bash
# Create archive
tar -czf chat-app-backup.tar.gz network-assignment/

# Extract
tar -xzf chat-app-backup.tar.gz
```

## Useful Links

### Documentation
- Java Socket: https://docs.oracle.com/javase/tutorial/networking/sockets/
- Next.js: https://nextjs.org/docs
- MySQL: https://dev.mysql.com/doc/
- WebSocket: https://developer.mozilla.org/en-US/docs/Web/API/WebSocket

### Tools
- MySQL Workbench: https://www.mysql.com/products/workbench/
- Postman: https://www.postman.com/ (for API testing)
- IntelliJ IDEA: https://www.jetbrains.com/idea/
- VS Code: https://code.visualstudio.com/

### Learning
- Java Concurrency: https://docs.oracle.com/javase/tutorial/essential/concurrency/
- React Hooks: https://react.dev/reference/react
- Tailwind CSS: https://tailwindcss.com/docs

## Troubleshooting Quick Fixes

**"Cannot find symbol" error:**
```bash
mvn clean install
```

**Frontend won't start:**
```bash
rm -rf node_modules package-lock.json
npm install
```

**Database connection timeout:**
```properties
# Add to db_config.properties
db.url=jdbc:mysql://localhost:3306/chat_app?useSSL=false&serverTimezone=UTC&allowPublicKeyRetrieval=true
```

**Port already in use:**
```powershell
# Windows
netstat -ano | findstr :<PORT>
taskkill /PID <PID> /F
```

**Maven dependencies not downloading:**
```bash
mvn clean install -U
```

## Pro Tips

1. **Use IntelliJ IDEA** for Java development (better than Eclipse)
2. **Keep terminals organized** - One per server with clear labels
3. **Test incrementally** - Don't build everything before first test
4. **Check logs first** - Most errors show up in console
5. **Use GitHub** - Version control is essential
6. **Comment your code** - Future you will thank you
7. **Read error messages** - They usually tell you what's wrong
8. **Google the error** - Someone else had it too
9. **Use DevTools** - Network tab shows WebSocket messages
10. **Take breaks** - Fresh eyes catch bugs faster

## Fun Customizations

### Change Welcome Message
```java
// In MainServer.java
System.out.println("🚀 YOUR CUSTOM MESSAGE HERE");
```

### Add Custom Emojis
```typescript
// In MessageBubble.tsx
const customEmojis = {
  ':fire:': '🔥',
  ':rocket:': '🚀',
  ':heart:': '❤️'
};
```

### Custom Color Themes
```typescript
// Create themes.ts
export const themes = {
  light: { primary: 'blue', secondary: 'purple' },
  dark: { primary: 'indigo', secondary: 'pink' }
};
```

---

**Happy Coding! 🚀**

*Remember: Every expert was once a beginner. Keep learning!*
