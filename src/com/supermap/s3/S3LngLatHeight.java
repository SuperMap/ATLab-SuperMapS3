package com.supermap.s3;

public class S3LngLatHeight {
	//经度
	private final double lngRadians;
	//纬度
	private final double latRadians;
	//高程-单位：米
	private final double height;

	public S3LngLatHeight() {
		// TODO Auto-generated constructor stub
		this(0.0, 0.0, 0.0);
	}

	/**
	 * 经纬度高程点的构造方法
	 * @param lngRadians 经度
	 * @param latRadians 纬度
	 * @param height 高程，从地心到点的距离，单位：米
	 */
	public S3LngLatHeight(double lngRadians, double latRadians, double height) {
		super();
		this.lngRadians = lngRadians;
		this.latRadians = latRadians;
		this.height = height;
	}

	/**
	 * 将经纬度高程点转换为s3点，地理经纬度经度+180，纬度+80，将范围变成从0开始
	 * @return
	 */
	public S3Point toS3Point() {
		/* 经纬度不变，将高度转换为角度 */
		double heightDegrees = height / S3Parameter.UNIT_METER_DEGREE;
		return new S3Point(this.lngRadians+S3Parameter.LNG_OFFSET, this.latRadians+S3Parameter.LAT_OFFSET, heightDegrees);
	}

	@Override
	public String toString() {
		return "S3经纬度高程点：[lngRadians=" + lngRadians + ", latRadians=" + latRadians + ", height=" + height + "]";
	}
	
	
}
