package gc;

import java.awt.*;
import java.io.BufferedWriter;
import java.io.FileWriter;
import java.io.IOException;
import java.util.Random;

/*
    Class that collects data on various runs of the genetic algorithm using different seeds
    No UI, just outputs data to the console
    Currently outputs seed, final generation, radius of best circle found, runtime and generation that the best circle belongs to
    Uses one thread for each GeneticAlgorithm instance
 */

public class DataCollector {

	private long start, finish; // values to time the total runtime of all threads

	static int runs; // how many times to run the algorithm

	static String[] outputs;

	static boolean generateFile = true; // generates text file based on output from console

	DataCollector() {

		// init static array for use in file generation
		outputs = new String[runs];

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
		Main.circles = new Point[runs][];
		for (int x = 0; x < runs; x++) {
			Random rand = new Random(x);
			Main.circles[x] = new Point[rand.nextInt(100)];
			for (int i = 0; i < Main.circles[x].length; i++) {
				Main.circles[x][i] = new Point(rand.nextInt(GlobalVars.SCREEN_WIDTH - 2 * GlobalVars.STAT_CIRCLE_RADIUS) + GlobalVars.STAT_CIRCLE_RADIUS,
						rand.nextInt(GlobalVars.SCREEN_HEIGHT - 2 * GlobalVars.STAT_CIRCLE_RADIUS) + GlobalVars.STAT_CIRCLE_RADIUS);
			}
		}

		System.out.println("Point arrays generated");

		GeneticAlgorithm[] threads = new GeneticAlgorithm[GlobalVars.THREADS - 1 < runs ? GlobalVars.THREADS - 1 : runs];
		String[] outputs = new String[runs];

		System.out.println("Starting threaded run");
		System.out.println();

		start = System.currentTimeMillis();

		// launches initial threads
		for (int x = 0; x < threads.length; x++) {
			threads[x] = new GeneticAlgorithm(Main.circles[x], x);
			threads[x].setDaemon(true);
			threads[x].setPriority(8);
			threads[x].start();
		}

		// once a thread is done, a new one with a new set of data is launched
		for (int x = threads.length; x < runs; ) {
			for (int a = 0; a < threads.length; a++) {
				if (threads[a].isDone() && x < runs) {
					outputs[(int) threads[a].getSeed()] = threads[a].getOutput();
					threads[a] = new GeneticAlgorithm(Main.circles[x], x);
					threads[a].setDaemon(true);
					threads[a].setPriority(8);
					threads[a].start();
					x++;
				}
			}

			try {
				Thread.sleep(500);
			} catch (InterruptedException e) {
				e.printStackTrace();
			}
		}

		System.out.println("Waiting for last threads to finish");
		System.out.println();

		// waits for last threads to finish
		for (GeneticAlgorithm thread : threads)
			try {
				thread.join();
				outputs[(int) thread.getSeed()] = thread.getOutput();
			} catch (InterruptedException e) {
				e.printStackTrace();
			}

		finish = System.currentTimeMillis();

		// generates a text file if it was requested in the program launch
		if (generateFile) {
			try {
				BufferedWriter bf = new BufferedWriter(new FileWriter("GeneticCircles-" + runs + "iterations.txt"));
				for (String str : outputs)
					bf.write(str + "\n");
				bf.write("Total time " + (double) (finish - start) / 1000 + "s");
				bf.close();
			} catch (IOException e) {
				e.printStackTrace();
			}
		}

		System.out.println("Total time " + (double) (finish - start) / 1000 + "s");
	}
}
