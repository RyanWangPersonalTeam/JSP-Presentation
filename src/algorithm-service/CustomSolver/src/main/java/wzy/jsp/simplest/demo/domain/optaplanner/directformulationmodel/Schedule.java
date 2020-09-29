package wzy.jsp.simplest.demo.domain.optaplanner.directformulationmodel;

import org.optaplanner.core.api.domain.solution.PlanningEntityCollectionProperty;
import org.optaplanner.core.api.domain.solution.PlanningScore;
import org.optaplanner.core.api.domain.solution.PlanningSolution;
import org.optaplanner.core.api.domain.solution.drools.ProblemFactCollectionProperty;
import org.optaplanner.core.api.score.buildin.bendable.BendableScore;
import wzy.jsp.simplest.demo.domain.optaplanner.directformulationmodel.common.AbstractPersistable;
import wzy.jsp.simplest.demo.domain.optaplanner.directformulationmodel.resource.Resource;

import java.util.*;

@PlanningSolution
public class Schedule extends AbstractPersistable {
    private Map<OptaJob, List<Allocation>> taskAllocationsGroupByJob = new HashMap<>();
    private Map<Resource, List<Allocation>> taskAllocationsGroupByResource = new HashMap<>();
    private List<Allocation> allocationList;

    private List<ResourceRequirement> resourceRequirements;
    private List<OptaJob> jobs;
    private List<Resource> resources;
    private List<ExecutionMode> executionModes;

    private BendableScore score;//多层分值

    public Schedule(){}
    public Schedule(UUID id, String name, List<Allocation> allocationList){
        super(id,name);
        this.allocationList=allocationList;
        setResourceRequirementsByInputAllocationList(this.allocationList);
        setJobsByInputAllocationList(this.allocationList);
        setExecutionModesByInputAllocationList(this.allocationList);
    }

    public Map<OptaJob, List<Allocation>> getTaskAllocationsGroupByJob() {
        this.taskAllocationsGroupByJob.clear();
        for (Allocation allocation : this.allocationList) {
            List<Allocation> tempList = taskAllocationsGroupByJob.get(allocation.getJob());
            if (tempList == null) {
                tempList = new ArrayList<>();
                tempList.add(allocation);
                taskAllocationsGroupByJob.put(allocation.getJob(), tempList);
            }
            else {
                tempList.add(allocation);
            }
        }
        return taskAllocationsGroupByJob;
    }

    public Map<Resource, List<Allocation>> getTaskAllocationsGroupByResource() {
        this.taskAllocationsGroupByResource.clear();
        for (Allocation allocation : this.allocationList) {
            for(ResourceRequirement resourceRequirement : allocation.getSelectedExecutionMode().getResourceRequirementsThisMode()){
                List<Allocation> tempList = taskAllocationsGroupByResource.get(resourceRequirement.getResource());
                if (tempList == null) {
                    tempList = new ArrayList<>();
                    tempList.add(allocation);
                    taskAllocationsGroupByResource.put(resourceRequirement.getResource(), tempList);
                }
                else {
                    tempList.add(allocation);
                }
            }
        }
        return taskAllocationsGroupByResource;
    }

    @PlanningEntityCollectionProperty
    public List<Allocation> getAllocationList() {
        return allocationList;
    }
    public void setAllocationList(List<Allocation> allocationList) {
        this.allocationList = allocationList;
    }

    @PlanningScore(bendableHardLevelsSize = 1, bendableSoftLevelsSize = 2)//一层硬约束, 两层软约束
    public BendableScore getScore() {
        return score;
    }
    public void setScore(BendableScore score) {
        this.score = score;
    }

    @ProblemFactCollectionProperty
    public List<ResourceRequirement> getResourceRequirements() {
        return resourceRequirements;
    }
    public void setResourceRequirements(List<ResourceRequirement> resourceRequirements){
        this.resourceRequirements=resourceRequirements;
    }

    @ProblemFactCollectionProperty
    public List<OptaJob> getJobs() {
        return jobs;
    }
    public void setJobs(List<OptaJob> jobs){
        this.jobs=jobs;
    }

    @ProblemFactCollectionProperty
    public List<Resource> getResources() {
        return resources;
    }
    public void setResources(List<Resource> resources){
        this.resources=resources;
    }

    @ProblemFactCollectionProperty
    public List<ExecutionMode> getExecutionModes() {
        return executionModes;
    }
    public void setExecutionModes(List<ExecutionMode> executionModes){
        this.executionModes=executionModes;
    }

    private void setResourceRequirementsByInputAllocationList(List<Allocation> allocationList){
        this.resourceRequirements=new ArrayList<>();
        for(Allocation allocation : allocationList){
            for(ExecutionMode executionMode : allocation.getTask().getOptionalExecutionModes()){
                for(ResourceRequirement resourceRequirement : executionMode.getResourceRequirementsThisMode()){
                    this.resourceRequirements.add(resourceRequirement);
                }
            }
        }
    }

    private void setJobsByInputAllocationList(List<Allocation> allocationList){
        Set jobSets=new HashSet();
        for(Allocation allocation : allocationList){
            jobSets.add(allocation.getJob());
        }
        this.jobs=new ArrayList<>(jobSets);
    }

    private void setExecutionModesByInputAllocationList(List<Allocation> allocationList){
        this.executionModes=new ArrayList<>();
        for(Allocation allocation : allocationList){
            for(ExecutionMode executionMode : allocation.getTask().getOptionalExecutionModes()){
                this.executionModes.add(executionMode);
            }
        }
    }

}
