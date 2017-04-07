package us.ihmc.euclid.geometry.tools;

import static org.junit.Assert.*;
import static us.ihmc.euclid.geometry.tools.EuclidGeometryPolygonTools.*;
import static us.ihmc.euclid.geometry.tools.EuclidGeometryRandomTools.generateRandomCircleBasedConvexPolygon2D;
import static us.ihmc.euclid.geometry.tools.EuclidGeometryRandomTools.generateRandomPointCloud2D;
import static us.ihmc.euclid.geometry.tools.EuclidGeometryTools.*;
import static us.ihmc.euclid.tools.EuclidCoreRandomTools.generateRandomDouble;
import static us.ihmc.euclid.tools.EuclidCoreRandomTools.generateRandomPoint2D;
import static us.ihmc.euclid.tools.EuclidCoreRandomTools.generateRandomVector2D;
import static us.ihmc.euclid.tools.EuclidCoreRandomTools.generateRandomVector2DWithFixedLength;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashSet;
import java.util.List;
import java.util.Random;
import java.util.Set;

import org.junit.Test;

import us.ihmc.euclid.tools.EuclidCoreRandomTools;
import us.ihmc.euclid.tools.EuclidCoreTestTools;
import us.ihmc.euclid.tuple2D.Point2D;
import us.ihmc.euclid.tuple2D.Vector2D;
import us.ihmc.euclid.tuple2D.interfaces.Point2DReadOnly;

public class EuclidGeometryPolygonToolsTest
{
   private static final double SMALL_EPSILON = 1.0e-9;
   private static final double SMALLEST_EPSILON = 1.0e-12;
   private static final int ITERATIONS = 1000;

   private static interface ConvexHullAlgorithm
   {
      int process(List<? extends Point2DReadOnly> vertices, int numberOfVertices);
   }

   @Test
   public void testIsPolygon2DConvexAtVertex() throws Exception
   {
      Random random = new Random(345345L);

      {
         int n = 4;
         List<Point2DReadOnly> clockwiseSquareVertices = new ArrayList<>();
         clockwiseSquareVertices.add(new Point2D(0.0, 1.0));
         clockwiseSquareVertices.add(new Point2D(1.0, 1.0));
         clockwiseSquareVertices.add(new Point2D(1.0, 0.0));
         clockwiseSquareVertices.add(new Point2D(0.0, 0.0));
         List<Point2DReadOnly> counterClockwiseSquareVertices = new ArrayList<>(clockwiseSquareVertices);
         Collections.reverse(counterClockwiseSquareVertices);

         for (int index = 0; index < n; index++)
         {
            assertTrue(isPolygon2DConvexAtVertex(index, clockwiseSquareVertices, true));
            assertFalse(isPolygon2DConvexAtVertex(index, clockwiseSquareVertices, false));
            assertTrue(isPolygon2DConvexAtVertex(index, counterClockwiseSquareVertices, false));
            assertFalse(isPolygon2DConvexAtVertex(index, counterClockwiseSquareVertices, true));
         }
      }

      for (int i = 0; i < ITERATIONS; i++)
      {
         int numberOfPoints = random.nextInt(97) + 3;
         List<Point2D> points = generateRandomCircleBasedConvexPolygon2D(random, 10.0, 1.0, numberOfPoints);
         boolean clockwiseOrdered = random.nextBoolean();
         if (!clockwiseOrdered)
            Collections.reverse(points);

         for (int index = 0; index < numberOfPoints; index++)
         {
            assertTrue("Iteration: " + i + ", index: " + index, isPolygon2DConvexAtVertex(index, points, clockwiseOrdered));
            assertFalse("Iteration: " + i + ", index: " + index, isPolygon2DConvexAtVertex(index, points, !clockwiseOrdered));
         }

         int numberOfExtraPoints = random.nextInt(20);
         for (int j = 0; j < numberOfExtraPoints; j++)
            points.add(generateRandomPoint2D(random, 10.0));

         for (int index = 0; index < numberOfPoints; index++)
         {
            assertTrue("Iteration: " + i + ", index: " + index, isPolygon2DConvexAtVertex(index, points, numberOfPoints, clockwiseOrdered));
            assertFalse("Iteration: " + i + ", index: " + index, isPolygon2DConvexAtVertex(index, points, numberOfPoints, !clockwiseOrdered));
         }

         try
         {
            isPolygon2DConvexAtVertex(-1, points, clockwiseOrdered);
            fail("Should have thrown an " + IndexOutOfBoundsException.class.getSimpleName());
         }
         catch (IndexOutOfBoundsException e)
         {
            // good
         }

         try
         {
            isPolygon2DConvexAtVertex(points.size(), points, clockwiseOrdered);
            fail("Should have thrown an " + IndexOutOfBoundsException.class.getSimpleName());
         }
         catch (IndexOutOfBoundsException e)
         {
            // good
         }

         try
         {
            isPolygon2DConvexAtVertex(0, points, -1, clockwiseOrdered);
            fail("Should have thrown an " + IllegalArgumentException.class.getSimpleName());
         }
         catch (IllegalArgumentException e)
         {
            // good
         }

         try
         {
            isPolygon2DConvexAtVertex(0, points, points.size() + 1, clockwiseOrdered);
            fail("Should have thrown an " + IllegalArgumentException.class.getSimpleName());
         }
         catch (IllegalArgumentException e)
         {
            // good
         }
      }

      assertFalse(isPolygon2DConvexAtVertex(0, Collections.singletonList(generateRandomPoint2D(random, 10.0)), true));
      assertFalse(isPolygon2DConvexAtVertex(0, Collections.singletonList(generateRandomPoint2D(random, 10.0)), false));

      List<Point2D> points = new ArrayList<>();
      points.add(generateRandomPoint2D(random, 10.0));
      points.add(generateRandomPoint2D(random, 10.0));

      assertFalse(isPolygon2DConvexAtVertex(0, points, true));
      assertFalse(isPolygon2DConvexAtVertex(1, points, true));
      assertFalse(isPolygon2DConvexAtVertex(0, points, false));
      assertFalse(isPolygon2DConvexAtVertex(1, points, false));
   }

   @Test
   public void testInPlaceGiftWrapConvexHull2D() throws Exception
   {
      Random random = new Random(3245345L);
      testConvexHullAlgorithm(random, (vertices, numberOfVertices) -> inPlaceGiftWrapConvexHull2D(vertices, numberOfVertices));

      for (int i = 0; i < ITERATIONS; i++)
      {
         int numberOfVertices = 100;
         List<? extends Point2DReadOnly> points = generateRandomPointCloud2D(random, 10.0, 10.0, numberOfVertices);
         List<? extends Point2DReadOnly> pointsCopy = new ArrayList<>(points);

         int actualHullSize = inPlaceGiftWrapConvexHull2D(points);
         int expectedHullSize = inPlaceGiftWrapConvexHull2D(pointsCopy, numberOfVertices);
         assertEquals(expectedHullSize, actualHullSize);
         assertEquals(points, pointsCopy);
      }
   }

   @Test
   public void testInPlaceGrahamScanConvexHull2D() throws Exception
   {
      Random random = new Random(5641651419L);
      testConvexHullAlgorithm(random, (vertices, numberOfVertices) -> inPlaceGrahamScanConvexHull2D(vertices, numberOfVertices));

      for (int i = 0; i < ITERATIONS; i++)
      {
         int numberOfVertices = 100;
         List<? extends Point2DReadOnly> points = generateRandomPointCloud2D(random, 10.0, 10.0, numberOfVertices);
         List<? extends Point2DReadOnly> pointsCopy = new ArrayList<>(points);

         int actualHullSize = inPlaceGrahamScanConvexHull2D(points);
         int expectedHullSize = inPlaceGrahamScanConvexHull2D(pointsCopy, numberOfVertices);
         assertEquals(expectedHullSize, actualHullSize);
         assertEquals(points, pointsCopy);
      }
   }

   @Test
   public void testCompareConvexHullAlgorithms() throws Exception
   {
      Random random = new Random(23454L);

      List<ConvexHullAlgorithm> algorithmsToTest = new ArrayList<>();
      algorithmsToTest.add((vertices, numberOfVertices) -> inPlaceGiftWrapConvexHull2D(vertices, numberOfVertices));
      algorithmsToTest.add((vertices, numberOfVertices) -> inPlaceGrahamScanConvexHull2D(vertices, numberOfVertices));

      for (int i = 0; i < ITERATIONS; i++)
      {
         int numberOfVertices = 100;
         List<? extends Point2DReadOnly> points = generateRandomPointCloud2D(random, 10.0, 10.0, numberOfVertices);
         List<List<? extends Point2DReadOnly>> pointsForEachAlgo = new ArrayList<>();
         while (pointsForEachAlgo.size() < algorithmsToTest.size())
            pointsForEachAlgo.add(new ArrayList<>(points));

         List<Integer> hullSizes = new ArrayList<>();

         for (int index = 0; index < algorithmsToTest.size(); index++)
            hullSizes.add(algorithmsToTest.get(index).process(pointsForEachAlgo.get(index), numberOfVertices));

         // Compare the different algorithms against the first one
         for (int algoIndex = 1; algoIndex < algorithmsToTest.size(); algoIndex++)
         {
            assertEquals(hullSizes.get(0), hullSizes.get(algoIndex));
            for (int vertexIndex = 0; vertexIndex < hullSizes.get(0); vertexIndex++)
               assertTrue(pointsForEachAlgo.get(0).get(vertexIndex) == pointsForEachAlgo.get(algoIndex).get(vertexIndex));
         }
      }
   }

   private static void testConvexHullAlgorithm(Random random, ConvexHullAlgorithm algorithmToTest) throws Exception
   {
      { // Test the exceptions
         int numberOfPoints = 100;
         List<Point2D> points = generateRandomPointCloud2D(random, 10.0, 10.0, numberOfPoints);

         try
         {
            algorithmToTest.process(points, -1);
            fail("Should have thrown an " + IllegalArgumentException.class.getSimpleName());
         }
         catch (IllegalArgumentException e)
         {
            // good
         }

         try
         {
            algorithmToTest.process(points, numberOfPoints + 1);
            fail("Should have thrown an " + IllegalArgumentException.class.getSimpleName());
         }
         catch (IllegalArgumentException e)
         {
            // good
         }
      }

      for (int i = 0; i < ITERATIONS; i++)
      { // Test simple features
         int numberOfPoints = 100;
         int numberOfPointsToProcess = random.nextInt(numberOfPoints - 1) + 1;
         List<Point2D> listToProcess = generateRandomPointCloud2D(random, 10.0, 10.0, numberOfPoints);
         List<Point2D> original = new ArrayList<>(listToProcess);

         int hullSize = algorithmToTest.process(listToProcess, numberOfPointsToProcess);
         // Test the given list does not get resized.
         assertEquals(numberOfPoints, listToProcess.size());
         // Test that the hull size is smaller or equal to the original number of points.
         assertTrue(hullSize <= numberOfPointsToProcess);
         // Test that the points in [numberOfPointsToProcess, numberOfPoints[ remain unchanged.
         for (int index = numberOfPointsToProcess; index < numberOfPoints; index++)
            assertTrue(original.get(index) == listToProcess.get(index));
         // Test that processing a twice time does not do anything.
         List<? extends Point2DReadOnly> reprocessedList = new ArrayList<>(listToProcess);
         int reprocessedHullSize = algorithmToTest.process(reprocessedList, numberOfPointsToProcess);
         assertEquals(hullSize, reprocessedHullSize);
         for (int index = 0; index < numberOfPoints; index++)
            assertTrue(listToProcess.get(index) == reprocessedList.get(index));

         // Test that with numberOfPointsToProcess = 1, the algorithm does not do anything 
         numberOfPointsToProcess = 1;
         listToProcess = new ArrayList<>(original);
         hullSize = algorithmToTest.process(listToProcess, numberOfPointsToProcess);
         assertEquals(numberOfPointsToProcess, hullSize);
         assertTrue(original.get(0) == listToProcess.get(0));

         // Test that with numberOfPointsToProcess = 2, the algorithm just reorders the two vertices to start with minXMaxYVertex 
         numberOfPointsToProcess = 2;
         listToProcess = new ArrayList<>(original);
         hullSize = algorithmToTest.process(listToProcess, numberOfPointsToProcess);
         assertEquals(numberOfPointsToProcess, hullSize);
         if (EuclidGeometryPolygonTools.findMinXMaxYVertexIndex(original, numberOfPointsToProcess) == 0)
         {
            assertTrue(original.get(0) == listToProcess.get(0));
            assertTrue(original.get(1) == listToProcess.get(1));
         }
         else
         {
            assertTrue(original.get(1) == listToProcess.get(0));
            assertTrue(original.get(0) == listToProcess.get(1));
         }
      }

      for (int i = 0; i < ITERATIONS; i++)
      { // Test with vertices that already form a convex hull make sure the algorithm just shift the points around so it starts with the min x, max y vertex.
         int numberOfPoints = 100;
         List<Point2D> vertices = generateRandomCircleBasedConvexPolygon2D(random, 10.0, 1.0, numberOfPoints);
         int startIndex = EuclidGeometryPolygonTools.findMinXMaxYVertexIndex(vertices, numberOfPoints);
         List<Point2D> convexHullVertices = new ArrayList<>();
         for (int index = 0; index < numberOfPoints; index++)
            convexHullVertices.add(vertices.get(wrap(index + startIndex, numberOfPoints)));

         List<? extends Point2DReadOnly> copy = new ArrayList<>(convexHullVertices);
         algorithmToTest.process(copy, copy.size());

         for (int index = 0; index < copy.size(); index++)
            assertTrue("Failed at index: " + index, copy.get(index) == convexHullVertices.get(index));
      }

      for (int i = 0; i < ITERATIONS; i++)
      { // Test that the resulting list is convex at every vertex
         int numberOfPoints = 100;
         List<? extends Point2DReadOnly> points = generateRandomPointCloud2D(random, 10.0, 10.0, numberOfPoints);
         int hullSize = algorithmToTest.process(points, numberOfPoints);
         for (int index = 0; index < hullSize; index++)
            assertTrue("Is not convex at vertex index: " + index, EuclidGeometryPolygonTools.isPolygon2DConvexAtVertex(index, points, hullSize, true));
      }

      for (int i = 0; i < ITERATIONS; i++)
      { // Test that Graham scan sorting algorithm does change the output of the already processed vertices.
         int numberOfPoints = 100;
         List<? extends Point2DReadOnly> processedList = generateRandomPointCloud2D(random, 10.0, 10.0, numberOfPoints);
         int hullSize = algorithmToTest.process(processedList, numberOfPoints);
         List<? extends Point2DReadOnly> sortedList = new ArrayList<>(processedList);
         EuclidGeometryPolygonTools.grahamScanAngleSort(sortedList, hullSize);
         for (int index = 0; index < hullSize; index++)
            assertTrue(processedList.get(index) == sortedList.get(index));
      }

      for (int i = 0; i < ITERATIONS; i++)
      { // Test that the sum of the angles from edge to edge is equal to 2*PI, ensuring that the resulting hull does not do several revolutions
         int numberOfPoints = 100;
         List<? extends Point2DReadOnly> processedList = generateRandomPointCloud2D(random, 10.0, 10.0, numberOfPoints);
         int hullSize = algorithmToTest.process(processedList, numberOfPoints);

         double sumOfAngles = 0.0;

         for (int index = 0; index < hullSize; index++)
         {
            Point2DReadOnly previousVertex = processedList.get(previous(index, hullSize));
            Point2DReadOnly vertex = processedList.get(index);
            Point2DReadOnly nextVertex = processedList.get(next(index, hullSize));
            Vector2D previousEdge = new Vector2D();
            previousEdge.sub(vertex, previousVertex);
            Vector2D nextEdge = new Vector2D();
            nextEdge.sub(nextVertex, vertex);
            sumOfAngles -= previousEdge.angle(nextEdge); // Because the vertices are clockwise ordered.
         }

         assertEquals(2.0 * Math.PI, sumOfAngles, SMALLEST_EPSILON);
      }

      for (int i = 0; i < ITERATIONS; i++)
      { // Test that duplicate vertices are removed from the hull
         int numberOfPoints = 100;
         List<Point2D> processedList = generateRandomPointCloud2D(random, 10.0, 10.0, numberOfPoints);
         int numberOfDuplicates = random.nextInt(50);
         while (numberOfDuplicates > 0)
         {
            int indexToDuplicate = random.nextInt(processedList.size());
            processedList.add(new Point2D(processedList.get(indexToDuplicate)));
            numberOfDuplicates--;
         }
         Collections.shuffle(processedList);

         int hullSize = algorithmToTest.process(processedList, numberOfPoints);
         // Assert there is no duplicate in [0, hullSize[
         for (int firstIndex = 0; firstIndex < hullSize; firstIndex++)
         {
            Point2DReadOnly first = processedList.get(firstIndex);

            for (int secondIndex = firstIndex + 1; secondIndex < hullSize; secondIndex++)
            {
               Point2DReadOnly second = processedList.get(secondIndex);
               assertFalse(duplicateMessage(processedList, hullSize, firstIndex, secondIndex), first.epsilonEquals(second, EuclidGeometryPolygonTools.EPSILON));
            }
         }
      }
   }

   private static String duplicateMessage(List<Point2D> processedList, int hullSize, int firstIndex, int secondIndex)
   {
      return "Found duplicate vertices (" + firstIndex + " and " + secondIndex + ") \n" + processedList.subList(0, hullSize);
   }

   @Test
   public void testGrahamScanAngleSort() throws Exception
   {
      Random random = new Random(324234L);

      for (int i = 0; i < ITERATIONS; i++)
      {
         int numberOfPoints = 10;
         List<Point2D> points = generateRandomPointCloud2D(random, 10.0, 10.0, numberOfPoints);
         List<Point2D> pointsCopy = new ArrayList<>(points);

         int minXMaxYIndex = EuclidGeometryPolygonTools.findMinXMaxYVertexIndex(points, points.size());
         Point2D minXMaxYVertex = points.get(minXMaxYIndex);

         Comparator<Point2DReadOnly> comparator = (vertex1, vertex2) -> grahamScanAngleCompare(minXMaxYVertex, vertex1, vertex2);
         Collections.sort(points, comparator);

         assertTrue(minXMaxYVertex == points.get(0));

         Point2D offset = new Point2D(minXMaxYVertex);
         points.forEach(v -> v.sub(offset));

         List<Double> angles = new ArrayList<>();
         // x and y are flipped on purpose as the comparison is based on the angle with respect to the y-axis.
         points.forEach(v -> angles.add(Math.atan2(v.getX(), v.getY())));

         for (int index = 1; index < numberOfPoints - 1; index++)
            assertTrue(angles.get(index) < angles.get(index + 1));

         EuclidGeometryPolygonTools.grahamScanAngleSort(pointsCopy, numberOfPoints);
         for (int index = 0; index < numberOfPoints; index++)
            assertTrue(points.get(index) == pointsCopy.get(index));
      }
   }

   @Test
   public void testComputeConvexPolygon2DArea() throws Exception
   {
      Random random = new Random(345345L);

      for (int i = 0; i < ITERATIONS; i++)
      {
         List<? extends Point2DReadOnly> convexPolygon2D = generateRandomPointCloud2D(random, 10.0, 10.0, 100);
         int hullSize = EuclidGeometryPolygonTools.inPlaceGrahamScanConvexHull2D(convexPolygon2D);
         boolean clockwiseOrdered = random.nextBoolean();
         if (!clockwiseOrdered)
            Collections.reverse(convexPolygon2D.subList(0, hullSize));

         Point2D average = new Point2D();
         convexPolygon2D.subList(0, hullSize).forEach(average::add);
         average.scale(1.0 / hullSize);

         double expectedArea = 0.0;
         for (int index = 0; index < hullSize; index++)
         {
            Point2DReadOnly vertex = convexPolygon2D.get(index);
            Point2DReadOnly nextVertex = convexPolygon2D.get(next(index, hullSize));
            expectedArea += triangleArea(average, vertex, nextVertex);
         }
         Point2D centroid = new Point2D();
         double actualArea = computeConvexPolyong2DArea(convexPolygon2D, hullSize, clockwiseOrdered, centroid);
         assertEquals(expectedArea, actualArea, SMALLEST_EPSILON);

         Point2D recomputedCentroid = new Point2D();

         for (int index = 0; index < hullSize; index++)
         {
            Point2DReadOnly previousVertex = convexPolygon2D.get(previous(index, hullSize));
            Point2DReadOnly vertex = convexPolygon2D.get(index);
            Point2DReadOnly nextVertex = convexPolygon2D.get(next(index, hullSize));

            double vertexWeight = 0.0;
            vertexWeight += triangleArea(centroid, vertex, previousVertex);
            vertexWeight += triangleArea(centroid, vertex, nextVertex);
            vertexWeight /= 2.0 * actualArea;
            recomputedCentroid.add(vertexWeight * vertex.getX(), vertexWeight * vertex.getY());
         }
         EuclidCoreTestTools.assertTuple2DEquals(recomputedCentroid, centroid, SMALLEST_EPSILON);

         try
         {
            computeConvexPolyong2DArea(convexPolygon2D, -1, clockwiseOrdered, centroid);
            fail("Should have thrown an " + IllegalArgumentException.class.getSimpleName());
         }
         catch (IllegalArgumentException e)
         {
            // good
         }

         try
         {
            computeConvexPolyong2DArea(convexPolygon2D, convexPolygon2D.size() + 1, clockwiseOrdered, centroid);
            fail("Should have thrown an " + IllegalArgumentException.class.getSimpleName());
         }
         catch (IllegalArgumentException e)
         {
            // good
         }
      }

      { // Test with empty polygon
         Point2D centroid = new Point2D();
         double area = computeConvexPolyong2DArea(Collections.emptyList(), 0, true, centroid);
         assertTrue(Double.isNaN(area));
         EuclidCoreTestTools.assertTuple2DContainsOnlyNaN(centroid);
      }

      { // Test with a polygon that has only one vertex
         Point2D vertex = generateRandomPoint2D(random, 10.0);
         Point2D centroid = new Point2D();
         double area = computeConvexPolyong2DArea(Collections.singletonList(vertex), 1, true, centroid);
         assertTrue(area == 0.0);
         EuclidCoreTestTools.assertTuple2DEquals(vertex, centroid, SMALLEST_EPSILON);
      }

      { // Test with a polygon that has only 2 vertex
         Point2D vertex0 = generateRandomPoint2D(random, 10.0);
         Point2D vertex1 = generateRandomPoint2D(random, 10.0);
         List<Point2D> points = new ArrayList<>();
         points.add(vertex0);
         points.add(vertex1);
         Point2D expectedCentroid = averagePoint2Ds(points);
         Point2D actualCentroid = new Point2D();
         double area = computeConvexPolyong2DArea(points, 2, true, actualCentroid);
         assertTrue(area == 0.0);
         EuclidCoreTestTools.assertTuple2DEquals(expectedCentroid, actualCentroid, SMALLEST_EPSILON);
      }

      { // Test with a tiny polygon
         Point2D vertex0 = generateRandomPoint2D(random, 10.0);
         Vector2D toVertex1 = generateRandomVector2DWithFixedLength(random, 1.0e-8);
         Vector2D toVertex2 = generateRandomVector2DWithFixedLength(random, 1.0e-8);
         Point2D vertex1 = new Point2D();
         vertex1.add(vertex0, toVertex1);
         Point2D vertex2 = new Point2D();
         vertex2.add(vertex0, toVertex2);

         List<Point2D> points = new ArrayList<>();
         points.add(vertex0);
         points.add(vertex1);
         points.add(vertex2);

         Point2D centroid = new Point2D();
         computeConvexPolyong2DArea(points, 3, true, centroid);
         EuclidCoreTestTools.assertTuple2DEquals(vertex0, centroid, SMALLEST_EPSILON);
      }
   }

   @Test
   public void testEdgeNormal() throws Exception
   {
      Random random = new Random(234234L);

      for (int i = 0; i < ITERATIONS; i++)
      {
         List<? extends Point2DReadOnly> convexPolygon2D = generateRandomCircleBasedConvexPolygon2D(random, 10.0, 10.0, 100);
         int hullSize = inPlaceGrahamScanConvexHull2D(convexPolygon2D);
         boolean clockwiseOrdered = random.nextBoolean();
         if (!clockwiseOrdered)
            Collections.reverse(convexPolygon2D.subList(0, hullSize));

         for (int edgeIndex = 0; edgeIndex < hullSize; edgeIndex++)
         {
            Point2DReadOnly edgeStart = convexPolygon2D.get(edgeIndex);
            Point2DReadOnly edgeEnd = convexPolygon2D.get(next(edgeIndex, hullSize));

            Vector2D edgeNormal = new Vector2D();
            boolean success = edgeNormal(edgeIndex, convexPolygon2D, hullSize, clockwiseOrdered, edgeNormal);
            assertTrue(success);
            assertEquals(1.0, edgeNormal.length(), SMALLEST_EPSILON);

            Vector2D edgeDirection = new Vector2D();
            edgeDirection.sub(edgeEnd, edgeStart);
            assertEquals(0.0, edgeDirection.dot(edgeNormal), SMALLEST_EPSILON);

            Point2D pointInsidePolygon = new Point2D();
            pointInsidePolygon.scaleAdd(-1.0e-8, edgeNormal, edgeStart);
            assertTrue("Iteration: " + i + ", edgeIndex: " + edgeIndex,
                       isPoint2DInsideConvexPolygon2D(pointInsidePolygon, convexPolygon2D, hullSize, clockwiseOrdered, 0.0));

            Point2D pointOutsidePolygon = new Point2D();
            pointOutsidePolygon.scaleAdd(1.0e8, edgeNormal, edgeStart);
            assertFalse(isPoint2DInsideConvexPolygon2D(pointOutsidePolygon, convexPolygon2D, hullSize, clockwiseOrdered, 0.0));
         }
      }

      { // Test that the method fails with singleton
         assertFalse(edgeNormal(0, Collections.singletonList(generateRandomPoint2D(random)), 1, true, new Vector2D()));
      }

      { // Test exceptions
         List<? extends Point2DReadOnly> convexPolygon2D = generateRandomCircleBasedConvexPolygon2D(random, 10.0, 10.0, 100);
         int hullSize = inPlaceGrahamScanConvexHull2D(convexPolygon2D);
         boolean clockwiseOrdered = random.nextBoolean();
         if (!clockwiseOrdered)
            Collections.reverse(convexPolygon2D.subList(0, hullSize));

         try
         {
            edgeNormal(0, convexPolygon2D, -1, clockwiseOrdered, new Vector2D());
            fail("Should have thrown an " + IllegalArgumentException.class.getSimpleName());
         }
         catch (IllegalArgumentException e)
         {
            // good
         }

         try
         {
            edgeNormal(0, convexPolygon2D, convexPolygon2D.size() + 1, clockwiseOrdered, new Vector2D());
            fail("Should have thrown an " + IllegalArgumentException.class.getSimpleName());
         }
         catch (IllegalArgumentException e)
         {
            // good
         }

         try
         {
            edgeNormal(-1, convexPolygon2D, convexPolygon2D.size(), clockwiseOrdered, new Vector2D());
            fail("Should have thrown an " + IndexOutOfBoundsException.class.getSimpleName());
         }
         catch (IndexOutOfBoundsException e)
         {
            // good
         }

         try
         {
            edgeNormal(convexPolygon2D.size(), convexPolygon2D, convexPolygon2D.size(), clockwiseOrdered, new Vector2D());
            fail("Should have thrown an " + IndexOutOfBoundsException.class.getSimpleName());
         }
         catch (IndexOutOfBoundsException e)
         {
            // good
         }
      }
   }

   @Test
   public void testIsPoint2DInsideConvexPolygon2D() throws Exception
   {
      { // Test examples with single point polygon
         List<Point2D> convexPolygon2D = new ArrayList<>();
         convexPolygon2D.add(new Point2D(1.0, 1.0));
         int hullSize = 1;
         Point2D query = new Point2D();

         query.set(1.0, 1.0);
         assertTrue(isPoint2DInsideConvexPolygon2D(query, convexPolygon2D, hullSize, true, 1.0e-10));

         query.set(0.8, 0.9);
         assertFalse(isPoint2DInsideConvexPolygon2D(query, convexPolygon2D, hullSize, true, 0.0));

         query.set(0.8, 1.1);
         assertTrue(isPoint2DInsideConvexPolygon2D(query, convexPolygon2D, hullSize, true, 0.3));

         query.set(1.0, 0.9);
         assertFalse(isPoint2DInsideConvexPolygon2D(query, convexPolygon2D, hullSize, true, 0.0));

         query.set(2.0, 1.0);
         assertFalse(isPoint2DInsideConvexPolygon2D(query, convexPolygon2D, hullSize, true, 0.0));
         assertTrue(isPoint2DInsideConvexPolygon2D(query, convexPolygon2D, hullSize, true, 1.0));

         query.set(1.0, 2.0);
         assertFalse(isPoint2DInsideConvexPolygon2D(query, convexPolygon2D, hullSize, true, 0.0));
         assertTrue(isPoint2DInsideConvexPolygon2D(query, convexPolygon2D, hullSize, true, 1.0));
      }

      { // Test examples with line polygon
         List<Point2D> convexPolygon2D = new ArrayList<>();
         convexPolygon2D.add(new Point2D(0.0, 0.0));
         convexPolygon2D.add(new Point2D(1.0, 0.0));
         int hullSize = 2;
         Point2D query = new Point2D();
         double epsilon = 1.0e-10;

         query.set(0.1, 0.0);
         assertTrue(isPoint2DInsideConvexPolygon2D(query, convexPolygon2D, hullSize, true, 0.0));

         query.set(0.1, 0.1);
         assertFalse(isPoint2DInsideConvexPolygon2D(query, convexPolygon2D, hullSize, true, epsilon));

         query.set(1.5, 0.0);
         assertFalse(isPoint2DInsideConvexPolygon2D(query, convexPolygon2D, hullSize, true, epsilon));

         query.set(1.0, 0.0);
         assertTrue(isPoint2DInsideConvexPolygon2D(query, convexPolygon2D, hullSize, true, 0.0));

         query.set(1.0, epsilon * 0.1);
         assertFalse(isPoint2DInsideConvexPolygon2D(query, convexPolygon2D, hullSize, true, 0.0));

         query.set(1.0, epsilon * 0.1);
         assertTrue(isPoint2DInsideConvexPolygon2D(query, convexPolygon2D, hullSize, true, epsilon));

         query.set(1.5, 0.0);
         assertTrue(isPoint2DInsideConvexPolygon2D(query, convexPolygon2D, hullSize, true, 0.5));
      }

      { // Test examples with triangle polygon

         List<Point2D> convexPolygon2D = new ArrayList<>();
         convexPolygon2D.add(new Point2D(0.0, 0.0));
         convexPolygon2D.add(new Point2D(3.0, 5.0));
         convexPolygon2D.add(new Point2D(5.0, 0.0));
         int hullSize = 3;
         Point2D query = new Point2D();
         double epsilon = 1.0e-10;

         query.set(0.3, 0.0);
         assertTrue(isPoint2DInsideConvexPolygon2D(query, convexPolygon2D, hullSize, true, epsilon));

         query.set(0.0, 0.0);
         assertTrue(isPoint2DInsideConvexPolygon2D(query, convexPolygon2D, hullSize, true, epsilon));

         query.set(2.0, 2.0);
         assertTrue(isPoint2DInsideConvexPolygon2D(query, convexPolygon2D, hullSize, true, 0.0));

         query.set(1.0, 0.3);
         assertTrue(isPoint2DInsideConvexPolygon2D(query, convexPolygon2D, hullSize, true, epsilon));

         query.set(-1.0, 4.0);
         assertFalse(isPoint2DInsideConvexPolygon2D(query, convexPolygon2D, hullSize, true, epsilon));

         query.set(6.0, 7.0);
         assertFalse(isPoint2DInsideConvexPolygon2D(query, convexPolygon2D, hullSize, true, epsilon));

         query.set(10.0, 0.0);
         assertFalse(isPoint2DInsideConvexPolygon2D(query, convexPolygon2D, hullSize, true, epsilon));

         query.set(0.1, 0.2);
         assertFalse(isPoint2DInsideConvexPolygon2D(query, convexPolygon2D, hullSize, true, 0.0));

         query.set(3.5, 4.9);
         assertFalse(isPoint2DInsideConvexPolygon2D(query, convexPolygon2D, hullSize, true, epsilon));

         query.set(3.5, -1.0);
         assertFalse(isPoint2DInsideConvexPolygon2D(query, convexPolygon2D, hullSize, true, 0.0));
      }

      { // Test using actual data

         List<Point2D> convexPolygon2D = new ArrayList<>();
         convexPolygon2D.add(new Point2D(-0.06, -0.08));
         convexPolygon2D.add(new Point2D(0.14, -0.08));
         convexPolygon2D.add(new Point2D(0.14, -0.19));
         convexPolygon2D.add(new Point2D(-0.06, -0.19));
         int hullSize = inPlaceGiftWrapConvexHull2D(convexPolygon2D);
         Point2D query = new Point2D();

         query.set(0.03, 0.0);
         assertFalse(isPoint2DInsideConvexPolygon2D(query, convexPolygon2D, hullSize, true, 0.02));

         query.set(0.03, -0.09);
         assertTrue(isPoint2DInsideConvexPolygon2D(query, convexPolygon2D, hullSize, true, 0.0));
      }

      Random random = new Random(324534L);

      for (int i = 0; i < ITERATIONS; i++)
      { // Test with epsilon == 0.0
         List<? extends Point2DReadOnly> convexPolygon2D = generateRandomCircleBasedConvexPolygon2D(random, 10.0, 10.0, 100);
         int hullSize = inPlaceGiftWrapConvexHull2D(convexPolygon2D);
         boolean clockwiseOrdered = random.nextBoolean();
         if (!clockwiseOrdered)
            Collections.reverse(convexPolygon2D.subList(0, hullSize));

         Point2D centroid = new Point2D();
         computeConvexPolyong2DArea(convexPolygon2D, hullSize, clockwiseOrdered, centroid);
         int vertexIndex = random.nextInt(hullSize);
         int nextVertexIndex = next(vertexIndex, hullSize);
         Point2DReadOnly vertex = convexPolygon2D.get(vertexIndex);
         Point2DReadOnly nextVertex = convexPolygon2D.get(nextVertexIndex);

         Point2D pointOnEdge = new Point2D();
         pointOnEdge.interpolate(vertex, nextVertex, random.nextDouble());

         double alphaOutside = EuclidCoreRandomTools.generateRandomDouble(random, 1.0, 3.0);
         Point2D outsidePoint = new Point2D();
         outsidePoint.interpolate(centroid, pointOnEdge, alphaOutside);
         assertFalse(isPoint2DInsideConvexPolygon2D(outsidePoint, convexPolygon2D, hullSize, clockwiseOrdered, 0));
         assertFalse(isPoint2DInsideConvexPolygon2D(outsidePoint.getX(), outsidePoint.getY(), convexPolygon2D, hullSize, clockwiseOrdered, 0));

         double alphaInside = EuclidCoreRandomTools.generateRandomDouble(random, 0.0, 1.0);
         Point2D insidePoint = new Point2D();
         insidePoint.interpolate(centroid, pointOnEdge, alphaInside);
         assertTrue(isPoint2DInsideConvexPolygon2D(insidePoint, convexPolygon2D, hullSize, clockwiseOrdered, 0));
         assertTrue(isPoint2DInsideConvexPolygon2D(insidePoint.getX(), insidePoint.getY(), convexPolygon2D, hullSize, clockwiseOrdered, 0));
      }

      for (int i = 0; i < ITERATIONS; i++)
      { // Test with epsilon > 0.0
         double epsilon = random.nextDouble();

         List<? extends Point2DReadOnly> convexPolygon2D = generateRandomCircleBasedConvexPolygon2D(random, 10.0, 10.0, 100);
         int hullSize = inPlaceGiftWrapConvexHull2D(convexPolygon2D);
         boolean clockwiseOrdered = random.nextBoolean();
         if (!clockwiseOrdered)
            Collections.reverse(convexPolygon2D.subList(0, hullSize));

         Point2D centroid = new Point2D();
         computeConvexPolyong2DArea(convexPolygon2D, hullSize, clockwiseOrdered, centroid);
         int vertexIndex = random.nextInt(hullSize);
         int nextVertexIndex = next(vertexIndex, hullSize);
         Point2DReadOnly vertex = convexPolygon2D.get(vertexIndex);
         Point2DReadOnly nextVertex = convexPolygon2D.get(nextVertexIndex);

         Point2D pointOnEdge = new Point2D();
         pointOnEdge.interpolate(vertex, nextVertex, random.nextDouble());

         double distanceOutside = EuclidCoreRandomTools.generateRandomDouble(random, 0.0, epsilon);
         Point2D outsidePoint = new Point2D();
         Vector2D orthogonal = new Vector2D();
         orthogonal.sub(nextVertex, vertex);
         orthogonal.normalize();
         orthogonal = perpendicularVector2D(orthogonal);
         if (!clockwiseOrdered)
            orthogonal.negate();

         outsidePoint.scaleAdd(distanceOutside, orthogonal, pointOnEdge);
         assertTrue(isPoint2DInsideConvexPolygon2D(outsidePoint, convexPolygon2D, hullSize, clockwiseOrdered, epsilon));
         assertTrue(isPoint2DInsideConvexPolygon2D(outsidePoint.getX(), outsidePoint.getY(), convexPolygon2D, hullSize, clockwiseOrdered, epsilon));

         distanceOutside = EuclidCoreRandomTools.generateRandomDouble(random, epsilon, epsilon + 1.0);
         outsidePoint.scaleAdd(distanceOutside, orthogonal, pointOnEdge);
         assertFalse(isPoint2DInsideConvexPolygon2D(outsidePoint, convexPolygon2D, hullSize, clockwiseOrdered, epsilon));
         assertFalse(isPoint2DInsideConvexPolygon2D(outsidePoint.getX(), outsidePoint.getY(), convexPolygon2D, hullSize, clockwiseOrdered, epsilon));

         double alphaInside = EuclidCoreRandomTools.generateRandomDouble(random, 0.0, 1.0);
         Point2D insidePoint = new Point2D();
         insidePoint.interpolate(centroid, pointOnEdge, alphaInside);
         assertTrue(isPoint2DInsideConvexPolygon2D(insidePoint, convexPolygon2D, hullSize, clockwiseOrdered, 0));
         assertTrue(isPoint2DInsideConvexPolygon2D(insidePoint.getX(), insidePoint.getY(), convexPolygon2D, hullSize, clockwiseOrdered, 0));
      }

      for (int i = 0; i < ITERATIONS; i++)
      { // Test with epsilon < 0.0
         double epsilon = -0.02; // Testing with a small value to avoid weird cases that are hard to deal with.

         List<? extends Point2DReadOnly> convexPolygon2D = generateRandomCircleBasedConvexPolygon2D(random, 10.0, 10.0, 100);
         int hullSize = inPlaceGiftWrapConvexHull2D(convexPolygon2D);
         boolean clockwiseOrdered = random.nextBoolean();
         if (!clockwiseOrdered)
            Collections.reverse(convexPolygon2D.subList(0, hullSize));

         Point2D centroid = new Point2D();
         computeConvexPolyong2DArea(convexPolygon2D, hullSize, clockwiseOrdered, centroid);
         int vertexIndex = random.nextInt(hullSize);
         int nextVertexIndex = next(vertexIndex, hullSize);
         Point2DReadOnly vertex = convexPolygon2D.get(vertexIndex);
         Point2DReadOnly nextVertex = convexPolygon2D.get(nextVertexIndex);

         Point2D pointOnEdge = new Point2D();
         pointOnEdge.interpolate(vertex, nextVertex, random.nextDouble());

         double alphaOutside = EuclidCoreRandomTools.generateRandomDouble(random, 1.0, 3.0);
         Point2D outsidePoint = new Point2D();
         outsidePoint.interpolate(centroid, pointOnEdge, alphaOutside);
         assertFalse(isPoint2DInsideConvexPolygon2D(outsidePoint, convexPolygon2D, hullSize, clockwiseOrdered, 0));
         assertFalse(isPoint2DInsideConvexPolygon2D(outsidePoint.getX(), outsidePoint.getY(), convexPolygon2D, hullSize, clockwiseOrdered, 0));

         double distanceInside = EuclidCoreRandomTools.generateRandomDouble(random, epsilon, 0.0);
         Vector2D orthogonal = new Vector2D();
         orthogonal.sub(nextVertex, vertex);
         orthogonal.normalize();
         orthogonal = perpendicularVector2D(orthogonal);
         if (!clockwiseOrdered)
            orthogonal.negate();

         outsidePoint.scaleAdd(distanceInside, orthogonal, pointOnEdge);
         assertFalse(isPoint2DInsideConvexPolygon2D(outsidePoint, convexPolygon2D, hullSize, clockwiseOrdered, epsilon));
         assertFalse(isPoint2DInsideConvexPolygon2D(outsidePoint.getX(), outsidePoint.getY(), convexPolygon2D, hullSize, clockwiseOrdered, epsilon));

         // Using the distance to the centroid as a max
         double distanceBetweenCentroidAndEdge = distanceFromPoint2DToLine2D(centroid, vertex, nextVertex);
         distanceInside = EuclidCoreRandomTools.generateRandomDouble(random, -distanceBetweenCentroidAndEdge, epsilon);
         Point2D insidePoint = new Point2D();
         insidePoint.scaleAdd(distanceInside, orthogonal, pointOnEdge);
         assertTrue(isPoint2DInsideConvexPolygon2D(insidePoint, convexPolygon2D, hullSize, clockwiseOrdered, 0));
         assertTrue(isPoint2DInsideConvexPolygon2D(insidePoint.getX(), insidePoint.getY(), convexPolygon2D, hullSize, clockwiseOrdered, 0));
      }

      {
         List<? extends Point2DReadOnly> convexPolygon2D = generateRandomCircleBasedConvexPolygon2D(random, 10.0, 10.0, 100);
         int hullSize = inPlaceGrahamScanConvexHull2D(convexPolygon2D);
         boolean clockwiseOrdered = random.nextBoolean();
         if (!clockwiseOrdered)
            Collections.reverse(convexPolygon2D.subList(0, hullSize));

         try
         {
            isPoint2DInsideConvexPolygon2D(new Point2D(), convexPolygon2D, -1, clockwiseOrdered, 0);
            fail("Should have thrown an " + IllegalArgumentException.class.getSimpleName());
         }
         catch (IllegalArgumentException e)
         {
            // good
         }

         try
         {
            isPoint2DInsideConvexPolygon2D(new Point2D(), convexPolygon2D, convexPolygon2D.size() + 1, clockwiseOrdered, 0);
            fail("Should have thrown an " + IllegalArgumentException.class.getSimpleName());
         }
         catch (IllegalArgumentException e)
         {
            // good
         }
      }
   }

   @Test
   public void testSignedDistanceFromPoint2DToConvexPolygon2D() throws Exception
   {
      { // Test examples single point polygon
         List<Point2D> convexPolygon2D = new ArrayList<>();
         convexPolygon2D.add(new Point2D(0.0, 0.0));
         int hullSize = 1;
         Point2D query = new Point2D();

         query.set(2.5, 1.0);
         double distance = signedDistanceFromPoint2DToConvexPolygon2D(query, convexPolygon2D, hullSize, true);
         assertEquals(Math.sqrt(2.5 * 2.5 + 1.0 * 1.0), distance, SMALLEST_EPSILON);
      }

      { // Test examples single line polygon
         List<Point2D> convexPolygon2D = new ArrayList<>();
         convexPolygon2D.add(new Point2D(0.0, 0.0));
         convexPolygon2D.add(new Point2D(1.0, 0.0));
         int hullSize = 2;
         Point2D query = new Point2D();

         query.set(2.5, 1.0);
         double distance = signedDistanceFromPoint2DToConvexPolygon2D(query, convexPolygon2D, hullSize, true);
         assertEquals(Math.sqrt(1.5 * 1.5 + 1.0 * 1.0), distance, SMALLEST_EPSILON);

         query.set(0.5, 1.0);
         distance = signedDistanceFromPoint2DToConvexPolygon2D(query, convexPolygon2D, hullSize, true);
         assertEquals(1.0, distance, SMALLEST_EPSILON);
      }

      { // Test examples single line polygon
         List<Point2D> convexPolygon2D = new ArrayList<>();
         convexPolygon2D.add(new Point2D(0.0, 0.0));
         convexPolygon2D.add(new Point2D(10.0, 0.0));
         convexPolygon2D.add(new Point2D(0.0, 10.0));
         int hullSize = inPlaceGiftWrapConvexHull2D(convexPolygon2D);
         Point2D query = new Point2D();

         query.set(10.0, 10.0);
         double distance = signedDistanceFromPoint2DToConvexPolygon2D(query, convexPolygon2D, hullSize, true);
         assertEquals(5.0 * Math.sqrt(2.0), distance, SMALLEST_EPSILON);

         query.set(1.2, 1.1);
         distance = signedDistanceFromPoint2DToConvexPolygon2D(query, convexPolygon2D, hullSize, true);
         assertEquals(-1.1, distance, SMALLEST_EPSILON);

         query.set(0.05, 9.8);
         distance = signedDistanceFromPoint2DToConvexPolygon2D(query, convexPolygon2D, hullSize, true);
         assertEquals(-0.05, distance, SMALLEST_EPSILON);

         query.set(9.8, 0.15);
         distance = signedDistanceFromPoint2DToConvexPolygon2D(query, convexPolygon2D, hullSize, true);
         assertEquals(-0.5 * Math.sqrt(0.05 * 0.05 * 2.0), distance, SMALLEST_EPSILON);

         query.set(5.0, -0.15);
         distance = signedDistanceFromPoint2DToConvexPolygon2D(query, convexPolygon2D, hullSize, true);
         assertEquals(0.15, distance, SMALLEST_EPSILON);

         query.set(15.0, -0.15);
         distance = signedDistanceFromPoint2DToConvexPolygon2D(query, convexPolygon2D, hullSize, true);
         assertEquals(Math.sqrt(5.0 * 5.0 + 0.15 * 0.15), distance, SMALLEST_EPSILON);
      }

      {// Trivial case: Square
         int n = 4;
         List<Point2DReadOnly> clockwiseSquareVertices = new ArrayList<>();
         clockwiseSquareVertices.add(new Point2D(0.0, 1.0));
         clockwiseSquareVertices.add(new Point2D(1.0, 1.0));
         clockwiseSquareVertices.add(new Point2D(1.0, 0.0));
         clockwiseSquareVertices.add(new Point2D(0.0, 0.0));
         List<Point2DReadOnly> counterClockwiseSquareVertices = new ArrayList<>(clockwiseSquareVertices);
         Collections.reverse(counterClockwiseSquareVertices);
         double x, y;
         double expectedDistance, actualDistance;

         for (x = 0.0; x <= 1.0; x += 0.05)
         {
            y = 1.1;
            expectedDistance = 0.1;
            actualDistance = signedDistanceFromPoint2DToConvexPolygon2D(x, y, clockwiseSquareVertices, n, true);
            assertEquals(expectedDistance, actualDistance, SMALLEST_EPSILON);
            actualDistance = signedDistanceFromPoint2DToConvexPolygon2D(x, y, counterClockwiseSquareVertices, n, false);
            assertEquals(expectedDistance, actualDistance, SMALLEST_EPSILON);
         }

         for (x = 0.0; x <= 1.0; x += 0.05)
         {
            y = -0.1;
            expectedDistance = 0.1;
            actualDistance = signedDistanceFromPoint2DToConvexPolygon2D(x, y, clockwiseSquareVertices, n, true);
            assertEquals(expectedDistance, actualDistance, SMALLEST_EPSILON);
            actualDistance = signedDistanceFromPoint2DToConvexPolygon2D(x, y, counterClockwiseSquareVertices, n, false);
            assertEquals(expectedDistance, actualDistance, SMALLEST_EPSILON);
         }

         for (y = 0.0; y <= 1.0; y += 0.05)
         {
            x = 1.1;
            expectedDistance = 0.1;
            actualDistance = signedDistanceFromPoint2DToConvexPolygon2D(x, y, clockwiseSquareVertices, n, true);
            assertEquals(expectedDistance, actualDistance, SMALLEST_EPSILON);
            actualDistance = signedDistanceFromPoint2DToConvexPolygon2D(x, y, counterClockwiseSquareVertices, n, false);
            assertEquals(expectedDistance, actualDistance, SMALLEST_EPSILON);
         }

         for (y = 0.0; y <= 1.0; y += 0.05)
         {
            x = -0.1;
            expectedDistance = 0.1;
            actualDistance = signedDistanceFromPoint2DToConvexPolygon2D(x, y, clockwiseSquareVertices, n, true);
            assertEquals(expectedDistance, actualDistance, SMALLEST_EPSILON);
            actualDistance = signedDistanceFromPoint2DToConvexPolygon2D(x, y, counterClockwiseSquareVertices, n, false);
            assertEquals(expectedDistance, actualDistance, SMALLEST_EPSILON);
         }

         for (x = 0.1; x <= 0.9; x += 0.05)
         {
            y = 0.9;
            expectedDistance = -0.1;
            actualDistance = signedDistanceFromPoint2DToConvexPolygon2D(x, y, clockwiseSquareVertices, n, true);
            assertEquals(expectedDistance, actualDistance, SMALLEST_EPSILON);
            actualDistance = signedDistanceFromPoint2DToConvexPolygon2D(x, y, counterClockwiseSquareVertices, n, false);
            assertEquals(expectedDistance, actualDistance, SMALLEST_EPSILON);
         }

         for (x = 0.1; x <= 0.9; x += 0.05)
         {
            y = 0.1;
            expectedDistance = -0.1;
            actualDistance = signedDistanceFromPoint2DToConvexPolygon2D(x, y, clockwiseSquareVertices, n, true);
            assertEquals(expectedDistance, actualDistance, SMALLEST_EPSILON);
            actualDistance = signedDistanceFromPoint2DToConvexPolygon2D(x, y, counterClockwiseSquareVertices, n, false);
            assertEquals(expectedDistance, actualDistance, SMALLEST_EPSILON);
         }
      }

      // Non-trivial cases
      Random random = new Random(324234L);
      double expectedDistance, actualDistance;

      for (int i = 0; i < ITERATIONS; i++)
      {
         List<? extends Point2DReadOnly> convexPolygon2D = generateRandomCircleBasedConvexPolygon2D(random, 10.0, 10.0, 100);
         int hullSize = inPlaceGiftWrapConvexHull2D(convexPolygon2D);
         boolean clockwiseOrdered = random.nextBoolean();
         if (!clockwiseOrdered)
            Collections.reverse(convexPolygon2D.subList(0, hullSize));

         Point2D centroid = new Point2D();
         computeConvexPolyong2DArea(convexPolygon2D, hullSize, clockwiseOrdered, centroid);
         int vertexIndex = random.nextInt(hullSize);
         int nextVertexIndex = next(vertexIndex, hullSize);
         Point2DReadOnly vertex = convexPolygon2D.get(vertexIndex);
         Point2DReadOnly nextVertex = convexPolygon2D.get(nextVertexIndex);

         Point2D pointOnEdge = new Point2D();
         pointOnEdge.interpolate(vertex, nextVertex, random.nextDouble());

         double alphaOutside = EuclidCoreRandomTools.generateRandomDouble(random, 1.0, 3.0);
         Point2D outsidePoint = new Point2D();
         outsidePoint.interpolate(centroid, pointOnEdge, alphaOutside);
         expectedDistance = distanceFromPoint2DToLineSegment2D(outsidePoint, vertex, nextVertex);
         actualDistance = signedDistanceFromPoint2DToConvexPolygon2D(outsidePoint, convexPolygon2D, hullSize, clockwiseOrdered);
         assertEquals("EdgeLength = " + vertex.distance(nextVertex), expectedDistance, actualDistance, SMALLEST_EPSILON);

         double alphaInside = EuclidCoreRandomTools.generateRandomDouble(random, 0.0, 1.0);
         Point2D insidePoint = new Point2D();
         insidePoint.interpolate(centroid, pointOnEdge, alphaInside);

         expectedDistance = Double.POSITIVE_INFINITY;

         for (int j = 0; j < hullSize; j++)
         {
            Point2DReadOnly edgeStart = convexPolygon2D.get(j);
            Point2DReadOnly edgeEnd = convexPolygon2D.get(next(j, hullSize));
            expectedDistance = Math.min(expectedDistance, distanceFromPoint2DToLineSegment2D(insidePoint, edgeStart, edgeEnd));
         }

         expectedDistance = -expectedDistance;
         actualDistance = signedDistanceFromPoint2DToConvexPolygon2D(insidePoint, convexPolygon2D, hullSize, clockwiseOrdered);
         assertEquals("EdgeLength = " + vertex.distance(nextVertex), expectedDistance, actualDistance, SMALLEST_EPSILON);
      }

      { // Test exceptions
         List<? extends Point2DReadOnly> convexPolygon2D = generateRandomCircleBasedConvexPolygon2D(random, 10.0, 10.0, 100);
         int hullSize = inPlaceGrahamScanConvexHull2D(convexPolygon2D);
         boolean clockwiseOrdered = random.nextBoolean();
         if (!clockwiseOrdered)
            Collections.reverse(convexPolygon2D.subList(0, hullSize));

         try
         {
            signedDistanceFromPoint2DToConvexPolygon2D(new Point2D(), convexPolygon2D, -1, clockwiseOrdered);
            fail("Should have thrown an " + IllegalArgumentException.class.getSimpleName());
         }
         catch (IllegalArgumentException e)
         {
            // good
         }

         try
         {
            signedDistanceFromPoint2DToConvexPolygon2D(new Point2D(), convexPolygon2D, convexPolygon2D.size() + 1, clockwiseOrdered);
            fail("Should have thrown an " + IllegalArgumentException.class.getSimpleName());
         }
         catch (IllegalArgumentException e)
         {
            // good
         }
      }

      { // Test with empty polygon
         double distance = signedDistanceFromPoint2DToConvexPolygon2D(0, 0, Collections.emptyList(), 0, true);
         assertTrue(Double.isNaN(distance));
      }

      { // Test with a single vertex polygon
         Point2D query = generateRandomPoint2D(random, 10.0);
         Point2D vertex = generateRandomPoint2D(random, 10.0);
         expectedDistance = query.distance(vertex);
         actualDistance = signedDistanceFromPoint2DToConvexPolygon2D(query, Collections.singletonList(vertex), 1, true);
         assertEquals(expectedDistance, actualDistance, SMALLEST_EPSILON);
         actualDistance = signedDistanceFromPoint2DToConvexPolygon2D(query, Collections.singletonList(vertex), 1, false);
         assertEquals(expectedDistance, actualDistance, SMALLEST_EPSILON);
      }

      { // Test with a two vertices polygon
         Point2D query = generateRandomPoint2D(random, 10.0);
         Point2D vertex0 = generateRandomPoint2D(random, 10.0);
         Point2D vertex1 = generateRandomPoint2D(random, 10.0);

         List<Point2D> points = new ArrayList<>();
         points.add(vertex0);
         points.add(vertex1);
         expectedDistance = distanceFromPoint2DToLineSegment2D(query, vertex0, vertex1);
         actualDistance = signedDistanceFromPoint2DToConvexPolygon2D(query, points, 2, true);
         assertEquals(expectedDistance, actualDistance, SMALLEST_EPSILON);
         actualDistance = signedDistanceFromPoint2DToConvexPolygon2D(query, points, 2, false);
         assertEquals(expectedDistance, actualDistance, SMALLEST_EPSILON);
      }
   }

   @Test
   public void testIntersectionBetweenLine2DAndConvexPolygon2D() throws Exception
   {
      Random random = new Random(234324L);

      for (int i = 0; i < ITERATIONS; i++)
      { // Setup: 2 intersections picked at random on the polygon from which the line can be built.
         List<? extends Point2DReadOnly> convexPolygon2D = generateRandomPointCloud2D(random, 10.0, 10.0, 100);
         int hullSize = inPlaceGrahamScanConvexHull2D(convexPolygon2D);
         boolean clockwiseOrdered = random.nextBoolean();
         if (!clockwiseOrdered)
            Collections.reverse(convexPolygon2D.subList(0, hullSize));

         int firstEdgeIndex = random.nextInt(hullSize);
         int secondEdgeIndex = wrap(random.nextInt(hullSize - 1) + firstEdgeIndex + 1, hullSize); // Making sure the two edges are different

         Point2D expectedFirstIntersection = new Point2D();
         {
            Point2DReadOnly vertex = convexPolygon2D.get(firstEdgeIndex);
            Point2DReadOnly nextVertex = convexPolygon2D.get(next(firstEdgeIndex, hullSize));
            expectedFirstIntersection.interpolate(vertex, nextVertex, random.nextDouble());
         }
         Point2D expectedSecondIntersection = new Point2D();
         {
            Point2DReadOnly vertex = convexPolygon2D.get(secondEdgeIndex);
            Point2DReadOnly nextVertex = convexPolygon2D.get(next(secondEdgeIndex, hullSize));
            expectedSecondIntersection.interpolate(vertex, nextVertex, random.nextDouble());
         }

         Point2D pointOnLine = new Point2D();
         pointOnLine.interpolate(expectedFirstIntersection, expectedSecondIntersection, EuclidCoreRandomTools.generateRandomDouble(random, 10.0));
         Vector2D lineDirection = new Vector2D();
         lineDirection.sub(expectedSecondIntersection, expectedFirstIntersection);
         lineDirection.normalize();
         lineDirection.scale(EuclidCoreRandomTools.generateRandomDouble(random, 10.0));

         Point2D actualFirstIntersection = new Point2D();
         Point2D actualSecondIntersection = new Point2D();
         int numberOfIntersections = intersectionBetweenLine2DAndConvexPolygon2D(pointOnLine, lineDirection, convexPolygon2D, hullSize, clockwiseOrdered,
                                                                                 actualFirstIntersection, actualSecondIntersection);
         assertEquals(2, numberOfIntersections);

         if (expectedFirstIntersection.distance(actualFirstIntersection) < expectedFirstIntersection.distance(actualSecondIntersection))
         {
            EuclidCoreTestTools.assertTuple2DEquals(expectedFirstIntersection, actualFirstIntersection, SMALL_EPSILON);
            EuclidCoreTestTools.assertTuple2DEquals(expectedSecondIntersection, actualSecondIntersection, SMALL_EPSILON);
         }
         else
         {
            EuclidCoreTestTools.assertTuple2DEquals(expectedFirstIntersection, actualSecondIntersection, SMALL_EPSILON);
            EuclidCoreTestTools.assertTuple2DEquals(expectedSecondIntersection, actualFirstIntersection, SMALL_EPSILON);
         }
      }

      for (int i = 0; i < ITERATIONS; i++)
      { // Setup: the line is collinear to an edge picked at random
         List<? extends Point2DReadOnly> convexPolygon2D = generateRandomPointCloud2D(random, 10.0, 10.0, 100);
         int hullSize = inPlaceGrahamScanConvexHull2D(convexPolygon2D);
         boolean clockwiseOrdered = random.nextBoolean();
         if (!clockwiseOrdered)
            Collections.reverse(convexPolygon2D.subList(0, hullSize));

         int edgeIndex = random.nextInt(hullSize);
         Point2DReadOnly vertex = convexPolygon2D.get(edgeIndex);
         Point2DReadOnly nextVertex = convexPolygon2D.get(next(edgeIndex, hullSize));

         Point2D pointOnLine = new Point2D();
         pointOnLine.interpolate(vertex, nextVertex, EuclidCoreRandomTools.generateRandomDouble(random, 10.0));
         Vector2D lineDirection = new Vector2D();
         lineDirection.sub(nextVertex, vertex);
         lineDirection.normalize();
         lineDirection.scale(EuclidCoreRandomTools.generateRandomDouble(random, 10.0));

         Point2D actualFirstIntersection = new Point2D();
         Point2D actualSecondIntersection = new Point2D();
         int numberOfIntersections = intersectionBetweenLine2DAndConvexPolygon2D(pointOnLine, lineDirection, convexPolygon2D, hullSize, clockwiseOrdered,
                                                                                 actualFirstIntersection, actualSecondIntersection);
         assertEquals("Iteration: " + i, 2, numberOfIntersections);

         if (vertex.distance(actualFirstIntersection) < vertex.distance(actualSecondIntersection))
         {
            EuclidCoreTestTools.assertTuple2DEquals(vertex, actualFirstIntersection, SMALL_EPSILON);
            EuclidCoreTestTools.assertTuple2DEquals(nextVertex, actualSecondIntersection, SMALL_EPSILON);
         }
         else
         {
            EuclidCoreTestTools.assertTuple2DEquals(vertex, actualSecondIntersection, SMALL_EPSILON);
            EuclidCoreTestTools.assertTuple2DEquals(nextVertex, actualFirstIntersection, SMALL_EPSILON);
         }
      }

      for (int i = 0; i < ITERATIONS; i++)
      { // (somewhat tricky) Setup: build a line that does NOT intersect with the polygon
         /* 
          * @formatter:off
          * - The goal is to build the query line such that it does not intersect with the polygon.
          * - Pick two successive vertices: v0 = convexPolygon2D.get(i) and vn1 = convexPolygon2D.get(i+1).
          * - Draw 2 lines going from the centroid through each vertex, they are called extrapolation lines.
          * - For the line going through v0, find the intersection v0Max with the line going through vn1 and vn2 = convexPolygon2D.get(i+2).
          *    The first point defining the query line should be between v0 and v0Max such that the line won't intersect with the edge (vn1, vn2).
          * - For the line going through vn1, find the intersection vn1Max with the line going through v0 and vp1 = convexPolygon2D.get(i-1).
          *    The second point defining the query line should be between vn1 and vn1Max such that the line won't intersect with the edge (v0, vp1).
          * @formatter:on
          */
         List<? extends Point2DReadOnly> convexPolygon2D = generateRandomPointCloud2D(random, 10.0, 10.0, 100);
         int hullSize = inPlaceGrahamScanConvexHull2D(convexPolygon2D);
         boolean clockwiseOrdered = random.nextBoolean();
         if (!clockwiseOrdered)
            Collections.reverse(convexPolygon2D.subList(0, hullSize));

         int v0Index = random.nextInt(hullSize);
         int vn1Index = next(v0Index, hullSize);
         int vn2Index = next(vn1Index, hullSize);
         int vp1Index = previous(v0Index, hullSize);

         Point2DReadOnly v0 = convexPolygon2D.get(v0Index);
         Point2DReadOnly vn1 = convexPolygon2D.get(vn1Index);
         Point2DReadOnly vn2 = convexPolygon2D.get(vn2Index);
         Point2DReadOnly vp1 = convexPolygon2D.get(vp1Index);

         Point2D centroid = new Point2D();
         computeConvexPolyong2DArea(convexPolygon2D, hullSize, clockwiseOrdered, centroid);

         Point2D v0Max = intersectionBetweenTwoLine2Ds(centroid, v0, vn1, vn2);
         Vector2D extrapolationDirection = new Vector2D();
         extrapolationDirection.sub(v0, centroid);

         if (!isPoint2DInFrontOfRay2D(v0Max, centroid, extrapolationDirection))
            v0Max.scaleAdd(10.0, extrapolationDirection, v0);

         Point2D vn1Max = intersectionBetweenTwoLine2Ds(centroid, vn1, v0, vp1);
         extrapolationDirection.sub(vn1, centroid);

         if (!isPoint2DInFrontOfRay2D(vn1Max, centroid, extrapolationDirection))
            vn1Max.scaleAdd(10.0, extrapolationDirection, vn1);

         Point2D firstExtrapolatedPoint = new Point2D();
         Point2D secondExtrapolatedPoint = new Point2D();

         firstExtrapolatedPoint.interpolate(v0, v0Max, generateRandomDouble(random, 0.0, 1.0));
         secondExtrapolatedPoint.interpolate(vn1, vn1Max, generateRandomDouble(random, 0.0, 1.0));

         Point2D pointOnLine = new Point2D();
         pointOnLine.interpolate(firstExtrapolatedPoint, secondExtrapolatedPoint, generateRandomDouble(random, 10.0));
         Vector2D lineDirection = new Vector2D();
         lineDirection.sub(secondExtrapolatedPoint, firstExtrapolatedPoint);
         lineDirection.normalize();
         lineDirection.scale(generateRandomDouble(random, 10.0));

         Point2D actualFirstIntersection = new Point2D();
         Point2D actualSecondIntersection = new Point2D();
         int numberOfIntersections = intersectionBetweenLine2DAndConvexPolygon2D(pointOnLine, lineDirection, convexPolygon2D, hullSize, clockwiseOrdered,
                                                                                 actualFirstIntersection, actualSecondIntersection);
         assertEquals("Iteration: " + i, 0, numberOfIntersections);
      }

      for (int i = 0; i < ITERATIONS; i++)
      { // Setup: intersection at a vertex
         List<? extends Point2DReadOnly> convexPolygon2D = generateRandomPointCloud2D(random, 10.0, 10.0, 100);
         int hullSize = inPlaceGrahamScanConvexHull2D(convexPolygon2D);
         boolean clockwiseOrdered = random.nextBoolean();
         if (!clockwiseOrdered)
            Collections.reverse(convexPolygon2D.subList(0, hullSize));

         int vertexIndex = random.nextInt(hullSize);
         Point2DReadOnly vertex = convexPolygon2D.get(vertexIndex);
         Point2DReadOnly nextVertex = convexPolygon2D.get(next(vertexIndex, hullSize));
         Point2DReadOnly previousVertex = convexPolygon2D.get(previous(vertexIndex, hullSize));

         Vector2D previousEdgeDirection = new Vector2D();
         previousEdgeDirection.sub(vertex, previousVertex);
         Vector2D nextEdgeDirection = new Vector2D();
         nextEdgeDirection.sub(nextVertex, vertex);

         // The line direction has to be between the direction of the previous and next edge.
         Vector2D lineDirection = new Vector2D();
         lineDirection.interpolate(previousEdgeDirection, nextEdgeDirection, generateRandomDouble(random, 0.0, 1.0));
         lineDirection.normalize();
         Point2D pointOnLine = new Point2D();
         pointOnLine.scaleAdd(generateRandomDouble(random, 10.0), lineDirection, vertex);
         lineDirection.scale(generateRandomDouble(random, 10.0));

         Point2D actualFirstIntersection = new Point2D();
         Point2D actualSecondIntersection = new Point2D();
         int numberOfIntersections = intersectionBetweenLine2DAndConvexPolygon2D(pointOnLine, lineDirection, convexPolygon2D, hullSize, clockwiseOrdered,
                                                                                 actualFirstIntersection, actualSecondIntersection);
         assertEquals("Iteration: " + i, 1, numberOfIntersections);
         EuclidCoreTestTools.assertTuple2DEquals(vertex, actualFirstIntersection, SMALL_EPSILON);
         EuclidCoreTestTools.assertTuple2DEquals(vertex, actualSecondIntersection, SMALL_EPSILON);
      }

      for (int i = 0; i < ITERATIONS; i++)
      { // Setup: make the line go exactly through one of the vertices
         List<? extends Point2DReadOnly> convexPolygon2D = generateRandomPointCloud2D(random, 10.0, 10.0, 100);
         int hullSize = inPlaceGrahamScanConvexHull2D(convexPolygon2D);
         boolean clockwiseOrdered = random.nextBoolean();
         if (!clockwiseOrdered)
            Collections.reverse(convexPolygon2D.subList(0, hullSize));

         int vertexIndex = random.nextInt(hullSize);
         Point2DReadOnly vertex = convexPolygon2D.get(vertexIndex);
         Point2D centroid = new Point2D();
         computeConvexPolyong2DArea(convexPolygon2D, hullSize, clockwiseOrdered, centroid);

         Point2D pointOnLine = new Point2D(centroid);
         Vector2D lineDirection = new Vector2D();
         lineDirection.sub(pointOnLine, vertex);

         Point2D actualFirstIntersection = new Point2D();
         Point2D actualSecondIntersection = new Point2D();

         int nIntersections = intersectionBetweenLine2DAndConvexPolygon2D(pointOnLine, lineDirection, convexPolygon2D, hullSize, clockwiseOrdered,
                                                                          actualFirstIntersection, actualSecondIntersection);
         assertEquals("Iteration: " + i, 2, nIntersections);
         if (vertex.distance(actualFirstIntersection) < vertex.distance(actualSecondIntersection))
            EuclidCoreTestTools.assertTuple2DEquals(vertex, actualFirstIntersection, SMALL_EPSILON);
         else
            EuclidCoreTestTools.assertTuple2DEquals(vertex, actualSecondIntersection, SMALL_EPSILON);
      }

      { // Test exceptions
         List<? extends Point2DReadOnly> convexPolygon2D = generateRandomCircleBasedConvexPolygon2D(random, 10.0, 10.0, 100);
         int hullSize = inPlaceGrahamScanConvexHull2D(convexPolygon2D);
         boolean clockwiseOrdered = random.nextBoolean();
         if (!clockwiseOrdered)
            Collections.reverse(convexPolygon2D.subList(0, hullSize));

         try
         {
            intersectionBetweenLine2DAndConvexPolygon2D(new Point2D(), new Vector2D(), convexPolygon2D, -1, clockwiseOrdered, new Point2D(), new Point2D());
            fail("Should have thrown an " + IllegalArgumentException.class.getSimpleName());
         }
         catch (IllegalArgumentException e)
         {
            // good
         }

         try
         {
            intersectionBetweenLine2DAndConvexPolygon2D(new Point2D(), new Vector2D(), convexPolygon2D, convexPolygon2D.size() + 1, clockwiseOrdered,
                                                        new Point2D(), new Point2D());
            fail("Should have thrown an " + IllegalArgumentException.class.getSimpleName());
         }
         catch (IllegalArgumentException e)
         {
            // good
         }
      }

      { // Test with empty polygon
         List<? extends Point2DReadOnly> convexPolygon2D = generateRandomCircleBasedConvexPolygon2D(random, 10.0, 10.0, 100);
         int hullSize = inPlaceGrahamScanConvexHull2D(convexPolygon2D);
         boolean clockwiseOrdered = random.nextBoolean();
         if (!clockwiseOrdered)
            Collections.reverse(convexPolygon2D.subList(0, hullSize));

         Point2D pointOnLine = generateRandomPoint2D(random);
         Vector2D lineDirection = generateRandomVector2D(random);

         int expectedNumberOfIntersections = 0;
         int actualNumberOfIntersections = intersectionBetweenLine2DAndConvexPolygon2D(pointOnLine, lineDirection, convexPolygon2D, 0, clockwiseOrdered,
                                                                                       new Point2D(), new Point2D());
         assertEquals(expectedNumberOfIntersections, actualNumberOfIntersections);
      }

      for (int i = 0; i < ITERATIONS; i++)
      { // Test with single vertex polygon
         Point2D vertex = generateRandomPoint2D(random);
         List<? extends Point2DReadOnly> convexPolygon2D = Collections.singletonList(vertex);
         Point2D pointOnLine = generateRandomPoint2D(random, 10.0);
         Vector2D lineDirection = generateRandomVector2D(random);

         Point2D firstIntersection = new Point2D(Double.NaN, Double.NaN);
         Point2D secondIntersection = new Point2D(Double.NaN, Double.NaN);

         boolean clockwiseOrdered = random.nextBoolean();
         int numberOfIntersections = intersectionBetweenLine2DAndConvexPolygon2D(pointOnLine, lineDirection, convexPolygon2D, 1, clockwiseOrdered,
                                                                                 firstIntersection, secondIntersection);
         assertEquals(0, numberOfIntersections);
         EuclidCoreTestTools.assertTuple2DContainsOnlyNaN(firstIntersection);
         EuclidCoreTestTools.assertTuple2DContainsOnlyNaN(secondIntersection);

         lineDirection.sub(pointOnLine, vertex);
         lineDirection.scale(generateRandomDouble(random, 10.0));
         numberOfIntersections = intersectionBetweenLine2DAndConvexPolygon2D(pointOnLine, lineDirection, convexPolygon2D, 1, clockwiseOrdered,
                                                                             firstIntersection, secondIntersection);
         assertEquals(1, numberOfIntersections);
         EuclidCoreTestTools.assertTuple2DEquals(vertex, firstIntersection, SMALLEST_EPSILON);
         EuclidCoreTestTools.assertTuple2DContainsOnlyNaN(secondIntersection);
      }
   }

   @Test
   public void testIntersectionBetweenLineSegment2DAndConvexPolygon2D() throws Exception
   {
      Random random = new Random(234324L);

      for (int i = 0; i < ITERATIONS; i++)
      { // Setup: 2 intersections picked at random on the polygon from which the line segment can be built to test 0, 1, and 2 intersections just by moving the end points on the same line.
         List<? extends Point2DReadOnly> convexPolygon2D = generateRandomPointCloud2D(random, 10.0, 10.0, 100);
         int hullSize = inPlaceGrahamScanConvexHull2D(convexPolygon2D);
         boolean clockwiseOrdered = random.nextBoolean();
         if (!clockwiseOrdered)
            Collections.reverse(convexPolygon2D.subList(0, hullSize));

         int firstEdgeIndex = random.nextInt(hullSize);
         int secondEdgeIndex = wrap(random.nextInt(hullSize - 1) + firstEdgeIndex + 1, hullSize); // Making sure the two edges are different

         double alphaFirst = random.nextDouble();
         Point2D expectedFirstIntersection = new Point2D();
         {
            Point2DReadOnly vertex = convexPolygon2D.get(firstEdgeIndex);
            Point2DReadOnly nextVertex = convexPolygon2D.get(next(firstEdgeIndex, hullSize));
            expectedFirstIntersection.interpolate(vertex, nextVertex, alphaFirst);
         }
         double alphaSecond = random.nextDouble();
         Point2D expectedSecondIntersection = new Point2D();
         {
            Point2DReadOnly vertex = convexPolygon2D.get(secondEdgeIndex);
            Point2DReadOnly nextVertex = convexPolygon2D.get(next(secondEdgeIndex, hullSize));
            expectedSecondIntersection.interpolate(vertex, nextVertex, alphaSecond);
         }

         Vector2D lineDirection = new Vector2D();
         lineDirection.sub(expectedSecondIntersection, expectedFirstIntersection);
         lineDirection.normalize();

         Point2D actualFirstIntersection = new Point2D();
         Point2D actualSecondIntersection = new Point2D();
         Point2D lineSegmentStart = new Point2D();
         Point2D lineSegmentEnd = new Point2D();

         // Make the line-segment endpoints such that we have 2 intersections
         double alphaOutsideStart = generateRandomDouble(random, -10.0, -0.01);
         double alphaOutsideEnd = generateRandomDouble(random, 1.01, 10.0);
         lineSegmentStart.interpolate(expectedFirstIntersection, expectedSecondIntersection, alphaOutsideStart);
         lineSegmentEnd.interpolate(expectedFirstIntersection, expectedSecondIntersection, alphaOutsideEnd);

         int numberOfIntersections = intersectionBetweenLineSegment2DAndConvexPolygon2D(lineSegmentStart, lineSegmentEnd, convexPolygon2D, hullSize,
                                                                                        clockwiseOrdered, actualFirstIntersection, actualSecondIntersection);
         assertEquals(2, numberOfIntersections);

         if (expectedFirstIntersection.distance(actualFirstIntersection) < expectedFirstIntersection.distance(actualSecondIntersection))
         {
            EuclidCoreTestTools.assertTuple2DEquals(expectedFirstIntersection, actualFirstIntersection, SMALL_EPSILON);
            EuclidCoreTestTools.assertTuple2DEquals(expectedSecondIntersection, actualSecondIntersection, SMALL_EPSILON);
         }
         else
         {
            EuclidCoreTestTools.assertTuple2DEquals(expectedFirstIntersection, actualSecondIntersection, SMALL_EPSILON);
            EuclidCoreTestTools.assertTuple2DEquals(expectedSecondIntersection, actualFirstIntersection, SMALL_EPSILON);
         }

         // Make the line-segment endpoints such that we have 0 intersection
         double alphaStartInside = generateRandomDouble(random, 0.0, 1.0);
         double alphaEndInside = generateRandomDouble(random, 0.0, 1.0);
         lineSegmentStart.interpolate(expectedFirstIntersection, expectedSecondIntersection, alphaStartInside);
         lineSegmentEnd.interpolate(expectedFirstIntersection, expectedSecondIntersection, alphaEndInside);

         numberOfIntersections = intersectionBetweenLineSegment2DAndConvexPolygon2D(lineSegmentStart, lineSegmentEnd, convexPolygon2D, hullSize,
                                                                                    clockwiseOrdered, actualFirstIntersection, actualSecondIntersection);
         assertEquals(0, numberOfIntersections);

         // Make the line-segment endpoints such that we have 1 intersection (two ways to test)
         lineSegmentStart.interpolate(expectedFirstIntersection, expectedSecondIntersection, alphaOutsideStart);
         lineSegmentEnd.interpolate(expectedFirstIntersection, expectedSecondIntersection, generateRandomDouble(random, 0.0, 1.0));

         numberOfIntersections = intersectionBetweenLineSegment2DAndConvexPolygon2D(lineSegmentStart, lineSegmentEnd, convexPolygon2D, hullSize,
                                                                                    clockwiseOrdered, actualFirstIntersection, actualSecondIntersection);
         assertEquals(1, numberOfIntersections);

         if (actualFirstIntersection.distance(expectedFirstIntersection) < actualFirstIntersection.distance(expectedSecondIntersection))
            EuclidCoreTestTools.assertTuple2DEquals(expectedFirstIntersection, actualFirstIntersection, SMALL_EPSILON);
         else
            EuclidCoreTestTools.assertTuple2DEquals(expectedSecondIntersection, actualFirstIntersection, SMALL_EPSILON);

         lineSegmentStart.interpolate(expectedFirstIntersection, expectedSecondIntersection, generateRandomDouble(random, 0.0, 1.0));
         lineSegmentEnd.interpolate(expectedFirstIntersection, expectedSecondIntersection, alphaOutsideEnd);

         numberOfIntersections = intersectionBetweenLineSegment2DAndConvexPolygon2D(lineSegmentStart, lineSegmentEnd, convexPolygon2D, hullSize,
                                                                                    clockwiseOrdered, actualFirstIntersection, actualSecondIntersection);
         assertEquals(1, numberOfIntersections);

         if (actualFirstIntersection.distance(expectedFirstIntersection) < actualFirstIntersection.distance(expectedSecondIntersection))
            EuclidCoreTestTools.assertTuple2DEquals(expectedFirstIntersection, actualFirstIntersection, SMALL_EPSILON);
         else
            EuclidCoreTestTools.assertTuple2DEquals(expectedSecondIntersection, actualFirstIntersection, SMALL_EPSILON);
      }

      for (int i = 0; i < ITERATIONS; i++)
      { // Setup: the line is collinear to an edge picked at random
         List<? extends Point2DReadOnly> convexPolygon2D = generateRandomPointCloud2D(random, 10.0, 10.0, 100);
         int hullSize = inPlaceGrahamScanConvexHull2D(convexPolygon2D);
         boolean clockwiseOrdered = random.nextBoolean();
         if (!clockwiseOrdered)
            Collections.reverse(convexPolygon2D.subList(0, hullSize));

         int edgeIndex = random.nextInt(hullSize);
         Point2DReadOnly vertex = convexPolygon2D.get(edgeIndex);
         Point2DReadOnly nextVertex = convexPolygon2D.get(next(edgeIndex, hullSize));

         Point2D pointOnLine = new Point2D();
         pointOnLine.interpolate(vertex, nextVertex, EuclidCoreRandomTools.generateRandomDouble(random, 10.0));
         Vector2D lineDirection = new Vector2D();
         lineDirection.sub(nextVertex, vertex);
         lineDirection.normalize();
         lineDirection.scale(EuclidCoreRandomTools.generateRandomDouble(random, 10.0));

         Point2D actualFirstIntersection = new Point2D();
         Point2D actualSecondIntersection = new Point2D();

         Point2D lineSegmentStart = new Point2D();
         Point2D lineSegmentEnd = new Point2D();

         // Make the line-segment completely overlap the edge
         double alphaStart = generateRandomDouble(random, -10.0, -0.01);
         double alphaEnd = generateRandomDouble(random, 1.01, 10.0);
         lineSegmentStart.interpolate(vertex, nextVertex, alphaStart);
         lineSegmentEnd.interpolate(vertex, nextVertex, alphaEnd);

         int numberOfIntersections = intersectionBetweenLineSegment2DAndConvexPolygon2D(lineSegmentStart, lineSegmentEnd, convexPolygon2D, hullSize,
                                                                                        clockwiseOrdered, actualFirstIntersection, actualSecondIntersection);
         assertEquals("Iteration: " + i, 2, numberOfIntersections);

         if (vertex.distance(actualFirstIntersection) < vertex.distance(actualSecondIntersection))
         {
            EuclidCoreTestTools.assertTuple2DEquals(vertex, actualFirstIntersection, SMALL_EPSILON);
            EuclidCoreTestTools.assertTuple2DEquals(nextVertex, actualSecondIntersection, SMALL_EPSILON);
         }
         else
         {
            EuclidCoreTestTools.assertTuple2DEquals(vertex, actualSecondIntersection, SMALL_EPSILON);
            EuclidCoreTestTools.assertTuple2DEquals(nextVertex, actualFirstIntersection, SMALL_EPSILON);
         }

         // Make the line-segment not overlap the edge (two sides to test)
         lineSegmentStart.interpolate(vertex, nextVertex, generateRandomDouble(random, -10.0, 0.0));
         lineSegmentEnd.interpolate(vertex, nextVertex, generateRandomDouble(random, -10.0, 0.0));
         numberOfIntersections = intersectionBetweenLineSegment2DAndConvexPolygon2D(lineSegmentStart, lineSegmentEnd, convexPolygon2D, hullSize,
                                                                                    clockwiseOrdered, actualFirstIntersection, actualSecondIntersection);
         assertEquals("Iteration: " + i, 0, numberOfIntersections);

         lineSegmentStart.interpolate(vertex, nextVertex, generateRandomDouble(random, 1.0, 10.0));
         lineSegmentEnd.interpolate(vertex, nextVertex, generateRandomDouble(random, 1.0, 10.0));
         numberOfIntersections = intersectionBetweenLineSegment2DAndConvexPolygon2D(lineSegmentStart, lineSegmentEnd, convexPolygon2D, hullSize,
                                                                                    clockwiseOrdered, actualFirstIntersection, actualSecondIntersection);
         assertEquals("Iteration: " + i, 0, numberOfIntersections);

         // Make the line-segment partially overlap (two sides to test)
         lineSegmentStart.interpolate(vertex, nextVertex, generateRandomDouble(random, -10.0, 0.0));
         lineSegmentEnd.interpolate(vertex, nextVertex, generateRandomDouble(random, 0.0, 1.0));

         numberOfIntersections = intersectionBetweenLineSegment2DAndConvexPolygon2D(lineSegmentStart, lineSegmentEnd, convexPolygon2D, hullSize,
                                                                                    clockwiseOrdered, actualFirstIntersection, actualSecondIntersection);
         assertEquals("Iteration: " + i, 1, numberOfIntersections);

         if (actualFirstIntersection.distance(vertex) < actualFirstIntersection.distance(nextVertex))
            EuclidCoreTestTools.assertTuple2DEquals(vertex, actualFirstIntersection, SMALL_EPSILON);
         else
            EuclidCoreTestTools.assertTuple2DEquals(nextVertex, actualFirstIntersection, SMALL_EPSILON);

         // Test second side
         lineSegmentStart.interpolate(vertex, nextVertex, generateRandomDouble(random, 0.0, 1.0));
         lineSegmentEnd.interpolate(vertex, nextVertex, generateRandomDouble(random, 1.0, 10.0));

         numberOfIntersections = intersectionBetweenLineSegment2DAndConvexPolygon2D(lineSegmentStart, lineSegmentEnd, convexPolygon2D, hullSize,
                                                                                    clockwiseOrdered, actualFirstIntersection, actualSecondIntersection);
         assertEquals("Iteration: " + i, 1, numberOfIntersections);

         if (actualFirstIntersection.distance(vertex) < actualFirstIntersection.distance(nextVertex))
            EuclidCoreTestTools.assertTuple2DEquals(vertex, actualFirstIntersection, SMALL_EPSILON);
         else
            EuclidCoreTestTools.assertTuple2DEquals(nextVertex, actualFirstIntersection, SMALL_EPSILON);
      }

      for (int i = 0; i < ITERATIONS; i++)
      { // Setup: make the line-segment start from the centroid and go exactly through one of the vertices
         List<? extends Point2DReadOnly> convexPolygon2D = generateRandomPointCloud2D(random, 10.0, 10.0, 100);
         int hullSize = inPlaceGrahamScanConvexHull2D(convexPolygon2D);
         boolean clockwiseOrdered = random.nextBoolean();
         if (!clockwiseOrdered)
            Collections.reverse(convexPolygon2D.subList(0, hullSize));

         int vertexIndex = random.nextInt(hullSize);
         Point2DReadOnly vertex = convexPolygon2D.get(vertexIndex);
         Point2D centroid = new Point2D();
         computeConvexPolyong2DArea(convexPolygon2D, hullSize, clockwiseOrdered, centroid);

         Point2D lineSegmentStart = new Point2D(centroid);
         Vector2D lineSegmentDirection = new Vector2D();
         lineSegmentDirection.sub(vertex, lineSegmentStart);
         lineSegmentDirection.scale(1.5);
         Point2D lineSegmentEnd = new Point2D();
         lineSegmentEnd.add(lineSegmentStart, lineSegmentDirection);

         Point2D actualFirstIntersection = new Point2D();
         Point2D actualSecondIntersection = new Point2D();

         int nIntersections = intersectionBetweenLineSegment2DAndConvexPolygon2D(lineSegmentStart, lineSegmentEnd, convexPolygon2D, hullSize, clockwiseOrdered,
                                                                                 actualFirstIntersection, actualSecondIntersection);
         assertEquals("Iteration: " + i, 1, nIntersections);
         EuclidCoreTestTools.assertTuple2DEquals(vertex, actualFirstIntersection, SMALL_EPSILON);
      }
   }

   @Test
   public void testIntersectionBetweenRay2DAndConvexPolygon2D() throws Exception
   {
      Random random = new Random(43545L);

      for (int i = 0; i < ITERATIONS; i++)
      { // Test using intersectionBetweenLine2DAndConvexPolygon2D
         List<? extends Point2DReadOnly> convexPolygon2D = generateRandomPointCloud2D(random, 10.0, 10.0, 100);
         int hullSize = inPlaceGrahamScanConvexHull2D(convexPolygon2D);
         boolean clockwiseOrdered = random.nextBoolean();
         if (!clockwiseOrdered)
            Collections.reverse(convexPolygon2D.subList(0, hullSize));

         Point2D rayOrigin = generateRandomPoint2D(random, 10.0);
         Vector2D rayDirection = generateRandomVector2D(random, -10.0, 10.0);
         Point2D firstIntersectionWithLine = new Point2D();
         Point2D secondIntersectionWithLine = new Point2D();
         int expectedNumberOfIntersections = intersectionBetweenLine2DAndConvexPolygon2D(rayOrigin, rayDirection, convexPolygon2D, hullSize, clockwiseOrdered,
                                                                                         firstIntersectionWithLine, secondIntersectionWithLine);

         List<Point2D> expectedIntersections = new ArrayList<>();

         if (expectedNumberOfIntersections == 2 && isPoint2DInFrontOfRay2D(secondIntersectionWithLine, rayOrigin, rayDirection))
            expectedIntersections.add(secondIntersectionWithLine);
         if (expectedNumberOfIntersections >= 1 && isPoint2DInFrontOfRay2D(firstIntersectionWithLine, rayOrigin, rayDirection))
            expectedIntersections.add(firstIntersectionWithLine);
         expectedNumberOfIntersections = expectedIntersections.size();

         Point2D firstIntersectionWithRay = new Point2D();
         Point2D secondIntersectionWithRay = new Point2D();

         int actualNumberOfIntersections = intersectionBetweenRay2DAndConvexPolygon2D(rayOrigin, rayDirection, convexPolygon2D, hullSize, clockwiseOrdered,
                                                                                      firstIntersectionWithRay, secondIntersectionWithRay);
         assertEquals("Iteration: " + i, expectedNumberOfIntersections, actualNumberOfIntersections);

         if (expectedNumberOfIntersections == 2)
         {
            if (firstIntersectionWithLine.distance(expectedIntersections.get(0)) < firstIntersectionWithLine.distance(expectedIntersections.get(1)))
            {
               EuclidCoreTestTools.assertTuple2DEquals(firstIntersectionWithRay, expectedIntersections.get(0), EPSILON);
               EuclidCoreTestTools.assertTuple2DEquals(secondIntersectionWithRay, expectedIntersections.get(1), EPSILON);
            }
            else
            {
               EuclidCoreTestTools.assertTuple2DEquals(firstIntersectionWithRay, expectedIntersections.get(1), EPSILON);
               EuclidCoreTestTools.assertTuple2DEquals(secondIntersectionWithRay, expectedIntersections.get(0), EPSILON);
            }
         }

         if (expectedNumberOfIntersections == 1)
            EuclidCoreTestTools.assertTuple2DEquals(expectedIntersections.get(0), firstIntersectionWithRay, EPSILON);
      }
   }

   @Test
   public void testOrthogonalProjectionOnConvexPolygon2D() throws Exception
   {
      Random random = new Random(43545L);

      for (int i = 0; i < ITERATIONS; i++)
      { // Setup: Create point on an edge picked at random, shift it orthogonally toward the outside of the polygon
         List<? extends Point2DReadOnly> convexPolygon2D = generateRandomPointCloud2D(random, 10.0, 10.0, 100);
         int hullSize = inPlaceGrahamScanConvexHull2D(convexPolygon2D);
         boolean clockwiseOrdered = random.nextBoolean();
         if (!clockwiseOrdered)
            Collections.reverse(convexPolygon2D.subList(0, hullSize));

         int edgeIndex = random.nextInt(hullSize);
         Point2DReadOnly edgeStart = convexPolygon2D.get(edgeIndex);
         Point2DReadOnly edgeEnd = convexPolygon2D.get(next(edgeIndex, hullSize));

         Vector2D edgeNormal = new Vector2D();
         edgeNormal(edgeIndex, convexPolygon2D, hullSize, clockwiseOrdered, edgeNormal);

         Point2D expectedProjection = new Point2D();
         expectedProjection.interpolate(edgeStart, edgeEnd, random.nextDouble());

         Point2D pointToProject = new Point2D();
         pointToProject.scaleAdd(random.nextDouble(), edgeNormal, expectedProjection);

         Point2D actualProjection = new Point2D();
         boolean success = orthogonalProjectionOnConvexPolygon2D(pointToProject, convexPolygon2D, hullSize, clockwiseOrdered, actualProjection);
         assertTrue(success);
         EuclidCoreTestTools.assertTuple2DEquals(expectedProjection, actualProjection, EPSILON);
      }

      for (int i = 0; i < ITERATIONS; i++)
      { // Setup: pick a vertex at random an shift it outside the polygon such that the projection of the resulting point is the vertex.
         List<? extends Point2DReadOnly> convexPolygon2D = generateRandomPointCloud2D(random, 10.0, 10.0, 100);
         int hullSize = inPlaceGrahamScanConvexHull2D(convexPolygon2D);
         boolean clockwiseOrdered = random.nextBoolean();
         if (!clockwiseOrdered)
            Collections.reverse(convexPolygon2D.subList(0, hullSize));

         int vertexIndex = random.nextInt(hullSize);
         Point2DReadOnly vertex = convexPolygon2D.get(vertexIndex);

         Vector2D previousEdgeNormal = new Vector2D();
         Vector2D nextEdgeNormal = new Vector2D();
         Vector2D shiftDirection = new Vector2D();
         edgeNormal(previous(vertexIndex, hullSize), convexPolygon2D, hullSize, clockwiseOrdered, previousEdgeNormal);
         edgeNormal(vertexIndex, convexPolygon2D, hullSize, clockwiseOrdered, nextEdgeNormal);
         shiftDirection.interpolate(previousEdgeNormal, nextEdgeNormal, random.nextDouble());

         Point2D pointToProject = new Point2D();
         pointToProject.scaleAdd(random.nextDouble(), shiftDirection, vertex);

         Point2D acualProjection = new Point2D();
         boolean success = orthogonalProjectionOnConvexPolygon2D(pointToProject, convexPolygon2D, hullSize, clockwiseOrdered, acualProjection);
         assertTrue(success);
         EuclidCoreTestTools.assertTuple2DEquals("Iteration: " + i, vertex, acualProjection, EPSILON);
      }

      for (int i = 0; i < ITERATIONS; i++)
      { // Setup: ensure nothing happens if the query is inside the polygon.
         List<? extends Point2DReadOnly> convexPolygon2D = generateRandomPointCloud2D(random, 10.0, 10.0, 100);
         int hullSize = inPlaceGrahamScanConvexHull2D(convexPolygon2D);
         boolean clockwiseOrdered = random.nextBoolean();
         if (!clockwiseOrdered)
            Collections.reverse(convexPolygon2D.subList(0, hullSize));

         Point2D centroid = new Point2D();
         computeConvexPolyong2DArea(convexPolygon2D, hullSize, clockwiseOrdered, centroid);
         int vertexIndex = random.nextInt(hullSize);
         int nextVertexIndex = next(vertexIndex, hullSize);
         Point2DReadOnly vertex = convexPolygon2D.get(vertexIndex);
         Point2DReadOnly nextVertex = convexPolygon2D.get(nextVertexIndex);

         Point2D pointOnEdge = new Point2D();
         pointOnEdge.interpolate(vertex, nextVertex, random.nextDouble());

         Point2D pointInside = new Point2D();
         pointInside.interpolate(centroid, pointOnEdge, random.nextDouble());

         Point2D actualProjection = new Point2D(Double.NaN, Double.NaN);
         boolean success = orthogonalProjectionOnConvexPolygon2D(pointInside, convexPolygon2D, hullSize, clockwiseOrdered, actualProjection);
         assertFalse(success);
         EuclidCoreTestTools.assertTuple2DContainsOnlyNaN(actualProjection);
      }
   }

   @Test
   public void testLineOfSightStartEndIndex() throws Exception
   {
      Random random = new Random(324234L);

      for (int i = 0; i < ITERATIONS; i++)
      {
         List<? extends Point2DReadOnly> convexPolygon2D = generateRandomPointCloud2D(random, 10.0, 10.0, 100);
         int hullSize = inPlaceGrahamScanConvexHull2D(convexPolygon2D);
         boolean clockwiseOrdered = random.nextBoolean();
         if (!clockwiseOrdered)
            Collections.reverse(convexPolygon2D.subList(0, hullSize));

         Point2D observer = new Point2D();
         { // Construction of the observer such that it is outside the polygon
            Point2D centroid = new Point2D();
            computeConvexPolyong2DArea(convexPolygon2D, hullSize, clockwiseOrdered, centroid);
            int vertexIndex = random.nextInt(hullSize);
            int nextVertexIndex = next(vertexIndex, hullSize);
            Point2DReadOnly vertex = convexPolygon2D.get(vertexIndex);
            Point2DReadOnly nextVertex = convexPolygon2D.get(nextVertexIndex);

            Point2D pointOnEdge = new Point2D();
            pointOnEdge.interpolate(vertex, nextVertex, random.nextDouble());

            observer.interpolate(centroid, pointOnEdge, generateRandomDouble(random, 1.0, 10.0));
         }

         assertFalse(isPoint2DInsideConvexPolygon2D(observer, convexPolygon2D, hullSize, clockwiseOrdered, 0.0));

         int lineOfSightStartIndex = lineOfSightStartIndex(observer, convexPolygon2D, hullSize, clockwiseOrdered);
         int lineOfSightEndIndex = lineOfSightEndIndex(observer, convexPolygon2D, hullSize, clockwiseOrdered);

         assertNotEquals(-1, lineOfSightStartIndex);
         assertNotEquals(-1, lineOfSightEndIndex);

         /*
          * Drawing lines from the observer going through the start/end vertices. Each line should
          * intersect only once the polygon.
          */
         {
            Point2DReadOnly startVertex = convexPolygon2D.get(lineOfSightStartIndex);
            Vector2D startDirection = new Vector2D();
            startDirection.sub(startVertex, observer);
            assertEquals(1, intersectionBetweenLine2DAndConvexPolygon2D(observer, startDirection, convexPolygon2D, hullSize, clockwiseOrdered, new Point2D(),
                                                                        new Point2D()));

            Point2DReadOnly endVertex = convexPolygon2D.get(lineOfSightEndIndex);
            Vector2D endDirection = new Vector2D();
            endDirection.sub(endVertex, observer);
            assertEquals(1, intersectionBetweenLine2DAndConvexPolygon2D(observer, endDirection, convexPolygon2D, hullSize, clockwiseOrdered, new Point2D(),
                                                                        new Point2D()));
         }

         Set<Integer> lineOfSightIndices = new HashSet<>();
         lineOfSightIndices.add(lineOfSightStartIndex);
         lineOfSightIndices.add(lineOfSightEndIndex);

         for (int j = next(lineOfSightStartIndex, hullSize); j != lineOfSightEndIndex; j = next(j, hullSize))
         {
            lineOfSightIndices.add(j);
         }

         /*
          * Shooting ray from the observer to each vertex of the polygon. If the vertex is in the
          * line of sight, that means, there should not anything between the observer and the
          * vertex.
          */
         for (Integer currentIndex = 0; currentIndex < hullSize; currentIndex++)
         {
            Point2DReadOnly vertex = convexPolygon2D.get(currentIndex);

            Vector2D fromObserverToVertex = new Vector2D();
            fromObserverToVertex.sub(vertex, observer);
            Vector2D deltaAwayFromVertex = new Vector2D();
            deltaAwayFromVertex.setAndNormalize(fromObserverToVertex);
            deltaAwayFromVertex.scale(-1.0e-3);

            Point2D rightBeforeVertex = new Point2D();
            rightBeforeVertex.add(fromObserverToVertex, observer);
            rightBeforeVertex.add(deltaAwayFromVertex);

            int expected = lineOfSightIndices.contains(currentIndex) ? 0 : 1;
            int actual = intersectionBetweenLineSegment2DAndConvexPolygon2D(observer, rightBeforeVertex, convexPolygon2D, hullSize, clockwiseOrdered,
                                                                            new Point2D(), new Point2D());
            assertEquals(expected, actual);
         }
      }
   }

   /**
    * Could not find a simpler to test this method with a thoroughly different piece of code. So in
    * addition to the usual random test, there's also a bunch of examples.
    * 
    * @throws Exception
    */
   @Test
   public void testClosestPointToNonInterectingRay2D() throws Exception
   {
      { // Test examples with a quadrilateral
         List<Point2D> convexPolygon2D = new ArrayList<>();
         convexPolygon2D.add(new Point2D(-1.0, 0.0));
         convexPolygon2D.add(new Point2D(0.0, 1.0));
         convexPolygon2D.add(new Point2D(2.0, 0.0));
         convexPolygon2D.add(new Point2D(1.0, -1.0));
         int hullSize = 4;
         Point2D rayOrigin = new Point2D();
         Vector2D rayDirection = new Vector2D();
         Point2D expected = new Point2D();
         Point2D actual = new Point2D();

         rayOrigin.set(5.0, -3.0);
         rayDirection.set(0.0, 1.0);
         expected.set(2.0, 0.0);
         closestPointToNonInterectingRay2D(rayOrigin, rayDirection, convexPolygon2D, hullSize, true, actual);
         EuclidCoreTestTools.assertTuple2DEquals(expected, actual, SMALLEST_EPSILON);

         rayOrigin.set(1.0, 1.0);
         rayDirection.set(0.5, 0.5);
         expected.set(4.0 / 5.0, 3.0 / 5.0);
         closestPointToNonInterectingRay2D(rayOrigin, rayDirection, convexPolygon2D, hullSize, true, actual);
         EuclidCoreTestTools.assertTuple2DEquals(expected, actual, SMALLEST_EPSILON);

         rayOrigin.set(1.0, 1.0);
         rayDirection.set(-0.5, 0.1);
         expected.set(0.0, 1.0);
         closestPointToNonInterectingRay2D(rayOrigin, rayDirection, convexPolygon2D, hullSize, true, actual);
         EuclidCoreTestTools.assertTuple2DEquals(expected, actual, SMALLEST_EPSILON);

         rayOrigin.set(-0.75, 0.75);
         rayDirection.set(0.0, 0.1);
         expected.set(-0.5, 0.5);
         closestPointToNonInterectingRay2D(rayOrigin, rayDirection, convexPolygon2D, hullSize, true, actual);
         EuclidCoreTestTools.assertTuple2DEquals(expected, actual, SMALLEST_EPSILON);

         rayOrigin.set(-0.75, 0.75);
         rayDirection.set(0.3, 0.3);
         expected.set(-0.5, 0.5);
         closestPointToNonInterectingRay2D(rayOrigin, rayDirection, convexPolygon2D, hullSize, true, actual);
         EuclidCoreTestTools.assertTuple2DEquals(expected, actual, SMALLEST_EPSILON);

         rayOrigin.set(-0.75, 0.75);
         rayDirection.set(-0.3, -0.3);
         expected.set(-0.5, 0.5);
         closestPointToNonInterectingRay2D(rayOrigin, rayDirection, convexPolygon2D, hullSize, true, actual);
         EuclidCoreTestTools.assertTuple2DEquals(expected, actual, SMALLEST_EPSILON);

         rayOrigin.set(-0.75, 0.75);
         rayDirection.set(0.3, 0.31);
         expected.set(-0.5, 0.5);
         closestPointToNonInterectingRay2D(rayOrigin, rayDirection, convexPolygon2D, hullSize, true, actual);
         EuclidCoreTestTools.assertTuple2DEquals(expected, actual, SMALLEST_EPSILON);

         rayOrigin.set(-0.75, 0.75);
         rayDirection.set(0.3, 0.29);
         expected.set(0.0, 1.0);
         closestPointToNonInterectingRay2D(rayOrigin, rayDirection, convexPolygon2D, hullSize, true, actual);
         EuclidCoreTestTools.assertTuple2DEquals(expected, actual, SMALLEST_EPSILON);

         rayOrigin.set(1.75, -0.75);
         rayDirection.set(1.0, 1.0);
         expected.set(1.5, -0.5);
         closestPointToNonInterectingRay2D(rayOrigin, rayDirection, convexPolygon2D, hullSize, true, actual);
         EuclidCoreTestTools.assertTuple2DEquals(expected, actual, SMALLEST_EPSILON);

         rayOrigin.set(1.75, -0.75);
         rayDirection.set(-0.3, -0.3);
         expected.set(1.5, -0.5);
         closestPointToNonInterectingRay2D(rayOrigin, rayDirection, convexPolygon2D, hullSize, true, actual);
         EuclidCoreTestTools.assertTuple2DEquals(expected, actual, SMALLEST_EPSILON);

         rayOrigin.set(1.0, -1.2);
         rayDirection.set(-2.0, 1.0);
         expected.set(1.0, -1.0);
         closestPointToNonInterectingRay2D(rayOrigin, rayDirection, convexPolygon2D, hullSize, true, actual);
         EuclidCoreTestTools.assertTuple2DEquals(expected, actual, SMALLEST_EPSILON);

         rayOrigin.set(1.0, -1.2);
         rayDirection.set(2.0, -1.0);
         expected.set(1.0, -1.0);
         closestPointToNonInterectingRay2D(rayOrigin, rayDirection, convexPolygon2D, hullSize, true, actual);
         EuclidCoreTestTools.assertTuple2DEquals(expected, actual, SMALLEST_EPSILON);

         rayOrigin.set(-0.1, -0.7);
         rayDirection.set(-2.0, 1.0);
         expected.set(0.0, -0.5);
         closestPointToNonInterectingRay2D(rayOrigin, rayDirection, convexPolygon2D, hullSize, true, actual);
         EuclidCoreTestTools.assertTuple2DEquals(expected, actual, SMALLEST_EPSILON);
      }

      { // Test examples with a single point polygon
         List<Point2D> convexPolygon2D = new ArrayList<>();
         Point2D vertex = new Point2D(1.0, -1.0);
         convexPolygon2D.add(vertex);
         int hullSize = 1;
         Point2D rayOrigin = new Point2D();
         Vector2D rayDirection = new Vector2D();
         Point2D expected = vertex;
         Point2D actual = new Point2D();

         rayOrigin.set(5.0, -3.0);
         rayDirection.set(0.0, 1.0);
         closestPointToNonInterectingRay2D(rayOrigin, rayDirection, convexPolygon2D, hullSize, true, actual);
         EuclidCoreTestTools.assertTuple2DEquals(expected, actual, SMALLEST_EPSILON);

         rayOrigin.set(0.0, 0.0);
         rayDirection.set(1.0, 0.0);
         closestPointToNonInterectingRay2D(rayOrigin, rayDirection, convexPolygon2D, hullSize, true, actual);
         EuclidCoreTestTools.assertTuple2DEquals(expected, actual, SMALLEST_EPSILON);
      }

      Random random = new Random(324234L);

      for (int i = 0; i < ITERATIONS; i++)
      { // Setup 1: the ray origin is positioned outside a given edge, and its direction is pointing toward the outside of the polygon.
         List<? extends Point2DReadOnly> convexPolygon2D = generateRandomPointCloud2D(random, 10.0, 10.0, 100);
         int hullSize = inPlaceGrahamScanConvexHull2D(convexPolygon2D);
         boolean clockwiseOrdered = random.nextBoolean();
         if (!clockwiseOrdered)
            Collections.reverse(convexPolygon2D.subList(0, hullSize));

         Point2D rayOrigin = new Point2D();
         Vector2D rayDirection = new Vector2D();

         int edgeIndex = random.nextInt(hullSize);
         Point2D pointOnEdge = new Point2D();
         Vector2D edgeNormal = new Vector2D();
         Vector2D edgeDirection = new Vector2D();
         pointOnEdge.interpolate(convexPolygon2D.get(edgeIndex), convexPolygon2D.get(next(edgeIndex, hullSize)), random.nextDouble());
         edgeNormal(edgeIndex, convexPolygon2D, hullSize, clockwiseOrdered, edgeNormal);
         edgeDirection.sub(convexPolygon2D.get(edgeIndex), convexPolygon2D.get(next(edgeIndex, hullSize)));
         edgeDirection.normalize();
         if (random.nextBoolean())
            edgeDirection.negate();

         rayOrigin.scaleAdd(generateRandomDouble(random, 0.0, 10.0), edgeNormal, pointOnEdge);
         rayDirection.interpolate(edgeDirection, edgeNormal, random.nextDouble());

         // Finding the closest vertex to the ray
         int closestVertexIndexToRay = closestVertexIndexToRay2D(rayOrigin, rayDirection, convexPolygon2D, hullSize, clockwiseOrdered);
         Point2DReadOnly closestVertexToRay = convexPolygon2D.get(closestVertexIndexToRay);
         // Getting the orthogonal projection of the ray's origin
         Point2D rayOriginProjected = new Point2D();
         orthogonalProjectionOnConvexPolygon2D(rayOrigin, convexPolygon2D, hullSize, clockwiseOrdered, rayOriginProjected);
         // Picking the closest of the two
         double distanceVertexToRay = distanceFromPoint2DToRay2D(closestVertexToRay, rayOrigin, rayDirection);
         double distancePojectionToRay = distanceFromPoint2DToRay2D(rayOriginProjected, rayOrigin, rayDirection);
         Point2D expectedClosestPoint = new Point2D();
         if (distanceVertexToRay < distancePojectionToRay)
            expectedClosestPoint.set(closestVertexToRay);
         else
            expectedClosestPoint.set(rayOriginProjected);

         Point2D actualClosestPoint = new Point2D();
         boolean success = closestPointToNonInterectingRay2D(rayOrigin, rayDirection, convexPolygon2D, hullSize, clockwiseOrdered, actualClosestPoint);
         assertTrue(success);
         if (!expectedClosestPoint.epsilonEquals(actualClosestPoint, SMALLEST_EPSILON))
         {
            int numberOfIntersections = intersectionBetweenRay2DAndConvexPolygon2D(rayOrigin, rayDirection, convexPolygon2D, hullSize, clockwiseOrdered,
                                                                                   new Point2D(), new Point2D());
            if (numberOfIntersections > 0)
               System.err.println("Intersecting ray, test is bad");

            double distanceExpected = distanceFromPoint2DToRay2D(expectedClosestPoint, rayOrigin, rayDirection);
            double distanceActual = distanceFromPoint2DToRay2D(actualClosestPoint, rayOrigin, rayDirection);
            if (distanceExpected > distanceActual)
               System.err.println("Test is bad");
         }
         EuclidCoreTestTools.assertTuple2DEquals("Iteration: " + i, expectedClosestPoint, actualClosestPoint, SMALLEST_EPSILON);
      }

      for (int i = 0; i < ITERATIONS; i++)
      { // (somewhat tricky) Setup: build a line that does NOT intersect with the polygon and put the ray origin somewhere on this line
         /* 
          * @formatter:off
          * - The goal is to build the query line such that it does not intersect with the polygon.
          * - Pick two successive vertices: v0 = convexPolygon2D.get(i) and vn1 = convexPolygon2D.get(i+1).
          * - Draw 2 lines going from the centroid through each vertex, they are called extrapolation lines.
          * - For the line going through v0, find the intersection v0Max with the line going through vn1 and vn2 = convexPolygon2D.get(i+2).
          *    The first point defining the query line should be between v0 and v0Max such that the line won't intersect with the edge (vn1, vn2).
          * - For the line going through vn1, find the intersection vn1Max with the line going through v0 and vp1 = convexPolygon2D.get(i-1).
          *    The second point defining the query line should be between vn1 and vn1Max such that the line won't intersect with the edge (v0, vp1).
          * @formatter:on
          */
         List<? extends Point2DReadOnly> convexPolygon2D = generateRandomPointCloud2D(random, 10.0, 10.0, 100);
         int hullSize = inPlaceGrahamScanConvexHull2D(convexPolygon2D);
         boolean clockwiseOrdered = random.nextBoolean();
         if (!clockwiseOrdered)
            Collections.reverse(convexPolygon2D.subList(0, hullSize));

         Point2D rayOrigin = new Point2D();
         Vector2D rayDirection = new Vector2D();

         { // Creation of the non-interesting line
            int v0Index = random.nextInt(hullSize);
            int vn1Index = next(v0Index, hullSize);
            int vn2Index = next(vn1Index, hullSize);
            int vp1Index = previous(v0Index, hullSize);

            Point2DReadOnly v0 = convexPolygon2D.get(v0Index);
            Point2DReadOnly vn1 = convexPolygon2D.get(vn1Index);
            Point2DReadOnly vn2 = convexPolygon2D.get(vn2Index);
            Point2DReadOnly vp1 = convexPolygon2D.get(vp1Index);

            Point2D centroid = new Point2D();
            computeConvexPolyong2DArea(convexPolygon2D, hullSize, clockwiseOrdered, centroid);

            Point2D v0Max = intersectionBetweenTwoLine2Ds(centroid, v0, vn1, vn2);
            Vector2D extrapolationDirection = new Vector2D();
            extrapolationDirection.sub(v0, centroid);

            if (!isPoint2DInFrontOfRay2D(v0Max, centroid, extrapolationDirection))
               v0Max.scaleAdd(10.0, extrapolationDirection, v0);

            Point2D vn1Max = intersectionBetweenTwoLine2Ds(centroid, vn1, v0, vp1);
            extrapolationDirection.sub(vn1, centroid);

            if (!isPoint2DInFrontOfRay2D(vn1Max, centroid, extrapolationDirection))
               vn1Max.scaleAdd(10.0, extrapolationDirection, vn1);

            Point2D firstExtrapolatedPoint = new Point2D();
            Point2D secondExtrapolatedPoint = new Point2D();

            firstExtrapolatedPoint.interpolate(v0, v0Max, generateRandomDouble(random, 0.0, 1.0));
            secondExtrapolatedPoint.interpolate(vn1, vn1Max, generateRandomDouble(random, 0.0, 1.0));

            Point2D pointOnLine = new Point2D(firstExtrapolatedPoint);
            Vector2D lineDirection = new Vector2D();
            lineDirection.sub(secondExtrapolatedPoint, firstExtrapolatedPoint);
            lineDirection.normalize();

            rayOrigin.scaleAdd(generateRandomDouble(random, 10.0), lineDirection, pointOnLine);
            rayDirection.scaleAdd(generateRandomDouble(random, 10.0), lineDirection);
         }

         // Finding the closest vertex to the ray
         int closestVertexIndexToRay = closestVertexIndexToRay2D(rayOrigin, rayDirection, convexPolygon2D, hullSize, clockwiseOrdered);
         Point2DReadOnly closestVertexToRay = convexPolygon2D.get(closestVertexIndexToRay);
         // Getting the orthogonal projection of the ray's origin
         Point2D rayOriginProjected = new Point2D();
         orthogonalProjectionOnConvexPolygon2D(rayOrigin, convexPolygon2D, hullSize, clockwiseOrdered, rayOriginProjected);
         // Picking the closest of the two
         double distanceVertexToRay = distanceFromPoint2DToRay2D(closestVertexToRay, rayOrigin, rayDirection);
         double distancePojectionToRay = distanceFromPoint2DToRay2D(rayOriginProjected, rayOrigin, rayDirection);
         Point2D expectedClosestPoint = new Point2D();
         if (distanceVertexToRay < distancePojectionToRay)
            expectedClosestPoint.set(closestVertexToRay);
         else
            expectedClosestPoint.set(rayOriginProjected);

         Point2D actualClosestPoint = new Point2D();
         boolean success = closestPointToNonInterectingRay2D(rayOrigin, rayDirection, convexPolygon2D, hullSize, clockwiseOrdered, actualClosestPoint);
         assertTrue(success);
         if (!expectedClosestPoint.epsilonEquals(actualClosestPoint, SMALLEST_EPSILON))
         {
            int numberOfIntersections = intersectionBetweenRay2DAndConvexPolygon2D(rayOrigin, rayDirection, convexPolygon2D, hullSize, clockwiseOrdered,
                                                                                   new Point2D(), new Point2D());
            if (numberOfIntersections > 0)
               System.err.println("Intersecting ray, test is bad");

            double distanceExpected = distanceFromPoint2DToRay2D(expectedClosestPoint, rayOrigin, rayDirection);
            double distanceActual = distanceFromPoint2DToRay2D(actualClosestPoint, rayOrigin, rayDirection);
            if (distanceExpected > distanceActual)
               System.err.println("Test is bad");
         }
         EuclidCoreTestTools.assertTuple2DEquals("Iteration: " + i, expectedClosestPoint, actualClosestPoint, SMALLEST_EPSILON);
      }
   }

   @Test
   public void testClosestVertexIndexToLine2D() throws Exception
   {
      { // Test examples with triangle

         Point2D vertex1 = new Point2D(0.0, 0.0);
         Point2D vertex2 = new Point2D(10.0, 0.0);
         Point2D vertex3 = new Point2D(0.0, 10.0);
         List<Point2D> convexPolygon2D = new ArrayList<>();
         convexPolygon2D.add(vertex1);
         convexPolygon2D.add(vertex2);
         convexPolygon2D.add(vertex3);
         int hullSize = inPlaceGrahamScanConvexHull2D(convexPolygon2D);
         Point2D firstPointOnLine = new Point2D();
         Point2D secondPointOnLine = new Point2D();
         Point2D closestVertex;

         firstPointOnLine.set(-1.0, 1.0);
         secondPointOnLine.set(1.0, -1.0);
         closestVertex = convexPolygon2D.get(closestVertexIndexToLine2D(firstPointOnLine, secondPointOnLine, convexPolygon2D, hullSize));
         assertTrue(vertex1 == closestVertex);

         firstPointOnLine.set(9.0, 0.0);
         secondPointOnLine.set(0.0, 1.0);
         closestVertex = convexPolygon2D.get(closestVertexIndexToLine2D(firstPointOnLine, secondPointOnLine, convexPolygon2D, hullSize));
         assertTrue(vertex2 == closestVertex);

         firstPointOnLine.set(11.0, 0.0);
         secondPointOnLine.set(0.0, 12.0);
         closestVertex = convexPolygon2D.get(closestVertexIndexToLine2D(firstPointOnLine, secondPointOnLine, convexPolygon2D, hullSize));
         assertTrue(vertex2 == closestVertex);

         firstPointOnLine.set(12.0, 0.0);
         secondPointOnLine.set(0.0, 11.0);
         closestVertex = convexPolygon2D.get(closestVertexIndexToLine2D(firstPointOnLine, secondPointOnLine, convexPolygon2D, hullSize));
         assertTrue(vertex3 == closestVertex);

         firstPointOnLine.set(-1.0, 13.0);
         secondPointOnLine.set(1.0, 14.0);
         closestVertex = convexPolygon2D.get(closestVertexIndexToLine2D(firstPointOnLine, secondPointOnLine, convexPolygon2D, hullSize));
         assertTrue(vertex3 == closestVertex);
      }

      Random random = new Random(324234L);

      for (int i = 0; i < ITERATIONS; i++)
      {
         List<? extends Point2DReadOnly> convexPolygon2D = generateRandomPointCloud2D(random, 10.0, 10.0, 100);
         int hullSize = inPlaceGrahamScanConvexHull2D(convexPolygon2D);
         boolean clockwiseOrdered = random.nextBoolean();
         if (!clockwiseOrdered)
            Collections.reverse(convexPolygon2D.subList(0, hullSize));

         Point2D pointOnLine = generateRandomPoint2D(random, 10.0);
         Vector2D lineDirection = generateRandomVector2D(random, -10.0, 10.0);

         // Nothing smart here, just going through the vertices checking which one is the closest.
         Point2DReadOnly closestVertex = null;
         double minDistance = Double.POSITIVE_INFINITY;
         for (Point2DReadOnly vertex : convexPolygon2D.subList(0, hullSize))
         {
            double distance = distanceFromPoint2DToLine2D(vertex, pointOnLine, lineDirection);
            if (distance < minDistance)
            {
               minDistance = distance;
               closestVertex = vertex;
            }
         }

         int actualIndex = closestVertexIndexToLine2D(pointOnLine, lineDirection, convexPolygon2D, hullSize);
         assertTrue(closestVertex == convexPolygon2D.get(actualIndex));
      }

      { // Test exceptions
         List<? extends Point2DReadOnly> convexPolygon2D = generateRandomPointCloud2D(random, 10.0, 10.0, 100);
         int hullSize = inPlaceGrahamScanConvexHull2D(convexPolygon2D);
         boolean clockwiseOrdered = random.nextBoolean();
         if (!clockwiseOrdered)
            Collections.reverse(convexPolygon2D.subList(0, hullSize));

         Point2D pointOnLine = generateRandomPoint2D(random, 10.0);
         Vector2D lineDirection = generateRandomVector2D(random, -10.0, 10.0);

         try
         {
            closestVertexIndexToLine2D(pointOnLine, lineDirection, convexPolygon2D, convexPolygon2D.size() + 1);
            fail("Should have thrown an " + IllegalArgumentException.class.getSimpleName());
         }
         catch (IllegalArgumentException e)
         {
            // good
         }

         try
         {
            closestVertexIndexToLine2D(pointOnLine, lineDirection, convexPolygon2D, -1);
            fail("Should have thrown an " + IllegalArgumentException.class.getSimpleName());
         }
         catch (IllegalArgumentException e)
         {
            // good
         }
      }
   }

   @Test
   public void testClosestVertexIndexToRay2D() throws Exception
   {
      Random random = new Random(324234L);

      for (int i = 0; i < ITERATIONS; i++)
      {
         List<? extends Point2DReadOnly> convexPolygon2D = generateRandomPointCloud2D(random, 10.0, 10.0, 100);
         int hullSize = inPlaceGrahamScanConvexHull2D(convexPolygon2D);
         boolean clockwiseOrdered = random.nextBoolean();
         if (!clockwiseOrdered)
            Collections.reverse(convexPolygon2D.subList(0, hullSize));

         Point2D rayOrigin = generateRandomPoint2D(random, 10.0);
         Vector2D rayDirection = generateRandomVector2D(random, -10.0, 10.0);

         // Nothing smart here, just going through the vertices checking which one is the closest.
         Point2DReadOnly closestVertex = null;
         double minDistance = Double.POSITIVE_INFINITY;
         for (Point2DReadOnly vertex : convexPolygon2D.subList(0, hullSize))
         {
            double distance = distanceFromPoint2DToRay2D(vertex, rayOrigin, rayDirection);
            if (distance < minDistance)
            {
               minDistance = distance;
               closestVertex = vertex;
            }
         }

         int actualIndex = closestVertexIndexToRay2D(rayOrigin, rayDirection, convexPolygon2D, hullSize, clockwiseOrdered);
         assertTrue(closestVertex == convexPolygon2D.get(actualIndex));
      }

      { // Test exceptions
         List<? extends Point2DReadOnly> convexPolygon2D = generateRandomPointCloud2D(random, 10.0, 10.0, 100);
         int hullSize = inPlaceGrahamScanConvexHull2D(convexPolygon2D);
         boolean clockwiseOrdered = random.nextBoolean();
         if (!clockwiseOrdered)
            Collections.reverse(convexPolygon2D.subList(0, hullSize));

         Point2D rayOrigin = generateRandomPoint2D(random, 10.0);
         Vector2D rayDirection = generateRandomVector2D(random, -10.0, 10.0);

         try
         {
            closestVertexIndexToRay2D(rayOrigin, rayDirection, convexPolygon2D, convexPolygon2D.size() + 1, clockwiseOrdered);
            fail("Should have thrown an " + IllegalArgumentException.class.getSimpleName());
         }
         catch (IllegalArgumentException e)
         {
            // good
         }

         try
         {
            closestVertexIndexToRay2D(rayOrigin, rayDirection, convexPolygon2D, -1, clockwiseOrdered);
            fail("Should have thrown an " + IllegalArgumentException.class.getSimpleName());
         }
         catch (IllegalArgumentException e)
         {
            // good
         }
      }
   }

   @Test
   public void testClosestVertexIndexToPoint2D() throws Exception
   {
      Random random = new Random(324234L);

      for (int i = 0; i < ITERATIONS; i++)
      {
         List<? extends Point2DReadOnly> convexPolygon2D = generateRandomPointCloud2D(random, 10.0, 10.0, 100);
         int hullSize = inPlaceGrahamScanConvexHull2D(convexPolygon2D);
         boolean clockwiseOrdered = random.nextBoolean();
         if (!clockwiseOrdered)
            Collections.reverse(convexPolygon2D.subList(0, hullSize));

         Point2D query = EuclidCoreRandomTools.generateRandomPoint2D(random, 10.0);

         Point2DReadOnly closestVertex = convexPolygon2D.get(0);
         for (Point2DReadOnly vertex : convexPolygon2D.subList(0, hullSize))
         {
            if (query.distance(vertex) < query.distance(closestVertex))
               closestVertex = vertex;
         }

         int closestVertexIndex = closestVertexIndexToPoint2D(query, convexPolygon2D, hullSize);
         assertTrue(closestVertex == convexPolygon2D.get(closestVertexIndex));
      }

      { // Test exceptions
         List<? extends Point2DReadOnly> convexPolygon2D = generateRandomPointCloud2D(random, 10.0, 10.0, 100);
         int hullSize = inPlaceGrahamScanConvexHull2D(convexPolygon2D);
         boolean clockwiseOrdered = random.nextBoolean();
         if (!clockwiseOrdered)
            Collections.reverse(convexPolygon2D.subList(0, hullSize));

         try
         {
            closestVertexIndexToPoint2D(new Point2D(), convexPolygon2D, convexPolygon2D.size() + 1);
            fail("Should have thrown an " + IllegalArgumentException.class.getSimpleName());
         }
         catch (IllegalArgumentException e)
         {
            // good
         }

         try
         {
            closestVertexIndexToPoint2D(new Point2D(), convexPolygon2D, -1);
            fail("Should have thrown an " + IllegalArgumentException.class.getSimpleName());
         }
         catch (IllegalArgumentException e)
         {
            // good
         }
      }
   }

   @Test
   public void testClosestEdgeIndexToPoint2D() throws Exception
   {
      Random random = new Random(324234L);

      for (int i = 0; i < ITERATIONS; i++)
      { // Test with the query being outside the polygon
         List<? extends Point2DReadOnly> convexPolygon2D = generateRandomPointCloud2D(random, 10.0, 10.0, 100);
         int hullSize = inPlaceGrahamScanConvexHull2D(convexPolygon2D);
         boolean clockwiseOrdered = random.nextBoolean();
         if (!clockwiseOrdered)
            Collections.reverse(convexPolygon2D.subList(0, hullSize));

         Point2D centroid = new Point2D();
         computeConvexPolyong2DArea(convexPolygon2D, hullSize, clockwiseOrdered, centroid);
         int vertexIndex = random.nextInt(hullSize);
         int nextVertexIndex = next(vertexIndex, hullSize);
         Point2DReadOnly vertex = convexPolygon2D.get(vertexIndex);
         Point2DReadOnly nextVertex = convexPolygon2D.get(nextVertexIndex);

         Point2D pointOnEdge = new Point2D();
         pointOnEdge.interpolate(vertex, nextVertex, random.nextDouble());

         double alphaOutside = EuclidCoreRandomTools.generateRandomDouble(random, 1.0, 3.0);
         Point2D outsidePoint = new Point2D();
         outsidePoint.interpolate(centroid, pointOnEdge, alphaOutside);

         // Since it is outside, the closest edge has the closest vertex
         int closestVertexIndex = closestVertexIndexToPoint2D(outsidePoint, convexPolygon2D, hullSize);
         // The closest edge has to be one the edges adjacent to the vertex
         int closestEdgeIndex = closestEdgeIndexToPoint2D(outsidePoint, convexPolygon2D, hullSize, clockwiseOrdered);

         boolean isEdgeAdjacentToClosestVertex = closestEdgeIndex == closestVertexIndex || closestEdgeIndex == previous(closestVertexIndex, hullSize);
         assertTrue(isEdgeAdjacentToClosestVertex);
         // The closest edge is also picked such that the query can see the edge.
         assertTrue(canObserverSeeEdge(closestEdgeIndex, outsidePoint, convexPolygon2D, hullSize, clockwiseOrdered));
      }

      for (int i = 0; i < ITERATIONS; i++)
      { // Test with the query being inside the polygon
         List<? extends Point2DReadOnly> convexPolygon2D = generateRandomPointCloud2D(random, 10.0, 10.0, 100);
         int hullSize = inPlaceGrahamScanConvexHull2D(convexPolygon2D);
         boolean clockwiseOrdered = random.nextBoolean();
         if (!clockwiseOrdered)
            Collections.reverse(convexPolygon2D.subList(0, hullSize));

         Point2D centroid = new Point2D();
         computeConvexPolyong2DArea(convexPolygon2D, hullSize, clockwiseOrdered, centroid);
         int vertexIndex = random.nextInt(hullSize);
         int nextVertexIndex = next(vertexIndex, hullSize);
         Point2DReadOnly vertex = convexPolygon2D.get(vertexIndex);
         Point2DReadOnly nextVertex = convexPolygon2D.get(nextVertexIndex);

         Point2D pointOnEdge = new Point2D();
         pointOnEdge.interpolate(vertex, nextVertex, random.nextDouble());

         double alphaInside = EuclidCoreRandomTools.generateRandomDouble(random, 0.0, 1.0);
         Point2D insidePoint = new Point2D();
         insidePoint.interpolate(centroid, pointOnEdge, alphaInside);

         // Nothing smart here, just going through the edges checking which one is the closest.
         int expectedIndex = -1;
         double minDistance = Double.POSITIVE_INFINITY;

         for (int edgeIndex = 0; edgeIndex < hullSize; edgeIndex++)
         {
            double distance = distanceFromPoint2DToLineSegment2D(insidePoint, convexPolygon2D.get(edgeIndex), convexPolygon2D.get(next(edgeIndex, hullSize)));
            if (distance < minDistance)
            {
               minDistance = distance;
               expectedIndex = edgeIndex;
            }
         }

         int actualIndex = closestEdgeIndexToPoint2D(insidePoint, convexPolygon2D, hullSize, clockwiseOrdered);
         assertEquals(expectedIndex, actualIndex);
      }

      { // Test exceptions
         List<? extends Point2DReadOnly> convexPolygon2D = generateRandomPointCloud2D(random, 10.0, 10.0, 100);
         int hullSize = inPlaceGrahamScanConvexHull2D(convexPolygon2D);
         boolean clockwiseOrdered = random.nextBoolean();
         if (!clockwiseOrdered)
            Collections.reverse(convexPolygon2D.subList(0, hullSize));

         try
         {
            closestEdgeIndexToPoint2D(new Point2D(), convexPolygon2D, convexPolygon2D.size() + 1, clockwiseOrdered);
            fail("Should have thrown an " + IllegalArgumentException.class.getSimpleName());
         }
         catch (IllegalArgumentException e)
         {
            // good
         }

         try
         {
            closestEdgeIndexToPoint2D(new Point2D(), convexPolygon2D, -1, clockwiseOrdered);
            fail("Should have thrown an " + IllegalArgumentException.class.getSimpleName());
         }
         catch (IllegalArgumentException e)
         {
            // good
         }
      }
   }

   @Test
   public void testRemove() throws Exception
   {
      Random random = new Random(35L);

      for (int i = 0; i < ITERATIONS; i++)
      {
         int numberOfPoints = 100;
         List<Integer> points = new ArrayList<>();
         for (int j = 0; j < numberOfPoints; j++)
            points.add(new Integer(j));

         List<Integer> pointsCopy = new ArrayList<>(points);
         int listSize = random.nextInt(numberOfPoints) + 1;

         int removeIndex = random.nextInt(listSize);
         Integer removedElement = pointsCopy.remove(removeIndex);

         EuclidGeometryPolygonTools.remove(points, removeIndex, listSize);

         assertTrue(points.get(listSize - 1) == removedElement);

         for (int j = removeIndex; j < listSize - 1; j++)
            assertTrue(points.get(j) == pointsCopy.get(j));

         for (int j = listSize; j < numberOfPoints; j++)
            assertTrue(points.get(j) == pointsCopy.get(j - 1));
      }
   }

   @Test
   public void testFindMinXMaxYVertexIndex() throws Exception
   {
      Random random = new Random(234234L);

      for (int i = 0; i < ITERATIONS; i++)
      { // Test with numberOfVerticess == list.size()
         int numberOfPoints = 100;
         List<Point2D> points = new ArrayList<>();
         double minX = EuclidCoreRandomTools.generateRandomDouble(random, 5.0);
         double minXMaxY = EuclidCoreRandomTools.generateRandomDouble(random, 5.0);

         for (int j = 0; j < numberOfPoints; j++)
         {
            double x;
            double y;

            if (random.nextDouble() < 0.15)
            {
               x = EuclidCoreRandomTools.generateRandomDouble(random, minX, minX + 10.0);
               y = EuclidCoreRandomTools.generateRandomDouble(random, 10.0);
            }
            else
            {
               x = minX;
               y = EuclidCoreRandomTools.generateRandomDouble(random, minXMaxY - 10.0, minXMaxY);
            }

            points.add(new Point2D(x, y));
         }

         int expectedMinXMaxYIndex = random.nextInt(numberOfPoints);
         points.set(expectedMinXMaxYIndex, new Point2D(minX, minXMaxY));

         int actualMinXMaxYIndex = EuclidGeometryPolygonTools.findMinXMaxYVertexIndex(points, numberOfPoints);

         assertEquals(expectedMinXMaxYIndex, actualMinXMaxYIndex);
      }

      for (int i = 0; i < ITERATIONS; i++)
      { // Test with numberOfVerticess != list.size()
         int numberOfPoints = 100;
         int listSize = numberOfPoints + random.nextInt(100);
         List<Point2D> points = new ArrayList<>();
         double minX = EuclidCoreRandomTools.generateRandomDouble(random, 5.0);
         double minXMaxY = EuclidCoreRandomTools.generateRandomDouble(random, 5.0);

         for (int j = 0; j < listSize; j++)
         {
            double x;
            double y;

            if (random.nextDouble() < 0.15)
            {
               x = EuclidCoreRandomTools.generateRandomDouble(random, minX, minX + 10.0);
               y = EuclidCoreRandomTools.generateRandomDouble(random, 10.0);
            }
            else
            {
               x = minX;
               y = EuclidCoreRandomTools.generateRandomDouble(random, minXMaxY - 10.0, minXMaxY);
            }

            points.add(new Point2D(x, y));
         }

         int expectedMinXMaxYIndex = random.nextInt(numberOfPoints);
         points.set(expectedMinXMaxYIndex, new Point2D(minX, minXMaxY));

         int actualMinXMaxYIndex = EuclidGeometryPolygonTools.findMinXMaxYVertexIndex(points, numberOfPoints);

         assertEquals(expectedMinXMaxYIndex, actualMinXMaxYIndex);
      }
   }
}
