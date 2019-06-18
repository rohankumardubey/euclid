package us.ihmc.euclid.shape.collision.epa;

import java.util.ArrayList;
import java.util.List;
import java.util.PriorityQueue;

import us.ihmc.euclid.shape.collision.EuclidShape3DCollisionResult;
import us.ihmc.euclid.shape.collision.gjk.GJKVertex3D;
import us.ihmc.euclid.shape.collision.gjk.GilbertJohnsonKeerthiCollisionDetector;
import us.ihmc.euclid.shape.collision.interfaces.EuclidShape3DCollisionResultBasics;
import us.ihmc.euclid.shape.collision.interfaces.SupportingVertexHolder;
import us.ihmc.euclid.shape.primitives.interfaces.Shape3DReadOnly;
import us.ihmc.euclid.tools.EuclidCoreTools;
import us.ihmc.euclid.tools.TupleTools;
import us.ihmc.euclid.tuple3D.Vector3D;
import us.ihmc.euclid.tuple3D.interfaces.Point3DReadOnly;

/**
 * Implementation of the Expanding Polytope algorithm used for collision detection.
 * <p>
 * The algorithm is an adaption from the algorithm introduced by Gin van den Bergen in
 * <a href="https://www.taylorfrancis.com/books/9781482297997">Collision Detection in Interactive 3D
 * Environments</a>.
 * </p>
 * <p>
 * This collision detector can be used to detect whether two shapes are colliding or not. When not
 * colliding, the detector provides information about the pair of closest points from each shape.
 * When there is collision, this algorithm provides a pair of points from each shape which represent
 * the endpoints of the collision and the distance separating them is the penetration depth between
 * the two shapes.
 * </p>
 * 
 * @author Sylvain Bertrand
 */
public class ExpandingPolytopeAlgorithm
{
   private static final boolean VERBOSE = false;
   /** The default value for the tolerance used to trigger the terminal condition. */
   public static final double DEFAULT_TERMINAL_CONDITION_EPSILON = 1.0e-12;

   private double epsilon = DEFAULT_TERMINAL_CONDITION_EPSILON;
   /** The limit to the number of iterations in case the algorithm does not succeed to converge. */
   private int maxIterations = 1000;
   /** The number of iterations the last evaluation required. */
   private int numberOfIterations = 0;
   /**
    * GJK collision detector needed to initial this detector in case a simplex is not provided for an
    * evaluation.
    */
   private final GilbertJohnsonKeerthiCollisionDetector gjkCollisionDetector = new GilbertJohnsonKeerthiCollisionDetector();
   /**
    * The face that is the closest to the origin or at the origin resulting from the last collision
    * evaluation.
    */
   private EPAFace3D lastResult = null;

   /**
    * Creates a new collision detector that can be used right away to evaluate collisions.
    */
   public ExpandingPolytopeAlgorithm()
   {
   }

   /**
    * Evaluates the collision state between the two given shapes.
    * <p>
    * This algorithm does not evaluate the surface normals.
    * </p>
    * 
    * @param shapeA the first shape to evaluate. Not modified.
    * @param shapeB the second shape to evaluate. Not modified.
    * @return the collision result.
    */
   public EuclidShape3DCollisionResult evaluateCollision(Shape3DReadOnly shapeA, Shape3DReadOnly shapeB)
   {
      EuclidShape3DCollisionResult result = new EuclidShape3DCollisionResult();
      evaluateCollision(shapeA, shapeB, result);
      return result;
   }

   /**
    * Evaluates the collision state between the two given shapes.
    * <p>
    * This algorithm does not evaluate the surface normals.
    * </p>
    * 
    * @param shapeA       the first shape to evaluate. Not modified.
    * @param shapeB       the second shape to evaluate. Not modified.
    * @param resultToPack the object in which the collision result is stored. Modified.
    * @return {@code true} if the shapes are colliding, {@code false} otherwise.
    */
   public boolean evaluateCollision(Shape3DReadOnly shapeA, Shape3DReadOnly shapeB, EuclidShape3DCollisionResult resultToPack)
   {
      boolean areShapesColliding = gjkCollisionDetector.evaluateCollision(shapeA, shapeB, resultToPack);
      if (areShapesColliding && gjkCollisionDetector.getSimplex() != null)
         evaluateCollision(shapeA, shapeB, gjkCollisionDetector.getSimplex().getVertices(), resultToPack);
      return areShapesColliding;
   }

   /**
    * Evaluates the collision state between the two given shapes given the simplex to start from.
    * <p>
    * This algorithm does not evaluate the surface normals.
    * </p>
    * 
    * @param shapeA       the first shape to evaluate. Not modified.
    * @param shapeB       the second shape to evaluate. Not modified.
    * @param simplex      the simplex used to start the expansion from. Not modified.
    * @param resultToPack the object in which the collision result is stored. Modified.
    * @return {@code true} if the shapes are colliding, {@code false} otherwise.
    */
   public boolean evaluateCollision(Shape3DReadOnly shapeA, Shape3DReadOnly shapeB, GJKVertex3D[] simplex, EuclidShape3DCollisionResult resultToPack)
   {
      boolean areShapesColliding = evaluateCollision((SupportingVertexHolder) shapeA, (SupportingVertexHolder) shapeB, simplex, resultToPack);
      resultToPack.setShapeA(shapeA);
      resultToPack.setShapeB(shapeB);
      return areShapesColliding;
   }

   /**
    * Evaluates the collision state between the two given shapes.
    * <p>
    * This algorithm does not evaluate the surface normals.
    * </p>
    * 
    * @param shapeA the first shape to evaluate. Not modified.
    * @param shapeB the second shape to evaluate. Not modified.
    * @return the collision result.
    */
   public EuclidShape3DCollisionResult evaluateCollision(SupportingVertexHolder shapeA, SupportingVertexHolder shapeB)
   {
      EuclidShape3DCollisionResult result = new EuclidShape3DCollisionResult();
      evaluateCollision(shapeA, shapeB, result);
      return result;
   }

   /**
    * Evaluates the collision state between the two given shapes.
    * <p>
    * This algorithm does not evaluate the surface normals.
    * </p>
    * 
    * @param shapeA       the first shape to evaluate. Not modified.
    * @param shapeB       the second shape to evaluate. Not modified.
    * @param resultToPack the object in which the collision result is stored. Modified.
    * @return {@code true} if the shapes are colliding, {@code false} otherwise.
    */
   public boolean evaluateCollision(SupportingVertexHolder shapeA, SupportingVertexHolder shapeB, EuclidShape3DCollisionResultBasics resultToPack)
   {
      boolean areShapesColliding = gjkCollisionDetector.evaluateCollision(shapeA, shapeB, resultToPack);
      if (areShapesColliding && gjkCollisionDetector.getSimplex() != null)
         evaluateCollision(shapeA, shapeB, gjkCollisionDetector.getSimplex().getVertices(), resultToPack);
      return areShapesColliding;
   }

   /**
    * Evaluates the collision state between the two given shapes.
    * <p>
    * This algorithm does not evaluate the surface normals.
    * </p>
    * 
    * @param shapeA       the first shape to evaluate. Not modified.
    * @param shapeB       the second shape to evaluate. Not modified.
    * @param simplex      the simplex used to start the expansion from. Not modified.
    * @param resultToPack the object in which the collision result is stored. Modified.
    * @return {@code true} if the shapes are colliding, {@code false} otherwise.
    */
   public boolean evaluateCollision(SupportingVertexHolder shapeA, SupportingVertexHolder shapeB, GJKVertex3D[] simplex,
                                    EuclidShape3DCollisionResultBasics resultToPack)
   {
      PriorityQueue<EPAFace3D> queue = new PriorityQueue<>();
      double mu = Double.POSITIVE_INFINITY;

      List<EPAFace3D> initialPolytope = EPATools.newEPAPolytopeFromGJKSimplex(shapeA, shapeB, simplex, epsilon);

      if (initialPolytope == null)
      {
         lastResult = null;
         if (VERBOSE)
            System.out.println("Initial polytope has triangle face that is affinely dependent.");
      }
      else
      {
         initialPolytope.stream().forEach(queue::add);
         Vector3D supportDirection = new Vector3D();
         numberOfIterations = 0;

         while (numberOfIterations < maxIterations)
         {
            if (queue.isEmpty())
            {
               if (VERBOSE)
                  System.out.println("Queue is empty, terminating.");
               break;
            }

            EPAFace3D entry = queue.poll();
            if (entry.isObsolete())
               continue;
            double currentNormSquared = entry.getDistanceSquaredToOrigin();

            numberOfIterations++;

            /*
             * In the original algorithm, the comparison does not add epsilon to mu, but this appears to be
             * needed to handle some edge-cases.
             */
            if (currentNormSquared > mu + epsilon)
            {
               if (VERBOSE)
                  System.out.println("Best norm exceeds upper bound, terminating.");
               break;
            }

            lastResult = entry;

            if (currentNormSquared > epsilon)
               supportDirection.set(entry.getClosestPointToOrigin());
            else
               supportDirection.set(entry.getNormal());

            Point3DReadOnly vertexA = shapeA.getSupportingVertex(supportDirection);
            supportDirection.negate();
            Point3DReadOnly vertexB = shapeB.getSupportingVertex(supportDirection);

            EPAVertex3D newVertex = new EPAVertex3D(vertexA, vertexB);

            if (entry.contains(newVertex))
            {
               if (VERBOSE)
                  System.out.println("New vertex equals to a vertex of entry, terminating.");
               break;
            }

            mu = Math.min(mu, EuclidCoreTools.square(TupleTools.dot(newVertex, supportDirection)) / currentNormSquared);

            if (mu <= EuclidCoreTools.square(1.0 + epsilon) * currentNormSquared)
            {
               if (VERBOSE)
                  System.out.println("Reached max accuracy, terminating.");
               break;
            }

            if (!entry.canObserverSeeFace(newVertex))
            {
               if (VERBOSE)
                  System.out.println("Malformed silhouette, terminating.");
               break;
            }

            entry.markObsolete();
            List<EPAHalfEdge3D> silhouette = new ArrayList<>();
            EPATools.silhouette(entry.getEdge0().getTwin(), newVertex, silhouette);
            EPATools.silhouette(entry.getEdge1().getTwin(), newVertex, silhouette);
            EPATools.silhouette(entry.getEdge2().getTwin(), newVertex, silhouette);

            boolean areNewTrianglesFine = true;

            for (EPAHalfEdge3D sentryEdge : silhouette)
            {
               EPAFace3D newEntry = EPAFace3D.fromVertexAndTwinEdge(newVertex, sentryEdge, epsilon);

               if (newEntry.isTriangleAffinelyDependent())
               {
                  if (VERBOSE)
                     System.out.println("New triangle affinely dependent, terminating.");
                  areNewTrianglesFine = false;
                  break;
               }

               /*
                * Same as above, the original check with mu does not include epsilon, but it seems to be needed to
                * handle some edge-cases.
                */
               if (newEntry.isClosestPointInternal() && currentNormSquared <= newEntry.getDistanceSquaredToOrigin()
                     && newEntry.getDistanceSquaredToOrigin() <= mu + epsilon)
               {
                  queue.add(newEntry);
               }
            }

            if (!areNewTrianglesFine)
               break;

            boolean terminate = false;

            for (int edgeIndex = 0; edgeIndex < newVertex.getNumberOfAssociatedEdges(); edgeIndex++)
            {
               EPAHalfEdge3D edge = newVertex.getAssociatedEdge(edgeIndex);
               EPAHalfEdge3D twin = edge.getDestination().getEdgeTo(newVertex);

               if (twin == null)
               {
                  if (VERBOSE)
                     System.out.println("Could not find twin of an edge, silhouette probably malformed, terminating.");
                  terminate = true;
                  break;
               }

               edge.setTwin(twin);
            }

            if (terminate)
               break;

            for (EPAHalfEdge3D sentryEdge : silhouette)
            {
               EPAVertex3D vertexOnSilhouette = sentryEdge.getOrigin();

               for (int index = vertexOnSilhouette.getNumberOfAssociatedEdges() - 1; index >= 0; index--)
               { // Remove obsolete edges to limit the growth of the internal list.
                  if (vertexOnSilhouette.getAssociatedEdge(index).isObsolete())
                     vertexOnSilhouette.removeAssociatedEdge(index);
               }
            }
         }
      }

      if (initialPolytope == null)
      {
         resultToPack.setToNaN();
      }
      else
      {
         resultToPack.setShapesAreColliding(true);
         resultToPack.setSignedDistance(-lastResult.getDistanceToOrigin());
         lastResult.computePointOnA(resultToPack.getPointOnA());
         lastResult.computePointOnB(resultToPack.getPointOnB());
         resultToPack.getNormalOnA().setToNaN();
         resultToPack.getNormalOnB().setToNaN();
      }

      if (VERBOSE)
         System.out.println("Number of iterations: " + numberOfIterations);

      return true;
   }

   /**
    * Gets the internal GJK collision detector that is used when the initial simplex is not provided
    * for an evaluation.
    * 
    * @return the internal GJK collision detector.
    */
   public GilbertJohnsonKeerthiCollisionDetector getGJKCollisionDetector()
   {
      return gjkCollisionDetector;
   }

   /**
    * Sets the limit to the number of iterations in case the algorithm does not succeed to converge.
    * 
    * @param maxIterations the maximum of iterations allowed before terminating.
    */
   public void setMaxIterations(int maxIterations)
   {
      this.maxIterations = maxIterations;
   }

   /**
    * Sets the tolerance used to trigger the termination condition of this algorithm.
    * 
    * @param epsilon the terminal condition tolerance to use, default value
    *                {@value #DEFAULT_TERMINAL_CONDITION_EPSILON}.
    */
   public void setTerminalConditionEpsilon(double epsilon)
   {
      this.epsilon = epsilon;
   }

   /**
    * Gets the current value of the tolerance used to trigger the termination condition of this
    * algorithm.
    * 
    * @return the current terminal condition tolerance.
    */
   public double getTerminalConditionEpsilon()
   {
      return epsilon;
   }

   /**
    * Gets the number of iterations needed for the last evaluation.
    * 
    * @return the number of iterations from the last evaluation.
    */
   public int getNumberOfIterations()
   {
      return numberOfIterations;
   }

   /**
    * Gets the face that is the closest to the origin or at the origin resulting from the last
    * collision evaluation.
    * 
    * @return the last evaluation resulting face.
    */
   public EPAFace3D getClosestFace()
   {
      return lastResult;
   }
}