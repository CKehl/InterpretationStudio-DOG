package ckehl.vtk;

import vtk.vtkActor;
import vtk.vtkInteractorStyle;
import vtk.vtkPolyDataMapper;
//import vtk.vtkPropPicker;
import vtk.vtkPointPicker;
import vtk.vtkCellPicker;
//import vtk.vtkRenderWindowInteractor;
//import vtk.vtkRenderer;
import vtk.vtkSphereSource;
import vtk.vtkTriangle;
//import vtk.vtkPolyData;
import vtk.vtkCell;
//import vtk.vtkPoints;
import vtk.vtkShortArray;
import vtk.vtkIdList;
import vtk.vtkMath;

//import java.util.ArrayList;
import java.util.Deque;
import java.util.LinkedList;
import java.util.Queue;

public class AreaPickInteractor extends vtkInteractorStyle {

	protected vtkActor pickposActor = null;
	protected vtkPolyDataMapper pickposMapper = null;
	protected int id_LeftButtonDown = 0;
	protected int id_LeftButtonUp = 0;
	protected int id_MouseMove = 0;
	protected int id_KeyPress = 0;
	protected boolean left_button_pressed = false;
	protected vtkPointPicker point_picker = null;
	protected vtkCellPicker cell_picker = null;
	protected SurfaceDataInterface data_interface = null;
	protected double pointer_size = 0.025;
	protected int ring_size = 0;
	protected double ring_radius = pointer_size;
	protected boolean use_distance_constraint = false;
	protected int active_category_number = 1;
	
	protected int area_selection_mode = 0;	// 0 - add; 1 - subtract
	
	
	public AreaPickInteractor() {
		// TODO Auto-generated constructor stub
		//System.out.println("MousePickInteractor created");
		super();
		point_picker = new vtkPointPicker();
		cell_picker = new vtkCellPicker();
	}
	
	@Override
	protected void finalize() throws Throwable {
		// TODO Auto-generated method stub
		point_picker = null;
		cell_picker = null;
		super.finalize();
	}
	
	public void SetPickConstraint_Actor(vtkActor constraint) {
		if(point_picker==null)
			point_picker = new vtkPointPicker();
		point_picker.PickFromListOn();
		point_picker.InitializePickList();
		point_picker.AddPickList(constraint);

		if(cell_picker==null)
			cell_picker = new vtkCellPicker();
		cell_picker.PickFromListOn();
		cell_picker.InitializePickList();
		cell_picker.AddPickList(constraint);
	}
	
	public void SetActiveCategoryNumber(int category_number) {
		active_category_number = category_number;
	}
	
	public int GetActiveCategoryNumber() {
		return active_category_number;
	}
	
	public void register(SurfaceDataInterface sdi) {
		// TODO Auto-generated method stub
		data_interface = sdi;
	}
	
	public void Enable() {
		id_LeftButtonDown = GetInteractor().AddObserver("LeftButtonPressEvent", this, "OnLeftButtonDown");
		id_LeftButtonUp = GetInteractor().AddObserver("LeftButtonReleaseEvent", this, "OnLeftButtonUp");
		id_MouseMove = GetInteractor().AddObserver("MouseMoveEvent", this, "OnMouseMove");
		id_KeyPress = GetInteractor().AddObserver("KeyPressEvent", this, "OnKeyDown");
		EnabledOn();
		On();
	}
	
	public void Disable() {
		//GetDefaultRenderer().RemoveActor(pickposActor);
		pickposActor = null;
		pickposMapper = null;
		left_button_pressed=false;
		
		if(id_LeftButtonDown!=0) {
			GetInteractor().RemoveObserver(id_LeftButtonDown);
			id_LeftButtonDown=0;
		}
		if(id_LeftButtonUp!=0) {
			GetInteractor().RemoveObserver(id_LeftButtonUp);
			id_LeftButtonUp=0;
		}
		if(id_MouseMove!=0) {
			GetInteractor().RemoveObserver(id_MouseMove);
			id_MouseMove=0;
		}
		if(id_KeyPress!=0) {
			GetInteractor().RemoveObserver(id_KeyPress);
			id_KeyPress=0;
		}
		EnabledOff();
		Off();
	}
	
	public void Toggle() {
		if(GetEnabled()==0) {
			Enable();
		} else {
			Disable();
		}
	}
	
	public void ToggleDistanceConstraint() {
		if(use_distance_constraint==false) {
			EnableDistanceConstraint();
		} else {
			DisableDistanceConstraint();
		}
	}
	
	public void EnableDistanceConstraint() {
		use_distance_constraint=true;
		System.out.println("use radial constraint");
	}
	
	public void DisableDistanceConstraint() {
		use_distance_constraint=false;
	}
	
	public int GetNRingDistance() {
		return ring_size;
	}
	
	public void UseNRingDistanceConstraint(int n_ring_size) {
		ring_size = n_ring_size;
	}
	
	public void DisuseNRingDistanceConstraint() {
		ring_size = 0;
	}
	
	public void AddMode() {
		area_selection_mode = 0;
	}
	
	public void SubtractMode() {
		area_selection_mode = 1;
	}
	
	public void ToggleMode() {
		area_selection_mode = (area_selection_mode+1)%2;
	}
	
	@Override
	public void OnKeyPress() {
		// TODO Auto-generated method stub
		super.OnKeyPress();
	}
	
	@Override
	public void OnKeyDown() {
		// TODO Auto-generated method stub
		//System.out.println("MousePickInteractor key pressed.");
    	char code = Character.toLowerCase(GetInteractor().GetKeyCode());
    	switch(code) {
    	case '+': {
    		pointer_size /= 0.95;
    		break;
    	}
    	case '-': {
    		pointer_size *= 0.95;
    		break;
    	}
    	}
		super.OnKeyDown();
	}
	
	@Override
	public void OnLeftButtonDown() {
		// TODO Auto-generated method stub
		//System.out.println("Mouse button pressed.");
		left_button_pressed = true;
		int clickPos[] = GetInteractor().GetEventPosition();
		point_picker.Pick(Double.valueOf(clickPos[0]), Double.valueOf(clickPos[1]), 0, GetDefaultRenderer());
		
		double pos[] = point_picker.GetPickPosition();
		//System.out.printf("picked pos: %f, %f, %f\n", pos[0], pos[1], pos[2]);
		
		vtkSphereSource sphereSource = new vtkSphereSource();
		sphereSource.SetCenter(pos[0], pos[1], pos[2]);
		sphereSource.SetRadius(pointer_size);
		sphereSource.Update();
		
		if(pickposMapper==null)
			pickposMapper = new vtkPolyDataMapper();
		pickposMapper.SetInputData(sphereSource.GetOutput());
		
		if(pickposActor==null) {
			pickposActor = new vtkActor();
		}
		pickposActor.SetMapper(pickposMapper);
		
		GetDefaultRenderer().AddActor(pickposActor);
		
		GetInteractor().GetRenderWindow().Render();
		//System.out.printf("left button pressed: %b\n", left_button_pressed);
		super.OnLeftButtonDown();
	}
	
	@Override
	public void OnLeftButtonUp() {
		// TODO Auto-generated method stub
		left_button_pressed = false;
		GetDefaultRenderer().RemoveActor(pickposActor);
		
		GetInteractor().GetRenderWindow().Render();
		//System.out.printf("left button released: %b\n", left_button_pressed);
		super.OnLeftButtonUp();
	}
	
	
	
	@Override
	public void OnMouseMove() {
		// TODO Auto-generated method stub
		if(left_button_pressed==true) {
			int clickPos[] = GetInteractor().GetEventPosition();
			cell_picker.SetTolerance(0.00001);
			cell_picker.Pick(Double.valueOf(clickPos[0]), Double.valueOf(clickPos[1]), 0, GetDefaultRenderer());
			
			double worldPosition[] = cell_picker.GetPickPosition();
			int cell_id = cell_picker.GetCellId();
			//System.out.printf("Cell id is: %d\n", cell_id);

			if(cell_id>=0) {
				ring_radius = pointer_size;
			//	System.out.printf("picked pos: %f, %f, %f\n", worldPosition[0], worldPosition[1], worldPosition[2]);
				Queue<Integer> cell_ids = new LinkedList<Integer>();
				cell_ids.add(cell_id);
				Deque<Integer> blacklist_ids = new LinkedList<Integer>();
				vtkCell ref_cell = data_interface.GetSurfaceData().GetCell(cell_id);
				double incenter[] = new double[3];
				vtkTriangle refTriCell = new vtkTriangle(ref_cell.GetVTKId());
				refTriCell.TriangleCenter(refTriCell.GetPoints().GetPoint(0), refTriCell.GetPoints().GetPoint(1), refTriCell.GetPoints().GetPoint(2), incenter);
				
				int current_ring=0;
				//while((!cell_ids.isEmpty()) && (current_ring <= ring_size)) {
				while(!cell_ids.isEmpty()) {
					int current_cell_id = cell_ids.remove();
					blacklist_ids.add(current_cell_id);

					vtkSphereSource sphereSource = new vtkSphereSource();
					sphereSource.SetCenter(worldPosition[0], worldPosition[1], worldPosition[2]);
					sphereSource.SetRadius(pointer_size);
					sphereSource.Update();
					if(pickposMapper!=null)
						pickposMapper.SetInputData(sphereSource.GetOutput());
	
					vtkCell cell_data = data_interface.GetSurfaceData().GetCell(current_cell_id);
					vtkIdList point_ids = cell_data.GetPointIds();
					
					try {
						/*
						 * Get the cell's indicator data array
						 */
			//			vtkShortArray cell_indicators = new vtkShortArray().FastDownCast(data_interface.GetSurfaceData().GetCellData().GetArray("FlagT"));
						vtkShortArray cell_indicators = new vtkShortArray(data_interface.GetSurfaceData().GetCellData().GetArray("FlagT").GetVTKId());
			//			System.out.printf("# cells allocated: %d\n", cell_indicators.GetNumberOfTuples());
						/* 
						 * Set the selected cell's indicator
						 */
						if(area_selection_mode==0) {
							cell_indicators.SetValue(current_cell_id, active_category_number);	// SegFault break
							//cell_indicators.SetValue(current_cell_id, 1);	// SegFault break
							//cell_indicators.SetTuple1(cell_id, Double.valueOf(1));
							//cell_indicators.SetTuple1(cell_id, (double)1);
						} else if(area_selection_mode==1) {
							cell_indicators.SetValue(current_cell_id, 0);	// SegFault break
							//cell_indicators.SetTuple1(cell_id, Double.valueOf(0));
							//cell_indicators.SetTuple1(cell_id, (double)0);
						}
						
						/* 
						 * Get the point indicator data array
						 */
			//			vtkShortArray point_indicators = new vtkShortArray().FastDownCast(data_interface.GetSurfaceData().GetPointData().GetArray("FlagV"));
						vtkShortArray point_indicators = new vtkShortArray(data_interface.GetSurfaceData().GetPointData().GetArray("FlagV").GetVTKId());
			//			System.out.printf("# points allocated: %d\n", point_indicators.GetNumberOfTuples());
						/*
						 * Set the selected points' indicators
						 */
						for(int i=0; i<point_ids.GetNumberOfIds(); i++) {
							int point_id = point_ids.GetId(i);
							if(area_selection_mode==0) {
								point_indicators.SetValue(point_id, active_category_number);
								//point_indicators.SetValue(point_id, 1);
								//point_indicators.SetTuple1(point_id, Double.valueOf(1));
								//point_indicators.SetTuple1(point_id, (double)1);
							} else if(area_selection_mode==1) {
								point_indicators.SetValue(point_id, 0);
								//point_indicators.SetTuple1(point_id, Double.valueOf(0));
								//point_indicators.SetTuple1(point_id, (double)0);
							}
						}
						/*
						 * optional: re-set the data arrays
						 */
						data_interface.GetSurfaceData().GetPointData().Modified();
						data_interface.GetSurfaceData().GetCellData().Modified();
					} catch (NullPointerException e) {
						// TODO: handle exception
					}
					
					if((!use_distance_constraint) && (current_ring>=ring_size))
						break;
					
					vtkIdList cellPointIds = new vtkIdList();
					data_interface.GetSurfaceData().GetCellPoints(current_cell_id, cellPointIds);
			//		System.out.printf("Cell-ID: %d, # pts: %d\n", current_cell_id, cellPointIds.GetNumberOfIds());
					
					
					for(int pid = 0; pid < cellPointIds.GetNumberOfIds(); pid++ ) {
						vtkIdList edge_vertex_ids = new vtkIdList();
						edge_vertex_ids.InsertNextId(cellPointIds.GetId(pid));
						if((pid+1) == cellPointIds.GetNumberOfIds()) {
							edge_vertex_ids.InsertNextId(cellPointIds.GetId(0));
						} else {
							edge_vertex_ids.InsertNextId(cellPointIds.GetId(pid+1));
						}
						vtkIdList neighbour_cell_ids = new vtkIdList();
						data_interface.GetSurfaceData().GetCellNeighbors(current_cell_id, edge_vertex_ids, neighbour_cell_ids);
			//			System.out.printf("Cell-ID: %d, # nbrs: %d\n", current_cell_id, neighbour_cell_ids.GetNumberOfIds());

						for(int neighbour_id_idx = 0; neighbour_id_idx < neighbour_cell_ids.GetNumberOfIds(); neighbour_id_idx++) {
							int nbr_id = neighbour_cell_ids.GetId(neighbour_id_idx);
			//				System.out.printf("  nbr. Cell-ID: %d\n", nbr_id);
							
							if((blacklist_ids.contains(nbr_id)==true) || (cell_ids.contains(nbr_id)==true))
								continue;
							
							vtkCell nbr_cell = data_interface.GetSurfaceData().GetCell(nbr_id);
							double nbr_incenter[] = new double[3];
							if(nbr_cell.GetNumberOfPoints()!=3)
								continue;
							vtkTriangle triangleCell = new vtkTriangle(nbr_cell.GetVTKId());
							triangleCell.TriangleCenter(triangleCell.GetPoints().GetPoint(0), triangleCell.GetPoints().GetPoint(1), triangleCell.GetPoints().GetPoint(2), nbr_incenter);
			//				System.out.printf("  pick center: (%f, %f, %f)\n", incenter[0], incenter[1], incenter[2]);
			//				System.out.printf("  center pt.: (%f, %f, %f)\n", nbr_incenter[0], nbr_incenter[1], nbr_incenter[2]);
							vtkMath mathobj = new vtkMath();
							double d = mathobj.Distance2BetweenPoints(incenter, nbr_incenter);
			//				System.out.printf("  distance: %f, limit: %f\n", d, ring_radius*ring_radius);
							if((use_distance_constraint) && (d<=(ring_radius*ring_radius))) {
								cell_ids.add(nbr_id);
							}
						}
						
					}
			//		System.out.printf("%d nbrs, radius: %f\n", cell_ids.size(), ring_radius);
					current_ring+=1;
					
				}
				
				cell_ids.clear();
				GetInteractor().GetRenderWindow().Render();
			}
		}
		
		super.OnMouseMove();
	}
	
}
