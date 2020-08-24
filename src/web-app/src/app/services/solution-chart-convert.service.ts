import { Injectable } from '@angular/core';
import { Solution } from '../models/solution';
import { ChartItems } from '../models/chart-items';
import { Task } from '../models/task';
import { rendererTypeName } from '@angular/compiler';
import { Job } from '../models/job';
import * as uuid from 'uuid';
import { DatePipe } from '@angular/common';

@Injectable({
  providedIn: 'root'
})
export class SolutionChartConvertService {

  colors:string[]=[
    'red', 
    'blue', 
    'blueviolet',
    'burlywood',
    'chartreuse',
    'darkgreen',
    'khaki',
    'aliceblue',
    'deeppink',
    'lightcoral',
    'orange',
    'orangered',
    'yellowgreen',
    'aqua',
    'black',
    'cadetblue',
    'darkmagenta',
    'brown',
    'olive',
    'yellow',
  ];


  constructor(private datepipe: DatePipe) { 
  }


  generateInitChart():ChartItems{
    let chartItems:ChartItems=new ChartItems();

    let minTime=new Date().getTime();
    let tempTime=new Date();
    tempTime.setHours(new Date().getHours()+24*7);
    let maxTime=tempTime.getTime();
    chartItems.rows=[
      {
        id:'0',
        label: "0",
        expanded: false
      },
      {
        id:'1',
        label: "1",
        expanded: false
      },
      {
        id:'2',
        label: "2",
        expanded: false
      },
      {
        id:'3',
        label: "3",
        expanded: false
      },
      {
        id:'4',
        label: "4",
        expanded: false
      },
      {
        id:'5',
        label: "5",
        expanded: false
      },
    ];

    chartItems.items=[
      {
        id:'0',
        label: "",
        time: {
          start: minTime,
          end: minTime
        },
        rowId: 0
      },
      {
        id:'1',
        label: "",
        time: {
          start: minTime,
          end: minTime
        },
        rowId: 1
      },
      {
        id:'2',
        label: "",
        time: {
          start: minTime,
          end: minTime
        },
        rowId: 2
      },
      {
        id:'3',
        label: "",
        time: {
          start: minTime,
          end: minTime
        },
        rowId: 3
      },
      {
        id:'4',
        label: "",
        time: {
          start: minTime,
          end: minTime
        },
        rowId: 4
      },
      {
        id:'5',
        label: "",
        time: {
          start: minTime,
          end: minTime
        },
        rowId: 5
      },
    ];

    chartItems.columns={
      percent: 80,
      resizer: {
        inRealTime: true
      },
      data: {
        label: {
          id: "label",
          data: "label",
          expander: false,
          isHtml: false,
          width: 150,
          minWidth: 100,
          header: {
            content: ""
          }
        }
      }
    };

    chartItems.timeline={
      from:minTime,
      to:maxTime,
      period: 'hour',
      zoom: 11,
    };

    return chartItems;

  }

  generateRandomTestTasks(minTime:string,maxTime:string):Solution{
    let solution:Solution=new Solution();

    let machines=['A','B','C','D','E'];
    let jobNum:number=this.randomIntFromInterval(1,8);
    let jobs:Job[]=[];
    for(let i=0;i<jobNum;i++){
      let job=new Job();
      job.id=uuid.v4().toString();
      job.name="Job_"+i;
      let tasks:Task[]=[];
      let taskNumThisJob:number=this.randomIntFromInterval(3,3);
      let lastEndTime=minTime;
      for(let j=0;j<taskNumThisJob;j++){
        let task=new Task();
        task.id=uuid.v4().toString();
        task.name="Task_"+i+"_"+j;
        let delay=this.randomIntFromInterval(5,5);
        let duration=this.randomIntFromInterval(5,10);
        let dateLast=new Date(lastEndTime);
        task.startTime=this.datepipe.transform(new Date().setHours(dateLast.getHours()+delay), 'yyyy-MM-dd HH:mm');
        task.endTime=this.datepipe.transform(new Date().setHours(dateLast.getHours()+delay+duration), 'yyyy-MM-dd HH:mm');  
        let selectMachine=machines[this.randomIntFromInterval(0,machines.length-1)];
        task.machine=selectMachine;
        lastEndTime=task.endTime;
        tasks.push(task);
      }
      job.tasks=tasks;
      jobs.push(job);
    }

    solution.minTime=minTime;
    solution.maxTime=maxTime;
    solution.jobs=jobs;
    return solution;
  }

  getChartItemsFromTasks(solution:Solution):ChartItems{
    
    let chartItems:ChartItems=new ChartItems();
    let rows=[];
    let items=[];
    let columns={};

    let itr=0;
    for(let i=0;i<solution.jobs.length;i++){
      for(let j=0;j<solution.jobs[i].tasks.length;j++){
        let currentTask : Task=solution.jobs[i].tasks[j];
        let machineThisTask:string=currentTask.machine;
        if(rows.length==0 || rows.find(row=>row.label==machineThisTask)==undefined){
          rows.push(
            {
              id:itr.toString(),
              label:machineThisTask
            }
          )
          itr++;
        }
      }
    }
    chartItems.rows=rows;
    
    itr=0;
    for(let i=0;i<solution.jobs.length;i++){
      let selectColor:string=this.colors[i%(this.colors.length)];
      for(let j=0;j<solution.jobs[i].tasks.length;j++){
        let currentTask : Task=solution.jobs[i].tasks[j];
        let rowIndex=rows.findIndex(row=>row.label==currentTask.machine);
        
        items.push(
          {
            id:currentTask.id,
            label: currentTask.name,
            time: {
              start: new Date(currentTask.startTime).getTime(),
              end: new Date(currentTask.endTime).getTime()
            },
            rowId: rowIndex.toString(),
            style:{background:selectColor}
          }
        );
        itr++;
      }
    }
    chartItems.items=items;

    chartItems.columns={
      percent: 80,
      resizer: {
        inRealTime: true
      },
      data: {
        label: {
          id: "label",
          data: "label",
          expander: false,
          isHtml: false,
          width: 200,
          minWidth: 100,
          header: {
            content: "Machine"
          }
        }
      }
    };


    let timeline={
        from:new Date(solution.minTime).getTime(),
        to:new Date(solution.maxTime).getTime(),
        period: 'hour',
        zoom: 11,
    }

    chartItems.timeline=timeline;

    return chartItems;
  }

  randomIntFromInterval(min:number, max:number):number { // min and max included 
    return Math.floor(Math.random() * (max - min + 1) + min);
  }


}

