# 1–2. Datos y generador sintético

## Tamaño y formato

`dataset.arff` contiene 200 filas: ocho clases, 25 muestras por enfermedad; 15 atributos predictores nominales binarios y una salida nominal `enfermedad`. Codificación UTF-8, líneas LF, sin datos faltantes ni pesos especiales. `@relation` identifica la relación; `@attribute` declara columnas; `@data` precede todas las filas. El atributo de salida es el último.

No se trata de observaciones de pacientes. No se han recolectado datos personales ni de propietarios. La enfermedad es la etiqueta elegida antes de muestrear signos, no un diagnóstico confirmado. Se incluyen parvovirosis (parvovirus canino), moquillo, rabia, otitis, parasitosis intestinal, traqueobronquitis infecciosa, dermatitis alérgica y leptospirosis. Son categorías educativas de granularidad heterogénea.

## Diccionario de síntomas

1 significa presente y 0 ausente, NO desconocido. Los valores se escriben numéricamente, pero se declaran nominales `{0,1}` en Weka. La aplicación comprueba los nombres y el orden exactos.

|Posición|Identificador ARFF/CLI|Interpretación y alcance|
|---:|---|---|
|1|`fiebre`|Fiebre observada/medida; no incluye temperatura continua.|
|2|`vomito`|Vómito; sin frecuencia o duración.|
|3|`diarrea`|Diarrea; sin graduación de severidad.|
|4|`tos`|Tos; no incluye dificultad respiratoria como variable independiente.|
|5|`letargo`|Letargo/disminución de actividad.|
|6|`perdida_apetito`|Pérdida de apetito.|
|7|`secrecion_oculonasal`|Secreción ocular o nasal, agrupadas para simplificar.|
|8|`signos_neurologicos`|Alteraciones neurológicas observadas, categoría amplia sin localización.|
|9|`salivacion_excesiva`|Salivación excesiva, inespecífica.|
|10|`prurito`|Prurito/picazón.|
|11|`molestia_oido`|Molestia en oído (dolor, sacudidas o rascado), agrupada.|
|12|`perdida_peso`|Pérdida de peso, sin cuantificación.|
|13|`sangre_heces`|Sangre en heces.|
|14|`ictericia`|Coloración amarillenta compatible con ictericia.|
|15|`sed_aumentada`|Sed aumentada, sin medición del consumo.|

## Matriz completa de parámetros del generador

Las columnas de esta tabla son los síntomas abreviados como S1…S15 según la posición anterior; cada fila corresponde a una clase. Cada valor es la probabilidad **artificial** de muestrear un 1 para ese síntoma y esa clase. No son probabilidades clínicas, sensibilidades, prevalencias ni estimaciones obtenidas de pacientes. Son elecciones manuales inspiradas cualitativamente en los cuadros de los textos citados en el informe. Ninguna fuente bibliográfica valida estas cifras.

|Clase|S1|S2|S3|S4|S5|S6|S7|S8|S9|S10|S11|S12|S13|S14|S15|
|---|---:|---:|---:|---:|---:|---:|---:|---:|---:|---:|---:|---:|---:|---:|---:|
|parvovirosis|0.80|0.92|0.94|0.04|0.92|0.90|0.04|0.03|0.12|0.03|0.03|0.45|0.78|0.03|0.30|
|moquillo|0.82|0.35|0.45|0.75|0.85|0.80|0.90|0.65|0.25|0.06|0.08|0.35|0.10|0.04|0.18|
|rabia|0.40|0.10|0.08|0.08|0.55|0.65|0.08|0.94|0.92|0.10|0.08|0.20|0.03|0.03|0.12|
|otitis|0.12|0.08|0.06|0.05|0.25|0.22|0.10|0.12|0.08|0.60|0.94|0.08|0.03|0.02|0.08|
|parasitosis_intestinal|0.15|0.35|0.82|0.06|0.55|0.50|0.05|0.03|0.06|0.18|0.04|0.88|0.35|0.03|0.18|
|traqueobronquitis_infecciosa|0.35|0.16|0.08|0.96|0.40|0.40|0.65|0.02|0.08|0.04|0.03|0.12|0.02|0.02|0.10|
|dermatitis_alergica|0.06|0.06|0.05|0.06|0.16|0.15|0.10|0.02|0.04|0.96|0.45|0.08|0.02|0.02|0.08|
|leptospirosis|0.88|0.72|0.42|0.15|0.90|0.86|0.08|0.08|0.18|0.03|0.03|0.30|0.12|0.82|0.85|

## Procedimiento que produjo el archivo

`DataGenerator.generate(Path)` crea `java.util.Random(20261127L)`, recorre las clases en el orden de la tabla y toma 25 muestras por clase. Para cada signo genera un uniforme `nextDouble()` y asigna 1 si es menor que el parámetro. Finalmente `Collections.shuffle(rows, random)` mezcla las 200 filas usando el mismo flujo pseudoaleatorio. La independencia de los síntomas dada la clase es un supuesto del generador, no un hallazgo. Favorece estructuralmente al modelo Naive Bayes.

No se aplicó eliminación de duplicados, limpieza clínica ni selección por resultados. Se obtienen 163 vectores de predictores distintos y 37 repeticiones por encima de los únicos, contabilizadas sin incluir la etiqueta. Las repeticiones pueden cruzar los pliegues de validación. No se afirma que las 200 observaciones contengan 200 patrones independientes o pacientes diferentes.

Regeneración, desde la raíz:

```sh
java -Dfile.encoding=UTF-8 -jar entrega/diagnosticador-canino.jar generar dataset.arff
```

SHA-256 esperado: `64d2ba7e29beb5ecd88934640822fe1e964f72681d668e60d4e897b16a938924`. La prueba automatizada compara tanto el hash como el archivo completo generado. Los parámetros son transparentes en `src/main/java/edu/canino/DataGenerator.java`. Si se modifican, se deben regenerar datos, modelos, experimento e informe; las pruebas doradas deben revisarse justificadamente, no eliminarse para ocultar diferencias.

## Limitaciones y uso responsable

- Datos 100 % sintéticos y muestra pequeña: 25 casos por clase. **Sin validación clínica**, sin revisión veterinaria de probabilidades.
- Perfiles cualitativamente plausibles, no evidencia epidemiológica ni tablas clínicas cuantitativas.
- Síntomas correlacionados en realidad se generan independientemente; pueden aparecer combinaciones atípicas.
- No hay sanos, otras enfermedades, estados desconocidos, coinfecciones ni múltiples etiquetas.
- Clase balanceada artificialmente: los prioris y porcentajes del modelo no se trasladan a una población.
- No se modelan edad, raza, vacunación, exposición, evolución temporal, gravedad, exploración, laboratorio o geografía.
- Una etiqueta forzada no excluye ninguna enfermedad. Un porcentaje bajo o 0 % no descarta rabia u otra urgencia.
- Los CSV y gráficos evalúan recuperación del mecanismo generador, no exactitud médica. Antes de una aplicación clínica harían falta datos reales, revisión profesional y validación externa/prospectiva.

La app muestra advertencias persistentes y no prescribe tratamientos. Ante signos de alarma o sospecha de rabia debe acudirse a profesionales independientemente de la salida del modelo.
