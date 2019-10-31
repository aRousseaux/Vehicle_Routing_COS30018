package agents;

import data.DataModel;
import data.Location;
import data.PheremoneModel;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Random;

public class Ant_VRP extends Ant {
    public Ant_VRP(DataModel aGraph, int input_vehicle_id) {
        super(aGraph, input_vehicle_id);
    }

    public Ant_VRP(DataModel aGraph, List<Integer> input_locations, int input_vehicle_id)
    {
        super(aGraph, input_locations, input_vehicle_id);
    }

    public ArrayList<Integer> nextLocation(PheremoneModel model, ArrayList<Integer> avalible_locations)
    {
        Random random = new Random();
        int max = Math.round((int) getRandomHigh(model, avalible_locations));

        double random_crossover_value;

        if (max > 0) {
            random_crossover_value = random.nextInt(max);
        } else {
            random_crossover_value = 0;
        }

        int current_location_id = fCurrentLocation.getfLocationID();
        double values = 0;

        //minus one, due to account for home location
        if ((intial_locations_size - avalible_locations.size()) <  fDataModel.getCapacities()[linked_vehicle_id] - 1)
        {
            return avalible_locations;
        }

        if (avalible_locations.size() > 0) {
            for (int i = 0; i < avalible_locations.size(); i++) {
                if (model.getDistanceMatrix()[current_location_id][avalible_locations.get(i)] > 0)
                {
                    values += model.getPheremone(current_location_id, avalible_locations.get(i)) * 10000 / model.getDistanceMatrix()[current_location_id][avalible_locations.get(i)];
                }

                if (values >= random_crossover_value)
                {
                    total_distance_travelled += fDataModel.getDistanceMatrix()[current_location_id][avalible_locations.get(i)];
                    location_mapping[current_location_id] = avalible_locations.get(i);

                    fCurrentLocation = fDataModel.getLocation(avalible_locations.get(i));

                    final int selected_index = i;

                    avalible_locations.removeIf(n -> (n == avalible_locations.get(selected_index).intValue()));

                    //probably can be condensed to this!
                    //avalible_locations.remove(i);

                    //this might be the problem!!!!!
                    if (avalible_locations.size() <= 1) {
                        for (int j = 0; j < location_mapping.length; j++) {
                            if (location_mapping[j] == 0) {
                                //total_distance_travelled += fDataModel.getDistanceMatrix()[j][0];
                            }
                        }
                    }

                    return (ArrayList<Integer>) avalible_locations.clone();
                }
            }
        }
        else
        {
            return avalible_locations;
        }

        return avalible_locations;
    }

    public double getRandomHigh(PheremoneModel model, ArrayList<Integer> avalible_locations) {
        double max_value = 0;
        int current_location_id = fCurrentLocation.getfLocationID();

        for (int i = 0; i < avalible_locations.size(); i++) {
            if (model.getDistanceMatrix()[current_location_id][avalible_locations.get(i)] > 0) {
                max_value += model.getPheremone(current_location_id, avalible_locations.get(i)) * 10000 / model.getDistanceMatrix()[current_location_id][avalible_locations.get(i)];
            }
        }

        return max_value;
    }

    public int getClosestLocation(Location current_location, ArrayList<Integer> avaliable_locations)
    {
        int closest_distance = 0;
        int closest_index = 0;
        for (int i = 0; i < avaliable_locations.size(); i++)
        {
            if (closest_distance == 0)
            {
                closest_distance = fDataModel.getDistanceMatrix()[current_location.getfLocationID()][avaliable_locations.get(i)];
            }

            if (closest_distance > fDataModel.getDistanceMatrix()[current_location.getfLocationID()][avaliable_locations.get(i)])
            {
                closest_distance = fDataModel.getDistanceMatrix()[current_location.getfLocationID()][avaliable_locations.get(i)];
                closest_index = i;
            }
        }

        return closest_index;
    }
}