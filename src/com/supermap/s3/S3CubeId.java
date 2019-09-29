package com.supermap.s3;

import java.math.BigDecimal;
import java.math.BigInteger;
import java.math.RoundingMode;
import java.util.Arrays;

public class S3CubeId {
	// byte数组用来存储编码后的id，最大长度为12
	private byte[] id;
	// 层级
	private int level;
	// 最大层级
	public static final int MAX_LEVEL = 31; // Valid levels: 0..MAX_LEVEL
	// 每一个方向上可以达到的最大值，有xyz三个方向
	public static final int MAX_SIZE = 1 << MAX_LEVEL;
	// 维度，当前使用的是三维点
	public static final int dimensions = 3;

	// 默认的空构造方法，返回一个长12的字节数组与层级为0的S3CubeId类
	public S3CubeId() {
		// TODO Auto-generated constructor stub
		this.id = new byte[12];
		this.level = 0;
	}

	/**
	 * 用长12的字节数组来构建S3CubeId
	 * 
	 * @param id
	 *            长度为12的byte数组
	 */
	public S3CubeId(byte[] id) {
		this.id = id;
		// 计算id对应的level
		BigInteger cubeId = new BigInteger(id);
		String strId = cubeId.toString(2);
		// System.out.println("strId" + strId);
		// 对于二进制长度小于94的id，在前面补0，统一所有id的长度
		if (strId.length() < 94) {
			String zeros = "";
			for (int i = 1; i <= 94 - strId.length(); i++) {
				zeros += "0";
			}
			strId = zeros + strId;
			// System.out.println("strId" + strId);
		}
		// String reverseStrId = new StringBuilder(strId).reverse().toString();
		// 找到二进制的第一个1所在的位数，就是层级
		for (int i = 0; i < strId.length(); i++) {
			if (strId.charAt(i) == 49) {
				this.level = (94 - i) / 3;
				break;
			}
		}
		// System.out.println(cubeId);
		// System.out.println(new BigDecimal(cubeId));
	}

	public byte[] getId() {
		return id;
	}

	public int getLevel() {
		return level;
	}

	/**
	 * 以最大层级来构建cube 注意:S3LngLatHeight可以通过toPoint()转换为S3 Point
	 * 
	 * @param point
	 *            S3点
	 * @return
	 */
	public static S3CubeId fromS3Point(S3Point point) {
		return fromPoint(point, MAX_LEVEL);
	}

	/**
	 * 获得点所在空间在指定层级下的编码 注意:S3LngLatHeight可以通过toPoint()转换为S3 Point
	 * 
	 * @param point
	 *            点
	 * @param level
	 *            层级
	 * @return S3CubeId
	 */
	public static S3CubeId fromPoint(S3Point point, int level) {
		// 将角度转换为xyz坐标
		// 注意：i存的是纬度，j存的是经度，k存的是高度
		level = (level < 0) ? 1 : level;
		level = (level > MAX_LEVEL) ? MAX_LEVEL : level;
		int i = stToIJK(point.getY(), level);
		int j = stToIJK(point.getX(), level);
		int k = stToIJK(point.getZ(), level);
		return fromIJK(i, j, k, level);
	}

	/**
	 * 通过xyz坐标和层级获得id
	 * 
	 * @param i
	 *            纬度对应的坐标
	 * @param j
	 *            经度对应的坐标
	 * @param k
	 *            高度对应的坐标
	 * @param level
	 *            层级
	 * @return
	 */
	private static S3CubeId fromIJK(int i, int j, int k, int level) {
		BigInteger id = index(level, i, j, k);
		BigInteger cubeId = new BigInteger("1").shiftLeft(level * dimensions - 1).add(id);
		// System.out.println("1-"+cubeId.toString(2));
		byte[] cubeIdByte = cubeId.toByteArray();
		return new S3CubeId(cubeIdByte);
	}

	/**
	 * 将角度转换成xyz坐标
	 * 
	 * @param s
	 * @return
	 */
	private static int stToIJK(double s, int level) {
		BigDecimal b1 = BigDecimal.valueOf(s);
		BigDecimal b2 = BigDecimal.valueOf(Math.pow(2, level));
		BigDecimal b3 = BigDecimal.valueOf(360);
		// BigDecimal b4 =
		// b1.multiply(b2,MathContext.DECIMAL128).divide(b5,0,RoundingMode.CEILING);
		BigDecimal b4 = b1.multiply(b2).divide(b3, 0, RoundingMode.HALF_UP);
		return b4.toBigInteger().intValue();
	}

	/**
	 * 通过指定层级和点来计算id
	 * 
	 * @param level
	 *            层级
	 * @param point
	 *            点
	 * @return
	 */
	private static BigInteger index(int level, long... point) {
		return toIndex(level, transposedIndex(level, point));
	}

	/**
	 * 通过转置信息和层级来计算id
	 * 
	 * @param level
	 * @param transposedIndex
	 * @return
	 */
	private static BigInteger toIndex(int level, long... transposedIndex) {
		int length = dimensions * level;
		byte[] b = new byte[length];
		int bIndex = length - 1;
		long mask = 1L << (level - 1);
		for (int i = 0; i < level; i++) {
			for (int j = 0; j < transposedIndex.length; j++) {
				if ((transposedIndex[j] & mask) != 0) {
					b[length - 1 - bIndex / 8] |= 1 << (bIndex % 8);
				}
				bIndex--;
			}
			mask >>= 1;
		}
		// b is expected to be BigEndian
		return new BigInteger(1, b);
	}

	/**
	 * 通过点和层级计算转置后的信息
	 * 
	 * @param level
	 * @param point
	 * @return
	 */
	private static long[] transposedIndex(int level, long... point) {
		final long M = 1L << (level - 1);
		final int n = point.length; // n: Number of dimensions
		final long[] x = Arrays.copyOf(point, n);
		long p, q, t;
		int i;
		// Inverse undo
		for (q = M; q > 1; q >>= 1) {
			p = q - 1;
			for (i = 0; i < n; i++)
				if ((x[i] & q) != 0)
					x[0] ^= p; // invert
				else {
					t = (x[0] ^ x[i]) & p;
					x[0] ^= t;
					x[i] ^= t;
				}
		} // exchange
			// Gray encode
		for (i = 1; i < n; i++)
			x[i] ^= x[i - 1];
		t = 0;
		for (q = M; q > 1; q >>= 1)
			if ((x[n - 1] & q) != 0)
				t ^= q - 1;
		for (i = 0; i < n; i++)
			x[i] ^= t;

		return x;
	}

	/**
	 * 通过id计算出映射的ijk，进一步计算出cube的范围
	 * 
	 * @param id
	 * @param level
	 */
	public S3Cube getS3Cube() {
		BigInteger cubeId = new BigInteger(this.id)
				.subtract(new BigInteger("1").shiftLeft(dimensions * this.level - 1));
		// System.out.println(cubeId.toString(2));
		// 计算出的xyz坐标
		long[] IJK = transposedIndexToPoint(this.level, transpose(cubeId));
		// 区间存储在st数组里，st[0]和st[1]是纬度上下限，st[2]和st[3]是经度，st[4]和st[5]是高度
		double[] st = iJKToSt(IJK);
		return new S3Cube(st[2], st[3], st[0], st[1], st[4], st[5]);
	}

	private double[] iJKToSt(long[] iJK) {
		// TODO Auto-generated method stub
		BigDecimal b2 = BigDecimal.valueOf(Math.pow(2, 31));
		BigDecimal b3 = BigDecimal.valueOf(360);
		double[] result = new double[iJK.length * 2];
		for (int i = 0; i < iJK.length * 2; i = i + 2) {
			BigDecimal b1 = BigDecimal.valueOf(iJK[i / 2]);
			result[i] = b1.multiply(b3).divide(b2, 20, RoundingMode.HALF_UP).doubleValue();
			result[i + 1] = b1.add(new BigDecimal("1")).multiply(b3).divide(b2, 20, RoundingMode.HALF_UP).doubleValue();
			// System.out.println(b1+"=="+b2+"=="+b3);
			// System.out.println(i+"=="+result[i]+","+(i+1)+"=="+result[i+1]);

		}
		return result;
	}

	long[] transpose(BigInteger index) {
		long[] x = new long[3];
		transpose(index, x);
		return x;
	}

	private void transpose(BigInteger index, long[] x) {
		byte[] b = index.toByteArray();
		for (int idx = 0; idx < 8 * b.length; idx++) {
			if ((b[b.length - 1 - idx / 8] & (1L << (idx % 8))) != 0) {
				int dim = (dimensions * level - idx - 1) % dimensions;
				int shift = (idx / dimensions) % level;
				x[dim] |= 1L << shift;
			}
		}
	}

	static long[] transposedIndexToPoint(int bits, long... x) {
		final long N = 2L << (bits - 1);
		// Note that x is mutated by this method (as a performance improvement
		// to avoid allocation)
		int n = x.length; // number of dimensions
		long p, q, t;
		int i;
		// Gray decode by H ^ (H/2)
		t = x[n - 1] >> 1;
		// Corrected error in Skilling's paper on the following line. The
		// appendix had i >= 0 leading to negative array index.
		for (i = n - 1; i > 0; i--)
			x[i] ^= x[i - 1];
		x[0] ^= t;
		// Undo excess work
		for (q = 2; q != N; q <<= 1) {
			p = q - 1;
			for (i = n - 1; i >= 0; i--)
				if ((x[i] & q) != 0L)
					x[0] ^= p; // invert
				else {
					t = (x[0] ^ x[i]) & p;
					x[0] ^= t;
					x[i] ^= t;
				}
		} // exchange
		return x;
	}
}
