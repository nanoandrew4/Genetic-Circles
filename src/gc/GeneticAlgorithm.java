package gc;

import javafx.application.Platform;

import java.awt.*;
import java.util.ArrayList;
import java.util.Random;

/*
	Class in charge of managing Genetic Algorithm instance. Will run one iteration with specified settings in a thread,
	and return the result when it has finished.
 */

public class GeneticAlgorithm extends Thread {

	private ArrayList<Chromosome> pool;
	private ArrayList<Chromosome> newPool;

	private Random rand;

	// best circle found at any given moment
	private CircleData geneticCircle;
	// data that is shared between chromosomes, as well as with the Genetic Algorithm class
	private SharedData sharedData;

	private long start;

	private long seed;
	private boolean done = false; // for multi-threaded run
	private String output = ""; // for generating a file

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

		while (!isDone()) {
			for (int x = 0; x < GlobalVars.POOL_SIZE; x += 2) {
				Chromosome c1 = Main.selectFittest(pool);
				Chromosome c2 = Main.selectFittest(pool);

				// cross chromosomes at random point, read crossover function in gc.Chromosome class for more detailed info
				c1.crossover(c2);

				// mutate chromosomes, read mutate function in gc.Chromosome class for more detailed info
				c1.mutate();
				c2.mutate();

				// recalculate fitness
				c1.calcFitness(geneticCircle);
				c2.calcFitness(geneticCircle);

				// add chromosomes to new pool
				newPool.add(c1);
				newPool.add(c2);
			}

			// prepare for next iteration
			pool.addAll(newPool);
			newPool.clear();

			sharedData.gen++;
		}

		done = true;
	}

	private boolean isDone() {
		// if no updates to genetic circle after specified number of generations, halt algorithm
		if (sharedData.gen - sharedData.lastUpdate > GlobalVars.BAD_GENERATIONS) {

			output += ("Genetic circle w/ seed: " + seed + '\n');
			output += ("Radius: " + geneticCircle.getRadius() + "\n");
			output += ("Generation: " + sharedData.lastUpdate + "\n");
			output += ("Total generations evolved: " + sharedData.gen + "\n");
			output += ("Generations evolved per second: " + (((long)sharedData.gen * 1000) / (System.currentTimeMillis() - start + 1)) + " gen/s\n");
			output += ("Total time taken: " + Util.getRunTime(System.currentTimeMillis() - start) + "\n");
			output += "\n\n";

			System.out.print(output);

			if (UI.pane != null) {
				Platform.runLater(() -> UI.draw(geneticCircle, sharedData.gen));
				if (Main.outputFileName != null)
					Main.writeToFile(output);
			}

			return true;
		}

		return false;
	}

	boolean getDone () {
		return done;
	}

	String getOutput() {
		return output;
	}

	SharedData getSharedData() {
		return sharedData;
	}
}
