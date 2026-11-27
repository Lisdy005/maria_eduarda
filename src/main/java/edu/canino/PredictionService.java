package edu.canino;

import java.nio.file.Files;
import java.nio.file.Path;
import weka.core.DenseInstance;
import weka.core.Instance;
import weka.core.Instances;
import weka.core.SerializationHelper;
import weka.core.Utils;
import weka.core.Version;

public final class PredictionService {
    public ModelBundle load(Path path) throws Exception {
        if (!Files.isRegularFile(path)) throw new IllegalArgumentException("Modelo no encontrado. Ejecute primero entrenar: " + path);
        // Solo archivos locales de confianza: la deserialización Java NO es segura para archivos ajenos.
        Object object = SerializationHelper.read(path.toString());
        if (!(object instanceof ModelBundle)) throw new IllegalArgumentException("Archivo no es un modelo de esta aplicación.");
        ModelBundle bundle = (ModelBundle) object;
        if (bundle.format != 1 || !Version.VERSION.equals(bundle.wekaVersion))
            throw new IllegalArgumentException("Modelo incompatible. Reentrene con esta versión de Weka.");
        Schema.validateHeader(bundle.header);
        return bundle;
    }
    public double[] predict(ModelBundle model, boolean[] symptoms) throws Exception {
        if (symptoms.length != Schema.SYMPTOMS.length) throw new IllegalArgumentException("Se necesitan 15 valores.");
        Instances context = new Instances(model.header, 0);
        double[] values = new double[context.numAttributes()];
        for (int i = 0; i < symptoms.length; i++) values[i] = symptoms[i] ? 1 : 0;
        values[context.classIndex()] = Utils.missingValue();
        Instance row = new DenseInstance(1.0, values);
        row.setDataset(context);
        double[] probabilities = model.classifier.distributionForInstance(row);
        double sum = 0;
        if (probabilities.length != Schema.DISEASES.length) throw new IllegalStateException("Distribución inválida.");
        for (double p : probabilities) {
            if (!Double.isFinite(p) || p < 0) throw new IllegalStateException("Probabilidad inválida.");
            sum += p;
        }
        if (Math.abs(sum - 1) > 1e-6) throw new IllegalStateException("Distribución no normalizada.");
        return probabilities;
    }
    public static String format(double[] p) {
        StringBuilder out = new StringBuilder("Clase sugerida por el modelo: " + Schema.DISEASES[Utils.maxIndex(p)] + "\n\n");
        out.append("Distribución del modelo (NO riesgo clínico ni certeza diagnóstica):\n");
        for (int i = 0; i < p.length; i++) out.append(String.format(java.util.Locale.ROOT, "%-30s %6.2f %%\n", Schema.DISEASES[i], 100 * p[i]));
        return out.append("\nIncluso un 100 % no confirma enfermedad; un 0 % no la descarta.\n\n")
            .append(Schema.WARNING).append('\n').append(Schema.URGENT).toString();
    }
}
