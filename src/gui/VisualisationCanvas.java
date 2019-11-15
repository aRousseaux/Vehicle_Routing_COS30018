
package gui;

import java.awt.Canvas;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.Graphics;
import java.awt.Image;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

import data.*;

public class VisualisationCanvas extends Canvas implements Runnable 
{
	private static final long serialVersionUID = 1L;

	private Thread fThread;
	private Connection fDBConnection; // connection to database
	private List<Location> fLocations;
	private double fLocationChecksum;
	private List<Vehicle> fVehicles;
	private double fVehiclesChecksum;
	private List<List<Integer>> fPaths; // list of paths referencing location id
	private double fPathsChecksum;

	private boolean continue_path_update;

	public VisualisationCanvas()
	{
		// set canvas size
		setPreferredSize( new Dimension( 1000, 1000 ) );

		// initialize fields
		fLocations = new ArrayList<Location>();
		fVehicles = new ArrayList<Vehicle>();
		fPaths = new ArrayList<List<Integer>>();

		continue_path_update = true;

		// connect to Database
		try
		{
			Class.forName( "com.mysql.jdbc.Driver" );
			fDBConnection = DriverManager.getConnection
			(
				"jdbc:mysql://localhost:3306/deliveryroutingdb", 
				"root", 
				""
			);
		}
		catch ( Exception e ) { e.printStackTrace(); }

		// start thread
		fThread = new Thread( this );
		fThread.start();
	}

	public void run() 
	{
		while( fThread != null )
		{
			try 
			{
				// update rate
				Thread.sleep(2000);

				Statement lLocationStatement = fDBConnection.createStatement();
				ResultSet lLocationResult;

				lLocationResult = lLocationStatement.executeQuery("CHECKSUM TABLE location_data;");
				lLocationResult.next();
				if (fLocationChecksum != lLocationResult.getDouble("Checksum"))
				{
					fLocationChecksum = lLocationResult.getDouble("Checksum");
					updateLocations();
				}

				lLocationResult = lLocationStatement.executeQuery("CHECKSUM TABLE agent_positions;");
				lLocationResult.next();
				if (fVehiclesChecksum != lLocationResult.getDouble("Checksum"))
				{
					fVehiclesChecksum = lLocationResult.getDouble("Checksum");
					updateVehicles();
				}

				lLocationResult = lLocationStatement.executeQuery("CHECKSUM TABLE agent_routes;");
				lLocationResult.next();
				if (fPathsChecksum != lLocationResult.getDouble("Checksum"))
				{
					fPathsChecksum = lLocationResult.getDouble("Checksum");
					Thread.sleep(200); // allow concurrent updates to occur
					updatePaths();
				}
			} 
			catch (InterruptedException e) { e.printStackTrace(); } 
			catch (SQLException e) { e.printStackTrace(); }

			repaint();
		}
	}

	public void paint( Graphics g )
	{
		// repaint canvas
		update( g );
	}

	// repaint the canvas
	public void update( Graphics g ) 
	{
		// create image for double buffering
		Image lBuffer = createImage( getWidth(), getHeight() );
		Graphics gg = lBuffer.getGraphics();

		// clear background
		gg.setColor( Color.WHITE );
		gg.fillRect( 0, 0, getWidth(), getHeight() );

		// draw paths
		for (int i = 0; i < fPaths.size(); i++) 
		{
			Random lRand = new Random(i);

			float red = lRand.nextFloat();
			float green = lRand.nextFloat();
			float blue = lRand.nextFloat();

			gg.setColor(new Color(red, green, blue));

			for (int j = 0; j < fPaths.get(i).size() - 1; j++) 
			{
				gg.drawLine
				(
					fLocations.get(fPaths.get(i).get(j)).x + 5, 
					fLocations.get(fPaths.get(i).get(j)).y + 5,
					fLocations.get(fPaths.get(i).get(j + 1)).x + 5, 
					fLocations.get(fPaths.get(i).get(j + 1)).y + 5
				);
			}
		}

		// draw locations
		gg.setColor( Color.BLACK );
		fLocations.forEach( (lLocation) ->
		{
			gg.fillRect( lLocation.x, lLocation.y, 10, 10 );
			gg.drawString(String.valueOf(lLocation.getfLocationID()), lLocation.x, lLocation.y);
		});

		// draw depot
		gg.setColor( Color.RED );
		if (fLocations.size() != 0)
		{
			gg.fillRect( fLocations.get(0).x, fLocations.get(0).y, 10, 10 );
		}

		// draw driver locations
		for (int i = 0; i < fVehicles.size(); i++)
		{
			Random lRand = new Random(i);

			float red = lRand.nextFloat();
			float green = lRand.nextFloat();
			float blue = lRand.nextFloat();

			gg.setColor( new Color(red, green, blue) );

			gg.fillOval(fVehicles.get(i).x, fVehicles.get(i).y, 10, 10);
		}

		if (continue_path_update)
		{
			new Thread(new Runnable() {
				public void run() {
					if (updatePaths() == fLocations.size())
					{
						continue_path_update = false;
					}
				}
			}).start();
		}
		
		// draw final image to the screen
		g.drawImage( lBuffer, 0, 0, null );
		gg.dispose();
	}

	public void updateLocations()
	{
		// update locations
		try
		{
			Statement lLocationStatement = fDBConnection.createStatement();
			ResultSet lLocationResult = lLocationStatement.executeQuery("SELECT * FROM location_data");

			fLocations = new ArrayList<Location>();

			while (lLocationResult.next())
			{
				fLocations.add(new Location(lLocationResult.getInt("Location_ID"), lLocationResult.getInt("Pos_X"), lLocationResult.getInt("Pos_Y")));
			}
		}
		catch (SQLException e) { e.printStackTrace(); }
	}

	public void updateVehicles()
	{
		// update driver positions
		try
		{
			fVehicles = new ArrayList<Vehicle>();

			Statement lLocationStatement = fDBConnection.createStatement();
			ResultSet lLocationResult = lLocationStatement.executeQuery
			(
				"SELECT p.Agent_Id, p.Pos_X, p.Pos_Y, "
				+ "p.Agent_Time FROM (SELECT Agent_ID, "
				+ "max(Agent_Time) as Latest FROM agent_positions "
				+ "GROUP BY Agent_ID) AS x INNER JOIN agent_positions "
				+ "as p ON p.Agent_ID = x.Agent_ID AND p.Agent_Time = x.Latest "
				+ "ORDER BY p.Agent_ID ASC;"
			);

			while (lLocationResult.next())
			{
				int PosX = lLocationResult.getInt("Pos_X");
				int PosY = lLocationResult.getInt("Pos_Y");

				fVehicles.add(new Vehicle(PosX, PosY));
			}
		}
		catch (SQLException e) { e.printStackTrace(); }
	}

	public int updatePaths()
	{
		// update paths
		try
		{
			fPaths = new ArrayList<List<Integer>>();

			for ( int i = 0; i < fVehicles.size(); i++)
			{
				Statement lRouteStatement = fDBConnection.createStatement();
				ResultSet lRouteResult = lRouteStatement.executeQuery("SELECT * FROM agent_routes WHERE Agent_ID = "+i+" ORDER BY Agent_ID ASC, Route_Position ASC");

				List<Integer> lPath = new ArrayList<Integer>();
				while (lRouteResult.next())
				{
					lPath.add(lRouteResult.getInt("Location_ID"));
				}

				fPaths.add(lPath);
			}
		}
		catch (SQLException e) { e.printStackTrace(); }

		Statement TotalEntries = null;

		try
		{
			TotalEntries = fDBConnection.createStatement();
			ResultSet TotalEntriesResult = TotalEntries.executeQuery("SELECT COUNT(DISTINCT Location_ID) as TotalCount FROM agent_routes");

			TotalEntriesResult.next();
			return TotalEntriesResult.getInt("TotalCount");

		} catch (SQLException e) {
			e.printStackTrace();
		}

		return 0;
	}
}
