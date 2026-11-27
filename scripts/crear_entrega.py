#!/usr/bin/env python3
"""Crea inventario, manifiesto SHA-256 y ZIP completo, sin cachés ni .git."""
from pathlib import Path
import hashlib
import zipfile

root = Path(__file__).resolve().parent.parent
output = root / "entrega"
output.mkdir(exist_ok=True)
archive = output / "proyecto-completo.zip"
archive_sha = output / "proyecto-completo.zip.sha256"
manifest = output / "SHA256SUMS.txt"
assert (output / "diagnosticador-canino.jar").is_file(), "Falta el JAR distribuido"

def sources():
    files = [root / name for name in ("README.md", "pom.xml", "dataset.arff", "LICENSE", ".gitignore")]
    for folder in ("src", "docs", "scripts", "modelos", "resultados", ".github", "entrega"):
        for file in (root / folder).rglob("*"):
            if file.is_file() and file not in (archive, archive_sha, manifest) and "__pycache__" not in file.parts:
                files.append(file)
    return sorted(set(files))

def role(path):
    name = path.name
    if name == "dataset.arff": return "1 · Dataset sintético completo (200 filas)."
    if name == "DataGenerator.java": return "2 · Generador determinista que produjo el ARFF."
    if name in ("Experiment.java", "Charts.java"): return "3 · Experimento, métricas y gráficas."
    if name == "INFORME.md": return "5 · Informe completo redactado, con tablas y gráficas."
    if name == "CALENDARIO.md": return "7 · Calendario de fases, tareas e hitos."
    if name == "README.md": return "6 · Instrucciones de ejecución y navegación de los siete entregables."
    if name == "pom.xml": return "4 · Dependencias y construcción Maven para Java 11+."
    if name.endswith(".java"): return "4 · Código completo Java / pruebas automatizadas."
    if "resources" in path.parts: return "Avisos de licencia incluidos en el JAR."
    if path.suffix == ".model": return "4 · Modelo Weka serializado con cabecera y metadatos."
    if "modelos" in path.parts: return "4 · Representación textual del modelo entrenado."
    if "resultados" in path.parts: return "3 · Resultado ejecutado, gráfico o evidencia de verificación."
    if "scripts" in path.parts: return "6 · Utilidad reproducible de auditoría/documentación/empaquetado."
    if ".github" in path.parts: return "6 · CI Maven, pruebas e integración en Java 11/17/21."
    if name.endswith(".jar"): return "4 · Aplicación ejecutable con Weka 3.8.5."
    if name.endswith(".zip"): return "Paquete completo descargable; no se incluye dentro de sí mismo."
    if name.endswith(".sha256") or name == "SHA256SUMS.txt": return "Manifiesto de integridad SHA-256."
    return "Documentación, contrato de datos, plantilla o licencia."

inventory = root / "docs/ARCHIVOS.md"
all_paths = sorted(set(sources() + [inventory, manifest, archive, archive_sha]))
text = "# 6. Inventario de archivos del proyecto\n\n"
text += "Se enumeran los archivos entregados, no las carpetas temporales de compilación. `target/`, `.git/`, JDK, cachés y dependencias de construcción no forman parte del ZIP. Los archivos ZIP/SHA externos se enumeran aquí aunque no se anidan dentro del propio paquete.\n\n"
text += "|Archivo|Función / entregable|\n|---|---|\n"
for file in all_paths:
    text += f"|`{file.relative_to(root).as_posix()}`|{role(file)}|\n"
text += "\nEl paquete contiene las fuentes completas, datos, modelos finales, resultados CSV/SVG, informe, instrucciones y el JAR. La aplicación no necesita Python ni Maven para ejecutarse desde el JAR. Para reconstruir desde código se recomienda Maven/JDK, como se explica en README.\n"
inventory.write_text(text, encoding="utf-8")
files = sources()
manifest.write_text("".join(f"{hashlib.sha256(f.read_bytes()).hexdigest()}  {f.relative_to(root).as_posix()}\n" for f in files), encoding="utf-8")
with zipfile.ZipFile(archive, "w", compression=zipfile.ZIP_DEFLATED, compresslevel=9) as z:
    for file in sorted(files + [manifest]):
        info = zipfile.ZipInfo(file.relative_to(root).as_posix(), (2026, 11, 27, 0, 0, 0))
        info.compress_type = zipfile.ZIP_DEFLATED
        info.external_attr = 0o100644 << 16
        z.writestr(info, file.read_bytes())
archive_sha.write_text(f"{hashlib.sha256(archive.read_bytes()).hexdigest()}  proyecto-completo.zip\n", encoding="utf-8")
print(f"Inventario: {inventory.relative_to(root)}")
print(f"Paquete: {archive.relative_to(root)} ({archive.stat().st_size:,} bytes, {len(files) + 1} archivos)")
