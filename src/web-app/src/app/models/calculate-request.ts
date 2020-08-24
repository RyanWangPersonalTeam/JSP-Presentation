import { Solution } from './solution';

export class CalculateRequest {
    public initialized:boolean;
    public representType:string;
    public algorithmType:string;
    public unsolvedSolution:Solution;
}
