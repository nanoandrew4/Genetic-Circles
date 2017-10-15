# Genetic-Circles

This program tries to determine the biggest circle that can fit on a screen with other circles already drawn on to it.

Screen does not refer to the visual element, it refers to a 2D plane on which the static circles exist and the algorithm evolves. Even when running in headless mode, the screen size matters.

Evolution of the algorithm runs until the algorithm has not found a better solution of a number of generations (specified with the -bg option, and who's default value can be found in GlobalVars.java)

Crossover currently is done with two chromosomes at a time, and selects a random point in the chromosome and either works forward or backward (determined randomly) to swap the bits of the chromosomes.

The DataCollector class uses all threads available to it minus one to run the sequential iterations of the Genetic Algorithm instances.
Not all threads are started at once to prevent crashing due to excessive threads being created. The number of threads is capped at the threads available minus one, and once a thread is finished, a new one will start.

# Usage
Compile code with the java files in the src directory

Command syntax: java -jar GeneticCircles.jar [mode] [options]

## Modes:

- ui: runs program with graphical interface to visualize results
- headless: runs program without graphical interface, can run multiple instances of the algorithm (see mode specific options)

## Options:

- \-sw: Screen width
- \-sh: Screen height
- \-t: Number of threads the program can use, default is all minus one * 1.1. Only used in headless mode, for data collection
- \-ps: Pool size, also known as population size. Must be divisible by 2
- \-bg: Number of generations to evolve without any improvement. If bg = 5, for example, and no improvements are found for the next 5 generations, the algorithm will stop
- \-scr: Radius of the static circles on the screen
- \-cr: Crossover rate, specifies how likely the two chromosomes are to perform crossover
- \-mr: Mutation rate, specifies how likely a function is to mutate
- \-w: Output file name to write to, if output should be stored in a file
- \-s: Seed to be used in random number generator, if none is entered the program will use the current time in milliseconds since the epoch

### Mode specific options:

- #### Headless:
  - \-hr: Random seeds used, default is sequential seeds based on offset and iterations
  - \-ho: Offset to use for seeds when running sequentially
  - \-hi: Number of iterations to run of the algorithm
