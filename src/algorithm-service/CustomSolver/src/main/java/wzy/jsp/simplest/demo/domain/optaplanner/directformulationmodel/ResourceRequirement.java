package wzy.jsp.simplest.demo.domain.optaplanner.directformulationmodel;

import wzy.jsp.simplest.demo.domain.optaplanner.directformulationmodel.common.AbstractPersistable;
import wzy.jsp.simplest.demo.domain.optaplanner.directformulationmodel.resource.Resource;

import java.util.UUID;

public class ResourceRequirement extends AbstractPersistable {
    private ExecutionMode executionMode;
    private Resource resource;
    private int requirement;

    public ResourceRequirement(){}

    public ResourceRequirement(UUID id,String name,Resource resource,int requirement){
        super(id,name);
        this.resource=resource;
        this.requirement=requirement;
    }

    public ExecutionMode getExecutionMode() {
        return executionMode;
    }

    public void setExecutionMode(ExecutionMode executionMode) {
        this.executionMode = executionMode;
    }

    public Resource getResource() {
        return resource;
    }

    public void setResource(Resource resource) {
        this.resource = resource;
    }

    public int getRequirement() {
        return requirement;
    }

    public void setRequirement(int requirement) {
        this.requirement = requirement;
    }

    // ************************************************************************
    // Complex methods
    // ************************************************************************

    public boolean isResourceRenewable() {
        return resource.isRenewable();
    }
}
