import java.nio.file.Files;
import java.nio.file.Paths;
import java.math.BigInteger;
import java.util.regex.*;

public class FindConstant {

    public static void main(String[] args) throws Exception {
        // Read file content
        String content = new String(Files.readAllBytes(Paths.get("input.json")));

        // Extract n and k from inside "keys"
        Pattern nkPattern = Pattern.compile("\"keys\"\\s*:\\s*\\{[^}]*\"n\"\\s*:\\s*(\\d+)\\s*,\\s*\"k\"\\s*:\\s*(\\d+)");
        Matcher nkMatcher = nkPattern.matcher(content);
        if (!nkMatcher.find()) {
            throw new RuntimeException("Could not find n and k in JSON");
        }
        int n = Integer.parseInt(nkMatcher.group(1));
        int k = Integer.parseInt(nkMatcher.group(2));
        System.out.println("n = " + n + ", k = " + k);

        // Prepare matrix and y-values
        double[][] matrix = new double[k][k];
        double[] yVals = new double[k];

        // Loop over first k points
        for (int i = 1; i <= k; i++) {
            Pattern pointPattern = Pattern.compile("\"" + i + "\"\\s*:\\s*\\{\\s*\"base\"\\s*:\\s*\"(\\d+)\"\\s*,\\s*\"value\"\\s*:\\s*\"([^\"]+)\"");
            Matcher pointMatcher = pointPattern.matcher(content);
            if (!pointMatcher.find()) {
                throw new RuntimeException("Could not find point for x = " + i);
            }

            int base = Integer.parseInt(pointMatcher.group(1));
            String valueStr = pointMatcher.group(2);

            // Decode y from base
            BigInteger bigY = new BigInteger(valueStr, base);
            double y = bigY.doubleValue(); // may lose precision for very large values

            double x = i;
            for (int j = 0; j < k; j++) {
                matrix[i - 1][j] = Math.pow(x, j);
            }
            yVals[i - 1] = y;
        }

        // Solve using Gaussian elimination
        double[] coeffs = gaussianElimination(matrix, yVals);
        System.out.println("Secret constant (C) = " + coeffs[0]);
    }

    private static double[] gaussianElimination(double[][] matrix, double[] yVals) {
        int n = yVals.length;
        for (int i = 0; i < n; i++) {
            // Find pivot row
            int max = i;
            for (int j = i + 1; j < n; j++) {
                if (Math.abs(matrix[j][i]) > Math.abs(matrix[max][i])) {
                    max = j;
                }
            }

            // Swap rows in matrix
            double[] tempRow = matrix[i];
            matrix[i] = matrix[max];
            matrix[max] = tempRow;

            // Swap y values
            double tempVal = yVals[i];
            yVals[i] = yVals[max];
            yVals[max] = tempVal;

            // Normalize pivot row
            double pivot = matrix[i][i];
            for (int j = i; j < n; j++) {
                matrix[i][j] /= pivot;
            }
            yVals[i] /= pivot;

            // Eliminate other rows
            for (int j = 0; j < n; j++) {
                if (j != i) {
                    double factor = matrix[j][i];
                    for (int k = i; k < n; k++) {
                        matrix[j][k] -= factor * matrix[i][k];
                    }
                    yVals[j] -= factor * yVals[i];
                }
            }
        }
        return yVals;
    }
}
