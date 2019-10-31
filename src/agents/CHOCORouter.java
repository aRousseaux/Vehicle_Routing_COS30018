
package agents;

import data.DataModel;
import jade.domain.FIPAAgentManagement.AMSAgentDescription;

import java.util.ArrayList;

import org.chocosolver.solver.Model;
import org.chocosolver.solver.Solution;
import org.chocosolver.solver.Solver;
import org.chocosolver.solver.variables.IntVar;

public class CHOCORouter extends GenericRouter
{
	private static final long serialVersionUID = 1L;

	public CHOCORouter()
	{
		registerO2AInterface(Router.class, this);

		fSelectedAgents = new ArrayList<AMSAgentDescription>();
	}

	protected void setup()
	{
		fDataModel = (DataModel) getArguments()[0];
	}

	//solves the best routes for drivers, within the constraint of already assigned packages
	public int[][] solveRoute( DataModel aDataModel, int aMaxRouteDistance ) 
	{
		// Model
		Model lModel = new Model("Vehicle Routing Problem");

		IntVar[][] vehicle_locations = lModel.intVarMatrix(aDataModel.numVehicles(), aDataModel.numLocations(), 0, (aDataModel.numLocations() - 1));
		IntVar[][] vehicle_routes = lModel.intVarMatrix(aDataModel.numVehicles(), aDataModel.numLocations(), 0, (aDataModel.numLocations() - 1));
		IntVar[][] route_lengths = lModel.intVarMatrix(aDataModel.numVehicles(), aDataModel.numLocations(), 0, 9999);
		IntVar total_length = lModel.intVar(0, 9999);

		IntVar[][] lVehiclePackages = lModel.intVarMatrix(aDataModel.numLocations(), aDataModel.numVehicles(), 0, 1);

		if (aDataModel.getTotalCapacity() >= aDataModel.numLocations())
		{
			for (int i = 0; i < lVehiclePackages.length; i++)
			{
				lModel.sum(lVehiclePackages[i], "=", 1).post();
			}
		}
		else
		{
			lModel.sum(MatrixToArray(lVehiclePackages), "=", aDataModel.getTotalCapacity()).post();
		}

		for (int i = 0; i < aDataModel.numVehicles(); i++)
		{
			lModel.sum((IntVar[]) getColumn(lVehiclePackages, i), "<=", aDataModel.getVehicle(i).getCapacity()).post();
		}

		for (int i = 0; i < vehicle_locations.length; i++)
		{
			lModel.ifThen(lModel.sum(vehicle_routes[i], ">", 0), lModel.arithm(vehicle_routes[i][0], ">", 0));

			for (int j = 0; j < vehicle_locations[i].length; j++)
			{
				lModel.ifThenElse(lModel.arithm(lVehiclePackages[j][i], ">", 0),
						lModel.arithm(vehicle_locations[i][j], "=", j),
						lModel.arithm(vehicle_locations[i][j], "=", 0));

				lModel.ifThen(lModel.arithm(vehicle_locations[i][j], ">", 0), lModel.count(vehicle_locations[i][j], vehicle_routes[i], lModel.intVar(1)));
				lModel.arithm(vehicle_routes[i][j], "!=", j).post();

				if (j > 0)
				{
					lModel.ifThen(lModel.and(lModel.arithm(vehicle_routes[i][j],"!=", 0), lModel.atLeastNValues(vehicle_locations[i], lModel.intVar(1), true)),
							lModel.or(lModel.element(vehicle_routes[i][j], vehicle_routes[i], lModel.intVar(j), 0), lModel.element(vehicle_routes[i][j - 1], vehicle_routes[i], lModel.intVar(j), 0)));

					lModel.ifThen(lModel.count(j, vehicle_routes[i], lModel.intVar(0)), lModel.arithm(vehicle_routes[i][j], "=", 0));

					if (aDataModel.numVehicles() == 1)
					{
						lModel.circuit(vehicle_routes[i]).post();
					}
				}

				for (int k = 1; k < aDataModel.numLocations(); k++)
				{
					lModel.ifThen(lModel.element(lModel.intVar(j), vehicle_routes[i], lModel.intVar(k), 0),
							lModel.arithm(route_lengths[i][k], "=", getBinPack(vehicle_routes[i], aDataModel.getDistanceMatrix()[k], lModel, k)));

					lModel.not(lModel.and(lModel.element(vehicle_routes[i][j], vehicle_locations[i], lModel.intVar(k), 0),
							lModel.arithm(vehicle_routes[i][k], "=", j))).post();

					lModel.ifThen(lModel.and(lModel.arithm(vehicle_routes[i][k], "!=", 0), lModel.element(lModel.intVar(0), vehicle_routes[i], vehicle_routes[i][k], 0)),
							lModel.arithm(route_lengths[i][0], "=", getBinPack(vehicle_routes[i], aDataModel.getDistanceMatrix()[0], lModel, 0)));
				}
			}

			lModel.sum(MatrixToArray(route_lengths), "=", total_length).post();
			lModel.setObjective(Model.MINIMIZE, total_length);

			lModel.allDifferentExcept0(vehicle_routes[i]).post();
			lModel.allDifferentExcept0(vehicle_locations[i]).post();
		}

		lModel.allDifferentExcept0(MatrixToArray(vehicle_routes)).post();


		Solver lSolver = lModel.getSolver();
		if (lSolver.solve())
		{
			Solution s = lSolver.findSolution();
		}
		else
		{
			System.out.print("No Solution Found!");
			// no solution
		}

		int[][] return_routes = new int[aDataModel.numVehicles()][];
		System.out.println("Num vehicle: " + aDataModel.numVehicles());

		for (int i = 0; i < aDataModel.numVehicles(); i++)
		{

			String route_string = "";

			route_string += "0, ";
			route_string += vehicle_routes[i][0].getValue() + ", ";
			int previous = vehicle_routes[i][0].getValue();
			for (int j = 0; j <= vehicle_routes[i].length; j++)
			{
				if (vehicle_routes[i][previous].getValue() > 0)
				{
					route_string += vehicle_routes[i][previous].getValue() + ", ";
				}

				previous = vehicle_routes[i][previous].getValue();

				if (previous == 0)
				{
					break;
				}
			}

			route_string += "0,";
			return_routes[i] = new int[route_string.split(",").length];
			for (int k = 0; k < return_routes[i].length; k++)
			{
				return_routes[i][k] = Integer.valueOf(route_string.split(",")[k].trim());
			}

			System.out.println("Route OUT: " + route_string);
		}

		return return_routes;
	}

	//creates an array of intvars that has the package value, for a specific package, for all the solution vehicles
	public static IntVar[] getColumn(IntVar[][] aMatrix, int index)
	{
		IntVar[] return_array = new IntVar[aMatrix.length];

		for (int i = 0; i <aMatrix.length; i++)
		{
			return_array[i] = aMatrix[i][index];
		}

		return return_array;
	}

	//converts the input matrix into an array, but just creating a big array, with the individual matrix within the array, one after the other
	public static IntVar[] MatrixToArray(IntVar[][] matrix)
	{
		IntVar[] return_array = new IntVar[(matrix.length * matrix[0].length)];

		for (int i = 0; i < matrix.length; i++)
		{
			for (int j = 0; j < matrix[i].length; j++)
			{
				return_array[i * (matrix[i].length) + j] = matrix[i][j];
			}
		}
		return return_array;
	}


	public static IntVar getBinPack(IntVar[] input_var, int[] matrix_location, Model model, int i)
	{
		IntVar[] return_value = model.intVarArray(matrix_location.length, 0, 9999);

		model.binPacking(input_var, matrix_location, return_value, 0).post();

		return return_value[i];
	}
}