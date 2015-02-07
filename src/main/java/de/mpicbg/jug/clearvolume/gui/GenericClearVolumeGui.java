/**
 *
 */
package de.mpicbg.jug.clearvolume.gui;

import java.awt.BorderLayout;
import java.awt.Container;
import java.awt.GridLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import javax.swing.BorderFactory;
import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTextField;

import net.imagej.ImgPlus;
import net.imglib2.algorithm.stats.ComputeMinMax;
import net.imglib2.type.NativeType;
import net.imglib2.type.numeric.RealType;
import clearvolume.renderer.ControlJPanel;

import com.jogamp.newt.awt.NewtCanvasAWT;


/**
 * @author jug
 */
public class GenericClearVolumeGui< T extends RealType< T > & NativeType< T >> extends JPanel implements ActionListener {

	private Container ctnrClearVolume;
	private JPanel panelControls;
	private JButton buttonReinitializeView;
	private JButton buttonResetView;
	private JButton buttonUpdateView;
	private JTextField txtTextureWidth;
	private JTextField txtTextureHeight;
	private JTextField txtMinInt;
	private JTextField txtMaxInt;
	private JTextField txtVoxelSizeX;
	private JTextField txtVoxelSizeY;
	private JTextField txtVoxelSizeZ;
	private JButton buttonToggleBox;
	private JButton buttonToggleRecording;

	private int textureWidth;
	private int textureHeight;

	private ImgPlus< T > imgPlus;
	private ClearVolumeManager< T > cvManager;

	public GenericClearVolumeGui( final ImgPlus< T > imgPlus ) {
		this( imgPlus, 512, 512 );
	}

	public GenericClearVolumeGui( final ImgPlus< T > imgPlus, final int textureWidth, final int textureHeight ) {
		super( true );

		setTextureSize( textureWidth, textureHeight );

		if ( imgPlus != null ) {
			setImgPlus( imgPlus );
		}
	}

	private void setTextureSize( final int textureWidth, final int textureHeight ) {
		this.textureWidth = textureWidth;
		this.textureHeight = textureHeight;
		if ( cvManager != null ) {
			cvManager.setTextureSize( textureWidth, textureHeight );
		}
	}

	public void setImgPlus( final ImgPlus< T > imgPlus ) {
		if ( imgPlus == null ) return;

		boolean isFirstTime = false;
		if ( this.imgPlus == null ) isFirstTime = true;
		this.imgPlus = imgPlus;

		double oldMin = 0, oldMax = 255;
		double oldVoxelSizeX = 1, oldVoxelSizeY = 1, oldVoxelSizeZ = 1;
		if ( cvManager != null ) {
			oldMin = cvManager.getMinIntensity();
			oldMax = cvManager.getMaxIntensity();
			oldVoxelSizeX = cvManager.getVoxelSizeX();
			oldVoxelSizeY = cvManager.getVoxelSizeY();
			oldVoxelSizeZ = cvManager.getVoxelSizeZ();
			cvManager.close();
		}
		cvManager = new ClearVolumeManager< T >( imgPlus, textureWidth, textureHeight );
		if ( isFirstTime ) {
			final T min = imgPlus.firstElement().createVariable();
			final T max = imgPlus.firstElement().createVariable();
			ComputeMinMax.computeMinMax( imgPlus, min, max );
			cvManager.setIntensityValues( min.getRealDouble(), max.getRealDouble() );
			cvManager.setVoxelSize( imgPlus.averageScale( 0 ), imgPlus.averageScale( 1 ), imgPlus.averageScale( 2 ) );
		} else {
			cvManager.setIntensityValues( oldMin, oldMax );
			cvManager.setVoxelSize( oldVoxelSizeX, oldVoxelSizeY, oldVoxelSizeZ );
		}
		cvManager.run();

		rebuildGui();
	}

	public ClearVolumeManager< T > getClearVolumeManager() {
		return cvManager;
	}

	public void pushParamsToGui() {
		txtTextureWidth.setText( "" + cvManager.getTextureWidth() );
		txtTextureHeight.setText( "" + cvManager.getTextureHeight() );

		txtMinInt.setText( "" + cvManager.getMinIntensity() );
		txtMaxInt.setText( "" + cvManager.getMaxIntensity() );

		txtVoxelSizeX.setText( "" + cvManager.getVoxelSizeX() );
		txtVoxelSizeY.setText( "" + cvManager.getVoxelSizeY() );
		txtVoxelSizeZ.setText( "" + cvManager.getVoxelSizeZ() );
	}

	/**
	 * Read all validly entered text field values and activate them.
	 */
	private void activateGuiValues() {
		int i;
		double d;

		try {
			i = Integer.parseInt( txtTextureWidth.getText() );
		} catch ( final NumberFormatException e ) {
			i = this.textureWidth;
		}
		this.textureWidth = i;

		try {
			i = Integer.parseInt( txtTextureHeight.getText() );
		} catch ( final NumberFormatException e ) {
			i = this.textureHeight;
		}
		this.textureHeight = i;

		cvManager.setTextureSize( this.textureWidth, this.textureHeight );

		try {
			d = Double.parseDouble( txtMinInt.getText() );
		} catch ( final NumberFormatException e ) {
			d = cvManager.getMinIntensity();
		}
		final double minIntensity = d;

		try {
			d = Double.parseDouble( txtMaxInt.getText() );
		} catch ( final NumberFormatException e ) {
			d = cvManager.getMaxIntensity();
		}
		final double maxIntensity = d;

		cvManager.setIntensityValues( minIntensity, maxIntensity );

		try {
			d = Double.parseDouble( txtVoxelSizeX.getText() );
		} catch ( final NumberFormatException e ) {
			d = cvManager.getVoxelSizeX();
		}
		final double voxelSizeX = d;

		try {
			d = Double.parseDouble( txtVoxelSizeY.getText() );
		} catch ( final NumberFormatException e ) {
			d = cvManager.getVoxelSizeY();
		}
		final double voxelSizeY = d;

		try {
			d = Double.parseDouble( txtVoxelSizeZ.getText() );
		} catch ( final NumberFormatException e ) {
			d = cvManager.getVoxelSizeZ();
		}
		final double voxelSizeZ = d;

		cvManager.setVoxelSize( voxelSizeX, voxelSizeY, voxelSizeZ );
	}

	private void rebuildGui() {
//		this.setIgnoreRepaint( true );
		this.setVisible( false );
		this.removeAll();

		this.setLayout( new BorderLayout() );

		ctnrClearVolume = new Container();
		ctnrClearVolume.setLayout( new BorderLayout() );

		ControlJPanel panelClearVolumeControl = null;

		if ( cvManager != null ) {
			final NewtCanvasAWT canvas = cvManager.getClearVolumeRendererInterface().getNewtCanvasAWT();
			ctnrClearVolume.add( canvas, BorderLayout.CENTER );

			panelClearVolumeControl = new ControlJPanel();
			panelClearVolumeControl.setClearVolumeRendererInterface( cvManager.getClearVolumeRendererInterface() );
		} else {
			System.err.println( "ClearVolumeTableCellView: Did you intend this? You called buildGui while cvManager==null!" );
		}

		// Main controls panel
		// -------------------
		panelControls = new JPanel();
		panelControls.setLayout( new BoxLayout( panelControls, BoxLayout.Y_AXIS ) );
		panelControls.add( Box.createVerticalGlue() );


		// Parameters requiring reinitialization
		// -------------------------------------
		JPanel panelControlsHelper = new JPanel( new GridLayout( 4, 2 ) );
		panelControlsHelper.setBorder( BorderFactory.createEmptyBorder( 0, 5, 2, 2 ) );

		final JLabel lblMinInt = new JLabel( "Min. intensity" );
		txtMinInt = new JTextField();
		final JLabel lblMaxInt = new JLabel( "Max. intensity" );
		txtMaxInt = new JTextField();

		final JLabel lblTextureWidth = new JLabel( "Texture width" );
		txtTextureWidth = new JTextField();
		final JLabel lblTextureHeight = new JLabel( "Texture height" );
		txtTextureHeight = new JTextField();

		panelControlsHelper.add( lblMinInt );
		panelControlsHelper.add( txtMinInt );
		panelControlsHelper.add( lblMaxInt );
		panelControlsHelper.add( txtMaxInt );

		panelControlsHelper.add( lblTextureWidth );
		panelControlsHelper.add( txtTextureWidth );
		panelControlsHelper.add( lblTextureHeight );
		panelControlsHelper.add( txtTextureHeight );

		JPanel shrinkingHelper = new JPanel( new BorderLayout() );
		shrinkingHelper.add( panelControlsHelper, BorderLayout.SOUTH );
		shrinkingHelper.setBorder( BorderFactory.createEmptyBorder( 0, 5, 22, 2 ) );
		panelControls.add( shrinkingHelper );

		buttonReinitializeView = new JButton( "Set" );
		buttonReinitializeView.addActionListener( this );

		shrinkingHelper = new JPanel( new BorderLayout() );
		shrinkingHelper.add( buttonReinitializeView, BorderLayout.SOUTH );
		shrinkingHelper.setBorder( BorderFactory.createEmptyBorder( 0, 5, 2, 2 ) );
		panelControls.add( shrinkingHelper );

		// Parameters that require a view update
		// -------------------------------------
		panelControlsHelper = new JPanel( new GridLayout( 3, 2 ) );
		panelControlsHelper.setBorder( BorderFactory.createEmptyBorder( 0, 5, 2, 2 ) );

		final JLabel lblVoxelSizeX = new JLabel( "VoxelDimension.X" );
		txtVoxelSizeX = new JTextField();
		final JLabel lblVoxelSizeY = new JLabel( "VoxelDimension.Y" );
		txtVoxelSizeY = new JTextField();
		final JLabel lblVoxelSizeZ = new JLabel( "VoxelDimension.Z" );
		txtVoxelSizeZ = new JTextField();

		panelControlsHelper.add( lblVoxelSizeX );
		panelControlsHelper.add( txtVoxelSizeX );
		panelControlsHelper.add( lblVoxelSizeY );
		panelControlsHelper.add( txtVoxelSizeY );
		panelControlsHelper.add( lblVoxelSizeZ );
		panelControlsHelper.add( txtVoxelSizeZ );

		shrinkingHelper = new JPanel( new BorderLayout() );
		shrinkingHelper.add( panelControlsHelper, BorderLayout.SOUTH );
		shrinkingHelper.setBorder( BorderFactory.createEmptyBorder( 0, 5, 2, 2 ) );
		panelControls.add( shrinkingHelper );

		buttonUpdateView = new JButton( "Set" );
		buttonUpdateView.addActionListener( this );

		shrinkingHelper = new JPanel( new BorderLayout() );
		shrinkingHelper.add( buttonUpdateView, BorderLayout.SOUTH );
		shrinkingHelper.setBorder( BorderFactory.createEmptyBorder( 0, 5, 2, 2 ) );
		panelControls.add( shrinkingHelper );

		buttonResetView = new JButton( "Reset" );
		buttonResetView.addActionListener( this );

		shrinkingHelper = new JPanel( new BorderLayout() );
		shrinkingHelper.add( buttonResetView, BorderLayout.SOUTH );
		shrinkingHelper.setBorder( BorderFactory.createEmptyBorder( 0, 5, 22, 2 ) );
		panelControls.add( shrinkingHelper );

		// Toggle-buttons
		// --------------
		buttonToggleBox = new JButton( "Show/Unshow Box" );
		buttonToggleBox.addActionListener( this );

		shrinkingHelper = new JPanel( new BorderLayout() );
		shrinkingHelper.add( buttonToggleBox, BorderLayout.SOUTH );
		shrinkingHelper.setBorder( BorderFactory.createEmptyBorder( 0, 5, 2, 2 ) );
		panelControls.add( shrinkingHelper );

		buttonToggleRecording = new JButton( "Start/Stop Recording" );
		buttonToggleRecording.addActionListener( this );

		shrinkingHelper = new JPanel( new BorderLayout() );
		shrinkingHelper.add( buttonToggleRecording, BorderLayout.SOUTH );
		shrinkingHelper.setBorder( BorderFactory.createEmptyBorder( 0, 5, 22, 2 ) );
		panelControls.add( shrinkingHelper );

		// Display hijacked control container if possible
		// ----------------------------------------------
		if ( panelClearVolumeControl != null ) {
			this.add( panelClearVolumeControl, BorderLayout.SOUTH );
		}

		this.add( ctnrClearVolume, BorderLayout.CENTER );

		final JPanel helperPanel = new JPanel( new BorderLayout() );
		helperPanel.add( panelControls, BorderLayout.NORTH );

		final JScrollPane scrollPane = new JScrollPane( helperPanel );
		scrollPane.setHorizontalScrollBarPolicy( JScrollPane.HORIZONTAL_SCROLLBAR_NEVER );
		this.add( scrollPane, BorderLayout.EAST );

		// Update the values in the gui fields
		pushParamsToGui();

//		this.setIgnoreRepaint( false );
		this.setVisible( true );
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public void actionPerformed( final ActionEvent e ) {

		if ( e.getSource().equals( buttonReinitializeView ) ) {
			activateGuiValues();
			// this resets the existing imgPlus and thereby rebuilds/resets all
			setImgPlus( imgPlus );
		} else if ( e.getSource().equals( buttonUpdateView ) ) {
			activateGuiValues();
			cvManager.updateView();
		} else if ( e.getSource().equals( buttonResetView ) ) {
			cvManager.setVoxelSize( imgPlus.averageScale( 0 ), imgPlus.averageScale( 1 ), imgPlus.averageScale( 2 ) );
			pushParamsToGui();
			cvManager.resetView();
		} else if ( e.getSource().equals( buttonToggleBox ) ) {
			cvManager.toggleBox();
		} else if ( e.getSource().equals( buttonToggleRecording ) ) {
			cvManager.toggleRecording();
		}

	}

}
