package wzy.jsp.simplest.demo.domain.algorithm.represent;

import wzy.jsp.simplest.demo.domain.communication.Solution;

public class GAParametersWithDirectFormulationModel {
    private DirectFormulationModel directFormulationModel;
    //Penalty value for hard constraints violating
    private int I=10000;
    //Chromosome size
    private int popSize=20;
    //Crossover probability
    private double Pc=0.90;
    //Mutation probability
    private double Pm=0.05;
    //Mutation delta value up limit
    private double mutateDeltaUpLimit=5;
    //Genetic num
    private int gN=1000;

    private Solution solution;

    public GAParametersWithDirectFormulationModel(DirectFormulationModel directFormulationModel, Solution solution){
        this.directFormulationModel = directFormulationModel;
        this.solution=solution;
    }

    public GAParametersWithDirectFormulationModel(DirectFormulationModel directFormulationModel, Solution solution,
                                                  int I, int popSize, double Pc, double Pm, double mutateDeltaUpLimit, int gN){
        this.directFormulationModel = directFormulationModel;
        this.solution=solution;
        this.I=I;
        this.popSize=popSize;
        this.Pc=Pc;
        this.Pm=Pm;
        this.mutateDeltaUpLimit=mutateDeltaUpLimit;
        this.gN=gN;
    }

    public DirectFormulationModel getDirectFormulationModel() {
        return directFormulationModel;
    }

    public int getI() {
        return I;
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

    public double getMutateDeltaUpLimit() {
        return mutateDeltaUpLimit;
    }

    public int getgN() {
        return gN;
    }

    public Solution getSolution() {
        return solution;
    }
}
