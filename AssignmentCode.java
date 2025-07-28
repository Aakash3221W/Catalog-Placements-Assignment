import java.io.*;
import java.math.BigInteger;
import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * ShamirSharing
 *
 * A Java implementation of a simplified Shamir's Secret Sharing reconstruction algorithm.
 * - Parses share data from JSON files: testcase1.json and testcase2.json
 * - Decodes y-values given in various bases into BigInteger
 * - Performs Lagrange interpolation to compute the secret (constant term)
 * - Outputs the secret for each test case
 *
 * Requirements:
 * - Java 15+ (for text blocks)
 * - No external libraries; uses only java.math.BigInteger and java.util.regex for JSON parsing
 *
 * Usage:
 *   java ShamirSecretSharing
 *
 * JSON files must reside in the same directory:
 *   - testcase1.json
 *   - testcase2.json
 *
 * See README.md for more details.
 */
public class ShamirSecretSharing {

    /** Represents a share point (x, y) on the secret polynomial */
    public static class Point {
        BigInteger x;
        BigInteger y;

        public Point(BigInteger x, BigInteger y) {
            this.x = x;
            this.y = y;
        }

        @Override
        public String toString() {
            return "(" + x + ", " + y + ")";
        }
    }

    public static void main(String[] args) {
        try {
            // Process Test Case 1
            System.out.println("=== TEST CASE 1 ===");
            String secret1 = solveTestCase("testcase1.json");
            System.out.println("Secret: " + secret1);
            System.out.println();

            // Process Test Case 2
            System.out.println("=== TEST CASE 2 ===");
            String secret2 = solveTestCase("testcase2.json");
            System.out.println("Secret: " + secret2);

        } catch (Exception e) {
            System.err.println("Error: " + e.getMessage());
            e.printStackTrace();
        }
    }

    /**
     * Reads a JSON file from disk and reconstructs the secret.
     *
     * @param filename JSON input file
     * @return the recovered secret as a decimal string
     * @throws IOException if file read fails
     */
    public static String solveTestCase(String filename) throws IOException {
        String jsonString = readFile(filename);

        // Extract n and k from the "keys" block
        Map<String, String> keys = parseKeys(jsonString);
        int n = Integer.parseInt(keys.get("n"));
        int k = Integer.parseInt(keys.get("k"));

        System.out.println("n (total points): " + n);
        System.out.println("k (minimum needed): " + k);
        System.out.println("Polynomial degree: " + (k - 1));
        System.out.println();

        // Parse all share points
        List<Point> points = parsePoints(jsonString);

        System.out.println("Decoded points:");
        for (Point p : points) {
            System.out.println("  " + p);
        }
        System.out.println();

        // Select the first k shares (sorted by x) for interpolation
        List<Point> selected = points.subList(0, k);
        System.out.println("Using first " + k + " points for interpolation:");
        for (Point p : selected) {
            System.out.println("  " + p);
        }
        System.out.println();

        // Compute the secret via Lagrange interpolation
        BigInteger secret = lagrangeInterpolation(selected);
        return secret.toString();
    }

    /** Reads entire file content into a String. */
    private static String readFile(String filename) throws IOException {
        StringBuilder sb = new StringBuilder();
        try (BufferedReader br = new BufferedReader(new FileReader(filename))) {
            String line;
            while ((line = br.readLine()) != null) {
                sb.append(line).append('\n');
            }
        }
        return sb.toString();
    }

    /**
     * Parses the "keys" block to extract n and k values.
     */
    private static Map<String, String> parseKeys(String json) {
        Map<String, String> keys = new HashMap<>();
        Pattern p = Pattern.compile(
            "\"keys\"\\s*:\\s*\\{[^}]*\"n\"\\s*:\\s*(\\d+)[^}]*\"k\"\\s*:\\s*(\\d+)[^}]*\\}"
        );
        Matcher m = p.matcher(json);
        if (m.find()) {
            keys.put("n", m.group(1));
            keys.put("k", m.group(2));
        }
        return keys;
    }

    /**
     * Parses all share entries of the form:
     *   "x": { "base": "b", "value": "v" }
     * into Point(x, BigInteger(v, b)), then sorts by x ascending.
     */
    private static List<Point> parsePoints(String json) {
        List<String[]> raw = new ArrayList<>();
        Pattern p = Pattern.compile(
            "\"(\\d+)\"\\s*:\\s*\\{\\s*\"base\"\\s*:\\s*\"(\\d+)\"\\s*,\\s*\"value\"\\s*:\\s*\"([^\"]+)\"\\s*\\}"
        );
        Matcher m = p.matcher(json);
        while (m.find()) {
            raw.add(new String[]{ m.group(1), m.group(2), m.group(3) });
        }

        // Sort entries by x coordinate
        raw.sort(Comparator.comparing(a -> new BigInteger(a[0])));

        // Convert to Point objects
        List<Point> points = new ArrayList<>();
        for (String[] entry : raw) {
            BigInteger x = new BigInteger(entry[0]);
            int base = Integer.parseInt(entry[1]);
            BigInteger y = new BigInteger(entry[2], base);
            points.add(new Point(x, y));
        }
        return points;
    }

    /**
     * Performs Lagrange interpolation at x=0 to recover the constant term:
     *   f(0) = sum_{i=0..k-1} [ y_i * ∏_{j≠i} (-x_j)/(x_i - x_j) ]
     */
    private static BigInteger lagrangeInterpolation(List<Point> pts) {
        BigInteger result = BigInteger.ZERO;
        int k = pts.size();

        for (int i = 0; i < k; i++) {
            Point pi = pts.get(i);
            BigInteger num = BigInteger.ONE;
            BigInteger den = BigInteger.ONE;

            // Build numerator & denominator for basis Li(0)
            for (int j = 0; j < k; j++) {
                if (i == j) continue;
                Point pj = pts.get(j);
                num = num.multiply(pj.x.negate());                  // (0 - x_j)
                den = den.multiply(pi.x.subtract(pj.x));            // (x_i - x_j)
            }

            // Accumulate yi * (num/den)
            BigInteger term = pi.y.multiply(num).divide(den);
            result = result.add(term);
        }

        return result;
    }
}
