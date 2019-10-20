
package controller;

import agents.DatabaseAgent;
import jade.core.Profile;
import jade.core.ProfileImpl;
import jade.core.Runtime;
import jade.wrapper.AgentController;
import jade.wrapper.ContainerController;
import jade.wrapper.StaleProxyException;

public class Controller 
{
	public Controller()
	{
		// get a hold to the JADE runtime
		Runtime rt = Runtime.instance();
		
		// launch the main container listening on port 8888
		Profile pMain = new ProfileImpl(null, 8888, null);
		pMain.setParameter(Profile.GUI, "true");
		ContainerController lMainCtrl = rt.createMainContainer(pMain);
		
		// create database agent
		AgentController dbAgentCtrl;
		try 
		{
			dbAgentCtrl = lMainCtrl.createNewAgent
			(
				"DBAgent", 
				DatabaseAgent.class.getName(), 
				new Object[]{}
			);
			
			dbAgentCtrl.start();
		} 
		catch ( StaleProxyException e ) { e.printStackTrace(); }
	}
}