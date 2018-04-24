package gc;

import javafx.application.Platform;

import java.awt.*;
import java.util.ArrayList;
import java.util.Random;

/**
 * Class in charge of managing Genetic Algorithm instance. Will run one iteration with specified settings in a thread,
 * and return the result when it has finished.
 */

public class GeneticAlgorithm extends Thread {

	private ArrayList<Chromosome> pool; // Pool of current gen chromosomes
	private ArrayList<Chromosome> newPool; // Pool of next gen chromosomes, which is then emptied into 'pool'

	private Random rand;

	// Best circle found at any given moment
	private CircleData geneticCircle;
	// Data that is shared between chromosomes, as well as with the 'GeneticAlgorithm' class
	private SharedData sharedData;

	private long start;

	private long seed;
	private boolean done = false; // For use on threaded runs, is true when the algorithm has finished
	private String output = ""; // Contains the output for this instance of the GA, which may be saved to a file later

	/**
	 * Receives an array of points, determining the static circles that the algorithm must work around, and a seed
	 * for the random number generator.
	 *
	 * @param circles Array of coordinates for static circles
	 * @param seed    Seed to seed random number generator with
	 */
	GeneticAlgorithm(Point[] circles, int seed) {
		this.seed = seed;
		rand = new Random(seed);
		init(circles);
	}

	/**
	 * Init pools and 'SharedData' instance, and set up chromosomes.
	 *
	 * @param circles Array of coordinates for static circles
	 */
	private void init(Point[] circles) {
		geneticCircle = new CircleData(0, 0, 0);
		pool = new ArrayList<>(GlobalVars.POOL_SIZE);
		newPool = new ArrayList<>(GlobalVars.POOL_SIZE);

		sharedData = new SharedData(circles);

		for (int x = 0; x < GlobalVars.POOL_SIZE; x++)
			pool.add(new Chromosome(rand, sharedData, geneticCircle));
	}

	/**
	 * Runs the GA. Performs constant evaluation, crossover and mutation, until no improvements happen in a given number
	 * of generations, denoted by GlobalVariables.BAD_GENERATIONS.
	 */
	public void run() {
		start = System.currentTimeMillis();

		while (!isDone()) {
			for (int x = 0; x < GlobalVars.POOL_SIZE; x += 2) {
				Chromosome c1 = selectFittest(pool);
				Chromosome c2 = selectFittest(pool);

				c1.crossover(c2);

				c1.mutate();
				c2.mutate();

				c1.calcFitness(geneticCircle);
				c2.calcFitness(geneticCircle);

				newPool.add(c1);
				newPool.add(c2);
			}

			pool.addAll(newPool);
			newPool.clear();

			sharedData.gen++;
		}

		done = true;
	}

	/**
	 * Selects the fittest chromosome out of a pool of chromosomes. Used in evolution of the chromosomes.
	 *
	 * @param pool Pool out of which to pick the fittest chromosome
	 * @return Fittest chromosome
	 */
	private Chromosome selectFittest(ArrayList<Chromosome> pool) {
		double maxFitness = pool.get(0).fitness;
		int fittest = 0;
		for (int x = 1; x < pool.size(); x++) {
			if (pool.get(x).fitness > maxFitness) {
				maxFitness = pool.get(x).fitness;
				fittest = x;
			}
		}

		Chromosome c = pool.get(fittest);
		pool.remove(c);

		return c;
	}

	/**
	 * Determines if the algorithm should continue running or not, based on the number of generations since the last
	 * best circle was found. Once the algorithm is stopped, it sets the output of the GA, and displays it, if the
	 * program is running in graphical mode.
	 *
	 * @return True if the algorithm has decided to stop, false if it will continue
	 */
	private boolean isDone() {
		// If no updates to genetic circle after specified number of generations, halt algorithm
		if (sharedData.gen - sharedData.lastUpdate > GlobalVars.BAD_GENERATIONS) {
			output += ("Genetic circle w/ seed: " + seed + '\n');
			output += ("Radius: " + geneticCircle.getRadius() + "\n");
			output += ("Generation: " + sharedData.lastUpdate + "\n");
			output += ("Total generations evolved: " + sharedData.gen + "\n");
			output += ("Generations evolved per second: "
					+ (((long) sharedData.gen * 1000) / (System.currentTimeMillis() - start + 1)) + " gen/s\n");
			output += ("Total time taken: " + Util.getRunTime(System.currentTimeMillis() - start) + "\n");
			output += "\n\n";

			System.out.print(output);

			if (UI.pane != null) {
				Platform.runLater(() -> UI.draw(geneticCircle));
				if (Main.outputFileName != null) Main.writeToFile(output);
			}

			return true;
		}

		return false;
	}

	boolean getDone() {
		return done;
	}

	String getOutput() {
		return output;
	}

	SharedData getSharedData() {
		return sharedData;
	}
}
