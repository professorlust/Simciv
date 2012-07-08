package simciv;

import org.newdawn.slick.Graphics;
import org.newdawn.slick.Image;

/**
 * Natural elements on the map
 * @author Marc
 *
 */
public class Nature
{
	// Constants
	public static final byte NONE = 0;
	public static final byte TREE = 1;
	
	private static Image treeSprites[] = new Image[3];
	
	public static void loadContent()
	{
		treeSprites[0] = ContentManager.instance().getImage("nature.tree");
		treeSprites[1] = ContentManager.instance().getImage("nature.tree2");
		treeSprites[2] = ContentManager.instance().getImage("nature.tree3");
	}
	
	/**
	 * Renders the natural element from the given cell
	 * @param gfx : Graphics object
	 * @param cell
	 * @param gx : graphical X pos (cellX * Game.tilesSize)
	 * @param gy : graphical Y pos (cellY * Game.tilesSize)
	 */
	public static void render(Graphics gfx, MapCell cell, int gx, int gy)
	{
		if(cell.nature == TREE)
		{
			// Apply a little offset to seem more "natural"
			if((cell.noise & 0x01) != 0) // 0000 0001
				gx -= 2;
			else if((cell.noise & 0x02) != 0) // 0000 0010
				gx += 2;
			else if((cell.noise & 0x03) != 0) // 0000 0011
				gy -= 2;
			else if((cell.noise & 0x04) != 0) // 0000 0100
				gy += 2;
			
			// Visual variants
			int i = 0;
			if((cell.noise & 0x10) != 0) // 0001 0000
				i = 1;
			else if((cell.noise & 0x20) != 0) // 0010 000
				i = 2;
			
			gfx.drawImage(treeSprites[i], gx, gy - (treeSprites[i].getHeight() - Game.tilesSize));
		}
	}
}


