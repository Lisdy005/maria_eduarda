package edu.canino;

import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.GraphicsEnvironment;
import java.awt.GridLayout;
import java.nio.file.Path;
import java.util.concurrent.Callable;
import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JFileChooser;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTabbedPane;
import javax.swing.JTextArea;
import javax.swing.JTextField;
import javax.swing.SwingUtilities;
import javax.swing.SwingWorker;

public final class MainWindow extends JFrame {
    private static final long serialVersionUID = 1L;
    private final JCheckBox[] symptoms = new JCheckBox[Schema.SYMPTOMS.length];
    public MainWindow() {
        super("Orientador canino — demostración educativa");
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setLayout(new BorderLayout(8, 8));
        JTextArea notice = new JTextArea(Schema.WARNING + "\n" + Schema.URGENT);
        notice.setEditable(false); notice.setLineWrap(true); notice.setWrapStyleWord(true);
        notice.setBackground(new java.awt.Color(255, 240, 214));
        notice.setBorder(BorderFactory.createEmptyBorder(12, 12, 12, 12));
        add(notice, BorderLayout.NORTH);
        JTabbedPane tabs = new JTabbedPane();
        tabs.addTab("1. Entrenar", trainingPanel());
        tabs.addTab("2. Orientación sintética", predictionPanel());
        add(tabs, BorderLayout.CENTER);
        setMinimumSize(new Dimension(850, 750)); setSize(1050, 850); setLocationRelativeTo(null);
    }
    private JPanel fileRow(String label, JTextField field, boolean directory) {
        JPanel row = new JPanel(new BorderLayout(8, 8)); row.add(new JLabel(label), BorderLayout.WEST); row.add(field);
        JButton browse = new JButton("Examinar"); row.add(browse, BorderLayout.EAST);
        browse.addActionListener(e -> {
            JFileChooser chooser = new JFileChooser();
            chooser.setFileSelectionMode(directory ? JFileChooser.DIRECTORIES_ONLY : JFileChooser.FILES_ONLY);
            if (chooser.showOpenDialog(this) == JFileChooser.APPROVE_OPTION) field.setText(chooser.getSelectedFile().getAbsolutePath());
        });
        return row;
    }
    private JPanel trainingPanel() {
        JPanel panel = new JPanel(new BorderLayout(10, 10)); panel.setBorder(BorderFactory.createEmptyBorder(16, 16, 16, 16));
        JPanel controls = new JPanel(new GridLayout(0, 1, 8, 8));
        JTextField arff = new JTextField("dataset.arff"); JTextField dir = new JTextField("modelos");
        controls.add(fileRow("ARFF: ", arff, false)); controls.add(fileRow("Salida: ", dir, true));
        controls.add(new JLabel("Entrena J48 y Naive Bayes; reemplaza los modelos de la carpeta elegida."));
        JButton train = new JButton("Entrenar ambos modelos"); controls.add(train);
        JTextArea log = area(); log.setText("Los archivos .model incluyen la cabecera del dataset y su SHA-256.\nNo cargue modelos de fuentes desconocidas.");
        panel.add(controls, BorderLayout.NORTH); panel.add(new JScrollPane(log));
        train.addActionListener(e -> {
            if (JOptionPane.showConfirmDialog(this, "¿Entrenar y reemplazar modelos en la carpeta indicada?", "Confirmar", JOptionPane.YES_NO_OPTION) != JOptionPane.YES_OPTION) return;
            final String input = arff.getText().trim(), output = dir.getText().trim();
            work(train, log, () -> { new TrainingService().train(Path.of(input), Path.of(output)); return "Modelos J48.model y NaiveBayes.model guardados en " + Path.of(output).toAbsolutePath() + "\n" + Schema.WARNING; });
        });
        return panel;
    }
    private JPanel predictionPanel() {
        JPanel panel = new JPanel(new BorderLayout(8, 8)); panel.setBorder(BorderFactory.createEmptyBorder(12, 12, 12, 12));
        JPanel top = new JPanel(new GridLayout(0, 1, 5, 5));
        JTextField model = new JTextField("modelos/NaiveBayes.model");
        top.add(fileRow("Modelo local de confianza: ", model, false));
        top.add(new JLabel("Seleccione J48.model o NaiveBayes.model. Marcado = presente; desmarcado = ausente, NO desconocido."));
        JPanel grid = new JPanel(new GridLayout(0, 3, 8, 8));
        for (int i = 0; i < symptoms.length; i++) { symptoms[i] = new JCheckBox(Schema.LABELS[i]); grid.add(symptoms[i]); }
        JPanel centerTop = new JPanel(new BorderLayout(8, 8)); centerTop.add(top, BorderLayout.NORTH); centerTop.add(grid);
        JCheckBox acknowledge = new JCheckBox("Entiendo que es una simulación y que los síntomas no marcados se codifican como ausentes.");
        JPanel actions = new JPanel(new GridLayout(0, 1, 5, 5)); actions.add(acknowledge);
        JButton predict = new JButton("Calcular orientación (no diagnóstico clínico)"); actions.add(predict);
        centerTop.add(actions, BorderLayout.SOUTH); panel.add(centerTop, BorderLayout.NORTH);
        JTextArea result = area(); result.setText("Marque los signos observados. Si no conoce un signo, no use esta simulación como evaluación clínica.\n\nNo se modelan animales sanos ni enfermedades fuera de las ocho clases.\n\n" + Schema.URGENT);
        panel.add(new JScrollPane(result));
        predict.addActionListener(e -> {
            if (!acknowledge.isSelected()) { JOptionPane.showMessageDialog(this, "Confirme primero las limitaciones y la codificación."); return; }
            boolean[] values = new boolean[symptoms.length]; boolean any = false;
            for (int i = 0; i < values.length; i++) { values[i] = symptoms[i].isSelected(); any |= values[i]; }
            if (!any) { JOptionPane.showMessageDialog(this, "Sin síntomas no se calcula una orientación. El modelo no incluye la clase sano."); return; }
            final String path = model.getText().trim();
            work(predict, result, () -> {
                PredictionService service = new PredictionService(); ModelBundle bundle = service.load(Path.of(path));
                return "Modelo: " + bundle.algorithm + " | Weka: " + bundle.wekaVersion + "\n" + PredictionService.format(service.predict(bundle, values));
            });
        });
        return panel;
    }
    private static JTextArea area() {
        JTextArea text = new JTextArea(); text.setEditable(false); text.setLineWrap(true); text.setWrapStyleWord(true);
        text.setFont(new Font(Font.MONOSPACED, Font.PLAIN, 13)); text.setBorder(BorderFactory.createEmptyBorder(12, 12, 12, 12)); return text;
    }
    private void work(JButton button, JTextArea output, Callable<String> action) {
        button.setEnabled(false); output.setText("Procesando…");
        new SwingWorker<String, Void>() {
            @Override protected String doInBackground() throws Exception { return action.call(); }
            @Override protected void done() {
                try { output.setText(get()); }
                catch (Exception ex) { Throwable cause = ex.getCause() == null ? ex : ex.getCause(); output.setText("No se pudo completar: " + cause.getMessage() + "\n\n" + Schema.WARNING); }
                finally { button.setEnabled(true); output.setCaretPosition(0); }
            }
        }.execute();
    }
    public static void launch() {
        if (GraphicsEnvironment.isHeadless()) throw new IllegalStateException("Swing requiere escritorio gráfico. Use los comandos entrenar/experimento/diagnosticar en este entorno.");
        SwingUtilities.invokeLater(() -> new MainWindow().setVisible(true));
    }
}
