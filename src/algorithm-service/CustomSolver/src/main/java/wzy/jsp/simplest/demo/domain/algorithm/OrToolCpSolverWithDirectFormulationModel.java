package wzy.jsp.simplest.demo.domain.algorithm;

import com.google.ortools.sat.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import wzy.jsp.simplest.demo.common.IAlgorithmCore;
import wzy.jsp.simplest.demo.common.IntermediateSolutionCallback;
import wzy.jsp.simplest.demo.component.AMQPHandler;
import wzy.jsp.simplest.demo.component.DateTimeConverter;
import wzy.jsp.simplest.demo.domain.algorithm.callback.SolutionPrintHelper;
import wzy.jsp.simplest.demo.domain.communication.Solution;
import wzy.jsp.simplest.demo.domain.communication.Task;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class OrToolCpSolverWithDirectFormulationModel implements IAlgorithmCore {
    private Solution unsolvedSolution;
    private AMQPHandler amqpHandler;
    private IntermediateSolutionCallback intermediateSolutionCallback;



    public OrToolCpSolverWithDirectFormulationModel(Solution unsolvedSolution, IntermediateSolutionCallback intermediateSolutionCallback, AMQPHandler amqpHandler){
        this.unsolvedSolution=unsolvedSolution;
        this.amqpHandler=amqpHandler;
        this.intermediateSolutionCallback=intermediateSolutionCallback;
    }


    public Solution Calculate(boolean initialized) throws Exception {
        if(initialized){
            if(this.intermediateSolutionCallback!=null){
                this.intermediateSolutionCallback.HandleIntermediateSolution(this.unsolvedSolution,this.amqpHandler);
            }
        }
        DateTimeConverter dateTimeConverter=new DateTimeConverter();
        CpModel model=new CpModel();
        int horizon=dateTimeConverter.CalculateCountValueBetweenDates(this.unsolvedSolution.MinTime,this.unsolvedSolution.MaxTime);
        List<List<IntVar>> startVars=new ArrayList<>();
        List<List<IntVar>> endVars=new ArrayList<>();
        List<List<IntervalVar>> interValVars=new ArrayList<>();
        Map<String,List<IntervalVar>> interValsPerMachine=new HashMap<>();
        List<IntVar> allEndVars=new ArrayList<>();
        for(int jobIndex=0;jobIndex<this.unsolvedSolution.Jobs.size();jobIndex++){
            List<IntVar> tempStartVars=new ArrayList<>();
            List<IntVar> tempEndVars=new ArrayList<>();
            List<IntervalVar> tempInterValVars=new ArrayList<>();
            for(int taskIndex=0;taskIndex<this.unsolvedSolution.Jobs.get(jobIndex).Tasks.size();taskIndex++){
                Task currentTask=this.unsolvedSolution.Jobs.get(jobIndex).Tasks.get(taskIndex);
                IntVar startV = model.newIntVar(0, horizon,currentTask.Name+"_StartVar");
                int duration = dateTimeConverter.MinToCountValueNum(currentTask.Duration);
                IntVar endV = model.newIntVar(0, horizon, currentTask.Name+"_EndVar");
                IntervalVar interval = model.newIntervalVar(startV, duration, endV, "");

                tempStartVars.add(startV);
                tempEndVars.add(endV);
                tempInterValVars.add(interval);
                allEndVars.add(endV);

                if(!interValsPerMachine.containsKey(currentTask.Machine)){
                    List<IntervalVar> temps=new ArrayList<>();
                    temps.add(interval);
                    interValsPerMachine.put(currentTask.Machine,temps);
                }
                else{
                    interValsPerMachine.get(currentTask.Machine).add(interval);
                }
            }
            startVars.add(tempStartVars);
            endVars.add(tempEndVars);
            interValVars.add(tempInterValVars);
        }

        for(int jobIndex=0;jobIndex<startVars.size();jobIndex++){
            for(int taskIndex=0;taskIndex<startVars.get(jobIndex).size()-1;taskIndex++){
                model.addLessOrEqual(endVars.get(jobIndex).get(taskIndex),startVars.get(jobIndex).get(taskIndex+1));
            }
        }

        for(Map.Entry<String, List<IntervalVar>> entry : interValsPerMachine.entrySet()){
            List<IntervalVar> intervals=entry.getValue();
            model.addNoOverlap(intervals.toArray(new IntervalVar[0]));
        }

        IntVar obj=model.newIntVar(0,horizon,"makespan");

        model.addMaxEquality(obj,allEndVars.toArray(new IntVar[0]));
        model.minimize(obj);

        CpSolver solver=new CpSolver();
        solver.getParameters().setMaxTimeInSeconds(60);
        CpIntermediateSolutionCallback cpIntermediateSolutionCallback=new CpIntermediateSolutionCallback(startVars,endVars,this.unsolvedSolution,this.amqpHandler,this.intermediateSolutionCallback);
        CpSolverStatus status=solver.solveWithSolutionCallback(model,cpIntermediateSolutionCallback);

        if (status == CpSolverStatus.OPTIMAL || status==CpSolverStatus.FEASIBLE){
            for(int jobIndex=0;jobIndex<this.unsolvedSolution.Jobs.size();jobIndex++){
                for(int taskIndex=0;taskIndex<this.unsolvedSolution.Jobs.get(jobIndex).Tasks.size();taskIndex++){
                    Task currentTask=this.unsolvedSolution.Jobs.get(jobIndex).Tasks.get(taskIndex);
                    currentTask.StartTime=dateTimeConverter.CalculateDateFromCountValue(this.unsolvedSolution.MinTime,new Long(solver.value(startVars.get(jobIndex).get(taskIndex))).intValue());
                    currentTask.EndTime=dateTimeConverter.CalculateDateFromCountValue(this.unsolvedSolution.MinTime,new Long(solver.value(endVars.get(jobIndex).get(taskIndex))).intValue());
                }
            }
        }
        else {
            return null;
        }
        this.unsolvedSolution.FinalResult=true;
        return this.unsolvedSolution;
    }


    public class CpIntermediateSolutionCallback extends CpSolverSolutionCallback{
        private List<List<IntVar>> startVars;
        private List<List<IntVar>> endVars;
        private Solution unsolvedSolution;
        private int solutionCount;
        private AMQPHandler amqpHandler;
        private IntermediateSolutionCallback intermediateSolutionCallback;

        private Logger logger;


        public CpIntermediateSolutionCallback(List<List<IntVar>> startVars,
                                              List<List<IntVar>> endVars,
                                              Solution unsolvedSolution,
                                              AMQPHandler amqpHandler,
                                              IntermediateSolutionCallback intermediateSolutionCallback){
            this.startVars=startVars;
            this.endVars=endVars;
            this.unsolvedSolution=unsolvedSolution;
            this.amqpHandler=amqpHandler;
            this.intermediateSolutionCallback=intermediateSolutionCallback;

            this.logger= LoggerFactory.getLogger(OrToolCpSolverWithDirectFormulationModel.class);
        }

        @Override
        public void onSolutionCallback(){
            DateTimeConverter dateTimeConverter=new DateTimeConverter();
            for(int jobIndex=0;jobIndex<this.unsolvedSolution.Jobs.size();jobIndex++){
                for(int taskIndex=0;taskIndex<this.unsolvedSolution.Jobs.get(jobIndex).Tasks.size();taskIndex++){
                    Task currentTask=this.unsolvedSolution.Jobs.get(jobIndex).Tasks.get(taskIndex);
                    currentTask.StartTime=dateTimeConverter.CalculateDateFromCountValue(this.unsolvedSolution.MinTime,new Long(value(startVars.get(jobIndex).get(taskIndex))).intValue());
                    currentTask.EndTime=dateTimeConverter.CalculateDateFromCountValue(this.unsolvedSolution.MinTime,new Long(value(endVars.get(jobIndex).get(taskIndex))).intValue());
                }
            }
            String st=String.format("Solution #%d: time = %.02f s%n , objective value = %f%n", solutionCount, wallTime(), objectiveValue());
            SolutionPrintHelper.printScheduleSolution(this.unsolvedSolution);

            solutionCount++;

            if(this.intermediateSolutionCallback!=null){
                this.unsolvedSolution.FinalResult=false;
                try {
                    this.intermediateSolutionCallback.HandleIntermediateSolution(this.unsolvedSolution,this.amqpHandler);
                } catch (Exception e) {
                    logger.error(e.getMessage(),e);
                }
            }
        }
    }

}
