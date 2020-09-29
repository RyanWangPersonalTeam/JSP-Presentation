package wzy.jsp.simplest.demo.domain.optaplanner.directformulationmodel;

import wzy.jsp.simplest.demo.domain.optaplanner.directformulationmodel.common.AbstractPersistable;

import java.util.List;
import java.util.UUID;

public class OptaTask extends AbstractPersistable {
    private OptaJob job;
    private List<ExecutionMode> optionalExecutionModes; //可能的执行模式
    private OptaTask predecessorTask;//前道task
    private OptaTask successorTask;//后道task

    private String defaultStartTime;

    public OptaTask(){

    }

    public OptaTask(UUID id,String name, OptaJob job,List<ExecutionMode> optionalExecutionModes,String defaultStartTime){
        super(id,name);
        this.job=job;
        this.optionalExecutionModes=optionalExecutionModes;
        this.defaultStartTime=defaultStartTime;
    }

    public OptaJob getJob() {
        return job;
    }

    public void setJob(OptaJob job) {
        this.job = job;
    }

    public List<ExecutionMode> getOptionalExecutionModes() {
        return optionalExecutionModes;
    }

    public void setOptionalExecutionModes(List<ExecutionMode> optionalExecutionModes) {
        this.optionalExecutionModes = optionalExecutionModes;
    }

    public OptaTask getPredecessorTask() {
        return predecessorTask;
    }

    public void setPredecessorTask(OptaTask predecessorTask) {
        this.predecessorTask = predecessorTask;
    }

    public OptaTask getSuccessorTask() {
        return successorTask;
    }

    public void setSuccessorTask(OptaTask successorTask) {
        this.successorTask = successorTask;
    }

    public String getDefaultStartTime() {
        return defaultStartTime;
    }

    public void setDefaultStartTime(String defaultStartTime) {
        this.defaultStartTime = defaultStartTime;
    }
}
