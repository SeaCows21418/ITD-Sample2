package seacow;

import org.opencv.core.CvType;
import org.opencv.core.Mat;
import org.opencv.core.MatOfFloat4;
import org.opencv.core.Point;

import java.util.ArrayList;
import java.util.List;

/**
 *  Represents a List of Targets.
 */
public class TargetList extends ArrayList<Target> {

    /**
     * Utility method to draw lines representing Targets from a TargetList onto a Mat.
     * @return Mat with drawn lines
     */
    public MatOfFloat4 centroidsFromTargetList() {
        Mat mat = new Mat(this.size(), 1, CvType.CV_32FC4);
        int row = 0;
        Point pt;
        double angle;
        double theta;
        double radius;
        Point xy;
        for (Target t : this) {

            theta = Math.toRadians(t.thetaDegrees);
            float[] line = {(float)(t.centroid.x + 10.0*Math.cos(theta)),
                    (float)(t.centroid.y - 10.0*Math.sin(theta)),
                    (float)(t.centroid.x - 10.0*Math.cos(theta)),
                    (float)(t.centroid.y + 10.0*Math.sin(theta))};
            mat.put(row++, 0, line);

            System.out.println(String.format("target: th:%3d centroid(%3d,%3d)",
                    t.thetaDegrees, t.centroid.x, t.centroid.y));
        }
        return new MatOfFloat4(mat);
    }


    /**
     * Utility method to check if a given Target is uniquely defined within the list
     * @param target
     * @return true if unique
     */
    public boolean isUniqueTarget(Target target) {
        boolean isUnique = true;
        for (Target t : this) {
            isUnique = isUnique & !Target.isSameTarget(t, target);
        }
        return isUnique;
    }

    /**
     * Constructor
     */
    public TargetList() {}

}
