package pe.edu.pucp.acoseg.exper;

import java.util.ArrayList;
import java.util.List;

import pe.edu.pucp.acoseg.ProblemConfiguration;

public class TestSuite {

	private List<ImageComparator> comparisonList = new ArrayList<ImageComparator>();

	public TestSuite() {
		comparisonList.add(new ImageComparator("CSF",
				ProblemConfiguration.INPUT_DIRECTORY
						+ "csf_21130transverse1_64.gif",
				ProblemConfiguration.GRAYSCALE_POSITIVE_THRESHOLD));
		comparisonList.add(new ImageComparator("Grey Matter",
				ProblemConfiguration.INPUT_DIRECTORY
						+ "grey_20342transverse1_64.gif",
				ProblemConfiguration.GRAYSCALE_POSITIVE_THRESHOLD));
		comparisonList.add(new ImageComparator("White Matter",
				ProblemConfiguration.INPUT_DIRECTORY
						+ "white_20358transverse1_64.gif",
				ProblemConfiguration.GRAYSCALE_POSITIVE_THRESHOLD));
	}

	public void executeReport() throws Exception {

		// TODO(cgavidia): It would be good to evaluate the behaviuor with
		// noise.
		System.out.println("\n\nEXPERIMENT EXECUTION REPORT");
		System.out.println("===============================");

		for (ImageComparator comparator : comparisonList) {
			double maximumBDP = 0;
			String maximumBDPClusterFile = "";
			for (int i = 0; i < ProblemConfiguration.NUMBER_OF_CLUSTERS; i++) {
				String currentFile = ProblemConfiguration.OUTPUT_DIRECTORY + i
						+ "_" + ProblemConfiguration.CLUSTER_IMAGE_FILE;
				comparator
						.setImageToValidateFile(ProblemConfiguration.OUTPUT_DIRECTORY
								+ i
								+ "_"
								+ ProblemConfiguration.CLUSTER_IMAGE_FILE);
				comparator.executeComparison();
				if (comparator.getBuildingDetectionPercentage() > maximumBDP) {
					maximumBDP = comparator.getBuildingDetectionPercentage();
					maximumBDPClusterFile = currentFile;
				}

			}
			comparator.setImageToValidateFile(maximumBDPClusterFile);
		}

		System.out.println(ProblemConfiguration.currentConfigurationAsString());

		for (ImageComparator comparator : comparisonList) {
			comparator.executeComparison();
			System.out.println(comparator.resultAsString());
		}

	}

	public static void main(String... args) throws Exception {

		System.out.println("\n\nEXPERIMENT EXECUTION REPORT");
		System.out.println("===============================");

		TestSuite testSuite = new TestSuite();

		for (ImageComparator comparator : testSuite.comparisonList) {
			double maximumBDP = 0;
			String maximumBDPClusterFile = "";
			for (int i = 0; i < ProblemConfiguration.NUMBER_OF_CLUSTERS; i++) {
				String currentFile = ProblemConfiguration.OUTPUT_DIRECTORY + i
						+ "_" + ProblemConfiguration.CLUSTER_IMAGE_FILE;
				comparator
						.setImageToValidateFile(ProblemConfiguration.OUTPUT_DIRECTORY
								+ i
								+ "_"
								+ ProblemConfiguration.CLUSTER_IMAGE_FILE);
				comparator.executeComparison();
				if (comparator.getBuildingDetectionPercentage() > maximumBDP) {
					maximumBDP = comparator.getBuildingDetectionPercentage();
					maximumBDPClusterFile = currentFile;
				}

			}
			comparator.setImageToValidateFile(maximumBDPClusterFile);
		}

		System.out.println(ProblemConfiguration.currentConfigurationAsString());

		double maxMeanJCI = -1;
		double maxThreshold = -1;

		for (int i = 5; i <= ProblemConfiguration.GRAYSCALE_MAX_RANGE; i++) {
			for (ImageComparator comparator : testSuite.comparisonList) {
				comparator.setGreyScalePositiveThreshold(i);
				comparator.executeComparison();
				if (comparator.getJaccardSimilarityIndex() > maxMeanJCI) {
					maxMeanJCI = comparator.getJaccardSimilarityIndex();
					maxThreshold = i;
				}
			}
		}

		System.out.println("maxMeanJCI=" + maxMeanJCI + ", maxThreshold= "
				+ maxThreshold);

	}
}
