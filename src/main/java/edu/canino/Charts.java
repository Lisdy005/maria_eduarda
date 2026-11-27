package edu.canino;

import java.nio.file.Path;
import java.util.Locale;
import weka.classifiers.Evaluation;

/**
 * Gráficas SVG vectoriales, sin librerías gráficas ni pantalla. Escala fija 0..1.
 * Paleta validada (contraste, croma y separación para daltonismo) y compartida con la
 * interfaz y la presentación: J48 = verde azulado, Naive Bayes = ámbar.
 * Cada marca lleva un elemento title, que el navegador muestra como tooltip.
 */
public final class Charts {
    private Charts() { }

    /** Color por algoritmo, en el orden de {@link TrainingService#ALGORITHMS}. */
    static final String[] ALGORITHM_COLORS = {"#00897B", "#E07A1F"};
    /** Precisión, recall y F1. Distintos de los colores de algoritmo para no mezclar significados. */
    static final String[] METRIC_COLORS = {"#5B5EA6", "#C2577F", "#3E8FD0"};
    static final String[] ALGORITHM_NAMES = {"J48", "Naive Bayes"};

    private static final String INK = "#1B2A2F", TITLE = "#0E3B43", MUTED = "#5F7176", GRID = "#E3E9E8", SURFACE = "#FFFFFF";
    private static final String FONT = "Inter, 'Segoe UI', Helvetica, Arial, sans-serif";

    private static String n(double value) { return String.format(Locale.ROOT, "%.2f", value); }
    private static String pct(double value) { return Double.isFinite(value) ? String.format(Locale.ROOT, "%.2f", value) : "NA"; }
    private static String esc(String t) { return t.replace("&", "&amp;").replace("<", "&lt;").replace(">", "&gt;").replace("\"", "&quot;"); }

    private static StringBuilder start(int w, int h, String title, String subtitle) {
        StringBuilder s = new StringBuilder("<svg xmlns=\"http://www.w3.org/2000/svg\" width=\"" + w + "\" height=\"" + h
            + "\" viewBox=\"0 0 " + w + " " + h + "\" role=\"img\" aria-label=\"" + esc(title) + "\"><title>" + esc(title) + "</title>"
            + "<rect width=\"100%\" height=\"100%\" fill=\"" + SURFACE + "\"/><g font-family=\"" + FONT + "\" fill=\"" + INK + "\">");
        text(s, 32, 42, title, 21, TITLE, "bold", "start");
        text(s, 32, 66, subtitle, 13.5, MUTED, "normal", "start");
        text(s, 32, h - 20, "Datos sintéticos · sin validación clínica · validación cruzada estratificada de 10 pliegues (200 predicciones fuera de muestra)", 11.5, MUTED, "normal", "start");
        return s;
    }
    private static void text(StringBuilder s, double x, double y, String t, double size, String color, String weight, String anchor) {
        s.append("<text x=\"").append(n(x)).append("\" y=\"").append(n(y)).append("\" font-size=\"").append(n(size))
            .append("\" fill=\"").append(color).append("\" font-weight=\"").append(weight).append("\" text-anchor=\"").append(anchor)
            .append("\">").append(esc(t)).append("</text>");
    }
    private static void line(StringBuilder s, double x1, double y1, double x2, double y2, String color) {
        s.append("<line x1=\"").append(n(x1)).append("\" y1=\"").append(n(y1)).append("\" x2=\"").append(n(x2)).append("\" y2=\"")
            .append(n(y2)).append("\" stroke=\"").append(color).append("\" stroke-width=\"1\"/>");
    }
    private static void swatch(StringBuilder s, double x, double y, String color, String label) {
        s.append("<rect x=\"").append(n(x)).append("\" y=\"").append(n(y - 10)).append("\" width=\"12\" height=\"12\" rx=\"3\" fill=\"").append(color).append("\"/>");
        text(s, x + 18, y, label, 13, INK, "normal", "start");
    }
    /** Columna que crece desde la base con extremo superior redondeado (4 px). */
    private static void column(StringBuilder s, double x, double base, double w, double h, String color, String tip) {
        double r = Math.min(4, Math.min(w / 2, h)), top = base - h;
        s.append("<path d=\"M").append(n(x)).append(',').append(n(base)).append(" V").append(n(top + r)).append(" Q").append(n(x)).append(',').append(n(top))
            .append(' ').append(n(x + r)).append(',').append(n(top)).append(" H").append(n(x + w - r)).append(" Q").append(n(x + w)).append(',').append(n(top))
            .append(' ').append(n(x + w)).append(',').append(n(top + r)).append(" V").append(n(base)).append(" Z\" fill=\"").append(color).append("\"><title>")
            .append(esc(tip)).append("</title></path>");
    }
    /** Barra horizontal que crece desde la izquierda con extremo derecho redondeado (4 px). */
    private static void bar(StringBuilder s, double x, double y, double w, double h, String color, String tip) {
        if (w <= 0) return;
        double r = Math.min(4, Math.min(h / 2, w));
        s.append("<path d=\"M").append(n(x)).append(',').append(n(y)).append(" H").append(n(x + w - r)).append(" Q").append(n(x + w)).append(',').append(n(y))
            .append(' ').append(n(x + w)).append(',').append(n(y + r)).append(" V").append(n(y + h - r)).append(" Q").append(n(x + w)).append(',').append(n(y + h))
            .append(' ').append(n(x + w - r)).append(',').append(n(y + h)).append(" H").append(n(x)).append(" Z\" fill=\"").append(color).append("\"><title>")
            .append(esc(tip)).append("</title></path>");
    }
    private static void end(Path path, StringBuilder s) throws Exception { Experiment.write(path, s.append("</g></svg>\n").toString()); }

    private static double[] macro(Evaluation e) {
        double p = 0, r = 0, f = 0;
        for (int c = 0; c < Schema.DISEASES.length; c++) { p += Experiment.safe(e.precision(c)); r += Experiment.safe(e.recall(c)); f += Experiment.safe(e.fMeasure(c)); }
        int k = Schema.DISEASES.length;
        return new double[] {e.pctCorrect() / 100, p / k, r / k, f / k};
    }

    /** Columnas agrupadas: accuracy y promedios macro de ambos algoritmos. */
    public static void comparison(Path path, Evaluation[] results) throws Exception {
        int w = 980, h = 480;
        StringBuilder s = start(w, h, "Comparación global de algoritmos", "Proporción de 0 a 1 · mayor es mejor");
        String[] labels = {"Accuracy", "Precisión macro", "Recall macro", "F1 macro"};
        double left = 80, right = w - 40, top = 110, base = 390, plotH = base - top;
        for (int t = 0; t <= 5; t++) {
            double v = t / 5.0, y = base - v * plotH;
            line(s, left, y, right, y, t == 0 ? "#C9D3D1" : GRID);
            text(s, left - 12, y + 4, String.format(Locale.ROOT, "%.1f", v), 12, MUTED, "normal", "end");
        }
        double legendX = right - 230;
        for (int a = 0; a < results.length; a++) swatch(s, legendX + a * 120, 92, ALGORITHM_COLORS[a], ALGORITHM_NAMES[a]);
        double group = (right - left) / labels.length, barW = 34, gap = 4;
        for (int j = 0; j < labels.length; j++) {
            double cx = left + group * (j + 0.5);
            text(s, cx, base + 26, labels[j], 14, INK, "normal", "middle");
            for (int a = 0; a < results.length; a++) {
                double v = macro(results[a])[j];
                double x = cx - (results.length * barW + (results.length - 1) * gap) / 2 + a * (barW + gap);
                column(s, x, base, barW, v * plotH, ALGORITHM_COLORS[a], ALGORITHM_NAMES[a] + " · " + labels[j] + ": " + pct(v));
                text(s, x + barW / 2, base - v * plotH - 8, n(v), 13, INK, "bold", "middle");
            }
        }
        end(path, s);
    }

    /** Barras horizontales por clase: precisión, recall y F1 (valor rotulado solo en F1). */
    public static void perClass(Path path, String name, Evaluation e) throws Exception {
        int w = 1000, h = 700;
        String display = "NaiveBayes".equals(name) ? "Naive Bayes" : name;
        StringBuilder s = start(w, h, display + " — precisión, recall y F1 por enfermedad", "Proporción de 0 a 1 · se rotula el F1; el resto aparece al pasar el cursor");
        String[] legends = {"Precisión", "Recall", "F1"};
        double lx = 620;
        for (int m = 0; m < 3; m++) { swatch(s, lx, 92, METRIC_COLORS[m], legends[m]); lx += 18 + legends[m].length() * 7.6 + 26; }
        double left = 250, right = w - 80, top = 118, rowH = 64, barH = 12, gap = 2;
        double bottom = top + rowH * Schema.DISEASES.length;
        for (int t = 0; t <= 5; t++) {
            double v = t / 5.0, x = left + v * (right - left);
            line(s, x, top - 6, x, bottom, t == 0 ? "#C9D3D1" : GRID);
            text(s, x, bottom + 20, String.format(Locale.ROOT, "%.1f", v), 12, MUTED, "normal", "middle");
        }
        for (int c = 0; c < Schema.DISEASES.length; c++) {
            double y = top + c * rowH + (rowH - (3 * barH + 2 * gap)) / 2;
            text(s, left - 16, y + 3 * barH / 2 + gap + 5, Schema.DISEASE_LABELS[c], 14, INK, "normal", "end");
            double[] v = {e.precision(c), e.recall(c), e.fMeasure(c)};
            for (int m = 0; m < 3; m++) {
                double val = Experiment.safe(v[m]), by = y + m * (barH + gap);
                bar(s, left, by, val * (right - left), barH, METRIC_COLORS[m], Schema.DISEASE_LABELS[c] + " · " + legends[m] + ": " + pct(v[m]));
                if (m == 2) text(s, left + val * (right - left) + 8, by + barH - 1, pct(v[m]), 12.5, INK, "bold", "start");
            }
        }
        end(path, s);
    }

    private static String mix(int[] from, int[] to, double t) {
        t = Math.max(0, Math.min(1, t));
        return String.format("#%02X%02X%02X", Math.round(from[0] + (to[0] - from[0]) * t), Math.round(from[1] + (to[1] - from[1]) * t), Math.round(from[2] + (to[2] - from[2]) * t));
    }

    /** Matriz de confusión: aciertos en escala verde azulado, errores en escala ámbar, ceros en gris. */
    public static void confusion(Path path, String name, double[][] matrix) throws Exception {
        int k = Schema.DISEASES.length, cell = 60, left = 250, top = 280;
        int w = left + k * cell + 190, h = top + k * cell + 120;
        String display = "NaiveBayes".equals(name) ? "Naive Bayes" : name;
        int total = 0, correct = 0, maxError = 1;
        for (int r = 0; r < k; r++) for (int c = 0; c < k; c++) { total += (int) matrix[r][c]; if (r == c) correct += (int) matrix[r][c]; else maxError = Math.max(maxError, (int) matrix[r][c]); }
        StringBuilder s = start(w, h, display + " — matriz de confusión", "Filas: enfermedad real · columnas: predicha · " + correct + " de " + total + " aciertos en la diagonal");
        swatch(s, 32, 100, "#00796B", "Aciertos (diagonal)");
        swatch(s, 200, 100, "#E07A1F", "Errores (más oscuro = más casos)");
        int[] okFrom = {0xDD, 0xF1, 0xEE}, okTo = {0x00, 0x69, 0x5C}, errFrom = {0xFD, 0xEB, 0xD8}, errTo = {0xC2, 0x5A, 0x10};
        for (int c = 0; c < k; c++) {
            double x = left + c * cell + cell / 2.0 - 4;
            s.append("<g transform=\"translate(").append(n(x)).append(',').append(top - 12).append(") rotate(-40)\">");
            text(s, 0, 0, Schema.DISEASE_LABELS[c], 13, INK, "normal", "start");
            s.append("</g>");
            text(s, left - 12, top + c * cell + cell / 2.0 + 5, Schema.DISEASE_LABELS[c], 13, INK, "normal", "end");
        }
        text(s, left + k * cell / 2.0, top + k * cell + 34, "Clase predicha →", 13, MUTED, "normal", "middle");
        for (int r = 0; r < k; r++) {
            for (int c = 0; c < k; c++) {
                int v = (int) matrix[r][c];
                String fill; boolean dark;
                if (r == c) { double t = v / 25.0; fill = mix(okFrom, okTo, t); dark = t > 0.45; }
                else if (v > 0) { double t = 0.25 + 0.75 * v / maxError; fill = mix(errFrom, errTo, t); dark = t > 0.6; }
                else { fill = "#F4F7F6"; dark = false; }
                double x = left + c * cell, y = top + r * cell;
                s.append("<rect x=\"").append(n(x + 1)).append("\" y=\"").append(n(y + 1)).append("\" width=\"").append(cell - 2).append("\" height=\"").append(cell - 2)
                    .append("\" rx=\"4\" fill=\"").append(fill).append("\"><title>Real: ").append(esc(Schema.DISEASE_LABELS[r])).append(" · Predicha: ")
                    .append(esc(Schema.DISEASE_LABELS[c])).append(" · ").append(v).append(v == 1 ? " caso" : " casos").append("</title></rect>");
                text(s, x + cell / 2.0, y + cell / 2.0 + 6, Integer.toString(v), v == 0 ? 13 : 17, dark ? "#FFFFFF" : (v == 0 ? "#9AA9AD" : INK), v == 0 ? "normal" : "bold", "middle");
            }
        }
        end(path, s);
    }
}
