package ckehl.vtk;

import vtk.vtkActor;
import vtk.vtkInteractorStyle;
import vtk.vtkPolyDataMapper;
//import vtk.vtkPropPicker;
import vtk.vtkPointPicker;
import vtk.vtkCellPicker;
import vtk.vtkSphereSource;
//import vtk.vtkPolyData;
import vtk.vtkCell;
import vtk.vtkPoints;
//import vtk.vtkPolyData;
import vtk.vtkShortArray;
import vtk.vtkIdList;
//import vtk.vtkMath;

public class PointPickInteractor extends vtkInteractorStyle {

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
	protected double pointer_size = 0.01;
	protected vtkPoints seeds = null;
	protected int active_category_number = 1;
	
	public PointPickInteractor() {
		// TODO Auto-generated constructor stub
		//System.out.println("MousePickInteractor created");
		super();
		point_picker = new vtkPointPicker();
		cell_picker = new vtkCellPicker();
		seeds = new vtkPoints();
	}
	
	@Override
	protected void finalize() throws Throwable {
		// TODO Auto-generated method stub
		point_picker = null;
		cell_picker = null;
		seeds = null;
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
	
	public vtkPoints GetPointSeeds() {
		return seeds;
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
		
		if(pickposMapper==null) {
			pickposMapper = new vtkPolyDataMapper();
		}
		if(pickposActor==null) {
			pickposActor = new vtkActor();
		}
		pickposActor.SetMapper(pickposMapper);
	}
	
	public void Disable() {
		//GetDefaultRenderer().RemoveActor(pickposActor);
		if(pickposMapper!=null) {
			pickposMapper = null;
		}
		if(pickposActor!=null) {
			pickposActor = null;
		}
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

	public void undo() {
		int max_index = seeds.GetNumberOfPoints()-1;
		vtkPoints tmp_seeds = new vtkPoints();
		double[] tmppt = new double[3];
		for(int i=0; i<max_index; i++) {
			seeds.GetPoint(i, tmppt);
			tmp_seeds.InsertNextPoint(tmppt);
		}
		seeds.Reset();
		seeds.DeepCopy(tmp_seeds);
		// remap on surface
		PointToSurfaceMap surface_mapper = new PointToSurfaceMap();
		surface_mapper.setSurfaceInterface(data_interface);
		surface_mapper.setPointSeeds(seeds);
		surface_mapper.setActiveCategoryNumber(active_category_number);
		surface_mapper.compute();

		data_interface.GetSurfaceData().GetPointData().Modified();
		data_interface.GetSurfaceData().GetCellData().Modified();

		if(GetInteractor()!=null)
			GetInteractor().GetRenderWindow().Render();
		//if(GetEnabled()>0)
		//	GetInteractor().GetRenderWindow().Render();
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
		
		//point_picker.Pick(Double.valueOf(clickPos[0]), Double.valueOf(clickPos[1]), 0, GetDefaultRenderer());
		//double pos[] = point_picker.GetPickPosition();
		cell_picker.SetTolerance(0.00001);
		cell_picker.Pick(Double.valueOf(clickPos[0]), Double.valueOf(clickPos[1]), 0, GetDefaultRenderer());
		double pos[] = cell_picker.GetPickPosition();
		int cell_id = cell_picker.GetCellId();
		//System.out.printf("picked pos: %f, %f, %f\n", pos[0], pos[1], pos[2]);
		
		vtkSphereSource sphereSource = new vtkSphereSource();
		sphereSource.SetCenter(pos[0], pos[1], pos[2]);
		sphereSource.SetRadius(pointer_size);
		sphereSource.Update();
		pickposMapper.SetInputData(sphereSource.GetOutput());
		
		if(cell_id>=0) {
			seeds.InsertNextPoint(pos[0], pos[1], pos[2]);
			vtkCell cell_data = data_interface.GetSurfaceData().GetCell(cell_id);
			vtkIdList point_ids = cell_data.GetPointIds();
			
			try {
				vtkShortArray cell_indicators = new vtkShortArray(data_interface.GetSurfaceData().GetCellData().GetArray("FlagT").GetVTKId());
				//cell_indicators.SetValue(cell_id, 1);	// SegFault break
				cell_indicators.SetValue(cell_id, active_category_number);	// SegFault break
				vtkShortArray point_indicators = new vtkShortArray(data_interface.GetSurfaceData().GetPointData().GetArray("FlagV").GetVTKId());
				for(int i=0; i<point_ids.GetNumberOfIds(); i++) {
					int point_id = point_ids.GetId(i);
					//point_indicators.SetValue(point_id, 1);
					point_indicators.SetValue(point_id, active_category_number);
				}
				data_interface.GetSurfaceData().GetPointData().Modified();
				data_interface.GetSurfaceData().GetCellData().Modified();
			} catch (NullPointerException e) {
				// TODO: handle exception
			}
		}

		GetDefaultRenderer().AddActor(pickposActor);
		
		if(GetInteractor()!=null)
			GetInteractor().GetRenderWindow().Render();
		//if(GetEnabled()>0)
		//	GetInteractor().GetRenderWindow().Render();
		
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
		// TODO: fill if needed
		super.OnMouseMove();
	}
	
}
