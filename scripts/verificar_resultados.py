#!/usr/bin/env python3
"""Auditor independiente de CSV: biblioteca estándar, no depende de Weka."""
import csv
import math
import pathlib
import sys
from collections import Counter

root = pathlib.Path(sys.argv[1] if len(sys.argv) > 1 else ".")
folder = root / "resultados"
def read(name):
    with (folder / name).open(encoding="utf-8", newline="") as f:
        return list(csv.DictReader(f))

global_rows = read("metricas_globales.csv")
classes_rows = read("metricas_por_clase.csv")
predictions = read("predicciones.csv")
assignment = read("asignacion_pliegues.csv")
assert len(assignment) == 200
assert len({r["indice_cv"] for r in assignment}) == 200
by_index = {r["indice_cv"]: r for r in assignment}
assert set(Counter(r["clase"] for r in assignment).values()) == {25}
assert set(Counter(r["pliegue"] for r in assignment).values()) == {20}
assert set(Counter((r["pliegue"], r["clase"]) for r in assignment).values()) <= {2, 3}
for summary in global_rows:
    name = summary["algoritmo"]
    rows = [r for r in predictions if r["algoritmo"] == name]
    classes = [r["clase"] for r in classes_rows if r["algoritmo"] == name]
    assert len(rows) == 200 and len({r["indice_cv"] for r in rows}) == 200
    cm = Counter((r["real"], r["predicha"]) for r in rows)
    for r in rows:
        ref = by_index[r["indice_cv"]]
        assert r["pliegue"] == ref["pliegue"] and r["real"] == ref["clase"]
        p = [float(r["p_" + c]) for c in classes]
        assert all(math.isfinite(x) and 0 <= x <= 1 for x in p)
        assert abs(sum(p) - 1) < 5e-6  # CSV redondea seis decimales
        assert p[classes.index(r["predicha"])] >= max(p) - 1e-6
    accuracy = sum(cm[c, c] for c in classes) / len(rows)
    assert abs(accuracy - float(summary["accuracy"])) < 1e-6
    metrics = []
    for c in classes:
        tp = cm[c, c]
        support = sum(cm[c, x] for x in classes)
        predicted = sum(cm[x, c] for x in classes)
        precision = tp / predicted if predicted else 0
        recall = tp / support if support else 0
        f1 = 2 * precision * recall / (precision + recall) if precision + recall else 0
        metrics.append((precision, recall, f1))
        exported = next(r for r in classes_rows if r["algoritmo"] == name and r["clase"] == c)
        assert int(exported["soporte"]) == support
        for key, val in zip(("precision", "recall", "f1"), metrics[-1]):
            assert abs(float(exported[key]) - val) < 1e-6
    for i, key in enumerate(("precision_macro", "recall_macro", "f1_macro")):
        assert abs(sum(m[i] for m in metrics) / len(classes) - float(summary[key])) < 1e-6
    with (folder / f"confusion_{name}.csv").open(encoding="utf-8", newline="") as f:
        for r in csv.DictReader(f):
            for c in classes:
                assert int(r[c]) == cm[r["real/predicha"], c]
    print(f"OK {name}: 200 predicciones OOF; accuracy={accuracy:.2%}; matriz y P/R/F1 consistentes")
print("OK estratificación, particiones emparejadas, probabilidades y cobertura de filas")
