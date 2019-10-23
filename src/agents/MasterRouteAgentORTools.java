package agents;

import com.google.ortools.constraintsolver.Assignment;
import com.google.ortools.constraintsolver.FirstSolutionStrategy;
import com.google.ortools.constraintsolver.RoutingDimension;
import com.google.ortools.constraintsolver.RoutingIndexManager;
import com.google.ortools.constraintsolver.RoutingModel;
import com.google.ortools.constraintsolver.RoutingSearchParameters;
import com.google.ortools.constraintsolver.main;

import data.DataModel;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.logging.Logger;
import jade.core.Agent;
import jade.core.behaviours.Behaviour;
import jade.core.behaviours.CyclicBehaviour;
import jade.domain.AMSService;
import jade.domain.FIPAAgentManagement.AMSAgentDescription;
import jade.domain.FIPAAgentManagement.SearchConstraints;
import jade.lang.acl.ACLMessage;

public class MasterRouteAgentORTools extends Agent implements Router
{
	private static final long serialVersionUID = 1L;
	private static final Logger logger = Logger.getLogger(MasterRouteAgentORTools.class.getName());
	private static Connection fDBConnection;
	private int[] vehicle_capacities;
	private DataModel fDataModel;
	private ArrayList<AMSAgentDescription> selectedAgents = new ArrayList<AMSAgentDescription>();

	static
	{
		System.loadLibrary("jniortools");
	}

	//registers interface for the class
	public MasterRouteAgentORTools()
	{
		registerO2AInterface(Router.class, this);
	}

	protected void setup()
	{
		// Connect to Database
		try
		{
			Class.forName("com.mysql.jdbc.Driver");
			fDBConnection = DriverManager.getConnection("jdbc:mysql://localhost:3306/deliveryroutingdb", "root", "");
		}
		catch (Exception e)
		{
			System.out.print(e);
		}

		fDataModel = (DataModel) getArguments()[0];

		/*
        addBehaviour(new CyclicBehaviour(this)
        {
			private static final long serialVersionUID = 1L;

			public void action()
            {
                ACLMessage msg = receive();
                if (msg!=null)
                {
                    logger.info(msg.getSender().getLocalName() + " <- " + msg.getContent());
                    System.out.println(msg.getSender().getLocalName() + " <- " + msg.getContent());
                }
                //block();
            }
        });

		 */
	}

	public void findDrivers()
	{
		try
		{
			SearchConstraints sc = new SearchConstraints();
			sc.setMaxResults(Long.valueOf(-1)); // not sure of the default value, but this ensures you get them all.
			AMSService.search(this, new AMSAgentDescription(), sc);
		}
		catch (Exception e)
		{
			System.out.println(e.toString());
		}
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

	/// @brief Print the solution.
	static void printSolution(data.DataModel data, RoutingModel routing, RoutingIndexManager manager, Assignment solution)
	{
		// Inspect solution.
		long maxRouteDistance = 0;

		for (int i = 0; i < data.numVehicles(); ++i)
		{
			long index = routing.start(i);
			long routeDistance = 0;
			String route = "";
			while (!routing.isEnd(index))
			{
				route += manager.indexToNode(index) + " -> ";
				long previousIndex = index;
				index = solution.value(routing.nextVar(index));
				System.out.println();
				routeDistance += routing.getArcCostForVehicle(previousIndex, index, i);
			}

			if (routeDistance > 0)
			{
				logger.info("Route for Vehicle " + i + ":");
				logger.info(route + manager.indexToNode(index));
				logger.info("Distance of the route: " + routeDistance + "m");
				maxRouteDistance = Math.max(routeDistance, maxRouteDistance);
			}
		}
		logger.info("Maximum of the route distances: " + maxRouteDistance + "m");
	}

	public int[][] solveRoute(DataModel aDataModel, int max_route_distance)
	{
		// Create Routing Index Manager
		RoutingIndexManager manager =
				new RoutingIndexManager(aDataModel.getDistanceMatrix().length, aDataModel.numVehicles(), 0);

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
			return fDataModel.getLocation(fromNode).getCapacity();
		});
		
		// Define cost of each arc.
		routing.setArcCostEvaluatorOfAllVehicles(transitCallbackIndex);

		//add constraints
		routing.addDimensionWithVehicleCapacity(demandCallbackIndex, 0, Arrays.stream(fDataModel.getCapacities()).mapToLong(i -> i).toArray(), true, "Capactiy_Constraint");
		
		// Add Distance constraint.
		routing.addDimension(transitCallbackIndex, 0, max_route_distance,
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

		// Upload solution to database
		for (int i = 0; i < return_multi_dimensional_array.length; i++)
		{
			String lRouteString = "";

			for (int j = 0; j < return_multi_dimensional_array[i].length; j++)
			{
				lRouteString += (return_multi_dimensional_array[i][j] + " ");
			}

			try 
			{
				System.out.println("uploading route");
				Statement stmt = fDBConnection.createStatement();
				stmt.executeUpdate("INSERT INTO agent_routes VALUES (" + i + ", '" + lRouteString + "', " + calculateRouteLength(return_multi_dimensional_array[i], aDataModel) + ");");
			}
			catch(SQLException e)
			{
				System.out.print(e);
			}
		}

		return return_multi_dimensional_array;
	}

	public int calculateRouteLength(int[] aLocations, DataModel aDataModel)
	{
		int lRouteLength = 0;

		for (int i = 1; i < aLocations.length; i++)
		{
			lRouteLength += aDataModel.getDistanceMatrix()[aLocations[i]][aLocations[i-1]];
		}

		return lRouteLength;
	}

	public void distributeRoutes()
	{
		System.out.print("Master Routing agent is distributing routes\n");
		AMSAgentDescription[] fAgents;
		
		SearchConstraints sc = new SearchConstraints();
		sc.setMaxResults(Long.valueOf(-1)); // not sure of the default value, but this ensures you get them all.

		try 
		{
			fAgents = AMSService.search(this, new AMSAgentDescription(), sc);

			for (int i = 0; i < fAgents.length; i++)
			{
				if (fAgents[i].getName().toString().contains("Delivery_Agent"))
				{
					selectedAgents.add(fAgents[i]);
				}
			}

			vehicle_capacities = new int[selectedAgents.size()];

			ACLMessage message = new ACLMessage(ACLMessage.REQUEST);
			message.setContent("GET CAPACITY");
			for (int i = 0; i < selectedAgents.size(); i++)
			{
				message.addReceiver(selectedAgents.get(i).getName());
			}
			send(message);
		}
		catch (Exception e)	{ e.printStackTrace(); }

		Behaviour check_response = new CyclicBehaviour(this)
		{
			private static final long serialVersionUID = 1L;
			int fNumberOfResponses = 0;

			public void action()
			{
				ACLMessage msg = receive();
				if (msg!=null)
				{
					if (msg.getContent().contains("Capacity"))
					{
						fNumberOfResponses++;
						String agent_name = msg.getSender().getLocalName();
						agent_name = agent_name.split("Delivery_Agent")[1];

						Integer capacity = Integer.valueOf(msg.getContent().split("Capacity: ")[1]);
						vehicle_capacities[Integer.valueOf(agent_name) - 1] = capacity;

						if (fNumberOfResponses == vehicle_capacities.length)
						{
							System.out.println("Running Solver");
							int[][] vehicle_routes = solveRoute(fDataModel, 1000000);
							sendVehicleRoutes(vehicle_routes);
							System.out.println("Sent solution");
						}
					}

					if (msg.getContent().contains("position="))
					{
						System.out.println(msg.getContent());

						//choose to have drive continue current path, or follow new path
						ACLMessage drive_response = new ACLMessage(7); // INFORM
						drive_response.addReceiver(msg.getSender());
						drive_response.setContent("continue_path");
					}
				}
				//block();
			}
		};

		addBehaviour(check_response);
	}

	//sends out all of the routes to the vehicles as ACL messages
	public void sendVehicleRoutes(int[][] input_routes)
	{
		System.out.print("Sending routes out\n");
		for (int i = 0; i < input_routes.length; i++)
		{
			System.out.println(Arrays.toString(input_routes[i]));
			ACLMessage message = new ACLMessage(ACLMessage.INFORM);

			for (int j = 0; j < selectedAgents.size(); j++)
			{
				if (selectedAgents.get(i).getName().getLocalName().contains("Delivery_Agent" + String.valueOf(j + 1)))
				{
					message.addReceiver(selectedAgents.get(i).getName());
					message.setContent("Route: " + Arrays.toString(input_routes[i]));
					send(message);
					break;
				}
			}
		}
	}
}