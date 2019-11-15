
package agents;

import data.DataModel;
import data.Location;
import data.PheremoneModel;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Random;

public class Ant
{
	protected List<Integer> fUnvisited;

	protected Location fCurrentLocation;
	protected DataModel fDataModel;
	protected int fInitialLocationSize;

	protected int fReferenceVehicleID;

	//keeps track of how far the ant has travelled for it's current journey
	protected int fTotalDistance;
	protected int[] fLocationMapping;

	//Ant objects are used within ACO solvers, finding the 'best' path based on the level of pheromones
	//leading to different locations
	public Ant(DataModel aGraph)
	{
		fTotalDistance = 0;
		fUnvisited = new ArrayList<Integer>();

		for (int i = 0; i < aGraph.numLocations(); i++)
		{
			fUnvisited.add(i);
		}

		fInitialLocationSize = fUnvisited.size();

		fLocationMapping = new int[aGraph.numLocations()];

		fDataModel = aGraph;
		fCurrentLocation = fDataModel.getLocation(0);
	}

	public Ant(DataModel aGraph, int aReferenceVehicleID)
	{
		fTotalDistance = 0;
		fUnvisited = new ArrayList<Integer>();
		fReferenceVehicleID = aReferenceVehicleID;

		for (int i = 0; i < aGraph.numLocations(); i++)
		{
			fUnvisited.add(i);
		}

		fInitialLocationSize = fUnvisited.size();

		fLocationMapping = new int[aGraph.numLocations()];

		fDataModel = aGraph;
		fCurrentLocation = fDataModel.getLocation(0);
	}

	public Ant(DataModel aGraph, List<Integer> aLocations)
	{
		fTotalDistance = 0;
		fUnvisited = aLocations;
		fInitialLocationSize = fUnvisited.size();

		fLocationMapping = new int[aGraph.numLocations()];

		fDataModel = aGraph;
		fCurrentLocation = fDataModel.getLocation(0);
	}

	public Ant(DataModel aGraph, List<Integer> aLocations, int aReferenceVehicleID)
	{
		fTotalDistance = 0;
		fUnvisited = aLocations;
		fInitialLocationSize = fUnvisited.size();
		fReferenceVehicleID = aReferenceVehicleID;

		fLocationMapping = new int[aGraph.numLocations()];

		fDataModel = aGraph;
		fCurrentLocation = fDataModel.getLocation(0);
	}

	//finds the next locations for the ant, based on the input pheremone model and the remaining locatiosn in unvisited_locations
	public boolean nextLocation(PheremoneModel aModel)
	{
		Random lRandom = new Random();
		int lMax = Math.round((int) getRandomHigh(aModel));
		double lCrossoverValue;

		if (lMax > 0)
		{
			lCrossoverValue = lRandom.nextInt(lMax);
		}
		else
		{
			lCrossoverValue = 0;
		}

		int lCurrentLocationID = fCurrentLocation.getfLocationID();
		double lValues = 0;

		if (fUnvisited.size() > 0)
		{
			for (int i = 0; i < fUnvisited.size(); i++)
			{
				if (aModel.getDistanceMatrix()[lCurrentLocationID][fUnvisited.get(i)] > 0)
				{
					lValues += aModel.getPheremone(lCurrentLocationID, fUnvisited.get(i)) * 10000/aModel.getDistanceMatrix()[lCurrentLocationID][fUnvisited.get(i)];
				}

				if (lValues >= lCrossoverValue)
				{
					fTotalDistance += fDataModel.getDistanceMatrix()[lCurrentLocationID][fUnvisited.get(i)];
					fLocationMapping[lCurrentLocationID] = fUnvisited.get(i).intValue();
					fCurrentLocation = fDataModel.getLocation(fUnvisited.get(i));

					final int lSelectedIndex = i;
					fUnvisited.removeIf(n -> (n == fUnvisited.get(lSelectedIndex)));

					if (fDataModel.numLocations() - fInitialLocationSize ==  fUnvisited.size())
					{
						fUnvisited.removeAll(fUnvisited);
					}

					if (fUnvisited.size() <= 1)
					{
						for (int j = 0; j < fLocationMapping.length; j++)
						{
							if (fLocationMapping[j] == 0)
							{
								fTotalDistance += fDataModel.getDistanceMatrix()[j][0];
							}
						}
					}

					return true;
				}
			}
		}
		else
		{
			return false;
		}

		return false;
	}

	//finds the next locations for the ant, based on the input pheremone model and the remaining locatiosn in unvisited_locations
	public boolean nextLocationFinal(PheremoneModel aModel)
	{
		Random lRandom = new Random();
		int lMax = Math.round((int) getRandomHigh(aModel));
		double lCrossoverValue;

		if (lMax > 0)
		{
			lCrossoverValue = lRandom.nextInt(lMax);
		}
		else
		{
			lCrossoverValue = 0;
		}

		int lCurrentLocationID = fCurrentLocation.getfLocationID();
		double lValues = 0;

		int selected_index = 0;

		if (fUnvisited.size() > 0)
		{
			for (int i = 0; i < fUnvisited.size(); i++)
			{
				if (fDataModel.getDistanceMatrix()[lCurrentLocationID][fUnvisited.get(i)] < fDataModel.getDistanceMatrix()[lCurrentLocationID][fUnvisited.get(selected_index)])
				{
					selected_index = i;
				}
			}

			fTotalDistance += fDataModel.getDistanceMatrix()[lCurrentLocationID][fUnvisited.get(selected_index)];
			fLocationMapping[lCurrentLocationID] = fUnvisited.get(selected_index).intValue();
			fCurrentLocation = fDataModel.getLocation(fUnvisited.get(selected_index));

			final int lSelectedIndex = selected_index;
			fUnvisited.removeIf(n -> (n == fUnvisited.get(lSelectedIndex)));

			if (fDataModel.numLocations() - fInitialLocationSize ==  fUnvisited.size())
			{
				fUnvisited.removeAll(fUnvisited);
			}

			return true;

		}
		else
		{
			return false;
		}
	}

	//gets the sum of all the pheromone values * distances, for later use
	public double getRandomHigh(PheremoneModel aModel)
	{
		double lMaxValue = 0;
		int lCurrentLocationID = fCurrentLocation.getfLocationID();

		for (int i = 0; i < fUnvisited.size(); i++)
		{
			if (aModel.getDistanceMatrix()[lCurrentLocationID][fUnvisited.get(i)] > 0)
			{
				lMaxValue += aModel.getPheremone(lCurrentLocationID, fUnvisited.get(i)) * 10000/aModel.getDistanceMatrix()[lCurrentLocationID][fUnvisited.get(i)];
			}
		}

		return lMaxValue;
	}

	//standard update model
	public PheremoneModel updateModel(PheremoneModel aModel)
	{
		return updateModel(aModel, 1);
	}

	//pheremonemodel, is updated, based on this ant's path and it's distance
	public PheremoneModel updateModel(PheremoneModel aModel, int aMultiplier)
	{
		if (fTotalDistance > 0)
		{
			for (int i = 0; i < fLocationMapping.length; i++)
			{
				//multiplier is utilized if a ACO elitist algorithm is used
				float lNewValue = aModel.getPheremone(i, fLocationMapping[i]) +  (aMultiplier * 10000)/fTotalDistance;

				aModel.updatePheremonePath(i, fLocationMapping[i], lNewValue);
				lNewValue = aModel.getPheremone(fLocationMapping[i], i) +   (aMultiplier * 10000)/fTotalDistance;

				aModel.updatePheremonePath(fLocationMapping[i], i, lNewValue);
			}
		}

		return aModel;
	}

	//reset all of the fields for ant object, so it can be re-used
	public void reset()
	{
		fTotalDistance = 0;

		//unvisited_locations arraylist is refilled, with all location indexes 0 .. n
		fUnvisited = new ArrayList<Integer>();

		for (int i = 0; i < fDataModel.numLocations(); i++)
		{
			fUnvisited.add(i);
		}

		fLocationMapping = new int[fDataModel.numLocations()];

		fCurrentLocation = fDataModel.getLocation(0);
	}

	public List<Integer> getUnvisitedLocations()
	{
		return fUnvisited;
	}

	public void setUnvisitedLocations(List<Integer> aUnvisitedLocations)
	{
		this.fUnvisited = aUnvisitedLocations;
	}

	public Location getCurrentLocation()
	{
		return fCurrentLocation;
	}

	public void setCurrentLocation(Location aCurrentLocation)
	{
		this.fCurrentLocation = aCurrentLocation;
	}

	public DataModel getDataModel()
	{
		return fDataModel;
	}

	public void setDataModel(DataModel aDataModel)
	{
		this.fDataModel = aDataModel;
	}

	public int getTotalDistanceTravelled()
	{
		return fTotalDistance;
	}

	public void setTotalDistanceTravelled(int aTotalDistanceTravelled)
	{
		this.fTotalDistance = aTotalDistanceTravelled;
	}

	public int[] getLocationMapping()
	{
		return fLocationMapping;
	}

	public void setLocationMapping(int[] aLocationMapping)
	{
		this.fLocationMapping = aLocationMapping;
	}


	//gets the current path of the ant, as a int array
	public int[] getPathArray()
	{
		int[] lReturnArray;
		int lSetLocations = 0;

		for (int i = 0; i < fLocationMapping.length; i++)
		{
			if (fLocationMapping[i] != 0)
			{
				lSetLocations++;
			}
		}

		//+= 2, is required, due to ant paths having to start and conclude at location 0
		lSetLocations += 2;

		lReturnArray = new int[lSetLocations];
		lReturnArray[0] = 0;
		int lSelectedIndex = 0;

		for (int i = 1; i < lSetLocations; i++)
		{
			if (fLocationMapping[lSelectedIndex] > 0)
			{
				lReturnArray[i] = fLocationMapping[lSelectedIndex];
				lSelectedIndex = fLocationMapping[lSelectedIndex];
			}
		}

		return lReturnArray;
	}

	public int getRouteLength()
	{
		int route_length = 0;
		int selected_location = 0;

		for (int i = 0; i < fLocationMapping.length; i++)
		{
			route_length += fDataModel.getDistanceMatrix()[selected_location][fLocationMapping[selected_location]];
			selected_location = fLocationMapping[selected_location];
			if (selected_location == 0)
			{
				return route_length;
			}
		}

		return route_length;
	}

	//returns the ant path in string form
	//helpful for de-bugging
	public String getPath()
	{
		String lReturnString = "0, " + fLocationMapping[0] + ", ";
		int lSelectedIndex = 0;
		for (int i = 0; i < fDataModel.numLocations() - 1; i++)
		{
			lReturnString += fLocationMapping[lSelectedIndex] + " (" + fDataModel.getDistanceMatrix()[i][lSelectedIndex] + ")" + ", ";
			lSelectedIndex = fLocationMapping[lSelectedIndex];
		}

		return lReturnString;
	}

	//resets the ant, based on a set of input location
	//in normal reset method, unvisited locations is filled with locations 0 ... n
	//however this can't happen in the ACOParitionRouter, hence it's inclusion
	public void reset(ArrayList<Integer> aLocations)
	{
		fTotalDistance = 0;
		//unvisited_locations is assigned input_locations
		fUnvisited = aLocations;

		fLocationMapping = new int[fDataModel.numLocations()];

		fCurrentLocation = fDataModel.getLocation(0);
	}
}
