
package data;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

public class DataModel 
{
	private int[][] fDistanceMatrix; // distances between each location
	private List< Vehicle > fVehicles; // delivery drivers
	private List< Location > fLocations; // each location
	
	public DataModel( int aVehicleNumber, int aNumLocations, int aSeed, int aCapacity )
	{
		fVehicles = new ArrayList< Vehicle >();
		
		// generate the vehicle representation
		for ( int i = 0; i < aVehicleNumber; i ++ )
		{
			fVehicles.add( new Vehicle( i, aCapacity, 8 ) );
		}

		generateLocations( aNumLocations, aSeed );
		
		// update distance matrix
		calculateDistances();
	}
	
	private void generateLocations( int aNumLocations, int aSeed )
	{
		// initialize locations variable
		fLocations = new ArrayList<Location>();

		// initialize class that generates numbers
		Random lRand = new Random( aSeed );

		for ( int i = 0; i < aNumLocations; i++ )
		{
			// set each locations x, y
			fLocations.add( new Location( i, lRand.nextInt( 1000 ), lRand.nextInt( 1000 ) ) );
		}
	}
	
	private void calculateDistances()
	{
		// initialize matrix
		fDistanceMatrix = new int[ numLocations() ][ numLocations() ];

		int x1; int y1;
		int x2; int y2;

		// calculates for each location all other locations
		for ( int i = 0; i < numLocations(); i++ )
		{
			for ( int j = 0; j < numLocations(); j++ )
			{
				x1 = getLocation(i).x;
				y1 = getLocation(i).y;

				x2 = getLocation(j).x;
				y2 = getLocation(j).y;

				// Distance formula
				fDistanceMatrix[i][j] = (int) Math.sqrt((x2-x1) * (x2-x1) + (y2-y1) * (y2-y1));
			}
		}
	}
	
	public int[] getCapacities()
	{
		int[] lCapacities = new int[ numVehicles() ];
		
		for ( int i = 0; i < numVehicles(); i++ )
		{
			lCapacities[i] = getVehicle(i).getCapacity();
		}
		
		return lCapacities;
	}
	
	public int[][] getDistanceMatrix()
	{
		return fDistanceMatrix;
	}
	
	public Vehicle getVehicle( int aIndex )
	{
		return fVehicles.get( aIndex );
	}
	
	public int numVehicles()
	{
		return fVehicles.size();
	}
	
	public Location getLocation( int aIndex )
	{
		return fLocations.get( aIndex );
	}
	
	public int numLocations()
	{
		return fLocations.size();
	}
}
