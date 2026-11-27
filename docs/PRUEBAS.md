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

Las pruebas incluyen generación/hash exactos, balance/binariedad, cabecera equivocada, datos faltantes/vacíos, ida/vuelta de modelos con 128 patrones por algoritmo y distribución normalizada, modelo inexistente, argumentos/comandos/síntomas/algoritmos inválidos y experimento ejecutado dos veces con resultados idénticos. La compatibilidad Java se comprueba por ejecución, no solo declarando el `pom.xml`. Se usó una revisión antigua de Java 11 únicamente porque estaba disponible para comprobar el mínimo; se recomienda instalar un Java actualizado.

## Comandos para repetir con herramientas habituales

```sh
mvn clean verify
java -Dfile.encoding=UTF-8 -jar target/diagnosticador-canino.jar generar dataset.arff
java -Dfile.encoding=UTF-8 -jar target/diagnosticador-canino.jar entrenar dataset.arff modelos
java -Dfile.encoding=UTF-8 -jar target/diagnosticador-canino.jar experimento dataset.arff resultados
python3 scripts/verificar_resultados.py
```

Si no dispone de Maven, los mismos comandos de la aplicación se pueden ejecutar contra `entrega/diagnosticador-canino.jar`. El auditor usa solo Python estándar. `scripts/empaquetar_jar.py` es un empaquetador auxiliar y no resuelve ni compila dependencias: requiere clases ya compiladas. El flujo normal Maven resuelve Weka y sus transitivas y usa Shade.

## Lo que NO se ha ejecutado en este entorno

- **Maven:** no estaba instalado y no hay acceso a Maven Central desde el entorno. No existe un registro local de `mvn verify`; la compilación/pruebas efectivas fueron con ECJ/JUnit y bibliotecas reales. La configuración Maven está incluida y la CI `verify.yml` la ejecutará cuando se publique/dispare, pero no se declara esa CI como aprobada antes de que ocurra.
- **Interacción visual Swing:** no hay servidor de pantalla. La clase compila, la lógica de entrenamiento/inferencia está probada independientemente de la vista y se comprueba el mensaje para entorno headless. No se afirma prueba de clics, captura de pantalla ni revisión de accesibilidad.
- **Validación clínica:** no hay datos reales, confirmación por laboratorio, revisión veterinaria, calibración, estudio prospectivo ni rendimiento en pacientes. No se debe usar el prototipo para triage clínico ni para descartar rabia.

## Lista de comprobación visual en un equipo con escritorio

1. Desde la raíz, ejecutar `java -jar entrega/diagnosticador-canino.jar gui` y confirmar que se ven el aviso sintético y la advertencia de urgencia.
2. En **Entrenar**, examinar `dataset.arff`, elegir una carpeta temporal, pulsar entrenar y confirmar. Deben aparecer ambos `.model` y ambos `.txt`; la ventana debe poder repintarse durante el trabajo.
3. Elegir un ARFF inexistente o incompatible: debe verse un error legible y no un mensaje de éxito.
4. En **Orientación sintética**, seleccionar el modelo J48 recién creado. Probar sin aceptar las limitaciones y luego sin marcar signos: la app debe impedir el cálculo.
5. Aceptar la codificación, marcar fiebre, vómito, diarrea, letargo, pérdida de apetito y sangre en heces. Debe mostrarse una clase y la distribución de las ocho clases, con advertencias.
6. Repetir usando `NaiveBayes.model`. Comprobar que la salida identifica el algoritmo elegido y que se recarga del archivo seleccionado.
7. Elegir una ruta inexistente: debe mostrarse el error sin presentar una nueva predicción como válida. No probar archivos maliciosos o desconocidos.
8. Marcar signos neurológicos/salivación: el aviso de urgencia debe seguir visible cualquiera que sea la etiqueta calculada. No interpretar los porcentajes como probabilidad clínica.
9. Redimensionar, usar navegación por teclado, revisar legibilidad/contraste y cerrar la ventana. Registrar sistema operativo, Java y cualquier defecto visual.

Criterio de aceptación final: conservar el registro automatizado y añadir fecha/resultado de esta lista tras probarla. Ninguna marca pendiente debe convertirse en una afirmación de ejecución por el simple hecho de que exista el código.
