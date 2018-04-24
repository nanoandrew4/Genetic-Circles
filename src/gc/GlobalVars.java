package gc;

/**
 * This class contains all global variables for use across the whole program.
 */

public class GlobalVars {

	// Global window size
	static int SCREEN_WIDTH = 1280;
	static int SCREEN_HEIGHT = 720;

	private static int availableProcessors = Runtime.getRuntime().availableProcessors();

	// Number of threads for the program to use, if running multiple instances of the GA at once
	static int THREADS = (int) ((availableProcessors > 0 ? availableProcessors - 1 : availableProcessors) * 1.1);

	static int POOL_SIZE = 30; // Global pool size for the genetic algorithm
	static int BAD_GENERATIONS = 10000; // Number of generations without achieving a better result until GA stops

	static int STAT_CIRCLE_RADIUS = 5; // Pixel radius of static circles

	// Variables used in the Chromosome class, to determine rate of crossover and mutation
	static double CROSSOVER_RATE = 0.7d;
	static double MUTATION_RATE = 0.01d;
}
