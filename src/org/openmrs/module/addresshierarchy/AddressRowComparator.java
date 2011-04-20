package org.openmrs.module.addresshierarchy;

import java.util.Comparator;


// TODO: confirm that this is being used somewhere

public class AddressRowComparator implements Comparator<Object[]>{

	@Override
	public int compare(Object[] row0, Object[] row1) {
		if(row0.length<0 || row1.length<0){
			return 0;
		}
		
		if((Integer)row0[0] > (Integer)row1[0])  
			return 1; 
		else if((Integer)row0[0] < (Integer)row1[0])
				return -1;
		else return 0;
		
	}

}
