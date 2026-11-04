param([ValidateSet('dev', 'prod')][string]$Perfil = 'dev')
$ErrorActionPreference = 'Stop'
$root = Split-Path $PSScriptRoot -Parent
$logs = Join-Path $root 'evidencias\servicios'
$state = Join-Path $logs 'procesos.json'
$java = (Get-Command java.exe -ErrorAction Stop).Source
# Resolver el JDK real: el acceso directo de Oracle puede lanzar otro proceso.
# Así el PID guardado corresponde a la aplicación, no a un intermediario.
$probeInfo = [System.Diagnostics.ProcessStartInfo]::new($java, '-XshowSettings:properties -version')
$probeInfo.UseShellExecute = $false
$probeInfo.RedirectStandardError = $true
$probeInfo.RedirectStandardOutput = $true
$probeInfo.CreateNoWindow = $true
$probe = [System.Diagnostics.Process]::Start($probeInfo)
$settings = $probe.StandardError.ReadToEnd()
$probe.WaitForExit()
$javaHome = [regex]::Match($settings, '(?m)^\s*java\.home\s*=\s*(.+)$').Groups[1].Value.Trim()
$probe.Dispose()
if (-not $javaHome) { throw 'No se pudo localizar el JDK real.' }
$java = Join-Path $javaHome 'bin\java.exe'
$services = @(
    @{ Name = 'usuario-service'; Port = 8081 },
    @{ Name = 'tecnico-service'; Port = 8082 },
    @{ Name = 'ticket-service'; Port = 8083 }
)
# Comprobar todos los requisitos antes de arrancar una aplicación.
foreach ($service in $services) {
    $jar = Join-Path $root "$($service.Name)\target\$($service.Name)-0.0.1-SNAPSHOT.jar"
    if (-not (Test-Path -LiteralPath $jar)) { throw "Falta $jar. Ejecuta mvn clean verify." }
    if (Get-NetTCPConnection -State Listen -LocalPort $service.Port -ErrorAction SilentlyContinue) {
        throw "El puerto $($service.Port) ya está ocupado. No se detendrá ese proceso."
    }
    $service.Jar = $jar
}
New-Item -ItemType Directory -Path $logs -Force | Out-Null
$started = @()
try {
    foreach ($service in $services) {
        $stamp = Get-Date -Format 'yyyyMMdd-HHmmss-fff'
        $output = Join-Path $logs "$($service.Name)-$stamp.out.log"
        $errorLog = Join-Path $logs "$($service.Name)-$stamp.err.log"
        $process = Start-Process -FilePath $java -WorkingDirectory $root -PassThru -WindowStyle Hidden `
            -ArgumentList @('-jar', "`"$($service.Jar)`"", "--spring.profiles.active=$Perfil", "--server.port=$($service.Port)") `
            -RedirectStandardOutput $output -RedirectStandardError $errorLog
        $started += [pscustomobject]@{ Servicio = $service.Name; Puerto = $service.Port; PID = $process.Id; Jar = $service.Jar; Perfil = $Perfil; Log = $output; ErrorLog = $errorLog }
    }
    $started | ConvertTo-Json | Set-Content -LiteralPath $state -Encoding UTF8
    $started | Select-Object Servicio, Puerto, PID, Perfil
    Write-Host 'Procesos iniciados. Espera el mensaje Started en los logs de evidencias/servicios.'
} catch {
    foreach ($entry in $started) { Stop-Process -Id $entry.PID -ErrorAction SilentlyContinue }
    throw
}
