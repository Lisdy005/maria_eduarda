# Dependencias, procedencia y licencias

## Código del proyecto

El código de este proyecto se distribuye bajo GNU GPL versión 3, texto completo en `LICENSE`, de forma coherente con la biblioteca Weka enlazada. Los campos de autor y afiliación académica permanecen como marcadores en la portada; deben completarse honestamente antes de entregar. No existe garantía de aptitud médica o clínica. Los datos sintéticos, documentación y resultados se incluyen como parte del proyecto bajo la misma licencia.

## Construcción Maven preferida

`pom.xml` declara exactamente **`nz.ac.waikato.cms.weka:weka-stable:3.8.5`**, no una versión aproximada. Maven resuelve también sus dependencias transitivas. **FlatLaf 3.5.4** (`com.formdev:flatlaf`, licencia Apache 2.0, <https://github.com/JFormDesigner/FlatLaf>) proporciona el aspecto visual de la interfaz Swing y se incluye en el JAR. JUnit 4.13.2 es una dependencia exclusiva de pruebas y no se incluye en el JAR Maven final. Maven Compiler, Surefire y Shade tienen versiones explícitas. Weka se distribuye bajo GPL v3; JUnit 4 bajo Eclipse Public License 1.0. Otras bibliotecas conservan sus licencias y avisos en los artefactos Maven.

Fuente estable de Weka: <https://github.com/Waikato/weka-3.8>. Estado de publicación 3.8.5: commit `91df467a5d042561b8b6f052b038144419f32ba2` (actualización previa a la publicación Maven). Sus fuentes pueden obtenerse por Git o como archivo de ese commit. No se modificó el código de Weka.

## Procedencia del JAR ya ejecutado e incluido

Este entorno no permite resolver Maven Central. Para compilar y ejecutar se utilizó un JAR **de distribución Weka 3.8.5**, disponible como copia en:

- <https://github.com/GauravPandeyLab/ensemble_integration/blob/HEAD/groovy_scripts/weka.jar>
- Blob Git fijado: `69e56305e086439ff93b1bb404119e8f4abf15d1`.
- SHA-256 del archivo original: `4ed886e9648c2ce61b52b66ce82b8d7e5358b0e7b403ac4abf9249e5458d801a`.
- Versión verificada ejecutando `weka.core.Version`: **3.8.5**. No se afirma haber comparado este binario con el checksum del artefacto Maven Central.

El JAR de distribución incorpora recursos anidados `mtj.jar`, `core.jar` y `arpack_combined.jar` para inicialización Weka. Se conservaron sin modificar. El JAR de entrega contiene además `bounce.jar`, tomado del repositorio estable Weka:

- <https://github.com/Waikato/weka-3.8/blob/master/weka/lib/bounce.jar>.
- SHA-256: `41ae9ec97d7f1ce7366b577601bdaecb43cbfa092365d3329f1765dc29fc57e7`.

Los avisos originales de bibliotecas de esa distribución se adjuntan en `docs/LICENCIAS-WEKA-LIB.txt`. Incluyen Bounce (licencia de tipo BSD), core/netlib (BSD), ARPACK (BSD) y MTJ (LGPL 2.1 o posterior). Los textos GPL-3 y LGPL-2.1 se conservan en `LICENSE` y `docs/LGPL-2.1.txt`. Las fuentes de componentes de terceros están disponibles en los proyectos Weka, <https://github.com/fommil/netlib-java>, <https://github.com/fommil/matrix-toolkits-java> y <https://github.com/opencollab/arpack-ng>. Los recursos originales de licencia dentro de cada archivo no se sustituyen por avisos del proyecto.

La primera versión de `entrega/diagnosticador-canino.jar` se ensambló con `scripts/empaquetar_jar.py` a partir de esos JAR. **La versión actual es el JAR producido por `mvn clean verify` con Maven Shade** (Weka 3.8.5 resuelto desde Maven Central, más FlatLaf), copiado de `target/` a `entrega/`. Este ejecutable está pensado para las funciones de este proyecto (ARFF, J48, Naive Bayes y Swing), no para ofrecer todas las funciones opcionales de Weka. `mvn clean verify` produce el equivalente de la aplicación con resolución transitiva y Maven Shade, en `target/diagnosticador-canino.jar`. Los dos procesos no prometen identidad binaria del archivo empaquetado; sí se comprueba identidad del dataset y de las métricas.

## Herramientas usadas solo durante construcción/pruebas

- Eclipse ECJ 3.16.0: compilación real con `-proc:none -11 -encoding UTF-8`, obtenido del repositorio `processing/processing4`, archivo `java/mode/org.eclipse.jdt.core.jar`, blob `b749e518b8a065856cc948363db71253fb855f84`. No está incluido en la aplicación.
- Java de paquetes `jdk4py` de PyPI: OpenJDK 21.0.8 y OpenJDK 11.0.9.1 para comprobación. No se distribuyen las máquinas virtuales; instale una versión mantenida de Java 11+.
- JUnit 4.13.2 y Hamcrest 1.3: pruebas ejecutadas de forma directa con `org.junit.runner.JUnitCore`, no mediante una ejecución local de Maven que no ocurrió. No se distribuyen como parte de la app.
- Python 3: scripts opcionales de auditoría, documentación y empaquetado; no interviene en el entrenamiento ni en la inferencia de la aplicación Java.

No se incluyeron cachés de herramientas, JDK, repositorios Maven, credenciales ni datasets externos. `entrega/SHA256SUMS.txt` registra hashes de los entregables y `proyecto-completo.zip.sha256` identifica el paquete descargable.
