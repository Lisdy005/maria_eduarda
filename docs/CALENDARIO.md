# 7. Calendario sugerido: Propuesta → Datos → Experimento → Entrega final

Plan académico de cuatro semanas (20 días hábiles). Sustituir las semanas por fechas del curso. La entrega de software y datos sintéticos no equivale a una autorización de uso clínico.

| Fase | Días | Trabajo | Producto verificable | Criterio de cierre |
|---|---|---|---|---|
| **Propuesta** | 1–4 | Delimitar clasificación multiclase y uso exclusivamente educativo; revisar Russell y Norvig, C4.5 y bibliografía veterinaria; definir ocho clases, quince signos, riesgos y objetivos. Dibujar arquitectura y acordar el contrato nominal. | Planteamiento, requisitos, esquema ARFF, revisión bibliográfica y diagrama inicial. | El tutor acepta el alcance; no se promete diagnóstico real; están definidos métricas y comparación antes de observar resultados. |
| **Datos** | 5–9 | Construir perfiles manuales, documentar su origen cualitativo y sus supuestos; programar generador con semilla; generar las 200 filas; validar tamaño, balance, valores y duplicados. | `DataGenerator.java`, `Schema.java`, `dataset.arff`, `docs/DATOS.md` y hash reproducible. | 25 filas por clase, 15 predictores binarios, sin faltantes; regeneración idéntica; limitaciones explícitas. |
| **Experimento** | 10–15 | Implementar CV estratificada emparejada, J48 y Naive Bayes, métricas y gráficos; separar modelos de evaluación de modelos finales; programar serialización, CLI e interfaz Swing; añadir pruebas. | Servicios Java, `Experiment.java`, CSV/SVG/Markdown, ambos `.model`, interfaz funcional y pruebas automáticas. | 200 predicciones OOF por algoritmo; matrices coherentes con métricas; inferencia tras recarga; auditoría reproducible. No ajustar modelos y afirmar evaluación imparcial con los mismos pliegues. |
| **Entrega final** | 16–20 | Interpretar resultados y sesgos; completar portada, informe y conclusiones; probar instrucciones en un equipo limpio y Swing con pantalla; revisar accesibilidad, licencias e inventario; empaquetar y demostrar. | `docs/INFORME.md`, README, registros de pruebas, JAR y ZIP completo. | Compilación Maven y pruebas en el equipo receptor, verificación visual según `PRUEBAS.md`, siete entregables accesibles y advertencias visibles. Registrar comprobaciones aún pendientes en lugar de declararlas realizadas. |

## Hitos de revisión

- **Fin de semana 1:** aprobación de problema, contrato de datos y plan experimental.
- **Fin de semana 2:** congelación del dataset y hash; cualquier cambio posterior exige versionar datos y repetir resultados.
- **Fin de semana 3:** demostración de entrenamiento, CV y predicción usando un modelo guardado.
- **Fin de semana 4:** revisión final del informe y entrega reproducible con autor, institución y fecha completados.

## Distribución sugerida de responsabilidades

En un proyecto individual, el autor asume desarrollo, análisis y redacción, usando la revisión del tutor para requisitos y alcance. En un equipo, una persona puede responsabilizarse de datos/documentación, otra de modelos/evaluación y otra de interfaz/pruebas; todos revisan la coherencia entre código, métricas e informe. Ninguno de estos roles sustituye revisión veterinaria.

## Después de la entrega académica

La obtención de datos reales, consentimiento, gobernanza, revisión veterinaria, calibración, validación externa y evaluación prospectiva se consideran **un proyecto futuro separado**. No se prometen dentro de cuatro semanas ni están completadas en este repositorio. El calendario se da como propuesta de organización; los registros `resultados/pruebas*.txt` y `docs/PRUEBAS.md` describen lo efectivamente comprobado en la entrega actual.
