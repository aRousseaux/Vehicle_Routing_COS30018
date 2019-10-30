
package agents;

import data.DataModel;
import data.PheremoneModel;
import jade.domain.FIPAAgentManagement.AMSAgentDescription;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
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

	//updates pheremone model and resets ants
	public void ConstructSolutions()
	{
		avalible_locations = new ArrayList<Integer>();

		for (int i = 1; i < fGraph.numLocations(); i++)
		{
			avalible_locations.add(i);
		}

		Collections.shuffle(avalible_locations);
		while (avalible_locations.size() > fDataModel.getTotalCapacity())
		{
			avalible_locations.remove(0);
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

		if (avalible_locations.size() <= 0)
		{
			for (Ant_VRP jAnts : fVRPAnts)
			{
				fGraph = jAnts.updateModel(fGraph);

				if (jAnts.total_distance_travelled > 0)
				{
					jAnts.total_distance_travelled = total_distance;
				}

				jAnts.reset();
			}

			avalible_locations = new ArrayList<Integer>();
			for (int i = 1; i < fGraph.numLocations(); i++)
			{
				avalible_locations.add(i);
			}

			Collections.shuffle(avalible_locations);
		}
	}

	//gets the solution, based on pheremone model
	//output int[][] is then sent through to all delivery agents
	public int[][] getSolutions()
	{
		avalible_locations = new ArrayList<Integer>();
		int[][] return_array = new int[fDataModel.numVehicles()][];

		for (int i = 1; i < fGraph.numLocations(); i++)
		{
			avalible_locations.add(i);
		}

		Collections.shuffle(avalible_locations);

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

			Collections.shuffle(avalible_locations);
		}

		return return_array;
	}

	//prune the pheremone model
	//if a value is too small is comparison to the largest value in the model, then these pheremone values are set to 0
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

		//loops through all iteratons, strengthening pheremone model
		for ( int t = 0; t < fIterations; t++ )
		{
			ConstructSolutions();
			//UpdateTrails();
		}

		return getSolutions();
	}
}
