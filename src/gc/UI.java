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
import org.omg.PortableServer.THREAD_POLICY_ID;

import java.awt.*;
import java.util.Random;

/**
 * Class containing code for all UI related elements of the program
 */

public class UI extends Application {
	private int seed = (int) Main.seed; // Seed for RNG

	private static GenCounter genCounter; // Generation counter to be used in UI

	static Pane pane;

	@Override
	public void start(Stage primaryStage) throws Exception {
		Random rand = new Random(seed);

		// Set the gene length and max radius, based on the max value a gene can take (virtual screen size)
		if (GlobalVars.SCREEN_HEIGHT > GlobalVars.SCREEN_WIDTH) {
			Main.geneLength = Integer.toBinaryString(GlobalVars.SCREEN_HEIGHT).length();
			Chromosome.maxSize = GlobalVars.SCREEN_HEIGHT / 2;
		} else {
			Main.geneLength = Integer.toBinaryString(GlobalVars.SCREEN_WIDTH).length();
			Chromosome.maxSize = GlobalVars.SCREEN_HEIGHT / 2;
		}

		pane = new Pane();
		Scene scene = new Scene(pane, GlobalVars.SCREEN_WIDTH, GlobalVars.SCREEN_HEIGHT);

		// Generate static circles
		int maxXCoord = GlobalVars.SCREEN_WIDTH - 2 * GlobalVars.STAT_CIRCLE_RADIUS;
		int maxYCoord = GlobalVars.SCREEN_HEIGHT - 2 * GlobalVars.STAT_CIRCLE_RADIUS;
		Main.circles = new Point[1][rand.nextInt(100)];
		for (int i = 0; i < Main.circles[0].length; i++) {
			Main.circles[0][i] = new Point(rand.nextInt(maxXCoord) + GlobalVars.STAT_CIRCLE_RADIUS,
										  rand.nextInt(maxYCoord) + GlobalVars.STAT_CIRCLE_RADIUS);
		}

		for (Point circle : Main.circles[0])
			pane.getChildren().add(new Circle(circle.getX(), circle.getY(), GlobalVars.STAT_CIRCLE_RADIUS,
					Paint.valueOf("orange")));

		primaryStage.setScene(scene);
		primaryStage.show();

		GeneticAlgorithm gA = new GeneticAlgorithm(Main.circles[0], seed);

		genCounter = new GenCounter(gA, pane);

		gA.setDaemon(true);
		gA.setPriority(Thread.MAX_PRIORITY);

		genCounter.setDaemon(true);
		genCounter.setPriority(Thread.MAX_PRIORITY);

		gA.start();
		genCounter.start();
	}

	/**
	 * Draws best circle found before GA decided to stop running. Only called once the GA has stopped.
	 *
	 * @param c Data representing best circle found
	 */
	static void draw(CircleData c) {
		pane.getChildren().add(new Circle(c.getX(), c.getY(), c.getRadius(), Paint.valueOf("red")));
		genCounter.displayFinalGen();
	}
}

/**
 * Generation counter for UI.
 */
class GenCounter extends Thread {
	private GeneticAlgorithm gA;
	private Text genText;
	private Pane parentPane;

	GenCounter(GeneticAlgorithm gA, Pane pane) {
		this.gA = gA;
		this.parentPane = pane;

		genText = new Text(10, 30, "Generation: 0");
		genText.setFont(Font.font(20));
		pane.getChildren().add(genText);
	}

	@Override
	public void run() {
		while (!gA.getDone()) {
			Platform.runLater(() -> genText.setText("Generations: " + gA.getSharedData().gen));
			try {
				Thread.sleep(33);
			} catch (InterruptedException e) {
				e.printStackTrace();
			}
		}
	}

	void displayFinalGen() {
		parentPane.getChildren().remove(genText);
		parentPane.getChildren().add(genText);
		genText.setText("Final generation: " + gA.getSharedData().gen);
	}
}