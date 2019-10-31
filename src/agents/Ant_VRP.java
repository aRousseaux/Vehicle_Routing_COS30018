
package agents;

import data.DataModel;
import data.Location;
import data.PheremoneModel;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Random;

public class Ant_VRP extends Ant
{
	public Ant_VRP(DataModel aGraph, int input_vehicle_id)
	{
		super(aGraph, input_vehicle_id);
	}

	public Ant_VRP(DataModel aGraph, List<Integer> aLocations, int input_vehicle_id)
	{
		super(aGraph, aLocations, input_vehicle_id);
	}

	public ArrayList<Integer> nextLocation(PheremoneModel aModel, ArrayList<Integer> aAvalibleLocations)
	{
		Random lRand = new Random();
		int lMax = Math.round((int) getRandomHigh(aModel, aAvalibleLocations));

		double lRandomCrossoverValue;

		if (lMax > 0)
		{
			lRandomCrossoverValue = lRand.nextInt(lMax);
		}
		else
		{
			lRandomCrossoverValue = 0;
		}

		int lCurrentLocationID = fCurrentLocation.getfLocationID();
		double lValues = 0;

		if (aAvalibleLocations.size() > 0)
		{
			for (int i = 0; i < aAvalibleLocations.size(); i++)
			{
				if (aModel.getDistanceMatrix()[lCurrentLocationID][aAvalibleLocations.get(i)] > 0)
				{
					lValues += aModel.getPheremone(lCurrentLocationID, aAvalibleLocations.get(i)) * 10000 / aModel.getDistanceMatrix()[lCurrentLocationID][aAvalibleLocations.get(i)];
				}

				if (lValues >= lRandomCrossoverValue)
				{
					fTotalDistance += fDataModel.getDistanceMatrix()[lCurrentLocationID][aAvalibleLocations.get(i)];
					fLocationMapping[lCurrentLocationID] = aAvalibleLocations.get(i);

					fCurrentLocation = fDataModel.getLocation(aAvalibleLocations.get(i));

					final int lSelectedIndex = i;

					aAvalibleLocations.removeIf(n -> (n == aAvalibleLocations.get(lSelectedIndex).intValue()));

					if (fDataModel.numLocations() - fInitialLocationSize ==  aAvalibleLocations.size())
					{
						//not valid!
						//aAvalibleLocations.removeAll(aAvalibleLocations);
						return aAvalibleLocations;
					}

					//this might be the problem!!!!!
					if (aAvalibleLocations.size() <= 1)
					{
						for (int j = 0; j < fLocationMapping.length; j++)
						{
							if (fLocationMapping[j] == 0)
							{
								//total_distance_travelled += fDataModel.getDistanceMatrix()[j][0];
							}
						}
					}

					return aAvalibleLocations;
				}
			}
		}
		else
		{
			return aAvalibleLocations;
		}

		return aAvalibleLocations;
	}

	public double getRandomHigh(PheremoneModel aModel, ArrayList<Integer> aAvalibleLocations)
	{
		double lMaxValue = 0;
		int lCurrentLocationID = fCurrentLocation.getfLocationID();

		for (int i = 0; i < aAvalibleLocations.size(); i++)
		{
			if (aModel.getDistanceMatrix()[lCurrentLocationID][aAvalibleLocations.get(i)] > 0)
			{
				lMaxValue += aModel.getPheremone(lCurrentLocationID, aAvalibleLocations.get(i)) * 10000 / aModel.getDistanceMatrix()[lCurrentLocationID][aAvalibleLocations.get(i)];
			}
		}

		return lMaxValue;
	}

	public int getClosestLocation(Location aCurrentLocation, ArrayList<Integer> aAvaliableLocations)
	{
		int lClosestDistance = 0;
		int lClosestIndex = 0;

		for (int i = 0; i < aAvaliableLocations.size(); i++)
		{
			if (lClosestDistance == 0)
			{
				lClosestDistance = fDataModel.getDistanceMatrix()[aCurrentLocation.getfLocationID()][aAvaliableLocations.get(i)];
			}

			if (lClosestDistance > fDataModel.getDistanceMatrix()[aCurrentLocation.getfLocationID()][aAvaliableLocations.get(i)])
			{
				lClosestDistance = fDataModel.getDistanceMatrix()[aCurrentLocation.getfLocationID()][aAvaliableLocations.get(i)];
				lClosestIndex = i;
			}
		}

		return lClosestIndex;
	}
}
