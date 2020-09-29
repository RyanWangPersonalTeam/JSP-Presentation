package wzy.jsp.simplest.demo.domain.optaplanner.directformulationmodel.solver;

import org.optaplanner.core.impl.domain.variable.listener.VariableListener;
import org.optaplanner.core.impl.score.director.ScoreDirector;
import wzy.jsp.simplest.demo.domain.optaplanner.directformulationmodel.Allocation;

import java.util.ArrayDeque;
import java.util.Objects;
import java.util.Queue;

/**
 * 当一个task被算法引擎调整后, 更新它后道的所有task的PredecessorsEndTime
 */
public class PredecessorsEndTimeUpdatingVariableListener implements VariableListener<Allocation> {
    @Override
    public void beforeEntityAdded(ScoreDirector scoreDirector, Allocation allocation) {
        // Do nothing
    }

    @Override
    public void afterEntityAdded(ScoreDirector scoreDirector, Allocation allocation) {
        updateAllocation(scoreDirector, allocation);
    }

    @Override
    public void beforeVariableChanged(ScoreDirector scoreDirector, Allocation allocation) {
        // Do nothing
    }

    @Override
    public void afterVariableChanged(ScoreDirector scoreDirector, Allocation allocation) {
        updateAllocation(scoreDirector, allocation);
    }

    @Override
    public void beforeEntityRemoved(ScoreDirector scoreDirector, Allocation allocation) {
        // Do nothing
    }

    @Override
    public void afterEntityRemoved(ScoreDirector scoreDirector, Allocation allocation) {
        // Do nothing
    }

    protected void updateAllocation(ScoreDirector scoreDirector, Allocation originalAllocation) {
        Queue<Allocation> uncheckedSuccessorQueue = new ArrayDeque<>();
        if(originalAllocation.getSuccessorTaskAllocation()!=null){
            uncheckedSuccessorQueue.add(originalAllocation.getSuccessorTaskAllocation());
        }
        while (!uncheckedSuccessorQueue.isEmpty()) {
            Allocation allocation = uncheckedSuccessorQueue.remove();
            boolean updated = updatePredecessorsEndTime(scoreDirector, allocation);
            if (updated && allocation.getSuccessorTaskAllocation()!=null) {
                uncheckedSuccessorQueue.add(allocation.getSuccessorTaskAllocation());
            }
        }
    }

    /**
     * @param scoreDirector never null
     * @param allocation never null
     * @return true if the startDate changed
     */
    protected boolean updatePredecessorsEndTime(ScoreDirector scoreDirector, Allocation allocation) {
        // For the source the endTime must be 0.
        Integer endTime = 0;
        if(allocation.getPredecessorTaskAllocation()!=null){
            endTime=allocation.getPredecessorTaskAllocation().getEndTime();
        }
        else{
            endTime=allocation.getPredecessorTaskAllocation().getEndTime();
        }
        if (Objects.equals(endTime, allocation.getPredecessorsEndTime())) {
            return false;
        }
        scoreDirector.beforeVariableChanged(allocation, "predecessorsEndTime");
        allocation.setPredecessorsEndTime(endTime);
        scoreDirector.afterVariableChanged(allocation, "predecessorsEndTime");
        return true;
    }
}
