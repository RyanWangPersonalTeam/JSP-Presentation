package wzy.jsp.simplest.demo.domain.optaplanner.directformulationmodel.common;

import org.optaplanner.core.api.domain.lookup.PlanningId;

import java.io.Serializable;
import java.util.UUID;

public abstract class AbstractPersistable implements Serializable {

    protected UUID id;
    protected String name;

    protected AbstractPersistable() {
    }

    protected AbstractPersistable(UUID id) {

        this.id = id;
    }
    protected AbstractPersistable(UUID id, String name) {
        this.id = id;
        this.name=name;
    }

    @PlanningId
    public UUID getId() {
        return id;
    }

    public void setId(UUID id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    @Override
    public String toString() {
        return getClass().getName().replaceAll(".*\\.", "") + "-" + id;
    }

}
