package wzy.jsp.simplest.demo.domain.optaplanner.delaytimemodel.test;

import wzy.jsp.simplest.demo.domain.optaplanner.delaytimemodel.*;
import wzy.jsp.simplest.demo.domain.optaplanner.delaytimemodel.resource.Resource;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

public class TestScheduleInstaneGenerator {
    public static Schedule getTestUnSolvedScheduleInstance(int jobNum,
                                                           int taskNumPerJob,
                                                           List<Resource> resourcesType1,
                                                           int durationLowLimit,
                                                           int durationUpLimit
    ){
        List<OptaTask> allTasks=new ArrayList<>();
        List<Allocation> allAllocations=new ArrayList<>();
        for(int jobIndex=0;jobIndex<jobNum;jobIndex++){
            OptaJob testJob=new OptaJob(UUID.randomUUID(),"Job_"+jobIndex,0,null);
            List<OptaTask> tasksThisJob=new ArrayList<>();
            for(int taskIndex=0;taskIndex<taskNumPerJob;taskIndex++){
                List<ExecutionMode> optionalExecutionModesThisTask=new ArrayList<>();
                Integer optionalExecutionModesCount=getRandomInteger(1,resourcesType1.size());
                List<Integer> mark=new ArrayList<>();
                for(int i=0;i<resourcesType1.size();i++){
                    mark.add(0);
                }
                for(int i=0;i<optionalExecutionModesCount;i++){
                    List<ResourceRequirement> resourceRequirementsThisExecutionMode=new ArrayList<>();
                    Integer selectResourceIndex=getRandomInteger(0,resourcesType1.size()-1);
                    while(mark.get(selectResourceIndex)==1){
                        selectResourceIndex=getRandomInteger(0,resourcesType1.size()-1);
                    }
                    mark.set(selectResourceIndex,1);
                    resourceRequirementsThisExecutionMode.add(new ResourceRequirement(UUID.randomUUID(),null,resourcesType1.get(selectResourceIndex),1));
                    ExecutionMode optionalExecutionMode=new ExecutionMode(UUID.randomUUID(),null,getRandomInteger(durationLowLimit,durationUpLimit),resourceRequirementsThisExecutionMode);
                    for(ResourceRequirement resourceRequirement : optionalExecutionMode.getResourceRequirementsThisMode()){
                        resourceRequirement.setExecutionMode(optionalExecutionMode);
                    }
                    optionalExecutionModesThisTask.add(optionalExecutionMode);
                }
                tasksThisJob.add(new OptaTask(UUID.randomUUID(),"Task_"+jobIndex+"_"+taskIndex,testJob,optionalExecutionModesThisTask,null));
                for(ExecutionMode executionMode : tasksThisJob.get(taskIndex).getOptionalExecutionModes()){
                    executionMode.setTask(tasksThisJob.get(taskIndex));
                }
            }
            for(int taskIndex=0;taskIndex<taskNumPerJob;taskIndex++){
                if(taskIndex==0){
                    tasksThisJob.get(taskIndex).setPredecessorTask(null);
                }else {
                    tasksThisJob.get(taskIndex).setPredecessorTask(tasksThisJob.get(taskIndex-1));
                }

                if(taskIndex==taskNumPerJob-1){
                    tasksThisJob.get(taskIndex).setSuccessorTask(null);
                }else {
                    tasksThisJob.get(taskIndex).setSuccessorTask(tasksThisJob.get(taskIndex+1));
                }
            }
            testJob.setTasksThisJob(tasksThisJob);
            allTasks.addAll(testJob.getTasksThisJob());

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

                if(index==taskNumPerJob-1){
                    allocations.get(index).setSuccessorTaskAllocation(null);
                }else {
                    allocations.get(index).setSuccessorTaskAllocation(allocations.get(index+1));
                }
            }
            allAllocations.addAll(allocations);
        };
        Schedule schedule=new Schedule(UUID.randomUUID(),null,allAllocations);
        schedule.setResources(resourcesType1);
        return schedule;
    }

    public static int getRandomInteger(int min,int max){
        return (int)(min+Math.random()*(max-min+1));
    }
}
