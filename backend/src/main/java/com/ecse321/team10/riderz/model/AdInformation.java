package com.ecse321.team10.riderz.model;

public class AdInformation {
	private String name;
	private String phone;
	private String make;
	private String model;
	private int year;
	private String licensePlate;
	
	public AdInformation(String name, String phone, String make,
						 String model, int year, String licensePlate) {
		this.name = name;
		this.phone = phone;
		this.make = make;
		this.model = model;
		this.year = year;
		this.licensePlate = licensePlate;
	}
	
	public String getName() {
		return this.name;
	}
	
	public String getPhone() {
		return this.phone;
	}
	
	public String getMake() {
		return this.make;
	}
	
	public String getModel() {
		return this.model;
	}
	
	public int getYear() {
		return this.year;
	}
	
	public String getLicensePlate() {
		return this.licensePlate;
	}
}
