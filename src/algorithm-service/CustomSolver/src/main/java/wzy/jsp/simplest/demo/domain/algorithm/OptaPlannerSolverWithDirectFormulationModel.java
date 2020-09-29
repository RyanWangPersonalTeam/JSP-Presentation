package wzy.jsp.simplest.demo.domain.algorithm;

import org.optaplanner.core.api.solver.SolverJob;
import org.optaplanner.core.api.solver.SolverManager;
import org.optaplanner.core.config.solver.SolverConfig;
import org.optaplanner.core.config.solver.SolverManagerConfig;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import wzy.jsp.simplest.demo.common.IAlgorithmCore;
import wzy.jsp.simplest.demo.common.IntermediateSolutionCallback;
import wzy.jsp.simplest.demo.component.AMQPHandler;
import wzy.jsp.simplest.demo.component.VariableConverter;
import wzy.jsp.simplest.demo.domain.communication.Solution;
import wzy.jsp.simplest.demo.domain.optaplanner.directformulationmodel.Schedule;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

public class OptaPlannerSolverWithDirectFormulationModel implements  IAlgorithmCore {
    private AMQPHandler amqpHandler;
    private VariableConverter variableConverter;
    private Solution originalUnsolvedSolution;
    private String optaPlannerSolverConfigPath="wzy/jsp/simplest/demo/optaplanner/solver/delaytimemodelSolverConfig.xml";
    private SolverManager<Schedule, UUID> solverManager;
    private SolverJob<Schedule,UUID> solverJob;
    private Map<UUID,Schedule> scheduleProblemMap=new HashMap<>();
    private Logger logger;
    private IntermediateSolutionCallback intermediateSolutionCallback;

    public OptaPlannerSolverWithDirectFormulationModel(AMQPHandler amqpHandler,
                                                       VariableConverter variableConverter,
                                                       Solution originalUnsolvedSolution,
                                                       IntermediateSolutionCallback intermediateSolutionCallback){
        this.amqpHandler=amqpHandler;
        this.variableConverter=variableConverter;
        this.originalUnsolvedSolution=originalUnsolvedSolution;
        this.intermediateSolutionCallback=intermediateSolutionCallback;
        this.logger= LoggerFactory.getLogger(OptaPlannerSolverWithDirectFormulationModel.class);

    }

    public OptaPlannerSolverWithDirectFormulationModel(AMQPHandler amqpHandler,
                                                       VariableConverter variableConverter,
                                                       Solution originalUnsolvedSolution,
                                                       IntermediateSolutionCallback intermediateSolutionCallback,
                                                       String optaPlannerSolverConfigPath){
        this.amqpHandler=amqpHandler;
        this.variableConverter=variableConverter;
        this.originalUnsolvedSolution=originalUnsolvedSolution;
        this.intermediateSolutionCallback=intermediateSolutionCallback;
        this.logger= LoggerFactory.getLogger(OptaPlannerSolverWithDirectFormulationModel.class);
        this.optaPlannerSolverConfigPath=optaPlannerSolverConfigPath;
    }

    @Override
    public Solution Calculate(boolean initialized) throws Exception {
        if(this.intermediateSolutionCallback!=null && initialized){
            try{
                this.intermediateSolutionCallback.HandleIntermediateSolution(this.originalUnsolvedSolution,this.amqpHandler);
            }catch (Exception ex){
                logger.error(ex.getMessage(),ex);
                throw ex;
            }
        }
        //In this system, the input solution has been initialized, so we can skip construction Heuristic
        Schedule unSolvedScheduleInstance=variableConverter.GetOptaPlannerScheduleInstanceFromSolution(this.originalUnsolvedSolution,initialized);
        UUID problemId=UUID.randomUUID();
        SolverConfig solverConfig = SolverConfig.createFromXmlResource(this.optaPlannerSolverConfigPath);
        this.solverManager = SolverManager.create(solverConfig, new SolverManagerConfig());
        scheduleProblemMap.put(problemId,unSolvedScheduleInstance);
        this.solverJob=this.solverManager.solveAndListen(problemId,
                this::findById,
                this::handleIntermediateSolution);
        Schedule solvedTaskScheduleProblem=null;
        try{
            solvedTaskScheduleProblem=this.solverJob.getFinalBestSolution();
            Solution solvedSolution=variableConverter.GetSolutionFromOptaPlannerScheduleInstance(solvedTaskScheduleProblem,this.originalUnsolvedSolution);
            solvedSolution.FinalResult=true;
            return solvedSolution;

        }catch (Exception e){
            throw e;
        }
    }

    //Handle intermediate result
    private void handleIntermediateSolution(Schedule intermediateScheduleResult) {
        if(this.intermediateSolutionCallback!=null){
            Solution intermediateSolution=this.variableConverter.GetSolutionFromOptaPlannerScheduleInstance(
                    intermediateScheduleResult,
                    this.originalUnsolvedSolution
            );
            intermediateSolution.FinalResult=false;
            try{
                this.intermediateSolutionCallback.HandleIntermediateSolution(intermediateSolution,this.amqpHandler);
            }catch (Exception ex){
                logger.error(ex.getMessage(),ex);
            }
        }
    }

    //Used in SolverManager
    private Schedule findById(UUID problemId){
        return this.scheduleProblemMap.get(problemId);
    }

    //Used in SolverManager
    private void stopSolving(UUID problemId) {
        solverManager.terminateEarly(problemId);
    }


}
