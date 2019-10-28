
package agents;

import data.DataModel;
import data.PheremoneModel;
import jade.core.Agent;
import jade.domain.AMSService;
import jade.domain.FIPAAgentManagement.AMSAgentDescription;
import jade.domain.FIPAAgentManagement.SearchConstraints;
import jade.lang.acl.ACLMessage;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class ACORouter extends GenericRouter implements Router
{
	private static final long serialVersionUID = 1L;
	public final int fIterations = 100;
	public static int fNumAnts;
	protected ArrayList<Integer> avalible_locations;
	public List<Ant> fAnts;
	public PheremoneModel fGraph;
	protected DataModel fDataModel;

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


	public int calculateRouteLength(int[] aLocations, DataModel aDataModel) 
	{
		return 0;
	}

	@Override
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
					fSelectedAgents.add(lAgents[i]);
				}
			}
		}
		catch (Exception e)	{ e.printStackTrace(); }

		int[][] lSolution = solveRoute(fDataModel, 2000);

		for (int i = 0; i < lSolution.length; i++)
		{
			System.out.println("Solution to be sent: " + Arrays.toString(lSolution[i]));
			ACLMessage message = new ACLMessage(ACLMessage.INFORM);

			for (int j = 0; j < fSelectedAgents.size(); j++)
			{
				if (fSelectedAgents.get(i).getName().getLocalName().contains("Delivery_Agent" + String.valueOf(j)))
				{
					message.addReceiver(fSelectedAgents.get(i).getName());
					message.setContent("Route: " + Arrays.toString(lSolution[i]));
					send(message);
					break;
				}
			}

			for (int j = 0; j < fSelectedAgents.size(); j++)
			{
				if (fSelectedAgents.get(i).getName().getLocalName().contains("MasterRouteAgent" + String.valueOf(j)))
				{
					message.addReceiver(fSelectedAgents.get(i).getName());
					message.setContent("agent_routes:" + i + " " +Arrays.toString(lSolution[i]).trim() + " " + calculateRouteLength(lSolution[i], fDataModel) );
					send(message);
				}
			}
		}
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
