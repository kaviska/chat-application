# 🧪 Testing Your Chat Application

## Quick Test Scenarios

### Test 1: Single User Registration & Login ✅

**Already Completed!**
```
✅ User registered: kaviska525@gmail.com
✅ Username: Kaviska Dilshan
✅ Password: Hashed with BCrypt
✅ Status: Stored in database
```

### Test 2: Multiple Users Chat

**Steps:**

1. **User 1 (Already registered):**
   - Open http://localhost:3000
   - Login with: kaviska525@gmail.com
   - Go to chat page

2. **User 2 (New user):**
   - Open http://localhost:3000 in **Incognito/Private window** or **different browser**
   - Click "Sign Up"
   - Register as:
     ```
     Username: John Doe
     Email: john@example.com
     Password: password123
     ```
   - Login and go to chat

3. **Test Real-Time Messaging:**
   - User 1: Send message "Hello from User 1!"
   - User 2: Should see the message instantly
   - User 2: Reply "Hi from User 2!"
   - User 1: Should see the reply instantly

**Expected Results:**
- ✅ Both users appear in "Online Users" sidebar
- ✅ Messages appear instantly for both users
- ✅ Timestamps are displayed
- ✅ User names are shown with messages

### Test 3: Verify Database Persistence

**Open phpMyAdmin:** http://localhost/phpmyadmin

**Query 1: Check Users**
```sql
SELECT id, email, username, status FROM users;
```
**Expected Output:**
```
+----+------------------------+------------------+--------+
| id | email                  | username         | status |
+----+------------------------+------------------+--------+
| 1  | kaviska525@gmail.com   | Kaviska Dilshan  | online |
| 2  | john@example.com       | John Doe         | online |
+----+------------------------+------------------+--------+
```

**Query 2: Check Messages**
```sql
SELECT sender, message, timestamp 
FROM messages 
ORDER BY timestamp DESC 
LIMIT 5;
```
**Expected Output:**
```
+------------------------+----------------------+---------------------+
| sender                 | message              | timestamp           |
+------------------------+----------------------+---------------------+
| john@example.com       | Hi from User 2!      | 2025-11-05 14:15:23 |
| kaviska525@gmail.com   | Hello from User 1!   | 2025-11-05 14:15:10 |
+------------------------+----------------------+---------------------+
```

### Test 4: User Join/Leave Notifications

**Steps:**

1. **User 1 & User 2 are chatting**
2. **User 3 joins:**
   - Open third browser/window
   - Register: alice@example.com
   - Login

**Expected Results:**
- ✅ User 1 & 2 see: "Alice joined the chat"
- ✅ All 3 users appear in sidebar
- ✅ Alice sees message history

3. **User 2 logs out:**
   - Click logout button

**Expected Results:**
- ✅ User 1 & 3 see: "John Doe left the chat"
- ✅ User 2 disappears from sidebar

### Test 5: Message History on Login

**Steps:**

1. **Close User 1's browser** (while messages exist)
2. **Reopen browser** and login as User 1
3. **Check chat page**

**Expected Results:**
- ✅ Previous messages are loaded
- ✅ Last 50 messages shown
- ✅ Correct timestamps
- ✅ Correct usernames

### Test 6: Server Logs Monitoring

**Check Backend Terminal:**

You should see logs like:
```
👤 Client added: kaviska525@gmail.com (Total: 1)
📨 Received: {"type":"message","content":"Hello!"}
👤 Client added: john@example.com (Total: 2)
👋 Client removed: john@example.com (Total: 1)
```

**Check WebSocket Bridge Terminal:**

You should see:
```
🔌 New WebSocket client connected
📤 WS → TCP: {"type":"login"...}
📥 TCP → WS: {"type":"login_response"...}
📤 WS → TCP: {"type":"message"...}
```

### Test 7: Error Handling

**Test 7.1: Wrong Password**
1. Try to login with wrong password
2. Expected: Error message "Invalid credentials"

**Test 7.2: Duplicate Email**
1. Try to register with existing email
2. Expected: Error "Email already exists"

**Test 7.3: Weak Password**
1. Try to register with password "123"
2. Expected: Error "Password must be at least 6 characters"

**Test 7.4: Server Down**
1. Stop Java server
2. Try to send message
3. Expected: Connection error, "Disconnected" status

### Test 8: Concurrent Users Stress Test

**Steps:**

1. Open 5-10 browser windows/tabs
2. Register different users in each
3. All users login simultaneously
4. All users send messages rapidly

**Expected Results:**
- ✅ All users can connect
- ✅ All messages are delivered
- ✅ No messages lost
- ✅ Correct message order
- ✅ Server handles load

### Test 9: Private Messaging (Advanced)

**Backend supports it! Just need to implement UI or test via browser console:**

```javascript
// In browser console while logged in as User 1
const socket = new WebSocket('ws://localhost:8082');

socket.onopen = () => {
  // Login first
  socket.send(JSON.stringify({
    type: 'login',
    content: JSON.stringify({
      email: 'kaviska525@gmail.com',
      password: 'Malidunew@123'
    })
  }));
  
  // Send private message to john@example.com
  setTimeout(() => {
    socket.send(JSON.stringify({
      type: 'private_message',
      receiver: 'john@example.com',
      content: 'This is a private message!'
    }));
  }, 2000);
};

socket.onmessage = (event) => {
  console.log('Received:', JSON.parse(event.data));
};
```

## 📊 Testing Checklist

### Functionality Tests
- [ ] User can register
- [ ] User can login
- [ ] User can send messages
- [ ] Messages appear in real-time
- [ ] Multiple users can chat
- [ ] User list updates
- [ ] User join/leave notifications
- [ ] Message history loads
- [ ] Timestamps are correct
- [ ] Logout works

### Database Tests
- [ ] Users saved to database
- [ ] Passwords are hashed
- [ ] Messages saved to database
- [ ] User status updates (online/offline)
- [ ] Foreign key relationships work
- [ ] Queries are efficient

### Security Tests
- [ ] Passwords are hashed (check database)
- [ ] Wrong password rejected
- [ ] Duplicate emails prevented
- [ ] SQL injection prevented (try: email = "' OR '1'='1")
- [ ] XSS prevented (try sending: `<script>alert('xss')</script>`)

### Performance Tests
- [ ] 10+ users can connect
- [ ] Messages deliver in < 1 second
- [ ] Server doesn't crash
- [ ] Database queries are fast
- [ ] Memory doesn't leak

### UI/UX Tests
- [ ] Responsive design works
- [ ] Forms validate input
- [ ] Error messages show
- [ ] Loading states work
- [ ] Auto-scroll to new messages
- [ ] Clean, readable interface

## 🐛 Known Issues to Test

### Issue 1: WebSocket Reconnection
**Test:** Kill WebSocket bridge while users are connected
**Expected:** Users should get disconnected status
**Fix:** Already implemented auto-reconnect in socket.ts

### Issue 2: Duplicate Messages
**Test:** Send message very quickly (spam)
**Check:** Each message appears only once
**Status:** ✅ Should work fine

### Issue 3: Long Messages
**Test:** Send very long message (1000+ characters)
**Check:** Message displays properly, doesn't break UI
**Status:** ✅ TEXT field in DB handles it

## 📈 Performance Benchmarks

### Expected Performance
- **Users:** Up to 100 concurrent users
- **Messages:** 1000+ messages per minute
- **Latency:** < 100ms message delivery
- **Database:** < 50ms query time

### How to Test
```powershell
# Monitor server CPU/Memory
Get-Process java | Select-Object CPU, WorkingSet

# Monitor active connections
netstat -an | findstr :8081 | Measure-Object
```

## ✅ Acceptance Criteria

Your application passes if:

1. ✅ At least 2 users can chat in real-time
2. ✅ Messages persist in database
3. ✅ Passwords are securely hashed
4. ✅ User list shows online users
5. ✅ Join/leave notifications work
6. ✅ Message history loads on login
7. ✅ No crashes under normal use
8. ✅ Clean, usable interface

## 🎯 Demo Script for Presentation

**1. Introduction (1 min)**
"I've built a real-time chat application using Java sockets, MySQL, and Next.js"

**2. Architecture Explanation (2 min)**
- Show architecture diagram
- Explain: Browser → WebSocket → Java Server → MySQL
- Highlight multi-threading

**3. Live Demo (5 min)**

**Step 1:** Open app, register new user
```
"Here I'm registering a new user. The password is hashed with BCrypt."
```

**Step 2:** Open phpMyAdmin, show database
```
"As you can see, the user is stored in the database with hashed password."
```

**Step 3:** Login, show chat interface
```
"The interface shows online users and loads message history from database."
```

**Step 4:** Open second browser, register another user
```
"Now I'm connecting a second user to demonstrate real-time messaging."
```

**Step 5:** Send messages between users
```
"When User 1 sends a message, it's immediately broadcast to User 2 through the Java server using sockets."
```

**Step 6:** Show server logs
```
"Here in the server logs, you can see the TCP connections and message broadcasting."
```

**Step 7:** Check database again
```
"All messages are persisted in the MySQL database for history."
```

**4. Technical Highlights (2 min)**
- Multi-threaded Java server
- BCrypt password security
- WebSocket real-time communication
- JDBC database integration
- Clean architecture and separation of concerns

**5. Q&A**

## 🎓 Learning Outcomes Demonstrated

After testing, you've demonstrated understanding of:

✅ **Network Programming**
- TCP socket programming
- Client-server architecture
- Message protocols

✅ **Multi-threading**
- Thread-per-client model
- Concurrent connection handling
- Thread safety

✅ **Database**
- JDBC connectivity
- SQL queries
- Data persistence

✅ **Security**
- Password hashing
- Input validation
- SQL injection prevention

✅ **Full-Stack Development**
- Backend API design
- Frontend integration
- Real-time communication

---

**Happy Testing! 🚀**

All tests passing means your chat application is **production-ready for educational purposes**!
