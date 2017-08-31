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

import java.util.Random;

public class UI extends Application {

    static Pane pane;

    private Point2D[] circles;
    static Circle geneticCircle;
    private Random rand;

    private int largestRadius, seed = (int)Main.seed;

    private static Text genText;

    public static void main(String[] args) {
        Application.launch(args);
    }

    @Override
    public void start(Stage primaryStage) throws Exception {

        rand = new Random(seed);

        // depending on window size, encoded chromosome length will vary due to more digits being present to represent bigger numbers
        if (GlobalVars.screenHeight > GlobalVars.screenWidth) {
            Main.geneLength = Integer.toBinaryString(GlobalVars.screenHeight).length();
            Chromosome.maxSize = GlobalVars.screenWidth / 2;
        } else {
            Main.geneLength = Integer.toBinaryString(GlobalVars.screenWidth).length();
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