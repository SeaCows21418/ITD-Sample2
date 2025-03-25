package seacow;

/**
 * Various utilities for working with angles.
 */
public final class Angle {
   private static final double TAU = 6.283185307179586;

   /**
    * Returns [angle] clamped to `[0, 2pi]`.
    *
    * @param angle angle measure in radians
    */
   public static double norm(double angle) {
      double modifiedAngle = angle % TAU;
      modifiedAngle = (modifiedAngle + TAU) % TAU;
      return modifiedAngle;
   }

   /**
    * Returns [angleDelta] clamped to `[-pi, pi]`.
    *
    * @param angleDelta angle delta in radians
    */
   public static double normDelta(double angleDelta) {
      double modifiedAngleDelta = norm(angleDelta);
      if (modifiedAngleDelta > Math.PI) {
         modifiedAngleDelta -= TAU;
      }

      return modifiedAngleDelta;
   }

   /**
    * Return [angle] in range [0, pi]
    */
   public static double normLine(double angle) {
      double modAngle = angle;
      while (modAngle < 0.0) {
         modAngle += TAU;
      }
      modAngle = modAngle % Math.PI;
      return modAngle;
   }
   
   /**
    * Calculates the acute angle between two lines
    * @param a - theta of line a in radians
    * @param b - theta of line b in radians
    * @return - acute angle between the lines, in radians [0, pi/2]
    */
   public static double acuteLineAngle(double a, double b) {
      double diff = Math.abs(a-b);
      while (diff >= Math.PI) {
         diff -= Math.PI;
      }
      if (diff >= Math.PI/2.0) {
         diff = Math.PI - diff;
      }
      return diff;
   }
}
