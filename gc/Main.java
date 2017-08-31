package gc;

import javafx.application.Application;
import javafx.scene.Scene;
import javafx.scene.layout.Pane;
import javafx.scene.paint.Paint;
import javafx.scene.shape.Circle;
import javafx.scene.text.Font;
import javafx.scene.text.Text;
import javafx.stage.Stage;
import kn.uni.voronoitreemap.j2d.Point2D;

import java.util.ArrayList;
import java.util.Random;

/*
    Launches UI along with the Genetic Algorithm
 */

public class Main extends Application {

    static Pane pane;
    private Point2D[] circles;
    private Random rand = new Random(seed);

    private static Circle geneticCircle;
    private static Text genText;

    static int geneLength, largestRadius;
    private static long seed = 2;

    public static void main(String[] args) {
        String generalInfo = "Run program with no arguments to output help on usage";
        if (args.length == 1 && args[0].equals("ui")) {
            seed = System.currentTimeMillis();
            launch();
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
            launch();
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

    @Override
    public void start(Stage primaryStage) throws Exception {

        // depending on window size, encoded chromosome length will vary due to more digits being present to represent bigger numbers
        if (GlobalVars.screenHeight > GlobalVars.screenWidth) {
            geneLength = Integer.toBinaryString(GlobalVars.screenHeight).length();
            Chromosome.maxSize = GlobalVars.screenWidth / 2;
        } else {
            geneLength = Integer.toBinaryString(GlobalVars.screenWidth).length();
            Chromosome.maxSize = GlobalVars.screenHeight / 2;
        }

        pane = new Pane();
        Scene scene = new Scene(pane, GlobalVars.screenWidth, GlobalVars.screenHeight);

        circles = new Point2D[rand.nextInt(85) + 15];

        // see fitness class for specification on what it does, returns largest radius that can be drawn in the window
        CircleData cd = new Fitness().getBiggestCircle(circles, rand, false);
        largestRadius = cd.radius;
        System.out.println("largest radius: " + cd.radius);

        // add static circles to display
        for (Point2D circle : circles)
            pane.getChildren().add(new Circle(circle.getX(), circle.getY(), GlobalVars.circlesRadius));

        // draw largest circle as determined by the voronoi diagram
        pane.getChildren().add(new Circle(cd.coords.getX(), cd.coords.getY(), cd.radius, Paint.valueOf("green")));

        // sets graphics elements
        geneticCircle = new Circle(-10, -10, 1);
        pane.getChildren().add(geneticCircle);

        genText = new Text(10, 30, "Generation: 0");
        genText.setFont(Font.font(20));
        pane.getChildren().add(genText);

        primaryStage.setScene(scene);
        primaryStage.show();

        // launches genetic algorithm thread

        GeneticAlgorithm gA = new GeneticAlgorithm(circles, (int)seed, largestRadius);
        Thread t = new Thread(gA);

        t.setDaemon(true);
        t.setPriority(Thread.MAX_PRIORITY);
        t.start();
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

    // draws best circle from the generation and updates text
    static void draw(int x, int y, int radius, int gen) {
        //pane.getChildren().remove(geneticCircle);
        //geneticCircle = new Circle(x, y, radius, Paint.valueOf("red"));
        //pane.getChildren().add(geneticCircle);
        genText.setText("Generation: " + gen);
    }

    // draws final circle that conforms to specifications
    static void draw(Circle c, int gen) {
        pane.getChildren().remove(geneticCircle);
        geneticCircle = c;
        pane.getChildren().add(geneticCircle);
        genText.setText("Final generation: " + gen);
    }
}