package gc;

import javafx.application.Application;

import java.awt.*;
import java.util.ArrayList;

/*
    Launches UI along with the Genetic Algorithm
 */

public class Main {

	static long seed;
	static int geneLength;

	static Point[][] circles;

	public static void main(String[] args) {
		String generalInfo = "Run program with no arguments to output help on usage";
		if (args.length == 1 && args[0].equals("ui")) {
			seed = System.currentTimeMillis();
			launchUI();
		} else if (args[0].equals("ui")) {
			try {
				seed = Integer.valueOf(args[1]);
				if (seed < 0) {
					System.out.println("Illegal argument. Please enter positive integer to be used as seed. " + generalInfo);
					return;
				}
				GlobalVars.BAD_GENERATIONS = Integer.valueOf(args[2]);
				if (GlobalVars.BAD_GENERATIONS < 0) {
					System.out.println("Illegal argument. Please enter a positive integer for badGenerations. " + generalInfo);
					return;
				}
			} catch (Exception e) {
				System.out.println(generalInfo);
				return;
			}
			launchUI();
		} else if (args[0].equals("headless")) {
			if ((args[1].equals("true") || args[1].equals("false")))
				DataCollector.generateFile = args[1].equals("true");
			else {
				System.out.println("Second argument must be either \"true\" or \"false\". " + generalInfo);
				return;
			}
			try {
				DataCollector.runs = Integer.valueOf(args[2]);
				if (DataCollector.runs < 1) {
					System.out.println("Illegal argument. Runs value must be higher than 0. " + generalInfo);
					return;
				}
				GlobalVars.BAD_GENERATIONS = Integer.valueOf(args[3]);
				if (GlobalVars.BAD_GENERATIONS < 0) {
					System.out.println("Illegal argument. badGenerations value must be greater than or equal to 0. " + generalInfo);
					return;
				}
			} catch (Exception e) {
				System.out.println(generalInfo);
				return;
			}

			new DataCollector();
		} else {
			System.out.println("To use graphical version: java -jar GeneticCircles.jar ui [seed] [badGenerations]");
			System.out.println("OPTIONAL: Seed argument is a positive integer representing the seed to be used when generating random numbers");
			System.out.println("OPTIONAL: badGenerations is a positive integer representing the number of generations to be evolved after the newest change to the genetic circle. " +
					"If badGenerations = 5000 and the generation of you current best genetic circle is 3000, the program will halt at generation 8001, unless it finds a better circle before then.");
			System.out.println();
			System.out.println("To use non-graphical version: java -jar GeneticCircles.jar headless [GenerateFile] [runs]");
			System.out.println("GenerateFile is either true or false, and specifies whether the program should write the results to a text file");
			System.out.println("Runs is a positive integer representing the number of iterations to do of the genetic algorithm " +
					" with seed values starting from 0 and ending at (runs - 1)");
			System.out.println("OPTIONAL: badGenerations is a positive integer representing the number of generations to be evolved after the newest change to the genetic circle. " +
					"If badGenerations = 5000 and the generation of you current best genetic circle is 3000, the program will halt at generation 8001, unless it finds a better circle before then.");
		}
	}

	private static void launchUI() {
		new Thread(() -> Application.launch(UI.class)).start();
	}

	// returns whether circle is out of bounds of the window
	private static boolean outOfBounds(CircleData c) {
		return c.getX() < c.getRadius() || c.getY() < c.getRadius()
				|| c.getX() + c.getRadius() > GlobalVars.SCREEN_WIDTH || c.getY() + c.getRadius() > GlobalVars.SCREEN_HEIGHT;
	}

	// returns whether circle intersects any others
	static boolean isValid(CircleData c, Point[] circles) {

		if (outOfBounds(c))
			return false;
		// check collision between genetic circle and all circles on screen
		for (Point circle : circles) {
			if (Util.calcEucledianDistance(c.getX(), c.getY(), circle) < c.getRadius() + GlobalVars.STAT_CIRCLE_RADIUS)
				return false;
		}
		return true;
	}

	// selects fittest chromosome from pool passed as arr
	static Chromosome selectFittest(ArrayList<Chromosome> arr) {
		double maxFitness = arr.get(0).fitness;
		int fittest = 0;
		for (int x = 1; x < arr.size(); x++)
			if (arr.get(x).fitness > maxFitness) {
				maxFitness = arr.get(x).fitness;
				fittest = x;
			}

		Chromosome c = arr.get(fittest);
		arr.remove(c);

		return c;
	}
}