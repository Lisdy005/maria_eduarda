const pptxgen = require("pptxgenjs");
const React = require("react");
const { renderToStaticMarkup } = require("react-dom/server");
const sharp = require("sharp");
const fa = require("react-icons/fa");
const { applyTheme } = require("/root/.claude/skills/synced/09e112c2-f64b-4780-996c-e9fd7cf61bf8_14a12303-9fa6-4781-ba16-5ded67161d33/pptx/scripts/apply_theme.js");

const OUT = process.argv[2] || "presentacion.pptx";
const SHOT = "/home/user/maria_eduarda/docs/img/gui_2_orientacion.png";

const THEME = {
  name: "Diagnosticador Canino",
  headFontFace: "Cambria",
  bodyFontFace: "Calibri",
  colors: {
    dk1: "1B2A2F", lt1: "FFFFFF", dk2: "0E3B43", lt2: "EDF3F2",
    accent1: "1F7A72", accent2: "E07A1F", accent3: "6B7F84", accent4: "B23A48",
    accent5: "A9D2CB", accent6: "3C6E71", hlink: "1F7A72", folHlink: "3C6E71",
  },
};
const HEX = THEME.colors;

async function icon(Comp, color = "FFFFFF") {
  const svg = renderToStaticMarkup(React.createElement(Comp, { color: "#" + color, size: 256 }));
  const buf = await sharp(Buffer.from(svg)).resize(256, 256).png().toBuffer();
  return "image/png;base64," + buf.toString("base64");
}

(async () => {
  const pres = new pptxgen();
  pres.layout = "LAYOUT_WIDE"; // 13.33 x 7.5
  pres.theme = { headFontFace: THEME.headFontFace, bodyFontFace: THEME.bodyFontFace };
  pres.title = "Diagnosticador funcional de enfermedades caninas";
  pres.subject = "Clasificación supervisada con J48 y Naive Bayes";
  const C = pres.SchemeColor;

  const IC = {
    dog: await icon(fa.FaDog), steth: await icon(fa.FaStethoscope), db: await icon(fa.FaDatabase),
    tree: await icon(fa.FaSitemap), code: await icon(fa.FaLaptopCode), chart: await icon(fa.FaChartBar),
    warn: await icon(fa.FaExclamationTriangle), flask: await icon(fa.FaFlask), book: await icon(fa.FaBookOpen),
    check: await icon(fa.FaCheckCircle), list: await icon(fa.FaListUl), arrow: await icon(fa.FaArrowRight, HEX.accent3),
    user: await icon(fa.FaUserMd), percent: await icon(fa.FaPercentage), random: await icon(fa.FaRandom),
    layers: await icon(fa.FaLayerGroup), redo: await icon(fa.FaRedo), road: await icon(fa.FaRoad),
    dogBig: await icon(fa.FaDog, HEX.accent5),
  };

  // ---------- Layouts ----------
  pres.defineSlideMaster({
    title: "PORTADA",
    background: { color: C.text2 },
    objects: [
      { placeholder: { options: { name: "title", type: "title", x: 0.8, y: 1.9, w: 8.4, h: 2.0, fontSize: 42, bold: true, color: C.background1, valign: "bottom", align: "left", margin: 0 }, text: "" } },
      { placeholder: { options: { name: "body", type: "body", x: 0.8, y: 4.1, w: 8.4, h: 0.9, fontSize: 20, color: C.accent5, valign: "top", align: "left", margin: 0 }, text: "" } },
    ],
  });
  pres.defineSlideMaster({
    title: "CONTENIDO",
    background: { color: C.background1 },
    margin: [0.5, 0.6, 0.6, 0.6],
    objects: [
      { placeholder: { options: { name: "title", type: "title", x: 0.6, y: 0.35, w: 12.1, h: 0.85, fontSize: 34, bold: true, color: C.text2, valign: "middle", margin: 0 }, text: "" } },
      { text: { text: "Datos sintéticos · sin validación clínica · no sustituye al veterinario", options: { x: 0.6, y: 7.0, w: 8, h: 0.3, fontSize: 10, color: C.accent3, margin: 0 } } },
    ],
    slideNumber: { x: 12.2, y: 7.0, w: 0.5, h: 0.3, fontSize: 10, color: C.accent3, align: "right" },
  });
  pres.defineSlideMaster({
    title: "CIERRE",
    background: { color: C.text2 },
    objects: [
      { placeholder: { options: { name: "title", type: "title", x: 0.6, y: 0.35, w: 12.1, h: 0.85, fontSize: 34, bold: true, color: C.background1, valign: "middle", margin: 0 }, text: "" } },
    ],
    slideNumber: { x: 12.2, y: 7.0, w: 0.5, h: 0.3, fontSize: 10, color: C.accent5, align: "right" },
  });

  const txt = (s, t, o) => s.addText(t, Object.assign({ isTextBox: true, margin: 0, fontSize: 16, color: C.text1, valign: "top" }, o));
  const circleIcon = (s, img, x, y, d, fill, name) => {
    s.addShape(pres.shapes.OVAL, { x, y, w: d, h: d, fill: { color: fill || C.accent1 }, line: { type: "none" }, objectName: name + " círculo" });
    const p = d * 0.26;
    s.addImage({ data: img, x: x + p, y: y + p, w: d - 2 * p, h: d - 2 * p, altText: name, objectName: name + " icono" });
  };
  const card = (s, x, y, w, h, fill, name) => s.addShape(pres.shapes.ROUNDED_RECTANGLE, {
    x, y, w, h, rectRadius: 0.12, fill: { color: fill || C.background2 }, line: { type: "none" }, objectName: name,
  });

  // ---------- 1. Portada ----------
  pres.addSection({ title: "Introducción" });
  let s = pres.addSlide({ masterName: "PORTADA", sectionTitle: "Introducción" });
  s.addText("Diagnosticador funcional de enfermedades caninas", { placeholder: "title" });
  s.addText("Clasificación supervisada con J48 y Naive Bayes en Weka", { placeholder: "body" });
  txt(s, [
    { text: "[Nombre del autor/a]", options: { bold: true, breakLine: true } },
    { text: "[Asignatura · Grupo] · [Docente]", options: { breakLine: true } },
    { text: "[Institución] · [Fecha]" },
  ], { x: 0.8, y: 5.35, w: 8.4, h: 1.1, fontSize: 16, color: C.background1, objectName: "Autor" });
  s.addShape(pres.shapes.OVAL, { x: 9.55, y: 1.75, w: 3.2, h: 3.2, fill: { color: C.accent1 }, line: { type: "none" }, objectName: "Círculo portada" });
  s.addImage({ data: IC.dog, x: 10.25, y: 2.45, w: 1.8, h: 1.8, altText: "Icono de perro", objectName: "Perro portada" });
  txt(s, "Prototipo académico · datos sintéticos · sin validación clínica", { x: 9.0, y: 5.3, w: 3.9, h: 0.7, fontSize: 13, italic: true, color: C.accent5, align: "center" });
  s.addNotes("Buenos días. Voy a presentar un prototipo que, a partir de los síntomas observados en un perro, sugiere a cuál de ocho enfermedades se parece más. Es un trabajo académico: los datos son sintéticos y no hay validación clínica, así que en ningún momento sustituye al veterinario.");

  // ---------- 2. Problema ----------
  s = pres.addSlide({ masterName: "CONTENIDO", sectionTitle: "Introducción" });
  s.addText("El problema y el papel de la IA", { placeholder: "title" });
  txt(s, [
    { text: "Los signos se superponen", options: { bold: true, fontSize: 20, color: C.text2, breakLine: true } },
    { text: "Fiebre, vómito, diarrea o letargo aparecen en muchas enfermedades. Ninguno identifica por sí solo la causa.", options: { breakLine: true } },
    { text: " ", options: { breakLine: true, fontSize: 10 } },
    { text: "Qué hace la IA aquí", options: { bold: true, fontSize: 20, color: C.text2, breakLine: true } },
    { text: "Aprende de ejemplos etiquetados una función que asigna a cada combinación de síntomas una enfermedad y una distribución de probabilidad." },
  ], { x: 0.6, y: 1.6, w: 5.4, h: 3.6, fontSize: 16 });
  card(s, 0.6, 5.45, 5.4, 1.2, C.background2, "Aviso");
  circleIcon(s, IC.user, 0.85, 5.65, 0.8, C.accent2, "Veterinario");
  txt(s, "Orienta y prioriza hipótesis. La decisión es siempre del veterinario.", { x: 1.85, y: 5.6, w: 3.95, h: 0.9, fontSize: 16, bold: true, color: C.text2, valign: "middle" });
  // pipeline
  const steps = [
    [IC.list, "Entrada", "15 síntomas marcados como presentes o ausentes"],
    [IC.tree, "Modelo", "J48 o Naive Bayes entrenado en Weka"],
    [IC.percent, "Salida", "Enfermedad sugerida + probabilidad de las 8 clases"],
  ];
  steps.forEach(([ic, h, d], i) => {
    const y = 1.6 + i * 1.75;
    card(s, 6.7, y, 6.0, 1.45, C.background2, "Paso " + h);
    circleIcon(s, ic, 6.95, y + 0.3, 0.85, C.accent1, h);
    txt(s, [{ text: h, options: { bold: true, fontSize: 20, color: C.text2, breakLine: true } }, { text: d }], { x: 8.05, y: y + 0.22, w: 4.4, h: 1.05, fontSize: 16, valign: "middle" });
  });
  s.addNotes("El problema es que muchos signos clínicos se comparten entre enfermedades. El papel concreto de la IA es aprender, a partir de ejemplos etiquetados, una función que va de una lista de síntomas a una enfermedad, y que además devuelve cuánta probabilidad asigna a cada una de las ocho clases. Es apoyo a la orientación diagnóstica, no un diagnóstico.");

  // ---------- 3. Modelación ----------
  pres.addSection({ title: "Desarrollo" });
  s = pres.addSlide({ masterName: "CONTENIDO", sectionTitle: "Desarrollo" });
  s.addText("Modelación formal del problema", { placeholder: "title" });
  const eqs = [
    ["Entrada", "X ∈ {0,1}²⁰⁰ˣ¹⁵", "200 casos × 15 síntomas binarios (1 = presente)"],
    ["Salida", "Y ∈ C,  |C| = 8", "Una enfermedad nominal por caso"],
    ["Modelo", "f̂ : x → ŷ = argmax p̂(c | x)", "Clase más probable y su distribución"],
  ];
  eqs.forEach(([h, eq, d], i) => {
    const y = 1.55 + i * 1.6;
    card(s, 0.6, y, 5.9, 1.35, C.background2, "Ecuación " + h);
    txt(s, h.toUpperCase(), { x: 0.85, y: y + 0.15, w: 5.4, h: 0.3, fontSize: 12, bold: true, color: C.accent1 });
    txt(s, eq, { x: 0.85, y: y + 0.45, w: 5.4, h: 0.45, fontSize: 22, bold: true, color: C.text2, fontFace: "Cambria" });
    txt(s, d, { x: 0.85, y: y + 0.92, w: 5.4, h: 0.35, fontSize: 14, color: C.accent3 });
  });
  txt(s, "Aprendizaje supervisado · multiclase · etiqueta única", { x: 0.6, y: 6.4, w: 5.9, h: 0.4, fontSize: 14, italic: true, color: C.accent3 });
  // tree diagram (real path from modelos/J48.txt)
  txt(s, "Representación como árbol de decisión (rama real del J48)", { x: 7.0, y: 1.55, w: 5.7, h: 0.4, fontSize: 16, bold: true, color: C.text2 });
  const node = (x, y, w, t, fill, col, name) => {
    s.addShape(pres.shapes.ROUNDED_RECTANGLE, { x, y, w, h: 0.6, rectRadius: 0.1, fill: { color: fill }, line: { type: "none" }, objectName: name });
    txt(s, t, { x, y, w, h: 0.6, fontSize: 14, bold: true, color: col, align: "center", valign: "middle" });
  };
  const edge = (x1, y1, x2, y2, label, lx, ly) => {
    s.addShape(pres.shapes.LINE, { x: Math.min(x1, x2), y: y1, w: Math.abs(x2 - x1) || 0.001, h: y2 - y1, flipH: x2 < x1, line: { color: C.accent3, width: 1.5 }, objectName: "Arista " + label });
    txt(s, label, { x: lx, y: ly, w: 0.4, h: 0.3, fontSize: 12, bold: true, color: C.accent3, align: "center" });
  };
  node(8.75, 2.15, 2.2, "¿ictericia?", C.accent1, C.background1, "Nodo ictericia");
  edge(9.85, 2.75, 8.6, 3.35, "0", 8.85, 2.85);
  node(7.4, 3.35, 2.4, "¿signos neurológicos?", C.accent1, C.background1, "Nodo neuro");
  edge(8.6, 3.95, 9.85, 4.55, "1", 9.3, 4.05);
  node(8.75, 4.55, 2.2, "¿tos?", C.accent1, C.background1, "Nodo tos");
  edge(9.85, 5.15, 8.6, 5.75, "0", 8.85, 5.25);
  edge(9.85, 5.15, 11.6, 5.75, "1", 10.9, 5.25);
  node(7.4, 5.75, 2.4, "¿salivación? = 1 → rabia", C.accent2, C.background1, "Hoja rabia");
  node(10.4, 5.75, 2.3, "letargo = 1 → moquillo", C.accent2, C.background1, "Hoja moquillo");
  txt(s, "Cada camino raíz → hoja es una regla «si … entonces …».", { x: 7.0, y: 6.45, w: 5.7, h: 0.4, fontSize: 14, italic: true, color: C.accent3 });
  s.addNotes("Formalmente: X es una matriz de 200 filas por 15 columnas con ceros y unos; Y es una variable nominal con ocho valores. El modelo aprende f que devuelve la clase de máxima probabilidad. A la derecha está una rama real del árbol J48 que aprendió el sistema: si no hay ictericia, hay signos neurológicos, no hay tos y hay salivación, predice rabia. Cada camino del árbol es una regla. Naive Bayes, en cambio, multiplica las probabilidades de cada síntoma dada la enfermedad.");

  // ---------- 4. Datos ----------
  s = pres.addSlide({ masterName: "CONTENIDO", sectionTitle: "Desarrollo" });
  s.addText("Recolección de datos: dataset sintético en ARFF", { placeholder: "title" });
  const stats = [["200", "casos en total"], ["15", "síntomas binarios"], ["8", "enfermedades"], ["25", "casos por enfermedad"]];
  stats.forEach(([n, l], i) => {
    const x = 0.6 + i * 3.1;
    card(s, x, 1.5, 2.8, 1.65, C.background2, "Dato " + l);
    txt(s, n, { x, y: 1.6, w: 2.8, h: 0.95, fontSize: 54, bold: true, color: C.accent1, align: "center", valign: "middle", fontFace: "Cambria" });
    txt(s, l, { x, y: 2.6, w: 2.8, h: 0.4, fontSize: 15, color: C.text2, align: "center" });
  });
  txt(s, [
    { text: "Enfermedades", options: { bold: true, fontSize: 18, color: C.text2, breakLine: true } },
    { text: "Parvovirosis · Moquillo · Rabia · Otitis · Parasitosis intestinal · Traqueobronquitis infecciosa · Dermatitis alérgica · Leptospirosis" },
  ], { x: 0.6, y: 3.5, w: 5.9, h: 1.5, fontSize: 15 });
  txt(s, [
    { text: "Síntomas", options: { bold: true, fontSize: 18, color: C.text2, breakLine: true } },
    { text: "Fiebre · Vómito · Diarrea · Tos · Letargo · Pérdida de apetito · Secreción oculonasal · Signos neurológicos · Salivación · Prurito · Molestia de oído · Pérdida de peso · Sangre en heces · Ictericia · Sed aumentada" },
  ], { x: 6.8, y: 3.5, w: 5.9, h: 1.5, fontSize: 15 });
  card(s, 0.6, 5.3, 12.1, 1.4, C.background2, "Generador");
  circleIcon(s, IC.db, 0.85, 5.5, 1.0, C.accent1, "Generador");
  txt(s, [
    { text: "DataGenerator.java: ", options: { bold: true, color: C.text2 } },
    { text: "cada enfermedad tiene un perfil de probabilidades por síntoma, inspirado en tablas clínicas veterinarias. Se muestrea con semilla fija (20261127), así que el ARFF se regenera idéntico." },
  ], { x: 2.1, y: 5.45, w: 10.4, h: 1.1, fontSize: 15, valign: "middle" });
  s.addNotes("No hay datos reales disponibles, así que construí un generador sintético en Java. Para cada enfermedad definí la probabilidad de cada síntoma, inspirada en la literatura veterinaria, y muestreé 25 casos por clase: 200 en total, en formato ARFF de Weka. Con la semilla fija se obtiene exactamente el mismo archivo. Limitación: son probabilidades didácticas, no prevalencias reales.");

  // ---------- 5. Métodos ----------
  s = pres.addSlide({ masterName: "CONTENIDO", sectionTitle: "Desarrollo" });
  s.addText("Revisión bibliográfica: dos enfoques de clasificación", { placeholder: "title" });
  const methods = [
    [IC.tree, "J48 (C4.5)", "Árbol de decisión", ["Elige en cada nodo el síntoma con mayor ganancia de información", "Poda por confianza 0,25 y mínimo 2 casos por hoja", "Interpretable: se lee como reglas"]],
    [IC.percent, "Naive Bayes", "Modelo probabilístico", ["Aplica el teorema de Bayes sobre cada clase", "Supone síntomas independientes dada la enfermedad", "Pocos parámetros: estable con muestras pequeñas"]],
  ];
  methods.forEach(([ic, h, sub, items], i) => {
    const x = 0.6 + i * 6.2;
    card(s, x, 1.5, 5.9, 3.4, C.background2, "Método " + h);
    circleIcon(s, ic, x + 0.3, 1.75, 0.95, i ? C.accent2 : C.accent1, h);
    txt(s, [{ text: h, options: { bold: true, fontSize: 24, color: C.text2, breakLine: true } }, { text: sub, options: { color: C.accent3 } }], { x: x + 1.45, y: 1.78, w: 4.2, h: 0.95, fontSize: 15, valign: "middle" });
    txt(s, items.map((t, k) => ({ text: t, options: { bullet: true, breakLine: k < items.length - 1 } })), { x: x + 0.35, y: 3.0, w: 5.25, h: 2.4, fontSize: 16, paraSpaceAfter: 8 });
  });
  circleIcon(s, IC.book, 0.6, 5.35, 0.75, C.accent3, "Referencias");
  txt(s, "Russell & Norvig (2021), Artificial Intelligence: A Modern Approach, 4.ª ed. (búsqueda: Temas 2–3; aprendizaje: Temas 4–5) · Quinlan (1993), C4.5 · Witten et al., Data Mining / Weka · literatura veterinaria para los signos clínicos", { x: 1.55, y: 5.3, w: 11.15, h: 0.9, fontSize: 14, color: C.accent3, valign: "middle" });
  s.addNotes("La referencia principal es Russell y Norvig. De los temas de búsqueda tomamos la idea de explorar hipótesis con heurísticas, que es lo que hace un árbol al elegir el mejor síntoma en cada nodo. De los temas de aprendizaje, la clasificación supervisada. Comparo dos familias: J48, la versión de C4.5 en Weka, que es interpretable; y Naive Bayes, que es probabilístico y asume independencia entre síntomas dada la enfermedad.");

  // ---------- 6. Arquitectura ----------
  s = pres.addSlide({ masterName: "CONTENIDO", sectionTitle: "Desarrollo" });
  s.addText("Proyecto de programación: arquitectura", { placeholder: "title" });
  const flow = [["DataGenerator", "genera ARFF"], ["DataIO + Schema", "carga y valida"], ["TrainingService", "entrena y guarda .model"], ["PredictionService", "carga y predice"], ["MainWindow / Main", "Swing y consola"]];
  const bw = 2.15, gap = 0.34;
  flow.forEach(([h, d], i) => {
    const x = 0.6 + i * (bw + gap);
    card(s, x, 1.6, bw, 1.3, i === 4 ? C.accent2 : C.accent1, "Módulo " + h);
    txt(s, [{ text: h, options: { bold: true, breakLine: true } }, { text: d, options: { fontSize: 13 } }], { x: x + 0.1, y: 1.6, w: bw - 0.2, h: 1.3, fontSize: 15, color: C.background1, align: "center", valign: "middle" });
    if (i < 4) s.addImage({ data: IC.arrow, x: x + bw + 0.05, y: 2.1, w: 0.24, h: 0.3, altText: "flecha", objectName: "Flecha " + i });
  });
  card(s, 3.05, 3.25, 4.6, 1.0, C.background2, "Módulo experimento");
  txt(s, [{ text: "Experiment + Charts", options: { bold: true, color: C.text2, breakLine: true } }, { text: "CV 10 pliegues → CSV, SVG y Markdown", options: { fontSize: 13 } }], { x: 3.15, y: 3.25, w: 4.4, h: 1.0, fontSize: 15, align: "center", valign: "middle" });
  txt(s, "Principios aplicados", { x: 0.6, y: 4.6, w: 6, h: 0.4, fontSize: 18, bold: true, color: C.text2 });
  txt(s, [
    { text: "Separación de responsabilidades: la interfaz solo es una vista", options: { bullet: true, breakLine: true } },
    { text: "Un contrato único de datos (Schema) usado por todos los módulos", options: { bullet: true, breakLine: true } },
    { text: "El .model guarda la cabecera y el hash del ARFF para evitar errores", options: { bullet: true, breakLine: true } },
    { text: "12 pruebas JUnit · mvn clean verify: BUILD SUCCESS", options: { bullet: true } },
  ], { x: 0.6, y: 5.05, w: 7.4, h: 1.8, fontSize: 15, paraSpaceAfter: 4 });
  const chips = ["Java 11+", "Weka 3.8.5", "Maven", "Swing", "ARFF"];
  chips.forEach((c, i) => {
    const x = 8.6 + (i % 2) * 2.1, y = 4.65 + Math.floor(i / 2) * 0.72;
    s.addShape(pres.shapes.ROUNDED_RECTANGLE, { x, y, w: 1.9, h: 0.55, rectRadius: 0.27, fill: { color: C.background2 }, line: { color: C.accent1, width: 1 }, objectName: "Tecnología " + c });
    txt(s, c, { x, y, w: 1.9, h: 0.55, fontSize: 15, bold: true, color: C.accent1, align: "center", valign: "middle" });
  });
  s.addNotes("La aplicación está dividida en módulos con una sola responsabilidad. DataGenerator crea el ARFF; DataIO y Schema lo cargan y validan; TrainingService entrena J48 y Naive Bayes y los guarda en archivos .model; PredictionService los vuelve a cargar y predice; y MainWindow es la interfaz Swing. El experimento es un módulo aparte que reutiliza los mismos algoritmos. Todo se compila con Maven y tiene 12 pruebas automáticas que pasan.");

  // ---------- 7. App ----------
  s = pres.addSlide({ masterName: "CONTENIDO", sectionTitle: "Desarrollo" });
  s.addText("La aplicación funcional", { placeholder: "title" });
  const iw = 6.9, ih = iw * 850 / 1050;
  s.addShape(pres.shapes.RECTANGLE, { x: 0.6, y: 1.4, w: iw, h: ih, fill: { color: C.background1 }, line: { color: C.accent3, width: 0.75 }, shadow: { type: "outer", color: "000000", opacity: 0.18, blur: 8, offset: 3, angle: 90 }, objectName: "Marco captura" });
  s.addImage({ path: SHOT, x: 0.6, y: 1.4, w: iw, h: ih, altText: "Captura de la pestaña de orientación: síntomas marcados y distribución de probabilidad con parvovirosis al 99,25 %", objectName: "Captura app" });
  const appSteps = [
    ["1", "Entrenar", "Lee el ARFF, entrena J48 y Naive Bayes y guarda los .model"],
    ["2", "Marcar síntomas", "15 casillas: marcada = presente, vacía = ausente"],
    ["3", "Calcular", "Carga el modelo del disco y predice"],
    ["4", "Resultado", "Enfermedad sugerida y % de las 8 clases, con avisos"],
  ];
  appSteps.forEach(([n, h, d], i) => {
    const y = 1.45 + i * 1.3;
    s.addShape(pres.shapes.OVAL, { x: 7.9, y, w: 0.7, h: 0.7, fill: { color: i === 3 ? C.accent2 : C.accent1 }, line: { type: "none" }, objectName: "Paso app " + n });
    txt(s, n, { x: 7.9, y, w: 0.7, h: 0.7, fontSize: 22, bold: true, color: C.background1, align: "center", valign: "middle" });
    txt(s, [{ text: h, options: { bold: true, fontSize: 18, color: C.text2, breakLine: true } }, { text: d }], { x: 8.8, y: y - 0.05, w: 3.9, h: 1.15, fontSize: 15 });
  });
  card(s, 7.9, 6.25, 4.8, 0.55, C.text2, "Comando");
  txt(s, "java -jar diagnosticador-canino.jar gui", { x: 7.9, y: 6.25, w: 4.8, h: 0.55, fontSize: 14, color: C.background1, fontFace: "Courier New", align: "center", valign: "middle" });
  s.addNotes("Esta es la aplicación real funcionando. En la primera pestaña se entrena. En la segunda se marcan los síntomas; aquí marqué fiebre, vómito, diarrea, letargo, pérdida de apetito y sangre en heces. Al pulsar Calcular, la app carga el modelo desde el disco y muestra la enfermedad sugerida, parvovirosis con 99 %, y el porcentaje de las ocho clases. Siempre muestra los avisos de que no es un diagnóstico clínico. También funciona por consola. (Si hay tiempo, hacer la demostración en vivo.)");

  // ---------- 8. Diseño experimento ----------
  pres.addSection({ title: "Experimento" });
  s = pres.addSlide({ masterName: "CONTENIDO", sectionTitle: "Experimento" });
  s.addText("Diseño del experimento", { placeholder: "title" });
  const exp = [
    [IC.random, "Barajar", "Semilla 42, reproducible"],
    [IC.layers, "Estratificar", "10 pliegues con 2–3 casos de cada clase"],
    [IC.redo, "Entrenar × 10", "180 casos entrenan, 20 prueban"],
    [IC.flask, "Comparar", "Mismos pliegues para J48 y Naive Bayes"],
    [IC.chart, "Medir", "200 predicciones fuera de muestra por algoritmo"],
  ];
  const ew = 2.2, eg = 0.275;
  s.addShape(pres.shapes.LINE, { x: 0.6 + ew / 2, y: 2.25, w: 4 * (ew + eg), h: 0, line: { color: C.accent5, width: 3 }, objectName: "Línea proceso" });
  exp.forEach(([ic, h, d], i) => {
    const x = 0.6 + i * (ew + eg);
    circleIcon(s, ic, x + (ew - 1.1) / 2, 1.7, 1.1, C.accent1, h);
    txt(s, h, { x, y: 3.0, w: ew, h: 0.45, fontSize: 18, bold: true, color: C.text2, align: "center" });
    txt(s, d, { x, y: 3.45, w: ew, h: 0.9, fontSize: 15, color: C.text1, align: "center" });
  });
  card(s, 0.6, 4.75, 12.1, 1.9, C.background2, "Métricas");
  txt(s, "Métricas calculadas", { x: 0.9, y: 4.9, w: 5, h: 0.4, fontSize: 18, bold: true, color: C.text2 });
  const mets = ["Accuracy", "Precisión por clase", "Recall por clase", "F1 por clase", "Matriz de confusión"];
  mets.forEach((m, i) => {
    const x = 0.9 + i * 2.35;
    s.addShape(pres.shapes.ROUNDED_RECTANGLE, { x, y: 5.5, w: 2.15, h: 0.75, rectRadius: 0.1, fill: { color: C.background1 }, line: { type: "none" }, objectName: "Métrica " + m });
    txt(s, m, { x, y: 5.5, w: 2.15, h: 0.75, fontSize: 15, bold: true, color: C.accent1, align: "center", valign: "middle" });
  });
  s.addNotes("Para comparar de forma justa usé validación cruzada estratificada de 10 pliegues: se barajan los datos con semilla fija, se reparten en 10 grupos con la misma proporción de cada enfermedad, y en cada vuelta se entrena con 180 y se prueba con 20. Los dos algoritmos usan exactamente los mismos pliegues. Así cada caso se predice una vez sin haber sido visto en el entrenamiento. Calculo accuracy, precisión, recall y F1 por clase y la matriz de confusión. Todo lo genera el código Experiment.java.");

  // ---------- 9. Resultados globales ----------
  s = pres.addSlide({ masterName: "CONTENIDO", sectionTitle: "Experimento" });
  s.addText("Resultados: Naive Bayes supera a J48", { placeholder: "title" });
  card(s, 0.6, 1.5, 3.6, 2.4, C.background2, "Stat NB");
  txt(s, "83 %", { x: 0.6, y: 1.6, w: 3.6, h: 1.3, fontSize: 66, bold: true, color: C.accent2, align: "center", valign: "middle", fontFace: "Cambria" });
  txt(s, [{ text: "Naive Bayes", options: { bold: true, breakLine: true } }, { text: "166 de 200 aciertos" }], { x: 0.6, y: 2.9, w: 3.6, h: 0.85, fontSize: 16, color: C.text2, align: "center" });
  card(s, 0.6, 4.15, 3.6, 2.4, C.background2, "Stat J48");
  txt(s, "70 %", { x: 0.6, y: 4.25, w: 3.6, h: 1.3, fontSize: 66, bold: true, color: C.accent1, align: "center", valign: "middle", fontFace: "Cambria" });
  txt(s, [{ text: "J48", options: { bold: true, breakLine: true } }, { text: "140 de 200 aciertos" }], { x: 0.6, y: 5.55, w: 3.6, h: 0.85, fontSize: 16, color: C.text2, align: "center" });
  const lab = ["Accuracy", "Precisión macro", "Recall macro", "F1 macro"];
  s.addChart(pres.charts.BAR, [
    { name: "J48", labels: lab, values: [0.700, 0.701, 0.700, 0.696] },
    { name: "Naive Bayes", labels: lab, values: [0.830, 0.834, 0.830, 0.829] },
  ], {
    x: 4.6, y: 1.4, w: 8.1, h: 5.3, barDir: "col", barGapWidthPct: 60,
    chartColors: [HEX.accent1, HEX.accent2],
    showTitle: true, title: "Métricas globales (CV 10 pliegues)", titleFontSize: 16, titleColor: HEX.dk2, titleFontFace: "+mn-lt",
    showValue: true, dataLabelPosition: "outEnd", dataLabelFormatCode: "0.00", dataLabelFontSize: 12, dataLabelColor: HEX.dk1, dataLabelFontFace: "+mn-lt",
    valAxisMinVal: 0, valAxisMaxVal: 1, valAxisMajorUnit: 0.2, valAxisLabelFormatCode: "0.0",
    catAxisLabelColor: HEX.accent3, valAxisLabelColor: HEX.accent3, catAxisLabelFontSize: 13, valAxisLabelFontSize: 12,
    catAxisLabelFontFace: "+mn-lt", valAxisLabelFontFace: "+mn-lt",
    valGridLine: { color: "DDE5E4", size: 0.75 }, catGridLine: { style: "none" },
    showLegend: true, legendPos: "b", legendFontSize: 13, legendFontFace: "+mn-lt", legendColor: HEX.dk1,
    objectName: "Gráfico métricas globales",
  });
  s.addNotes("El resultado principal: Naive Bayes acierta 166 de 200 casos, un 83 %, y J48 140, un 70 %. La diferencia es de 13 puntos y se mantiene en precisión, recall y F1 macro, que son el promedio entre las ocho clases. Una explicación honesta: el generador produce cada síntoma de forma independiente dada la enfermedad, que es justo lo que asume Naive Bayes, así que el dato le favorece.");

  // ---------- 10. F1 por clase ----------
  s = pres.addSlide({ masterName: "CONTENIDO", sectionTitle: "Experimento" });
  s.addText("F1 por enfermedad", { placeholder: "title" });
  const cls = ["Parvovirosis", "Moquillo", "Rabia", "Otitis", "Parasitosis int.", "Traqueobronquitis", "Dermatitis alérgica", "Leptospirosis"];
  s.addChart(pres.charts.BAR, [
    { name: "J48", labels: cls, values: [0.653, 0.766, 0.792, 0.700, 0.640, 0.833, 0.409, 0.776] },
    { name: "Naive Bayes", labels: cls, values: [0.846, 0.863, 0.923, 0.778, 0.816, 0.800, 0.696, 0.913] },
  ], {
    x: 0.6, y: 1.4, w: 7.9, h: 5.4, barDir: "bar", barGapWidthPct: 50, catAxisOrientation: "maxMin",
    chartColors: [HEX.accent1, HEX.accent2],
    showValue: true, dataLabelPosition: "outEnd", dataLabelFormatCode: "0.00", dataLabelFontSize: 11, dataLabelColor: HEX.dk1, dataLabelFontFace: "+mn-lt",
    valAxisMinVal: 0, valAxisMaxVal: 1, valAxisMajorUnit: 0.2, valAxisLabelFormatCode: "0.0",
    catAxisLabelColor: HEX.dk1, valAxisLabelColor: HEX.accent3, catAxisLabelFontSize: 13, valAxisLabelFontSize: 11,
    catAxisLabelFontFace: "+mn-lt", valAxisLabelFontFace: "+mn-lt",
    valGridLine: { color: "DDE5E4", size: 0.75 }, catGridLine: { style: "none" },
    showLegend: true, legendPos: "t", legendFontSize: 13, legendFontFace: "+mn-lt", legendColor: HEX.dk1,
    objectName: "Gráfico F1 por clase",
  });
  const obs = [
    [C.accent2, "Mejor clase", "Rabia: F1 0,92 con Naive Bayes"],
    [C.accent4, "Peor clase", "Dermatitis alérgica: 0,41 (J48) y 0,70 (NB)"],
    [C.accent1, "No gana en todo", "Traqueobronquitis: J48 0,83 frente a NB 0,80"],
  ];
  obs.forEach(([col, h, d], i) => {
    const y = 1.5 + i * 1.75;
    card(s, 8.9, y, 3.8, 1.5, C.background2, "Observación " + h);
    s.addShape(pres.shapes.OVAL, { x: 9.1, y: y + 0.25, w: 0.3, h: 0.3, fill: { color: col }, line: { type: "none" }, objectName: "Punto " + h });
    txt(s, h, { x: 9.55, y: y + 0.18, w: 3.0, h: 0.45, fontSize: 18, bold: true, color: C.text2 });
    txt(s, d, { x: 9.1, y: y + 0.7, w: 3.45, h: 0.7, fontSize: 15 });
  });
  s.addNotes("Mirando clase por clase, Naive Bayes gana en siete de las ocho enfermedades. La mejor es rabia, con F1 de 0,92. La más difícil es dermatitis alérgica, sobre todo para J48. Y hay una excepción: en traqueobronquitis J48 es ligeramente mejor. Por eso no basta con mirar el promedio.");

  // ---------- 11. Matriz confusión ----------
  s = pres.addSlide({ masterName: "CONTENIDO", sectionTitle: "Experimento" });
  s.addText("Matriz de confusión de Naive Bayes", { placeholder: "title" });
  const ab = ["Parvo", "Moqu", "Rabia", "Otitis", "Paras", "Traq", "Derm", "Lepto"];
  const M = [[22,1,0,0,2,0,0,0],[0,22,1,0,1,1,0,0],[0,0,24,0,0,1,0,0],[0,0,1,21,0,0,3,0],[3,0,1,0,20,0,1,0],[0,3,0,1,0,20,1,0],[0,0,0,7,1,1,16,0],[2,0,0,0,0,2,0,21]];
  const hdr = { bold: true, color: C.background1, fill: { color: C.text2 }, align: "center", valign: "middle", fontSize: 13 };
  const rows = [[{ text: "Real ↓ / Pred →", options: Object.assign({}, hdr, { fontSize: 11 }) }].concat(ab.map((a) => ({ text: a, options: Object.assign({}, hdr) })))];
  M.forEach((r, i) => {
    rows.push([{ text: ab[i], options: Object.assign({}, hdr) }].concat(r.map((v, j) => ({
      text: String(v),
      options: {
        align: "center", valign: "middle", fontSize: 14, bold: i === j || (i === 6 && j === 3),
        color: i === j ? C.background1 : (i === 6 && j === 3) ? C.background1 : v ? C.text1 : C.accent3,
        fill: { color: i === j ? C.accent1 : (i === 6 && j === 3) ? C.accent4 : v ? C.accent5 : C.background1 },
      },
    }))));
  });
  s.addTable(rows, { x: 0.6, y: 1.45, w: 7.6, colW: [1.52].concat(Array(8).fill(0.76)), rowH: 0.56, border: { type: "solid", pt: 0.75, color: "FFFFFF" }, fontFace: "Calibri", objectName: "Matriz de confusión" });
  txt(s, "Diagonal = aciertos (166/200). Filas: enfermedad real; columnas: predicha.", { x: 0.6, y: 6.6, w: 7.6, h: 0.3, fontSize: 12, color: C.accent3 });
  card(s, 8.6, 1.45, 4.1, 2.45, C.background2, "Confusión derm-otitis");
  txt(s, [{ text: "Dermatitis → otitis", options: { bold: true, fontSize: 18, color: C.accent4, breakLine: true } }, { text: "7 de 25 casos de dermatitis se predicen como otitis (J48: 10). Comparten prurito y molestia de oído." }], { x: 8.85, y: 1.6, w: 3.6, h: 2.2, fontSize: 16, valign: "middle" });
  card(s, 8.6, 4.15, 4.1, 2.45, C.background2, "Rabia");
  txt(s, [{ text: "Falsos negativos de rabia", options: { bold: true, fontSize: 18, color: C.text2, breakLine: true } }, { text: "Naive Bayes deja escapar 1 de 25; J48, 4. Aun así, el modelo no sirve para descartar rabia." }], { x: 8.85, y: 4.3, w: 3.6, h: 2.2, fontSize: 16, valign: "middle" });
  s.addNotes("En la matriz, la diagonal verde son los aciertos. El error más frecuente, en rojo, es confundir dermatitis alérgica con otitis: siete casos, porque ambas comparten picazón y molestia de oído. En J48 eran diez. En rabia, Naive Bayes solo falla un caso y J48 cuatro. Incluso con un 96 % de recall, esta herramienta no se puede usar para descartar rabia.");

  // ---------- 12. Limitaciones ----------
  pres.addSection({ title: "Cierre" });
  s = pres.addSlide({ masterName: "CONTENIDO", sectionTitle: "Cierre" });
  s.addText("Limitaciones declaradas", { placeholder: "title" });
  const lim = [
    ["Datos sintéticos", "Los perfiles de síntomas son supuestos didácticos, no prevalencias reales."],
    ["Muestra pequeña", "25 casos por clase; hay 37 vectores de síntomas repetidos."],
    ["Sin validación clínica", "Ni datos reales, ni revisión veterinaria, ni estudio prospectivo."],
    ["Alcance cerrado", "Solo 8 enfermedades; no existe la clase «sano». Casilla vacía = ausente."],
  ];
  lim.forEach(([h, d], i) => {
    const x = 0.6 + (i % 2) * 6.2, y = 1.5 + Math.floor(i / 2) * 2.6;
    card(s, x, y, 5.9, 2.3, C.background2, "Limitación " + h);
    circleIcon(s, IC.warn, x + 0.3, y + 0.67, 0.95, C.accent4, h);
    txt(s, [{ text: h, options: { bold: true, fontSize: 20, color: C.text2, breakLine: true } }, { text: d }], { x: x + 1.5, y: y + 0.25, w: 4.15, h: 1.8, fontSize: 17, valign: "middle" });
  });
  s.addNotes("Es importante ser explícito con las limitaciones. Los datos son sintéticos, la muestra es pequeña, no hay ningún tipo de validación clínica y el sistema solo conoce ocho enfermedades: no sabe decir que un perro está sano. Además, una casilla sin marcar se interpreta como síntoma ausente, no como desconocido. Por todo esto, las cifras no son un rendimiento clínico.");

  // ---------- 13. Conclusiones ----------
  s = pres.addSlide({ masterName: "CIERRE", sectionTitle: "Cierre" });
  s.addText("Conclusiones y trabajo futuro", { placeholder: "title" });
  const colw = 5.9;
  [["Conclusiones", IC.check, C.accent1, [
    "Sistema completo y reproducible: datos, entrenamiento, experimento e interfaz",
    "Mejor método: Naive Bayes (83 % accuracy, F1 macro 0,83)",
    "J48 se mantiene por ser interpretable y mejor en traqueobronquitis",
  ]], ["Trabajo futuro", IC.road, C.accent2, [
    "Recoger datos clínicos reales con revisión veterinaria",
    "Añadir la clase «sano» y la opción «síntoma desconocido»",
    "Probar más algoritmos (Random Forest, SVM) y calibrar probabilidades",
  ]]].forEach(([h, ic, col, items], i) => {
    const x = 0.6 + i * 6.2;
    circleIcon(s, ic, x, 1.6, 0.9, col, h);
    txt(s, h, { x: x + 1.1, y: 1.6, w: colw - 1.1, h: 0.9, fontSize: 24, bold: true, color: C.background1, valign: "middle", fontFace: "Cambria" });
    txt(s, items.map((t, k) => ({ text: t, options: { bullet: true, breakLine: k < items.length - 1 } })), { x, y: 2.8, w: colw, h: 2.9, fontSize: 18, color: C.background1, paraSpaceAfter: 12 });
  });
  txt(s, "¿Preguntas?", { x: 0.6, y: 6.05, w: 12.1, h: 0.8, fontSize: 32, bold: true, color: C.accent5, align: "center", valign: "middle", fontFace: "Cambria" });
  s.addNotes("Para concluir: construí un sistema completo y reproducible, desde la generación de datos hasta la interfaz. En este experimento el mejor método es Naive Bayes, con 83 % de acierto, aunque recomiendo conservar J48 porque se puede leer como reglas. Como trabajo futuro, lo esencial es conseguir datos reales con revisión veterinaria, añadir la clase sano y la opción de síntoma desconocido, y probar otros algoritmos. Muchas gracias, ¿preguntas?");

  await pres.writeFile({ fileName: OUT });
  await applyTheme(OUT, THEME);
  console.log("OK", OUT);
})().catch((e) => { console.error(e); process.exit(1); });
