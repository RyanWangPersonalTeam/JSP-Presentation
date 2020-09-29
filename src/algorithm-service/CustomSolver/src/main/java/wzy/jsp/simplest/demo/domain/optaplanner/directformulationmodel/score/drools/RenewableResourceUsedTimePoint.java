package wzy.jsp.simplest.demo.domain.optaplanner.directformulationmodel.score.drools;

import org.apache.commons.lang3.builder.EqualsBuilder;
import org.apache.commons.lang3.builder.HashCodeBuilder;
import wzy.jsp.simplest.demo.domain.optaplanner.directformulationmodel.resource.Resource;

import java.io.Serializable;

/**
 * 每个RenewableResourceUsedTimePoint实例代表该可刷新资源的一个时间刻度
 * 根据输入的task的ResourceRequirement信息, 对可能要使用的Resource生成
 * 对应的时间刻度, 某个时刻下该资源是否超载, 检查运行时间涵盖这个时刻的task,
 * 计算它们的资源占用累加值
 */

public class RenewableResourceUsedTimePoint implements Serializable {
    private final Resource resource;
    private final int timePoint;

    public RenewableResourceUsedTimePoint(Resource resource, int timePoint) {
        this.resource = resource;
        this.timePoint = timePoint;
    }

    public Resource getResource() {
        return resource;
    }

    public int getTimePoint() {
        return timePoint;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        } else if (o instanceof RenewableResourceUsedTimePoint) {
            RenewableResourceUsedTimePoint other = (RenewableResourceUsedTimePoint) o;
            return new EqualsBuilder()
                    .append(resource, other.resource)
                    .append(timePoint, other.timePoint)
                    .isEquals();
        } else {
            return false;
        }
    }

    @Override
    public int hashCode() {
        return new HashCodeBuilder()
                .append(resource)
                .append(timePoint)
                .toHashCode();
    }

    @Override
    public String toString() {
        return resource + " on " + timePoint;
    }

    public int getResourceCapacity() {
        return resource.getCapacity();
    }
}
