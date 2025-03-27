package seacow;

import org.opencv.core.CvType;
import org.opencv.core.Mat;
import org.opencv.core.MatOfFloat4;
import org.opencv.core.MatOfPoint;
import org.opencv.core.Point;
import org.opencv.core.Rect;

import java.util.ArrayList;
import java.util.List;

public class SampleDetector {

    public Rect regionOfInterest;
    public Point focalPoint;
    public SegmentList segL;
    public ContourList contourL;
    public TargetList targetL;

    public List<Integer> idxLongSegL;

    public TargetList transformCentroidTargetL;

    public void doSampleDetection() {
        this.segL.buildCompositeSegments();
        // Generate a list of Long Segments
        this.idxLongSegL = SegmentList.getLongIndices(this.segL,this.focalPoint);
        System.out.println("found long segments");
        this.targetL = this.segL.findTargets(this.idxLongSegL, this.contourL);
        System.out.println("found targets");
        this.transformCentroidTargetL = transform(targetL);
        System.out.println("transformed centroids");

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


    public TargetList transform(TargetList targetL) {
        TargetList centroidL = new TargetList();

        List<LinearXfrm> chLatL = new ArrayList<LinearXfrm>();
        chLatL.add(new LinearXfrm(-6.0, 0.471, -178.0));
        chLatL.add(new LinearXfrm(-4.0, 0.404, -84.6));
        chLatL.add(new LinearXfrm(-2.0, 0.328, 22.2));
        chLatL.add(new LinearXfrm( 0.0, 0.236, 143.0));
        chLatL.add(new LinearXfrm( 2.0, 0.153, 268.0));
        chLatL.add(new LinearXfrm( 4.0, 0.0597, 403.0));
        List<LinearXfrm> chAxiL = new ArrayList<LinearXfrm>();
        chAxiL.add(new LinearXfrm(1.0, -0.032, 63.3));
        chAxiL.add(new LinearXfrm(2.0, 0.000497, 118.0));
        chAxiL.add(new LinearXfrm(3.0, 0.0286, 169.0));
        chAxiL.add(new LinearXfrm(4.0, 0.0523, 217.0));
        chAxiL.add(new LinearXfrm(5.0, 0.0702, 260.0));
        chAxiL.add(new LinearXfrm(6.0, 0.0984, 296.0));
        chAxiL.add(new LinearXfrm(7.0, 0.117, 331.0));
        chAxiL.add(new LinearXfrm(8.0, 0.136, 362.0));
        chAxiL.add(new LinearXfrm(9.0, 0.145, 394.0));
        chAxiL.add(new LinearXfrm(10.0, 0.169, 419.0));
        chAxiL.add(new LinearXfrm(11.0, 0.18, 445.0));
        chAxiL.add(new LinearXfrm(12.0, 0.194, 469.0));
        chAxiL.add(new LinearXfrm(13.0, 0.211, 489.0));
        chAxiL.add(new LinearXfrm(14.0, 0.221, 511.0));
        chAxiL.add(new LinearXfrm(15.0, 0.228, 531.0));
        chAxiL.add(new LinearXfrm(16.0, 0.246, 547.0));
        chAxiL.add(new LinearXfrm(17.0, 0.25, 565.0));

        List<LinearXfrm> ehLatL = new ArrayList<LinearXfrm>();
        ehLatL.add(new LinearXfrm(-6.0, -0.488, 653.0));
        ehLatL.add(new LinearXfrm(-4.0, -0.416, 555.0));
        ehLatL.add(new LinearXfrm(-2.0, -0.361, 466.0));
        ehLatL.add(new LinearXfrm( 0.0, -0.286, 358.0));
        ehLatL.add(new LinearXfrm( 2.0, -0.18,  222.0));
        ehLatL.add(new LinearXfrm( 4.0, -0.0708, 73.2));
        List<LinearXfrm> ehAxiL = new ArrayList<LinearXfrm>();
        ehAxiL.add(new LinearXfrm(1.0, 0.0632, 22.8));
        ehAxiL.add(new LinearXfrm(2.0, 0.0452, 90.7));
        ehAxiL.add(new LinearXfrm(3.0, 0.00764, 158.0));
        ehAxiL.add(new LinearXfrm(4.0, -0.0267, 219.0));
        ehAxiL.add(new LinearXfrm(5.0, -0.0541, 274.0));
        ehAxiL.add(new LinearXfrm(6.0, -0.0742, 321.0));
        ehAxiL.add(new LinearXfrm(7.0, -0.0924, 364.0));
        ehAxiL.add(new LinearXfrm(8.0, -0.112, 405.0));
        ehAxiL.add(new LinearXfrm(9.0, -0.13, 442.0));
        ehAxiL.add(new LinearXfrm(10.0, -0.142, 477.0));
        ehAxiL.add(new LinearXfrm(11.0, -0.155, 507.0));
        ehAxiL.add(new LinearXfrm(12.0, -0.172, 539.0));
        ehAxiL.add(new LinearXfrm(13.0, -0.183, 566.0));
        ehAxiL.add(new LinearXfrm(14.0, -0.195, 592.0));
        ehAxiL.add(new LinearXfrm(15.0, -0.207, 617.0));
        ehAxiL.add(new LinearXfrm(16.0, -0.219, 639.0));
        ehAxiL.add(new LinearXfrm(17.0, -0.229, 661.0));
        ehAxiL.add(new LinearXfrm(18.0, -0.242, 683.0));

        double eqnValue;

        Double interpLat = null;
        Double interpAxi = null;
        double interpAngle;

        Double loValue = null;
        Double hiValue = null;
        Double loEqn   = null;
        Double hiEqn   = null;
        double loAngle = 0.0;
        double hiAngle = 0.0;
        double compensationAngle = 0.0;

        for (Target target : targetL) {

            int imX = target.centroid.x;
            int imY = target.centroid.y;

            loValue = null;
            hiValue = null;
            loEqn   = null;
            hiEqn   = null;

            for (LinearXfrm lx : chLatL) {
                // get the expected Y value, for given linear equation
                eqnValue = lx.eqn.function(imX);
                // find closest above and below values to our Y value
                if (eqnValue < imY) {
                    if ((loEqn == null) || (eqnValue > loEqn)) {
                        loValue = lx.value;
                        loEqn = eqnValue;
                    }
                } else {
                    if ((hiEqn == null) || (eqnValue < hiEqn)) {
                        hiValue = lx.value;
                        hiEqn = eqnValue;
                    }
                }
            }

            // We need both below and above values to do an interpolation
            if ((loValue != null) && (hiValue != null)) {
                interpLat = (imY-loEqn)/(hiEqn-loEqn)*(hiValue-loValue)+loValue;
            }

            loValue = null;
            hiValue = null;
            loEqn   = null;
            hiEqn   = null;

            for (LinearXfrm lx : chAxiL) {
                // get the expected X value, for given linear equation
                eqnValue = lx.eqn.function(imY);
                // find closest above and below values to our Y value
                if (eqnValue < imX) {
                    if ((loEqn == null) || (eqnValue > loEqn)) {
                        loValue = lx.value;
                        loAngle = lx.eqn.m;
                        loEqn = eqnValue;
                    }
                } else {
                    if ((hiEqn == null) || (eqnValue < hiEqn)) {
                        hiValue = lx.value;
                        hiAngle = lx.eqn.m;
                        hiEqn = eqnValue;
                    }
                }
            }

            // We need both below and above values to do an interpolation
            if ((loValue != null) && (hiValue != null)) {
                double ratio = (imX-loEqn)/(hiEqn-loEqn);
                interpAxi = ratio*(hiValue-loValue)+loValue;
                compensationAngle = ratio*(hiAngle-loAngle)+loAngle;
            }

            if ((interpLat == null) || (interpAxi == null)) {
                System.out.println("No transform avaiable");
            } else {
                // Angle of Sample to Axial
                interpAngle = Math.toRadians(target.thetaDegrees) + compensationAngle;
                // Rotation Angle of Bot to Sample
                double rotationAngle = Math.atan(interpLat / (interpAxi+8.8));
                // Angle of Sample to Rotated Bot
                double grabAngle = Math.abs(interpAngle - rotationAngle);
                if (grabAngle > Math.PI/2.0) {grabAngle = Math.PI - grabAngle;}

                Target xfrm = new Target(new Point(25.4*interpAxi, 25.4*interpLat), (int) Math.toDegrees(interpAngle));
                System.out.println(String.format("Axi: %8.4f Lat: %8.4f Angle: %d", interpAxi, interpLat, (int) Math.toDegrees(interpAngle)));
                System.out.println(String.format("  rotate bot %d, grab angle %d", (int) Math.toDegrees(rotationAngle), (int) Math.toDegrees(grabAngle)));
                centroidL.add(xfrm);
            }
        }    
        return centroidL;
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