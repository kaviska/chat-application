# Start all servers in separate windows

Write-Host "╔════════════════════════════════════════╗" -ForegroundColor Cyan
Write-Host "║   Starting Chat Application...         ║" -ForegroundColor Cyan
Write-Host "╚════════════════════════════════════════╝" -ForegroundColor Cyan
Write-Host ""

# Get the current directory
$currentDir = Get-Location

# Start Java Backend (includes both TCP and WebSocket servers)
Write-Host "🚀 Starting Java Backend (TCP + WebSocket)..." -ForegroundColor Yellow
Start-Process powershell -ArgumentList "-NoExit", "-Command", "cd '$currentDir\backend'; .\start-java-backend.ps1"

# Wait for servers to initialize
Start-Sleep -Seconds 5

# Start Frontend
Write-Host "🚀 Starting Frontend..." -ForegroundColor Yellow
Start-Process powershell -ArgumentList "-NoExit", "-Command", "cd '$currentDir\frontend'; npm run dev"

Write-Host ""
Write-Host "╔════════════════════════════════════════╗" -ForegroundColor Green
Write-Host "║   ✅ All servers started!              ║" -ForegroundColor Green
Write-Host "╚════════════════════════════════════════╝" -ForegroundColor Green
Write-Host ""
Write-Host "📡 Services running:" -ForegroundColor Cyan
Write-Host "  - Java TCP Server: localhost:8081" -ForegroundColor White
Write-Host "  - Java WebSocket Server: ws://localhost:8082" -ForegroundColor White
Write-Host "  - Frontend: http://localhost:3000" -ForegroundColor White
Write-Host ""
Write-Host "🌐 Open http://localhost:3000 in your browser" -ForegroundColor Green
Write-Host ""
Write-Host "Press Ctrl+C in each window to stop the servers" -ForegroundColor Yellow
