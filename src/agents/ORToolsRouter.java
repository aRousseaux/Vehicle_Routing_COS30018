
package agents;

import java.util.ArrayList;
import java.util.Arrays;

import com.google.ortools.constraintsolver.Assignment;
import com.google.ortools.constraintsolver.FirstSolutionStrategy;
import com.google.ortools.constraintsolver.RoutingDimension;
import com.google.ortools.constraintsolver.RoutingIndexManager;
import com.google.ortools.constraintsolver.RoutingModel;
import com.google.ortools.constraintsolver.RoutingSearchParameters;
import com.google.ortools.constraintsolver.main;

import data.DataModel;
import jade.domain.FIPAAgentManagement.AMSAgentDescription;

public class ORToolsRouter extends GenericRouter
{
	private static final long serialVersionUID = 1L;

	static
	{
		System.loadLibrary("jniortools");
	}

	//registers interface for the class
	public ORToolsRouter()
	{
		registerO2AInterface(Router.class, this);

		fSelectedAgents = new ArrayList<AMSAgentDescription>();
	}

	protected void setup()
	{
		fDataModel = (DataModel) getArguments()[0];
	}

	public int[][] solveRoute(DataModel aDataModel, int aMaxRouteDistance) 
	{
		// update data model
		fDataModel = aDataModel;

		// Create Routing Index Manager
		RoutingIndexManager manager =
				new RoutingIndexManager(aDataModel.getDistanceMatrix().length, aDataModel.numVehicles(), 0); // depot always 0

		// Create Routing Model.
		RoutingModel routing = new RoutingModel(manager);

		// Create and register a transit callback.
		final int transitCallbackIndex =
				routing.registerTransitCallback((long fromIndex, long toIndex) -> {
					// Convert from routing variable Index to user NodeIndex.
					int fromNode = manager.indexToNode(fromIndex);
					int toNode = manager.indexToNode(toIndex);
					return aDataModel.getDistanceMatrix()[fromNode][toNode];
				});

		//https://developers.google.com/optimization/routing/cvrp
		final int demandCallbackIndex = routing.registerUnaryTransitCallback((long fromIndex) -> {
			// Convert from routing variable Index to user NodeIndex.
			int fromNode = manager.indexToNode(fromIndex);
			return aDataModel.getLocation(fromNode).getCapacity();
		});

		// Define cost of each arc.
		routing.setArcCostEvaluatorOfAllVehicles(transitCallbackIndex);

		//add constraints
		routing.addDimensionWithVehicleCapacity(demandCallbackIndex, 0, Arrays.stream(aDataModel.getCapacities()).mapToLong(i -> i).toArray(), true, "Capactiy_Constraint");
		System.out.println("Capacities " + Arrays.toString(aDataModel.getCapacities()));

		// Add Distance constraint.
		routing.addDimension(transitCallbackIndex, 0, aMaxRouteDistance,
				true, // start cumul to zero
				"Distance");
		RoutingDimension distanceDimension = routing.getMutableDimension("Distance");
		distanceDimension.setGlobalSpanCostCoefficient(100);

		// Setting first solution heuristic.
		RoutingSearchParameters searchParameters =
				main.defaultRoutingSearchParameters()
				.toBuilder()
				.setFirstSolutionStrategy(FirstSolutionStrategy.Value.PATH_CHEAPEST_ARC)
				.build();
		// Solve the problem.
		Assignment solution = routing.solveWithParameters(searchParameters);

		// Print solution on console.
		//printSolution(aDataModel, routing, manager, solution);

		// Get array of arrays
		int[][] return_multi_dimensional_array = new int[aDataModel.numVehicles()][];

		for (int j = 0; j < aDataModel.numVehicles(); j++)
		{
			int[] output = getRouteArray(j, aDataModel, manager, routing, solution);
			return_multi_dimensional_array[j] = output;
		}

		return return_multi_dimensional_array;
	}

	static int[] getRouteArray(int input_index, data.DataModel data, RoutingIndexManager manager, RoutingModel routing, Assignment solution)
	{
		ArrayList<Integer> output_route_array = new ArrayList<Integer>();

		long index = routing.start(input_index);

		while (!routing.isEnd(index))
		{
			output_route_array.add(manager.indexToNode(index));
			index = solution.value(routing.nextVar(index));
		}

		output_route_array.add(0);

		if (output_route_array.size() > 0) {
			int[] return_array = new int[output_route_array.size()];

			for (int i = 0; i < output_route_array.size(); i++)
			{
				return_array[i] = output_route_array.get(i);
			}

			return return_array;
		}
		else
		{
			return null;
		}
	}
}
