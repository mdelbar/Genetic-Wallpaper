
package geneticwallpaper;

import java.awt.Dimension;
import javax.swing.JFrame;

/**
 *
 * @author Matthias Delbar
 */
public class DebugFrame extends JFrame {


    public DebugFrame(Wallpaper wp, int width, int height, String title) {
        super(title);
        setContentPane(new DebugPanel(wp));
        setMinimumSize(new Dimension(width + 10, height + 30));
        setVisible(true);
        setDefaultCloseOperation(EXIT_ON_CLOSE);
    }
}
