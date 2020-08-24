package wzy.jsp.simplest.demo.common;

import wzy.jsp.simplest.demo.component.AMQPHandler;
import wzy.jsp.simplest.demo.domain.communication.amqp.CalculateRequest;

public interface IHandleNewCalculateRequest {
    public void Handle(CalculateRequest calculateRequest, AMQPHandler amqpHandler) throws Exception;
}
