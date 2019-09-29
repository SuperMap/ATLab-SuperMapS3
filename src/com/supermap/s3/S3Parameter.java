package com.supermap.s3;

public strictfp final class S3Parameter {

	// 地球的长轴长度
	public static final double EARTH_LONG_RADIUS_METERS = 6378137.0;
	// 计算1度代表多少米
	public static final double UNIT_METER_DEGREE = Math.PI * EARTH_LONG_RADIUS_METERS / 180.0;

	/**
	 * 坐标映射：把经度从[-180,180]映射到[0,360],纬度从[-90，90]映射到[0，180]
	 */
	public static final double LNG_OFFSET = 180;
	public static final double LAT_OFFSET = 90;
}
