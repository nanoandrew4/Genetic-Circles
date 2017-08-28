package gc;

import javafx.application.Application;
import javafx.application.Platform;
import javafx.scene.Scene;
import javafx.scene.layout.Pane;
import javafx.scene.paint.Paint;
import javafx.scene.shape.Circle;
import javafx.scene.text.Font;
import javafx.scene.text.Text;
import javafx.stage.Stage;
import kn.uni.voronoitreemap.j2d.Point2D;

import java.util.ArrayList;

/*
    Launches UI along with the Genetic Algorithm
 */

public class Main extends Application {

    static Pane pane;

    private static Circle geneticCircle;
    private static Text genText;

    static int geneLength;

    public static void main(String[] args) {
        launch();
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

        // see fitness class for specification on what it does, returns largest radius that can be drawn in the window
        CircleData cd = new Fitness().getBiggestCircle(false);
        GlobalVars.largestRadius = cd.radius;
        System.out.println("largest radius: " + cd.radius);

        // add static circles to display
        for (Point2D circle : GlobalVars.circles)
            pane.getChildren().add(new Circle(circle.getX(), circle.getY(), GlobalVars.circlesRadius));

        // draw largest circle as determined by the voronoi diagram
        pane.getChildren().add(new Circle(cd.coords.getX(), cd.coords.getY(), cd.radius, Paint.valueOf("green")));

        // sets graphics elements
        geneticCircle = new Circle(-10, -10, 1);
        pane.getChildren().add(geneticCircle);

        genText = new Text(10, 30, "Generation: " + GlobalVars.gen);
        genText.setFont(Font.font(20));
        pane.getChildren().add(genText);

        primaryStage.setScene(scene);
        primaryStage.show();

        // launches genetic algorithm thread

        GeneticAlgorithm gA = new GeneticAlgorithm();
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
    static boolean isValid(CircleData c) {

        if (outOfBounds(c))
            return false;

        // last element will be genetic circle, do not check, nor the text element
        for (Point2D circle : GlobalVars.circles)
            if (Fitness.calcEucledianDistance(c.coords, circle) < c.radius + GlobalVars.circlesRadius)
                return false;

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
    static void draw(int x, int y, int radius) {
        //pane.getChildren().remove(geneticCircle);
        //geneticCircle = new Circle(x, y, radius, Paint.valueOf("red"));
        //pane.getChildren().add(geneticCircle);
        genText.setText("Generation: " + GlobalVars.gen);
    }

    // draws final circle that conforms to specifications
    static void draw(Circle c) {
        pane.getChildren().remove(geneticCircle);
        geneticCircle = c;
        pane.getChildren().add(geneticCircle);
        genText.setText("Final generation: " + GlobalVars.gen);
    }
}

class GeneticAlgorithm extends Thread {

    private ArrayList<Chromosome> pool;
    private ArrayList<Chromosome> newPool;

    // initializes pools
    GeneticAlgorithm() {
        pool = new ArrayList<>(GlobalVars.poolSize);
        newPool = new ArrayList<>(GlobalVars.poolSize);

        for (int x = 0; x < GlobalVars.poolSize; x++)
            pool.add(new Chromosome());
    }

    // runs genetic algorithm
    public void run() {

        while (true) {
            for (int x = 0; x < GlobalVars.poolSize; x+=2) {
                Chromosome c1 = Main.selectFittest(pool);
                Chromosome c2 = Main.selectFittest(pool);

                // cross chromosomes at random point, read crossover function in gc.Chromosome class for more detailed info
                c1.crossover(c2);

                // mutate chromosomes, read mutate function in gc.Chromosome class for more detailed info
                c1.mutate();
                c2.mutate();

                // recalculate fitness for both chromosomes
                c1.calcFitness();
                c2.calcFitness();

                // if either chromosome is a valid solution, stops thread from running and draws solution to screen
                if (c1.fitness == Integer.MAX_VALUE && Main.isValid(c1.getCircleData())) {
                    System.out.println("Circle of generation " + GlobalVars.gen + " is a valid solution");
                    if (Main.pane != null) {
                        CircleData cd = c1.getCircleData();
                        Platform.runLater(() -> Main.draw(new Circle(cd.coords.getX(), cd.coords.getY(), cd.radius, Paint.valueOf("red"))));
                    }
                    System.out.println("Circle size is: " + ((Double.valueOf(c1.decode().split("-")[0])) / GlobalVars.largestRadius) * 100d
                            + "% with radius " + (Integer.valueOf(c1.decode().split("-")[0])));
                    System.out.println();
                    return;
                } else if (c2.fitness == Integer.MAX_VALUE && Main.isValid(c2.getCircleData())) {
                    System.out.println("Circle of generation " + GlobalVars.gen + " is a valid solution");
                    if (Main.pane != null) {
                        CircleData cd = c2.getCircleData();
                        Platform.runLater(() -> Main.draw(new Circle(cd.coords.getX(), cd.coords.getY(), cd.radius, Paint.valueOf("red"))));
                    }
                    System.out.println("Circle size is: " + ((Double.valueOf(c2.decode().split("-")[0])) / GlobalVars.largestRadius) * 100d
                            + "% with radius " + (Integer.valueOf(c2.decode().split("-")[0])));
                    System.out.println();
                    return;
                }

                // add chromosomes to new pool
                newPool.add(c1);
                newPool.add(c2);
            }

            // prepare for next iteration
            pool.addAll(newPool);
            newPool.clear();
            GlobalVars.gen++;


            //gc.Chromosome fittest = gc.Main.selectFittest(pool);
            //String[] genes = fittest.decode().split("-");
            //pool.add(fittest);
            //Platform.runLater(() -> {gc.Main.draw(Integer.valueOf(genes[1]), Integer.valueOf(genes[2]), Integer.valueOf(genes[0]));});

            // will run algorithm faster
            if (Main.pane != null)
                Platform.runLater(() -> Main.draw(-1, -1, -1));
        }
    }
}
