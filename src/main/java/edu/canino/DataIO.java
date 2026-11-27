package edu.canino;

import java.io.BufferedReader;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.security.MessageDigest;
import java.util.HashSet;
import java.util.Set;
import weka.core.Instance;
import weka.core.Instances;

public final class DataIO {
    private DataIO() { }
    public static Instances load(Path file) throws Exception {
        Instances data;
        try (BufferedReader reader = Files.newBufferedReader(file, StandardCharsets.UTF_8)) { data = new Instances(reader); }
        Schema.validateHeader(data);
        if (data.numInstances() == 0) throw new IllegalArgumentException("ARFF sin filas.");
        int[] counts = new int[data.numClasses()];
        for (Instance row : data) {
            for (int j = 0; j < data.numAttributes(); j++) if (row.isMissing(j))
                throw new IllegalArgumentException("No se permiten valores faltantes en entrenamiento.");
            if (row.weight() != 1.0) throw new IllegalArgumentException("No se permiten pesos distintos de 1.");
            counts[(int) row.classValue()]++;
        }
        for (int c : counts) if (c < 10) throw new IllegalArgumentException("Cada clase necesita al menos 10 filas para CV de 10 pliegues.");
        return data;
    }
    public static String sha256(Path file) throws Exception {
        byte[] digest = MessageDigest.getInstance("SHA-256").digest(Files.readAllBytes(file));
        StringBuilder out = new StringBuilder();
        for (byte b : digest) out.append(String.format("%02x", b & 255));
        return out.toString();
    }
    public static int duplicatePredictors(Instances data) {
        Set<String> seen = new HashSet<>();
        int duplicates = 0;
        for (Instance row : data) {
            StringBuilder key = new StringBuilder();
            for (int j = 0; j < Schema.SYMPTOMS.length; j++) key.append((int) row.value(j));
            if (!seen.add(key.toString())) duplicates++;
        }
        return duplicates;
    }
}
