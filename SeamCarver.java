/* *****************************************************************************
 *  Name: Nicholas Yap
 *  Date: 24/07/2021
 *  Description: Seam Carving. Score 100/100
 **************************************************************************** */

import edu.princeton.cs.algs4.Picture;

import java.awt.Color;

public class SeamCarver {
    private static final int BORDER_ENERGY = 1000;
    private Picture picture;
    private int h, w;
    private double[][] energy;
    private boolean transposed = false;


    // create a seam carver object based on the given picture
    public SeamCarver(Picture picture) {
        checkNull(picture);
        this.picture = new Picture(picture);
        this.h = picture.height();
        this.w = picture.width();
        energy = new double[w][h];
        for (int y = 0; y < h; y++)
            for (int x = 0; x < w; x++) {
                energy[x][y] = energy(x, y);
            }
    }

    // current picture
    public Picture picture() {
        return new Picture(picture);
    }

    // width of current picture
    public int width() {
        return w;
    }

    // height of current picture
    public int height() {
        return h;
    }

    // energy of pixel at column x and row y
    public double energy(int x, int y) {
        if (x < 0 || x > w - 1 || y < 0 || y > h - 1)
            throw new IllegalArgumentException();
        if (x == 0 || x == w - 1 || y == 0 || y == h - 1) return BORDER_ENERGY;
        return Math.sqrt(xGradientSq(x, y) + yGradientSq(x, y));
    }

    // sequence of indices for horizontal seam
    public int[] findHorizontalSeam() {
        transpose();
        // Note: Even though we transpose and found a vertical seam, if we transpose back,
        // this SAME seam can be interpreted as a horizontal seam. DRAW IT OUT TO PROVE.
        int[] seam = findSeam();
        transpose();
        return seam;
    }

    // sequence of indices for vertical seam
    public int[] findVerticalSeam() {
        return findSeam();
    }

    private int[] findSeam() {
        int[] seam = new int[h];
        double[][] distTo = new double[w][h];
        int[][] parent = new int[w][h]; // parent is 1 level above, so y is inferred.

        // Initialize distTo[][]
        for (int y = 0; y < h; y++) {
            for (int x = 0; x < w; x++) {
                if (y == 0) distTo[x][y] = 0;
                else distTo[x][y] = Double.POSITIVE_INFINITY;
            }
        }

        // If finding vertical seam, go through matrix in row-major order which is also topological order
        for (int y = 0; y < h - 1; y++) { // Exclude last row (no pixels below)
            for (int x = 0; x < w; x++) {
                for (int k = x - 1; k <= x + 1; k++) {
                    if (k >= 0 && k < w) {
                        // Relax
                        if (distTo[x][y] + energy[x][y] < distTo[k][y + 1]) {
                            distTo[k][y + 1] = distTo[x][y] + energy[x][y];
                            parent[k][y + 1] = x;
                        }
                    }
                }
            }
        }

        // Look at last row of distTo to identify shortest path.
        double minDist = Float.MAX_VALUE;
        for (int x = 0; x < w; x++) {
            if (distTo[x][h - 1] < minDist) {
                minDist = distTo[x][h - 1];
                seam[h - 1] = x;
            }
        }

        // Trace path
        int xIndex = seam[h - 1];
        for (int y = h - 1; y > 0; y--) {
            seam[y - 1] = parent[xIndex][y];
            xIndex = parent[xIndex][y];
        }
        return seam;
    }

    // remove horizontal seam from current picture
    public void removeHorizontalSeam(int[] seam) {
        checkNull(seam);
        checkSeam(seam, w, h - 1);
        if (h <= 1) throw new IllegalArgumentException();
        transpose();
        // Note: Horizontal seam can be interpreted as a vertical seam after transposing.
        removeSeam(seam);
        transpose();
    }

    // remove vertical seam from current picture
    public void removeVerticalSeam(int[] seam) {
        checkNull(seam);
        checkSeam(seam, h, w - 1);
        if (w <= 1) throw new IllegalArgumentException();
        removeSeam(seam);
    }

    private void removeSeam(int[] seam) {
        double[][] newEnergy = new double[w - 1][h];
        Picture newPic = new Picture(w - 1, h);

        for (int y = 0; y < h; y++) {
            int count = 0;
            for (int x = 0; x < w; x++) {
                if (x == seam[y])
                    continue;
                newEnergy[count][y] = energy[x][y];
                newPic.setRGB(count, y, picture.getRGB(x, y));
                count++;
            }
        }

        picture = newPic;
        w--;

        // Recalculate energy of surrounding pixels based on newPic
        for (int y = 0; y < h; y++) {
            // if seam[y] is 0, don't calculate for -1.
            if (seam[y] != 0) {
                newEnergy[seam[y] - 1][y] = energy(seam[y] - 1, y);
            }
            // if seam[y] is last index, no need to calculate for a right pixel.
            if (seam[y] != w) {
                newEnergy[seam[y]][y] = energy(seam[y],
                                               y); // Energy based on transposed pic??. YES.
            }
        }
        energy = newEnergy;
    }

    private void checkSeam(int[] seam, int length, int maxVal) {
        if (seam.length != length) throw new IllegalArgumentException();
        for (int i = 0; i < seam.length; i++) {
            if (seam[i] < 0 || seam[i] > maxVal) throw new IllegalArgumentException();
            if (i != 0) {
                int diff = Math.abs(seam[i] - seam[i - 1]);
                if (diff != 1 && diff != 0) throw new IllegalArgumentException();
            }
        }
    }

    private void transpose() {
        double[][] newEnergy = new double[h][w];
        for (int y = 0; y < h; y++) {
            for (int x = 0; x < w; x++) {
                newEnergy[y][x] = energy[x][y];
            }
        }
        // swap picture
        Picture newPic = new Picture(h, w);
        for (int y = 0; y < h; y++) {
            for (int x = 0; x < w; x++) {
                newPic.setRGB(y, x, picture.getRGB(x, y));
            }
        }
        picture = newPic;
        energy = newEnergy;
        h = picture.height();
        w = picture.width();
        transposed = !transposed;
    }

    private double xGradientSq(int x, int y) {
        Color right = picture.get(x + 1, y);
        Color left = picture.get(x - 1, y);
        int red = right.getRed() - left.getRed();
        int blue = right.getBlue() - left.getBlue();
        int green = right.getGreen() - left.getGreen();
        return red * red + blue * blue + green * green;
    }

    private double yGradientSq(int x, int y) {
        Color bottom = picture.get(x, y + 1);
        Color top = picture.get(x, y - 1);
        int red = bottom.getRed() - top.getRed();
        int blue = bottom.getBlue() - top.getBlue();
        int green = bottom.getGreen() - top.getGreen();
        return red * red + blue * blue + green * green;
    }

    private void checkNull(Object obj) {
        if (obj == null) throw new IllegalArgumentException();
    }

    //  unit testing (optional)
    public static void main(String[] args) {

    }

}
