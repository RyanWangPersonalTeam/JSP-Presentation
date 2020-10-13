package wzy.jsp.simplest.demo.domain.algorithm.callback;

import com.google.gson.Gson;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import wzy.jsp.simplest.demo.common.IAlgorithmCore;
import wzy.jsp.simplest.demo.common.IHandleNewCalculateRequest;
import wzy.jsp.simplest.demo.component.AMQPHandler;
import wzy.jsp.simplest.demo.component.AlgorithmFactory;
import wzy.jsp.simplest.demo.component.DateTimeConverter;
import wzy.jsp.simplest.demo.component.VariableConverter;
import wzy.jsp.simplest.demo.domain.communication.Solution;
import wzy.jsp.simplest.demo.domain.communication.amqp.CalculateRequest;
import wzy.jsp.simplest.demo.domain.communication.amqp.CalculateResponse;


/**
 * The main process of algorithm handling calculate requests
 *
 *     set service status to busy
 *                |
 *                |
 *     create algorithm object according to parameters in request
 *                |
 *                |
 *     calculating(invoke default callback function to send immediate results)
 *                |
 *                |
 *     send final result
 */
public class MainAlgorithmProcess implements IHandleNewCalculateRequest {

    private Logger logger;
    public MainAlgorithmProcess(){
        this.logger= LoggerFactory.getLogger(MainAlgorithmProcess.class);
    }
    @Override
    public void Handle(CalculateRequest calculateRequest, AMQPHandler amqpHandler) throws Exception {
        this.logger.info("A new calculate request comes : \r\n"+new Gson().toJson(calculateRequest,CalculateRequest.class));
        amqpHandler.SendServiceStatus(true);

        IAlgorithmCore algorithmInstance= AlgorithmFactory.CreateAlgorithmInstance(calculateRequest,amqpHandler);
        CalculateResponse response=new CalculateResponse();
        if(algorithmInstance==null){
            response.SolvedSolution=calculateRequest.UnsolvedSolution;
            response.Success=false;
            response.ErrorMessage="Not support this algorithm or model";
        }
        else{
            Solution solvedSolution=algorithmInstance.Calculate(calculateRequest.Initialized);
            if(solvedSolution==null){
                this.logger.warn("Can't find feasible solution");
                response.SolvedSolution=null;
                response.Success=false;
                response.ErrorMessage="Can't find feasible solution";
            }
            else{
                this.logger.info("Complete calculating this time ");
                SolutionPrintHelper.printScheduleSolution(solvedSolution);
                response.Success=true;
                response.SolvedSolution=solvedSolution;
                Integer score=new VariableConverter(new DateTimeConverter()).getMakespanFromSolution(solvedSolution);
                response.LogInfo=String.format("Current best solution, score : %d, status : %s",score,solvedSolution.FinalResult?"Final Result":"Intermediate Result");
            }
        }
        for(int i=0;i<3;i++){
            amqpHandler.SendCalculateResponse(response);
            Thread.sleep(200);
        }


        amqpHandler.SendServiceStatus(false);
        amqpHandler.setExit(true);
    }
}
