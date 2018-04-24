package gc;

/**
 * This class holds a circle's relevant data for use in the genetic algorithm.
 */

public class CircleData {
	private int x, y, radius;

	CircleData(int x, int y, int radius) {
		this.x = x;
		this.y = y;
		this.radius = radius;
	}

	void setX(int x) {
		this.x = x;
	}

	void setY(int y) {
		this.y = y;
	}

	void setRadius(int radius) {
		this.radius = radius;
	}

	int getX() {
		return x;
	}

	int getY() {
		return y;
	}

	int getRadius() {
		return radius;
	}
}
