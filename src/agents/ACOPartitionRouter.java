
package agents;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import data.*;
import jade.domain.FIPAAgentManagement.AMSAgentDescription;

public class ACOPartitionRouter extends ACORouter
{
	private static final long serialVersionUID = 1L;

	// refer to table 1; On Partitioning Grids into Equal Parts
	private final double[] fKPartitionTable = new double[] { 0, 1, 1.66, 2, 2.66, 3, 3.47, 3.75, 4 };
	private ArrayList<Integer>[] ant_routes;

	public ACOPartitionRouter()
	{
		registerO2AInterface(Router.class, this);
		fSelectedAgents = new ArrayList<AMSAgentDescription>();
	}

	protected void setup()
	{
		fDataModel = (DataModel) getArguments()[0];
		if (fDataModel == null)
		{
			System.out.println("NULL");
		}

		fGraph = new PheremoneModel( fDataModel.numVehicles(), fDataModel.numLocations(), fDataModel.getfSeed(), fDataModel.getCapacities());
		fDataModel = fGraph;

		fVRPAnts = new ArrayList<Ant_VRP>();

		fNumAnts = (int) Math.pow(fDataModel.numLocations(), 2);
		ant_routes = (ArrayList<Integer>[]) new ArrayList[fDataModel.numVehicles()];
	}


	private ArrayList<Location>[] kPartitionLocations( DataModel aDataModel )
	{
		/*
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
		 */

		KMeans kMeans = new KMeans(aDataModel);
		List<Cluster> clusters = kMeans.getClusters();

		ArrayList<Location>[] cluster_locations = (ArrayList<Location>[]) new ArrayList[clusters.size()];

		for (int i = 0; i < clusters.size(); i++)
		{
			cluster_locations[i] = (ArrayList<Location>) clusters.get(i).getPoints();
		}

		return cluster_locations;
	}

	//locations are randomly divided to an Array of ArrayLists of Locations, to test underlying ACO functionality
	private ArrayList<Location>[] randomPartition(DataModel aDataModel)
	{
		ArrayList<Location>[] lResult = (ArrayList<Location>[]) new ArrayList[aDataModel.numVehicles()];
		List<Integer> lRemaining = new ArrayList<Integer>();

		for (int i = 0; i < lResult.length; i++)
		{
			lResult[i] = new ArrayList<Location>();
		}

		for ( int i = 0; i < aDataModel.numLocations(); i++ )
		{
			lRemaining.add( i );
		}

		Collections.shuffle(lRemaining);

		while (lRemaining.size() > 0)
		{
			for (int i = 0; i < aDataModel.numVehicles(); i++)
			{
				if (lRemaining.size() > 0)
				{
					lResult[i].add(aDataModel.getLocation(lRemaining.get(0)));
					lRemaining.remove(0);
				}
			}
		}

		return lResult;
	}

	public int[][] getSolutions() {
		return solveRoute(fDataModel,10000);
	}

	public int[][] solveRoute(DataModel aDataModel, int aMaxRouteDistance)
	{
		// solve each partition
		ArrayList<Location>[] lPartitions = kPartitionLocations( fDataModel );
		System.out.println("Partition Size: " + lPartitions.length);

		//gets the location_id, from the locations in lPartitions
		for (int i = 0; i < lPartitions.length; i++)
		{
			ant_routes[i] = arrayListIntFromArrayListLocation(lPartitions[i]);
		}

		fNumAnts = (int) Math.pow(fDataModel.numLocations(), 2);

		int counter = 0;

		//the partitions are assigned to the ants present, looping through all the partitions, before resetting back to the first partition
		for (int i = 0; i < fNumAnts; i++)
		{
			fVRPAnts.add( new Ant_VRP(fDataModel, (ArrayList<Integer>) ant_routes[counter].clone(), (fDataModel.numVehicles()  - 1) % (i + 1)));
			counter++;
			if (counter >= ant_routes.length)
			{
				counter = 0;
			}
		}

		//then ACO run for fIterations, constantly refining the pheremone model.
		for ( int t = 0; t < fIterations; t++ )
		{
			ConstructSolutions(ant_routes);
			UpdateTrails();
		}


		for ( Ant lAnts : fVRPAnts ) {
			while (lAnts.nextLocation(fGraph))
			{

			}
		}

		int[][] return_array = new int[fDataModel.numVehicles()][];
		for (int i = 0; i < return_array.length; i++)
		{
			return_array[i] = fVRPAnts.get(i).getPathArray();
		}

		return return_array;
	}

	//gets the location IDs from input locations in locations list, returning them
	public ArrayList<Integer> arrayListIntFromArrayListLocation(ArrayList<Location> location_list)
	{
		ArrayList<Integer> return_list = new ArrayList<Integer>();

		for (int i = 0; i < location_list.size(); i++)
		{
			if (location_list.get(i).getfLocationID() != 0)
			{
				return_list.add(location_list.get(i).getfLocationID());
			}
		}

		return return_list;
	}

	//updates pheremone model and resets ants
	public void ConstructSolutions(ArrayList<Integer>[] input_routes)
	{
		int counter = 0;
		for ( Ant lAnts : fVRPAnts )
		{
			while (lAnts.nextLocation(fGraph))
			{

			}

			fGraph = lAnts.updateModel(fGraph);

			lAnts.reset((ArrayList<Integer>) input_routes[counter].clone());

			counter++;
			if (counter >= input_routes.length)
			{
				counter = 0;
			}
		}
	}
}
