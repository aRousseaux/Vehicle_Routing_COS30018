
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

		IntVar[][] lVehicleLocations = lModel.intVarMatrix(aDataModel.numVehicles(), aDataModel.numLocations(), 0, (aDataModel.numLocations() - 1));
		IntVar[][] lVehicleRoutes = lModel.intVarMatrix(aDataModel.numVehicles(), aDataModel.numLocations(), 0, (aDataModel.numLocations() - 1));
		IntVar[][] lRouteLengths = lModel.intVarMatrix(aDataModel.numVehicles(), aDataModel.numLocations(), 0, 9999);
		IntVar lTotalLength = lModel.intVar(0, 9999);

		IntVar[][] lVehiclePackages = lModel.intVarMatrix(aDataModel.numLocations(), aDataModel.numVehicles(), 0, 1);

		for (int i = 0; i < aDataModel.numVehicles(); i++)
		{
			lModel.sum((IntVar[]) getColumn(lVehiclePackages, i), "<=", aDataModel.getVehicle(i).getCapacity()).post();
		}

		if (aDataModel.getTotalCapacity() >= aDataModel.numLocations())
		{
			for (int i = 0; i < lVehiclePackages.length; i++)
			{
				lModel.sum(lVehiclePackages[i], "=", 1).post();
			}
		}
		else
		{
			lModel.sum(MatrixToArray(lVehiclePackages), "=", aDataModel.getTotalCapacity() - 2).post();
		}

		for (int i = 0; i < lVehicleLocations.length; i++)
		{
			lModel.ifThen(lModel.sum(lVehicleRoutes[i], ">", 0), lModel.arithm(lVehicleRoutes[i][0], ">", 0));

			for (int j = 0; j < lVehicleLocations[i].length; j++)
			{
				lModel.ifThenElse(lModel.arithm(lVehiclePackages[j][i], ">", 0),
						lModel.arithm(lVehicleLocations[i][j], "=", j),
						lModel.arithm(lVehicleLocations[i][j], "=", 0));

				lModel.ifThen(lModel.arithm(lVehicleLocations[i][j], ">", 0), lModel.count(lVehicleLocations[i][j], lVehicleRoutes[i], lModel.intVar(1)));
				lModel.arithm(lVehicleRoutes[i][j], "!=", j).post();

				if (j > 0)
				{
					lModel.ifThen(lModel.and(lModel.arithm(lVehicleRoutes[i][j],"!=", 0), lModel.atLeastNValues(lVehicleLocations[i], lModel.intVar(1), true)),
							lModel.or(lModel.element(lVehicleRoutes[i][j], lVehicleRoutes[i], lModel.intVar(j), 0), lModel.element(lVehicleRoutes[i][j - 1], lVehicleRoutes[i], lModel.intVar(j), 0)));

					lModel.ifThen(lModel.count(j, lVehicleRoutes[i], lModel.intVar(0)), lModel.arithm(lVehicleRoutes[i][j], "=", 0));

					if (aDataModel.numVehicles() == 1)
					{
						lModel.circuit(lVehicleRoutes[i]).post();
					}
				}

				for (int k = 1; k < aDataModel.numLocations(); k++)
				{
					/*
					lModel.ifThen(lModel.element(lModel.intVar(j), lVehicleRoutes[i], lModel.intVar(k), 0),
							lModel.arithm(lRouteLengths[i][k], "=", getBinPack(lVehicleRoutes[i], aDataModel.getDistanceMatrix()[k], lModel, k)));
					 */

					lModel.not(lModel.and(lModel.element(lVehicleRoutes[i][j], lVehicleLocations[i], lModel.intVar(k), 0),
							lModel.arithm(lVehicleRoutes[i][k], "=", j))).post();

					lModel.ifThen(lModel.and(lModel.arithm(lVehicleRoutes[i][k], "!=", 0), lModel.element(lModel.intVar(0), lVehicleRoutes[i], lVehicleRoutes[i][k], 0)),
							lModel.arithm(lRouteLengths[i][0], "=", getBinPack(lVehicleRoutes[i], aDataModel.getDistanceMatrix()[0], lModel, 0)));
				}
			}

			lModel.sum(MatrixToArray(lRouteLengths), "=", lTotalLength).post();
			lModel.setObjective(Model.MINIMIZE, lTotalLength);

			lModel.allDifferentExcept0(lVehicleRoutes[i]).post();
			lModel.allDifferentExcept0(lVehicleLocations[i]).post();
		}

		lModel.allDifferentExcept0(MatrixToArray(lVehicleRoutes)).post();


		Solver lSolver = lModel.getSolver();
		if (lSolver.solve())
		{
			Solution lSolution = lSolver.findSolution();
		}
		else
		{
			System.out.print("No Solution Found!");
			// no solution
		}

        lSolver.printShortStatistics();
		int[][] lReturnRoutes = new int[aDataModel.numVehicles()][];
		System.out.println("Num vehicle: " + aDataModel.numVehicles());

		for (int i = 0; i < aDataModel.numVehicles(); i++)
		{

			String lRouteString = "";

			lRouteString += "0, ";
			lRouteString += lVehicleRoutes[i][0].getValue() + ", ";
			int lPrevious = lVehicleRoutes[i][0].getValue();
			
			for (int j = 0; j <= lVehicleRoutes[i].length; j++)
			{
				if (lVehicleRoutes[i][lPrevious].getValue() > 0)
				{
					lRouteString += lVehicleRoutes[i][lPrevious].getValue() + ", ";
				}

				lPrevious = lVehicleRoutes[i][lPrevious].getValue();

				if (lPrevious == 0)
				{
					break;
				}
			}

			lRouteString += "0,";
			lReturnRoutes[i] = new int[lRouteString.split(",").length];
			for (int k = 0; k < lReturnRoutes[i].length; k++)
			{
				lReturnRoutes[i][k] = Integer.valueOf(lRouteString.split(",")[k].trim());
			}

			System.out.println("Route OUT: " + lRouteString);
		}

		return lReturnRoutes;
	}

	//creates an array of intvars that has the package value, for a specific package, for all the solution vehicles
	public static IntVar[] getColumn(IntVar[][] aMatrix, int aIndex)
	{
		IntVar[] lReturnArray = new IntVar[aMatrix.length];

		for (int i = 0; i <aMatrix.length; i++)
		{
			lReturnArray[i] = aMatrix[i][aIndex];
		}

		return lReturnArray;
	}

	//converts the input matrix into an array, but just creating a big array, with the individual matrix within the array, one after the other
	public static IntVar[] MatrixToArray(IntVar[][] aMatrix)
	{
		IntVar[] lReturnArray = new IntVar[(aMatrix.length * aMatrix[0].length)];

		for (int i = 0; i < aMatrix.length; i++)
		{
			for (int j = 0; j < aMatrix[i].length; j++)
			{
				lReturnArray[i * (aMatrix[i].length) + j] = aMatrix[i][j];
			}
		}
		return lReturnArray;
	}


	public static IntVar getBinPack(IntVar[] aVar, int[] aMatrixLocation, Model aModel, int aIndex)
	{
		IntVar[] lReturnValue = aModel.intVarArray(aMatrixLocation.length, 0, 9999);

		aModel.binPacking(aVar, aMatrixLocation, lReturnValue, 0).post();

		return lReturnValue[aIndex];
	}
}