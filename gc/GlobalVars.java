package gc;

/*
    This class contains all static variables accessed by various classes, or various instances of a class

    Maximum verified proximity is 97% compared to the calculated largest circle in general, more than that
    does not complete on some seed values
 */

public class GlobalVars {

    // global window size
    final static int screenWidth = 1280;
    final static int screenHeight = 720;

    final static int threads = Runtime.getRuntime().availableProcessors(); // number of threads for the program to use, if running multiple instances of the genetic algorithm at once

    final static int poolSize = 30; // global pool size for the genetic algorithm
    final static double proximityToMax = 0.97d; // percentage circle has to surpass in relation to largestRadius variable to be considered valid

    final static int circlesRadius = 5; // pixel radius of static circles

    final static double crossoverRate = 0.7d; // see crossover function in gc.Chromosome class for utilization info
    final static double mutationRate = 0.01d; // see mutation function in gc.Chromosome class for utilization info
}
