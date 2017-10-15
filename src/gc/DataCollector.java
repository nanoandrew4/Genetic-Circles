package gc;

import java.awt.*;
import java.util.Random;

/*
    Class that collects data on various iterations of the genetic algorithm using different seeds
    No UI, just outputs data to the console
    Uses one thread for each GeneticAlgorithm instance
 */

public class DataCollector {

	DataCollector(boolean sequential, int offset, int iterations) {

		// init static array for use in file generation
		String[] outputs = new String[iterations];

		// depending on window size, encoded chromosome length will vary due to more digits being present to represent bigger numbers
		if (GlobalVars.SCREEN_HEIGHT > GlobalVars.SCREEN_WIDTH) {
			Main.geneLength = Integer.toBinaryString(GlobalVars.SCREEN_HEIGHT).length();
			Chromosome.maxSize = GlobalVars.SCREEN_WIDTH / 2;
		} else {
			Main.geneLength = Integer.toBinaryString(GlobalVars.SCREEN_WIDTH).length();
			Chromosome.maxSize = GlobalVars.SCREEN_HEIGHT / 2;
		}

		System.out.println("Generating point arrays");

		// generates data to be used by each thread for fast access after
		Main.circles = new Point[iterations][];
		int[] seeds = new int[iterations];
		Random seedGen = new Random();
		for (int x = 0; x < iterations; x++) {
			Random rand = new Random(seeds[x] = (sequential ? x + offset : seedGen.nextInt()));
			Main.circles[x] = new Point[rand.nextInt(100)];
			for (int i = 0; i < Main.circles[x].length; i++) {
				Main.circles[x][i] = new Point(rand.nextInt(GlobalVars.SCREEN_WIDTH - 2 * GlobalVars.STAT_CIRCLE_RADIUS) + GlobalVars.STAT_CIRCLE_RADIUS,
						rand.nextInt(GlobalVars.SCREEN_HEIGHT - 2 * GlobalVars.STAT_CIRCLE_RADIUS) + GlobalVars.STAT_CIRCLE_RADIUS);
			}
		}

		System.out.println("Point arrays generated");

		GeneticAlgorithm[] threads = new GeneticAlgorithm[GlobalVars.THREADS < iterations ? GlobalVars.THREADS : iterations];

		System.out.println("Starting threaded run\n");

		long start = System.currentTimeMillis();

		// launches initial threads
		for (int i = 0; i < threads.length; i++) {
			threads[i] = new GeneticAlgorithm(Main.circles[i], seeds[i]);
			threads[i].setDaemon(true);
			threads[i].setPriority(9);
			threads[i].start();
		}

		System.out.println("Initial batch of threads successfully launched\n");

		int threadNum = 0;

		// once a thread is done, a new one with a new set of data is launched
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

		// waits for last threads to finish
		for (GeneticAlgorithm geneticAlgorithm : threads) {
			while (!geneticAlgorithm.getDone())
				try {Thread.sleep(200);} catch (InterruptedException e) {e.printStackTrace();}
			outputs[threadNum++] = geneticAlgorithm.getOutput();
			sumOfRadii += geneticAlgorithm.getSharedData().largestRadius;
		}

		long finish = System.currentTimeMillis();

		if (Main.outputFileName != null)
			Main.writeToFile(outputs);

		System.out.println("Total time " + Util.getRunTime(finish - start));
		System.out.println("Average radius was: " + (sumOfRadii / iterations));
	}
}
