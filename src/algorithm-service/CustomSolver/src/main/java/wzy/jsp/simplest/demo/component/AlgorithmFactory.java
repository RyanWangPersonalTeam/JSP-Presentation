package wzy.jsp.simplest.demo.component;

import wzy.jsp.simplest.demo.common.IAlgorithmCore;
import wzy.jsp.simplest.demo.domain.algorithm.*;
import wzy.jsp.simplest.demo.domain.algorithm.callback.SendIntermediateSolution;
import wzy.jsp.simplest.demo.domain.algorithm.represent.*;
import wzy.jsp.simplest.demo.domain.communication.amqp.CalculateRequest;

public class AlgorithmFactory {
    public static IAlgorithmCore CreateAlgorithmInstance(
            CalculateRequest calculateRequest,
            AMQPHandler amqpHandler){
        IAlgorithmCore algorithmCore=null;
        JspRepresentType jspRepresentType=JspRepresentType.valueOf(calculateRequest.RepresentType);
        AlgorithmType algorithmType=AlgorithmType.valueOf(calculateRequest.AlgorithmType);
        if(jspRepresentType==JspRepresentType.DirectFormulation){
            switch (algorithmType){
//                case NativeSimulatedAnnealing:{
//                    DateTimeConverter dateTimeConverter=new DateTimeConverter();
//                    VariableConverter variableConverter=new VariableConverter(dateTimeConverter);
//                    DelayTimeRepresentModel delayTimeRepresentModel=variableConverter.getDelayTimeRepresentModelFromSolution(calculateRequest.UnsolvedSolution,calculateRequest.Initialized);
//                    SAAlgorithmWithDelayTimeRepresentModel sa=new SAAlgorithmWithDelayTimeRepresentModel(
//                            new SAParametersWithDelayTimeRepresentModel(delayTimeRepresentModel,calculateRequest.UnsolvedSolution),
//                            variableConverter,
//                            new SendIntermediateSolution(),
//                            amqpHandler);
//                    algorithmCore=sa;
//                }
//                break;
                case RandomSequenceHeuristic:{
                    DateTimeConverter dateTimeConverter=new DateTimeConverter();
                    VariableConverter variableConverter=new VariableConverter(dateTimeConverter);
                    DirectFormulationModel directFormulationModel =variableConverter.getDirectFormulationRepresentModelFromSolution(calculateRequest.UnsolvedSolution,calculateRequest.Initialized);
                    SimpleHeuristicAlgorithmWithDirectFormulationModel sha=new SimpleHeuristicAlgorithmWithDirectFormulationModel(
                            directFormulationModel,
                            variableConverter,
                            calculateRequest.UnsolvedSolution
                    );
                    algorithmCore=sha;
                }
                break;
//                case NativeGeneticAlgorithm:{
//                    DateTimeConverter dateTimeConverter=new DateTimeConverter();
//                    VariableConverter variableConverter=new VariableConverter(dateTimeConverter);
//                    DelayTimeRepresentModel delayTimeRepresentModel=variableConverter.getDelayTimeRepresentModelFromSolution(calculateRequest.UnsolvedSolution,calculateRequest.Initialized);
//                    GAAlgorithmWithDelayTimeRepresentModel ga=new GAAlgorithmWithDelayTimeRepresentModel(
//                            new GAParametersWithDelayTimeRepresentModel(delayTimeRepresentModel,calculateRequest.UnsolvedSolution),
//                            variableConverter,
//                            new SendIntermediateSolution(),
//                            amqpHandler);
//                    algorithmCore=ga;
//                }
//                break;
                case GoogleOrToolCpSolver:{
                    OrToolCpSolverWithDirectFormulationModel ot=new OrToolCpSolverWithDirectFormulationModel(
                            calculateRequest.UnsolvedSolution,
                            new SendIntermediateSolution(),
                            amqpHandler
                    );
                    algorithmCore=ot;
                }
                break;
                case OptaPlannerSolver:{
                    IAlgorithmCore op= new OptaPlannerSolverWithDirectFormulationModel(
                            amqpHandler,
                            new VariableConverter(new DateTimeConverter()),
                            calculateRequest.UnsolvedSolution,
                            new SendIntermediateSolution()
                    );
                    algorithmCore=op;
                }
                break;
            }
        }
        else if(jspRepresentType==JspRepresentType.DisjunctiveGraph){
            switch (algorithmType){
                case GoogleOrToolCpSolver:{
                    OrToolCpSolverWithDisjunctiveGraphModel ot=new OrToolCpSolverWithDisjunctiveGraphModel(
                            calculateRequest.UnsolvedSolution,
                            new SendIntermediateSolution(),
                            amqpHandler
                    );
                    algorithmCore=ot;
                }
                break;
                case NativeGeneticAlgorithm:{
                    DateTimeConverter dateTimeConverter=new DateTimeConverter();
                    VariableConverter variableConverter=new VariableConverter(dateTimeConverter);
                    DisjunctiveGraphModel disjunctiveGraphModel=variableConverter.getDisjunctiveGraphFromSolution(
                            calculateRequest.UnsolvedSolution,
                            true
                    );
                    GAAlgorithmWithDisjunctiveGraph ga=new GAAlgorithmWithDisjunctiveGraph(
                            new GAParametersWithDisjunctiveGraph(disjunctiveGraphModel,calculateRequest.UnsolvedSolution),
                            variableConverter,
                            new SendIntermediateSolution(),
                            amqpHandler
                    );
                    algorithmCore=ga;
                }
            }
        }
        return algorithmCore;
    }
}
