package seacow;


import java.awt.Image;
import java.awt.image.BufferedImage;
import java.awt.image.DataBufferByte;
import javax.imageio.ImageIO;


import java.io.File;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;


import org.opencv.core.Core;
import org.opencv.core.CvType;
import org.opencv.core.Mat;
import org.opencv.core.MatOfFloat4;
import org.opencv.core.MatOfPoint;
import org.opencv.core.MatOfPoint2f;
import org.opencv.core.Point;
import org.opencv.core.Rect;
import org.opencv.core.Scalar;
import org.opencv.imgcodecs.Imgcodecs;
import org.opencv.imgproc.Imgproc;
import org.opencv.imgproc.LineSegmentDetector;

public class LineDetect {

//   public static final Point FOCAL_POINT = new Point(0.0, 119.5);
   public static final Point FOCAL_POINT = new Point(0.0, 240.0);
   public static final Rect FRAME = new Rect(0,0,320,240);

   public static final double LONG_LENGTH = 50.0;
   public static final double SHORT_SQUARED = (0.5*LONG_LENGTH*1.5/3.5)*(0.5*LONG_LENGTH*1.5/3.5);


   public static void main(String args[]) {
      try {
         //Loading the OpenCV core library
         System.loadLibrary( Core.NATIVE_LIBRARY_NAME );

         // Read image from file into Mat
         File input = new File("image2CH.jpg");
         BufferedImage image = ImageIO.read(input);
         byte[] data = ((DataBufferByte) image.getRaster().getDataBuffer()).getData();
         Mat sourceImg = new Mat(image.getHeight(), image.getWidth(), CvType.CV_8UC3);
         sourceImg.put(0, 0, data);
         //Save source image to file 
         Imgcodecs.imwrite("LineDetect_src.jpg", sourceImg);


         //
         // Color-filter source image
         // convert from RGB to HSV and apply Scalar limits for filtering
         //
         Mat hsv = new Mat();
         Mat msk = new Mat();
         // Initialize output Mat to all zeros; and to same Size as input Mat
         Mat hsvFilterMat = Mat.zeros(
                 sourceImg.rows(), // int - number of rows
                 sourceImg.cols(), // int - number of columns
                 CvType.CV_8U      // int - Mat data type
         );
         // If the source image was a camera then the Mat is RGB
         // BUT if the source image was a file then the Mat is BGR
         // Convert source Mat in RGB color space to HSV color space
         Imgproc.cvtColor(
                 sourceImg,             // Mat - source
                 hsv,                   // Mat - destination
                 Imgproc.COLOR_BGR2HSV  // int - code space conversion code
         );
         // Create masking Mat msk of all pixels within Scalar boundaries
         Scalar lowerb = new Scalar (15, 100, 100);
         Scalar upperb = new Scalar (31, 255, 255);
         Core.inRange(
                 hsv,       // Mat    - input Mat
                 lowerb,    // Scalar - inclusive lower boundary scalar
                 upperb,    // Scalar - inclusive upper boundary scalar
                 msk        // Mat    - output Mat, same size as src, and of CV_8U type
         );
         // Copy matImgSrc pixels to matImgDst, filtered by msk
         Core.copyTo(
                 sourceImg,      // Mat - source Mat
                 hsvFilterMat,   // Mat - destination Mat
                 msk             // Mat - masking Mat
         );
         Imgcodecs.imwrite("LineDetect_ssrc.jpg", sourceImg);
         Imgcodecs.imwrite("LineDetect_msk.jpg", msk);
         Imgcodecs.imwrite("LineDetect_hsv.jpg", hsvFilterMat);

         //  Create grayed color-filtered image
         Mat filterGrayMat = new Mat(image.getHeight(),image.getWidth(),CvType.CV_8UC1);
         Imgproc.cvtColor(hsvFilterMat, filterGrayMat, Imgproc.COLOR_RGB2GRAY);
         Mat filterGrayMat0 = new Mat(image.getHeight(),image.getWidth(),CvType.CV_8UC1);
         Imgproc.cvtColor(hsvFilterMat, filterGrayMat0, Imgproc.COLOR_RGB2GRAY);
         Mat filterGrayMat1 = new Mat(image.getHeight(),image.getWidth(),CvType.CV_8UC1);
         Imgproc.cvtColor(hsvFilterMat, filterGrayMat1, Imgproc.COLOR_RGB2GRAY);
         Mat filterGrayMat2 = new Mat(image.getHeight(),image.getWidth(),CvType.CV_8UC1);
         Imgproc.cvtColor(hsvFilterMat, filterGrayMat2, Imgproc.COLOR_RGB2GRAY);
         Mat filterGrayMat3 = new Mat(image.getHeight(),image.getWidth(),CvType.CV_8UC1);
         Imgproc.cvtColor(hsvFilterMat, filterGrayMat3, Imgproc.COLOR_RGB2GRAY);

         Mat linesImg = new Mat(image.getHeight(), image.getWidth(), CvType.CV_8UC3);
         Core.copyTo(sourceImg, linesImg, msk);


         //
         // generate contours from grayed color-filtered image
         //
         Mat hierarchy = new Mat();
         List<MatOfPoint> contours = new ArrayList<>();
         Imgproc.findContours(
                 filterGrayMat,           // Mat - input image
                 contours,                // List of MatOfPoints - output List of contours
                 hierarchy,               // Mat - output hierarchy Mat
                 Imgproc.RETR_TREE,       // int - contour retrieval mode
                 Imgproc.CHAIN_APPROX_SIMPLE    // int - contour approximation method
         );

         // Write simplified contour results to file
         ContourHandler.saveContours("LineDetect_ctr.txt", sourceImg, contours);

         //
         // Simplify contours as polygons
         //
         List<MatOfPoint> polygonContours = new ArrayList<>();
         MatOfPoint2f curve = new MatOfPoint2f();
         MatOfPoint2f approxCurve = new MatOfPoint2f();
         for (MatOfPoint contour : contours) {
               System.out.println("contour");
               ContourHandler.printContour(contour);
               // eliminate any contours with less than 4 points
               if (contour.rows() < 4) { continue; }
               // approximate contour to a polygon
               contour.convertTo(curve, CvType.CV_32FC2);
               Imgproc.approxPolyDP(
                        curve,             // input contour (MatOfPoint2f)
                        approxCurve,       // output contour (MatOfPoint2f)
                        2,                 // double, parameter specifying approximation accuracy
                        true               // boolean, true if closed contour
               );
               MatOfPoint approxContour = new MatOfPoint();
               approxCurve.convertTo(approxContour, CvType.CV_32S);
               // eliminate any approximate contours with less than 4 points
               if (approxContour.rows() < 4) { continue; }
               
               System.out.println("approxContour");
               ContourHandler.printContour(approxContour);
               polygonContours.add(approxContour);
         }

         // Write simplified contours to text file
         ContourHandler.saveContours("LineDetect_poly.txt", sourceImg, polygonContours);

         // Write simplified contours to image file
         Mat polyImg = new Mat(image.getHeight(), image.getWidth(), CvType.CV_8UC3);
         Scalar color = new Scalar(0,255,255);
         for (int i = 0; i < polygonContours.size(); i++) {
            //Scalar color = new Scalar(rng.nextInt(256), rng.nextInt(256), rng.nextInt(256));
            Imgproc.drawContours(
                    polyImg,            // input/output mat image
                    polygonContours,    // input List of Mats of contours
                    i,                  // index into List of Mats of contours
                    color,              // Scalar color of drawn contour
                    2,                  // pixel thickness of drawn contour
                    Imgproc.LINE_8,     // LineType of drawn contour
                    hierarchy,          // input Mat of contour hierarchy
                    0,                  // hierarchy threshold of drawn contours
                    new Point()         // contour x,y offset
            );
         }
         Imgcodecs.imwrite("LinesDetect_poly.jpg", polyImg);

         // Find line segments within the grayed color-filtered image
         MatOfFloat4 lines = new MatOfFloat4();
         LineSegmentDetector ls = Imgproc.createLineSegmentDetector();
         ls.detect(filterGrayMat, lines);

         //Draw detected lines in the image
         ls.drawSegments(linesImg, lines);
         Imgcodecs.imwrite("LinesDetect_0_lines.jpg", linesImg);


         double[] line;
         System.out.println("lines detected:");
         System.out.println(lines.toString());
         for (int rows = 0; rows < lines.rows(); rows++) {
            line = lines.get(rows,0);
            System.out.println(Arrays.toString(line));
         }

         
         // Debugging ContourList.isInsideContour with known coordinates
//         System.out.println(c.isInsideContour(new Coordinate(1,1)));
//         System.out.println(c.isInsideContour(new Coordinate(40,30)));
//         System.out.println(c.isInsideContour(new Coordinate(170,50)));
//         System.out.println(c.isInsideContour(new Coordinate(200,170)));
//         System.out.println(c.isInsideContour(new Coordinate(10,170)));
//         System.out.println(c.isInsideContour(new Coordinate(60,85)));

         //
         // Process the image from lines and contours found in image
         // SampleDetector does the mathematical processing to discern Samples within the image.
         //
         System.out.println("SampleDetector");
         SampleDetector sd = new SampleDetector(lines, polygonContours, FRAME, FOCAL_POINT);

         // Draw segments onto image
         ls.drawSegments(filterGrayMat0, sd.segmentsFromSegmentList(sd.segL));
         Imgcodecs.imwrite("LinesDetect_1_segments.jpg", filterGrayMat0);
         // Draw raw Vertices onto image
         ls.drawSegments(filterGrayMat1, sd.segmentsFromSegmentList(sd.idxLongSegL, sd.segL));
         Imgcodecs.imwrite("LinesDetect_2_longs.jpg", filterGrayMat1);
         
         // Draw contour-filtered Vertices onto image
      //   ls.drawSegments(filterGrayMat2, sd.segmentsFromVertexList(sd.filtVertexL));
      //   Imgcodecs.imwrite("LinesDetect_3_filtVertices.jpg", filterGrayMat2);
         // Draw non-duplicate filtered Vertices onto image
      //   ls.drawSegments(filterGrayMat3, sd.segmentsFromVertexList(sd.dupVertexL));
      //   Imgcodecs.imwrite("LinesDetect_4_dupVertices.jpg", filterGrayMat3);
         // Draw legit Vertices onto image
      //   ls.drawSegments(filterGrayMat, sd.segmentsFromVertexList(sd.vertexL));
      //   Imgcodecs.imwrite("LinesDetect_5_legitVertices.jpg", filterGrayMat);

         Mat samplesImg0 = new Mat(image.getHeight(), image.getWidth(), CvType.CV_8UC3);
         samplesImg0 = sourceImg.clone();
         Mat samplesImg = new Mat(image.getHeight(), image.getWidth(), CvType.CV_8UC3);
         samplesImg = sourceImg.clone();

         // Draw Samples onto image
         ls.drawSegments(samplesImg, sd.targetL.centroidsFromTargetList());
         Imgcodecs.imwrite("LinesDetect_7_centroidsTargets.jpg", samplesImg);

         //best target selection
         System.out.println("Target List:");

         for(Target target : sd.targetL)
         {
         System.out.println(target);

         }


         BestTarget targetSelector = new BestTarget(sd.targetL);
        //targetSelector.CompileFakeTargetList();

        Target bestTarget = targetSelector.bestTarget_v2();
        System.out.println(bestTarget);

        //Draw best target
        Mat samplesImg2 = new Mat(image.getHeight(), image.getWidth(), CvType.CV_8UC3);
        samplesImg2 = sourceImg.clone();

        ls.drawSegments(samplesImg2, sd.drawBestTarget(bestTarget));
        Imgcodecs.imwrite(fileShorthand + "_8_bestTargetCentriod.jpg", samplesImg2);




      } catch (Exception e) {
        System.out.println("error: " + e.getMessage());
      }
   }

}