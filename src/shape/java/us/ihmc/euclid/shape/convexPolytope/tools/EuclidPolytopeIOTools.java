package us.ihmc.euclid.shape.convexPolytope.tools;

import static us.ihmc.euclid.tools.EuclidCoreIOTools.*;

import java.util.Collection;
import java.util.Iterator;
import java.util.function.Function;

import us.ihmc.euclid.geometry.interfaces.LineSegment3DReadOnly;
import us.ihmc.euclid.shape.convexPolytope.interfaces.ConvexPolytope3DReadOnly;
import us.ihmc.euclid.shape.convexPolytope.interfaces.Face3DReadOnly;
import us.ihmc.euclid.shape.convexPolytope.interfaces.HalfEdge3DReadOnly;
import us.ihmc.euclid.shape.convexPolytope.interfaces.Vertex3DReadOnly;
import us.ihmc.euclid.tuple3D.interfaces.Point3DReadOnly;
import us.ihmc.euclid.tuple3D.interfaces.Tuple3DReadOnly;
import us.ihmc.euclid.tuple3D.interfaces.Vector3DReadOnly;

public class EuclidPolytopeIOTools
{

   public static String getVertex3DString(Vertex3DReadOnly vertex3D)
   {
      return getVertex3DString(DEFAULT_FORMAT, vertex3D);
   }

   public static String getVertex3DString(String format, Vertex3DReadOnly vertex3D)
   {
      return getVertex3DString(format, vertex3D, vertex3D.getAssociatedEdges());
   }

   public static String getVertex3DString(String format, Tuple3DReadOnly vertex3DPosition, Collection<? extends HalfEdge3DReadOnly> vertexHalfEdges)
   {
      String string = "Vertex 3D: " + getTuple3DString(format, vertex3DPosition) + ", number of edges: " + vertexHalfEdges.size();
      string += getHalfEdge3DCollectionString(format, "\n\t", vertexHalfEdges);
      return string;
   }

   public static String getHalfEdge3DString(HalfEdge3DReadOnly halfEdge3D)
   {
      return getHalfEdge3DString(DEFAULT_FORMAT, halfEdge3D);
   }

   public static String getHalfEdge3DString(String format, HalfEdge3DReadOnly halfEdge3D)
   {
      if (halfEdge3D == null)
         return "null";
      return getHalfEdge3DString(format, halfEdge3D, halfEdge3D.getTwinEdge(), halfEdge3D.getNextEdge(), halfEdge3D.getPreviousEdge(), halfEdge3D.getFace());
   }

   public static String getHalfEdge3DString(String format, LineSegment3DReadOnly edgeSupportingSegment, HalfEdge3DReadOnly twinEdge,
                                            HalfEdge3DReadOnly nextEdge, HalfEdge3DReadOnly previousEdge, Face3DReadOnly face)
   {
      String string = "Half-edge 3D: " + getLineSegment3DShortString(format, edgeSupportingSegment);
      string += "\n\tTwin    : " + getLineSegment3DShortString(format, twinEdge);
      string += "\n\tNext    : " + getLineSegment3DShortString(format, nextEdge);
      string += "\n\tPrevious: " + getLineSegment3DShortString(format, previousEdge);
      string += "\n\tFace: " + getFace3DShortString(format, face);
      return string;
   }

   public static String getFace3DString(Face3DReadOnly face3D)
   {
      return getFace3DString(DEFAULT_FORMAT, face3D);
   }

   public static String getFace3DString(String format, Face3DReadOnly face3D)
   {
      if (face3D == null)
         return "null";
      return getFace3DString(format, face3D.getCentroid(), face3D.getNormal(), face3D.getEdges());
   }

   public static String getFace3DString(String format, Point3DReadOnly centroid, Vector3DReadOnly normal, Collection<? extends HalfEdge3DReadOnly> faceEdges)
   {
      String string = "Face 3D: " + getFace3DShortString(format, centroid, normal) + ", number of edges: " + faceEdges.size()
            + getHalfEdge3DCollectionString(format, "\n\t", faceEdges);
      return string;
   }

   public static String getConvexPolytope3DString(ConvexPolytope3DReadOnly convexPolytope3D)
   {
      return getConvexPolytope3DString(DEFAULT_FORMAT, convexPolytope3D);
   }

   public static String getConvexPolytope3DString(String format, ConvexPolytope3DReadOnly convexPolytope3D)
   {
      if (convexPolytope3D == null)
         return "null";
      return getConvexPolytope3DString(format, convexPolytope3D.getVertices(), convexPolytope3D.getEdges(), convexPolytope3D.getFaces());
   }

   public static String getConvexPolytope3DString(String format, Collection<? extends Vertex3DReadOnly> polytopeVertices,
                                                  Collection<? extends HalfEdge3DReadOnly> polytopeEdges, Collection<? extends Face3DReadOnly> polytopeFaces)
   {
      String string = "Convex polytope 3D: number of: [faces: " + polytopeFaces.size() + ", edges: " + polytopeEdges.size() + ", vertices: "
            + polytopeVertices.size();
      String linePrefix = "\n\t";
      string += "\nFace list: " + getFace3DCollectionString(format, linePrefix, polytopeFaces);
      string += "\nEdge list: " + getHalfEdge3DCollectionString(format, linePrefix, polytopeEdges);
      string += "\nVertex list: " + getVertex3DCollectionString(format, linePrefix, polytopeVertices);
      return string;
   }

   private static String getVertex3DCollectionString(String format, String linePrefix, Collection<? extends Vertex3DReadOnly> vertices)
   {
      return getCollectionString(linePrefix, null, linePrefix, format, vertices, vertex -> getTuple3DString(format, vertex));
   }

   private static String getHalfEdge3DCollectionString(String format, String linePrefix, Collection<? extends HalfEdge3DReadOnly> halfEdges)
   {
      return getCollectionString(linePrefix, null, linePrefix, format, halfEdges, halfEdge -> getLineSegment3DShortString(format, halfEdge));
   }

   private static String getFace3DCollectionString(String format, String linePrefix, Collection<? extends Face3DReadOnly> faces)
   {
      return getCollectionString(linePrefix, null, linePrefix, format, faces, face -> getFace3DShortString(format, face));
   }

   public static <T> String getCollectionString(String prefix, String suffix, String separator, Collection<T> collection,
                                                Function<T, String> elementToStringFunction)
   {
      return getCollectionString(prefix, suffix, separator, DEFAULT_FORMAT, collection, elementToStringFunction);
   }

   public static <T> String getCollectionString(String prefix, String suffix, String separator, String format, Collection<T> collection,
                                                Function<T, String> elementToStringFunction)
   {
      if (collection == null)
         return "null";

      String ret = getCollectionString(separator, format, collection, elementToStringFunction);

      if (prefix != null)
         ret = prefix + ret;

      if (suffix != null)
         ret += suffix;

      return ret;
   }

   public static <T> String getCollectionString(String separator, String format, Collection<T> collection, Function<T, String> elementToStringFunction)
   {
      if (collection == null)
         return "null";
      if (collection.isEmpty())
         return "";

      Iterator<T> iterator = collection.iterator();
      String ret = elementToStringFunction.apply(iterator.next());
      while (iterator.hasNext())
         ret += separator + elementToStringFunction.apply(iterator.next());
      return ret;
   }

   private static String getLineSegment3DShortString(String format, LineSegment3DReadOnly edgeSupportingSegment)
   {
      if (edgeSupportingSegment == null)
         return null;
      return "[" + getTuple3DString(format, edgeSupportingSegment.getFirstEndpoint()) + "; "
            + getTuple3DString(format, edgeSupportingSegment.getSecondEndpoint()) + "]";
   }

   private static String getFace3DShortString(String format, Face3DReadOnly face3D)
   {
      if (face3D == null)
         return "null";
      return getFace3DShortString(format, face3D.getCentroid(), face3D.getNormal());
   }

   private static String getFace3DShortString(String format, Point3DReadOnly faceCentroid, Vector3DReadOnly faceNormal)
   {
      return "centroid: " + getTuple3DString(format, faceCentroid) + ", normal: " + getTuple3DString(format, faceNormal);
   }
}
