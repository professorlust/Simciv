package simciv;

import java.util.HashMap;

import org.newdawn.slick.GameContainer;
import org.newdawn.slick.Graphics;

import simciv.units.Citizen;

public class World
{	
	public Map map;
	
	private int nbTicks;
	private int tickTime;
	private int nextTickTime;
	private HashMap<Integer,Unit> units = new HashMap<Integer,Unit>();
	private HashMap<Integer,Building> buildings = new HashMap<Integer,Building>();

	public World(int width, int height)
	{
		nbTicks = 0;
		tickTime = 500;
		nextTickTime = tickTime;

		map = new Map(width, height);
		
		spawnUnit(new Citizen(this), 20, 10);
		placeBuilding(BuildingList.createBuildingFromName("House", this), 10, 10);
	}
	
	/**
	 * Updates the world, and calls the tick() method on buildings
	 * and units at each time interval (tickTime).
	 * @param delta
	 */
	public void update(int delta)
	{
		nextTickTime -= delta;
		if(nextTickTime < 0)
		{
			nextTickTime += tickTime;
			nbTicks++;
			
			for(Unit u : units.values())
			{
				u.tick();
			}
			for(Building b : buildings.values())
			{
				b.tick();
			}
		}
	}
	
	/**
	 * Spawns an unit in the world at (x,y).
	 * Note : a unit can be in a building without being in the units map.
	 * @param u : unit
	 * @param x
	 * @param y
	 * @return : true if the unit has been spawned.
	 */
	public boolean spawnUnit(Unit u, int x, int y)
	{
		u.setPosition(x, y);
		if(!units.containsKey(u.getID()))
		{
			units.put(u.getID(), u);
			return true;
		}
		return false;
	}
	
	/**
	 * Places a new building at (x,y).
	 * It may occupy one or more cells on the map.
	 * @param b : new building
	 * @param x : x origin
	 * @param y : y origin
	 * @return : true if the building has been placed
	 */
	public boolean placeBuilding(Building b, int x, int y)
	{
		b.setPosition(x, y);
		if(map.markBuilding(b, true) && !buildings.containsKey(b.getID()))
		{
			buildings.put(b.getID(), b);
			return true;
		}
		return false;
	}
	
	/**
	 * Erases the building assumed to occupy the cell at (x,y).
	 * @param x
	 * @param y
	 * @return : true if the building has been erased.
	 */
	public boolean eraseBuilding(int x, int y)
	{
		int ID = map.getBuildingID(x, y);
		if(ID >= 0)
		{
			Building b = getBuilding(ID);
			if(b != null)
			{
				map.markBuilding(b, false);
				buildings.remove(ID);
				return true;
			}
		}
		return false;
	}
	
	Unit getUnit(int ID)
	{
		return units.get(ID);
	}
	
	Building getBuilding(int ID)
	{
		return buildings.get(ID);
	}
	
	/**
	 * Draws a part of the world within the specified map range
	 * @param mapRange
	 * @param gc
	 * @param gfx
	 */
	public void render(IntRange2D mapRange, GameContainer gc, Graphics gfx)
	{
		map.render(mapRange, gc, gfx);
		
		for(Building b : buildings.values())
		{
			b.render(gfx);
		}
		for(Unit u : units.values())
		{
			u.render(gfx);
		}
	}
}

