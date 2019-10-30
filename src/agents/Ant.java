package agents;

import data.DataModel;
import data.Location;
import data.PheremoneModel;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

import data.*;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

public class Ant
{
    private List<Integer> unvisited_locations;

    protected Location fCurrentLocation;
    protected DataModel fDataModel;

    //keeps track of how far the ant has travelled for it's current journey
    protected int total_distance_travelled;
    protected int[] location_mapping;

    public Ant(DataModel aGraph)
    {
        total_distance_travelled = 0;
        unvisited_locations = new ArrayList<Integer>();
        for (int i = 0; i < aGraph.numLocations(); i++)
        {
            unvisited_locations.add(i);
        }

        location_mapping = new int[aGraph.numLocations()];

        fDataModel = aGraph;
        fCurrentLocation = fDataModel.getLocation(0);
    }

    public boolean nextLocation(PheremoneModel model)
    {
        Random random = new Random();
        int max = Math.round((int) getRandomHigh(model));
        double random_crossover_value;

        if (max > 0)
        {
            random_crossover_value = random.nextInt(max);
        }
        else
        {
            random_crossover_value = 0;
        }

        int current_location_id = fCurrentLocation.getfLocationID();
        double values = 0;

        if (unvisited_locations.size() > 0)
        {
            for (int i = 0; i < unvisited_locations.size(); i++)
            {
                if (model.getDistanceMatrix()[current_location_id][unvisited_locations.get(i)] > 0)
                {
                    values += model.getPheremone(current_location_id, unvisited_locations.get(i)) * 10000/model.getDistanceMatrix()[current_location_id][unvisited_locations.get(i)];
                }

                if (values >= random_crossover_value)
                {
                    if (i == 0 && unvisited_locations.size() != fDataModel.numLocations())
                    {
                        continue;
                    }

                    total_distance_travelled += fDataModel.getDistanceMatrix()[current_location_id][unvisited_locations.get(i)];
                    location_mapping[current_location_id] = unvisited_locations.get(i).intValue();
                    fCurrentLocation = fDataModel.getLocation(unvisited_locations.get(i));

                    final int selected_index = i;
                    unvisited_locations.removeIf(n -> (n == unvisited_locations.get(selected_index)));

                    if (unvisited_locations.size() <= 1)
                    {
                        for (int j = 0; j < location_mapping.length; j++)
                        {
                            if (location_mapping[j] == 0)
                            {
                                total_distance_travelled += fDataModel.getDistanceMatrix()[j][0];
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

    //gets the sum of all the pheromone values * distances, for later use
    public double getRandomHigh(PheremoneModel model)
    {
        double max_value = 0;
        int current_location_id = fCurrentLocation.getfLocationID();

        for (int i = 0; i < unvisited_locations.size(); i++)
        {
            if (model.getDistanceMatrix()[current_location_id][unvisited_locations.get(i)] > 0)
            {
                max_value += model.getPheremone(current_location_id, unvisited_locations.get(i)) * 10000/model.getDistanceMatrix()[current_location_id][unvisited_locations.get(i)];
            }
        }

        return max_value;
    }

    public PheremoneModel updateModel(PheremoneModel input_model)
    {
        return updateModel(input_model, 1);
    }

    public PheremoneModel updateModel(PheremoneModel input_model, int multiplier)
    {
        if (total_distance_travelled > 0)
        {
            for (int i = 0; i < location_mapping.length; i++)
            {
                float new_value = input_model.getPheremone(i, location_mapping[i]) + 10000/total_distance_travelled;
                input_model.updatePheremonePath(i, location_mapping[i], new_value);
                new_value = input_model.getPheremone(location_mapping[i], i) +  10000/total_distance_travelled;
                input_model.updatePheremonePath(location_mapping[i], i, new_value);
            }
        }

        return input_model;
    }

    //reset all of the fields for ant object, so it can be re-used
    public void reset()
    {
        total_distance_travelled = 0;
        unvisited_locations = new ArrayList<Integer>();

        for (int i = 0; i < fDataModel.numLocations(); i++)
        {
            unvisited_locations.add(i);
        }

        location_mapping = new int[fDataModel.numLocations()];

        fCurrentLocation = fDataModel.getLocation(0);
    }

    public List<Integer> getUnvisited_locations() {
        return unvisited_locations;
    }

    public void setUnvisited_locations(List<Integer> unvisited_locations) {
        this.unvisited_locations = unvisited_locations;
    }

    public Location getfCurrentLocation() {
        return fCurrentLocation;
    }

    public void setfCurrentLocation(Location fCurrentLocation) {
        this.fCurrentLocation = fCurrentLocation;
    }

    public DataModel getfDataModel() {
        return fDataModel;
    }

    public void setfDataModel(DataModel fDataModel) {
        this.fDataModel = fDataModel;
    }

    public int getTotal_distance_travelled() {
        return total_distance_travelled;
    }

    public void setTotal_distance_travelled(int total_distance_travelled) {
        this.total_distance_travelled = total_distance_travelled;
    }

    public int[] getLocation_mapping() {
        return location_mapping;
    }

    public void setLocation_mapping(int[] location_mapping) {
        this.location_mapping = location_mapping;
    }

    //returns a string with information regarding vehicle path
    public String getPath_alt()
    {
        String return_string = "";
        for (int i = 0; i < fDataModel.numLocations(); i++)
        {
            return_string += location_mapping[i] + " (" + fDataModel.getDistanceMatrix()[i][location_mapping[i]] + ")" + ", ";
        }

        return return_string;
    }

    public int[] getPathArray()
    {
        int[] return_array;
        int set_locations = 0;
        for (int i = 0; i < location_mapping.length; i++)
        {
            if (location_mapping[i] != 0)
            {
                set_locations++;
            }
        }

        set_locations += 2;

        return_array = new int[set_locations];
        return_array[0] = 0;
        int selected_index = 0;

        for (int i = 1; i < set_locations; i++)
        {
            if (location_mapping[selected_index] > 0)
            {
                return_array[i] = location_mapping[selected_index];
                selected_index = location_mapping[selected_index];
            }
        }

        return return_array;
    }

    public String getPath()
    {
        String return_string = "0, " + location_mapping[0] + ", ";
        int selected_index = 0;
        for (int i = 0; i < fDataModel.numLocations() - 1; i++)
        {
            return_string += location_mapping[selected_index] + " (" + fDataModel.getDistanceMatrix()[i][selected_index] + ")" + ", ";
            selected_index = location_mapping[selected_index];
        }

        return return_string;
    }
}

