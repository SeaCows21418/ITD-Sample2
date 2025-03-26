package seacow;

import org.opencv.core.CvType;
import org.opencv.core.Mat;
import org.opencv.core.MatOfFloat4;
import org.opencv.core.MatOfPoint;
import org.opencv.core.Point;
import org.opencv.core.Rect;

import java.util.List;

public class SampleDetector {

    public Rect regionOfInterest;
    public Point focalPoint;
    public SegmentList segL;
    public ContourList contourL;
    public TargetList targetL;

    public List<Integer> idxLongSegL;


    public void doSampleDetection() {
        this.segL.buildCompositeSegments();
        // Generate a list of Long Segments
        this.idxLongSegL = SegmentList.getLongIndices(this.segL,this.focalPoint);
        System.out.println("found long segments");
        this.targetL = this.segL.findTargets(this.idxLongSegL, this.contourL);
        System.out.println("found targets");
/*        
        // Generate a list of Vertices (filtered intersections of two line segments)
        this.rawVertexL = new VertexList(this.segL, this.focalPoint);
        // Filter Vertices against Contours
        System.out.println("CONTOUR CHECK...");
        this.filtVertexL = new VertexList(this.rawVertexL, this.contourL);
        System.out.println(String.format("Number of contour-filtered vertices: %d", this.filtVertexL.size()));
        // Filter out near-duplicate Vertices
        System.out.println("DUPLICATION CHECK...");
        this.dupVertexL = new VertexList(this.filtVertexL, true);
        System.out.println(String.format("Number on non-duplicate vertices: %d", this.dupVertexL.size()));
        // Filter out Vertices where long side is not valid
        System.out.println("VALID CHECK...");
        this.vertexL = new VertexList(this.dupVertexL);
        System.out.println(String.format("Number of double-legit vertices: %d", this.vertexL.size()));
        
    
        //Create a list of Targets
        System.out.println("TARGET GEN...");
        this.targetL = new TargetList(this.vertexL);
        System.out.println(String.format("Number of targets: %d", this.targetL.size()));
        */
    }

    /**
     * Utility method to use the lineSegmentDetector.drawSegment by constructing a Mat of lines
     * @param iL - List of indices to sL
     * @param sL - SegmentList
     * @return   - MatOfFloat4 to overlay onto an image
     */
    public MatOfFloat4 segmentsFromSegmentList(List<Integer> iL, SegmentList sL) {
        Mat mat = new Mat(iL.size()*1, 1, CvType.CV_32FC4);
        int row = 0;
        for (Integer i : iL) {
            Segment s = sL.get(i);
            float[] pts = {(float)s.x1, (float)s.y1, (float)s.x2, (float)s.y2};
            mat.put(row++, 0, pts);
        }
        return new MatOfFloat4(mat);
    }

    /**
     * Utility method to use the lineSegmentDetector.drawSegment by constructing a Mat of lines
     * @param sL - SegmentList
     * @return   - MatOfFloat4 to overlay onto an image
     */
    public MatOfFloat4 segmentsFromSegmentList(SegmentList sL) {
        Mat mat = new Mat(sL.size()*1, 1, CvType.CV_32FC4);
        int row = 0;
        for (Segment s : sL) {
            float[] pts = {(float)s.x1, (float)s.y1, (float)s.x2, (float)s.y2};
            mat.put(row++, 0, pts);
        }
        return new MatOfFloat4(mat);
    }

    public MatOfFloat4 drawBestTarget(Target bestT) {
        Mat mat = new Mat(targetL.size(), 1, CvType.CV_32FC4);
        int row = 0;
        Point pt;
        double angle;
        double theta;
        double radius;
        Point xy;
        
            theta = Math.toRadians(bestT.thetaDegrees);
            float[] line = {(float)(bestT.centroid.x + 10.0*Math.cos(theta)),
                    (float)(bestT.centroid.y - 10.0*Math.sin(theta)),
                    (float)(bestT.centroid.x - 10.0*Math.cos(theta)),
                    (float)(bestT.centroid.y + 10.0*Math.sin(theta))};
            mat.put(row++, 0, line);

            System.out.println(String.format("target: th:%3d centroid(%3d,%3d)",
                    bestT.thetaDegrees, bestT.centroid.x, bestT.centroid.y));
        
        return new MatOfFloat4(mat);
    }

    /**
     *  Constructor
     *  @param lineMat   Mat of line segments
     *  @param regionOfInterest  border definition
     */
    public SampleDetector(Mat lineMat, List<MatOfPoint> polygonContours, Rect regionOfInterest, Point focalPoint) {
//        System.out.println("create SegmentList");
        this.segL = new SegmentList(lineMat);
        this.contourL = new ContourList(polygonContours);
        this.regionOfInterest = regionOfInterest;
        this.focalPoint = focalPoint;
//        System.out.println("doSampleDetection");
        doSampleDetection();
    }

}