
package gui;

import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.TextField;
import java.io.FileInputStream;
import java.io.InputStream;
import java.util.Properties;

import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JComboBox;
import javax.swing.JLabel;
import javax.swing.JPanel;

import data.DataModel;

public class EditorForm extends JPanel
{
	private static final long serialVersionUID = 1L;

	private TextField fLocations;
	private TextField fVehicles;
	private TextField fCapacity;
	private TextField fSeed;
	private JComboBox<String> fRoutingMethods;
	private JCheckBox fCargoWeight;
	private JCheckBox fNormality;
	private JButton fSubmit;
	private JButton fDefaults;

	public EditorForm( RoutingFrame aRoutingFrame )
	{
		this.setLayout(new GridBagLayout());

		GridBagConstraints lConstraints = new GridBagConstraints();
		lConstraints.fill = GridBagConstraints.HORIZONTAL;

		lConstraints.gridx = 0;
		lConstraints.gridy = 0;
		lConstraints.gridheight = 1;
		lConstraints.gridwidth = 1;
		this.add(new JLabel("Locations:"), lConstraints);

		fLocations = new TextField();
		lConstraints.gridx = 1;
		lConstraints.gridy = 0;
		lConstraints.gridheight = 1;
		lConstraints.gridwidth = 1;
		this.add(fLocations, lConstraints);

		lConstraints.gridx = 0;
		lConstraints.gridy = 1;
		lConstraints.gridheight = 1;
		lConstraints.gridwidth = 1;
		this.add(new JLabel("Vehicles:"), lConstraints);

		fVehicles = new TextField();
		lConstraints.gridx = 1;
		lConstraints.gridy = 1;
		lConstraints.gridheight = 1;
		lConstraints.gridwidth = 1;
		this.add(fVehicles, lConstraints);

		lConstraints.gridx = 0;
		lConstraints.gridy = 2;
		lConstraints.gridheight = 1;
		lConstraints.gridwidth = 1;
		this.add(new JLabel("Capacity:"), lConstraints);

		fCapacity = new TextField();
		lConstraints.gridx = 1;
		lConstraints.gridy = 2;
		lConstraints.gridheight = 1;
		lConstraints.gridwidth = 1;
		this.add(fCapacity, lConstraints);

		lConstraints.gridx = 0;
		lConstraints.gridy = 3;
		lConstraints.gridheight = 1;
		lConstraints.gridwidth = 1;
		this.add(new JLabel("Seed:"), lConstraints);

		fSeed = new TextField();
		lConstraints.gridx = 1;
		lConstraints.gridy = 3;
		lConstraints.gridheight = 1;
		lConstraints.gridwidth = 1;
		this.add(fSeed, lConstraints);

		lConstraints.gridx = 0;
		lConstraints.gridy = 4;
		lConstraints.gridheight = 1;
		lConstraints.gridwidth = 1;
		this.add(new JLabel("Method:"), lConstraints);

		fRoutingMethods = new JComboBox<String>(new String[] 
		{
			"OR-Tools", 
			"CHOCO",
			"CHOCO2",
			"ACO", 
			"ACO-Partition"
		});
		lConstraints.gridx = 1;
		lConstraints.gridy = 4;
		lConstraints.gridheight = 1;
		lConstraints.gridwidth = 1;
		this.add(fRoutingMethods, lConstraints);

		lConstraints.gridx = 0;
		lConstraints.gridy = 5;
		lConstraints.gridheight = 1;
		lConstraints.gridwidth = 1;
		this.add(new JLabel("Capacity Weight:"), lConstraints);

		fCargoWeight = new JCheckBox();
		lConstraints.gridx = 1;
		lConstraints.gridy = 5;
		lConstraints.gridheight = 1;
		lConstraints.gridwidth = 1;
		this.add(fCargoWeight, lConstraints);

		lConstraints.gridx = 0;
		lConstraints.gridy = 6;
		lConstraints.gridheight = 1;
		lConstraints.gridwidth = 1;
		this.add(new JLabel("Capacity Dist:"), lConstraints);

		fNormality = new JCheckBox();
		lConstraints.gridx = 1;
		lConstraints.gridy = 6;
		lConstraints.gridheight = 1;
		lConstraints.gridwidth = 1;
		this.add(fNormality, lConstraints);

		fSubmit = new JButton("Submit");
		lConstraints.gridx = 0;
		lConstraints.gridy = 7;
		lConstraints.gridheight = 1;
		lConstraints.gridwidth = 2;
		fSubmit.addActionListener( e -> onSubmit( aRoutingFrame ) );
		this.add(fSubmit, lConstraints);

		fDefaults = new JButton("Default");
		lConstraints.gridx = 0;
		lConstraints.gridy = 8;
		lConstraints.gridheight = 1;
		lConstraints.gridwidth = 2;
		fDefaults.addActionListener( e -> onDefault( aRoutingFrame ) );
		this.add(fDefaults, lConstraints);
	}

	private void onSubmit( RoutingFrame aRoutingFrame ) 
	{
		try
		{
			DataModel lDataModel = new DataModel
			(
				Integer.parseInt(fVehicles.getText()), 
				Integer.parseInt(fLocations.getText()), 
				Integer.parseInt(fSeed.getText()), 
				Integer.parseInt(fCapacity.getText()),
				fCargoWeight.isSelected(),
				fNormality.isSelected()
			);
			aRoutingFrame.onSubmit( lDataModel, (String) fRoutingMethods.getSelectedItem() );
		}
		catch (Exception e) { e.printStackTrace(); }
	}

	private void onDefault( RoutingFrame aRoutingFrame )
	{
		try
		{
			InputStream lDefaults = new FileInputStream("Defaults/config.properties");

			Properties lProperties = new Properties();
			lProperties.load(lDefaults);

			DataModel lDataModel = new DataModel
			(
				Integer.parseInt(lProperties.getProperty("vehicles")), 
				Integer.parseInt(lProperties.getProperty("locations")), 
				Integer.parseInt(lProperties.getProperty("seed")), 
				Integer.parseInt(lProperties.getProperty("capacity"))
			);
			aRoutingFrame.onSubmit( lDataModel, lProperties.getProperty("method") );
		}
		catch (Exception e) { e.printStackTrace(); }
	}
}
