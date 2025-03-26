package seacow;

import java.util.Comparator;
import java.util.*;
import static seacow.LineDetect.FOCAL_POINT;

public class TargetComparator implements Comparator<Target>
{
    //Note: adjust weights as needed.
    float distanceWeight = 0.7f;
    float angleWeight = 0.3f;


    //Note: Origin automatically set to (0,0). Can adjust later though


    @Override
    public int compare(Target t, Target t2)
    {
        //The smaller the weight, the better the target.
        double score1 = Coordinate.distance(FOCAL_POINT, t.centroid) * distanceWeight + Math.abs(t.thetaDegrees * angleWeight);
        double score2 =  Coordinate.distance(FOCAL_POINT, t2.centroid) * distanceWeight + Math.abs(t.thetaDegrees * angleWeight);

        System.out.println(String.format("Score 1: %.3f , (%s) | Score 2: %.3f , (%s)", score1, t.toString(), score2, t2.toString()));
        return Double.compare(score1, score2);
      

        

    }
}