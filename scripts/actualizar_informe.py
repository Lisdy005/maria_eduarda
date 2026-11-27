#!/usr/bin/env python3
"""Inserta tablas y gráficas reales en el informe y evita discusión obsoleta."""
import csv
from pathlib import Path

root = Path(__file__).resolve().parent.parent
with (root / "resultados/metricas_globales.csv").open(encoding="utf-8") as f:
    rows = {r["algoritmo"]: r for r in csv.DictReader(f)}
expected = {"J48": ("0.700000", "0.696176"), "NaiveBayes": ("0.830000", "0.829347")}
for name, (accuracy, f1) in expected.items():
    if (rows[name]["accuracy"], rows[name]["f1_macro"]) != (accuracy, f1):
        raise SystemExit("Cambiaron los resultados: revise también la discusión de la plantilla antes de generar el informe.")
results = (root / "resultados/metricas.md").read_text(encoding="utf-8").split("\n", 1)[1].strip()
results = results.replace("\n## ", "\n#### ")
for name in ("comparacion.svg", "clases_J48.svg", "clases_NaiveBayes.svg", "confusion_J48.svg", "confusion_NaiveBayes.svg"):
    results = results.replace(f"]({name})", f"](../resultados/{name})")
template = (root / "docs/INFORME.plantilla.md").read_text(encoding="utf-8")
assert template.count("{{RESULTADOS}}") == 1
(root / "docs/INFORME.md").write_text(template.replace("{{RESULTADOS}}", results), encoding="utf-8")
print("Informe completo generado: docs/INFORME.md")
