package wzy.jsp.simplest.demo.domain.algorithm.callback;

import wzy.jsp.simplest.demo.common.IntermediateSolutionCallback;
import wzy.jsp.simplest.demo.component.AMQPHandler;
import wzy.jsp.simplest.demo.domain.communication.Solution;

public class PrintIntermediateSolution implements IntermediateSolutionCallback {
    @Override
    public void HandleIntermediateSolution(Solution solution, AMQPHandler amqpHandler) {
        System.out.println("Current best solution : ");
        SolutionPrintHelper.printScheduleSolution(solution);
    }
}
