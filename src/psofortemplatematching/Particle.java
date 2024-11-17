package psofortemplatematching;

import processing.core.PImage;

public class Particle {
	private ProblemSet problem;
	private double fitnessValue;
	private Velocity velocity;
	private Location location;

	public Particle(ProblemSet problem) {
		super();
		this.problem = problem;
	}

	public Particle(PImage img, double fitnessValue, Velocity velocity,
			Location location, ProblemSet problem) {
		super();
		this.problem = problem;
		this.fitnessValue = fitnessValue;
		this.velocity = velocity;
		this.location = location;
	}

	public Velocity getVelocity() {
		return velocity;
	}

	public void setVelocity(Velocity velocity) {
		this.velocity = velocity;
	}

	public Location getLocation() {
		return location;
	}

	public void setLocation(Location location) {
		this.location = location;
	}

	public double getFitnessValue() {
		fitnessValue = problem.evaluate(location);
		return fitnessValue;
	}
}
