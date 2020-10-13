package wzy.jsp.simplest.demo.common;

import wzy.jsp.simplest.demo.component.AMQPHandler;
import wzy.jsp.simplest.demo.domain.communication.Solution;

/**
 * Algorithm can use this callback to do some operation for intermediate solution,
 * e.g. send this intermediate result to a queue
 */
public interface IntermediateSolutionCallback {
    void HandleIntermediateSolution(Solution solution, AMQPHandler amqpHandler, int score) throws Exception;
}
