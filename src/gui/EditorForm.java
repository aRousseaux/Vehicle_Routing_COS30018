
package gui;

import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.TextField;

import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JLabel;
import javax.swing.JPanel;

public class EditorForm extends JPanel
{
private static final long serialVersionUID = 1L;
	
	private TextField fLocations;
	private TextField fCapacity;
	private TextField fVehicles;
	private JComboBox<String> fRoutingMethods;
	private JButton fSubmit;
	
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
		this.add(new JLabel("Method:"), lConstraints);

		fRoutingMethods = new JComboBox<String>(new String[] 
		{
			"OR-Tools", 
			"CHOCO", 
			"ACO", 
			"ACO-Partition"
		});
		lConstraints.gridx = 1;
		lConstraints.gridy = 3;
		lConstraints.gridheight = 1;
		lConstraints.gridwidth = 1;
		this.add(fRoutingMethods, lConstraints);

		fSubmit = new JButton("Submit");
		lConstraints.gridx = 0;
		lConstraints.gridy = 4;
		lConstraints.gridheight = 1;
		lConstraints.gridwidth = 2;
		fSubmit.addActionListener( e -> onSubmit( aRoutingFrame ) );
		this.add(fSubmit, lConstraints);
	}

	private void onSubmit( RoutingFrame aRoutingFrame ) 
	{
		aRoutingFrame.onSubmission();
	}
}
