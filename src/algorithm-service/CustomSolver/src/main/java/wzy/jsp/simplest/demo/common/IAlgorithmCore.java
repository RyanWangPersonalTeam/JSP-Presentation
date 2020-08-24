package wzy.jsp.simplest.demo.common;

import wzy.jsp.simplest.demo.domain.communication.Solution;

public interface IAlgorithmCore {
    public Solution Calculate(boolean initialized) throws Exception;
}
