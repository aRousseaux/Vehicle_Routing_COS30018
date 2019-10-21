
package controller;

import agents.DatabaseAgent;
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
	
	public Controller()
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
				new Object[]{}
			);
			
			fDBAgentCtrl.start();
		} 
		catch ( StaleProxyException e ) { e.printStackTrace(); }
		
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