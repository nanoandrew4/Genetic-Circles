package gc;

import java.util.Random;

/*
    Class that collects data on various runs of the genetic algorithm using different seeds
    No UI, just outputs data to the console
    Currently outputs seed, final generation, size and radius of the valid circle
 */

public class DataCollector {

    private long start;

    public static void main(String[] args) {
        new DataCollector();
    }

    private DataCollector() {
        // depending on window size, encoded chromosome length will vary due to more digits being present to represent bigger numbers
        if (GlobalVars.screenHeight > GlobalVars.screenWidth) {
            Main.geneLength = Integer.toBinaryString(GlobalVars.screenHeight).length();
            Chromosome.maxSize = GlobalVars.screenWidth / 2;
        } else {
            Main.geneLength = Integer.toBinaryString(GlobalVars.screenWidth).length();
            Chromosome.maxSize = GlobalVars.screenHeight / 2;
        }

        start = System.currentTimeMillis();

        for (int x = 0; x < 50; x++) {
            System.out.println("Starting Genetic Algorithm with seed: " + x);
            System.out.println();
            GlobalVars.rand = new Random(x);

            // calculates largest possible circle in window with current seed
            CircleData cd = new Fitness().getBiggestCircle(false);
            GlobalVars.largestRadius = cd.radius;

            // start new gA thread
            GeneticAlgorithm gA = new GeneticAlgorithm();
            Thread t = new Thread(gA);
            t.setDaemon(true);
            t.setPriority(Thread.MAX_PRIORITY);
            t.start();

            // wait until thread is dead to continue, currently single thread only
            while (t.isAlive()) {
                try {
                    Thread.sleep(100);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }

            GlobalVars.gen = 0;
        }

        System.out.println("Total time " + (double)(System.currentTimeMillis() - start) / 1000 + "s");
    }
}
