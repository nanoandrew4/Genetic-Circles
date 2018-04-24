package gc;

import java.awt.*;
import java.util.Random;

/**
 * Contains data that is shared across all chromosomes.
 */
class SharedData {
	int largestRadius = 1; // Largest valid radius found by genetic algorithm
	Point[] circles; // Array of static circles in the virtual screen

	int gen; // Current generation being evolved
	int lastUpdate; // Generation the largest radius was found

	SharedData(Point[] circles) {
		this.circles = circles;
		largestRadius = gen = lastUpdate = 0;
	}
}

public class Chromosome {
	private Random rand;
	private StringBuffer chromo; // Encoded circle data in binary
	private SharedData sharedData; // Data to be shared with all chromosomes

	double fitness; // Fitness score
	static int maxSize; // Max size of initial chromosome

	/**
	 * Initializes instance of a chromosome, encodes the data for a circle in binary (radius and coordinates on virtual
	 * screen) and calculates the initial fitness of this chromosome.
	 *
	 * @param rand          Random number generator instance
	 * @param sharedData    Shared data object that all chromosomes are to share
	 * @param geneticCircle Circle data to be encoded in this chromosome
	 */
	Chromosome(Random rand, SharedData sharedData, CircleData geneticCircle) {
		this.rand = rand;
		this.sharedData = sharedData;
		chromo = new StringBuffer(Main.geneLength * 3);

		int size = rand.nextInt(maxSize);
		addGene(size); // First gene is size of circle
		addGene(rand.nextInt(GlobalVars.SCREEN_WIDTH - size)); // Second gene is center x-coord of circle
		addGene(rand.nextInt(GlobalVars.SCREEN_HEIGHT - size)); // Third gene is center y-coord of circle

		calcFitness(geneticCircle);
	}

	/**
	 * Encodes a gene in the chromosome, in binary. The chromosome will end up being a sequence of genes (binary nums)
	 *
	 * @param gene Integer to be converted to binary and encoded in the chromosome
	 */
	private void addGene(int gene) {
		String geneBinary = Integer.toBinaryString(gene);
		for (int x = geneBinary.length(); x < Main.geneLength; x++)
			chromo.append('0'); // Pads gene so that each gene uses the same number of bits
		chromo.append(geneBinary);
	}

	/**
	 * Decodes a chromosome to a regular integer, for better readability, and returns a sequence of genes in integer
	 * format as a string.
	 *
	 * @return String representation of the sequence of genes encoded in the chromosome, as a sequence of integers
	 */
	private String decode() {
		String decodedChromo = "";

		for (int x = 0; x < chromo.length(); x += Main.geneLength)
			decodedChromo += Integer.parseInt(chromo.substring(x, x + Main.geneLength), 2)
					+ (x != chromo.length() - Main.geneLength ? "-" : "");

		return decodedChromo;
	}

	/**
	 * Returns a 'CircleData' object with the data encoded in the chromosome. Used in 'calcFitness' method.
	 *
	 * @return 'CircleData' object with the data encoded in the chromosome.
	 */
	private CircleData getCircleData() {
		String[] genes = decode().split("-");
		return new CircleData(Integer.valueOf(genes[0]), Integer.valueOf(genes[1]), Integer.valueOf(genes[2]));
	}

	/**
	 * Calculates the fitness of this chromosome. The fitness is the inverse of the difference between the largest
	 * radius found and the radius of the circle this chromosome represents.
	 *
	 * @param geneticCircle 'CircleData' object of the best circle found at the present time
	 */
	void calcFitness(CircleData geneticCircle) {
		CircleData c = getCircleData();
		if (c.getRadius() > sharedData.largestRadius && isValid(c, sharedData.circles)) {
			geneticCircle.setRadius(c.getRadius());
			geneticCircle.setX(c.getX());
			geneticCircle.setY(c.getY());
			sharedData.largestRadius = c.getRadius();
			sharedData.lastUpdate = sharedData.gen;
			fitness = c.getRadius();
		} else if (c.getRadius() == sharedData.largestRadius)
			fitness = 1;
		else
			fitness = 1 / Math.abs(sharedData.largestRadius - c.getRadius());
	}

	/**
	 * Determines if a circle is in the bounds of the virtual screen.
	 *
	 * @param c Circle to check bounds for
	 * @return True if out of bounds, false if in bounds
	 */
	private boolean outOfBounds(CircleData c) {
		return c.getX() < c.getRadius() || c.getY() < c.getRadius()
				|| c.getX() + c.getRadius() > GlobalVars.SCREEN_WIDTH
				|| c.getY() + c.getRadius() > GlobalVars.SCREEN_HEIGHT;
	}

	/**
	 * Determines if a circle intersects with any of the static circles that dot the virual screen.
	 *
	 * @param c Circle to check validity for
	 * @param circles Array of static circles to check against the genetic circle
	 * @return True if the genetic circle is valid, false if it intersects with any of the static circles
	 */
	private boolean isValid(CircleData c, Point[] circles) {
		if (outOfBounds(c)) return false;
		for (Point circle : circles) {
			if (Util.calcEucledianDistance(c.getX(), c.getY(), circle) < c.getRadius() + GlobalVars.STAT_CIRCLE_RADIUS)
				return false;
		}
		return true;
	}

	/**
	 * Performs crossover on two chromosomes. A random position in the chromosome is chosen, then the bits are swapped
	 * from the start to that position, or from that position to the end.
	 *
	 * @param c Chromosome to perform crossover with
	 */
	void crossover(Chromosome c) {
		if (rand.nextDouble() > GlobalVars.CROSSOVER_RATE)
			return;

		int pos = rand.nextInt(chromo.length());

		if (rand.nextBoolean()) // Swap bits from random position to end of chromosome
			for (int x = pos; x < chromo.length(); x++) {
				char tmp = c.chromo.charAt(x);
				c.chromo.setCharAt(x, this.chromo.charAt(x));
				this.chromo.setCharAt(x, tmp);
			}
		else // Swap bits from start of chromosome to random position
			for (int x = pos; x > 0; x--) {
				char tmp = c.chromo.charAt(x);
				c.chromo.setCharAt(x, this.chromo.charAt(x));
				this.chromo.setCharAt(x, tmp);
			}
	}

	/**
	 * Performs a mutation of the chromosome, by flipping a random bit in the chromosome.
	 */
	void mutate() {
		if (rand.nextDouble() > GlobalVars.MUTATION_RATE)
			return;

		int pos = rand.nextInt(chromo.length() - 1);
		chromo.setCharAt(pos, chromo.charAt(pos) == '0' ? '1' : '0');
	}
}
