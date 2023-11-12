package ckehl.vtk;

import java.util.HashMap;
import java.util.Map;
import CGEL.common.Vector.*;

/**
 * Author: Dr. Christian Kehl
 * Date: 13 April 2020
 **/

public class LookUpValues {
	public Map<Integer, Vector4> categoryColours = null;
	
	public LookUpValues() {
		categoryColours = new HashMap<Integer, Vector4>();
		categoryColours.put( 0, new Vector4(1.0        , 1.0        , 1.0        , 1.0));
		categoryColours.put( 1, new Vector4(228.0/255.0, 26.0 /255.0, 28.0 /255.0, 1.0));
		categoryColours.put( 2, new Vector4(45.0 /255.0, 124.0/255.0, 184.0/255.0, 1.0));
		categoryColours.put( 3, new Vector4(77.0 /255.0, 175.0/255.0, 74.0 /255.0, 1.0));
		categoryColours.put( 4, new Vector4(151.0/255.0, 72.0 /255.0, 163.0/255.0, 1.0));
		categoryColours.put( 5, new Vector4(1.0        , 127.0/255.0, 0.0        , 1.0));
		categoryColours.put( 6, new Vector4(1.0        , 1.0        , 0.0        , 1.0));
		categoryColours.put( 7, new Vector4(166.0/255.0, 86.0 /255.0, 40.0 /255.0, 1.0));
		categoryColours.put( 8, new Vector4(247.0/255.0, 45.0 /255.0, 153.0/255.0, 1.0));
		categoryColours.put( 9, new Vector4(112.0/255.0, 112.0/255.0, 112.0/255.0, 1.0));
		categoryColours.put(10, new Vector4(228.0/255.0, 81.0 /255.0, 84.0 /255.0, 1.0));
		categoryColours.put(11, new Vector4(106.0/255.0, 152.0/255.0, 184.0/255.0, 1.0));
		categoryColours.put(12, new Vector4(127.0/255.0, 174.0/255.0, 125.0/255.0, 1.0));
		categoryColours.put(13, new Vector4(156.0/255.0, 107.0/255.0, 163.0/255.0, 1.0));
		categoryColours.put(14, new Vector4(255.0/255.0, 165.0/255.0, 91.0 /255.0, 1.0));
		categoryColours.put(15, new Vector4(1.0        , 1.0        , 91.0 /255.0, 1.0));
		categoryColours.put(16, new Vector4(166.0/255.0, 120.0/255.0, 96.0 /255.0, 1.0));
		categoryColours.put(17, new Vector4(247.0/255.0, 129.0/255.0, 192.0/255.0, 1.0));
		categoryColours.put(18, new Vector4(164.0/255.0, 164.0/255.0, 164.0/255.0, 1.0));
	}
}
