package wzy.jsp.simplest.demo.domain.algorithm.represent;

import wzy.jsp.simplest.demo.domain.communication.Solution;

public class SAParametersWithDisjunctiveGraph {
    private DisjunctiveGraphModel disjunctiveGraphModel;

    //Loop num in annealing
    private int loopN=1000;
    //Min temperature to terminate
    private double minTemperature=0.01;
    //Init temperature
    private double temperature0=10000;
    //Temperature cooling coefficient
    private double a=0.95;

    private Solution solution;

    public SAParametersWithDisjunctiveGraph(DisjunctiveGraphModel disjunctiveGraphModel, Solution solution){
        this.disjunctiveGraphModel = disjunctiveGraphModel;
        this.solution=solution;
    }

    public SAParametersWithDisjunctiveGraph(DisjunctiveGraphModel disjunctiveGraphModel, Solution solution,
                                                  int loopN, double minTemperature, double temperature0, double a){
        this.disjunctiveGraphModel = disjunctiveGraphModel;
        this.solution=solution;
        this.loopN=loopN;
        this.minTemperature=minTemperature;
        this.temperature0=temperature0;
        this.a=a;
    }

    public DisjunctiveGraphModel getDisjunctiveGraphModel() {
        return disjunctiveGraphModel;
    }

    public int getLoopN() {
        return loopN;
    }

    public double getMinTemperature() {
        return minTemperature;
    }

    public double getTemperature0() {
        return temperature0;
    }

    public double getA() {
        return a;
    }

    public Solution getSolution() {
        return solution;
    }
}
