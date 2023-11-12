package ckehl.vtk;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Set;
import java.util.Map.Entry;

import CGEL.common.Vector.Vector4;
import vtk.vtkActor;
//import vtk.vtkAppendPolyData;
import vtk.vtkInteractorStyle;
import vtk.vtkLine;
import vtk.vtkLookupTable;
import vtk.vtkMath;
import vtk.vtkPolyDataMapper;
//import vtk.vtkPropPicker;
import vtk.vtkPointPicker;
import vtk.vtkCellPicker;
import vtk.vtkGlyph3D;
//import vtk.vtkRenderWindowInteractor;
import vtk.vtkSphereSource;
import vtk.vtkTubeFilter;
//import vtk.vtkUnsignedIntArray;
//import vtk.vtkUnsignedShortArray;
import vtk.vtkShortArray;
import vtk.vtkPolyData;
//import vtk.vtkCell;
import vtk.vtkCellArray;
import vtk.vtkPoints;
//import vtk.vtkIdList;

public class WellLogCreator extends vtkInteractorStyle {

	protected vtkActor pickposActor = null;
	protected vtkPolyDataMapper pickposMapper = null;
	protected boolean pickpos_actor_active = false;
	protected vtkActor wellTubeOverlayActor = null;
	protected vtkPolyDataMapper wellTubeOverlayMapper = null;
	protected vtkPolyData wellTubeOverlayData = null;
	protected boolean wellTubeOverlay_actor_active = false;
	protected vtkActor wellTickOverlayActor = null;
	protected vtkPolyDataMapper wellTickOverlayMapper = null;
	protected vtkPolyData wellTickOverlayData = null;
	protected boolean wellTickOverlay_actor_active = false;
	
	protected vtkLookupTable tubeOverlayLUT = null;
	protected int id_LeftButtonDown = 0;
	protected int id_LeftButtonUp = 0;
	protected int id_MouseWheelForward = 0;
	protected int id_MouseWheelBackward = 0;
	protected int id_MouseMove = 0;
	protected int id_KeyPress = 0;
	protected boolean left_button_pressed = false;
	protected vtkPointPicker point_picker = null;
	protected vtkCellPicker cell_picker = null;
	protected SurfaceDataInterface data_interface = null;
	protected SurfaceSamplingInterface sample_interface = null;
	protected ListViewUpdateInterface nameUpdate_interface = null;
	protected double pointer_size = 0.025;
	protected double tick_scaler = 1.0;
	protected double sample_distance = 1.0;
	protected double[] last_pts = null;
	protected double currentDistance = 0;
	protected vtkSphereSource point_ticker = null; 

	protected List<Integer[]> lineSegmentIndices = null;
	protected List<Integer[]> lineSegmentIndicators = null;
	protected List<vtkPoints> lineSegmentPoints = null;
	
	protected vtkPolyData lineSegmentData = null;
	protected vtkShortArray lineSegmentCornerIndicators = null;	
	protected List<Integer> currentLineIndices = null;
	protected List<Integer> currentLineIndicators = null;
	protected vtkPoints currentLinePoints = null;
	protected int currentNumCorners = 0;
	protected int max_index = 0;
	protected vtkPoints lineCorners = null;
	protected vtkCellArray lineCells = null;
	
	public WellLogCreator() {
		//System.out.println("MousePickInteractor created");
		super();
		point_picker = new vtkPointPicker();
		cell_picker = new vtkCellPicker();
		
		lineSegmentIndices = new ArrayList<Integer[]>();
		lineSegmentIndicators = new ArrayList<Integer[]>();
		lineSegmentPoints = new ArrayList<vtkPoints>();
		
		lineSegmentData = new vtkPolyData();
		lineSegmentCornerIndicators = new vtkShortArray();
		lineSegmentCornerIndicators.SetName("I_v");
		lineSegmentCornerIndicators.SetNumberOfComponents(1);
		
		lineCorners = new vtkPoints();
		lineCells = new vtkCellArray();
		
		wellTubeOverlayData = new vtkPolyData();
		wellTickOverlayData = new vtkPolyData();

		lineSegmentData.SetPoints(lineCorners);
		lineSegmentData.SetLines(lineCells);
		lineSegmentData.GetPointData().AddArray(lineSegmentCornerIndicators);
		//lineSegmentData.GetPointData().SetScalars(lineSegmentCornerIndicators);
		lineSegmentData.GetPointData().SetActiveScalars("I_v");
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
		if(wellTubeOverlayActor!=null) {
			if(wellTubeOverlay_actor_active) {
				GetDefaultRenderer().RemoveActor(wellTubeOverlayActor);
				wellTubeOverlay_actor_active = false;
			}
			wellTubeOverlayActor = null;
		}
		wellTubeOverlayMapper = null;
		wellTubeOverlayData = null;
		if(wellTickOverlayActor!=null) {
			if(wellTickOverlay_actor_active) {
				GetDefaultRenderer().RemoveActor(wellTickOverlayActor);
				wellTickOverlay_actor_active = false;
			}
			wellTickOverlayActor = null;
		}
		wellTickOverlayMapper = null;
		wellTickOverlayData = null;
		point_picker = null;
		cell_picker = null;
		lineSegmentPoints = null;
		lineSegmentIndicators = null;
		lineSegmentIndices = null;
		lineSegmentCornerIndicators = null;
		lineSegmentData = null;
		tubeOverlayLUT = null;
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
	
	public vtkPolyData GetWellTubeOverlay() {
		return wellTubeOverlayData;
	}
	public vtkPolyData GetWellTickOverlay() {
		return wellTickOverlayData;
	}
	public vtkPolyData GetLineGeometry() {
		return lineSegmentData;
	}
	
	public List<vtkPoints> GetLineOrderedPoints() {
		if((lineSegmentPoints.size()!=lineSegmentIndices.size()) && (lineSegmentPoints.size()!=lineSegmentIndicators.size())) {
			System.out.println("Point, indicator- and line arrays are not equal in size. No segments returned.");
			return null;
		}
		List<vtkPoints> results = new ArrayList<>();
		int N = lineSegmentPoints.size();
		for(int i=0; i<N; i++) {
			vtkPoints tmp_pts = lineSegmentPoints.get(i);
			vtkPoints out_pts = new vtkPoints();
			Integer[] tmp_index_arr = lineSegmentIndices.get(i);
			int id_prev = tmp_index_arr[0];
			double pt[] = tmp_pts.GetPoint(id_prev);
			out_pts.InsertNextPoint(pt);
			for(int j=0; j<tmp_index_arr.length; j+=2) {
				int id1 = tmp_index_arr[j];
				int id2 = tmp_index_arr[j+1];
				if(id1 != id_prev) {
					System.err.println("Successive line indices don't match.");
				}
				pt = tmp_pts.GetPoint(id2);
				out_pts.InsertNextPoint(pt);
				id_prev = id2;
			}
			results.add(out_pts);
		}
		return results;
	}
	
	public List<Integer[]> GetLineOrderedIndicators() {
		if((lineSegmentPoints.size()!=lineSegmentIndices.size()) && (lineSegmentPoints.size()!=lineSegmentIndicators.size())) {
			System.out.println("Point, indicator- and line arrays are not equal in size. No segments returned.");
			return null;
		}
		List<Integer[]> results = new ArrayList<>();
		int N = lineSegmentPoints.size();
		for(int i=0; i<N; i++) {
			Integer[] tmp_indicator_arr = lineSegmentIndicators.get(i);
			List<Integer> tout_indicator = new ArrayList<>();
			Integer[] tmp_index_arr = lineSegmentIndices.get(i);
			int id_prev = tmp_index_arr[0];
			int indicatorValue = tmp_indicator_arr[id_prev];
			tout_indicator.add(indicatorValue);
			for(int j=0; j<tmp_index_arr.length; j+=2) {
				int id1 = tmp_index_arr[j];
				int id2 = tmp_index_arr[j+1];
				if(id1 != id_prev) {
					System.err.println("Successive line indices don't match.");
				}
				indicatorValue = tmp_indicator_arr[id2];
				tout_indicator.add(indicatorValue);
				id_prev = id2;
			}
			Integer[] out_indicators = new Integer[tout_indicator.size()];
			tout_indicator.toArray(out_indicators);
			results.add(out_indicators);
		}
		return results;
	}
	
	public vtkPolyData[] GetLineSegments() {
		if((lineSegmentPoints.size()!=lineSegmentIndices.size()) && (lineSegmentPoints.size()!=lineSegmentIndicators.size())) {
			System.out.println("Point, indicator- and line arrays are not equal in size. No segments returned.");
			return null;
		}
		
		int N = lineSegmentPoints.size();
		vtkPolyData[] resultArray = new vtkPolyData[N];
		for(int i=0; i<N; i++) {
			resultArray[i] = new vtkPolyData();
			vtkPoints tmp_pts = lineSegmentPoints.get(i);
			Integer[] tmp_index_arr = lineSegmentIndices.get(i);
			Integer[] tmp_indicator_arr = lineSegmentIndicators.get(i);
			
			
			vtkShortArray tmp_indicators = new vtkShortArray();
			tmp_indicators.SetName("I_v");
			tmp_indicators.SetNumberOfComponents(1);
			tmp_indicators.SetNumberOfTuples(tmp_indicator_arr.length);
			
	        for(int j=0; j<tmp_indicator_arr.length; j++) {
				tmp_indicators.SetValue(j, (tmp_indicator_arr[j]).shortValue());
	        }
			
			vtkCellArray tmp_lines = new vtkCellArray();
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
			//resultArray[i].GetPointData().SetScalars(tmp_indicators);
			resultArray[i].GetPointData().AddArray(tmp_indicators);
			resultArray[i].GetPointData().SetActiveScalars("I_v");
		}
		return resultArray;
	}
	
	public vtkPolyData GetLineSegment(int line_index) {
		if(lineSegmentPoints.size()!=lineSegmentIndices.size()) {
			System.out.println("Point- and line arrays are not equal in size. No segments returned.");
			return null;
		}
		int N = lineSegmentPoints.size();
		if((line_index<0) || (line_index>(N-1))) {
			return null;
		}
		

		vtkPoints tmp_pts = lineSegmentPoints.get(line_index);
		Integer[] tmp_index_arr = lineSegmentIndices.get(line_index);
		Integer[] tmp_indicator_arr = lineSegmentIndicators.get(line_index);

		vtkPolyData resultArray = new vtkPolyData();
		vtkCellArray tmp_lines = new vtkCellArray();
		
		vtkShortArray tmp_indicators = new vtkShortArray();
		tmp_indicators.SetName("I_v");
		tmp_indicators.SetNumberOfComponents(1);
		tmp_indicators.SetNumberOfTuples(tmp_indicator_arr.length);
        for(int j=0; j<tmp_indicator_arr.length; j++) {
			tmp_indicators.SetValue(j, (tmp_indicator_arr[j]).shortValue());
        }
		
		for(int j=0; j<tmp_index_arr.length; j+=2) {
			int id1 = tmp_index_arr[j];
			int id2 = tmp_index_arr[j+1];
			vtkLine lineFilter = new vtkLine();
			lineFilter.GetPointIds().SetId(0, id1);
			lineFilter.GetPointIds().SetId(1, id2);
			tmp_lines.InsertNextCell(lineFilter);
		}
		resultArray.SetPoints(tmp_pts);
		resultArray.SetLines(tmp_lines);
		//resultArray[i].GetPointData().SetScalars(tmp_indicators);
		resultArray.GetPointData().AddArray(tmp_indicators);
		resultArray.GetPointData().SetActiveScalars("I_v");

		
		//System.out.printf("returning %d elements.\n", resultArray.length);
		return resultArray;
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
		lineSegmentCornerIndicators.Reset();
		for(Integer[] indic_arr : lineSegmentIndicators) {
			for(int j=0; j<indic_arr.length; j++) {
				lineSegmentCornerIndicators.InsertNextTuple1((double)(indic_arr[j]));
				//lineSegmentCornerIndicators.InsertNextValue(indic_arr[j]);
			}
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
		//lineSegmentData.SetPoints(lineCorners);
		//lineSegmentData.SetLines(lineCells);
		//lineSegmentData.GetPointData().AddArray(lineSegmentCornerIndicators);
		////lineSegmentData.GetPointData().SetScalars(lineSegmentCornerIndicators);
		//lineSegmentData.GetPointData().SetActiveScalars("I_v");
		lineSegmentData.GetPointData().Modified();
		lineSegmentData.Modified();

		
		vtkSphereSource spheres = new vtkSphereSource();
		spheres.SetPhiResolution(18);
		spheres.SetThetaResolution(9);
		spheres.SetRadius(0.0044*tick_scaler);
		spheres.Update();
		vtkPolyData sphereGeometry = spheres.GetOutput();
		
		
		vtkGlyph3D glyphs = new vtkGlyph3D();
		//glyphs.SetIndexModeToScalar();
		//glyphs.SetIndexModeToOff();
		glyphs.SetInputData(lineSegmentData);
		glyphs.SetSourceData(sphereGeometry);
		//glyphs.SetScaleModeToDataScalingOff();
		//glyphs.SetVectorModeToVectorRotationOff();
		//glyphs.SetColorModeToColorByScalar();
		//glyphs.FillCellDataOn();
		//glyphs.SetInputArrayToProcess(0, 0, 0, 0, "I_v");  // id0 = {0=scalars, 1=vector, 2=normals, 3=colors]
		glyphs.ScalingOff();
		glyphs.OrientOff();
		glyphs.Update();
		vtkPolyData glyphedGeometry = glyphs.GetOutput();
		if(glyphedGeometry.GetPointData().HasArray("I_v")>0) {
			glyphedGeometry.GetPointData().SetActiveScalars("I_v");
			//int numTupleIndicators = lineSegmentData.GetPointData().GetArray("I_v").GetNumberOfTuples();
			//int numValueIndicators = lineSegmentData.GetPointData().GetArray("I_v").GetNumberOfValues();
			//System.out.println(String.format("Glyphs have 'I_v' attribute. Base # Tuples: Base %d # Values %d", numTupleIndicators, numValueIndicators));
		} else {
			//System.out.println("Glyphs lack 'I_v' attribute.");
		}
		
		vtkTubeFilter tubeGen = new vtkTubeFilter();
		tubeGen.SetInputData(lineSegmentData);
		//tubeGen.AddInputData(lineSegmentData);
		tubeGen.CappingOn();
		tubeGen.SetRadiusFactor(1.0);
		tubeGen.SetRadius(0.0021*tick_scaler);
		tubeGen.SetNumberOfSides(12);
		tubeGen.Update();
		vtkPolyData tubeGeometry = tubeGen.GetOutput();
		
		if(wellTickOverlayMapper==null) {
			wellTickOverlayMapper = new vtkPolyDataMapper();
	        LookUpValues luv = new LookUpValues();
			tubeOverlayLUT = new vtkLookupTable();
			tubeOverlayLUT.SetNumberOfTableValues(luv.categoryColours.size());
			tubeOverlayLUT.Build();
	        Set<Entry<Integer, Vector4>> colourEntrySet = luv.categoryColours.entrySet();
	        Iterator<Entry<Integer, Vector4>> colourEntryIterator = colourEntrySet.iterator();
	        while(colourEntryIterator.hasNext()) {
	        	Entry<Integer, Vector4> colourEntry = colourEntryIterator.next();
	        	tubeOverlayLUT.SetTableValue(colourEntry.getKey(), colourEntry.getValue().getVar0(), colourEntry.getValue().getVar1(), colourEntry.getValue().getVar2(), colourEntry.getValue().getVar3());
	        }
	        wellTickOverlayMapper.InterpolateScalarsBeforeMappingOff();
	        wellTickOverlayMapper.SetScalarRange(0, (double)(luv.categoryColours.size()-1));
	        //wellTickOverlayMapper.SetScalarModeToUseCellData();
	        wellTickOverlayMapper.SetScalarModeToUsePointData();
	        //wellTickOverlayMapper.SetColorModeToDirectScalars();
	        wellTickOverlayMapper.SetColorModeToMapScalars();
	        wellTickOverlayMapper.ScalarVisibilityOn();
	        wellTickOverlayMapper.SetArrayName("I_v");
	        wellTickOverlayMapper.SetLookupTable(tubeOverlayLUT);
		}
		if(wellTickOverlayActor==null)
			wellTickOverlayActor = new vtkActor();
		wellTickOverlayActor.SetMapper(wellTickOverlayMapper);
		
		if(wellTubeOverlayMapper==null) {
			wellTubeOverlayMapper = new vtkPolyDataMapper();
			wellTubeOverlayMapper.InterpolateScalarsBeforeMappingOff();
			wellTubeOverlayMapper.ScalarVisibilityOff();
		}
		if(wellTubeOverlayActor==null)
			wellTubeOverlayActor = new vtkActor();
		wellTubeOverlayActor.SetMapper(wellTubeOverlayMapper);

		if(wellTickOverlayMapper!=null) {
			wellTickOverlayMapper.SetInputData(glyphedGeometry);
			wellTickOverlayMapper.Update();
		}
		if(wellTubeOverlayMapper!=null) {
			wellTubeOverlayMapper.SetInputData(tubeGeometry);
			wellTubeOverlayMapper.Update();
		}
		
		if(!wellTickOverlay_actor_active) {
			GetDefaultRenderer().AddActor(wellTickOverlayActor);
			wellTickOverlay_actor_active = true;
		}
		if(!wellTubeOverlay_actor_active) {
			GetDefaultRenderer().AddActor(wellTubeOverlayActor);
			wellTubeOverlay_actor_active = true;
		}

		if(GetInteractor()!=null)
			GetInteractor().GetRenderWindow().Render();
	}
	
	public void register(SurfaceDataInterface sdi, SurfaceSamplingInterface ssi) {
		data_interface = sdi;
		sample_interface = ssi;
	}
	
	public void registerDataInterface(SurfaceDataInterface sdi) {
		data_interface = sdi;
	}
	
	public void registerSamplingInterface(SurfaceSamplingInterface ssi) {
		sample_interface = ssi;
	}
	
	public void registerNameUpdate(ListViewUpdateInterface nui) {
		nameUpdate_interface = nui;
	}
	
	public void Enable() {
		if(pickposMapper==null)
			pickposMapper = new vtkPolyDataMapper();
		if(pickposActor==null)
			pickposActor = new vtkActor();
		pickposActor.SetMapper(pickposMapper);
		
		if(wellTickOverlayMapper==null) {
			wellTickOverlayMapper = new vtkPolyDataMapper();
	        LookUpValues luv = new LookUpValues();
			tubeOverlayLUT = new vtkLookupTable();
			tubeOverlayLUT.SetNumberOfTableValues(luv.categoryColours.size());
			tubeOverlayLUT.Build();
	        Set<Entry<Integer, Vector4>> colourEntrySet = luv.categoryColours.entrySet();
	        Iterator<Entry<Integer, Vector4>> colourEntryIterator = colourEntrySet.iterator();
	        while(colourEntryIterator.hasNext()) {
	        	Entry<Integer, Vector4> colourEntry = colourEntryIterator.next();
	        	tubeOverlayLUT.SetTableValue(colourEntry.getKey(), colourEntry.getValue().getVar0(), colourEntry.getValue().getVar1(), colourEntry.getValue().getVar2(), colourEntry.getValue().getVar3());
	        }
	        wellTickOverlayMapper.InterpolateScalarsBeforeMappingOff();
	        wellTickOverlayMapper.SetScalarRange(0, (double)(luv.categoryColours.size()-1));
	        //wellTickOverlayMapper.SetScalarModeToUseCellData();
	        wellTickOverlayMapper.SetScalarModeToUsePointData();
	        //wellTickOverlayMapper.SetColorModeToDirectScalars();
	        wellTickOverlayMapper.SetColorModeToMapScalars();
	        wellTickOverlayMapper.ScalarVisibilityOn();
	        wellTickOverlayMapper.SetArrayName("I_v");
	        wellTickOverlayMapper.SetLookupTable(tubeOverlayLUT);
		}
		if(wellTickOverlayActor==null)
			wellTickOverlayActor = new vtkActor();
		wellTickOverlayActor.SetMapper(wellTickOverlayMapper);
		
		if(wellTubeOverlayMapper==null) {
			wellTubeOverlayMapper = new vtkPolyDataMapper();
			wellTubeOverlayMapper.InterpolateScalarsBeforeMappingOff();
			wellTubeOverlayMapper.ScalarVisibilityOff();
		}
		if(wellTubeOverlayActor==null)
			wellTubeOverlayActor = new vtkActor();
		wellTubeOverlayActor.SetMapper(wellTubeOverlayMapper);
		
		if(!wellTickOverlay_actor_active) {
			GetDefaultRenderer().AddActor(wellTickOverlayActor);
			wellTickOverlay_actor_active = true;
		}
		if(!wellTubeOverlay_actor_active) {
			GetDefaultRenderer().AddActor(wellTubeOverlayActor);
			wellTubeOverlay_actor_active = true;
		}
		
		if(sample_interface!=null) {
			sample_interface.UpdateSampleSize();
		}
		
		id_LeftButtonDown = GetInteractor().AddObserver("LeftButtonPressEvent", this, "OnLeftButtonDown");
		id_LeftButtonUp = GetInteractor().AddObserver("LeftButtonReleaseEvent", this, "OnLeftButtonUp");
		id_MouseWheelForward = GetInteractor().AddObserver("MouseWheelForwardEvent", this, "OnMouseWheelForward");
		id_MouseWheelBackward = GetInteractor().AddObserver("MouseWheelBackwardEvent", this, "OnMouseWheelBackward");
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
		
		//if(wellTubeOverlay_actor_active) {
		//	GetDefaultRenderer().RemoveActor(wellTubeOverlayActor);
		//	wellTubeOverlay_actor_active = false;
		//}
		//if(wellTickOverlay_actor_active) {
		//	GetDefaultRenderer().RemoveActor(wellTickOverlayActor);
		//	wellTickOverlay_actor_active = false;
		//}
		
		left_button_pressed=false;
		
		if(id_LeftButtonDown!=0) {
			GetInteractor().RemoveObserver(id_LeftButtonDown);
			id_LeftButtonDown=0;
		}
		if(id_LeftButtonUp!=0) {
			GetInteractor().RemoveObserver(id_LeftButtonUp);
			id_LeftButtonUp=0;
		}
		if(id_MouseWheelForward!=0) {
			GetInteractor().RemoveObserver(id_MouseWheelForward);
			id_MouseWheelForward=0;
		}
		if(id_MouseWheelBackward!=0) {
			GetInteractor().RemoveObserver(id_MouseWheelBackward);
			id_MouseWheelBackward=0;
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
	
	public double GetSampleSize() {
		return sample_distance* pointer_size * tick_scaler;
	}

	public void undo() {
		boolean is_in_interaction = (GetEnabled()==0);
		if(is_in_interaction) {
			Disable();
		}
		
		int remove_index = lineSegmentIndices.size()-1;
		if(remove_index > 0) {
			lineSegmentIndices.remove(remove_index);
			lineSegmentIndicators.remove(remove_index);
			// remove_index = lineSegmentPoints.size()-1;
			lineSegmentPoints.remove(remove_index);
			max_index -= 1;
			if(nameUpdate_interface!=null)
				nameUpdate_interface.RemoveLastItem();
			
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
			lineSegmentCornerIndicators.Reset();
			for(Integer[] indic_arr : lineSegmentIndicators) {
				for(int j=0; j<indic_arr.length; j++) {
					lineSegmentCornerIndicators.InsertNextTuple1(indic_arr[j]);
					//lineSegmentCornerIndicators.InsertNextValue(indic_arr[j]);
				}
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
			//lineSegmentData.SetPoints(lineCorners);
			//lineSegmentData.SetLines(lineCells);
			//lineSegmentData.GetPointData().AddArray(lineSegmentCornerIndicators);
			////lineSegmentData.GetPointData().SetScalars(lineSegmentCornerIndicators);
			//lineSegmentData.GetPointData().SetActiveScalars("I_v");
			lineSegmentData.GetPointData().Modified();
			lineSegmentData.Modified();

			
			vtkSphereSource spheres = new vtkSphereSource();
			spheres.SetPhiResolution(9);
			spheres.SetThetaResolution(18);
			spheres.SetRadius(0.0044*tick_scaler);
			spheres.Update();
			vtkPolyData sphereGeometry = spheres.GetOutput();
			
			vtkGlyph3D glyphs = new vtkGlyph3D();
			glyphs.SetInputData(lineSegmentData);
			glyphs.SetSourceData(sphereGeometry);
			glyphs.ScalingOff();
			glyphs.OrientOff();
			glyphs.Update();
			vtkPolyData glyphedGeometry = glyphs.GetOutput();
			if(glyphedGeometry.GetPointData().HasArray("I_v")>0) {
				glyphedGeometry.GetPointData().SetActiveScalars("I_v");
				//int numTupleIndicators = lineSegmentData.GetPointData().GetArray("I_v").GetNumberOfTuples();
				//int numValueIndicators = lineSegmentData.GetPointData().GetArray("I_v").GetNumberOfValues();
				//System.out.println(String.format("Glyphs have 'I_v' attribute. Base # Tuples: Base %d # Values %d", numTupleIndicators, numValueIndicators));
			} else {
				//System.out.println("Glyphs lack 'I_v' attribute.");
			}
			
			vtkTubeFilter tubeGen = new vtkTubeFilter();
			tubeGen.SetInputData(lineSegmentData);
			tubeGen.CappingOn();
			tubeGen.SetRadiusFactor(1.0);
			tubeGen.SetRadius(0.0021*tick_scaler);
			tubeGen.SetNumberOfSides(12);
			tubeGen.Update();
			vtkPolyData tubeGeometry = tubeGen.GetOutput();

			if(wellTubeOverlayMapper!=null) {
				wellTubeOverlayMapper.SetInputData(tubeGeometry);
				wellTubeOverlayMapper.Update();
			}
			if(wellTickOverlayMapper!=null) {
				wellTickOverlayMapper.SetInputData(glyphedGeometry);
				wellTickOverlayMapper.Update();
			}

			if(!wellTubeOverlay_actor_active) {
				GetDefaultRenderer().AddActor(wellTubeOverlayActor);
				wellTubeOverlay_actor_active = true;
			}
			if(!wellTickOverlay_actor_active) {
				GetDefaultRenderer().AddActor(wellTickOverlayActor);
				wellTickOverlay_actor_active = true;
			}
		}
		
		if(is_in_interaction) {
			Enable();
		}
		
		if(GetInteractor()!=null) {
			GetInteractor().GetRenderWindow().Render();
			GetInteractor().GetRenderWindow().Render();
		}
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
		
		if(sample_interface!=null) {
			sample_interface.UpdateSampleSize();
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

		if(point_ticker==null) {
			point_ticker = new vtkSphereSource();
		}
		point_ticker.SetCenter(worldPosition[0], worldPosition[1], worldPosition[2]);
		double tick_size = pointer_size*tick_scaler;
		point_ticker.SetRadius(tick_size);
		point_ticker.Update();
		
		currentLineIndices = new ArrayList<Integer>();
		currentLineIndicators = new ArrayList<Integer>();
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
			Integer[] currLineIndicators = new Integer[currentLineIndicators.size()];
			currentLineIndicators.toArray(currLineIndicators);
			lineSegmentIndices.add(currLineData);
			lineSegmentIndicators.add(currLineIndicators);
			lineSegmentPoints.add(currentLinePoints);
			max_index = lineSegmentIndices.size();
			if(nameUpdate_interface!=null)
				nameUpdate_interface.AddItem();
		}
		if(pickpos_actor_active) {
			GetDefaultRenderer().RemoveActor(pickposActor);
			pickpos_actor_active = false;
		}
		GetInteractor().GetRenderWindow().Render();
		super.OnLeftButtonUp();
	}
	
	@Override
	public void OnMouseWheelForward() {
		// TODO Auto-generated method stub
		tick_scaler /=0.95;
		
		if(sample_interface!=null) {
			sample_interface.UpdateSampleSize();
		}
		
		super.OnMouseWheelForward();
	}
	
	@Override
	public void OnMouseWheelBackward() {
		// TODO Auto-generated method stub
		tick_scaler *= 0.95;
		
		if(sample_interface!=null) {
			sample_interface.UpdateSampleSize();
		}
		
		super.OnMouseWheelBackward();
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
					
					// Get the attribute data //
					int pickedCategory = 0;
					// vtkCell cell_data = data_interface.GetSurfaceData().GetCell(cell_id);
					try {
						vtkShortArray cell_indicators = new vtkShortArray(data_interface.GetSurfaceData().GetCellData().GetArray("FlagT").GetVTKId());
						pickedCategory = cell_indicators.GetValue(cell_id);
						//System.out.println(String.format("Picked values: %d", pickedCategory));
					} catch (NullPointerException e) {
						//System.out.println("Error Picking data.");
					}
					//System.out.println(String.format("Picked values: %d", pickedCategory));
					currentLineIndicators.add(pickedCategory);
					
					int currentIndex = currentLinePoints.GetNumberOfPoints();
					currentNumCorners += 1;
					currentLinePoints.InsertNextPoint(worldPosition[0], worldPosition[1], worldPosition[2]);
					if(currentNumCorners>1) {
						currentLineIndices.add(currentIndex-1);
						currentLineIndices.add(currentIndex);
					}

					if((lineCorners!=null) && (lineSegmentData!=null) && (lineCells!=null) && (lineSegmentCornerIndicators!=null)) {
						int max_vtk_pt_index = lineCorners.GetNumberOfPoints();
						lineCorners.InsertNextPoint(worldPosition[0], worldPosition[1], worldPosition[2]);
						if(currentNumCorners>1) {
							vtkLine lineFilter = new vtkLine();
							lineFilter.GetPointIds().SetId(0, max_vtk_pt_index-1);
							lineFilter.GetPointIds().SetId(1, max_vtk_pt_index);
							lineCells.InsertNextCell(lineFilter);
						}
						
						lineSegmentCornerIndicators.InsertNextTuple1(pickedCategory);
						//lineSegmentCornerIndicators.InsertNextValue(pickedCategory);
						lineSegmentData.GetPointData().Modified();
						lineSegmentData.Modified();
					}					
					vtkSphereSource spheres = new vtkSphereSource();
					spheres.SetPhiResolution(9);
					spheres.SetThetaResolution(18);
					spheres.SetRadius(0.0044*tick_scaler);
					spheres.Update();
					vtkPolyData sphereGeometry = spheres.GetOutput();

					vtkGlyph3D glyphs = new vtkGlyph3D();
					glyphs.SetInputData(lineSegmentData);
					glyphs.SetSourceData(sphereGeometry);
					glyphs.ScalingOff();
					glyphs.OrientOff();
					glyphs.Update();
					vtkPolyData glyphedGeometry = glyphs.GetOutput();
					if(glyphedGeometry.GetPointData().HasArray("I_v")>0) {
						glyphedGeometry.GetPointData().SetActiveScalars("I_v");
						//int numTupleIndicators = lineSegmentData.GetPointData().GetArray("I_v").GetNumberOfTuples();
						//int numValueIndicators = lineSegmentData.GetPointData().GetArray("I_v").GetNumberOfValues();
						//System.out.println(String.format("Glyphs have 'I_v' attribute. Base # Tuples: Base %d # Values %d", numTupleIndicators, numValueIndicators));
					} else {
						//System.out.println("Glyphs lack 'I_v' attribute.");
					}
					
					vtkTubeFilter tubeGen = new vtkTubeFilter();
					tubeGen.SetInputData(lineSegmentData);
					tubeGen.CappingOn();
					tubeGen.SetRadiusFactor(1.0);
					tubeGen.SetRadius(0.0021*tick_scaler);
					tubeGen.SetNumberOfSides(12);
					tubeGen.Update();
					vtkPolyData tubeGeometry = tubeGen.GetOutput();

					if(wellTubeOverlayMapper!=null) {
						wellTubeOverlayMapper.SetInputData(tubeGeometry);
						wellTubeOverlayMapper.Update();
					}
					if(wellTickOverlayMapper!=null) {
						wellTickOverlayMapper.SetInputData(glyphedGeometry);
						wellTickOverlayMapper.Update();
					}
				}

				GetInteractor().GetRenderWindow().Render();
			}
		}
		
		super.OnMouseMove();
	}
	
}
