package gc;

import javafx.application.Application;

import java.awt.*;
import java.io.BufferedWriter;
import java.io.FileWriter;
import java.io.IOException;

/**
 * Launches UI along with the Genetic Algorithm
 */

public class Main {
	static long seed = System.currentTimeMillis();
	static int geneLength;

	static String outputFileName;

	static Point[][] circles;

	public static void main(String[] args) {

		// Defaults
		boolean sequential = true;
		int offset = 0, iterations = 1;

		if (args.length == 0) {
			System.out.println("Command syntax: java -jar GeneticCircles.jar [mode] [options]\n");
			System.out.println("Modes:\n");
			System.out.println("\tui: runs program with graphical interface to visualize results");
			System.out.println("\theadless: runs program without graphical interface, can run multiple instances of the algorithm (see mode specific options)\n");
			System.out.println("Options:\n");
			System.out.println("\t-sw: Screen width, default is: " + GlobalVars.SCREEN_WIDTH + "\n");
			System.out.println("\t-sh: Screen height, default is: " + GlobalVars.SCREEN_HEIGHT + "\n");
			System.out.println("\t-t: Number of threads the program can use, default is all, which on this computer is: " + GlobalVars.THREADS + ". \n\t    Only used when using headless mode, since ui mode only evolves one circle with one set of settings\n");
			System.out.println("\t-ps: Pool size, also known as population size, default is: " + GlobalVars.POOL_SIZE + "\n");
			System.out.println("\t-bg: Number of generations to evolve without any improvement. \n" + "\t     If bg = 5 and no improvements are found for the next 5 generations, the algorithm will stop, default is: " + GlobalVars.BAD_GENERATIONS + "\n");
			System.out.println("\t-scr: Radius of the static circles on the screen, default is: " + GlobalVars.STAT_CIRCLE_RADIUS + "\n");
			System.out.println("\t-cr: Crossover rate, specifies how likely the two chromosomes are to perform crossover, default value is: " + GlobalVars.CROSSOVER_RATE + "\n");
			System.out.println("\t-mr: Mutation rate, specifies how likely a function is to mutate, default value is: " + GlobalVars.MUTATION_RATE + "\n");
			System.out.println("\t-w: Output file name to write to, if output should be stored in a file\n");
			System.out.println("\t-s: Seed to be used in random number generator, if none is entered the program will use the current time in milliseconds since the epoch\n");
			System.out.println("Mode specific options:\n");
			System.out.println("\tHeadless:\n");
			System.out.println("\t\t-hr: Random seeds used, default is sequential seeds based on offset and iterations\n");
			System.out.println("\t\t-ho: Offset to use for seeds when running sequentially. \n\t\t     Default is: " + offset + " which the first seed will be 0, the second 1 and so on\n");
			System.out.println("\t\t-hi: Number of iterations to run of the algorithm, default is: " + iterations + "\n");
			return;
		}
		if (!args[0].equals("ui") && !args[0].equals("headless")) {
			System.out.println("First argument must be specified, to determine mode to run program in");
			System.out.println("Current supported modes are:");
			System.out.println("ui -> runs program with graphical interface to visualize results");
			System.out.println("headless -> runs program without graphical interface");
		}
		if (args.length % 2 == 0) {
			System.out.println("Invalid arguments");
		}
		try {
			for (int i = 0; i < args.length; i++) {
				switch (args[i]) {
					case "-s": // Seed
						seed = Integer.valueOf(args[i + 1]);
						break;
					case "-w": // Output file name
						outputFileName = args[i + 1];
						break;
					case "-t": // Threads to use
						GlobalVars.THREADS = Integer.valueOf(args[i + 1]);
						break;
					case "-sw": // Virtual (and physical if in ui mode) screen width
						GlobalVars.SCREEN_WIDTH = Integer.valueOf(args[i + 1]);
						break;
					case "-sh": // Virtual (and physical if in ui mode) screen height
						GlobalVars.SCREEN_HEIGHT = Integer.valueOf(args[i + 1]);
						break;
					case "-ps": // Pool size (genetic algorithm)
						GlobalVars.POOL_SIZE = Integer.valueOf(args[i + 1]);
						break;
					case "-bg": // Bad generations
						GlobalVars.BAD_GENERATIONS = Integer.valueOf(args[i + 1]);
						break;
					case "-cr": // Cross-over rate
						GlobalVars.CROSSOVER_RATE = Integer.valueOf(args[i + 1]);
						break;
					case "-mr": // Mutation rate
						GlobalVars.MUTATION_RATE = Integer.valueOf(args[i + 1]);
						break;
					case "-scr": // Static circle radius
						GlobalVars.STAT_CIRCLE_RADIUS = Integer.valueOf(args[i + 1]);
						break;
					case "-hr": // Random set of iterations for DataCollector
						sequential = false;
						break;
					case "-ho": // Offset for sequential reads
						offset = Integer.valueOf(args[i + 1]);
						break;
					case "-hi": // Number of iterations to run in headless mode
						iterations = Integer.valueOf(args[i + 1]);
						break;
				}
			}

			if (args[0].equals("ui")) launchUI();
			else if (args[0].equals("headless")) new DataCollector(sequential, offset, iterations);
		} catch (Exception e) {
			e.printStackTrace();
			System.out.println("Invalid input. Run program with no arguments to output help on usage.");
		}
	}

	/**
	 * Launches the UI in a new thread.
	 */
	private static void launchUI() {
		new Thread(() -> Application.launch(UI.class)).start();
	}

	/**
	 * Writes output of any number of GA instances to a file, if requested at program runtime.
	 *
	 * @param output Output to be written to a file
	 */
	static void writeToFile(String... output) {
		if (Main.outputFileName != null) {
			try {
				BufferedWriter bf = new BufferedWriter(
						new FileWriter(outputFileName + (outputFileName.contains(".txt") ? "" : ".txt"))
				);
				for (String str : output)
					bf.write(str);
				bf.close();
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
	}
}
