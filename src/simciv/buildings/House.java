package simciv.buildings;

import java.util.ArrayList;
import java.util.HashMap;
import org.newdawn.slick.GameContainer;
import org.newdawn.slick.Graphics;
import org.newdawn.slick.Image;
import org.newdawn.slick.SpriteSheet;
import org.newdawn.slick.state.StateBasedGame;

import simciv.Cheats;
import simciv.Game;
import simciv.Vector2i;
import simciv.World;
import simciv.content.Content;
import simciv.maptargets.RoadMapTarget;
import simciv.units.Citizen;

/**
 * Every citizen need a house. Houses produce citizens.
 * @author Marc
 *
 */
public class House extends Building
{
	private static BuildingProperties properties[];
	private static SpriteSheet sprites[];
	private static byte MAX_LEVEL = 1;
	
	static
	{
		properties = new BuildingProperties[MAX_LEVEL+1];
		
		properties[0] = new BuildingProperties("House lv.1")
			.setUnitsCapacity(2).setCost(5).setSize(1, 1, 1);
		
		properties[1] = new BuildingProperties("House lv.2")
			.setUnitsCapacity(8).setCost(25).setSize(2, 2, 2);
	}

	// References to citizen living here
	private HashMap<Integer,Citizen> inhabitants = new HashMap<Integer,Citizen>();
	private byte level;
	private byte nbCitizensToProduce;

	public House(World w)
	{
		super(w);
		
		if(sprites == null)
		{
			sprites = new SpriteSheet[MAX_LEVEL+1];
			Image img;
				
			img = Content.images.buildHouseLv1;
			sprites[0] = new SpriteSheet(img, properties[0].width * Game.tilesSize, img.getHeight());
				
			img = Content.images.buildHouseLv2;
			sprites[1] = new SpriteSheet(img, properties[1].width * Game.tilesSize, img.getHeight());
		}
		
		direction = (byte) (4 * Math.random());
		level = 0;
		nbCitizensToProduce = 1;
	}
	
	@Override
	public boolean isHouse()
	{
		return true;
	}

	@Override
	public void tick()
	{
		if(state == Building.CONSTRUCTION)
		{
			if((getTicks() > 15 || Cheats.isFastCitizenProduction()))
				state = Building.NORMAL;
		}
		else if(state == Building.NORMAL)
		{
			if(level != MAX_LEVEL && getTicks() % 20 == 0)
			{
				if(tryLevelUp())
					nbCitizensToProduce++;
			}
			
			if(nbCitizensToProduce != 0)
				nbCitizensToProduce -= produceCitizens(nbCitizensToProduce);
		}
	}

	/**
	 * Produces nbToProduce citizens, following conditions :
	 * - There must be room for them in the house
	 * - The map must comport roads nearby
	 * @param nbToProduce : number of citizen to produce
	 * @return the amount effectively produced, always in [0, nbToProduce]
	 */
	protected byte produceCitizens(byte nbToProduce)
	{
		// It must have room for the new inhabitant
		if(!isRoomForInhabitant())
			return 0;
		
		// It must have free space to appear
		RoadMapTarget roads = new RoadMapTarget();
		ArrayList<Vector2i> availablePositions = 
			worldRef.map.getAvailablePositionsAround(this, roads, worldRef);		
		if(availablePositions.isEmpty())
			return 0;
		
		// Produce citizens
		byte citizensProduced = 0;
		for(byte i = 0; i < nbToProduce; i++)
		{
			Citizen c = new Citizen(worldRef);
			if(addInhabitant(c))
			{
				// It will appear at random following available positions
				Vector2i unitPos = availablePositions.get((int) (availablePositions.size() * Math.random()));
				worldRef.spawnUnit(c, unitPos.x, unitPos.y);
				citizensProduced++;
			}
		}
		
		if(citizensProduced != 0)
			Content.sounds.unitNewCitizen.play(1.f, 0.25f);
		
		return citizensProduced;
	}
	
	/**
	 * Puts all the content of this house into another, and destroys it
	 * @param other
	 */
	protected void mergeTo(House other)
	{
		for(Citizen c : inhabitants.values())
			c.setHouse(other);
		other.inhabitants.putAll(this.inhabitants);
		this.inhabitants.clear();
		other.nbCitizensToProduce += this.nbCitizensToProduce;
		dispose();
	}
	
	/**
	 * Tries to level up the house.
	 * If it's possible, the level up is performed and true is returned.
	 * If not, returns false.
	 * @return
	 */
	protected boolean tryLevelUp()
	{
//		System.out.println("--- tryLevelUp " + posX + ", " + posY); // debug
		if(level == 0)
		{
			// Check if 4 1x1 houses are forming a quad
			Building b[] = new Building[3]; // quad neighbors
			int nxy[][] = {{1, 0}, {0, 1}, {1, 1}}; // neighboring
			for(int i = 0; i < 3; i++)
			{
				b[i] = worldRef.getBuilding(posX + nxy[i][0], posY + nxy[i][1]);
				if(b[i] == null ||
					!b[i].isHouse() ||
					!b[i].is1x1() ||
					b[i].getState() != Building.NORMAL)
				{
					return false;
				}
			}
			
			// Check if the future 2x2 house will have roads nearby
			RoadMapTarget roads = new RoadMapTarget();
			if(worldRef.map.getAvailablePositionsAround(posX, posY, 2, 2, roads, worldRef).isEmpty())
				return false;
			
			// Merge houses			
			House housesToMerge[] = new House[3];
			for(int i = 0; i < 3; i++)
				housesToMerge[i] = (House)(b[i]);
			for(House h : housesToMerge)
				h.mergeTo(this);
			
			level++;

			// Mark the map (we know that cells are free, as they were occupied by houses)
			worldRef.map.markBuilding(this, true);
			
			return true;
		}
		return false;
	}
	
	public boolean isRoomForInhabitant()
	{
		return inhabitants.size() < getProperties().unitsCapacity;
	}

	public boolean addInhabitant(Citizen c)
	{
		if(isRoomForInhabitant())
		{
			if(inhabitants.put(c.getID(), c) == null)
			{
				c.setHouse(this);
				return true;
			}
		}
		return false;
	}

	public void removeInhabitant(int id)
	{
		inhabitants.remove(id);
	}

	@Override
	public void render(GameContainer gc, StateBasedGame game, Graphics gfx)
	{
		if(state == CONSTRUCTION)
		{
			renderAsConstructing(gfx);
		}
		else if(level == 0)
		{
			// Note : directionnal sprites are only supported with the first level yet
			gfx.drawImage(sprites[level].getSprite(direction, 0), 
					posX * Game.tilesSize, 
					(posY - 1) * Game.tilesSize);
		}
		else
		{
			gfx.drawImage(sprites[level].getSprite(0, 0), 
					posX * Game.tilesSize, 
					(posY - 1) * Game.tilesSize);
		}
	}

	@Override
	public BuildingProperties getProperties()
	{
		return properties[level];
	}

	@Override
	public void onDestruction()
	{
		for(Citizen c : inhabitants.values())
		{
			c.kill();
		}
	}

	@Override
	protected int getTickTime()
	{
		return 1000;
	}

	public int getNbInhabitants()
	{
		return inhabitants.size();
	}

	@Override
	public String getInfoString()
	{
		return "[" + getProperties().name + "] inhabitants : " + getNbInhabitants();
	}

}




