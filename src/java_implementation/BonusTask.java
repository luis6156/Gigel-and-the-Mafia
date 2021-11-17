// Copyright 2020
// Author: Matei Simtinică

import java.io.*;
import java.util.ArrayList;
import java.util.List;
import java.util.Scanner;
import java.util.stream.Collectors;
import java.util.stream.Stream;

/**
 * Bonus Task
 * You have to implement 4 methods:
 * readProblemData         - read the problem input and store it however you see fit
 * formulateOracleQuestion - transform the current problem instance into a SAT instance and write the oracle input
 * decipherOracleAnswer    - transform the SAT answer back to the current problem's answer
 * writeAnswer             - write the current problem's answer
 */
public class BonusTask extends Task {
    private final List<List<Integer>> relationships = new ArrayList<>();
    private final List<List<Integer>> noRelationships = new ArrayList<>();
    private int families;
    private int totalRelationships;
    private int dimension;
    private List<Integer> solutions;

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
            totalRelationships = scanner.nextInt();

            // Calculate total number of no-edge relationships using the total number of
            // vertices in a complete graph and the total number of relationships from the input
            int totalNoRelationships = families * (families - 1) / 2 - totalRelationships;
            // First Method for Clique Size: used the total number of vertices in a complete graph
            dimension = (int) Math.floor((1 + Math.sqrt(1 + 8 * totalNoRelationships)) / 2);

            for (int i = 0; i < families; ++i) {
                // Initialize the relationship list
                relationships.add(new ArrayList<>());
                // Create the lists for the list of no-edge relationships and add it
                List<Integer> numbers = Stream.iterate(0, n -> n + 1)
                        .limit(families)
                        .collect(Collectors.toList());
                numbers.remove(Integer.valueOf(i));
                noRelationships.add(numbers);
            }

            // Generate list of family indexes
            solutions = Stream.iterate(1, n -> n + 1)
                    .limit(families)
                    .collect(Collectors.toList());

            while (scanner.hasNext()) {
                int firstNode = scanner.nextInt();
                int secondNode = scanner.nextInt();
                // Add the relationship to the edge list
                relationships.get(firstNode - 1).add(secondNode - 1);
                // Remove the relationship from the non-edge list
                noRelationships.get(firstNode - 1).remove(Integer.valueOf(secondNode - 1));
                noRelationships.get(secondNode - 1).remove(Integer.valueOf(firstNode - 1));
            }

            // Second Method for Clique Size: used the internal degree of a node
            int max = 0;
            for (List<Integer> noRelationship : noRelationships) {
                if (max < noRelationship.size()) {
                    int size = noRelationship.size();
                    boolean found = noRelationship.stream()
                            .mapToInt(integer -> noRelationships.get(integer).size())
                            .noneMatch(curr -> curr < size);
                    if (found) {
                        max = noRelationship.size();
                    }
                }
            }

            // Use the best method for finding clique size
            if (max != 0 && dimension > max + 1) {
                dimension = max + 1;
            }
        } catch (FileNotFoundException e) {
            System.out.println("Input file not found.");
            e.printStackTrace();
        }
    }

    @Override
    public void formulateOracleQuestion() throws IOException {
        int totalVariables = families * dimension;
        int totalClauses = dimension + (dimension - 1) * dimension * totalRelationships
                + dimension * families * (families - 1) / 2 + families
                * (dimension * (dimension - 1)) / 2;
        int totalWeights = dimension * (dimension + 1) / 2 + 1;

        BufferedWriter writer = new BufferedWriter(new FileWriter(oracleInFilename));
        writer.write("p wcnf " + totalVariables + " " + totalClauses + " " + totalWeights
                + "\n");

        // First clause
        // Every node in a clique needs to be represented by one of the families
        for (int i = 0; i < dimension; ++i) {
            int currNode = i + 1;
            writer.write((i + 1) + " ");
            for (int j = 0; j < families; ++j) {
                writer.write(currNode + " ");
                currNode += dimension;
            }
            writer.write("0\n");
        }

        // Second clause
        // Two nodes in a edge relationship (complementary graph) cannot coexist as nodes with
        // different indexes in a clique
        for (int i = 0; i < relationships.size(); ++i) {
            for (int j = 0; j < relationships.get(i).size(); ++j) {
                int currNode = i * dimension;
                int adjacentNode = relationships.get(i).get(j) * dimension;
                for (int k = 1; k <= dimension; ++k) {
                    for (int l = 1; l <= dimension; ++l) {
                        if (k != l) {
                            writer.write(totalWeights + " -" + (currNode + k) + " -"
                                    + (adjacentNode + l) + " 0\n");
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
                writer.write(totalWeights + " -" + (currNode) + " -" + (adjacentNode) + " 0\n");
                adjacentNode += dimension;
            }
        }

        // Forth clause
        // A node can have represent at most one node of a clique
        for (int i = 0; i < families; ++i) {
            int currNode = i * dimension;
            for (int j = 0; j < dimension - 1; ++j) {
                for (int k = j + 1; k < dimension; ++k) {
                    writer.write(totalWeights + " -" + (currNode + j + 1) + " -"
                            + (currNode + k + 1) + " " + "0\n");
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

            int totalVariables = scanner.nextInt();

            // Redundant variable from output so I ignore it
            scanner.next();

            // Find the size of the maximal clique
            dimension = totalVariables / families;

            while (scanner.hasNext()) {
                int solution = scanner.nextInt();
                // Remove from the list of family indexes the family given as solution
                if (solution >= 0) {
                    int family = (int) Math.ceil(solution / (float) dimension);
                    solutions.remove(Integer.valueOf(family));
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

        // Write all remaining family indexes to output
        for (Integer solution : solutions) {
            writer.write(solution + " ");
        }

        writer.flush();
        writer.close();
    }
}
