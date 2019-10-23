
package controller;

import agents.*;
import data.DataModel;

import jade.core.Profile;
import jade.core.ProfileImpl;
import jade.core.Runtime;
import jade.wrapper.AgentController;
import jade.wrapper.ContainerController;
import jade.wrapper.StaleProxyException;

public class Controller implements Runnable
{
	private Thread fThread;
	private ContainerController fContainerCtrl;
	private AgentController fDBAgentCtrl;
	private AgentController fRouteAgentCtrl;
	
	public Controller( DataModel aDataModel, String aRoutingMethod )
	{
		// get a hold to the JADE runtime
		Runtime rt = Runtime.instance();
		
		// launch the main container listening on port 8888
		Profile pMain = new ProfileImpl(null, 8888, null);
		pMain.setParameter(Profile.GUI, "true");
		fContainerCtrl = rt.createMainContainer(pMain);
		
		// create database agent
		try 
		{
			fDBAgentCtrl = fContainerCtrl.createNewAgent
			(
				"DBAgent", 
				DatabaseAgent.class.getName(), 
				new Object[]{ aDataModel }
			);
			
			fDBAgentCtrl.start();
		} 
		catch ( StaleProxyException e ) { e.printStackTrace(); }
		
		// create drivers
		for ( int i = 0; i < aDataModel.numVehicles(); i++ )
		{
			//initialize agent
			AgentController delivery;
			try 
			{
				delivery = fContainerCtrl.createNewAgent
				(
					"Delivery_Agent" + i,
					DriverAgent.class.getName(),
					new Object[]{aDataModel, i}
				);

				delivery.start();
			} 
			catch (StaleProxyException e) {	e.printStackTrace(); }
		}
		
		// create master
		try
		{
			switch(aRoutingMethod)
			{
			case "CHOCO":
				System.out.print("Creating a CHOCO Master Route Agent...\n");
				fRouteAgentCtrl = fContainerCtrl.createNewAgent("MasterRouteAgent", CHOCORouter.class.getName(), new Object[]{aDataModel});
				break;
			case "OR-Tools":
				System.out.print("Creating an OR-Tools Master Route Agent...\n");
				fRouteAgentCtrl = fContainerCtrl.createNewAgent("MasterRouteAgent", ORToolsRouter.class.getName(), new Object[]{aDataModel});
				break;
			case "ACO":
				System.out.print("Creating an ACO Master Route Agent...\n");
				fRouteAgentCtrl = fContainerCtrl.createNewAgent("MasterRouteAgent", ACORouter.class.getName(), new Object[]{aDataModel});
			case "ACO-Partition":
				System.out.print("Creating an ACO Master Route Agent...\n");
				fRouteAgentCtrl = fContainerCtrl.createNewAgent("MasterRouteAgent", ACOPartitionRouter.class.getName(), new Object[]{aDataModel});
			default:
				System.out.print("Creating a default Master Route Agent...\n");
				fRouteAgentCtrl = fContainerCtrl.createNewAgent("MasterRouteAgent", ORToolsRouter.class.getName(), new Object[]{aDataModel});
				break;
			}

			fRouteAgentCtrl.start();
		}
		catch( StaleProxyException e ) { e.printStackTrace(); }
		
		// initialize thread
		fThread = new Thread(this);
		fThread.start();
	}

	public void run() 
	{
		try 
		{
			// wait for drivers to initialize
			Thread.sleep(2000);
			
			// grab interface
			Router lMasterInterface = fRouteAgentCtrl.getO2AInterface(Router.class);
			
			long lStart = System.currentTimeMillis();
			
			// solve and distribute routes
			lMasterInterface.distributeRoutes();
			
			long lFinish = System.currentTimeMillis();
			System.out.println("Time taken to route: " + (lFinish - lStart) + " milliseconds.");
		} 
		catch ( InterruptedException | StaleProxyException e ) { e.printStackTrace(); }
	}
	
	// delete thread, terminates controller
	public void shutdown() throws StaleProxyException
	{
		fContainerCtrl.kill();
		fThread = null;
	}
}