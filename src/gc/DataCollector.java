package gc;

import java.awt.*;
import java.util.Random;

/**
 * Class that collects data on various iterations of the genetic algorithm using different seeds.
 * No UI, just outputs data to the console.
 * Uses one thread for each GeneticAlgorithm instance.
 */

public class DataCollector {

	/**
	 * Runs the data collector, which will gather information on the best circles found for different seed values.
	 *
	 * @param sequential True if you wish to run a sequence of seed values, false for random seed values
	 * @param offset Offset at which to start the sequence of seeds. Only used if 'sequential' is true
	 * @param iterations Number of GA instances to collect data for
	 */
	DataCollector(boolean sequential, int offset, int iterations) {

		// Array that will contain the output of each instance of a genetic algorithm, if requested by user
		String[] outputs = new String[iterations];

		// Set the gene length and max radius, based on the max value a gene can take (virtual screen size)
		if (GlobalVars.SCREEN_HEIGHT > GlobalVars.SCREEN_WIDTH) {
			Main.geneLength = Integer.toBinaryString(GlobalVars.SCREEN_HEIGHT).length();
			Chromosome.maxSize = GlobalVars.SCREEN_WIDTH / 2;
		} else {
			Main.geneLength = Integer.toBinaryString(GlobalVars.SCREEN_WIDTH).length();
			Chromosome.maxSize = GlobalVars.SCREEN_HEIGHT / 2;
		}

		System.out.println("Generating point arrays");

		/*
		 * Generates all sets of static circles to be used by each instance of a genetic algorithm. The static circles
		 * reside in an array in Main.
		 */
		Main.circles = new Point[iterations][];
		int[] seeds = new int[iterations];
		Random seedGen = new Random();
		int maxXCoord = GlobalVars.SCREEN_WIDTH - 2 * GlobalVars.STAT_CIRCLE_RADIUS;
		int maxYCoord = GlobalVars.SCREEN_HEIGHT - 2 * GlobalVars.STAT_CIRCLE_RADIUS;
		for (int x = 0; x < iterations; x++) {
			// Depending on params, seed for RNG is a number in a sequence, or a random number
			Random rand = new Random(seeds[x] = (sequential ? x + offset : seedGen.nextInt()));
			Main.circles[x] = new Point[rand.nextInt(100)];
			for (int i = 0; i < Main.circles[x].length; i++)
				Main.circles[x][i] = new Point(rand.nextInt(maxXCoord) + GlobalVars.STAT_CIRCLE_RADIUS,
											  rand.nextInt(maxYCoord) + GlobalVars.STAT_CIRCLE_RADIUS);
		}

		System.out.println("Point arrays generated");

		GeneticAlgorithm[] threads = new GeneticAlgorithm[Math.min(GlobalVars.THREADS, iterations)];

		System.out.println("Starting threaded run\n");

		long start = System.currentTimeMillis();

		for (int i = 0; i < threads.length; i++) {
			threads[i] = new GeneticAlgorithm(Main.circles[i], seeds[i]);
			threads[i].setDaemon(true);
			threads[i].setPriority(Thread.MAX_PRIORITY);
			threads[i].start();
		}

		System.out.println("Initial batch of threads successfully launched\n");

		int threadNum = 0;

		// Waits for a thread to complete, and then launches a new one, until all iterations are complete
		int sumOfRadii = 0;
		for (int i = threads.length; i < iterations; ) {
			for (int a = 0; a < threads.length; a++) {
				if (threads[a].getDone() && i < iterations) {
					outputs[threadNum++] = threads[a].getOutput();
					sumOfRadii += threads[a].getSharedData().largestRadius;

					threads[a] = new GeneticAlgorithm(Main.circles[i], seeds[i]);
					threads[a].setDaemon(true);
					threads[a].setPriority(9);
					threads[a].start();
					i++;
				}
			}

			try {
				Thread.sleep(200);
			} catch (InterruptedException e) {
				e.printStackTrace();
			}
		}

		System.out.println("Waiting for last threads to finish\n");

		for (GeneticAlgorithm geneticAlgorithm : threads) {
			while (!geneticAlgorithm.getDone()) try {
				Thread.sleep(200);
			} catch (InterruptedException e) {
				e.printStackTrace();
			}
			outputs[threadNum++] = geneticAlgorithm.getOutput();
			sumOfRadii += geneticAlgorithm.getSharedData().largestRadius;
		}

		System.out.println("Total time " + Util.getRunTime(System.currentTimeMillis() - start));
		System.out.println("Average radius was: " + (sumOfRadii / iterations));

		if (Main.outputFileName != null) Main.writeToFile(outputs);
	}
}
