package psofortemplatematching;

import java.awt.Color;
import java.awt.Point;
import java.awt.Rectangle;
import java.awt.geom.AffineTransform;
import java.awt.image.AffineTransformOp;
import java.awt.image.BufferedImage;
//Read file
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Scanner;

import processing.core.PConstants;
import processing.core.PImage;
import processing.core.PGraphics;

//For drawing...
import java.awt.Graphics2D;
import java.awt.image.BufferedImage;

public class LidarMap2D {
	/*public static class mapDataType {
		public static final int DEGERYOK = 0;
		public static final int BILINMEYEN = 1;
		public static final int BOS = 2;
		public static final int YASAK = 3;
		public static final int DUVAR = 4;
		public static final int SABITENGEL = 5;
	}*/

	public PImage map; // Map data for calculations
	public Point center;
	private ArrayList<Point> inflationTable;
	
	public LidarMap2D(String path) {
		this(path, null);
	}

	public LidarMap2D(String path, Point center) {
		this.center = center;
		
		initMap(readMap(path));
		//map = rotate90(map);
		initInfTable(8);//8
		inflate();
	}

	public static PImage rotate90(PImage srcImg) {
		BufferedImage srcBImg = (BufferedImage) srcImg.getNative();
		AffineTransform transform = new AffineTransform();

		transform.translate( -(srcBImg.getWidth()-srcBImg.getHeight()) / 2,
				(srcBImg.getWidth()-srcBImg.getHeight()) / 2);
		BufferedImage destBImg = new BufferedImage(srcBImg.getHeight(), srcBImg.getWidth(),
				BufferedImage.TYPE_INT_ARGB);
		transform.rotate(Math.toRadians(90), srcBImg.getWidth() / 2, srcBImg.getHeight() / 2);
		AffineTransformOp op = new AffineTransformOp(transform, AffineTransformOp.TYPE_NEAREST_NEIGHBOR);
		srcBImg = op.filter(srcBImg, destBImg);

		// Buffered Image to PImage
		PImage destPImg = new PImage(destBImg.getWidth(), destBImg.getHeight(), PConstants.ARGB);
		destBImg.getRGB(0, 0, destPImg.width, destPImg.height, destPImg.pixels, 0, destPImg.width);
		destPImg.updatePixels();
		return destPImg;
	}



	public int get(int x, int y) throws NullPointerException {
		Color color = new Color(map.get(x, y));
		return color.getRed();
	}

	public void set(int x, int y, int blueChannel) {
		map.set(x, y, blueChannel);
	}

	// Reads map on the specified file(path). Converts map to image
	private void initMap(byte[] byteMap) {
		int width = 2048; //TODO: Parameterize
		int height = 2048;

		// Decrease region by not converting coordinates outside of roi.
		Rectangle roi = new Rectangle();
		int minX = width - 1;
		int minY = height - 1;
		int maxX = 0;
		int maxY = 0;
		for (int x = 0; x < width; x++) {
			for (int y = 0; y < height; y++) {
				if (byteMap[y * width + x] > 1) {
					if (x < minX) {
						minX = x;
					}
					if (x > maxX) {
						maxX = x;
					}
					if (y < minY) {
						minY = y;
					}
					if (y > maxY) {
						maxY = y;
					}
				}
				roi.x = minX;
				roi.y = minY;
				roi.width = maxX - minX+1;
				roi.height = maxY - minY+1;
			}
		}

		// Adjust the roi to make the center parameter as roi's center point.
		if (center != null) {
			int dXLeft = center.x - roi.x;
			int dXRight = (int) (roi.getMaxX() - center.x);
			int dYUp = center.y - roi.y;
			int dYDown = (int) (roi.getMaxY() - center.y);
			if (dXLeft > dXRight) {
				roi.width = dXLeft * 2;
			} else {
				roi.width = dXRight * 2;
				roi.x = center.x - dXRight;
			}
			if (dYUp > dYDown) {
				roi.height = dYUp * 2;
			} else {
				roi.height = dYDown * 2;
				roi.y = center.y - dYDown;
			}
		}

		int bottomRightX = (int) roi.getMaxX();
		int bottomRightY = (int) roi.getMaxY();
		map = new PImage(roi.width, roi.height, PConstants.ARGB); // Size of image decided according to roi.
		int imgX = 0;
		int imgY;
		for (int x = roi.x; x < bottomRightX; x++) {
			imgY = 0;
			for (int y = roi.y; y < bottomRightY; y++) {
				map.set(imgX, imgY, byteMap[y * width + x]);
				++imgY;
			}
			++imgX;
		}
	}

	//Constructs mapImg from map
	public PImage getMapImg() {
		return getMapImg(map);
	}

	//Constructs mapImg from map
	public static PImage getMapImg(PImage map) {
		PImage mapImg = new PImage(map.width, map.height, PConstants.ARGB); // Size of image decided according to roi.

		for (int i = 0; i < map.pixels.length; i++) {
			int cellType = map.pixels[i];// red channel shows the type of cell.
			switch (cellType) {
			case 1: // Unknown
				mapImg.pixels[i] = (new Color(0, 0, 0, 0)).getRGB();
				break;
			case 2: // Empty
				mapImg.pixels[i] = (new Color(175, 255, 50, 255)).getRGB();
				break;
			case 3: // Inflated
				mapImg.pixels[i] = (new Color(255, 255, 0, 255)).getRGB();
				break;
			case 4: // Obstacle
				mapImg.pixels[i] = (new Color(248, 24, 0, 255)).getRGB();
				break;
			case 5: // Obstacle
				mapImg.pixels[i] = (new Color(248, 24, 0, 255)).getRGB();
				break;
			}
		}
		//mapImg.save("C:\\Users\\ERDEM\\Desktop\\genMap.png");
		return mapImg;
	}

	private byte[] readMap(String path) {
		String buffer;
		byte[] data = null;
		Scanner input = null;
		try {
			File file = new File(path);
			input = new Scanner(file);
			if (input.hasNext()) {
				buffer = input.next();
				data = buffer.getBytes();
			}
			input.close();
		} catch (IOException e) {
			e.printStackTrace();
		} finally {
			if (input != null) {
				input.close();
			}
		}
		return data;
	}

	private void initInfTable(int inflationWidth) {
		int inflationWidthSq = inflationWidth * inflationWidth;
		inflationTable = new ArrayList<Point>();
		for (int y = -inflationWidth; y <= inflationWidth; y++)
		 {
			for (int x = -inflationWidth; x <= inflationWidth; x++)
			 {
				if (x * x + y * y <= inflationWidthSq)
				 {
					inflationTable.add(new Point(x, y));// origin+x , origin+y
				}
			}
		}
	}

	//Inflates map by inflationTable data
	private void inflate() {
		if(inflationTable==null) {
			return;
		}

		for (int x = 0; x < map.width; x++) {
			for (int y = 0; y < map.height; y++) {
				int ind = y * map.width + x;
				if (map.pixels[ind] > 3) { // if coordinate is not empty
					for (Point p : inflationTable) {
											try {
												int inflateInd = (y + p.y) * map.width + (x + p.x);
												if (map.pixels[inflateInd] < 3)
													map.pixels[inflateInd] = 3;
											} catch (IndexOutOfBoundsException e) {
											}
										}
				}
			}
		}
	}

}