package wzy.jsp.simplest.demo;

import com.google.ortools.sat.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import wzy.jsp.simplest.demo.component.DateTimeConverter;
import wzy.jsp.simplest.demo.component.VariableConverter;
import wzy.jsp.simplest.demo.domain.algorithm.callback.MainAlgorithmProcess;
import wzy.jsp.simplest.demo.component.AMQPHandler;
import wzy.jsp.simplest.demo.domain.algorithm.callback.SolutionPrintHelper;
import wzy.jsp.simplest.demo.domain.algorithm.represent.DisjunctiveGraphModel;
import wzy.jsp.simplest.demo.domain.communication.Solution;
import wzy.jsp.simplest.demo.domain.communication.Task;
import wzy.jsp.simplest.demo.domain.ortools.DisjunctiveBinaryVariable;
import wzy.jsp.simplest.demo.generator.TestSolutionGenerator;

import java.net.ConnectException;
import java.net.UnknownHostException;
import java.util.*;


/**
 * Hello world!
 *
 */
public class App 
{
    static {
        System.loadLibrary("jniortools");
    }

    public static void main( String[] args ) throws Exception {

        while(true){
            Logger logger = LoggerFactory.getLogger(App.class);
            try {
                while (true) {
                    try (AMQPHandler amqpHandler = new AMQPHandler("rabbitmq")) {//if deployed into container, it should be the container name or service name
                        amqpHandler.SendServiceStatus(false);
                        amqpHandler.RegisterRequestHandleCallback(new MainAlgorithmProcess());
                        while (!amqpHandler.isExit()) ;
                    }
                }

            }catch (UnknownHostException uex){//The rabbitmq service may still be down, if so, wait for a while and try again
                logger.warn(uex.getMessage(), uex);
                logger.warn("Algorithm will try again after 5 s");
                Thread.sleep(5000);
            }catch (ConnectException cex){
                logger.warn(cex.getMessage(), cex);
                logger.warn("Algorithm will try again after 5 s");
                Thread.sleep(5000);
            }catch (Exception ex) {
                logger.error(ex.getMessage(), ex);
                logger.info("Algorithm service stopped");
                break;
            }
        }

//        VariableConverter variableConverter=new VariableConverter(new DateTimeConverter());
//        Solution testSolution=new TestSolutionGenerator().GenerateTestSolution2();
//        SolutionPrintHelper.printScheduleSolution(testSolution);
//        DisjunctiveGraphModel disjunctiveGraphModel=variableConverter.getDisjunctiveGraphFromSolution(testSolution,true);
//
//        Solution solvedSolution=variableConverter.getSolutionFromDisjunctiveGraph(testSolution,disjunctiveGraphModel);
//        SolutionPrintHelper.printScheduleSolution(solvedSolution);

    }


}


