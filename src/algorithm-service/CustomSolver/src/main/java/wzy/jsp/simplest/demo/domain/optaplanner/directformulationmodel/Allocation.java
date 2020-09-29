package wzy.jsp.simplest.demo.domain.optaplanner.delaytimemodel;

import org.optaplanner.core.api.domain.entity.PlanningEntity;
import org.optaplanner.core.api.domain.valuerange.CountableValueRange;
import org.optaplanner.core.api.domain.valuerange.ValueRangeFactory;
import org.optaplanner.core.api.domain.valuerange.ValueRangeProvider;
import org.optaplanner.core.api.domain.variable.CustomShadowVariable;
import org.optaplanner.core.api.domain.variable.PlanningVariable;
import org.optaplanner.core.api.domain.variable.PlanningVariableReference;
import wzy.jsp.simplest.demo.domain.optaplanner.delaytimemodel.common.AbstractPersistable;
import wzy.jsp.simplest.demo.domain.optaplanner.delaytimemodel.solver.DelayStrengthComparator;
import wzy.jsp.simplest.demo.domain.optaplanner.delaytimemodel.solver.PredecessorsEndTimeUpdatingVariableListener;

import java.util.List;
import java.util.UUID;

/**
 * 对一个task的分配, 也可以把这些计划变量添在task类里, 让task自身成为一个计划实体,
 * 不过抽象成Allocation类更好一点
 */
@PlanningEntity
public class Allocation extends AbstractPersistable implements Comparable{
    private OptaTask task;
    private Allocation predecessorTaskAllocation;
    private Allocation successorTaskAllocation;
    private int delayMaxValue=500;

    // Planning variables
    private ExecutionMode selectedExecutionMode;//选择哪种执行模式
    private Integer delayAfterPredecessor;//前道task结束后多久开始

    // Shadow variables
    private Integer predecessorsEndTime;//前道的结束时间

    public Allocation(){}
    public Allocation(UUID id, String name, OptaTask task){
        super(id,name);
        this.task=task;
    }
    public Allocation(UUID id, String name, OptaTask task, int delayMaxValue){
        super(id,name);
        this.task=task;
        this.delayMaxValue=delayMaxValue;
    }

    public OptaTask getTask() {
        return task;
    }

    public void setTask(OptaTask task) {
        this.task = task;
    }

    public Allocation getPredecessorTaskAllocation() {
        return predecessorTaskAllocation;
    }

    public void setPredecessorTaskAllocation(Allocation predecessorTaskAllocation) {
        this.predecessorTaskAllocation = predecessorTaskAllocation;
    }

    public Allocation getSuccessorTaskAllocation() {
        return successorTaskAllocation;
    }

    public void setSuccessorTaskAllocation(Allocation successorTaskAllocation) {
        this.successorTaskAllocation = successorTaskAllocation;
    }

    public int getDelayMaxValue() {
        return delayMaxValue;
    }

    @PlanningVariable(valueRangeProviderRefs = {"executionModeRange"}/*, strengthWeightFactoryClass = ExecutionModeStrengthWeightFactory.class*/)
    public ExecutionMode getSelectedExecutionMode() {
        return selectedExecutionMode;
    }
    public void setSelectedExecutionMode(ExecutionMode selectedExecutionMode) {
        this.selectedExecutionMode = selectedExecutionMode;
    }

    @PlanningVariable(valueRangeProviderRefs = {"delayRange"},
            strengthComparatorClass = DelayStrengthComparator.class)
    public Integer getDelayAfterPredecessor() {
        return delayAfterPredecessor;
    }
    public void setDelayAfterPredecessor(Integer delayAfterPredecessor) {
        this.delayAfterPredecessor = delayAfterPredecessor;
    }

    @CustomShadowVariable(variableListenerClass = PredecessorsEndTimeUpdatingVariableListener.class,
            sources = {@PlanningVariableReference(variableName = "selectedExecutionMode"),
                    @PlanningVariableReference(variableName = "delayAfterPredecessor")})
    public Integer getPredecessorsEndTime() {
        return predecessorsEndTime;
    }
    public void setPredecessorsEndTime(Integer predecessorsEndTime) {
        this.predecessorsEndTime = predecessorsEndTime;
    }

    // ************************************************************************
    // Complex methods
    // ************************************************************************
    //task的开始时间需要根据前道task的结束时间计算得到
    public Integer getStartTime() {
        //这是第一道task
        if (this.task.getPredecessorTask() == null) {
            return this.task.getJob().getScheduleStartTime()+(delayAfterPredecessor == null ? 0 : delayAfterPredecessor);
        }
        //从第二道task开始都需要根据前道来计算
        return predecessorsEndTime + (delayAfterPredecessor == null ? 0 : delayAfterPredecessor);
    }

    //task的结束时间需要根据前道task的结束时间计算得到
    public Integer getEndTime() {
        if (this.task.getPredecessorTask() == null) {
            return this.task.getJob().getScheduleStartTime()+ (delayAfterPredecessor == null ? 0 : delayAfterPredecessor)
                    + (selectedExecutionMode == null ? 0 : selectedExecutionMode.getDuration());
        }
        return predecessorsEndTime + (delayAfterPredecessor == null ? 0 : delayAfterPredecessor)
                + (selectedExecutionMode == null ? 0 : selectedExecutionMode.getDuration());
    }

    public OptaJob getJob() {
        return task.getJob();
    }

    // ************************************************************************
    // Ranges
    // ************************************************************************

    @ValueRangeProvider(id = "executionModeRange")
    public List<ExecutionMode> getExecutionModeRange() {
        return task.getOptionalExecutionModes();
    }

    @ValueRangeProvider(id = "delayRange")
    public CountableValueRange<Integer> getDelayRange() {
        return ValueRangeFactory.createIntValueRange(0, this.delayMaxValue);
    }

    @Override
    public int compareTo(Object o) {
        Allocation s = (Allocation) o;
        if (this.getStartTime() > s.getStartTime()) {
            return 1;
        }
        else {
            return -1;
        }
    }
}
