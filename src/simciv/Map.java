package simciv;

import org.newdawn.slick.Color;
import org.newdawn.slick.GameContainer;
import org.newdawn.slick.Graphics;

/**
 * The map is a 2D array that stores terrain, plants and roads information.
 * It is also used to mark places occupied by buildings.
 * @author Marc
 *
 */
public class Map
{
	// Map layers :
	// 2D access is made using (width * y + x).
	byte terrain[];
	byte roads[];
	int buildings[];
	//short plants[];

	int width;
	int height;	
	boolean renderGrid;

	/**
	 * Creates an empty map
	 * @param width : X dimension 
	 * @param height : Y dimension
	 */
	public Map(int width, int height)
	{
		renderGrid = false;

		this.width = width;
		this.height = height;
		int area = getArea();
		
		terrain = new byte[area];
		fillTerrain(Terrain.GRASS);
		
		roads = new byte[area];
		for(int i = 0; i < area; i++)
			roads[i] = -1;
		
		buildings = new int[area];
		for(int i = 0; i < area; i++)
			buildings[i] = -1;
	}
	
	/**
	 * Get the area of the map. It is also the length of data buffers.
	 * @return area (length)
	 */
	public int getArea()
	{
		return width * height;
	}
	
	/*
	 * Terrain
	 */
	
	/**
	 * Get terrain's properties at (x,y)
	 * @param x
	 * @param y
	 * @return terrain properties
	 */
	public Terrain getTerrain(int x, int y)
	{
		if(contains(x, y))
			return Terrain.get(terrain[width * y + x]);
		else
			return Terrain.get(Terrain.VOID);
	}
	
	/**
	 * Sets the terrain type at (x,y)
	 * @param x
	 * @param y
	 * @param t : terrain type (ID)
	 */
	public void setTerrain(int x, int y, byte t)
	{
		if(contains(x, y))
			terrain[width * y + x] = t;
	}
	
	/**
	 * Fills all cells with a terrain type.
	 * @param value : terrain type
	 */
	public void fillTerrain(byte value)
	{
		int size = getArea();
		for(int i = 0; i < size; i++)
			terrain[i] = value;
	}
	
	/*
	 * Roads
	 */
	
	public boolean isRoad(int x, int y)
	{
		return getRoad(x, y) != -1;
	}
	
	/**
	 * Get road index at (x,y).
	 * @param x
	 * @param y
	 * @return road index. -1 if there are no road.
	 */
	private byte getRoad(int x, int y)
	{
		if(contains(x, y))
			return roads[width * y + x];
		else
			return -1;
	}
	
	/**
	 * Sets the road index at (x,y).
	 * @param x
	 * @param y
	 * @param i : index (using Road.getIndex(map,x,y))
	 */
	private void setRoad(int x, int y, byte i)
	{
		if(contains(x, y))
			roads[width * y + x] = i;
	}
	
	/**
	 * Places a road at (x,y)
	 * @param x
	 * @param y
	 * @return true if the road has been successfully placed.
	 */
	public boolean placeRoad(int x, int y)
	{
		if(canPlaceObject(x, y))
		{
			roads[width * y + x] = Road.getIndex(this, x, y);
			updateRoads(x, y);
			return true;
		}
		return false;
	}
	
	/**
	 * Updates roads around the point (x,y).
	 * Must be done after placing a road here, in order to
	 * make the roads join if possible.
	 * @param x
	 * @param y
	 */
	private void updateRoads(int x, int y)
	{
		if(isRoad(x-1, y))
			setRoad(x-1, y, Road.getIndex(this, x-1, y));
		if(isRoad(x+1, y))
			setRoad(x+1, y, Road.getIndex(this, x+1, y));
		if(isRoad(x, y-1))
			setRoad(x, y-1, Road.getIndex(this, x, y-1));
		if(isRoad(x, y+1))
			setRoad(x, y+1, Road.getIndex(this, x, y+1));
	}
	
	/**
	 * Erases roads at (x,y).
	 * @param x
	 * @param y
	 * @return : true if a road has been erased.
	 */
	public boolean eraseRoad(int x, int y)
	{
		if(isRoad(x, y))
		{
			setRoad(x, y, (byte) -1);
			updateRoads(x, y);
			return true;
		}
		return false;
	}
	
	/*
	 * Buildings
	 */
	
	/**
	 * Marks cells as occupied by a building, if possible,
	 * or clears marked cells.
	 * @param b : building to mark
	 * @param mark : true to mark, false to clear marks.
	 * @return : false if there is no room for the building
	 */
	public boolean markBuilding(Building b, boolean mark)
	{
		// Check if we can place a mark
		if(mark && !canPlaceObject(b.getX(), b.getY(), b.getWidth(), b.getHeight()))
			return false;
		
		// TODO check if we are not erasing marks of different buildings
		
		int x, y;
		int xmax = b.getX() + b.getWidth() - 1;
		int ymax = b.getY() + b.getHeight() - 1;
		
		for(y = b.getY(); y <= ymax; y++)
		{
			for(x = b.getX(); x <= xmax; x++)
			{
				buildings[width * y + x] = mark ? b.getID() : -1;
			}
		}
		return true;
	}
	
	/**
	 * Get the building ID at (x,y).
	 * @param x
	 * @param y
	 * @return building ID, -1 if there are no building here.
	 */
	public int getBuildingID(int x, int y)
	{
		if(contains(x, y))
		{
			return buildings[width * y + x];
		}
		return -1;
	}

	/*
	 * Render
	 */
	
	/**
	 * Renders the map within the specified range.
	 * @param range : map range (in cells)
	 * @param gc
	 * @param gfx
	 */
	public void render(IntRange2D range, GameContainer gc, Graphics gfx)
	{
		gfx.setColor(Color.white);
		int x, y;
		
		for(y = range.minY; y <= range.maxY; y++)
		{
			for(x = range.minX; x <= range.maxX; x++)
			{
				if(contains(x, y))
					renderCell(x, y, gfx);
			}
		}
		
		if(renderGrid)
			renderGrid(range.minX, range.minY, gc, gfx);
	}
	
	private void renderCell(int x, int y, Graphics gfx)
	{
		getTerrain(x, y).render(gfx, x, y);

		if(isRoad(x, y))
			Road.render(roads[width * y + x], x, y, gfx);
	}
	
	private void renderGrid(int x0, int y0, GameContainer gc, Graphics gfx)
	{
		// TODO improve grid rendering
		// it don't work as expected
		
		gfx.setColor(Color.black);
		int x, y;
		x0 *= Game.tilesSize;
		y0 *= Game.tilesSize;
		
		for(x = 0; x < gc.getWidth(); x += Game.tilesSize)
		{
			gfx.drawLine(x0 + x, 0, x0 + x, gc.getHeight());
		}
		for(y = 0; y < gc.getHeight(); y += Game.tilesSize)
		{
			gfx.drawLine(0, y0 + y, gc.getWidth(), y0 + y);
		}
	}
	
	public void toggleRenderGrid()
	{
		renderGrid = !renderGrid;
	}
	
	/*
	 * Miscellaneous / tests
	 */

	public int getWidth()
	{
		return width;
	}
	
	public int getHeight()
	{
		return height;
	}
	
	/**
	 * Test if (x,y) is a right position
	 * @param x
	 * @param y
	 * @return : true if the point (x,y) is right
	 */
	public boolean contains(int x, int y)
	{
		return x >= 0 && y >= 0 && x < width && y < height;
	}

	/**
	 * Tests if we can place an object at (x,y).
	 * Here, an object can be a road, a plant or a building.
	 * @param x
	 * @param y
	 * @return : true if we can place an object
	 */
	public boolean canPlaceObject(int x, int y)
	{
		if(!contains(x, y)) // invalid position
			return false;
		if(isRoad(x, y)) // road
			return false;
		if(terrain[y * width + x] == Terrain.WATER) // invalid terrain
			return false;
		if(buildings[y * width + x] >= 0) // building
			return false;
		return true; // OK !
	}
	
	/**
	 * Tests if we can place an object in the (xmin,ymin,w,h) area.
	 * @param xmin : X origin
	 * @param ymin : Y origin
	 * @param w : area width
	 * @param h : area height
	 * @return true if all cells in the area are free
	 */
	public boolean canPlaceObject(int xmin, int ymin, int w, int h)
	{
		int x, y, xmax = xmin + w - 1, ymax = ymin + h - 1;
		for(y = ymin; y <= ymax; y++)
		{
			for(x = xmin; x <= xmax; x++)
			{
				if(!canPlaceObject(x, y))
					return false;
			}
		}		
		return true;
	}
}

