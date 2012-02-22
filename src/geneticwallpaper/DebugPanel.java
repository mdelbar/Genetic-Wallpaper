
package geneticwallpaper;

import java.awt.Color;
import java.awt.Graphics;
import javax.swing.JPanel;

/**
 *
 * @author Matthias Delbar
 */
public class DebugPanel extends JPanel {

    private Wallpaper wp;

    public DebugPanel(Wallpaper wp) {
        super();
        this.wp = wp;
    }

    @Override
    protected void paintComponent(Graphics g) {
        super.paintComponent(g);
        int pixelSize = 1;
        for(int y = 0; y < wp.height; y++) {
            for(int x = 0; x < wp.width; x++) {
                g.setColor(new Color(wp.pixels[y][x]));
                g.fillRect(x*pixelSize, y*pixelSize, pixelSize, pixelSize);
            }
        }
    }
}
