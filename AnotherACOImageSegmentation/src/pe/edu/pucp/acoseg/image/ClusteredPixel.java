package pe.edu.pucp.acoseg.image;

import java.util.ArrayList;
import java.util.List;

public class ClusteredPixel {

	private int xCoordinate = -1;
	private int yCoordinate = -1;
	private int greyScaleValue = -1;
	private int cluster = -1;

	public ClusteredPixel(int xCoordinate, int yCoordinate, int[][] imageGraph,
			int cluster) {
		super();
		this.xCoordinate = xCoordinate;
		this.yCoordinate = yCoordinate;
		this.cluster = cluster;
		this.greyScaleValue = imageGraph[xCoordinate][yCoordinate];
	}

	public int getxCoordinate() {
		return xCoordinate;
	}

	public void setxCoordinate(int xCoordinate) {
		this.xCoordinate = xCoordinate;
	}

	public int getyCoordinate() {
		return yCoordinate;
	}

	public void setyCoordinate(int yCoordinate) {
		this.yCoordinate = yCoordinate;
	}

	public int getGreyScaleValue() {
		return greyScaleValue;
	}

	public void setGreyScaleValue(int greyScaleValue) {
		this.greyScaleValue = greyScaleValue;
	}

	public int getCluster() {
		return cluster;
	}

	public void setCluster(int cluster) {
		this.cluster = cluster;
	}

	public String toString() {
		return "Pixel (" + xCoordinate + ", " + yCoordinate + ") to Cluster: "
				+ cluster;
	}

	// TODO(cgavidia): There must be a more elegant way to do this
	public List<ClusteredPixel> getNeighbourhood(int[][] imageGraph) {
		ArrayList<ClusteredPixel> neighbours = new ArrayList<ClusteredPixel>();
		if (yCoordinate - 1 >= 0) {
			neighbours.add(new ClusteredPixel(xCoordinate, yCoordinate - 1,
					imageGraph, -1));
		}

		if (yCoordinate + 1 < imageGraph[0].length) {
			neighbours.add(new ClusteredPixel(xCoordinate, yCoordinate + 1,
					imageGraph, -1));
		}

		if (xCoordinate - 1 >= 0) {
			neighbours.add(new ClusteredPixel(xCoordinate - 1, yCoordinate,
					imageGraph, -1));
		}

		if (xCoordinate + 1 < imageGraph.length) {
			neighbours.add(new ClusteredPixel(xCoordinate + 1, yCoordinate,
					imageGraph, -1));
		}

		if (xCoordinate - 1 >= 0 && yCoordinate - 1 >= 0) {
			neighbours.add(new ClusteredPixel(xCoordinate - 1, yCoordinate - 1,
					imageGraph, -1));
		}

		if (xCoordinate + 1 < imageGraph.length && yCoordinate - 1 >= 0) {
			neighbours.add(new ClusteredPixel(xCoordinate + 1, yCoordinate - 1,
					imageGraph, -1));
		}

		if (xCoordinate - 1 >= 0 && yCoordinate + 1 < imageGraph[0].length) {
			neighbours.add(new ClusteredPixel(xCoordinate - 1, yCoordinate + 1,
					imageGraph, -1));
		}

		if (xCoordinate + 1 < imageGraph.length
				&& yCoordinate + 1 < imageGraph[0].length) {
			neighbours.add(new ClusteredPixel(xCoordinate + 1, yCoordinate + 1,
					imageGraph, -1));
		}

		return neighbours;
	}

	// TODO(cgavidia): Again, this surely can be improved.
	public boolean isNeighbour(ClusteredPixel currentPixel, int[][] imageGraph) {
		for (ClusteredPixel neighbour : getNeighbourhood(imageGraph)) {
			if (neighbour.getxCoordinate() == currentPixel.getxCoordinate()
					&& neighbour.getyCoordinate() == currentPixel
							.getyCoordinate()) {
				return true;
			}
		}
		return false;
	}
}
