package wzy.jsp.simplest.demo.domain.algorithm.represent;

import wzy.jsp.simplest.demo.domain.communication.Job;
import wzy.jsp.simplest.demo.domain.communication.Solution;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * This class represents Disjunctive Graph of JSP
 */
public class DisjunctiveGraphModel {

    public List<NodeInDG> nodesInDG;
    public List<ConjunctiveArc> conjunctiveArcs;
    public List<DisjunctiveArc> disjunctiveArcs;

    public Map<String,List<NodeInDG>> nodesPerJob;
    //Permutation representation
    //Key : machine name
    //Value : Node sequence, for example, List[0]=Node_x, List[1]=Node_y,
    //        So Node_x is the first one in this machine, after that , is Node_y
    public Map<String,List<NodeInDG>> nodesPerMachine;


}
