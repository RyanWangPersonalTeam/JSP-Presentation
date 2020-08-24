package wzy.jsp.simplest.demo.domain.algorithm;

import wzy.jsp.simplest.demo.common.IAlgorithmCore;
import wzy.jsp.simplest.demo.common.IMetaHeuristicAlgorithm;
import wzy.jsp.simplest.demo.component.VariableConverter;
import wzy.jsp.simplest.demo.domain.algorithm.represent.DelayTimeRepresentModel;
import wzy.jsp.simplest.demo.domain.communication.Solution;

import java.util.*;

/**
 * A simple heuristic algorithm to schedule tasks in delay time represent model
 * Array tasks one by one in random sequence
 */
public class SimpleHeuristicAlgorithmWithDelayTimeRepresentModel implements IAlgorithmCore {
    private DelayTimeRepresentModel delayTimeRepresentModel;
    private VariableConverter variableConverter;
    private Solution unsolvedSolution;
    public SimpleHeuristicAlgorithmWithDelayTimeRepresentModel(DelayTimeRepresentModel delayTimeRepresentModel,
                                                               VariableConverter variableConverter,
                                                               Solution unsolvedSolution){
        this.delayTimeRepresentModel=delayTimeRepresentModel;
        this.variableConverter=variableConverter;
        this.unsolvedSolution=unsolvedSolution;
    }

    public List<List<Integer>> Calculate(){
        //First , use shuffle algorithm to decide assign sequence of jobs
        int[] jobIndexs=new int[this.delayTimeRepresentModel.es.size()];
        for(int i=0;i<jobIndexs.length;i++){
            jobIndexs[i]=i;
        }
        for(int i=jobIndexs.length-1;i>0;i--){
            int randomIndex=new Random().nextInt(i+1);
            int temp=jobIndexs[i];
            jobIndexs[i]=jobIndexs[randomIndex];
            jobIndexs[randomIndex]=temp;
        }

        //Key: Machine
        //Value: The earliest available start time of this machine
        Map<String,Integer> machineTime=new HashMap<>();

        List<List<Integer>> calculatedEs=new ArrayList<>();
        for(int i=0;i<this.delayTimeRepresentModel.es.size();i++){
            List<Integer> calculatedEsThisJob=new ArrayList<>();
            calculatedEs.add(calculatedEsThisJob);
        }

        for(int i=0;i<jobIndexs.length;i++){
            int jobIndex=jobIndexs[i];
            List<Integer> esThisJob=this.delayTimeRepresentModel.es.get(jobIndex);
            List<Integer> calculatedEsThisJob=calculatedEs.get(jobIndex);
            int lastEndTime=0;
            calculatedEsThisJob.add(0);
            for(int j=1;j<esThisJob.size()-1;j++){
                int machineIndex=0;
                for(int k=0;k<this.delayTimeRepresentModel.occupies.get(jobIndex).get(j).length;k++){
                    if(this.delayTimeRepresentModel.occupies.get(jobIndex).get(j)[k]==1){
                        machineIndex=k;
                        break;
                    }
                }
                String machineName=this.delayTimeRepresentModel.machines[machineIndex];
                Integer temp=machineTime.get(machineName);
                if(temp==null){
                    calculatedEsThisJob.add(0);
                }
                else{
                    if(j!=1){
                        calculatedEsThisJob.add(lastEndTime>temp?0:temp-lastEndTime);
                    }
                    else{
                        calculatedEsThisJob.add(temp);
                    }
                }
                lastEndTime=lastEndTime+calculatedEsThisJob.get(calculatedEsThisJob.size()-1)+this.delayTimeRepresentModel.durations.get(jobIndex).get(j);
                machineTime.put(machineName,lastEndTime);
            }
            calculatedEsThisJob.add(0);
        }

        return calculatedEs;
    }

    @Override
    public Solution Calculate(boolean initialized) throws Exception {

        List<List<Integer>> calculatedEs=this.Calculate();
        this.delayTimeRepresentModel.es=calculatedEs;
        Solution solvedSolution=this.variableConverter.getScheduledSolutionFromDelayTimeRepresentModel(
                this.delayTimeRepresentModel, this.unsolvedSolution
        );
        solvedSolution.FinalResult=true;
        return solvedSolution;
    }

}
