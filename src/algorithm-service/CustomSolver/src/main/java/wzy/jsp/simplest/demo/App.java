package wzy.jsp.simplest.demo;

import com.google.ortools.linearsolver.MPConstraint;
import com.google.ortools.linearsolver.MPObjective;
import com.google.ortools.linearsolver.MPSolver;
import com.google.ortools.linearsolver.MPVariable;
import com.google.ortools.sat.*;
import org.drools.core.rule.QueryArgument;
import org.optaplanner.core.api.solver.Solver;
import org.optaplanner.core.api.solver.SolverFactory;
import org.optaplanner.core.api.solver.SolverJob;
import org.optaplanner.core.api.solver.SolverManager;
import org.optaplanner.core.config.solver.SolverConfig;
import org.optaplanner.core.config.solver.SolverManagerConfig;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import wzy.jsp.simplest.demo.common.IAlgorithmCore;
import wzy.jsp.simplest.demo.component.AlgorithmFactory;
import wzy.jsp.simplest.demo.component.DateTimeConverter;
import wzy.jsp.simplest.demo.component.VariableConverter;
import wzy.jsp.simplest.demo.domain.algorithm.OptaPlannerSolverWithDelayTimeRepresentModel;
import wzy.jsp.simplest.demo.domain.algorithm.OrToolCpSolverWithDelayTimeRepresentModel;
import wzy.jsp.simplest.demo.domain.algorithm.callback.MainAlgorithmProcess;
import wzy.jsp.simplest.demo.component.AMQPHandler;
import wzy.jsp.simplest.demo.domain.algorithm.callback.PrintIntermediateSolution;
import wzy.jsp.simplest.demo.domain.algorithm.callback.SolutionPrintHelper;
import wzy.jsp.simplest.demo.domain.communication.Solution;
import wzy.jsp.simplest.demo.domain.optaplanner.delaytimemodel.*;
import wzy.jsp.simplest.demo.domain.optaplanner.delaytimemodel.resource.GlobalResource;
import wzy.jsp.simplest.demo.domain.optaplanner.delaytimemodel.resource.Resource;
import wzy.jsp.simplest.demo.domain.optaplanner.delaytimemodel.test.TestScheduleInstaneGenerator;
import wzy.jsp.simplest.demo.generator.TestSolutionGenerator;

import java.net.ConnectException;
import java.net.UnknownHostException;
import java.nio.channels.ClosedSelectorException;
import java.util.*;
import java.util.concurrent.ExecutionException;


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

//        Logger logger = LoggerFactory.getLogger(App.class);
//        try {
//            while (true) {
//                try (AMQPHandler amqpHandler = new AMQPHandler("rabbitmq")) {//if deployed into container, it should be the container name or service name
//                    amqpHandler.SendServiceStatus(false);
//                    amqpHandler.RegisterRequestHandleCallback(new MainAlgorithmProcess());
//                    while (!amqpHandler.isExit()) ;
//                }
//            }
//
//        }catch (Exception ex) {
//            logger.error(ex.getMessage(), ex);
//            logger.info("Algorithm service stopped");
//        }


    }
}


