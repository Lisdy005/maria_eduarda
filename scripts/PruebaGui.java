import java.awt.Component;
import java.awt.Container;
import java.awt.image.BufferedImage;
import java.io.File;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import javax.imageio.ImageIO;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JTabbedPane;
import javax.swing.SwingUtilities;

/**
 * Prueba GUI automatizada: abre la ventana real (MainWindow), marca seis síntomas, pulsa
 * «Calcular orientación», lee la clase mostrada y guarda capturas PNG. Uso en docs/PRUEBAS.md.
 */
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
    public static void main(String[] a) throws Exception {
        String out = a[0];
        Class<?> window = Class.forName("edu.canino.MainWindow");
        Method laf = window.getDeclaredMethod("installLookAndFeel");
        laf.setAccessible(true);
        final JFrame[] fr = new JFrame[1];
        SwingUtilities.invokeAndWait(() -> {
            try { laf.invoke(null); fr[0] = (JFrame) window.getConstructor().newInstance(); }
            catch (ReflectiveOperationException e) { throw new IllegalStateException(e); }
            fr[0].setVisible(true);
        });
        JFrame f = fr[0];
        Thread.sleep(800);
        JTabbedPane tabs = null;
        for (Component c : all(f.getContentPane(), new ArrayList<>())) if (c instanceof JTabbedPane) tabs = (JTabbedPane) c;
        final JTabbedPane t = tabs;
        SwingUtilities.invokeAndWait(() -> t.setSelectedIndex(0)); Thread.sleep(300);
        shot(f, out + "/gui_1_entrenar.png");
        SwingUtilities.invokeAndWait(() -> t.setSelectedIndex(1)); Thread.sleep(300);
        Set<String> want = new HashSet<>(Arrays.asList("Fiebre", "Vómito", "Diarrea", "Letargo", "Pérdida de apetito", "Sangre en heces"));
        JButton predict = null;
        JLabel result = null;
        for (Component c : all(f.getContentPane(), new ArrayList<>())) {
            if (c instanceof JCheckBox) {
                JCheckBox b = (JCheckBox) c;
                if (want.contains(b.getText()) || b.getText().startsWith("Entiendo")) SwingUtilities.invokeAndWait(() -> b.setSelected(true));
            }
            if (c instanceof JButton && ((JButton) c).getText().startsWith("Calcular")) predict = (JButton) c;
            if (c instanceof JLabel && "resultado.clase".equals(c.getName())) result = (JLabel) c;
        }
        final JButton p = predict;
        SwingUtilities.invokeAndWait(p::doClick);
        for (int i = 0; i < 100 && !p.isEnabled(); i++) Thread.sleep(100);
        Thread.sleep(400);
        String shown = result.getText();
        System.out.println("=== RESULTADO GUI (NaiveBayes) ===");
        System.out.println("Clase mostrada: " + shown);
        System.out.println(result.getParent().getParent().getAccessibleContext().getAccessibleDescription());
        shot(f, out + "/gui_2_orientacion.png");
        System.exit("Parvovirosis".equals(shown) ? 0 : 2);
    }
}
