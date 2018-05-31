package us.ihmc.euclid.referenceFrame.tools;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;

import us.ihmc.euclid.referenceFrame.ReferenceFrame;
import us.ihmc.euclid.transform.RigidBodyTransform;
import us.ihmc.euclid.tuple3D.interfaces.Tuple3DReadOnly;

public class ReferenceFrameTools
{
   /**
    * {@code worldFrame} is a root reference frame and is most of time the only root reference frame.
    * <p>
    * It is commonly assumed that its axes are aligned as follows:
    * <ul>
    * <li>The x-axis is usually referred to as the forward axis.
    * <li>With the x-axis referring forward, the y-axis points to the left.
    * <li>The z-axis points upward and usually points to the opposite direction to the gravitational
    * acceleration.
    * </ul>
    * </p>
    */
   private static final ReferenceFrame worldFrame = constructARootFrame("World");

   /**
    * Construct a new z-up root reference frame.
    * <p>
    * Most of the time, {@link #worldFrame} is the only root frame from which children reference frames
    * are added.
    * </p>
    * <p>
    * Note that frames added as children of this root frame belongs to a different reference frame tree
    * than the tree starting off of {@link #worldFrame}. Transformation across two different trees of
    * reference frames is forbidden as the transformation between them is undefined.
    * </p>
    * <p>
    * The parent frame and transforms of a root frame are all {@code null}.
    * </p>
    *
    * @param frameName the name of the new world frame.
    * @return the new non-moving z-up root reference frame.
    */
   public static ReferenceFrame constructARootFrame(String frameName)
   {
      ReferenceFrame ret = new ReferenceFrame(frameName)
      {
         @Override
         protected void updateTransformToParent(RigidBodyTransform transformToParent)
         {
         }
      };

      return ret;
   }

   /**
    * Return the world reference frame that is a root reference frame.
    * <p>
    * The world reference frame can be used to create child reference frames. It is usually assumed
    * that the z-axis represents the up/down direction (parallel to gravity), the x-axis represents the
    * forward/backward direction and the y-axis represents the transversal direction.
    * </p>
    *
    * @return the world reference frame.
    */
   public static ReferenceFrame getWorldFrame()
   {
      return worldFrame;
   }

   /**
    * Creates a reference frame with an immutable transform from its parent.
    * <p>
    * The {@code transformFromParent} should describe the pose of the parent frame expressed in this
    * new frame.
    * </p>
    *
    * @param frameName the name of the new frame.
    * @param parentFrame the parent frame of the new reference frame.
    * @param transformFromParent the transform that can be used to transform a geometry object from the
    *           parent frame to this frame. Not modified.
    * @return the new reference frame.
    */
   public static ReferenceFrame constructFrameWithUnchangingTransformFromParent(String frameName, ReferenceFrame parentFrame,
                                                                                RigidBodyTransform transformFromParent)
   {
      RigidBodyTransform transformToParent = new RigidBodyTransform(transformFromParent);
      transformToParent.invert();

      return constructFrameWithUnchangingTransformToParent(frameName, parentFrame, transformToParent);
   }

   /**
    * Creates a reference frame with an immutable translation offset from its parent.
    * <p>
    * The new reference frame has the same orientation as its parent frame.
    * </p>
    *
    * @param frameName the name of the new frame.
    * @param parentFrame the parent frame of the new reference frame.
    * @param translationOffsetFromParent describes the position of the new reference frame's origin
    *           expressed in the parent frame. Not modified.
    * @return the new reference frame.
    */
   public static ReferenceFrame constructFrameWithUnchangingTranslationFromParent(String frameName, ReferenceFrame parentFrame,
                                                                                  Tuple3DReadOnly translationOffsetFromParent)
   {
      RigidBodyTransform transformToParent = new RigidBodyTransform();
      transformToParent.setTranslation(translationOffsetFromParent);

      return constructFrameWithUnchangingTransformToParent(frameName, parentFrame, transformToParent);
   }

   /**
    * Creates a reference frame with an immutable transform to its parent.
    * <p>
    * The {@code transformToParent} should describe the pose of the new frame expressed in its parent
    * frame.
    * </p>
    *
    * @param frameName the name of the new frame.
    * @param parentFrame the parent frame of the new reference frame.
    * @param transformToParent the transform that can be used to transform a geometry object the new
    *           frame to its parent frame. Not modified.
    * @return the new reference frame.
    */
   public static ReferenceFrame constructFrameWithUnchangingTransformToParent(String frameName, ReferenceFrame parentFrame,
                                                                              RigidBodyTransform transformToParent)
   {
      boolean isZupFrame = parentFrame.isZupFrame() && transformToParent.isRotation2D();
      boolean isAStationaryFrame = parentFrame.isAStationaryFrame();

      ReferenceFrame ret = new ReferenceFrame(frameName, parentFrame, transformToParent, isAStationaryFrame, isZupFrame)
      {
         @Override
         protected void updateTransformToParent(RigidBodyTransform transformToParent)
         {
         }
      };

      return ret;
   }

   public static List<ReferenceFrame> constructFramesStartingWithRootEndingWithThis(ReferenceFrame thisFrame)
   {
      ReferenceFrame parentFrame = thisFrame.getParent();
      if (parentFrame == null)
      {
         return Collections.singletonList(thisFrame);
      }

      List<ReferenceFrame> ret = new ArrayList<>(parentFrame.getFramesStartingWithRootEndingWithThis());
      ret.add(thisFrame);

      return Collections.unmodifiableList(ret);
   }

   /**
    * Will remove the provided frame from the frame tree.
    * <p>
    * This will disable the frame and cause the frame tree to loose all references to the frame and it's children. Note,
    * that you can not use the frame after this method is called. This method is meant to allow the JVM to collect the
    * frame as garbage and all future method calls on this frame will throw exceptions.
    * </p>
    * <p>
    * This recursively disables all children of this frame also. If the provided frame is a root frame this method will
    * do nothing.
    * </p>
    * @param frame is the {@link ReferenceFrame} that will be removed from the tree.
    */
   public static void removeFrame(ReferenceFrame frame)
   {
      frame.remove();
   }

   /**
    * Will remove all provided frames from the frame tree.
    * @param frames to be removed and disabled.
    * @see ReferenceFrameTools#removeFrame(ReferenceFrame)
    */
   public static void removeFrames(ReferenceFrame[] frames)
   {
      for (int frameIdx = 0; frameIdx < frames.length; frameIdx++)
      {
         removeFrame(frames[frameIdx]);
      }
   }

   /**
    * Will clear the entire frame tree that this frame is part of leaving only the root frame
    * enabled. All other frames in the tree will be removed and disabled.
    * @param frame in the frame tree that will be cleared.
    */
   public static void clearFrameTree(ReferenceFrame frame)
   {
      frame.getRootFrame().clearChildren();
   }

   /**
    * Will clear the entire frame tree of the {@link ReferenceFrameTools#worldFrame} tree.
    */
   public static void clearWorldFrameTree()
   {
      worldFrame.clearChildren();
   }

   /**
    * Will create a collection of all reference frames in the frame tree that the provided frame
    * is part of.
    * @param frame in the reference frame tree of interest
    * @return all frames in the reference frame tree
    */
   public static Collection<ReferenceFrame> getAllFramesInTree(ReferenceFrame frame)
   {
      Collection<ReferenceFrame> frames = new ArrayList<>();
      frames.add(frame.getRootFrame());
      getAllChildren(frame.getRootFrame(), frames);
      return frames;
   }

   private static void getAllChildren(ReferenceFrame frame, Collection<ReferenceFrame> collectionToPack)
   {
      collectionToPack.addAll(frame.getChildren());
      frame.getChildren().forEach(childFrame -> getAllChildren(childFrame, collectionToPack));
   }
}