package seacow;

import org.opencv.core.Mat;
import org.opencv.core.MatOfPoint;
import org.opencv.core.Point;
import org.opencv.core.Rect;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Locale;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import static java.lang.Math.*;


/**
 *  Represents a List of SegmentedContours.
 */
public class ContourList extends ArrayList<SegmentedContour> {
    
    public enum ContourFileState {
        FRAME,
        COUNT,
        CONTOUR,
        COORDINATE,
        NONE
    }
    public ContourFileState state = ContourFileState.FRAME;
    public ContourFileState nextState;
    
    public Point frameSize;
    public int contourNumber;
    public int contourCount;
    public int coordinateNumber;
    public int coordinateCount = 0;
    public List<Point> contourCoordinates;
    public List<List<Point>> coordinatesLL = new ArrayList<>();

    /**
     *  Constructor to build List of Contours as a List of List of Coordinates from JULIP text File.
     *  @param file     JULIP contour file
     */
    public ContourList(File file) {

        BufferedReader infile;     // Input file pointer
        String line;               // placeholder to hold Strings read from input file
        Matcher zz;                // placeholder for results of Regex matcher method

        // text format for both:
        //   frame size
        //   contour size
        // dddxddd        
        String contentString =
                "(\\d+)x(\\d+)";
        Pattern contentPattern = Pattern.compile(contentString);

        // A JULIP contour file is as follows:
        // dddxddd    - frame size
        // ddd        - number of contours
        // dddxddd    - for each contour, contour size comes first
        // ddd  ddd   - x y coordinates of contour point - a series of contour points will be sequentially listed
        String coordinateString =
                "(\\d+)\\s+(\\d+)";
        Pattern coordinatePattern = Pattern.compile(coordinateString);
        
        try {
            infile = new BufferedReader(new FileReader(file));
            while (true) {
                line = infile.readLine();
                if (line == null) {break; }
                System.out.println(String.format("%s %s %d", line, state, coordinateCount));

                nextState = state;
                switch(state) {
                    case FRAME:
                        zz = contentPattern.matcher(line);
                        System.out.println(zz.groupCount());
                        if (zz.find()) {
                            frameSize = new Point((double) Integer.parseInt(zz.group(1)), (double) Integer.parseInt(zz.group(2)));
                            nextState = ContourFileState.COUNT;
                        }
                        break;
                    case COUNT:
                        contourNumber = Integer.parseInt(line);
                        contourCount = 0;
                        nextState = ContourFileState.CONTOUR;
                        break;
                    case CONTOUR:
                        zz = contentPattern.matcher(line);
                        if (zz.find()) {
                            coordinateNumber = Integer.parseInt(zz.group(2));
                            System.out.println(String.format("Looking for new contour %d with %d coords", contourCount, coordinateNumber));
                            coordinateCount = 0;
                            contourCount += 1;
                            nextState = ContourFileState.COORDINATE;
                            contourCoordinates = new ArrayList<>();
                        }
                        break;
                    case COORDINATE:
                        zz = coordinatePattern.matcher(line);
                        if (zz.find()) {
                            double xc = (double)Integer.parseInt(zz.group(1));
                            double yc = (double)Integer.parseInt(zz.group(2));
                            contourCoordinates.add(new Point(xc,yc));
                            coordinateCount += 1;
                            if (coordinateCount == coordinateNumber) {
                                // insert last coordinate into front of list
                                contourCoordinates.add(0, new Point(xc,yc));
                                System.out.println(String.format("# coords: %d", contourCoordinates.size()));
                                // append this set of contour coordinates into uber list
                                coordinatesLL.add(contourCoordinates);

                                if (contourCount < contourNumber) {
                                    nextState = ContourFileState.CONTOUR;
                                } else {
                                    nextState = ContourFileState.NONE;
                                }
                            }
                        }
                        break;
                        
                    default:
                        break;
                } // switch
                state = nextState;
            } // while
        } catch (IOException e) {
            e.printStackTrace();
        } // try-catch

        System.out.println(String.format("Size of coordinatesLL = %d", coordinatesLL.size()));

        // Build list of segmented contours from coordinate structure
        for (List<Point> coordL: coordinatesLL) {
            if (coordL.size() < 4) {
                continue;
            }
            SegmentedContour sc = new SegmentedContour();
            for (int i = 0; i < coordL.size()-1; i++) {
                Point ci = coordL.get(i+0);
                Point cj = coordL.get(i+1);
                sc.segments.add(new Segment(ci.x, ci.y, cj.x, cj.y));
                if (sc.minBound == null) { sc.minBound = new Point(ci.x, ci.y); }
                if (sc.maxBound == null) { sc.maxBound = new Point(ci.x, ci.y); }
                sc.minBound.x = Math.min(sc.minBound.x, Math.min(ci.x, cj.x));
                sc.minBound.y = Math.min(sc.minBound.y, Math.min(ci.y, cj.y));
                sc.maxBound.x = Math.max(sc.maxBound.x, Math.max(ci.x, cj.x));
                sc.maxBound.y = Math.max(sc.maxBound.y, Math.max(ci.y, cj.y));
            }
            this.add(sc);
        }
        System.out.println(String.format("Size of contoursList = %d", this.size()));

    }

    /**
     *  Constructor to build List of Contours as a List of List of Coordinates from a MatOfPoint.
     *  @param mat     MatOfPoint of contours
     */
    public ContourList(List<MatOfPoint> mat) {
        // iterate over all contours
        for (int c = 0; c < mat.size(); c++) {
            Mat contour = mat.get(c);
            contourCoordinates = new ArrayList<>();
            Point pt = null;
            for (int row = 0; row < contour.rows(); row++) {
                double[] coor = contour.get(row, 0);
                pt = new Point(coor[0],coor[1]);
                // write each point in the contour
                contourCoordinates.add(pt);
            }
            // insert last coordinate into front of list
            contourCoordinates.add(0, pt);
            coordinatesLL.add(contourCoordinates);
        }

//        System.out.println(String.format("Size of coordinatesLL = %d", coordinatesLL.size()));

        // Build list of segmented contours from coordinate structure
        for (List<Point> coordL: coordinatesLL) {
            if (coordL.size() < 4) {
                continue;
            }
            SegmentedContour sc = new SegmentedContour();
            for (int i = 0; i < coordL.size()-1; i++) {
                Point ci = coordL.get(i+0);
                Point cj = coordL.get(i+1);
                sc.segments.add(new Segment(ci.x, ci.y, cj.x, cj.y));
                if (sc.minBound == null) { sc.minBound = new Point(ci.x, ci.y); }
                if (sc.maxBound == null) { sc.maxBound = new Point(ci.x, ci.y); }
                sc.minBound.x = Math.min(sc.minBound.x, Math.min(ci.x, cj.x));
                sc.minBound.y = Math.min(sc.minBound.y, Math.min(ci.y, cj.y));
                sc.maxBound.x = Math.max(sc.maxBound.x, Math.max(ci.x, cj.x));
                sc.maxBound.y = Math.max(sc.maxBound.y, Math.max(ci.y, cj.y));
            }
            this.add(sc);
        }
//        System.out.println(String.format("Size of contoursList = %d", this.size()));

    }

    /**
     * Determine if a given point is contained within a Contour
     * @param pt - (x,y)
     * @return true if bounded by the interior of a Contour
     */
    public boolean isInsideContour(Point pt) {
        int slice;
        boolean logResults = false;

        for (SegmentedContour sc : this) {
            // if pt is outside bounds of contour then it's not in the contour
            if (pt.x < sc.minBound.x || pt.x > sc.maxBound.x ||
                pt.y < sc.minBound.y || pt.y > sc.maxBound.y) {
                if (logResults) {
//                    System.out.println(String.format("    (%.2f,%.2f) (%.2f,%.2f) outside bounds",
//                            sc.minBound.x, sc.minBound.y, sc.maxBound.x, sc.maxBound.y));
                }
                continue;
            }
            // count how many segments a horizontal extension of the point to x=0 would slice
            slice = 0;
            System.out.println(sc.segments.size());
            for (int s = 0; s < sc.segments.size(); s++) {
                Segment seg = sc.segments.get(s);
                int equaledY = 0;
                // Check if the segment was already considered from analyzing prior segment
                if (equaledY > 0) {
                    equaledY -= 1;
                    continue;
                }
                // if pt is outside the y-bounds of the segment, it doesn't slice it
                if (pt.y < Math.min(seg.y1, seg.y2) ||
                        pt.y > Math.max(seg.y1, seg.y2)) {
                    if (logResults) {
//                        System.out.println(String.format("    (%.2f,%.2f) (%.2f,%.2f) outside y", seg.x1, seg.y1, seg.x2, seg.y2));
                    }
                    continue;
                }
                // if pt is less then x-bounds of segemnt, it doesn't slice it
                if (pt.x < Math.min(seg.x1, seg.x2)) {
                    if (logResults) {
//                        System.out.println(String.format("    (%.2f,%.2f) (%.2f,%.2f) >min x", seg.x1, seg.y1, seg.x2, seg.y2));
                    }
                    continue;
                }
                // if segment is horiontally parallel to the point, it doesn't slice it
                if ((pt.y == seg.y2) && (pt.y == seg.y1)) {
                    if (logResults) {
//                        System.out.println(String.format("    (%.2f,%.2f) (%.2f,%.2f) parallel", seg.x1, seg.y1, seg.x2, seg.y2));
                    }
                    continue;
                }
                // if pt equals the far endpoint of the segment, then we have to check
                // the sequential segment(s) to determine if it slices the contour or just hits a vertex
                if (pt.y == seg.y2)  {
                    // check for any run of segments that may happen to be horizontal at the vertex
                    Segment thisSeg = seg;
                    int si = s;
                    while (pt.y == thisSeg.y2) {
                        equaledY += 1;
                        si += 1;
                        if (si >= sc.segments.size()) {
                            si = 0;
                        }
                        thisSeg = sc.segments.get(si);
                    }
                    // check if merely vertex or not
                    if (((pt.y > seg.y1) && (pt.y < thisSeg.y2)) ||
                        ((pt.y < seg.y1) && (pt.y > thisSeg.y2))) {
                        if (logResults) {
//                            System.out.println(String.format("(%.2f,%.2f) (%.2f,%.2f) %d =y2", seg.x1, seg.y1, seg.x2, seg.y2, slice));
                        }
                        slice += 1;
                    } else {
                        if (logResults) {
//                            System.out.println(String.format("    (%.2f,%.2f) (%.2f,%.2f) %d vertex", seg.x1, seg.y1, seg.x2, seg.y2, slice));
                        }
                    }
                    continue;
                }
                // if pt y equals the near endpoint of the segement, it doesn't slice it
                if (pt.y == seg.y1) {
                    if (logResults) {
//                        System.out.println(String.format("    (%.2f,%.2f) (%.2f,%.2f) %d =y1", seg.x1, seg.y1, seg.x2, seg.y2, slice));
                    }
                    continue;
                }
                // if pt is greater than x-bounds of segment, it does slice it
                if (pt.x > Math.max(seg.x1, seg.x2)) {
                    if (logResults) {
 //                       System.out.println(String.format("(%.2f,%.2f) (%.2f,%.2f) %d >x", seg.x1, seg.y1, seg.x2, seg.y2, slice));
                    }
                    slice += 1;
                }
                // if pt is within x,y-bounds of the segment, compare it to x-intercept of segment
                else {
                    double xSeg;
                    // find x-intercept of segment, given y=pt.y
                    if (seg.y2 == seg.y1) {
                        xSeg = 0;
                    } else {
                        xSeg = ((seg.x2 - seg.x1) * (pt.y - seg.y1) / (seg.y2 - seg.y1)) + seg.x1;
                    }
                    // cast ray from pt to (0,pt.y)
                    // if pt.x > x-intercept of segment, then it slices it
                    if (pt.x >= xSeg) {
                        slice += 1;
                        if (logResults) {
//                            System.out.println(String.format("(%.2f,%.2f) (%.2f,%.2f) [%.2f,%.2f] %d int", seg.x1, seg.y1, seg.x2, seg.y2, xSeg, pt.y, slice));
                        }
                    } else {
                        if (logResults) {
//                            System.out.println(String.format("    (%.2f,%.2f) (%.2f,%.2f) [%.2f,%.2f] miss", seg.x1, seg.y1, seg.x2, seg.y2, xSeg, pt.y));
                        }
                    }
                }
            }
            // if an odd number of segments are sliced then it is inside a contour
            if (slice % 2 == 1) {
                return true;
            }
        }
        return false;
    }
}