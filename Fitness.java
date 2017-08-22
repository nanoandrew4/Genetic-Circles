import javafx.scene.paint.Paint;
import javafx.scene.shape.Circle;
import javafx.scene.shape.Polygon;
import kn.uni.voronoitreemap.datastructure.OpenList;
import kn.uni.voronoitreemap.diagram.PowerDiagram;
import kn.uni.voronoitreemap.j2d.Point2D;
import kn.uni.voronoitreemap.j2d.PolygonSimple;
import kn.uni.voronoitreemap.j2d.Site;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.LinkedList;
import java.util.Random;

/*
    Code in this class to draw voronoi diagram sourced from GitHub:
    Arlind Nocaj, Ulrik Brandes, "Computing Voronoi Treemaps: Faster, Simpler, and Resolution-independent", Computer Graphics Forum, vol. 31, no. 3, June 2012, pp. 855-864
    Source code can be found at the following link: https://github.com/ArlindNocaj/power-voronoi-diagram

    Code from the above source adapted to fit the needs of this application, but contains all code specified in the README at the above link
 */

public class Fitness {

    private int circlesRadius = 5; // pixel radius of static circles
    static int circles; // so validity checking function does not get confused and throw out of bounds

    // constructor
    Fitness() {}

    // returns largest possible radius of circle using voronoi diagram
    int getBiggestCircle(boolean drawCircle, boolean drawVoronoi) {

        PowerDiagram diagram = new PowerDiagram();

        // normal list based on an array
        OpenList sites = new OpenList();

        Random rand = new Random();
        // create a root polygon which limits the voronoi diagram.
        // here it is just a rectangle.

        PolygonSimple rootPolygon = new PolygonSimple();
        int width = Main.screenWidth;
        int height = Main.screenHeight;
        rootPolygon.add(0, 0);
        rootPolygon.add(width, 0);
        rootPolygon.add(width, height);
        rootPolygon.add(0, height);

        circles = rand.nextInt(55) + 15;

        // create points (sites) and set random positions in the rectangle defined above
        for (int i = 0; i < circles; i++) {
            Site site = new Site(rand.nextInt(width), rand.nextInt(height));
            // we could also set a different weighting to some sites
            // site.setWeight(30)
            sites.add(site);
        }

        // set the list of points (sites), necessary for the power diagram
        diagram.setSites(sites);
        // set the clipping polygon, which limits the power voronoi diagram
        diagram.setClipPoly(rootPolygon);

        // do the computation
        diagram.computeDiagram();

        // for debuging vornonoi diagram implementation, colors polygons with random colors
        String[] colors = {"blue", "green", "yellow", "brown", "grey", "purple", "lime", "violet"};

        // for each site we can no get the resulting polygon of its cell
        // note that the cell can also be empty, in this case there is no polygon for the corresponding site
        // if drawVoronoi is true, fills cell with random color specified in colors array
        for (int i=0;i<sites.size;i++){
            Site site=sites.array[i];
            PolygonSimple polygon=site.getPolygon();
            if (polygon == null)
                continue;

            if (drawVoronoi) {
                Polygon p = new Polygon();
                for (int x = 0; x < polygon.getNumPoints(); x++) {
                    p.getPoints().addAll(polygon.getXPoints()[x], polygon.getYPoints()[x]);
                }

                p.relocate(polygon.getBounds().getX(), polygon.getBounds().getY());
                p.setFill(Paint.valueOf(colors[rand.nextInt(colors.length)]));
                Main.pane.getChildren().add(p);
            }
            Main.pane.getChildren().add(new Circle(polygon.getCentroid().getX(), polygon.getCentroid().getY(), circlesRadius, Paint.valueOf("black")));
        }

        ArrayList<Point2D> vertexes = new ArrayList<>(); // stores all vertexes that exist in the diagram
        ArrayList<Point2D> centroids = new ArrayList<>(); // stores all centroids that exist in the diagram

        // loops through all polygons to add vertexes and centroids to the corresponding lists
        for (int x = 0; x < sites.size; x++)
            if (sites.get(x).getPolygon() != null)
                for (int a = 0; a < sites.get(x).getPolygon().getNumPoints(); a++) {
                    vertexes.add(new Point2D(sites.get(x).getPolygon().getXPoints()[a], sites.get(x).getPolygon().getYPoints()[a]));
                    centroids.add(sites.get(x).getPolygon().getCentroid());
                }

        LinkedList<Integer> circleCoordsList = new LinkedList<>(); // list containing all possible centres for circles
        LinkedList<Double> radiiList = new LinkedList<>(); // list containing sizes of radii for corresponding circles in above list

        // searches for closest vertex to circle centre and determines the distance to the vertex, which is the radius of the largest circle that can be drawn
        for (int x = 0; x < vertexes.size(); x++) {
            double distance = Double.MAX_VALUE;
            Point2D v = vertexes.get(x);
            for (int a = 0; a < centroids.size(); a++) {
                if (calcEucledianDistance(v, centroids.get(a)) < distance)
                    distance = calcEucledianDistance(v, centroids.get(a));
            }

            radiiList.add(distance);
            circleCoordsList.add(x);
        }

        // copies lists into arrays for sorting using special implementation of quicksort
        Integer[] circleCoords = Arrays.copyOf(circleCoordsList.toArray(), circleCoordsList.size(), Integer[].class);
        Double[] radii = Arrays.copyOf(radiiList.toArray(), radiiList.size(), Double[].class);
        doubleArrQuickSort(0, radii.length - 1, radii, circleCoords);

        // find largest valid circle and set return radius for that circle
        for (int x = circleCoords.length - 1; x >= 0; x--) {
            Circle c = new Circle(vertexes.get(circleCoords[x]).getX(), vertexes.get(circleCoords[x]).getY(), radii[x] - circlesRadius, Paint.valueOf("green"));
            if (isEmpty(c)) {
                if (drawCircle)
                    Main.pane.getChildren().add(c);
                System.out.println("circle drawn");
                System.out.println("Biggest radius possible: " + ((int)c.getRadius() - circlesRadius));
                return (int)c.getRadius() - circlesRadius;
            }
        }

        return -1;
    }

    void doubleArrQuickSort(int lPivot, int rPivot, Double[] independent, Integer[] dependant) {

        /*
            Sorts two arrays using the quicksort algorithm, but only the independent one is truly sorted. When the value at
            positions a and b are swapped in the independent array, they are also swapped in the dependant, so that values that
            correspond to each other are maintained. If the independent array contained values 5 3 1 2 4 and the dependant 1 3 5 7 9,
            the sorted arrays would look like this: independent 1 2 3 4 5, dependant 5 7 3 9 1
         */

        int a = lPivot, b = rPivot;
        double  pivot = independent[(a + b) / 2];
        while (a <= b) {
            while (independent[a] < pivot)
                a++;
            while (independent[b] > pivot)
                b--;
            if (a <= b) {
                double temp = independent[a];
                independent[a] = independent[b];
                independent[b] = temp;
                int itemp = dependant[a];
                dependant[a] = dependant[b];
                dependant[b] = itemp;
                a++;
                b--;
            }
        }

        if (lPivot < b)
            doubleArrQuickSort(lPivot, b, independent, dependant);
        if (a < rPivot)
            doubleArrQuickSort(a, rPivot, independent, dependant);
    }

    // calculates eucledian distance between two points
    double calcEucledianDistance(Point2D p1, Point2D p2) {
        return Math.sqrt(Math.pow(p1.getX() - p2.getX(), 2) + Math.pow(p1.getY() - p2.getY(), 2));
    }

    // determines if a circle object is out of bounds of the window
    boolean outOfBounds(Circle c) {
        return !(c.getCenterX() < c.getRadius() || c.getCenterX() > Main.screenWidth - c.getRadius() ||
                c.getCenterY() < c.getRadius() || c.getCenterY() > Main.screenHeight - c.getRadius());
    }

    // determines if a circle is either out of bounds or colliding with other screen elements
    boolean isEmpty(Circle c) {

        if (!outOfBounds(c))
            return false;

        // last element will be genetic circle, do not check, nor the text element
        for (int x = 0; x < Main.pane.getChildren().size(); x++) {
            if (Main.pane.getChildren().get(x) instanceof Circle) {
                Circle test = (Circle) Main.pane.getChildren().get(x);
                if (c.contains(test.getCenterX(), test.getCenterY()))
                    return false;
            }
        }

        return true;
    }
}
