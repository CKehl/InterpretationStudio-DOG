package ckehl.vtk;

//import java.util.ArrayList;
//import java.util.List;

import vtk.vtkPolyData;
import vtk.vtkShortArray;
import vtk.vtkCell;
import vtk.vtkCellLocator;
import vtk.vtkIdList;
import vtk.vtkPoints;

public class LineToSurfaceMap {
	protected SurfaceDataInterface _surface_interface = null;
	protected vtkPolyData _surface = null;
	protected vtkPolyData _lines = null;
	protected int active_category_number = 1;
	
	public void setSurfaceInterface(SurfaceDataInterface value) {
		_surface_interface = value;
	}
	public void setSurface(vtkPolyData surface_data) {
		_surface = surface_data;
	}
	public void setLines(vtkPolyData lines_data) {
		_lines = lines_data;
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
		
		double[] previousPoint = new double[3];
		double[] currentPoint = new double[3];
		vtkCell cell;
		
		for(int i = 0; i < _lines.GetNumberOfPoints(); i++)
		{
			/*
			 * Localize closest cell of the mesh to the present point.
			 * Retrieve Normal, minimal curvature and average curvature.
			 */
			_lines.GetPoint(i, currentPoint);
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
		for(int i = 1; i < _lines.GetNumberOfPoints(); i++)
		{
			_lines.GetPoint(i-1, previousPoint);
			_lines.GetPoint(i, currentPoint);
			vtkPoints intersect_pts = new vtkPoints();
			vtkIdList intersect_cell_ids = new vtkIdList();
			int result = cell_localize.IntersectWithLine(previousPoint, currentPoint, intersect_pts, intersect_cell_ids);
			if(result>0) {
				for(int j=0; j<intersect_cell_ids.GetNumberOfIds(); j++) {
					int cellId = intersect_cell_ids.GetId(j);
					if(cellId>-1)
					{
						cell = _surface_interface.GetSurfaceData().GetCell(cellId);
						cell_indicators.SetValue(cellId, active_category_number);
						
						point_indicators.SetValue(cell.GetPointId(0), active_category_number);
						point_indicators.SetValue(cell.GetPointId(1), active_category_number);
						point_indicators.SetValue(cell.GetPointId(2), active_category_number);
					}
				}
			}
		}
	}
}
