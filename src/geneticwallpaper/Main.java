
package geneticwallpaper;

/**
 *
 * @author Matthias Delbar
 */
public class Main {


    public static void main(String[] args) {
        int w = 50;
        int h = 50;
        int popsize = 10;

        GeneticWallpaper cgw = new GeneticWallpaper(popsize, w, h);
        Wallpaper[] cpopulation = cgw.generate();
        
        new DebugFrame(cpopulation[0], w, h, "" + "Genetic Wallpaper");

    }

}
