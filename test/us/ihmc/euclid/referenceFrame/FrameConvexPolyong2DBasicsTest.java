package us.ihmc.euclid.referenceFrame;

import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.util.HashMap;
import java.util.Map;
import java.util.Random;
import java.util.function.Predicate;

import org.junit.Test;

import us.ihmc.euclid.geometry.ConvexPolygon2DTest;
import us.ihmc.euclid.geometry.interfaces.ConvexPolygon2DBasics;
import us.ihmc.euclid.geometry.interfaces.ConvexPolygon2DReadOnly;
import us.ihmc.euclid.geometry.tools.EuclidGeometryRandomTools;
import us.ihmc.euclid.referenceFrame.interfaces.FrameConvexPolygon2DBasics;
import us.ihmc.euclid.referenceFrame.interfaces.FrameTuple2DBasics;
import us.ihmc.euclid.referenceFrame.interfaces.ReferenceFrameHolder;
import us.ihmc.euclid.referenceFrame.tools.EuclidFrameAPITestTools;
import us.ihmc.euclid.referenceFrame.tools.EuclidFrameAPITestTools.FrameTypeBuilder;
import us.ihmc.euclid.referenceFrame.tools.EuclidFrameAPITestTools.GenericTypeBuilder;
import us.ihmc.euclid.tuple2D.interfaces.Tuple2DBasics;

public abstract class FrameConvexPolyong2DBasicsTest<F extends FrameConvexPolygon2DBasics> extends FrameConvexPolygon2DReadOnlyTest<F>
{
   public ConvexPolygon2DBasics createRandomFramelessConvexPolygon2D(Random random)
   {
      return EuclidGeometryRandomTools.nextConvexPolygon2D(random, 1.0, 10);
   }

   @Override
   public void testOverloading() throws Exception
   {
      super.testOverloading();
      Map<String, Class<?>[]> framelessMethodsToIgnore = new HashMap<>();
      EuclidFrameAPITestTools.assertOverloadingWithFrameObjects(FrameTuple2DBasics.class, Tuple2DBasics.class, true, 1, framelessMethodsToIgnore);
   }

   @Test
   public void testReferenceFrameChecks() throws Throwable
   {
      Random random = new Random(234);
      Predicate<Method> methodFilter = m -> !m.getName().contains("IncludingFrame") && !m.getName().contains("MatchingFrame") && !m.getName().equals("equals")
            && !m.getName().equals("epsilonEquals");
      EuclidFrameAPITestTools.assertMethodsOfReferenceFrameHolderCheckReferenceFrame(frame -> createRandomFrameConvexPolygon2D(random, frame), methodFilter);
   }

   @Test
   public void testConsistencyWithTuple2D() throws Exception
   {
      Random random = new Random(3422);
      FrameTypeBuilder<? extends ReferenceFrameHolder> frameTypeBuilder = (frame, polygon) -> createFrameConvexPolygon2D(frame,
                                                                                                                         (ConvexPolygon2DReadOnly) polygon);
      GenericTypeBuilder framelessTypeBuilber = () -> createRandomFramelessConvexPolygon2D(random);
      Predicate<Method> methodFilter = m -> !m.getName().equals("hashCode");
      EuclidFrameAPITestTools.assertFrameMethodsOfFrameHolderPreserveFunctionality(frameTypeBuilder, framelessTypeBuilber, methodFilter);
   }

   @Test
   public void testConvexPolygon2DBasicsFeatures() throws Exception
   {
      ConvexPolygon2DTest convexPolygonTest = new ConvexPolygon2DTest();

      for (Method testMethod : convexPolygonTest.getClass().getMethods())
      {
         if (!testMethod.getName().startsWith("test"))
            continue;
         if (!Modifier.isPublic(testMethod.getModifiers()))
            continue;
         if (Modifier.isStatic(testMethod.getModifiers()))
            continue;

         testMethod.invoke(convexPolygonTest);
      }
   }
}
