package com.supermap.s3;
/**
 * S3中的点
 * @author SunYasong
 *
 */
public strictfp class S3Point {

	//经度
	final double x;
	//纬度
	final double y;
	//高程-角度
	final double z;

	public S3Point() {
		// TODO Auto-generated constructor stub
		x = y = z = 0;
	}

	/**
	 * s3点的构造方法
	 * 
	 * @param x 经度
	 * @param y 纬度
	 * @param z 高程转换成的角度，具体计算方式为：（高程*180）/（3.14*地球长轴长度）
	 */
	public S3Point(double x, double y, double z) {
		this.x = x;
		this.y = y;
		this.z = z;
	}

	public double getX() {
		return x;
	}

	public double getY() {
		return y;
	}

	public double getZ() {
		return z;
	}

	@Override
	public String toString() {
		return "S3Point [x=" + x + ", y=" + y + ", z=" + z + "]";
	}
	
}
