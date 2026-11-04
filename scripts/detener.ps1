$ErrorActionPreference = 'Stop'
$root = Split-Path $PSScriptRoot -Parent
$state = Join-Path $root 'evidencias\servicios\procesos.json'
if (-not (Test-Path -LiteralPath $state)) { throw 'No hay un registro de procesos iniciados por iniciar.ps1.' }
foreach ($entry in (Get-Content -Raw -LiteralPath $state | ConvertFrom-Json)) {
    $process = Get-CimInstance Win32_Process -Filter "ProcessId = $($entry.PID)" -ErrorAction SilentlyContinue
    # No detener un PID reutilizado por otro programa ni una aplicación ajena.
    if ($process -and $process.Name -eq 'java.exe' -and $process.CommandLine.Contains($entry.Jar)) {
        Stop-Process -Id $entry.PID
        Write-Host "Detenido: $($entry.Servicio)"
    } else {
        Write-Host "Sin proceso coincidente: $($entry.Servicio)"
    }
}
