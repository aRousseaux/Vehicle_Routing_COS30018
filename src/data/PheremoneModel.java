
package data;

//Child of DataModel
//inside of a matrix of distances between locations, it is a matrix of probablities to travel between specific locations
public class PheremoneModel extends DataModel
{
	private float[][] fPaths;

	public PheremoneModel(int aVehicleNumber, int aNumLocations, int aSeed, int aIterations)
	{
		//FIX CAPACITY
		super(aVehicleNumber, aNumLocations, aSeed, 1, aIterations);

		// pheremone level for each path between locations (i to j)
		fPaths = new float[numLocations()][numLocations()];
		for ( int i = 0; i < numLocations(); i++ )
		{
			for ( int j = 0; j < numLocations(); j++ )
			{
				fPaths[i][j] = 0;
			}
		}
	}

	public PheremoneModel(int aVehicleNumber, int aNumLocations, int aSeed, int Capacity, int aIterations)
	{
		//FIX CAPACITY
		super(aVehicleNumber, aNumLocations, aSeed, Capacity, aIterations);

		// pheremone level for each path between locations (i to j)
		fPaths = new float[numLocations()][numLocations()];
		for ( int i = 0; i < numLocations(); i++ )
		{
			for ( int j = 0; j < numLocations(); j++ )
			{
				fPaths[i][j] = 0;
			}
		}
	}

    public PheremoneModel(int aVehicleNumber, int aNumLocations, int aSeed, int[] Capacity, int aIterations)
    {
        //FIX CAPACITY
        super(aVehicleNumber, aNumLocations, aSeed, Capacity, aIterations);

        // pheremone level for each path between locations (i to j)
        fPaths = new float[numLocations()][numLocations()];
        for ( int i = 0; i < numLocations(); i++ )
        {
            for ( int j = 0; j < numLocations(); j++ )
            {
                fPaths[i][j] = 0;
            }
        }
    }


    //update a pheremone value at a specific matrix position
	public void updatePheremonePath(int aLocation1, int aLocation2, float aPheromone)
	{
		fPaths[aLocation1][aLocation2] = aPheromone;
	}

	//get a pheremone value at a specific matrix position
	public float getPheremone(int aLocation1, int aLocation2)
	{
		return fPaths[aLocation1][aLocation2];
	}

}
