package gui;

import javax.swing.*;
import java.awt.*;
import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class DeliveryInformation extends JPanel
{
    private static final long serialVersionUID = 1L;

    private static Connection fDBConnection;
    private static JList<String> fPathList;
    private JScrollPane fPaths;

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

        // Create environment
        fPathList = new JList<String>();
        fPaths = new JScrollPane(fPathList);
        lConstraints.gridx = 0;
        lConstraints.gridy = 0;
        add(fPaths, lConstraints);
    }

    public void updatePathList()
    {
        try
        {
            List<String> lPaths = new ArrayList<String>();

            // Query database for all routes
            Statement lLocationStatement = fDBConnection.createStatement();
            ResultSet lLocationResult = lLocationStatement.executeQuery
            (
            "SELECT * FROM agent_routes GROUP BY agent_routes.Agent_ID, agent_routes.Route_Position, agent_routes.Location_ID"
            );

            int current_agent = 0;
            String agent_route = "";

            while (lLocationResult.next())
            {
                if (lLocationResult.getInt("Agent_ID") == current_agent)
                {
                    agent_route += lLocationResult.getInt("Location_ID") + ", ";
                }
                else
                {
                    if (agent_route.length() > 0)
                    {
                        System.out.print(agent_route);
                        lPaths.add(current_agent + ": " + agent_route);
                        current_agent = lLocationResult.getInt("Agent_ID");
                    }
                }
            }

            // Create JList from array and set environment to display JList
            fPathList = new JList<String>(lPaths.toArray(new String[0]));
            fPaths.getViewport().setView(fPathList);
        }
        catch (SQLException e) { e.printStackTrace(); }
    }
}
