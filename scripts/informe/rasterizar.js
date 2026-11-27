// Convierte las gráficas SVG de resultados/ y el diagrama Mermaid del informe a PNG (2x),
// para incrustarlos en INFORME.docx e INFORME.pdf. Usa el Chromium de Playwright.
//
//   node scripts/informe/rasterizar.js [ruta/a/mermaid.min.js]
//
// Sin la ruta de Mermaid se conserva docs/img/arquitectura.png ya generado.
const path = require("path");
const fs = require("fs");
const { chromium } = require("playwright");

const root = path.resolve(__dirname, "..", "..");
const outDir = path.join(root, "docs", "img", "graficas");
const charts = ["comparacion", "clases_J48", "confusion_J48", "clases_NaiveBayes", "confusion_NaiveBayes"];

(async () => {
  fs.mkdirSync(outDir, { recursive: true });
  const browser = await chromium.launch();
  const page = await browser.newPage({ deviceScaleFactor: 2 });
  for (const name of charts) {
    await page.goto("file://" + path.join(root, "resultados", name + ".svg"));
    const box = await page.locator("svg").boundingBox();
    await page.setViewportSize({ width: Math.ceil(box.width), height: Math.ceil(box.height) });
    await page.locator("svg").screenshot({ path: path.join(outDir, name + ".png") });
    console.log("PNG", name);
  }
  const mermaidJs = process.argv[2];
  if (mermaidJs) {
    const md = fs.readFileSync(path.join(root, "docs", "INFORME.md"), "utf8");
    const code = md.match(/```mermaid\n([\s\S]*?)```/)[1];
    await page.goto("about:blank");
    await page.setViewportSize({ width: 1400, height: 1400 });
    await page.setContent('<!doctype html><html><body style="margin:0;background:#fff"><div id="d"></div></body></html>');
    await page.addScriptTag({ path: mermaidJs });
    await page.evaluate(async (src) => {
      window.mermaid.initialize({
        startOnLoad: false, theme: "base", flowchart: { curve: "basis", padding: 14, wrappingWidth: 320 },
        themeVariables: {
          fontFamily: "Carlito, Calibri, Arial, sans-serif", fontSize: "15px",
          primaryColor: "#E3EFED", primaryBorderColor: "#1F7A72", primaryTextColor: "#0E3B43",
          lineColor: "#5F7176", secondaryColor: "#FDF1E4", tertiaryColor: "#FFFFFF",
        },
      });
      const { svg } = await window.mermaid.render("arq", src);
      document.getElementById("d").innerHTML = svg;
    }, code);
    await page.locator("#d svg").screenshot({ path: path.join(root, "docs", "img", "arquitectura.png") });
    console.log("PNG arquitectura");
  }
  await browser.close();
})().catch((e) => { console.error(e); process.exit(1); });
