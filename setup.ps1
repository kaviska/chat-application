# Build and run the entire chat application

Write-Host "╔════════════════════════════════════════╗" -ForegroundColor Cyan
Write-Host "║   Chat Application Setup Script       ║" -ForegroundColor Cyan
Write-Host "╚════════════════════════════════════════╝" -ForegroundColor Cyan
Write-Host ""

# Check if MySQL is running
Write-Host "🔍 Checking MySQL..." -ForegroundColor Yellow
$mysqlProcess = Get-Process mysqld -ErrorAction SilentlyContinue
if (!$mysqlProcess) {
    Write-Host "❌ MySQL is not running!" -ForegroundColor Red
    Write-Host "Please start MySQL from XAMPP Control Panel" -ForegroundColor Yellow
    exit 1
}
Write-Host "✅ MySQL is running" -ForegroundColor Green

# Build backend
Write-Host ""
Write-Host "🔨 Building Java backend..." -ForegroundColor Yellow
Set-Location backend
mvn clean package
if ($LASTEXITCODE -ne 0) {
    Write-Host "❌ Backend build failed!" -ForegroundColor Red
    exit 1
}
Write-Host "✅ Backend built successfully" -ForegroundColor Green

# Install bridge dependencies
Write-Host ""
Write-Host "📦 Installing WebSocket bridge dependencies..." -ForegroundColor Yellow
npm install
if ($LASTEXITCODE -ne 0) {
    Write-Host "❌ Bridge dependencies installation failed!" -ForegroundColor Red
    exit 1
}
Write-Host "✅ Bridge dependencies installed" -ForegroundColor Green

# Install frontend dependencies
Write-Host ""
Write-Host "📦 Installing frontend dependencies..." -ForegroundColor Yellow
Set-Location ../frontend
npm install
if ($LASTEXITCODE -ne 0) {
    Write-Host "❌ Frontend dependencies installation failed!" -ForegroundColor Red
    exit 1
}
Write-Host "✅ Frontend dependencies installed" -ForegroundColor Green

Write-Host ""
Write-Host "╔════════════════════════════════════════╗" -ForegroundColor Green
Write-Host "║   ✅ Setup Complete!                   ║" -ForegroundColor Green
Write-Host "╚════════════════════════════════════════╝" -ForegroundColor Green
Write-Host ""
Write-Host "📝 Next steps:" -ForegroundColor Cyan
Write-Host "1. Make sure database 'chat_app' exists and schema is imported" -ForegroundColor White
Write-Host "2. Run: .\start-all.ps1" -ForegroundColor White
Write-Host ""
