
package main;

import gui.RoutingFrame;

public class Main 
{
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
	}
}