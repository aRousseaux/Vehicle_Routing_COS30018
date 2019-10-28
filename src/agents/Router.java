
package agents;

import data.DataModel;

public interface Router 
{
		//remote interface for class that extend Generic Router
		//allows methods to be called, from the controller

		// grabs the database agent and drivers to upload routes
		public abstract void findDrivers();
		
		// routes driver agents given data model
		public abstract int[][] solveRoute(DataModel aDataModel, int aMaxRouteDistance);

		// distributes routes to the relevant drivers 
		public abstract void distributeRoutes();
		
		// calculates a route length
		public abstract int calculateRouteLength(int[] aRoute, DataModel aDataModel);
}