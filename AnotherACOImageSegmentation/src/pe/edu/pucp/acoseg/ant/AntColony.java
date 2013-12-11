package pe.edu.pucp.acoseg.ant;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import pe.edu.pucp.acoseg.ProblemConfiguration;
import pe.edu.pucp.acoseg.image.ClusteredPixel;

public class AntColony {

	private List<Ant> antColony;
	private int numberOfAnts;
	private Environment environment;

	private ClusteredPixel bestPartition[];
	public double bestPartitionQuality = -1.0;

	public AntColony(Environment environment) {
		this.environment = environment;
		// Ant Ant per every pixel
		this.numberOfAnts = ProblemConfiguration.NUMBER_OF_ANTS;
		System.out.println("Number of Ants in Colony: " + numberOfAnts);
		this.antColony = new ArrayList<Ant>(numberOfAnts);
		for (int j = 0; j < numberOfAnts; j++) {
			antColony.add(new Ant(environment.getNumberOfPixels(), environment
					.getNumberOfClusters()));
		}
	}

	public void buildSolutions(boolean depositPheromone) throws Exception {
		System.out.println("BUILDING ANT SOLUTIONS");

		// TODO(cgavidia): We need to pick ants randomly
		if (ProblemConfiguration.RANDOMIZE_BEFORE_BUILD) {
			Collections.shuffle(antColony);
		}

		int antCounter = 0;
		for (Ant ant : antColony) {
			System.out.println("Ant " + antCounter + " is building a partition ...");
			int pixelCounter = environment.getNumberOfPixels();
			for (int i = 0; i < environment.getImageGraph().length; i++) {
				for (int j = 0; j < environment.getImageGraph()[0].length; j++) {
					ClusteredPixel nextPixel = ant.selectedPixelAssignment(i,
							j, environment.getPheromoneTrails(),
							environment.getImageGraph());
					//System.out.println("Ant " + antCounter + ": " + nextPixel
					//		+ ". Left " + (pixelCounter - 1) + " to end.");
					if (nextPixel == null) {
						throw new Exception(
								"No pixel was selected, for ant with path: "
										+ ant.pathAsString());
					}
					ant.addAsignmentToSolution(nextPixel);
					pixelCounter--;
				}
			}
			if (depositPheromone) {
				depositPheromoneInAntPath(ant);
			}
			antCounter++;
			// TODO(cgavidia): Local search is also omitted. No recording of
			// best solutions either.
		}
	}

	public void clearAntSolutions() {
		System.out.println("CLEARING ANT SOLUTIONS");
		for (Ant ant : antColony) {
			ant.clear();
			ant.setCurrentIndex(0);
		}
	}

	public void depositPheromone() throws Exception {
		System.out.println("Depositing pheromone");

		if (ProblemConfiguration.ONLY_BEST_CAN_UPDATE) {
			System.out.println("Depositing pheromone on Best Ant trail.");
			Ant bestAnt = getBestAnt();
			depositPheromoneInAntPath(bestAnt);
		} else {
			for (Ant ant : antColony) {
				depositPheromoneInAntPath(ant);
			}
		}
	}

	private void depositPheromoneInAntPath(Ant ant) throws Exception {

		double contribution = 1 / ant.getPartitionQuality(environment
				.getImageGraph());

		for (int i = 0; i < environment.getNumberOfPixels(); i++) {
			ClusteredPixel imagePixel = ant.getPartition()[i];
			double newValue = environment.getPheromoneTrails()[imagePixel
					.getxCoordinate()
					* environment.getImageGraph()[0].length
					+ imagePixel.getyCoordinate()][imagePixel.getCluster()]
					* ProblemConfiguration.EXTRA_WEIGHT + contribution;
			if (ProblemConfiguration.MMAS_PHEROMONE_UPDATE
					&& newValue < ProblemConfiguration.MINIMUM_PHEROMONE_VALUE) {
				newValue = ProblemConfiguration.MINIMUM_PHEROMONE_VALUE;
			} else if (ProblemConfiguration.MMAS_PHEROMONE_UPDATE
					&& newValue > ProblemConfiguration.MAXIMUM_PHEROMONE_VALUE) {
				newValue = ProblemConfiguration.MAXIMUM_PHEROMONE_VALUE;
			}

			if (Double.isNaN(newValue)) {
				throw new Exception(
						"Invalid feromone final value. Original value: "
								+ environment.getPheromoneTrails()[imagePixel
										.getxCoordinate()][imagePixel
										.getyCoordinate()] + ". Contribution: "
								+ contribution);
			}
			environment.getPheromoneTrails()[imagePixel.getxCoordinate()
					* environment.getImageGraph()[0].length
					+ imagePixel.getyCoordinate()][imagePixel.getCluster()] = newValue;
		}
	}

	private Ant getBestAnt() {
		Ant bestAnt = antColony.get(0);
		for (Ant ant : antColony) {
			if (ant.getPartitionQuality(environment.getImageGraph()) < bestAnt
					.getPartitionQuality(environment.getImageGraph())) {
				bestAnt = ant;
			}
		}
		return bestAnt;

	}

	public void recordBestSolution() {
		System.out.println("GETTING BEST SOLUTION FOUND");
		Ant bestAnt = getBestAnt();

		// TODO(cgavidia): Again, some CPU cicles can be saved here.
		if (bestPartition == null
				|| bestPartitionQuality > bestAnt
						.getPartitionQuality(environment.getImageGraph())) {
			bestPartition = bestAnt.getPartition().clone();
			bestPartitionQuality = bestAnt.getPartitionQuality(environment
					.getImageGraph());
		}
		System.out.println("Best solution so far > Quality: "
				+ bestPartitionQuality);
	}

	public double getBestPartitionQuality() {
		return bestPartitionQuality;
	}

	public ClusteredPixel[] getBestPartition() {
		return bestPartition;
	}

	
	
}
