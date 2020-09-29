package wzy.jsp.simplest.demo.domain.algorithm.represent;

import java.util.List;

/**
 * This class means the simple JSP represent model using delay time of each task, the decision
 * variables are delay times of each task. It's simple to understand and encode or decode in algorithms,
 * but inefficiency obviously
 */
public class DelayTimeRepresentModel {
    //Decision variables
    //es[i][j] means the delay time of task[i][j] to start after end time of task[i][j-1]
    //i=0,...,N-1, N is the number of job
    //j=0,...,M, M is the number of task in a job, j=0 and j=M are fake tasks, source and sink, their values
    //are both zero
    public List<List<Integer>> es;
    //Duration of each task
    //i=0,...,N-1, N is the number of job
    //j=0,...,M, M is the number of task in a job, j=0 and j=M are fake tasks, source and sink, their values
    //are both zero
    public List<List<Integer>> durations;
    //The list of available machines
    public String[] machines;
    //The occupy requirement for machines of each task,
    //occupies[i][j][k] means occupy requirement to k machine of the task[i][j] during occupying ,
    //In simple JSP, this value is 0 or 1
    public List<List<int[]>> occupies;
    //Upper limit of time axis
    public int timeUpperLimit;
    //Upper limit of decision variables
    public int decisionUpperLimit;

}
