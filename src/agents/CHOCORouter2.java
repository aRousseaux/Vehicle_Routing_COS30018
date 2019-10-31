
package agents;

import data.DataModel;
import org.chocosolver.solver.Model;
import org.chocosolver.solver.Solution;
import org.chocosolver.solver.Solver;
import org.chocosolver.solver.variables.IntVar;

public class CHOCORouter2 extends CHOCORouter
{
	private static final long serialVersionUID = 1L;

	//solves the best possible route for input drivers and locations
	public int[][] solveRoute(DataModel aDataModel, int aMaxRouteDistance )
	{
		// lModel
		Model lModel = new Model("Vehicle Routing Problem");
		IntVar[][] vehicle_routes = lModel.intVarMatrix(aDataModel.numVehicles(), aDataModel.numLocations(), 0, (aDataModel.numLocations() - 1));
		IntVar[][] route_lengths = lModel.intVarMatrix(aDataModel.numVehicles(), aDataModel.numLocations(), 0, 9999);
		IntVar total_distance = lModel.intVar("Distance", 0, 9999);
		IntVar[] routes_as_array = MatrixToArray(vehicle_routes);

		for (int i = 0; i < vehicle_routes.length; i++)
		{
			for (int j = 0; j < vehicle_routes[i].length; j++)
			{
				for (int z = 0; z < vehicle_routes[i].length; z++)
				{
					if (j > 0 && z > 0)
					{
						lModel.ifThen(lModel.element(lModel.intVar(z), vehicle_routes[i], lModel.intVar(j), 0), lModel.count(j,vehicle_routes[i], lModel.intVar(1)));
						lModel.ifThen(lModel.element(lModel.intVar(z), vehicle_routes[i], lModel.intVar(j), 0), lModel.arithm(route_lengths[i][j], ">", 0));
					}
				}

				lModel.arithm(routes_as_array[i * vehicle_routes[i].length + j],"=", vehicle_routes[i][j]).post();
				lModel.arithm(vehicle_routes[i][j], "!=", j).post();
			}

			lModel.allDifferentExcept0(vehicle_routes[i]).post();
		}

		lModel.allDifferentExcept0(routes_as_array).post();

		for (int i = 0; i < vehicle_routes.length; i++)
		{
			for (int j = 0; j < aDataModel.numLocations(); j++)
			{
				if (j > 0)
				{
					lModel.ifThen(lModel.and(lModel.arithm(vehicle_routes[i][j],"!=", 0), lModel.atLeastNValues(vehicle_routes[i], lModel.intVar(1), true)),
							lModel.or(lModel.element(vehicle_routes[i][j], vehicle_routes[i], lModel.intVar(j), 0), lModel.element(vehicle_routes[i][j - 1], vehicle_routes[i], lModel.intVar(j), 0)));

					lModel.ifThen(lModel.count(j, vehicle_routes[i], lModel.intVar(0)), lModel.arithm(vehicle_routes[i][j], "=", 0));
				}

				if (aDataModel.numVehicles() == 1)
				{
					lModel.circuit(vehicle_routes[i]).post();
				}

				for (int k = 1; k < aDataModel.numLocations(); k++)
				{
					int y = j - k;
					if (y < 0)
					{
						y += aDataModel.numLocations();
					}
					if (y >= aDataModel.numLocations())
					{
						y -= aDataModel.numLocations();
					}

					lModel.ifThen(lModel.arithm(vehicle_routes[i][j], "=", k),  lModel.arithm(vehicle_routes[i][k], "!=", j));

					lModel.ifThen(lModel.element(lModel.intVar(j), vehicle_routes[i], lModel.intVar(k), 0),
							lModel.arithm(route_lengths[i][k], "=", getBinPack(vehicle_routes[i], aDataModel.getDistanceMatrix()[k], lModel, y)));

					lModel.ifThen(lModel.and(lModel.arithm(vehicle_routes[i][k], "!=", 0), lModel.element(lModel.intVar(0), vehicle_routes[i], vehicle_routes[i][k], 0)),
							lModel.arithm(route_lengths[i][0], "=", getBinPack(vehicle_routes[i], aDataModel.getDistanceMatrix()[0], lModel, 0)));
				}
			}
		}

		lModel.sum(MatrixToArray(route_lengths), "=", total_distance).post();

		if (aDataModel.numLocations() < aDataModel.getTotalCapacity())
		{
			lModel.atLeastNValues(routes_as_array, lModel.intVar(aDataModel.numLocations()), true).post();
		}
		else
		{
			lModel.atLeastNValues(routes_as_array, lModel.intVar(aDataModel.getTotalCapacity() - 1), true).post();
			lModel.atMostNValues(routes_as_array, lModel.intVar(aDataModel.getTotalCapacity() + 1), true).post();

			for (int i = 0; i < aDataModel.numVehicles(); i++)
			{
			    lModel.atMostNValues(vehicle_routes[i], lModel.intVar(aDataModel.getCapacities()[i] + 1), true).post();
            }
		}

		lModel.setObjective(lModel.MINIMIZE, total_distance);
		lModel.arithm(total_distance, ">", aDataModel.numVehicles()).post();


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
}
