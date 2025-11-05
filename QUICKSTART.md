# Quick Start Guide

## 🚀 Running the Application

Follow these steps in order:

### Step 1: Start MySQL (XAMPP)
1. Open XAMPP Control Panel
2. Click "Start" for MySQL
3. Click "Admin" for MySQL to open phpMyAdmin
4. Import the database:
   - Go to SQL tab
   - Copy and paste contents from `backend/src/main/resources/schema.sql`
   - Click "Go"

### Step 2: Configure Database
1. Open `backend/src/main/resources/db_config.properties`
2. Update password if needed:
   ```properties
   db.password=YOUR_MYSQL_PASSWORD
   ```

### Step 3: Build Java Backend
```powershell
cd backend
mvn clean package
```

### Step 4: Start WebSocket Bridge (Terminal 1)
```powershell
cd backend
npm install
npm start
```
You should see:
```
╔════════════════════════════════════════╗
║   WebSocket Bridge Server Started      ║
╚════════════════════════════════════════╝
📡 WebSocket Server: ws://localhost:8082
```

### Step 5: Start Java Server (Terminal 2)
```powershell
cd backend
java -jar target/chat-server-1.0-SNAPSHOT-jar-with-dependencies.jar
```
You should see:
```
╔════════════════════════════════════════╗
║   🚀 Chat Server Started Successfully  ║
╚════════════════════════════════════════╝
📡 Server listening on port: 8081
💾 Database: Connected
```

### Step 6: Start Frontend (Terminal 3)
```powershell
cd frontend
npm install
npm run dev
```
You should see:
```
  ▲ Next.js 14.x.x
  - Local:        http://localhost:3000
```

### Step 7: Test the Application

1. Open http://localhost:3000 in your browser
2. Click "Sign Up" and create an account
3. Login with your credentials
4. Start chatting!

**To test with multiple users:**
- Open the app in a different browser or incognito window
- Register another account
- Both users should see each other online and can chat in real-time

## 🎯 Expected Behavior

### When Starting
- ✅ All three servers should start without errors
- ✅ Database connection should be successful
- ✅ WebSocket bridge should connect to Java server

### When Using
- ✅ Registration creates user in database
- ✅ Login validates credentials
- ✅ Messages appear instantly for all users
- ✅ User list updates when users join/leave
- ✅ Message history loads on login

## ❌ Common Issues & Solutions

### "Database connection failed"
**Solution:**
- Make sure MySQL is running in XAMPP
- Check database name is `chat_app`
- Verify credentials in `db_config.properties`

### "Port 8081 already in use"
**Solution:**
```powershell
# Find process using port 8081
netstat -ano | findstr :8081

# Kill the process (replace PID with actual number)
taskkill /PID <PID> /F
```

### "WebSocket connection failed"
**Solution:**
- Ensure WebSocket bridge is running on port 8082
- Ensure Java server is running on port 8081
- Check both are started in correct order

### Maven build fails
**Solution:**
```powershell
# Clean and rebuild
mvn clean install -U

# Or skip tests
mvn clean package -DskipTests
```

### Frontend build errors
**Solution:**
```powershell
# Delete node_modules and reinstall
rm -rf node_modules
npm install
```

## 📊 Testing Checklist

- [ ] MySQL server is running
- [ ] Database `chat_app` exists with tables
- [ ] WebSocket bridge running on port 8082
- [ ] Java server running on port 8081
- [ ] Frontend running on port 3000
- [ ] Can register new user
- [ ] Can login successfully
- [ ] Can send and receive messages
- [ ] Can see online users
- [ ] Messages persist in database
- [ ] Multiple users can chat simultaneously

## 🔍 Monitoring

### Check Java Server Logs
- Look for user connections: `👤 Client added: email@example.com`
- Monitor messages: `📨 Received: {"type":"message"...}`
- Watch for errors: `❌ Error messages`

### Check WebSocket Bridge
- Monitor connections: `🔌 New WebSocket client connected`
- Watch message flow: `📤 WS → TCP` and `📥 TCP → WS`

### Check Database
```sql
-- Check registered users
SELECT * FROM users;

-- Check messages
SELECT * FROM messages ORDER BY timestamp DESC LIMIT 10;

-- Check online users
SELECT * FROM users WHERE status = 'online';
```

## 🎉 Success Indicators

If everything is working:
1. ✅ Three terminals running without errors
2. ✅ Browser shows chat interface
3. ✅ Can send messages that appear instantly
4. ✅ Database shows new users and messages
5. ✅ Multiple browser windows can chat together

Enjoy your chat application! 🚀
