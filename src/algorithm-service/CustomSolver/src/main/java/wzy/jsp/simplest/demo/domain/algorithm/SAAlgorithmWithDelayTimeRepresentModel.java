package wzy.jsp.simplest.demo.domain.algorithm;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import wzy.jsp.simplest.demo.common.IAlgorithmCore;
import wzy.jsp.simplest.demo.common.IMetaHeuristicAlgorithm;
import wzy.jsp.simplest.demo.common.IntermediateSolutionCallback;
import wzy.jsp.simplest.demo.component.AMQPHandler;
import wzy.jsp.simplest.demo.component.VariableConverter;
import wzy.jsp.simplest.demo.domain.algorithm.represent.SAParametersWithDelayTimeRepresentModel;
import wzy.jsp.simplest.demo.domain.communication.Solution;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

/**
 * SA Algorithm using delay time represent model
 * It will calculate the best es value
 */
public class SAAlgorithmWithDelayTimeRepresentModel implements IMetaHeuristicAlgorithm, IAlgorithmCore {
    private SAParametersWithDelayTimeRepresentModel saParameters;
    private VariableConverter variableConverter;
    private AMQPHandler amqpHandler;
    private double currentT;//current Temperature
    private int currentBestScore=Integer.MAX_VALUE;//Record the best score
    private List<List<Integer>> currentBestES=null;//Record the best solution
    private int currentEvalValue=Integer.MAX_VALUE;

    private IntermediateSolutionCallback intermediateSolutionCallback;

    private Logger logger;


    public SAAlgorithmWithDelayTimeRepresentModel(SAParametersWithDelayTimeRepresentModel saParameters,
                                                  VariableConverter variableConverter,
                                                  IntermediateSolutionCallback intermediateSolutionCallback,
                                                  AMQPHandler amqpHandler){
        this.saParameters=saParameters;
        this.variableConverter=variableConverter;
        this.intermediateSolutionCallback=intermediateSolutionCallback;
        this.amqpHandler=amqpHandler;

        this.logger= LoggerFactory.getLogger(SAAlgorithmWithDelayTimeRepresentModel.class);
    }

    public Solution Calculate(boolean initialized) throws Exception {
        this.currentT=this.saParameters.getTemperature0();
        this.currentBestScore=Integer.MAX_VALUE;
        this.currentBestES=null;

        //If not initialized, using Heuristic Algorithm to generate a init solution
        if(!initialized){
            SimpleHeuristicAlgorithmWithDelayTimeRepresentModel simpleAlgorithm=new SimpleHeuristicAlgorithmWithDelayTimeRepresentModel(
                    this.saParameters.getDelayTimeRepresentModel(),
                    this.variableConverter,
                    this.saParameters.getSolution()
            );
            this.saParameters.getDelayTimeRepresentModel().es=simpleAlgorithm.Calculate();
        }
        this.currentEvalValue=this.getEvaluationValue();
        this.currentBestScore=this.currentEvalValue;
        this.currentBestES=new ArrayList<>();
        for(int jobIndex=0;jobIndex<this.saParameters.getDelayTimeRepresentModel().es.size();jobIndex++){
            List<Integer> es=new ArrayList<>();
            for(int taskIndex=0;taskIndex<this.saParameters.getDelayTimeRepresentModel().es.get(jobIndex).size();taskIndex++){
                es.add(this.saParameters.getDelayTimeRepresentModel().es.get(jobIndex).get(taskIndex));
            }
            this.currentBestES.add(es);
        }

        this.HandleIntermediateSolution(this.saParameters.getSolution());

        //Simulated Annealing
        int index=0;
        while(this.currentT>this.saParameters.getMinTemperature()){
//            if(this.currentEvalValue>=this.saParameters.getI()){
//                this.saParameters.getDelayTimeRepresentModel().es=new ArrayList<>();
//                for(int jobIndex=0;jobIndex<this.currentBestES.size();jobIndex++){
//                    List<Integer> es=new ArrayList<>();
//                    for(int taskIndex=0;taskIndex<this.currentBestES.get(jobIndex).size();taskIndex++){
//                        es.add(this.currentBestES.get(jobIndex).get(taskIndex));
//                    }
//                    this.saParameters.getDelayTimeRepresentModel().es.add(es);
//                }
//                this.currentEvalValue=this.currentBestScore;
//            }
            for(int i=0;i<this.saParameters.getLoopN();i++){
                this.logger.info("Loop1 : "+index+", Loop2 : "+i+", Current Temperature : "+this.currentT);
                this.Transfer();
            }
            index++;
            this.currentT=this.currentT*this.saParameters.getA();
        }

        this.saParameters.getDelayTimeRepresentModel().es=this.currentBestES;
        Solution solvedSolution=this.variableConverter.getScheduledSolutionFromDelayTimeRepresentModel(
                this.saParameters.getDelayTimeRepresentModel(), this.saParameters.getSolution()
        );
        solvedSolution.FinalResult=true;
        return solvedSolution;
    }

    //Evaluation function
    public int getEvaluationValue(){
        List<List<Integer>> startTimes=this.variableConverter.getStartTimesFromEs(this.saParameters.getDelayTimeRepresentModel());
        int maxEndTime=this.getBasicEvaluationValue(startTimes);
        int hardConstraintValue=this.checkHardConstraint(startTimes,maxEndTime);
        return maxEndTime+hardConstraintValue;
    }

    //The base value of evaluation function, the max end time of tasks
    private int getBasicEvaluationValue(List<List<Integer>> startTimes){
        int max=0;
        for(int i=0;i<startTimes.size();i++){
            int lastIndex=startTimes.get(i).size()-1;
            int temp=startTimes.get(i).get(lastIndex)+this.saParameters.getDelayTimeRepresentModel().durations.get(i).get(lastIndex);
            if(temp>max){
                max=temp;
            }
        }
        return max;
    }

    //Check whether hard constraints are violated, if yes, return penalty, if not return zero
    private int checkHardConstraint(List<List<Integer>> startTimes, int maxEndTime){
        int penaltySum=0;
        //Check every time point
        for(int t=0;t<=maxEndTime/*this.saParameters.getDelayTimeRepresentModel().timeUpperLimit*/;t++){//replace 'timeUpperLimit' into 'maxEndTime' to improve performance
            //Check the occupy status of every machine this time point
            for(int k=0;k<this.saParameters.getDelayTimeRepresentModel().machines.length;k++){
                int occupySum=0;
                for(int i=0;i<startTimes.size();i++){
                    for(int j=0;j<startTimes.get(i).size();j++){
                        if(j==0 || j==startTimes.get(i).size()-1){
                            continue;
                        }
                        int startTime=startTimes.get(i).get(j);
                        int duration=this.saParameters.getDelayTimeRepresentModel().durations.get(i).get(j);
                        int occupyValueThisMachine=this.saParameters.getDelayTimeRepresentModel().occupies.get(i).get(j)[k];
                        if(startTime<=t && startTime+duration>t){
                            occupySum+=occupyValueThisMachine;
                        }
                        if(occupySum>1){
                            penaltySum+=this.saParameters.getI();
                            //return this.saParameters.getI();
                        }

                    }
                }
            }
        }
        return penaltySum;
    }

    //Generate a solution from neighborhood area
    private NeighborhoodDecisionVariableRecord getNeighborhoodDecisionVariables(){
        Random r = new Random();
        //Select a task randomly
        int selectJobIndex=r.nextInt(this.saParameters.getDelayTimeRepresentModel().es.size());
        int selectTaskIndex=r.nextInt(this.saParameters.getDelayTimeRepresentModel().es.get(selectJobIndex).size()-2)+1;//注意不能生成为首末两个虚拟task索引
        //Change the decision variable of this task
        int previousValue=this.saParameters.getDelayTimeRepresentModel().es.get(selectJobIndex).get(selectTaskIndex);
        int newEValue=this.getLinnearRandomNumber(this.saParameters.getDelayTimeRepresentModel().decisionUpperLimit+1);
        this.saParameters.getDelayTimeRepresentModel().es.get(selectJobIndex).set(selectTaskIndex,newEValue);

        return new NeighborhoodDecisionVariableRecord(selectJobIndex,selectTaskIndex,previousValue,newEValue);
    }

    //Annealing process
    public void Transfer() throws Exception {
        //Eval value of current solution
        int F_current=this.currentEvalValue;
        //Get a neighborhood solution
        NeighborhoodDecisionVariableRecord record=this.getNeighborhoodDecisionVariables();
        //Eval value of new solution
        int F_new=this.getEvaluationValue();
        String str=String.format("Neighborhood operation : es of task_%d_%d, from %d (F: %d) -> %d (F: %d) ",
                record.jobIndex,
                record.taskIndex,
                record.previousValue,
                F_current,
                record.newValue,
                F_new);
        this.logger.info(str);
        //Transfer probability
        if(F_current>=F_new){
            this.logger.info("Transfer accepted!");//transfer directly
            this.currentEvalValue=F_new;
        }
        else{
            double delta=F_new-F_current;
            double theta=-delta/this.currentT;
            double acceptProbability=Math.exp(theta);
            double randomValue=Math.random();
            if(randomValue<acceptProbability){
                this.logger.info("Transfer accepted!");//transfer directly
                this.currentEvalValue=F_new;
            }
            else{
                //If not accept, need roll back
                this.saParameters.getDelayTimeRepresentModel().es
                        .get(record.getJobIndex())
                        .set(record.getTaskIndex(),record.previousValue);
                this.currentEvalValue=F_current;
                this.logger.info("Transfer can't accept!");
            }
        }

        if(this.currentBestES==null || this.currentBestScore>Math.min(F_current,F_new)){
            this.currentBestScore=Math.min(F_current,F_new);
            this.currentBestES=new ArrayList<>();
            for(int jobIndex=0;jobIndex<this.saParameters.getDelayTimeRepresentModel().es.size();jobIndex++){
                List<Integer> es=new ArrayList<>();
                for(int taskIndex=0;taskIndex<this.saParameters.getDelayTimeRepresentModel().es.get(jobIndex).size();taskIndex++){
                    es.add(this.saParameters.getDelayTimeRepresentModel().es.get(jobIndex).get(taskIndex));
                }
                this.currentBestES.add(es);
            }
            this.HandleIntermediateSolution(this.saParameters.getSolution());
        }

    }


    //Generate a random value in [0, maxSize),obeying the descending linear probability distribution,
    //the smaller value has more possibility to be selected; using Inverse transform sampling algorithm
    public int getLinnearRandomNumber(int maxSize){
        //Get a linearly multiplied random number
        int randomMultiplier = maxSize * (maxSize + 1) / 2;
        Random r=new Random();
        int randomInt = r.nextInt(randomMultiplier);

        //Linearly iterate through the possible values to find the correct one
        int linearRandomNumber = 0;
        for(int i=maxSize; randomInt >= 0; i--){
            randomInt -= i;
            linearRandomNumber++;
        }

        return linearRandomNumber-1;
    }

    @Override
    public void HandleIntermediateSolution(Solution solution) throws Exception {
        if(this.intermediateSolutionCallback!=null){
            this.saParameters.getSolution().FinalResult=false;
            Solution intermediateSolution=this.variableConverter.getScheduledSolutionFromDelayTimeRepresentModel(this.saParameters.getDelayTimeRepresentModel(),this.saParameters.getSolution());
            intermediateSolution.FinalResult=false;
            this.intermediateSolutionCallback.HandleIntermediateSolution(intermediateSolution,this.amqpHandler);
        }
    }

    //Record the neighborhood change, roll back if necessary
    class NeighborhoodDecisionVariableRecord{
        public int getJobIndex() {
            return jobIndex;
        }

        public int getTaskIndex() {
            return taskIndex;
        }

        public int getPreviousValue() {
            return previousValue;
        }

        public int getNewValue() {
            return newValue;
        }

        int jobIndex;
        int taskIndex;
        int previousValue;
        int newValue;

        public NeighborhoodDecisionVariableRecord(int jobIndex,int taskIndex,int previousValue,int newValue){
            this.jobIndex=jobIndex;
            this.taskIndex=taskIndex;
            this.previousValue=previousValue;
            this.newValue=newValue;
        }
    }
}
