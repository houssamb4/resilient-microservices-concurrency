param(
    [int]$BookId = 1,
    [int]$Requests = 50
)

$ErrorActionPreference = "Stop"

# On répartit sur 3 instances
$Ports = @(8081, 8083, 8084)

Write-Host "== Load test ==" -ForegroundColor Cyan
Write-Host "BookId=$BookId Requests=$Requests"
Write-Host "Ports=$($Ports -join ', ')"
Write-Host ""

$tmpdir = Join-Path $env:TEMP "loadtest_$(Get-Date -Format 'yyyyMMdd_HHmmss')"
New-Item -ItemType Directory -Path $tmpdir -Force | Out-Null

$successFile = Join-Path $tmpdir "success.txt"
$conflictFile = Join-Path $tmpdir "conflict.txt"
$otherFile = Join-Path $tmpdir "other.txt"

New-Item -ItemType File -Path $successFile -Force | Out-Null
New-Item -ItemType File -Path $conflictFile -Force | Out-Null
New-Item -ItemType File -Path $otherFile -Force | Out-Null

$jobs = @()

for ($i = 1; $i -le $Requests; $i++) {
    $port = $Ports[($i - 1) % 3]
    $url = "http://localhost:$port/api/books/$BookId/borrow"
    
    $job = Start-Job -ScriptBlock {
        param($url, $port, $tmpdir, $successFile, $conflictFile, $otherFile, $index)
        
        $bodyFile = Join-Path $tmpdir "body_$index.json"
        
        try {
            $response = Invoke-WebRequest -Uri $url -Method POST -ErrorAction SilentlyContinue
            $status = $response.StatusCode
            $body = $response.Content
            
            if ($status -eq 200) {
                "$port $status $body" | Out-File -FilePath $successFile -Append -Encoding utf8
            } else {
                "$port $status $body" | Out-File -FilePath $otherFile -Append -Encoding utf8
            }
        } catch {
            $status = $_.Exception.Response.StatusCode.value__
            $body = ""
            
            try {
                $stream = $_.Exception.Response.GetResponseStream()
                $reader = New-Object System.IO.StreamReader($stream)
                $body = $reader.ReadToEnd()
            } catch {}
            
            if ($status -eq 409) {
                "$port $status $body" | Out-File -FilePath $conflictFile -Append -Encoding utf8
            } else {
                "$port $status $body" | Out-File -FilePath $otherFile -Append -Encoding utf8
            }
        }
    } -ArgumentList $url, $port, $tmpdir, $successFile, $conflictFile, $otherFile, $i
    
    $jobs += $job
}

# Attendre que tous les jobs se terminent
Write-Host "Executing $Requests requests..." -ForegroundColor Yellow
$jobs | Wait-Job | Out-Null
$jobs | Remove-Job

Write-Host ""
Write-Host "== Résultats ==" -ForegroundColor Cyan

$successCount = 0
$conflictCount = 0
$otherCount = 0

if (Test-Path $successFile) {
    $successCount = (Get-Content $successFile -ErrorAction SilentlyContinue).Count
    if ($null -eq $successCount) { $successCount = 0 }
}

if (Test-Path $conflictFile) {
    $conflictCount = (Get-Content $conflictFile -ErrorAction SilentlyContinue).Count
    if ($null -eq $conflictCount) { $conflictCount = 0 }
}

if (Test-Path $otherFile) {
    $otherCount = (Get-Content $otherFile -ErrorAction SilentlyContinue).Count
    if ($null -eq $otherCount) { $otherCount = 0 }
}

Write-Host "Success (200):  $successCount" -ForegroundColor Green
Write-Host "Conflict (409): $conflictCount" -ForegroundColor Yellow
Write-Host "Other:          $otherCount" -ForegroundColor Red
Write-Host ""
Write-Host "Fichiers détails: $tmpdir" -ForegroundColor Cyan
Write-Host " - success.txt  : appels OK"
Write-Host " - conflict.txt : stock épuisé (normal)"
Write-Host " - other.txt    : erreurs à diagnostiquer"
