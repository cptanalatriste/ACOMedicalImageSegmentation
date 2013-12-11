package pe.edu.pucp.acoseg;

import java.util.Date;

import pe.edu.pucp.acoseg.ant.AntColony;
import pe.edu.pucp.acoseg.ant.Environment;
import pe.edu.pucp.acoseg.exper.TestSuite;
import pe.edu.pucp.acoseg.image.ClusteredPixel;
import pe.edu.pucp.acoseg.image.ImageFileHelper;

public class ACOImageSegmentation {

	private Environment environment;
	private AntColony antColony;

	public ACOImageSegmentation(Environment environment) {
		this.environment = environment;
		this.antColony = new AntColony(environment);
	}

	private ClusteredPixel[] solveProblem() throws Exception {
		this.environment.initializePheromoneMatrix();
		int iteration = 0;
		System.out.println("STARTING ITERATIONS");
		System.out.println("Number of iterations: "
				+ ProblemConfiguration.MAX_ITERATIONS);
		while (iteration < ProblemConfiguration.MAX_ITERATIONS) {
			System.out.println("Current iteration: " + iteration);
			this.antColony.clearAntSolutions();
			this.antColony
					.buildSolutions(ProblemConfiguration.DEPOSITE_PHEROMONE_ONLINE);
			System.out.println("UPDATING PHEROMONE TRAILS");
			if (!ProblemConfiguration.DEPOSITE_PHEROMONE_ONLINE) {
				this.antColony.depositPheromone();
			}
			this.environment.performEvaporation();
			this.antColony.recordBestSolution();
			iteration++;
		}
		System.out.println("EXECUTION FINISHED");
		System.out.println("Best partition quality: "
				+ antColony.getBestPartitionQuality());
		return antColony.getBestPartition();
	}

	/**
	 * @param args
	 */
	public static void main(String[] args) {
		System.out.println("ACO FOR IMAGE SEGMENTATION");
		System.out.println("=============================");

		try {
			String imageFile = ProblemConfiguration.INPUT_DIRECTORY
					+ ProblemConfiguration.IMAGE_FILE;
			System.out.println("Data file: " + imageFile);

			int[][] imageGraph = ImageFileHelper
					.getImageArrayFromFile(imageFile);

			System.out.println("Generating original image from matrix");
			ImageFileHelper.generateImageFromArray(imageGraph,
					ProblemConfiguration.OUTPUT_DIRECTORY
							+ ProblemConfiguration.ORIGINAL_IMAGE_FILE);

			Environment environment = new Environment(imageGraph,
					ProblemConfiguration.NUMBER_OF_CLUSTERS);

			long startTime = System.nanoTime();
			ClusteredPixel[] resultingPartition = null;
			if (ProblemConfiguration.USE_PHEROMONE_FOR_CLUSTERING) {
				ACOImageSegmentation acoImageSegmentation = new ACOImageSegmentation(
						environment);
				System.out.println("Starting computation at: " + new Date());
				resultingPartition = acoImageSegmentation.solveProblem();
			}

			// TODO(cgavidia): There should a method to get the number of
			// clústers automatically. Also, son preprocessing or postprocessing
			// would improve quality.

			// TODO(cgavidia): For now

			long endTime = System.nanoTime();
			System.out.println("Finishing computation at: " + new Date());
			System.out.println("Duration (in seconds): "
					+ ((double) (endTime - startTime) / 1000000000.0));

			System.out.println("Generating segmented image");
			int[][] segmentedImageAsMatrix = generateSegmentedImage(
					resultingPartition, environment);
			ImageFileHelper.generateImageFromArray(segmentedImageAsMatrix,
					ProblemConfiguration.OUTPUT_DIRECTORY
							+ ProblemConfiguration.OUTPUT_IMAGE_FILE);

			System.out.println("Generating images per cluster");
			for (int i = 0; i < ProblemConfiguration.NUMBER_OF_CLUSTERS; i++) {
				// TODO(cgavidia): For now
				/*ImageFileHelper.generateImageFromArray(null,
						ProblemConfiguration.OUTPUT_DIRECTORY + i + "_"
								+ ProblemConfiguration.CLUSTER_IMAGE_FILE);*/
			}

			// TODO(cgavidia): For now
			//new TestSuite().executeReport();

		} catch (Exception e) {
			e.printStackTrace();
		}

	}

	private static int[][] generateSegmentedImage(
			ClusteredPixel[] resultingPartition, Environment environment) {
		int[][] resultMatrix = new int[environment.getImageGraph().length][environment
				.getImageGraph()[0].length];
		for (ClusteredPixel clusteredPixel : resultingPartition) {
			resultMatrix[clusteredPixel.getxCoordinate()][clusteredPixel
					.getyCoordinate()] = (int) ((clusteredPixel.getCluster() + 1)
					/ environment.getNumberOfClusters() * ProblemConfiguration.GRAYSCALE_MAX_RANGE);
		}
		return resultMatrix;
	}

}
