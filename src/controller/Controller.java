
package controller;

import jade.core.Profile;
import jade.core.ProfileImpl;
import jade.core.Runtime;
import jade.wrapper.ContainerController;

public class Controller 
{
	public Controller()
	{
		// get a hold to the JADE runtime
		Runtime rt = Runtime.instance();
		
		// launch the main container listening on port 8888
		Profile pMain = new ProfileImpl(null, 8888, null);
		pMain.setParameter(Profile.GUI, "true");
		ContainerController mainCtrl = rt.createMainContainer(pMain);
	}
}