# 🔧 Troubleshooting Guide

## ✅ Current Status

**Good news!** Your application is already running successfully:
- ✅ Java Server: Running on port 8081
- ✅ WebSocket Bridge: Running on port 8082  
- ✅ Database: Connected
- ✅ First user registered successfully

## Common Issues & Solutions

### 1. "ClassNotFoundException: com.chatapp.server.MainServer"

**Issue:** JAR file doesn't contain compiled classes

**Solution:**
```powershell
cd backend
mvn clean package
java -jar target/chat-server-1.0-SNAPSHOT-jar-with-dependencies.jar
```

**Status:** ✅ **FIXED** - Your server is now running!

---

### 2. "Port 8081 already in use"

**Symptoms:**
```
Error: Address already in use
```

**Solution:**
```powershell
# Find process using port 8081
netstat -ano | findstr :8081

# Kill the process (replace <PID> with actual number)
taskkill /PID <PID> /F
```

**Alternative:** Change port in MainServer.java:
```java
private static final int PORT = 8082; // Use different port
```

---

### 3. "Database connection failed"

**Symptoms:**
```
❌ Failed to connect to database
```

**Checklist:**
- [ ] Is MySQL running in XAMPP?
- [ ] Does database `chat_app` exist?
- [ ] Are credentials correct in `db_config.properties`?
- [ ] Is port 3306 available?

**Solution:**

**Step 1:** Start MySQL in XAMPP
```
Open XAMPP Control Panel → Start MySQL
```

**Step 2:** Create database
```sql
CREATE DATABASE chat_app;
USE chat_app;
-- Then run schema.sql content
```

**Step 3:** Check credentials
```properties
# In db_config.properties
db.username=root
db.password=       # Leave blank if no password
```

**Step 4:** Test connection
```powershell
mysql -u root -p
# Then: USE chat_app;
```

---

### 4. "WebSocket connection refused"

**Symptoms:**
- Frontend shows "Disconnected"
- Browser console: `WebSocket connection failed`

**Checklist:**
- [ ] Is WebSocket bridge running? (port 8082)
- [ ] Is Java server running? (port 8081)
- [ ] Are they started in correct order?

**Solution:**

**Start in this order:**
1. Java Server (port 8081)
2. WebSocket Bridge (port 8082)
3. Frontend (port 3000)

```powershell
# Terminal 1
cd backend
java -jar target/chat-server-1.0-SNAPSHOT-jar-with-dependencies.jar

# Terminal 2 (wait 2 seconds)
cd backend
npm start

# Terminal 3 (wait 2 seconds)
cd frontend
npm run dev
```

---

### 5. "npm install" fails

**Symptoms:**
```
Error: EACCES permission denied
Error: Cannot find module
```

**Solution:**

**Option 1:** Clear cache
```powershell
npm cache clean --force
rm -rf node_modules
npm install
```

**Option 2:** Use admin PowerShell
```powershell
# Run as Administrator
npm install --legacy-peer-deps
```

**Option 3:** Update npm
```powershell
npm install -g npm@latest
```

---

### 6. "Maven build fails"

**Symptoms:**
```
[ERROR] Failed to execute goal
[ERROR] Compilation failure
```

**Solution:**

**Check Java version:**
```powershell
java -version
# Should be 17 or higher
```

**Clean and rebuild:**
```powershell
cd backend
mvn clean install -U
```

**Skip tests:**
```powershell
mvn clean package -DskipTests
```

**Update Maven:**
```powershell
mvn -version
# Update from https://maven.apache.org/download.cgi
```

---

### 7. Frontend build errors

**Symptoms:**
```
Module not found
Type error
```

**Solution:**

**Reinstall dependencies:**
```powershell
cd frontend
rm -rf node_modules
rm package-lock.json
npm install
```

**Check Node version:**
```powershell
node -version
# Should be 18+ for Next.js 14
```

**Clear Next.js cache:**
```powershell
rm -rf .next
npm run dev
```

---

### 8. "Cannot read property 'email' of null"

**Symptoms:**
- Login works but chat page crashes
- Console error about null user

**Solution:**

**Clear browser storage:**
```javascript
// In browser console
localStorage.clear();
sessionStorage.clear();
// Then refresh page
```

**Check user in localStorage:**
```javascript
// In browser console
console.log(localStorage.getItem('user'));
```

---

### 9. Messages not appearing

**Symptoms:**
- User can send message
- Message doesn't show for other users

**Debug Steps:**

**Check server logs:**
```
Look for: 📨 Received: {"type":"message"...}
Should see: Broadcasting to all clients
```

**Check WebSocket bridge:**
```
Look for: 📤 WS → TCP
Look for: 📥 TCP → WS
```

**Check browser console:**
```javascript
// Should see WebSocket messages
WebSocket is connected
Received: {...}
```

**Common causes:**
- WebSocket not connected
- Message type incorrect
- Server not broadcasting
- User not authenticated

---

### 10. Database errors

**Symptoms:**
```
SQLException
Foreign key constraint fails
Duplicate entry
```

**Solution:**

**Check table structure:**
```sql
DESCRIBE users;
DESCRIBE messages;
```

**Reset database:**
```sql
DROP DATABASE chat_app;
CREATE DATABASE chat_app;
-- Run schema.sql again
```

**Check foreign keys:**
```sql
SHOW CREATE TABLE messages;
-- Should see FOREIGN KEY constraints
```

---

### 11. Multiple instances running

**Symptoms:**
- Port conflicts
- Duplicate messages
- Strange behavior

**Solution:**

**Check running processes:**
```powershell
# Check Java processes
Get-Process java

# Check Node processes  
Get-Process node

# Kill all Java
Get-Process java | Stop-Process -Force

# Kill all Node
Get-Process node | Stop-Process -Force
```

**Check ports:**
```powershell
netstat -ano | findstr :8081
netstat -ano | findstr :8082
netstat -ano | findstr :3000
```

---

### 12. Passwords not working

**Symptoms:**
- Can register but can't login
- "Invalid credentials" error

**Debug:**

**Check password in database:**
```sql
SELECT email, password FROM users;
-- Password should be a hash like: $2a$10$...
```

**If plain text password:**
```
Problem: BCrypt not working
Solution: Check BCrypt dependency in pom.xml
```

**Test BCrypt:**
```java
String hash = BCrypt.hashpw("test123", BCrypt.gensalt());
System.out.println(hash);
boolean match = BCrypt.checkpw("test123", hash);
System.out.println(match); // Should be true
```

---

### 13. Performance issues

**Symptoms:**
- Slow message delivery
- High CPU usage
- Memory leaks

**Solutions:**

**Monitor resources:**
```powershell
# Check Java memory
Get-Process java | Select-Object WS, CPU

# Check connections
netstat -an | findstr :8081 | Measure-Object
```

**Optimize:**

1. **Increase heap size:**
```powershell
java -Xmx512m -jar target/chat-server-1.0-SNAPSHOT-jar-with-dependencies.jar
```

2. **Add connection limit:**
```java
// In MainServer.java
if (connectedClients.size() >= MAX_CLIENTS) {
    socket.close();
}
```

3. **Add database connection pooling**

---

## 🔍 Diagnostic Commands

### Check All Services
```powershell
# MySQL
Get-Process mysqld

# Java Server
Get-Process java

# Node Bridge
Get-Process node

# Check ports
netstat -ano | findstr "8081 8082 3000"
```

### Test Individual Components

**Test Database:**
```powershell
mysql -u root -p
USE chat_app;
SELECT COUNT(*) FROM users;
SELECT COUNT(*) FROM messages;
```

**Test WebSocket:**
```powershell
# In Node.js terminal, you should see:
✅ Connected to Java server
```

**Test Frontend:**
```
Open: http://localhost:3000
Browser console should be error-free
```

---

## 📊 Health Check Indicators

### ✅ Everything is Working If:

1. **Server Logs Show:**
   ```
   ✅ Database connected successfully
   🚀 Chat Server Started Successfully
   ⏳ Waiting for clients...
   ```

2. **Bridge Logs Show:**
   ```
   WebSocket Bridge Server Started
   🔌 New WebSocket client connected
   ✅ Connected to Java server
   ```

3. **Frontend Shows:**
   ```
   ● Connected (green dot)
   Online users list populated
   Messages sending/receiving
   ```

4. **Database Contains:**
   ```sql
   SELECT * FROM users;    -- Has users
   SELECT * FROM messages; -- Has messages
   ```

---

## 🆘 Emergency Reset

If nothing works, start fresh:

```powershell
# 1. Stop everything
Get-Process java | Stop-Process -Force
Get-Process node | Stop-Process -Force

# 2. Reset database
mysql -u root
DROP DATABASE chat_app;
CREATE DATABASE chat_app;
# Run schema.sql

# 3. Clean build
cd backend
mvn clean package

cd ../frontend  
rm -rf node_modules .next
npm install

# 4. Restart in order
# Terminal 1: Java server
cd backend
java -jar target/chat-server-1.0-SNAPSHOT-jar-with-dependencies.jar

# Terminal 2: Bridge
cd backend
npm start

# Terminal 3: Frontend
cd frontend
npm run dev
```

---

## 📞 Getting Help

### When Asking for Help, Provide:

1. **Error message** (full stack trace)
2. **Server logs** (what you see in terminals)
3. **Browser console** (F12 → Console tab)
4. **What you tried** (steps to reproduce)
5. **Environment** (Java version, Node version, OS)

### Useful Debug Info:

```powershell
# System info
java -version
node -version
npm -version
mvn -version

# MySQL status
mysql --version
Get-Process mysqld

# Port usage
netstat -ano | findstr "8081 8082 3000"
```

---

## ✅ Your Current Status

Based on the logs, everything is working:

- ✅ Java Server: Running successfully
- ✅ WebSocket Bridge: Running successfully  
- ✅ Database: Connected
- ✅ User Registration: Working (user created)
- ✅ WebSocket Communication: Working (message forwarded)

**You're all set! The application is ready to use! 🎉**

If you encounter any issues in the future, refer to this guide.

---

**Pro Tip:** Keep this guide bookmarked for quick reference during development!
