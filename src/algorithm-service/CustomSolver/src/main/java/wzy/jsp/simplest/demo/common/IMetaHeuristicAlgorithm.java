package wzy.jsp.simplest.demo.common;

import wzy.jsp.simplest.demo.domain.communication.Solution;

public interface IMetaHeuristicAlgorithm {
    void HandleIntermediateSolution(Solution solution,int score) throws Exception;
}
