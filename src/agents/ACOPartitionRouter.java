
package agents;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import data.DataModel;
import data.Location;

public class ACOPartitionRouter extends GenericRouter
{
	private static final long serialVersionUID = 1L;

	// refer to table 1; On Partitioning Grids into Equal Parts
	private final double[] fKPartitionTable = new double[] { 0, 1, 1.66, 2, 2.66, 3, 3.47, 3.75, 4 };
	
	private void kPartitionLocations( DataModel aDataModel )
	{
		double a = 1000;
		double b = 1000;
		
		for (int i = 1; i < ( aDataModel.numVehicles() / 2 ); i++)
		{
			// divide the rectangle a by b into two rectangles of the size...
			double ap = a * ( (i / aDataModel.numVehicles()) * b);
			double bp = a * ( ( ( aDataModel.numVehicles() - i ) / aDataModel.numVehicles() ) * b);
			// parallel to a
			
			// use the table to get the best i-decomposition of the new rectangles
			double lKValue = fKPartitionTable[aDataModel.numVehicles()];
		}
	}
	
	private Location[][] randomPartition(DataModel aDataModel)
	{
		Location[][] lResult = new Location[aDataModel.numVehicles()][(int) Math.ceil(aDataModel.numLocations() / aDataModel.numVehicles())];
		List<Integer> lRemaining = new ArrayList<Integer>();
		
		for ( int i = 0; i < aDataModel.numLocations(); i++ )
		{
			lRemaining.add( i );
		}
		
		Collections.shuffle(lRemaining);
		// fix index, too tired to think about this
		for (int i = 0; i < aDataModel.numLocations(); i++)
		{
			for (int j = 0; j < aDataModel.numLocations() / aDataModel.numVehicles(); j++)
			{
				lResult[j][i] = aDataModel.getLocation(lRemaining.get(i));
			}
		}
		
		return lResult;
	}
	
	public int[][] solveRoute(DataModel aDataModel, int aMaxRouteDistance) 
	{
		// solve each partition
		Location[][] lPartitions = randomPartition( aDataModel );
		
		return null;
	}
}
