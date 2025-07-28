# Shamir Secret Sharing (Reconstruction)

A Java implementation of a simplified Shamir’s Secret Sharing reconstruction algorithm.  
No external dependencies; uses only the Java Standard Library.

---

## Features

- **Manual JSON Parsing**  
  Uses regex to parse input files (`testcase1.json` and `testcase2.json`) without requiring third-party libraries.

- **Mixed-Base Decoding**  
  Supports decoding y-values given in bases from 2 to 16 via `BigInteger(value, base)`.

- **Lagrange Interpolation**  
  Reconstructs the secret (constant term) using the formula:
  \[
    f(0) \;=\; \sum_{i=0}^{k-1} y_i \;\prod_{\substack{j=0\\j \neq i}}^{k-1} \frac{-x_j}{x_i - x_j}
  \]

- **Large Number Support**  
  Handles 256-bit integers with `java.math.BigInteger`.

---

## Prerequisites

- **Java 15 or later**  
  Required for text blocks (`"""…"""`).  
  If your environment uses Java 8–11, replace text blocks with concatenated strings.

---

## Project Structure

├── Assignment.java
├── testcase1.json
├── testcase2.json
└── README.md



- **ShamirSecretSharing.java**  
  Main source file implementing parsing, decoding, and interpolation.

- **testcase1.json**  
  Sample input (n=4, k=3) provided by assignment.

- **testcase2.json**  
  Sample input (n=10, k=7) provided by assignment.

- **README.md**  
  This documentation.
