package gc;

import java.awt.*;
import java.util.Random;

/*
    Contains and manipulates all chromosome data
 */

class SharedData {
	// in current genetic algorithm instance, largest valid radius found
	int largestRadius = 1;
	// for current genetic algorithm instance, set of static circles
	Point[] circles;

	// in current genetic algorithm instance, current generation
	int gen;
	// in current genetic algorithm instance, last gen a better genetic circle was found
	int lastUpdate;

	SharedData(Point[] circles) {
		this.circles = circles;
		largestRadius = 0;
		gen = 0;
		lastUpdate = 0;
	}
}

public class Chromosome {

	private Random rand;

	private StringBuffer chromo; // encoded chromosome in binary
	private SharedData sharedData;

	double fitness; // fitness score
	static int maxSize; // max size of initial chromosome

	// initializes chromosome with random size and coordinates
	Chromosome(Random rand, SharedData sharedData, CircleData geneticCircle) {
		this.rand = rand;
		this.sharedData = sharedData;
		chromo = new StringBuffer(Main.geneLength * 3);

		int size = rand.nextInt(maxSize);
		addGene(size); // first gene is size of circle
		addGene(rand.nextInt(GlobalVars.SCREEN_WIDTH - size)); // second gene is center x-coord of circle
		addGene(rand.nextInt(GlobalVars.SCREEN_HEIGHT - size)); // third gene is center y-coord of circle

		// calculates initial fitnessInteger.valueOf(genes[0])
		calcFitness(geneticCircle);
	}

	// adds gene to the chromosome, encoded in binary
	private void addGene(int i) {
		String gene = Integer.toBinaryString(i);
		for (int x = gene.length(); x < Main.geneLength; x++)
			chromo.append('0');
		chromo.append(gene);
	}

	// decodes the chromosome binary to understandable format using numbers
	private String decode() {
		String decodedChromo = "";

		for (int x = 0; x < chromo.length(); x += Main.geneLength)
			decodedChromo += Integer.parseInt(chromo.substring(x, x + Main.geneLength), 2) + (x != chromo.length() - Main.geneLength ? "-" : "");

		return decodedChromo;
	}

	// returns new circle object with the data provided by the chromosome in this chromosome object
	private CircleData getCircleData() {
		String[] genes = decode().split("-");
		return new CircleData(Integer.valueOf(genes[0]), Integer.valueOf(genes[1]), Integer.valueOf(genes[2]));
	}

	// calculates fitness of circle based on size relative to largestRadius in SharedData class
	void calcFitness(CircleData geneticCircle) {
		CircleData c = getCircleData();
		if (c.getRadius() >= sharedData.largestRadius && Main.isValid(c, sharedData.circles)) {
			geneticCircle.setRadius(c.getRadius());
			geneticCircle.setX(c.getX());
			geneticCircle.setY(c.getY());
			sharedData.largestRadius = c.getRadius();
			sharedData.lastUpdate = sharedData.gen;
			fitness = c.getRadius();
		} else if (c.getRadius() >= sharedData.largestRadius && c.getRadius() != sharedData.largestRadius)
			fitness = 1 / Math.abs(sharedData.largestRadius - c.getRadius());
		else if (c.getRadius() == sharedData.largestRadius)
			fitness = 1 / Math.abs(sharedData.largestRadius - c.getRadius() + 1);
	}

	// swaps bits from chromosomes from a random position, either forward or backwards
	void crossover(Chromosome c) {
		if (rand.nextDouble() > GlobalVars.CROSSOVER_RATE)
			return;

		int pos = rand.nextInt(chromo.length());

		if (rand.nextBoolean())
			for (int x = pos; x < chromo.length(); x++) {
				char tmp = c.chromo.charAt(x);
				c.chromo.setCharAt(x, this.chromo.charAt(x));
				this.chromo.setCharAt(x, tmp);
			}
		else
			for (int x = pos; x > 0; x--) {
				char tmp = c.chromo.charAt(x);
				c.chromo.setCharAt(x, this.chromo.charAt(x));
				this.chromo.setCharAt(x, tmp);
			}
	}

	// mutates a random bit in the chromosome
	void mutate() {
		if (rand.nextDouble() > GlobalVars.MUTATION_RATE)
			return;

		int pos = rand.nextInt(chromo.length() - 1);
		chromo.setCharAt(pos, chromo.charAt(pos) == '0' ? '1' : '0');
	}
}
