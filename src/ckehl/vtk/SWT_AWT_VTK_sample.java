package ckehl.vtk;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Map.Entry;
// import java.util.concurrent.locks.ReentrantLock;
import java.util.Set;

import org.eclipse.swt.SWT;
import org.eclipse.swt.awt.SWT_AWT;
import org.eclipse.swt.custom.SashForm;
import org.eclipse.swt.custom.ScrolledComposite;
import org.eclipse.swt.events.ControlEvent;
import org.eclipse.swt.events.ControlListener;
import org.eclipse.swt.events.FocusEvent;
import org.eclipse.swt.events.FocusListener;
import org.eclipse.swt.events.KeyEvent;
import org.eclipse.swt.events.KeyListener;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.events.SelectionListener;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.graphics.Point;
import org.eclipse.swt.graphics.RGB;
import org.eclipse.swt.layout.FillLayout;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.layout.RowLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.ColorDialog;
import org.eclipse.swt.widgets.Combo;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Event;
import org.eclipse.swt.widgets.FileDialog;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Layout;
import org.eclipse.swt.widgets.Listener;
import org.eclipse.swt.widgets.Menu;
import org.eclipse.swt.widgets.MenuItem;
import org.eclipse.swt.widgets.MessageBox;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.swt.widgets.Text;

import vtk.vtkActor;
import vtk.vtkCamera;
import vtk.vtkCanvas;
//import vtk.vtkCellArray;
import vtk.vtkConeSource;
//import vtk.vtkDoubleArray;
//import vtk.vtkIdList;
//import vtk.vtkImageReader2;
//import vtk.vtkImageReader2Factory;
import vtk.vtkInteractorStyleTrackballCamera;
import vtk.vtkJPEGReader;
import vtk.vtkLight;
import vtk.vtkLookupTable;
import vtk.vtkNativeLibrary;
import vtk.vtkPolyDataMapper;
import vtk.vtkRenderWindow;
//import vtk.vtkRenderWindowInteractor;
import vtk.vtkRenderer;
import vtk.vtkShortArray;
import vtk.vtkTIFFReader;
import vtk.vtkTexture;
import vtk.vtkTextureMapToSphere;
import vtk.vtkPolyData;
import vtk.vtkPolyDataReader;
import vtk.vtkXMLPolyDataReader;
import vtk.vtkPLYReader;
import vtk.vtkOBJReader;
import vtk.vtkPolyDataWriter;
import vtk.vtkXMLPolyDataWriter;
// import vtk.rendering.swt.vtkSwtComponent;
// import vtk.sample.VTKCanvas;
// import vtk.vtkPanel;
import vtk.vtkPLYWriter;
import vtk.vtkPNGReader;
import CGEL.combinatorics.CombinatoricsCompounds.LineSegment;
import CGEL.combinatorics.CombinatoricsCompounds.Mesh;
import CGEL.combinatorics.CombinatoricsPatch.Propagation;
import CGEL.combinatorics.CombinatoricsPatch.Morphology;
import CGEL.combinatorics.CombinatoricsPatch.Optimization;
import CGEL.combinatorics.CombinatoricsPrimitives.typedefs;
import CGEL.common.Vector.Vector4;
//import jogamp.nativewindow.jawt.windows.WindowsJAWTWindow;

public class SWT_AWT_VTK_sample implements SurfaceDataInterface, SurfaceSamplingInterface, ListViewUpdateInterface {
	private static final long serialVersionUID = 1L;
	
	public static Shell shell = null;
	public static Shell getShell() { return shell; }
	
	protected Display display = null;
	protected SashForm form = null;
	protected SashForm top = null;
	protected SashForm bottom = null;
	
	ScrolledComposite buttonBar = null;
	ScrolledComposite contextBar = null;
	Composite buttonContainer = null;
	Composite contextContainer = null;
	Composite listEditContainer = null;
	
	//protected vtkSwtComponent swt_widget = null;
	//protected vtkSwtComponent linkedView_widget = null;
	//protected vtkCanvas swt_component = null;
	protected SimpleVTKcanvas swt_component = null;
	protected java.awt.Frame swt_awt_frame=null;
	protected vtkCanvas linkedView = null;
	protected org.eclipse.swt.widgets.List category_listview = null;
	protected org.eclipse.swt.widgets.List wellLog_listview = null;
	protected Label out_size_illustration = null;
	//protected Text wellLog_nameedit= null;
	protected Button addCategoryButton = null;
	protected Button remLastCategoryButton = null;
	protected Button editWellLogButton = null;
	protected Text edt_propagate_struct_elem_size = null;
	protected Text edt_refine_struct_elem_size = null;
	protected Text edt_alpha_weight = null;
	protected Text edt_beta_weight = null;
	protected Text edt_gamma_weight = null;
	//Label lbl_propagate_struct_elem_size = null;
	//Label lbl_refine_struct_elem_size = null;
	protected int num_patches = 0;
	protected int active_category_id = 1;
	protected int selected_well_id = 0;
	
	Menu menu = null;
	Menu fileMenu = null;
	Menu viewMenu = null;
	Menu renderMenu = null;
	Menu selectInterpretMenu = null;
	MenuItem selectInterpretMenuItem = null;
	MenuItem welllogItem = null;
	MenuItem measureMenuItem = null;
	MenuItem saveWellLogItem = null;
	MenuItem loadLineMenu = null;
	MenuItem saveLineMenu = null;
	MenuItem saveMorphMenu = null;
	//MenuItem switchLightingItem = null;	// needed to enable/disable option
	//MenuItem switchNormalItem = null;	// needed to enable/disable option
	int cb_refine_candidate_selection_index = 0;
	int cb_refine_algorithm_selection_index = 0;
	int cb_refine_struct_elem_threshold_mode_index = 0;
	int cb_refine_radius_metric_index = 0;
	int cb_propagation_method_index = 0;
	int cb_propagation_struct_elem_radius_metric_index = 0;
	double cb_propagation_struct_elem_radius = 1.0;
	double vl_refine_struct_elem_radius = 1.0;
	double vl_alpha = 0.1; //0.01; //0.8;
	double vl_beta = 0.5; //0.3; //0.1;
	double vl_gamma = 0.3; //0.69; //0.1;
	
	Button addPatchButton = null;
	Button subtractPatchButton = null;
	Label lbl_sample_size = null;
	double sample_size_wellLog = 0;

	protected vtkRenderWindow rw = null; //new vtkRenderWindow();
	protected vtkRenderer ren = null; //new vtkRenderer();
	protected vtkCamera cam = null;
	protected vtkActor commonActor = null;
	protected vtkPolyDataMapper commonMapper = null;
	//protected vtkActor texturedActor = null;
	//protected vtkPolyDataMapper texturedMapper = null;
	protected vtkLookupTable defaultLookupTable = null;
	protected vtkLookupTable indicatorTagging = null;
	
	protected int active_mode = 0;	// 0 - normal navigation; 1 - point; 2 - line; 3 - area; 4 - wellLog;
	protected vtkInteractorStyleTrackballCamera mstyle = null;
	protected AreaPickInteractor area_select_style = null;
	protected LinePickInteractor line_select_style = null;
	protected PointPickInteractor point_select_style = null;
	protected WellLogCreator welllog_create_style = null;
	
	protected vtkPolyData main_obj = null;
	protected vtkPolyData init_patch_obj = null;
	protected Mesh mesh_obj_cgel = null;
	protected Mesh init_patch_obj_cgel = null;
	protected vtkPolyData morph_obj = null;
	protected vtkTexture main_tex = null;
	protected int stored_init_flags[];
	protected int stored_opt_flags[];
	protected int stored_refined_flags[];
	
	protected boolean global_propagate_refine = false;
	
	protected List<String> categoryNames = null;
	protected List<String> wellLogNames = null;
	
	/**
	 * Interaction data
	 */
	//char curIStyle = 'M';	// M = Move; P = Pick
	double _cam_pos[];
	double _cam_up[];
	double _cam_foc[];

	
	static void initLibraries() {
    	//System.loadLibrary("CGELcommond");
    	//System.loadLibrary("CGELcombinatoricsd");
        if (!vtkNativeLibrary.LoadAllNativeLibraries()) {
        	System.out.printf("loading libs individually");
            for (vtkNativeLibrary lib : vtkNativeLibrary.values()) {
            	//if(lib.name().contains("vtkFiltersPythonJava"))
            	//	continue;
            	//if(lib.name().contains("vtkPythonContext2DJava"))
            	//	continue;
                if (!lib.IsLoaded()) {
                    System.out.println(lib.GetLibraryName() + " not loaded");
                }
            }
        }
        vtkNativeLibrary.DisableOutputWindow(null);
	}
	
	/*
    // -----------------------------------------------------------------
    // Load VTK library and print which library was not properly loaded
    static {
    	//System.loadLibrary("CGELcommond");
    	//System.loadLibrary("CGELcombinatoricsd");
        if (!vtkNativeLibrary.LoadAllNativeLibraries()) {
        	System.out.printf("loading libs individually");
            for (vtkNativeLibrary lib : vtkNativeLibrary.values()) {
            	//if(lib.name().contains("vtkFiltersPythonJava"))
            	//	continue;
            	//if(lib.name().contains("vtkPythonContext2DJava"))
            	//	continue;
                if (!lib.IsLoaded()) {
                    System.out.println(lib.GetLibraryName() + " not loaded");
                }
            }
        }
        vtkNativeLibrary.DisableOutputWindow(null);
    }
    */



	
	/**
	 * DEPRECATED - USE BUTTON INTERACTIONS TO AVOID UNINTENTIONAL INTERNAL PICKER POPUP
	 * Key-press event and key evaluation
	 */
	/*
    public void keyDownEvent() {
    	System.out.printf("Key pressed: %s\n", swt_component.getRenderWindowInteractor().GetKeyCode());
    	char code = Character.toLowerCase(swt_component.getRenderWindowInteractor().GetKeyCode());
    	//System.out.printf("Key pressed: %s\n", swt_widget.getRenderWindowInteractor().GetKeyCode());
    	//char code = Character.toLowerCase(swt_widget.getRenderWindowInteractor().GetKeyCode());
    	if (code == 'p') {
    		if(area_select_style.GetEnabled()==0) {
    			area_select_style.SetInteractor(swt_component.getRenderWindowInteractor());
    			//area_select_style.SetInteractor(swt_widget.getRenderWindowInteractor());
    			swt_component.setInteractorStyle(area_select_style);
    			//swt_widget.setInteractorStyle(area_select_style);
    			area_select_style.Enable();
    			System.out.println("Enabled surface Mouse Pick Interactor");
    		} else {
    			area_select_style.Disable();
    			mstyle.SetInteractor(swt_component.getRenderWindowInteractor());
    			//mstyle.SetInteractor(swt_widget.getRenderWindowInteractor());
    			swt_component.setInteractorStyle(mstyle);
    			//swt_widget.setInteractorStyle(mstyle);
    			System.out.println("Disabled surface Mouse Pick Interactor");
    		}
		}
    }
    */
    
    /**
     * Setting up 3D rendering framework
     */
    public void setup_rendering() {
        // build VTK Pipeline
        vtkConeSource cone = new vtkConeSource();
        cone.SetResolution(8);
        cone.Update();
        main_obj = cone.GetOutput();

        if(commonMapper==null)
        	commonMapper = new vtkPolyDataMapper();
        commonMapper.SetInputData(main_obj);
        defaultLookupTable = new vtkLookupTable();
        defaultLookupTable.DeepCopy(commonMapper.GetLookupTable()); //vtkScalarsToColors
        indicatorTagging = new vtkLookupTable();
        LookUpValues luv = new LookUpValues();
        //indicatorTagging.GetTable().Reset();
        indicatorTagging.SetNumberOfColors(luv.categoryColours.size());
        indicatorTagging.Build();
        Set<Entry<Integer, Vector4>> colourEntrySet = luv.categoryColours.entrySet();
        Iterator<Entry<Integer, Vector4>> colourEntryIterator = colourEntrySet.iterator();
        //Vector4 colourEntry = null;
        //for(int lu_index=0; lu_index<luv.categoryColours.size(); lu_index++) {
        //	colourEntry = luv.categoryColours.get(lu_index);
        while(colourEntryIterator.hasNext()) {
        	Entry<Integer, Vector4> colourEntry = colourEntryIterator.next();
        	indicatorTagging.SetTableValue(colourEntry.getKey(), colourEntry.getValue().getVar0(), colourEntry.getValue().getVar1(), colourEntry.getValue().getVar2(), colourEntry.getValue().getVar3());
        }
        //indicatorTagging.SetTableValue(0, 1.0, 1.0, 1.0, 1.0);
        //indicatorTagging.SetTableValue(1, 95.0/255.0, 201.0/255.0, 73.0/255.0, 1.0); //95,201,73
        //indicatorTagging.SetTableValue(2, 31.0/255.0, 120.0/255.0, 180.0/255.0, 1.0);
        //indicatorTagging.SetTableValue(3, 227.0/255.0, 26.0/255.0, 28.0/255.0, 1.0);
        //indicatorTagging.SetTableValue(4, 255.0/255.0, 127.0/255.0, 0.0/255.0, 1.0);
        //indicatorTagging.SetTableValue(5, 106.0/255.0, 61.0/255.0, 154.0/255.0, 1.0);
        //indicatorTagging.SetTableValue(6, 177.0/255.0, 89.0/255.0, 40.0/255.0, 1.0);
        //indicatorTagging.SetTableValue(7, 178.0/255.0, 223.0/255.0, 138.0/255.0, 1.0);
        //indicatorTagging.SetTableValue(8, 166.0/255.0, 206.0/255.0, 227.0/255.0, 1.0);
        //indicatorTagging.SetTableValue(9, 251.0/255.0, 154.0/255.0, 153.0/255.0, 1.0);
        //indicatorTagging.SetTableValue(10, 253.0/255.0, 191.0/255.0, 111.0/255.0, 1.0);
        //indicatorTagging.SetTableValue(11, 202.0/255.0, 178.0/255.0, 214.0/255.0, 1.0);
        //indicatorTagging.SetTableValue(12, 255.0/255.0, 255.0/255.0, 153.0/255.0, 1.0);
        commonMapper.SetLookupTable(indicatorTagging);
        //commonMapper.InterpolateScalarsBeforeMappingOn();
        commonMapper.InterpolateScalarsBeforeMappingOff();
        commonMapper.SetScalarRange(0, luv.categoryColours.size()-1);
        commonMapper.SetArrayName("FlagT");
        commonMapper.SetScalarModeToUseCellData();
        commonMapper.SetScalarVisibility(1);


        if(commonActor==null)
        	commonActor = new vtkActor();
        commonActor.SetMapper(commonMapper);
        
        //if(texturedMapper==null)
        //	texturedMapper = new vtkPolyDataMapper();
        //texturedMapper.SetInputData(main_obj);
        //if(texturedActor==null)
        //	texturedActor = new vtkActor();
        //texturedActor.SetMapper(texturedMapper);
        //if(main_tex!=null)
        //	texturedActor.SetTexture(main_tex);

        ren.AddActor(commonActor);
        //ren.AddActor(texturedActor);
        if(area_select_style!=null)
        	area_select_style.SetPickConstraint_Actor(commonActor);
        if(line_select_style!=null)
        	line_select_style.SetPickConstraint_Actor(commonActor);
        if(point_select_style!=null)
        	point_select_style.SetPickConstraint_Actor(commonActor);
        if(welllog_create_style!=null)
        	welllog_create_style.SetPickConstraint_Actor(commonActor);

        ren.ResetCamera();
        vtkCamera active_cam = ren.GetActiveCamera();
    	_cam_pos = active_cam.GetPosition();
    	//System.out.printf("Cam position: %f, %f, %f\n", _cam_pos[0], _cam_pos[1], _cam_pos[2]);
    	_cam_up = active_cam.GetViewUp();
    	//System.out.printf("Cam view up: %f, %f, %f\n", _cam_up[0], _cam_up[1], _cam_up[2]);
    	_cam_foc = active_cam.GetFocalPoint();
    	//System.out.printf("Cam focal view: %f, %f, %f\n", _cam_foc[0], _cam_foc[1], _cam_foc[2]);
    }
	
	/**
	 * Open the window.
	 */
	public void open() {
		display = new Display();
		// create UI
		createShell();
		createForms();
		//create_option_section();
		createMenu();
		//create_3D();
		setup_rendering();

		shell.layout();
	    //shell.pack();
	    shell.open();
		while (!shell.isDisposed()) {
			if (!display.readAndDispatch()) {
				display.sleep();
			}
		}
	}
	
	/**
	 * Close the window
	 */
	public void close()
	{
		if(display!=null)
		{
			// dispose sub-components
			dispose_3D();
			disposeMenu();
			disposeForms();
			disposeShell();
			display.dispose();
		}
	}
	
	/**
	 * Create background shell
	 */
	protected void createShell()
	{
		shell = new Shell(display, SWT.SHELL_TRIM);
	    shell.addControlListener(new ControlListener() {

	        @Override
	        public void controlResized(ControlEvent e) {
	          if (e.widget instanceof Shell) {
	            Shell s = (Shell) e.widget;
	            if(swt_component!=null) {
	            	swt_component.setSize(s.getClientArea().width, s.getClientArea().height);
	            }
	            //if(swt_widget!=null) {
	            //	swt_widget.setSize(s.getClientArea().width, s.getClientArea().height);
	            //}
	            if(swt_component!=null) {
	            //	swt_component.lock();
	            	if(ren!=null)
	            		ren.ResetCamera();
	            	//if(rw!=null)
	            	//	rw.Render();
	            	
	            	//swt_component.GetRenderWindow().Render();
	            //	swt_component.unlock();
	            }
	          }
	        }

	        @Override
	        public void controlMoved(ControlEvent e) {

	        }
	      });
		shell.setSize(800, 600);
		GridLayout shellLayout = new GridLayout();
		shell.setLayout(shellLayout);
		GridData shellLayoutData = new GridData(SWT.FILL, SWT.FILL, true, true);
		shell.setLayoutData(shellLayoutData);
		shell.setText("Interpretation Studio");
		
		area_select_style = new AreaPickInteractor();
		line_select_style = new LinePickInteractor();
		point_select_style = new PointPickInteractor();
		welllog_create_style = new WellLogCreator();
		
		categoryNames = new ArrayList<String>();
		wellLogNames = new ArrayList<String>();
	}
	
	protected void disposeShell() {
		if(mesh_obj_cgel!=null) {
			mesh_obj_cgel.dispose();
			mesh_obj_cgel=null;
		}
		if(init_patch_obj_cgel!=null) {
			init_patch_obj_cgel.dispose();
			init_patch_obj_cgel=null;
		}
		if(area_select_style!=null) {
			area_select_style.Disable();
			area_select_style = null;
		}
		if(line_select_style!=null) {
			line_select_style.Disable();
			line_select_style = null;
		}
		if(point_select_style!=null) {
			point_select_style.Disable();
			point_select_style = null;
		}
		if(welllog_create_style!=null) {
			welllog_create_style.Disable();
			welllog_create_style = null;
		}
	}
	
	/**
	 * Create the menu
	 */
	protected void createMenu() {
		menu = new Menu(shell, SWT.BAR);
		menu.setLocation(new Point(0, 0));

		MenuItem cascadeFileMenu = new MenuItem(menu, SWT.CASCADE);
		cascadeFileMenu.setText("&File");		
		Menu fileMenu = new Menu(shell, SWT.DROP_DOWN);
		cascadeFileMenu.setMenu(fileMenu);
		
		MenuItem openDataMenu = new MenuItem(fileMenu, SWT.PUSH);
		openDataMenu.addListener(SWT.Selection, new Listener() {
			
			@Override
			public void handleEvent(Event arg0) {
				//MessageBox choiceDLG = new MessageBox(shell, SWT.ICON_QUESTION | SWT.OK | SWT.CANCEL);
				
		        FileDialog dialog = new FileDialog(shell, SWT.OPEN);
		        String[] filterNames = new String[] 
		            {"VTK legacy container (*.vtk)", "VTK PolyData (*.vtp)", "Standford PLY format (*.ply)", "Lightware Object Format (*.obj)", "All Files (*)"};

		        String[] filterExtensions = new String[] 
		            {"*.vtk", "*.vtp", "*.ply", "*.obj", "*"};

		        dialog.setFilterNames(filterNames);
		        dialog.setFilterExtensions(filterExtensions);
		        dialog.setText("Select (Patch) Surface File to Import");
				//String lastPath = Config.getInstance().getString(Config.LAST_OPEN_TEXT_PATH);
				//if (lastPath != null && !lastPath.isEmpty())
				//	fd.setFileName(lastPath);
		        dialog.setFilterPath("/media/christian/DATA/VGC2020");
		        //dialog.setFileName("/media/christian/DATA/VGC2020/");
		        String path = dialog.open();
		        
		        if (path != null) {
		        	if(path.contains("vtk")) {
			        	System.out.printf("file: %s\n", path);
			        	vtkPolyDataReader data_reader = new vtkPolyDataReader();
			        	data_reader.SetFileName(path);
			        	data_reader.Update();
			        	main_obj = data_reader.GetOutput();
		        	} else if(path.contains("vtp")) {
			        	System.out.printf("file: %s\n", path);
			        	vtkXMLPolyDataReader data_reader = new vtkXMLPolyDataReader();
			        	data_reader.SetFileName(path);
			        	data_reader.Update();
			        	main_obj = data_reader.GetOutput();
		        	} else if(path.contains("ply")) {
		        		System.out.printf("file: %s\n", path);
		        		vtkPLYReader data_reader = new vtkPLYReader();
		        		data_reader.SetFileName(path);
		        		data_reader.Update();
		        		main_obj = data_reader.GetOutput();
		        	} else if(path.contains(".obj")) {
		        		System.out.printf("file: %s\n", path);
		        		vtkOBJReader data_reader = new vtkOBJReader();
		        		data_reader.SetFileName(path);
		        		data_reader.Update();
		        		main_obj = data_reader.GetOutput();
		        	}
			        if((main_obj.GetPointData().HasArray("TCoords")==0) && (main_obj.GetPointData().HasArray("TexCoords")==0)) {
				        vtkTextureMapToSphere tcoord_gen = new vtkTextureMapToSphere();
				        tcoord_gen.SetInputData(main_obj);
				        tcoord_gen.PreventSeamOn();
				        tcoord_gen.Update();
				        main_obj = new vtkPolyData(tcoord_gen.GetOutput().GetVTKId());
			        }
			        
		        	int numCells = main_obj.GetNumberOfCells();
		        	int numPoints = main_obj.GetNumberOfPoints();
		        	int numVerts = main_obj.GetNumberOfVerts();
		        	System.out.printf("# points: %d, # verts: %d, # cells polydata: %d\n", numPoints, numVerts, numCells);
			        if(commonMapper!=null) {
			        	commonMapper.SetInputData(main_obj);
			        	commonMapper.Update();
			        	System.out.printf("common mapper set.\n");
			        } else {
			        	System.out.printf("common mapper not available.\n");
			        }
			        //if(texturedMapper!=null) {
			        //	texturedMapper.SetInputData(main_obj);
			        //}
			        
			        if(ren!=null) {
			        	
			        	ren.ResetCamera();
			        	vtkCamera active_cam = ren.GetActiveCamera();
			        	_cam_pos = active_cam.GetPosition();
			        	//System.out.printf("Cam position: %f, %f, %f\n", _cam_pos[0], _cam_pos[1], _cam_pos[2]);
			        	_cam_up = active_cam.GetViewUp();
			        	//System.out.printf("Cam view up: %f, %f, %f\n", _cam_up[0], _cam_up[1], _cam_up[2]);
			        	_cam_foc = active_cam.GetFocalPoint();
			        	//System.out.printf("Cam focal view: %f, %f, %f\n", _cam_foc[0], _cam_foc[1], _cam_foc[2]);
			        }
			        if(swt_component!=null) {
						//ren.Modified();
						//swt_component.getRenderWindowInteractor().Render();
			        	//swt_component.lock();
			        	//if(rw!=null)
			        	//	rw.Render();
			        	//if(ren!=null)
			        	//	ren.Render();
			        	//swt_component.Render();
		            	//swt_component.getRenderWindowInteractor().GetRenderWindow().Render();
			        	//swt_component.unlock();
			        }
			        
			        active_category_id=1;
			        category_listview.removeAll();
			        
			        numVerts = Math.max(numVerts, numPoints);
			        boolean has_vertex_indicators = ((main_obj.GetPointData().HasArray("FlagV")==1) ? true : false);
			        boolean has_cell_indicators = ((main_obj.GetCellData().HasArray("FlagT")==1) ? true : false);
			        boolean has_vertex_alt_indicators = ((main_obj.GetPointData().HasArray("Iv")==1) ? true : false);
			        boolean has_cell_alt_indicators = ((main_obj.GetCellData().HasArray("I")==1) ? true : false);
			        vtkShortArray vertex_alt_flags=null;
			        vtkShortArray cell_alt_flags=null;
			        int num_patches_v = 0;
			        int num_patches_t = 0;
			        boolean hasValidDecomp = false;
			        if(has_vertex_alt_indicators) {
			        	vertex_alt_flags = new vtkShortArray(main_obj.GetPointData().GetArray("Iv").GetVTKId());
			        }
			        if(has_cell_alt_indicators) {
			        	cell_alt_flags = new vtkShortArray(main_obj.GetCellData().GetArray("I").GetVTKId());
			        }
			        if(!has_vertex_indicators) {
			        	//System.out.println("Recompute vertex attribute.");
				        vtkShortArray vertex_flags = new vtkShortArray();
				        vertex_flags.SetName("FlagV");
				        vertex_flags.SetNumberOfComponents(1);
				        vertex_flags.SetNumberOfTuples(numVerts);
				        for(int i=0; i<numVerts; i++) {
				        	if(has_vertex_alt_indicators) {
				        		vertex_flags.SetValue(i, vertex_alt_flags.GetValue(i));
				        	} else {
				        		vertex_flags.SetValue(i, 0);
				        	}
				        }
				        main_obj.GetPointData().AddArray(vertex_flags);
			        } else {
			        	//System.out.println("Contains vertex attribute.");
			        	vtkShortArray vertex_flags = new vtkShortArray(main_obj.GetPointData().GetArray("FlagV").GetVTKId());
			        	for(int i=0; i<numVerts; i++) {
				        	if(has_vertex_alt_indicators) {
				        		num_patches_v = Math.max(vertex_alt_flags.GetValue(i), num_patches_v);
				        	} else {
				        		num_patches_v = Math.max(vertex_flags.GetValue(i), num_patches_v);
				        	}
			        	}
			        }
			        
			        if(!has_cell_indicators) {
			        	//System.out.println("Recompute cell attribute.");
				        vtkShortArray surface_flags = new vtkShortArray();
				        surface_flags.SetName("FlagT");
				        surface_flags.SetNumberOfComponents(1);
				        surface_flags.SetNumberOfTuples(numCells);
				        for(int i=0; i<numCells; i++) {
				        	if(has_cell_alt_indicators) {
				        		surface_flags.SetValue(i, cell_alt_flags.GetValue(i));
				        	} else {
				        		surface_flags.SetValue(i, 0);
				        	}
				        }
				        main_obj.GetCellData().AddArray(surface_flags);
			        } else {
			        	//System.out.println("Contains cell attribute.");
			        	vtkShortArray surface_flags = new vtkShortArray(main_obj.GetCellData().GetArray("FlagT").GetVTKId());
			        	for(int i=0; i<numCells; i++) {
				        	if(has_cell_alt_indicators) {
				        		num_patches_t = Math.max(cell_alt_flags.GetValue(i), num_patches_t);
				        	} else {
				        		num_patches_t = Math.max(surface_flags.GetValue(i), num_patches_t);
				        	}
			        	}
			        }
			        
			        num_patches = Math.max(num_patches_v, num_patches_t);
			        if(num_patches > 0)
			        	hasValidDecomp=true;
			        for(int i=0; i<num_patches; i++) {
						category_listview.add("Patch"+Integer.toString(i));
			        }
			        saveMorphMenu.setEnabled(false);
			        //System.out.printf("Updated UI for %d patches\n", num_patches);
			        if(init_patch_obj!=null) {
			        	init_patch_obj = null;
			        }
			        if(morph_obj!=null) {
			        	morph_obj = null;
			        }
			        if(init_patch_obj_cgel!=null) {
			        	init_patch_obj_cgel=null;
			        }
			        if(mesh_obj_cgel!=null) {
			        	mesh_obj_cgel=null;
			        }

			        main_obj.GetPointData().SetActiveScalars("FlagV");
			        main_obj.GetCellData().SetActiveScalars("FlagT");

			        if(hasValidDecomp) {
						init_patch_obj = new vtkPolyData();
						init_patch_obj.CopyStructure(main_obj);
						
					    vtkShortArray vertex_flags = new vtkShortArray();
					    vertex_flags.SetName("FlagV");
					    vertex_flags.SetNumberOfComponents(1);
					    vertex_flags.SetNumberOfTuples(numVerts);
					    init_patch_obj.GetPointData().AddArray(vertex_flags);
				        vtkShortArray dst_v_flags = new vtkShortArray(init_patch_obj.GetPointData().GetArray("FlagV").GetVTKId());
				        vtkShortArray src_v_flags = new vtkShortArray(main_obj.GetPointData().GetArray("FlagV").GetVTKId());
				        for(int i=0; i<numVerts; i++) {
				        	dst_v_flags.SetValue(i, src_v_flags.GetValue(i));
				        }
				        
					    vtkShortArray surface_flags = new vtkShortArray();
					    surface_flags.SetName("FlagT");
					    surface_flags.SetNumberOfComponents(1);
					    surface_flags.SetNumberOfTuples(numCells);
					    init_patch_obj.GetCellData().AddArray(surface_flags);
				        vtkShortArray dst_f_flags = new vtkShortArray(init_patch_obj.GetCellData().GetArray("FlagT").GetVTKId());
				        vtkShortArray src_f_flags = new vtkShortArray(main_obj.GetCellData().GetArray("FlagT").GetVTKId());
				        for(int i=0; i<numCells; i++) {
				        	dst_f_flags.SetValue(i, src_f_flags.GetValue(i));
				        }
			        }
			        
			        try {
			        mesh_obj_cgel = new Mesh(main_obj);
			        mesh_obj_cgel.computeNormals();
			        mesh_obj_cgel.computeCurvature();
			        mesh_obj_cgel.computeDeltaK();
			        System.out.println("Attributes and CGEL data structure ready.");
			        } catch (Exception e) {
			        	e.printStackTrace();
					}
			        
			        selectInterpretMenuItem.setEnabled(true);
			        measureMenuItem.setEnabled(true);
			        loadLineMenu.setEnabled(true);
			        saveLineMenu.setEnabled(true);
			        disable_selection();
			        clear_button_area();
			        clear_context_area();
		        }
			}
		});
		openDataMenu.setText("&Open ...");
		
		MenuItem saveDataMenu = new MenuItem(fileMenu, SWT.PUSH);
		saveDataMenu.addListener(SWT.Selection, new Listener() {
			
			@Override
			public void handleEvent(Event arg0) {
				//MessageBox choiceDLG = new MessageBox(shell, SWT.ICON_QUESTION | SWT.OK | SWT.CANCEL);
				//choiceDLG.setMessage("Which format do you choose ?");
				//choiceDLG.
				
		        FileDialog dialog = new FileDialog(shell, SWT.SAVE);
		        String[] filterNames = new String[] 
		            {"VTK legacy container (*.vtk)", "VTK PolyData (*.vtp)", "Standford PLY format (*.ply)", "All Files (*)"};

		        String[] filterExtensions = new String[] 
		            {"*.vtk", "*.vtp", "*.ply", "*"};

		        dialog.setFilterNames(filterNames);
		        dialog.setFilterExtensions(filterExtensions);
		        dialog.setText("Save (Patch) Surface to File");
		        String path = dialog.open();
		        
		        if (path != null) {
		        	if(path.contains("vtk")) {
			        	System.out.printf("file: %s\n", path);
			        	vtkPolyDataWriter data_writer = new vtkPolyDataWriter();
			        	data_writer.SetFileTypeToASCII();
			        	data_writer.SetFileName(path);
			        	data_writer.SetInputData(main_obj);
			        	data_writer.Write();
		        	} else if(path.contains("vtp")) {
			        	System.out.printf("file: %s\n", path);
			        	vtkXMLPolyDataWriter data_writer = new vtkXMLPolyDataWriter();
			        	data_writer.SetDataModeToAscii();
			        	data_writer.SetCompressorTypeToNone();
			        	data_writer.SetInputData(main_obj);
			        	data_writer.SetFileName(path);
			        	data_writer.Write();
		        	} else if(path.contains("ply")) {
		        		System.out.printf("file: %s\n", path);
		        		vtkPLYWriter data_writer = new vtkPLYWriter();
		        		data_writer.SetDataByteOrderToLittleEndian();
		        		data_writer.SetFileTypeToASCII();
		        		data_writer.SetInputData(main_obj);
		        		data_writer.SetLookupTable(indicatorTagging);
		        		data_writer.SetFileName(path);
		        		data_writer.Write();
		        	}
		        }
			}
		});
		saveDataMenu.setText("&Save ...");
		
		saveMorphMenu = new MenuItem(fileMenu, SWT.PUSH);
		saveMorphMenu.addListener(SWT.Selection, new Listener() {
			
			@Override
			public void handleEvent(Event arg0) {
				
		        FileDialog dialog = new FileDialog(shell, SWT.SAVE);
		        String[] filterNames = new String[] 
		            {"VTK legacy container (*.vtk)", "VTK PolyData (*.vtp)", "All Files (*)"};

		        String[] filterExtensions = new String[] 
		            {"*.vtk", "*.vtp", "*"};

		        dialog.setFilterNames(filterNames);
		        dialog.setFilterExtensions(filterExtensions);
		        dialog.setText("Save Morphology Surface to File");
		        String path = dialog.open();
		        
		        if (path != null) {
		        	if(morph_obj==null) {
		        		return;
		        	}
		        	if(path.contains("vtk")) {
			        	vtkPolyDataWriter data_writer = new vtkPolyDataWriter();
			        	data_writer.SetFileTypeToASCII();
			        	data_writer.SetFileName(path);
			        	data_writer.SetInputData(morph_obj);
			        	data_writer.Write();
		        	} else if(path.contains("vtp")) {
			        	vtkXMLPolyDataWriter data_writer = new vtkXMLPolyDataWriter();
			        	data_writer.SetDataModeToAscii();
			        	data_writer.SetCompressorTypeToNone();
			        	data_writer.SetInputData(morph_obj);
			        	data_writer.SetFileName(path);
			        	data_writer.Write();
		        	}
		        }
			}
		});
		saveMorphMenu.setText("Save Morphology ...");
		saveMorphMenu.setEnabled(false);
		
		loadLineMenu = new MenuItem(fileMenu, SWT.PUSH);
		loadLineMenu.addSelectionListener(new SelectionListener() {
			
			@Override
			public void widgetSelected(SelectionEvent arg0) {
		        FileDialog dialog = new FileDialog(shell, SWT.OPEN | SWT.MULTI);
		        String[] filterNames = new String[] 
		            {"VTK legacy container (*.vtk)", "VTK PolyData (*.vtp)", "All Files (*)"};

		        String[] filterExtensions = new String[] 
		            {"*.vtk", "*.vtp", "*"};

		        dialog.setFilterNames(filterNames);
		        dialog.setFilterExtensions(filterExtensions);
		        dialog.setText("Select VTK line Files to Import");
				//String lastPath = Config.getInstance().getString(Config.LAST_OPEN_TEXT_PATH);
				//if (lastPath != null && !lastPath.isEmpty())
				//	fd.setFileName(lastPath);
		        dialog.setFileName("/media/christian/DATA/VGC2020");
		        //dialog.setFilterPath("/media/christian/DATA/VGC2020/");
		        String path = dialog.open();
				
		        if (path != null) {
		        	String [] fnames = dialog.getFileNames();
		        	for(int i=0; i<fnames.length; i++) {
		        		String fname = fnames[i];
		        		int numpos = fname.lastIndexOf("_");
		        		int dotpos = fname.lastIndexOf(".");
		        		String num_str = fname.substring(numpos+1, dotpos);
		        		
		        		System.out.println(num_str);
		        		int cat_num = Integer.parseInt(num_str);
		        		cat_num = cat_num-1;
		        		
		        		//int idx_num = Integer.parseInt(num_str);
		        		//num_str = fname.substring(0, numpos);
		        		//int catpos = num_str.lastIndexOf("_");
		        		//num_str = fname.substring(catpos+1,numpos);
		        		//int cat_num = Integer.parseInt(num_str);
		        		
		        		String full_fname = dialog.getFilterPath()+"/"+fname;
		        		if(cat_num < num_patches) {
				        	if(path.contains("vtk")) {
					        	System.out.printf("file: %s\n", full_fname);
					        	vtkPolyDataReader data_reader = new vtkPolyDataReader();
					        	data_reader.SetFileName(full_fname);
					        	data_reader.Update();
					        	vtkPolyData line_pData = data_reader.GetOutput();
				        		if(line_select_style!=null)
				        			line_select_style.addLines(cat_num, line_pData);
				        	} else if(path.contains("vtp")) {
					        	System.out.printf("file: %s\n", full_fname);
					        	vtkXMLPolyDataReader data_reader = new vtkXMLPolyDataReader();
					        	data_reader.SetFileName(full_fname);
					        	data_reader.Update();
					        	vtkPolyData line_pData = data_reader.GetOutput();
				        		if(line_select_style!=null)
				        			line_select_style.addLines(cat_num, line_pData);
				        	}
		        		} else {
		        			while(num_patches < cat_num) {
			    				int index = category_listview.getItemCount();
			    				category_listview.add("Patch"+Integer.toString(index));
			    				num_patches++;
		        			}
				        	if(path.contains("vtk")) {
					        	System.out.printf("file: %s\n", full_fname);
					        	vtkPolyDataReader data_reader = new vtkPolyDataReader();
					        	data_reader.SetFileName(full_fname);
					        	data_reader.Update();
					        	vtkPolyData line_pData = data_reader.GetOutput();
				        		if(line_select_style!=null)
				        			line_select_style.addLines(cat_num, line_pData);
				        	} else if(path.contains("vtp")) {
					        	System.out.printf("file: %s\n", full_fname);
					        	vtkXMLPolyDataReader data_reader = new vtkXMLPolyDataReader();
					        	data_reader.SetFileName(full_fname);
					        	data_reader.Update();
					        	vtkPolyData line_pData = data_reader.GetOutput();
				        		if(line_select_style!=null)
				        			line_select_style.addLines(cat_num, line_pData);
				        	}
		        		}
		        	}
		        	//if(line_select_style!=null)
		        	//	line_select_style.revertLineVis();
		        }
			}
			
			@Override
			public void widgetDefaultSelected(SelectionEvent arg0) {
				
			}
		});
		loadLineMenu.setText("Load Lines ...");
		loadLineMenu.setEnabled(false);
		
		saveLineMenu = new MenuItem(fileMenu, SWT.PUSH);
		saveLineMenu.addSelectionListener(new SelectionListener() {
			
			@Override
			public void widgetSelected(SelectionEvent arg0) {
		        FileDialog dialog = new FileDialog(shell, SWT.SAVE);
		        String[] filterNames = new String[] 
		            {"VTK legacy container (*.vtk)", "VTK PolyData (*.vtp)", "All Files (*)"};

		        String[] filterExtensions = new String[] 
		            {"*.vtk", "*.vtp", "*"};

		        dialog.setFilterNames(filterNames);
		        dialog.setFilterExtensions(filterExtensions);
		        dialog.setText("Save VTK Lines to File");
		        String path = dialog.open();
		        
		        if (path != null) {
		        	int dotpos = path.lastIndexOf(".");
		        	String basepath_str = path.substring(0, dotpos);
		        	// ext_str includes "."
		        	String ext_str = path.substring(dotpos,path.length());
		        	
					for(int i=0; i<num_patches; i++) {
						int pnum = i+1;
					//	System.out.println("Working on lines for patch "+Integer.toString(pnum));
						vtkPolyData[] catlines = line_select_style.GetLineSegments(pnum);
						if(catlines!=null) {
					//		System.out.println("Found lines of that category.");
							vtk.vtkAppendPolyData appender = new vtk.vtkAppendPolyData();
							vtkPolyData combinedData;
							if(catlines.length==1) {
								combinedData = catlines[0];
							} else if(catlines.length==0) {
								continue;
							} else {
								for(int j=0; j<catlines.length; j++) {
									appender.SetInputData(j, catlines[j]);
								}
								appender.Update();
								combinedData = appender.GetOutput();
							}
							
							//String fpath = basepath_str+"_"+Integer.toString(pnum)+"_"+Integer.toString(j)+ext_str;
							String fpath = basepath_str+"_"+Integer.toString(pnum)+ext_str;
				//			System.out.printf("file: %s\n", fpath);
				        	if(path.contains("vtk")) {
				//	        	System.out.printf("file: %s\n", fpath);
					        	vtkPolyDataWriter data_writer = new vtkPolyDataWriter();
					        	data_writer.SetFileTypeToASCII();
					        	data_writer.SetFileName(fpath);
					        	data_writer.SetInputData(combinedData);
					        	data_writer.Write();
				        	} else if(path.contains("vtp")) {
				//	        	System.out.printf("file: %s\n", fpath);
					        	vtkXMLPolyDataWriter data_writer = new vtkXMLPolyDataWriter();
					        	data_writer.SetDataModeToAscii();
					        	data_writer.SetCompressorTypeToNone();
					        	data_writer.SetFileName(fpath);
					        	data_writer.SetInputData(combinedData);
					        	data_writer.Write();
				        	}
						}
					}
		        }
			}
			
			@Override
			public void widgetDefaultSelected(SelectionEvent arg0) {
				
			}
		});
		saveLineMenu.setText("Save Lines ...");
		saveLineMenu.setEnabled(false);
		
		MenuItem openTexMenu = new MenuItem(fileMenu, SWT.PUSH);
		openTexMenu.addListener(SWT.Selection, new Listener() {
			
			@Override
			public void handleEvent(Event arg0) {
		        FileDialog dialog = new FileDialog(shell, SWT.OPEN);
		        String[] filterNames = new String[] 
		            {"TIFF files (*.tif)", "PNG file (.png)", "JPEG file (.jpg)", "All Files (*)"};

		        String[] filterExtensions = new String[] 
		            {"*.tif", "*.png", "*.jpg", "*"};

		        dialog.setFilterNames(filterNames);
		        dialog.setFilterExtensions(filterExtensions);
		        dialog.setText("Select Texture Image File");
		        String path = dialog.open();
		        
		        if (path != null) {
		        	System.out.printf("file: %s\n", path);
		        	if(main_tex != null) {
		        		//main_tex.dispose();
		        		main_tex = null;
		        	}
		        	main_tex = new vtkTexture();
		        	if(path.contains("tif")) {
		        		vtkTIFFReader tex_reader = new vtkTIFFReader();
		        		tex_reader.SetFileName(path);
		        		tex_reader.Update();
		        		main_tex.SetInputData(tex_reader.GetOutput());
		        	} else if(path.contains("png")) {
		        		vtkPNGReader tex_reader = new vtkPNGReader();
		        		tex_reader.SetFileName(path);
		        		tex_reader.Update();
		        		main_tex.SetInputData(tex_reader.GetOutput());
		        	} else if(path.contains("jpg")) {
		        		vtkJPEGReader tex_reader = new vtkJPEGReader();
		        		tex_reader.SetFileName(path);
		        		tex_reader.Update();
		        		main_tex.SetInputData(tex_reader.GetOutput());
		        	}
		        	//vtkImageReader2Factory tex_reader_factory = new vtkImageReader2Factory();
		        	//vtkImageReader2 tex_reader = tex_reader_factory.CreateImageReader2(path);
		        	//tex_reader.SetFileName(path);
		        	//tex_reader.Update();
		        	main_tex.Update();
		        }
		        if(main_tex!=null) {
		        	//texturedActor.SetTexture(main_tex);
		        	commonActor.SetTexture(main_tex);
					main_obj.GetPointData().Modified();
					main_obj.GetCellData().Modified();
		        }
				if(swt_component!=null) {
					//ren.Modified();
					//swt_component.getRenderWindowInteractor().Render();

					//swt_component.lock();
					//rw.Render();
	            	//swt_component.GetRenderWindow().Render();
					//swt_component.unlock();
		        }
			}
		});
		openTexMenu.setText("&Attach texture ...");
        
		MenuItem detachTexMenu = new MenuItem(fileMenu, SWT.PUSH);
		detachTexMenu.addListener(SWT.Selection, new Listener() {
			
			@Override
			public void handleEvent(Event arg0) {
				if(main_tex!=null) {
					main_tex.Delete();
					//main_tex.Modified();
					//main_tex.SetColorModeToDirectScalars();
					commonActor.SetTexture(null);
					commonActor.Modified();
				}
				
				main_obj.GetPointData().Modified();
				main_obj.GetCellData().Modified();
				
				if(swt_component!=null) {
				//	swt_component.lock();
				//	rw.Render();
				//	swt_component.GetRenderWindow().Render();
				//	swt_component.unlock();

				//	ren.Modified();
				//	swt_component.getRenderWindowInteractor().Render();
				}
				
				commonActor.SetTexture(null);
				main_tex=null;
			}
		});
		detachTexMenu.setText("&Detach texture ...");
		
		MenuItem cascadeViewMenu = new MenuItem(menu, SWT.CASCADE);
		cascadeViewMenu.setText("&View");
		viewMenu = new Menu(shell, SWT.DROP_DOWN);
		cascadeViewMenu.setMenu(viewMenu);
		
		MenuItem resetViewItem = new MenuItem(viewMenu, SWT.PUSH);
		resetViewItem.setText("Reset vie&w");
		resetViewItem.addSelectionListener(new SelectionListener() {
			
			@Override
			public void widgetSelected(SelectionEvent arg0) {
				if(ren!=null) {
				    ren.ResetCamera();
				    vtkCamera active_cam = ren.GetActiveCamera();
				    active_cam.SetPosition(_cam_pos);
				    active_cam.SetViewUp(_cam_up);
				    active_cam.SetFocalPoint(_cam_foc);
				}

				if(swt_component!=null) {
				//	ren.Modified();
				//	swt_component.getRenderWindowInteractor().Render();

			    //swt_component.lock();
				//ReentrantLock vtk_lock = swt_widget.getVTKLock();
				//vtk_lock.lock();
			    //rw.Render();
	            //swt_component.GetRenderWindow().Render();
			    //swt_component.unlock();
			    //vtk_lock.unlock();
				}
			}
			
			@Override
			public void widgetDefaultSelected(SelectionEvent arg0) {
				
			}
		});

		MenuItem backgroundColourSetupItem = new MenuItem(viewMenu, SWT.PUSH);
		backgroundColourSetupItem.setText("&Background colour");
		backgroundColourSetupItem.addSelectionListener(new SelectionListener() {
			
			@Override
			public void widgetSelected(SelectionEvent arg0) {
				ColorDialog cd = new ColorDialog(shell);
				cd.setText("Color Selection");
				cd.setRGB(new RGB(0, 0, 0));
				RGB newColor = cd.open();
				if (newColor == null) {
					return;
				}
				else
				{
					ren.SetBackground(((float)newColor.red)/255.0f, ((float)newColor.green)/255.0f, ((float)newColor.blue)/255.0f);
					ren.Modified();
					//swt_component.getRenderWindowInteractor().Render();

					//swt_component.lock();
					//ReentrantLock vtk_lock = swt_widget.getVTKLock();
					//vtk_lock.lock();
					//if(rw!=null)
					//	rw.Render();
					//if(ren!=null)
					//	ren.Render();
					//swt_component.unlock();
				    //vtk_lock.unlock();
					//swt_component.Render();
				}
			}
			
			@Override
			public void widgetDefaultSelected(SelectionEvent arg0) {
				
			}
		});
		
		MenuItem screenshotItem = new MenuItem(viewMenu, SWT.PUSH);
		screenshotItem.setText("Take Screenshot");
		screenshotItem.addSelectionListener(new SelectionListener() {
			
			@Override
			public void widgetSelected(SelectionEvent arg0) {
		        FileDialog dialog = new FileDialog(shell, SWT.SAVE);
		        String[] filterNames = new String[] 
		            {"TIFF image (*.tif)", "PNG image (*.png)", "JPEG image (*.jpg)", "GIF image (*.gif)", "All Files (*)"};

		        String[] filterExtensions = new String[] 
		            {"*.tif", "*.png", "*.jpg", "*.gif", "*"};

		        dialog.setText("Save Screenshot");
		        dialog.setFilterNames(filterNames);
		        dialog.setFilterExtensions(filterExtensions);
		        String path = dialog.open();
		        
		        if (path != null) {
					//swt_component.lock();
					//ReentrantLock vtk_lock = swt_widget.getVTKLock();
					//vtk_lock.lock();
		        	
					swt_component.HardCopy(path, 2);
					
					//swt_component.unlock();
				    //vtk_lock.unlock();
		        }
			}
			
			@Override
			public void widgetDefaultSelected(SelectionEvent arg0) {
				
			}
		});
		
		MenuItem cascadeRenderMenu = new MenuItem(menu, SWT.CASCADE);
		cascadeRenderMenu.setText("&Rendering ...");
		renderMenu = new Menu(shell, SWT.DROP_DOWN);
		cascadeRenderMenu.setMenu(renderMenu);
		
		MenuItem switchLightingItem = new MenuItem(renderMenu, SWT.PUSH);
		switchLightingItem.setText("switch Light on/off");
		switchLightingItem.addSelectionListener(new SelectionListener() {
			
			@Override
			public void widgetSelected(SelectionEvent arg0) {
				int numLights = ren.GetLights().GetNumberOfItems();
				if(numLights>0) {
					vtkLight mainLight = ren.GetLights().GetNextItem();
					if(mainLight!=null) {
						if(mainLight.GetSwitch()==0) {
							mainLight.SwitchOn();
							commonActor.GetProperty().LightingOn();
							commonActor.GetProperty().ShadingOn();
							commonActor.GetProperty().SetInterpolationToPhong();
							commonActor.Modified();
						} else {
							mainLight.SwitchOff();
							commonActor.GetProperty().LightingOff();
							commonActor.GetProperty().ShadingOff();
							commonActor.GetProperty().SetInterpolationToFlat();
							commonActor.Modified();
						}
					}
				}
			}
			
			@Override
			public void widgetDefaultSelected(SelectionEvent arg0) {
				
			}
		});
		//switchLightingItem.setEnabled(false);
		
		MenuItem switchNormalItem = new MenuItem(renderMenu, SWT.PUSH);
		switchNormalItem.setText("show Normal Vis on/off");
		switchNormalItem.addSelectionListener(new SelectionListener() {
			
			@Override
			public void widgetSelected(SelectionEvent arg0) {

			}
			
			@Override
			public void widgetDefaultSelected(SelectionEvent arg0) {
				
			}
		});
		//switchNormalItem.setEnabled(false);
		
		selectInterpretMenuItem = new MenuItem(menu, SWT.CASCADE);
		selectInterpretMenuItem.setText("Select/Interpret Surface");
		selectInterpretMenu = new Menu(shell, SWT.DROP_DOWN);
		selectInterpretMenuItem.setMenu(selectInterpretMenu);
		
		MenuItem pointSelectItem = new MenuItem(selectInterpretMenu, SWT.PUSH);
		pointSelectItem.setText("point input");
		pointSelectItem.addSelectionListener(new SelectionListener() {
			
			@Override
			public void widgetSelected(SelectionEvent arg0) {
				active_mode = 1;
				create_point_selection_button_area();
				create_point_selection_context_area();
			}
			
			@Override
			public void widgetDefaultSelected(SelectionEvent arg0) {
				
			}
		});
		
		MenuItem lineSelectItem = new MenuItem(selectInterpretMenu, SWT.PUSH);
		lineSelectItem.setText("line input");
		lineSelectItem.addSelectionListener(new SelectionListener() {
			
			@Override
			public void widgetSelected(SelectionEvent arg0) {
				active_mode = 2;
				create_line_selection_button_area();
				create_line_selection_context_area();
			}
			
			@Override
			public void widgetDefaultSelected(SelectionEvent arg0) {
			}
		});
		
		MenuItem areaSelectItem = new MenuItem(selectInterpretMenu, SWT.PUSH);
		areaSelectItem.setText("area input");
		areaSelectItem.addSelectionListener(new SelectionListener() {
			
			@Override
			public void widgetSelected(SelectionEvent arg0) {
				active_mode = 3;
				create_area_selection_button_area();
				create_area_selection_context_area();
			}
			
			@Override
			public void widgetDefaultSelected(SelectionEvent arg0) {
				
			}
		});

		//MenuItem selectItemSeparator = new MenuItem(selectInterpretMenu, SWT.SEPARATOR);
		new MenuItem(selectInterpretMenu, SWT.SEPARATOR);

		MenuItem cascadeGrowPatchMenu = new MenuItem(selectInterpretMenu, SWT.CASCADE);
		cascadeGrowPatchMenu.setText("grow patch ..");
		Menu growPatchMenu = new Menu(shell, SWT.DROP_DOWN);
		cascadeGrowPatchMenu.setMenu(growPatchMenu);
		
		MenuItem growSelectionItem = new MenuItem(growPatchMenu, SWT.PUSH);
		growSelectionItem.setText("selection");
		growSelectionItem.addSelectionListener(new SelectionListener() {
			
			@Override
			public void widgetSelected(SelectionEvent arg0) {
				global_propagate_refine = false;
				clear_button_area();
				clear_context_area();
				create_propagation_context_area();
			}
			
			@Override
			public void widgetDefaultSelected(SelectionEvent arg0) {
				
			}
		});
		MenuItem growGlobalItem = new MenuItem(growPatchMenu, SWT.PUSH);
		growGlobalItem.setText("global");
		growGlobalItem.addSelectionListener(new SelectionListener() {
			
			@Override
			public void widgetSelected(SelectionEvent arg0) {
				global_propagate_refine = true;
				clear_button_area();
				clear_context_area();
				create_propagation_context_area();
			}
			
			@Override
			public void widgetDefaultSelected(SelectionEvent arg0) {
				
			}
		});

		MenuItem cascadeRefinePatchMenu = new MenuItem(selectInterpretMenu, SWT.CASCADE);
		cascadeRefinePatchMenu.setText("refine patch ..");
		Menu refinePatchMenu = new Menu(shell, SWT.DROP_DOWN);
		cascadeRefinePatchMenu.setMenu(refinePatchMenu);
		
		MenuItem refineSelectionItem = new MenuItem(refinePatchMenu, SWT.PUSH);
		refineSelectionItem.setText("selection");
		refineSelectionItem.addSelectionListener(new SelectionListener() {
			
			@Override
			public void widgetSelected(SelectionEvent arg0) {
				global_propagate_refine = false;
				clear_button_area();
				clear_context_area();
				create_refinement_context_area();
			}
			
			@Override
			public void widgetDefaultSelected(SelectionEvent arg0) {
				
			}
		});
		MenuItem refineGlobalItem = new MenuItem(refinePatchMenu, SWT.PUSH);
		refineGlobalItem.setText("global");
		refineGlobalItem.addSelectionListener(new SelectionListener() {
			
			@Override
			public void widgetSelected(SelectionEvent arg0) {
				global_propagate_refine = true;
				clear_button_area();
				clear_context_area();
				create_refinement_context_area();
			}
			
			@Override
			public void widgetDefaultSelected(SelectionEvent arg0) {
				
			}
		});
		
		MenuItem growNrefineItem = new MenuItem(selectInterpretMenu, SWT.PUSH);
		growNrefineItem.setText("grow'n'refine selection");
		growNrefineItem.addSelectionListener(new SelectionListener() {
			
			@Override
			public void widgetSelected(SelectionEvent arg0) {
				global_propagate_refine = false;
				clear_button_area();
				clear_context_area();
				create_prop_n_refine_context_area();
			}
			
			@Override
			public void widgetDefaultSelected(SelectionEvent arg0) {
				
			}
		});
		selectInterpretMenuItem.setEnabled(false);
		
		
		measureMenuItem = new MenuItem(menu, SWT.CASCADE);
		measureMenuItem.setText("Measure on Surface");
		Menu measureMenu = new Menu(shell, SWT.DROP_DOWN);
		measureMenuItem.setMenu(measureMenu);
		
		welllogItem = new MenuItem(measureMenu, SWT.PUSH);
		welllogItem.setText("Add Well Log");
		welllogItem.addSelectionListener(new SelectionListener() {
			
			@Override
			public void widgetSelected(SelectionEvent arg0) {
				active_mode = 4;
				create_welllog_creator_button_area();
				create_welllog_creator_context_area();
			}
			
			@Override
			public void widgetDefaultSelected(SelectionEvent arg0) {
				
			}
		});
		
		saveWellLogItem = new MenuItem(measureMenu, SWT.PUSH);
		saveWellLogItem.setText("Save Well Logs ...");
		saveWellLogItem.addSelectionListener(new SelectionListener() {
			
			@Override
			public void widgetSelected(SelectionEvent arg0) {
				if(welllog_create_style==null)
					return;
				
		        FileDialog dialog = new FileDialog(shell, SWT.SAVE);
		        String[] filterNames = new String[] 
		            {"VTK legacy container (*.vtk)", "VTK PolyData (*.vtp)", "Petrel (.zip)", "All Files (*)"};

		        String[] filterExtensions = new String[] 
		            {"*.vtk", "*.vtp", "*.zip", "*"};

		        dialog.setFilterNames(filterNames);
		        dialog.setFilterExtensions(filterExtensions);
		        dialog.setText("Save Well Log to File");
		        String path = dialog.open();
		        
		        if (path != null) {
		        	int dotpos = path.lastIndexOf(".");
		        	String basepath_str = path.substring(0, dotpos);
		        	// ext_str includes "."
		        	String ext_str = path.substring(dotpos,path.length());
		        	
		        	if(ext_str.contains("vtk") || ext_str.contains("vtp")) {
		        		vtkPolyData[] wellLogs = welllog_create_style.GetLineSegments();
		        		if(wellLogs!=null) {
							vtk.vtkAppendPolyData appender = new vtk.vtkAppendPolyData();
							vtkPolyData combinedData;
							if(wellLogs.length==1) {
								combinedData = wellLogs[0];
							} else if(wellLogs.length==0) {
								return;
							} else {
								for(int j=0; j<wellLogs.length; j++) {
									// appender.SetInputData(j, wellLogs[j]);
									appender.AddInputData(wellLogs[j]);
								}
								appender.Update();
								combinedData = appender.GetOutput();
							}
				        	if(ext_str.contains("vtk")) {
				        		System.out.printf("file: %s\n", path);
					        	vtkPolyDataWriter data_writer = new vtkPolyDataWriter();
					        	data_writer.SetFileTypeToASCII();
					        	data_writer.SetFileName(path);
					        	data_writer.SetInputData(combinedData);
					        	data_writer.Write();
				        	} else if(ext_str.contains("vtp")) {
				        		System.out.printf("file: %s\n", path);
					        	vtkXMLPolyDataWriter data_writer = new vtkXMLPolyDataWriter();
					        	data_writer.SetDataModeToAscii();
					        	data_writer.SetCompressorTypeToNone();
					        	data_writer.SetFileName(path);
					        	data_writer.SetInputData(combinedData);
					        	data_writer.Write();
				        	}
		        		}
		        	} else if(ext_str.contains("zip")) {
		        		PetrelWellLogFormatter formatter = new PetrelWellLogFormatter(welllog_create_style.GetLineOrderedPoints(), welllog_create_style.GetLineOrderedIndicators(), wellLogNames);
		        		formatter.SetBasepath(basepath_str);
		        		formatter.SetExtensionString(ext_str);
		        		formatter.write();
		        	}
		        }
			}
			
			@Override
			public void widgetDefaultSelected(SelectionEvent arg0) {
				
			}
		});
		measureMenuItem.setEnabled(false);
		
		
		MenuItem informationMenuItem = new MenuItem(menu, SWT.CASCADE);
		informationMenuItem.setText("Help");
		Menu informationMenu = new Menu(shell, SWT.DROP_DOWN);
		informationMenuItem.setMenu(informationMenu);
		
		MenuItem aboutItem = new MenuItem(informationMenu, SWT.PUSH);
		aboutItem.setText("About Interpretation Studio");
		aboutItem.addSelectionListener(new SelectionListener() {
			
			@Override
			public void widgetSelected(SelectionEvent arg0) {
				MessageBox mBox = new MessageBox(shell, SWT.ICON_INFORMATION | SWT.OK);
				mBox.setText("About Interpretation Studio");
				mBox.setMessage("Copyright 2018-2020 by Dr. Christian Kehl\nAll rights reserved");
				mBox.open();
			}
			
			@Override
			public void widgetDefaultSelected(SelectionEvent arg0) {
				
			}
		});
	
		
		
		
		if(shell!=null)
			shell.setMenuBar(menu);
	}
	
	protected void disposeMenu() {
		if(selectInterpretMenu!=null) {
			selectInterpretMenu.dispose();
			selectInterpretMenu = null;
		}
		if(renderMenu!=null) {
			renderMenu.dispose();
			renderMenu = null;
		}
		if(viewMenu!=null) {
			viewMenu.dispose();
			viewMenu = null;
		}
		if(fileMenu!=null) {
			fileMenu.dispose();
			fileMenu = null;
		}
		if(menu!=null) {
			menu.dispose();
			menu = null;
		}
	}
	
	
	/**
	 * Create surface forms
	 */
	protected void createForms()
	{
		if(bottom!=null)
		{
			bottom.dispose();
			bottom=null;
		}
		if(top!=null)
		{
			top.dispose();
			top=null;
		}
		if(form!=null)
		{
			form.dispose();
			form=null;
		}
		
		form = new SashForm(shell, SWT.VERTICAL);
		//form.setLayout(new GridLayout(1, false));
		form.setLayout(new FillLayout());
		form.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, true));
		form.setSize(800,640);
		

		top = new SashForm(form, SWT.HORIZONTAL);
		GridLayout top_layout = new GridLayout(2, false);
		top_layout.marginHeight = top_layout.marginWidth = 0;
		top.setLayout(top_layout);
		top.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, true));


		//bottom = new SashForm(form, SWT.HORIZONTAL);
		//GridLayout bottom_layout = new GridLayout(2, false);
		//bottom_layout.marginHeight = bottom_layout.marginWidth = 0;
		//bottom.setLayout(bottom_layout);
		//bottom.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, true));
		
		create_option_section();
		create_3D();
		create_listview();
		
		top.setWeights(new int[] {9,1});
		form.setWeights(new int[]{9,2});
		shell.layout();
	}
	
	protected void disposeForms() {
		dispose_option_section();
		dispose_3D();
		
		if(bottom!=null) {
			bottom.dispose();
			bottom = null;
		}
		if(top!=null) {
			top.dispose();
			top = null;
		}
	}
	
	protected void create_option_section() {
		//bottom.dispose();
		bottom = new SashForm(form, SWT.HORIZONTAL);
		GridLayout bottom_layout = new GridLayout(3, false);
		bottom_layout.marginHeight = bottom_layout.marginWidth = 0;
		bottom.setLayout(bottom_layout);
		bottom.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, true));
		
		create_button_options(bottom);
		create_empty_context_options(bottom);
		create_empty_linked_view(bottom);
		
		bottom.setWeights(new int[] {3,5,2});
		
		bottom.layout();
	}
	
	protected void dispose_option_section() {
		dispose_linked_view();
		dispose_context_options();
		dispose_button_options();
	}
	
	protected void create_button_options(Composite parent) {
		buttonBar = new ScrolledComposite(parent, SWT.V_SCROLL | SWT.H_SCROLL);
		
		//ScrolledComposite buttonBar = new ScrolledComposite(parent, SWT.V_SCROLL);
		//FillLayout buttonBarLayout = new FillLayout(SWT.BORDER);
		//buttonBarLayout.type = SWT.VERTICAL;
		//buttonBar.setLayout(buttonBarLayout);
		
		//Composite buttonBar = new Composite(parent, SWT.PUSH);
		//FillLayout buttonBarLayout = new FillLayout(SWT.BORDER);
		//buttonBarLayout.type = SWT.VERTICAL;
		//buttonBar.setLayout(buttonBarLayout);
		
		buttonContainer = new Composite(buttonBar, SWT.PUSH | SWT.BORDER);
		//RowLayout buttonContainerLayout = new RowLayout();
		FillLayout buttonContainerLayout = new FillLayout();
		buttonContainerLayout.type = SWT.VERTICAL;
		//buttonContainerLayout.wrap = true;
		//buttonContainerLayout.pack = true;
		//buttonContainerLayout.justify = false;
		buttonContainerLayout.spacing = 5;
		buttonContainer.setLayout(buttonContainerLayout);
		
		/*
		GridLayout buttonContainerLayout = new GridLayout();
		buttonContainerLayout.numColumns = 2;
		buttonContainerLayout.makeColumnsEqualWidth = true;
		GridData buttonContainerLayoutData = new GridData();
		buttonContainerLayoutData.horizontalAlignment = SWT.FILL;
		buttonContainerLayoutData.grabExcessHorizontalSpace = true;
		buttonContainer.setLayout(buttonContainerLayout);
		*/
		
		/**
		 * TODO: possibly fill with ghost container
		 */
		buttonBar.setMinSize(buttonContainer.computeSize(SWT.DEFAULT, SWT.DEFAULT));
		buttonBar.setContent(buttonContainer);
		buttonBar.setExpandVertical(true);
		buttonBar.setExpandHorizontal(true);

		buttonContainer.layout();
		buttonBar.layout();
	}
	
	protected void dispose_button_options() {
		switch(active_mode) {
		case 0: {
			dispose_point_selection_button_area();
			dispose_line_selection_button_area();
			dispose_area_selection_button_area();
			dispose_welllog_creator_button_area();
			break;
		}
		case 1: {
			dispose_point_selection_button_area();
			break;
		}
		case 2: {
			dispose_line_selection_button_area();
			break;
		}
		case 3: {
			dispose_area_selection_button_area();
			break;
		}
		case 4: {
			dispose_welllog_creator_button_area();
			break;
		}
		default: {
			break;
		}
		}
		
		if(buttonContainer!=null) {
			buttonContainer.dispose();
			buttonContainer=null;
		}
	}
	
	protected void create_empty_context_options(Composite parent) {
		contextBar = new ScrolledComposite(parent, SWT.V_SCROLL | SWT.H_SCROLL);
		
		contextContainer = new Composite(contextBar, SWT.PUSH | SWT.BORDER);
		/*
		RowLayout contextContainerLayout = new RowLayout();
		contextContainerLayout.type = SWT.HORIZONTAL;
		contextContainerLayout.wrap = true;
		contextContainerLayout.pack = true;
		contextContainer.setLayout(contextContainerLayout);
		contextContainer.setLayoutData(new RowData(300, 35));
		*/
		GridLayout contextContainerLayout = new GridLayout(2, false);
		contextContainerLayout.numColumns = 2;
		contextContainer.setLayout(contextContainerLayout);

		contextBar.setMinSize(contextContainer.computeSize(SWT.DEFAULT, SWT.DEFAULT));
		contextBar.setContent(contextContainer);
		contextBar.setExpandVertical(true);
		contextBar.setExpandHorizontal(true);

		contextContainer.layout();
		contextBar.layout();
	}
	
	protected void dispose_context_options() {
		switch(active_mode) {
		case 0: {
			dispose_point_selection_context_area();
			dispose_line_selection_context_area();
			dispose_area_selection_context_area();
			dispose_welllog_creator_context_area();
			break;
		}
		case 1: {
			dispose_point_selection_context_area();
			break;
		}
		case 2: {
			dispose_line_selection_context_area();
			break;
		}
		case 3: {
			dispose_area_selection_context_area();
			break;
		}
		case 4: {
			dispose_welllog_creator_context_area();
			break;
		}
		default: {
			break;
		}
		}
		
		if(contextContainer!=null) {
			contextContainer.dispose();
			contextContainer = null;
		}
	}
	
	protected void create_empty_linked_view(Composite parent) {
		SashForm linkedViewComposite = new SashForm(parent, SWT.VERTICAL);
		
		Composite linkedViewbuttonArea = new Composite(linkedViewComposite, SWT.PUSH | SWT.BORDER);
		Layout linkedViewButtonAreaLayout = new FillLayout();
		GridData linkedViewButtonAreaLayoutData = new GridData();
		linkedViewButtonAreaLayoutData.horizontalAlignment = SWT.LEFT;
		linkedViewButtonAreaLayoutData.verticalAlignment = SWT.TOP;
		linkedViewButtonAreaLayoutData.horizontalSpan = 1;
		linkedViewButtonAreaLayoutData.verticalSpan = 1;
		linkedViewbuttonArea.setLayout(linkedViewButtonAreaLayout);
		linkedViewbuttonArea.setLayoutData(linkedViewButtonAreaLayoutData);
		linkedViewbuttonArea.layout();
		
		Composite renderComposite = new Composite(linkedViewComposite, SWT.EMBEDDED);
		java.awt.Frame linkedView_frame = SWT_AWT.new_Frame(renderComposite);
		linkedView = new vtkCanvas();
		linkedView_frame.add(linkedView);
		
		//Composite renderComposite = new Composite(linkedViewComposite, SWT.PUSH | SWT.BORDER);
		//linkedView_widget = new vtkSwtComponent(new vtkRenderWindow(), renderComposite);
		
		linkedViewComposite.setWeights(new int[]{2,9});
	}
	
	protected void dispose_linked_view() {
		switch(active_mode) {
		case 0: {
			break;
		}
		case 1: {
			break;
		}
		case 2: {
			break;
		}
		case 3: {
			break;
		}
		case 4: {
			break;
		}
		default: {
			break;
		}
		}
		
		if(linkedView!=null)
			linkedView = null;
		//if(linkedView_widget!=null)
		//	linkedView_widget = null;
	}
	
	protected void create_3D() {
		if(top==null)
			System.out.println("top container empty.");
		//Composite composite = new Composite(top, SWT.PUSH | SWT.BORDER);
		//swt_widget = new vtkSwtComponent(new vtkRenderWindow(), composite);
		//rw = swt_widget.getRenderWindow();
		//ren = swt_widget.getRenderer();
		Composite composite = new Composite(top, SWT.EMBEDDED);
		swt_awt_frame = SWT_AWT.new_Frame(composite);
		//rw = new vtk.vtkRenderWindow();
		//swt_component = new vtkCanvas(rw);
		//swt_component = new vtkCanvas();
		swt_component = new SimpleVTKcanvas();
		swt_awt_frame.add(swt_component);
		rw = swt_component.GetRenderWindow();
		ren = swt_component.GetRenderer();
		
		//swt_awt_frame.setSize(320, 240);
		//rw.SetSize(320, 240);
		
		swt_awt_frame.setSize(800, 640);
		//swt_component.setSize(800, 640);
		//swt_widget.setSize(800,  640);
		cam = ren.GetActiveCamera();
		
		mstyle = new vtkInteractorStyleTrackballCamera();
		mstyle.SetDefaultRenderer(swt_component.GetRenderer());
		//mstyle.SetDefaultRenderer(swt_widget.getRenderer());
		mstyle.SetInteractor(swt_component.getRenderWindowInteractor());
		//mstyle.SetInteractor(swt_widget.getRenderWindowInteractor());
		swt_component.setInteractorStyle(mstyle);
		//swt_widget.setInteractorStyle(mstyle);
		/*
		pstyle = new MousePickInteractor();
		pstyle.SetDefaultRenderer(swt_component.GetRenderer());
		pstyle.SetInteractor(swt_component.getRenderWindowInteractor());
		pstyle.register(this);
		*/

		area_select_style.SetDefaultRenderer(swt_component.GetRenderer());
		area_select_style.SetInteractor(swt_component.getRenderWindowInteractor());
		//area_select_style.SetDefaultRenderer(swt_widget.getRenderer());
		//area_select_style.SetInteractor(swt_widget.getRenderWindowInteractor());
		area_select_style.register(this);

		line_select_style.SetDefaultRenderer(swt_component.GetRenderer());
		line_select_style.SetInteractor(swt_component.getRenderWindowInteractor());
		//line_select_style.SetDefaultRenderer(swt_widget.getRenderer());
		//line_select_style.SetInteractor(swt_widget.getRenderWindowInteractor());
		line_select_style.register(this);

		point_select_style.SetDefaultRenderer(swt_component.GetRenderer());
		point_select_style.SetInteractor(swt_component.getRenderWindowInteractor());
		//point_select_style.SetDefaultRenderer(swt_widget.getRenderer());
		//point_select_style.SetInteractor(swt_widget.getRenderWindowInteractor());
		point_select_style.register(this);
		
		welllog_create_style.SetDefaultRenderer(swt_component.GetRenderer());
		welllog_create_style.SetInteractor(swt_component.getRenderWindowInteractor());
		//welllog_create_style.SetDefaultRenderer(swt_widget.getRenderer());
		//welllog_create_style.SetInteractor(swt_widget.getRenderWindowInteractor());
		//welllog_create_style.register(this);
		welllog_create_style.registerDataInterface(this);
		welllog_create_style.registerSamplingInterface(this);
		welllog_create_style.registerNameUpdate(this);

		
		// DEPRECATED - DONE BY UI AND BUTTONS
		//swt_component.getRenderWindowInteractor().AddObserver("KeyPressEvent", this, "keyDownEvent");
		
		//top.setWeights(new int[] {9,1});
		//form.setWeights(new int[]{9,2});
		//shell.layout();
	}
	
	protected void dispose_3D() {
		if(swt_component!=null)
			swt_component = null;
		//if(swt_widget!=null)
		//	swt_widget = null;
	}
	
	protected void create_listview() {
		if(top==null)
			System.out.println("top container empty.");
		SashForm listContainer = new SashForm(top, SWT.PUSH|SWT.BORDER|SWT.VERTICAL);
		listContainer.layout();
		
		category_listview = new org.eclipse.swt.widgets.List(listContainer, SWT.V_SCROLL);
		category_listview.addSelectionListener(new SelectionListener() {
			
			@Override
			public void widgetSelected(SelectionEvent arg0) {
				
				int index = category_listview.getSelectionIndex();
				active_category_id = index+1;
		        if(area_select_style!=null)
		        	area_select_style.SetActiveCategoryNumber(active_category_id);
		        if(line_select_style!=null)
		        	line_select_style.SetActiveCategoryNumber(active_category_id);
		        if(point_select_style!=null)
		        	point_select_style.SetActiveCategoryNumber(active_category_id);
			}
			
			@Override
			public void widgetDefaultSelected(SelectionEvent arg0) {
				
				
			}
		});
		
		listEditContainer = new Composite(listContainer, SWT.PUSH|SWT.BORDER);
		RowLayout listEditContainerLayout = new RowLayout();
		listEditContainerLayout.wrap = true;
		listEditContainerLayout.pack = true;
		listEditContainerLayout.justify = false;
		listEditContainerLayout.marginLeft = 5;
		listEditContainerLayout.marginTop = 5;
		listEditContainerLayout.marginRight = 5;
		listEditContainerLayout.marginBottom = 5;
		listEditContainerLayout.spacing = 0;
		listEditContainer.setLayout(listEditContainerLayout);
		listContainer.layout();

		Button editCategoryNameButton = new Button(listEditContainer, SWT.PUSH|SWT.BORDER);
		editCategoryNameButton.setText("Edit");
		editCategoryNameButton.addSelectionListener(new SelectionListener() {
			@Override
			public void widgetSelected(SelectionEvent arg0) {
				String name = category_listview.getItem(active_category_id-1);
				InputDialog nameChange = new InputDialog(shell);
				nameChange.setText("Change category name");
				nameChange.setMessage("Category name: ");
				nameChange.setInput(name);
				String newName = nameChange.open();
				if(newName!=null) {
					category_listview.setItem(active_category_id-1, newName);
				}
				category_listview.deselectAll();
			}
			
			@Override
			public void widgetDefaultSelected(SelectionEvent arg0) { }
		});
		
		Composite listButtonContainer = new Composite(listContainer, SWT.PUSH|SWT.BORDER);
		RowLayout listButtonContainerLayout = new RowLayout();
		listButtonContainerLayout.wrap = true;
		listButtonContainerLayout.pack = true;
		listButtonContainerLayout.justify = false;
		listButtonContainerLayout.marginLeft = 5;
		listButtonContainerLayout.marginTop = 5;
		listButtonContainerLayout.marginRight = 5;
		listButtonContainerLayout.marginBottom = 5;
		listButtonContainerLayout.spacing = 0;
		listButtonContainer.setLayout(listButtonContainerLayout);
		
		addCategoryButton = new Button(listButtonContainer, SWT.PUSH);
		addCategoryButton.setText("Add Patch");
		addCategoryButton.addSelectionListener(new SelectionListener() {
			@Override
			public void widgetSelected(SelectionEvent arg0) {
				
				int index = category_listview.getItemCount();
				category_listview.add("Patch"+Integer.toString(index));
				num_patches++;
			}
			
			@Override
			public void widgetDefaultSelected(SelectionEvent arg0) {
				
				
			}
		});
		
		remLastCategoryButton = new Button(listButtonContainer, SWT.PUSH);
		remLastCategoryButton.setText("Remove Last Patch");
		remLastCategoryButton.addSelectionListener(new SelectionListener() {
			@Override
			public void widgetSelected(SelectionEvent arg0) {
				
				int index = category_listview.getItemCount()-1;
				category_listview.remove(index);
				num_patches--;
			}
			
			@Override
			public void widgetDefaultSelected(SelectionEvent arg0) {
				
				
			}
		});
		
		listContainer.setWeights(new int[] {10,2,2});
	}
	
	protected void dispose_listview() {
		
	}
	
	protected void toggle_selection() {
		if(area_select_style.GetEnabled()==0) {
			enable_selection();
		} else {
			disable_selection();
		}
	}
	
	protected void disable_selection() {
		area_select_style.Disable();
		mstyle.SetInteractor(swt_component.getRenderWindowInteractor());
		swt_component.setInteractorStyle(mstyle);
		//mstyle.SetInteractor(swt_widget.getRenderWindowInteractor());
		//swt_widget.setInteractorStyle(mstyle);
	}
	
	protected void enable_selection() {
		area_select_style.SetInteractor(swt_component.getRenderWindowInteractor());
		swt_component.setInteractorStyle(area_select_style);
		//area_select_style.SetInteractor(swt_widget.getRenderWindowInteractor());
		//swt_widget.setInteractorStyle(area_select_style);
		area_select_style.Enable();
	}
	
	protected void clear_button_area() {
		dispose_area_selection_button_area();
		dispose_line_selection_button_area();
		dispose_point_selection_button_area();
		dispose_welllog_creator_button_area();
	}
	
	protected void clear_context_area() {
		dispose_area_selection_context_area();
		dispose_line_selection_context_area();
		dispose_point_selection_context_area();
		dispose_area_selection_context_area();
	}
	
	protected void create_propagation_context_area() {
		Label lbl_propagate_method = new Label(contextContainer, SWT.PUSH);
		lbl_propagate_method.setText("Propagation Method: ");
		Combo cb_propagate_method = new Combo(contextContainer, SWT.PUSH);
		String cb_prop_method_description[] = {"Local Shape Categories", "Major-Coverage Shape Cat.", "Extremal Shape Boundaries"};
		cb_propagate_method.setItems(cb_prop_method_description);
		cb_propagate_method.select(cb_propagation_method_index);
		cb_propagate_method.addSelectionListener(new SelectionListener() {
			
			@Override
			public void widgetSelected(SelectionEvent arg0) {
				
				cb_propagation_method_index = cb_propagate_method.getSelectionIndex();
			}
			
			@Override
			public void widgetDefaultSelected(SelectionEvent arg0) {
				
				
			}
		});
		
		Label lbl_propagate_struct_elem_mode = new Label(contextContainer, SWT.PUSH);
		lbl_propagate_struct_elem_mode.setText("Estimation Mode Radial Constraint: ");
		Combo cb_propagate_struct_elem_mode = new Combo(contextContainer, SWT.PUSH);
		String cb_prop_struct_elem_mode_description[] = {"1-Ring","Automatic Estimation", "Manual"};
		cb_propagate_struct_elem_mode.setItems(cb_prop_struct_elem_mode_description);
		cb_propagate_struct_elem_mode.select(cb_propagation_struct_elem_radius_metric_index);
		cb_propagate_struct_elem_mode.addSelectionListener(new SelectionListener() {
			
			@Override
			public void widgetSelected(SelectionEvent arg0) {
				
				cb_propagation_struct_elem_radius_metric_index = cb_propagate_struct_elem_mode.getSelectionIndex();
				if(cb_propagation_struct_elem_radius_metric_index==2) {
					if(edt_propagate_struct_elem_size!=null) {
						edt_propagate_struct_elem_size.setEnabled(true);
					}
				} else {
					if(edt_propagate_struct_elem_size!=null) {
						edt_propagate_struct_elem_size.setEnabled(false);
					}
				}
			}
			
			@Override
			public void widgetDefaultSelected(SelectionEvent arg0) {
				
				
			}
		});
		
		Label lbl_propagate_struct_elem_size = new Label(contextContainer, SWT.PUSH);
		lbl_propagate_struct_elem_size.setText("Radial Constraint Size: ");
		edt_propagate_struct_elem_size = new Text(contextContainer, SWT.PUSH | SWT.BORDER);
		edt_propagate_struct_elem_size.setText(Double.toString(cb_propagation_struct_elem_radius));
		if(cb_propagation_struct_elem_radius_metric_index!=2) {
			edt_propagate_struct_elem_size.setEnabled(false);
		}
		edt_propagate_struct_elem_size.setEditable(true);
		edt_propagate_struct_elem_size.addKeyListener(new KeyListener() {
			
			@Override
			public void keyReleased(KeyEvent arg0) {
				
				if((arg0.keyCode == SWT.CR) || (arg0.keyCode == SWT.KEYPAD_CR)) {
					cb_propagation_struct_elem_radius = Double.parseDouble(edt_propagate_struct_elem_size.getText());
				}
			}
			
			@Override
			public void keyPressed(KeyEvent arg0) {
				
				
			}
		});
		
		Button btn_propagation_compute = new Button(contextContainer, SWT.PUSH);
		btn_propagation_compute.setText("Compute");
		btn_propagation_compute.addSelectionListener(new SelectionListener() {
			
			@Override
			public void widgetSelected(SelectionEvent arg0) {
				
				// ====== First stash the initial patch config ====== //
				if(init_patch_obj==null) {
					init_patch_obj = new vtkPolyData();
					init_patch_obj.CopyStructure(main_obj);
			        int numVerts = main_obj.GetNumberOfPoints();
			        boolean has_vertex_indicators = (init_patch_obj.GetPointData().HasArray("FlagV")!=0 ? true : false);
			        boolean has_cell_indicators = (init_patch_obj.GetPointData().HasArray("FlagT")!=0 ? true : false);
			        if(!has_vertex_indicators) {
				        vtkShortArray vertex_flags = new vtkShortArray();
				        vertex_flags.SetName("FlagV");
				        vertex_flags.SetNumberOfComponents(1);
				        vertex_flags.SetNumberOfTuples(numVerts);
				        init_patch_obj.GetPointData().AddArray(vertex_flags);
			        }
			        vtkShortArray dst_v_flags = new vtkShortArray(init_patch_obj.GetPointData().GetArray("FlagV").GetVTKId());
			        vtkShortArray src_v_flags = new vtkShortArray(main_obj.GetPointData().GetArray("FlagV").GetVTKId());
			        for(int i=0; i<numVerts; i++) {
			        	dst_v_flags.SetValue(i, src_v_flags.GetValue(i));
			        }
			        int numCells = main_obj.GetNumberOfCells();
			        if(!has_cell_indicators) {
				        vtkShortArray surface_flags = new vtkShortArray();
				        surface_flags.SetName("FlagT");
				        surface_flags.SetNumberOfComponents(1);
				        surface_flags.SetNumberOfTuples(numCells);
				        init_patch_obj.GetCellData().AddArray(surface_flags);
			        }
			        vtkShortArray dst_f_flags = new vtkShortArray(init_patch_obj.GetCellData().GetArray("FlagT").GetVTKId());
			        vtkShortArray src_f_flags = new vtkShortArray(main_obj.GetCellData().GetArray("FlagT").GetVTKId());
			        for(int i=0; i<numCells; i++) {
			        	dst_f_flags.SetValue(i, src_f_flags.GetValue(i));
			        }
				}
		        // ====== Second, expand the curve depending on the recent mode off patch addition ====== //
		        
		        if(global_propagate_refine) {
					if(mesh_obj_cgel==null)
						mesh_obj_cgel = new Mesh(main_obj);
					Propagation cgel_expand = new Propagation();
					cgel_expand.setBaseMesh(mesh_obj_cgel);
					switch(cb_propagation_struct_elem_radius_metric_index) {
					case typedefs.AUTO_1RING: {
						cgel_expand.use_1RingDistance();
						break;
					}
					case typedefs.AUTO_DISTANCE_CONSTRAINT: {
						cgel_expand.use_AutoDistanceCompute();
						break;
					}
					case typedefs.MANUAL_DISTANCE_CONSTRAINT: {
						cgel_expand.use_ManualDistance();
						cgel_expand.setStructuringElementSize_manually(cb_propagation_struct_elem_radius);
						break;
					}
					default: {
						return;
					}
					}
					switch(cb_propagation_method_index) {
						case 0: {
							cgel_expand.evolveByLocalShapeCategory();
							break;
						}
						case 1: {
							cgel_expand.evolveByShapeCategory();
							break;
						}
						case 2: {
							cgel_expand.evolveByCurvatureConstraint();
							break;
						}
						default: {
							return;
						}
					}
					cgel_expand.disableGeometrySmoothing();
					cgel_expand.enablePostprocessing();
					for(int i=0; i<num_patches; i++) {
						vtkPolyData[] catlines = line_select_style.GetLineSegments(i+1);
						if(catlines!=null) {
							//System.out.println("Found Line Segments.");
							for(int j=0; j<catlines.length; j++) {
								vtkPolyData lseg = catlines[j];
								LineSegment cgel_lseg = new LineSegment(lseg);
								cgel_expand.addLineSegment(cgel_lseg);
								cgel_lseg.dispose();
							}
						}
						
						mesh_obj_cgel.updateByMaskingIndicator(main_obj, "FlagT", i+1);
						mesh_obj_cgel.updateByMaskingIndicator(main_obj, "FlagV", i+1);
						cgel_expand.compute();
						mesh_obj_cgel.updateByUnmaskingIndicator(main_obj, "FlagT", i+1);
						mesh_obj_cgel.updateByUnmaskingIndicator(main_obj, "FlagV", i+1);
						if(catlines!=null) {
							cgel_expand.clearLineSegments();
						}
					}

			        
			        boolean has_shpcat = (main_obj.GetPointData().HasArray("ShapeClass")!=0 ? true : false);
			        if(!has_shpcat) {
				        vtkShortArray vertex_shpcat = new vtkShortArray();
				        vertex_shpcat.SetName("ShapeClass");
				        vertex_shpcat.SetNumberOfComponents(1);
				        vertex_shpcat.SetNumberOfTuples(main_obj.GetNumberOfPoints());
				        main_obj.GetPointData().AddArray(vertex_shpcat);
				        for(int i=0; i<main_obj.GetNumberOfPoints(); i++) {
				        	vertex_shpcat.SetValue(i, 0);
				        }
			        }
			        // update the shape cat field from CGEL to VTK
			        lbl_propagate_struct_elem_size.setText(Double.toString(cgel_expand.getStructuringElementSize()));
					mesh_obj_cgel.updateVTKattribute(main_obj, "ShapeClass");
					
					//mesh_obj_cgel.dispose();
					//mesh_obj_cgel=null;
					cgel_expand.dispose();
					cgel_expand=null;
		        } else {
					//Mesh cgel_mesh = Mesh.from_vtkPolyData(main_obj);
					if(mesh_obj_cgel==null)
						mesh_obj_cgel = new Mesh(main_obj);
					else {
						mesh_obj_cgel.updateByMaskingIndicator(main_obj, "FlagT", active_category_id);
						mesh_obj_cgel.updateByMaskingIndicator(main_obj, "FlagV", active_category_id);
					}
					Propagation cgel_expand = new Propagation();
					cgel_expand.setBaseMesh(mesh_obj_cgel);
					switch(cb_propagation_struct_elem_radius_metric_index) {
					case typedefs.AUTO_1RING: {
						cgel_expand.use_1RingDistance();
						break;
					}
					case typedefs.AUTO_DISTANCE_CONSTRAINT: {
						cgel_expand.use_AutoDistanceCompute();
						break;
					}
					case typedefs.MANUAL_DISTANCE_CONSTRAINT: {
						cgel_expand.use_ManualDistance();
						cgel_expand.setStructuringElementSize_manually(cb_propagation_struct_elem_radius);
						break;
					}
					default: {
						return;
					}
					}
					switch(cb_propagation_method_index) {
						case 0: {
							cgel_expand.evolveByLocalShapeCategory();
							break;
						}
						case 1: {
							cgel_expand.evolveByShapeCategory();
							break;
						}
						case 2: {
							cgel_expand.evolveByCurvatureConstraint();
							break;
						}
						default: {
							return;
						}
					}
					cgel_expand.disableGeometrySmoothing();
					cgel_expand.enablePostprocessing();
					vtkPolyData[] catlines = line_select_style.GetLineSegments(active_category_id);
					if(catlines!=null) {
						//System.out.println("Found Line Segments.");
						for(int j=0; j<catlines.length; j++) {
							vtkPolyData lseg = catlines[j];
							LineSegment cgel_lseg = new LineSegment(lseg);
							cgel_expand.addLineSegment(cgel_lseg);
							cgel_lseg.dispose();
						}
					}
					cgel_expand.compute();
					//cgel_mesh.update_vtkPolyData(main_obj);
					mesh_obj_cgel.updateByUnmaskingIndicator(main_obj, "FlagT", active_category_id);
					mesh_obj_cgel.updateByUnmaskingIndicator(main_obj, "FlagV", active_category_id);
					if(catlines!=null) {
						cgel_expand.clearLineSegments();
					}
					
			        boolean has_shpcat = (main_obj.GetPointData().HasArray("ShapeClass")!=0 ? true : false);
			        if(!has_shpcat) {
				        vtkShortArray vertex_shpcat = new vtkShortArray();
				        vertex_shpcat.SetName("ShapeClass");
				        vertex_shpcat.SetNumberOfComponents(1);
				        vertex_shpcat.SetNumberOfTuples(main_obj.GetNumberOfPoints());
				        main_obj.GetPointData().AddArray(vertex_shpcat);
				        for(int i=0; i<main_obj.GetNumberOfPoints(); i++) {
				        	vertex_shpcat.SetValue(i, 0);
				        }
			        }
			        // update the shape cat field from CGEL to VTK
			        lbl_propagate_struct_elem_size.setText(Double.toString(cgel_expand.getStructuringElementSize()));
					mesh_obj_cgel.updateVTKattribute(main_obj, "ShapeClass");
					
					//mesh_obj_cgel.dispose();
					//mesh_obj_cgel=null;
					cgel_expand.dispose();
					cgel_expand=null;
		        }
				
				main_obj.GetPointData().Modified();
				main_obj.GetCellData().Modified();
				
				//swt_component.lock();
				//rw.Render();
				//swt_component.unlock();
		        
		        dispose_propagation_context_area();
			}
			
			@Override
			public void widgetDefaultSelected(SelectionEvent arg0) {
				
				
			}
		});
		
		contextBar.setMinSize(contextContainer.computeSize(SWT.DEFAULT, SWT.DEFAULT));
		contextContainer.layout();
	}
	
	protected void dispose_propagation_context_area() {
		if((!contextContainer.isDisposed()) && (contextContainer!=null)) {
			for(Control cnt_elem : contextContainer.getChildren()) {
				cnt_elem.dispose();
				cnt_elem = null;
			}
			contextBar.setMinSize(contextContainer.computeSize(SWT.DEFAULT, SWT.DEFAULT));
			contextContainer.layout();
		}
	}
	
	protected void create_prop_n_refine_context_area() {
		
		Label lbl_majcov_shpcat = new Label(contextContainer, SWT.PUSH);
		lbl_majcov_shpcat.setText("major shape categories; circumcircle refinement: ");
		Button btn_majcov_shpcat = new Button(contextContainer, SWT.PUSH);
		btn_majcov_shpcat.setText("compute");
		btn_majcov_shpcat.addSelectionListener(new SelectionListener() {
			
			@Override
			public void widgetSelected(SelectionEvent arg0) {
				
				// ====== First stash the initial patch config ====== //
				if(init_patch_obj==null) {
					init_patch_obj = new vtkPolyData();
					init_patch_obj.CopyStructure(main_obj);
			        int numVerts = main_obj.GetNumberOfPoints();
			        boolean has_vertex_indicators = (init_patch_obj.GetPointData().HasArray("FlagV")!=0 ? true : false);
			        boolean has_cell_indicators = (init_patch_obj.GetPointData().HasArray("FlagT")!=0 ? true : false);
			        if(!has_vertex_indicators) {
				        vtkShortArray vertex_flags = new vtkShortArray();
				        vertex_flags.SetName("FlagV");
				        vertex_flags.SetNumberOfComponents(1);
				        vertex_flags.SetNumberOfTuples(numVerts);
				        init_patch_obj.GetPointData().AddArray(vertex_flags);
			        }
			        vtkShortArray dst_v_flags = new vtkShortArray(init_patch_obj.GetPointData().GetArray("FlagV").GetVTKId());
			        vtkShortArray src_v_flags = new vtkShortArray(main_obj.GetPointData().GetArray("FlagV").GetVTKId());
			        for(int i=0; i<numVerts; i++) {
			        	dst_v_flags.SetValue(i, src_v_flags.GetValue(i));
			        }
			        int numCells = main_obj.GetNumberOfCells();
			        if(!has_cell_indicators) {
				        vtkShortArray surface_flags = new vtkShortArray();
				        surface_flags.SetName("FlagT");
				        surface_flags.SetNumberOfComponents(1);
				        surface_flags.SetNumberOfTuples(numCells);
				        main_obj.GetCellData().AddArray(surface_flags);
			        }
			        vtkShortArray dst_f_flags = new vtkShortArray(init_patch_obj.GetCellData().GetArray("FlagT").GetVTKId());
			        vtkShortArray src_f_flags = new vtkShortArray(main_obj.GetCellData().GetArray("FlagT").GetVTKId());
			        for(int i=0; i<numCells; i++) {
			        	dst_f_flags.SetValue(i, src_f_flags.GetValue(i));
			        }
				}
		        // ====== Second, expand the curve depending on the recent mode off patch addition ====== //
		        if(global_propagate_refine) {

		        } else {
					if(mesh_obj_cgel==null)
						mesh_obj_cgel = new Mesh(main_obj);
					mesh_obj_cgel.updateByMaskingIndicator(main_obj, "FlagT", active_category_id);
					mesh_obj_cgel.updateByMaskingIndicator(main_obj, "FlagV", active_category_id);
					Propagation cgel_expand = new Propagation();
					cgel_expand.setBaseMesh(mesh_obj_cgel);
					//cgel_expand.use_1RingDistance();
					cgel_expand.use_AutoDistanceCompute();
					cgel_expand.evolveByShapeCategory();
					cgel_expand.disableGeometrySmoothing();
					cgel_expand.enablePostprocessing();
					cgel_expand.compute();
					//mesh_obj_cgel.updateByUnmaskingIndicator(main_obj, "FlagT", active_category_id);
					//mesh_obj_cgel.updateByUnmaskingIndicator(main_obj, "FlagV", active_category_id);
					
					cgel_expand.dispose();
					cgel_expand=null;
					System.out.println("Propagated patch.");

			        if(init_patch_obj_cgel==null) {
			        	init_patch_obj_cgel = new Mesh(init_patch_obj);
			        }
			        init_patch_obj_cgel.updateByMaskingIndicator(init_patch_obj, "FlagT", active_category_id);
			        init_patch_obj_cgel.updateByMaskingIndicator(init_patch_obj, "FlagV", active_category_id);

					//mesh_obj_cgel.updateByMaskingIndicator(main_obj, "FlagT", active_category_id);
					//mesh_obj_cgel.updateByMaskingIndicator(main_obj, "FlagV", active_category_id);
					Morphology cgel_morph = new Morphology();
					cgel_morph.setSizeThresholdMode(typedefs.MEDIAN_AVG);
					cgel_morph.setTriangleSizeMetric(typedefs.TRIANGLE_CIRCUMCIRCLE_METRIC);
					cgel_morph.enablePostprocessing();
					cgel_morph.setBaseMesh(mesh_obj_cgel);
					cgel_morph.setInitPatchMesh(init_patch_obj_cgel);
					cgel_morph.compute();
					System.out.println("Computed morphology.");
					System.out.printf("%d core patches, %d anticore patches, %d mortar patches.\n", cgel_morph.getNumberOfCorePatches(),cgel_morph.getNumberOfAnticorePatches(),cgel_morph.getNumberOfMortarPatches());

					Optimization cgel_optim = new Optimization();
					cgel_optim.setMesh(mesh_obj_cgel);
					cgel_optim.setInternalPriority(0.8);
					cgel_optim.setLengthWeight(0.4);
					cgel_optim.setIterationLimit(2200);
					cgel_optim.setStableIterationLimit(10);
					cgel_optim.setErrorLimit(0.000001);
					cgel_optim.enablePostprocessing();
					cgel_optim.activate_InternalExternalWeighting();
					cgel_optim.activate_LengthAngleWeighting();
					System.out.println("recompute morpholgy from indicators.");
					cgel_optim.autofill_morphology();
					System.out.println("start simulated annealing.");
					cgel_optim.compute_SimulatedAnnealing();
					System.out.println("Computed optimization.");
					
					mesh_obj_cgel.updateByUnmaskingIndicator(main_obj, "FlagT", active_category_id);
					mesh_obj_cgel.updateByUnmaskingIndicator(main_obj, "FlagV", active_category_id);
			        
			        boolean has_shpcat = (main_obj.GetPointData().HasArray("ShapeClass")!=0 ? true : false);
			        if(!has_shpcat) {
				        vtkShortArray vertex_shpcat = new vtkShortArray();
				        vertex_shpcat.SetName("ShapeClass");
				        vertex_shpcat.SetNumberOfComponents(1);
				        vertex_shpcat.SetNumberOfTuples(main_obj.GetNumberOfPoints());
				        main_obj.GetPointData().AddArray(vertex_shpcat);
				        for(int i=0; i<main_obj.GetNumberOfPoints(); i++) {
				        	vertex_shpcat.SetValue(i, 0);
				        }
			        }
			        // update the shape cat field from CGEL to VTK
					mesh_obj_cgel.updateVTKattribute(main_obj, "ShapeClass");

					cgel_morph.dispose();
					cgel_morph=null;
					cgel_optim.dispose();
					cgel_optim=null;
					System.out.println("Refined patch.");
		        }
				
				main_obj.GetPointData().Modified();
				main_obj.GetCellData().Modified();

				//swt_component.lock();
				//rw.Render();
				//swt_component.unlock();
				
				dispose_propagation_context_area();
			}
			
			@Override
			public void widgetDefaultSelected(SelectionEvent arg0) {
				
				
			}
		});
		
		Label lbl_extremalBounds_incircle = new Label(contextContainer, SWT.PUSH);
		lbl_extremalBounds_incircle.setText("extremal bounds grow; incircle refinement: ");
		Button btn_extremalBounds_incircle = new Button(contextContainer, SWT.PUSH);
		btn_extremalBounds_incircle.setText("compute");
		btn_extremalBounds_incircle.addSelectionListener(new SelectionListener() {
			
			@Override
			public void widgetSelected(SelectionEvent arg0) {
				
				// ====== First stash the initial patch config ====== //
				if(init_patch_obj==null) {
					init_patch_obj = new vtkPolyData();
					init_patch_obj.CopyStructure(main_obj);
			        int numVerts = main_obj.GetNumberOfPoints();
			        boolean has_vertex_indicators = (init_patch_obj.GetPointData().HasArray("FlagV")!=0 ? true : false);
			        boolean has_cell_indicators = (init_patch_obj.GetPointData().HasArray("FlagT")!=0 ? true : false);
			        if(!has_vertex_indicators) {
				        vtkShortArray vertex_flags = new vtkShortArray();
				        vertex_flags.SetName("FlagV");
				        vertex_flags.SetNumberOfComponents(1);
				        vertex_flags.SetNumberOfTuples(numVerts);
				        init_patch_obj.GetPointData().AddArray(vertex_flags);
			        }
			        vtkShortArray dst_v_flags = new vtkShortArray(init_patch_obj.GetPointData().GetArray("FlagV").GetVTKId());
			        vtkShortArray src_v_flags = new vtkShortArray(main_obj.GetPointData().GetArray("FlagV").GetVTKId());
			        for(int i=0; i<numVerts; i++) {
			        	dst_v_flags.SetValue(i, src_v_flags.GetValue(i));
			        }
			        int numCells = main_obj.GetNumberOfCells();
			        if(!has_cell_indicators) {
				        vtkShortArray surface_flags = new vtkShortArray();
				        surface_flags.SetName("FlagT");
				        surface_flags.SetNumberOfComponents(1);
				        surface_flags.SetNumberOfTuples(numCells);
				        init_patch_obj.GetCellData().AddArray(surface_flags);
			        }
			        vtkShortArray dst_f_flags = new vtkShortArray(init_patch_obj.GetCellData().GetArray("FlagT").GetVTKId());
			        vtkShortArray src_f_flags = new vtkShortArray(main_obj.GetCellData().GetArray("FlagT").GetVTKId());
			        for(int i=0; i<numCells; i++) {
			        	dst_f_flags.SetValue(i, src_f_flags.GetValue(i));
			        }
				}
				
		        if(global_propagate_refine) {

		        } else {
					if(mesh_obj_cgel==null)
						mesh_obj_cgel = new Mesh(main_obj);
					mesh_obj_cgel.updateByMaskingIndicator(main_obj, "FlagT", active_category_id);
					mesh_obj_cgel.updateByMaskingIndicator(main_obj, "FlagV", active_category_id);
					Propagation cgel_expand = new Propagation();
					cgel_expand.setBaseMesh(mesh_obj_cgel);
					cgel_expand.use_1RingDistance();
					//cgel_expand.use_AutoDistanceCompute();
					cgel_expand.evolveByCurvatureConstraint();
					cgel_expand.disableGeometrySmoothing();
					cgel_expand.enablePostprocessing();
					cgel_expand.compute();
					//mesh_obj_cgel.updateByUnmaskingIndicator(main_obj, "FlagT", active_category_id);
					//mesh_obj_cgel.updateByUnmaskingIndicator(main_obj, "FlagV", active_category_id);

					cgel_expand.dispose();
					cgel_expand=null;
					System.out.println("Propagated patch.");

			        if(init_patch_obj_cgel==null) {
			        	init_patch_obj_cgel = new Mesh(init_patch_obj);
			        }
			        init_patch_obj_cgel.updateByMaskingIndicator(init_patch_obj, "FlagT", active_category_id);
			        init_patch_obj_cgel.updateByMaskingIndicator(init_patch_obj, "FlagV", active_category_id);

					//mesh_obj_cgel.updateByMaskingIndicator(main_obj, "FlagT", active_category_id);
					//mesh_obj_cgel.updateByMaskingIndicator(main_obj, "FlagV", active_category_id);
					Morphology cgel_morph = new Morphology();
					cgel_morph.setSizeThresholdMode(typedefs.MEDIAN_AVG);
					cgel_morph.setTriangleSizeMetric(typedefs.TRIANGLE_INCIRCLE_METRIC);
					cgel_morph.enablePostprocessing();
					cgel_morph.setBaseMesh(mesh_obj_cgel);
					cgel_morph.setInitPatchMesh(init_patch_obj_cgel);
					cgel_morph.compute();
					System.out.println("Computed morphology.");
					System.out.printf("%d core patches, %d anticore patches, %d mortar patches.\n", cgel_morph.getNumberOfCorePatches(),cgel_morph.getNumberOfAnticorePatches(),cgel_morph.getNumberOfMortarPatches());

					Optimization cgel_optim = new Optimization();
					cgel_optim.setMesh(mesh_obj_cgel);
					cgel_optim.setInternalPriority(0.8);
					cgel_optim.setLengthWeight(0.4);
					cgel_optim.setIterationLimit(2200);
					cgel_optim.setStableIterationLimit(10);
					cgel_optim.setErrorLimit(0.000001);
					cgel_optim.enablePostprocessing();
					cgel_optim.activate_InternalExternalWeighting();
					cgel_optim.activate_LengthAngleWeighting();
					System.out.println("recompute morpholgy from indicators.");
					cgel_optim.autofill_morphology();
					System.out.println("start simulated annealing.");
					cgel_optim.compute_SimulatedAnnealing();
					System.out.println("Computed optimization.");
					
					mesh_obj_cgel.updateByUnmaskingIndicator(main_obj, "FlagT", active_category_id);
					mesh_obj_cgel.updateByUnmaskingIndicator(main_obj, "FlagV", active_category_id);
			        
			        boolean has_shpcat = (main_obj.GetPointData().HasArray("ShapeClass")!=0 ? true : false);
			        if(!has_shpcat) {
				        vtkShortArray vertex_shpcat = new vtkShortArray();
				        vertex_shpcat.SetName("ShapeClass");
				        vertex_shpcat.SetNumberOfComponents(1);
				        vertex_shpcat.SetNumberOfTuples(main_obj.GetNumberOfPoints());
				        main_obj.GetPointData().AddArray(vertex_shpcat);
				        for(int i=0; i<main_obj.GetNumberOfPoints(); i++) {
				        	vertex_shpcat.SetValue(i, 0);
				        }
			        }
			        // update the shape cat field from CGEL to VTK
					mesh_obj_cgel.updateVTKattribute(main_obj, "ShapeClass");

					cgel_morph.dispose();
					cgel_morph=null;
					cgel_optim.dispose();
					cgel_optim=null;
					System.out.println("Refined patch.");
		        }
				
				main_obj.GetPointData().Modified();
				main_obj.GetCellData().Modified();

				//swt_component.lock();
				//rw.Render();
				//swt_component.unlock();
		        
				dispose_prop_n_refine_context_area();
			}
			
			@Override
			public void widgetDefaultSelected(SelectionEvent arg0) {
				
				
			}
		});
		
		Label lbl_extremalBounds_circumcircle = new Label(contextContainer, SWT.PUSH);
		lbl_extremalBounds_circumcircle.setText("extremal bounds grow; circumcircle refinement: ");
		Button btn_extremalBounds_circumcircle = new Button(contextContainer, SWT.PUSH);
		btn_extremalBounds_circumcircle.setText("compute");
		btn_extremalBounds_circumcircle.addSelectionListener(new SelectionListener() {
			
			@Override
			public void widgetSelected(SelectionEvent arg0) {
				
				// ====== First stash the initial patch config ====== //
				if(init_patch_obj==null) {
					init_patch_obj = new vtkPolyData();
					init_patch_obj.CopyStructure(main_obj);
			        int numVerts = main_obj.GetNumberOfPoints();
			        boolean has_vertex_indicators = (init_patch_obj.GetPointData().HasArray("FlagV")!=0 ? true : false);
			        boolean has_cell_indicators = (init_patch_obj.GetPointData().HasArray("FlagT")!=0 ? true : false);
			        if(!has_vertex_indicators) {
				        vtkShortArray vertex_flags = new vtkShortArray();
				        vertex_flags.SetName("FlagV");
				        vertex_flags.SetNumberOfComponents(1);
				        vertex_flags.SetNumberOfTuples(numVerts);
				        init_patch_obj.GetPointData().AddArray(vertex_flags);
			        }
			        vtkShortArray dst_v_flags = new vtkShortArray(init_patch_obj.GetPointData().GetArray("FlagV").GetVTKId());
			        vtkShortArray src_v_flags = new vtkShortArray(main_obj.GetPointData().GetArray("FlagV").GetVTKId());
			        for(int i=0; i<numVerts; i++) {
			        	dst_v_flags.SetValue(i, src_v_flags.GetValue(i));
			        }
			        int numCells = main_obj.GetNumberOfCells();
			        if(!has_cell_indicators) {
				        vtkShortArray surface_flags = new vtkShortArray();
				        surface_flags.SetName("FlagT");
				        surface_flags.SetNumberOfComponents(1);
				        surface_flags.SetNumberOfTuples(numCells);
				        init_patch_obj.GetCellData().AddArray(surface_flags);
			        }
			        vtkShortArray dst_f_flags = new vtkShortArray(init_patch_obj.GetCellData().GetArray("FlagT").GetVTKId());
			        vtkShortArray src_f_flags = new vtkShortArray(main_obj.GetCellData().GetArray("FlagT").GetVTKId());
			        for(int i=0; i<numCells; i++) {
			        	dst_f_flags.SetValue(i, src_f_flags.GetValue(i));
			        }
				}
				
		        if(global_propagate_refine) {

		        } else {
					if(mesh_obj_cgel==null)
						mesh_obj_cgel = new Mesh(main_obj);
					mesh_obj_cgel.updateByMaskingIndicator(main_obj, "FlagT", active_category_id);
					mesh_obj_cgel.updateByMaskingIndicator(main_obj, "FlagV", active_category_id);
					Propagation cgel_expand = new Propagation();
					cgel_expand.setBaseMesh(mesh_obj_cgel);
					cgel_expand.use_1RingDistance();
					//cgel_expand.use_AutoDistanceCompute();
					cgel_expand.evolveByCurvatureConstraint();
					cgel_expand.disableGeometrySmoothing();
					cgel_expand.enablePostprocessing();
					cgel_expand.compute();
					//mesh_obj_cgel.updateByUnmaskingIndicator(main_obj, "FlagT", active_category_id);
					//mesh_obj_cgel.updateByUnmaskingIndicator(main_obj, "FlagV", active_category_id);

					cgel_expand.dispose();
					cgel_expand=null;
					System.out.println("Propagated patch.");

			        if(init_patch_obj_cgel==null) {
			        	init_patch_obj_cgel = new Mesh(init_patch_obj);
			        }
			        init_patch_obj_cgel.updateByMaskingIndicator(init_patch_obj, "FlagT", active_category_id);
			        init_patch_obj_cgel.updateByMaskingIndicator(init_patch_obj, "FlagV", active_category_id);

					//mesh_obj_cgel.updateByMaskingIndicator(main_obj, "FlagT", active_category_id);
					//mesh_obj_cgel.updateByMaskingIndicator(main_obj, "FlagV", active_category_id);
					Morphology cgel_morph = new Morphology();
					cgel_morph.setSizeThresholdMode(typedefs.MEDIAN_AVG);
					cgel_morph.setTriangleSizeMetric(typedefs.TRIANGLE_CIRCUMCIRCLE_METRIC);
					cgel_morph.enablePostprocessing();
					cgel_morph.setBaseMesh(mesh_obj_cgel);
					cgel_morph.setInitPatchMesh(init_patch_obj_cgel);
					cgel_morph.compute();
					System.out.println("Computed morphology.");
					System.out.printf("%d core patches, %d anticore patches, %d mortar patches.\n", cgel_morph.getNumberOfCorePatches(),cgel_morph.getNumberOfAnticorePatches(),cgel_morph.getNumberOfMortarPatches());

					Optimization cgel_optim = new Optimization();
					cgel_optim.setMesh(mesh_obj_cgel);
					cgel_optim.setInternalPriority(0.8);
					cgel_optim.setLengthWeight(0.4);
					cgel_optim.setIterationLimit(2200);
					cgel_optim.setStableIterationLimit(10);
					cgel_optim.setErrorLimit(0.000001);
					cgel_optim.enablePostprocessing();
					cgel_optim.activate_InternalExternalWeighting();
					cgel_optim.activate_LengthAngleWeighting();
					System.out.println("recompute morpholgy from indicators.");
					cgel_optim.autofill_morphology();
					System.out.println("start simulated annealing.");
					cgel_optim.compute_SimulatedAnnealing();
					System.out.println("Computed optimization.");
					
					mesh_obj_cgel.updateByUnmaskingIndicator(main_obj, "FlagT", active_category_id);
					mesh_obj_cgel.updateByUnmaskingIndicator(main_obj, "FlagV", active_category_id);
			        
			        boolean has_shpcat = (main_obj.GetPointData().HasArray("ShapeClass")!=0 ? true : false);
			        if(!has_shpcat) {
				        vtkShortArray vertex_shpcat = new vtkShortArray();
				        vertex_shpcat.SetName("ShapeClass");
				        vertex_shpcat.SetNumberOfComponents(1);
				        vertex_shpcat.SetNumberOfTuples(main_obj.GetNumberOfPoints());
				        main_obj.GetPointData().AddArray(vertex_shpcat);
				        for(int i=0; i<main_obj.GetNumberOfPoints(); i++) {
				        	vertex_shpcat.SetValue(i, 0);
				        }
			        }
			        // update the shape cat field from CGEL to VTK
					mesh_obj_cgel.updateVTKattribute(main_obj, "ShapeClass");

					cgel_morph.dispose();
					cgel_morph=null;
					cgel_optim.dispose();
					cgel_optim=null;
					System.out.println("Refined patch.");
		        }
				
				main_obj.GetPointData().Modified();
				main_obj.GetCellData().Modified();

				//swt_component.lock();
				//rw.Render();
				//swt_component.unlock();
		        
				dispose_prop_n_refine_context_area();
			}
			
			@Override
			public void widgetDefaultSelected(SelectionEvent arg0) {
				
				
			}
		});
		
		contextBar.setMinSize(contextContainer.computeSize(SWT.DEFAULT, SWT.DEFAULT));
		contextContainer.layout();
	}
	
	protected void dispose_prop_n_refine_context_area() {
		if((!contextContainer.isDisposed()) && (contextContainer!=null)) {
			for(Control cnt_elem : contextContainer.getChildren()) {
				cnt_elem.dispose();
				cnt_elem = null;
			}
			contextBar.setMinSize(contextContainer.computeSize(SWT.DEFAULT, SWT.DEFAULT));
			contextContainer.layout();
		}
	}
	
	protected void create_refinement_context_area() {
		Label lbl_candidate_selection = new Label(contextContainer, SWT.PUSH);
		lbl_candidate_selection.setText("Candidate Selection Method: ");
		Combo cb_candidate_selection = new Combo(contextContainer, SWT.PUSH);
		String candidate_sel_items[] = { "Vertex Fan", "Large Triangle Components", "Small Triangle Components", "Adaptive Component Selection" };
		cb_candidate_selection.setItems(candidate_sel_items);
		cb_candidate_selection.select(cb_refine_candidate_selection_index);
		cb_candidate_selection.addSelectionListener(new SelectionListener() {
			
			@Override
			public void widgetSelected(SelectionEvent arg0) {
				cb_refine_candidate_selection_index = cb_candidate_selection.getSelectionIndex();
			}
			
			@Override
			public void widgetDefaultSelected(SelectionEvent arg0) {
				
				
			}
		});
		
		Label lbl_refine_algorithm = new Label(contextContainer, SWT.PUSH);
		lbl_refine_algorithm.setText("Optimization Algorithm: ");
		Combo cb_refine_algorithm = new Combo(contextContainer, SWT.PUSH);
		String refineAlgorithm_sel_items[] = { "Hill-Climbing", "Simulated Annealing" };
		cb_refine_algorithm.setItems(refineAlgorithm_sel_items);
		cb_refine_algorithm.select(cb_refine_algorithm_selection_index);
		cb_refine_algorithm.addSelectionListener(new SelectionListener() {
			
			@Override
			public void widgetSelected(SelectionEvent arg0) {
				cb_refine_algorithm_selection_index = cb_refine_algorithm.getSelectionIndex();
				//System.out.printf("Refine algorithm index: %d\n", cb_refine_algorithm.getSelectionIndex());
			}
			
			@Override
			public void widgetDefaultSelected(SelectionEvent arg0) {
				
				
			}
		});
		
		Label lbl_refine_radius_metric = new Label(contextContainer, SWT.PUSH);
		lbl_refine_radius_metric.setText("Morph. Radius Constraint Metric: ");
		Combo cb_refine_radius_metric = new Combo(contextContainer, SWT.PUSH);
		String refine_radius_metric_items[] = {"<None>","Insphere Radius","Circumsphere Radius", "Radius-of-Curvature"};
		cb_refine_radius_metric.setItems(refine_radius_metric_items);
		cb_refine_radius_metric.select(cb_refine_radius_metric_index);
		cb_refine_radius_metric.addSelectionListener(new SelectionListener() {
			
			@Override
			public void widgetSelected(SelectionEvent arg0) {
				
				cb_refine_radius_metric_index = cb_refine_radius_metric.getSelectionIndex();
			}
			
			@Override
			public void widgetDefaultSelected(SelectionEvent arg0) {
				
				
			}
		});

		Label lbl_refine_struct_elem_threshold_mode = new Label(contextContainer, SWT.PUSH);
		lbl_refine_struct_elem_threshold_mode.setText("Morph. Radius Threshold Mode: ");
		Combo cb_refine_struct_elem_threshold_mode = new Combo(contextContainer, SWT.PUSH);
		String refine_struct_elem_threshold_mode[] = {"Mean Radius", "Median Radius", "Confidence95 Radius", "Manual Radius"};
		cb_refine_struct_elem_threshold_mode.setItems(refine_struct_elem_threshold_mode);
		cb_refine_struct_elem_threshold_mode.select(cb_refine_struct_elem_threshold_mode_index);
		cb_refine_struct_elem_threshold_mode.addSelectionListener(new SelectionListener() {
			
			@Override
			public void widgetSelected(SelectionEvent arg0) {
				
				cb_refine_struct_elem_threshold_mode_index = cb_refine_struct_elem_threshold_mode.getSelectionIndex();
				if(cb_refine_struct_elem_threshold_mode_index==3) {
					edt_refine_struct_elem_size.setEnabled(true);
				} else {
					edt_refine_struct_elem_size.setEnabled(false);
				}
			}
			
			@Override
			public void widgetDefaultSelected(SelectionEvent arg0) {
				
				
			}
		});
		
		Label lbl_refine_struct_elem_size = new Label(contextContainer, SWT.PUSH);
		lbl_refine_struct_elem_size.setText("Radial Constraint Size: ");
		edt_refine_struct_elem_size = new Text(contextContainer, SWT.PUSH | SWT.BORDER);
		edt_refine_struct_elem_size.setText(Double.toString(vl_refine_struct_elem_radius));
		if(cb_refine_struct_elem_threshold_mode_index==3) {
			edt_refine_struct_elem_size.setEnabled(true);
		} else {
			edt_refine_struct_elem_size.setEnabled(false);
		}
		edt_refine_struct_elem_size.setEditable(true);
		edt_refine_struct_elem_size.addKeyListener(new KeyListener() {
			
			@Override
			public void keyReleased(KeyEvent arg0) {
				if((arg0.keyCode == SWT.CR) || (arg0.keyCode == SWT.KEYPAD_CR)) {
					vl_refine_struct_elem_radius = Double.parseDouble(edt_refine_struct_elem_size.getText());
				}
			}
			
			@Override
			public void keyPressed(KeyEvent arg0) {
				
			}
		});
		
		Label lbl_hillclimb_weights = new Label(contextContainer, SWT.PUSH);
		lbl_hillclimb_weights.setText("Hill-Climbing weights (a-ß-y): ");
		
		Composite weightGroup = new Composite(contextContainer, SWT.PUSH|SWT.BORDER);
		RowLayout weightGroupLayout = new RowLayout();
		weightGroupLayout.wrap = true;
		weightGroupLayout.pack = false;
		weightGroupLayout.justify = false;
		weightGroupLayout.marginLeft = 1;
		weightGroupLayout.marginTop = 1;
		weightGroupLayout.marginRight = 1;
		weightGroupLayout.marginBottom = 1;
		weightGroupLayout.spacing = 0;
		weightGroup.setLayout(weightGroupLayout);
		
		edt_alpha_weight = new Text(weightGroup, SWT.PUSH | SWT.BORDER);
		edt_alpha_weight.setText(Double.toString(vl_alpha));
		edt_alpha_weight.setEditable(true);
		edt_alpha_weight.addKeyListener(new KeyListener() {
			
			@Override
			public void keyReleased(KeyEvent arg0) {
				if((arg0.keyCode == SWT.CR) || (arg0.keyCode == SWT.KEYPAD_CR)) {
					vl_alpha = Double.parseDouble(edt_alpha_weight.getText());
				}
			}
			
			@Override
			public void keyPressed(KeyEvent arg0) {
				
			}
		});
		edt_beta_weight = new Text(weightGroup, SWT.PUSH | SWT.BORDER);
		edt_beta_weight.setText(Double.toString(vl_beta));
		edt_beta_weight.setEditable(true);
		edt_beta_weight.addKeyListener(new KeyListener() {
			
			@Override
			public void keyReleased(KeyEvent arg0) {
				vl_beta = Double.parseDouble(edt_beta_weight.getText());
			}
			
			@Override
			public void keyPressed(KeyEvent arg0) {
				
			}
		});
		edt_gamma_weight = new Text(weightGroup, SWT.PUSH | SWT.BORDER);
		edt_gamma_weight.setText(Double.toString(vl_gamma));
		edt_gamma_weight.setEditable(true);
		edt_gamma_weight.addKeyListener(new KeyListener() {
			
			@Override
			public void keyReleased(KeyEvent arg0) {
				vl_gamma = Double.parseDouble(edt_gamma_weight.getText());
			}
			
			@Override
			public void keyPressed(KeyEvent arg0) {
				
			}
		});
		
		
		if(cb_refine_algorithm_selection_index==0) {
			edt_alpha_weight.setEnabled(true);
			edt_beta_weight.setEnabled(true);
			edt_gamma_weight.setEnabled(true);
		} else {
			edt_alpha_weight.setEnabled(false);
			edt_beta_weight.setEnabled(false);
			edt_gamma_weight.setEnabled(false);
		}
		
		
		Button btn_start_computation = new Button(contextContainer, SWT.PUSH);
		btn_start_computation.setText("compute");
		btn_start_computation.addSelectionListener(new SelectionListener() {
			
			@Override
			public void widgetSelected(SelectionEvent arg0) {
				if(init_patch_obj==null) {
					init_patch_obj = new vtkPolyData();
					init_patch_obj.CopyStructure(main_obj);
			        int numVerts = main_obj.GetNumberOfPoints();
			        boolean has_vertex_indicators = (init_patch_obj.GetPointData().HasArray("FlagV")!=0 ? true : false);
			        boolean has_cell_indicators = (init_patch_obj.GetPointData().HasArray("FlagT")!=0 ? true : false);
			        if(!has_vertex_indicators) {
				        vtkShortArray vertex_flags = new vtkShortArray();
				        vertex_flags.SetName("FlagV");
				        vertex_flags.SetNumberOfComponents(1);
				        vertex_flags.SetNumberOfTuples(numVerts);
				        init_patch_obj.GetPointData().AddArray(vertex_flags);
			        }
			        vtkShortArray dst_v_flags = new vtkShortArray(init_patch_obj.GetPointData().GetArray("FlagV").GetVTKId());
			        vtkShortArray src_v_flags = new vtkShortArray(main_obj.GetPointData().GetArray("FlagV").GetVTKId());
			        for(int i=0; i<numVerts; i++) {
			        	dst_v_flags.SetValue(i, src_v_flags.GetValue(i));
			        }
			        int numCells = main_obj.GetNumberOfCells();
			        if(!has_cell_indicators) {
				        vtkShortArray surface_flags = new vtkShortArray();
				        surface_flags.SetName("FlagT");
				        surface_flags.SetNumberOfComponents(1);
				        surface_flags.SetNumberOfTuples(numCells);
				        init_patch_obj.GetCellData().AddArray(surface_flags);
			        }
			        vtkShortArray dst_f_flags = new vtkShortArray(init_patch_obj.GetCellData().GetArray("FlagT").GetVTKId());
			        vtkShortArray src_f_flags = new vtkShortArray(main_obj.GetCellData().GetArray("FlagT").GetVTKId());
			        for(int i=0; i<numCells; i++) {
			        	dst_f_flags.SetValue(i, src_f_flags.GetValue(i));
			        }
				}
				
				if(morph_obj==null) {
					morph_obj = new vtkPolyData();
					morph_obj.CopyStructure(main_obj);
			        boolean has_cell_indicators = (morph_obj.GetPointData().HasArray("FlagT")!=0 ? true : false);
			        int numCells = main_obj.GetNumberOfCells();
			        if(!has_cell_indicators) {
				        vtkShortArray surface_flags = new vtkShortArray();
				        surface_flags.SetName("FlagT");
				        surface_flags.SetNumberOfComponents(1);
				        surface_flags.SetNumberOfTuples(numCells);
				        morph_obj.GetCellData().AddArray(surface_flags);
			        }
			        vtkShortArray dst_f_flags = new vtkShortArray(morph_obj.GetCellData().GetArray("FlagT").GetVTKId());
			        //vtkShortArray src_f_flags = new vtkShortArray(main_obj.GetCellData().GetArray("FlagT").GetVTKId());
			        for(int i=0; i<numCells; i++) {
			        	dst_f_flags.SetValue(i, 0);
			        }
				}
				
				/*
				 * WHAT HAPPENS IF WE OPEN A FILES THAT IS ALREADY PROPAGATED ?
				 */
				if(cb_refine_radius_metric_index == typedefs.NONE_METRIC)
					return;
		        if(init_patch_obj_cgel==null) {
		        	init_patch_obj_cgel = new Mesh(init_patch_obj);
		        }
				if(mesh_obj_cgel==null) {
					mesh_obj_cgel = new Mesh(main_obj);
				}
		        if(global_propagate_refine) {
					Morphology cgel_morph = new Morphology();
					cgel_morph.setSizeThresholdMode(cb_refine_struct_elem_threshold_mode_index);
					cgel_morph.setTriangleSizeMetric(cb_refine_radius_metric_index);
					if(cb_refine_struct_elem_threshold_mode_index==typedefs.MANUAL) {
						cgel_morph.setStructuringElementSize_manually(vl_refine_struct_elem_radius);
					}
					cgel_morph.enablePostprocessing();
					cgel_morph.setBaseMesh(mesh_obj_cgel);
					cgel_morph.setInitPatchMesh(init_patch_obj_cgel);
					
					Optimization cgel_optim = new Optimization();
					cgel_optim.setMesh(mesh_obj_cgel);
					cgel_optim.setCandidateSelectionMethod(cb_refine_candidate_selection_index);
					cgel_optim.setInternalPriority(0.8);
					cgel_optim.setLengthWeight(0.4);
					cgel_optim.setIterationLimit(2200);
					cgel_optim.setStableIterationLimit(10);
					cgel_optim.setErrorLimit(0.000001);
					if(cb_refine_algorithm_selection_index==0) {
						cgel_optim.setAlpha(vl_alpha);
						cgel_optim.setBeta(vl_beta);
						cgel_optim.setGamma(vl_gamma);
					}
					cgel_optim.enablePostprocessing();
					cgel_optim.activate_InternalExternalWeighting();
					cgel_optim.activate_LengthAngleWeighting();
					
					for(int i=0; i<num_patches; i++) {
				        init_patch_obj_cgel.updateByMaskingIndicator(init_patch_obj, "FlagT", i+1);
				        init_patch_obj_cgel.updateByMaskingIndicator(init_patch_obj, "FlagV", i+1);
						mesh_obj_cgel.updateByMaskingIndicator(main_obj, "FlagT", i+1);
						mesh_obj_cgel.updateByMaskingIndicator(main_obj, "FlagV", i+1);
						cgel_morph.compute();
						if(morph_obj!=null) {
							cgel_morph.StoreMeshMorphologyInVTK(morph_obj);
							saveMorphMenu.setEnabled(true);
						} else {
							System.out.println("morph_obj is NULL");
						}
						System.out.println("Computed morphology.");
						System.out.printf("%d core patches, %d anticore patches, %d mortar patches.\n", cgel_morph.getNumberOfCorePatches(),cgel_morph.getNumberOfAnticorePatches(),cgel_morph.getNumberOfMortarPatches());
						System.out.printf("Structuring element size: %f\n", cgel_morph.getStructuringElementSize());
						vl_refine_struct_elem_radius = cgel_morph.getStructuringElementSize();
						edt_refine_struct_elem_size.setText(Double.toString(vl_refine_struct_elem_radius));

						System.out.println("recompute morphology from indicators.");
						cgel_optim.autofill_morphology();
						if(cb_refine_algorithm_selection_index==0) {
							System.out.println("start hill-climbing.");
							cgel_optim.compute_GreedyOptimization();
						} else if(cb_refine_algorithm_selection_index==1) {
							System.out.println("start simulated annealing.");
							cgel_optim.compute_SimulatedAnnealing();
						}
						System.out.println("Computed optimization.");
						mesh_obj_cgel.updateByUnmaskingIndicator(main_obj, "FlagT", i+1);
						mesh_obj_cgel.updateByUnmaskingIndicator(main_obj, "FlagV", i+1);
					}
					cgel_morph.dispose();
					cgel_morph=null;
					cgel_optim.dispose();
					cgel_optim=null;
		        } else {
			        init_patch_obj_cgel.updateByMaskingIndicator(init_patch_obj, "FlagT", active_category_id);
			        init_patch_obj_cgel.updateByMaskingIndicator(init_patch_obj, "FlagV", active_category_id);

					mesh_obj_cgel.updateByMaskingIndicator(main_obj, "FlagT", active_category_id);
					mesh_obj_cgel.updateByMaskingIndicator(main_obj, "FlagV", active_category_id);
					Morphology cgel_morph = new Morphology();
					cgel_morph.setSizeThresholdMode(cb_refine_struct_elem_threshold_mode_index);
					cgel_morph.setTriangleSizeMetric(cb_refine_radius_metric_index);
					if(cb_refine_struct_elem_threshold_mode_index==typedefs.MANUAL) {
						cgel_morph.setStructuringElementSize_manually(vl_refine_struct_elem_radius);
					}
					cgel_morph.enablePostprocessing();
					cgel_morph.setBaseMesh(mesh_obj_cgel);
					cgel_morph.setInitPatchMesh(init_patch_obj_cgel);
					cgel_morph.compute();
					if(morph_obj!=null) {
						cgel_morph.StoreMeshMorphologyInVTK(morph_obj);
						saveMorphMenu.setEnabled(true);
					} else {
						System.out.println("morph_obj is NULL");
					}
					System.out.println("Computed morphology.");
					System.out.printf("%d core patches, %d anticore patches, %d mortar patches.\n", cgel_morph.getNumberOfCorePatches(),cgel_morph.getNumberOfAnticorePatches(),cgel_morph.getNumberOfMortarPatches());
					System.out.printf("Structuring element size: %f \n", cgel_morph.getStructuringElementSize());
					vl_refine_struct_elem_radius = cgel_morph.getStructuringElementSize();
					edt_refine_struct_elem_size.setText(Double.toString(vl_refine_struct_elem_radius));

					Optimization cgel_optim = new Optimization();
					cgel_optim.setMesh(mesh_obj_cgel);
					cgel_optim.setCandidateSelectionMethod(cb_refine_candidate_selection_index);
					cgel_optim.setInternalPriority(0.8);
					cgel_optim.setLengthWeight(0.4);
					cgel_optim.setIterationLimit(2200);
					cgel_optim.setStableIterationLimit(10);
					cgel_optim.setErrorLimit(0.000001);
					if(cb_refine_algorithm_selection_index==0) {
						cgel_optim.setAlpha(vl_alpha);
						cgel_optim.setBeta(vl_beta);
						cgel_optim.setGamma(vl_gamma);
					}
					cgel_optim.enablePostprocessing();
					cgel_optim.activate_InternalExternalWeighting();
					cgel_optim.activate_LengthAngleWeighting();
					System.out.println("recompute morphology from indicators.");
					cgel_optim.autofill_morphology();
					if(cb_refine_algorithm_selection_index==0) {
						System.out.println("start hill-climbing.");
						cgel_optim.compute_GreedyOptimization();
					} else if(cb_refine_algorithm_selection_index==1) {
						System.out.println("start simulated annealing.");
						cgel_optim.compute_SimulatedAnnealing();
					}
					System.out.println("Computed optimization.");
					
					mesh_obj_cgel.updateByUnmaskingIndicator(main_obj, "FlagT", active_category_id);
					mesh_obj_cgel.updateByUnmaskingIndicator(main_obj, "FlagV", active_category_id);
					
					//cgel_mesh_opt.dispose();
					//cgel_mesh_opt=null;
					cgel_morph.dispose();
					cgel_morph=null;
					cgel_optim.dispose();
					cgel_optim=null;
		        }
				
				main_obj.GetPointData().Modified();
				main_obj.GetCellData().Modified();

				//swt_component.lock();
				//rw.Render();
				//swt_component.unlock();
				
				
				dispose_refinement_context_area();
			}
			
			@Override
			public void widgetDefaultSelected(SelectionEvent arg0) {
				
			}
		});
		

		/* 
		
		Label lbl_connected_shpcat = new Label(contextContainer, SWT.PUSH);
		lbl_connected_shpcat.setText("insphere metric: ");
		Button btn_connected_shpcat = new Button(contextContainer, SWT.PUSH);
		btn_connected_shpcat.setText("compute");
		btn_connected_shpcat.addSelectionListener(new SelectionListener() {
			
			@Override
			public void widgetSelected(SelectionEvent arg0) {
		        if(init_patch_obj_cgel==null) {
		        	init_patch_obj_cgel = new Mesh(init_patch_obj);
		        }
				if(mesh_obj_cgel==null) {
					mesh_obj_cgel = new Mesh(main_obj);
				}
		        if(global_propagate_refine) {
					Morphology cgel_morph = new Morphology();
					cgel_morph.setSizeThresholdMode(typedefs.MEDIAN_AVG);
					//cgel_morph.setSizeThresholdMode(typedefs.CONFIDENCE_95);
					cgel_morph.setTriangleSizeMetric(typedefs.TRIANGLE_INCIRCLE_METRIC);
					//cgel_morph.setTriangleSizeMetric(typedefs.TRIANGLE_CIRCUMCIRCLE_METRIC);
					//cgel_morph.setTriangleSizeMetric(typedefs.CURVATURE_METRIC);
					cgel_morph.enablePostprocessing();
					cgel_morph.setBaseMesh(mesh_obj_cgel);
					cgel_morph.setInitPatchMesh(init_patch_obj_cgel);
					
					Optimization cgel_optim = new Optimization();
					cgel_optim.setMesh(mesh_obj_cgel);
					cgel_optim.setInternalPriority(0.8);
					cgel_optim.setLengthWeight(0.4);
					cgel_optim.setIterationLimit(2200);
					cgel_optim.setStableIterationLimit(10);
					cgel_optim.setErrorLimit(0.000001);
					cgel_optim.enablePostprocessing();
					cgel_optim.activate_InternalExternalWeighting();
					cgel_optim.activate_LengthAngleWeighting();
					
					for(int i=0; i<num_patches; i++) {
				        init_patch_obj_cgel.updateByMaskingIndicator(init_patch_obj, "FlagT", i);
				        init_patch_obj_cgel.updateByMaskingIndicator(init_patch_obj, "FlagV", i);
						mesh_obj_cgel.updateByMaskingIndicator(main_obj, "FlagT", i);
						mesh_obj_cgel.updateByMaskingIndicator(main_obj, "FlagV", i);
						cgel_morph.compute();
						System.out.println("Computed morphology.");
						System.out.printf("%d core patches, %d anticore patches, %d mortar patches.\n", cgel_morph.getNumberOfCorePatches(),cgel_morph.getNumberOfAnticorePatches(),cgel_morph.getNumberOfMortarPatches());

						System.out.println("recompute morpholgy from indicators.");
						cgel_optim.autofill_morphology();
						System.out.println("start simulated annealing.");
						cgel_optim.compute_SimulatedAnnealing();
						System.out.println("Computed optimization.");	
						
						mesh_obj_cgel.updateByUnmaskingIndicator(main_obj, "FlagT", active_category_id);
						mesh_obj_cgel.updateByUnmaskingIndicator(main_obj, "FlagV", active_category_id);
					}
					cgel_morph.dispose();
					cgel_morph=null;
					cgel_optim.dispose();
					cgel_optim=null;
		        } else {
			        init_patch_obj_cgel.updateByMaskingIndicator(init_patch_obj, "FlagT", active_category_id);
			        init_patch_obj_cgel.updateByMaskingIndicator(init_patch_obj, "FlagV", active_category_id);

					mesh_obj_cgel.updateByMaskingIndicator(main_obj, "FlagT", active_category_id);
					mesh_obj_cgel.updateByMaskingIndicator(main_obj, "FlagV", active_category_id);
					Morphology cgel_morph = new Morphology();
					cgel_morph.setSizeThresholdMode(typedefs.MEDIAN_AVG);
					//cgel_morph.setSizeThresholdMode(typedefs.CONFIDENCE_95);
					cgel_morph.setTriangleSizeMetric(typedefs.TRIANGLE_INCIRCLE_METRIC);
					//cgel_morph.setTriangleSizeMetric(typedefs.TRIANGLE_CIRCUMCIRCLE_METRIC);
					//cgel_morph.setTriangleSizeMetric(typedefs.CURVATURE_METRIC);
					cgel_morph.enablePostprocessing();
					cgel_morph.setBaseMesh(mesh_obj_cgel);
					cgel_morph.setInitPatchMesh(init_patch_obj_cgel);
					cgel_morph.compute();
					System.out.println("Computed morphology.");
					System.out.printf("%d core patches, %d anticore patches, %d mortar patches.\n", cgel_morph.getNumberOfCorePatches(),cgel_morph.getNumberOfAnticorePatches(),cgel_morph.getNumberOfMortarPatches());

					Optimization cgel_optim = new Optimization();
					cgel_optim.setMesh(mesh_obj_cgel);
					cgel_optim.setInternalPriority(0.8);
					cgel_optim.setLengthWeight(0.4);
					cgel_optim.setIterationLimit(2200);
					cgel_optim.setStableIterationLimit(10);
					cgel_optim.setErrorLimit(0.000001);
					cgel_optim.enablePostprocessing();
					cgel_optim.activate_InternalExternalWeighting();
					cgel_optim.activate_LengthAngleWeighting();
					System.out.println("recompute morpholgy from indicators.");
					cgel_optim.autofill_morphology();
					System.out.println("start simulated annealing.");
					cgel_optim.compute_SimulatedAnnealing();
					System.out.println("Computed optimization.");
					
					mesh_obj_cgel.updateByUnmaskingIndicator(main_obj, "FlagT", active_category_id);
					mesh_obj_cgel.updateByUnmaskingIndicator(main_obj, "FlagV", active_category_id);
					
					//cgel_mesh_opt.dispose();
					//cgel_mesh_opt=null;
					cgel_morph.dispose();
					cgel_morph=null;
					cgel_optim.dispose();
					cgel_optim=null;
		        }
				
				main_obj.GetPointData().Modified();
				main_obj.GetCellData().Modified();
				rw.Render();
				
				
				dispose_refinement_context_area();
			}
			
			@Override
			public void widgetDefaultSelected(SelectionEvent arg0) {
				
			}
		});
		
		Label lbl_majcov_shpcat = new Label(contextContainer, SWT.PUSH);
		lbl_majcov_shpcat.setText("circumsphere metric: ");
		Button btn_majcov_shpcat = new Button(contextContainer, SWT.PUSH);
		btn_majcov_shpcat.setText("compute");
		btn_majcov_shpcat.addSelectionListener(new SelectionListener() {
			
			@Override
			public void widgetSelected(SelectionEvent arg0) {
				//Mesh cgel_mesh_init = Mesh.from_vtkPolyData(main_obj);
				//Mesh cgel_mesh_init = new Mesh(init_patch_obj);
		        if(init_patch_obj_cgel==null) {
		        	init_patch_obj_cgel = new Mesh(init_patch_obj);
		        }
				if(mesh_obj_cgel==null) {
					mesh_obj_cgel = new Mesh(main_obj);
				}
				if(global_propagate_refine) {
					Morphology cgel_morph = new Morphology();
					cgel_morph.setSizeThresholdMode(typedefs.MEDIAN_AVG);
					//cgel_morph.setSizeThresholdMode(typedefs.CONFIDENCE_95);
					//cgel_morph.setTriangleSizeMetric(typedefs.TRIANGLE_INCIRCLE_METRIC);
					cgel_morph.setTriangleSizeMetric(typedefs.TRIANGLE_CIRCUMCIRCLE_METRIC);
					//cgel_morph.setTriangleSizeMetric(typedefs.CURVATURE_METRIC);
					cgel_morph.enablePostprocessing();
					cgel_morph.setBaseMesh(mesh_obj_cgel);
					cgel_morph.setInitPatchMesh(init_patch_obj_cgel);
					
					Optimization cgel_optim = new Optimization();
					cgel_optim.setMesh(mesh_obj_cgel);
					cgel_optim.setInternalPriority(0.8);
					cgel_optim.setLengthWeight(0.4);
					cgel_optim.setIterationLimit(2200);
					cgel_optim.setStableIterationLimit(10);
					cgel_optim.setErrorLimit(0.000001);
					cgel_optim.enablePostprocessing();
					cgel_optim.activate_InternalExternalWeighting();
					cgel_optim.activate_LengthAngleWeighting();
					
					for(int i=0; i<num_patches; i++) {
				        init_patch_obj_cgel.updateByMaskingIndicator(init_patch_obj, "FlagT", i);
				        init_patch_obj_cgel.updateByMaskingIndicator(init_patch_obj, "FlagV", i);
						mesh_obj_cgel.updateByMaskingIndicator(main_obj, "FlagT", i);
						mesh_obj_cgel.updateByMaskingIndicator(main_obj, "FlagV", i);
						cgel_morph.compute();
						System.out.println("Computed morphology.");
						System.out.printf("%d core patches, %d anticore patches, %d mortar patches.\n", cgel_morph.getNumberOfCorePatches(),cgel_morph.getNumberOfAnticorePatches(),cgel_morph.getNumberOfMortarPatches());

						System.out.println("recompute morpholgy from indicators.");
						cgel_optim.autofill_morphology();
						System.out.println("start simulated annealing.");
						cgel_optim.compute_SimulatedAnnealing();
						System.out.println("Computed optimization.");	
						
						mesh_obj_cgel.updateByUnmaskingIndicator(main_obj, "FlagT", active_category_id);
						mesh_obj_cgel.updateByUnmaskingIndicator(main_obj, "FlagV", active_category_id);
					}
					cgel_morph.dispose();
					cgel_morph=null;
					cgel_optim.dispose();
					cgel_optim=null;
				} else {
			        init_patch_obj_cgel.updateByMaskingIndicator(init_patch_obj, "FlagT", active_category_id);
			        init_patch_obj_cgel.updateByMaskingIndicator(init_patch_obj, "FlagV", active_category_id);
					
					//Mesh cgel_mesh = Mesh.from_vtkPolyData(main_obj);
					//Mesh cgel_mesh_opt = new Mesh(main_obj);
					mesh_obj_cgel.updateByMaskingIndicator(main_obj, "FlagT", active_category_id);
					mesh_obj_cgel.updateByMaskingIndicator(main_obj, "FlagV", active_category_id);
					Morphology cgel_morph = new Morphology();
					cgel_morph.setSizeThresholdMode(typedefs.MEDIAN_AVG);
					//cgel_morph.setSizeThresholdMode(typedefs.CONFIDENCE_95);
					//cgel_morph.setTriangleSizeMetric(typedefs.TRIANGLE_INCIRCLE_METRIC);
					cgel_morph.setTriangleSizeMetric(typedefs.TRIANGLE_CIRCUMCIRCLE_METRIC);
					//cgel_morph.setTriangleSizeMetric(typedefs.CURVATURE_METRIC);
					cgel_morph.enablePostprocessing();
					cgel_morph.setBaseMesh(mesh_obj_cgel);
					cgel_morph.setInitPatchMesh(init_patch_obj_cgel);
					cgel_morph.compute();
					//cgel_mesh_init.dispose();
					//cgel_mesh_init=null;
					System.out.println("Computed morphology.");
					System.out.printf("%d core patches, %d anticore patches, %d mortar patches.\n", cgel_morph.getNumberOfCorePatches(),cgel_morph.getNumberOfAnticorePatches(),cgel_morph.getNumberOfMortarPatches());
					//mesh_obj_cgel.updateByUnmaskingIndicator(main_obj, "FlagT", active_category_id);
					//mesh_obj_cgel.updateByUnmaskingIndicator(main_obj, "FlagV", active_category_id);
	
					//mesh_obj_cgel.updateByMaskingIndicator(main_obj, "FlagT", active_category_id);
					//mesh_obj_cgel.updateByMaskingIndicator(main_obj, "FlagV", active_category_id);
					Optimization cgel_optim = new Optimization();
					cgel_optim.setMesh(mesh_obj_cgel);
					cgel_optim.setInternalPriority(0.8);
					cgel_optim.setLengthWeight(0.4);
					cgel_optim.setIterationLimit(2200);
					cgel_optim.setStableIterationLimit(10);
					cgel_optim.setErrorLimit(0.000001);
					cgel_optim.enablePostprocessing();
					cgel_optim.activate_InternalExternalWeighting();
					cgel_optim.activate_LengthAngleWeighting();
					System.out.println("recompute morpholgy from indicators.");
					cgel_optim.autofill_morphology();
					System.out.println("start simulated annealing.");
					cgel_optim.compute_SimulatedAnnealing();
					System.out.println("Computed optimization.");
					
					//cgel_mesh_opt.update_vtkPolyData(main_obj);
					mesh_obj_cgel.updateByUnmaskingIndicator(main_obj, "FlagT", active_category_id);
					mesh_obj_cgel.updateByUnmaskingIndicator(main_obj, "FlagV", active_category_id);
					
					//cgel_mesh_opt.dispose();
					//cgel_mesh_opt=null;
					cgel_morph.dispose();
					cgel_morph=null;
					cgel_optim.dispose();
					cgel_optim=null;
				}
				
				main_obj.GetPointData().Modified();
				main_obj.GetCellData().Modified();
				rw.Render();
				
				
				dispose_refinement_context_area();
			}
			
			@Override
			public void widgetDefaultSelected(SelectionEvent arg0) {
				
				
			}
		});
		
		Label lbl_extremal_bounds = new Label(contextContainer, SWT.PUSH);
		lbl_extremal_bounds.setText("radius-of-curvature metric: ");
		Button btn_extremal_bounds = new Button(contextContainer, SWT.PUSH);
		btn_extremal_bounds.setText("compute");
		btn_extremal_bounds.addSelectionListener(new SelectionListener() {
			
			@Override
			public void widgetSelected(SelectionEvent arg0) {
				
				//Mesh cgel_mesh_init = Mesh.from_vtkPolyData(main_obj);
				//Mesh cgel_mesh_init = new Mesh(init_patch_obj);
		        if(init_patch_obj_cgel==null) {
		        	init_patch_obj_cgel = new Mesh(init_patch_obj);
		        }
				if(mesh_obj_cgel==null)
					mesh_obj_cgel = new Mesh(main_obj);
				
				if(global_propagate_refine) {
					Morphology cgel_morph = new Morphology();
					cgel_morph.setSizeThresholdMode(typedefs.MEDIAN_AVG);
					//cgel_morph.setSizeThresholdMode(typedefs.CONFIDENCE_95);
					//cgel_morph.setTriangleSizeMetric(typedefs.TRIANGLE_INCIRCLE_METRIC);
					//cgel_morph.setTriangleSizeMetric(typedefs.TRIANGLE_CIRCUMCIRCLE_METRIC);
					cgel_morph.setTriangleSizeMetric(typedefs.CURVATURE_METRIC);
					cgel_morph.enablePostprocessing();
					cgel_morph.setBaseMesh(mesh_obj_cgel);
					cgel_morph.setInitPatchMesh(init_patch_obj_cgel);
					
					Optimization cgel_optim = new Optimization();
					cgel_optim.setMesh(mesh_obj_cgel);
					cgel_optim.setInternalPriority(0.8);
					cgel_optim.setLengthWeight(0.4);
					cgel_optim.setIterationLimit(2200);
					cgel_optim.setStableIterationLimit(10);
					cgel_optim.setErrorLimit(0.000001);
					cgel_optim.enablePostprocessing();
					cgel_optim.activate_InternalExternalWeighting();
					cgel_optim.activate_LengthAngleWeighting();
					
					for(int i=0; i<num_patches; i++) {
				        init_patch_obj_cgel.updateByMaskingIndicator(init_patch_obj, "FlagT", i);
				        init_patch_obj_cgel.updateByMaskingIndicator(init_patch_obj, "FlagV", i);
						mesh_obj_cgel.updateByMaskingIndicator(main_obj, "FlagT", i);
						mesh_obj_cgel.updateByMaskingIndicator(main_obj, "FlagV", i);
						cgel_morph.compute();
						System.out.println("Computed morphology.");
						System.out.printf("%d core patches, %d anticore patches, %d mortar patches.\n", cgel_morph.getNumberOfCorePatches(),cgel_morph.getNumberOfAnticorePatches(),cgel_morph.getNumberOfMortarPatches());

						System.out.println("recompute morpholgy from indicators.");
						cgel_optim.autofill_morphology();
						System.out.println("start simulated annealing.");
						cgel_optim.compute_SimulatedAnnealing();
						System.out.println("Computed optimization.");	
						
						mesh_obj_cgel.updateByUnmaskingIndicator(main_obj, "FlagT", active_category_id);
						mesh_obj_cgel.updateByUnmaskingIndicator(main_obj, "FlagV", active_category_id);
					}
					cgel_morph.dispose();
					cgel_morph=null;
					cgel_optim.dispose();
					cgel_optim=null;
				} else {
			        init_patch_obj_cgel.updateByMaskingIndicator(init_patch_obj, "FlagT", active_category_id);
			        init_patch_obj_cgel.updateByMaskingIndicator(init_patch_obj, "FlagV", active_category_id);
					
					//Mesh cgel_mesh = Mesh.from_vtkPolyData(main_obj);
					//Mesh cgel_mesh_opt = new Mesh(main_obj);
					mesh_obj_cgel.updateByMaskingIndicator(main_obj, "FlagT", active_category_id);
					mesh_obj_cgel.updateByMaskingIndicator(main_obj, "FlagV", active_category_id);
					Morphology cgel_morph = new Morphology();
					//cgel_morph.setSizeThresholdMode(typedefs.MEDIAN_AVG);
					cgel_morph.setSizeThresholdMode(typedefs.CONFIDENCE_95);
					//cgel_morph.setTriangleSizeMetric(typedefs.TRIANGLE_INCIRCLE_METRIC);
					//cgel_morph.setTriangleSizeMetric(typedefs.TRIANGLE_CIRCUMCIRCLE_METRIC);
					cgel_morph.setTriangleSizeMetric(typedefs.CURVATURE_METRIC);
					cgel_morph.enablePostprocessing();
					cgel_morph.setBaseMesh(mesh_obj_cgel);
					cgel_morph.setInitPatchMesh(init_patch_obj_cgel);
					cgel_morph.compute();
					//cgel_mesh_init.dispose();
					//cgel_mesh_init=null;
					System.out.println("Computed morphology.");
					System.out.printf("%d core patches, %d anticore patches, %d mortar patches.\n", cgel_morph.getNumberOfCorePatches(),cgel_morph.getNumberOfAnticorePatches(),cgel_morph.getNumberOfMortarPatches());
					
					Optimization cgel_optim = new Optimization();
					cgel_optim.setMesh(mesh_obj_cgel);
					cgel_optim.setInternalPriority(0.8);
					cgel_optim.setLengthWeight(0.4);
					cgel_optim.setIterationLimit(2200);
					cgel_optim.setStableIterationLimit(10);
					cgel_optim.setErrorLimit(0.000001);
					cgel_optim.enablePostprocessing();
					cgel_optim.activate_InternalExternalWeighting();
					cgel_optim.activate_LengthAngleWeighting();
					System.out.println("recompute morpholgy from indicators.");
					cgel_optim.autofill_morphology();
					System.out.println("start simulated annealing.");
					cgel_optim.compute_SimulatedAnnealing();
					System.out.println("Computed optimization.");
					
					//cgel_mesh_opt.update_vtkPolyData(main_obj);
					mesh_obj_cgel.updateByUnmaskingIndicator(main_obj, "FlagT", active_category_id);
					mesh_obj_cgel.updateByUnmaskingIndicator(main_obj, "FlagV", active_category_id);
					
					//cgel_mesh_opt.dispose();
					//cgel_mesh_opt=null;
					cgel_morph.dispose();
					cgel_morph=null;
					cgel_optim.dispose();
					cgel_optim=null;
				}
				
				main_obj.GetPointData().Modified();
				main_obj.GetCellData().Modified();
				rw.Render();
				
				
				dispose_refinement_context_area();
			}
			
			@Override
			public void widgetDefaultSelected(SelectionEvent arg0) {
				
				
			}
		});
		
		*/
		
		contextBar.setMinSize(contextContainer.computeSize(SWT.DEFAULT, SWT.DEFAULT));
		contextContainer.layout();
	}
	
	protected void dispose_refinement_context_area() {
		if((!contextContainer.isDisposed()) && (contextContainer!=null)) {
			for(Control cnt_elem : contextContainer.getChildren()) {
				cnt_elem.dispose();
				cnt_elem = null;
			}
			contextBar.setMinSize(contextContainer.computeSize(SWT.DEFAULT, SWT.DEFAULT));
			contextContainer.layout();
		}
	}
	
	protected void create_point_selection_button_area() {
		dispose_area_selection_button_area();
		dispose_line_selection_button_area();
		dispose_welllog_creator_button_area();

		addPatchButton = new Button(buttonContainer, SWT.PUSH);
		addPatchButton.setText("Add Point Seed");
		Point absize = addPatchButton.computeSize(SWT.DEFAULT, SWT.DEFAULT);
		addPatchButton.setSize(absize);
		addPatchButton.addSelectionListener(new SelectionListener() {
			
			@Override
			public void widgetSelected(SelectionEvent e) {
				//line_select_style
	    		if(point_select_style.GetEnabled()==0) {
	    			point_select_style.SetInteractor(swt_component.getRenderWindowInteractor());
	    			swt_component.setInteractorStyle(point_select_style);
	    			//point_select_style.SetInteractor(swt_widget.getRenderWindowInteractor());
	    			//swt_widget.setInteractorStyle(point_select_style);
	    			point_select_style.Enable();
	    			active_mode = 1;
	    			addPatchButton.setText("[ACTIVE] Adding");
	    		} else {
	    			point_select_style.Disable();
	    			mstyle.SetInteractor(swt_component.getRenderWindowInteractor());
	    			swt_component.setInteractorStyle(mstyle);
	    			//mstyle.SetInteractor(swt_widget.getRenderWindowInteractor());
	    			//swt_widget.setInteractorStyle(mstyle);
	    			active_mode = 0;
	    			addPatchButton.setText("Add Point Seed");
	    		}
			}
			
			@Override
			public void widgetDefaultSelected(SelectionEvent e) {
				
			}
		});
		
		subtractPatchButton = new Button(buttonContainer, SWT.PUSH);
		subtractPatchButton.setText("Undo");
		Point sbsize = subtractPatchButton.computeSize(SWT.DEFAULT, SWT.DEFAULT);
		subtractPatchButton.setSize(sbsize);
		subtractPatchButton.addSelectionListener(new SelectionListener() {
			
			@Override
			public void widgetSelected(SelectionEvent e) {
				point_select_style.undo();
			}
			
			@Override
			public void widgetDefaultSelected(SelectionEvent e) {
				
			}
		});
		
		buttonBar.setMinSize(buttonContainer.computeSize(SWT.DEFAULT, SWT.DEFAULT));
		buttonContainer.layout();
	}
	
	protected void dispose_point_selection_button_area() {
		if((point_select_style!=null) && (point_select_style.GetEnabled()>0)) {
			point_select_style.Disable();
			if(swt_component!=null) {
				mstyle.SetInteractor(swt_component.getRenderWindowInteractor());
				swt_component.setInteractorStyle(mstyle);
			}
			//if(swt_widget!=null) {
			//	mstyle.SetInteractor(swt_widget.getRenderWindowInteractor());
			//	swt_widget.setInteractorStyle(mstyle);
			//}
			if((addPatchButton!=null) && (!addPatchButton.isDisposed())) {
				addPatchButton.setText("Add Point Seed");
			}
			if((subtractPatchButton!=null) && (!subtractPatchButton.isDisposed())) {
				subtractPatchButton.setText("Undo");
			}
		}
		if(addPatchButton!=null) {
			addPatchButton.dispose();
			addPatchButton = null;
		}
		if(subtractPatchButton!=null) {
			subtractPatchButton.dispose();
			subtractPatchButton = null;
		}
	}
	
	protected void create_point_selection_context_area() {
		dispose_area_selection_context_area();
		dispose_line_selection_context_area();
		dispose_welllog_creator_context_area();
		
		//int ring_size = area_select_style.GetNRingDistance();
		//Label lbl_ring_size = new Label(contextContainer, SWT.PUSH);
		//lbl_ring_size.setText("selection ring size: ");
		//Label in_ring_size = new Label(contextContainer, SWT.PUSH);
		//in_ring_size.setText(Integer.toString(ring_size));

		contextBar.setMinSize(contextContainer.computeSize(SWT.DEFAULT, SWT.DEFAULT));
		contextContainer.layout();
	}
	
	protected void dispose_point_selection_context_area() {
		if((contextContainer!=null) && (!contextContainer.isDisposed())) {
			for(Control cnt_elem : contextContainer.getChildren()) {
				cnt_elem.dispose();
				cnt_elem = null;
			}
			contextBar.setMinSize(contextContainer.computeSize(SWT.DEFAULT, SWT.DEFAULT));
			contextContainer.layout();
		}
	}
	
	protected void create_line_selection_button_area() {
		dispose_area_selection_button_area();
		dispose_point_selection_button_area();
		dispose_welllog_creator_button_area();

		addPatchButton = new Button(buttonContainer, SWT.PUSH);
		addPatchButton.setText("Add to Line Seg.");
		Point absize = addPatchButton.computeSize(SWT.DEFAULT, SWT.DEFAULT);
		addPatchButton.setSize(absize);
		addPatchButton.addSelectionListener(new SelectionListener() {
			
			@Override
			public void widgetSelected(SelectionEvent e) {
				//line_select_style
	    		if(line_select_style.GetEnabled()==0) {
	    			line_select_style.SetInteractor(swt_component.getRenderWindowInteractor());
	    			swt_component.setInteractorStyle(line_select_style);
	    			//line_select_style.SetInteractor(swt_widget.getRenderWindowInteractor());
	    			//swt_widget.setInteractorStyle(line_select_style);
	    			line_select_style.Enable();
	    			active_mode = 2;
	    			addPatchButton.setText("[ACTIVE] Adding");
	    		} else {
	    			line_select_style.Disable();
	    			mstyle.SetInteractor(swt_component.getRenderWindowInteractor());
	    			swt_component.setInteractorStyle(mstyle);
	    			//mstyle.SetInteractor(swt_widget.getRenderWindowInteractor());
	    			//swt_widget.setInteractorStyle(mstyle);
	    			active_mode = 0;
	    			addPatchButton.setText("Add to Line Seg.");
	    		}
			}
			
			@Override
			public void widgetDefaultSelected(SelectionEvent e) {
				
			}
		});
		
		subtractPatchButton = new Button(buttonContainer, SWT.PUSH);
		subtractPatchButton.setText("Undo");
		Point sbsize = subtractPatchButton.computeSize(SWT.DEFAULT, SWT.DEFAULT);
		subtractPatchButton.setSize(sbsize);
		subtractPatchButton.addSelectionListener(new SelectionListener() {
			
			@Override
			public void widgetSelected(SelectionEvent e) {
				line_select_style.undo();
			}
			
			@Override
			public void widgetDefaultSelected(SelectionEvent e) {
				
			}
		});
		
		buttonBar.setMinSize(buttonContainer.computeSize(SWT.DEFAULT, SWT.DEFAULT));
		buttonContainer.layout();
	}
	
	protected void dispose_line_selection_button_area() {
		if((line_select_style!=null) && (line_select_style.GetEnabled()>0)) {
			line_select_style.Disable();
			if(swt_component!=null) {
				mstyle.SetInteractor(swt_component.getRenderWindowInteractor());
				swt_component.setInteractorStyle(mstyle);
			}
			//if(swt_widget!=null) {
			//	mstyle.SetInteractor(swt_widget.getRenderWindowInteractor());
			//	swt_widget.setInteractorStyle(mstyle);
			//}
			if((addPatchButton!=null) && (!addPatchButton.isDisposed())) {
				addPatchButton.setText("Add to Line Seg.");
			}
			if((subtractPatchButton!=null) && (!subtractPatchButton.isDisposed())) {
				subtractPatchButton.setText("Undo");
			}
		}
		if(addPatchButton!=null) {
			addPatchButton.dispose();
			addPatchButton = null;
		}
		if(subtractPatchButton!=null) {
			subtractPatchButton.dispose();
			subtractPatchButton = null;
		}
	}
	
	protected void create_line_selection_context_area() {
		dispose_area_selection_context_area();
		dispose_point_selection_context_area();
		dispose_welllog_creator_context_area();
		
		//int ring_size = area_select_style.GetNRingDistance();
		//Label lbl_ring_size = new Label(contextContainer, SWT.PUSH);
		//lbl_ring_size.setText("selection ring size: ");
		//Label in_ring_size = new Label(contextContainer, SWT.PUSH);
		//in_ring_size.setText(Integer.toString(ring_size));

		contextBar.setMinSize(contextContainer.computeSize(SWT.DEFAULT, SWT.DEFAULT));
		contextContainer.layout();
	}
	
	protected void dispose_line_selection_context_area() {
		if((contextContainer!=null) && (!contextContainer.isDisposed())) {
			for(Control cnt_elem : contextContainer.getChildren()) {
				cnt_elem.dispose();
				cnt_elem = null;
			}
			contextBar.setMinSize(contextContainer.computeSize(SWT.DEFAULT, SWT.DEFAULT));
			contextContainer.layout();
		}
	}
	
	protected void create_area_selection_button_area() {
		dispose_point_selection_button_area();
		dispose_line_selection_button_area();
		dispose_welllog_creator_button_area();
		
		//GridData gridData;
		addPatchButton = new Button(buttonContainer, SWT.PUSH);
		addPatchButton.setText("Add to Patch");
		Point absize = addPatchButton.computeSize(SWT.DEFAULT, SWT.DEFAULT);
		addPatchButton.setSize(absize);
		addPatchButton.addSelectionListener(new SelectionListener() {
			
			@Override
			public void widgetSelected(SelectionEvent e) {
				
				area_select_style.AddMode();
	    		if(area_select_style.GetEnabled()==0) {
	    			area_select_style.SetInteractor(swt_component.getRenderWindowInteractor());
	    			swt_component.setInteractorStyle(area_select_style);
	    			//area_select_style.SetInteractor(swt_widget.getRenderWindowInteractor());
	    			//swt_widget.setInteractorStyle(area_select_style);
	    			area_select_style.Enable();
	    			addPatchButton.setText("[ACTIVE] Adding");
	    		} else {
	    			area_select_style.Disable();
	    			mstyle.SetInteractor(swt_component.getRenderWindowInteractor());
	    			swt_component.setInteractorStyle(mstyle);
	    			//mstyle.SetInteractor(swt_widget.getRenderWindowInteractor());
	    			//swt_widget.setInteractorStyle(mstyle);
	    			addPatchButton.setText("Add to Patch");
	    		}
			}
			
			@Override
			public void widgetDefaultSelected(SelectionEvent e) {
				
				
			}
		});
		
		subtractPatchButton = new Button(buttonContainer, SWT.PUSH);
		subtractPatchButton.setText("Subtract from Patch");
		Point sbsize = subtractPatchButton.computeSize(SWT.DEFAULT, SWT.DEFAULT);
		subtractPatchButton.setSize(sbsize);
		subtractPatchButton.addSelectionListener(new SelectionListener() {
			
			@Override
			public void widgetSelected(SelectionEvent e) {
				
				area_select_style.SubtractMode();
	    		if(area_select_style.GetEnabled()==0) {
	    			area_select_style.SetInteractor(swt_component.getRenderWindowInteractor());
	    			swt_component.setInteractorStyle(area_select_style);
	    			//area_select_style.SetInteractor(swt_widget.getRenderWindowInteractor());
	    			//swt_widget.setInteractorStyle(area_select_style);
	    			area_select_style.Enable();
	    			active_mode = 3;
	    			subtractPatchButton.setText("[ACTIVE] Subtracting");
	    		} else {
	    			area_select_style.Disable();
	    			mstyle.SetInteractor(swt_component.getRenderWindowInteractor());
	    			swt_component.setInteractorStyle(mstyle);
	    			//mstyle.SetInteractor(swt_widget.getRenderWindowInteractor());
	    			//swt_widget.setInteractorStyle(mstyle);
	    			active_mode = 0;
	    			subtractPatchButton.setText("Subtract from Patch");
	    		}
			}
			
			@Override
			public void widgetDefaultSelected(SelectionEvent e) {
				
				
			}
		});
		
		buttonBar.setMinSize(buttonContainer.computeSize(SWT.DEFAULT, SWT.DEFAULT));
		buttonContainer.layout();
	}
	
	protected void dispose_area_selection_button_area() {
		if((area_select_style!=null) && (area_select_style.GetEnabled()>0)) {
			area_select_style.Disable();
			if(swt_component!=null) {
				mstyle.SetInteractor(swt_component.getRenderWindowInteractor());
				swt_component.setInteractorStyle(mstyle);
			}
			//if(swt_widget!=null) {
			//	mstyle.SetInteractor(swt_widget.getRenderWindowInteractor());
			//	swt_widget.setInteractorStyle(mstyle);
			//}
			if((addPatchButton!=null) && (!addPatchButton.isDisposed())) {
				addPatchButton.setText("Add to Patch");
			}
			if((subtractPatchButton!=null) && (!subtractPatchButton.isDisposed())) {
				subtractPatchButton.setText("Subtract from Patch");
			}
		}
		if(addPatchButton!=null) {
			addPatchButton.dispose();
			addPatchButton = null;
		}
		if(subtractPatchButton!=null) {
			subtractPatchButton.dispose();
			subtractPatchButton = null;
		}
	}
	
	protected void create_area_selection_context_area() {
		dispose_point_selection_context_area();
		dispose_line_selection_context_area();
		dispose_welllog_creator_context_area();
		
		List<Image> imageList = new ArrayList<Image>();
		
		int ring_size = area_select_style.GetNRingDistance();
		Label lbl_ring_size = new Label(contextContainer, SWT.PUSH);
		lbl_ring_size.setText("selection ring size: ");
		Label in_ring_size = new Label(contextContainer, SWT.PUSH);
		in_ring_size.setText(Integer.toString(ring_size));
		
		Button btn_raise_ring_size = new Button(contextContainer, SWT.PUSH);
		btn_raise_ring_size.setText("+");
		//Point absize = btn_raise_ring_size.computeSize(SWT.DEFAULT, SWT.DEFAULT);
		//btn_raise_ring_size.setSize(absize);
		btn_raise_ring_size.addSelectionListener(new SelectionListener() {
			
			@Override
			public void widgetSelected(SelectionEvent e) {
				int n_ring_size = area_select_style.GetNRingDistance();
				n_ring_size = Math.max(0, n_ring_size+1);
				if(n_ring_size>0) {
					area_select_style.UseNRingDistanceConstraint(n_ring_size);
				} else {
					area_select_style.DisuseNRingDistanceConstraint();
				}
				in_ring_size.setText(Integer.toString(n_ring_size));

				if((out_size_illustration!=null) && (imageList!=null)) {
					out_size_illustration.setImage(imageList.get(Math.max(0, Math.min(imageList.size()-1, n_ring_size))));
					out_size_illustration.update();
					out_size_illustration.getParent().layout();
				}
			}
			
			@Override
			public void widgetDefaultSelected(SelectionEvent e) {
				
			}
		});
		Button btn_lower_ring_size = new Button(contextContainer, SWT.PUSH);
		btn_lower_ring_size.setText("-");
		//absize = btn_lower_ring_size.computeSize(SWT.DEFAULT, SWT.DEFAULT);
		//btn_lower_ring_size.setSize(absize);
		btn_lower_ring_size.addSelectionListener(new SelectionListener() {
			
			@Override
			public void widgetSelected(SelectionEvent arg0) {
				int n_ring_size = area_select_style.GetNRingDistance();
				n_ring_size = Math.max(0, n_ring_size-1);
				if(n_ring_size>0) {
					area_select_style.UseNRingDistanceConstraint(n_ring_size);
				} else {
					area_select_style.DisuseNRingDistanceConstraint();
				}
				in_ring_size.setText(Integer.toString(n_ring_size));

				if((out_size_illustration!=null) && (imageList!=null)) {
					out_size_illustration.setImage(imageList.get(Math.max(0, Math.min(imageList.size()-1, n_ring_size))));
					out_size_illustration.update();
					out_size_illustration.getParent().layout();
				}
			}
			
			@Override
			public void widgetDefaultSelected(SelectionEvent arg0) {
				
			}
		});
		
		Button btn_toggle_constraint = new Button(contextContainer, SWT.PUSH);
		btn_toggle_constraint.setText("Toggle radial constraint");
		btn_toggle_constraint.addSelectionListener(new SelectionListener() {
			
			@Override
			public void widgetSelected(SelectionEvent arg0) {
				
				area_select_style.ToggleDistanceConstraint();
			}
			
			@Override
			public void widgetDefaultSelected(SelectionEvent arg0) {
				
				
			}
		});
		
		//Label lbl_fill_01 = new Label(contextContainer, SWT.PUSH);
		new Label(contextContainer, SWT.PUSH);
		
		//imageList.add(new Image(Display.getDefault(), "/home/christian/ic_uib-web.png"));
		try {
			imageList.add(new Image(display, getClass().getClassLoader().getResourceAsStream("assets/1.jpg")));
			imageList.add(new Image(display, getClass().getClassLoader().getResourceAsStream("assets/2.jpg")));
			imageList.add(new Image(display, getClass().getClassLoader().getResourceAsStream("assets/3.jpg")));
			imageList.add(new Image(display, getClass().getClassLoader().getResourceAsStream("assets/4p.jpg")));
		} catch(Exception e) {
			imageList.add(new Image(display, 100, 100));
		}
		shell.addListener(SWT.Close, new Listener() {
			@Override
			public void handleEvent(Event event) {
				for(Image item : imageList) {
					item.dispose();
				}
			}
		});
		
		Label lbl_size_illustration = new Label(contextContainer, SWT.PUSH);
		lbl_size_illustration.setText("size");
		out_size_illustration = new Label(contextContainer, SWT.PUSH);
		out_size_illustration.setImage(imageList.get(Math.max(0, ring_size)));
		
		
		contextBar.setMinSize(contextContainer.computeSize(SWT.DEFAULT, SWT.DEFAULT));
		contextContainer.layout();
	}
	
	protected void dispose_area_selection_context_area() {
		if((contextContainer!=null) && (!contextContainer.isDisposed())) {
			for(Control cnt_elem : contextContainer.getChildren()) {
				cnt_elem.dispose();
				cnt_elem = null;
			}
			contextBar.setMinSize(contextContainer.computeSize(SWT.DEFAULT, SWT.DEFAULT));
			contextContainer.layout();
		}
		//if(contextBar!=null) {
		//	contextBar.dispose();
		//	contextBar=null;
		//}
		//if(contextContainer!=null) {
		//	contextContainer.dispose();
		//	contextContainer=null;
		//}
		//create_empty_context_options(bottom);
	}
	
	
	
	
	@Override
	public synchronized void UpdateSampleSize() {
		sample_size_wellLog = welllog_create_style.GetSampleSize();
		if(lbl_sample_size!=null) {
			if(display==null)
				display = Display.getDefault();
			display.asyncExec(new Runnable() {
				@Override
				public void run() {
					lbl_sample_size.setText(String.format("%.6f", sample_size_wellLog));
					lbl_sample_size.pack();
				}
			});
		}
	}
	protected void create_welllog_creator_button_area() {
		dispose_area_selection_button_area();
		dispose_point_selection_button_area();
		dispose_line_selection_button_area();

		addPatchButton = new Button(buttonContainer, SWT.PUSH);
		addPatchButton.setText("Add (pseudo-)Well Log");
		Point absize = addPatchButton.computeSize(SWT.DEFAULT, SWT.DEFAULT);
		addPatchButton.setSize(absize);
		addPatchButton.addSelectionListener(new SelectionListener() {
			
			@Override
			public void widgetSelected(SelectionEvent e) {
				//line_select_style
	    		if(welllog_create_style.GetEnabled()==0) {
	    			welllog_create_style.SetInteractor(swt_component.getRenderWindowInteractor());
	    			sample_size_wellLog = welllog_create_style.GetSampleSize();
	    			swt_component.setInteractorStyle(welllog_create_style);
	    			//line_select_style.SetInteractor(swt_widget.getRenderWindowInteractor());
	    			//swt_widget.setInteractorStyle(line_select_style);
	    			welllog_create_style.Enable();
	    			active_mode = 4;
	    			addPatchButton.setText("[ACTIVE] Adding");
	    		} else {
	    			welllog_create_style.Disable();
	    			mstyle.SetInteractor(swt_component.getRenderWindowInteractor());
	    			swt_component.setInteractorStyle(mstyle);
	    			//mstyle.SetInteractor(swt_widget.getRenderWindowInteractor());
	    			//swt_widget.setInteractorStyle(mstyle);
	    			active_mode = 0;
	    			addPatchButton.setText("Add (pseudo-)Well Log");
	    		}
			}
			
			@Override
			public void widgetDefaultSelected(SelectionEvent e) {
				
			}
		});
		
		subtractPatchButton = new Button(buttonContainer, SWT.PUSH);
		subtractPatchButton.setText("Undo");
		Point sbsize = subtractPatchButton.computeSize(SWT.DEFAULT, SWT.DEFAULT);
		subtractPatchButton.setSize(sbsize);
		subtractPatchButton.addSelectionListener(new SelectionListener() {
			
			@Override
			public void widgetSelected(SelectionEvent e) {
				if((welllog_create_style!=null) && (welllog_create_style.GetEnabled()>0)) {
					welllog_create_style.undo();
				}
			}
			
			@Override
			public void widgetDefaultSelected(SelectionEvent e) {
				
			}
		});
		
		buttonBar.setMinSize(buttonContainer.computeSize(SWT.DEFAULT, SWT.DEFAULT));
		buttonContainer.layout();
	}
	protected void dispose_welllog_creator_button_area() {
		if((welllog_create_style!=null) && (welllog_create_style.GetEnabled()>0)) {
			welllog_create_style.Disable();
			if(swt_component!=null) {
				mstyle.SetInteractor(swt_component.getRenderWindowInteractor());
				swt_component.setInteractorStyle(mstyle);
			}
			//if(swt_widget!=null) {
			//	mstyle.SetInteractor(swt_widget.getRenderWindowInteractor());
			//	swt_widget.setInteractorStyle(mstyle);
			//}
			if((addPatchButton!=null) && (!addPatchButton.isDisposed())) {
				addPatchButton.setText("Add (pseudo-)Well Log");
			}
			if((subtractPatchButton!=null) && (!subtractPatchButton.isDisposed())) {
				subtractPatchButton.setText("Undo");
			}
		}
		if(addPatchButton!=null) {
			addPatchButton.dispose();
			addPatchButton = null;
		}
		if(subtractPatchButton!=null) {
			subtractPatchButton.dispose();
			subtractPatchButton = null;
		}
	}
	protected void create_welllog_creator_context_area() {
		dispose_area_selection_context_area();
		dispose_point_selection_context_area();
		dispose_line_selection_context_area();
		
		Label lbl_sample_distance_metric = new Label(contextContainer, SWT.PUSH);
		lbl_sample_distance_metric.setText("Sampling distance [m]: ");
		lbl_sample_size = new Label(contextContainer, SWT.PUSH);
		lbl_sample_size.setText(Double.toString(sample_size_wellLog));
		
		Label lbl_wellLogNames = new Label(contextContainer, SWT.PUSH);
		lbl_wellLogNames.setText("(pseudo)- Well Log Names: ");
		// here: combined table-and-button container //
		SashForm wellListContainer = new SashForm(contextContainer, SWT.PUSH|SWT.BORDER|SWT.VERTICAL);
		//Composite wellListContainer = new Composite(contextContainer, SWT.PUSH|SWT.BORDER|SWT.VERTICAL);
		wellListContainer.layout();
		wellLog_listview = new org.eclipse.swt.widgets.List(wellListContainer, SWT.V_SCROLL);
		wellLog_listview.setSize(100, 140);
		wellLog_listview.addSelectionListener(new SelectionListener() {
			@Override
			public void widgetSelected(SelectionEvent arg0) {
				selected_well_id = wellLog_listview.getSelectionIndex();
				if( (selected_well_id < 0) || (selected_well_id > (wellLog_listview.getItemCount()-1)) )
					return;
			    if(welllog_create_style!=null) {
			    	// TODO: interact with the interactor, if required
			    }
			}

			@Override
			public void widgetDefaultSelected(SelectionEvent arg0) { }
		});
		if( (wellLogNames!=null) && !wellLogNames.isEmpty()) {
			for(String name : wellLogNames) {
				wellLog_listview.add(name);
			}
		}

		editWellLogButton = new Button(wellListContainer, SWT.PUSH|SWT.BORDER);
		editWellLogButton.setText("Edit");
		editWellLogButton.addSelectionListener(new SelectionListener() {
			@Override
			public void widgetSelected(SelectionEvent arg0) {
				if((selected_well_id<0) && (selected_well_id>(wellLogNames.size()-1)))
					return;
				String name = wellLogNames.get(selected_well_id);
				InputDialog dlg = new InputDialog(shell);
				dlg.setText("Change well log name");
				dlg.setMessage("Well log name: ");
				dlg.setInput(name);
				String newName = dlg.open();
				if(newName!=null) {
					wellLogNames.set(selected_well_id, newName);
					wellLog_listview.setItem(selected_well_id, newName);
				}
				wellLog_listview.deselectAll();
			}

			@Override
			public void widgetDefaultSelected(SelectionEvent arg0) {
				
			}
		});
		wellListContainer.setWeights(new int[] {7,3});
		

		contextBar.setMinSize(contextContainer.computeSize(SWT.DEFAULT, SWT.DEFAULT));
		contextContainer.layout();
	}
	protected void dispose_welllog_creator_context_area() {
		if((contextContainer!=null) && (!contextContainer.isDisposed())) {
			for(Control cnt_elem : contextContainer.getChildren()) {
				cnt_elem.dispose();
				cnt_elem = null;
			}
			contextBar.setMinSize(contextContainer.computeSize(SWT.DEFAULT, SWT.DEFAULT));
			contextContainer.layout();
		}
	}
	
	public void disable_3D_interaction() {
    	if( (mstyle!=null) && (active_mode==0) && (mstyle.GetEnabled()>0) )
    		mstyle.EnabledOff();
    	if( (point_select_style!=null) && (active_mode==1) && (point_select_style.GetEnabled()>0) )
    		point_select_style.Disable();
    	if( (line_select_style!=null) && (active_mode==2) && (line_select_style.GetEnabled()>0) )
    		line_select_style.Disable();
    	if( (area_select_style!=null) && (active_mode==3) && (area_select_style.GetEnabled()>0) )
    		area_select_style.Disable();
    	if( (welllog_create_style!=null) && (active_mode==4) && (welllog_create_style.GetEnabled()>0) )
    		welllog_create_style.Disable();
    	if(swt_component!=null) {
	    	swt_component.setInteractorStyle(null);
	    	swt_component.setEnabled(false);
    	}
	}
	
	public void enable_3D_interaction() {
    	if((mstyle!=null) && (active_mode==0) && (mstyle.GetEnabled()==0)) {
	    	swt_component.setInteractorStyle(mstyle);
    		mstyle.EnabledOn();
    	}
    	if((point_select_style!=null) && (active_mode==1) && (point_select_style.GetEnabled()==0)) {
	    	swt_component.setInteractorStyle(point_select_style);
    		point_select_style.Disable();
    	}
    	if((line_select_style!=null) && (active_mode==2) && (line_select_style.GetEnabled()==0)) {
	    	swt_component.setInteractorStyle(line_select_style);
    		line_select_style.Disable();
    	}
    	if((area_select_style!=null) && (active_mode==3) && (area_select_style.GetEnabled()==0)) {
	    	swt_component.setInteractorStyle(area_select_style);
    		area_select_style.Disable();
    	}
    	if((welllog_create_style!=null) && (active_mode==4) && (welllog_create_style.GetEnabled()==0)) {
	    	swt_component.setInteractorStyle(welllog_create_style);
    		welllog_create_style.Disable();
    	}
    	if(swt_component!=null) {
	    	swt_component.setEnabled(true);
    	}
	}
	
	/**
	 * Launch the application.
	 * @param args
	 */
	public static void main(String[] args) {
		// === ENABLE TO ACTIVELY LOAD LIBRARIES === //
		initLibraries();
		CGEL.common.misc.Generics.CGEL_XInitThreads();
		SWT_AWT_VTK_sample window = null;
		try {
			window = new SWT_AWT_VTK_sample();
			window.open();
		} catch (Exception e) {
			e.printStackTrace();
		}
		finally
		{
			if(window!=null)
				window.close();
		}
	}

	@Override
	public vtkPolyData GetSurfaceData() {
		return main_obj;
	}

	@Override
	public void AddItem() {
		if(wellLog_listview!=null) {
			if(display==null)
				display = Display.getDefault();
			display.asyncExec(new Runnable() {
				@Override
				public void run() {
					int index = wellLog_listview.getItemCount();
					String wellName = "Well"+Integer.toString(index);
					wellLogNames.add(wellName);
					wellLog_listview.add(wellName);
				}
			});
		}
	}

	@Override
	public void RemoveLastItem() {
		if(wellLog_listview!=null) {
			if(display==null)
				display = Display.getDefault();
			display.asyncExec(new Runnable() {
				@Override
				public void run() {
					int size = wellLog_listview.getItemCount();
					if(size > 0) {
						int index = size-1;
						wellLogNames.remove(index);
						wellLog_listview.remove(index);
					}
				}
			});
		}
	}
}
