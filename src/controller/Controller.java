
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
		Runtime lRuntime = Runtime.instance();

		// launch the main container listening on port 8888
		Profile lMainProfile = new ProfileImpl( null, 8888, null );
		// no JADE gui
		lMainProfile.setParameter( Profile.GUI, "false" );
		fContainerCtrl = lRuntime.createMainContainer( lMainProfile );

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
			AgentController lDeliveryController;
			try 
			{
				lDeliveryController = fContainerCtrl.createNewAgent
				(
					"Delivery_Agent" + i,
					DriverAgent.class.getName(),
					new Object[]{ aDataModel, i }
				);

				lDeliveryController.start();
			} 
			catch (StaleProxyException e) {	e.printStackTrace(); }
		}

		// create master
		try
		{
			//based on form input, a specific MasterRouteAgent will be created
			switch( aRoutingMethod )
			{
			case "CHOCO":
				fRouteAgentCtrl = fContainerCtrl.createNewAgent( "MasterRouteAgent", CHOCORouter.class.getName(), new Object[]{ aDataModel } );
				break;
			case "CHOCO2":
				fRouteAgentCtrl = fContainerCtrl.createNewAgent( "MasterRouteAgent", CHOCORouter2.class.getName(), new Object[]{ aDataModel } );
				break;
			case "OR-Tools":
				fRouteAgentCtrl = fContainerCtrl.createNewAgent( "MasterRouteAgent", ORToolsRouter.class.getName(), new Object[]{ aDataModel } );
				break;
			case "ACO":
				fRouteAgentCtrl = fContainerCtrl.createNewAgent( "MasterRouteAgent", ACORouter.class.getName(), new Object[]{ aDataModel } );
				break;
			case "ACO-Partition":
				fRouteAgentCtrl = fContainerCtrl.createNewAgent( "MasterRouteAgent", ACOPartitionRouter.class.getName(), new Object[]{ aDataModel } );
				break;
			default:
				fRouteAgentCtrl = fContainerCtrl.createNewAgent( "MasterRouteAgent", ORToolsRouter.class.getName(), new Object[]{ aDataModel } );
				break;
			}

			fRouteAgentCtrl.start();
		}
		catch( StaleProxyException e ) { e.printStackTrace(); }

		// initialize thread
		fThread = new Thread( this );
		fThread.start();
	}

	public void run() 
	{
		try 
		{
			// wait for drivers to initialize
			Thread.sleep( 2000 );

			// grab interface
			Router lMasterInterface = fRouteAgentCtrl.getO2AInterface( Router.class );

			//records start time of solver
			long lStart = System.currentTimeMillis();

			// solve and distribute routes
			lMasterInterface.distributeRoutes();

			//records end time of solver
			long lFinish = System.currentTimeMillis();

			//Displays total time required for solver to run, providing crucial data for further performance anaylsis
			System.out.println( "Time taken to route and distribute: " + (lFinish - lStart) + "ms" );
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