//package org.firstinspires.ftc.teamcode.pkg_image;
package seacow;

import org.opencv.core.Point;

import java.net.ServerSocket;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Collections;
import java.util.Map;
import java.util.stream.Collectors;
import java.util.Comparator;

public class BestTarget
{

  //this is the practice target list
  //TODO: replace this with actual TargetList.
  List<Target> targetList = new ArrayList<Target>();

  List<Double> distanceList = new ArrayList<Double>();
  List<Integer> angleList = new ArrayList<Integer>();

  //should initialize this list to be the size of targetList in order to have a space for every target obj.
 Double[] targetArrayScores;

  private double distanceFromFocalPoint(Coordinate coordinate)
  {
    return Coordinate.distance(FOCAL_POINT, coordinate);
  }

 
  public void CompileFakeTargetList()
  {
    Target A = new Target(new Point(20, 15), 50);
    Target B = new Target(new Point(30,50), 10);
    Target C = new Target(new Point(60, 10), 5);
    Target D = new Target(new Point(0, 50), 30);
    Target E = new Target(new Point(100, 100), 76);
    Target F = new Target(new Point(0, 10), 10);

    targetList.add(A);
    targetList.add(B);
    targetList.add(C);
    targetList.add(D);
    targetList.add(E);
    targetList.add(F);
  }

  ///returns top ranked target object. Score system works off of idx values so lowest score = best score (like golf).
  public Target bestTarget_v2()
  {
    //Taylor:
    //Asked Chat-GPT my question about doing sorting that allowed me to weight the variables differently. 
    //This Comparator is what it recommended. And, I see why. I completely overthought weighting variables. 
    //It's actually very easy. So, all those lines of code got reduced to this. And, I learned about comparators.
    //I understand how it works. I'm just upset I didn't come up with this on my own T-T
    
    

    System.out.println("\n");
    System.out.println("NEW ALGORITHM TEST");

    
    TargetComparator tComp = new TargetComparator();
    Collections.sort(targetList, tComp);

    
    //One line statement that iterates through now-sorted target list and prints the targets.

    for(int i = 0; i < targetList.size(); i++) System.out.println(String.format("%d : %s", i, targetList.get(i).toString()));
    return targetList.get(0);
  }


///incomplete. Does not work. Use v2
  public Target bestTarget_v1()
  {
    System.out.println("Target Listtttttt best target");
    System.out.println(targetList);
    System.out.println("\n");


    //get distances and angles
    int idx = 0;

    targetArrayScores = new Double[targetList.size()];
    for(Target target : targetList)
    {
      Coordinate targetCoordinate = target.centroid;
      //distanceHashmap.put(target, distanceFromOrigin(targetCoordinate));

      distanceList.add(distanceFromFocalPoint(targetCoordinate));
      angleList.add(Integer.valueOf(target.thetaDegrees));
      
      targetArrayScores[idx] = 0.0;
     // System.out.println(String.format("%d , %d , %d", 0, distanceFromOrigin(targetCoordinate), Integer.valueOf(target.thetaDegrees)));
      idx ++;
    }

    System.out.println(distanceList);

    System.out.println("\n");
    //-----distance ranking----
    List<Double> sortedDistance = new ArrayList<>(distanceList);

   
    Collections.sort(sortedDistance);
    idx = 0;

    System.out.println("UNSORTED DISTANCE:");
    System.out.println(distanceList);
    System.out.println("SORTED DISTANCE: ");
    //iterate over sorted distance list. Then, iterate over unsorted distance list.
    //The idx of the unsorted distance list will match with the idx of the target list.
    for(Double distance : sortedDistance)
    {
   //   System.out.println(String.format("%d : %.2f", idx , distance));
      int nestedLoopIdx = 0;
      for(Double originalDistance : distanceList)
      {
        if(distance == originalDistance)
        {

          //set the score of the targetArrayScore list to the idx of the sortedDistanceList.
          //This will give it the score in ranked order.

          targetArrayScores[nestedLoopIdx] = (double) idx;
          System.out.println(String.format("sorted target distance id %d (Target %d) - distance %.2f", idx, nestedLoopIdx, distance));
          System.out.println(String.format("Target %d score updated to %.2f", nestedLoopIdx, targetArrayScores[nestedLoopIdx]));
        }
        nestedLoopIdx ++;
      }
      idx ++;
    }
    System.out.println("\n");

  
    //-----angle ranking----

    //get angles
    List<Integer> sortedAngles = new ArrayList<>(angleList);

    Collections.sort(sortedAngles);

    int angleIdx = 0;
  
    System.out.println("UNSORTED ANGLES:");
    System.out.println(angleList);

    System.out.println("SORTED ANGLES");
    //iterate over sorted angle list. Then, iterate over unsorted angle list.
    //The idx of the unsorted angle list will match with the idx of the target list.
    for(Integer angle : sortedAngles)
    {

     // System.out.println(String.format("%d : %d", angleIdx , angle));

      int nestedLoopIdx = 0;
      for(Integer originalAngle : angleList)
      {


        if(angle == originalAngle)
        {

         System.out.println(String.format("sorted target angle id %d (Target %d) - angle %d", angleIdx, nestedLoopIdx, angle));

          float angleIdxF = (float) angleIdx;

          System.out.println(String.format("Angle Idx: %.2f", angleIdxF));

          Double newScore = angleIdxF - 0.0;
          System.out.println(String.format("Angle Score to add: %.2f", newScore));

          //set the score of the targetArrayScore list to the idx of the sortedAngleList.
          //This will give it the score in ranked order.

          //Add this index rank with the ones currently scored
          //Added extra point for angle. (this is subtraction because of like golf).
          Double oldScore =  targetArrayScores[nestedLoopIdx];

         
          targetArrayScores[nestedLoopIdx] = targetArrayScores[nestedLoopIdx] + newScore;
          
          System.out.println(String.format("Target %d score updated from %.2f to %.2f", nestedLoopIdx, oldScore, targetArrayScores[nestedLoopIdx]));

        }
        nestedLoopIdx ++;
      }
      angleIdx ++;
    }

    System.out.println("\n");

    //Debug print to make sure everything is still mapped
    for(int i = 0; i < targetArrayScores.length; i++)
    {
      Double distance = distanceList.get(i);
      int angle = angleList.get(i);
      System.out.println(String.format("Score %.2f : Target %d - distance %.2f , angle %d", targetArrayScores[i], i, distance, angle));
    }

    System.out.println("\n");

    //-----get ranked target list-----
    Double[] sortedScoreArray = targetArrayScores;

    Arrays.sort(sortedScoreArray);

    int scorePrintCounter = 0;
    System.out.println("SCORES");
    for(Double score : sortedScoreArray)
    {
      System.out.println(String.format("%d : %.2f", scorePrintCounter , score));
      scorePrintCounter ++;

    }


    //return lowest score:

    //Note: will error if actually returns with this idx. But, it should catch this in the loop below.
    int scoreIdx = 0;
    int idxOfTopContender = 0;

    Double topScore = sortedScoreArray[0];

    System.out.println(java.util.Arrays.stream(sortedScoreArray).distinct().count());

  //only save the top 3 that are the same.
    int[] topContenders = new int[3];

    System.out.println("TOP 3 CONTENDERS");
    for(Double score : targetArrayScores)
    {

      System.out.println(String.format("%d : %.2f", scoreIdx , score));

      //check if score in targetArrayScores is same as sortedScoreArray first one.

      //Save any idx that matches the highest score. Will sort to see which one has lowest angle.
      if(score == sortedScoreArray[0])
      {
        try {
          System.out.println(String.format("Adding target %d with score %.2f . Matches: %b", scoreIdx, score, (score == sortedScoreArray[0])));
          topContenders[idxOfTopContender] = scoreIdx;
          idxOfTopContender++;
        } catch (IndexOutOfBoundsException e)
        {
          System.out.println("breaking....");
          break;
         
          //do nothing because only getting top three.
        }
        // idxOfTopScore = scoreIdx;
      }
      scoreIdx++;

        //break;
    }

   /* for(Double score : topContenders)
    {
      System.out.println(score);
    }*/
    Integer[] topContenderAngles = new Integer[3];
   int topContenderAngleIdx = 0;

    System.out.println("TOP CONTENDER ANGLES");
    System.out.println(topContenderAngles);

    
    for(int targetIdx : topContenders)
    {
        topContenderAngles[topContenderAngleIdx] = angleList.get(targetIdx);
        System.out.println( topContenderAngles[topContenderAngleIdx]);
    }
    
    
    Integer[] sortedTopContenderAngles = topContenderAngles;

    Arrays.sort(sortedTopContenderAngles);


    int idxOfTopScore = -1;
    int idxOfangle = 0;
    for(Integer angle : topContenderAngles)
    {
      if(angle == sortedTopContenderAngles[0])
      {
        idxOfTopScore = idxOfangle;
      }
      idxOfangle ++;
    }



    System.out.println(String.format("Selected target with score %d with angle %.2f", sortedScoreArray[0], topContenderAngles[topContenderAngleIdx]));
   // return targetList.get(idxOfTopScore);
  return targetList.get(idxOfTopScore);
  }

  public BestTarget(List<Target> tl)
  {
    this.targetList = tl;
  }

 /* public static void main(String args[]) 
  {
    BestTarget bt = new BestTarget();
    bt.CompileFakeTargetList();
    
    
    System.out.println(bt.bestTarget_v2());


  }*/
}
