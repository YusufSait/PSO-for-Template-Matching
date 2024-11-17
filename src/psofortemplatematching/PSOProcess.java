package psofortemplatematching;

// Derived from: https://github.com/therealmanalu/pso-example-java

import java.awt.Color;
import java.awt.Point;
//Affine Transformation
import java.awt.geom.AffineTransform;
import java.awt.image.AffineTransformOp;
import java.awt.image.BufferedImage;
import java.util.ArrayList;
import java.util.Random;
import java.util.Vector;

import processing.core.PApplet;
import processing.core.PConstants;
import processing.core.PImage;
import java.io.File;

public class PSOProcess extends PApplet implements PSOConstants  {
	private ProblemSet problem;
	PImage frame;
	private String baseImgPath = "src/data/maps/full_size.map";	// Clipboard01.png
	private String searcImgPath = "src/data/maps/local-full_size.map";
	Point searchImgCenter;

	private Vector<Particle> swarm = new Vector<Particle>();
	private double[] pBest = new double[SWARM_SIZE];
	private Vector<Location> pBestLocation = new Vector<Location>();
	private double gBest;
	private Location gBestLocation;
	private double[] fitnessValueList = new double[SWARM_SIZE];
	Random generator = new Random();
	int updateCount = 0;

	public void resetPSO(){
		swarm = new Vector<Particle>();
		pBest = new double[SWARM_SIZE];
		pBestLocation = new Vector<Location>();
		gBest = 0;
		gBestLocation=null;
		fitnessValueList = new double[SWARM_SIZE];
		generator = new Random();
	}

	@Override
	public void setup(){
		//Match on custom location.
		/*frame = (new LidarMap2D(baseImgPath)).map;
		PImage searchImg = (new LidarMap2D(searcImgPath, searchImgCenter)).map;
		ArrayList<PImageWithAngle> imgAngleSet = getRotatedPImgSet(searchImg, 8);
		problem = new ProblemSet(frame, imgAngleSet);
		displayResult(new Location(new double[] {93, 454}));
		*/

		//drawSimilarityMatrix(8);

		runPSO();

		//testPSO(3, 8, new Location(new double[] { 93, 454 })); //4 (108,435)

		System.out.println("Done");
	}

	//Uses frame.
	private void displayResult(Location finalLoc) {
		System.out.println("     Best X: " + Math.round(finalLoc.getLoc()[0]));
		System.out.println("     Best Y: " + Math.round(finalLoc.getLoc()[1]));
		PImageWithAngle resultingImg = new PImageWithAngle(null, -1);
		//TODO: Get resulting score in the first run.
		System.out.println("     Value: " + problem.evaluate(finalLoc, resultingImg));
		System.out.println("	Angle: " + degrees(resultingImg.angleRad));

		PImage baseImg = LidarMap2D.getMapImg(frame);
		PImage searchImg = LidarMap2D.getMapImg(resultingImg.img);
		finalLoc.getLoc()[0] = round((float) finalLoc.getLoc()[0]);
		finalLoc.getLoc()[1] = round((float) finalLoc.getLoc()[1]);
		PImage dispImg = attachToFrame(baseImg, searchImg, (int) Math.round(finalLoc.getLoc()[0]),
							(int) Math.round(finalLoc.getLoc()[1]));
		
		dispImg = LidarMap2D.rotate90(dispImg);
		String savePath = System.getProperty("user.dir") + "/genMap.png";
		dispImg.save(savePath);
		System.out.println("Output is saved to: " + savePath);
		
		//Display the result
		//size(dispImg.width, dispImg.height);
		image(dispImg, 0, 0);
	}

	//Draw similarities on every coordinates
	private void drawSimilarityMatrix(int angleCount){
		frame = (new LidarMap2D(baseImgPath)).map;
		PImage searchImg = (new LidarMap2D(searcImgPath, searchImgCenter)).map;
		ArrayList<PImageWithAngle> imgAngleSet = getRotatedPImgSet(searchImg, angleCount);
		problem = new ProblemSet(frame, imgAngleSet);

		PImage dispImg = new PImage(frame.width, frame.height);
		double maxSim=-1;
		double[] maxCoor = new double[2];
		double[] tmpCoor = new double[2];
		Location tmpLocation = new Location(tmpCoor);
		for (int x = 0; x < frame.width; x++) {
			for (int y = 0; y < frame.height; y++) {
				System.out.println("%" + (y + (x * frame.height)) * 100 / frame.pixels.length);
				tmpCoor[0] = x;
				tmpCoor[1] = y;
				tmpLocation = new Location(tmpCoor);
				double sim = problem.evaluate(tmpLocation);
				if (sim > maxSim) {
					maxSim=sim;
					maxCoor[0] = tmpCoor[0];
					maxCoor[1] = tmpCoor[1];
				}
				int colorVal = (int) (sim / 0.207 * 255.0);
				try {
					dispImg.pixels[x + (y * frame.width)] = color(colorVal, colorVal, colorVal);
				} catch (NullPointerException e) {//Do nothing
				}
			}
		}
		System.out.println("Max: "+maxSim);
		System.out.println("Max coordinate: "+maxCoor[0]+"-"+maxCoor[1]);
		//imgAngleSet.get(0).img, (int)maxCoor[0],(int)maxCoor[1]);*/

		dispImg = LidarMap2D.rotate90(dispImg);
		dispImg.save("bin/data/simMap.png");

		size(dispImg.width, dispImg.height);
		image(dispImg, 0, 0);
	}

	private void runPSO(){
		frame = (new LidarMap2D(baseImgPath)).map;  // TODO: Rename frame
		LidarMap2D searchLidarMap = new LidarMap2D(searcImgPath);
		PImage searchImg = searchLidarMap.map;
		searchImgCenter = new Point(searchImg.width/2, searchImg.height/2); // TODO: Parameterize...
		searchLidarMap.center = searchImgCenter;
		
		//(new LidarMap2D(searcImgPath)).getMapImg().save("C:\\Users\\ERDEM\\Desktop\\no inflate.png");  //TODO: Delete

		ArrayList<PImageWithAngle> imgAngleSet = getRotatedPImgSet(searchImg, 8); // TODO: Parameterize
		problem = new ProblemSet(frame, imgAngleSet);

		Location finalLoc = execute();
		displayResult(finalLoc);
	}

	private void testPSO(int repeateCount, int angleCount, Location trueLocation){
		frame = (new LidarMap2D(baseImgPath)).map;
		PImage searchImg = (new LidarMap2D(searcImgPath, searchImgCenter)).map;
		ArrayList<PImageWithAngle> imgAngleSet = getRotatedPImgSet(searchImg, angleCount);
		problem = new ProblemSet(frame, imgAngleSet);

		final int repCount = repeateCount;
		Location[] results = new Location[repCount];
		double[] errInPx = new double[results.length];
		double totalErr = 0;

		for (int k = 0; k < repCount; k++) {
			results[k] = execute();
			errInPx[k] = sqrt((float) (Math.pow(results[k].getLoc()[0] - trueLocation.getLoc()[0], 2)
					+ Math.pow(results[k].getLoc()[1] - trueLocation.getLoc()[1], 2)));
			System.out.println(errInPx[k]);
			totalErr += errInPx[k];
			resetPSO();
		}
		System.out.println("Average error: "+totalErr/errInPx.length);
		System.out.println("Update count: "+updateCount);

		image(frame,0,0);
		System.out.println("Test done!");
	}

	public ArrayList<PImageWithAngle> getRotatedPImgSet(PImage srcImg, float numOfAngles){
		ArrayList<PImageWithAngle> resultSet = new ArrayList<PImageWithAngle>();
		double tRad = TWO_PI/numOfAngles;

		BufferedImage srcBImg = (BufferedImage) srcImg.getNative();
		int arc = ceil(sqrt( (srcBImg.getHeight()*srcBImg.getHeight())
				+ (srcBImg.getWidth()*srcBImg.getWidth()) ));
		AffineTransform transform = new AffineTransform();
		transform.translate( (arc-srcBImg.getWidth())/2, (arc-srcBImg.getHeight())/2 );

		for(double cRad=tRad; cRad<=TWO_PI; cRad+=tRad){
			BufferedImage tmpBImg = new BufferedImage(arc, arc, BufferedImage.TYPE_INT_ARGB); //srcBImg.getType()
			transform.rotate( tRad, srcImg.width/2, srcImg.height/2 );
			AffineTransformOp op = new AffineTransformOp( transform
					, AffineTransformOp.TYPE_NEAREST_NEIGHBOR);
			op.filter(srcBImg, tmpBImg);

			// Buffered Image to PImage
			PImage tmpPImg = new PImage(tmpBImg.getWidth(), tmpBImg.getHeight(), PConstants.ARGB);
			tmpBImg.getRGB(0, 0, tmpPImg.width, tmpPImg.height, tmpPImg.pixels, 0, tmpPImg.width);
			tmpPImg.updatePixels();
			resultSet.add(new PImageWithAngle(tmpPImg, (float)cRad));
		}
		return resultSet;
	}

	public static PImage attachToFrame(PImage base, PImage newImg, int x, int y) throws NullPointerException{
		PImage combinedImg = base.get();
		
		// Draw searchImageCenter and original center...
        int borderLenPx = 3;
        for (int _x = 0; _x < newImg.width; _x++) {
            for (int _y = 0; _y < newImg.height; _y++) {
                if (_x < borderLenPx || _x >= newImg.width - borderLenPx || _y < borderLenPx || _y >= newImg.height - borderLenPx) {
                	newImg.set(_x, _y, (new Color(0, 225, 255, 255)).getRGB());
                }
            }
        }
		// TODO: draw vector: searchImageCenter, degrees(resultingImg.angleRad)
        // Attach search image to base image for displaying:
		combinedImg.blend(newImg, 0, 0, combinedImg.width, combinedImg.height, 0, 0, newImg.width , newImg.height, BLEND);
		
		// Attach found image to the base image in found location and angle:
		combinedImg.blend(newImg, 0, 0, combinedImg.width, combinedImg.height, x, y, newImg.width , newImg.height, BLEND);
		return combinedImg;
	}

	public Location execute() {
		initializeSwarm();

		for(int i=0; i<SWARM_SIZE; i++) {
			pBest[i] = fitnessValueList[i];
			pBestLocation.add(swarm.get(i).getLocation());
		}

		int t = 0;
		double w;
		while(t < MAX_ITERATION) {
			// step 1 - evaluate swarm members
			updateFitnessList();

			// step 2 - update pBest
			for(int i=0; i<SWARM_SIZE; i++) {
				if(fitnessValueList[i] < pBest[i]) {
					pBest[i] = fitnessValueList[i];
					pBestLocation.set(i, swarm.get(i).getLocation());
				}
			}

			// step 3 - update gBest
			int bestParticleIndex = PSOUtility.getMaxValue(fitnessValueList);
			if(t == 0 || fitnessValueList[bestParticleIndex] > gBest) {	//gBest is the biggest value.
				gBest = fitnessValueList[bestParticleIndex];
				gBestLocation = swarm.get(bestParticleIndex).getLocation();
			}

			w = W_UPPERBOUND - (((double) t) / MAX_ITERATION) * (W_UPPERBOUND - W_LOWERBOUND);

			for(int i=0; i<SWARM_SIZE; i++) {
				double r1 = generator.nextDouble();
				double r2 = generator.nextDouble();

				Particle p = swarm.get(i);

				// step 4 - update velocity
				double[] newVel = new double[PROBLEM_DIMENSION];
				newVel[0] = (w * p.getVelocity().getPos()[0]) +
							(r1 * C1) * (pBestLocation.get(i).getLoc()[0] - p.getLocation().getLoc()[0]) +
							(r2 * C2) * (gBestLocation.getLoc()[0] - p.getLocation().getLoc()[0]);
				newVel[1] = (w * p.getVelocity().getPos()[1]) +
							(r1 * C1) * (pBestLocation.get(i).getLoc()[1] - p.getLocation().getLoc()[1]) +
							(r2 * C2) * (gBestLocation.getLoc()[1] - p.getLocation().getLoc()[1]);
				Velocity vel = new Velocity(newVel);
				p.setVelocity(vel);

				// step 5 - update location
				double[] newLoc = new double[PROBLEM_DIMENSION];
				newLoc[0] = p.getLocation().getLoc()[0] + newVel[0];
				newLoc[1] = p.getLocation().getLoc()[1] + newVel[1];
				Location loc = new Location(newLoc);
				p.setLocation(loc);
			}
			t++;
		}
		updateFitnessList();

		// step 3 - update gBest
		int bestParticleIndex = PSOUtility.getMaxValue(fitnessValueList);
		if (fitnessValueList[bestParticleIndex] > gBest) {	//gBest is the biggest value.
			gBest = fitnessValueList[bestParticleIndex];
			gBestLocation = swarm.get(bestParticleIndex).getLocation();
		}
		
		return gBestLocation;
	}

	public void initializeSwarm() {
		Particle p;
		for(int i=0; i<SWARM_SIZE; i++) {
			p = new Particle(problem);

			// randomize location inside a space defined in Problem Set
			double[] loc = new double[PROBLEM_DIMENSION];
			loc[0] = ProblemSet.LOC_X_LOW + generator.nextDouble() * (ProblemSet.LOC_X_HIGH - ProblemSet.LOC_X_LOW);
			loc[1] = ProblemSet.LOC_Y_LOW + generator.nextDouble() * (ProblemSet.LOC_Y_HIGH - ProblemSet.LOC_Y_LOW);
			Location location = new Location(loc);

			// randomize velocity in the range defined in Problem Set
			double[] vel = new double[PROBLEM_DIMENSION];
			vel[0] = ProblemSet.VEL_LOW + generator.nextDouble() * (ProblemSet.VEL_HIGH - ProblemSet.VEL_LOW);
			vel[1] = ProblemSet.VEL_LOW + generator.nextDouble() * (ProblemSet.VEL_HIGH - ProblemSet.VEL_LOW);
			Velocity velocity = new Velocity(vel);

			p.setLocation(location);
			p.setVelocity(velocity);
			swarm.add(p);
		}
	}

	public void updateFitnessList() {
		for(int i=0; i<SWARM_SIZE; i++) {
			++updateCount;
			fitnessValueList[i] = swarm.get(i).getFitnessValue();
		}
	}

	public static void main(String _args[]) {
        System.out.println("Starting...");
		PApplet.main(new String[] { psofortemplatematching.PSOProcess.class.getName() });
	}
}
