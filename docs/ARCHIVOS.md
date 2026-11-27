# 6. Inventario de archivos del proyecto

Se enumeran los archivos entregados, no las carpetas temporales de compilación. `target/`, `.git/`, JDK, cachés y dependencias de construcción no forman parte del ZIP. Los archivos ZIP/SHA externos se enumeran aquí aunque no se anidan dentro del propio paquete.

|Archivo|Función / entregable|
|---|---|
|`.github/workflows/verify.yml`|6 · CI Maven, pruebas e integración en Java 11/17/21.|
|`.gitignore`|Documentación, contrato de datos, plantilla o licencia.|
|`LICENSE`|Documentación, contrato de datos, plantilla o licencia.|
|`README.md`|6 · Instrucciones de ejecución y navegación de los siete entregables.|
|`dataset.arff`|1 · Dataset sintético completo (200 filas).|
|`docs/ARCHIVOS.md`|Documentación, contrato de datos, plantilla o licencia.|
|`docs/CALENDARIO.md`|7 · Calendario de fases, tareas e hitos.|
|`docs/DATOS.md`|Documentación, contrato de datos, plantilla o licencia.|
|`docs/INFORME.md`|5 · Informe completo redactado, con tablas y gráficas.|
|`docs/INFORME.plantilla.md`|Documentación, contrato de datos, plantilla o licencia.|
|`docs/LGPL-2.1.txt`|Documentación, contrato de datos, plantilla o licencia.|
|`docs/LICENCIAS-WEKA-LIB.txt`|Documentación, contrato de datos, plantilla o licencia.|
|`docs/PRUEBAS.md`|Documentación, contrato de datos, plantilla o licencia.|
|`docs/TERCEROS.md`|Documentación, contrato de datos, plantilla o licencia.|
|`entrega/SHA256SUMS.txt`|Manifiesto de integridad SHA-256.|
|`entrega/diagnosticador-canino.jar`|4 · Aplicación ejecutable con Weka 3.8.5.|
|`entrega/proyecto-completo.zip`|Paquete completo descargable; no se incluye dentro de sí mismo.|
|`entrega/proyecto-completo.zip.sha256`|Manifiesto de integridad SHA-256.|
|`modelos/J48.model`|4 · Modelo Weka serializado con cabecera y metadatos.|
|`modelos/J48.txt`|4 · Representación textual del modelo entrenado.|
|`modelos/NaiveBayes.model`|4 · Modelo Weka serializado con cabecera y metadatos.|
|`modelos/NaiveBayes.txt`|4 · Representación textual del modelo entrenado.|
|`pom.xml`|4 · Dependencias y construcción Maven para Java 11+.|
|`resultados/asignacion_pliegues.csv`|3 · Resultado ejecutado, gráfico o evidencia de verificación.|
|`resultados/auditoria.txt`|3 · Resultado ejecutado, gráfico o evidencia de verificación.|
|`resultados/clases_J48.svg`|3 · Resultado ejecutado, gráfico o evidencia de verificación.|
|`resultados/clases_NaiveBayes.svg`|3 · Resultado ejecutado, gráfico o evidencia de verificación.|
|`resultados/comparacion.svg`|3 · Resultado ejecutado, gráfico o evidencia de verificación.|
|`resultados/compilacion.txt`|3 · Resultado ejecutado, gráfico o evidencia de verificación.|
|`resultados/confusion_J48.csv`|3 · Resultado ejecutado, gráfico o evidencia de verificación.|
|`resultados/confusion_J48.svg`|3 · Resultado ejecutado, gráfico o evidencia de verificación.|
|`resultados/confusion_NaiveBayes.csv`|3 · Resultado ejecutado, gráfico o evidencia de verificación.|
|`resultados/confusion_NaiveBayes.svg`|3 · Resultado ejecutado, gráfico o evidencia de verificación.|
|`resultados/ejecucion.txt`|3 · Resultado ejecutado, gráfico o evidencia de verificación.|
|`resultados/metricas.md`|3 · Resultado ejecutado, gráfico o evidencia de verificación.|
|`resultados/metricas_globales.csv`|3 · Resultado ejecutado, gráfico o evidencia de verificación.|
|`resultados/metricas_por_clase.csv`|3 · Resultado ejecutado, gráfico o evidencia de verificación.|
|`resultados/pliegues.csv`|3 · Resultado ejecutado, gráfico o evidencia de verificación.|
|`resultados/predicciones.csv`|3 · Resultado ejecutado, gráfico o evidencia de verificación.|
|`resultados/pruebas.txt`|3 · Resultado ejecutado, gráfico o evidencia de verificación.|
|`resultados/pruebas_java11.txt`|3 · Resultado ejecutado, gráfico o evidencia de verificación.|
|`resultados/weka_J48.txt`|3 · Resultado ejecutado, gráfico o evidencia de verificación.|
|`resultados/weka_NaiveBayes.txt`|3 · Resultado ejecutado, gráfico o evidencia de verificación.|
|`scripts/actualizar_informe.py`|6 · Utilidad reproducible de auditoría/documentación/empaquetado.|
|`scripts/crear_entrega.py`|6 · Utilidad reproducible de auditoría/documentación/empaquetado.|
|`scripts/empaquetar_jar.py`|6 · Utilidad reproducible de auditoría/documentación/empaquetado.|
|`scripts/verificar_resultados.py`|6 · Utilidad reproducible de auditoría/documentación/empaquetado.|
|`src/main/java/edu/canino/Charts.java`|3 · Experimento, métricas y gráficas.|
|`src/main/java/edu/canino/DataGenerator.java`|2 · Generador determinista que produjo el ARFF.|
|`src/main/java/edu/canino/DataIO.java`|4 · Código completo Java / pruebas automatizadas.|
|`src/main/java/edu/canino/Experiment.java`|3 · Experimento, métricas y gráficas.|
|`src/main/java/edu/canino/Main.java`|4 · Código completo Java / pruebas automatizadas.|
|`src/main/java/edu/canino/MainWindow.java`|4 · Código completo Java / pruebas automatizadas.|
|`src/main/java/edu/canino/ModelBundle.java`|4 · Código completo Java / pruebas automatizadas.|
|`src/main/java/edu/canino/PredictionService.java`|4 · Código completo Java / pruebas automatizadas.|
|`src/main/java/edu/canino/Schema.java`|4 · Código completo Java / pruebas automatizadas.|
|`src/main/java/edu/canino/TrainingService.java`|4 · Código completo Java / pruebas automatizadas.|
|`src/main/resources/META-INF/licenses/LGPL-2.1.txt`|Avisos de licencia incluidos en el JAR.|
|`src/main/resources/META-INF/licenses/PROJECT-GPL-3.0.txt`|Avisos de licencia incluidos en el JAR.|
|`src/main/resources/META-INF/licenses/WEKA-LIB.txt`|Avisos de licencia incluidos en el JAR.|
|`src/test/java/edu/canino/ProjectTest.java`|4 · Código completo Java / pruebas automatizadas.|

El paquete contiene las fuentes completas, datos, modelos finales, resultados CSV/SVG, informe, instrucciones y el JAR. La aplicación no necesita Python ni Maven para ejecutarse desde el JAR. Para reconstruir desde código se recomienda Maven/JDK, como se explica en README.
