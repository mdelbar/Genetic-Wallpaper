
package geneticwallpaper;

import java.util.Arrays;
import java.util.Random;
import java.util.concurrent.BrokenBarrierException;
import java.util.concurrent.CyclicBarrier;

/**
 *
 * @author Matthias Delbar
 */
public class GeneticWallpaper {

    private Wallpaper[] population;
    private int[] fitnesses;
    private int populationLimit;
    private int maxDiff;
    private int diffPerColor = 128;
    private int minDiffPerColor = 16;
    private int maxLookaround;
    private int maxX, maxY;
    private int w, h;
    private boolean calculateAllFitnesses;
    Random r = new Random();

    public GeneticWallpaper(int popsize, int width, int height) {
        population = new Wallpaper[popsize];
        fitnesses = new int[popsize];
        populationLimit = popsize;
        maxDiff = (((diffPerColor << 8) + diffPerColor) << 8) + diffPerColor;
        maxLookaround = Math.max(1, Math.min(height / 10, 15));
        maxX = width - 1;
        maxY = height - 1;
        w = width;
        h = height;
        calculateAllFitnesses = true;

        // Init random population
        for(int i = 0; i < popsize; i++) {
            population[i] = new Wallpaper(width, height);
        }
    }

    public Wallpaper[] generate() {
        int numIterations = 100000;
        int printThreshold = Math.max(1, numIterations / 10);
//        int diffDivideThreshold = Math.max(1, numIterations / 5);

        int maxFit = 8*maxLookaround*w*h;

        long timeBefore = System.currentTimeMillis();

        calculateFitnessesConc(population);
//        calculateFitnesses(population);
        population = orderPopulation(population);

        int[] fitnessesBefore = new int[fitnesses.length];
        System.arraycopy(fitnesses, 0, fitnessesBefore, 0, fitnesses.length);

        for(int i = 0; i < numIterations; i++) {
            if(i % printThreshold == 0) {
                System.out.println("Generation " + (i+1));
            }
//            if((diffPerColor > minDiffPerColor) && ((i % diffDivideThreshold) == 0)) {
//                diffPerColor /= 2;
//                diffDivideThreshold *= 2;
//                maxDiff = (((diffPerColor << 8) + diffPerColor) << 8) + diffPerColor;
//                calculateAllFitnesses = true;
//            }
            if((diffPerColor > minDiffPerColor) && ((((double)fitnesses[0]) / ((double)maxFit)) > 0.75)) {
                diffPerColor /= 2;
                maxDiff = (((diffPerColor << 8) | diffPerColor) << 8) | diffPerColor;
                calculateAllFitnesses = true;
            }

            Wallpaper[] extPop = extendPopulation(population);
            calculateFitnessesConc(extPop);
//            calculateFitnesses(extPop);
            Wallpaper[] orderedPop = orderPopulation(extPop);
            population = reducePopulation(orderedPop);
        }

        long timeAfter = System.currentTimeMillis();

        int[] fitnessesAfter = new int[fitnesses.length];
        System.arraycopy(fitnesses, 0, fitnessesAfter, 0, fitnesses.length);


        System.out.println("Fitnesses: (Maximum  = " + maxFit + ")");

        System.out.println("Before\t| After\t| Fitness (%)");
        for(int i = 0; i <= 0 * fitnessesBefore.length; i++) {
            int before = fitnessesBefore[i];
            int after = fitnessesAfter[i];
            double fitpct = (((double) after) / ((double) maxFit)) * 100.0;
            System.out.println(before + "\t| " + after + "\t| " + fitpct);
        }
        System.out.println("Time: " + ((double) (timeAfter - timeBefore)/1000.0) + " seconds");

//        int before = fitnessesBefore[0];
//        int after = fitnessesAfter[0];
//        int diff = after - before;
//        double diffpct = (((double) Math.abs(diff)) / ((double) Math.abs(before))) * 100.0;
//        double fitpct = (((double) after) / ((double) maxFit)) * 100.0;
//        System.out.println(fitpct / ((double) (timeAfter - timeBefore)/1000.0) + " fitness/second");


        return population;
    }

    private Wallpaper[] orderPopulation(Wallpaper[] pop) {
//        calculateFitnesses(pop);

        boolean swapped = true;
        for(int elsToComp = fitnesses.length - 2; swapped && elsToComp >= 0; elsToComp--) {
            swapped = false;
            for(int i = 0; i <= elsToComp; i++) {
                if(fitnesses[i] < fitnesses[i+1]) {
                    swapped = true;
                    // Swap fitnesses
                    int tmpFitness = fitnesses[i];
                    fitnesses[i] = fitnesses[i+1];
                    fitnesses[i+1] = tmpFitness;
                    // Swap wallpapers
                    Wallpaper tmpWp = pop[i];
                    pop[i] = pop[i+1];
                    pop[i+1] = tmpWp;
                }
            }
        }

        return pop;
    }

    private Wallpaper[] extendPopulation(Wallpaper[] pop) {
        Wallpaper[] newPop = new Wallpaper[2*pop.length];
        int[] newFit = new int[2*pop.length];

        System.arraycopy(pop, 0, newPop, 0, pop.length);
        System.arraycopy(fitnesses, 0, newFit, 0, fitnesses.length);

        for(int i = 0; i < pop.length; i+= 2) {
            Wallpaper[] children = procreate(newPop[i], newPop[i+1]);
            System.arraycopy(children, 0, newPop, pop.length + i, 2);
        }

        fitnesses = newFit;
        return newPop;
    }

    private Wallpaper[] reducePopulation(Wallpaper[] pop) {
        Wallpaper[] newPop = new Wallpaper[populationLimit];
        int[] newFit = new int[populationLimit];

        System.arraycopy(pop, 0, newPop, 0, populationLimit);
        System.arraycopy(fitnesses, 0, newFit, 0, populationLimit);

        fitnesses = newFit;
        return newPop;
    }

    private void calculateFitnesses(Wallpaper[] pop) {
        // If first time calculating fitnesses, calculate all.
        // If not, start from the popLimit (don't recalculate).
        int start = (calculateAllFitnesses) ? (0) : (populationLimit);
        calculateAllFitnesses = false;

        for(int i = start; i < fitnesses.length; i++) {
            fitnesses[i] = fitness(pop[i]);
        }
    }

    private void calculateFitnessesConc(Wallpaper[] pop) {
        // If first time calculating fitnesses, calculate all.
        // If not, start from the popLimit (don't recalculate).
        int start = (calculateAllFitnesses) ? (0) : (populationLimit);
        calculateAllFitnesses = false;

        CyclicBarrier barrier = new CyclicBarrier(pop.length - start + 1);

        for(int i = start; i < fitnesses.length; i++) {
            new Thread(new FitnessRunnable(i, pop[i], barrier)).start();
        }
        try {
            barrier.await();
        } catch (InterruptedException ex) {
            System.err.println("Interrupted Exception!");
            ex.printStackTrace();
        } catch (BrokenBarrierException ex) {
            System.err.println("Broken Barrier Exception!");
            ex.printStackTrace();
        }
    }

    private class FitnessRunnable implements Runnable {

        private int wpNumber;
        private Wallpaper wp;
        private CyclicBarrier barrier;

        FitnessRunnable(int wpNumber, Wallpaper wp, CyclicBarrier barrier) {
            super();
            this.wpNumber = wpNumber;
            this.wp = wp;
            this.barrier = barrier;
        }

        public void run() {
            fitnesses[wpNumber] = fitness(wp);
            try {
                barrier.await();
            } catch (InterruptedException ex) {
                System.err.println("Interrupted Exception!");
                ex.printStackTrace();
            } catch (BrokenBarrierException ex) {
                System.err.println("Broken Barrier Exception!");
                ex.printStackTrace();
            }
        }

    }

    private Wallpaper[] procreate(Wallpaper parent1, Wallpaper parent2) {
        Wallpaper[] children = crossover(parent1, parent2);

        Wallpaper[] newWps = {mutate(children[0]), mutate(children[1])};

        return newWps;
    }

    public int fitness(Wallpaper wp) {
        int fitness = 0;

        for(int y = 0; y < h; y++) {
            for(int x = 0; x < w; x++) {
                fitness += pixelFitness(wp, x, y);
            }
        }

        return fitness;
    }

    private int pixelFitness(Wallpaper wp, int x, int y) {
        int fitness = 0;
        
        int pixelC = wp.pixels[y][x];

        for(int i = 1; i <= maxLookaround; i++) {
            int minYLook = Math.max(y-i, 0);
            int maxYLook = Math.min(y+i, maxY);
            int minXLook = Math.max(x-i, 0);
            int maxXLook = Math.min(x+i, maxX);
            
            int fitnessValueNW = pixelC ^ wp.pixels[minYLook][minXLook];
            int fitnessValueN = pixelC ^ wp.pixels[minYLook][x];
            int fitnessValueNE = pixelC ^ wp.pixels[minYLook][maxXLook];
            int fitnessValueW = pixelC ^ wp.pixels[y][minXLook];
            int fitnessValueE = pixelC ^ wp.pixels[y][maxXLook];
            int fitnessValueSW = pixelC ^ wp.pixels[maxYLook][minXLook];
            int fitnessValueS = pixelC ^ wp.pixels[maxYLook][x];
            int fitnessValueSE = pixelC ^ wp.pixels[maxYLook][maxXLook];

            fitness += (fitnessValueN <= maxDiff) ? (1) : (-1);
            fitness += (fitnessValueNE <= maxDiff) ? (1) : (-1);
            fitness += (fitnessValueE <= maxDiff) ? (1) : (-1);
            fitness += (fitnessValueSE <= maxDiff) ? (1) : (-1);
            fitness += (fitnessValueS <= maxDiff) ? (1) : (-1);
            fitness += (fitnessValueSW <= maxDiff) ? (1) : (-1);
            fitness += (fitnessValueW <= maxDiff) ? (1) : (-1);
            fitness += (fitnessValueNW <= maxDiff) ? (1) : (-1);
        }

        return fitness;
    }

    public Wallpaper[] crossover(Wallpaper wp1, Wallpaper wp2) {
        Wallpaper[] newWps = new Wallpaper[2];
        newWps[0] = new Wallpaper(wp1);
        newWps[1] = new Wallpaper(wp2);
//        int crossPoint = r.nextInt(h);
        int[] crossPoints = new int[r.nextInt(10)];

        for(int i = 0; i < crossPoints.length; i++) {
            crossPoints[i] = r.nextInt(h);
        }
        Arrays.sort(crossPoints);

        for(int c = 0; c < crossPoints.length-1; c += 2) {
            // Copy part before crossover point
            for(int i = crossPoints[c]; i < crossPoints[c+1]; i++) {
                System.arraycopy(wp2.pixels[i], 0, newWps[0].pixels[i], 0, w);
                System.arraycopy(wp1.pixels[i], 0, newWps[1].pixels[i], 0, w);
            }
        }

        return newWps;
    }

    public Wallpaper mutate(Wallpaper wp) {
        int mutations = Math.max(1, (w*h)/400);
        Wallpaper newWp = new Wallpaper(wp);
        for(int i = 0; i < mutations; i++) {
            newWp.pixels[r.nextInt(h)][r.nextInt(w)] = newWp.getRandomPixel();
        }
        return newWp;
    }
}
