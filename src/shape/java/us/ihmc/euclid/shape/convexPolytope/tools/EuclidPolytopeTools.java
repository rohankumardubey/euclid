package us.ihmc.euclid.shape.convexPolytope.tools;

import static us.ihmc.euclid.tools.EuclidCoreTools.normSquared;

import java.util.List;

import org.ejml.data.DenseMatrix64F;
import org.ejml.factory.DecompositionFactory;
import org.ejml.interfaces.decomposition.EigenDecomposition;

import us.ihmc.euclid.geometry.tools.EuclidGeometryTools;
import us.ihmc.euclid.matrix.Matrix3D;
import us.ihmc.euclid.matrix.interfaces.Matrix3DBasics;
import us.ihmc.euclid.matrix.interfaces.Matrix3DReadOnly;
import us.ihmc.euclid.tools.EuclidCoreTools;
import us.ihmc.euclid.tuple3D.Vector3D;
import us.ihmc.euclid.tuple3D.interfaces.Point3DBasics;
import us.ihmc.euclid.tuple3D.interfaces.Point3DReadOnly;
import us.ihmc.euclid.tuple3D.interfaces.Tuple3DBasics;
import us.ihmc.euclid.tuple3D.interfaces.Tuple3DReadOnly;
import us.ihmc.euclid.tuple3D.interfaces.Vector3DBasics;
import us.ihmc.euclid.tuple3D.interfaces.Vector3DReadOnly;

public class EuclidPolytopeTools
{
   private static final double ONE_TRILLIONTH = EuclidGeometryTools.ONE_TRILLIONTH;

   public static boolean isPoint3DOnLeftSideOfLine3D(Point3DReadOnly point, Point3DReadOnly firstPointOnLine, Point3DReadOnly secondPointOnLine,
                                                     Vector3DReadOnly planeNormal)
   {
      return isPoint3DOnSideOfLine3D(point, firstPointOnLine, secondPointOnLine, planeNormal, true);
   }

   public static boolean isPoint3DOnLeftSideOfLine3D(Point3DReadOnly point, Point3DReadOnly pointOnLine, Vector3DReadOnly lineDirection,
                                                     Vector3DReadOnly planeNormal)
   {
      return isPoint3DOnSideOfLine3D(point, pointOnLine, lineDirection, planeNormal, true);
   }

   public static boolean isPoint3DOnRightSideOfLine3D(Point3DReadOnly point, Point3DReadOnly firstPointOnLine, Point3DReadOnly secondPointOnLine,
                                                      Vector3DReadOnly planeNormal)
   {
      return isPoint3DOnSideOfLine3D(point, firstPointOnLine, secondPointOnLine, planeNormal, false);
   }

   public static boolean isPoint3DOnRightSideOfLine3D(Point3DReadOnly point, Point3DReadOnly pointOnLine, Vector3DReadOnly lineDirection,
                                                      Vector3DReadOnly planeNormal)
   {
      return isPoint3DOnSideOfLine3D(point, pointOnLine, lineDirection, planeNormal, false);
   }

   public static boolean isPoint3DOnSideOfLine3D(double pointX, double pointY, double pointZ, double pointOnLineX, double pointOnLineY, double pointOnLineZ,
                                                 double lineDirectionX, double lineDirectionY, double lineDirectionZ, double planeNormalX, double planeNormalY,
                                                 double planeNormalZ, boolean testLeftSide)
   {
      double dx = pointX - pointOnLineX;
      double dy = pointY - pointOnLineY;
      double dz = pointZ - pointOnLineZ;
      // (lineDirection) X (dx, dy, dz)
      double crossX = lineDirectionY * dz - lineDirectionZ * dy;
      double crossY = lineDirectionZ * dx - lineDirectionX * dz;
      double crossZ = lineDirectionX * dy - lineDirectionY * dx;

      double crossProduct = crossX * planeNormalX + crossY * planeNormalY + crossZ * planeNormalZ;

      if (testLeftSide)
         return crossProduct > 0.0;
      else
         return crossProduct < 0.0;
   }

   public static boolean isPoint3DOnSideOfLine3D(Point3DReadOnly point, Point3DReadOnly firstPointOnLine, Point3DReadOnly secondPointOnLine,
                                                 Vector3DReadOnly planeNormal, boolean testLeftSide)
   {
      double pointOnLineX = firstPointOnLine.getX();
      double pointOnLineY = firstPointOnLine.getY();
      double pointOnLineZ = firstPointOnLine.getZ();
      double lineDirectionX = secondPointOnLine.getX() - firstPointOnLine.getX();
      double lineDirectionY = secondPointOnLine.getY() - firstPointOnLine.getY();
      double lineDirectionZ = secondPointOnLine.getZ() - firstPointOnLine.getZ();
      return isPoint3DOnSideOfLine3D(point.getX(), point.getY(), point.getZ(), pointOnLineX, pointOnLineY, pointOnLineZ, lineDirectionX, lineDirectionY,
                                     lineDirectionZ, planeNormal.getX(), planeNormal.getY(), planeNormal.getZ(), testLeftSide);
   }

   public static boolean isPoint3DOnSideOfLine3D(Point3DReadOnly point, Point3DReadOnly pointOnLine, Vector3DReadOnly lineDirection,
                                                 Vector3DReadOnly planeNormal, boolean testLeftSide)
   {
      return isPoint3DOnSideOfLine3D(point.getX(), point.getY(), point.getZ(), pointOnLine.getX(), pointOnLine.getY(), pointOnLine.getZ(), lineDirection.getX(),
                                     lineDirection.getY(), lineDirection.getZ(), planeNormal.getX(), planeNormal.getY(), planeNormal.getZ(), testLeftSide);
   }

   /**
    * Returns the minimum signed distance between the projection of a 3D point and an infinitely long
    * 3D line defined by a point and a direction onto a plane of given normal.
    * <p>
    * The calculated distance is negative if the query is located on the right side of the line. The
    * notion of left side is defined as the semi-open space that starts at {@code pointOnLine} and
    * extends to the direction given by the vector <tt>planeNormal &times; lineDirection</tt> and the
    * right side starts at {@code pointOnLine} and direction
    * <tt>-planeNormal &times; lineDirection</tt>. In an intuitive manner, the left side refers to the
    * side on the left of the line when looking at the line with the plane normal pointing toward you.
    * </p>
    * <p>
    * Note that the position of the plane does not affect the distance separating the query from the
    * line, so it not required.
    * </p>
    * <p>
    * Edge cases:
    * <ul>
    * <li>if {@code lineDirection.length() < }{@value #ONE_TRILLIONTH}, this method returns the
    * distance between {@code pointOnLine} and the given {@code point}.
    * </ul>
    * </p>
    *
    * @param pointX x-coordinate of the query.
    * @param pointY y-coordinate of the query.
    * @param pointZ z-coordinate of the query.
    * @param pointOnLineX x-coordinate of a point located on the line.
    * @param pointOnLineY y-coordinate of a point located on the line.
    * @param pointOnLineZ z-coordinate of a point located on the line.
    * @param lineDirectionX x-component of the line direction.
    * @param lineDirectionY y-component of the line direction.
    * @param lineDirectionZ y-component of the line direction.
    * @param planeNormalX x-component of the plane normal.
    * @param planeNormalY y-component of the plane normal.
    * @param planeNormalZ z-component of the plane normal.
    * @return the minimum distance between the projection of the 3D point and line onto the plane. The
    *         distance is negative if the query is located on the right side of the line.
    */
   public static double signedDistanceFromPoint3DToLine3D(double pointX, double pointY, double pointZ, double pointOnLineX, double pointOnLineY,
                                                          double pointOnLineZ, double lineDirectionX, double lineDirectionY, double lineDirectionZ,
                                                          double planeNormalX, double planeNormalY, double planeNormalZ)
   {
      double directionMagnitude = Math.sqrt(normSquared(lineDirectionX, lineDirectionY, lineDirectionZ));

      double dx = pointOnLineX - pointX;
      double dy = pointOnLineY - pointY;
      double dz = pointOnLineZ - pointZ;

      if (directionMagnitude < ONE_TRILLIONTH)
      {
         return Math.sqrt(normSquared(dx, dy, dz));
      }
      else
      {
         // (lineDirection) X (dx, dy, dz)
         double crossX = lineDirectionY * dz - lineDirectionZ * dy;
         double crossY = lineDirectionZ * dx - lineDirectionX * dz;
         double crossZ = lineDirectionX * dy - lineDirectionY * dx;

         return (crossX * planeNormalX + crossY * planeNormalY + crossZ * planeNormalZ) / directionMagnitude;
      }
   }

   public static double signedDistanceFromPoint3DToLine3D(Point3DReadOnly point, Point3DReadOnly firstPointOnLine, Point3DReadOnly secondPointOnLine,
                                                          Vector3DReadOnly planeNormal)
   {
      double pointOnLineX = firstPointOnLine.getX();
      double pointOnLineY = firstPointOnLine.getY();
      double pointOnLineZ = firstPointOnLine.getZ();
      double lineDirectionX = secondPointOnLine.getX() - firstPointOnLine.getX();
      double lineDirectionY = secondPointOnLine.getY() - firstPointOnLine.getY();
      double lineDirectionZ = secondPointOnLine.getZ() - firstPointOnLine.getZ();
      return signedDistanceFromPoint3DToLine3D(point.getX(), point.getY(), point.getZ(), pointOnLineX, pointOnLineY, pointOnLineZ, lineDirectionX,
                                               lineDirectionY, lineDirectionZ, planeNormal.getX(), planeNormal.getY(), planeNormal.getZ());
   }

   public static double signedDistanceFromPoint3DToLine3D(Point3DReadOnly point, Point3DReadOnly pointOnLine, Vector3DReadOnly lineDirection,
                                                          Vector3DReadOnly planeNormal)
   {
      return signedDistanceFromPoint3DToLine3D(point.getX(), point.getY(), point.getZ(), pointOnLine.getX(), pointOnLine.getY(), pointOnLine.getZ(),
                                               lineDirection.getX(), lineDirection.getY(), lineDirection.getZ(), planeNormal.getX(), planeNormal.getY(),
                                               planeNormal.getZ());
   }

   public static double updateFace3DNormal(List<? extends Point3DReadOnly> vertices, Point3DBasics averageToPack, Vector3DBasics normalToUpdate)
   {
      Matrix3D covariance = new Matrix3D();
      computeCovariance3D(vertices, averageToPack, covariance);
      Vector3D eigenValues = new Vector3D();
      Vector3D newNormal = new Vector3D();
      boolean success = computeEigenVectors(covariance, eigenValues , null, null, newNormal);
      if (!success)
         return Double.NaN;

      if (newNormal.dot(normalToUpdate) < 0.0)
         newNormal.negate();

      normalToUpdate.set(newNormal);

      return EuclidCoreTools.norm(eigenValues.getX(), eigenValues.getY()) / eigenValues.getZ();
   }

   public static boolean computeEigenVectors(Matrix3DReadOnly matrix, Tuple3DBasics eigenValues, Vector3DBasics firstEigenVector,
                                             Vector3DBasics secondEigenVector, Vector3DBasics thirdEigenVector)
   {
      DenseMatrix64F denseMatrix = new DenseMatrix64F(3, 3);
      matrix.get(denseMatrix);

      EigenDecomposition<DenseMatrix64F> eig = DecompositionFactory.eig(3, true, true);
      if (!eig.decompose(denseMatrix))
         return false;

      if (eigenValues != null)
      {
         eigenValues.setX(eig.getEigenvalue(0).getReal());
         eigenValues.setY(eig.getEigenvalue(1).getReal());
         eigenValues.setZ(eig.getEigenvalue(2).getReal());
      }

      if (firstEigenVector != null)
         firstEigenVector.set(eig.getEigenVector(0));
      if (secondEigenVector != null)
         secondEigenVector.set(eig.getEigenVector(1));
      if (thirdEigenVector != null)
         thirdEigenVector.set(eig.getEigenVector(2));

      return true;
   }

   public static void computeCovariance3D(List<? extends Tuple3DReadOnly> input, Tuple3DBasics averageToPack, Matrix3DBasics covarianceToPack)
   {
      double meanX = 0.0;
      double meanY = 0.0;
      double meanZ = 0.0;

      for (int i = 0; i < input.size(); i++)
      {
         Tuple3DReadOnly element = input.get(i);
         meanX += element.getX();
         meanY += element.getY();
         meanZ += element.getZ();
      }

      double inverseOfInputSize = 1.0 / input.size();

      meanX *= inverseOfInputSize;
      meanY *= inverseOfInputSize;
      meanZ *= inverseOfInputSize;

      if (averageToPack != null)
      {
         averageToPack.set(meanX, meanY, meanZ);
      }

      covarianceToPack.setToZero();

      for (int i = 0; i < input.size(); i++)
      {
         Tuple3DReadOnly element = input.get(i);
         double devX = element.getX() - meanX;
         double devY = element.getY() - meanY;
         double devZ = element.getZ() - meanZ;

         double covXX = devX * devX * inverseOfInputSize;
         double covYY = devY * devY * inverseOfInputSize;
         double covZZ = devZ * devZ * inverseOfInputSize;
         double covXY = devX * devY * inverseOfInputSize;
         double covXZ = devX * devZ * inverseOfInputSize;
         double covYZ = devY * devZ * inverseOfInputSize;

         covarianceToPack.addM00(covXX);
         covarianceToPack.addM11(covYY);
         covarianceToPack.addM22(covZZ);
         covarianceToPack.addM01(covXY);
         covarianceToPack.addM10(covXY);
         covarianceToPack.addM02(covXZ);
         covarianceToPack.addM20(covXZ);
         covarianceToPack.addM12(covYZ);
         covarianceToPack.addM21(covYZ);
      }
   }
}
