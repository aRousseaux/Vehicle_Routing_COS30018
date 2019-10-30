
package agents;

import data.DataModel;
import data.PheremoneModel;
import jade.domain.FIPAAgentManagement.AMSAgentDescription;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class ACORouter extends GenericRouter
{
	private static final long serialVersionUID = 1L;
	public final int fIterations = 100;
	public static int fNumAnts;
	protected ArrayList<Integer> avalible_locations;
	public List<Ant_VRP> fVRPAnts;
	public PheremoneModel fGraph;
	protected DataModel fDataModel;

	public ACORouter()
	{
		registerO2AInterface(Router.class, this);

		fSelectedAgents = new ArrayList<AMSAgentDescription>();
	}

	protected void setup()
	{
		fDataModel = (DataModel) getArguments()[0];
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
			for (Ant_VRP lAnts : fVRPAnts)
			{
				avalible_locations = lAnts.nextLocation(fGraph, avalible_locations);

				total_distance += lAnts.total_distance_travelled;
			}
		}

		for (Ant_VRP lAnts : fVRPAnts)
		{
			System.out.println("Path: " + lAnts.getPath());
		}



		if (avalible_locations.size() <= 0)
		{
			for (Ant_VRP jAnts : fVRPAnts)
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

		ArrayList<Ant_VRP> return_ants = new ArrayList<Ant_VRP>();
		for (int i = 0; i < fDataModel.numVehicles(); i++)
		{
			return_ants.add(new Ant_VRP(fDataModel));
		}

		int total_distance = 0;
		while (avalible_locations.size() > 0)
		{
			for (Ant_VRP lAnts : return_ants)
			{
				avalible_locations = lAnts.nextLocation(fGraph, avalible_locations);

				total_distance += lAnts.total_distance_travelled;
			}
		}

		int count = 0;
		if (avalible_locations.size() <= 0)
		{
			for (Ant jAnts : return_ants)
			{
				fGraph = jAnts.updateModel(fGraph);

				if (jAnts.total_distance_travelled > 0)
				{
					jAnts.total_distance_travelled = total_distance;
				}

				System.out.println(Arrays.toString(jAnts.getPathArray()));
				System.out.println(count);
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
				if (fGraph.getPheremone(i, j) > max_value)
				{
					max_value = fGraph.getPheremone(i,j);
				}
			}

			for (int j = 0; j < fGraph.numLocations(); j++)
			{
				//play around with this if statement
				if (fGraph.getPheremone(i,j) < max_value * 0.25)
				{
					fGraph.updatePheremonePath(i,j, 0);
				}
			}
		}
	}

	public int[][] solveRoute(DataModel aDataModel, int aMaxRouteDistance)
	{
		// initialize
		fGraph = new PheremoneModel(fDataModel.numVehicles(), fDataModel.numLocations(), fDataModel.getfSeed());
		fDataModel = fGraph;
		fVRPAnts = new ArrayList<Ant_VRP>();
		fNumAnts = fGraph.numVehicles();

		for (int i = 0; i < fNumAnts; i++)
		{
			fVRPAnts.add( new Ant_VRP( fDataModel ) );
		}

		avalible_locations = new ArrayList<Integer>();

		Ant lBestAnt = null;
		int lBestPathLength = 0;

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
			//UpdateTrails();

			for (int i = 0; i < fDataModel.numLocations(); i++)
			{
				for (int j = 0; j < fDataModel.numLocations(); j++)
				{
					System.out.print(fGraph.getPheremone(i, j) +  ", ");
				}
				System.out.println();
			}

			System.out.println();
		}

		return getSolutions();
	}
}
