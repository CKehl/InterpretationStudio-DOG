package ckehl.vtk;

import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;

import vtk.vtkActor;
import vtk.vtkAppendPolyData;
import vtk.vtkInteractorStyle;
import vtk.vtkLine;
import vtk.vtkMath;
import vtk.vtkPolyDataMapper;
//import vtk.vtkPropPicker;
import vtk.vtkPointPicker;
import vtk.vtkCellPicker;
import vtk.vtkGlyph3D;
//import vtk.vtkRenderWindowInteractor;
import vtk.vtkSphereSource;
import vtk.vtkTubeFilter;
import vtk.vtkPolyData;
import vtk.vtkCell;
import vtk.vtkCellArray;
import vtk.vtkPoints;
import vtk.vtkShortArray;
import vtk.vtkIdList;

public class LinePickInteractor extends vtkInteractorStyle {

	protected vtkActor pickposActor = null;
	protected vtkPolyDataMapper pickposMapper = null;
	protected boolean pickpos_actor_active = false;
	protected vtkActor tubeOverlayActor = null;
	protected vtkPolyDataMapper tubeOverlayMapper = null;
	protected boolean tubeOverlay_actor_active = false;
	protected int id_LeftButtonDown = 0;
	protected int id_LeftButtonUp = 0;
	protected int id_MouseMove = 0;
	protected int id_KeyPress = 0;
	protected boolean left_button_pressed = false;
	protected vtkPointPicker point_picker = null;
	protected vtkCellPicker cell_picker = null;
	protected SurfaceDataInterface data_interface = null;
	protected double pointer_size = 0.025;
	protected double tick_scaler = 1.0;
	protected double sample_distance = 1.0;
	protected double[] last_pts = null;
	protected double currentDistance = 0;
	protected int active_category_number = 1;
	protected vtkSphereSource point_ticker = null; 
	
	protected vtkPolyData lineSegmentData = null;
	protected List<Integer[]> lineSegmentIndices = null;
	protected List<vtkPoints> lineSegmentPoints = null;
	protected List<Integer> currentLineIndices = null;
	protected List<Integer> cat_num_mapping = null;
	protected vtkPoints currentLinePoints = null;
	protected int currentNumCorners = 0;
	protected int max_index = 0;
	protected vtkPoints lineCorners = null;
	protected vtkCellArray lineCells = null;
	protected vtkPolyData tubeOverlayData = null;
	
	public LinePickInteractor() {
		//System.out.println("MousePickInteractor created");
		super();
		point_picker = new vtkPointPicker();
		cell_picker = new vtkCellPicker();
		lineSegmentData = new vtkPolyData();
		lineSegmentIndices = new ArrayList<Integer[]>();
		lineSegmentPoints = new ArrayList<vtkPoints>();
		cat_num_mapping = new LinkedList<Integer>();
		lineCorners = new vtkPoints();
		lineCells = new vtkCellArray();
		tubeOverlayData = new vtkPolyData();
		
		lineSegmentData.SetPoints(lineCorners);
		lineSegmentData.SetLines(lineCells);
	}
	
	@Override
	protected void finalize() throws Throwable {
		if(pickposActor!=null) {
			if(pickpos_actor_active) {
				GetDefaultRenderer().RemoveActor(pickposActor);
				pickpos_actor_active = false;
			}
			pickposActor = null;
		}
		if(tubeOverlayActor!=null) {
			if(tubeOverlay_actor_active) {
				GetDefaultRenderer().RemoveActor(tubeOverlayActor);
				tubeOverlay_actor_active = false;
			}
			tubeOverlayActor = null;
		}
		point_picker = null;
		cell_picker = null;
		lineSegmentData = null;
		tubeOverlayData = null;
		tubeOverlayMapper = null;
		pickposMapper = null;
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
	
	public vtkPolyData GetTubeOverlay() {
		return tubeOverlayData;
	}
	public vtkPolyData GetLineGeometry() {
		return lineSegmentData;
	}
	
	public vtkPolyData[] GetLineSegments() {
		if(lineSegmentPoints.size()!=lineSegmentIndices.size()) {
			System.out.println("Point- and line arrays are not equal in size. No segments returned.");
			return null;
		}
		
		int N = lineSegmentPoints.size();
		vtkPolyData[] resultArray = new vtkPolyData[N];
		for(int i=0; i<N; i++) {
			resultArray[i] = new vtkPolyData();
			vtkPoints tmp_pts = lineSegmentPoints.get(i);
			vtkCellArray tmp_lines = new vtkCellArray();
			Integer[] tmp_index_arr = lineSegmentIndices.get(i);
			for(int j=0; j<tmp_index_arr.length; j+=2) {
				int id1 = tmp_index_arr[j];
				int id2 = tmp_index_arr[j+1];
				vtkLine lineFilter = new vtkLine();
				lineFilter.GetPointIds().SetId(0, id1);
				lineFilter.GetPointIds().SetId(1, id2);
				tmp_lines.InsertNextCell(lineFilter);
			}
			resultArray[i].SetPoints(tmp_pts);
			resultArray[i].SetLines(tmp_lines);
		}
		return resultArray;
	}
	
	public vtkPolyData[] GetLineSegments(int category_num) {
		if(lineSegmentPoints.size()!=lineSegmentIndices.size()) {
			System.out.println("Point- and line arrays are not equal in size. No segments returned.");
			return null;
		}
		
		//Integer[] cat_num_array = cat_num_mapping.toArray(new Integer[0]);
		Integer[] cat_num_array = new Integer[cat_num_mapping.size()];
		cat_num_mapping.toArray(cat_num_array);
		List<Integer> active_ids = new ArrayList<Integer>();
		for(int i=0; i<cat_num_array.length; i++) {
			if(cat_num_array[i].intValue()==category_num) {
				active_ids.add(i);
			}
		}
		
		//int N = lineSegmentPoints.size();
		int N = active_ids.size();
		if(N==0)
			return null;
		vtkPolyData[] resultArray = new vtkPolyData[N];
		for(int i=0; i<N; i++) {
			int lseg_index = active_ids.get(i);
			resultArray[i] = new vtkPolyData();
			
			vtkPoints tmp_pts = lineSegmentPoints.get(lseg_index);
			vtkCellArray tmp_lines = new vtkCellArray();
			Integer[] tmp_index_arr = lineSegmentIndices.get(lseg_index);
			for(int j=0; j<tmp_index_arr.length; j+=2) {
				int id1 = tmp_index_arr[j];
				int id2 = tmp_index_arr[j+1];
				vtkLine lineFilter = new vtkLine();
				lineFilter.GetPointIds().SetId(0, id1);
				lineFilter.GetPointIds().SetId(1, id2);
				tmp_lines.InsertNextCell(lineFilter);
			}
			resultArray[i].SetPoints(tmp_pts);
			resultArray[i].SetLines(tmp_lines);
		}
		
		//System.out.printf("returning %d elements.\n", resultArray.length);
		return resultArray;
	}
	
	public void addLines(int patch_num, vtkPolyData lines) {
		int numLinesSegments = lines.GetNumberOfLines();
		vtkCell cell = null;
		vtkPoints points = lines.GetPoints();
		List<Integer> internalIds = new ArrayList<Integer>();
		for(int cellId = 0; cellId < numLinesSegments; cellId++) {
			cell = lines.GetCell(cellId);
			//if(lines.GetCellType(cellId) == 3) {
				int id0 = cell.GetPointId(0);
				int id1 = cell.GetPointId(1);
				internalIds.add(id0);
				internalIds.add(id1);
			//}
		}
		Integer[] lineDataSet = new Integer[internalIds.size()]; 
		internalIds.toArray(lineDataSet);
		lineSegmentPoints.add(points);
		lineSegmentIndices.add(lineDataSet);
		cat_num_mapping.add(patch_num);
	}
	
	public void revertLineVis() {
		max_index = lineSegmentIndices.size()-1;
		
		lineCorners.Reset();
		List<Integer> rt_pt_indices = new ArrayList<Integer>();
		//int runtime_pt_index = 0;
		for(vtkPoints cpt : lineSegmentPoints) {
			int runtime_pt_index = 0;
			for(int i=0; i<cpt.GetNumberOfPoints(); i++) {
				double[] _pt = cpt.GetPoint(i);
				lineCorners.InsertNextPoint(_pt[0], _pt[1], _pt[2]);
				runtime_pt_index += 1;
			}
			rt_pt_indices.add(runtime_pt_index);
		}
		lineCells.Reset();
		int offset = 0;
		for(int i=0; i<rt_pt_indices.size(); i++) {
			for(int j=1; j<rt_pt_indices.get(i); j++) {
				vtkLine lineFilter = new vtkLine();
				lineFilter.GetPointIds().SetId(0, (offset+j)-1);
				lineFilter.GetPointIds().SetId(1, (offset+j));
				lineCells.InsertNextCell(lineFilter);
			}
			offset += rt_pt_indices.get(i);
		}
		lineSegmentData.SetPoints(lineCorners);
		lineSegmentData.SetLines(lineCells);
		
		
		// ==== recompute surface map ==== //
		//LineToSurfaceMap mapper = new LineToSurfaceMap();
		//mapper.setSurfaceInterface(data_interface);
		//mapper.setLines(lineSegmentData);
		//mapper.setActiveCategoryNumber(active_category_number);
		//mapper.compute();
		
		vtkSphereSource spheres = new vtkSphereSource();
		spheres.SetPhiResolution(18);
		spheres.SetThetaResolution(9);
		spheres.SetRadius(0.0044*tick_scaler);
		spheres.Update();
		
		vtkGlyph3D glyphs = new vtkGlyph3D();
		glyphs.SetIndexModeToOff();
		glyphs.SetInputData(lineSegmentData);
		glyphs.SetScaleModeToDataScalingOff();
		glyphs.SetSourceData(spheres.GetOutput());
		glyphs.SetVectorModeToVectorRotationOff();
		glyphs.ScalingOff();
		glyphs.OrientOff();
		glyphs.Update();
		
		vtkTubeFilter tubeGen = new vtkTubeFilter();
		tubeGen.SetInputData(lineSegmentData);
		//tubeGen.AddInputData(lineSegmentData);
		tubeGen.CappingOn();
		tubeGen.SetRadiusFactor(1.0);
		tubeGen.SetRadius(0.0021*tick_scaler);
		tubeGen.SetNumberOfSides(12);
		tubeGen.Update();
		//tubeOverlayData = tubeGen.GetOutput();
		
		vtkAppendPolyData fuser = new vtkAppendPolyData();
		fuser.AddInputData(glyphs.GetOutput());
		fuser.AddInputData(tubeGen.GetOutput());
		fuser.Update();
		
		if(tubeOverlayMapper==null)
			tubeOverlayMapper = new vtkPolyDataMapper();
		if(tubeOverlayActor==null)
			tubeOverlayActor = new vtkActor();
		tubeOverlayActor.SetMapper(tubeOverlayMapper);
		// Should be on ALL THE TIME
		if(!tubeOverlay_actor_active) {
			GetDefaultRenderer().AddActor(tubeOverlayActor);
			tubeOverlay_actor_active = true;
		}
		
		tubeOverlayData = fuser.GetOutput();

		if(tubeOverlayMapper!=null) {
			tubeOverlayMapper.SetInputData(fuser.GetOutput());
		}
		
		if(!tubeOverlay_actor_active) {
			GetDefaultRenderer().AddActor(tubeOverlayActor);
			tubeOverlay_actor_active = true;
		}

		data_interface.GetSurfaceData().GetPointData().Modified();
		data_interface.GetSurfaceData().GetCellData().Modified();
		if(GetInteractor()!=null)
			GetInteractor().GetRenderWindow().Render();
	}
	
	public void register(SurfaceDataInterface sdi) {
		data_interface = sdi;
	}
	
	public void Enable() {
		if(pickposMapper==null)
			pickposMapper = new vtkPolyDataMapper();
		if(pickposActor==null)
			pickposActor = new vtkActor();
		pickposActor.SetMapper(pickposMapper);
		
		if(tubeOverlayMapper==null)
			tubeOverlayMapper = new vtkPolyDataMapper();
		if(tubeOverlayActor==null)
			tubeOverlayActor = new vtkActor();
		tubeOverlayActor.SetMapper(tubeOverlayMapper);
		// Should be on ALL THE TIME
		if(!tubeOverlay_actor_active) {
			GetDefaultRenderer().AddActor(tubeOverlayActor);
			tubeOverlay_actor_active = true;
		}
		
		id_LeftButtonDown = GetInteractor().AddObserver("LeftButtonPressEvent", this, "OnLeftButtonDown");
		id_LeftButtonUp = GetInteractor().AddObserver("LeftButtonReleaseEvent", this, "OnLeftButtonUp");
		id_MouseMove = GetInteractor().AddObserver("MouseMoveEvent", this, "OnMouseMove");
		id_KeyPress = GetInteractor().AddObserver("KeyPressEvent", this, "OnKeyDown");
		EnabledOn();
		On();
	}
	
	public void Disable() {
		if(pickposActor!=null) {
			if(pickpos_actor_active) {
				GetDefaultRenderer().RemoveActor(pickposActor);
				pickpos_actor_active = false;
			}
			pickposActor = null;
		}
		if(pickposActor!=null)
			pickposMapper = null;
		//if(tubeOverlayActor!=null) {
		//	if(tubeOverlay_actor_active) {
		//		GetDefaultRenderer().RemoveActor(tubeOverlayActor);
		//		tubeOverlay_actor_active = false;
		//	}
		//	tubeOverlayActor = null;
		//}
		//if(tubeOverlayMapper!=null)
		//	tubeOverlayMapper = null;
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
		int remove_index = lineSegmentIndices.size()-1;
		int last_cat_index = cat_num_mapping.toArray().length-1;
		if(cat_num_mapping.toArray(new Integer[0])[last_cat_index].intValue()!=active_category_number)
			return;
		if(remove_index > 0) {
			cat_num_mapping.remove(remove_index);
			lineSegmentIndices.remove(remove_index);
			remove_index = lineSegmentPoints.size()-1;
			lineSegmentPoints.remove(remove_index);
			max_index -= 1;
			
			lineCorners.Reset();
			List<Integer> rt_pt_indices = new ArrayList<Integer>();
			//int runtime_pt_index = 0;
			for(vtkPoints cpt : lineSegmentPoints) {
				int runtime_pt_index = 0;
				for(int i=0; i<cpt.GetNumberOfPoints(); i++) {
					double[] _pt = cpt.GetPoint(i);
					lineCorners.InsertNextPoint(_pt[0], _pt[1], _pt[2]);
					runtime_pt_index += 1;
				}
				rt_pt_indices.add(runtime_pt_index);
			}
			lineCells.Reset();
			int offset = 0;
			for(int i=0; i<rt_pt_indices.size(); i++) {
				for(int j=1; j<rt_pt_indices.get(i); j++) {
					vtkLine lineFilter = new vtkLine();
					lineFilter.GetPointIds().SetId(0, (offset+j)-1);
					lineFilter.GetPointIds().SetId(1, (offset+j));
					lineCells.InsertNextCell(lineFilter);
				}
				offset += rt_pt_indices.get(i);
			}
			lineSegmentData.SetPoints(lineCorners);
			lineSegmentData.SetLines(lineCells);
			
			
			// recompute surface map
			LineToSurfaceMap mapper = new LineToSurfaceMap();
			mapper.setSurfaceInterface(data_interface);
			mapper.setLines(lineSegmentData);
			mapper.setActiveCategoryNumber(active_category_number);
			mapper.compute();
			
			vtkSphereSource spheres = new vtkSphereSource();
			spheres.SetPhiResolution(18);
			spheres.SetThetaResolution(9);
			spheres.SetRadius(0.0044*tick_scaler);
			spheres.Update();
			
			vtkGlyph3D glyphs = new vtkGlyph3D();
			glyphs.SetIndexModeToOff();
			glyphs.SetInputData(lineSegmentData);
			glyphs.SetScaleModeToDataScalingOff();
			glyphs.SetSourceData(spheres.GetOutput());
			glyphs.SetVectorModeToVectorRotationOff();
			glyphs.ScalingOff();
			glyphs.OrientOff();
			glyphs.Update();
			
			vtkTubeFilter tubeGen = new vtkTubeFilter();
			tubeGen.SetInputData(lineSegmentData);
			//tubeGen.AddInputData(lineSegmentData);
			tubeGen.CappingOn();
			tubeGen.SetRadiusFactor(1.0);
			tubeGen.SetRadius(0.0021*tick_scaler);
			tubeGen.SetNumberOfSides(12);
			tubeGen.Update();
			//tubeOverlayData = tubeGen.GetOutput();
			
			vtkAppendPolyData fuser = new vtkAppendPolyData();
			fuser.AddInputData(glyphs.GetOutput());
			fuser.AddInputData(tubeGen.GetOutput());
			fuser.Update();
			
			tubeOverlayData = fuser.GetOutput();
	
			if(tubeOverlayMapper!=null) {
				tubeOverlayMapper.SetInputData(fuser.GetOutput());
			}
			
			if(!tubeOverlay_actor_active) {
				GetDefaultRenderer().AddActor(tubeOverlayActor);
				tubeOverlay_actor_active = true;
			}
	
			data_interface.GetSurfaceData().GetPointData().Modified();
			data_interface.GetSurfaceData().GetCellData().Modified();
		}
		
		if(GetInteractor()!=null)
			GetInteractor().GetRenderWindow().Render();
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
    		//pointer_size /= 0.95;
    		tick_scaler /=0.95;
    		break;
    	}
    	case '-': {
    		//pointer_size *= 0.95;
    		tick_scaler *= 0.95;
    		break;
    	}
    	}
		super.OnKeyDown();
	}
	
	@Override
	public void OnLeftButtonDown() {
		// TODO Auto-generated method stub

		left_button_pressed = true;
		int clickPos[] = GetInteractor().GetEventPosition();
		point_picker.Pick(Double.valueOf(clickPos[0]), Double.valueOf(clickPos[1]), 0, GetDefaultRenderer());
		double[] worldPosition = point_picker.GetPickPosition();
		
		boolean addToActor=false;
		if(point_ticker==null) {
			point_ticker = new vtkSphereSource();
			addToActor=true;
		}
		point_ticker.SetCenter(worldPosition[0], worldPosition[1], worldPosition[2]);
		double tick_size = pointer_size*tick_scaler;
		point_ticker.SetRadius(tick_size);
		point_ticker.Update();
		if(addToActor) {
		//	pickposMapper.AddInputDataObject(point_ticker.GetOutput());
		}
		
		currentLineIndices = new ArrayList<Integer>();
		currentLinePoints = new vtkPoints();
		currentNumCorners = 0;
		
		if(!pickpos_actor_active) {
			GetDefaultRenderer().AddActor(pickposActor);
			pickpos_actor_active = true;
		}
		GetInteractor().GetRenderWindow().Render();
		
		super.OnLeftButtonDown();
	}
	
	@Override
	public void OnLeftButtonUp() {
		if(point_ticker!=null) {
			point_ticker=null;
		}
		
		left_button_pressed = false;
		last_pts = null;
		if(currentLineIndices.size()>1) {
			//Integer[] currLineData = (Integer[]) (List.toArray(currentLineIndices));
			Integer[] currLineData = new Integer[currentLineIndices.size()]; 
			currentLineIndices.toArray(currLineData);
			lineSegmentIndices.add(currLineData);
			lineSegmentPoints.add(currentLinePoints);
			cat_num_mapping.add(active_category_number);
			max_index = lineSegmentIndices.size();
		}
		if(pickpos_actor_active) {
			GetDefaultRenderer().RemoveActor(pickposActor);
			pickpos_actor_active = false;
		}
		GetInteractor().GetRenderWindow().Render();
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

			vtkSphereSource sphereSource = new vtkSphereSource();
			sphereSource.SetCenter(worldPosition[0], worldPosition[1], worldPosition[2]);
			double tick_size = pointer_size*tick_scaler;
			sphereSource.SetRadius(tick_size);
			sphereSource.Update();
			if(pickposMapper!=null)
				pickposMapper.SetInputData(sphereSource.GetOutput());

			if(cell_id>=0) {
				double limit = (sample_distance*tick_size);
				if(last_pts==null) {
					currentDistance = (limit*limit)+1.0;
				} else {
					vtkMath mutil = new vtkMath();
					currentDistance = mutil.Distance2BetweenPoints(last_pts, worldPosition);
				}
				
				if((currentDistance>(limit*limit)) && (currentLinePoints!=null) && (currentLineIndices!=null)) {
					if(last_pts==null) {
						last_pts = new double[3];
					}
					last_pts[0] = worldPosition[0];
					last_pts[1] = worldPosition[1];
					last_pts[2] = worldPosition[2];
					
					int currentIndex = currentLinePoints.GetNumberOfPoints();
					currentNumCorners += 1;
					currentLinePoints.InsertNextPoint(worldPosition[0], worldPosition[1], worldPosition[2]);
					if(currentNumCorners>1) {
						currentLineIndices.add(currentIndex-1);
						currentLineIndices.add(currentIndex);
					}
					//System.out.printf("# tube verts.: %d\n", currentLinePoints.GetNumberOfPoints());

					if((lineCorners!=null) && (lineSegmentData!=null) && (lineCells!=null)) {
						int max_vtk_pt_index = lineCorners.GetNumberOfPoints();
						lineCorners.InsertNextPoint(worldPosition[0], worldPosition[1], worldPosition[2]);
						if(currentNumCorners>1) {
							vtkLine lineFilter = new vtkLine();
							lineFilter.GetPointIds().SetId(0, max_vtk_pt_index-1);
							lineFilter.GetPointIds().SetId(1, max_vtk_pt_index);
							lineCells.InsertNextCell(lineFilter);
						}
						
						lineSegmentData.SetPoints(lineCorners);
						lineSegmentData.SetLines(lineCells);
						lineSegmentData.Modified();
					}
					//System.out.printf("# tube cells.: %d\n", lineSegmentData.GetNumberOfCells());
					
					vtkSphereSource spheres = new vtkSphereSource();
					spheres.SetPhiResolution(18);
					spheres.SetThetaResolution(9);
					spheres.SetRadius(0.0044*tick_scaler);
					spheres.Update();
					
					vtkGlyph3D glyphs = new vtkGlyph3D();
					glyphs.SetIndexModeToOff();
					glyphs.SetInputData(lineSegmentData);
					glyphs.SetScaleModeToDataScalingOff();
					glyphs.SetSourceData(spheres.GetOutput());
					glyphs.SetVectorModeToVectorRotationOff();
					glyphs.ScalingOff();
					glyphs.OrientOff();
					glyphs.Update();
					
					vtkTubeFilter tubeGen = new vtkTubeFilter();
					tubeGen.SetInputData(lineSegmentData);
					tubeGen.CappingOn();
					tubeGen.SetRadiusFactor(1.0);
					tubeGen.SetRadius(0.0021*tick_scaler);
					tubeGen.SetNumberOfSides(12);
					tubeGen.Update();
					
					vtkAppendPolyData fuser = new vtkAppendPolyData();
					fuser.AddInputData(glyphs.GetOutput());
					fuser.AddInputData(tubeGen.GetOutput());
					fuser.Update();
					
					tubeOverlayData = fuser.GetOutput();

					if(tubeOverlayMapper!=null) {
						tubeOverlayMapper.SetInputData(fuser.GetOutput());
					}
				}

				vtkCell cell_data = data_interface.GetSurfaceData().GetCell(cell_id);
				vtkIdList point_ids = cell_data.GetPointIds();
				try {
					vtkShortArray cell_indicators = new vtkShortArray(data_interface.GetSurfaceData().GetCellData().GetArray("FlagT").GetVTKId());
					//cell_indicators.SetValue(cell_id, 1);
					cell_indicators.SetValue(cell_id, active_category_number);
					vtkShortArray point_indicators = new vtkShortArray(data_interface.GetSurfaceData().GetPointData().GetArray("FlagV").GetVTKId());
					for(int i=0; i<point_ids.GetNumberOfIds(); i++) {
						int point_id = point_ids.GetId(i);
						//point_indicators.SetValue(point_id, 1);
						point_indicators.SetValue(point_id, active_category_number);
					}
					data_interface.GetSurfaceData().GetPointData().Modified();
					data_interface.GetSurfaceData().GetCellData().Modified();
				} catch (NullPointerException e) {
					
				}

				GetInteractor().GetRenderWindow().Render();
			}
		}
		
		super.OnMouseMove();
	}
	
}
