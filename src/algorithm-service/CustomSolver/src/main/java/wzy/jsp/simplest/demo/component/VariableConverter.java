package wzy.jsp.simplest.demo.component;

import wzy.jsp.simplest.demo.domain.algorithm.represent.DelayTimeRepresentModel;
import wzy.jsp.simplest.demo.domain.communication.Job;
import wzy.jsp.simplest.demo.domain.communication.Solution;
import wzy.jsp.simplest.demo.domain.communication.Task;
import wzy.jsp.simplest.demo.domain.optaplanner.delaytimemodel.*;
import wzy.jsp.simplest.demo.domain.optaplanner.delaytimemodel.resource.GlobalResource;
import wzy.jsp.simplest.demo.domain.optaplanner.delaytimemodel.resource.Resource;

import java.util.*;
import java.util.stream.Collectors;

/**
 * Use this converter to do variable type convert operation between business domain and algorithm logic
 * Current algorithm use a simple JSP model, the decision variables are the delay time of each task
 */
public class VariableConverter {
    private DateTimeConverter dateTimeConverter;

    public VariableConverter(DateTimeConverter dateTimeConverter){
        this.dateTimeConverter=dateTimeConverter;
    }

    //Convert solution object into DelayTimeRepresentModel
    //If solved==true, init es to correct values
    //If solved==false, init es to zeros
    public DelayTimeRepresentModel getDelayTimeRepresentModelFromSolution(Solution solution, boolean solved){
        DelayTimeRepresentModel delayTimeRepresentModel=new DelayTimeRepresentModel();
        delayTimeRepresentModel.timeUpperLimit=this.dateTimeConverter.CalculateCountValueBetweenDates(
                solution.MinTime,solution.MaxTime
        );
        delayTimeRepresentModel.decisionUpperLimit=this.dateTimeConverter.MinToCountValueNum(6*60);

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
        delayTimeRepresentModel.es=es;
        delayTimeRepresentModel.durations=durations;
        delayTimeRepresentModel.machines=machines;
        delayTimeRepresentModel.occupies=occupies;
        return delayTimeRepresentModel;
    }

    //After calculation of algorithm, we need calculate the actual start time of each task from delay times
    public Solution getScheduledSolutionFromDelayTimeRepresentModel(DelayTimeRepresentModel delayTimeRepresentModel, Solution unsolvedSolution){
        for(int i=0;i<unsolvedSolution.Jobs.size();i++){
            List<Integer> esThisJob=delayTimeRepresentModel.es.get(i);
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
    public List<List<Integer>> getStartTimesFromEs(DelayTimeRepresentModel delayTimeRepresentModel){
        List<List<Integer>> startTimes=new ArrayList<>();
        for(int i=0;i<delayTimeRepresentModel.es.size();i++){
            List<Integer> startTimesThisJob=new ArrayList<>();
            int lastStartTime=0;
            int lastDuration=0;
            for(int j=0;j<delayTimeRepresentModel.es.get(i).size();j++){
                int startTimeThisTask=lastStartTime+lastDuration+delayTimeRepresentModel.es.get(i).get(j);
                startTimesThisJob.add(startTimeThisTask);
                lastStartTime=startTimeThisTask;
                lastDuration=delayTimeRepresentModel.durations.get(i).get(j);
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

}
