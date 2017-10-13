package gc;


import javafx.application.Platform;

import java.awt.*;
import java.util.ArrayList;
import java.util.Random;

public class GeneticAlgorithm extends Thread {

	private ArrayList<Chromosome> pool;
	private ArrayList<Chromosome> newPool;

	private Random rand;
	private long start;

	private CircleData geneticCircle;
	// data that is shared between chromosomes, as well as with the Genetic Algorithm class
	private SharedData sharedData;

	private long seed;
	private boolean done = false; // for multi-threaded run
	private String output; // for generating a file in the DataCollector class

	// set seed and random number generator
	GeneticAlgorithm(Point[] circles) {
		seed = System.currentTimeMillis();
		rand = new Random(seed);
		init(circles);
	}

	// set seed and random number generator
	GeneticAlgorithm(Point[] circles, int seed) {
		this.seed = seed;
		rand = new Random(seed);
		init(circles);
	}

	// initialize pools and set up chromosomes as well as shared data instance
	private void init(Point[] circles) {
		geneticCircle = new CircleData(0, 0, 0);
		pool = new ArrayList<>(GlobalVars.POOL_SIZE);
		newPool = new ArrayList<>(GlobalVars.POOL_SIZE);

		sharedData = new SharedData(circles);

		for (int x = 0; x < GlobalVars.POOL_SIZE; x++)
			pool.add(new Chromosome(rand, sharedData, geneticCircle));
	}

	// runs genetic algorithm
	public void run() {

		start = System.currentTimeMillis();

		while (true) {
			for (int x = 0; x < GlobalVars.POOL_SIZE; x += 2) {
				Chromosome c1 = Main.selectFittest(pool);
				Chromosome c2 = Main.selectFittest(pool);

				// cross chromosomes at random point, read crossover function in gc.Chromosome class for more detailed info
				c1.crossover(c2);

				// mutate chromosomes, read mutate function in gc.Chromosome class for more detailed info
				c1.mutate();
				c2.mutate();

				// recalculate fitness for both chromosomesbadGenerations
				c1.calcFitness(geneticCircle);
				c2.calcFitness(geneticCircle);

				// if no updates to genetic circle after specified number of generations, halt algorithm
				if (sharedData.gen - sharedData.lastUpdate > GlobalVars.BAD_GENERATIONS) {
					if (UI.pane != null) {
						Platform.runLater(() -> UI.draw(geneticCircle, sharedData.gen));
					}
					System.out.println("Genetic circle w/ seed: " + seed);
					System.out.println("Radius: " + geneticCircle.getRadius());
					System.out.println("Generation: " + sharedData.lastUpdate);
					System.out.println("Total generations evolved: " + sharedData.gen);
					System.out.println("Total time taken: " + Util.getRunTime(System.currentTimeMillis() - start));

					System.out.println("\n");
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

			sharedData.gen++;

			// will run algorithm faster
			if (UI.pane != null)
				Platform.runLater(() -> UI.updateText(sharedData.gen));
		}
	}

	public int getGen() {
		return sharedData.gen;
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
