package wzy.jsp.simplest.demo.domain.optaplanner.delaytimemodel.resource;

import wzy.jsp.simplest.demo.domain.optaplanner.delaytimemodel.common.AbstractPersistable;

import java.util.UUID;

/**
 * 资源可以是运行task的设备, 也可以是分配在task上的夹具
 * 资源按是否可刷新分成了两类, 可刷新资源要在每个时间上要满足资源限制, 非刷新资源是在整个时间段上要满足限制
 */
public abstract class Resource extends AbstractPersistable {
    private int capacity;

    public Resource(){}
    public Resource(UUID id, String name, int capacity){
        super(id,name);
        this.capacity=capacity;
    }

    public int getCapacity() {
        return capacity;
    }

    public void setCapacity(int capacity) {
        this.capacity = capacity;
    }

    // ************************************************************************
    // Complex methods
    // ************************************************************************

    public abstract boolean isRenewable();
}
