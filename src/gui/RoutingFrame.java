
package gui;

import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;

import javax.swing.JFrame;

import controller.Controller;
import data.DataModel;

public class RoutingFrame extends JFrame
{
	private static final long serialVersionUID = 1L;

	private VisualisationCanvas fVisualisation; // canvas for drawing database data
	private EditorForm fForm;   // initialization form
	private Controller fController; // agent controller
	
	public RoutingFrame(String aTitle)
	{
		super(aTitle);
		
		// window decorations
		JFrame.setDefaultLookAndFeelDecorated( true );
		setDefaultCloseOperation( JFrame.EXIT_ON_CLOSE );
		
		// grid back layout
		getContentPane().setLayout( new GridBagLayout() );
		GridBagConstraints lConstraints = new GridBagConstraints();
		lConstraints.fill = GridBagConstraints.HORIZONTAL;
		
		// add visualization to the GUI frame
		fVisualisation = new VisualisationCanvas();
		lConstraints.gridx = 0;
		lConstraints.gridy = 0;
		lConstraints.gridheight = 2;
		lConstraints.gridwidth = 1;
		add( fVisualisation, lConstraints );
		
		// add form to the frame
		fForm = new EditorForm(this);
		lConstraints.gridx = 1;
		lConstraints.gridy = 0;
		lConstraints.gridheight = 1;
		lConstraints.gridwidth = 1;
		add( fForm, lConstraints );
		
		// resize window to fit all
		pack();
	}
	
	public void onSubmit( DataModel aDataModel, String aMethod )
	{
		if (fController != null)
		{
			fController.shutdown();
		}
		
		fController = new Controller( aDataModel, "OR-Tools" );
	}
}
