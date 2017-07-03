package us.ihmc.euclid.referenceFrame;

import static org.junit.Assert.*;

import java.util.Random;

import org.junit.Test;

import us.ihmc.euclid.interfaces.GeometryObject;
import us.ihmc.euclid.referenceFrame.exceptions.ReferenceFrameMismatchException;
import us.ihmc.euclid.referenceFrame.tools.EuclidFrameRandomTools;
import us.ihmc.euclid.tools.EuclidCoreRandomTools;
import us.ihmc.euclid.transform.RigidBodyTransform;

public abstract class FrameGeometryObjectTest<F extends FrameGeometryObject<F, G>, G extends GeometryObject<G>>
{
   public static final double EPSILON = 1.0e-12;
   public static final int ITERATIONS = 100;

   protected abstract G createEmptyGeometryObject();

   protected abstract G createRandomGeometryObject(Random random);

   protected abstract F createEmptyFrameGeometryObject(ReferenceFrame referenceFrame);

   protected abstract F createFrameGeometryObject(ReferenceFrame referenceFrame, G geometryObject);

   protected abstract F createRandomFrameGeometryObject(Random random, ReferenceFrame referenceFrame);

   @Test
   public void testSetToZero() throws Exception
   {
      Random random = new Random(234234L);

      for (int i = 0; i < ITERATIONS; i++)
      {
         ReferenceFrame[] referenceFrames = EuclidFrameRandomTools.generateRandomReferenceFrameTree(random);

         G expectedGeometryObject = createRandomGeometryObject(random);
         expectedGeometryObject.setToZero();

         ReferenceFrame initialFrame = referenceFrames[random.nextInt(referenceFrames.length)];
         F frameGeometryObject = createRandomFrameGeometryObject(random, initialFrame);
         assertEquals(initialFrame, frameGeometryObject.getReferenceFrame());
         assertFalse(expectedGeometryObject.epsilonEquals(frameGeometryObject.getGeometryObject(), EPSILON));
         frameGeometryObject.setToZero();
         assertTrue(expectedGeometryObject.epsilonEquals(frameGeometryObject.getGeometryObject(), EPSILON));

         frameGeometryObject = createRandomFrameGeometryObject(random, initialFrame);
         ReferenceFrame newFrame = referenceFrames[random.nextInt(referenceFrames.length)];

         assertEquals(initialFrame, frameGeometryObject.getReferenceFrame());
         assertFalse(expectedGeometryObject.epsilonEquals(frameGeometryObject.getGeometryObject(), EPSILON));
         frameGeometryObject.setToZero(newFrame);
         assertEquals(newFrame, frameGeometryObject.getReferenceFrame());
         assertTrue(expectedGeometryObject.epsilonEquals(frameGeometryObject.getGeometryObject(), EPSILON));
      }
   }

   @Test
   public void testSetToNaN() throws Exception
   {
      Random random = new Random(574);

      for (int i = 0; i < ITERATIONS; i++)
      {
         ReferenceFrame[] referenceFrames = EuclidFrameRandomTools.generateRandomReferenceFrameTree(random);

         ReferenceFrame initialFrame = referenceFrames[random.nextInt(referenceFrames.length)];
         F frameGeometryObject = createRandomFrameGeometryObject(random, initialFrame);
         assertEquals(initialFrame, frameGeometryObject.getReferenceFrame());
         assertFalse(frameGeometryObject.getGeometryObject().containsNaN());
         frameGeometryObject.setToZero();
         assertTrue(frameGeometryObject.getGeometryObject().containsNaN());

         frameGeometryObject = createRandomFrameGeometryObject(random, initialFrame);
         ReferenceFrame newFrame = referenceFrames[random.nextInt(referenceFrames.length)];

         assertEquals(initialFrame, frameGeometryObject.getReferenceFrame());
         assertFalse(frameGeometryObject.getGeometryObject().containsNaN());
         frameGeometryObject.setToZero(newFrame);
         assertEquals(newFrame, frameGeometryObject.getReferenceFrame());
         assertTrue(frameGeometryObject.getGeometryObject().containsNaN());
      }
   }

   @Test
   public void testContains() throws Exception
   {
      Random random = new Random(63);

      for (int i = 0; i < ITERATIONS; i++)
      {
         ReferenceFrame[] referenceFrames = EuclidFrameRandomTools.generateRandomReferenceFrameTree(random);

         ReferenceFrame initialFrame = referenceFrames[random.nextInt(referenceFrames.length)];
         F frameGeometryObject = createRandomFrameGeometryObject(random, initialFrame);
         assertEquals(initialFrame, frameGeometryObject.getReferenceFrame());
         assertFalse(frameGeometryObject.containsNaN());
         assertFalse(frameGeometryObject.getGeometryObject().containsNaN());
         frameGeometryObject.setToZero();
         assertTrue(frameGeometryObject.containsNaN());
         assertTrue(frameGeometryObject.getGeometryObject().containsNaN());

         frameGeometryObject = createRandomFrameGeometryObject(random, initialFrame);
         ReferenceFrame newFrame = referenceFrames[random.nextInt(referenceFrames.length)];

         assertEquals(initialFrame, frameGeometryObject.getReferenceFrame());
         assertFalse(frameGeometryObject.containsNaN());
         assertFalse(frameGeometryObject.getGeometryObject().containsNaN());
         frameGeometryObject.setToZero(newFrame);
         assertEquals(newFrame, frameGeometryObject.getReferenceFrame());
         assertTrue(frameGeometryObject.containsNaN());
         assertTrue(frameGeometryObject.getGeometryObject().containsNaN());
      }
   }

   @Test
   public void testSet() throws Exception
   {
      Random random = new Random(3454);

      for (int i = 0; i < ITERATIONS; i++)
      { // Tests set(G geometryObject)
         ReferenceFrame[] referenceFrames = EuclidFrameRandomTools.generateRandomReferenceFrameTree(random);

         G expectedGeometry = createRandomGeometryObject(random);

         ReferenceFrame initialFrame = referenceFrames[random.nextInt(referenceFrames.length)];
         F frameGeometry = createRandomFrameGeometryObject(random, initialFrame);

         assertFalse(expectedGeometry.epsilonEquals(frameGeometry.getGeometryObject(), EPSILON));

         frameGeometry.set(expectedGeometry);

         assertTrue(expectedGeometry.epsilonEquals(frameGeometry.getGeometryObject(), EPSILON));
         assertFalse(expectedGeometry == frameGeometry.getGeometryObject());
         assertEquals(initialFrame, frameGeometry.getReferenceFrame());
      }

      for (int i = 0; i < ITERATIONS; i++)
      { // Tests set(ReferenceFrame referenceFrame, G geometryObject)
         ReferenceFrame[] referenceFrames = EuclidFrameRandomTools.generateRandomReferenceFrameTree(random);

         G expectedGeometry = createRandomGeometryObject(random);

         int initialFrameIndex = random.nextInt(referenceFrames.length);
         ReferenceFrame initialFrame = referenceFrames[initialFrameIndex];
         F frameGeometry = createRandomFrameGeometryObject(random, initialFrame);

         assertFalse(expectedGeometry.epsilonEquals(frameGeometry.getGeometryObject(), EPSILON));

         frameGeometry.set(initialFrame, expectedGeometry);

         assertTrue(expectedGeometry.epsilonEquals(frameGeometry.getGeometryObject(), EPSILON));
         assertFalse(expectedGeometry == frameGeometry.getGeometryObject());
         assertEquals(initialFrame, frameGeometry.getReferenceFrame());

         frameGeometry.set(createRandomGeometryObject(random));

         assertFalse(expectedGeometry.epsilonEquals(frameGeometry.getGeometryObject(), EPSILON));

         expectedGeometry.set(frameGeometry.getGeometryObject());

         int differenceFrameIndex = initialFrameIndex + random.nextInt(referenceFrames.length - 1);
         differenceFrameIndex %= referenceFrames.length;
         ReferenceFrame differentFrame = referenceFrames[differenceFrameIndex];

         try
         {
            frameGeometry.set(differentFrame, createRandomGeometryObject(random));
            fail("Should have thrown a ReferenceFrameMismatchException");
         }
         catch (ReferenceFrameMismatchException e)
         {
            // good
            assertTrue(expectedGeometry.epsilonEquals(frameGeometry.getGeometryObject(), EPSILON));
         }
      }

      for (int i = 0; i < ITERATIONS; i++)
      { // Tests set(F other)
         ReferenceFrame[] referenceFrames = EuclidFrameRandomTools.generateRandomReferenceFrameTree(random);

         int initialFrameIndex = random.nextInt(referenceFrames.length);
         ReferenceFrame initialFrame = referenceFrames[initialFrameIndex];
         F frameGeometry1 = createRandomFrameGeometryObject(random, initialFrame);
         F frameGeometry2 = createRandomFrameGeometryObject(random, initialFrame);

         assertFalse(frameGeometry1.getGeometryObject().epsilonEquals(frameGeometry2.getGeometryObject(), EPSILON));
         assertFalse(frameGeometry1.epsilonEquals(frameGeometry2, EPSILON));

         frameGeometry1.set(frameGeometry2);

         assertTrue(frameGeometry1.epsilonEquals(frameGeometry2, EPSILON));
         assertTrue(frameGeometry1.getGeometryObject().epsilonEquals(frameGeometry2.getGeometryObject(), EPSILON));
         assertEquals(initialFrame, frameGeometry1.getReferenceFrame());
         assertEquals(initialFrame, frameGeometry2.getReferenceFrame());

         frameGeometry1.set(createRandomGeometryObject(random));

         assertFalse(frameGeometry1.getGeometryObject().epsilonEquals(frameGeometry2.getGeometryObject(), EPSILON));

         frameGeometry2.getGeometryObject().set(frameGeometry1.getGeometryObject());

         int differenceFrameIndex = initialFrameIndex + random.nextInt(referenceFrames.length - 1);
         differenceFrameIndex %= referenceFrames.length;
         ReferenceFrame differentFrame = referenceFrames[differenceFrameIndex];

         try
         {
            frameGeometry1.set(createRandomFrameGeometryObject(random, differentFrame));
            fail("Should have thrown a ReferenceFrameMismatchException");
         }
         catch (ReferenceFrameMismatchException e)
         {
            // good
            assertTrue(frameGeometry1.epsilonEquals(frameGeometry2, EPSILON));
         }
      }
   }

   @Test
   public void testSetIncludingFrame() throws Exception
   {
      Random random = new Random(3453456);

      for (int i = 0; i < ITERATIONS; i++)
      { // Tests setIncludingFrame(ReferenceFrame referenceFrame, G geometryObject)
         ReferenceFrame[] referenceFrames = EuclidFrameRandomTools.generateRandomReferenceFrameTree(random);
         ReferenceFrame initialFrame = referenceFrames[random.nextInt(referenceFrames.length)];
         ReferenceFrame anotherFrame = referenceFrames[random.nextInt(referenceFrames.length)];

         G expectedGeometry = createRandomGeometryObject(random);

         F frameGeometry = createRandomFrameGeometryObject(random, initialFrame);

         assertFalse(expectedGeometry.epsilonEquals(frameGeometry.getGeometryObject(), EPSILON));
         assertEquals(initialFrame, frameGeometry.getReferenceFrame());

         frameGeometry.setIncludingFrame(anotherFrame, expectedGeometry);

         assertTrue(expectedGeometry.epsilonEquals(frameGeometry.getGeometryObject(), EPSILON));
         assertEquals(anotherFrame, frameGeometry.getReferenceFrame());
      }

      for (int i = 0; i < ITERATIONS; i++)
      { // Tests setIncludingFrame(F other)
         ReferenceFrame[] referenceFrames = EuclidFrameRandomTools.generateRandomReferenceFrameTree(random);
         ReferenceFrame initialFrame = referenceFrames[random.nextInt(referenceFrames.length)];
         ReferenceFrame anotherFrame = referenceFrames[random.nextInt(referenceFrames.length)];

         F frameGeometry1 = createRandomFrameGeometryObject(random, initialFrame);
         F frameGeometry2 = createRandomFrameGeometryObject(random, anotherFrame);

         assertFalse(frameGeometry1.epsilonEquals(frameGeometry2, EPSILON));
         assertFalse(frameGeometry1.getGeometryObject().epsilonEquals(frameGeometry2.getGeometryObject(), EPSILON));
         assertEquals(initialFrame, frameGeometry1.getReferenceFrame());
         assertEquals(anotherFrame, frameGeometry2.getReferenceFrame());

         frameGeometry1.setIncludingFrame(frameGeometry2);

         assertTrue(frameGeometry1.epsilonEquals(frameGeometry2, EPSILON));
         assertTrue(frameGeometry1.getGeometryObject().epsilonEquals(frameGeometry2.getGeometryObject(), EPSILON));
         assertEquals(anotherFrame, frameGeometry1.getReferenceFrame());
         assertEquals(anotherFrame, frameGeometry2.getReferenceFrame());
      }
   }

   @Test
   public void testSetFromReferenceFrame() throws Exception
   {
      Random random = new Random(6572);

      for (int i = 0; i < ITERATIONS; i++)
      {
         ReferenceFrame[] referenceFrames = EuclidFrameRandomTools.generateRandomReferenceFrameTree(random);
         ReferenceFrame initialFrame = referenceFrames[random.nextInt(referenceFrames.length)];
         ReferenceFrame anotherFrame = referenceFrames[random.nextInt(referenceFrames.length)];

         G expectedGeometry = createRandomGeometryObject(random);
         expectedGeometry.setToZero();
         RigidBodyTransform transform = initialFrame.getTransformToDesiredFrame(anotherFrame);
         expectedGeometry.applyTransform(transform);

         F frameGeometry = createRandomFrameGeometryObject(random, initialFrame);
         frameGeometry.setFromReferenceFrame(anotherFrame);
         assertTrue(initialFrame == frameGeometry.getReferenceFrame());
         assertTrue(expectedGeometry.epsilonEquals(frameGeometry.getGeometryObject(), EPSILON));
      }
   }

   @Test
   public void testChangeFrame() throws Exception
   {
      Random random = new Random(43563);

      for (int i = 0; i < ITERATIONS; i++)
      {
         ReferenceFrame[] referenceFrames = EuclidFrameRandomTools.generateRandomReferenceFrameTree(random);
         ReferenceFrame initialFrame = referenceFrames[random.nextInt(referenceFrames.length)];
         ReferenceFrame anotherFrame = referenceFrames[random.nextInt(referenceFrames.length)];

         G expectedGeometry = createRandomGeometryObject(random);

         F frameGeometry = createFrameGeometryObject(initialFrame, expectedGeometry);

         RigidBodyTransform transform = initialFrame.getTransformToDesiredFrame(anotherFrame);
         expectedGeometry.applyTransform(transform);

         frameGeometry.changeFrame(anotherFrame);
         assertTrue(anotherFrame == frameGeometry.getReferenceFrame());
         assertTrue(expectedGeometry.epsilonEquals(frameGeometry.getGeometryObject(), EPSILON));

         ReferenceFrame differentRootFrame = ReferenceFrame.constructARootFrame("anotherRootFrame");
         try
         {
            frameGeometry.changeFrame(differentRootFrame);
            fail("Should have thrown a RuntimeException");
         }
         catch (RuntimeException e)
         {
            // good
         }
      }
   }

   @Test
   public void testApplyTransform() throws Exception
   {
      Random random = new Random(43563);

      for (int i = 0; i < ITERATIONS; i++)
      { // Test with a 3D transform, it might throw an exception
         ReferenceFrame[] referenceFrames = EuclidFrameRandomTools.generateRandomReferenceFrameTree(random);
         ReferenceFrame initialFrame = referenceFrames[random.nextInt(referenceFrames.length)];

         G expectedGeometry = createRandomGeometryObject(random);

         F frameGeometry = createFrameGeometryObject(initialFrame, expectedGeometry);

         RigidBodyTransform transform = EuclidCoreRandomTools.generateRandomRigidBodyTransform(random);

         boolean shouldThrowAndException = false;
         Class<?> exceptionClass = null;

         try
         {
            expectedGeometry.applyTransform(transform);
         }
         catch (Exception e)
         {
            shouldThrowAndException = true;
            exceptionClass = e.getClass();
         }

         if (!shouldThrowAndException)
         {
            frameGeometry.applyTransform(transform);
            assertTrue(initialFrame == frameGeometry.getReferenceFrame());
            assertTrue(expectedGeometry.epsilonEquals(frameGeometry.getGeometryObject(), EPSILON));
         }
         else
         {
            try
            {
               frameGeometry.applyTransform(transform);
               fail("Should have thrown a " + exceptionClass.getSimpleName());
            }
            catch (Exception e)
            {
               assertEquals(exceptionClass, e.getClass());
            }
         }
      }

      for (int i = 0; i < ITERATIONS; i++)
      { // Test with a 2D transform, it should not throw an exception
         ReferenceFrame[] referenceFrames = EuclidFrameRandomTools.generateRandomReferenceFrameTree(random);
         ReferenceFrame initialFrame = referenceFrames[random.nextInt(referenceFrames.length)];

         G expectedGeometry = createRandomGeometryObject(random);

         F frameGeometry = createFrameGeometryObject(initialFrame, expectedGeometry);

         RigidBodyTransform transform = EuclidCoreRandomTools.generateRandomRigidBodyTransform2D(random);
         expectedGeometry.applyTransform(transform);

         frameGeometry.applyTransform(transform);
         assertTrue(initialFrame == frameGeometry.getReferenceFrame());
         assertTrue(expectedGeometry.epsilonEquals(frameGeometry.getGeometryObject(), EPSILON));
      }
   }

   @Test
   public void testApplyInverseTransform() throws Exception
   {
      Random random = new Random(43563);

      for (int i = 0; i < ITERATIONS; i++)
      { // Test with a 3D transform, it might throw an exception
         ReferenceFrame[] referenceFrames = EuclidFrameRandomTools.generateRandomReferenceFrameTree(random);
         ReferenceFrame initialFrame = referenceFrames[random.nextInt(referenceFrames.length)];

         G expectedGeometry = createRandomGeometryObject(random);

         F frameGeometry = createFrameGeometryObject(initialFrame, expectedGeometry);

         RigidBodyTransform transform = EuclidCoreRandomTools.generateRandomRigidBodyTransform(random);

         boolean shouldThrowAndException = false;
         Class<?> exceptionClass = null;

         try
         {
            expectedGeometry.applyInverseTransform(transform);
         }
         catch (Exception e)
         {
            shouldThrowAndException = true;
            exceptionClass = e.getClass();
         }

         if (!shouldThrowAndException)
         {
            frameGeometry.applyInverseTransform(transform);
            assertTrue(initialFrame == frameGeometry.getReferenceFrame());
            assertTrue(expectedGeometry.epsilonEquals(frameGeometry.getGeometryObject(), EPSILON));
         }
         else
         {
            try
            {
               frameGeometry.applyInverseTransform(transform);
               fail("Should have thrown a " + exceptionClass.getSimpleName());
            }
            catch (Exception e)
            {
               assertEquals(exceptionClass, e.getClass());
            }
         }
      }

      for (int i = 0; i < ITERATIONS; i++)
      { // Test with a 2D transform, it should not throw an exception
         ReferenceFrame[] referenceFrames = EuclidFrameRandomTools.generateRandomReferenceFrameTree(random);
         ReferenceFrame initialFrame = referenceFrames[random.nextInt(referenceFrames.length)];

         G expectedGeometry = createRandomGeometryObject(random);

         F frameGeometry = createFrameGeometryObject(initialFrame, expectedGeometry);

         RigidBodyTransform transform = EuclidCoreRandomTools.generateRandomRigidBodyTransform2D(random);
         expectedGeometry.applyInverseTransform(transform);

         frameGeometry.applyInverseTransform(transform);
         assertTrue(initialFrame == frameGeometry.getReferenceFrame());
         assertTrue(expectedGeometry.epsilonEquals(frameGeometry.getGeometryObject(), EPSILON));
      }
   }
}
