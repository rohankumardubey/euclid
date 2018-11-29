package us.ihmc.euclid.shape.convexPolytope.tools;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import java.util.List;
import java.util.Random;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

import org.ejml.data.DenseMatrix64F;
import org.ejml.ops.CommonOps;
import org.junit.Test;

import us.ihmc.euclid.Axis;
import us.ihmc.euclid.geometry.tools.EuclidGeometryTools;
import us.ihmc.euclid.matrix.Matrix3D;
import us.ihmc.euclid.testSuite.EuclidTestSuite;
import us.ihmc.euclid.tools.EuclidCoreRandomTools;
import us.ihmc.euclid.tools.EuclidCoreTestTools;
import us.ihmc.euclid.tuple3D.Point3D;
import us.ihmc.euclid.tuple3D.Vector3D;
import us.ihmc.euclid.tuple3D.interfaces.Point3DReadOnly;

public class EuclidPolytopeToolsTest
{
   private final static int ITERATIONS = EuclidTestSuite.ITERATIONS;
   private static final double EPSILON = 1.0e-12;

   @Test
   public void testEigenVector() throws Exception
   {
      Random random = new Random(4535);

      for (int i = 0; i < ITERATIONS; i++)
      {
         double minX = 500.0;
         double maxX = 1000.0;
         double minY = 50.0;
         double maxY = 100.0;
         double minZ = 0.0;
         double maxZ = 25.0;
         int numberOfPoints = random.nextInt(1000) + 100;
         List<Point3D> points = IntStream.range(0, numberOfPoints).mapToObj(h -> EuclidCoreRandomTools.nextPoint3D(random, minX, maxX, minY, maxY, minZ, maxZ))
                                         .collect(Collectors.toList());
         points.stream().filter(p -> random.nextBoolean()).forEach(Point3D::negate);

         Matrix3D actualCovariance = new Matrix3D();
         Vector3D[] actual = {new Vector3D(), new Vector3D(), new Vector3D()};
         EuclidPolytopeTools.computeCovariance3D(points, null, actualCovariance);
         Point3D eigenValues = new Point3D();
         EuclidPolytopeTools.computeEigenVectors(actualCovariance, eigenValues, actual[0], actual[1], actual[2]);

         Vector3D[] expected = {new Vector3D(Axis.X), new Vector3D(Axis.Y), new Vector3D(Axis.Z)};

         assertTrue(eigenValues.getX() > eigenValues.getY());
         assertTrue(eigenValues.getY() > eigenValues.getZ());

         for (int j = 0; j < 3; j++)
         {
            assertEquals(0.0, Math.abs(actual[j].dot(actual[(j + 1) % 3])), EPSILON);
            assertEquals(1.0, actual[j].length(), EPSILON);

            String errorMessage = "Iteration" + i + ", nPoints: " + numberOfPoints + ", angle: " + expected[j].angle(actual[j]);
            assertTrue(errorMessage, EuclidGeometryTools.areVector3DsParallel(expected[j], actual[j], 0.30));
         }
      }
   }

   @Test
   public void testCovariance3D()
   {
      Random random = new Random(4524523);

      for (int i = 0; i < ITERATIONS; i++)
      {
         int numberOfPoints = random.nextInt(100) + 3;
         double maxAbsoluteX = EuclidCoreRandomTools.nextDouble(random, 0.0, 100.0);
         double maxAbsoluteY = EuclidCoreRandomTools.nextDouble(random, 0.0, 100.0);
         double maxAbsoluteZ = EuclidCoreRandomTools.nextDouble(random, 0.0, 100.0);
         List<Point3D> points = IntStream.range(0, numberOfPoints)
                                         .mapToObj(h -> EuclidCoreRandomTools.nextPoint3D(random, maxAbsoluteX, maxAbsoluteY, maxAbsoluteZ))
                                         .collect(Collectors.toList());

         Matrix3D actualCovariance = new Matrix3D();
         EuclidPolytopeTools.computeCovariance3D(points, null, actualCovariance);
         Matrix3D expectedCovariance = computeCovarianceMatrix(points);

         double maxValue = 1.0;

         for (int row = 0; row < 3; row++)
         {
            for (int column = 0; column < 3; column++)
            {
               maxValue = Math.max(maxValue, Math.abs(expectedCovariance.getElement(row, column)));
            }
         }

         EuclidCoreTestTools.assertMatrix3DEquals(expectedCovariance, actualCovariance, EPSILON * maxValue);
      }
   }

   /**
    * Using the actual formula of the covariance matrix,
    * <a href="https://en.wikipedia.org/wiki/Principal_component_analysis"> here</a>.
    */
   private static Matrix3D computeCovarianceMatrix(List<? extends Point3DReadOnly> dataset)
   {
      DenseMatrix64F covariance = new DenseMatrix64F(3, 3);
      int n = dataset.size();
      DenseMatrix64F datasetMatrix = new DenseMatrix64F(n, 3);

      Point3D average = EuclidGeometryTools.averagePoint3Ds(dataset);

      for (int i = 0; i < n; i++)
      {
         Point3DReadOnly dataPoint = dataset.get(i);
         datasetMatrix.set(i, 0, dataPoint.getX() - average.getX());
         datasetMatrix.set(i, 1, dataPoint.getY() - average.getY());
         datasetMatrix.set(i, 2, dataPoint.getZ() - average.getZ());
      }

      CommonOps.multInner(datasetMatrix, covariance);

      CommonOps.scale(1.0 / (double) n, covariance);

      return new Matrix3D(covariance);
   }
}
