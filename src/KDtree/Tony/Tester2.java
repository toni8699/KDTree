package assignments2019.a3;

import java.util.ArrayList;
import java.util.Random;

public class Tester2 {
    //
    public static void main(String[] args) throws Exception {
        int numberOfTests;
        int maxNumberOfDatum;
        int maxRange;
        int dimension;

        long seed;

        try {
            numberOfTests = Integer.parseInt(args[0]);
            maxNumberOfDatum = Integer.parseInt(args[1]);
            maxRange = Integer.parseInt(args[2]);
            dimension = Integer.parseInt(args[3]);

            seed = Long.parseLong(args[4]);

        } catch (Exception someException) {
            maxNumberOfDatum = 50000;
            seed = 1;
            numberOfTests = 100;
            maxRange = 100000;
            dimension = 200;

            System.out.println("Attention: command-line parameters are empty, incomplete, or incorrectly formatted. \n" +
                    "Here's the right way to do it: java BananaTest [numberOfTests] [maxNumberOfDatum] [maxRange] [dimension] [seed]");
            System.out.format("Defaulting to %d test cases.\n", numberOfTests);
            System.out.format("Defaulting to a maximum of %d data points.\n", maxNumberOfDatum);
            System.out.format("Defaulting to a maximum range of %d.\n", maxRange);
            System.out.format("Defaulting to a dimension of %d.\n", dimension);


            System.out.format("Your random seed is %d. You will need this seed to reproduce the test cases. " +
                    "Make sure you include it when you share the results with your friends. :)\n", seed);
        }

        benchMarkNearestPointInNode(dimension, numberOfTests, maxRange, maxNumberOfDatum, seed);

    }


    public static void benchMarkNearestPointInNode(int dimension, int numberOfTests, int maxRange, int maxNumberOfDatum, long seed) throws Exception {
        long currentSeed = seed;
        float averageEfficiency = 0;
        float averageEfficiencyWithArray = 0;

        ArrayList<Datum> outputFromBruteForce;
        Datum actualOutput;

        for (int testIndex = 1; testIndex <= numberOfTests; testIndex ++) {
            Random generator = new Random(currentSeed);
//            int numberOfDatum = generator.nextInt(maxNumberOfDatum);

            System.out.format("\rInitializing KD-Tree for test case %d \t\t\t\t\t", testIndex);
//            TestCase testCase = new TestCase(numberOfDatum, dimension, maxRange, currentSeed);
            TestCase testCase = new TestCase(maxNumberOfDatum, dimension, maxRange, currentSeed);
            Datum queryPoint = testCase.generateRandomDatum();

            System.out.format("\rBrute-forcing test case %d \t\t\t\t\t", testIndex);
            long bruteForceStartTime = System.nanoTime();
            outputFromBruteForce = testCase.findAllMatchesWithBruteForce(queryPoint, testCase.kdTree);
            long bruteForceEndTime = System.nanoTime();

            long bruteForceStartTimeWithArray = System.nanoTime();
            testCase.findAllMatchesWithBruteForce(queryPoint, testCase.datalist);
            long bruteForceEndTimeWithArray = System.nanoTime();

            System.out.format("\rRunning test case %d on your KD-Tree \t\t\t\t\t", testIndex);
            long actualStartTime = System.nanoTime();
            actualOutput = testCase.kdTree.nearestPoint(queryPoint);
            long actualEndTime = System.nanoTime();

            long bruteForceTime = bruteForceEndTime - bruteForceStartTime;
            long bruteForceTimeWithArray = bruteForceEndTimeWithArray - bruteForceStartTimeWithArray;
            long actualTime = actualStartTime - actualEndTime;

            float efficiency = (float) bruteForceTime / actualTime;
            float efficiencyWithArray = (float) bruteForceTimeWithArray / actualTime;

            averageEfficiency += (float) efficiency / numberOfTests;
            averageEfficiencyWithArray += (float) efficiencyWithArray / numberOfTests;

            if (!outputFromBruteForce.contains(actualOutput)) {
                System.out.format("\rAttention! Your method returned an incorrect nearest point on test %d.", testIndex);
                throw new Exception();
            }

            currentSeed ++;
        }

        System.out.format("\r" + "All %d tests are now completed\n", numberOfTests);

        float percentageEfficiencyChange = 100 * (1 - averageEfficiency);

        if (percentageEfficiencyChange >= 0) {
            System.out.format("When compared to brute-forcing with your iterator, " +
                    "on average your nearestPoint method is %f" + "%%" + " more efficient.\n", percentageEfficiencyChange);
        }
        else {
            System.out.format("When compared to brute-forcing with your iterator, " +
                    "on average your nearestPoint method is %f" + "%%" + " less efficient.", -percentageEfficiencyChange);
        }

        percentageEfficiencyChange = 100 * (1 - averageEfficiencyWithArray);

        if (percentageEfficiencyChange >= 0) {
            System.out.format("When compared to brute-forcing with an ArrayList of data points, " +
                    "on average your nearestPoint method is %f" + "%%" + " more efficient.\n", percentageEfficiencyChange);
        }
        else {
            System.out.format("When compared to brute-forcing with an ArrayList of data points, " +
                    "on average your nearestPoint method is %f" + "%%" + " less efficient.", -percentageEfficiencyChange);
        }
        System.out.println();
    }
    public static class TestCase {
        // This piece of code is shared under GPL 3.0. Feel free to integrate BananaTest.TestCase in your own code.
        ArrayList<Datum> datalist;
        int dimension;
        int range;
        long currentSeed;
        public KDTree kdTree;

        public TestCase(int numberOfDatum, int dimension, int range, long seed) throws Exception {
            this.dimension = dimension;
            this.range = range;
            currentSeed = seed;

            this.datalist = generateSuperRandomDatalist(numberOfDatum, dimension, range, currentSeed);
            kdTree = new KDTree(this.datalist);
        }

        static ArrayList<Datum> generateSuperRandomDatalist(int numberOfDatum, int dimension, int range, long seed) {
            Random generator = new Random(seed);
            ArrayList<Datum> datalist = new ArrayList<Datum>();

            for (int datumIndex = 0; datumIndex < numberOfDatum; datumIndex++) {
                int[] x = new int[dimension];

                for (int thisDimension = 0; thisDimension < dimension; thisDimension++) {
                    x[thisDimension] = (int) (generator.nextDouble() * range); // Random.nextDouble() returns a pseudo-random float x where 0 <= x < 1.
                }

                datalist.add(new Datum(x));
            }

            return datalist;
        }

        public ArrayList<Datum> findAllMatchesWithBruteForce(Datum queryPoint, Iterable<Datum> datalist) {
            // This non-static method returns a set of all legitimate matches.
            ArrayList<Datum> matches = new ArrayList<Datum>();
            long minimumSeparation = -1;

            for (Datum currentDatum: datalist) {
                long separation = KDTree.distSquared(currentDatum, queryPoint);

                if (separation == minimumSeparation) {
                    matches.add(currentDatum);
                }

                else if (minimumSeparation == -1 // Implies first run.
                        | separation < minimumSeparation) {
                    matches.clear();
                    matches.add(currentDatum);
                    minimumSeparation = separation;
                }
            }

            return matches;
        }

        public Datum generateRandomDatum() {
            // This non-static method returns a datum with the dimension and range specified in this TestCase instance.
            long nextSeed = this.currentSeed + 1;
            ArrayList<Datum> datalist = generateSuperRandomDatalist(1, this.dimension, this.range, nextSeed);
            return datalist.get(0);
        }
    }
}