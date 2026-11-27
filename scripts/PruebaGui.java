import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.File;
import java.util.*;
import java.util.List;
import javax.imageio.ImageIO;
import javax.swing.*;

/** Prueba GUI automatizada: abre MainWindow, marca síntomas, pulsa Calcular y captura PNG. Uso en docs/PRUEBAS.md. */
public class PruebaGui {
    static List<Component> all(Container c, List<Component> out) {
        for (Component k : c.getComponents()) { out.add(k); if (k instanceof Container) all((Container) k, out); }
        return out;
    }
    static void shot(JFrame f, String file) throws Exception {
        BufferedImage img = new BufferedImage(f.getWidth(), f.getHeight(), BufferedImage.TYPE_INT_RGB);
        SwingUtilities.invokeAndWait(() -> f.paint(img.getGraphics()));
        ImageIO.write(img, "png", new File(file));
    }
    static JTextArea resultArea(JFrame f) {
        JTextArea last = null;
        for (Component c : all(f.getContentPane(), new ArrayList<>())) if (c instanceof JTextArea && c.isShowing()) last = (JTextArea) c;
        return last;
    }
    public static void main(String[] a) throws Exception {
        String out = a[0];
        JOptionPane.setRootFrame(null);
        final JFrame[] fr = new JFrame[1];
        SwingUtilities.invokeAndWait(() -> { fr[0] = new edu.canino.MainWindow(); fr[0].setVisible(true); });
        JFrame f = fr[0]; Thread.sleep(800);
        shot(f, out + "/gui_1_entrenar.png");
        JTabbedPane tabs = null;
        for (Component c : all(f.getContentPane(), new ArrayList<>())) if (c instanceof JTabbedPane) tabs = (JTabbedPane) c;
        final JTabbedPane t = tabs;
        SwingUtilities.invokeAndWait(() -> t.setSelectedIndex(1)); Thread.sleep(300);
        Set<String> want = new HashSet<>(Arrays.asList("Fiebre","Vómito","Diarrea","Letargo","Pérdida de apetito","Sangre en heces"));
        List<Component> comps = all(f.getContentPane(), new ArrayList<>());
        JButton predict = null;
        for (Component c : comps) {
            if (c instanceof JCheckBox) { JCheckBox b = (JCheckBox) c; if (want.contains(b.getText()) || b.getText().startsWith("Entiendo")) SwingUtilities.invokeAndWait(() -> b.setSelected(true)); }
            if (c instanceof JButton && ((JButton) c).getText().startsWith("Calcular")) predict = (JButton) c;
        }
        final JButton p = predict;
        SwingUtilities.invokeAndWait(p::doClick);
        for (int i = 0; i < 100 && !p.isEnabled(); i++) Thread.sleep(100);
        Thread.sleep(300);
        String text = resultArea(f).getText();
        System.out.println("=== RESULTADO GUI (NaiveBayes) ===\n" + text);
        shot(f, out + "/gui_2_orientacion.png");
        System.exit(text.contains("Clase sugerida por el modelo: parvovirosis") ? 0 : 2);
    }
}
