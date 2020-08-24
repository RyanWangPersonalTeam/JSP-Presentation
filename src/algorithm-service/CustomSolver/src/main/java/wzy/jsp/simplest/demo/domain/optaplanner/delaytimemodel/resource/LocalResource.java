package wzy.jsp.simplest.demo.domain.optaplanner.delaytimemodel.resource;

import wzy.jsp.simplest.demo.domain.optaplanner.delaytimemodel.OptaJob;

import java.util.UUID;

public class LocalResource extends Resource {

    private OptaJob job;
    private boolean renewable;

    public LocalResource(){}
    public LocalResource(UUID id, String name, int capacity, OptaJob job, boolean renewable){
        super(id,name,capacity);
        this.job=job;
        this.renewable=renewable;
    }

    public OptaJob getJob() {
        return job;
    }

    public void setJob(OptaJob job) {
        this.job = job;
    }

    @Override
    public boolean isRenewable() {
        return renewable;
    }

    public void setRenewable(boolean renewable) {
        this.renewable = renewable;
    }
}
