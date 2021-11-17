// Copyright 2020
// Author: Matei Simtinică

import java.io.*;
import java.util.ArrayList;
import java.util.List;
import java.util.Scanner;
import java.util.stream.Collectors;
import java.util.stream.Stream;

/**
 * Task2
 * You have to implement 4 methods:
 * readProblemData         - read the problem input and store it however you see fit
 * formulateOracleQuestion - transform the current problem instance into a SAT instance and write the oracle input
 * decipherOracleAnswer    - transform the SAT answer back to the current problem's answer
 * writeAnswer             - write the current problem's answer
 */
public class Task2 extends Task {
    // TODO: define necessary variables and/or data structures
    private int families;
    private int totalNoRelationships;
    private int dimension;
    private final List<List<Integer>> relationships = new ArrayList<>();
    private final List<List<Integer>> noRelationships = new ArrayList<>();
    private String result;
    private final List<Integer> solutions = new ArrayList<>();

    @Override
    public void solve() throws IOException, InterruptedException {
        readProblemData();
        formulateOracleQuestion();
        askOracle();
        decipherOracleAnswer();
        writeAnswer();
    }

    @Override
    public void readProblemData() {
        try {
            File file = new File(inFilename);
            Scanner scanner = new Scanner(file);

            families = scanner.nextInt();
            int totalRelationships = scanner.nextInt();
            dimension = scanner.nextInt();

            // Calculate total number of no-edge relationships using the total number of
            // vertices in a complete graph and the total number of relationships from the input
            totalNoRelationships = families * (families - 1) / 2 - totalRelationships;

            for (int i = 0; i < families; ++i) {
                // Initialize the relationship list
                relationships.add(new ArrayList<>());
                // Create the lists for the list of no-edge relationships and add it (does not have
                // indexes that are smaller than the current index)
                List<Integer> numbers = Stream.iterate(i + 1, n -> n + 1)
                        .limit(families - (i + 1))
                        .collect(Collectors.toList());
                noRelationships.add(numbers);
            }

            while (scanner.hasNext()) {
                int firstNode = scanner.nextInt();
                int secondNode = scanner.nextInt();
                // Add the relationship to the edge list
                relationships.get(firstNode - 1).add(secondNode - 1);
                // Remove the relationship from the non-edge list
                noRelationships.get(firstNode - 1).remove(Integer.valueOf(secondNode - 1));
            }
        } catch (FileNotFoundException e) {
            System.out.println("Input file not found.");
            e.printStackTrace();
        }
    }

    @Override
    public void formulateOracleQuestion() throws IOException {
        int totalVariables = families * dimension;
        int totalClauses = dimension + (dimension - 1) * dimension * totalNoRelationships
                + dimension * families * (families - 1) / 2 + families
                * (dimension * (dimension - 1)) / 2;

        BufferedWriter writer = new BufferedWriter(new FileWriter(oracleInFilename));
        writer.write("p cnf " + totalVariables + " " + totalClauses + "\n");

        // First clause
        // Every node in a clique needs to be represented by one of the families
        for (int i = 0; i < dimension; ++i) {
            int currNode = i + 1;
            for (int j = 0; j < families; ++j) {
                writer.write(currNode + " ");
                currNode += dimension;
            }
            writer.write("0\n");
        }

        // Second clause
        // Two nodes in a non-edge relationship cannot coexist as nodes with different
        // indexes in a clique
        for (int i = 0; i < noRelationships.size(); ++i) {
            for (int j = 0; j < noRelationships.get(i).size(); ++j) {
                int currNode = i * dimension;
                int adjacentNode = noRelationships.get(i).get(j) * dimension;
                for (int k = 1; k <= dimension; ++k) {
                    for (int l = 1; l <= dimension; ++l) {
                        if (k != l) {
                            writer.write("-" + (currNode + k) + " -" + (adjacentNode + l) + " 0\n");
                        }
                    }
                }
            }
        }

        // Third clause
        // Any two nodes should not represent the same node index of a clique
        int limit = 0;
        for (int i = 0; i < totalVariables; ++i) {
            int currNode = i + 1;
            int adjacentNode = currNode + dimension;
            if (i % dimension == 0) {
                ++limit;
            }
            for (int k = 0; k < families - limit; ++k) {
                writer.write("-" + (currNode) + " -" + (adjacentNode) + " 0\n");
                adjacentNode += dimension;
            }
        }

        // Forth clause
        // A node can have represent at most one node of a clique
        for (int i = 0; i < families; ++i) {
            int currNode = i * dimension;
            for (int j = 0; j < dimension - 1; ++j) {
                for (int k = j + 1; k < dimension; ++k) {
                    writer.write("-" + (currNode + j + 1) + " -" + (currNode + k + 1) + " " +
                            "0\n");
                }
            }
        }

        writer.flush();
        writer.close();
    }

    @Override
    public void decipherOracleAnswer() {
        try {
            File file = new File(oracleOutFilename);
            Scanner scanner = new Scanner(file);

            result = scanner.next();

            if (result.equals("True")) {
                // Total number of variables is useless so I ignore it
                scanner.next();

                // Add to the list only the positive values
                while (scanner.hasNext()) {
                    int solution = scanner.nextInt();
                    if (solution > 0) {
                        solutions.add(solution);
                    }
                }
            }
        } catch (FileNotFoundException e) {
            System.out.println("Oracle output file not found.");
            e.printStackTrace();
        }
    }

    @Override
    public void writeAnswer() throws IOException {
        BufferedWriter writer = new BufferedWriter(new FileWriter(outFilename));

        writer.write(result + "\n");

        if (result.equals("True")) {
            for (Integer solution : solutions) {
                // Dividing by the dimension of the clique, returns the index of the family
                int family = (int) Math.ceil(solution / (float) dimension);
                writer.write(family + " ");
            }
        }

        writer.flush();
        writer.close();
    }
}
