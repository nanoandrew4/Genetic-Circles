package gc;


import javafx.application.Platform;
import javafx.scene.paint.Paint;
import javafx.scene.shape.Circle;
import kn.uni.voronoitreemap.j2d.Point2D;

import java.util.ArrayList;
import java.util.Random;

class GeneticAlgorithm extends Thread {

    private ArrayList<Chromosome> pool;
    private ArrayList<Chromosome> newPool;

    private Point2D[] circles;
    private Random rand;

    private int largestRadius, gen;
    private long seed;
    private boolean done = false; // for multi-threaded run
    private String output; // for generating a file in the DataCollector class

    // initializes pools
    GeneticAlgorithm(Point2D[] circles, int largestRadius) {
        this.circles = circles;
        this.largestRadius = largestRadius;
        rand = new Random(System.currentTimeMillis());
        seed = System.currentTimeMillis();
        init();
    }

    GeneticAlgorithm(Point2D[] circles, int seed, int largestRadius) {
        this.circles = circles;
        this.seed = seed;
        rand = new Random(seed);
        this.largestRadius = largestRadius;
        init();
    }

    private void init() {
        pool = new ArrayList<>(GlobalVars.poolSize);
        newPool = new ArrayList<>(GlobalVars.poolSize);

        for (int x = 0; x < GlobalVars.poolSize; x++)
            pool.add(new Chromosome(circles, rand, largestRadius));
    }

    // runs genetic algorithm
    public void run() {

        while (true) {
            for (int x = 0; x < GlobalVars.poolSize; x+=2) {
                Chromosome c1 = Main.selectFittest(pool);
                Chromosome c2 = Main.selectFittest(pool);

                // cross chromosomes at random point, read crossover function in gc.Chromosome class for more detailed info
                c1.crossover(c2);

                // mutate chromosomes, read mutate function in gc.Chromosome class for more detailed info
                c1.mutate();
                c2.mutate();

                // recalculate fitness for both chromosomes
                c1.calcFitness(circles, largestRadius);
                c2.calcFitness(circles, largestRadius);

                // if either chromosome is a valid solution, stops thread from running and draws solution to screen
                Chromosome c = null;
                if (c1.fitness == Integer.MAX_VALUE && Main.isValid(c1.getCircleData(), circles))
                    c = c1;
                else if (c2.fitness == Integer.MAX_VALUE && Main.isValid(c2.getCircleData(), circles))
                    c = c2;

                if (c != null) {
                    if (Main.pane != null) {
                        CircleData cd = c.getCircleData();
                        Platform.runLater(() -> Main.draw(new Circle(cd.coords.getX(), cd.coords.getY(), cd.radius, Paint.valueOf("red")), gen));
                    }
                    System.out.println(output = "Circle with seed " + seed + " from generation " + gen + " with proximity of " + ((Double.valueOf(c.decode().split("-")[0])) / largestRadius) * 100d
                            + "% and radius " + (Integer.valueOf(c.decode().split("-")[0])) + " is a valid solution");
                    System.out.println();
                    done = true;
                    return;
                }

                // add chromosomes to new pool
                newPool.add(c1);
                newPool.add(c2);
            }

            // prepare for next iteration
            pool.addAll(newPool);
            newPool.clear();
            gen++;


            //gc.Chromosome fittest = gc.Main.selectFittest(pool);
            //String[] genes = fittest.decode().split("-");
            //pool.add(fittest);
            //Platform.runLater(() -> {gc.Main.draw(Integer.valueOf(genes[1]), Integer.valueOf(genes[2]), Integer.valueOf(genes[0]));});

            // will run algorithm faster
            if (Main.pane != null)
                Platform.runLater(() -> Main.draw(-1, -1, -1, gen));
        }
    }

    public int getGen() {
        return gen;
    }

    public boolean isDone() {
        return done;
    }

    public String getOutput() {
        return output;
    }

    public long getSeed() {
        return seed;
    }
}
