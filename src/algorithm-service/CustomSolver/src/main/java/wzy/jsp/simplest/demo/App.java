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

        try {
            while (true) {
                try (AMQPHandler amqpHandler = new AMQPHandler("localhost")) {
                    amqpHandler.SendServiceStatus(false);
                    amqpHandler.RegisterRequestHandleCallback(new MainAlgorithmProcess());
                    while (!amqpHandler.isExit()) ;
                }
            }

        } catch (Exception ex) {
            Logger logger = LoggerFactory.getLogger(App.class);
            logger.error(ex.getMessage(), ex);
            logger.info("Algorithm service stopped");
        }

    }
}


