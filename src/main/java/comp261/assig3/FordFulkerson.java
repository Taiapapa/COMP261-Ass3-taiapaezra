package comp261.assig3;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.Queue;

import javafx.util.Pair;

// helper class that does not need local memory

public class FordFulkerson {
    // class members
    // Augmentation paths and the corresponding bottleneck flows
    private static ArrayList<Pair<ArrayList<Node>, Double>> augmentationPaths;
    // residual graph
    private static double[][] residualGraph;

    // constructor
    public FordFulkerson() {
        augmentationPaths = null;
        residualGraph = null;
    }

    // find maximum flow value
    public static double calcMaxflows(Graph graph, Node source, Node sink) {
        augmentationPaths = new ArrayList<Pair<ArrayList<Node>, Double>>();

        double flow = 0;

        int rows = graph.getAdjacencyMatrix().length;
        int cols = graph.getAdjacencyMatrix()[0].length;

        residualGraph = new double[rows][cols];
        // Initialise residual graph as adjacency matrix
        for (int i = 0; i < rows; i++) {
            for (int j = 0; j < cols; j++) {
                residualGraph[i][j] = graph.getAdjacencyMatrix()[i][j];
            }
        }

        Node parent[] = new Node[graph.getNodeList().size()];
        double foundFlow = 0;

        // Find augmentation path and flow, update residual graph with each path
        while ((foundFlow = bfs(residualGraph, source, sink, parent)) != 0) {
            flow = flow + foundFlow;
            int current = sink.getId();
            while (current != source.getId()) {
                int previous = parent[current].getId();
                residualGraph[previous][current] -= foundFlow;
                residualGraph[current][previous] += foundFlow;
                current = previous;
            }
        }

        return flow * graph.getVehicleTypeMulti();
    }

    // TODO:Use BFS to find an augmentation path from s to t
    // add the augmentation path found to the arraylist of augmentation paths
    // return bottleneck flow
    public static double bfs(double[][] residualGraph, Node s, Node t, Node[] parent) {
        for(int i = 0; i < parent.length; i++){
            parent[i] = null;
        }
      
        Queue<Pair<Node, Double>> queue = new LinkedList<Pair<Node, Double>>();
        queue.add(new Pair<Node, Double>(s, Double.MAX_VALUE));

        while (!queue.isEmpty()) {
            Pair<Node, Double> firstPair = queue.remove();
            Node current = firstPair.getKey();
            double flow = firstPair.getValue();
            ArrayList<Node> adjacentNodes = new ArrayList<>();
            adjacentNodes = current.getNeighbours();

            for (Node neighbour : adjacentNodes) {
                if (parent[neighbour.getId()] == null && residualGraph[current.getId()][neighbour.getId()] != 0) {
                    parent[neighbour.getId()] = current;
                    double newFlow = Math.min(flow, residualGraph[current.getId()][neighbour.getId()]);
                    
                    // Found sink, so build ap
                    if (neighbour == t) {
                        flowPath(s, t, parent, newFlow);
                        return newFlow;
                    }
                    queue.add(new Pair<Node, Double>(neighbour, newFlow));
                }
            }
        }
        return 0;
    }

    // TODO: For each flow identified by bfs() build the path from source to sink
    // Add this path to the Array list of augmentation paths: augmentationPaths
    // along with the corresponding flow
    public static void flowPath(Node s, Node t, Node[] parent, double newFlow) {
        if (parent[t.getId()] == null) {
            return;
        }

        ArrayList<Node> augmentationPath = new ArrayList<Node>();

        // TODO: find the augmentation path identified by the graph traversal algorithm
        // and add it to the list of augmentation paths
        Node child = t;
        while (parent[child.getId()] != null) {
            augmentationPath.add(child);
            child = parent[child.getId()];
        }
        augmentationPath.add(s);

        Collections.reverse(augmentationPath);
        augmentationPaths.add(new Pair<ArrayList<Node>, Double>(augmentationPath, newFlow * Main.graph.getVehicleTypeMulti()));
    }

    // getter for augmentation paths
    public static ArrayList<Pair<ArrayList<Node>, Double>> getAugmentationPaths() {
        return augmentationPaths;
    }

    // TODO: find min-cut - as a set of sets and the corresponding cut-capacity
    public static Pair<Pair<HashSet<Node>, HashSet<Node>>, Double> minCut_s_t(Graph graph, Node source, Node sink) {
        calcMaxflows(graph, source, sink);

        int rows = graph.getAdjacencyMatrix().length;
        int cols = graph.getAdjacencyMatrix()[0].length;

        HashSet setS = new HashSet<Node>();
        HashSet setT = new HashSet<Node>();

        setS.add(source);
        setT.add(sink);

        // Find nodes connected to source using dfs
        dfs(graph, source);
        for (int i = 0; i < rows; i++) {
            if (graph.findNode(i).isVisited()) {
                setS.add(graph.findNode(i));
            }
        }

        // Add unvisited nodes to set T
        for (int i = 0; i < graph.getAdjacencyMatrix().length; i++) {
            if (!graph.findNode(i).isVisited()) {
                setT.add(graph.findNode(i));
            }
        }

        // Cut capacity equals the sum of the edge weights from set S to set T
        double minCut = 0;
        for (int i = 0; i < rows; i++) {
            for (int j = 0; j < cols; j++) {
                if (graph.getAdjacencyMatrix()[i][j] > 0 && graph.findNode(i).isVisited() && !graph.findNode(j).isVisited()) {
                    minCut += graph.getAdjacencyMatrix()[i][j] * graph.getVehicleTypeMulti();
                }
            }
        }

        Pair<HashSet<Node>, HashSet<Node>> temp = new Pair<HashSet<Node>,HashSet<Node>>(setS, setT);
        Pair<Pair<HashSet<Node>, HashSet<Node>>, Double> minCutwithSets = new Pair<Pair<HashSet<Node>,HashSet<Node>>,Double>(temp, minCut);
        return minCutwithSets;
    }

    // TODO: Use dfs to find set of nodes connected to s
    private static void dfs(Graph graph, Node s) {
        s.setVisited(true);
        
        for (int i = 0; i < graph.getNodeList().size(); i++) {
            if (residualGraph[s.getId()][i] > 0 && !graph.findNode(i).isVisited()) {
                dfs(graph, graph.findNode(i));
            }
        }
    }
}
