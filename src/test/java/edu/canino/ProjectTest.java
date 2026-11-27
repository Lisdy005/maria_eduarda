package edu.canino;

import static org.junit.Assert.*;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Arrays;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.TemporaryFolder;
import weka.core.Instances;

public class ProjectTest {
    @Rule public TemporaryFolder temp = new TemporaryFolder();
    private Path data;
    @Before public void prepare() throws Exception {
        data = temp.getRoot().toPath().resolve("dataset.arff"); DataGenerator.generate(data);
    }
    @Test public void reproducibleDataset() throws Exception {
        assertEquals("64d2ba7e29beb5ecd88934640822fe1e964f72681d668e60d4e897b16a938924", DataIO.sha256(data));
        assertArrayEquals(Files.readAllBytes(Path.of("dataset.arff")), Files.readAllBytes(data));
    }
    @Test public void balancedBinarySchema() throws Exception {
        Instances rows = DataIO.load(data); assertEquals(200, rows.numInstances()); assertEquals(16, rows.numAttributes());
        int[] counts = new int[8];
        for (weka.core.Instance row : rows) {
            counts[(int) row.classValue()]++;
            for (int i = 0; i < 15; i++) assertTrue(row.value(i) == 0 || row.value(i) == 1);
        }
        for (int count : counts) assertEquals(25, count);
        assertEquals(37, DataIO.duplicatePredictors(rows));
    }
    @Test(expected = IllegalArgumentException.class) public void rejectsWrongHeader() throws Exception {
        Files.writeString(data, Files.readString(data).replace("@attribute fiebre", "@attribute incorrecto")); DataIO.load(data);
    }
    @Test(expected = IllegalArgumentException.class) public void rejectsMissingValue() throws Exception {
        Files.writeString(data, Files.readString(data).replaceFirst("(?m)^([01]),", "?,")); DataIO.load(data);
    }
    @Test(expected = IllegalArgumentException.class) public void rejectsTooSmallDataset() throws Exception {
        String text = Files.readString(data); Files.writeString(data, text.substring(0, text.indexOf("@data") + 6)); DataIO.load(data);
    }
    @Test public void modelRoundTripAndProbabilities() throws Exception {
        Path dir = temp.getRoot().toPath().resolve("models"); new TrainingService().train(data, dir);
        PredictionService service = new PredictionService();
        for (String name : TrainingService.ALGORITHMS) {
            ModelBundle model = service.load(dir.resolve(name + ".model"));
            assertEquals(name, model.algorithm); assertEquals(200, model.trainingRows); assertEquals(0, model.header.numInstances());
            assertEquals(DataIO.sha256(data), model.trainingSha256);
            for (int mask = 0; mask < 128; mask++) {
                boolean[] symptoms = new boolean[15];
                for (int i = 0; i < 15; i++) symptoms[i] = ((mask * 257) & (1 << i)) != 0;
                double[] p = service.predict(model, symptoms);
                assertEquals(8, p.length); assertEquals(1.0, Arrays.stream(p).sum(), 1e-6);
                for (double v : p) assertTrue(Double.isFinite(v) && v >= 0 && v <= 1);
            }
        }
    }
    @Test(expected = IllegalArgumentException.class) public void rejectsMissingModel() throws Exception {
        new PredictionService().load(temp.getRoot().toPath().resolve("absent.model"));
    }
    @Test(expected = IllegalArgumentException.class) public void rejectsUnknownCommand() throws Exception { Main.run(new String[]{"inexistente"}); }
    @Test(expected = IllegalArgumentException.class) public void rejectsUnknownSymptom() throws Exception { Main.run(new String[]{"diagnosticar", "x", "inventado"}); }
    @Test(expected = IllegalArgumentException.class) public void rejectsWrongArgumentCount() throws Exception { Main.run(new String[]{"diagnosticar"}); }
    @Test(expected = IllegalArgumentException.class) public void rejectsUnknownAlgorithm() { TrainingService.create("otro"); }
    @Test public void experimentGoldenAndReproducible() throws Exception {
        Path a = temp.getRoot().toPath().resolve("exp1"), b = temp.getRoot().toPath().resolve("exp2");
        new Experiment().run(data, a); new Experiment().run(data, b);
        String metrics = Files.readString(a.resolve("metricas_globales.csv"));
        assertTrue(metrics.contains("J48,0.700000,0.701221,0.700000,0.696176"));
        assertTrue(metrics.contains("NaiveBayes,0.830000,0.833654,0.830000,0.829347"));
        for (String file : new String[]{"metricas_globales.csv", "metricas_por_clase.csv", "predicciones.csv", "asignacion_pliegues.csv", "pliegues.csv"})
            assertArrayEquals(Files.readAllBytes(a.resolve(file)), Files.readAllBytes(b.resolve(file)));
        assertEquals(401, Files.readAllLines(a.resolve("predicciones.csv")).size());
        assertEquals(201, Files.readAllLines(a.resolve("asignacion_pliegues.csv")).size());
        assertEquals(21, Files.readAllLines(a.resolve("pliegues.csv")).size());
    }
}
