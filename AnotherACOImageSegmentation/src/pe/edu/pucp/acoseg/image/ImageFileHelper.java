package pe.edu.pucp.acoseg.image;

import java.awt.image.BufferedImage;
import java.awt.image.Raster;
import java.awt.image.WritableRaster;
import java.io.File;
import java.io.IOException;
import java.util.Arrays;

import javax.imageio.ImageIO;

import pe.edu.pucp.acoseg.ACOImageSegmentation;

public class ImageFileHelper {

	public static int[][] getImageArrayFromFile(String imageFile)
			throws IOException {

		BufferedImage image = ImageIO.read(new File(imageFile));
		Raster imageRaster = image.getData();

		int[][] imageAsArray;
		int[] pixel = new int[1];
		int[] buffer = new int[1];

		imageAsArray = new int[imageRaster.getWidth()][imageRaster.getHeight()];

		for (int i = 0; i < imageRaster.getWidth(); i++)
			for (int j = 0; j < imageRaster.getHeight(); j++) {
				pixel = imageRaster.getPixel(i, j, buffer);
				imageAsArray[i][j] = pixel[0];
			}
		return imageAsArray;
	}

	public static ImageSegment getSegmentWithoutBackground(int[][] originalImage) {
		int grayScaleDelta = 10;
		int yLeftBoundary = -1;
		int yRightBoundary = -1;
		int xTopBoundary = -1;
		int xBottomBoundary = -1;

		int previousGreyScale = originalImage[originalImage.length / 2][0];
		for (int i = 1; i < originalImage[0].length; i++) {
			int currentGreyScale = originalImage[originalImage.length / 2][i];
			if (Math.abs(currentGreyScale - previousGreyScale) > grayScaleDelta
					&& yLeftBoundary < 0) {
				yLeftBoundary = i;
			} else if (Math.abs(currentGreyScale - previousGreyScale) > grayScaleDelta
					&& yLeftBoundary > 0) {
				yRightBoundary = i;
			}
		}

		previousGreyScale = originalImage[0][originalImage[0].length / 2];
		for (int i = 1; i < originalImage.length; i++) {
			int currentGreyScale = originalImage[i][originalImage[0].length / 2];
			if (Math.abs(currentGreyScale - previousGreyScale) > grayScaleDelta
					&& xTopBoundary < 0) {
				xTopBoundary = i;
			} else if (Math.abs(currentGreyScale - previousGreyScale) > grayScaleDelta
					&& xTopBoundary > 0) {
				xBottomBoundary = i;
			}
		}
		return new ImageSegment(yLeftBoundary, yRightBoundary, xTopBoundary,
				xBottomBoundary);
	}

	public static int[][] cropImage(ImageSegment imageSegment,
			int[][] originalImage) {

		int numberOfRows = imageSegment.getxBottomBoundary()
				- imageSegment.getxTopBoundary() + 1;
		int[][] croppedImage = new int[numberOfRows][];
		for (int i = 0, j = imageSegment.getxTopBoundary(); i < numberOfRows; i++, j++) {
			croppedImage[i] = Arrays.copyOfRange(originalImage[j],
					imageSegment.getyLeftBoundary(),
					imageSegment.getyRightBoundary());
		}
		return croppedImage;
	}

	public static void generateImageFromArray(
			int[][] normalizedPheromoneMatrix, String outputImageFile)
			throws IOException {
		System.out.println(ACOImageSegmentation.getComputingTimeAsString()
				+ "Generating output image");
		BufferedImage outputImage = new BufferedImage(
				normalizedPheromoneMatrix.length,
				normalizedPheromoneMatrix[0].length,
				BufferedImage.TYPE_BYTE_GRAY);
		WritableRaster raster = outputImage.getRaster();
		for (int x = 0; x < normalizedPheromoneMatrix.length; x++) {
			for (int y = 0; y < normalizedPheromoneMatrix[x].length; y++) {
				raster.setSample(x, y, 0, normalizedPheromoneMatrix[x][y]);
			}
		}
		File imageFile = new File(outputImageFile);
		ImageIO.write(outputImage, "bmp", imageFile);
		System.out.println(ACOImageSegmentation.getComputingTimeAsString()

		+ "Resulting image stored in: " + outputImageFile);
	}
}
