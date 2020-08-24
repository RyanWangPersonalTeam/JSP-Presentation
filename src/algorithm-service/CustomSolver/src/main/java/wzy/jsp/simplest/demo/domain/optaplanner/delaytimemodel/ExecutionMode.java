package wzy.jsp.simplest.demo.domain.optaplanner.delaytimemodel;

import wzy.jsp.simplest.demo.domain.optaplanner.delaytimemodel.common.AbstractPersistable;

import java.util.List;
import java.util.UUID;

/**
 * 一个task的执行模式, 一种执行模式需要用到某些资源, 需要耗费一段时间
 */
public class ExecutionMode extends AbstractPersistable {
    private OptaTask task;
    private int duration;
    private List<ResourceRequirement> resourceRequirementsThisMode;//这种模式下的资源需求

    public ExecutionMode(){

    }

    public ExecutionMode(UUID id, String name, int duration, List<ResourceRequirement> resourceRequirementsThisMode){
        super(id,name);
        this.duration=duration;
        this.resourceRequirementsThisMode=resourceRequirementsThisMode;
    }


    public OptaTask getTask() {
        return task;
    }

    public void setTask(OptaTask task) {
        this.task = task;
    }

    public int getDuration() {
        return duration;
    }

    public void setDuration(int duration) {
        this.duration = duration;
    }

    public List<ResourceRequirement> getResourceRequirementsThisMode() {
        return resourceRequirementsThisMode;
    }

    public void setResourceRequirementsThisMode(List<ResourceRequirement> resourceRequirementsThisMode) {
        this.resourceRequirementsThisMode = resourceRequirementsThisMode;
    }

}
