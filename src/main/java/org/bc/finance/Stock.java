package org.bc.finance;

import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;

@Entity
public class Stock {

	@Id
//	@GeneratedValue(strategy=GenerationType.AUTO)
	public String id;
	
	public String code;
	
	public String name;
	
	public String type;
}
