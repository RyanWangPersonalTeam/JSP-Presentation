import { Component, OnInit, Inject } from '@angular/core';
import { DemoSetting, RepresentType, AlgorithmType } from '../models/demo-setting';
import { FormGroup, FormBuilder, Validators } from '@angular/forms';
import {MatDialog, MatDialogRef, MAT_DIALOG_DATA} from '@angular/material/dialog';

interface RepresentTypePair {
  key: RepresentType;
  value: string;
  img:string;
  disabled:boolean;
}
interface AlgorithmTypePair {
  key: AlgorithmType;
  value: string;
  description:string;
  disabled:boolean;
}

@Component({
  selector: 'app-algorithm-setting',
  templateUrl: './algorithm-setting.component.html',
  styleUrls: ['./algorithm-setting.component.css']
})
export class AlgorithmSettingComponent implements OnInit {


  public testDataSettings: FormGroup;
  public representTypes:RepresentTypePair[];
  public algorithmTypes:AlgorithmTypePair[];

  public selectedRepresent:RepresentType;
  public selectedAlgorithm:AlgorithmType;

  constructor(fb: FormBuilder,
      public dialogRef: MatDialogRef<AlgorithmSettingComponent>,
      @Inject(MAT_DIALOG_DATA) public demoSetting: DemoSetting) {
    this.testDataSettings = fb.group({
      jobNum:[this.demoSetting.jobNum, [Validators.required, Validators.min(2), Validators.max(8)]],
      taskNum:[this.demoSetting.taskNumPerJob, [Validators.required, Validators.min(2), Validators.max(8)]],
    });

    this.representTypes=[{key:RepresentType.MixedIntegerModel,value:"Mixed Integer Model",img:"/assets/imgs/model1.png",disabled:false},
                          {key:RepresentType.DisjunctiveGraph,value:"Disjunctive Graph",img:"/assets/imgs/model2.png",disabled:false},];
    this.algorithmTypes=[{key:AlgorithmType.RandomSequenceHeuristic,value:AlgorithmType[AlgorithmType.RandomSequenceHeuristic],description:"A simple heuristic algorihtm, just assign jobs one by one",disabled:false},
                         {key:AlgorithmType.NativeSimulatedAnnealing,value:AlgorithmType[AlgorithmType.NativeSimulatedAnnealing],description:"Basic Simulated Annealing algorithm",disabled:false},
                         {key:AlgorithmType.NativeGeneticAlgorithm,value:AlgorithmType[AlgorithmType.NativeGeneticAlgorithm],description:"Basic Genetic algorithm",disabled:false},
                         {key:AlgorithmType.NativeTubeSearch,value:AlgorithmType[AlgorithmType.NativeTubeSearch],description:"Basic Tube Search algorithm",disabled:false},
                         {key:AlgorithmType.GoogleOrToolCpSolver,value:AlgorithmType[AlgorithmType.GoogleOrToolCpSolver],description:"Use Google OR-Tools Solver with CP model",disabled:false},
                         {key:AlgorithmType.OptaPlannerSolver,value:AlgorithmType[AlgorithmType.OptaPlannerSolver],description:"Use Optaplanner Solver with Meta-heuristic algorithm (Tabu search, 5 min)",disabled:false},];   
    this.selectedRepresent=this.demoSetting.representType;
    this.selectedAlgorithm=this.demoSetting.algorithmType;
  }

  ngOnInit() {
  }

  closeDialog(): void {
    let result=new DemoSetting();
    result.jobNum=this.testDataSettings.get('jobNum').value;
    result.taskNumPerJob=this.testDataSettings.get('taskNum').value;
    result.representType=this.selectedRepresent;
    result.algorithmType=this.selectedAlgorithm;
    this.dialogRef.close(result);
  }

  closeDialogWithoutReturn():void{
    this.dialogRef.close();
  }

  representChange(e:any):void{
    // if(e.value==0 || e.value==1){
    //   this.algorithmTypes[0].disabled=false;
    //   this.algorithmTypes[1].disabled=false;
    //   this.algorithmTypes[2].disabled=false;
    //   this.algorithmTypes[3].disabled=false;
    //   this.algorithmTypes[4].disabled=true;
    //   this.selectedAlgorithm=AlgorithmType.NativeSimulatedAnnealing;
    // }
    // if(e.value==2){
    //   this.algorithmTypes[0].disabled=true;
    //   this.algorithmTypes[1].disabled=true;
    //   this.algorithmTypes[2].disabled=true;
    //   this.algorithmTypes[3].disabled=true;
    //   this.algorithmTypes[4].disabled=false;
    //   this.selectedAlgorithm=AlgorithmType.GoogleOrToolCpSolver;
    // }
  }
}
