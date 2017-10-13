package gc;

/*
    This class contains all static variables accessed by various classes, or various instances of a class

 */

public class GlobalVars {

    // global window size
    final static int SCREEN_WIDTH = 1280;
    final static int SCREEN_HEIGHT = 720;

    final static int THREADS = Runtime.getRuntime().availableProcessors(); // number of threads for the program to use, if running multiple instances of the genetic algorithm at once

    final static int POOL_SIZE = 30; // global pool size for the genetic algorithm
    static int BAD_GENERATIONS = 10000; // number of generations without achieving a better result until genetic algorithm stops

    final static int STAT_CIRCLE_RADIUS = 5; // pixel radius of static circles

    final static double CROSSOVER_RATE = 0.7d; // see crossover function in gc.Chromosome class for utilization info
    final static double MUTATION_RATE = 0.01d; // see mutation function in gc.Chromosome class for utilization info
}
