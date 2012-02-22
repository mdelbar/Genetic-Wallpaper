
package geneticwallpaper;

import java.util.Random;

/**
 *
 * @author Matthias Delbar
 */
public class Wallpaper {

    public int[][] pixels;
    public int width, height;
    public Random r = new Random();

    public Wallpaper(int width, int height) {
        this.width = width;
        this.height = height;
        pixels = new int[height][width];
        initPixels();
    }
    public Wallpaper(Wallpaper wp) {
        this.height = wp.height;
        this.width = wp.width;
        this.pixels = new int[height][width];
        for(int i = 0; i < wp.pixels.length; i++) {
            System.arraycopy(wp.pixels[i], 0, this.pixels[i], 0, wp.pixels[i].length);
        }
    }

    private void initPixels() {
        for(int y = 0; y < height; y++) {
            for(int x = 0; x < width; x++) {
                pixels[y][x] = getRandomPixel();
            }
        }
    }

    public int getRandomPixel() {
        return r.nextInt() | (255 << 24);
    }
}
