package ckehl.vtk;

//import java.util.ArrayList;
//import java.util.List;

import vtk.vtkPolyData;
import vtk.vtkShortArray;
import vtk.vtkCell;
import vtk.vtkCellLocator;
import vtk.vtkPoints;

public class PointToSurfaceMap {
	protected SurfaceDataInterface _surface_interface = null;
	protected vtkPolyData _surface = null;
	protected vtkPoints _seeds = null;
	protected int active_category_number = 1;
	
	public void setSurfaceInterface(SurfaceDataInterface value) {
		_surface_interface = value;
	}
	public void setSurface(vtkPolyData surface_data) {
		_surface = surface_data;
	}
	public void setPointSeeds(vtkPoints point_data) {
		_seeds = point_data;
	}
	public void setActiveCategoryNumber(int number) {
		active_category_number = number;
	}
	public int getActiveCategoryNumber() {
		return active_category_number;
	}
	public void compute() {
		vtkShortArray cell_indicators = new vtkShortArray(_surface_interface.GetSurfaceData().GetCellData().GetArray("FlagT").GetVTKId());
		for(int i=0; i<_surface_interface.GetSurfaceData().GetNumberOfCells(); i++) {
			cell_indicators.SetValue(i, 0);
		}
		vtkShortArray point_indicators = new vtkShortArray(_surface_interface.GetSurfaceData().GetPointData().GetArray("FlagV").GetVTKId());
		for(int i=0; i<_surface_interface.GetSurfaceData().GetNumberOfPoints(); i++) {
			point_indicators.SetValue(i, 0);
		}
		
		vtkCellLocator cell_localize = new vtkCellLocator();
		cell_localize.SetDataSet(_surface_interface.GetSurfaceData());
		cell_localize.BuildLocator();
		
		double[] currentPoint = new double[3];
		vtkCell cell;
		
		for(int i = 0; i < _seeds.GetNumberOfPoints(); i++)
		{
			/*
			 * Localize closest cell of the mesh to the present point.
			 * Retrieve Normal, minimal curvature and average curvature.
			 */
			_seeds.GetPoint(i, currentPoint);
			int cell_id = cell_localize.FindCell(currentPoint);
			//FindClosestPoint(currentPoint, closestPoint, cell, cellId, subId, dist);
			if(cell_id>-1)
			{
				cell = _surface_interface.GetSurfaceData().GetCell(cell_id);
				cell_indicators.SetValue(cell_id, active_category_number);
				
				point_indicators.SetValue(cell.GetPointId(0), active_category_number);
				point_indicators.SetValue(cell.GetPointId(1), active_category_number);
				point_indicators.SetValue(cell.GetPointId(2), active_category_number);
			}
		}
	}
}
