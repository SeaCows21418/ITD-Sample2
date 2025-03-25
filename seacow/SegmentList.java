package seacow;

import org.opencv.core.Mat;
import org.opencv.core.Point;
import org.opencv.core.Rect;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Locale;

import static java.lang.Math.*;

/**
 *  Represents a List of Segments.
 *  Segments being Objects with properties representing line segments.
 */
public class SegmentList extends ArrayList<Segment>{

    /**
     * We need to find where the LineDetect function generated what looks like a Segment to the human eye
     * as multiple Segments. We want to construct a single segment from those shorter segments.
     */
    public void buildCompositeSegments() {
        int i;
        int j;
        int size = this.size();
        boolean compositesMade = false;
        Segment seg;
        // Find all permutations of Segments that are parallely and uniquely proximal.
        // Create composite Segments that are the non-proximal endpoints of the two original Segments.
        // These composite Segments will index to its original Segments.
        // This is fine for joining 2 segments together.
        // *-----*  B
        //    A  *----*
        //  to
        // *----------*  [A,B]
        for (i = 0; i < size-1; i++) {
            for (j = i+1; j < size; j++) {
                Integer prox = areUniquelyProximate(i, j, 4.0);
                boolean para = areParallely(i,j,0.15); // ||within ~9 deg
                if ((prox != null) && para) {
                    Segment a = this.get(i);
                    Segment b = this.get(j);
                    switch (prox) {
                        case 0:seg = new Segment(a.x2, a.y2, b.x2, b.y2); break;  // a1,b1 proc
                        case 1:seg = new Segment(a.x2, a.y2, b.x1, b.y1); break;  // a1,b2 proc
                        case 2:seg = new Segment(a.x1, a.y1, b.x2, b.y2); break;  // a2,b1 proc
                        default:seg = new Segment(a.x1, a.y1, b.x1, b.y1); break;  // a2,b2 proc
                    }
                    seg.idxL.add(i);
                    seg.idxL.add(j);
                    System.out.println(String.format("%4d composite Segment %d %d %s", this.size(), i, j, seg.toString()));
                    this.add(seg);
                    compositesMade = true;
                }    
            }
        }
        // Now we need to consider how more than 2 original segments may combine to construct a longer segments.
        //       *--* B  
        // *--------* [B,C]
        // [A,B] *--------* 
        // to
        // *--------------* [A,B,C]
        while (compositesMade) {
            compositesMade = false;
            int cSize = this.size();
            List<Integer> commonL = new ArrayList<Integer>(); 
            List<Integer> discardL = new ArrayList<Integer>();
            for (i = size; i < cSize-1; i++) {
                for (j = i+1; j < cSize; j++) {
                    commonL.clear();
                    // find all common indices between two composite segments
                    Segment a = this.get(i);
                    Segment b = this.get(j);
                    if (!a.discarded && !b.discarded) {
                        for (int k : a.idxL) {
                            if (b.idxL.contains(k)) {
                                commonL.add(k);
                            }
                        }
                        // check if composites share all indices but one each
                        if (!commonL.isEmpty() && 
                            (a.idxL.size() == b.idxL.size()) && 
                            (a.idxL.size() == commonL.size()+1)) {
                            // tag all the segments comprising the new composite segment for discarding
//                            for (int k : a.idxL) {
//                                discardL.add(k);
//                            }
//                            for (int k : b.idxL) {
//                                discardL.add(k);
//                            }
                            discardL.add(i);
                            discardL.add(j);
                            // find farthest endpoints for new Segment
                            double d1 = Segment.sqrtDistance(a.x1, a.y1, b.x1, b.y1);
                            double d2 = Segment.sqrtDistance(a.x1, a.y1, b.x2, b.y2);
                            double d3 = Segment.sqrtDistance(a.x2, a.y2, b.x1, b.y1);
                            double d4 = Segment.sqrtDistance(a.x2, a.y2, b.x2, b.y2);
                            int farPair = 1;
                            double farthest = d1;
                            if (d2 > farthest) { 
                                farPair = 2;
                                farthest = d2;
                            }
                            if (d3 > farthest) {
                                farPair = 3;
                                farthest = d3;
                            }
                            if (d4 > farthest) {
                                farPair = 4;
                                farthest = d4;
                            }
                            switch (farPair) {
                                case 1:seg = new Segment(a.x1, a.y1, b.x1, b.y1); break;  // a1,b1 proc
                                case 2:seg = new Segment(a.x1, a.y1, b.x2, b.y2); break;  // a1,b2 proc
                                case 3:seg = new Segment(a.x2, a.y2, b.x1, b.y1); break;  // a2,b1 proc
                                default:seg = new Segment(a.x2, a.y2, b.x2, b.y2); break;  // a2,b2 proc
                            }
                            for (int k : a.idxL) {
                                seg.idxL.add(k);
                            }
                            for (int k : b.idxL) {
                                if (!seg.idxL.contains(k)) {
                                    seg.idxL.add(k);
                                }
                            }
                            for (int k : seg.idxL) {
                                System.out.println(k);
                            }
                            System.out.println(String.format("%4d composite Segment %s", this.size(), seg.toString()));
                            this.add(seg);
                            compositesMade = true;
                        }
                    }
                }
            }
            // discard all discard-tagged segments
            if (compositesMade) {
                for (int k : discardL) {
                    this.get(k).discarded = true;
                }
            }
        }  
        // do a final discard of all original segments used in composite segments
        for (i = size; i < this.size(); i++) {
            for (int k : this.get(i).idxL) {
                this.get(k).discarded = true;
            }
        }    
    }
    
    public static List<Integer> getLongIndices(SegmentList segL, Point focalPoint) {
        int i;
        int cnt = 0;
        List<Integer> idxL = new ArrayList<Integer>();
        for (i = 0; i < segL.size(); i++) {
            Segment seg = segL.get(i);
            if (!seg.discarded) {
                // get midpoint
                double midx = (seg.x1 + seg.x2)/2.0;
                double midy = (seg.y1 + seg.y2)/2.0;
                // get distance focalPoint to midpoint
                double dist = Segment.sqrtDistance(midx, midy, focalPoint.x, focalPoint.y);
                //System.out.println(String.format("%f %f %f %f %f", midx, midy, focalPoint.x, focalPoint.y, dist));
                double projectedLength = 211 - 0.183*dist;
                double errPct = Math.abs(seg.d - projectedLength) / seg.d;
                String msg;
                if (errPct < 0.50) {
                    idxL.add(i);
                    cnt++;
                    msg = "pass";
                    System.out.println(String.format("%s seg: %3d %8.2f %s", msg, i, dist, seg.toString()));
                } else {
                    msg = "fail";
                }
//            System.out.println(String.format("%s seg: %3d %8.2f %s", msg, i, dist, seg.toString()));
            }
        }
        System.out.println(String.format("Long segments found: %d", cnt));
        return idxL;
    }

    public TargetList findTargets(List<Integer> lSegL, ContourList contourL) {
        TargetList targetL = new TargetList();

        List<Integer> longAL = new ArrayList<Integer>();
        List<Integer> longBL = new ArrayList<Integer>();
        List<Boolean> keepL  = new ArrayList<Boolean>();
        List<Integer> validateL = new ArrayList<Integer>();
        int i;
        int j;
        // iterate through all permutations of long-sided Segments
        // build a pair of Lists that represent related long-sided Segments
        for (i = 0; i < lSegL.size()-1; i++) {
            for (j = i+1; j < lSegL.size(); j++) {
                int adx = lSegL.get(i);
                int bdx = lSegL.get(j);
                Segment a = this.get(adx);
                Segment b = this.get(bdx);
                // parallel-enough
                if (areParallely(adx, bdx, 0.2)) { //&& areSquarish(adx,bdx,PI/4.0)) {
                    Segment mid = new Segment(a.midx, a.midy, b.midx, b.midy);
                    // nearby to each other
                    if ((mid.d < a.d*0.75) && (mid.d < b.d*075)) {
                        System.out.println(String.format("%4d %4d", adx, bdx));
                        longAL.add(adx);
                        longBL.add(bdx);
                        keepL.add(true);
                    }
                }
            }
        }

        // First step in eliminating superfluous related-segments:
        // A pair of related Segment must not have another related Segment between them.
        for (i = 0; i < lSegL.size(); i++) {
            int posIdxTheta = -1;
            int negIdxTheta = -1;
            int idx = lSegL.get(i);
            for (int m = 0; m < longAL.size(); m++) {
                if (idx == longAL.get(m)) {
                    posIdxTheta = closestMatch(longBL.get(m), idx, posIdxTheta, +1);
                    negIdxTheta = closestMatch(longBL.get(m), idx, negIdxTheta, -1);
                }
                if (idx == longBL.get(m)) {
                    posIdxTheta = closestMatch(longAL.get(m), idx, posIdxTheta, +1);
                    negIdxTheta = closestMatch(longAL.get(m), idx, negIdxTheta, -1);
                }    
            }
            System.out.println(String.format("Seg %4d pos %4d neg %4d", idx, posIdxTheta, negIdxTheta));
            // With the two closest Segments, identified by index of posIdxTheta and negIdxTheta
            // knock out all related-Segments with given Segment that don't have pos|negIdxTheta
            for (int m = 0; m < longAL.size(); m++) {
                if (idx == longAL.get(m)) {
                    if ((longBL.get(m) != posIdxTheta) && (longBL.get(m) != negIdxTheta)) {
                        keepL.set(m, false);
                    }
                }
                if (idx == longBL.get(m)) {
                    if ((longAL.get(m) != posIdxTheta) && (longAL.get(m) != negIdxTheta)) {
                        keepL.set(m, false);
                    }
                }
             }
        }

        System.out.println("remaining pairs 1");
        int count1 = 0;
        for (int m = 0; m < keepL.size(); m++) {
            if (keepL.get(m)) {
                System.out.println(String.format("%d %d", longAL.get(m), longBL.get(m)));
                count1++;
            }
        }   
        System.out.println(count1);

        // Second step in eliminating superfluous related-segments:
        // A pair of related Segment must have their common midpoint in a contour.
        for (int m = 0; m < keepL.size(); m++) {
            if (keepL.get(m)) {
                Segment a = this.get(longAL.get(m));
                Segment b = this.get(longBL.get(m));
                Point p = new Point((a.midx+b.midx)/2.0, (a.midy+b.midy)/2.0);
                if (!contourL.isInsideContour(p)) {
                    keepL.set(m, false);
                }    
            }
        }   

        System.out.println("remaining pairs 2");
        int count2 = 0;
        for (int m = 0; m < keepL.size(); m++) {
            if (keepL.get(m)) {
                System.out.println(String.format("%d %d", longAL.get(m), longBL.get(m)));
                count2++;
            }
        }   
        System.out.println(count2);

        // Third step in eliminating superfluous related-segments;
        // Find all chains of related-segments and reduce chains to a single pair of related-segments
        List<Integer> pairL = new ArrayList<Integer>();
        List<Integer> relSegL = new ArrayList<Integer>();
        List<Integer> pairLongL = new ArrayList<Integer>();
        for (int m = 0; m < keepL.size(); m++) {
            if (keepL.get(m)) {
                pairL.clear();
                relSegL.clear();
                pairL.add(m);
                relSegL.add(longAL.get(m));
                relSegL.add(longBL.get(m));
                for (int n = m+1; n < keepL.size(); n++) {
                    if (keepL.get(n)) {
                        int q = longAL.get(n);
                        int r = longBL.get(n);
                        if (relSegL.contains(q)) {
                            if (!pairL.contains(n)) { pairL.add(n); }
                            relSegL.add(r);
                            keepL.set(n, false);
                        }
                        if (relSegL.contains(r)) {
                            if (!pairL.contains(n)) { pairL.add(n); }
                            relSegL.add(q);
                            keepL.set(n, false);
                        }
                    }
                }
                System.out.println("related chain");
                for (int n : pairL) {
                    System.out.println(String.format("pair: %4d  %4d  %4d", n, longAL.get(n), longBL.get(n)));
                }
                // find pair with largest area    
                int largestAreaIdx = pairL.get(0);
                double largestArea = 0.0;
                if (pairL.size() > 1) {
                    for (int zz :  pairL) {
                        Segment a = this.get(longAL.get(zz));
                        Segment b = this.get(longBL.get(zz));
                        // Heron's formula
                        // A = sqrt(s*(s-a)*(s-b)*(s-c))
                        //
                        // find closest endpoints
                        double d11 = Segment.sqrtDistance(a.x1, a.y1, b.x1, b.y1);
                        double d12 = Segment.sqrtDistance(a.x1, a.y1, b.x2, b.y2);
                        double da1 = a.d;
                        double db1;
                        double dc1;
                        double da2 = b.d;
                        double db2;
                        double dc2;
                        if (d11 < d12) {
                            db1 = d11;
                            dc1 = Segment.sqrtDistance(a.x2, a.y2, b.x1, b.y1);
                            db2 = dc1;
                            dc2 = Segment.sqrtDistance(a.x2, a.y2, b.x2, b.y2);
                        } else {
                            db1 = d12;
                            dc1 = Segment.sqrtDistance(a.x2, a.y2, b.x2, b.y2);
                            db2 = dc1;
                            dc2 = Segment.sqrtDistance(a.x2, a.y2, b.x1, b.y1);
                        }
                        double s1 = (da1+db1+dc1)/2.0;
                        double a1 = Math.sqrt(s1*(s1-da1)*(s1-db1)*(s1-dc1));
                        double s2 = (da2+db2+dc2)/2.0;
                        double a2 = Math.sqrt(s2*(s2-da2)*(s2-db2)*(s2-dc2));
                        double area = a1+a2;
                        System.out.println(String.format("s1: %10.4f da1:%10.4f db1:%10.4f dc1:%10.4f", s1, da1, db1, dc1));
                        System.out.println(String.format("s2: %10.4f da2:%10.4f db2:%10.4f dc2:%10.4f", s2, da2, db2, dc2));
                        System.out.println(String.format("area: %10.4f %4d %4d", area, longAL.get(zz), longBL.get(zz)));
                        if (area > largestArea) {
                            largestArea = area;
                            largestAreaIdx = zz;
                        }
                    }
                }
                System.out.println(String.format("Target: %4d %4d", longAL.get(largestAreaIdx), longBL.get(largestAreaIdx)));
                pairLongL.add(largestAreaIdx);
            }
        }

        // Create Targets from filtered related-Segments and return TargetList
        for (int n : pairLongL) {
            Segment a = this.get(longAL.get(n));
            Segment b = this.get(longBL.get(n));
            Point centroid = new Point(((a.x1+a.x2+b.x1+b.x2)/4.0), ((a.y1+a.y2+b.y1+b.y2)/4.0));
            double ath = a.theta;
            double bth = b.theta;
            double theta = (a.theta + b.theta)/2.0;
            if ((a.theta - b.theta) > PI/2.0) { theta += PI/2.0; }
            if (theta < 0) { theta += PI; }
            if (theta > PI) { theta -= PI; }
            int thetaD = (int) Math.toDegrees(theta);
            System.out.println(String.format("a.th: %8.4f b.th %8.4f th %8.4f deg %d", ath, bth, theta, thetaD));
            Target target = new Target(centroid, thetaD);
            targetL.add(target);
        }
        return targetL;
    }

    private int closestMatch(int targetSeg, int thisSeg, int closestSeg, int side) {
        Segment mySeg = this.get(thisSeg);
        Segment targSeg = this.get(targetSeg);
        Segment closeSeg;
        // define a Segment between midpoints of given Segment and a potentially close Segment
        Segment myTargSeg = new Segment(mySeg.midx, mySeg.midy, targSeg.midx, targSeg.midy);
        // get the angle between given Segment and midpoint Segment [-PI,+PI]
        double midTheta = mySeg.theta - myTargSeg.theta;
        double closeTheta;
        if (midTheta > +PI) { midTheta = midTheta - 2.0*PI; }
        if (midTheta < -PI) { midTheta = midTheta + 2.0*PI; }
        System.out.println(String.format("this %4d targ %4d is %4d side %2d dist %8.4f theta %8.4f", thisSeg, targetSeg, closestSeg, side, myTargSeg.d, myTargSeg.theta));
        if ((side > 0) && (midTheta > 0)) {
            if (closestSeg < 0) { return targetSeg; }
            closeSeg = this.get(closestSeg);
            Segment myCloseSeg = new Segment(mySeg.midx, mySeg.midy, closeSeg.midx, closeSeg.midy);
            return (myTargSeg.d < myCloseSeg.d) ? targetSeg : closestSeg;
        } else if ((side < 0) && (midTheta < 0)) {
            if (closestSeg < 0) { return targetSeg; }
            closeSeg = this.get(closestSeg);
            Segment myCloseSeg = new Segment(mySeg.midx, mySeg.midy, closeSeg.midx, closeSeg.midy);
            return (myTargSeg.d < myCloseSeg.d) ? targetSeg : closestSeg;
        }
        return closestSeg;
    }



 
    /**
     *  Verifies if a given line segment is within proximate
     *  distance of a given point.    
     *  @param x  x-coordinate of Point
     *  @param y  y-coordinate of Point
     *  @param i  index into SegmentList to identify a Segment
     *  @param d  distance constraint
     *  @return   True, if either Segment endpoint is within distance constraint of Point
     */
    public boolean pointProximate(double x, double y, int i, double d) {
        Segment seg = this.get(i);
        return (((abs(seg.x1 - x) <= d) && (abs(seg.y1 - y) <= d)) ||
                ((abs(seg.x2 - x) <= d) && (abs(seg.y2 - y) <= d)));
    }

    /**
     * Deteremine if the endpoint of one Segment is proximate to the endpoint of another Segment
     * @param i - index of 1st Segment within SegmentList
     * @param j - index of 2nd Segment within SegmentList
     * @param d - maximum distnace allowed to be proximate
     * @return - true only if extactly one endpoint of 1st Segment is proximate to exactly one endpoint of 2nd Segment
     */
    public Integer areUniquelyProximate(int i, int j, double d) {
        Segment a = this.get(i);
        Segment b = this.get(j);
        // If there's multiple intersections, then return null
        // if there's no intersections, then return null
        // Else return a number corresponding to Segment endpoints that intersect
        // 0 = a1,b1
        // 1 = a1,b2
        // 2 = a2,b1
        // 3 = a2,b2
        int count = 0;
        Integer intersection = null;
        if (Segment.sqrtDistance(a.x1,a.y1,b.x1,b.y1) <= d) {
            intersection = 0;
            count++;
        }
        if (Segment.sqrtDistance(a.x1,a.y1,b.x2,b.y2) <= d) {
            intersection = 1;
            count++;
        }
        if (Segment.sqrtDistance(a.x2,a.y2,b.x1,b.y1) <= d) {
            intersection = 2;
            count++;
        }
        if (Segment.sqrtDistance(a.x2,a.y2,b.x2,b.y2) <= d) {
            intersection = 3;
            count++;
        }
        if (count != 1) { return null; }
        return intersection;
    }

    /**
     *  Verifies if either endpoint of a given line segment is within
     *  distant constrain of either endpoint of a second given line segment.    
     *  @param i  index into SegmentList to identify a Segment
     *  @param j  index into SegmentList to identify a Segment
     *  @param d  distance constraint
     *  @return   True, if endpoints are within distance constraint of each other
     */
    public boolean areProximate(int i, int j, double d) {
        Segment a = this.get(i);
        Segment b = this.get(j);
        return (((abs(a.x1 - b.x1) <= d) && (abs(a.y1 - b.y1) <= d)) ||
                ((abs(a.x1 - b.x2) <= d) && (abs(a.y1 - b.y2) <= d)) ||
                ((abs(a.x2 - b.x1) <= d) && (abs(a.y2 - b.y1) <= d)) ||
                ((abs(a.x2 - b.x2) <= d) && (abs(a.y2 - b.y2) <= d)));
    }

    /**
     *  Verifies if line segment endpoints are proximate to each other, with
     *  endpoints indexed '1' proximate to each other and endpoints indexed '2'
     *  proximate to each other, within given distance tolerance. 
     *  @param i  index into SegmentList to identify a Segment
     *  @param j  index into SegmentList to identify a Segment
     *  @param d  distance constraint
     *  @return   True, if endpoints are within distance constraint of each other
     */
    public boolean adjacentTwiceProximate(int i, int j, double d) {
        Segment a = this.get(i);
        Segment b = this.get(j);
        return (( (abs(a.x1 - b.x1) <= d) && (abs(a.y1 - b.y1) <= d) ) &&
                ( (abs(a.x2 - b.x2) <= d) && (abs(a.y2 - b.y2) <= d) ));
    }
    
    /**
     *  Verifies if line segment endpoints are proximate to each other, with
     *  endpoints oppositely indexed, '1' and '2', proximate to each other,
     *  within given distance tolerance. 
     *  @param i  index into SegmentList to identify a Segment
     *  @param j  index into SegmentList to identify a Segment
     *  @param d  distance constraint
     *  @return   True, if endpoints are within distance constraint of each other
     */
    public boolean oppositeTwiceProximate(int i, int j, double d) {
        Segment a = this.get(i);
        Segment b = this.get(j);
        return (( (abs(a.x1 - b.x2) <= d) && (abs(a.y1 - b.y2) <= d) ) &&
                ( (abs(a.x2 - b.x1) <= d) && (abs(a.y2 - b.y1) <= d) ));
    }
    
    /**
     *  Verifies if each of a given line segments' endpoints are proximate to
     *  the two endpoints of a second given line segment, which given
     *  distance tolerance.
     *  @param i  index into SegmentList to identify a Segment
     *  @param j  index into SegmentList to identify a Segment
     *  @param d  distance constraint
     *  @return   True, if endpoints are within distance constraint of each other
     */     
    public boolean areTwiceProximate(int i, int j, double d) {
        return (adjacentTwiceProximate(i,j,d) || oppositeTwiceProximate(i,j,d));
    }
    
    /**
     *  Returns endpoint identifiers (1 or 2) for Segments i, j if Segment
     *  endpoints are proximate to each other, within given distance. 
     *  Note: this method does not flag condition if multiple pairs of proximal
     *  endpoints were found.
     *  @param i  index into SegmentList to identify a Segment
     *  @param j  index into SegmentList to identify a Segment
     *  @param d  distance constraint
     *  @return   Array of 2 integers, or empty Array
     */     
    public int[] whichProximal(int i, int j, double d) {
        Segment a = this.get(i);
        Segment b = this.get(j);
        if ( (abs(a.x1 - b.x1) <= d) && (abs(a.x1 - b.x1) <= d) ) {
            int[] val = {1,1};
            return val;
        }
        if ( (abs(a.x1 - b.x2) <= d) && (abs(a.x1 - b.x2) <= d) ) {
            int[] val = {1,2};
            return val;
        }
        if ( (abs(a.x2 - b.x1) <= d) && (abs(a.x2 - b.x1) <= d) ) {
            int[] val = {2,1};
            return val;
        }
        if ( (abs(a.x2 - b.x2) <= d) && (abs(a.x2 - b.x2) <= d) ) {
            int[] val = {2,2};
            return val;
        }
        int[] val = {};
        return (val);
    }

    public boolean areSquarish(int i, int j, double rad) {
        Segment a = this.get(i);
        Segment b = this.get(j);
        Segment midSeg = new Segment(a.midx, a.midy, b.midx, b.midy);
        double th1 = abs(a.theta + 0.0 - midSeg.theta);
        double th2 = abs(a.theta + PI  - midSeg.theta);
        double th3 = abs(a.theta - PI  - midSeg.theta);
        double minTheta = Math.min(th1, th2);
        double perpTheta = Math.abs(minTheta - PI/2.0);
        return perpTheta < rad;
    }
 
    /**
     *  Returns true if theta (radian angle) of two Segments are within
     *  given tolerance, else returns false. 
     *  @param i    index into SegmentList to identify a Segment
     *  @param j    index into SegmentList to identify a Segment
     *  @param rad  radian constraint
     *  @return   True if segments are parallel-enough
     */     
    public boolean areParallely(int i, int j, double rad) {
        Segment a = this.get(i);
        Segment b = this.get(j);
        return((abs(a.theta + 0.0 - b.theta) < rad) ||
               (abs(a.theta +  PI - b.theta) < rad) ||
               (abs(a.theta -  PI - b.theta) < rad));
    }

    /**
     * Returns the theta difference between two Segments
     */
    public double thetaDifference(int i, int j) {
        Segment a = this.get(i);
        Segment b = this.get(j);
        double th1 = abs(a.theta + 0.0 - b.theta);
        double th2 = abs(a.theta + PI  - b.theta);
        double th3 = abs(a.theta - PI  - b.theta);
        double minTheta = Math.min(th1, th2);
        minTheta = Math.min(minTheta, th3);
        return minTheta;
    }

    /**
     *  Mark all Segments within proximity of boundary of 
     *  region-of-interest as discarded.
     *  @param rect  Rectangle representing border of region-of-interest
     *  @return      Number of Segments marked as discarded
     */
    public int doBorderCheck(Rect rect) {
        int count = 0;
        int bx1 = (int) rect.tl().x;
        int bx2 = (int) rect.br().x;
        int by1 = (int) rect.tl().y;
        int by2 = (int) rect.br().y;
        for (Segment s : this) {
            // set proximity for discard to '2' pixels
            if (s.borderCheck(bx1, by1, bx2, by2, 2)) { count += 1; }
        }
        return count;
    }
    

   /**
     *  Constructor to build List of Segments from given Mat.
     *  @param lineMat  Mat of 1 col x N rows with each row being a 4-vector field of doubles. 
     *                  Vector is ordered: (x1,y1,x2,y2) with (x1,y1) and (x2,y2) the endpoints
     *                  of a line segment.
     */
    public SegmentList(Mat lineMat) {
        int i;
        int count = 0;
        double x1, y1, x2, y2;
        for( i = 0; i < lineMat.rows(); i++ ) {
            double[] value = lineMat.get(i, 0);
//            System.out.println(Arrays.toString(value));
            x1 = value[0];
            y1 = value[1];
            x2 = value[2];
            y2 = value[3];
            // for debugging, isolating valid segments within an area of interest
            // for normal operation, comment out the continue
            if (x1 > 160.0 || x2 > 160.0 || y1 < 80.0 || y2 < 80.0) {
//                continue;
            }
            Segment seg = new Segment(x1, y1, x2, y2);
//            System.out.println(String.format("Segment to add: (%.2f,%.2f) (%.2f,%.2f) th %.2f d %.2f", x1, y1, x2, y2, Math.toDegrees(seg.theta), seg.d));
//            System.out.println(String.format("Adding segment: %d", count++));
            this.add(seg);
        }
    }

    /**
     *  Constructor to build null List of Segments.
     */
    public SegmentList() {}


}