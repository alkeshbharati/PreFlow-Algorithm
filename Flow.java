package ASB180015;
import ASB180015.Graph.*;
import java.util.*;

public class Flow {
    Vertex s,t;
    HashMap<Edge, Integer> capacity;
    HashMap<Edge, Integer> flow;
    Graph g;
    LinkedList<Vertex> list;
    int excess[];
    int height[];
    HashMap<Vertex, Integer>  distance;
    Set<Vertex> tSet;

    public Flow(Graph g, Vertex s, Vertex t, HashMap<Edge, Integer> capacity) {
        this.g = g;
        this.s = s;
        this.t = t;
        this.capacity = capacity;
        distance= new HashMap<>();
        flow = new HashMap<>();
        excess = new int[g.n];
        height = new int[g.n];
        list = new LinkedList<>();
        tSet = new HashSet<>();

    }

    // Return max flow found. Use either relabel to front or FIFO.
    public int preflowPush() {

        int maxflow=0;
        initialize();
        while( list.size() != 0 )
        {
            Vertex  u = list.remove();
            discharge(u);
            if ( excess[u.getIndex()] > 0 )
            {
                relabel(u);
            }
        }
        maxflow = excess[t.getIndex()];
        //System.out.println("Excess: " + Arrays.toString(excess));
        //System.out.println("Height: " + Arrays.toString(height));
        //System.out.println("Flow: " + flow.values());
        //System.out.println("Capacity: " + capacity.values());
        return maxflow;
    }

    /**
     * function initializes the flow along edges, height of nodes and excess at each node
     */
    private void initialize() {
        for(Edge e : g.getEdgeArray()){
            flow.put(e, 0);
        }
        Queue<Vertex> queue = new LinkedList<>();
        for ( Vertex u :g )
        {
            distance.put(u, Integer.MAX_VALUE);
        }

        queue.add(t);
        distance.put(t, 0);
        while(!queue.isEmpty()){
            Vertex n = queue.remove();
            Iterable<Edge> result=g.inEdges(n);
            Iterator<Edge> itr = result.iterator();
            while(itr.hasNext()) {
                Vertex v = itr.next().otherEnd(n);
                if ( distance.get(v) == Integer.MAX_VALUE )
                {
                    distance.put(v,distance.get(n)+1);
                    queue.add(v);
                }
            }
        }

        for(Vertex u: g){
            excess[u.getIndex()] = 0;
            height[u.getIndex()] = distance.get(u);
        }

        height[s.getIndex()] = g.size();

        for(Edge e: g.adj(s).outEdges) {
            int c = capacity(e);
            flow.put(e, c);
            excess[s.getIndex()] = excess[s.getIndex()] - c;
            excess[e.otherEnd(s).getIndex()] = excess[e.otherEnd(s).getIndex()] + c;
            list.add(e.otherEnd(s));
        }
    }

    /**
     * function transfer the excess out of a given vertex
     * @param u - current vertex for which excess has to be transferred
     */
    private void discharge(Vertex u) {

        for(Edge e : g.adj(u).outEdges) {
            Vertex v = e.otherEnd(u);
            if( height[u.getIndex()] == (height[v.getIndex()]+1 )  ) {
                push(u,v,e);
            }
        }
        for(Edge e : g.adj(u).inEdges) {
            Vertex v = e.fromVertex();
            if(height[u.getIndex()] == (height[v.getIndex()]+1 ) ) {
                push(u,v,e);
            }
        }
    }

    /**
     * functions push/transfer flow from u along the edge e
     * @param u - from node
     * @param v - to node
     * @param e - directed edge along which flow is sent
     */
    private void push( Vertex u, Vertex v , Edge e) {
        int delta;
        if(e.fromVertex().equals(u)) {
            //  for(Edge z :g.incident(u)){
            delta = Math.min( excess[u.getIndex()] , (capacity(e) - flow(e)));
            // }
            flow.put(e, flow(e) + delta);
        }
        else {
            delta = Math.min( excess[u.getIndex()] , flow(e));
            flow.put(e, flow(e) - delta);
        }

        excess[u.getIndex()] = excess[u.getIndex()] - delta;
        excess[v.getIndex()] = excess[v.getIndex()] + delta;

        if(!list.contains((v)) && v != s && v!= t)
        {
            list.add(v);
        }

        if(excess[u.getIndex()] == 0)
            return;
    }


    /**
     * function updates the height of the vertex if discharge is done
     * @param u - vertex
     */
    private void relabel(Vertex u) {
        int minheight = Integer.MAX_VALUE;
        Vertex temp = u;
    /*    for(Edge e:g.outEdges(u)){
            Vertex v=e.otherEnd(u);
            if(height[v.getIndex()]<minheight){
                minheight=height[v.getIndex()];
                x=v;
            }
        }*/
        for(Edge e:g.inEdges(u)){
            Vertex v = e.otherEnd(u);
            if(height[v.getIndex()]<minheight && ((height[v.getIndex()] - minheight) == 1)){
                minheight = height[v.getIndex()];
                temp = v;
            }
        }
        height[u.getIndex()] = height[temp.getIndex()] + 1;
        list.add(u);
    }
    /**
     * function returns the flow through an edge
     * @param e - edge
     * @return - flow through the edge if it present in the graph, otherwise flow through it is returned as 0
     */
    public int flow(Edge e) {

        if (flow.containsKey(e))
        {
            return flow.get(e);
        }else{
            return 0;
        }

    }

    /**
     * function return the capacity of an edge
     * @param e - edge
     * @return - capacity of the edge if it present in the graph, otherwise capacity is returned as 0
     */
    public int capacity(Edge e) {

        if (capacity.containsKey(e))
        {
            return capacity.get(e);
        }else{
            return 0;
        }

    }


    /**
     * function computes the S side of min cut set
     * @return - returns Hashset of S
     */
    public Set<Vertex> minCutS() {
        Set<Vertex> sSet = new HashSet<>();
        boolean[] visited = new boolean[g.size()];
        Queue<Vertex> q = new LinkedList<Vertex>();
        q.add(s);
        sSet.add(s);
        while (!q.isEmpty()) {
            Vertex u = q.poll();
            for(Edge e : g.adj(u).outEdges) {
                Vertex v = e.otherEnd(u);
                if( capacity(e) - flow(e) > 0  && visited[u.getIndex()]== false ) {
                    sSet.add(v);
                    visited[u.getIndex()] = true;
                    q.add(v);
                }
            }
            for(Edge e : g.adj(u).inEdges) {
                Vertex v = e.fromVertex();
                if( flow(e) > 0  && visited[u.getIndex()]== false) {
                    sSet.add(v);
                    visited[u.getIndex()] = true;
                    q.add(v);
                }
            }
         /*   for(Edge e : g.incident(u)) {
                Vertex v = e.otherEnd(u);
                if( e.fromVertex().equals(u) && capacity(e) - flow(e) > 0 && flowCheck(e, u) ) {
                    set.add(v);
                    visited[v.getIndex()] = true;
                    q.add(v);
                }

            }*/
        }
        for( Vertex u : g)
        {
            if(!(visited[u.getIndex()]))
            {
                tSet.add(u);
            }
        }
        return sSet;
    }

    /* After maxflow has been computed, this method can be called to
       get the "T"-side of the min-cut found by the algorithm
    */
    /**
     * function compute the T side of min cut found by the algorithm
     * @return - returns Hashset of T
     */
    public Set<Vertex> minCutT() {
        return tSet;
    }
}
