package wzy.jsp.simplest.demo.domain.optaplanner.delaytimemodel.resource;

import java.util.UUID;

public class GlobalResource extends Resource {
    public GlobalResource(){}
    public GlobalResource(UUID id, String name, int capacity){
        super(id,name,capacity);
    }
    // ************************************************************************
    // Complex methods
    // ************************************************************************

    @Override
    public boolean isRenewable() {
        return true;
    }
}
