package simciv.units;

import java.util.List;

import org.newdawn.slick.Graphics;

import backend.Direction2D;
import simciv.Map;
import simciv.ResourceSlot;
import simciv.builds.Build;
import simciv.builds.Workplace;
import simciv.content.Content;
import simciv.maptargets.BuildMapTarget;
import simciv.maptargets.FreeWarehouseMapTarget;

/**
 * A conveyer can carry resources from a place to another.
 * @author Marc
 *
 */
public class Conveyer extends Citizen
{
	private static final long serialVersionUID = 1L;
	
	private ResourceSlot carriedResource;
	
	public Conveyer(Map m, Workplace w)
	{
		super(m, w);
		carriedResource = new ResourceSlot();
	}
	
	@Override
	public byte getJobID()
	{
		return Job.CONVEYER;
	}

	public void addResourceCarriage(ResourceSlot r)
	{
		carriedResource.addAllFrom(r);
	}

	@Override
	public void tick()
	{
		/*
		 * Current behavior :
		 * The conveyer goes out of its workplace with resources.
		 * He moves at random, distributing resources.
		 * When he distributed all his resources, he goes back to its workplace.
		 */
		
		if(getState() == Unit.THINKING)
			return;
		
		if(!carriedResource.isEmpty() && !isMovement())
		{
			findAndGoTo(new FreeWarehouseMapTarget());
		}
		
		if(isMovementFinished())
		{
			if(!carriedResource.isEmpty())
				distributeResources(); // distribute resources
			if(!carriedResource.isEmpty()) // if still resources left, search another warehouse
				findAndGoTo(new FreeWarehouseMapTarget());
			
			if(getTargetBuildingID() == getWorkplaceID())
			{
				// End of mission, ready for the next
				setMovement(null);
				dispose();
			}
		}
		else if(isMovementBlocked())
		{
			findAndGoTo(getMovementTarget());
		}
		else
		{
			if(carriedResource.isEmpty()) // I have no resource to distribute
			{
				// Go back to workplace
				if(getMovementTarget() == null || getTargetBuildingID() != getWorkplaceID())
					findAndGoTo(getWorkplace());
			}
			else // I have resource to distribute
			{
				distributeResources(); // distribute it
			}
		}
	}
		
	private void distributeResources()
	{
		if(carriedResource.isEmpty())
			return;
		List<Build> buildingsAround = getMap().getBuildsAround(getX(), getY());
		for(Build b : buildingsAround)
		{
			if(b.isAcceptResources())
				b.storeResource(carriedResource);
		}
		if(carriedResource.isEmpty()) // if no resources left,
			findAndGoTo(getWorkplace()); // Back to my workplace
	}
	
	private int getTargetBuildingID()
	{
		if(BuildMapTarget.class.isInstance(getMovementTarget()))
			return ((BuildMapTarget)(getMovementTarget())).buildingID;
		return -1;
	}
	
	@Override
	public void renderUnit(Graphics gfx)
	{
		if(getDirection() == Direction2D.NORTH)
		{
			carriedResource.renderCarriage(gfx, 0, 0, getDirection());
			defaultRender(gfx, Content.sprites.unitConveyer);
		}
		else
		{
			defaultRender(gfx, Content.sprites.unitConveyer);
			carriedResource.renderCarriage(gfx, 0, 0, getDirection());
		}
	}

}



