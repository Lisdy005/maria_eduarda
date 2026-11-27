package edu.canino;

import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Locale;
import java.util.Random;
import weka.classifiers.Classifier;
import weka.classifiers.Evaluation;
import weka.core.Instance;
import weka.core.Instances;
import weka.core.Utils;
import weka.core.Version;

/** CV manual estratificada y emparejada: mismos pliegues para ambos algoritmos. */
public final class Experiment {
    public static final int FOLDS = 10;
    public static final long SEED = 42L;
    public static String f(double x) { return Double.isFinite(x) ? String.format(Locale.ROOT, "%.6f", x) : "NA"; }
    public static double safe(double x) { return Double.isFinite(x) ? x : 0; }
    private static double macro(Evaluation e, int metric) {
        double sum = 0;
        for (int c = 0; c < Schema.DISEASES.length; c++) sum += safe(metric == 0 ? e.precision(c) : metric == 1 ? e.recall(c) : e.fMeasure(c));
        return sum / Schema.DISEASES.length;
    }
    public void run(Path arff, Path out) throws Exception {
        Instances data = DataIO.load(arff);
        Files.createDirectories(out);
        Instances shuffled = new Instances(data);
        shuffled.randomize(new Random(SEED));
        shuffled.stratify(FOLDS);
        StringBuilder global = new StringBuilder("algoritmo,accuracy,precision_macro,recall_macro,f1_macro,precision_ponderada,recall_ponderado,f1_ponderado\n");
        StringBuilder perClass = new StringBuilder("algoritmo,clase,soporte,precision,recall,f1\n");
        StringBuilder folds = new StringBuilder("algoritmo,pliegue,n_entrenamiento,n_prueba,accuracy,f1_macro\n");
        StringBuilder predictions = new StringBuilder("algoritmo,pliegue,indice_cv,real,predicha");
        for (String c : Schema.DISEASES) predictions.append(",p_").append(c);
        predictions.append('\n');
        StringBuilder assignment = new StringBuilder("indice_cv,pliegue,clase");
        for (String s : Schema.SYMPTOMS) assignment.append(',').append(s);
        assignment.append('\n');
        int offset = 0;
        for (int k = 0; k < FOLDS; k++) {
            Instances test = shuffled.testCV(FOLDS, k);
            for (int i = 0; i < test.numInstances(); i++) {
                Instance row = test.instance(i);
                assignment.append(offset + i).append(',').append(k + 1).append(',').append(row.stringValue(data.classIndex()));
                for (int j = 0; j < Schema.SYMPTOMS.length; j++) assignment.append(',').append((int) row.value(j));
                assignment.append('\n');
            }
            offset += test.numInstances();
        }
        StringBuilder md = new StringBuilder("# Resultados ejecutados automáticamente\n\n");
        md.append("Weka ").append(Version.VERSION).append("; Java ").append(System.getProperty("java.version"))
            .append(". Dataset: ").append(data.numInstances()).append(" filas. SHA-256: `").append(DataIO.sha256(arff)).append("`.\n\n")
            .append("CV estratificada de 10 pliegues, semilla 42, mismos pliegues para ambos modelos. No se ajustaron hiperparámetros con estos resultados. ")
            .append("Las métricas por clase agrupan las 200 predicciones fuera de muestra. Métricas no definidas se registran NA en CSV y se tratan como 0 para macro.\n\n")
            .append("Vectores de síntomas repetidos (exceso sobre únicos): ").append(DataIO.duplicatePredictors(data))
            .append(". Los duplicados no se eliminan; pueden cruzar pliegues. No representan perros reales.\n\n")
            .append("|Algoritmo|Accuracy|Precisión macro|Recall macro|F1 macro|F1 ponderado|\n|---|---:|---:|---:|---:|---:|\n");
        Evaluation[] results = new Evaluation[TrainingService.ALGORITHMS.length];
        for (int a = 0; a < TrainingService.ALGORITHMS.length; a++) {
            String name = TrainingService.ALGORITHMS[a];
            Evaluation total = new Evaluation(data);
            offset = 0;
            for (int k = 0; k < FOLDS; k++) {
                Instances train = shuffled.trainCV(FOLDS, k);
                Instances test = shuffled.testCV(FOLDS, k);
                Classifier model = TrainingService.create(name);
                model.buildClassifier(train);
                Evaluation fold = new Evaluation(train);
                for (int i = 0; i < test.numInstances(); i++) {
                    Instance actual = test.instance(i);
                    Instance masked = (Instance) actual.copy();
                    masked.setDataset(test);
                    masked.setClassMissing();
                    double[] p = model.distributionForInstance(masked);
                    total.evaluateModelOnceAndRecordPrediction(p, actual);
                    fold.evaluateModelOnceAndRecordPrediction(p, actual);
                    predictions.append(name).append(',').append(k + 1).append(',').append(offset + i).append(',')
                        .append(actual.stringValue(data.classIndex())).append(',').append(Schema.DISEASES[Utils.maxIndex(p)]);
                    for (double value : p) predictions.append(',').append(f(value));
                    predictions.append('\n');
                }
                offset += test.numInstances();
                folds.append(name).append(',').append(k + 1).append(',').append(train.numInstances()).append(',')
                    .append(test.numInstances()).append(',').append(f(fold.pctCorrect() / 100)).append(',').append(f(macro(fold, 2))).append('\n');
            }
            results[a] = total;
            global.append(name).append(',').append(f(total.pctCorrect() / 100)).append(',').append(f(macro(total, 0))).append(',')
                .append(f(macro(total, 1))).append(',').append(f(macro(total, 2))).append(',').append(f(total.weightedPrecision())).append(',')
                .append(f(total.weightedRecall())).append(',').append(f(total.weightedFMeasure())).append('\n');
            md.append('|').append(name).append('|').append(f(total.pctCorrect() / 100)).append('|').append(f(macro(total, 0)))
                .append('|').append(f(macro(total, 1))).append('|').append(f(macro(total, 2))).append('|').append(f(total.weightedFMeasure())).append("|\n");
            double[][] matrix = total.confusionMatrix();
            StringBuilder csv = new StringBuilder("real/predicha," + String.join(",", Schema.DISEASES) + "\n");
            for (int c = 0; c < data.numClasses(); c++) {
                csv.append(Schema.DISEASES[c]);
                double support = 0;
                for (double count : matrix[c]) { csv.append(',').append((int) count); support += count; }
                csv.append('\n');
                perClass.append(name).append(',').append(Schema.DISEASES[c]).append(',').append((int) support).append(',')
                    .append(f(total.precision(c))).append(',').append(f(total.recall(c))).append(',').append(f(total.fMeasure(c))).append('\n');
            }
            write(out.resolve("confusion_" + name + ".csv"), csv.toString());
            write(out.resolve("weka_" + name + ".txt"), total.toSummaryString() + total.toClassDetailsString() + total.toMatrixString());
            Charts.confusion(out.resolve("confusion_" + name + ".svg"), name, matrix);
            Charts.perClass(out.resolve("clases_" + name + ".svg"), name, total);
        }
        md.append("\n![Comparación global](comparacion.svg)\n\n");
        for (int a = 0; a < results.length; a++) {
            Evaluation e = results[a]; String name = TrainingService.ALGORITHMS[a];
            md.append("## ").append(name).append("\n\n|Clase|Soporte|Precisión|Recall|F1|\n|---|---:|---:|---:|---:|\n");
            for (int c = 0; c < data.numClasses(); c++) {
                double n = 0; for (double v : e.confusionMatrix()[c]) n += v;
                md.append('|').append(Schema.DISEASES[c]).append('|').append((int) n).append('|').append(f(e.precision(c)))
                    .append('|').append(f(e.recall(c))).append('|').append(f(e.fMeasure(c))).append("|\n");
            }
            md.append("\nMatriz: filas reales, columnas predichas, orden de clases igual al ARFF.\n\n|Real / predicha|");
            for (String c : Schema.DISEASES) md.append(c).append('|');
            md.append("\n|---|"); for (int c = 0; c < data.numClasses(); c++) md.append("---:|"); md.append('\n');
            for (int c = 0; c < data.numClasses(); c++) {
                md.append('|').append(Schema.DISEASES[c]).append('|');
                for (double n : e.confusionMatrix()[c]) md.append((int) n).append('|');
                md.append('\n');
            }
            md.append("\n![Métricas por clase](clases_").append(name).append(".svg)\n\n![Matriz](confusion_").append(name).append(".svg)\n\n");
        }
        write(out.resolve("metricas_globales.csv"), global.toString());
        write(out.resolve("metricas_por_clase.csv"), perClass.toString());
        write(out.resolve("pliegues.csv"), folds.toString());
        write(out.resolve("predicciones.csv"), predictions.toString());
        write(out.resolve("asignacion_pliegues.csv"), assignment.toString());
        write(out.resolve("metricas.md"), md.toString());
        Charts.comparison(out.resolve("comparacion.svg"), results);
        System.out.print(global);
        System.out.println("Resultados y gráficas: " + out.toAbsolutePath());
    }
    static void write(Path path, String text) throws Exception { Files.write(path, text.getBytes(StandardCharsets.UTF_8)); }
}
