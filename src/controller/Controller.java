
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
	
	public Controller( DataModel aDataModel, String aRoutingMethod )
	{
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
			System.out.println("Creating delivery agent number: " + i);
			
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
		System.out.println(Controller.class.getName() + ": Starting up a Master Route Agent...");
		AgentController lAgentCtrl;
		try
		{
			switch(aRoutingMethod)
			{
			case "CHOCO":
				System.out.print("Creating a CHOCO Master Route Agent...\n");
				//lAgentCtrl = lMainCtrl.createNewAgent("MasterRouteAgent", MasterRouteAgentCHOCO.class.getName(), new Object[]{fDataModel});
				break;
			case "OR-Tools":
				System.out.print("Creating an OR-Tools Master Route Agent...\n");
				lAgentCtrl = lMainCtrl.createNewAgent("MasterRouteAgent", ORToolsRouter.class.getName(), new Object[]{aDataModel});
				break;
			default:
				System.out.print("Creating a default Master Route Agent...\n");
				//lAgentCtrl = lMainCtrl.createNewAgent("MasterRouteAgent", MasterRouteAgentORTools.class.getName(), new Object[]{fDataModel});
				break;
			}
		}
		catch( StaleProxyException e ) { e.printStackTrace(); }
		
		fThread = new Thread(this);
		fThread.start();
	}

	public void run() 
	{
		while (fThread != null)
		{
			try 
			{
				Thread.sleep(5000);
				// do stuff (change location, router, etc.)
			} 
			catch ( InterruptedException e ) { e.printStackTrace(); }
		}
		
		// ask agent to drop tables
	}
	
	// delete thread, terminates controller
	public void shutdown()
	{
		fThread = null;
	}
}