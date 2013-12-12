package pe.edu.pucp.acoseg.ant;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

import pe.edu.pucp.acoseg.ACOImageSegmentation;
import pe.edu.pucp.acoseg.ProblemConfiguration;
import pe.edu.pucp.acoseg.image.ClusteredPixel;
import pe.edu.pucp.acoseg.image.PosibleAssignment;

public class Ant {

	private int currentIndex = 0;
	private int numberOfClusters;
	private ClusteredPixel partition[];

	// TODO(cgavidia):Visited matrix was removed because memory concerns

	public Ant(int numberOfPixels, int numberOfClusters) {
		this.partition = new ClusteredPixel[numberOfPixels];
		this.numberOfClusters = numberOfClusters;
	}

	public void setCurrentIndex(int currentIndex) {
		this.currentIndex = currentIndex;
	}

	public void addAsignmentToSolution(ClusteredPixel visitedPixel) {
		partition[currentIndex] = visitedPixel;
		currentIndex++;
	}

	public void clear() {
		for (int i = 0; i < partition.length; i++) {
			partition[i] = null;
		}
	}

	// TODO(cgavidia): Maybe we can get rid of this
	public boolean isPixelVisited(ClusteredPixel imagePixel) {
		// TODO(cgavidia): Pass this to equals
		if (!ProblemConfiguration.ALLOW_VISITED_PIXELS) {
			for (ClusteredPixel visitedPixel : partition) {
				if (visitedPixel != null
						&& visitedPixel.getxCoordinate() == imagePixel
								.getxCoordinate()
						&& visitedPixel.getyCoordinate() == imagePixel
								.getyCoordinate()
						&& visitedPixel.getGreyScaleValue() == imagePixel
								.getGreyScaleValue()) {
					return true;
				}
			}
		}
		return false;
	}

	public int getCurrentIndex() {
		return currentIndex;
	}

	public ClusteredPixel[] getPartition() {
		return partition;
	}

	public ClusteredPixel selectedPixelAssignment(int xCoordinate,
			int yCoordinate, double[][] pheromoneTrails, int[][] imageGraph)
			throws Exception {
		ClusteredPixel nextPixel = null;
		Random random = new Random();
		double randomValue = random.nextDouble();
		List<PosibleAssignment> probabilities = getProbabilities(xCoordinate,
				yCoordinate, pheromoneTrails, imageGraph);

		// TODO(cgavidia): Calibrate this parameter
		if (randomValue < ProblemConfiguration.BEST_CHOICE_PROBABILITY) {
			return getMaximumValueAssignment(probabilities);
		} else {
			double anotherRandomValue = random.nextDouble();
			double total = 0;
			for (PosibleAssignment posiblePixel : probabilities) {
				if (Double.isNaN(posiblePixel.getProbability())) {
					throw new Exception(
							"Pixel doesn't have an associated probability: "
									+ posiblePixel);
				}
				total = total + posiblePixel.getProbability();
				if (total >= anotherRandomValue) {
					return posiblePixel.getImagePixel();
				}

			}
		}

		// TODO(cgavidia): Workarround. Not the best approach
		nextPixel = probabilities.get(random.nextInt(probabilities.size()))
				.getImagePixel();
		return nextPixel;
	}

	public ClusteredPixel getMaximumValueAssignment(
			List<PosibleAssignment> probabilities) {
		ClusteredPixel nextPixel = null;
		double currentMaximumPheromoneTimesHeuristic = -1;
		for (PosibleAssignment posiblePixel : probabilities) {
			if (!isPixelVisited(posiblePixel.getImagePixel())
					&& posiblePixel.getHeuristicTimesPheromone() > currentMaximumPheromoneTimesHeuristic) {
				currentMaximumPheromoneTimesHeuristic = posiblePixel
						.getHeuristicTimesPheromone();
				nextPixel = posiblePixel.getImagePixel();
			}
		}
		return nextPixel;
	}

	public List<PosibleAssignment> getProbabilities(int xCoordinate,
			int yCoordinate, double[][] pheromoneTrails, int[][] imageGraph) {

		List<PosibleAssignment> pixelsWithProbabilities = new ArrayList<PosibleAssignment>();
		double denominator = ProblemConfiguration.DELTA;

		for (int cluster = 0; cluster < numberOfClusters; cluster++) {
			ClusteredPixel currentPixel = new ClusteredPixel(xCoordinate,
					yCoordinate, imageGraph, cluster);
			// TODO(cgavidia): This will get real soon
			double heuristicValue = getHeuristicValue(currentPixel, imageGraph)
					+ ProblemConfiguration.DELTA;
			double pheromoneTrailValue = pheromoneTrails[xCoordinate
					* imageGraph[0].length + yCoordinate][cluster]
					+ ProblemConfiguration.DELTA;
			double heuristicTimesPheromone = Math.pow(heuristicValue,
					ProblemConfiguration.HEURISTIC_IMPORTANCE)
					* Math.pow(pheromoneTrailValue,
							ProblemConfiguration.PHEROMONE_IMPORTANCE);
			pixelsWithProbabilities.add(new PosibleAssignment(currentPixel,
					heuristicValue, pheromoneTrailValue,
					heuristicTimesPheromone, 0.0));
			denominator = denominator + heuristicTimesPheromone;
		}

		for (PosibleAssignment posiblePixel : pixelsWithProbabilities) {
			double heuristicTimesPheromone = posiblePixel
					.getHeuristicTimesPheromone();
			// Now we're dividing by the total sum
			posiblePixel.setProbability(heuristicTimesPheromone / denominator);
		}

		return pixelsWithProbabilities;
	}

	// TODO(cgavidia): Maybe a Cluster Class would be more appropiate
	public double getHeuristicValue(ClusteredPixel currentPixel,
			int[][] imageGraph) {
		double euclideanDistance = Math.abs(currentPixel.getGreyScaleValue()
				- getClusterMeanValue(currentPixel.getCluster()));
		double contiguityMeasure = getContiguityMeasure(currentPixel,
				imageGraph);
		return euclideanDistance
				+ ProblemConfiguration.CONTIGUITY_MEASURE_PARAM
				* contiguityMeasure;
	}

	public double getPartitionQuality(int[][] imageGraph) {

		double partitionQuality = 0.0;
		// TODO(cgavidia): This MUST be optimized. The Cluster clas would
		// be a great help
		for (int cluster = 0; cluster < numberOfClusters; cluster++) {
			int clusterCounter = 0;
			double clusterQuality = 0;
			for (int pixel = 0; pixel < currentIndex; pixel++) {
				if (partition[pixel].getCluster() == cluster) {
					clusterCounter++;
					clusterQuality += getHeuristicValue(partition[pixel],
							imageGraph);
				}
			}
			partitionQuality += clusterCounter != 0 ? clusterQuality
					/ clusterCounter : 0;
		}
		return partitionQuality;
	}

	public double getContiguityMeasure(ClusteredPixel currentPixel,
			int[][] imageGraph) {
		// TODO(cgavidia): Contiguity measure taken from: A contiguity-enhanced
		// k-means clustering algorithm forunsupervised multispectral image
		// segmentation
		double neighboursWithSameClass = 0;
		for (int i = 0; i < currentIndex; i++) {
			ClusteredPixel clusteredPixel = partition[i];
			if (clusteredPixel.getCluster() == currentPixel.getCluster()
					&& clusteredPixel.isNeighbour(currentPixel, imageGraph)) {
				neighboursWithSameClass++;
			}
		}
		return currentIndex != 0 ? neighboursWithSameClass / currentIndex : 0;
	}

	public double getClusterMeanValue(int cluster) {
		// TODO(cgavidia): This should me stored to save some CPU time
		double greyScaleSum = 0.0;
		double clusterCounter = 0;
		for (int i = 0; i < currentIndex; i++) {
			ClusteredPixel clusteredPixel = partition[i];
			if (clusteredPixel.getCluster() == cluster) {
				greyScaleSum = greyScaleSum
						+ clusteredPixel.getGreyScaleValue();
				clusterCounter = clusterCounter + 1;
			}
		}
		return clusterCounter != 0 ? greyScaleSum / clusterCounter : 0;
	}

	// TODO(cgavidia): This can also dissapear
	public double getMeanGrayScaleValue() {
		double grayScaleSum = 0.0;
		for (int i = 0; i < currentIndex; i++) {
			ClusteredPixel currentPixel = partition[i];
			grayScaleSum = grayScaleSum + currentPixel.getGreyScaleValue();
		}

		return grayScaleSum / (currentIndex);
	}

	public double getStandarDeviation() {
		double deviationSum = 0.0;
		double mean = getMeanGrayScaleValue();
		for (int i = 0; i < currentIndex; i++) {
			ClusteredPixel currentPixel = partition[i];
			deviationSum = deviationSum
					+ Math.pow(currentPixel.getGreyScaleValue() - mean, 2);
		}
		return Math.sqrt(deviationSum / currentIndex);
	}

	public String pathAsString() {
		String result = "";
		for (ClusteredPixel pixel : partition) {
			if (pixel != null) {
				result = result + "(" + pixel.getxCoordinate() + ", "
						+ pixel.getyCoordinate() + ")  ";
			}
		}
		return result;
	}

}
