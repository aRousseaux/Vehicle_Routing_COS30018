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
    protected List<Integer> unvisited_locations;

    protected Location fCurrentLocation;
    protected DataModel fDataModel;
    protected int intial_locations_size;
    protected int linked_vehicle_id;

    //keeps track of how far the ant has travelled for it's current journey
    protected int total_distance_travelled;
    protected int[] location_mapping;

    //Ant objects are used within ACO solvers, finding the 'best' path based on the level of pheromones
    //leading to different locations
    public Ant(DataModel aGraph, int input_vehicle_id)
    {
        linked_vehicle_id = input_vehicle_id;
        total_distance_travelled = 0;
        unvisited_locations = new ArrayList<Integer>();
        for (int i = 0; i < aGraph.numLocations(); i++)
        {
            unvisited_locations.add(i);
        }

        intial_locations_size = unvisited_locations.size();

        location_mapping = new int[aGraph.numLocations()];

        fDataModel = aGraph;
        fCurrentLocation = fDataModel.getLocation(0);
    }

    public Ant(DataModel aGraph, List<Integer> input_locations, int input_vehicle_id)
    {
        linked_vehicle_id = input_vehicle_id;
        total_distance_travelled = 0;
        unvisited_locations = input_locations;
        intial_locations_size = unvisited_locations.size();

        location_mapping = new int[aGraph.numLocations()];

        fDataModel = aGraph;
        fCurrentLocation = fDataModel.getLocation(0);
    }

    //finds the next locations for the ant, based on the input pheremone model and the remaining locatiosn in unvisited_locations
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
                    total_distance_travelled += fDataModel.getDistanceMatrix()[current_location_id][unvisited_locations.get(i)];
                    location_mapping[current_location_id] = unvisited_locations.get(i).intValue();
                    fCurrentLocation = fDataModel.getLocation(unvisited_locations.get(i));

                    final int selected_index = i;
                    unvisited_locations.removeIf(n -> (n == unvisited_locations.get(selected_index)));

                    if (intial_locations_size - unvisited_locations.size() <  fDataModel.getCapacities()[linked_vehicle_id % (fDataModel.numVehicles() - 1)])
                    {

                        unvisited_locations.removeAll(unvisited_locations);
                    }

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

    //standard update model
    public PheremoneModel updateModel(PheremoneModel input_model)
    {
        return updateModel(input_model, 1);
    }

    //pheremonemodel, is updated, based on this ant's path and it's distance
    public PheremoneModel updateModel(PheremoneModel input_model, int multiplier)
    {
        if (total_distance_travelled > 0)
        {
            for (int i = 0; i < location_mapping.length; i++)
            {
                //multiplier is utilized if a ACO elitist algorithm is used
                float new_value = input_model.getPheremone(i, location_mapping[i]) +  (multiplier * 10000)/total_distance_travelled;
                input_model.updatePheremonePath(i, location_mapping[i], new_value);
                new_value = input_model.getPheremone(location_mapping[i], i) +   (multiplier * 10000)/total_distance_travelled;
                input_model.updatePheremonePath(location_mapping[i], i, new_value);
            }
        }

        return input_model;
    }

    //reset all of the fields for ant object, so it can be re-used
    public void reset()
    {
        total_distance_travelled = 0;

        //unvisited_locations arraylist is refilled, with all location indexes 0 .. n
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


    //gets the current path of the ant, as a int array
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

        //+= 2, is required, due to ant paths having to start and conclude at location 0
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

    //returns the ant path in string form
    //helpful for de-bugging
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

    //resets the ant, based on a set of input location
    //in normal reset method, unvisited locations is filled with locations 0 ... n
    //however this can't happen in the ACOParitionRouter, hence it's inclusion
    public void reset(ArrayList<Integer> input_locations)
    {
        total_distance_travelled = 0;
        //unvisited_locations is assigned input_locations
        unvisited_locations = input_locations;

        location_mapping = new int[fDataModel.numLocations()];

        fCurrentLocation = fDataModel.getLocation(0);
    }
}

