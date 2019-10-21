
package main;

import controller.Controller;
import data.DataModel;
import gui.RoutingFrame;

public class Main 
{
	private static Controller fController;
	
	private static void createAndShowGUI()
	{
		( new RoutingFrame( "Vehicle Routing" ) ).setVisible( true );
	}

	public static void main(String[] args)
	{
		// schedule GUI creation
		javax.swing.SwingUtilities.invokeLater( new Runnable() 
		{
			public void run() 
			{
				createAndShowGUI();
			}
		});
		
		int lNumVehicles = 4;
		int lNumLocations = 16;
		int lSeed = 1;
		int lCapacity = 4;
		DataModel lDataModel = new DataModel(lNumVehicles, lNumLocations, lSeed, lCapacity);
		
		// create jade controller
		fController = new Controller( lDataModel, "OR-Tools" );
	}
}