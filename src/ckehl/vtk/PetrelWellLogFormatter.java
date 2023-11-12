/**
 * Author: Dr. Christian Kehl
 * date: 10 April 2020
 **/
package ckehl.vtk;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.UUID;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;

import vtk.vtkPoints;
import CGEL.common.Vector.*;

public class PetrelWellLogFormatter {
	protected List<vtkPoints> P=null;
	protected List<Integer[]> I=null;
	protected List<String> NM=null;
	protected List<Vector3> top_pts=null;
	protected List<Vector3> bottom_pts=null;
	protected List<Integer> Nsamples=null;
	protected List<String> well_UUIDs=null;
	protected String parentpath = null;
	protected String basepath=null;
	protected String basename=null;
	protected String ext_str=null;
	protected String wellHeader_path;
	protected List<String> wellDeviation_paths=null;
	protected List<String> wellLog_paths = null;
	
	public PetrelWellLogFormatter(List<vtkPoints> P_wells, List<Integer[]> I_wells, List<String> NM_wells) {
		P = P_wells;
		I = I_wells;
		NM = NM_wells;
		if( (P==null) || (I==null) || (NM==null) )
			return;
		if( (P.size()<1) || (I.size()<1) || (NM.size()<1) )
			return;
		
		top_pts = new ArrayList<Vector3>();
		bottom_pts = new ArrayList<Vector3>();
		Nsamples = new ArrayList<>();
		well_UUIDs = new ArrayList<>();
		wellDeviation_paths = new ArrayList<>();
		wellLog_paths = new ArrayList<>();

		int N = P.size();
		for(int wellID = 0; wellID<N; wellID++) {
			vtkPoints pts = P.get(wellID);
			if(pts.GetNumberOfPoints()<1)
				continue;
			double first_pt[] = pts.GetPoint(0);
			double last_pt[] = pts.GetPoint(pts.GetNumberOfPoints()-1);
			if( (first_pt.length!=3) || (last_pt.length!=3) )
				continue;
			double top_pt[], bottom_pt[];
			if(first_pt[2]>last_pt[2]) {
				top_pt = first_pt;
				bottom_pt = last_pt;
			} else {
				top_pt = last_pt;
				bottom_pt = first_pt;
			}
			top_pts.add(new Vector3(top_pt[0], top_pt[1], top_pt[2]));
			bottom_pts.add(new Vector3(bottom_pt[0], bottom_pt[1], bottom_pt[2]));
			Nsamples.add(pts.GetNumberOfPoints());
			String uniqueID = UUID.randomUUID().toString();
			well_UUIDs.add(uniqueID);
		}
	}
	
	public void SetBasepath(String path) {
		basepath = path;
	}
	
	public void SetExtensionString(String extension) {
		ext_str = extension;
	}
	
	public void write() {
		if( (P==null) || (I==null) || (NM==null) )
			return;
		if( (P.size()<1) || (I.size()<1) || (NM.size()<1) )
			return;
		if( (basepath==null) || (ext_str==null) )
			return;
		
		if( (parentpath==null) || (basename==null) ) {
			ext_str = ext_str.substring(1, ext_str.length());
			int slashpos = basepath.lastIndexOf('/');
			parentpath = basepath.substring(0, slashpos);
			basename = basepath.substring(slashpos+1, basepath.length());
		}

		System.out.println(String.format("Basepath: %s, Extension: %s", basepath, ext_str));
		
		writeWellHeader();
		writeWellDeviations();
		writeWellLog();
		zipFiles();
	}
	
	protected void writeWellHeader() {
		try {
			String fname = parentpath+"/"+"Wellheader";
			BufferedWriter header_writer = new BufferedWriter(new FileWriter(fname));
			header_writer.append(String.format("WellName    X-Coord      Y-Coord      KB      TopDepth     BottomDepth    Symbol\n"));
			int N = P.size();
			for(int wellID = 0; wellID<N; wellID++) {
				header_writer.append(String.format("%s    %.6f      %.6f      %.6f      %.6f     %.6f    %d\n", NM.get(wellID), top_pts.get(wellID).get(0), top_pts.get(wellID).get(1), .0, top_pts.get(wellID).get(2), bottom_pts.get(wellID).get(2), 1));
			}
			header_writer.close();
			
			wellHeader_path = fname;
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
	
	protected void writeWellDeviations() {
		try {
			int N = P.size();
			for(int wellID = 0; wellID<N; wellID++) {
				String fname = parentpath+"/"+NM.get(wellID)+".dev";
				BufferedWriter header_writer = new BufferedWriter(new FileWriter(fname));
				header_writer.append(String.format("# %-25s\n", "WELL TRACE FROM PETREL"));
				header_writer.append(String.format("# %-25s %s %s\n", "WELL NAME:", "Well", NM.get(wellID)));
				header_writer.append(String.format("# %-25s %f\n", "WELL HEAD X-COORDINATE:", top_pts.get(wellID).get(0)));
				header_writer.append(String.format("# %-25s %f\n", "WELL HEAD Y-COORDINATE:", top_pts.get(wellID).get(1)));
				header_writer.append(String.format("# %-25s %f\n", "WELL KB", .0));
				header_writer.append(String.format("# %-25s %d\n", "WELL TYPE", 1));
				header_writer.append("# MD AND TVD ARE REFERENCED (=0) AT KB AND INCREASE DOWNWARDS\n");
				header_writer.append("# ANGLES ARE GIVEN IN DEGREES\n");
				
				header_writer.append("#========================================================================================================================\n");
				header_writer.append("      MD              X             Y            Z          TVD           DX           DY         AZIM         INCL\n");
				header_writer.append("#========================================================================================================================\n");

				vtkPoints pts = P.get(wellID);
				int M = pts.GetNumberOfPoints();
				double pt_p[] = pts.GetPoint(0);
				for(int measureID=0; measureID<M; measureID++) {
					double pt_c[] = pts.GetPoint(measureID);
					double dx=0, dy=0;
					if(measureID>0) {
						dx = pt_c[0] - pt_p[0];
						dy = pt_c[1] - pt_p[1];
					}
					header_writer.append(String.format("\t%.6f\t%.6f\t%.6f\t%.6f\t%.6f\t%.6f\t%.6f\t%.6f\t%.6f\n", pt_c[2], pt_c[0], pt_c[1], -pt_c[2], pt_c[2], dx, dy, .0, .0));
					pt_p = pt_c;
				}
				header_writer.close();
				
				wellDeviation_paths.add(fname);
			}
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
	
	protected void writeWellLog() {
		int N = P.size();
		for(int wellID = 0; wellID<N; wellID++) {
			try {
				String fname = parentpath+"/"+NM.get(wellID)+".las";
				BufferedWriter header_writer = new BufferedWriter(new FileWriter(fname));
				header_writer.append(String.format("# LAS format log file from PETREL\n"));
				header_writer.append(String.format("# Project units are specified as depth units\n"));
				header_writer.append(String.format("#==================================================================\n"));
				header_writer.append(String.format("~Version Information\n"));
				header_writer.append(String.format("VERS.   2.0:\n"));
				header_writer.append(String.format("WRAP.   NO:\n"));
				header_writer.append(String.format("#==================================================================\n"));
				
				header_writer.append(String.format("~Well\n"));
				header_writer.append(String.format("%-9s %.6f : %s\n", "STRT .m", top_pts.get(wellID).get(2), ""));
				header_writer.append(String.format("%-9s %.6f : %s\n", "STOP .m", bottom_pts.get(wellID).get(2), ""));
				header_writer.append(String.format("%-9s %.6f : %s\n", "STEP .m", .0, ""));
				header_writer.append(String.format("%-9s %.6f : %s\n", "NULL .", -999.999999, ""));
				header_writer.append(String.format("%-9s %s : %s\n", "COMP.", "", "COMPANY"));
				header_writer.append(String.format("%-9s %s : %s\n", "WELL.", NM.get(wellID), "WELL"));
				header_writer.append(String.format("%-9s %s : %s\n", "FLD.", "", "FIELD"));
				header_writer.append(String.format("%-9s %s : %s\n", "LOC.", "", "LOCATION"));
				header_writer.append(String.format("%-9s %s : %s\n", "SRVC.", "", "SERVICE COMPANY"));
				Date today = Calendar.getInstance().getTime();
				header_writer.append(String.format("%-9s %s : %s\n", "DATE.", today.toString(), "DATE"));
				header_writer.append(String.format("%-9s %s : %s\n", "PROV.", "", "PROVINCE"));
				header_writer.append(String.format("%-9s %s : %s\n", "UWI.", well_UUIDs.get(wellID), "UNIQUE WELL ID"));
				header_writer.append(String.format("%-9s %s : %s\n", "API.", "", "API NUMBER"));
				header_writer.append(String.format("#==================================================================\n"));

				header_writer.append(String.format("~Curve\n"));
				header_writer.append(String.format("%-9s %s : %s\n", "DEPT .m", "", "DEPTH"));
				header_writer.append(String.format("%-9s %s : %s\n", "FACI .m", "", "FACIES"));

				header_writer.append(String.format("~Parameter\n"));
				header_writer.append(String.format("#==================================================================\n"));
				header_writer.append(String.format("~Ascii\n"));
				
				vtkPoints pts = P.get(wellID);
				Integer[] indicators = I.get(wellID);
				int Mp = pts.GetNumberOfPoints();
				int Mi = indicators.length;
				if(Mp!=Mi)
					continue;
				int M = Mi;
				for(int measureID=0; measureID<M; measureID++) {
					double pt_c[] = pts.GetPoint(measureID);
					int facies_c = indicators[measureID];
					header_writer.append(String.format("\t%.6f\t%.6f\n", pt_c[2], (double)facies_c));
				}
				header_writer.close();
				
				wellLog_paths.add(fname);
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
	}
	
	protected void zipFiles() {
		try {
			String zip_path = parentpath+"/"+basename+".zip";
			FileOutputStream fos = new FileOutputStream(zip_path);
			ZipOutputStream zipOut = new ZipOutputStream(fos);
			byte[] bytes = new byte[1024];
			int blen;
			
			File fileToZip = new File(wellHeader_path);
			FileInputStream fis = new FileInputStream(fileToZip);
			ZipEntry zipEntry = new ZipEntry(fileToZip.getName());
			zipOut.putNextEntry(zipEntry);
			while((blen=fis.read(bytes)) >= 0) {
				zipOut.write(bytes, 0, blen);
			}
			fis.close();
			fileToZip=null;
			fis=null;
			zipEntry=null;
			
			for(String srcFilePath : wellDeviation_paths) {
				fileToZip = new File(srcFilePath);
				fis = new FileInputStream(fileToZip);
				zipEntry = new ZipEntry(fileToZip.getName());
				zipOut.putNextEntry(zipEntry);
				while((blen=fis.read(bytes)) >= 0) {
					zipOut.write(bytes, 0, blen);
				}
				fis.close();
				fileToZip=null;
				fis=null;
				zipEntry=null;
			}
			
			for(String srcFilePath : wellLog_paths) {
				fileToZip = new File(srcFilePath);
				fis = new FileInputStream(fileToZip);
				zipEntry = new ZipEntry(fileToZip.getName());
				zipOut.putNextEntry(zipEntry);
				while((blen=fis.read(bytes)) >= 0) {
					zipOut.write(bytes, 0, blen);
				}
				fis.close();
				fileToZip=null;
				fis=null;
				zipEntry=null;
			}
			
			zipOut.close();
			fos.close();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
}
