package seacow;

import org.opencv.core.Point;

/**
 *  Coordinates, in integer (vs. opencv Point which is double).
 */
public class Coordinate {
    public int x;             // x-coordinate
    public int y;             // y-coordinate

    /**
     * Utility method to find distance between two Coordinates
     * @param a - 1st given Coordinate
     * @param b - 2nd given Coordinate
     */
    public static double distance(Coordinate a, Coordinate b) {
        double x1 = (double)a.x;
        double y1 = (double)a.y;
        double x2 = (double)b.x;
        double y2 = (double)b.y; 
        return Math.sqrt((x1-x2)*(x1-x2)+(y1-y2)*(y1-y2));
    }

    /**
     * Utility method to find distance between two Points
     * @param a - 1st given Point
     * @param b - 2nd given Point
     */
    public static double distance(Point a, Point b) {
        return Math.sqrt((a.x-b.x)*(a.x-b.x)+(a.y-b.y)*(a.y-b.y));
    }

    /**
     *  Constructor to generate a Coordinate.
     *  @param x  x-coordinate
     *  @param y  y-coordinate
     */
    public Coordinate(int x, int y) {
        this.x = x;
        this.y = y;
    }

    /**
     * Constructor from another Coordinate
     */
    public Coordinate(Coordinate pt) {
        this.x = pt.x;
        this.y = pt.y;
    }
    
}
