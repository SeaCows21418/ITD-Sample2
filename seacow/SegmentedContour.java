package seacow;

import org.opencv.core.Point;

import java.util.ArrayList;
import java.util.List;

public class SegmentedContour {
    public List<Segment> segments;  // List of contiguous Segments connecting contour points
    public Point minBound;          // min x,y of bounding rectangle of contour points
    public Point maxBound;          // max x,y of bounding rectangle of contour points

    /**
     *  Constructor to generate SegmentedContour.
     */
    public SegmentedContour() {
        this.segments = new ArrayList<>();
        this.minBound = null;
        this.maxBound = null;
    }
}
