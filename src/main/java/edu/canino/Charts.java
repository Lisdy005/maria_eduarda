package edu.canino;

import java.nio.file.Path;
import java.util.Locale;
import weka.classifiers.Evaluation;

/** Gráficas SVG vectoriales, sin librerías gráficas ni pantalla. Escala fija 0..1. */
public final class Charts {
    private Charts() { }
    private static final String[] COLORS = {"#176b87", "#b54a24", "#7253a6"};
    private static String n(double value) { return String.format(Locale.ROOT, "%.2f", value); }
    private static StringBuilder start(int w, int h, String title) {
        StringBuilder s = new StringBuilder("<svg xmlns=\"http://www.w3.org/2000/svg\" width=\"" + w + "\" height=\"" + h + "\" viewBox=\"0 0 " + w + " " + h + "\" role=\"img\"><title>" + title + "</title><rect width=\"100%\" height=\"100%\" fill=\"#fafbfc\"/><g font-family=\"sans-serif\" fill=\"#172d3c\">");
        text(s, 24, 30, title, 19);
        text(s, 24, h - 18, "DATOS SINTÉTICOS · Sin validación clínica · CV estratificada 10 pliegues", 12);
        return s;
    }
    private static void text(StringBuilder s, double x, double y, String t, int size) {
        s.append("<text x=\"").append(n(x)).append("\" y=\"").append(n(y)).append("\" font-size=\"").append(size).append("\">")
            .append(t.replace("&", "&amp;").replace("<", "&lt;")).append("</text>");
    }
    private static void rect(StringBuilder s, double x, double y, double w, double h, String color) {
        s.append("<rect x=\"").append(n(x)).append("\" y=\"").append(n(y)).append("\" width=\"").append(n(w)).append("\" height=\"").append(n(h)).append("\" fill=\"").append(color).append("\"/>");
    }
    private static void end(Path path, StringBuilder s) throws Exception { Experiment.write(path, s.append("</g></svg>\n").toString()); }
    public static void comparison(Path path, Evaluation[] results) throws Exception {
        StringBuilder s = start(980, 450, "Comparación global: proporciones (0–1)");
        String[] labels = {"Accuracy", "Precisión macro", "Recall macro", "F1 macro"};
        for (int a = 0; a < results.length; a++) {
            Evaluation e = results[a]; double p = 0, r = 0, f = 0;
            for (int c = 0; c < Schema.DISEASES.length; c++) { p += Experiment.safe(e.precision(c)); r += Experiment.safe(e.recall(c)); f += Experiment.safe(e.fMeasure(c)); }
            double[] values = {e.pctCorrect() / 100, p / 8, r / 8, f / 8};
            rect(s, 650 + a * 150, 48, 14, 14, COLORS[a]); text(s, 670 + a * 150, 60, TrainingService.ALGORITHMS[a], 13);
            for (int j = 0; j < 4; j++) {
                double x = 80 + j * 225 + a * 64; double h = values[j] * 260;
                rect(s, x, 350 - h, 54, h, COLORS[a]); text(s, x, 340 - h, n(values[j]), 13);
                if (a == 0) text(s, x - 12, 377, labels[j], 14);
            }
        }
        text(s, 24, 95, "1.0", 12); text(s, 24, 350, "0.0", 12); end(path, s);
    }
    public static void perClass(Path path, String name, Evaluation e) throws Exception {
        StringBuilder s = start(1080, 730, name + " — precisión, recall y F1 por clase (0–1)");
        String[] legends = {"Precisión", "Recall", "F1"};
        for (int m = 0; m < 3; m++) { rect(s, 400 + 180 * m, 50, 14, 14, COLORS[m]); text(s, 420 + 180 * m, 62, legends[m], 13); }
        for (int c = 0; c < Schema.DISEASES.length; c++) {
            double y = 92 + c * 73; text(s, 20, y + 30, Schema.DISEASES[c], 12);
            double[] v = {e.precision(c), e.recall(c), e.fMeasure(c)};
            for (int m = 0; m < 3; m++) { rect(s, 290, y + m * 19, 650 * Experiment.safe(v[m]), 15, COLORS[m]); text(s, 950, y + m * 19 + 12, Double.isFinite(v[m]) ? n(v[m]) : "NA", 12); }
        }
        text(s, 290, 695, "0", 12); text(s, 930, 695, "1", 12); end(path, s);
    }
    public static void confusion(Path path, String name, double[][] matrix) throws Exception {
        StringBuilder s = start(1100, 770, name + " — matriz de confusión (recuento)");
        text(s, 275, 58, "Columnas: predicha; filas: real. Clases 1–8 según leyenda inferior.", 13);
        for (int c = 0; c < 8; c++) {
            text(s, 312 + 66 * c, 84, Integer.toString(c + 1), 14);
            text(s, 15, 119 + 55 * c, (c + 1) + ". " + Schema.DISEASES[c], 12);
            for (int p = 0; p < 8; p++) {
                int light = 248 - (int) Math.round(120 * matrix[c][p] / 25.0);
                light = Math.max(80, Math.min(248, light));
                rect(s, 285 + p * 66, 95 + c * 55, 64, 53, "rgb(" + (light - 25) + "," + light + "," + Math.min(255, light + 5) + ")");
                text(s, 312 + p * 66, 126 + c * 55, Integer.toString((int) matrix[c][p]), 15);
            }
        }
        for (int c = 0; c < 8; c++) text(s, 24 + (c / 4) * 530, 577 + (c % 4) * 30, (c + 1) + ". " + Schema.DISEASES[c], 13);
        end(path, s);
    }
}
