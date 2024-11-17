package psofortemplatematching;

import java.awt.Color;
import java.util.ArrayList;

import processing.core.PImage;

public class ProblemSet{
	public static PImage baseImg;
	public static PImage parImg;
	public static ArrayList<PImageWithAngle> parImgSet;
	public static int LOC_X_LOW = 0;
	public static int LOC_X_HIGH = 975;
	public static int LOC_Y_LOW = 0;
	public static int LOC_Y_HIGH = 605;	//TODO: Sinirlari otomatik al!!!
	public static double VEL_LOW = -2.1;
	public static double VEL_HIGH = 2.1;

	//public static final double ERR_TOLERANCE = 1E-20; // the smaller the tolerance, the more accurate the result.

	public ProblemSet(PImage frame, PImage putImg) {
		baseImg = frame;
		parImg = putImg;
		LOC_X_HIGH = frame.width;
		LOC_Y_HIGH = frame.height;
	}

	public ProblemSet(PImage frame, ArrayList<PImageWithAngle> imgAngleSet) {
		baseImg = frame;
		parImgSet = imgAngleSet;
		LOC_X_HIGH = frame.width;
		LOC_Y_HIGH = frame.height;
	}

	// Binary comparison of overlapped pixel.
	public static double evaluate1(Location location) {
		double xPos = location.getLoc()[0];
		double yPos = location.getLoc()[1];
		double matchCount = 0;
		int numOfCrossingPix = 0;

		Color baseColor, parColor;
		for (int x = 0; x < parImg.width; x++) {
			for (int y = 0; y < parImg.height; y++) {
				baseColor = new Color(baseImg.get((int) (x + xPos), (int) (y + yPos)), true);
				parColor = new Color(parImg.get(x, y), true);

				if (baseColor.getBlue() != 0 && baseColor.getRed() != 0 && baseColor.getGreen() != 0) { // Discard coordinate that isn't valid on base image.
					if (baseColor.getBlue() == 254 && parColor.getBlue() == 255) { // TODO: Correct the equality query.
						++matchCount;
					}
					++numOfCrossingPix;
				}
			}
		}

		if (numOfCrossingPix == 0) {
			return 0;
		}
		return matchCount;
	}

	// Returns 1 at min similarity, inf. at max similarity.
	public double evaluate(Location location, PImageWithAngle outputImg) {
		double finalResult = -1;
		PImageWithAngle finalImg = null;

		for(PImageWithAngle parImg:parImgSet){
			double result = 0;
			// double distRate=0;
			int xPos = (int) Math.round(location.getLoc()[0]);
			int yPos = (int) Math.round(location.getLoc()[1]);
			double similarity = 0;
			int numOfCrossingPix = 0;

			int baseColor, parColor;
			for (int x = 0; x < parImg.img.width; x++) {
				for (int y = 0; y < parImg.img.height; y++) {
					baseColor = baseImg.get(x + xPos, y + yPos);
					parColor = parImg.img.get(x, y);
					if (parColor >1) {
						similarity += matchPixels(baseColor, parColor);
						++numOfCrossingPix;
					}
				}
			}

			if (numOfCrossingPix == 0) {
				return 0;
			}
			result = similarity / numOfCrossingPix;

			if(result>finalResult){
				finalResult = result;
				finalImg = parImg;
			}
		}
		//Clone object
		outputImg.img =finalImg.img;
		outputImg.angleRad = finalImg.angleRad;

		return finalResult;
	}

	public double evaluate(Location location) {
		PImageWithAngle outputImgDummy = new PImageWithAngle(null, -1);
		return evaluate(location, outputImgDummy);
	}

	// Returns 1 at max distance, 0 at min distance.
	private static float matchPixels(int typeC1, int typeC2) {
		float sim = 0;
		if (typeC1 == 2 && typeC2 == 2) {
			sim =  (float) 0.1;// TODO: Parameterize
		} else if(typeC1 == 3 && typeC2 == 3) { //Both are inflated areas.
			sim = (float) 0.1;//0.3// TODO: Parameterize
		} else if(typeC1 == 4 && typeC2 == 4) { //Both are obstacles.
			sim = 3; // TODO: Parameterize
		} else if((typeC1==3||typeC1==4)&&(typeC2==3||typeC2==4)) { //implicitly includes "&&(typeC1!=typeC2)" condition
			sim = (float)0.1;
		}
		return sim;

		/*
		float dist = 1;
		if (typeC1 == 2 && typeC2 == 2)
			dist =  (float) 0.20;// TODO: Parameterize
		else if(typeC1 > 2 && typeC2 > 2) //Both are obstacles.
			dist = 0;// TODO: Parameterize

		return dist;*/
		
	}

}
