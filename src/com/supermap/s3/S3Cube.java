package com.supermap.s3;
/**
 * S3Cube类用来表示空间划分后的一个单元立方体
 * @author SunYasong
 *
 */
public class S3Cube {

	private double lngDown;
	private double lngUp;
	private double latDown;
	private double latUp;
	private double heightDown;
	private double heightUp;
	private static final double lngOffset = S3Parameter.LNG_OFFSET;
	private static final double latOffset = S3Parameter.LAT_OFFSET;

	public S3Cube(double lngDown, double lngUp, double latDown, double latUp, double heightDown, double heightUp) {
		super();
		this.lngDown = lngDown;
		this.lngUp = lngUp;
		this.latDown = latDown ;
		this.latUp = latUp;
		this.heightDown = heightDown;
		this.heightUp = heightUp;
	}

	public double getLngUp() {
		return lngUp;
	}

	public double getLngDown() {
		return lngDown;
	}

	public double getLatUp() {
		return latUp;
	}

	public double getLatDown() {
		return latDown;
	}

	public double getHeightUp() {
		return heightUp;
	}

	public double getHeightDown() {
		return heightDown;
	}
	
	public S3Cube toLngLatCube(){
		return new S3Cube(this.lngDown - lngOffset, this.lngUp - lngOffset, this.latDown - latOffset, this.latUp - latOffset, this.heightDown, this.heightUp);
	}

	@Override
	public String toString() {
		return " Cube范围：\n [LngDown=" + lngDown + ", LngUp=" + lngUp + "\n LatDown=" + latDown + ", LatUp=" + latUp
				+ "\n HeightDown=" + heightDown + ", HeightUp=" + heightUp + "]";
	}

}
