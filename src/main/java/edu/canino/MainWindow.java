package edu.canino;

import com.formdev.flatlaf.FlatLaf;
import com.formdev.flatlaf.FlatLightLaf;
import java.awt.BasicStroke;
import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Component;
import java.awt.Cursor;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.Font;
import java.awt.FontMetrics;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.GraphicsEnvironment;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.GridLayout;
import java.awt.Insets;
import java.awt.LayoutManager;
import java.awt.RenderingHints;
import java.nio.file.Path;
import java.util.Map;
import java.util.concurrent.Callable;
import java.util.function.Consumer;
import javax.swing.BorderFactory;
import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JComboBox;
import javax.swing.JComponent;
import javax.swing.JFileChooser;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTabbedPane;
import javax.swing.JTextArea;
import javax.swing.JTextField;
import javax.swing.SwingConstants;
import javax.swing.SwingUtilities;
import javax.swing.SwingWorker;
import javax.swing.UIManager;
import javax.swing.filechooser.FileNameExtensionFilter;

/**
 * Interfaz Swing (FlatLaf). Pestaña 1: entrenar ambos modelos. Pestaña 2: marcar síntomas,
 * cargar el modelo elegido desde disco y mostrar la clase sugerida y la distribución completa.
 */
public final class MainWindow extends JFrame {
    private static final long serialVersionUID = 1L;

    // Paleta compartida con la presentación del proyecto.
    static final Color TEAL_DARK = new Color(0x0E3B43);
    static final Color TEAL = new Color(0x1F7A72);
    static final Color TEAL_LIGHT = new Color(0xA9D2CB);
    static final Color TEAL_PALE = new Color(0xE3EFED);
    static final Color AMBER = new Color(0xE07A1F);
    static final Color AMBER_PALE = new Color(0xFDF1E4);
    static final Color RED = new Color(0xB23A48);
    static final Color BACKGROUND = new Color(0xF1F5F4);
    static final Color TEXT = new Color(0x1B2A2F);
    static final Color MUTED = new Color(0x5F7176);

    /** Nombres legibles de las clases, en el orden de {@link Schema#DISEASES}. */
    static final String[] DISEASE_LABELS = {"Parvovirosis", "Moquillo", "Rabia", "Otitis",
        "Parasitosis intestinal", "Traqueobronquitis infecciosa", "Dermatitis alérgica", "Leptospirosis"};

    /** Agrupación clínica de los 15 síntomas (índices de {@link Schema#SYMPTOMS}). */
    private static final String[] GROUPS = {"Generales", "Digestivo", "Respiratorio", "Neurológico", "Piel y oído"};
    private static final int[][] GROUP_INDEX = {{0, 4, 5, 11, 14}, {1, 2, 12, 13}, {3, 6}, {7, 8}, {9, 10}};

    private static final String[] MODEL_OPTIONS = {"Naive Bayes (recomendado)", "J48 · árbol de decisión", "Otro archivo .model…"};

    private final JCheckBox[] symptoms = new JCheckBox[Schema.SYMPTOMS.length];
    private final JTextField modelsDir = new JTextField("modelos");
    private String customModel;

    public MainWindow() {
        super("Diagnosticador canino — demostración educativa");
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        JPanel root = new JPanel(new BorderLayout());
        root.setBackground(BACKGROUND);
        JPanel top = new JPanel(new BorderLayout());
        top.add(header(), BorderLayout.NORTH);
        top.add(notice(), BorderLayout.SOUTH);
        root.add(top, BorderLayout.NORTH);
        JTabbedPane tabs = new JTabbedPane();
        tabs.putClientProperty("JTabbedPane.tabHeight", 40);
        tabs.addTab("1. Entrenar", trainingPanel());
        tabs.addTab("2. Diagnosticar", predictionPanel());
        tabs.setSelectedIndex(1);
        root.add(tabs, BorderLayout.CENTER);
        setContentPane(root);
        setMinimumSize(new Dimension(1000, 720));
        setSize(1200, 820);
        setLocationRelativeTo(null);
    }

    // ---------------------------------------------------------------- cabecera y avisos
    private JComponent header() {
        JPanel bar = new JPanel(new BorderLayout(16, 0));
        bar.setBackground(TEAL_DARK);
        bar.setBorder(BorderFactory.createEmptyBorder(16, 24, 16, 24));
        JLabel badge = new JLabel("DC", SwingConstants.CENTER) {
            private static final long serialVersionUID = 1L;
            @Override protected void paintComponent(Graphics g) {
                Graphics2D g2 = smooth(g);
                g2.setColor(TEAL);
                g2.fillOval(0, 0, getWidth() - 1, getHeight() - 1);
                g2.dispose();
                super.paintComponent(g);
            }
        };
        badge.setPreferredSize(new Dimension(46, 46));
        badge.setForeground(Color.WHITE);
        badge.setFont(badge.getFont().deriveFont(Font.BOLD, 17f));
        bar.add(badge, BorderLayout.WEST);
        JPanel titles = transparent(new GridLayout(0, 1));
        JLabel title = new JLabel("Diagnosticador de enfermedades caninas");
        title.setForeground(Color.WHITE);
        title.setFont(title.getFont().deriveFont(Font.BOLD, 21f));
        JLabel subtitle = new JLabel("Clasificación supervisada con J48 y Naive Bayes · Weka " + weka.core.Version.VERSION);
        subtitle.setForeground(TEAL_LIGHT);
        subtitle.setFont(subtitle.getFont().deriveFont(13.5f));
        titles.add(title);
        titles.add(subtitle);
        bar.add(titles, BorderLayout.CENTER);
        return bar;
    }

    private JComponent notice() {
        JPanel strip = new JPanel(new BorderLayout(10, 0));
        strip.setBackground(AMBER_PALE);
        strip.setBorder(BorderFactory.createEmptyBorder(9, 24, 9, 24));
        JLabel tag = new JLabel("AVISO");
        tag.setForeground(AMBER.darker());
        tag.setFont(tag.getFont().deriveFont(Font.BOLD, 12.5f));
        JLabel text = new JLabel(Schema.WARNING + " Ante signos graves o sospecha de rabia, acuda al veterinario de inmediato.");
        text.setForeground(TEXT);
        text.setToolTipText("<html><body style='width:420px'>" + Schema.URGENT + "</body></html>");
        strip.add(tag, BorderLayout.WEST);
        strip.add(text, BorderLayout.CENTER);
        return strip;
    }

    // ---------------------------------------------------------------- pestaña entrenar
    private JPanel trainingPanel() {
        JPanel page = page();
        Card card = new Card(new GridBagLayout());
        GridBagConstraints c = new GridBagConstraints();
        c.gridx = 0; c.gridwidth = 3; c.anchor = GridBagConstraints.WEST; c.fill = GridBagConstraints.HORIZONTAL; c.weightx = 1;
        c.insets = new Insets(0, 0, 4, 0);
        card.add(heading("Entrenar los modelos"), c);
        c.insets = new Insets(0, 0, 18, 0);
        card.add(muted("Lee el ARFF, entrena J48 y Naive Bayes con todas las filas y guarda ambos archivos .model en la carpeta elegida."), c);
        JTextField arff = new JTextField("dataset.arff");
        c.gridwidth = 1; c.insets = new Insets(0, 0, 10, 12);
        addFileRow(card, c, 2, "Dataset ARFF", arff, false);
        addFileRow(card, c, 3, "Carpeta de salida", modelsDir, true);
        JButton train = primary("Entrenar ambos modelos");
        c.gridx = 1; c.gridy = 4; c.gridwidth = 1; c.fill = GridBagConstraints.NONE; c.insets = new Insets(8, 0, 0, 0);
        card.add(train, c);
        page.add(card, BorderLayout.NORTH);

        JTextArea log = new JTextArea("Los archivos .model incluyen la cabecera del dataset y su SHA-256.\n"
            + "Cargue solo modelos locales de confianza: la deserialización Java no es segura para archivos ajenos.");
        log.setEditable(false); log.setLineWrap(true); log.setWrapStyleWord(true);
        log.setFont(new Font(Font.MONOSPACED, Font.PLAIN, 13));
        log.setForeground(TEXT); log.setBackground(Color.WHITE);
        log.setBorder(BorderFactory.createEmptyBorder(14, 16, 14, 16));
        JScrollPane scroll = new JScrollPane(log);
        scroll.setBorder(BorderFactory.createLineBorder(new Color(0xDCE5E3)));
        JPanel logBox = transparent(new BorderLayout(0, 8));
        logBox.setBorder(BorderFactory.createEmptyBorder(18, 0, 0, 0));
        logBox.add(label("Registro", Font.BOLD, 14f, TEXT), BorderLayout.NORTH);
        logBox.add(scroll, BorderLayout.CENTER);
        page.add(logBox, BorderLayout.CENTER);

        train.addActionListener(e -> {
            if (JOptionPane.showConfirmDialog(this, "¿Entrenar y reemplazar los modelos en la carpeta indicada?", "Confirmar", JOptionPane.YES_NO_OPTION) != JOptionPane.YES_OPTION) return;
            final String input = arff.getText().trim(), output = modelsDir.getText().trim();
            log.setText("Entrenando J48 y Naive Bayes…");
            work(train, () -> { new TrainingService().train(Path.of(input), Path.of(output)); return Path.of(output).toAbsolutePath(); },
                dir -> log.setText("Modelos guardados correctamente:\n  " + dir.resolve("J48.model") + "\n  " + dir.resolve("NaiveBayes.model")
                    + "\n\nLa pestaña Diagnosticar usará esta carpeta.\n" + Schema.WARNING),
                err -> log.setText("No se pudo entrenar: " + err + "\n\n" + Schema.WARNING));
        });
        return page;
    }

    private void addFileRow(JPanel card, GridBagConstraints c, int row, String text, JTextField field, boolean directory) {
        c.gridy = row; c.gridx = 0; c.weightx = 0; c.fill = GridBagConstraints.NONE;
        card.add(label(text, Font.BOLD, 13.5f, TEXT), c);
        c.gridx = 1; c.weightx = 1; c.fill = GridBagConstraints.HORIZONTAL;
        field.setPreferredSize(new Dimension(420, 34));
        card.add(field, c);
        c.gridx = 2; c.weightx = 0; c.fill = GridBagConstraints.NONE; c.insets = new Insets(0, 0, 10, 0);
        JButton browse = new JButton("Examinar…");
        browse.addActionListener(e -> {
            JFileChooser chooser = new JFileChooser(".");
            chooser.setFileSelectionMode(directory ? JFileChooser.DIRECTORIES_ONLY : JFileChooser.FILES_ONLY);
            if (!directory) chooser.setFileFilter(new FileNameExtensionFilter("Weka ARFF", "arff"));
            if (chooser.showOpenDialog(this) == JFileChooser.APPROVE_OPTION) field.setText(chooser.getSelectedFile().getAbsolutePath());
        });
        card.add(browse, c);
        c.insets = new Insets(0, 0, 10, 12);
    }

    // ---------------------------------------------------------------- pestaña diagnosticar
    private JPanel predictionPanel() {
        JPanel page = page();
        JPanel columns = transparent(new GridBagLayout());
        GridBagConstraints c = new GridBagConstraints();
        c.fill = GridBagConstraints.BOTH; c.weighty = 1; c.gridy = 0;

        // Columna izquierda: síntomas y controles.
        Card left = new Card(new BorderLayout(0, 14));
        JPanel intro = transparent(new GridLayout(0, 1, 0, 2));
        intro.add(heading("Síntomas observados"));
        intro.add(muted("Marcado = presente · sin marcar = ausente (no «desconocido»)."));
        left.add(intro, BorderLayout.NORTH);
        JPanel groups = transparent(new GridLayout(0, 2, 14, 14));
        for (int g = 0; g < GROUPS.length; g++) groups.add(symptomGroup(g));
        groups.add(modelSelector());
        left.add(groups, BorderLayout.CENTER);

        JPanel actions = transparent(new BorderLayout(0, 10));
        JCheckBox acknowledge = new JCheckBox("Entiendo que es una simulación y que los síntomas no marcados se codifican como ausentes.");
        acknowledge.setFont(acknowledge.getFont().deriveFont(12.5f));
        acknowledge.setOpaque(false);
        actions.add(acknowledge, BorderLayout.NORTH);
        JPanel buttons = transparent(new FlowLayout(FlowLayout.LEFT, 0, 0));
        JButton predict = primary("Calcular orientación");
        JButton clear = new JButton("Limpiar");
        clear.setPreferredSize(new Dimension(110, 40));
        buttons.add(predict);
        buttons.add(Box.createHorizontalStrut(10));
        buttons.add(clear);
        actions.add(buttons, BorderLayout.SOUTH);
        left.add(actions, BorderLayout.SOUTH);
        c.gridx = 0; c.weightx = 0.52; c.insets = new Insets(0, 0, 0, 16);
        columns.add(left, c);

        // Columna derecha: resultado.
        ResultPanel result = new ResultPanel();
        c.gridx = 1; c.weightx = 0.48; c.insets = new Insets(0, 0, 0, 0);
        columns.add(result, c);
        page.add(columns, BorderLayout.CENTER);

        clear.addActionListener(e -> { for (JCheckBox box : symptoms) box.setSelected(false); result.reset(); });
        predict.addActionListener(e -> {
            if (!acknowledge.isSelected()) { JOptionPane.showMessageDialog(this, "Confirme primero las limitaciones y la codificación."); return; }
            boolean[] values = new boolean[symptoms.length]; boolean any = false;
            for (int i = 0; i < values.length; i++) { values[i] = symptoms[i].isSelected(); any |= values[i]; }
            if (!any) { JOptionPane.showMessageDialog(this, "Sin síntomas no se calcula una orientación. El modelo no incluye la clase «sano»."); return; }
            final Path path = selectedModel();
            if (path == null) return;
            result.loading();
            work(predict, () -> {
                PredictionService service = new PredictionService();
                ModelBundle bundle = service.load(path);
                return new Object[] {bundle, service.predict(bundle, values)};
            }, r -> result.show((ModelBundle) r[0], (double[]) r[1]), result::error);
        });
        return page;
    }

    private JComboBox<String> modelBox;
    private JLabel modelPath;

    private JPanel modelSelector() {
        JPanel box = groupBox("Modelo");
        modelBox = new JComboBox<>(MODEL_OPTIONS);
        modelBox.setAlignmentX(Component.LEFT_ALIGNMENT);
        modelBox.setMaximumSize(new Dimension(Integer.MAX_VALUE, 34));
        modelPath = label("", Font.PLAIN, 11.5f, MUTED);
        modelPath.setAlignmentX(Component.LEFT_ALIGNMENT);
        modelBox.addActionListener(e -> {
            if (modelBox.getSelectedIndex() == 2) {
                JFileChooser chooser = new JFileChooser(modelsDir.getText().trim());
                chooser.setFileFilter(new FileNameExtensionFilter("Modelo de esta aplicación (.model)", "model"));
                if (chooser.showOpenDialog(this) == JFileChooser.APPROVE_OPTION) customModel = chooser.getSelectedFile().getAbsolutePath();
                else if (customModel == null) modelBox.setSelectedIndex(0);
            }
            refreshModelPath();
        });
        refreshModelPath();
        box.add(modelBox);
        box.add(Box.createVerticalStrut(6));
        box.add(modelPath);
        return box;
    }

    private Path selectedModel() {
        int i = modelBox.getSelectedIndex();
        if (i == 2) return customModel == null ? null : Path.of(customModel);
        return Path.of(modelsDir.getText().trim(), i == 0 ? "NaiveBayes.model" : "J48.model");
    }

    private void refreshModelPath() {
        Path p = selectedModel();
        modelPath.setText(p == null ? "Ningún archivo elegido" : p.toString());
        modelPath.setToolTipText(p == null ? null : p.toAbsolutePath().toString());
    }

    private JPanel symptomGroup(int g) {
        JPanel box = groupBox(GROUPS[g]);
        for (int index : GROUP_INDEX[g]) {
            JCheckBox check = new JCheckBox(Schema.LABELS[index]);
            check.setOpaque(false);
            check.setFont(check.getFont().deriveFont(14f));
            check.setForeground(TEXT);
            check.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
            check.setToolTipText("Atributo ARFF: " + Schema.SYMPTOMS[index]);
            symptoms[index] = check;
            box.add(check);
            box.add(Box.createVerticalStrut(2));
        }
        return box;
    }

    private static JPanel groupBox(String title) {
        Card box = new Card(null, TEAL_PALE, 12);
        box.setLayout(new BoxLayout(box, BoxLayout.Y_AXIS));
        box.setBorder(BorderFactory.createEmptyBorder(10, 12, 10, 12));
        JLabel head = label(title.toUpperCase(), Font.BOLD, 11.5f, TEAL);
        head.setAlignmentX(Component.LEFT_ALIGNMENT);
        box.add(head);
        box.add(Box.createVerticalStrut(6));
        return box;
    }

    // ---------------------------------------------------------------- panel de resultado
    private static final class ResultPanel extends Card {
        private static final long serialVersionUID = 1L;
        private final JLabel kicker = label("RESULTADO DEL MODELO", Font.BOLD, 11.5f, TEAL);
        private final JLabel disease = label("", Font.BOLD, 28f, TEXT);
        private final JLabel detail = label("", Font.PLAIN, 14f, MUTED);
        private final ProbabilityBars bars = new ProbabilityBars();
        private final JTextArea footnote = new JTextArea();

        ResultPanel() {
            super(new BorderLayout(0, 16));
            disease.setName("resultado.clase");
            JPanel head = transparent(new GridLayout(0, 1, 0, 4));
            head.add(kicker); head.add(disease); head.add(detail);
            add(head, BorderLayout.NORTH);
            JPanel body = transparent(new BorderLayout(0, 8));
            body.add(label("Distribución de probabilidad entre las 8 clases", Font.BOLD, 13.5f, TEXT), BorderLayout.NORTH);
            body.add(bars, BorderLayout.CENTER);
            add(body, BorderLayout.CENTER);
            footnote.setEditable(false); footnote.setLineWrap(true); footnote.setWrapStyleWord(true); footnote.setOpaque(false);
            footnote.setFont(footnote.getFont().deriveFont(12f)); footnote.setForeground(MUTED); footnote.setFocusable(false);
            footnote.setText("Probabilidades del modelo, no riesgo clínico: un 100 % no confirma una enfermedad y un 0 % no la descarta. " + Schema.URGENT);
            add(footnote, BorderLayout.SOUTH);
            reset();
        }
        void reset() {
            disease.setForeground(TEXT); disease.setText("Sin calcular");
            detail.setText("Marque los síntomas y pulse «Calcular orientación».");
            bars.set(null);
        }
        void loading() { disease.setForeground(TEXT); disease.setText("Calculando…"); detail.setText("Cargando el modelo desde el disco."); }
        void show(ModelBundle model, double[] p) {
            int best = weka.core.Utils.maxIndex(p);
            disease.setForeground(TEAL_DARK);
            disease.setText(DISEASE_LABELS[best]);
            detail.setText(String.format(java.util.Locale.ROOT, "%.1f %% · modelo %s · Weka %s", 100 * p[best], model.algorithm, model.wekaVersion));
            bars.set(p);
            getAccessibleContext().setAccessibleDescription(PredictionService.format(p));
        }
        void error(String message) {
            disease.setForeground(RED); disease.setText("No se pudo calcular");
            detail.setText("<html>" + message.replace("<", "&lt;") + "</html>");
            bars.set(null);
        }
    }

    /** Barras horizontales de probabilidad; la clase sugerida se resalta en ámbar. */
    private static final class ProbabilityBars extends JComponent {
        private static final long serialVersionUID = 1L;
        private static final int ROW = 38;
        private double[] p;
        ProbabilityBars() { setPreferredSize(new Dimension(420, ROW * DISEASE_LABELS.length)); }
        void set(double[] values) { p = values == null ? null : values.clone(); repaint(); }
        @Override protected void paintComponent(Graphics g) {
            Graphics2D g2 = smooth(g);
            Font base = getFont() == null ? UIManager.getFont("Label.font") : getFont();
            Font name = base.deriveFont(13.5f), value = base.deriveFont(Font.BOLD, 13.5f);
            FontMetrics fm = g2.getFontMetrics(name);
            int labelW = 0;
            for (String s : DISEASE_LABELS) labelW = Math.max(labelW, fm.stringWidth(s));
            labelW += 14;
            int valueW = g2.getFontMetrics(value).stringWidth("100.0 %") + 10;
            int trackX = labelW, trackW = Math.max(40, getWidth() - labelW - valueW);
            int best = p == null ? -1 : weka.core.Utils.maxIndex(p);
            for (int i = 0; i < DISEASE_LABELS.length; i++) {
                int y = i * ROW, mid = y + ROW / 2;
                g2.setFont(i == best ? value : name);
                g2.setColor(i == best ? TEXT : MUTED.darker());
                g2.drawString(DISEASE_LABELS[i], 0, mid + fm.getAscent() / 2 - 2);
                int h = 14, top = mid - h / 2;
                g2.setColor(new Color(0xE6ECEB));
                g2.fillRoundRect(trackX, top, trackW, h, h, h);
                if (p != null) {
                    int w = (int) Math.round(trackW * Math.max(0, Math.min(1, p[i])));
                    if (w >= 4) {
                        g2.setColor(i == best ? AMBER : TEAL);
                        g2.fillRoundRect(trackX, top, Math.max(w, h), h, h, h);
                    }
                    g2.setFont(value);
                    g2.setColor(i == best ? AMBER.darker() : TEXT);
                    String pct = String.format(java.util.Locale.ROOT, "%.1f %%", 100 * p[i]);
                    g2.drawString(pct, getWidth() - g2.getFontMetrics().stringWidth(pct), mid + fm.getAscent() / 2 - 2);
                }
            }
            g2.dispose();
        }
    }

    // ---------------------------------------------------------------- utilidades de estilo
    /** Panel blanco con esquinas redondeadas. */
    private static class Card extends JPanel {
        private static final long serialVersionUID = 1L;
        private final Color fill; private final int arc;
        Card(LayoutManager layout) { this(layout, Color.WHITE, 18); setBorder(BorderFactory.createEmptyBorder(22, 24, 22, 24)); }
        Card(LayoutManager layout, Color fill, int arc) { super(layout); this.fill = fill; this.arc = arc; setOpaque(false); }
        @Override protected void paintComponent(Graphics g) {
            Graphics2D g2 = smooth(g);
            g2.setColor(fill);
            g2.fillRoundRect(0, 0, getWidth() - 1, getHeight() - 1, arc, arc);
            if (fill.equals(Color.WHITE)) {
                g2.setColor(new Color(0xDCE5E3));
                g2.setStroke(new BasicStroke(1f));
                g2.drawRoundRect(0, 0, getWidth() - 1, getHeight() - 1, arc, arc);
            }
            g2.dispose();
            super.paintComponent(g);
        }
    }

    private static Graphics2D smooth(Graphics g) {
        Graphics2D g2 = (Graphics2D) g.create();
        g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        g2.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING, RenderingHints.VALUE_TEXT_ANTIALIAS_ON);
        return g2;
    }
    private static JPanel page() {
        JPanel page = new JPanel(new BorderLayout());
        page.setBackground(BACKGROUND);
        page.setBorder(BorderFactory.createEmptyBorder(20, 24, 20, 24));
        return page;
    }
    private static JPanel transparent(LayoutManager layout) { JPanel p = new JPanel(layout); p.setOpaque(false); return p; }
    private static JLabel label(String text, int style, float size, Color color) {
        JLabel l = new JLabel(text); l.setFont(l.getFont().deriveFont(style, size)); l.setForeground(color); return l;
    }
    private static JLabel heading(String text) { return label(text, Font.BOLD, 19f, TEXT); }
    private static JLabel muted(String text) { return label(text, Font.PLAIN, 13.5f, MUTED); }
    private static JButton primary(String text) {
        JButton b = new JButton(text);
        b.setBackground(TEAL); b.setForeground(Color.WHITE);
        b.setFont(b.getFont().deriveFont(Font.BOLD, 14f));
        b.setPreferredSize(new Dimension(Math.max(200, b.getPreferredSize().width + 40), 40));
        b.putClientProperty("JButton.buttonType", "roundRect");
        b.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        return b;
    }

    /** Ejecuta la acción fuera del hilo de la interfaz; la vista solo se actualiza en done(). */
    private <T> void work(JButton button, Callable<T> action, Consumer<T> onSuccess, Consumer<String> onError) {
        button.setEnabled(false);
        new SwingWorker<T, Void>() {
            @Override protected T doInBackground() throws Exception { return action.call(); }
            @Override protected void done() {
                try { onSuccess.accept(get()); }
                catch (Exception ex) { Throwable cause = ex.getCause() == null ? ex : ex.getCause(); onError.accept(String.valueOf(cause.getMessage())); }
                finally { button.setEnabled(true); }
            }
        }.execute();
    }

    /** Aplica FlatLaf con el color de acento del proyecto; si falla, se mantiene el aspecto por defecto. */
    static void installLookAndFeel() {
        try {
            FlatLaf.setGlobalExtraDefaults(Map.of("@accentColor", "#1F7A72"));
            FlatLightLaf.setup();
            UIManager.put("Component.arc", 10);
            UIManager.put("Button.arc", 10);
            UIManager.put("CheckBox.arc", 6);
            UIManager.put("TabbedPane.selectedBackground", Color.WHITE);
            UIManager.put("TabbedPane.underlineColor", TEAL);
            UIManager.put("TabbedPane.tabInsets", new Insets(4, 18, 4, 18));
            UIManager.put("defaultFont", UIManager.getFont("defaultFont").deriveFont(14f));
        } catch (RuntimeException | LinkageError e) {
            System.err.println("Aviso: no se pudo aplicar FlatLaf (" + e + "); se usa el aspecto estándar de Swing.");
        }
    }

    public static void launch() {
        if (GraphicsEnvironment.isHeadless()) throw new IllegalStateException("Swing requiere escritorio gráfico. Use los comandos entrenar/experimento/diagnosticar en este entorno.");
        SwingUtilities.invokeLater(() -> { installLookAndFeel(); new MainWindow().setVisible(true); });
    }
}
