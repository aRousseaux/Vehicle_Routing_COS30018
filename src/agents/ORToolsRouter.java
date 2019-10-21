
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
import jade.core.Agent;
import jade.domain.AMSService;
import jade.domain.FIPAAgentManagement.AMSAgentDescription;
import jade.domain.FIPAAgentManagement.SearchConstraints;
import jade.lang.acl.ACLMessage;

public class ORToolsRouter extends Agent implements Router
{
	private static final long serialVersionUID = 1L;
	
	private DataModel fDataModel;
	private ArrayList<AMSAgentDescription> selectedAgents;
	
	static
	{
		System.loadLibrary("jniortools");
	}
	
	//registers interface for the class
	public ORToolsRouter()
	{
		registerO2AInterface(Router.class, this);
		
		selectedAgents = new ArrayList<AMSAgentDescription>();
	}
	
	protected void setup()
	{
		fDataModel = (DataModel) getArguments()[0];
	}
	
	public void findDrivers() 
	{
		try
		{
			SearchConstraints sc = new SearchConstraints();
			sc.setMaxResults(Long.valueOf(-1));
			AMSService.search(this, new AMSAgentDescription(), sc);
		}
		catch (Exception e) { e.printStackTrace(); }
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

	public void distributeRoutes() 
	{
		AMSAgentDescription[] lAgents;

		SearchConstraints sc = new SearchConstraints();
		sc.setMaxResults(Long.valueOf(-1)); 
		
		try 
		{
			lAgents = AMSService.search(this, new AMSAgentDescription(), sc);

			for (int i = 0; i < lAgents.length; i++)
			{
				if (lAgents[i].getName().toString().contains("Delivery_Agent"))
				{
					selectedAgents.add(lAgents[i]);
				}
			}
		}
		catch (Exception e)	{ e.printStackTrace(); }

		int[][] lSolution = solveRoute(fDataModel, 2000);
		
		System.out.println("Sending routes out");
		for (int i = 0; i < lSolution.length; i++)
		{
			System.out.println(Arrays.toString(lSolution[i]));
			ACLMessage message = new ACLMessage(ACLMessage.INFORM);

			for (int j = 0; j < selectedAgents.size(); j++)
			{
				if (selectedAgents.get(i).getName().getLocalName().contains("Delivery_Agent" + String.valueOf(j + 1)))
				{
					message.addReceiver(selectedAgents.get(i).getName());
					message.setContent("Route: " + Arrays.toString(lSolution[i]));
					send(message);
					break;
				}
			}
		}
	}

	public int calculateRouteLength(int[] aRoute, DataModel aDataModel) 
	{
		int lRouteLength = 0;

		for (int i = 1; i < aRoute.length; i++)
		{
			lRouteLength += aDataModel.getDistanceMatrix()[aRoute[i]][aRoute[i-1]];
		}

		return lRouteLength;
	}
	
}
