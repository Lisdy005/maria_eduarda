package edu.canino;

import java.io.Serializable;
import weka.classifiers.Classifier;
import weka.core.Instances;
import weka.core.Version;

/** Modelo y cabecera juntos: impide inferencia con orden de atributos diferente. */
public final class ModelBundle implements Serializable {
    private static final long serialVersionUID = 1L;
    public final int format = 1;
    public final String algorithm;
    public final String trainingSha256;
    public final String wekaVersion = Version.VERSION;
    public final int trainingRows;
    public final Instances header;
    public final Classifier classifier;
    public ModelBundle(String algorithm, String sha, Instances data, Classifier classifier) {
        this.algorithm = algorithm;
        this.trainingSha256 = sha;
        this.trainingRows = data.numInstances();
        this.header = new Instances(data, 0);
        this.classifier = classifier;
    }
}
