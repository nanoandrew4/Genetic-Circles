package gc;

import javafx.application.Application;
import kn.uni.voronoitreemap.j2d.Point2D;

import java.util.ArrayList;

/*
    Launches UI along with the Genetic Algorithm
 */

public class Main {

    static long seed;
    static int geneLength;

    public static void main(String[] args) {
        String generalInfo = "Run program with no arguments to output help on usage";
        if (args.length == 1 && args[0].equals("ui")) {
            seed = System.currentTimeMillis();
            launchUI();
        } else if (args.length == 2 && args[0].equals("ui")) {
            try {
                seed = Integer.valueOf(args[1]);
                if (seed < 0) {
                    System.out.println("Illegal argument. Please enter positive integer to be used as seed. " + generalInfo);
                }
            } catch (NumberFormatException e) {
                System.out.println("Illegal argument. Please enter positive integer to be used as seed. " + generalInfo);
                return;
            }
            launchUI();
        } else if (args.length == 3 && args[0].equals("headless")) {
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
            } catch (NumberFormatException e) {
                System.out.println("Illegal argument. Please enter positive integer to be used as seed. " + generalInfo);
                return;
            }

            new DataCollector();
        } else {
            System.out.println("To use graphical version: java -jar GeneticCircles.jar ui [seed]");
            System.out.println("Seed argument is a positive integer representing the seed to be used when generating random numbers, and is optional");
            System.out.println();
            System.out.println("To use non-graphical version: java -jar GeneticCircles.jar headless [GenerateFile] [runs]");
            System.out.println("GenerateFile is either true or false, and specifies whether the program should write the results to a text file");
            System.out.println("Runs is a positive integer representing the number of iterations to do of the genetic algorithm " +
                    " with seed values starting from 0 and ending at (runs - 1)");
        }
    }

    static void launchUI() {
        new Thread(() -> Application.launch(UI.class)).start();
    }

    // returns whether circle is out of bounds of the window
    private static boolean outOfBounds(CircleData c) {
        return c.coords.getX() < c.radius || c.coords.getY() < c.radius
                || c.coords.getX() + c.radius > GlobalVars.screenWidth || c.coords.getY() + c.radius > GlobalVars.screenHeight;
    }

    // returns whether circle intersects any others
    static boolean isValid(CircleData c, Point2D[] circles) {

        if (outOfBounds(c))
            return false;

        // last element will be genetic circle, do not check, nor the text element
        for (Point2D circle : circles) {
            if (circle != null && Fitness.calcEucledianDistance(c.coords, circle) < c.radius + GlobalVars.circlesRadius)
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