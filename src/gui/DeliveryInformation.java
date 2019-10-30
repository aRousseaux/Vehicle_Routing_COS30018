package gui;

import javax.swing.*;

import data.DataModel;

import java.awt.*;
import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class DeliveryInformation extends JPanel implements Runnable
{
    private static final long serialVersionUID = 1L;

    private static Connection fDBConnection;
    private static JList<String> fPathList;
    private JScrollPane fPaths;
    private double fPathsChecksum;
	private DataModel fDataModel;
	private Thread fThread;

    public DeliveryInformation()
    {
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

        // Set layout
        setLayout(new GridBagLayout());
        GridBagConstraints lConstraints = new GridBagConstraints();
        lConstraints.fill = GridBagConstraints.HORIZONTAL;

        // initialise data model
        fDataModel = null;
        
        // Create environment
        fPathList = new JList<String>();
        fPaths = new JScrollPane(fPathList);
        lConstraints.gridx = 0;
        lConstraints.gridy = 0;
        add(fPaths, lConstraints);
        
        // start updating
        fThread = new Thread( this );
        fThread.start();
    }

    public void updateDataModel(DataModel aDataModel) 
    {
		fDataModel = aDataModel;
	}
    
    public void updatePathList()
    {
    	// update paths
    	if ( fDataModel != null )
    	{
			try
			{
				List<List<Integer>> lPaths = new ArrayList<List<Integer>>();
				
				for ( int i = 0; i < fDataModel.numVehicles(); i++)
				{
					Statement lRouteStatement = fDBConnection.createStatement();
					ResultSet lRouteResult = lRouteStatement.executeQuery("SELECT * FROM agent_routes WHERE Agent_ID = "+i+" ORDER BY Agent_ID ASC, Route_Position ASC");
					
					List<Integer> lPath = new ArrayList<Integer>();
					while (lRouteResult.next())
					{
						lPath.add(lRouteResult.getInt("Location_ID"));
					}
					
					lPaths.add(lPath);
				}
				
				// Create JList from array and set environment to display JList
				String[] lTemp = new String[fDataModel.numVehicles()];
				for (int i = 0; i < fDataModel.numVehicles(); i++)
				{
					lTemp[i] = lPaths.get(i).toString();
				}
				fPathList = new JList<String>(lTemp);
	            fPaths.getViewport().setView(fPathList);
			}
			catch (SQLException e) { e.printStackTrace(); }
    	}
    	else { System.out.println("data model is null!"); }
    }

	public void run() 
	{
		try 
		{
			while ( fThread != null )
			{
				Statement lLocationStatement = fDBConnection.createStatement();
				ResultSet lLocationResult;
				
				lLocationResult = lLocationStatement.executeQuery("CHECKSUM TABLE agent_routes;");
				lLocationResult.next();
				if (fPathsChecksum != lLocationResult.getDouble("Checksum"))
				{
					fPathsChecksum = lLocationResult.getDouble("Checksum");
					updatePathList();
				}
				Thread.sleep(2000);
			}
		} 
		catch (InterruptedException | SQLException e) { e.printStackTrace(); }		
	}
}
