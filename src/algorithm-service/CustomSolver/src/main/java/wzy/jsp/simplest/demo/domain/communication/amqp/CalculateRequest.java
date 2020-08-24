package wzy.jsp.simplest.demo.domain.communication.amqp;

import wzy.jsp.simplest.demo.domain.communication.Solution;

public class CalculateRequest {
    public Boolean Initialized=false;
    public String RepresentType;
    public String AlgorithmType;
    public Solution UnsolvedSolution;
}
