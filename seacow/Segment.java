package seacow;


import org.opencv.core.Point;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

import static java.lang.Math.*;

/**
 *  Represents a line segment and its associated parametrics.
 */
public class Segment {
    public double x1;             // x-coordinate of endpoint: Point 1
    public double y1;             // y-coordinate of endpoint: Point 1
    public double x2;             // x-coordinate of endpoint: Point 2
    public double y2;             // y-coordinate of endpoint: Point 2
    public double midx; 
    public double midy;
    public boolean discarded;     // =True for voided Segment; else False
    public double theta;          // radian angle of Segment
    public double d;              // length of Segment
    public List<Integer> idxL;    // if a compositeSegment then indices to a list of segments

    /**
     *  Constructor to generate Segment.
     *  @param x1  x-coordinate of a given endpoint
     *  @param y1  y-coordinate of a given endpoint
     *  @param x2  x-coordinate of second endpoint
     *  @param y2  y-coordinate of second endpoint
     */    
    public Segment(double x1, double y1, double x2, double y2) {
        this.x1 = x1;
        this.y1 = y1;
        this.x2 = x2; 
        this.y2 = y2;
        this.midx = (x1+x2)/2.0;
        this.midy = (y1+y2)/2.0;
        this.discarded = false;
        this.idxL = new ArrayList<Integer>();

 //       System.out.println(String.format("Segment (%d,%d) (%d,%d)", x1, y1, x2, y2));
        if (x1 == x2) {
            if (y2 > y1) {
                this.theta = Math.PI / 2.0;
            } else {
                this.theta = Math.PI / -2.0;
            }
        }
        else {
            // Because y-coordinate in image are +y in normally downward position,
            // Y is flipped
            this.theta = Math.atan2(y1-y2 , x2-x1);
        }
//        System.out.println(String.format("theta:%.2f", Math.toDegrees(this.theta)));
        this.d = sqrtDistance(x1,y1,x2,y2);
    }

    /**
     * Constructor, given a Segment
     */
    public Segment(Segment seg) {
        this(seg.x1, seg.y1, seg.x2, seg.y2);
    }

    /**
     * write to String
     * @return String
     */
    public String toString() {
        return String.format("(%8.3f,%8.3f)-(%8.3f,%8.3f) d:%8.3f a:%8.3f", x1, y1, x2, y2, d, theta);
    }

    /**
     * Retrieve an endpoint of the Segment
     * @param n - either 1 or 2, representing each endpoint
     * @return  - Point (x,y) of the endpoint
     */
    public Point getCoordinate(int n) {
        if (n == 1) {
            return new Point(x1, y1);
        } else if (n == 2) {
            return new Point(x2, y2);
        }
        return null;
    }
    
    /**
     *  Calculates distance between two (x,y) points: 1,2.
     *  @param x1  x-coodinate, point 1
     *  @param y1  y-coodinate, point 1
     *  @param x2  x-coodinate, point 2
     *  @param y2  y-coodinate, point 2
     *  @return    distance between Point 1, Point 2 
     */    
    public static double sqrtDistance (double x1, double y1, double x2, double y2) {
        return (sqrt((x2-x1)*(x2-x1) + (y2-y1)*(y2-y1)));
    }
    
    /**
     *  Calculates (squared) distance between two (x,y) points: 1,2.
     *  Note: can use this for less computationl length algorithm.
     *  @param x1  x-coodinate, point 1
     *  @param y1  y-coodinate, point 1
     *  @param x2  x-coodinate, point 2
     *  @param y2  y-coodinate, point 2
     *  @return    distance^2 between Point 1, Point 2 
     */    
    public static double sqDistance (double x1, double y1, double x2, double y2) {
        return ((x2-x1)*(x2-x1) + (y2-y1)*(y2-y1));
    }
    
    public Segment reverse() {
        return new Segment(this.x2, this.y2, this.x1, this.y1);
    }
    

    /**
     *  Tag all Segments within proximity of border coordinates
     *  for discard. Note origin is upper-left.
     *  @param bx1  left  (lesser x) x-coordinate of border
     *  @param by1  upper (lesser y) y-coordinate of border
     *  @param bx2  right (higher x) x-coordinate of border
     *  @param by2  lower (higher y) y-coordinate of border
     *  @param d   proximity to border for discarding Segment
     *  @return    Number of discarded Segments
     */     
    public boolean borderCheck (int bx1, int by1, int bx2, int by2, int d) {
        this.discarded = (
                (this.x1 <= bx1 + d) ||
                (this.x2 <= bx1 + d) ||
                (this.x1 >= bx2 - d) ||
                (this.x2 >= bx2 - d) ||
                (this.y1 <= by1 + d) ||
                (this.y2 <= by1 + d) ||
                (this.y1 >= by2 - d) ||
                (this.y2 >= by2 - d));
        return this.discarded;
    }

}
