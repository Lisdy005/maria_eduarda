package edu.canino;

import java.nio.file.Path;
import java.util.Arrays;

public final class Main {
    private Main() { }
    public static void main(String[] args) {
        // Consolas/servidores con locale POSIX también deben conservar las tildes.
        System.setOut(new java.io.PrintStream(System.out, true, java.nio.charset.StandardCharsets.UTF_8));
        System.setErr(new java.io.PrintStream(System.err, true, java.nio.charset.StandardCharsets.UTF_8));
        try { run(args); }
        catch (Exception e) { System.err.println("ERROR: " + e.getMessage()); System.exit(1); }
    }
    static void run(String[] args) throws Exception {
        String command = args.length == 0 ? "gui" : args[0];
        switch (command) {
            case "generar":
                require(args, 1, 2); DataGenerator.generate(Path.of(args.length > 1 ? args[1] : "dataset.arff")); break;
            case "entrenar":
                require(args, 1, 3); new TrainingService().train(Path.of(args.length > 1 ? args[1] : "dataset.arff"), Path.of(args.length > 2 ? args[2] : "modelos")); break;
            case "experimento":
                require(args, 1, 3); new Experiment().run(Path.of(args.length > 1 ? args[1] : "dataset.arff"), Path.of(args.length > 2 ? args[2] : "resultados")); break;
            case "diagnosticar":
                require(args, 3, 3);
                boolean[] values = new boolean[Schema.SYMPTOMS.length];
                for (String token : args[2].split(",", -1)) {
                    int index = Arrays.asList(Schema.SYMPTOMS).indexOf(token.trim());
                    if (index < 0) throw new IllegalArgumentException("Síntoma desconocido: " + token + ". Opciones: " + String.join(",", Schema.SYMPTOMS));
                    values[index] = true;
                }
                PredictionService service = new PredictionService(); ModelBundle model = service.load(Path.of(args[1]));
                System.out.println("No mencionados = ausentes. Modelo: " + model.algorithm);
                System.out.println(PredictionService.format(service.predict(model, values))); break;
            case "gui": require(args, 0, 1); MainWindow.launch(); break;
            case "ayuda": case "--help": case "-h": System.out.println(help()); break;
            default: throw new IllegalArgumentException("Comando desconocido.\n" + help());
        }
    }
    private static void require(String[] args, int min, int max) {
        if (args.length < min || args.length > max) throw new IllegalArgumentException("Argumentos incorrectos.\n" + help());
    }
    private static String help() {
        return "Uso: java -jar diagnosticador-canino.jar [gui | generar [dataset.arff] | entrenar [dataset.arff [modelos]] | experimento [dataset.arff [resultados]] | diagnosticar MODELO sintoma1,sintoma2 | ayuda]\n" + Schema.WARNING;
    }
}
