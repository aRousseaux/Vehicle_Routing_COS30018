
package main;

import controller.Controller;
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
		// create jade controller
		fController = new Controller();
		
		// schedule GUI creation
		javax.swing.SwingUtilities.invokeLater( new Runnable() 
		{
			public void run() 
			{
				createAndShowGUI();
			}
		});
	}
}