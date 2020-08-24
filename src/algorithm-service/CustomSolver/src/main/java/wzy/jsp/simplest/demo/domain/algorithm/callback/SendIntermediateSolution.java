package wzy.jsp.simplest.demo.domain.algorithm.callback;

import wzy.jsp.simplest.demo.common.IntermediateSolutionCallback;
import wzy.jsp.simplest.demo.component.AMQPHandler;
import wzy.jsp.simplest.demo.domain.communication.Solution;
import wzy.jsp.simplest.demo.domain.communication.amqp.CalculateResponse;

public class SendIntermediateSolution implements IntermediateSolutionCallback {
    @Override
    public void HandleIntermediateSolution(Solution solution, AMQPHandler amqpHandler) throws Exception {
        System.out.println("Current best solution : ");
        SolutionPrintHelper.printScheduleSolution(solution);
        CalculateResponse response=new CalculateResponse();
        response.Success=true;
        response.SolvedSolution=solution;
        amqpHandler.SendCalculateResponse(response);
    }
}
