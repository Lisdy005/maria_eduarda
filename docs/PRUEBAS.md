# Verificación, alcance y comprobaciones pendientes

## Ejecutado realmente en esta entrega

| Comprobación | Resultado | Evidencia |
|---|---|---|
| Compilación de las diez clases de producción | Correcta, bytecode Java 11 (major 55), sin pseudocódigo | ECJ 3.16.0, opción `-11`, registro `resultados/compilacion.txt` |
| Generación reproducible | 200 filas, 25 por clase, 15 síntomas; SHA-256 esperado | `dataset.arff`, JUnit y `docs/DATOS.md` |
| JUnit 4.13.2 con Java 21.0.8 | **12 pruebas correctas** | `resultados/pruebas.txt` |
| JUnit 4.13.2 con Java 11.0.9.1 | **12 pruebas correctas** | `resultados/pruebas_java11.txt` |
| Entrenar desde el JAR | Correcto: se guardan J48 y Naive Bayes y sus representaciones legibles | `modelos/` y `resultados/ejecucion.txt` |
| Evaluación desde el JAR | Correcta, accuracy 0.70/0.83 | `resultados/metricas_globales.csv` |
| Mismas métricas en Java 11 y 21 | Iguales, comparación de CSV sin diferencias | `resultados/ejecucion.txt` |
| Cargar modelo entrenado en Java 21 desde Java 11 | Inferencia correcta | `resultados/ejecucion.txt` |
| Auditor independiente de CSV | Matrices, accuracy, P/R/F1, pliegues y probabilidades consistentes | `resultados/auditoria.txt` |
| Errores por comando/síntoma/modelo inexistente | Fallo explícito, sin predicción fabricada | JUnit y comprobación CLI |
| **`mvn -B clean verify`** (Maven 3, JDK 21, `release 11`) | **BUILD SUCCESS, 12/12 pruebas**; JAR con Shade generado | `resultados/maven_verify.txt` |
| Reproducción completa con el JAR de Maven | ARFF idéntico byte a byte; los 7 CSV y 5 SVG idénticos a los incluidos; auditor correcto | Comparación `cmp` y `scripts/verificar_resultados.py` |
| **Interfaz Swing real bajo Xvfb** | Ventana abierta, 6 checkboxes marcados, botón pulsado, modelo cargado del disco; muestra `parvovirosis` y las 8 probabilidades | `resultados/prueba_gui.txt`, `docs/img/gui_1_entrenar.png`, `docs/img/gui_2_orientacion.png` |

Las pruebas incluyen generación/hash exactos, balance/binariedad, cabecera equivocada, datos faltantes/vacíos, ida/vuelta de modelos con 128 patrones por algoritmo y distribución normalizada, modelo inexistente, argumentos/comandos/síntomas/algoritmos inválidos y experimento ejecutado dos veces con resultados idénticos. La compatibilidad Java se comprueba por ejecución, no solo declarando el `pom.xml`. Se usó una revisión antigua de Java 11 únicamente porque estaba disponible para comprobar el mínimo; se recomienda instalar un Java actualizado.

## Comandos para repetir con herramientas habituales

```sh
mvn clean verify
java -Dfile.encoding=UTF-8 -jar target/diagnosticador-canino.jar generar dataset.arff
java -Dfile.encoding=UTF-8 -jar target/diagnosticador-canino.jar entrenar dataset.arff modelos
java -Dfile.encoding=UTF-8 -jar target/diagnosticador-canino.jar experimento dataset.arff resultados
python3 scripts/verificar_resultados.py
```

Prueba automatizada de la interfaz (Linux con `xvfb-run`; en un escritorio normal omita `xvfb-run ...`):

```sh
mkdir -p target/gui
javac -cp target/diagnosticador-canino.jar -d target/gui scripts/PruebaGui.java
xvfb-run -a java --add-opens=java.base/java.lang=ALL-UNNAMED -cp target/diagnosticador-canino.jar:target/gui PruebaGui target/gui
```

Guarda `gui_1_entrenar.png` y `gui_2_orientacion.png` en `target/gui` y termina con código 0 si la ventana muestra la clase esperada.

Si no dispone de Maven, los mismos comandos de la aplicación se pueden ejecutar contra `entrega/diagnosticador-canino.jar`. El auditor usa solo Python estándar. `scripts/empaquetar_jar.py` es un empaquetador auxiliar y no resuelve ni compila dependencias: requiere clases ya compiladas. El flujo normal Maven resuelve Weka y sus transitivas y usa Shade.

## Lo que NO se ha ejecutado en este entorno

- **Revisión manual de Swing en escritorio físico:** la prueba automatizada (Xvfb) demuestra que la ventana se construye, que los checkboxes y el botón funcionan y que se muestra la distribución, pero no sustituye una revisión humana de accesibilidad, navegación por teclado, contraste y diálogos de selección de archivos. Use la lista de abajo.
- **CI de GitHub Actions:** está configurada; su resultado depende de que se ejecute en GitHub y no se declara aquí.
- **Validación clínica:** no hay datos reales, confirmación por laboratorio, revisión veterinaria, calibración, estudio prospectivo ni rendimiento en pacientes. No se debe usar el prototipo para triage clínico ni para descartar rabia.

## Lista de comprobación visual en un equipo con escritorio

1. Desde la raíz, ejecutar `java -jar entrega/diagnosticador-canino.jar gui` y confirmar que se ven el aviso sintético y la advertencia de urgencia.
2. En **Entrenar**, examinar `dataset.arff`, elegir una carpeta temporal, pulsar entrenar y confirmar. Deben aparecer ambos `.model` y ambos `.txt`; la ventana debe poder repintarse durante el trabajo.
3. Elegir un ARFF inexistente o incompatible: debe verse un error legible y no un mensaje de éxito.
4. En **Diagnosticar**, elegir «J48 · árbol de decisión» en el selector de modelo. Probar sin aceptar las limitaciones y luego sin marcar signos: la app debe impedir el cálculo.
5. Aceptar la codificación, marcar fiebre, vómito, diarrea, letargo, pérdida de apetito y sangre en heces. Debe mostrarse una clase y la distribución de las ocho clases, con advertencias.
6. Repetir usando `NaiveBayes.model`. Comprobar que la salida identifica el algoritmo elegido y que se recarga del archivo seleccionado.
7. Elegir una ruta inexistente: debe mostrarse el error sin presentar una nueva predicción como válida. No probar archivos maliciosos o desconocidos.
8. Marcar signos neurológicos/salivación: el aviso de urgencia debe seguir visible cualquiera que sea la etiqueta calculada. No interpretar los porcentajes como probabilidad clínica.
9. Redimensionar, usar navegación por teclado, revisar legibilidad/contraste y cerrar la ventana. Registrar sistema operativo, Java y cualquier defecto visual.

Criterio de aceptación final: conservar el registro automatizado y añadir fecha/resultado de esta lista tras probarla. Ninguna marca pendiente debe convertirse en una afirmación de ejecución por el simple hecho de que exista el código.
