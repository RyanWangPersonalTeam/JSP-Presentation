package wzy.jsp.simplest.demo.component;

import org.javatuples.Pair;
import org.javatuples.Triplet;
import wzy.jsp.simplest.demo.domain.algorithm.represent.*;
import wzy.jsp.simplest.demo.domain.communication.Solution;
import wzy.jsp.simplest.demo.domain.communication.Task;
import wzy.jsp.simplest.demo.domain.optaplanner.directformulationmodel.*;
import wzy.jsp.simplest.demo.domain.optaplanner.directformulationmodel.resource.GlobalResource;
import wzy.jsp.simplest.demo.domain.optaplanner.directformulationmodel.resource.Resource;

import java.util.*;

/**
 * Use this converter to do variable type convert operation between business domain and algorithm logic
 * Current algorithm use a simple JSP model, the decision variables are the delay time of each task
 */
public class VariableConverter {


    private DateTimeConverter dateTimeConverter;

    public VariableConverter(DateTimeConverter dateTimeConverter){
        this.dateTimeConverter=dateTimeConverter;
    }

    public DateTimeConverter getDateTimeConverter() {
        return dateTimeConverter;
    }

    //Convert solution object into DelayTimeRepresentModel
    //If solved==true, init es to correct values
    //If solved==false, init es to zeros
    public DirectFormulationModel getDirectFormulationRepresentModelFromSolution(Solution solution, boolean solved){
        DirectFormulationModel directFormulationModel =new DirectFormulationModel();
        directFormulationModel.timeUpperLimit=this.dateTimeConverter.CalculateCountValueBetweenDates(
                solution.MinTime,solution.MaxTime
        );
        directFormulationModel.decisionUpperLimit=this.dateTimeConverter.MinToCountValueNum(6*60);

        List<List<Integer>> es=new ArrayList<>();
        List<List<Integer>> durations=new ArrayList<>();
        String[] machines=null;
        List<List<int[]>> occupies=new ArrayList<>();

        //es
        for(int i=0;i<solution.Jobs.size();i++){
            List<Integer> esThisJob=new ArrayList<>();
            esThisJob.add(0);
            int lastStartTime=0;
            int lastDuration=0;
            for(int j=0;j<solution.Jobs.get(i).Tasks.size();j++){
                Task currentTask=solution.Jobs.get(i).Tasks.get(j);
                if(solved){
                    int lastEndTime=lastStartTime+lastDuration;
                    int currentTaskStartTime=this.dateTimeConverter.CalculateCountValueBetweenDates(
                            solution.MinTime,
                            currentTask.StartTime
                    );
                    int e=currentTaskStartTime-lastEndTime;
                    esThisJob.add(e);
                    lastStartTime=currentTaskStartTime;
                    lastDuration=this.dateTimeConverter.MinToCountValueNum(currentTask.Duration);
                }
                else{
                    esThisJob.add(0);
                }
            }
            esThisJob.add(0);
            es.add(esThisJob);
        }
        //durations
        for(int i=0;i<solution.Jobs.size();i++){
            List<Integer> dsThisJob=new ArrayList<>();
            dsThisJob.add(0);
            for(int j=0;j<solution.Jobs.get(i).Tasks.size();j++){
                Task currentTask=solution.Jobs.get(i).Tasks.get(j);
                dsThisJob.add(this.dateTimeConverter.MinToCountValueNum(currentTask.Duration));
            }
            dsThisJob.add(0);
            durations.add(dsThisJob);
        }
        //Machines
        Set<String> machineSet=new HashSet<>();
        for(int i=0;i<solution.Jobs.size();i++){
            List<Integer> dsThisJob=new ArrayList<>();
            for(int j=0;j<solution.Jobs.get(i).Tasks.size();j++){
                Task currentTask=solution.Jobs.get(i).Tasks.get(j);
                machineSet.add(currentTask.Machine);
            }
        }
        machines=machineSet.toArray(new String[0]);
        //Occupies
        for(int i=0;i<solution.Jobs.size();i++){
            List<int[]> occupiesThisJob=new ArrayList<>();
            occupiesThisJob.add(new int[machines.length]);
            for(int j=0;j<solution.Jobs.get(i).Tasks.size();j++){
                Task currentTask=solution.Jobs.get(i).Tasks.get(j);
                int[] occupiesThisTask=new int[machines.length];
                for(int k=0;k<machines.length;k++){
                    if(machines[k].compareTo(currentTask.Machine)==0){
                        occupiesThisTask[k]=1;
                    }
                }
                occupiesThisJob.add(occupiesThisTask);
            }
            occupiesThisJob.add(new int[machines.length]);
            occupies.add(occupiesThisJob);
        }
        directFormulationModel.es=es;
        directFormulationModel.durations=durations;
        directFormulationModel.machines=machines;
        directFormulationModel.occupies=occupies;
        return directFormulationModel;
    }

    //After calculation of algorithm, we need calculate the actual start time of each task from delay times
    public Solution getScheduledSolutionFromDirectFormulationRepresentModel(DirectFormulationModel directFormulationModel, Solution unsolvedSolution){
        for(int i=0;i<unsolvedSolution.Jobs.size();i++){
            List<Integer> esThisJob= directFormulationModel.es.get(i);
            int lastStartTime=esThisJob.get(0);
            int lastDuration=0;
            for(int j=0;j<unsolvedSolution.Jobs.get(i).Tasks.size();j++){
                Task currentTask=unsolvedSolution.Jobs.get(i).Tasks.get(j);
                int countValue=lastStartTime+lastDuration+esThisJob.get(j+1);
                currentTask.StartTime=this.dateTimeConverter.CalculateDateFromCountValue(unsolvedSolution.MinTime,countValue);
                lastStartTime=countValue;
                lastDuration=this.dateTimeConverter.MinToCountValueNum(currentTask.Duration);
                currentTask.EndTime=this.dateTimeConverter.CalculateDateFromCountValue(currentTask.StartTime,lastDuration);
            }
        }

        return unsolvedSolution;
    }

    //Calculate start times (count value) of each task from es
    //i=0,..., N-1, N is job num
    //j=0,..., M, M is task num, j=0 and j=M is fake tasks, task[i][0]=0
    public List<List<Integer>> getStartTimesFromEs(DirectFormulationModel directFormulationModel){
        List<List<Integer>> startTimes=new ArrayList<>();
        for(int i = 0; i< directFormulationModel.es.size(); i++){
            List<Integer> startTimesThisJob=new ArrayList<>();
            int lastStartTime=0;
            int lastDuration=0;
            for(int j = 0; j< directFormulationModel.es.get(i).size(); j++){
                int startTimeThisTask=lastStartTime+lastDuration+ directFormulationModel.es.get(i).get(j);
                startTimesThisJob.add(startTimeThisTask);
                lastStartTime=startTimeThisTask;
                lastDuration= directFormulationModel.durations.get(i).get(j);
            }
            startTimes.add(startTimesThisJob);
        }
        return startTimes;
    }



    public Schedule GetOptaPlannerScheduleInstanceFromSolution(Solution solution, boolean initialized){

        List<OptaTask> allTasks=new ArrayList<>();
        List<Allocation> allAllocations=new ArrayList<>();
        List<Resource> machineResources=new ArrayList<>();

        Set<String> machineNames=new HashSet<>();
        for(int i=0;i<solution.Jobs.size();i++){
            for(int j=0;j<solution.Jobs.get(i).Tasks.size();j++){
                String machineName=solution.Jobs.get(i).Tasks.get(j).Machine;
                if(!machineNames.contains(machineName)){
                    machineResources.add(new GlobalResource(UUID.randomUUID(),machineName,1));
                    machineNames.add(machineName);
                }
            }
        }

        int jobNum=solution.Jobs.size();
        for(int jobIndex=0;jobIndex<jobNum;jobIndex++){
            OptaJob optaJob=new OptaJob(UUID.fromString(solution.Jobs.get(jobIndex).Id),solution.Jobs.get(jobIndex).Name,0,null);
            List<OptaTask> tasksThisJob=new ArrayList<>();
            int taskNum=solution.Jobs.get(jobIndex).Tasks.size();
            for(int taskIndex=0;taskIndex<taskNum;taskIndex++){
                List<ExecutionMode> optionalExecutionModesThisTask=new ArrayList<>();
                List<ResourceRequirement> resourceRequirementsThisExecutionMode=new ArrayList<>();
                int machineIndex=0;
                for(int t=0;t<machineResources.size();t++){
                    if(machineResources.get(t).getName().equals(solution.Jobs.get(jobIndex).Tasks.get(taskIndex).Machine)){
                        machineIndex=t;
                        break;
                    }
                }
                resourceRequirementsThisExecutionMode.add(new ResourceRequirement(UUID.randomUUID(),null,machineResources.get(machineIndex),1));
                ExecutionMode optionalExecutionMode=new ExecutionMode(UUID.randomUUID(),null,dateTimeConverter.MinToCountValueNum(solution.Jobs.get(jobIndex).Tasks.get(taskIndex).Duration),resourceRequirementsThisExecutionMode);
                for(ResourceRequirement resourceRequirement : optionalExecutionMode.getResourceRequirementsThisMode()){
                    resourceRequirement.setExecutionMode(optionalExecutionMode);
                }
                optionalExecutionModesThisTask.add(optionalExecutionMode);
                tasksThisJob.add(new OptaTask(
                        UUID.fromString(solution.Jobs.get(jobIndex).Tasks.get(taskIndex).Id),
                        solution.Jobs.get(jobIndex).Tasks.get(taskIndex).Name,
                        optaJob,
                        optionalExecutionModesThisTask,
                        solution.Jobs.get(jobIndex).Tasks.get(taskIndex).StartTime));
                for(ExecutionMode executionMode : tasksThisJob.get(taskIndex).getOptionalExecutionModes()){
                    executionMode.setTask(tasksThisJob.get(taskIndex));
                }
            }
            for(int taskIndex=0;taskIndex<taskNum;taskIndex++){
                if(taskIndex==0){
                    tasksThisJob.get(taskIndex).setPredecessorTask(null);
                }else {
                    tasksThisJob.get(taskIndex).setPredecessorTask(tasksThisJob.get(taskIndex-1));
                }

                if(taskIndex==taskNum-1){
                    tasksThisJob.get(taskIndex).setSuccessorTask(null);
                }else {
                    tasksThisJob.get(taskIndex).setSuccessorTask(tasksThisJob.get(taskIndex+1));
                }
            }
            optaJob.setTasksThisJob(tasksThisJob);
            allTasks.addAll(optaJob.getTasksThisJob());

            List<Allocation> allocations=new ArrayList<>();
            for(OptaTask task : tasksThisJob){
                Allocation allocation=new Allocation(UUID.randomUUID(),task.getName(),task);
                allocations.add(allocation);
            }
            for(int index=0;index<allocations.size();index++){
                if(index==0){
                    allocations.get(index).setPredecessorTaskAllocation(null);
                }else {
                    allocations.get(index).setPredecessorTaskAllocation(allocations.get(index-1));
                }

                if(index==taskNum-1){
                    allocations.get(index).setSuccessorTaskAllocation(null);
                }else {
                    allocations.get(index).setSuccessorTaskAllocation(allocations.get(index+1));
                }
            }
            //If the input is initialized, we need initialize the plan variables of Allocation objects too
            if(initialized){
                for(int index=0;index<allocations.size();index++){
                    Allocation allocation=allocations.get(index);
                    allocation.setSelectedExecutionMode(allocation.getTask().getOptionalExecutionModes().get(0));
                    allocation.setDelayAfterPredecessor(
                            this.dateTimeConverter.CalculateCountValueBetweenDates(
                                    solution.MinTime,
                                    allocation.getTask().getDefaultStartTime()
                            )
                    );
                    if(allocation.getPredecessorTaskAllocation()==null){
                        allocation.setPredecessorsEndTime(allocation.getDelayAfterPredecessor());
                    }
                    else{
                        allocation.setPredecessorsEndTime(
                                allocation.getPredecessorTaskAllocation().getEndTime()
                        );
                    }
                }
            }

            allAllocations.addAll(allocations);
        }
        Schedule schedule=new Schedule(UUID.randomUUID(),null,allAllocations);
        schedule.setResources(machineResources);
        return schedule;
    }

    Map<String,Integer> jobIndexRecordByTaskId=new HashMap<>();
    Map<String,Integer> taskIndexRecordByTaskId=new HashMap<>();
    public Solution GetSolutionFromOptaPlannerScheduleInstance(Schedule schedule,Solution unsolvedSolution){
        for(Allocation allocation:schedule.getAllocationList()){
            int jobIndex=0;
            int taskIndex=0;
            String taskId=allocation.getTask().getId().toString();
            if(jobIndexRecordByTaskId.containsKey(taskId)){
                jobIndex=jobIndexRecordByTaskId.get(taskId);
                taskIndex=taskIndexRecordByTaskId.get(taskId);
            }
            else{
                boolean breakOut=false;
                for(int i=0;i<unsolvedSolution.Jobs.size();i++){
                    if(breakOut){
                        break;
                    }
                    for(int j=0;j<unsolvedSolution.Jobs.get(i).Tasks.size();j++){
                        if(unsolvedSolution.Jobs.get(i).Tasks.get(j).Id.equals(taskId)){
                            jobIndex=i;
                            taskIndex=j;
                            breakOut=true;
                            break;
                        }

                    }
                }
                jobIndexRecordByTaskId.put(taskId,jobIndex);
                taskIndexRecordByTaskId.put(taskId,taskIndex);
            }

            Task currentTask=unsolvedSolution.Jobs.get(jobIndex).Tasks.get(taskIndex);
            currentTask.StartTime=this.dateTimeConverter.CalculateDateFromCountValue(
                    unsolvedSolution.MinTime,
                    allocation.getStartTime()
            );
            currentTask.EndTime=this.dateTimeConverter.CalculateDateFromCountValue(
                    unsolvedSolution.MinTime,
                    allocation.getEndTime()
            );
        }
        return unsolvedSolution;
    }


    public DisjunctiveGraphModel getDisjunctiveGraphFromSolution(Solution solution, boolean solved){
        List<NodeInDG> nodes=new ArrayList<>();
        List<ConjunctiveArc> conjunctiveArcs=new ArrayList<>();

        NodeInDG sourceNode=new NodeInDG();
        sourceNode.processTime=0;
        NodeInDG sinkNode=new NodeInDG();
        sinkNode.processTime=0;
        nodes.add(sourceNode);
        nodes.add(sinkNode);

        Map<String,List<NodeInDG>> nodesPerJob=new HashMap<>();
        Map<String,List<NodeInDG>> nodesPerMachine=new HashMap<>();
        for(int jobIndex=0;jobIndex<solution.Jobs.size();jobIndex++){
            NodeInDG lastNode=sourceNode;
            List<NodeInDG> nodesThisJob=new ArrayList<>();
            nodesThisJob.add(sourceNode);
            nodesPerJob.put(solution.Jobs.get(jobIndex).Id,nodesThisJob);
            for(int taskIndex=0;taskIndex<solution.Jobs.get(jobIndex).Tasks.size();taskIndex++){
                NodeInDG currentNode=new NodeInDG();
                currentNode.taskIndexs=new Pair<Integer, Integer>(jobIndex,taskIndex);
                currentNode.processTime=this.dateTimeConverter.MinToCountValueNum(solution.Jobs.get(jobIndex).Tasks.get(taskIndex).Duration);
                nodes.add(currentNode);
                nodesPerJob.get(solution.Jobs.get(jobIndex).Id).add(currentNode);

                ConjunctiveArc conjunctiveArc= new ConjunctiveArc();
                conjunctiveArc.fromNode=lastNode;
                conjunctiveArc.toNode=currentNode;
                conjunctiveArcs.add(conjunctiveArc);
                lastNode=currentNode;

                if(!nodesPerMachine.containsKey(solution.Jobs.get(jobIndex).Tasks.get(taskIndex).Machine)){
                    List<NodeInDG> nodesThisMachine=new ArrayList<>();
                    nodesThisMachine.add(currentNode);
                    nodesPerMachine.put(solution.Jobs.get(jobIndex).Tasks.get(taskIndex).Machine, nodesThisMachine);
                }
                else{
                    nodesPerMachine.get(solution.Jobs.get(jobIndex).Tasks.get(taskIndex).Machine).add(currentNode);
                }
            }
            ConjunctiveArc conjunctiveArc= new ConjunctiveArc();
            conjunctiveArc.fromNode=lastNode;
            conjunctiveArc.toNode=sinkNode;
            conjunctiveArcs.add(conjunctiveArc);
        }

        List<DisjunctiveArc> disjunctiveArcs=new ArrayList<>();
        for(Map.Entry<String, List<NodeInDG>> entry : nodesPerMachine.entrySet()){
            String machineName=entry.getKey();
            List<NodeInDG> nodesThisMachine=entry.getValue();
            if(nodesThisMachine.size()==1){
                DisjunctiveArc disjunctiveArc=new DisjunctiveArc();
                disjunctiveArc.leftNode=null;
                disjunctiveArc.rightNode=null;
                disjunctiveArc.machine=machineName;
                disjunctiveArc.left2right=false;
                disjunctiveArcs.add(disjunctiveArc);
                continue;
            }
            for(int i=0;i<nodesThisMachine.size();i++){
                for(int j=i+1;j<nodesThisMachine.size();j++){
                    DisjunctiveArc disjunctiveArc=new DisjunctiveArc();
                    disjunctiveArc.leftNode=nodesThisMachine.get(i);
                    disjunctiveArc.rightNode=nodesThisMachine.get(j);
                    disjunctiveArc.machine=machineName;
                    if(solved){
                        Task task_i=solution.Jobs.get(disjunctiveArc.leftNode.taskIndexs.getValue0()).Tasks.get(disjunctiveArc.leftNode.taskIndexs.getValue1());
                        Task task_j=solution.Jobs.get(disjunctiveArc.rightNode.taskIndexs.getValue0()).Tasks.get(disjunctiveArc.rightNode.taskIndexs.getValue1());
                        if(task_i.compareTo(task_j)<0){
                            disjunctiveArc.left2right=true;
                        }
                        else{
                            disjunctiveArc.left2right=false;
                        }
                    }
                    else{
                        disjunctiveArc.left2right=null;
                    }
                    disjunctiveArcs.add(disjunctiveArc);
                }
            }
        }

        DisjunctiveGraphModel disjunctiveGraphModel=new DisjunctiveGraphModel();
        disjunctiveGraphModel.nodesInDG=nodes;
        disjunctiveGraphModel.conjunctiveArcs=conjunctiveArcs;
        disjunctiveGraphModel.disjunctiveArcs=disjunctiveArcs;
        disjunctiveGraphModel.nodesPerJob=nodesPerJob;
        disjunctiveGraphModel.nodesPerMachine=nodesPerMachine;

        if(solved){
            disjunctiveGraphModel=this.initPermutation(disjunctiveGraphModel);
        }

        return disjunctiveGraphModel;
    }

    //Set permutation to correct arrangement (the disjunctive graph must be completed)
    private DisjunctiveGraphModel initPermutation(DisjunctiveGraphModel disjunctiveGraphModel){
        Map<String,List<NodeInDG>> nodesPerMachine=disjunctiveGraphModel.nodesPerMachine;
        List<DisjunctiveArc> disjunctiveArcs=disjunctiveGraphModel.disjunctiveArcs;
        for(Map.Entry<String, List<NodeInDG>> entry : nodesPerMachine.entrySet()){
            String machineName=entry.getKey();
            List<NodeInDG> nodesThisMachine=entry.getValue();
            List<NodeInDG> newNodesThisMachine=new ArrayList<NodeInDG>(Collections.nCopies(nodesThisMachine.size(),null));
            Map<NodeInDG,Integer> outflowStatistics=new HashMap<>();
            for(DisjunctiveArc disjunctiveArc : disjunctiveArcs){
                if(!disjunctiveArc.machine.equals(machineName) || !disjunctiveArc.left2right){
                    continue;
                }
                if(!outflowStatistics.containsKey(disjunctiveArc.leftNode)){
                    outflowStatistics.put(disjunctiveArc.leftNode,1);
                }
                else{
                    outflowStatistics.replace(disjunctiveArc.leftNode,outflowStatistics.get(disjunctiveArc.leftNode)+1);
                }
            }
            for(NodeInDG node : nodesThisMachine){
                if(!outflowStatistics.containsKey(node)){
                    outflowStatistics.put(node,0);
                }
            }
            for(Map.Entry<NodeInDG,Integer> v : outflowStatistics.entrySet()){
                Integer arrangeIndex=nodesThisMachine.size()-1-v.getValue();
                newNodesThisMachine.set(arrangeIndex,v.getKey());
            }
            nodesThisMachine=newNodesThisMachine;
        }
        return disjunctiveGraphModel;
    }

    //Schedule decode algorithm to get the actual schedule result from semi-active disjunctive graph model
    public Solution getSolutionFromDisjunctiveGraph(Solution unsolvedSolution, DisjunctiveGraphModel semiActiveDGModel) {
        //Pair.item1: job index,  Pair.item2: task index
        Set<Pair<Integer,Integer>> earliestTasks=new HashSet<>();
        //Key: task_i_j,  Value: this task is scheduled
        Map<Pair<Integer,Integer>,Boolean> taskScheduledMap=new HashMap<>();
        Pair<Integer,Integer> selectTask=null;
        String selectMachine=null;
        Map<String,String> earliestAvailableTimeEachMachine=new HashMap<>();


        //Init
        for(int jobIndex=0;jobIndex<unsolvedSolution.Jobs.size();jobIndex++){
            earliestTasks.add(new Pair<Integer, Integer>(jobIndex,0));
        }
        for(int jobIndex=0;jobIndex<unsolvedSolution.Jobs.size();jobIndex++){
            for(int taskIndex=0;taskIndex<unsolvedSolution.Jobs.get(jobIndex).Tasks.size();taskIndex++){
                taskScheduledMap.put(new Pair<Integer, Integer>(jobIndex,taskIndex),false);
            }
        }
        for(String machine : semiActiveDGModel.nodesPerMachine.keySet()){
            earliestAvailableTimeEachMachine.put(machine,unsolvedSolution.MinTime);
        }

        //Main loop
        while(!earliestTasks.isEmpty()){
            selectTask=null;
            //Key: jnext(j),
            //Value: <machine, index of mnext(jnext(j).machine), index of jnext(j)>
            Map<Pair<Integer,Integer>,Triplet<String,Integer,Integer>> globalRepairMarks=new HashMap<>();
            for(Pair<Integer,Integer> scheduleableTask: earliestTasks){
                if(selectTask!=null){
                    break;
                }
                for(Map.Entry<String, List<NodeInDG>> v : semiActiveDGModel.nodesPerMachine.entrySet()){
                    String machineName=v.getKey();
                    List<NodeInDG> permutationThisMachine=v.getValue();
                    int startPos=0;
                    int endPos=0;
                    boolean inThisMachine=false;
                    for(int i=0;i<permutationThisMachine.size();i++){
                        NodeInDG node = permutationThisMachine.get(i);
                        if(taskScheduledMap.get(new Pair<Integer,Integer>(node.taskIndexs.getValue0(),node.taskIndexs.getValue1()))){
                            startPos=i+1;
                            endPos=i+1;
                            continue;
                        }
                        if(node.taskIndexs.getValue0()==scheduleableTask.getValue0()
                        && node.taskIndexs.getValue1()==scheduleableTask.getValue1()
                        ){
                            endPos=i;
                            inThisMachine=true;
                            if(startPos==endPos){
                                selectTask=node.taskIndexs;
                                selectMachine=machineName;
                            }
                            break;
                        }
                        else{
                            startPos=i;
                            //break;
                        }

                    }

                    if(inThisMachine){
                        globalRepairMarks.put(
                                new Pair<Integer, Integer>(scheduleableTask.getValue0(),scheduleableTask.getValue1()),
                                new Triplet<String, Integer, Integer>(
                                        machineName,
                                        startPos,
                                        endPos
                                )
                        );
                    }

                }

            }

            if(selectTask==null){ //If selectTask is null, means deadlock detected
                String minDistanceMachine=null;
                Integer minDistance=Integer.MAX_VALUE;
                Pair<Integer,Integer> minDistanceSelectTask=null;
                for(Pair<Integer,Integer> scheduleableTask: earliestTasks){
                    Triplet<String,Integer,Integer> globalRepairMark=globalRepairMarks.get(new Pair<Integer,Integer>(scheduleableTask.getValue0(),scheduleableTask.getValue1()));
                    Integer d=globalRepairMark.getValue2()-globalRepairMark.getValue1();
                    if(minDistanceMachine==null || d<minDistance){
                        minDistance=d;
                        minDistanceMachine=globalRepairMark.getValue0();
                        minDistanceSelectTask=scheduleableTask;
                    }
                }

                //Right shift
                List<NodeInDG> nodes=semiActiveDGModel.nodesPerMachine.get(minDistanceMachine);
                Triplet<String,Integer,Integer> globalRepairMark=globalRepairMarks.get(new Pair<Integer,Integer>(minDistanceSelectTask.getValue0(),minDistanceSelectTask.getValue1()));
                NodeInDG temp=nodes.get(globalRepairMark.getValue1());
                for(int j=globalRepairMark.getValue1();j<globalRepairMark.getValue2();j++){
                    nodes.set(j,nodes.get(j+1));
                }
                nodes.set(globalRepairMark.getValue2(),temp);
                semiActiveDGModel.nodesPerMachine.replace(minDistanceMachine,nodes);
                continue;
            }
            else{
                String predecessorEndtime=unsolvedSolution.MinTime;
                if(selectTask.getValue1()>0){
                    predecessorEndtime=unsolvedSolution.Jobs.get(selectTask.getValue0()).Tasks.get(selectTask.getValue1()-1).EndTime;
                }
                String earliestAvailableTimeThisMachine=earliestAvailableTimeEachMachine.get(selectMachine);
                unsolvedSolution.Jobs.get(selectTask.getValue0()).Tasks.get(selectTask.getValue1()).StartTime=
                        this.dateTimeConverter.CalculateCountValueBetweenDates(predecessorEndtime,earliestAvailableTimeThisMachine)<0?
                                predecessorEndtime:earliestAvailableTimeThisMachine;
                unsolvedSolution.Jobs.get(selectTask.getValue0()).Tasks.get(selectTask.getValue1()).EndTime=
                        this.dateTimeConverter.CalculateDateFromCountValue(
                                unsolvedSolution.Jobs.get(selectTask.getValue0()).Tasks.get(selectTask.getValue1()).StartTime,
                                this.dateTimeConverter.MinToCountValueNum(unsolvedSolution.Jobs.get(selectTask.getValue0()).Tasks.get(selectTask.getValue1()).Duration)
                        );

                //After a task scheduled
                taskScheduledMap.put(new Pair<Integer, Integer>(selectTask.getValue0(),selectTask.getValue1()),true);
                earliestAvailableTimeEachMachine.replace(selectMachine,unsolvedSolution.Jobs.get(selectTask.getValue0()).Tasks.get(selectTask.getValue1()).EndTime);
                earliestTasks.remove(selectTask);
                if(selectTask.getValue1()<unsolvedSolution.Jobs.get(selectTask.getValue0()).Tasks.size()-1){
                    earliestTasks.add(new Pair<Integer, Integer>(
                            selectTask.getValue0(),
                            selectTask.getValue1()+1
                    ));
                }
            }
        }
        return unsolvedSolution;
    }

    public Map<String, List<NodeInDG>> deepCopyPermutation(Map<String, List<NodeInDG>> permutation){
        Map<String, List<NodeInDG>> taskPermutation=new HashMap<>();
        for(Map.Entry<String, List<NodeInDG>> v : permutation.entrySet()){
            String machineName=v.getKey();
            List<NodeInDG> nodes=v.getValue();
            List<NodeInDG> newNodes=new ArrayList<>();
            for(int i=0;i<nodes.size();i++){
                newNodes.add(nodes.get(i));
            }
            taskPermutation.put(machineName,newNodes);
        }
        return taskPermutation;
    }

    public Integer getMakespanFromSolution(Solution solution){
        String maxEndTime=null;
        for(int jobIndex=0;jobIndex<solution.Jobs.size();jobIndex++){
            String endTime=solution.Jobs.get(jobIndex).Tasks.get(
                    solution.Jobs.get(jobIndex).Tasks.size()-1
            ).EndTime;
            if(maxEndTime==null || this.dateTimeConverter.CalculateCountValueBetweenDates(maxEndTime,endTime)>0){
                maxEndTime=endTime;
            }
        }
        Integer makespan=this.dateTimeConverter.CalculateCountValueBetweenDates(
                solution.MinTime,
                maxEndTime
        );
        return makespan;
    }

}
