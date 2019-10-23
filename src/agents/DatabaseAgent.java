
package agents;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.sql.Statement;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

import data.DataModel;
import jade.core.Agent;
import jade.core.behaviours.CyclicBehaviour;
import jade.lang.acl.ACLMessage;

public class DatabaseAgent extends Agent
{
	private static final long serialVersionUID = 1L;

	private Connection fDBConnection;
	private DataModel fDataModel;
	
	private void createTables()
	{
		try
		{
			Statement stmt = fDBConnection.createStatement();

			stmt.execute("CREATE TABLE IF NOT EXISTS agent_data(Agent_ID INT, Agent_Capacity INT, PRIMARY KEY (Agent_ID));");
			stmt.execute("CREATE TABLE IF NOT EXISTS location_data(Location_ID INT, Pos_X INT, Pos_Y INT, PRIMARY KEY (Location_ID));");
			stmt.execute("CREATE TABLE IF NOT EXISTS agent_positions(Agent_ID INT, Pos_X INT, Pos_Y INT, Agent_Time VARCHAR(256), FOREIGN KEY (Agent_ID) REFERENCES agent_data(Agent_ID));");
			stmt.execute("CREATE TABLE IF NOT EXISTS agent_routes(Route_ID INT, Route VARCHAR(1000), Route_Length INT);");

			stmt.executeUpdate("DELETE FROM Agent_Positions");
			stmt.executeUpdate("DELETE FROM agent_routes");
			stmt.executeUpdate("DELETE FROM location_data");

		}
		catch(Exception e) { e.printStackTrace(); }
	}
	
	protected void setup()
	{
		Object[] lArguments = getArguments();
		fDataModel = (DataModel) lArguments[0];
		
		// Connect to Database
		try
		{
			Class.forName("com.mysql.jdbc.Driver");
			fDBConnection = DriverManager.getConnection
			(
				"jdbc:mysql://localhost:3306/deliveryroutingdb", 
				"root", 
				""
			);
		}
		catch (Exception e) { e.printStackTrace(); }
		
		// generate database
		createTables();
		
		try
		{
			for (int i = 0; i < fDataModel.numLocations(); i++)
			{
				Statement lLocationStatement = fDBConnection.createStatement();
				lLocationStatement.executeUpdate("INSERT INTO location_data VALUES (" + i + "," + fDataModel.getLocation(i).x + "," + fDataModel.getLocation(i).y + ")");
			}
		}
		catch ( SQLException e ) { e.printStackTrace(); }
		
		// add message receiving behavior
		addBehaviour(new CyclicBehaviour(this)
		{
			private static final long serialVersionUID = 1L;

			public void action() 
			{
				ACLMessage lMessage = receive();

				if (lMessage != null)
				{
					// driver wanting to update its position
					if (lMessage.getContent().contains("agent_position"))
					{
						DateTimeFormatter lDateFormatter = DateTimeFormatter.ofPattern("HH:mm:ss:SSS");

						LocalDateTime lNow = LocalDateTime.now();
						String agent_positions = lMessage.getContent().split("agent_position:")[1];
						long lPosX = Math.round(Double.valueOf(agent_positions.split(",")[0]));
						long lPosY = Math.round(Double.valueOf(agent_positions.split(",")[1]));

						addVehiclePosition
						(
							Integer.valueOf(lMessage.getSender().getLocalName().replace("Delivery_Agent","")),
							lPosX,
							lPosY,
							lDateFormatter.format(lNow)
						);
					}

					if (lMessage.getContent().contains("agent_details"))
					{
						String message_string = lMessage.getContent().split("agent_details:")[1];
						addVehicle(Integer.valueOf(message_string.split(",")[0]), Integer.valueOf(message_string.split(",")[1]));
					}

					// routing agent wanting to add new route
					if (lMessage.getContent().contains("agent_routes"))
					{
						String message_string = lMessage.getContent().split("agent_details:")[1];
						String[] lMessageArray = message_string.split(" ");
						addRoute(Integer.parseInt(lMessageArray[0]), lMessageArray[1].substring(0, lMessageArray[1].length() - 1), Integer.parseInt(lMessageArray[2]));
					}
				}

				// wait until next message
				block();
			}
		});
	}
	
	private void addVehiclePosition(int aVehicleNum, long aPosX, long aPosY, String aTime)
	{
		try
		{
			Statement lStatement = fDBConnection.createStatement();
			lStatement.executeUpdate
			(
				"INSERT INTO Agent_Positions VALUES (" 
				+ aVehicleNum + "," 
				+ aPosX + "," 
				+ aPosY + ",'" 
				+ aTime + "')"
			);
		}
		catch (SQLException e) { e.printStackTrace(); }
	}

	private void addVehicle(int aVehicleNum, int aVehicleCapacity)
	{
		try
		{
			Statement lStatement = fDBConnection.createStatement();
			lStatement.executeUpdate
					(
					"INSERT INTO agent_data VALUES ("
							+ aVehicleNum + "," + aVehicleCapacity + ");"
					);
		}
		catch (SQLException e) { e.printStackTrace(); }
	}

	private void addRoute(int aID, String aRouteString, int aRouteLength)
	{
		try 
		{
			Statement stmt = fDBConnection.createStatement();
			stmt.executeUpdate("INSERT INTO agent_routes VALUES (" + aID + ", '" + aRouteString + "', " + aRouteLength + ");");
		}
		catch ( SQLException e ) { e.printStackTrace(); }
	}
	
	protected void takeDown()
	{
		try 
		{
			Statement lDropStatement = fDBConnection.createStatement();
			lDropStatement.executeUpdate("DROP TABLE IF EXISTS agent_positions");
			lDropStatement.executeUpdate("DROP TABLE IF EXISTS agent_data");
			lDropStatement.executeUpdate("DROP TABLE IF EXISTS location_data");
			lDropStatement.executeUpdate("DROP TABLE IF EXISTS agent_routes");
		} 
		catch (SQLException e) { e.printStackTrace(); }
	}
}
