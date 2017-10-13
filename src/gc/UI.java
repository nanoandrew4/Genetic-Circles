package gc;

import javafx.application.Application;
import javafx.scene.Scene;
import javafx.scene.layout.Pane;
import javafx.scene.paint.Paint;
import javafx.scene.shape.Circle;
import javafx.scene.text.Font;
import javafx.scene.text.Text;
import javafx.stage.Stage;

import java.awt.*;
import java.util.Random;

public class UI extends Application {

	private Random rand;
	private int seed = (int)Main.seed; // seed to be used in random number generator

	static Pane pane;
	private static Text genText;

	public static void main(String[] args) {
		Application.launch(args);
	}

	@Override
	public void start(Stage primaryStage) throws Exception {

		rand = new Random(seed);

		// depending on window size, encoded chromosome length will vary due to more digits being present to represent bigger numbers
		if (GlobalVars.SCREEN_HEIGHT > GlobalVars.SCREEN_WIDTH) {
			Main.geneLength = Integer.toBinaryString(GlobalVars.SCREEN_HEIGHT).length();
			Chromosome.maxSize = GlobalVars.SCREEN_HEIGHT / 2;
		} else {
			Main.geneLength = Integer.toBinaryString(GlobalVars.SCREEN_WIDTH).length();
			Chromosome.maxSize = GlobalVars.SCREEN_HEIGHT / 2;
		}

		pane = new Pane();
		Scene scene = new Scene(pane, GlobalVars.SCREEN_WIDTH, GlobalVars.SCREEN_HEIGHT);

		// generate static circles
		Main.circles = new Point[1][rand.nextInt(100)];
		for (int i = 0; i < Main.circles[0].length; i++) {
			Main.circles[0][i] = new Point(rand.nextInt(GlobalVars.SCREEN_WIDTH - 2 * GlobalVars.STAT_CIRCLE_RADIUS) + GlobalVars.STAT_CIRCLE_RADIUS,
					rand.nextInt(GlobalVars.SCREEN_HEIGHT - 2 * GlobalVars.STAT_CIRCLE_RADIUS) + GlobalVars.STAT_CIRCLE_RADIUS);
		}

		// add static circles to display
		for (Point circle : Main.circles[0])
			pane.getChildren().add(new Circle(circle.getX(), circle.getY(), GlobalVars.STAT_CIRCLE_RADIUS, Paint.valueOf("black")));

		// add generation counter to screen
		genText = new Text(10, 30, "Generation: 0");
		genText.setFont(Font.font(20));
		pane.getChildren().add(genText);

		primaryStage.setScene(scene);
		primaryStage.show();

		// launches genetic algorithm thread

		GeneticAlgorithm gA = new GeneticAlgorithm(Main.circles[0], seed);
		Thread t = new Thread(gA);

		t.setDaemon(true);
		t.setPriority(8);
		t.start();
	}

	// draws best circle from the generation and updates text
	static void updateText(int gen) {
		genText.setText("Generation: " + gen);
	}

	// draws final circle that conforms to specifications
	static void draw(CircleData c, int gen) {
		pane.getChildren().add(new Circle(c.getX(), c.getY(), c.getRadius(), Paint.valueOf("red")));
		genText.setText("Final generation: " + gen);
	}
}