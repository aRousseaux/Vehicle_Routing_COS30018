package data;

public class PheremoneModel extends DataModel
{
    private float[][] fPaths;

    public PheremoneModel(int aVehicleNumber, int aNumLocations, int aSeed)
    {
        //FIX CAPACITY
        super(aVehicleNumber, aNumLocations, aSeed, 1);

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

    public void updatePheremonePath(int aLocation1, int aLocation2, float aPheremone)
    {
        fPaths[aLocation1][aLocation2] = aPheremone;
    }

    public float getFPathAtIndex(int aLocation1, int aLocation2)
    {
        return fPaths[aLocation1][aLocation2];
    }

}
