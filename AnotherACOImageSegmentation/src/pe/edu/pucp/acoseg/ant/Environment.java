package pe.edu.pucp.acoseg.ant;

import pe.edu.pucp.acoseg.ProblemConfiguration;

public class Environment {

	private int numberOfClusters;
	private int numberOfPixels;

	private int[][] imageGraph;
	private double pheromoneTrails[][] = null;

	public Environment(int[][] imageGraph, int numberOfClusters) {
		super();
		this.numberOfClusters = numberOfClusters;
		System.out.println("Number of Clusters: " + numberOfClusters);
		this.imageGraph = imageGraph;
		this.numberOfPixels = imageGraph.length * imageGraph[0].length;
		System.out.println("Number of Píxels: " + numberOfPixels);
		this.pheromoneTrails = new double[numberOfPixels][numberOfClusters];
	}

	public int[][] getImageGraph() {
		return imageGraph;
	}

	public double[][] getPheromoneTrails() {
		return pheromoneTrails;
	}

	public int getNumberOfClusters() {
		return numberOfClusters;
	}

	public int getNumberOfPixels() {
		return numberOfPixels;
	}

	public void initializePheromoneMatrix() {
		System.out.println("INITIALIZING PHEROMONE MATRIX");
		double initialPheromoneValue = ProblemConfiguration.INITIAL_PHEROMONE_VALUE;
		if (ProblemConfiguration.MMAS_PHEROMONE_UPDATE) {
			initialPheromoneValue = ProblemConfiguration.MAXIMUM_PHEROMONE_VALUE;
		}

		System.out.println("Initial pheromone value: " + initialPheromoneValue);
		for (int i = 0; i < numberOfPixels; i++) {
			for (int j = 0; j < numberOfClusters; j++) {
				pheromoneTrails[i][j] = initialPheromoneValue;
			}
		}
	}

	public void performEvaporation() {
		System.out.println("Performing evaporation on all edges");
		System.out.println("Evaporation ratio: "
				+ ProblemConfiguration.EVAPORATION);
		for (int i = 0; i < numberOfPixels; i++) {
			for (int j = 0; j < numberOfClusters; j++) {
				double newValue = pheromoneTrails[i][j]
						* ProblemConfiguration.EVAPORATION;
				if (ProblemConfiguration.MMAS_PHEROMONE_UPDATE
						&& newValue < ProblemConfiguration.MINIMUM_PHEROMONE_VALUE) {
					newValue = ProblemConfiguration.MINIMUM_PHEROMONE_VALUE;
				}
				pheromoneTrails[i][j] = newValue;
			}
		}
	}
}
