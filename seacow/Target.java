package seacow;

import org.opencv.core.Point;

/**
 *  Represents the targeting (x,y,theta) required for Sample Scoring Element.
 */
public class Target {
    public Coordinate centroid;     // x,y position of centroid
    public int thetaDegrees;        // (degree) angle of Target


    /**
     * Check if two Targets are identical
     * @param a - Target
     * @param b - Target
     * @return true if the same
     */
    public static boolean isSameTarget(Target a, Target b) {
        if ((a == null) || (b == null)) { return false; }
        return  (a.centroid.x == b.centroid.x) &&
                (a.centroid.y == b.centroid.y) &&
                (a.thetaDegrees == b.thetaDegrees);
    }

    /**
     *  Constructor to generate Target.
     *  @param centroid - Point with fields: x,y
     *  @param thetaD   - theta (in degrees)
     */
    public Target(Point centroid, int thetaD) {
        this.centroid = new Coordinate((int)centroid.x, (int)centroid.y);
        this.thetaDegrees = thetaD;
    }

}
