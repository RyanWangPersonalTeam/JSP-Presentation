package wzy.jsp.simplest.demo.domain.algorithm.callback;

import wzy.jsp.simplest.demo.common.IntermediateSolutionCallback;
import wzy.jsp.simplest.demo.component.AMQPHandler;
import wzy.jsp.simplest.demo.domain.communication.Solution;
import wzy.jsp.simplest.demo.domain.communication.amqp.CalculateResponse;

public class SendIntermediateSolution implements IntermediateSolutionCallback {
    @Override
    public void HandleIntermediateSolution(Solution solution, AMQPHandler amqpHandler, int score) throws Exception {
        System.out.println("Current best solution : ");
        SolutionPrintHelper.printScheduleSolution(solution);
        CalculateResponse response=new CalculateResponse();
        response.Success=true;
        response.SolvedSolution=solution;
        String logInfo=String.format("Current best solution, score : %d, status : %s",score,solution.FinalResult?"Final Result":"Intermediate Result");
        response.LogInfo=logInfo;
        amqpHandler.SendCalculateResponse(response);
    }
}
