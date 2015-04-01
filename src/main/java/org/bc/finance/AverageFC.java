package org.bc.finance;

public class AverageFC implements Comparable<AverageFC>{

	public float fangcha;
	
	public String code;
	
	public String name;

	@Override
	public int compareTo(AverageFC other) {
		if(fangcha>other.fangcha){
			return 1;
		}else if(fangcha<other.fangcha){
			return -1;
		}
		return 0;
	}
}
