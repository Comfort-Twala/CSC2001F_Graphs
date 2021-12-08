import java.util.Hashtable;
import java.util.Scanner;

/**
 * SimulatorOne class that accepts as input a file containing data on roads,
 * locations and delivery requests. The program will then, for each delivery,
 * calculate and output details of the lowest-cost trip to service that delivery.
 * 
 * @author Comfort Twala - TWLCOM001
 * @version 1.0
 */
public class SimulatorOne {
    // Data Arrays
    static Hashtable<Integer, int[][]> nodes;                     // <source node>: <destination node> <weight>
    static int[] driverNodes;                                     // [<driver start node> <driver start node> <driver start node>] 
    static int[][] deliveryRequests;                              // [[<source node> <destination node>] [<source node> <destination node>]]
    static boolean possible;                                      // boolean to keep track of posible deliveries
    static double[] totalCost;                                    // Costs of all the driver paths added together
    static double[][] pathCosts;                                  // Costs of individual paths 
    static String[][] paths;                                      // Paths to be taken to make successfull delivery
    static Graph graph = new Graph();
    static String nodePath;
    /**
     * Main program driver method
     * @param args
     */
    public static void main(String[] args) {
        // User input and data arrays population
        Scanner input = new Scanner(System.in);
       
        int n = input.nextInt();
        String[][] nodeValues = new String[n][];
        String[] n_values; 
        String vals;
        input.nextLine();
        for (int i = 0; i < n; i++) {
            vals = input.nextLine();
            n_values = vals.split(" ");
            nodeValues[i] = n_values;
        }

        int numDrivers = input.nextInt();
        input.nextLine();
        String driversNum = input.nextLine();
        int requests = input.nextInt();
        input.nextLine();
        String info = input.nextLine();
        input.close();

        populateNodes(nodeValues);
        populateDrivers(driversNum, numDrivers);
        populateRequests(requests, info);

        // Logic Loop
        totalCost = new double[driverNodes.length];                  // Costs of all the driver paths added together
        pathCosts = new double[driverNodes.length][3];               // Costs of individual paths 
        paths = new String[driverNodes.length][3];                   // Paths to be taken to make successfull delivery
        
        // looping over nodes and population graph
        nodes.entrySet().forEach(node -> {
            String key = Integer.toString(node.getKey());
            for (int[] n_val: node.getValue()){
                graph.addEdge(key, Integer.toString(n_val[0]), n_val[1]);
            }
        });

        // loop for the amount of requests received
        // request = [<source node> <delivery node>]
        for (int[] request : deliveryRequests){
            possible = true;

            int driver = fastestDriver(graph, request);
            // output
            System.out.println("client " + request[0] + " " + request[1]);
            if (possible){
                results(driver, request);
            } else {
                System.out.println("cannot be helped");
            }
        }
    }

    /**
     * Method to request user input n times for data to 
     * populate the nodes Hashtable
     * 
     * @param n number of nodes
     */
    public static void populateNodes(String[][] n_values){
        nodes = new Hashtable<Integer, int[][]>();
        for (int i = 0; i < n_values.length; i++){
            int source = Integer.parseInt(n_values[i][0]);
            // <des><weight> combination can occur multiple times
            // <des> is odd index, <weight> is even index

            int times = (n_values[i].length - 1) / 2;
            int[][] values;
            if (times > 0){
                values = new int[times][2];
                // values = [[2 5], [3 7]]
                int valIndex = 1;
                for (int index = 0; index < times; index++){
                    for (int index2 = 0; index2 < 2; index2++){
                        values[index][index2] = Integer.parseInt(n_values[i][valIndex]);
                        valIndex++;
                    }
                }
            } else {
                values = null;
            }
            try {
                nodes.put(source, values);
            } catch (NullPointerException e) {
                ;
            }
        }
    }

    /**
     * Method to populate driverNodes array with drivers starting nodes
     * received from input.
     * 
     * @param numDrivers number of drivers
     */
    public static void populateDrivers(String driversNum, int numDrivers) {
        driverNodes = new int[numDrivers];
        String[] drivers = driversNum.split(" ");
        for (int i = 0; i < numDrivers; i++){
            driverNodes[i] = Integer.parseInt(drivers[i]);
        }
    }

    /**
     * Method to populate deliveryRequests array with source nodes and delivery nodes
     * 
     * @param requests number of requests
     */
    public static void populateRequests(int requests, String info) {
        deliveryRequests = new int[requests][2];
        String[] requestInfo = info.split(" ");
        int data = 0;
        for (int index = 0; index < requests; index++) {
            for (int node = 0; node < 2; node++) {
                deliveryRequests[index][node] = Integer.parseInt(requestInfo[data]);
                data++;
            }
        }
    }
    
    /**
     * Method to output the result outcome of the delivery if it were successfull
     */
    public static void results(int driver, int[] request) {
        String[] from = {"truck", "pickup", "dropoff"};
        for (String path : from){
            System.out.print(path);
            switch (path){
                case "truck":
                    System.out.println(" " + driver);
                    break;
                case "pickup":
                    System.out.println(" " + request[0]);
                    break;
                case "dropoff":
                    System.out.println(" " + request[1]);
                    break;
            }
            printResult(path, driver);
        }
    }

    /**
     * Method to print the results path or multiple solutions cost of path
     * 
     * @param path path taken
     * @param driver that took the path
     */
    private static void printResult(String path, int driver){
        int index = 0;
        for (int i = 0; i < driverNodes.length; i++){
            if (driverNodes[i] == driver){
                index = i;
            }
        } 

        String res;
        switch (path){
            case "truck":
                res = (paths[index][0].equals("multiple")) ? "multiple solutions cost " + (int)pathCosts[index][0] : paths[index][0];
                System.out.println(res);
                break;
            case "pickup":
                res = (paths[index][1].equals("multiple")) ? "multiple solutions cost " + (int)pathCosts[index][1] : paths[index][1];
                System.out.println(res);
                break;
            case "dropoff":
                res = (paths[index][2].equals("multiple")) ? "multiple solutions cost " + (int)pathCosts[index][2] : paths[index][2];
                System.out.println(res);
                break;
        }
    }

    /** 
     * Method to loop through all drivers and search for the one
     * with the shortest path using given start- and end node 
     * 
     * @param request [<source node> <destination node>]
     */
    public static int fastestDriver(Graph graph, int[] request) {
        // Compare drivers and determine the fastest one
        int driverIndex = 0;
        for (int driver: driverNodes){
            double cost = 0;
            double[] pathCost = new double[3];
            String[] path = new String[3];
            
            // performing dijkstra's algoritm
            try {
                String finalPath;
                boolean m;
                // driver's node to pickup node
                graph.dijkstra(Integer.toString(driver));
                Vertex dp = getVertex(graph, Integer.toString(request[0]));
                cost += dp.dist;
                pathCost[0] = dp.dist;
                nodePath = "";
                getPath(dp);
                finalPath = nodePath;
                nodePath = "";
                m = multiple(finalPath, graph, Integer.toString(driver), Integer.toString(request[0]));
                path[0] = (m) ? "multiple" : finalPath;
                // pickup to dropoff
                graph.dijkstra(Integer.toString(request[0]));
                dp = getVertex(graph, Integer.toString(request[1]));
                cost += dp.dist;
                pathCost[1] = dp.dist;
                nodePath = "";
                getPath(dp); 
                finalPath = nodePath;
                nodePath = "";
                m = multiple(finalPath, graph, Integer.toString(request[0]), Integer.toString(request[1]));
                path[1] = (m) ? "multiple" : finalPath;
                // dropoff to driver's home
                graph.dijkstra(Integer.toString(request[1]));
                dp = getVertex(graph, Integer.toString(driver));
                cost += dp.dist;
                pathCost[2] = dp.dist;
                nodePath = "";
                getPath(dp); 
                finalPath = nodePath;
                nodePath = "";
                m = multiple(finalPath, graph, Integer.toString(request[1]), Integer.toString(driver));
                path[2] = (m) ? "multiple" : finalPath;

                // updating lists
                totalCost[driverIndex] = cost;
                pathCosts[driverIndex] = pathCost;
                paths[driverIndex] = path;

            } catch (Exception e) {
                possible = false;
                totalCost[driverIndex] = 0;
                pathCost[driverIndex] = 0;
                paths[driverIndex] = null;
            }
            
            driverIndex++;
        }

        return fastest(totalCost);
    }

    /**
     * Method to return a vertex when given the destination 
     * 
     * @param g graph 
     * @param destination node to get vertex of
     * @return vertex
     */
    private static Vertex getVertex(Graph g, String destination) {
        Vertex vertex = g.getVertex(destination);
        if (vertex == null || vertex.dist == Graph.INFINITY){
            possible = false;
            return null;
        } else {
            return vertex;
        }
    }

    /**
     * Method to get the path of the vertex given
     * 
     * @param vertex which path is retrieved
     */
    private static void getPath(Vertex vertex){
        String path = "";
        if (vertex.prev != null){
            getPath(vertex.prev);
            nodePath += " ";
        }
        path += vertex.name;
        nodePath += path;
    }

    /**
     * Method to determine the driver which the shortest path
     * 
     * @param t list of driver's total distance
     * @return driver with the least distance
     */
    private static int fastest(double[] t) {
        double min = t[0];   // start with the first value
        int minPlace = 0;
        int driver = driverNodes[0];
        for (int i=1; i<t.length; i++) {
            if (t[i] < min) {
                min = t[i];
                minPlace = i;   // new maximum
                driver = driverNodes[minPlace];
            } else if (t[i] == min){
                driver = minimum(driverNodes[i], driverNodes[minPlace]);
            }
        }
        return driver;
    }

    /**
     * Method to return the smaller int 
     * @param a first int
     * @param b second int
     * @return smaller int
     */
    private static int minimum(int a, int b) {
        return (a < b) ? a : b;
    }

    /**
     * Method to determine whether driver had alternative routes using dijkstra's algo
     * 
     * @param s Current shortest path
     * @param g Graph to check
     * @param a node to check from
     * @param b destination node
     * @return true if there are multiple paths
     */
    private static boolean multiple(String s, Graph g, String a, String b) {
        g.dijkstra2(a);
        Vertex dp = getVertex(g, b);
        getPath(dp);
        if (!(nodePath.equals(s))){
            return true;
        } 
        return false;
    }
}