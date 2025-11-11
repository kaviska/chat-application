# Start all servers in separate windows

Write-Host "╔════════════════════════════════════════╗" -ForegroundColor Cyan
Write-Host "║   Starting Chat Application...         ║" -ForegroundColor Cyan
Write-Host "╚════════════════════════════════════════╝" -ForegroundColor Cyan
Write-Host ""

# Get the current directory
$currentDir = Get-Location

# Start WebSocket Bridge
Write-Host "🚀 Starting WebSocket Bridge..." -ForegroundColor Yellow
Start-Process powershell -ArgumentList "-NoExit", "-Command", "cd '$currentDir\backend'; npm start"

# Wait a moment for bridge to start
Start-Sleep -Seconds 2

# Start Java Server
Write-Host "🚀 Starting Java Server..." -ForegroundColor Yellow
Start-Process powershell -ArgumentList "-NoExit", "-Command", "cd '$currentDir\backend'; java -jar target/chat-server-1.0-SNAPSHOT-jar-with-dependencies.jar"

# Wait for server to initialize
Start-Sleep -Seconds 3

# Start Frontend
Write-Host "🚀 Starting Frontend..." -ForegroundColor Yellow
Start-Process powershell -ArgumentList "-NoExit", "-Command", "cd '$currentDir\frontend'; npm run dev"

Write-Host ""
Write-Host "╔════════════════════════════════════════╗" -ForegroundColor Green
Write-Host "║   ✅ All servers started!              ║" -ForegroundColor Green
Write-Host "╚════════════════════════════════════════╝" -ForegroundColor Green
Write-Host ""
Write-Host "📡 Services running:" -ForegroundColor Cyan
Write-Host "  - WebSocket Bridge: ws://localhost:8082" -ForegroundColor White
Write-Host "  - Java Server: localhost:8081" -ForegroundColor White
Write-Host "  - Frontend: http://localhost:3000" -ForegroundColor White
Write-Host ""
Write-Host "🌐 Open http://localhost:3000 in your browser" -ForegroundColor Green
Write-Host ""
Write-Host "Press Ctrl+C in each window to stop the servers" -ForegroundColor Yellow
