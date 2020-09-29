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
import wzy.jsp.simplest.demo.domain.ortools.DisjunctiveBinaryVariable;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class OrToolCpSolverWithDisjunctiveGraphModel implements IAlgorithmCore {
    private Solution unsolvedSolution;
    private AMQPHandler amqpHandler;
    private IntermediateSolutionCallback intermediateSolutionCallback;

    public OrToolCpSolverWithDisjunctiveGraphModel(Solution unsolvedSolution,IntermediateSolutionCallback intermediateSolutionCallback,AMQPHandler amqpHandler){
        this.unsolvedSolution=unsolvedSolution;
        this.amqpHandler=amqpHandler;
        this.intermediateSolutionCallback=intermediateSolutionCallback;
    }
    
    @Override
    public Solution Calculate(boolean initialized) throws Exception {
        if(initialized){
            if(this.intermediateSolutionCallback!=null){
                this.intermediateSolutionCallback.HandleIntermediateSolution(this.unsolvedSolution,this.amqpHandler);
            }
        }

        DateTimeConverter dateTimeConverter=new DateTimeConverter();
        CpModel model=new CpModel();
        // Define all variables
        int horizon=dateTimeConverter.CalculateCountValueBetweenDates(this.unsolvedSolution.MinTime,this.unsolvedSolution.MaxTime);
        int M=0;
        for(int jobIndex=0;jobIndex<this.unsolvedSolution.Jobs.size();jobIndex++){
            for(int taskIndex=0;taskIndex<this.unsolvedSolution.Jobs.get(jobIndex).Tasks.size();taskIndex++){
                M+=dateTimeConverter.MinToCountValueNum(this.unsolvedSolution.Jobs.get(jobIndex).Tasks.get(taskIndex).Duration);
            }
        }
        List<List<IntVar>> startVars=new ArrayList<>();
        List<List<IntVar>> durationVars=new ArrayList<>();
        List<List<IntVar>> endVars=new ArrayList<>();
        //key: machine, value: disjunctive arc binary variables
        Map<String,List<DisjunctiveBinaryVariable>> disjunctiveBinaryVariablesPerMachine=new HashMap<>();
        Map<String,List<Task>> tasksPerMachine=new HashMap<>();
        List<IntVar> allEndVars=new ArrayList<>();

        for(int jobIndex=0;jobIndex<this.unsolvedSolution.Jobs.size();jobIndex++){
            List<IntVar> tempStartVars=new ArrayList<>();
            List<IntVar> tempDurationVars=new ArrayList<>();
            List<IntVar> tempEndVars=new ArrayList<>();
            for(int taskIndex=0;taskIndex<this.unsolvedSolution.Jobs.get(jobIndex).Tasks.size();taskIndex++){
                Task currentTask=this.unsolvedSolution.Jobs.get(jobIndex).Tasks.get(taskIndex);
                IntVar startV = model.newIntVar(0, horizon,currentTask.Name+"_StartVar");
                IntVar endV = model.newIntVar(0, horizon, currentTask.Name+"_EndVar");
                int duration = dateTimeConverter.MinToCountValueNum(currentTask.Duration);
                IntVar durationV=model.newIntVar(duration,duration,"");
                tempStartVars.add(startV);
                tempDurationVars.add(durationV);
                tempEndVars.add(endV);

                if(!tasksPerMachine.containsKey(currentTask.Machine)){
                    List<Task> temps=new ArrayList<>();
                    temps.add(currentTask);
                    tasksPerMachine.put(currentTask.Machine,temps);
                }
                else{
                    tasksPerMachine.get(currentTask.Machine).add(currentTask);
                }

                allEndVars.add(endV);
            }
            startVars.add(tempStartVars);
            durationVars.add(tempDurationVars);
            endVars.add(tempEndVars);
        }
        for(Map.Entry<String, List<Task>> entry : tasksPerMachine.entrySet()){
            List<Task> tasks=entry.getValue();
            if(tasks.size()<2){
                continue;
            }
            for(int i=0;i<tasks.size();i++){
                for(int j=0;j<tasks.size();j++){
                    if(i==j){
                        continue;
                    }
                    DisjunctiveBinaryVariable disjunctiveBinaryVariable=new DisjunctiveBinaryVariable();
                    disjunctiveBinaryVariable.taskId_o=tasks.get(i).Id;
                    disjunctiveBinaryVariable.taskId_p=tasks.get(j).Id;
                    disjunctiveBinaryVariable.x=model.newBoolVar("");
                    if(!disjunctiveBinaryVariablesPerMachine.containsKey(entry.getKey())){
                        List<DisjunctiveBinaryVariable> temps=new ArrayList<>();
                        temps.add(disjunctiveBinaryVariable);
                        disjunctiveBinaryVariablesPerMachine.put(entry.getKey(),temps);
                    }
                    else{
                        disjunctiveBinaryVariablesPerMachine.get(entry.getKey()).add(disjunctiveBinaryVariable);
                    }
                }
            }
        }

        //Job sequence constraint
        for(int jobIndex=0;jobIndex<this.unsolvedSolution.Jobs.size();jobIndex++){
            for(int taskIndex=0;taskIndex<this.unsolvedSolution.Jobs.get(jobIndex).Tasks.size();taskIndex++){
                Task currentTask=this.unsolvedSolution.Jobs.get(jobIndex).Tasks.get(taskIndex);
                IntVar startV = startVars.get(jobIndex).get(taskIndex);
                IntVar endV = endVars.get(jobIndex).get(taskIndex);
                IntVar durationV=durationVars.get(jobIndex).get(taskIndex);
                model.addEquality(endV, LinearExpr.sum(new IntVar[]{startV,durationV}));

                if(taskIndex>0){
                    Task preTask=this.unsolvedSolution.Jobs.get(jobIndex).Tasks.get(taskIndex-1);
                    IntVar preEndV=endVars.get(jobIndex).get(taskIndex-1);
                    model.addGreaterOrEqual(startV,preEndV);
                }
            }
        }
        //Disjunctive arc constraint
        for(Map.Entry<String, List<DisjunctiveBinaryVariable>> entry : disjunctiveBinaryVariablesPerMachine.entrySet()){
            List<DisjunctiveBinaryVariable> disjunctiveBinaryVariables=entry.getValue();
            for(DisjunctiveBinaryVariable disjunctiveBinaryVariable:disjunctiveBinaryVariables){
                String id_o=disjunctiveBinaryVariable.taskId_o;
                String id_p=disjunctiveBinaryVariable.taskId_p;
                int jobIndex_o=0;
                int taskIndex_o=0;
                int jobIndex_p=0;
                int taskIndex_p=0;
                for(int jobIndex=0;jobIndex<this.unsolvedSolution.Jobs.size();jobIndex++){
                    for(int taskIndex=0;taskIndex<this.unsolvedSolution.Jobs.get(jobIndex).Tasks.size();taskIndex++){
                        Task currentTask=this.unsolvedSolution.Jobs.get(jobIndex).Tasks.get(taskIndex);
                        if(currentTask.Id.equals(id_o)){
                            jobIndex_o=jobIndex;
                            taskIndex_o=taskIndex;
                        }
                        if(currentTask.Id.equals(id_p)){
                            jobIndex_p=jobIndex;
                            taskIndex_p=taskIndex;
                        }
                    }
                }

                IntVar startVar_o=startVars.get(jobIndex_o).get(taskIndex_o);
                IntVar endVar_o=endVars.get(jobIndex_o).get(taskIndex_o);
                IntVar startVar_p=startVars.get(jobIndex_p).get(taskIndex_p);
                IntVar endVar_p=endVars.get(jobIndex_p).get(taskIndex_p);
                IntVar x=disjunctiveBinaryVariable.x;

                IntVar MV=model.newIntVar(-M,-M,"");
                IntVar x1=model.newIntVar(-M,M,"");
                model.addProductEquality(x1,new IntVar[]{MV,x});
                IntVar x2=model.newIntVar(-M,M,"");
                IntVar nx=model.newBoolVar("");
                model.addDifferent(nx,x);
                model.addProductEquality(x2,new IntVar[]{MV,nx});

                model.addGreaterOrEqual(startVar_o,LinearExpr.sum(new IntVar[]{endVar_p,x1}));
                model.addGreaterOrEqual(startVar_p,LinearExpr.sum(new IntVar[]{endVar_o,x2}));
            }

            for(int i=0;i<disjunctiveBinaryVariables.size();i++){
                for(int j=i+1;j<disjunctiveBinaryVariables.size();j++){
                    if(disjunctiveBinaryVariables.get(i).taskId_o.equals(disjunctiveBinaryVariables.get(j).taskId_p)
                    && disjunctiveBinaryVariables.get(i).taskId_p.equals(disjunctiveBinaryVariables.get(j).taskId_o)){
                        model.addDifferent(disjunctiveBinaryVariables.get(i).x,disjunctiveBinaryVariables.get(j).x);
                    }
                }
            }
        }

        //Objective
        IntVar obj=model.newIntVar(0,horizon,"makespan");

        model.addMaxEquality(obj,allEndVars.toArray(new IntVar[0]));
        model.minimize(obj);

        CpSolver solver=new CpSolver();
        solver.getParameters().setMaxTimeInSeconds(300);
        CpIntermediateDGSolutionCallback solutionCallback=new CpIntermediateDGSolutionCallback(startVars,endVars,this.unsolvedSolution,this.amqpHandler,this.intermediateSolutionCallback);
        CpSolverStatus status=solver.solveWithSolutionCallback(model,solutionCallback);

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


    public class CpIntermediateDGSolutionCallback extends CpSolverSolutionCallback {
        private List<List<IntVar>> startVars;
        private List<List<IntVar>> endVars;
        private Solution unsolvedSolution;
        private int solutionCount;
        private AMQPHandler amqpHandler;
        private IntermediateSolutionCallback intermediateSolutionCallback;

        private Logger logger;


        public CpIntermediateDGSolutionCallback(List<List<IntVar>> startVars,
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
