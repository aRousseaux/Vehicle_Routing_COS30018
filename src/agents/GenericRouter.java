
package agents;

import java.util.ArrayList;
import java.util.Arrays;

import data.DataModel;
import jade.core.Agent;
import jade.domain.AMSService;
import jade.domain.FIPAAgentManagement.AMSAgentDescription;
import jade.domain.FIPAAgentManagement.SearchConstraints;
import jade.lang.acl.ACLMessage;

public abstract class GenericRouter extends Agent implements Router
{
	private static final long serialVersionUID = 1L;

	protected DataModel fDataModel;
	protected ArrayList<AMSAgentDescription> fSelectedAgents;

	public abstract int[][] solveRoute(DataModel aDataModel, int aMaxRouteDistance);

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
