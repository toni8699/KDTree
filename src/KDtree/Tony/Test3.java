package assignments2019.a3;
//package assignments2019.a3;
import java.util.*;

import assignments2019.a3.KDTree.KDNode;


public class Test3 {

    static enum Option {
        UNIFORM,
        SHAPE,
        RANDOM,
    };

    //TEST PARAMETERS

    // How many dimensional space?
    // Testing of the iterator is available only for 1D points.
    static int numdimensions = 2; // Options: 1, 2, 3
    // Data distribution?
    static Option op = Option.RANDOM; // Options: Option.UNIFORM, Option.SHAPE, Option.RANDOM,
    // Allow duplicates? Hint, Hint...
    static boolean allow_duplicates = true; // Options: true, false
    // Print README?
    static boolean printReadme = false;
    // How many constructor tries?
    static int constructorTries = 10;
    // Test parameters
    static boolean testParameters = false;
    //Test tree structure validity
    static boolean testValidity = true;
    // Print note? (only available when testing validity)
    static boolean printNote = true;
    // Test nearest neighbor search?
    static boolean testNNSearch = false;
    // Test iteration? (only available for 1D)
    static boolean testIteration = true;


    //   uniformly spaced points
    public static ArrayList<Datum> makeDataPoints1D(int low, int high, int step) throws Exception{
        ArrayList<Datum> points = new ArrayList<Datum>();
        int[] x = new int[1];
        for (int x1=low; x1 < high; x1 += step) {
            x[0] = x1;
            points.add( new Datum(x));
        }

        return points;
    }

    public static ArrayList<Datum> makeDataPoints2D_uniform(int width, int step){
        ArrayList<Datum> points = new ArrayList<Datum>();
        int[] x = new int[2];
        for (int x1 = -width; x1 < width; x1 += step)
            for (int x2 = -width; x2 < width; x2 += step) {
                x[0] = x1;
                x[1] = x2;
                points.add( new Datum(x));
            }
        return points;
    }

    public static ArrayList<Datum> makeDataPoints2D_circle(double radius){
        ArrayList<Datum> points = new ArrayList<Datum>();
        int[] x = new int[2];
        int bound = (int) radius + 1;
        double r;
        for (int x1 = -bound; x1 < bound; x1++)
            for (int x2 = -bound; x2 < bound; x2++) {
                x[0] = x1;
                x[1] = x2;
                r = Math.sqrt(1.0*x1*x1 + x2*x2);
                if (( r <= radius ) && (r > radius-1))
                    points.add( new Datum(x));
            }

        return points;
    }

    public static ArrayList<Datum> makeDataPoints3D_sphere(double radius){
        ArrayList<Datum> points = new ArrayList<Datum>();
        int[] x = new int[3];
        int bound = (int) radius + 1;
        double distFromOrigin;
        for (int x1= -bound; x1 < bound; x1 ++)
            for (int x2= -bound; x2 < bound; x2++) {
                for (int x3= -bound; x3 < bound; x3++) {
                    x[0] = x1;
                    x[1] = x2;
                    x[2] = x3;
                    distFromOrigin = Math.sqrt(1.0*x1*x1 + x2*x2 + x3*x3);
                    if (( distFromOrigin <= radius ) && (distFromOrigin > radius-1))
                        points.add( new Datum(x));
                }
            }
        return points;
    }

    private static ArrayList<Datum> makeDataPointsRandom(int[] lows, int[] highs, int numpoints, long seed, boolean allow_duplicates) throws Exception {
        if (lows.length != highs.length) {
            throw new Exception();
        }

        Collection<Datum> points;
        if (allow_duplicates) {
            points = new ArrayList<>();
        } else {
            points = new HashSet<>();
        }
        Random r = new Random(seed);
        for (int n = 0; n < numpoints; n++) {
            int[] x = new int[lows.length];
            for (int i = 0; i < lows.length; i++) {
                x[i] = r.nextInt((highs[i]-lows[i])) + lows[i];
            }
            points.add(new Datum(x));
        }
        return new ArrayList<Datum>(points);
    }

    public static long minDistSquaredBruteForce(ArrayList<Datum> points, Datum query) {
        long tmpDistSqr, minDistSqr = ((long) Integer.MAX_VALUE) * Integer.MAX_VALUE;
        for (Datum d : points) {
            tmpDistSqr = KDTree.distSquared(query, d );
            if (tmpDistSqr < minDistSqr) {
                minDistSqr = tmpDistSqr;
            }
        }
        return minDistSqr;
    }

    public static void testQuery(int[] q, ArrayList<Datum> points,  KDTree tree) {
        Datum query = new Datum(q);

        long tree_s_time = System.nanoTime();
        Datum nearestPoint = tree.nearestPoint(query);
        long tree_e_time = System.nanoTime();
        long tree_np_sqdst = KDTree.distSquared(query, nearestPoint);
        float tree_time = ((float)(tree_e_time-tree_s_time)) / 1000000; // Convert from nanoseconds to milliseconds


        long brute_s_time = System.nanoTime();
        long brute_np_sqdst = minDistSquaredBruteForce(points, query);
        long brute_e_time = System.nanoTime();
        float brute_time = ((float)(brute_e_time-brute_s_time)) / 1000000; // Convert from nanoseconds to milliseconds

        System.out.println("Query point is " + query);
        System.out.println("Nearest point (returned by KDTree) is " + nearestPoint);
        System.out.println("Minimum squared distance to nearest point (as returned by KDTree) is: "+tree_np_sqdst);
        System.out.println("Minimum squared distance to nearest point (expected): "+brute_np_sqdst);

        // Finding the nearest point via brute force should be: O(n)
        double n = points.size();

        // Finding the nearest point via a KDTree should be: O(log2(n))
        double log_n = Math.log(n) / Math.log(2);

        // The speedup (over brute force) should be: Ω(n/log2(n))
        double constant = 0.002;
        double required_speedup = constant * (n / log_n);

        if (tree_np_sqdst == brute_np_sqdst) {
            // KDTree.nearestPoint(query) only superior to brute force for trees of sufficient size
            if (points.size() >= 50000) {
                // Tree method must be significantly faster than brute force
                if ((brute_time / tree_time) >= required_speedup) {
                    System.out.println("Test Passed. Speedup over brute force: "+(brute_time/tree_time)+"x");
                } else {
                    System.out.println("Test Failed (Not significantly faster than brute force)");
                }
            } else {
                System.out.println("Test Passed");
            }
        } else {
            System.out.println("Test Failed (Point returned is not the nearest point)");
        }
        System.out.println();
    }

    public static void testStructure(KDTree tree) {
        int[] validity = testStructure(tree.rootNode);
        System.out.print("There are "+validity[0]+" invalid leaves and "+validity[1]+" invalid branches.");
        if (validity[0] == 0 && validity[1] == 0) {
            System.out.println(" Structure test passed.");
        } else {
            System.out.println(" Structure test failed.");
        }
    }
    private static int[] testStructure(KDNode node) {
        boolean isValid;
        int[] returnArr = {0, 0};
        if (node.leaf == true) {
            isValid = (node.lowChild == null && node.highChild == null
                    && node.leafDatum instanceof Datum);
            if (!isValid) {
                returnArr[0]++;
            }
            return returnArr;
        } else {
            isValid = (node.lowChild instanceof KDNode && node.highChild instanceof KDNode
                    && node.leaf == false && node.leafDatum == null);
            if (!isValid) {
                returnArr[1]++;
            }
            int[] testLow = testStructure(node.lowChild);
            int[] testHigh = testStructure(node.highChild);
            returnArr[0] += testLow[0] + testHigh[0];
            returnArr[1] += testLow[1] + testLow[1];
            return returnArr;
        }
    }

    public static void testIterator(KDTree tree, ArrayList<Datum> points){
        Iterator<Datum> it = tree.iterator();
        int [] raw_points = new int[points.size()];
        int [] raw_points_set =  new int[tree.size()];
        boolean check_size = false;
        if (points.get(0).x.length > 1) {
            System.out.println("Warning!  This Tester only checks for the validity of the order of the points of the iterator for 1D case.");
        }
        else {
            for (int i = 0; i < points.size(); i++)
                raw_points[i] = points.get(i).x[0];
            //sorting the array
            Arrays.sort(raw_points);
            //removing duplicates
            raw_points_set[0] = raw_points[0];
            int j = 0;
            for (int i = 1; i < raw_points.length ; i++) {
                if (raw_points_set[j] != raw_points[i]) {
                    j++;
                    raw_points_set[j] = raw_points[i];
                }

            }
        }
        int ct = 0;
        boolean ordering_flag=true;
        while (it.hasNext()) {
            int iterval = it.next().x[0];
            //System.out.println(iterval+" - "+raw_points_set[ct]);
            if(raw_points_set[ct] != iterval)
                ordering_flag=false;
            ct++;
        }
        System.out.println("Size is " + Integer.valueOf(tree.size()) + " & iterator count is " + Integer.valueOf(ct));
        check_size = Integer.valueOf(tree.size()).equals(Integer.valueOf(ct));
        if (points.get(0).x.length > 1)
        {
            if (check_size)
                System.out.println("The number of datapoints encountered by the iterator is equal to the points in the tree. Correct");
            else
                System.out.println("The number of datapoints encountered by the iterator is not equal to the points in the tree. Incorrect");

        }
        if (points.get(0).x.length==1)
        {
            if(ordering_flag && check_size)
                System.out.println("The ordering and the number of datapoints encountered by the iterator is correct. ");
            if (op != Option.RANDOM) {
                System.out.println("For a more robust check of the ordering keep the option RANDOM.");
            }
            else if (!check_size)
                System.out.println("The number of datapoints encountered by the iterator does not match with the actual count. Incorrect");
            else if (check_size && !ordering_flag)
                System.out.println("The number of datapoints encountered by the iterator is equal to the points in the tree, but " +
                        "the ordering of datapoints encountered by the iterator is incorrect.");

        }

    }

    public static void printTreeParameters(KDTree tree) {
        System.out.println("*****Tests for tree parameters:******");
        System.out.println( "Number of nodes in tree is " + tree.countNodes());
        System.out.println( "Number of leaves in tree is " + tree.size() );
        System.out.println( "Height of tree is " + tree.height() );
        System.out.print("(For reference, the height of a balanced tree with "+ tree.size() + " leaves in general should be around (best case): ");
        System.out.printf("%.3f )\n",(Math.log(tree.size())/Math.log(2)));
        System.out.println("I.e. in the order of log(n).");
        System.out.println( "Mean depth of leaves is " + Double.valueOf( tree.meanDepth()));


    }

    public static void main(String args[]) throws Exception {
        int[] q;
        long s_time, e_time;
        float t_time;
        ArrayList<Datum> points = null;
        KDTree tree;


        if (printReadme) {
            System.out.println("PLEASE READ: \n"
                    + "1) We include tests for the 1D, 2D, and 3D cases. For each dimension \n"
                    + "   you can also select UNIFORM, SHAPE, or RANDOM to control the distribution of \n"
                    + "   points. You must modify the appropriate variables in the tester to switch between \n"
                    + "   the various tests. \n"
                    + "2) There is a limit on the amount of time you can take to construct your \n"
                    + "   tree (choose the 'RANDOM' option to verify that you are constructing your tree efficiently). \n"
                    + "3) For a tree of sufficent size (# Datum >= 50000), your KDNode.nearestPointInNode \n"
                    + "   must (on average) beat a brute force approach (choose the 'RANDOM' option to verify that your \n"
                    + "   KDNode.nearestPointInNode implimentation is sufficently fast). Note that, in general, the \n"
                    + "   more Datums a KDTree constains, the greater the speedup. Brute force nearest point search should \n"
                    + "   scale with O(n), whereas KDTree nearest point search should scale with O(log(n)). For reference, \n "
                    + "   in a tree with 100000 Datums, you should expect to see a speedup of anywhere from 20x - 2000x \n"
                    + "   for any given query (over brute force).\n"
                    + "4) Although you are free to create additional fields / methods in the starter code, \n"
                    + "   you must update the value of the provided fields appropriatly (for instance, splitDim / splitValue \n"
                    + "   should be set by your code if a given KDNode is not a leaf). \n"
                    + "5) Many of the test cases in the grader will use data with more than 3 dimensions. \n"
                    + "   We encourage you to read and understand the tester code and make your own tests to verify your solution.\n");
        } else {
            System.out.println("PLEASE READ: DISABLED");
        }

        System.out.println("Current test parameters:\n"
                + "Number of dimensions: "+numdimensions+"\n"
                + "Data distribution: "+op+"\n");
        switch(numdimensions) { // dimension
            case 1: // Test for 1D points

                //  data points
                switch(op) {
                    case UNIFORM:
                        points = makeDataPoints1D(-20000, 20000, 10);
                        tree = new KDTree(points);
                        break;

                    case RANDOM:
                        int[] lows = {-200000};
                        int[] highs = {200000};
                        points = makeDataPointsRandom(lows, highs, 100000, 0, allow_duplicates);
                        tree = new KDTree(points);

                        System.out.println("Constructing "+constructorTries+" tree(s)... (Should only take a few hundred milliseconds to construct a tree containing 100000 Datums)");
                        float avgTime = 0;
                        for (int i=0; i<constructorTries; i++) {
                            points = makeDataPointsRandom(lows, highs, 100000, 0, allow_duplicates);
                            s_time = System.nanoTime();
                            tree = new KDTree(points);
                            e_time = System.nanoTime();
                            t_time = ((float)(e_time-s_time)) / 1000000; // Convert from nanoseconds to milliseconds
                            avgTime = (avgTime*i + t_time)/(i+1);
                        }
                        t_time = avgTime;
                        System.out.println("Avg time required to construct "+constructorTries+" tree(s): "+t_time+" milliseconds");
                        if (t_time >= 600 && t_time < 1200) {
                            System.out.println("WARNING: Tree construction time is borderline too slow (maximum tree construction time is only a few thousand milliseconds)\n");
                        } else if (t_time >= 1200) {
                            System.out.println("WARNING: Tree construction time is much too slow (maximum tree construction time is only a few thousand milliseconds)\n");
                        } else {
                            System.out.println("Tree construction time seems ok.");
                        }
                        System.out.println();
                        break;

                    default:
                        return;
                }

                if (testParameters) {
                    printTreeParameters(tree);
                } else {
                    System.out.println("*****Tests for tree parameters: DISABLED******");
                }

                if (testValidity) {
                    System.out.println("*****Tests for tree validity:******");
                    if (printNote) {
                        System.out.println("Note : For a valid tree, each internal node should have exactly 2 children with no datapoints stored in it and\n " +
                                "each leaf should have 0 child with exactly one datapoint stored in it.");
                    }
                    testStructure(tree);
                } else {
                    System.out.println("*****Tests for tree validity: DISABLED******");
                }

                //  query points
                int[] qList_1 =  {-15000, -10000, -5000, -2500, 0, 2500, 5000, 10000, 15000};

                // test query

                if (testNNSearch) {
                    System.out.println("*****Tests for KDNode.nearestPointInNode:*****");
                    q = new int[1];
                    for (int i = 0; i < qList_1.length; i++) {
                        q[0] = qList_1[i];
                        testQuery(q, points, tree);
                    }
                } else {
                    System.out.println("*****Tests for KDNode.nearestPointInNode: DISABLED*****");
                }

                //testing the iterator
                if (testIteration && points.get(0).x.length == 1) {
                    System.out.println("******Testing for the iterator:******");
                    testIterator(tree, points);
                } else {
                    System.out.println("******Testing for the iterator: DISABLED******");
                }
                break;

            case 2: // Test for 2D points

                // data points
                switch(op) {
                    case UNIFORM:
                        points = makeDataPoints2D_uniform(1000, 100);
                        tree = new KDTree(points);
                        break;

                    case SHAPE:
                        points = makeDataPoints2D_circle(30);
                        tree = new KDTree(points);
                        break;

                    case RANDOM:
                        int[] lows = {-500, -500};
                        int[] highs = {500, 500};
                        points = makeDataPointsRandom(lows, highs, 100000, 1, allow_duplicates);
                        tree = new KDTree(points);

                        System.out.println("Constructing "+constructorTries+" tree(s)... (Should only take a few hundred milliseconds to construct a tree containing 100000 Datums)");
                        float avgTime = 0;
                        for (int i=0; i<constructorTries; i++) {
                            points = makeDataPointsRandom(lows, highs, 100000, 1, allow_duplicates);
                            s_time = System.nanoTime();
                            tree = new KDTree(points);
                            e_time = System.nanoTime();
                            t_time = ((float)(e_time-s_time)) / 1000000; // Convert from nanoseconds to milliseconds
                            avgTime = (avgTime*i + t_time)/(i+1);
                        }
                        t_time = avgTime;
                        System.out.println("Avg time required to construct "+constructorTries+" tree(s): "+t_time+" milliseconds");
                        if (t_time >= 600 && t_time < 1200) {
                            System.out.println("WARNING: Tree construction time is borderline too slow (maximum tree construction time is only a few thousand milliseconds)\n");
                        } else if (t_time >= 1200) {
                            System.out.println("WARNING: Tree construction time is much too slow (maximum tree construction time is only a few thousand milliseconds)\n");
                        } else {
                            System.out.println("Tree construction time seems ok.");
                        }
                        System.out.println();
                        break;

                    default:
                        return;
                }

                if (testParameters) {
                    printTreeParameters(tree);
                } else {
                    System.out.println("*****Tests for tree parameters: DISABLED******");
                }

                if (testValidity) {
                    System.out.println("*****Tests for tree validity:******");
                    if (printNote) {
                        System.out.println("Note : For a valid tree, each internal node should have exactly 2 children with no datapoints stored in it and\n " +
                                "each leaf should have 0 child with exactly one datapoint stored in it.");
                    }
                    testStructure(tree);
                } else {
                    System.out.println("*****Tests for tree validity: DISABLED******");
                }

                // query points
                int[] qList_2[] =  {{-400,200}, {234, 536}, {42, 0}, {333, 101}, {-222, 5}, {5, -432}, {123, 56}};

                // test query
                if (testNNSearch) {
                    System.out.println("*****Tests for KDNode.nearestPointInNode:*****");
                    q = new int[2];
                    for (int i = 0; i < qList_2.length; i++) {
                        q[0] = qList_2[i][0];
                        q[1] = qList_2[i][1];
                        testQuery(q,points, tree);
                    }
                } else {
                    System.out.println("*****Tests for KDNode.nearestPointInNode: DISABLED*****");
                }

                //testing the iterator
                if (testIteration && points.get(0).x.length == 1) {
                    System.out.println("******Testing for the iterator : ******");
                    testIterator(tree, points);
                } else {
                    System.out.println("******Testing for the iterator: DISABLED******");
                }
                break;

            case 3: // 3D points

                // data points
                switch(op) {
                    case SHAPE:
                        points = makeDataPoints3D_sphere(50);
                        tree = new KDTree(points);
                        break;

                    case RANDOM:
                        int[] lows = {-50, -50, -50};
                        int[] highs = {50, 50, 50};
                        points = makeDataPointsRandom(lows, highs, 100000, 2, allow_duplicates);
                        tree = new KDTree(points);

                        System.out.println("Constructing "+constructorTries+" tree(s)... (Should only take a few hundred milliseconds to construct a tree containing 100000 Datums)");
                        float avgTime = 0;
                        for (int i=0; i<constructorTries; i++) {
                            points = makeDataPointsRandom(lows, highs, 100000, 2, allow_duplicates);
                            s_time = System.nanoTime();
                            tree = new KDTree(points);
                            e_time = System.nanoTime();
                            t_time = ((float)(e_time-s_time)) / 1000000; // Convert from nanoseconds to milliseconds
                            avgTime = (avgTime*i + t_time)/(i+1);
                        }
                        t_time = avgTime;
                        System.out.println("Avg time required to construct "+constructorTries+" tree(s): "+t_time+" milliseconds");
                        if (t_time >= 600 && t_time < 1200) {
                            System.out.println("WARNING: Tree construction time is borderline too slow (maximum tree construction time is only a few thousand milliseconds)\n");
                        } else if (t_time >= 1200) {
                            System.out.println("WARNING: Tree construction time is much too slow (maximum tree construction time is only a few thousand milliseconds)\n");
                        } else {
                            System.out.println("Tree construction time seems .");
                        }
                        System.out.println();
                        break;

                    default:
                        return;
                }

                if (testParameters) {
                    printTreeParameters(tree);
                } else {
                    System.out.println("*****Tests for tree parameters: DISABLED******");
                }

                if (testValidity) {
                    System.out.println("*****Tests for tree validity:******");
                    if (printNote) {
                        System.out.println("Note : For a valid tree, each internal node should have exactly 2 children with no datapoints stored in it and\n " +
                                "each leaf should have 0 child with exactly one datapoint stored in it.");
                    }
                    testStructure(tree);
                } else {
                    System.out.println("*****Tests for tree validity: DISABLED******");
                }

                //  query points
                if (testNNSearch) {
                    q = new int[3];
                    for (int x1= -40; x1 <= 40; x1 += 40) { // make a unit 3x3 cube of query points
                        for (int x2= -40; x2 <= 40; x2 += 40) {
                            for (int x3= -40; x3 <= 40; x3 += 40) {
                                q[0] = x1;
                                q[1] = x2;
                                q[2] = x3;
                                testQuery(q, points, tree);
                            }
                        }
                    }
                } else {
                    System.out.println("*****Tests for KDNode.nearestPointInNode: DISABLED*****");
                }

                //testing the iterator
                if (testIteration && points.get(0).x.length == 1) {
                    System.out.println("******Testing for the iterator : ******");
                    testIterator(tree, points);
                } else {
                    System.out.println("******Testing for the iterator: DISABLED******");
                }
                break;

            case 4:
                break;



        }
    }
}