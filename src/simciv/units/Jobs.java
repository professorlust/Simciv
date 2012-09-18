package simciv.units;

import org.newdawn.slick.util.Log;

import simciv.Map;
import simciv.builds.Workplace;

/**
 * List of all jobs associated with citizen units.
 * @author Marc
 *
 */
public abstract class Jobs
{
	// Job IDs
	public static final byte EMPLOYER = 0;
	public static final byte CONVEYER = 1;
	public static final byte TAXMAN = 2;
	public static final byte MARKET_DELIVERY = 3;
	public static final byte ARCHITECT = 4;
	public static final byte FIREMAN = 5;
	
	public static Citizen createUnitFromJobID(byte jobID, Map m, Workplace w)
	{
		switch(jobID)
		{
		case CONVEYER : 		return new Conveyer(m, w);
		case TAXMAN : 			return new Taxman(m, w);
		case MARKET_DELIVERY : 	return new MarketDelivery(m, w);
		case ARCHITECT : 		return new Architect(m, w);
		case FIREMAN :			return new Fireman(m, w);
		
		default :
			Log.error("unknown job ID: " + jobID);
			return null;
		}
	}
	
}




