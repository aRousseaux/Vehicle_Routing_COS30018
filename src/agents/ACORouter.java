
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
	public final int fIterations = 500;
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

		int total_distance = 0;
		while (avalible_locations.size() > 0)
		{
			if (fDataModel.getTotalCapacity() < fDataModel.numLocations() && fDataModel.getTotalCapacity() <= fDataModel.numLocations() - avalible_locations.size())
			{
				avalible_locations = new ArrayList<Integer>();
			}

			for (Ant_VRP lAnts : fVRPAnts)
			{
				avalible_locations = lAnts.nextLocation(fGraph, avalible_locations);

				total_distance += lAnts.fTotalDistance;
			}
		}

		if (avalible_locations.size() <= 0)
		{
			for (Ant_VRP jAnts : fVRPAnts)
			{
				fGraph = jAnts.updateModel(fGraph, 2);

				if (jAnts.fTotalDistance > 0)
				{
					jAnts.fTotalDistance = total_distance;
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
		System.out.println("get solution");

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
			return_ants.add(new Ant_VRP(fDataModel, i));
		}

		int total_distance = 0;
		while (avalible_locations.size() > 0)
		{
			if (fDataModel.getTotalCapacity() < fDataModel.numLocations() && fDataModel.getTotalCapacity() <= fDataModel.numLocations() - avalible_locations.size())
			{
				avalible_locations = new ArrayList<Integer>();
			}

			for (Ant_VRP lAnts : return_ants)
			{
				avalible_locations = lAnts.nextLocation(fGraph, avalible_locations);

				total_distance += lAnts.fTotalDistance;
			}
		}

		int count = 0;
		if (avalible_locations.size() <= 0)
		{
			for (Ant jAnts : return_ants)
			{
				fGraph = jAnts.updateModel(fGraph, 2);

				if (jAnts.fTotalDistance > 0)
				{
					jAnts.fTotalDistance = total_distance;
				}

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
	public void UpdateTrails(Integer current_iteration)
	{
		float ratio = Float.valueOf((float) current_iteration)/((float) fIterations);

		for (int i = 0; i < fGraph.numLocations(); i++)
		{
			float max_value = 0;
			for (int j = 0; j < fGraph.numLocations(); j++)
			{
				if (fGraph.getPheremone(i, j) > max_value)
				{
					max_value = fGraph.getPheremone(i,j);
				}
			}

			for (int j = 0; j < fGraph.numLocations(); j++)
			{
				//play around with this if statement
				if (fGraph.getPheremone(i,j) * 2 < max_value * ratio)
				{
					fGraph.updatePheremonePath(i,j, 0);
				}
			}
		}
	}

	public int[][] solveRoute(DataModel aDataModel, int aMaxRouteDistance)
	{
		fGraph = new PheremoneModel(fDataModel.numVehicles(), fDataModel.numLocations(), fDataModel.getfSeed(), fDataModel.getCapacities());

		fDataModel = fGraph;
		fVRPAnts = new ArrayList<Ant_VRP>();
		fNumAnts = fGraph.numVehicles();

		for (int i = 0; i < fNumAnts; i++)
		{
			fVRPAnts.add(new Ant_VRP(fDataModel, i));
		}

		avalible_locations = new ArrayList<Integer>();

		//loops through all iteratons, strengthening pheremone model
		for ( int t = 0; t < fIterations; t++ )
		{
			ConstructSolutions();
			UpdateTrails(Integer.valueOf(t));
		}

        for (int i = 0; i < fGraph.getDistanceMatrix().length; i++)
        {
            for (int j = 0 ; j < fGraph.getDistanceMatrix()[i].length; j++)
            {
                System.out.print(  fGraph.getPheremone(i, j) + ", ");
            }
            System.out.println();
        }

		return getSolutions();
	}
}
