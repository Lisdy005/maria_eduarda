package edu.canino;

import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Random;

/** Probabilidades DIDÁCTICAS elegidas manualmente, NO prevalencias clínicas. */
public final class DataGenerator {
    private DataGenerator() { }
    public static final long SEED = 20261127L;
    public static final int PER_CLASS = 25;
    // Columnas en el orden de Schema.SYMPTOMS. Filas en Schema.DISEASES.
    public static final double[][] PROFILES = {
        {.80,.92,.94,.04,.92,.90,.04,.03,.12,.03,.03,.45,.78,.03,.30},
        {.82,.35,.45,.75,.85,.80,.90,.65,.25,.06,.08,.35,.10,.04,.18},
        {.40,.10,.08,.08,.55,.65,.08,.94,.92,.10,.08,.20,.03,.03,.12},
        {.12,.08,.06,.05,.25,.22,.10,.12,.08,.60,.94,.08,.03,.02,.08},
        {.15,.35,.82,.06,.55,.50,.05,.03,.06,.18,.04,.88,.35,.03,.18},
        {.35,.16,.08,.96,.40,.40,.65,.02,.08,.04,.03,.12,.02,.02,.10},
        {.06,.06,.05,.06,.16,.15,.10,.02,.04,.96,.45,.08,.02,.02,.08},
        {.88,.72,.42,.15,.90,.86,.08,.08,.18,.03,.03,.30,.12,.82,.85}
    };
    public static void generate(Path file) throws Exception {
        Random random = new Random(SEED);
        List<String> rows = new ArrayList<>();
        for (int c = 0; c < Schema.DISEASES.length; c++) {
            for (int n = 0; n < PER_CLASS; n++) {
                StringBuilder row = new StringBuilder();
                for (double p : PROFILES[c]) row.append(random.nextDouble() < p ? '1' : '0').append(',');
                rows.add(row.append(Schema.DISEASES[c]).toString());
            }
        }
        Collections.shuffle(rows, random);
        StringBuilder text = new StringBuilder("% SINTETICO. NO VALIDADO CLINICAMENTE.\n% Generador Java: semilla 20261127; 25 muestras/clase.\n@relation enfermedades_caninas_sinteticas\n\n");
        for (String s : Schema.SYMPTOMS) text.append("@attribute ").append(s).append(" {0,1}\n");
        text.append("@attribute enfermedad {").append(String.join(",", Schema.DISEASES)).append("}\n\n@data\n");
        for (String row : rows) text.append(row).append('\n');
        Path parent = file.toAbsolutePath().getParent();
        Files.createDirectories(parent);
        Files.write(file, text.toString().getBytes(StandardCharsets.UTF_8));
        System.out.println("Dataset generado: " + file + " (" + rows.size() + " filas)");
    }
}
