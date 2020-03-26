package us.ihmc.euclid.referenceFrame.interfaces;

import us.ihmc.euclid.shape.primitives.interfaces.PointShape3DBasics;

public interface FixedFramePointShape3DBasics extends PointShape3DBasics, FramePointShape3DReadOnly, FixedFrameShape3DBasics, FixedFramePoint3DBasics
{
   @Override
   default FixedFrameShape3DPoseBasics getPose()
   {
      return null;
   }

   @Override
   FixedFramePointShape3DBasics copy();
}
