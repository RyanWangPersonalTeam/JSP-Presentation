package wzy.jsp.simplest.demo.domain.optaplanner.delaytimemodel;

import wzy.jsp.simplest.demo.domain.optaplanner.delaytimemodel.common.AbstractPersistable;
import wzy.jsp.simplest.demo.domain.optaplanner.delaytimemodel.resource.LocalResource;

import java.util.List;
import java.util.UUID;

public class OptaJob extends AbstractPersistable {
    private int scheduleStartTime;//一个job有计划开始时间, 它下面的task不会在这之前开始
    private List<LocalResource> localResourcesThisJob;
    private List<OptaTask> tasksThisJob;

    public OptaJob(){

    }

    public OptaJob(UUID id, String name, int scheduleStartTime, List<LocalResource> localResourcesThisJob){
        super(id,name);
        this.scheduleStartTime=scheduleStartTime;
        this.localResourcesThisJob=localResourcesThisJob;
    }

    public int getScheduleStartTime() {
        return scheduleStartTime;
    }

    public void setScheduleStartTime(int scheduleStartTime) {
        this.scheduleStartTime = scheduleStartTime;
    }

    public List<LocalResource> getLocalResourcesThisJob() {
        return localResourcesThisJob;
    }

    public void setLocalResourcesThisJob(List<LocalResource> localResourcesThisJob) {
        this.localResourcesThisJob = localResourcesThisJob;
    }

    public List<OptaTask> getTasksThisJob() {
        return tasksThisJob;
    }

    public void setTasksThisJob(List<OptaTask> tasksThisJob) {
        this.tasksThisJob = tasksThisJob;
    }


}
