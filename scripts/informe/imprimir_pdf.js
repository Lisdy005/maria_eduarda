// Imprime la portada (sin pie) y el cuerpo del informe (con número de página) a PDF con Chromium.
//   node scripts/informe/imprimir_pdf.js portada.html cuerpo.html carpeta_salida
const path = require("path");
const { chromium } = require("playwright");

(async () => {
  const [cover, body, out] = process.argv.slice(2);
  const browser = await chromium.launch();
  const page = await browser.newPage();
  await page.goto("file://" + path.resolve(cover), { waitUntil: "networkidle" });
  await page.pdf({ path: path.join(out, "portada.pdf"), format: "A4", printBackground: true, preferCSSPageSize: true });
  await page.goto("file://" + path.resolve(body), { waitUntil: "networkidle" });
  await page.pdf({
    path: path.join(out, "cuerpo.pdf"), format: "A4", printBackground: true, preferCSSPageSize: true,
    displayHeaderFooter: true, headerTemplate: "<span></span>",
    footerTemplate: '<div style="width:100%;font-family:Carlito,Calibri,sans-serif;font-size:8pt;color:#5F7176;padding:0 21mm;display:flex;justify-content:space-between">'
      + "<span>Diagnosticador canino · datos sintéticos, sin validación clínica</span><span class=\"pageNumber\"></span></div>",
  });
  await browser.close();
})().catch((e) => { console.error(e); process.exit(1); });
