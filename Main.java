import javafx.application.Application;
import javafx.application.Platform;
import javafx.scene.Scene;
import javafx.scene.layout.Pane;
import javafx.scene.shape.Circle;
import javafx.scene.shape.Shape;
import javafx.scene.text.Font;
import javafx.scene.text.Text;
import javafx.stage.Stage;

import java.util.ArrayList;
import java.util.Random;

public class Main extends Application {

    static Pane pane;
    GeneticAlgorithm gA;

    static int screenWidth = 800;
    static int screenHeight = 600;
    static Random rand = new Random();
    final static int poolSize = 40;
    final static double proximityToMax = 0.97d;
    static int largestRadius;
    static int gen;

    static Circle geneticCircle;
    static Text genText;
    Thread t;

    static int geneLength;

    public static void main(String[] args) {
        launch();
    }

    @Override
    public void start(Stage primaryStage) throws Exception {

        // depending on window size, encoded chromosome length will vary due to more digits being present to represent bigger numbers
        if (screenHeight > screenWidth) {
            geneLength = Integer.toBinaryString(screenHeight).length();
            Chromosome.maxSize = screenHeight / 2;
        } else {
            geneLength = Integer.toBinaryString(screenWidth).length();
            Chromosome.maxSize = screenWidth / 2;
        }

        pane = new Pane();
        Scene scene = new Scene(pane, screenWidth, screenHeight);

        // see fitness class for specification on what it does, returns largest radius that can be drawn in the window
        // also adds static circles
        largestRadius = new Fitness().getBiggestCircle(true, false);


        // sets graphics elements
        geneticCircle = new Circle(-10, -10, 1);
        pane.getChildren().add(geneticCircle);

        genText = new Text(10, 30, "Generation: " + gen);
        genText.setFont(Font.font(20));
        pane.getChildren().add(genText);

        primaryStage.setScene(scene);
        primaryStage.show();

        // launches genetic algorithm thread

        gA = new GeneticAlgorithm();
        t = new Thread(gA);

        t.setDaemon(true);
        t.setPriority(Thread.MAX_PRIORITY);
        t.start();
    }

    // returns whether shape is intersecting with any others
    static boolean isEmpty(Circle c) {

        if (c.getCenterX() < c.getRadius() || c.getCenterX() > screenWidth - c.getRadius() ||
                c.getCenterY() < c.getRadius() || c.getCenterY() > screenHeight - c.getRadius())
            return false;

        // last element will be genetic circle, do not check, nor the text element
        for (int x = 0; x < Fitness.circles; x++)
            if (pane.getChildren().get(x) instanceof Circle)
                if (Shape.intersect(c, (Circle)pane.getChildren().get(x)).getBoundsInLocal().getWidth() != -1)
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
        genText.setText("Generation: " + gen);
    }

    // draws final circle that conforms to specifications
    static void draw(Circle c) {
        pane.getChildren().remove(geneticCircle);
        geneticCircle = c;
        pane.getChildren().add(geneticCircle);
        genText.setText("Final generation: " + gen);
    }
}

class GeneticAlgorithm extends Thread {

    private ArrayList<Chromosome> pool;
    private ArrayList<Chromosome> newPool;

    // initializes pools
    GeneticAlgorithm() {
        pool = new ArrayList<>(Main.poolSize);
        newPool = new ArrayList<>(Main.poolSize);

        for (int x = 0; x < Main.poolSize; x++)
            pool.add(new Chromosome());
    }

    // runs genetic algorithm
    public void run() {

        while (true) {
            for (int x = 0; x < Main.poolSize; x+=2) {
                Chromosome c1 = Main.selectFittest(pool);
                Chromosome c2 = Main.selectFittest(pool);

                // cross chromosomes at random point, read crossover function in Chromosome class for more detailed info
                c1.crossover(c2);

                // mutate chromosomes, read mutate function in Chromosome class for more detailed info
                c1.mutate();
                c2.mutate();

                // recalculate fitness for both chromosomes
                c1.calcFitness();
                c2.calcFitness();

                // if either chromosome is a valid solution, stops thread from running and draws solution to screen
                if (c1.fitness == Integer.MAX_VALUE && Main.isEmpty(c1.getCircle())) {
                    System.out.println("Circle of generation " + Main.gen + " is a valid solution");
                    Platform.runLater(() -> Main.draw(c1.getCircle()));
                    System.out.println("Circle size is: " + (Double.valueOf(c1.decode().split("-")[0]) / (double)Main.largestRadius) * 100d
                            + "% with radius " + c1.decode().split("-")[0]);
                    return;
                } else if (c2.fitness == Integer.MAX_VALUE && Main.isEmpty(c2.getCircle())) {
                    System.out.println("Circle of generation " + Main.gen + " is a valid solution");
                    Platform.runLater(() -> Main.draw(c2.getCircle()));
                    System.out.println("Circle size is: " + (Double.valueOf(c2.decode().split("-")[0]) / (double)Main.largestRadius) * 100d
                            + "% with radius " + c2.decode().split("-")[0]);
                    return;
                }

                // add chromosomes to new pool
                newPool.add(c1);
                newPool.add(c2);
            }

            // prepare for next iteration
            pool.addAll(newPool);
            newPool.clear();
            Main.gen++;


            //Chromosome fittest = Main.selectFittest(pool);
            //String[] genes = fittest.decode().split("-");
            //pool.add(fittest);
            //Platform.runLater(() -> {Main.draw(Integer.valueOf(genes[1]), Integer.valueOf(genes[2]), Integer.valueOf(genes[0]));});

            // will run algorithm faster
            Platform.runLater(() -> {Main.draw(-1, -1, -1);});
        }
    }
}
