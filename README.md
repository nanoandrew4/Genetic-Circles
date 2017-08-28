# Genetic-Circles

!This info will be updated as changes are made to the algorithm!

This program tries to determine the biggest circle that can fit on a window with other circles already drawn on to it.
Through the use of voronoi diagrams, the computer is able to compute with a good level of accuracy which the biggest radius achievable is.

With this number, the genetic algorithm can work towards a solution with a simple fitness function that will get closer to 1 as the circle approaches its maximum size, jumping to Double.MAX_VALUE when the size of the biggest circle is achieved. As an alternative, since some windows containing a lot of circles can make the algorithm run longer, there is an option to change how close the algorithm should get to the maximum size. Generally, a value in the range of 95-97 will yield a good result, and cut down on runtime a lot.

Voronoi diagram generally works well with many circles, but as the number decreases it can sometimes fail to find the biggest circle, causing the algorithm to find a solution that is more than 100% valid. Though this is supported, it is mentioned in case the user finds that it sometimes fails to find the best solution.

Crossover currently is done with two chromosomes at a time, and selects a random point in the chromosome and either works forward or backward (determined randomly) to swap the bits of the chromosomes.

# Usage
Compile code with the java files in the src directory alongside the kn folder, which contains all the necessary code for the voronoi diagrams

# Citation
Voronoi diagrams computed with the algorithm found at this page: https://github.com/ArlindNocaj/power-voronoi-diagram

Full citation as requested by the programmer of the above voronoi implementation:
Arlind Nocaj, Ulrik Brandes, "Computing Voronoi Treemaps: Faster, Simpler, and Resolution-independent", Computer Graphics Forum, vol. 31, no. 3, June 2012, pp. 855-864
