package edu.canino;

import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;
import weka.classifiers.Classifier;
import weka.classifiers.bayes.NaiveBayes;
import weka.classifiers.trees.J48;
import weka.core.Instances;
import weka.core.SerializationHelper;

public final class TrainingService {
    public static final String[] ALGORITHMS = {"J48", "NaiveBayes"};
    public static Classifier create(String name) {
        if ("J48".equals(name)) {
            J48 tree = new J48();
            tree.setConfidenceFactor(0.25f);
            tree.setMinNumObj(2);
            tree.setUnpruned(false);
            return tree;
        }
        if ("NaiveBayes".equals(name)) return new NaiveBayes();
        throw new IllegalArgumentException("Algoritmo desconocido: " + name);
    }
    public void train(Path arff, Path directory) throws Exception {
        Instances data = DataIO.load(arff);
        String sha = DataIO.sha256(arff);
        Files.createDirectories(directory);
        for (String name : ALGORITHMS) {
            Classifier classifier = create(name);
            classifier.buildClassifier(data);
            ModelBundle bundle = new ModelBundle(name, sha, data, classifier);
            Path temp = Files.createTempFile(directory, name, ".tmp");
            try {
                SerializationHelper.write(temp.toString(), bundle);
                Files.move(temp, directory.resolve(name + ".model"), StandardCopyOption.REPLACE_EXISTING);
            } finally { Files.deleteIfExists(temp); }
            Files.writeString(directory.resolve(name + ".txt"), classifier.toString(), java.nio.charset.StandardCharsets.UTF_8);
            System.out.println("Guardado " + directory.resolve(name + ".model"));
        }
    }
}
