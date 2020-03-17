package us.ihmc.euclid.referenceFrame;

import java.lang.reflect.Method;
import java.util.HashMap;
import java.util.Map;
import java.util.function.Predicate;

import org.junit.jupiter.api.Test;

import us.ihmc.euclid.geometry.BoundingBox2D;
import us.ihmc.euclid.geometry.interfaces.BoundingBox2DReadOnly;
import us.ihmc.euclid.geometry.tools.EuclidGeometryRandomTools;
import us.ihmc.euclid.referenceFrame.api.EuclidFrameAPITester;
import us.ihmc.euclid.referenceFrame.interfaces.ReferenceFrameHolder;

public class FrameBoundingBox2DTest extends FrameBoundingBox2DReadOnlyTest<FrameBoundingBox2D>
{
   @Override
   public FrameBoundingBox2D createFrameBoundingBox(ReferenceFrame referenceFrame, BoundingBox2DReadOnly boundingBox)
   {
      return new FrameBoundingBox2D(referenceFrame, boundingBox);
   }

   @Test
   public void testConsistencyWithBoundingBox2D()
   {
      EuclidFrameAPITester.FrameTypeBuilder<? extends ReferenceFrameHolder> frameTypeBuilder = (frame,
                                                                                                   boundingBox) -> createFrameBoundingBox(frame,
                                                                                                                                          (BoundingBox2DReadOnly) boundingBox);
      EuclidFrameAPITester.GenericTypeBuilder framelessTypeBuilder = EuclidGeometryRandomTools::nextBoundingBox2D;
      Predicate<Method> methodFilter = m -> !m.getName().equals("hashCode") && !m.getName().equals("epsilonEquals");
      EuclidFrameAPITester.assertFrameMethodsOfFrameHolderPreserveFunctionality(frameTypeBuilder, framelessTypeBuilder, methodFilter);
   }

   @Override
   @Test
   public void testOverloading() throws Exception
   {
      super.testOverloading();
      Map<String, Class<?>[]> framelessMethodsToIgnore = new HashMap<>();
      framelessMethodsToIgnore.put("set", new Class<?>[] {BoundingBox2D.class});
      framelessMethodsToIgnore.put("epsilonEquals", new Class<?>[] {BoundingBox2D.class, Double.TYPE});
      framelessMethodsToIgnore.put("geometricallyEquals", new Class<?>[] {BoundingBox2D.class, Double.TYPE});
      EuclidFrameAPITester.assertOverloadingWithFrameObjects(FrameBoundingBox2D.class, BoundingBox2D.class, false, 1, framelessMethodsToIgnore);
   }
}
