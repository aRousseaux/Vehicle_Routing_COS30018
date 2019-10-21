
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
	private AgentController fDBAgentCtrl;
	private AgentController fRouteAgentCtrl;
	
	public Controller( DataModel aDataModel, String aRoutingMethod )
	{
		// initialize thread
		fThread = new Thread(this);
		
		// get a hold to the JADE runtime
		Runtime rt = Runtime.instance();
		
		// launch the main container listening on port 8888
		Profile pMain = new ProfileImpl(null, 8888, null);
		pMain.setParameter(Profile.GUI, "true");
		ContainerController lMainCtrl = rt.createMainContainer(pMain);
		
		// create database agent
		try 
		{
			fDBAgentCtrl = lMainCtrl.createNewAgent
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
				delivery = lMainCtrl.createNewAgent
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
				//fRouteAgentCtrl = lMainCtrl.createNewAgent("MasterRouteAgent", MasterRouteAgentCHOCO.class.getName(), new Object[]{fDataModel});
				break;
			case "OR-Tools":
				System.out.print("Creating an OR-Tools Master Route Agent...\n");
				fRouteAgentCtrl = lMainCtrl.createNewAgent("MasterRouteAgent", ORToolsRouter.class.getName(), new Object[]{aDataModel});
				break;
			default:
				System.out.print("Creating a default Master Route Agent...\n");
				//fRouteAgentCtrl = lMainCtrl.createNewAgent("MasterRouteAgent", MasterRouteAgentORTools.class.getName(), new Object[]{fDataModel});
				break;
			}
			
			fThread.wait(2000);
			// run solver
			Router lMasterInterface = fRouteAgentCtrl.getO2AInterface(Router.class);
			lMasterInterface.distributeRoutes();
		}
		catch( StaleProxyException | InterruptedException e ) { e.printStackTrace(); }
		
		
		fThread.start();
	}

	public void run() 
	{
		try 
		{
			// wait for drivers to initialize
			Thread.sleep(5000);
			System.out.println("Controller is still running");
		} 
		catch ( InterruptedException e ) { e.printStackTrace(); }
	}
	
	// delete thread, terminates controller
	public void shutdown()
	{
		fThread = null;
		// ask agent to drop tables
	}
}