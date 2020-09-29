package wzy.jsp.simplest.demo.domain.algorithm.represent;

import wzy.jsp.simplest.demo.domain.communication.Solution;

public class GAParametersWithDisjunctiveGraph {
    private DisjunctiveGraphModel disjunctiveGraphModel;
    //Chromosome size
    private int popSize=500;
    //Crossover probability
    private double Pc=0.90;
    //Mutation probability
    private double Pm=0.05;
    //Genetic num
    private int gN=1000;
    //Pressure adjust factor
    private double a=0.9;
    private double zeta=10;
    //Tournament Selection parameters
    private Integer numEachTime=100;

    private Solution solution;

    public GAParametersWithDisjunctiveGraph(DisjunctiveGraphModel disjunctiveGraphModel, Solution solution){
        this.disjunctiveGraphModel = disjunctiveGraphModel;
        this.solution=solution;
    }

    public GAParametersWithDisjunctiveGraph(DisjunctiveGraphModel disjunctiveGraphModel, Solution solution,
                                                int popSize, double Pc, double Pm, int gN, double a,double zeta, Integer numEachTime){
        this.disjunctiveGraphModel = disjunctiveGraphModel;
        this.solution=solution;
        this.popSize=popSize;
        this.Pc=Pc;
        this.Pm=Pm;
        this.gN=gN;
        this.a=a;
        this.zeta=zeta;
        this.numEachTime=numEachTime;
    }

    public DisjunctiveGraphModel getDisjunctiveGraphModel() {
        return disjunctiveGraphModel;
    }

    public int getPopSize() {
        return popSize;
    }

    public double getPc() {
        return Pc;
    }

    public double getPm() {
        return Pm;
    }

    public int getgN() {
        return gN;
    }

    public Solution getSolution() {
        return solution;
    }

    public double getA() {
        return a;
    }

    public double getZeta() {
        return zeta;
    }

    public Integer getNumEachTime() {
        return numEachTime;
    }
}
