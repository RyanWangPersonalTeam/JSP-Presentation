package wzy.jsp.simplest.demo.domain.algorithm;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import wzy.jsp.simplest.demo.common.IAlgorithmCore;
import wzy.jsp.simplest.demo.common.IMetaHeuristicAlgorithm;
import wzy.jsp.simplest.demo.common.IntermediateSolutionCallback;
import wzy.jsp.simplest.demo.component.AMQPHandler;
import wzy.jsp.simplest.demo.component.DateTimeConverter;
import wzy.jsp.simplest.demo.component.VariableConverter;
import wzy.jsp.simplest.demo.domain.algorithm.represent.GAParametersWithDisjunctiveGraph;
import wzy.jsp.simplest.demo.domain.algorithm.represent.NodeInDG;
import wzy.jsp.simplest.demo.domain.communication.Solution;

import java.text.DecimalFormat;
import java.util.*;

public class GAAlgorithmWithDisjunctiveGraph implements IMetaHeuristicAlgorithm, IAlgorithmCore {
    private GAParametersWithDisjunctiveGraph gaParameters;
    private VariableConverter variableConverter;
    private AMQPHandler amqpHandler;
    private Integer currentBestScore=Integer.MAX_VALUE;//Record the best score (original objective function value, not fitness or combining with M)
    private Map<String, List<NodeInDG>> currentBestPermutation=null;//Record the best permutation
    private Chromosome[] population;
    //private int M; //Use a big number to transfer min-problem to max-problem

    private IntermediateSolutionCallback intermediateSolutionCallback;

    private Logger logger;

    public GAAlgorithmWithDisjunctiveGraph(GAParametersWithDisjunctiveGraph gaParameters,
                                                 VariableConverter variableConverter,
                                                 IntermediateSolutionCallback intermediateSolutionCallback,
                                                 AMQPHandler amqpHandler){
        this.gaParameters=gaParameters;
        this.variableConverter=variableConverter;
        this.intermediateSolutionCallback=intermediateSolutionCallback;
        this.amqpHandler=amqpHandler;

        this.logger= LoggerFactory.getLogger(GAAlgorithmWithDisjunctiveGraph.class);

        //this.M=new DateTimeConverter().CalculateCountValueBetweenDates(this.gaParameters.getSolution().MinTime,this.gaParameters.getSolution().MaxTime)*100;
    }

    @Override
    public Solution Calculate(boolean initialized) throws Exception {
        this.currentBestScore=Integer.MAX_VALUE;
        this.currentBestPermutation=null;
        if(initialized){
            this.HandleIntermediateSolution(this.gaParameters.getSolution(),this.currentBestScore);
        }

        InitPopulation();
        Double zeta=Eval(null);
        Select();
        for(int i=0;i<this.gaParameters.getgN();i++){
            this.logger.info("################### Generation : "+i+" #####################");
            Cross();
            Mutate();
            zeta=Eval(zeta);
            Select();
        }

        this.gaParameters.getDisjunctiveGraphModel().nodesPerMachine=this.currentBestPermutation;
        Solution solvedSolution=this.variableConverter.getSolutionFromDisjunctiveGraph(
                this.gaParameters.getSolution(),
                this.gaParameters.getDisjunctiveGraphModel()
        );
        solvedSolution.FinalResult=true;
        return solvedSolution;
    }

    //Init the population
    public void InitPopulation(){
        this.population=new Chromosome[this.gaParameters.getPopSize()];
        Map<String, List<NodeInDG>> initPermutation=this.gaParameters.getDisjunctiveGraphModel().nodesPerMachine;
        for(int i=0;i<this.gaParameters.getPopSize();i++){
            Map<String, List<NodeInDG>> newPermutation=this.variableConverter.deepCopyPermutation(initPermutation);
            newPermutation=shuffleTaskPermutation(newPermutation);
            Chromosome chromosome=new Chromosome(newPermutation);
            this.population[i]=chromosome;
        }
    }

    //Objective value of each chromosome(The makespan)
    public Double Fun(Chromosome chromosome){
        this.gaParameters.getDisjunctiveGraphModel().nodesPerMachine=chromosome.taskPermutation;
        Solution decodeSolution=this.variableConverter.getSolutionFromDisjunctiveGraph(this.gaParameters.getSolution(),this.gaParameters.getDisjunctiveGraphModel());
        Integer makespan=this.variableConverter.getMakespanFromSolution(decodeSolution);
        chromosome.setOriginalScore(makespan);
        //return (M-makespan);
        return Double.valueOf(makespan);
    }

    //Calculate fitness of each chromosome
    public Double Eval(Double lastZeta){

        //double sum=0;
        for(int i=0;i<this.population.length;i++){
            this.population[i]=this.LocalRepair(this.population[i]);
            this.population[i].setFun(this.Fun(this.population[i]));
            //sum+=this.population[i].getFun();
        }

        //fitness scale
        //F'=F_max-F+zeta
        Double maxFun=this.population[0].getFun();
        Double zeta=0.0;
        if(lastZeta==null){
            zeta=this.gaParameters.getZeta();
        }
        else{
            zeta=lastZeta*this.gaParameters.getA();
        }

        for(int i=1;i<this.population.length;i++){
            if(this.population[i].getFun()>maxFun){
                maxFun=this.population[i].getFun();
            }
        }

        double sum=0;
        for(int i=0;i<this.population.length;i++){
            this.population[i].setFun(maxFun-this.population[i].getFun()+zeta);
            sum+=this.population[i].getFun();
        }


        for(int i=0;i<this.population.length;i++){
            this.population[i].setEval(this.population[i].getFun()/sum);
        }

        return zeta;
    }

    //Cross
    public void Cross(){
        //Select parents
        List<Integer> parentsIndex=new ArrayList<>();
        for(int i=0;i<this.gaParameters.getPopSize();i++){
            double r=new Random().nextDouble();
            if(r<this.gaParameters.getPc()){
                parentsIndex.add(i);
            }
        }
        if(parentsIndex.size()%2!=0){
            parentsIndex.remove(parentsIndex.size()-1);
        }

        int i=0;
        for(i=0;i<parentsIndex.size();i+=2){

            GAAlgorithmWithDisjunctiveGraph.Chromosome parent1=this.population[parentsIndex.get(i)];
            GAAlgorithmWithDisjunctiveGraph.Chromosome parent2=this.population[parentsIndex.get(i+1)];
            Chromosome child1=getUsxxCrossChild(parent1,parent2);
            Chromosome child2=getUsxxCrossChild(parent2,parent1);
            parent1=child1;
            parent2=child2;
        }
    }

    //USXX, parent1 as the base
    public Chromosome getUsxxCrossChild(Chromosome parent1,Chromosome parent2){
        Map<String, List<NodeInDG>> child=new HashMap<>();
        for(Map.Entry<String, List<NodeInDG>> v : parent1.taskPermutation.entrySet()){
            String machineName=v.getKey();
            List<NodeInDG> parent1Permutation=v.getValue();
            List<NodeInDG> childPermutation=new ArrayList<NodeInDG>(Collections.nCopies(parent1Permutation.size(),null));
            int crossIndex1=new Random().nextInt(parent1Permutation.size());
            int crossIndex2=Integer.parseInt(new DecimalFormat("0").format(new Random().nextDouble() * (parent1Permutation.size()-1 - crossIndex1) + crossIndex1));
            //System.out.println("Cross points: from "+crossIndex1+" to "+crossIndex2);
            List<NodeInDG> selectNodes=parent1Permutation.subList(crossIndex1,crossIndex2+1);
            List<NodeInDG> parent2Permutation=parent2.taskPermutation.get(machineName);
            int indexInChild=0;
            int indexInParent2=0;
            for(indexInChild=0;indexInChild<childPermutation.size();indexInChild++){
                if(indexInChild>=crossIndex1 && indexInChild<=crossIndex2){
                    childPermutation.set(indexInChild,parent1Permutation.get(indexInChild));
                }
                else{
                    for(;indexInParent2<parent2Permutation.size();indexInParent2++){
                        boolean exist=false;
                        for(int i=0;i<selectNodes.size();i++){
                            if(selectNodes.get(i).taskIndexs.getValue0()==parent2Permutation.get(indexInParent2).taskIndexs.getValue0()
                            && selectNodes.get(i).taskIndexs.getValue1()==parent2Permutation.get(indexInParent2).taskIndexs.getValue1()){
                                exist=true;
                                break;
                            }
                        }
                        if(!exist){
                            childPermutation.set(indexInChild,parent2Permutation.get(indexInParent2));
                            indexInParent2++;
                            break;
                        }
                    }
                }
            }
            child.put(machineName,childPermutation);
        }
        Chromosome childChromosome=new Chromosome(child);
        return childChromosome;
    }

    //Some tasks from the same job may operate in the same machine, cross and mutation may
    //cause invalid sequence, repair operation is must be done.
    public Chromosome LocalRepair(Chromosome chromosome){
        Map<String, List<NodeInDG>> permutation=chromosome.taskPermutation;
        for(Map.Entry<String, List<NodeInDG>> p : permutation.entrySet()){
            List<NodeInDG> nodesThisMachine=p.getValue();
            for(int i=0;i<nodesThisMachine.size();i++){
                int minTaskIndex=nodesThisMachine.get(i).taskIndexs.getValue1();
                for(int j=i+1;j<nodesThisMachine.size();j++){
                    if(nodesThisMachine.get(j).taskIndexs.getValue0()==nodesThisMachine.get(i).taskIndexs.getValue0()
                    && nodesThisMachine.get(j).taskIndexs.getValue1()<minTaskIndex){
                        NodeInDG temp=nodesThisMachine.get(i);
                        nodesThisMachine.set(i,nodesThisMachine.get(j));
                        nodesThisMachine.set(j,temp);
                        minTaskIndex=nodesThisMachine.get(j).taskIndexs.getValue1();
                    }
                }
            }
            permutation.replace(p.getKey(),nodesThisMachine);
        }
        return chromosome;
    }

    //Mutate
    public void Mutate(){
        //select parents
        List<Integer> parentsIndex=new ArrayList<>();
        for(int i=0;i<this.gaParameters.getPopSize();i++){
            double r=new Random().nextDouble();
            if(r<this.gaParameters.getPm()){
                parentsIndex.add(i);
            }
        }

        for(int i=0;i<parentsIndex.size();i++){
            MutateOneChromosome(this.population[parentsIndex.get(i)]);
        }

    }

    //Do inversion mutation in each machine
    public void MutateOneChromosome(Chromosome chromosome){
        Map<String, List<NodeInDG>> permutations=chromosome.taskPermutation;
        for(Map.Entry<String, List<NodeInDG>> v : permutations.entrySet()){
            int crossIndex1=new Random().nextInt(v.getValue().size());
            int crossIndex2=Integer.parseInt(new DecimalFormat("0").format(new Random().nextDouble() * (v.getValue().size()-1 - crossIndex1) + crossIndex1));
            //System.out.println("Cross points: from "+crossIndex1+" to "+crossIndex2);
            for(int i=crossIndex1,j=crossIndex2;i<j;i++,j--){
                NodeInDG temp=v.getValue().get(i);
                v.getValue().set(i,v.getValue().get(j));
                v.getValue().set(j,temp);
            }
        }
    }

    public void Select() throws Exception {
        int bestIndex=0;
        double bestEval=this.population[0].getEval();
        for(int i=1;i<this.population.length;i++){
            if(this.population[i].getEval()>bestEval){
                bestIndex=i;
                bestEval=this.population[i].getEval();
            }
        }
        if(this.currentBestScore==null || this.currentBestScore>this.population[bestIndex].getOriginalScore()){
            this.currentBestScore=this.population[bestIndex].getOriginalScore();
            this.currentBestPermutation=this.variableConverter.deepCopyPermutation(this.population[bestIndex].taskPermutation);
            this.gaParameters.getDisjunctiveGraphModel().nodesPerMachine=this.currentBestPermutation;
            this.HandleIntermediateSolution(this.gaParameters.getSolution(),this.currentBestScore);
        }
        this.logger.info("Best eval this generation : "+bestEval);
        this.logger.info("Best score this generation : "+(this.population[bestIndex].getOriginalScore()));
        this.logger.info("Current best score : "+this.currentBestScore);

        //Tournament Selection
        Chromosome[] newPopulation=new Chromosome[this.population.length];
        for(int index=0;index<this.population.length;index++){
            Chromosome[] competitors=new Chromosome[this.gaParameters.getNumEachTime()];
            for(int i=0;i<this.gaParameters.getNumEachTime();i++){
                int selectIndex=new Random().nextInt(this.population.length);
                competitors[i]=this.population[selectIndex];
            }
            int maxEvalIndexInCompetitors=0;
            double maxEval=competitors[0].getEval();
            for(int i=1;i<competitors.length;i++){
                if(competitors[i].getEval()>maxEval){
                    maxEval=competitors[i].getEval();
                    maxEvalIndexInCompetitors=i;
                }
            }

            newPopulation[index]=competitors[maxEvalIndexInCompetitors];
        }

    }


    @Override
    public void HandleIntermediateSolution(Solution solution,int score) throws Exception {
        if(this.intermediateSolutionCallback!=null){
            this.gaParameters.getSolution().FinalResult=false;
            Solution intermediateSolution=this.variableConverter.getSolutionFromDisjunctiveGraph(this.gaParameters.getSolution(),this.gaParameters.getDisjunctiveGraphModel());
            intermediateSolution.FinalResult=false;
            this.intermediateSolutionCallback.HandleIntermediateSolution(intermediateSolution,this.amqpHandler,score);
        }
    }


    //Use shuffle algorithm to shuffle the task permutation in each machine
    public Map<String, List<NodeInDG>> shuffleTaskPermutation(Map<String, List<NodeInDG>> taskPermutation){
        for(Map.Entry<String, List<NodeInDG>> v : taskPermutation.entrySet()){
            String machineName=v.getKey();
            List<NodeInDG> nodes=v.getValue();

            for(int i=nodes.size()-1;i>0;i--){
                int randomIndex= new Random().nextInt(i+1);
                NodeInDG nodeTemp=nodes.get(i);
                nodes.set(i,nodes.get(randomIndex));
                nodes.set(randomIndex,nodeTemp);
            }
        }
        return taskPermutation;
    }

    //Chromosome
    public static class Chromosome{
        private Map<String, List<NodeInDG>> taskPermutation;
        private Integer originalScore;
        private Double fun;
        private Double eval;

        public Chromosome(Map<String, List<NodeInDG>> taskPermutation){
            this.taskPermutation=taskPermutation;
        }

        public Map<String, List<NodeInDG>> getTaskPermutation() {
            return taskPermutation;
        }

        public void setTaskPermutation(Map<String, List<NodeInDG>> taskPermutation) {
            this.taskPermutation = taskPermutation;
        }

        public Double getFun() {
            return fun;
        }

        public void setFun(Double fun) {
            this.fun = fun;
        }

        public Double getEval() {
            return eval;
        }

        public void setEval(Double eval) {
            this.eval = eval;
        }

        public Integer getOriginalScore() {
            return originalScore;
        }

        public void setOriginalScore(Integer originalScore) {
            this.originalScore = originalScore;
        }

    }
}
