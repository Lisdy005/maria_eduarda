#!/usr/bin/env python3
"""Empaquetado alternativo sin Maven (requiere clases ya compiladas y dependencias).
Uso: python3 scripts/empaquetar_jar.py CLASES SALIDA.jar dependencia.jar ...
El flujo preferido es mvn clean verify (Shade resuelve dependencias transitivas).
"""
import pathlib
import sys
import zipfile

if len(sys.argv) < 4:
    sys.exit(__doc__)
classes, dest = pathlib.Path(sys.argv[1]), pathlib.Path(sys.argv[2])
assert (classes / "edu/canino/Main.class").is_file()
dest.parent.mkdir(parents=True, exist_ok=True)
entries = {}
services = {}
for jar in map(pathlib.Path, sys.argv[3:]):
    with zipfile.ZipFile(jar) as z:
        for name in z.namelist():
            upper = name.upper()
            if name.endswith("/") or upper == "META-INF/MANIFEST.MF" or upper.endswith((".SF", ".DSA", ".RSA")) or name.endswith("module-info.class"):
                continue
            data = z.read(name)
            if name.startswith("META-INF/services/"):
                services.setdefault(name, []).append(data.decode("utf-8"))
            else:
                entries.setdefault(name, data)
for file in classes.rglob("*"):
    if file.is_file():
        data = file.read_bytes()
        if file.suffix == ".class":
            assert int.from_bytes(data[6:8], "big") <= 55, "Las clases de la app deben ser Java 11"
        entries[file.relative_to(classes).as_posix()] = data
for name, content in services.items():
    entries[name] = ("\n".join(content) + "\n").encode()
entries["META-INF/MANIFEST.MF"] = b"Manifest-Version: 1.0\r\nMain-Class: edu.canino.Main\r\nAdd-Opens: java.base/java.lang\r\n\r\n"
with zipfile.ZipFile(dest, "w", compression=zipfile.ZIP_DEFLATED, compresslevel=9) as z:
    for name, data in sorted(entries.items()):
        info = zipfile.ZipInfo(name, (2026, 11, 27, 0, 0, 0))
        info.compress_type = zipfile.ZIP_DEFLATED
        z.writestr(info, data)
print(dest)
