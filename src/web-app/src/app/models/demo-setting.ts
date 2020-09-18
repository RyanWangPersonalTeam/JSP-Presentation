export enum RepresentType{
    DirectFormulation,
    DisjunctiveGraph,
}

export enum AlgorithmType{
    RandomSequenceHeuristic,
    NativeSimulatedAnnealing,
    NativeGeneticAlgorithm,
    NativeTubeSearch,
    GoogleOrToolCpSolver,
    OptaPlannerSolver,
}

export class DemoSetting {
    public jobNum:number;
    public taskNumPerJob:number;
    public representType:RepresentType;
    public algorithmType:AlgorithmType;
}
