"""Ejecuta Postman contra los tres servicios y su MySQL/MariaDB real, sin crear bases auxiliares."""
import json
import os
from pathlib import Path
import shutil
import subprocess
import sys
import urllib.request
from datetime import datetime, timezone

ROOT = Path(__file__).resolve().parents[1]


def main():
    if hasattr(sys.stdout, "reconfigure"):
        sys.stdout.reconfigure(encoding="utf-8")
    npx = shutil.which("npx.cmd" if os.name == "nt" else "npx")
    if not npx:
        raise RuntimeError("Se requiere Node.js y npx en PATH")
    services = (
        ("usuarioBaseUrl", os.environ.get("USUARIO_SERVICE_URL", "http://localhost:8081"), "usuarios"),
        ("tecnicoBaseUrl", os.environ.get("TECNICO_SERVICE_URL", "http://localhost:8082"), "tecnicos"),
        ("ticketBaseUrl", os.environ.get("TICKET_SERVICE_URL", "http://localhost:8083"), "tickets"),
    )
    for _, url, route in services:
        with urllib.request.urlopen(url.rstrip("/") + "/api/" + route, timeout=10) as response:
            if response.status != 200:
                raise RuntimeError("No está disponible " + route)
    output = ROOT / "evidencias" / ("ejecucion-real-" + datetime.now().strftime("%Y%m%d-%H%M%S-%f"))
    output.mkdir(parents=True)
    report = output / "newman.json"
    command = [npx, "--yes", "newman@6.2.1", "run",
               str(ROOT / "postman/SoporteTecnico.postman_collection.json"),
               "--reporters", "cli,json", "--reporter-json-export", str(report),
               "--timeout-request", "10000"]
    for variable, url, _ in services:
        command.extend(("--env-var", f"{variable}={url}"))
    result = subprocess.run(command, cwd=ROOT, check=False)
    if report.exists():
        data = json.loads(report.read_text(encoding="utf-8"))
        summary = {"fechaUtc": datetime.now(timezone.utc).isoformat(),
                   "baseDatos": "MariaDB/MySQL real configurado en los servicios",
                   "baseAuxiliar": False, "codigoSalida": result.returncode,
                   "estadisticas": data["run"]["stats"], "fallos": data["run"]["failures"]}
        (output / "resumen.json").write_text(json.dumps(summary, ensure_ascii=False, indent=2), encoding="utf-8")
    if result.returncode:
        raise RuntimeError(f"La colección presentó errores. Revisar {output}")
    print(f"Pruebas correctas contra la base real. Reporte: {output}")


if __name__ == "__main__":
    try:
        main()
    except (RuntimeError, subprocess.SubprocessError, OSError) as error:
        print(f"ERROR: {error}", file=sys.stderr)
        sys.exit(1)
