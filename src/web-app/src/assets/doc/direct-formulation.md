We define a set $\text{J}$ contains n jobs; A set $\text{M}$ contains m machines; A set $\text{O}$ of n operations, for each operation $v\in O$ there is a job $J_v \in J$ to which it belongs, a machine $M_v \in M$ on which it requires processing, and a processing time $t_v$.There is a binary relation $\to$ on $\text{O}$ that decomposes $\text{O}$ into chains corresponding to the jobs, i.e. if $v \to w$, then $J_v=J_w$, and there is no $x \notin \{v,w\}$ such that $v \to x$ or $x \to w$. The problem is to find a start time $S_v$ for each operation $v \in O$ such that:

<center>$max_{v \in O}S_v+t_v$&emsp;&emsp;&emsp;&emsp;&emsp;&emsp;&emsp;&emsp;&emsp;&emsp;&emsp;&emsp;&emsp;&emsp;&emsp;&emsp;&emsp;&emsp;(1)</center> 


is minimized subject to 
<center>$S_v \geq 0 \quad\quad for\; all\; v \in O$&emsp;&emsp;&emsp;&emsp;&emsp;&emsp;&emsp;&emsp;&emsp;&emsp;&emsp;&emsp;&emsp;&emsp;&emsp;&emsp;&emsp;&emsp;(2)</center> 
<center>$S_w-S_v \geq t_v \quad\quad if\; v \to w,\;v,w \in O$&emsp;&emsp;&emsp;&emsp;&emsp;&emsp;&emsp;&emsp;&emsp;&emsp;&emsp;&emsp;(3)</center> 
<center>$S_w - S_v \geq t_v \bigvee S_v-S_w \geq t_w \quad\quad if\; M_v=M_w,\; v,w \in O$&emsp;&emsp;(4)</center>

These inequalities are the basic constraints in JSP. The inequality 2 means that all start times of operations should begin from zero; Inequality 3 means for two adjacent operations in the same job, they must obey sequence constraint, i.e. the next operation can't start until the previous operation finishes. The inequality 4 indicates the machine capacity constraint, a machine can't process more than one operation at any time, so two operations' occupied times in the same machine can't overlap.

The formula above is generated directly from JSP description, the decision variables are just the start time of each operations.The advantage of this representation is that it is easy to understand, no need considering encode or decode, and it's convenient to construct more complex business model. But this representation is not efficient for most algorithms, the scope of its each decision variable (start time) is too large, which leads easily to a huge search space. Usually, Integer optimization algorithms is more efficient than Meta-heuristic algorithms based on this representation.




