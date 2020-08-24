import { Job } from './job';

export class Solution {
    public id:string;
    public clientId:string;
    public finalResult:boolean;
    public errMessage:String;
    public minTime:string;
    public maxTime:string;
    public jobs:Job[];
}
