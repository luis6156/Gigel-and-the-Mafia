// Copyright 2020
// Author: Matei Simtinică

import java.io.*;
import java.util.ArrayList;
import java.util.List;
import java.util.Scanner;

/**
 * Task1
 * You have to implement 4 methods:
 * readProblemData         - read the problem input and store it however you see fit
 * formulateOracleQuestion - transform the current problem instance into a SAT instance and write the oracle input
 * decipherOracleAnswer    - transform the SAT answer back to the current problem's answer
 * writeAnswer             - write the current problem's answer
 */
public class Task1 extends Task {
    private int families;
    private int totalRelationships;
    private int spies;
    private final List<List<Integer>> relationships = new ArrayList<>();
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
            totalRelationships = scanner.nextInt();
            spies = scanner.nextInt();

            // Initialize lists
            for (int i = 0; i < families; ++i) {
                relationships.add(new ArrayList<>());
            }

            // Add relationships
            while (scanner.hasNext()) {
                relationships.get(scanner.nextInt() - 1).add(scanner.nextInt() - 1);
            }
        } catch (FileNotFoundException e) {
            System.out.println("Input file not found.");
            e.printStackTrace();
        }
    }

    @Override
    public void formulateOracleQuestion() throws IOException {
        int totalVariables = families * spies;
        int totalClauses =  spies * totalRelationships + families + families
                * (spies * (spies - 1)) / 2;

        BufferedWriter writer = new BufferedWriter(new FileWriter(oracleInFilename));
        writer.write("p cnf " + totalVariables + " " + totalClauses + "\n");

        // First clause
        // Every two nodes that have an edge should not be assigned the same spy
        for (int i = 0; i < relationships.size(); ++i) {
            for (int j = 0; j < relationships.get(i).size(); ++j) {
                int currNode = i * spies;
                int adjacentNode = relationships.get(i).get(j) * spies;
                for (int k = 1; k <= spies; ++k) {
                    writer.write("-" + (currNode + k) + " -" + (adjacentNode + k) + " 0\n");
                }
            }
        }

        // Second clause
        // Every node should not be left without a spy
        for (int i = 0; i < families; ++i) {
            int currNode = i * spies;
            for (int k = 1; k <= spies; ++k) {
                writer.write((currNode + k) + " ");
            }
            writer.write("0\n");
        }

        // Third clause
        // A node can have at most one spy
        for (int i = 0; i < families; ++i) {
            int currNode = i * spies;
            for (int j = 0; j < spies - 1; ++j) {
                for (int k = j + 1; k < spies; ++k) {
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
                // The mod operation results in the spy index
                int spy = solution % spies;
                // If the spy index is divisible by the total number of spies, write the total
                // number of spies as index, else write the result
                if (spy == 0) {
                    writer.write(spies + " ");
                } else {
                    writer.write(spy + " ");
                }
            }
        }

        writer.flush();
        writer.close();
    }
}
