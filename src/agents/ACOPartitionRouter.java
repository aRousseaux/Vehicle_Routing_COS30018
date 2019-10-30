
package agents;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import data.DataModel;
import data.Location;
import data.PheremoneModel;
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

		fGraph = new PheremoneModel( fDataModel.numVehicles(), fDataModel.numLocations(), 9 );
		fDataModel = fGraph;

		fVRPAnts = new ArrayList<Ant_VRP>();

		fNumAnts = (int) Math.pow(fDataModel.numLocations(), 2);
		ant_routes = (ArrayList<Integer>[]) new ArrayList[fDataModel.numVehicles()];
	}


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
		// fix index, too tired to think about this

		int counter = 0;

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

		for (int i = 0; i < lResult.length; i++)
		{
			for (int j = 0; j < lResult[i].size(); j++)
			{
				System.out.print(lResult[i].get(j).getfLocationID() + ", ");
			}

			System.out.println();
		}

		return lResult;
	}

	public int[][] getSolutions() {
		return solveRoute(fDataModel,10000);
	}

	public int[][] solveRoute(DataModel aDataModel, int aMaxRouteDistance)
	{
		// solve each partition
		ArrayList<Location>[] lPartitions = randomPartition( fDataModel );

		for (int i = 0; i < lPartitions.length; i++)
		{
			ant_routes[i] = arrayListIntFromArrayListLocation(lPartitions[i]);
		}

		fNumAnts = (int) Math.pow(fDataModel.numLocations(), 2);

		for (int i = 0; i < fDataModel.numLocations(); i++)
		{
			for (int j = 0; j < fDataModel.numLocations(); j++)
			{
				System.out.print(fDataModel.getDistanceMatrix()[i][j] +  ", ");
			}
			System.out.println();
		}

		int counter = 0;

		for (int i = 0; i < fNumAnts; i++)
		{
			//System.out.println(Arrays.toString(ant_routes[counter].toArray()));
			fVRPAnts.add( new Ant_VRP( fDataModel, (ArrayList<Integer>) ant_routes[counter].clone()) );
			counter++;
			if (counter >= ant_routes.length)
			{
				counter = 0;
			}
		}

		for ( int t = 0; t < fIterations; t++ )
		{
			ConstructSolutions(ant_routes);
			UpdateTrails();

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

		for ( Ant lAnts : fVRPAnts ) {
			while (lAnts.nextLocation(fGraph)) {

			}
		}

		int[][] return_array = new int[fDataModel.numVehicles()][];

		System.out.println("Final Routes: ");

		for (int i = 0; i < return_array.length; i++)
		{
			return_array[i] = fVRPAnts.get(i).getPathArray();
			System.out.println(Arrays.toString(return_array[i]));
		}

		return return_array;
	}

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
