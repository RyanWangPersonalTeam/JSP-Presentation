package wzy.jsp.simplest.demo.domain.algorithm;

import org.javatuples.Pair;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import wzy.jsp.simplest.demo.common.IAlgorithmCore;
import wzy.jsp.simplest.demo.common.IMetaHeuristicAlgorithm;
import wzy.jsp.simplest.demo.common.IntermediateSolutionCallback;
import wzy.jsp.simplest.demo.component.AMQPHandler;
import wzy.jsp.simplest.demo.component.VariableConverter;
import wzy.jsp.simplest.demo.domain.algorithm.represent.NodeInDG;
import wzy.jsp.simplest.demo.domain.algorithm.represent.SAParametersWithDisjunctiveGraph;
import wzy.jsp.simplest.demo.domain.communication.Solution;

import java.util.*;

public class SAAlgorithmWithDisjunctiveGraph implements IMetaHeuristicAlgorithm, IAlgorithmCore {
    private SAParametersWithDisjunctiveGraph saParameters;
    private VariableConverter variableConverter;
    private AMQPHandler amqpHandler;
    private double currentT;//current Temperature
    private int currentBestScore=Integer.MAX_VALUE;//Record the best score
    private Map<String, List<NodeInDG>> currentBestPermutation=null;//Record the best permutation
    private int currentEvalValue=Integer.MAX_VALUE;

    private IntermediateSolutionCallback intermediateSolutionCallback;

    private Logger logger;

    public SAAlgorithmWithDisjunctiveGraph(SAParametersWithDisjunctiveGraph saParameters,
                                                 VariableConverter variableConverter,
                                                 IntermediateSolutionCallback intermediateSolutionCallback,
                                                 AMQPHandler amqpHandler){
        this.saParameters=saParameters;
        this.variableConverter=variableConverter;
        this.intermediateSolutionCallback=intermediateSolutionCallback;
        this.amqpHandler=amqpHandler;

        this.logger= LoggerFactory.getLogger(SAAlgorithmWithDisjunctiveGraph.class);
    }


    @Override
    public Solution Calculate(boolean initialized) throws Exception {
        this.currentT=this.saParameters.getTemperature0();
        this.currentBestScore=Integer.MAX_VALUE;
        this.currentBestPermutation=this.saParameters.getDisjunctiveGraphModel().nodesPerMachine;

        //If not initialized, using Heuristic Algorithm to generate a init solution
        if(!initialized){
            throw new Exception("Must init");
        }
        this.currentEvalValue=this.getEvaluationValue();
        this.currentBestScore=this.currentEvalValue;
        this.currentBestPermutation=this.variableConverter.deepCopyPermutation(this.saParameters.getDisjunctiveGraphModel().nodesPerMachine);

        this.HandleIntermediateSolution(this.saParameters.getSolution(),this.currentBestScore);

        //Simulated Annealing
        int index=0;
        while(this.currentT>this.saParameters.getMinTemperature()){
            for(int i=0;i<this.saParameters.getLoopN();i++){
                this.logger.info("Loop1 : "+index+", Loop2 : "+i+", Current Temperature : "+this.currentT);
                this.Transfer();
            }
            index++;
            this.currentT=this.currentT*this.saParameters.getA();
        }

        this.saParameters.getDisjunctiveGraphModel().nodesPerMachine=this.variableConverter.deepCopyPermutation(this.currentBestPermutation);
        Solution solvedSolution=this.variableConverter.getSolutionFromDisjunctiveGraph(
                this.saParameters.getSolution(), this.saParameters.getDisjunctiveGraphModel()
        );
        solvedSolution.FinalResult=true;
        return solvedSolution;
    }

    //Evaluation function
    public int getEvaluationValue(){
        Solution decodeSolution=this.variableConverter.getSolutionFromDisjunctiveGraph(this.saParameters.getSolution(),this.saParameters.getDisjunctiveGraphModel());

        Integer makespan=this.variableConverter.getMakespanFromSolution(decodeSolution);

        return makespan;
    }

    //Generate a solution from neighborhood area
    private NeighborhoodPermutationRecord getNeighborhoodDecisionVariables(){
        Random r = new Random();
        //Select a Machine
        String selectMachineName=null;
        while (selectMachineName==null || this.saParameters.getDisjunctiveGraphModel().nodesPerMachine.get(selectMachineName).size()<2){
            int selectMachineIndex=r.nextInt(this.saParameters.getDisjunctiveGraphModel().nodesPerMachine.keySet().size());
            int i=0;
            for(String machine:this.saParameters.getDisjunctiveGraphModel().nodesPerMachine.keySet()){
                if(i==selectMachineIndex){
                    selectMachineName=machine;
                    break;
                }
                i++;
            }
        }

        //swap two node in this machine randomly
        List<NodeInDG> nodesThisMachine=this.saParameters.getDisjunctiveGraphModel().nodesPerMachine.get(selectMachineName);
        int nodeIndex1=r.nextInt(nodesThisMachine.size());
        int nodeIndex2=nodeIndex1;
        while(nodeIndex1==nodeIndex2){
            nodeIndex2=r.nextInt(nodesThisMachine.size());
        }

        NodeInDG temp=nodesThisMachine.get(nodeIndex1);
        nodesThisMachine.set(nodeIndex1,nodesThisMachine.get(nodeIndex2));
        nodesThisMachine.set(nodeIndex2,temp);
        this.saParameters.getDisjunctiveGraphModel().nodesPerMachine.replace(selectMachineName,nodesThisMachine);

        return new NeighborhoodPermutationRecord(
                selectMachineName,
                nodesThisMachine.get(nodeIndex1),
                nodeIndex1,
                nodesThisMachine.get(nodeIndex2),
                nodeIndex2);
    }

    //Annealing process
    public void Transfer() throws Exception {
        //Eval value of current solution
        int F_current=this.currentEvalValue;
        //Get a neighborhood solution
        NeighborhoodPermutationRecord record=this.getNeighborhoodDecisionVariables();
        //Eval value of new solution
        int F_new=this.getEvaluationValue();
        String str=String.format("Neighborhood operation : swap task_%d_%d and task_%d_%d in machine %s, score from %d to %d",
                record.getNodeSwapLeft().taskIndexs.getValue0(),
                record.getNodeSwapLeft().taskIndexs.getValue1(),
                record.getNodeSwapRight().taskIndexs.getValue0(),
                record.getNodeSwapRight().taskIndexs.getValue1(),
                record.getMachineName(),
                F_current,
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
                List<NodeInDG> nodesThisMachine=this.saParameters.getDisjunctiveGraphModel().nodesPerMachine.get(record.getMachineName());
                int nodeIndex1=record.getNodeSwapLeftIndex();
                int nodeIndex2=record.getNodeSwapRightIndex();

                NodeInDG temp=nodesThisMachine.get(nodeIndex1);
                nodesThisMachine.set(nodeIndex1,nodesThisMachine.get(nodeIndex2));
                nodesThisMachine.set(nodeIndex2,temp);
                this.saParameters.getDisjunctiveGraphModel().nodesPerMachine.replace(record.getMachineName(),nodesThisMachine);

                this.currentEvalValue=F_current;
                this.logger.info("Transfer can't accept!");
            }
        }

        if(this.currentBestPermutation==null || this.currentBestScore>Math.min(F_current,F_new)){
            this.currentBestScore=Math.min(F_current,F_new);
            this.currentBestPermutation=this.variableConverter.deepCopyPermutation(this.saParameters.getDisjunctiveGraphModel().nodesPerMachine);
            this.HandleIntermediateSolution(this.saParameters.getSolution(),this.currentBestScore);
        }

    }


    @Override
    public void HandleIntermediateSolution(Solution solution,int score) throws Exception {
        if(this.intermediateSolutionCallback!=null){
            this.saParameters.getSolution().FinalResult=false;
            Solution intermediateSolution=this.variableConverter.getSolutionFromDisjunctiveGraph(this.saParameters.getSolution(),this.saParameters.getDisjunctiveGraphModel());
            intermediateSolution.FinalResult=false;
            this.intermediateSolutionCallback.HandleIntermediateSolution(intermediateSolution,this.amqpHandler,score);
        }
    }



    //Record the neighborhood change, roll back if necessary
    class NeighborhoodPermutationRecord{


        public NodeInDG getNodeSwapLeft() {
            return nodeSwapLeft;
        }

        public NodeInDG getNodeSwapRight() {
            return nodeSwapRight;
        }

        public String getMachineName() {
            return machineName;
        }

        public int getNodeSwapLeftIndex() {
            return nodeSwapLeftIndex;
        }

        public int getNodeSwapRightIndex() {
            return nodeSwapRightIndex;
        }

        String machineName;
        NodeInDG nodeSwapLeft;
        int nodeSwapLeftIndex;
        NodeInDG nodeSwapRight;
        int nodeSwapRightIndex;

        public NeighborhoodPermutationRecord(String machineName,NodeInDG nodeSwapLeft,int nodeSwapLeftIndex, NodeInDG nodeSwapRight,int nodeSwapRightIndex){
            this.machineName=machineName;
            this.nodeSwapLeft=nodeSwapLeft;
            this.nodeSwapLeftIndex=nodeSwapLeftIndex;
            this.nodeSwapRight=nodeSwapRight;
            this.nodeSwapRightIndex=nodeSwapRightIndex;
        }
    }
}
