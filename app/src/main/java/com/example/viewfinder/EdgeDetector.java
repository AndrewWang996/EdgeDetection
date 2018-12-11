package com.example.viewfinder;

import android.graphics.Bitmap;
import java.lang.Math;

public class EdgeDetector {
    private static final int[][] Sob_K_X_3x3 = new int[][]{
            {1, 0, -1},
            {2, 0, -2},
            {1, 0, -1}
    };

    private static final int[][] Sob_K_Y_3x3 = new int[][]{
            {1, 2, 1},
            {0, 0, 0},
            {-1, -2, -1}
    };

    private static final int[][] Prew_K_X_3x3 = new int[][]{
            {1, 0, -1},
            {1, 0, -1},
            {1, 0, -1}
    };

    private static final int[][] Prew_K_Y_3x3 = new int[][]{
            {1, 1, 1},
            {0, 0, 0},
            {-1, -1, -1}
    };


    private static final double[][] GAUSSIAN = new double[][]{
            {0.01257861635, 0.0251572327, 0.03144654088, 0.0251572327, 0.01257861635},
            {0.0251572327, 0.05660377358, 0.07547169811, 0.05660377358, 0.0251572327},
            {0.03144654088, 0.07547169811, 0.09433962264, 0.07547169811, 0.03144654088},
            {0.0251572327, 0.05660377358, 0.07547169811, 0.05660377358, 0.0251572327},
            {0.01257861635, 0.0251572327, 0.03144654088, 0.0251572327, 0.01257861635},
    };

    private static final double LOWER_THRESH = 0.3;
    private static final double UPPER_THRESH = 0.8;

    private static final int[] DIRECTION_BINS = new int[]{0, 45, 90, 135};

    private static int[][] magnitude(int[][] Sx, int[][]Sy ) {
        int rows = Sx.length;
        int cols = Sx[0].length;

        int[][] mag = new int[rows][cols];
        for (int r=0; r<rows; r++) {
            for (int c = 0; c < cols; c++) {
                int x = Sx[r][c];
                int y = Sy[r][c];

                mag[r][c] = (int) Math.round(Math.sqrt(Math.pow(x, 2) + Math.pow(y, 2)));
            }
        }
        return mag;
    }

    public static int[][] SobelKernel(SobelOp sobelOp) {
        switch (sobelOp) {
            case X_3x3:
                return Sob_K_X_3x3;
            case Y_3x3:
                return Sob_K_Y_3x3;
            default:
                System.err.printf(
                        "Sobel operator %s not implemented\n",
                        sobelOp.toString());
                return new int[0][0];   // yea let's not reach here...
        }
    }

    public static int[][] PrewittKernel(PrewittOp prewittOp) {
        switch (prewittOp) {
            case X_3x3:
                return Prew_K_X_3x3;
            case Y_3x3:
                return Prew_K_Y_3x3;
            default:
                System.err.printf(
                        "Prewitt operator %s not implemented\n",
                        prewittOp.toString());
                return new int[0][0];   // yea let's not reach here...
        }
    }

    private static int GetPixel(int[][] image, int r, int c) {
        if (image.length == 0) {
            return 0;
        }
        if (r < 0) r = 0;
        if (r >= image.length) r = image.length - 1;
        if (c < 0) c = 0;
        if (c >= image[0].length) c = image.length - 1;

        return image[r][c];
    }

    private static double GetPixel(double[][] image, int r, int c) {
        if (image.length == 0) {
            return 0;
        }
        if (r < 0) r = 0;
        if (r >= image.length) r = image.length - 1;
        if (c < 0) c = 0;
        if (c >= image[0].length) c = image.length - 1;

        return image[r][c];
    }

    private static int[][] ApplyKernel(
            int[][] image,
            int[][] kernel) {

        if (image.length == 0) {
            return new int[0][0];
        }
        if (kernel.length % 2 == 0) {
            return image;
        }

        int rows = image.length;
        int cols = image[0].length;
        int krows = kernel.length;
        int kcols = kernel[0].length;
        int[][] newImage = new int[rows][cols];

        for (int r=0; r < rows; r++) {
            for (int c=0; c < cols; c++) {
                int sum = 0;
                for (int rk=0; rk < krows; rk++) {
                    for (int rc=0; rc < kcols; rc++) {
                        int dr = rk - (krows / 2);
                        int dc = rc - (kcols / 2);
                        sum += GetPixel(image, r + dr, c + dc)
                                * GetPixel(kernel, rk, rc);
                    }
                }
                // Prevent negatives
                newImage[r][c] = Math.max(0, sum);
            }
        }

        return newImage;
    }

    private static int[][] ApplyKernel(
            int[][] image,
            double[][] kernel) {

        if (image.length == 0) {
            return new int[0][0];
        }
        if (kernel.length % 2 == 0) {
            return image;
        }

        int rows = image.length;
        int cols = image[0].length;
        int krows = kernel.length;
        int kcols = kernel[0].length;
        int[][] newImage = new int[rows][cols];

        for (int r=0; r < rows; r++) {
            for (int c=0; c < cols; c++) {
                double sum = 0;
                for (int rk=0; rk < krows; rk++) {
                    for (int rc=0; rc < kcols; rc++) {
                        int dr = rk - (krows / 2);
                        int dc = rc - (kcols / 2);
                        sum += GetPixel(image, r + dr, c + dc)
                                * GetPixel(kernel, rk, rc);
                    }
                }
                // Prevent negatives
                newImage[r][c] =(int) Math.max(0, sum);
            }
        }

        return newImage;
    }


    private static int[][] ToGrayValue(int[][] grayBits) {
        if (grayBits.length == 0) {
            return new int[0][0];
        }
        int rows = grayBits.length;
        int cols = grayBits[0].length;
        int[][] values = new int[rows][cols];
        for (int r=0; r<rows; r++) {
            for (int c=0; c<cols; c++) {
                values[r][c] = (grayBits[r][c] & 0xFF);
            }
        }
        return values;
    }

    private static Bitmap ToBitmap(int[][] grayValues) {
        if (grayValues.length == 0) {
            return null;
        }
        int rows = grayValues.length;
        int cols = grayValues[0].length;

        int[] grayV = new int[rows * cols];
        for (int r=0; r<rows; r++) {
            for (int c=0; c<cols; c++) {
                int y = grayValues[r][c];
                grayV[r*cols + c] =
                        0xFF000000 | (y << 16) | (y << 8) | y;
            }
        }

        Bitmap bm = Bitmap.createBitmap(cols, rows, Bitmap.Config.ARGB_8888);
        bm.setPixels(grayV, 0, cols, 0, 0, cols, rows);
        return bm;
    }

    private static int[][] getGradientDirection(
            int[][] G_x,
            int[][] G_y
    ) {
        int rows = G_x.length;
        int cols = G_x[0].length;
        int[][] values = new int[rows][cols];
        for (int r=0; r<rows; r++) {
            for (int c=0; c<cols; c++) {
                int gx = G_x[r][c];
                int gy = G_y[r][c];
                double dir = Math.atan2(gx, gy);

                double diff = 360.;
                int value = 0;
                for (int i=0; i < DIRECTION_BINS.length; i++) {
                    int bin = DIRECTION_BINS[i];
                    if (dir - bin < diff) {
                        diff = dir - bin;
                        value = bin;
                    }
                }
                values[r][c] = value;
            }
        }
        return values;
    }

    private static int[][] suppressNonMaximum(
            int[][] gradients,
            int[][] directions
    ) {
        int rows = gradients.length;
        int cols = gradients[0].length;
        int[][] values = new int[rows][cols];
        for (int r=0; r<rows; r++) {
            for (int c=0; c<cols; c++) {
                int grad = gradients[r][c];
                int dir = directions[r][c];

                int[] n1 = new int[]{0,0};
                int[] n2 = new int[]{0,0};

                switch (dir) {
                    case 0:
                        n1 = new int[]{r+1,c};
                        n2 = new int[]{r-1,c};
                        break;
                    case 45:
                        n1 = new int[]{r+1,c-1};
                        n2 = new int[]{r-1,c+1};
                        break;
                    case 90:
                        n1 = new int[]{r,c-1};
                        n2 = new int[]{r,c+1};
                        break;
                    case 135:
                        n1 = new int[]{r-1,c-1};
                        n2 = new int[]{r+1,c+1};
                        break;
                    default:
                        System.err.printf("Shouldn't get here\n");
                }

                n1[0] = Math.max(Math.min(rows-1, n1[0]), 0);
                n2[0] = Math.max(Math.min(rows-1, n2[0]), 0);

                n1[1] = Math.max(Math.min(cols-1, n1[1]), 0);
                n2[1] = Math.max(Math.min(cols-1, n2[1]), 0);

                int grad1 = gradients[n1[0]][n1[1]];
                int grad2 = gradients[n2[0]][n2[1]];

                if (grad < grad1 || grad < grad2) {
                    values[r][c] = 0;
                } else {
                    values[r][c] = grad;
                }
            }
        }
        return values;
    }

    // Apply the lower threshold
    // Hysteresis - Remove points in between the lower+upper threshold depending on neighbors
    private static int[][] filterSmallValues(
            int[][] img
    ) {
        int rows = img.length;
        int cols = img[0].length;
        double sum = 0.;
        for (int r=0; r<rows; r++) {
            for (int c = 0; c < cols; c++) {
                sum += img[r][c];
            }
        }
        double avg = sum/(rows*cols);
        double lower_thresh = LOWER_THRESH * avg;
        double upper_thresh = UPPER_THRESH * avg;

        for (int r=0; r<rows; r++) {
            for (int c = 0; c < cols; c++) {
                if (img[r][c] < lower_thresh) {
                    img[r][c] = 0;
                } // HYSTERESIS
                else if (img[r][c] < upper_thresh) {
                    int rmin = Math.max(0, r-1);
                    int rmax = Math.min(rows-1, r+1);
                    int cmin = Math.max(0, c-1);
                    int cmax = Math.min(cols-1, c+1);

                    boolean next_to_strong_neighbor = false;
                    for (int x=rmin; x<rmax; x++) {
                        for (int y=cmin; y<cmax; y++) {
                            if (img[x][y] >= upper_thresh) {
                                next_to_strong_neighbor = true;
                            }
                        }
                    }

                    if (!next_to_strong_neighbor) {
                        img[r][c] = 0;
                    }
                }
            }
        }
        return img;
    }

    /**
     * Okay yea before we call this function, we probably need
     * to do some preprocessing on the output of decodeYUVGray()
     * because idk wtf it's doing.
     *
     * @param grayscale
     * @return
     */
    public static Bitmap GetSobelImage(
            int[][] grayscale
    ) {
        if (grayscale.length == 0) {
            return null;
        }
        // preprocess grayscale to convert from bits
        grayscale = ToGrayValue(grayscale);

        // apply sobel kernel
        int[][] kernel_x = SobelKernel(SobelOp.X_3x3);
        int[][] sobel_x = ApplyKernel(grayscale, kernel_x);

        int[][] kernel_y = SobelKernel(SobelOp.Y_3x3);
        int[][] sobel_y = ApplyKernel(grayscale, kernel_y);

        int[][] sobel_x_y = magnitude(sobel_x, sobel_y);

        // convert output to bitmap
        Bitmap bm = ToBitmap(sobel_x_y);
        return bm;
    }

    public static Bitmap GetPrewittImage(
            int[][] grayscale
    ) {
        if (grayscale.length == 0) {
            return null;
        }
        // preprocess grayscale to convert from bits
        grayscale = ToGrayValue(grayscale);

        // apply  kernel
        int[][] kernel_x = PrewittKernel(PrewittOp.X_3x3);
        int[][] G_x = ApplyKernel(grayscale, kernel_x);

        int[][] kernel_y = PrewittKernel(PrewittOp.Y_3x3);
        int[][] G_y = ApplyKernel(grayscale, kernel_y);

        int[][] G_x_y = magnitude(G_x, G_y);

        // convert output to bitmap
        Bitmap bm = ToBitmap(G_x_y);
        return bm;
    }

    public static Bitmap GetCannyImage(
            int[][] grayscale
    ) {

        // preprocess grayscale to convert from bits
        grayscale = ToGrayValue(grayscale);

        // apply gaussian filter
        int[][] filtered = ApplyKernel(grayscale, GAUSSIAN);

        // apply sobel kernel
        int[][] kernel_x = SobelKernel(SobelOp.X_3x3);
        int[][] sobel_x = ApplyKernel(filtered, kernel_x);

        int[][] kernel_y = SobelKernel(SobelOp.Y_3x3);
        int[][] sobel_y = ApplyKernel(filtered, kernel_y);

        int[][] img = magnitude(sobel_x, sobel_y);
        int[][] gradient_dir = getGradientDirection(sobel_x, sobel_y);

        // non-max suppresion
        img = suppressNonMaximum(img, gradient_dir);

        // filter small values
        img = filterSmallValues(img);

        // convert output to bitmap
        Bitmap bm = ToBitmap(img);
        return bm;
    }

}
