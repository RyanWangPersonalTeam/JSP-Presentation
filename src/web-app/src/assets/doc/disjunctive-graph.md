In academia, Disjunctive Graph is one of the most popular representation models for JSP. 

We define a disjunctive graph $G=(V,C \cup D)$, where:
- $\text{V}$ is a set of nodes representing operations of the jobs together with two special nodes, a source (0) and a sink $\text{*}$ , representing the begin and end of the schedule, respectively.
- $\text{C}$ is a set of conjunctive arcs representing technological sequences of the operations.
- $\text{D}$ is a set of disjunctive arcs representing pairs of operations that must be performed on the same machines.
- The processing time for each operation is the weighted value attached to the corresponding nodes.
  
For example, a 3X3 JSP problem can be showed as below table.In each cell, left number determines running in which machine, right number indicates the process time.


<span><div style="text-align: center;">
![a sample of 3X3 JSP](../../../assets/imgs/DG3.png)
</div></span>
<span><div style="text-align: center;">
A sample of 3X3 JSP
</div></span>

A corresponding disjunctive graph can be generated:

<span><div style="text-align: center;">
![disjunctive graph sample](../../../assets/imgs/DG2.png)
</div></span>
<span><div style="text-align: center;">
A disjunctive graph of 3X3 JSP
</div></span>

To this sample problem, one possible solution may be presented as the Gantt-Chart below:

<span><div style="text-align: center;">
![a schedule of 3X3 JSP](../../../assets/imgs/DG1.png)
</div></span>
<span><div style="text-align: center;">
A schedule of 3X3 JSP
</div></span>

And in the disjunctive graph, the scheduling process can be viewed as defining the ordering between all operations that must be processed on the same machine, i.e. this is done by turning all disjunctive(undirected) arcs into directed ones. The figure below is the disjunctive graph result of the above schedule:

<span><div style="text-align: center;">
![a disjunctive graph of schedule](../../../assets/imgs/DG4.png)
</div></span>
<span><div style="text-align: center;">
a disjunctive graph of schedule
</div></span>

Disjunctive Graph is an indirect representation for JSP, the decision variables are directions of disjunctive arcs not start times, so this representation actually doesn't map a complete schedule, just represents an arrange strategy. To get the exact start times of each operations, additional decoding algorithms need to be applied.

By disjunctive graph, we can encode the JSP schedule into binary string or permutation, it is convenient for Meta-heuristic algorithms, especially genetic algorithm.  