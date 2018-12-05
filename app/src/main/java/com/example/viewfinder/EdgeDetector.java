package com.example.viewfinder;

import android.graphics.Bitmap;

public class EdgeDetector {
    private static final int[][] K_X_3x3 = new int[][]{
            {1, 0, -1},
            {2, 0, -2},
            {1, 0, -1}
    };

    private static final int[][] K_Y_3x3 = new int[][]{
            {1, 2, 1},
            {0, 0, 0},
            {-1, -2, -1}
    };

    public static int[][] SobelKernel(SobelOp sobelOp) {
        switch (sobelOp) {
            case X_3x3:
                return K_X_3x3;
            case Y_3x3:
                return K_Y_3x3;
            default:
                System.err.printf(
                        "Sobel operator %s not implemented\n",
                        sobelOp.toString());
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

    /**
     * Okay yea before we call this function, we probably need
     * to do some preprocessing on the output of decodeYUVGray()
     * because idk wtf it's doing.
     *
     * @param grayscale
     * @return
     */
    public static Bitmap GetSobelImage(
            int[][] grayscale,
            SobelOp sobelOp) {
        if (grayscale.length == 0) {
            return null;
        }
        // preprocess grayscale to convert from bits
        grayscale = ToGrayValue(grayscale);

        // apply sobel kernel
        int[][] kernel = SobelKernel(sobelOp);
        int[][] sobelImage = ApplyKernel(grayscale, kernel);

        // convert output to bitmap
        Bitmap bm = ToBitmap(sobelImage);

        return bm;
    }
}
