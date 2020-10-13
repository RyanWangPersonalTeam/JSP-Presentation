package wzy.jsp.simplest.demo.domain.algorithm;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import wzy.jsp.simplest.demo.common.IAlgorithmCore;
import wzy.jsp.simplest.demo.common.IMetaHeuristicAlgorithm;
import wzy.jsp.simplest.demo.common.IntermediateSolutionCallback;
import wzy.jsp.simplest.demo.component.AMQPHandler;
import wzy.jsp.simplest.demo.component.VariableConverter;
import wzy.jsp.simplest.demo.domain.algorithm.represent.GAParametersWithDirectFormulationModel;
import wzy.jsp.simplest.demo.domain.communication.Solution;

import java.util.*;

/**
 * GA Algorithm using delay time represent model
 * It will calculate the best es value
 */
public class GAAlgorithmWithDirectFormulationModel implements IMetaHeuristicAlgorithm, IAlgorithmCore {
    private GAParametersWithDirectFormulationModel gaParameters;
    private VariableConverter variableConverter;
    private AMQPHandler amqpHandler;
    private int currentBestScore=Integer.MAX_VALUE;//Record the best score (original objective function value , not fitness or combining with M)
    private List<List<Integer>> currentBestES=null;//Record the best solution
    private Chromosome[] population;//Record population
    private int M;//Use a big number to transfer min-problem to max-problem

    private IntermediateSolutionCallback intermediateSolutionCallback;

    private Logger logger;

    private int initFeasibleSolutionNum;

    public GAAlgorithmWithDirectFormulationModel(GAParametersWithDirectFormulationModel gaParameters,
                                                 VariableConverter variableConverter,
                                                 IntermediateSolutionCallback intermediateSolutionCallback,
                                                 AMQPHandler amqpHandler){
        this.gaParameters=gaParameters;
        this.variableConverter=variableConverter;
        this.intermediateSolutionCallback=intermediateSolutionCallback;
        this.amqpHandler=amqpHandler;

        this.logger= LoggerFactory.getLogger(GAAlgorithmWithDirectFormulationModel.class);

        this.M=this.gaParameters.getDirectFormulationModel().timeUpperLimit*10;

        this.initFeasibleSolutionNum=1;
        if(this.gaParameters.getSolution().Jobs.size()<=10){
            for(int i=1;i<=this.gaParameters.getSolution().Jobs.size();i++){
                this.initFeasibleSolutionNum*=i;
            }
        }
        else{
            for(int i=1;i<=10;i++){
                this.initFeasibleSolutionNum*=i;
            }
        }
    }

    @Override
    public Solution Calculate(boolean initialized) throws Exception {
        this.currentBestScore=Integer.MAX_VALUE;
        this.currentBestES=null;
        if(initialized){
            this.HandleIntermediateSolution(this.gaParameters.getSolution(),this.currentBestScore);
        }

        InitPopulation();
        Eval();
        Select();
        for(int i=0;i<this.gaParameters.getgN();i++){
            this.logger.info("################### Generation : "+i+" #####################");
            Cross();
            Mutate();
            Eval();
            Select();
        }

        this.gaParameters.getDirectFormulationModel().es=this.currentBestES;
        Solution solvedSolution=this.variableConverter.getScheduledSolutionFromDirectFormulationRepresentModel(
                this.gaParameters.getDirectFormulationModel(), this.gaParameters.getSolution()
        );
        solvedSolution.FinalResult=true;
        return solvedSolution;
    }

    //Init the population
    public void InitPopulation(){
        //Add random value in initial solution to generate population
        this.population=new Chromosome[this.gaParameters.getPopSize()];

        SimpleHeuristicAlgorithmWithDirectFormulationModel simpleAlgorithm=new SimpleHeuristicAlgorithmWithDirectFormulationModel(
                this.gaParameters.getDirectFormulationModel(),
                this.variableConverter,
                this.gaParameters.getSolution()
        );
        if(this.initFeasibleSolutionNum>=this.gaParameters.getPopSize()){
            for(int i=0;i<this.gaParameters.getPopSize();i++){
                this.population[i]=new Chromosome(this.DeepCopyEs(simpleAlgorithm.Calculate()));
            }
        }
        else{
            int minNum=this.gaParameters.getPopSize()/this.initFeasibleSolutionNum;
            int modNum=this.gaParameters.getPopSize()%this.initFeasibleSolutionNum;
            int i=0;
            int j=0;
            for(i=0;i<this.initFeasibleSolutionNum;i++){
                int s=0+i*minNum;
                this.population[s]=new Chromosome(this.DeepCopyEs(simpleAlgorithm.Calculate()));
                for(j=s+1;j<s+minNum;j++){
                    this.population[j]=new Chromosome(this.DeepCopyEs(this.population[s].getEs()));
                    for(int index1=0;index1<this.population[j].getEs().size();index1++){
                        for(int index2=0;index2<this.population[j].getEs().get(index1).size();index2++){
                            double min=Math.min(this.population[j].getEs().get(index1).get(index2),
                                    this.gaParameters.getMutateDeltaUpLimit());
                            int randomDir=new Random().nextInt(2)==0 ? -1 : 1;
                            int delta=0;
                            if(randomDir==-1){
                                delta=new Random().nextInt((new Double(min)).intValue()+1);
                            }
                            else{
                                delta=new Random().nextInt((new Double(this.gaParameters.getMutateDeltaUpLimit())).intValue()+1);
                            }
                            this.population[j].getEs().get(index1).set(index2, this.population[j].getEs().get(index1).get(index2)+randomDir*delta);
                        }
                    }

                }
            }
            for(int k=j;k<this.gaParameters.getPopSize();k++){
                this.population[k]=new Chromosome(this.DeepCopyEs(this.population[k%this.initFeasibleSolutionNum].getEs()));
            }
        }
    }

    //Calculate fitness of each chromosome
    public void Eval(){
        double sum=0;
        for(int i=0;i<this.population.length;i++){
            this.population[i].setFun(this.Fun(this.population[i]));
            sum+=this.population[i].getFun();
        }

        for(int i=0;i<this.population.length;i++){
            this.population[i].setEval(this.population[i].getFun()/sum);
        }

    }

    //Objective value of each chromosome
    public int Fun(Chromosome chromosome){
        this.gaParameters.getDirectFormulationModel().es=chromosome.getEs();
        List<List<Integer>> startTimes=this.variableConverter.getStartTimesFromEs(this.gaParameters.getDirectFormulationModel());
        int maxEndTime=this.getBasicEvaluationValue(startTimes);
        int hardConstraintValue=this.checkHardConstraint(startTimes,maxEndTime);
        int originalScore=maxEndTime+hardConstraintValue;
        chromosome.setOriginalScore(originalScore);
        if(originalScore<this.gaParameters.getI()){
            return (M-originalScore)*100;
        }
        else{
            return (M-originalScore);
        }
    }

    //The base value of evaluation function, the max end time of tasks
    private int getBasicEvaluationValue(List<List<Integer>> startTimes){
        int max=0;
        for(int i=0;i<startTimes.size();i++){
            int lastIndex=startTimes.get(i).size()-1;
            int temp=startTimes.get(i).get(lastIndex)+this.gaParameters.getDirectFormulationModel().durations.get(i).get(lastIndex);
            if(temp>max){
                max=temp;
            }
        }
        return max;
    }

    //Check whether hard constraints are violated, if yes, return penalty, if not return zero
    private int checkHardConstraint(List<List<Integer>> startTimes, int maxEndTime){
        //Check every time point
        for(int t=0;t<=maxEndTime/*this.saParameters.getDelayTimeRepresentModel().timeUpperLimit*/;t++){//replace 'timeUpperLimit' into 'maxEndTime' to improve performance
            //Check the occupy status of every machine this time point
            for(int k = 0; k<this.gaParameters.getDirectFormulationModel().machines.length; k++){
                int occupySum=0;
                for(int i=0;i<startTimes.size();i++){
                    for(int j=0;j<startTimes.get(i).size();j++){
                        if(j==0 || j==startTimes.get(i).size()-1){
                            continue;
                        }
                        int startTime=startTimes.get(i).get(j);
                        int duration=this.gaParameters.getDirectFormulationModel().durations.get(i).get(j);
                        int occupyValueThisMachine=this.gaParameters.getDirectFormulationModel().occupies.get(i).get(j)[k];
                        if(startTime<=t && startTime+duration>t){
                            occupySum+=occupyValueThisMachine;
                        }
                        if(occupySum>1){
                            return this.gaParameters.getI();
                        }

                    }
                }
            }
        }
        return 0;
    }

    //Cross
    public void Cross(){
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

            Chromosome parent1=this.population[parentsIndex.get(i)];
            Chromosome parent2=this.population[parentsIndex.get(i+1)];
            TwoChromosomeCross(parent1,parent2);
        }
    }

    //Multiple point cross of two real numbers chromosome
    public void TwoChromosomeCross(Chromosome parent1,Chromosome parent2){

        //Generate a binary-vector randomly
        List<List<Integer>> randomBinaryVector=new ArrayList<>();
        for(int i=0;i<parent1.getEs().size();i++){
            List<Integer> tempVector=new ArrayList<>();
            for(int j=0;j<parent1.getEs().get(i).size();j++){
                int r=new Random().nextInt(2);
                tempVector.add(r);
            }
            randomBinaryVector.add(tempVector);
        }

        for(int i=0;i<randomBinaryVector.size();i++){
            for(int j=0;j<randomBinaryVector.get(i).size();j++){
                if(randomBinaryVector.get(i).get(j)==1){
                    double esParent1=parent1.getEs().get(i).get(j);
                    double esParent2=parent2.getEs().get(i).get(j);
                    double a=new Random().nextDouble();
                    double temp1=esParent1*a+(1-a)*esParent2;
                    double temp2=esParent2*a+(1-a)*esParent1;
                    parent1.getEs().get(i).set(j,(new Double(temp1)).intValue());
                    parent2.getEs().get(i).set(j,(new Double(temp2)).intValue());
                }
            }
        }
    }

    //Mutate
    public void Mutate(){
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

    public void MutateOneChromosome(Chromosome chromosome){
        int selectJobIndex=new Random().nextInt(chromosome.getEs().size());
        int selectTaskIndex=new Random().nextInt(chromosome.getEs().get(selectJobIndex).size());
        int currentEs=chromosome.getEs().get(selectJobIndex).get(selectTaskIndex);

        double min=Math.min(currentEs,
                gaParameters.getMutateDeltaUpLimit());
        int randomDir=new Random().nextInt(2)==0 ? -1 : 1;
        int delta=0;
        if(randomDir==-1){
            delta=new Random().nextInt((new Double(min)).intValue()+1);
        }
        else{
            delta=new Random().nextInt((new Double(gaParameters.getMutateDeltaUpLimit())).intValue()+1);
        }

        chromosome.getEs().get(selectJobIndex).set(selectTaskIndex,currentEs+randomDir*delta);
    }

    //Using roulette algorithm to select
    public void Select() throws Exception {
        int bestIndex=0;
        double bestEval=this.population[0].getEval();
        for(int i=1;i<this.population.length;i++){
            if(this.population[i].getEval()>bestEval){
                bestIndex=i;
                bestEval=this.population[i].getEval();
            }
        }
        if(this.currentBestES==null || this.currentBestScore>this.population[bestIndex].getOriginalScore()){
            this.currentBestScore=this.population[bestIndex].getOriginalScore();
            this.currentBestES=new ArrayList<>();
            for(int jobIndex=0;jobIndex<this.population[bestIndex].getEs().size();jobIndex++){
                List<Integer> es=new ArrayList<>();
                for(int taskIndex=0;taskIndex<this.population[bestIndex].getEs().get(jobIndex).size();taskIndex++){
                    es.add(this.population[bestIndex].getEs().get(jobIndex).get(taskIndex));
                }
                this.currentBestES.add(es);
            }
            this.gaParameters.getDirectFormulationModel().es=this.currentBestES;
            this.HandleIntermediateSolution(this.gaParameters.getSolution(),this.currentBestScore);
        }
        this.logger.info("Best eval this generation : "+bestEval);
        this.logger.info("Best score this generation : "+(this.population[bestIndex].getOriginalScore()));
        this.logger.info("Current best score : "+this.currentBestScore);

//        Set<Integer> feasibleIndexRecords=new HashSet<>();//Record the position of feasible chromosomes
//        for(int i=0;i<this.population.length;i++){
//            double temp=M-this.population[i].getFun();
//            if(temp<this.gaParameters.getI()){
//                feasibleIndexRecords.add(i);
//            }
//        }

        double[] qs=new double[this.population.length+1];
        qs[0]=0;
        double currentAccumulation=0;
        for(int j=1;j<qs.length;j++){
            qs[j]=currentAccumulation+this.population[j-1].getEval();
            currentAccumulation=qs[j];
        }

        Chromosome[] newPopulation=new Chromosome[this.population.length];
        //Feasible chromosome must be selected
//        Iterator iter = feasibleIndexRecords.iterator();
//        int s=0;
//        for(Integer index : feasibleIndexRecords){
//            newPopulation[s]=this.population[index];
//            s++;
//        }
        int s=0;
        for(int i=s;i<this.population.length;i++){
            double r=0;
            while (r==0){
                r=(new Random().nextDouble())*qs[qs.length-1];
            }
            int index=GetHitIndex(qs,0,qs.length-1,r);
            newPopulation[i]=this.population[index];
        }
        this.population=newPopulation;
    }

    public int GetHitIndex(double[] qs, int startPos, int endPos, double r){
        if(startPos+1==endPos){
            return startPos;
        }
        int midPos=(startPos+endPos)/2;
        if(r>qs[midPos-1] && r<=qs[midPos]){
            return midPos-1;
        }
        else if(r<=qs[midPos-1] && r<=qs[midPos]){
            return GetHitIndex(qs,startPos,midPos,r);
        }
        else{
            return GetHitIndex(qs,midPos,endPos,r);
        }
    }

    @Override
    public void HandleIntermediateSolution(Solution solution,int score) throws Exception {
        if(this.intermediateSolutionCallback!=null){
            this.gaParameters.getSolution().FinalResult=false;
            Solution intermediateSolution=this.variableConverter.getScheduledSolutionFromDirectFormulationRepresentModel(this.gaParameters.getDirectFormulationModel(),this.gaParameters.getSolution());
            intermediateSolution.FinalResult=false;
            this.intermediateSolutionCallback.HandleIntermediateSolution(intermediateSolution,this.amqpHandler,score);
        }
    }


    private List<List<Integer>> DeepCopyEs(List<List<Integer>> es){
        List<List<Integer>> newEs=new ArrayList<>();
        for(int i=0;i<es.size();i++){
            List<Integer> temp=new ArrayList<>();
            for(int j=0;j<es.get(i).size();j++){
                temp.add(es.get(i).get(j));
            }
            newEs.add(temp);
        }
        return newEs;
    }

    //Chromosome
    public class Chromosome{
        private List<List<Integer>> es;
        private int originalScore;
        private double fun;
        private double eval;

        public Chromosome(List<List<Integer>> es){
            this.es=es;
        }

        public List<List<Integer>> getEs() {
            return es;
        }

        public void setEs(List<List<Integer>> es) {
            this.es = es;
        }

        public double getFun() {
            return fun;
        }

        public void setFun(double fun) {
            this.fun = fun;
        }

        public double getEval() {
            return eval;
        }

        public void setEval(double eval) {
            this.eval = eval;
        }

        public int getOriginalScore() {
            return originalScore;
        }

        public void setOriginalScore(int originalScore) {
            this.originalScore = originalScore;
        }
    }
}
