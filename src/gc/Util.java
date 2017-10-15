package gc;

import java.awt.*;

/*
	Contains static functions used in the program
 */

public class Util {

	// calculates euclidean distance between two points
	static double calcEucledianDistance(int cx, int cy, Point p2) {
		return Math.sqrt(Math.pow(cx - p2.getX(), 2) + Math.pow(cy - p2.getY(), 2));
	}

	static String getRunTime(long millis) {
		long seconds = millis / 1000;
		String time = "";

		if (seconds / 86400 >= 1)
			time += String.valueOf(seconds / 86400) + " days, ";
		if ((seconds / 3600) >= 1)
			time += String.valueOf((seconds / 3600) % 24) + " hours, ";
		if ((seconds / 60) >= 1)
			time += String.valueOf((seconds / 60) % 60) + " minutes, ";

		time += String.valueOf(seconds % 60) + " seconds, ";
		time += String.valueOf(millis % 1000) + " millis";

		return time;
	}
}
