package us.ihmc.euclid.shape.interfaces;

import us.ihmc.euclid.geometry.interfaces.Line3DReadOnly;
import us.ihmc.euclid.geometry.tools.EuclidGeometryTools;
import us.ihmc.euclid.shape.tools.EuclidShapeTools;
import us.ihmc.euclid.tools.EuclidCoreTools;
import us.ihmc.euclid.tuple3D.interfaces.Point3DBasics;
import us.ihmc.euclid.tuple3D.interfaces.Point3DReadOnly;
import us.ihmc.euclid.tuple3D.interfaces.Vector3DBasics;
import us.ihmc.euclid.tuple3D.interfaces.Vector3DReadOnly;

public interface Sphere3DReadOnly extends Shape3DReadOnly
{
   /**
    * Gets the radius of this sphere.
    *
    * @return the value of the radius.
    */
   double getRadius();

   /** {@inheritDoc} */
   @Override
   default boolean containsNaN()
   {
      return Shape3DReadOnly.super.containsNaN() || Double.isNaN(getRadius());
   }

   @Override
   default boolean checkIfInside(Point3DReadOnly pointToCheck, Point3DBasics closestPointOnSurfaceToPack, Vector3DBasics normalAtClosestPointToPack)
   {
      Point3DBasics queryInLocal = getIntermediateVariableSupplier().requestPoint3D();
      getPose().inverseTransform(pointToCheck, queryInLocal);
      boolean isInside = EuclidShapeTools.evaluatePoint3DWithSphere3D(queryInLocal, closestPointOnSurfaceToPack, normalAtClosestPointToPack,
                                                                      getRadius()) <= 0.0;

      getIntermediateVariableSupplier().releasePoint3D(queryInLocal);

      if (closestPointOnSurfaceToPack != null)
         transformToWorld(closestPointOnSurfaceToPack);

      if (normalAtClosestPointToPack != null)
         transformToWorld(normalAtClosestPointToPack);

      return isInside;
   }

   @Override
   default double signedDistance(Point3DReadOnly point)
   {
      Point3DBasics queryInLocal = getIntermediateVariableSupplier().requestPoint3D();
      getPose().inverseTransform(point, queryInLocal);
      double signedDistance = EuclidShapeTools.signedDistanceBetweenPoint3DAndSphere3D(queryInLocal, getRadius());
      getIntermediateVariableSupplier().releasePoint3D(queryInLocal);
      return signedDistance;
   }

   @Override
   default boolean isInsideEpsilon(Point3DReadOnly query, double epsilon)
   {
      Point3DBasics queryInLocal = getIntermediateVariableSupplier().requestPoint3D();
      getPose().inverseTransform(query, queryInLocal);
      boolean isInside = EuclidShapeTools.isPoint3DInsideSphere3D(queryInLocal, getRadius(), epsilon);
      getIntermediateVariableSupplier().releasePoint3D(queryInLocal);
      return isInside;
   }

   /** {@inheritDoc} */
   @Override
   default boolean orthogonalProjection(Point3DReadOnly pointToProject, Point3DBasics projectionToPack)
   {
      // Saving the coordinates in case pointToProject is inside and that pointToProject == projectionToPack.
      double xOriginal = pointToProject.getX();
      double yOriginal = pointToProject.getY();
      double zOriginal = pointToProject.getZ();

      Point3DBasics pointInLocal = getIntermediateVariableSupplier().requestPoint3D();
      getPose().inverseTransform(pointToProject, pointInLocal);

      boolean isInside = EuclidShapeTools.orthogonalProjectionOntoSphere3D(pointInLocal, projectionToPack, getRadius());

      getIntermediateVariableSupplier().releasePoint3D(pointInLocal);

      if (isInside) // Set the coordinates to the original point to save a transform operation
         projectionToPack.set(xOriginal, yOriginal, zOriginal);
      else
         transformToWorld(projectionToPack);

      return !isInside;
   }

   /**
    * Computes the coordinates of the possible intersections between a line and this sphere.
    * <p>
    * In the case the line and this sphere do not intersect, this method returns {@code 0} and
    * {@code firstIntersectionToPack} and {@code secondIntersectionToPack} remain unmodified.
    * </p>
    *
    * @param line the line expressed in world coordinates that may intersect this sphere. Not modified.
    * @param firstIntersectionToPack the coordinate in world of the first intersection. Can be
    *           {@code null}. Modified.
    * @param secondIntersectionToPack the coordinate in world of the second intersection. Can be
    *           {@code null}. Modified.
    * @return the number of intersections between the line and this sphere. It is either equal to 0, 1,
    *         or 2.
    */
   default int intersectionWith(Line3DReadOnly line, Point3DBasics firstIntersectionToPack, Point3DBasics secondIntersectionToPack)
   {
      return intersectionWith(line.getPoint(), line.getDirection(), firstIntersectionToPack, secondIntersectionToPack);
   }

   /**
    * Computes the coordinates of the possible intersections between a line and this sphere.
    * <p>
    * In the case the line and this sphere do not intersect, this method returns {@code 0} and
    * {@code firstIntersectionToPack} and {@code secondIntersectionToPack} are set to
    * {@link Double#NaN}.
    * </p>
    *
    * @param pointOnLine a point expressed in world located on the infinitely long line. Not modified.
    * @param lineDirection the direction expressed in world of the line. Not modified.s
    * @param firstIntersectionToPack the coordinate in world of the first intersection. Can be
    *           {@code null}. Modified.
    * @param secondIntersectionToPack the coordinate in world of the second intersection. Can be
    *           {@code null}. Modified.
    * @return the number of intersections between the line and this sphere. It is either equal to 0, 1,
    *         or 2.
    */
   default int intersectionWith(Point3DReadOnly pointOnLine, Vector3DReadOnly lineDirection, Point3DBasics firstIntersectionToPack,
                               Point3DBasics secondIntersectionToPack)
   {
      Point3DBasics pointOnLineInLocal = getIntermediateVariableSupplier().requestPoint3D();
      Vector3DBasics lineDirectionInLocal = getIntermediateVariableSupplier().requestVector3D();
      getPose().inverseTransform(pointOnLine, pointOnLineInLocal);
      getPose().inverseTransform(lineDirection, lineDirectionInLocal);

      int numberOfIntersections = EuclidGeometryTools.intersectionBetweenLine3DAndEllipsoid3D(getRadius(), getRadius(), getRadius(), pointOnLineInLocal, lineDirectionInLocal,
                                                                                              firstIntersectionToPack, secondIntersectionToPack);

      getIntermediateVariableSupplier().releasePoint3D(pointOnLineInLocal);
      getIntermediateVariableSupplier().releaseVector3D(lineDirectionInLocal);

      if (firstIntersectionToPack != null && numberOfIntersections >= 1)
         transformToWorld(firstIntersectionToPack);
      if (secondIntersectionToPack != null && numberOfIntersections == 2)
         transformToWorld(secondIntersectionToPack);
      return numberOfIntersections;
   }

   /**
    * Tests separately and on a per component basis if the pose and the radius of this sphere and
    * {@code other}'s pose and radius are equal to an {@code epsilon}.
    *
    * @param other the other sphere which pose and radius is to be compared against this radius pose
    *           and radius. Not modified.
    * @param epsilon tolerance to use when comparing each component.
    * @return {@code true} if the two spheres are equal component-wise, {@code false} otherwise.
    */
   default boolean epsilonEquals(Sphere3DReadOnly other, double epsilon)
   {
      return EuclidCoreTools.epsilonEquals(getRadius(), other.getRadius(), epsilon) && getPosition().epsilonEquals(other.getPosition(), epsilon)
            && getOrientation().epsilonEquals(other.getOrientation(), epsilon);
   }

   /**
    * Compares {@code this} to {@code other} to determine if the two spheres are geometrically similar,
    * i.e. the position of each sphere is geometrically similar given {@code epsilon} and the
    * difference between the radius of each sphere is less than or equal to {@code epsilon}.
    *
    * @param other the sphere to compare to. Not modified.
    * @param epsilon the tolerance of the comparison.
    * @return {@code true} if the two boxes represent the same geometry, {@code false} otherwise.
    */
   default boolean geometricallyEquals(Sphere3DReadOnly other, double epsilon)
   {
      return EuclidCoreTools.epsilonEquals(getRadius(), other.getRadius(), epsilon) && getPosition().geometricallyEquals(other.getPosition(), epsilon);
   }
}
