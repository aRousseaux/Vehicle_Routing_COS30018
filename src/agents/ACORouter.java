
package agents;

import data.DataModel;
import data.PheremoneModel;
import jade.core.Agent;

import java.util.ArrayList;
import java.util.List;

public class ACORouter extends Agent implements Router
{
	private static final long serialVersionUID = 1L;
	public final int fIterations = 100;
	public static int fNumAnts;
	protected ArrayList<Integer> avalible_locations;
	public List<Ant> fAnts;
	public PheremoneModel fGraph;
	protected DataModel fDataModel;

	public void findDrivers() 
	{
	}

	public int[][] solveRoute(DataModel aDataModel, int aMaxRouteDistance) 
	{
		// initialize
		fAnts = new ArrayList<Ant>();
		Ant lBestAnt = null;
		int lBestPathLength = 0;

		fGraph = new PheremoneModel( 4, 6, 9 );
		fDataModel = fGraph;
		fNumAnts = (int) Math.pow(fDataModel.numLocations(), 2);

		for (int i = 0; i < fNumAnts; i++)
		{
			fAnts.add( new Ant( fDataModel ) );
		}

		avalible_locations = new ArrayList<Integer>();

		for (int i = 0; i < fDataModel.numLocations(); i++)
		{
			for (int j = 0; j < fDataModel.numLocations(); j++)
			{
				System.out.print(fDataModel.getDistanceMatrix()[i][j] +  ", ");
			}
			System.out.println();
		}

		for ( int t = 0; t < fIterations; t++ )
		{
			ConstructSolutions();
			UpdateTrails();

			for (int i = 0; i < fDataModel.numLocations(); i++)
			{
				for (int j = 0; j < fDataModel.numLocations(); j++)
				{
					System.out.print(fGraph.getFPathAtIndex(i, j) +  ", ");
				}
				System.out.println();
			}

			System.out.println();
		}

		return getSolutions();	}

	public void distributeRoutes() 
	{
	}

	public int calculateRouteLength(int[] aLocations, DataModel aDataModel) 
	{
		return 0;
	}

	public void ConstructSolutions()
	{
		avalible_locations = new ArrayList<Integer>();

		for (int i = 1; i < fGraph.numLocations(); i++)
		{
			avalible_locations.add(i);
		}

		int total_distance = 0;
		while (avalible_locations.size() > 0)
		{
			for (Ant lAnts : fAnts)
			{
				avalible_locations = lAnts.nextLocation(fGraph, avalible_locations);

				total_distance += lAnts.total_distance_travelled;
			}
		}

		if (avalible_locations.size() <= 0)
		{
			for (Ant jAnts : fAnts)
			{
				fGraph = jAnts.updateModel(fGraph);

				if (jAnts.total_distance_travelled > 0)
				{
					jAnts.total_distance_travelled = total_distance;
				}

				System.out.println(jAnts.getPath_alt() + " | " + jAnts.getTotal_distance_travelled());

				jAnts.reset();
			}

			avalible_locations = new ArrayList<Integer>();
			for (int i = 1; i < fGraph.numLocations(); i++)
			{
				avalible_locations.add(i);
			}
		}
	}

	public int[][] getSolutions()
	{
		avalible_locations = new ArrayList<Integer>();
		int[][] return_array = new int[fDataModel.numVehicles()][];

		for (int i = 1; i < fGraph.numLocations(); i++)
		{
			avalible_locations.add(i);
		}

		int total_distance = 0;
		while (avalible_locations.size() > 0)
		{
			for (Ant lAnts : fAnts)
			{
				avalible_locations = lAnts.nextLocation(fGraph, avalible_locations);

				total_distance += lAnts.total_distance_travelled;
			}
		}

		int count = 0;
		if (avalible_locations.size() <= 0)
		{
			for (Ant jAnts : fAnts)
			{
				fGraph = jAnts.updateModel(fGraph);

				if (jAnts.total_distance_travelled > 0)
				{
					jAnts.total_distance_travelled = total_distance;
				}

				return_array[count] = jAnts.getPathArray();
				count++;
			}

			avalible_locations = new ArrayList<Integer>();
			for (int i = 1; i < fGraph.numLocations(); i++)
			{
				avalible_locations.add(i);
			}
		}

		return return_array;
	}

	public void UpdateTrails()
	{
		for (int i = 0; i < fGraph.numLocations(); i++)
		{
			float max_value = 0;
			for (int j = 0; j < fGraph.numLocations(); j++)
			{
				//if (fGraph.getFPathAtIndex(i,j) > max_value)
				if (fGraph.getFPathAtIndex(i, j) > max_value)
				{
					max_value = fGraph.getFPathAtIndex(i,j);
				}
			}

			for (int j = 0; j < fGraph.numLocations(); j++)
			{
				//play around with this if statement
				if (fGraph.getFPathAtIndex(i,j) < max_value * 0.25)
				{
					fGraph.updatePheremonePath(i,j, 0);
				}
			}
		}
	}

}
