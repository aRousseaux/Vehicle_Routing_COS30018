/* 
 * KMeans.java ; Cluster.java ; Point.java
 *
 * Solution implemented by DataOnFocus
 * www.dataonfocus.com
 * 2015
 *
 */
package data;

import java.util.ArrayList;
import java.util.List;

public class KMeans 
{
	//Number of Clusters. This metric should be related to the number of points
	//private int NUM_CLUSTERS = 3; (data model num vehicles)
	
	//Number of Points
	private int NUM_POINTS = 15;
	
	//Min and Max X and Y
	private static final int MIN_COORDINATE = 0;
	private static final int MAX_COORDINATE = 10;

	private List<Cluster> clusters;
	private DataModel fDataModel; // locations are points

	public KMeans(DataModel aDataModel) 
	{
		this.clusters = new ArrayList();
		
		fDataModel = aDataModel;
		init();
	}

	//Initializes the process
	public void init() 
	{
		//Create Points
		//points = Point.createRandomPoints(MIN_COORDINATE,MAX_COORDINATE,NUM_POINTS);

		//Create Clusters
		//Set Random Centroids
		for (int i = 0; i < fDataModel.numVehicles(); i++) 
		{
			Cluster cluster = new Cluster(i);
			//Point centroid = Point.createRandomPoint(MIN_COORDINATE,MAX_COORDINATE);
			Location lCentroid = fDataModel.getLocation(0); // use depot as centroid
			cluster.setCentroid(lCentroid);
			clusters.add(cluster);
		}

		//Print Initial state
		plotClusters();
	}

	private void plotClusters() 
	{
		for (int i = 0; i < fDataModel.numVehicles(); i++) 
		{
			Cluster c = (Cluster) clusters.get(i);
			c.plotCluster();
		}
	}

	//The process to calculate the K Means, with iterating method.
	public void calculate() 
	{
		boolean finish = false;
		int iteration = 0;

		// Add in new data, one at a time, recalculating centroids with each new one. 
		while(!finish) 
		{
			//Clear cluster state
			clearClusters();

			List lastCentroids = getCentroids();

			//Assign points to the closer cluster
			assignCluster();

			//Calculate new centroids.
			calculateCentroids();

			iteration++;

			List currentCentroids = getCentroids();

			//Calculates total distance between new and old Centroids
			double distance = 0;
			for(int i = 0; i < lastCentroids.size(); i++) 
			{
				distance += fDataModel.getDistanceMatrix()[(int) lastCentroids.get(i)][(int) currentCentroids.get(i)];
			}
			
			System.out.println("#################");
			System.out.println("Iteration: " + iteration);
			System.out.println("Centroid distances: " + distance);
			plotClusters();

			if(distance == 0) {
				finish = true;
			}
		}
	}

	private void clearClusters() 
	{
		for(Cluster cluster : clusters) 
		{
			cluster.clear();
		}
	}

	private List getCentroids() 
	{
		List centroids = new ArrayList(fDataModel.numVehicles());
		
		for(Cluster cluster : clusters) 
		{
			Location aux = cluster.getCentroid();
			Location point = new Location(aux.x, aux.y);
			centroids.add(point);
		}
		
		return centroids;
	}

	private void assignCluster() 
	{
		double max = Double.MAX_VALUE;
		double min = max; 
		int cluster = 0;                 
		double distance = 0.0; 

		/*for(Point point : points) 
		{
			min = max;
			for(int i = 0; i < NUM_CLUSTERS; i++) 
			{
				Cluster c = clusters.get(i);
				distance = Point.distance(point, c.getCentroid());
				if(distance &lt; min){
					min = distance;
					cluster = i;
				}
			}
			point.setCluster(cluster);
			clusters.get(cluster).addPoint(point);
		}*/
		for (int i = 0; i < fDataModel.numLocations(); i++)
		{
			min = max;
			for (int j = 0; j < fDataModel.numVehicles(); j++)
			{
				Cluster c = clusters.get(j);
				//distance = Point.distance(point, c.getCentroid());
				//fDataModel.getDistanceMatrix()[(int) lastCentroids.get(i)][(int) currentCentroids.get(i)]
			}
		}
	}

	private void calculateCentroids() 
	{
		for(Cluster cluster : clusters) 
		{
			double sumX = 0;
			double sumY = 0;
			List list = cluster.getPoints();
			int n_points = list.size();

			/*for(Point point : list) 
			{
				sumX += point.getX();
				sumY += point.getY();
			}

			Point centroid = cluster.getCentroid();
			if(n_points > 0) 
			{
				double newX = sumX / n_points;
				double newY = sumY / n_points;
				centroid.setX(newX);
				centroid.setY(newY);
			}*/
		}
	}
}