#!/usr/bin/env python3
"""Exporta docs/INFORME.md a docs/INFORME.docx (Word) y docs/INFORME.pdf.

Requisitos: pandoc 3, Node con Playwright (Chromium) y pypdf. Antes, genere las imágenes:

    node scripts/informe/rasterizar.js [ruta/a/mermaid.min.js]
    python3 scripts/informe/exportar_informe.py

El Markdown sigue siendo la fuente: este script no cambia su contenido, solo lo maqueta
(portada, índice, figuras numeradas, tablas con nombres legibles, ecuaciones y paginación).
"""
import html
import re
import shutil
import subprocess
import sys
import tempfile
import zipfile
from pathlib import Path

ROOT = Path(__file__).resolve().parents[2]
DOCS = ROOT / "docs"
HERE = Path(__file__).resolve().parent

IDS = ["parvovirosis", "moquillo", "rabia", "otitis", "parasitosis_intestinal",
       "traqueobronquitis_infecciosa", "dermatitis_alergica", "leptospirosis"]
LABELS = ["Parvovirosis", "Moquillo", "Rabia", "Otitis", "Parasitosis intestinal",
          "Traqueobronquitis infecciosa", "Dermatitis alérgica", "Leptospirosis"]
SHORT = ["Parvo.", "Moqu.", "Rabia", "Otitis", "Paras.", "Traq.", "Derm.", "Lepto."]

# Figuras en orden de aparición: archivo original -> (PNG a incrustar, ancho, pie de figura).
FIGURES = {
    "mermaid": ("img/arquitectura.png", "62%", "Diagrama de componentes: flujo desde la generación de datos hasta la predicción."),
    "img/gui_2_orientacion.png": ("img/gui_2_orientacion.png", "100%", "Interfaz Swing (FlatLaf) en modo diagnosticar, capturada durante la prueba automatizada. Datos sintéticos; no es un diagnóstico clínico."),
    "../resultados/comparacion.svg": ("img/graficas/comparacion.png", "100%", "Comparación global de J48 y Naive Bayes (validación cruzada estratificada de 10 pliegues)."),
    "../resultados/clases_J48.svg": ("img/graficas/clases_J48.png", "78%", "J48: precisión, recall y F1 por enfermedad."),
    "../resultados/confusion_J48.svg": ("img/graficas/confusion_J48.png", "60%", "J48: matriz de confusión (filas = clase real, columnas = clase predicha)."),
    "../resultados/clases_NaiveBayes.svg": ("img/graficas/clases_NaiveBayes.png", "78%", "Naive Bayes: precisión, recall y F1 por enfermedad."),
    "../resultados/confusion_NaiveBayes.svg": ("img/graficas/confusion_NaiveBayes.png", "60%", "Naive Bayes: matriz de confusión (filas = clase real, columnas = clase predicha)."),
}


def run(cmd, **kw):
    print("+", " ".join(str(c) for c in cmd))
    subprocess.run(cmd, check=True, **kw)


# --------------------------------------------------------------------------- Markdown
def split_cover(md):
    """Devuelve (campos de portada, nota de naturaleza, cuerpo desde la Introducción)."""
    start = md.index("## 1. Portada")
    intro = md.index("## 2. Introducción")
    cover = md[start:intro]
    fields = re.findall(r"^\*\*(.+?):\*\*\s*(.+?)\s*$", cover, re.M)
    note = re.search(r"^\*\*Naturaleza del trabajo:\*\*(.+)$", cover, re.M).group(1).strip()
    fields = [(k, v) for k, v in fields if k != "Naturaleza del trabajo"]
    return fields, note, md[intro:]


def readable_tables(body):
    out = []
    for line in body.split("\n"):
        if line.startswith("|Real / predicha|"):
            line = "|Real \\\\ predicha|" + "|".join(SHORT) + "|"
        elif line.startswith("|"):
            for i, ident in enumerate(IDS):
                line = re.sub(r"(?<=\|)" + ident + r"(?=\|)", LABELS[i], line)
            line = line.replace("|NaiveBayes|", "|Naive Bayes|")
        out.append(line)
    text = "\n".join(out)
    legend = "Abreviaturas de columnas: " + "; ".join(f"{s} = {l}" for s, l in zip(SHORT, LABELS)) + "."
    return text.replace("\nMatriz: filas reales, columnas predichas, orden de clases igual al ARFF.\n",
                        "\nMatriz: filas reales, columnas predichas, orden de clases igual al ARFF. " + legend + "\n")


def number_figures(body):
    counter = [0]
    numbers = {}

    def figure(key):
        counter[0] += 1
        png, width, caption = FIGURES[key]
        numbers[key] = counter[0]
        return f"![Figura {counter[0]}. {caption}]({png}){{width={width}}}"

    body = re.sub(r"```mermaid\n.*?```", lambda m: figure("mermaid"), body, flags=re.S)
    body = re.sub(r"!\[[^\]]*\]\(([^)]+)\)", lambda m: figure(m.group(1)) if m.group(1) in FIGURES else m.group(0), body)
    # El pie en cursiva del Markdown queda sustituido por el pie de figura numerado.
    body = re.sub(r"\n\*Figura 1\. Interfaz Swing[^\n]*\*\n", "\n", body)
    gui = numbers["img/gui_2_orientacion.png"]
    body = body.replace("La Figura 1 muestra la captura obtenida", f"La Figura {gui} muestra la captura obtenida")
    return body


def fit_math(body):
    """Divide en dos líneas la ecuación del conjunto de clases, demasiado ancha para una página."""
    old = ("X\\in\\{0,1\\}^{200\\times15},\\quad Y\\in\\mathcal C^{200},\\quad\n"
           "\\mathcal C=\\{\\text{parvovirosis, moquillo, rabia, otitis, parasitosis intestinal, traqueobronquitis infecciosa, dermatitis alérgica, leptospirosis}\\}.\n\\]")
    new = ("X\\in\\{0,1\\}^{200\\times15},\\quad Y\\in\\mathcal C^{200},\n\\]\n\n\\[\n"
           "\\mathcal C=\\{\\text{parvovirosis, moquillo, rabia, otitis, parasitosis intestinal,}\n\\]\n\n\\[\n"
           "\\text{traqueobronquitis infecciosa, dermatitis alérgica, leptospirosis}\\}.\n\\]")
    if old not in body:
        sys.exit("No se encontró la ecuación del conjunto de clases; revise fit_math().")
    return body.replace(old, new)


def prepare():
    md = (DOCS / "INFORME.md").read_text(encoding="utf-8")
    fields, note, body = split_cover(md)
    body = number_figures(readable_tables(fit_math(body)))
    body = body.replace("\n#### NaiveBayes\n", "\n#### Naive Bayes\n")
    return fields, note, body


# --------------------------------------------------------------------------- Word
STYLES = {
    "Normal": '<w:style w:type="paragraph" w:default="1" w:styleId="Normal"><w:name w:val="Normal"/><w:qFormat/>'
              '<w:pPr><w:spacing w:after="120" w:line="276" w:lineRule="auto"/></w:pPr>'
              '<w:rPr><w:rFonts w:ascii="Calibri" w:hAnsi="Calibri" w:cs="Calibri"/><w:color w:val="1B2A2F"/><w:sz w:val="22"/><w:szCs w:val="22"/><w:lang w:val="es-ES"/></w:rPr></w:style>',
    "BodyText": '<w:style w:type="paragraph" w:styleId="BodyText"><w:name w:val="Body Text"/><w:basedOn w:val="Normal"/><w:link w:val="BodyTextChar"/><w:qFormat/>'
                '<w:pPr><w:spacing w:before="0" w:after="140"/><w:jc w:val="both"/></w:pPr></w:style>',
    "FirstParagraph": '<w:style w:type="paragraph" w:styleId="FirstParagraph"><w:name w:val="First Paragraph"/><w:basedOn w:val="BodyText"/><w:next w:val="BodyText"/><w:qFormat/></w:style>',
    "Compact": '<w:style w:type="paragraph" w:customStyle="1" w:styleId="Compact"><w:name w:val="Compact"/><w:basedOn w:val="BodyText"/><w:qFormat/>'
               '<w:pPr><w:spacing w:before="40" w:after="40" w:line="240" w:lineRule="auto"/><w:jc w:val="left"/></w:pPr><w:rPr><w:sz w:val="19"/><w:szCs w:val="19"/></w:rPr></w:style>',
    "Title": '<w:style w:type="paragraph" w:styleId="Title"><w:name w:val="Title"/><w:basedOn w:val="Normal"/><w:next w:val="BodyText"/><w:qFormat/>'
             '<w:pPr><w:spacing w:before="120" w:after="240" w:line="240" w:lineRule="auto"/></w:pPr>'
             '<w:rPr><w:rFonts w:ascii="Cambria" w:hAnsi="Cambria"/><w:b/><w:color w:val="0E3B43"/><w:sz w:val="60"/><w:szCs w:val="60"/></w:rPr></w:style>',
    "Subtitle": '<w:style w:type="paragraph" w:styleId="Subtitle"><w:name w:val="Subtitle"/><w:basedOn w:val="Normal"/><w:next w:val="BodyText"/><w:qFormat/>'
                '<w:pPr><w:spacing w:after="720"/></w:pPr><w:rPr><w:color w:val="1F7A72"/><w:sz w:val="30"/><w:szCs w:val="30"/></w:rPr></w:style>',
    "Heading1": '<w:style w:type="paragraph" w:styleId="Heading1"><w:name w:val="heading 1"/><w:basedOn w:val="Normal"/><w:next w:val="BodyText"/><w:uiPriority w:val="9"/><w:qFormat/>'
                '<w:pPr><w:keepNext/><w:keepLines/><w:pageBreakBefore/><w:spacing w:before="0" w:after="240"/><w:outlineLvl w:val="0"/></w:pPr>'
                '<w:rPr><w:rFonts w:ascii="Cambria" w:hAnsi="Cambria"/><w:b/><w:bCs/><w:color w:val="0E3B43"/><w:sz w:val="40"/><w:szCs w:val="40"/></w:rPr></w:style>',
    "Heading2": '<w:style w:type="paragraph" w:styleId="Heading2"><w:name w:val="heading 2"/><w:basedOn w:val="Normal"/><w:next w:val="BodyText"/><w:uiPriority w:val="9"/><w:unhideWhenUsed/><w:qFormat/>'
                '<w:pPr><w:keepNext/><w:keepLines/><w:spacing w:before="360" w:after="120"/><w:outlineLvl w:val="1"/></w:pPr>'
                '<w:rPr><w:rFonts w:ascii="Cambria" w:hAnsi="Cambria"/><w:b/><w:bCs/><w:color w:val="1F7A72"/><w:sz w:val="28"/><w:szCs w:val="28"/></w:rPr></w:style>',
    "Heading3": '<w:style w:type="paragraph" w:styleId="Heading3"><w:name w:val="heading 3"/><w:basedOn w:val="Normal"/><w:next w:val="BodyText"/><w:uiPriority w:val="9"/><w:unhideWhenUsed/><w:qFormat/>'
                '<w:pPr><w:keepNext/><w:keepLines/><w:spacing w:before="280" w:after="100"/><w:outlineLvl w:val="2"/></w:pPr>'
                '<w:rPr><w:b/><w:bCs/><w:color w:val="0E3B43"/><w:sz w:val="24"/><w:szCs w:val="24"/></w:rPr></w:style>',
    "Caption": '<w:style w:type="paragraph" w:styleId="Caption"><w:name w:val="caption"/><w:basedOn w:val="Normal"/><w:link w:val="BodyTextChar"/>'
               '<w:pPr><w:spacing w:before="80" w:after="280"/><w:jc w:val="center"/></w:pPr><w:rPr><w:i/><w:color w:val="5F7176"/><w:sz w:val="19"/><w:szCs w:val="19"/></w:rPr></w:style>',
    "Figure": '<w:style w:type="paragraph" w:customStyle="1" w:styleId="Figure"><w:name w:val="Figure"/><w:basedOn w:val="Normal"/>'
              '<w:pPr><w:keepNext/><w:spacing w:before="200" w:after="0"/><w:jc w:val="center"/></w:pPr></w:style>',
    "CaptionedFigure": '<w:style w:type="paragraph" w:customStyle="1" w:styleId="CaptionedFigure"><w:name w:val="Captioned Figure"/><w:basedOn w:val="Figure"/><w:pPr><w:keepNext/></w:pPr></w:style>',
    "TOCHeading": '<w:style w:type="paragraph" w:styleId="TOCHeading"><w:name w:val="TOC Heading"/><w:basedOn w:val="Normal"/><w:next w:val="BodyText"/><w:uiPriority w:val="39"/><w:unhideWhenUsed/><w:qFormat/>'
                  '<w:pPr><w:spacing w:before="0" w:after="240"/></w:pPr><w:rPr><w:rFonts w:ascii="Cambria" w:hAnsi="Cambria"/><w:b/><w:color w:val="0E3B43"/><w:sz w:val="40"/><w:szCs w:val="40"/></w:rPr></w:style>',
    "Table": '<w:style w:type="table" w:default="1" w:styleId="Table"><w:name w:val="Table"/><w:basedOn w:val="TableNormal"/><w:qFormat/>'
             '<w:pPr><w:spacing w:after="0"/></w:pPr>'
             '<w:tblPr><w:tblInd w:w="0" w:type="dxa"/><w:tblBorders><w:top w:val="single" w:sz="4" w:color="C9D3D1"/><w:bottom w:val="single" w:sz="4" w:color="C9D3D1"/>'
             '<w:insideH w:val="single" w:sz="4" w:color="E3E9E8"/></w:tblBorders>'
             '<w:tblCellMar><w:top w:w="40" w:type="dxa"/><w:left w:w="100" w:type="dxa"/><w:bottom w:w="40" w:type="dxa"/><w:right w:w="100" w:type="dxa"/></w:tblCellMar></w:tblPr>'
             '<w:tblStylePr w:type="firstRow"><w:rPr><w:b/><w:color w:val="FFFFFF"/></w:rPr><w:tcPr><w:shd w:val="clear" w:color="auto" w:fill="0E3B43"/><w:vAlign w:val="bottom"/></w:tcPr></w:tblStylePr></w:style>',
    "AbstractTitle": '<w:style w:type="paragraph" w:customStyle="1" w:styleId="AbstractTitle"><w:name w:val="Abstract Title"/><w:basedOn w:val="Normal"/><w:next w:val="Abstract"/><w:qFormat/>'
                     '<w:pPr><w:keepNext/><w:keepLines/><w:spacing w:before="300" w:after="0"/><w:jc w:val="center"/></w:pPr><w:rPr><w:b/><w:color w:val="345A8A"/><w:sz w:val="20"/><w:szCs w:val="20"/></w:rPr></w:style>',
    "PortadaEtiqueta": '<w:style w:type="paragraph" w:customStyle="1" w:styleId="PortadaEtiqueta"><w:name w:val="Portada Etiqueta"/><w:basedOn w:val="Normal"/>'
                       '<w:pPr><w:spacing w:before="1800" w:after="120"/></w:pPr><w:rPr><w:b/><w:caps/><w:color w:val="1F7A72"/><w:spacing w:val="20"/><w:sz w:val="20"/></w:rPr></w:style>',
    "PortadaDato": '<w:style w:type="paragraph" w:customStyle="1" w:styleId="PortadaDato"><w:name w:val="Portada Dato"/><w:basedOn w:val="Normal"/>'
                   '<w:pPr><w:spacing w:after="80"/></w:pPr><w:rPr><w:sz w:val="23"/></w:rPr></w:style>',
    "PortadaNota": '<w:style w:type="paragraph" w:customStyle="1" w:styleId="PortadaNota"><w:name w:val="Portada Nota"/><w:basedOn w:val="Normal"/>'
                   '<w:pPr><w:pBdr><w:top w:val="single" w:sz="2" w:space="8" w:color="FDF1E4"/><w:left w:val="single" w:sz="2" w:space="8" w:color="FDF1E4"/>'
                   '<w:bottom w:val="single" w:sz="2" w:space="8" w:color="FDF1E4"/><w:right w:val="single" w:sz="2" w:space="8" w:color="FDF1E4"/></w:pBdr>'
                   '<w:shd w:val="clear" w:color="auto" w:fill="FDF1E4"/><w:spacing w:before="720" w:after="0"/><w:ind w:left="170" w:right="170"/></w:pPr>'
                   '<w:rPr><w:color w:val="1B2A2F"/><w:sz w:val="20"/></w:rPr></w:style>',
}


def reference_docx(tmp):
    ref = tmp / "reference.docx"
    run(["pandoc", "-o", str(ref), "--print-default-data-file", "reference.docx"])
    with zipfile.ZipFile(ref) as z:
        parts = {n: z.read(n) for n in z.namelist()}
    styles = parts["word/styles.xml"].decode("utf-8")
    for sid, xml in STYLES.items():
        pattern = re.compile(r'<w:style [^>]*w:styleId="' + sid + r'"[^>]*>.*?</w:style>', re.S)
        styles, count = pattern.subn(lambda m: xml, styles, count=1)
        if count == 0:
            styles = styles.replace("</w:styles>", xml + "</w:styles>")
    styles = styles.replace('<w:lang w:val="en-US" w:eastAsia="en-US" w:bidi="ar-SA" />', '<w:lang w:val="es-ES" w:eastAsia="en-US" w:bidi="ar-SA" />')
    parts["word/styles.xml"] = styles.encode("utf-8")
    with zipfile.ZipFile(ref, "w", zipfile.ZIP_DEFLATED) as z:
        for n, data in parts.items():
            z.writestr(n, data)
    return ref


PAGE_BREAK = '```{=openxml}\n<w:p><w:r><w:br w:type="page"/></w:r></w:p>\n```'
TOC_FIELD = ('```{=openxml}\n<w:p><w:pPr><w:pStyle w:val="TOCHeading"/></w:pPr><w:r><w:t>Índice</w:t></w:r></w:p>'
             '<w:p><w:r><w:fldChar w:fldCharType="begin" w:dirty="true"/></w:r>'
             '<w:r><w:instrText xml:space="preserve"> TOC \\o "1-2" \\h \\z \\u </w:instrText></w:r>'
             '<w:r><w:fldChar w:fldCharType="separate"/></w:r>'
             '<w:r><w:rPr><w:i/><w:color w:val="5F7176"/></w:rPr><w:t>El índice se genera al abrir el documento en Word (si no aparece: clic derecho → Actualizar campo).</w:t></w:r>'
             '<w:r><w:fldChar w:fldCharType="end"/></w:r></w:p>\n```')
FOOTER = ('<?xml version="1.0" encoding="UTF-8" standalone="yes"?>'
          '<w:ftr xmlns:w="http://schemas.openxmlformats.org/wordprocessingml/2006/main" xmlns:r="http://schemas.openxmlformats.org/officeDocument/2006/relationships">'
          '<w:p><w:pPr><w:tabs><w:tab w:val="right" w:pos="9638"/></w:tabs><w:spacing w:after="0"/></w:pPr>'
          '<w:r><w:rPr><w:color w:val="5F7176"/><w:sz w:val="17"/></w:rPr><w:t xml:space="preserve">Diagnosticador canino · datos sintéticos, sin validación clínica</w:t></w:r>'
          '<w:r><w:rPr><w:color w:val="5F7176"/><w:sz w:val="17"/></w:rPr><w:tab/></w:r>'
          '<w:r><w:rPr><w:color w:val="5F7176"/><w:sz w:val="17"/></w:rPr><w:fldChar w:fldCharType="begin"/></w:r>'
          '<w:r><w:rPr><w:color w:val="5F7176"/><w:sz w:val="17"/></w:rPr><w:instrText xml:space="preserve"> PAGE </w:instrText></w:r>'
          '<w:r><w:rPr><w:color w:val="5F7176"/><w:sz w:val="17"/></w:rPr><w:fldChar w:fldCharType="separate"/></w:r>'
          '<w:r><w:rPr><w:color w:val="5F7176"/><w:sz w:val="17"/></w:rPr><w:t>1</w:t></w:r>'
          '<w:r><w:rPr><w:color w:val="5F7176"/><w:sz w:val="17"/></w:rPr><w:fldChar w:fldCharType="end"/></w:r></w:p></w:ftr>')
SECT = ('<w:sectPr><w:footerReference w:type="default" r:id="rIdPieInforme"/><w:pgSz w:w="11906" w:h="16838"/>'
        '<w:pgMar w:top="1418" w:right="1304" w:bottom="1418" w:left="1304" w:header="709" w:footer="567" w:gutter="0"/>'
        '<w:titlePg/></w:sectPr>')


TEXT_WIDTH = 11906 - 2 * 1304  # A4 menos márgenes, en DXA
HEADER_RUN = '<w:r><w:rPr><w:b/><w:color w:val="FFFFFF"/></w:rPr>'


def format_table(tbl):
    """Anchos de columna proporcionales al contenido, a todo el ancho, y cabecera con fondo oscuro."""
    rows = re.findall(r"<w:tr\b.*?</w:tr>", tbl, flags=re.S)
    if not rows:
        return tbl
    cols = len(re.findall(r"<w:tc>", rows[0]))
    weights = [4] * cols
    for row in rows:
        for i, cell in enumerate(re.findall(r"<w:tc>.*?</w:tc>", row, flags=re.S)[:cols]):
            text = "".join(re.findall(r"<w:t[^>]*>([^<]*)</w:t>", cell))
            longest_word = max((len(w) for w in text.split()), default=0)
            weights[i] = max(weights[i], min(len(text), 34), longest_word + 2)
    widths = [int(TEXT_WIDTH * w / sum(weights)) for w in weights]
    tbl = re.sub(r"<w:tblGrid>.*?</w:tblGrid>", "<w:tblGrid>" + "".join(f'<w:gridCol w:w="{w}"/>' for w in widths) + "</w:tblGrid>", tbl, flags=re.S)
    tbl = re.sub(r'<w:tblW [^>]*/>', f'<w:tblW w:type="dxa" w:w="{TEXT_WIDTH}"/>', tbl)

    def fix_row(row, header):
        cells = re.findall(r"<w:tc>.*?</w:tc>", row, flags=re.S)
        for i, cell in enumerate(cells[:cols]):
            pr = f'<w:tcPr><w:tcW w:w="{widths[i]}" w:type="dxa"/>' + ('<w:shd w:val="clear" w:color="auto" w:fill="0E3B43"/>' if header else "") + "</w:tcPr>"
            new = re.sub(r"<w:tcPr\s*/>|<w:tcPr>.*?</w:tcPr>", pr, cell, count=1, flags=re.S)
            if header:
                new = new.replace("<w:r>", HEADER_RUN)
            row = row.replace(cell, new, 1)
        return row

    for k, row in enumerate(rows):
        tbl = tbl.replace(row, fix_row(row, k == 0), 1)
    return tbl


# Orden de los hijos de w:settings según el esquema OOXML (CT_Settings). pandoc los escribe
# desordenados; Word lo tolera, pero así el archivo valida contra el esquema.
SETTINGS_ORDER = ["writeProtection", "view", "zoom", "removePersonalInformation", "removeDateAndTime",
    "doNotDisplayPageBoundaries", "displayBackgroundShape", "printPostScriptOverText", "printFractionalCharacterWidth",
    "printFormsData", "embedTrueTypeFonts", "embedSystemFonts", "saveSubsetFonts", "saveFormsData", "mirrorMargins",
    "alignBordersAndEdges", "bordersDoNotSurroundHeader", "bordersDoNotSurroundFooter", "gutterAtTop", "hideSpellingErrors",
    "hideGrammaticalErrors", "activeWritingStyle", "proofState", "formsDesign", "attachedTemplate", "linkStyles",
    "stylePaneFormatFilter", "stylePaneSortMethod", "documentType", "mailMerge", "revisionView", "trackRevisions",
    "doNotTrackMoves", "doNotTrackFormatting", "documentProtection", "autoFormatOverride", "styleLockTheme",
    "styleLockQFSet", "defaultTabStop", "autoHyphenation", "consecutiveHyphenLimit", "hyphenationZone",
    "doNotHyphenateCaps", "showEnvelope", "summaryLength", "clickAndTypeStyle", "defaultTableStyle", "evenAndOddHeaders",
    "bookFoldRevPrinting", "bookFoldPrinting", "bookFoldPrintingSheets", "drawingGridHorizontalSpacing",
    "drawingGridVerticalSpacing", "displayHorizontalDrawingGridEvery", "displayVerticalDrawingGridEvery",
    "doNotUseMarginsForDrawingGridOrigin", "drawingGridHorizontalOrigin", "drawingGridVerticalOrigin",
    "doNotShadeFormData", "noPunctuationKerning", "characterSpacingControl", "printTwoOnOne", "strictFirstAndLastChars",
    "noLineBreaksAfter", "noLineBreaksBefore", "savePreviewPicture", "doNotValidateAgainstSchema", "saveInvalidXml",
    "ignoreMixedContent", "alwaysShowPlaceholderText", "doNotDemarcateInvalidXml", "saveXmlDataOnly", "useXSLTWhenSaving",
    "saveThroughXslt", "showXMLTags", "alwaysMergeEmptyNamespace", "updateFields", "hdrShapeDefaults", "footnotePr",
    "endnotePr", "compat", "docVars", "rsids", "mathPr", "attachedSchema", "themeFontLang", "clrSchemeMapping",
    "doNotIncludeSubdocsInStats", "doNotAutoCompressPictures", "forceUpgrade", "captions", "readModeInkLockDown",
    "smartTagType", "schemaLibrary", "shapeDefaults", "doNotEmbedSmartTags", "decimalSymbol", "listSeparator"]


def ordered_settings(data):
    from lxml import etree
    w = "http://schemas.openxmlformats.org/wordprocessingml/2006/main"
    root = etree.fromstring(data)
    if root.find(f"{{{w}}}updateFields") is None:
        update = etree.SubElement(root, f"{{{w}}}updateFields")
        update.set(f"{{{w}}}val", "true")
    rank = {name: i for i, name in enumerate(SETTINGS_ORDER)}
    children = list(root)
    children.sort(key=lambda e: rank.get(etree.QName(e).localname, len(rank)))
    for child in children:
        root.remove(child)
        root.append(child)
    return etree.tostring(root, xml_declaration=True, encoding="UTF-8", standalone=True)


def finish_docx(path):
    """Añade pie con número de página (sin número en la portada), A4 y márgenes; quita entradas de carpeta."""
    with zipfile.ZipFile(path) as z:
        parts = {n: z.read(n) for n in z.namelist() if not n.endswith("/")}
    doc = parts["word/document.xml"].decode("utf-8")
    doc = re.sub(r"<w:tbl>.*?</w:tbl>", lambda m: format_table(m.group(0)), doc, flags=re.S)
    # Correcciones de orden que pandoc escribe fuera de esquema (Word las tolera; así el archivo valida).
    doc = re.sub(r"(<w:tblLook [^>]*/>)\s*<w:jc [^>]*/>", r"\1", doc)
    doc = re.sub(r"(<m:endChr [^>]*/>)(<m:sepChr [^>]*/>)", r"\2\1", doc)
    doc = re.sub(r"(<m:sty [^>]*/>)(<m:scr [^>]*/>)", r"\2\1", doc)
    doc = re.sub(r"(<m:nor\s*/>)<m:sty [^>]*/>", r"\1", doc)
    if "<w:sectPr" in doc:
        doc = re.sub(r"<w:sectPr\b.*?</w:sectPr>(?=\s*</w:body>)", SECT, doc, flags=re.S)
    else:
        doc = doc.replace("</w:body>", SECT + "</w:body>")
    if 'xmlns:r=' not in doc[:2000]:
        doc = doc.replace("<w:document ", '<w:document xmlns:r="http://schemas.openxmlformats.org/officeDocument/2006/relationships" ', 1)
    parts["word/document.xml"] = doc.encode("utf-8")
    parts["word/footer1.xml"] = FOOTER.encode("utf-8")
    rels = parts["word/_rels/document.xml.rels"].decode("utf-8")
    rels = rels.replace("</Relationships>", '<Relationship Id="rIdPieInforme" Type="http://schemas.openxmlformats.org/officeDocument/2006/relationships/footer" Target="footer1.xml"/></Relationships>')
    parts["word/_rels/document.xml.rels"] = rels.encode("utf-8")
    types = parts["[Content_Types].xml"].decode("utf-8")
    if 'Extension="png"' not in types:
        types = types.replace('<Default Extension="xml"', '<Default Extension="png" ContentType="image/png" /><Default Extension="xml"', 1)
    types = types.replace("</Types>", '<Override PartName="/word/footer1.xml" ContentType="application/vnd.openxmlformats-officedocument.wordprocessingml.footer+xml"/></Types>')
    parts["[Content_Types].xml"] = types.encode("utf-8")
    parts["word/settings.xml"] = ordered_settings(parts["word/settings.xml"])
    if "word/numbering.xml" in parts:
        numbering = parts["word/numbering.xml"].decode("utf-8")
        numbering = re.sub(r'(<w:nsid w:val=")([0-9A-Fa-f]{1,7})(")', lambda m: m.group(1) + m.group(2).zfill(8) + m.group(3), numbering)
        parts["word/numbering.xml"] = numbering.encode("utf-8")
    with zipfile.ZipFile(path, "w", zipfile.ZIP_DEFLATED) as z:
        z.writestr("[Content_Types].xml", parts.pop("[Content_Types].xml"))
        for n, data in parts.items():
            z.writestr(n, data)


def build_docx(fields, note, body, tmp):
    title = dict(fields).get("Título", "")
    main, _, sub = title.partition(":")
    cover = ["::: {custom-style=\"Portada Etiqueta\"}\nInforme de proyecto · Inteligencia artificial\n:::",
             "::: {custom-style=\"Title\"}\n" + main.strip() + "\n:::",
             "::: {custom-style=\"Subtitle\"}\n" + sub.strip()[:1].upper() + sub.strip()[1:] + "\n:::"]
    for key, value in fields:
        if key != "Título":
            cover.append("::: {custom-style=\"Portada Dato\"}\n**" + key + ":** " + value + "\n:::")
    cover.append("::: {custom-style=\"Portada Nota\"}\n**Naturaleza del trabajo:** " + note + "\n:::")
    text = "\n\n".join(cover + [PAGE_BREAK, TOC_FIELD, body])
    src = tmp / "informe_docx.md"
    src.write_text(text, encoding="utf-8")
    out = DOCS / "INFORME.docx"
    run(["pandoc", str(src), "-f", "markdown+tex_math_single_backslash", "-t", "docx", "-o", str(out),
         "--reference-doc", str(reference_docx(tmp)), "--resource-path", str(DOCS),
         "--shift-heading-level-by=-1", "-M", "lang=es-ES", "-M", "title-meta=" + title])
    finish_docx(out)
    print("Word:", out)


# --------------------------------------------------------------------------- PDF
CSS = r"""
@page { size: A4; margin: 22mm 21mm 24mm 21mm; }
:root { --ink:#1B2A2F; --teal-dark:#0E3B43; --teal:#1F7A72; --muted:#5F7176; --pale:#E3EFED; --amber-pale:#FDF1E4; --grid:#E3E9E8; }
html { font-family: Carlito, Calibri, "Liberation Sans", Arial, sans-serif; font-size: 11pt; color: var(--ink); line-height: 1.45; }
body { margin: 0; max-width: none; padding: 0; }
p { margin: 0 0 8pt; text-align: justify; hyphens: auto; orphans: 3; widows: 3; }
h1, h2, h3 { font-family: Caladea, Cambria, "Liberation Serif", serif; color: var(--teal-dark); line-height: 1.2; break-after: avoid; }
h1 { font-size: 21pt; margin: 0 0 14pt; break-before: page; }
h2 { font-size: 14.5pt; color: var(--teal); margin: 20pt 0 6pt; }
h3 { font-family: Carlito, Calibri, sans-serif; font-size: 12pt; margin: 14pt 0 4pt; }
a { color: var(--teal); text-decoration: none; }
code { font-family: "JetBrains Mono", "DejaVu Sans Mono", monospace; font-size: 8.6pt; background: #F1F5F4; padding: 0 2pt; border-radius: 2pt; word-break: break-all; }
strong { color: var(--teal-dark); }
math { font-family: "FreeSerif", "DejaVu Serif", serif; font-size: 1.05em; }
math[display="block"] { margin: 8pt 0; }
table { border-collapse: collapse; width: 100%; margin: 6pt 0 12pt; font-size: 9pt; break-inside: avoid; }
thead th { background: var(--teal-dark); color: #fff; font-weight: bold; padding: 4pt 5pt; text-align: left; }
td { padding: 3pt 5pt; border-bottom: 0.6pt solid var(--grid); font-variant-numeric: tabular-nums; }
tbody tr:nth-child(even) td { background: #F7FAF9; }
td[style*="right"], th[style*="right"] { text-align: right; }
figure { margin: 10pt 0 14pt; text-align: center; break-inside: avoid; }
figure img { max-width: 100%; }
figcaption { font-size: 9pt; font-style: italic; color: var(--muted); margin-top: 5pt; }
nav#TOC { break-after: page; }
nav#TOC > h2, .toc-title { font-family: Caladea, Cambria, serif; font-size: 21pt; color: var(--teal-dark); margin: 0 0 14pt; }
nav#TOC ul { list-style: none; padding-left: 0; margin: 0; }
nav#TOC > ul > li { margin: 9pt 0 3pt; font-weight: bold; font-size: 12pt; }
nav#TOC ul ul { padding-left: 16pt; }
nav#TOC ul ul li { font-weight: normal; font-size: 11pt; margin: 3pt 0; }
nav#TOC a { color: var(--ink); }
header#title-block-header { display: none; }
"""

COVER_CSS = r"""
@page { size: A4; margin: 0; }
html, body { margin: 0; height: 100%; font-family: Carlito, Calibri, sans-serif; color: #1B2A2F; }
.cover { box-sizing: border-box; height: 297mm; padding: 0; display: flex; flex-direction: column; }
.band { background: #0E3B43; color: #fff; padding: 52mm 22mm 18mm; }
.kicker { font-size: 10.5pt; letter-spacing: 1.6pt; text-transform: uppercase; color: #A9D2CB; font-weight: bold; margin-bottom: 10pt; }
.band h1 { font-family: Caladea, Cambria, serif; font-size: 34pt; line-height: 1.12; margin: 0 0 12pt; }
.band .sub { font-size: 15pt; color: #A9D2CB; }
.data { padding: 18mm 22mm 0; flex: 1; }
.data table { border-collapse: collapse; font-size: 12pt; }
.data td { padding: 5pt 16pt 5pt 0; vertical-align: top; }
.data td:first-child { color: #5F7176; font-weight: bold; white-space: nowrap; }
.note { margin: 0 22mm 22mm; background: #FDF1E4; border-radius: 6pt; padding: 12pt 14pt; font-size: 10pt; line-height: 1.45; }
.note b { color: #9A4E0E; }
"""


def inline_md(text):
    text = html.escape(text)
    return re.sub(r"\*\*(.+?)\*\*", r"<b>\1</b>", text)


def build_pdf(fields, note, body, tmp):
    from pypdf import PdfWriter
    title = dict(fields).get("Título", "")
    main, _, sub = title.partition(":")
    rows = "".join(f"<tr><td>{html.escape(k)}</td><td>{html.escape(v)}</td></tr>" for k, v in fields if k != "Título")
    cover = (f'<!doctype html><html lang="es"><head><meta charset="utf-8"><style>{COVER_CSS}</style></head><body><div class="cover">'
             f'<div class="band"><div class="kicker">Informe de proyecto · Inteligencia artificial</div><h1>{html.escape(main.strip())}</h1>'
             f'<div class="sub">{html.escape(sub.strip()[:1].upper() + sub.strip()[1:])}</div></div>'
             f'<div class="data"><table>{rows}</table></div>'
             f'<div class="note"><b>Naturaleza del trabajo:</b> {inline_md(note)}</div></div></body></html>')
    (tmp / "portada.html").write_text(cover, encoding="utf-8")
    (tmp / "estilo.css").write_text(CSS, encoding="utf-8")
    src = tmp / "informe_pdf.md"
    src.write_text(body, encoding="utf-8")
    run(["pandoc", str(src), "-f", "markdown+tex_math_single_backslash", "-t", "html5", "-s", "--mathml",
         "--toc", "--toc-depth=2", "--shift-heading-level-by=-1", "--resource-path", str(DOCS), "--embed-resources",
         "--css", str(tmp / "estilo.css"), "-M", "pagetitle=" + title, "-M", "toc-title=Índice", "-M", "lang=es",
         "-o", str(tmp / "cuerpo.html")])
    run(["node", str(HERE / "imprimir_pdf.js"), str(tmp / "portada.html"), str(tmp / "cuerpo.html"), str(tmp)])
    writer = PdfWriter()
    for part in ("portada.pdf", "cuerpo.pdf"):
        writer.append(str(tmp / part))
    writer.add_metadata({"/Title": title, "/Subject": "Clasificación supervisada de enfermedades caninas (datos sintéticos)"})
    out = DOCS / "INFORME.pdf"
    with out.open("wb") as f:
        writer.write(f)
    print("PDF:", out)


def main():
    missing = [p for p in [FIGURES[k][0] for k in FIGURES] if not (DOCS / p).is_file()]
    if missing:
        sys.exit("Faltan imágenes; ejecute antes scripts/informe/rasterizar.js: " + ", ".join(missing))
    fields, note, body = prepare()
    tmp = Path(tempfile.mkdtemp(prefix="informe_"))
    try:
        build_docx(fields, note, body, tmp)
        build_pdf(fields, note, body, tmp)
    finally:
        shutil.rmtree(tmp, ignore_errors=True)


if __name__ == "__main__":
    main()
