import { Component, OnInit, ElementRef } from '@angular/core';
import { SolutionChartConvertService} from './services/solution-chart-convert.service';
import * as signalR from '@aspnet/signalr';
import { Solution } from './models/solution';
import { HttpClient, HttpHeaders } from '@angular/common/http';
import { CalculateResponse } from './models/calculate-response';
import { CalculateRequest } from './models/calculate-request';
import { MatDialog } from '@angular/material/dialog';
import { DemoSetting, RepresentType, AlgorithmType } from './models/demo-setting';
import { AlgorithmSettingComponent } from './algorithm-setting/algorithm-setting.component';
import { environment } from '../environments/environment';

enum CurrentStatus {
  None,
  NewDataCreated,
  ClickCalculateButton,
  AlgorithmServiceIsBusyNow,
  Calculating,
  CalculateComplete
}

@Component({
  selector: 'app-root',
  templateUrl: './app.component.html',
  styleUrls: ['./app.component.css']
})
export class AppComponent implements OnInit{
  baseUrl=environment.baseUrl;
  config: any;
  gstcState: any;
  testHub: signalR.HubConnection;
  currentSolution: Solution;
  disableGenerateTestData:boolean;
  disabledCalculate:boolean;
  disabledSpinner:boolean;
  disabledSetting:boolean;
  currentStatusStr:string;

  public demoSetting:DemoSetting;
  representNames:string[]=[];
  algorithmNames:string[]=[];

  currentStatus:CurrentStatus;

  public timeBegan = null
  public timeStopped:any = null
  public timer = null
  public blankTime = "00:00.000"
  public time = "00:00.000"


  constructor(private solutionChartConvertService:SolutionChartConvertService,
    private http:HttpClient,
    public dialog: MatDialog) {

    for (let r in RepresentType) {
      this.representNames.push(RepresentType[r]);
    }
    for (let a in AlgorithmType) {
      this.algorithmNames.push(AlgorithmType[a]);
    }

    this.testHub=new signalR.HubConnectionBuilder()
              .withUrl(this.baseUrl+'/algorithmhub')
              //.withHubProtocol(new signalRmsgpack.MessagePackHubProtocol())
              .configureLogging(signalR.LogLevel.Information)
              .build();
    this.testHub.on('PushSolutionResponse',(response:CalculateResponse)=>{
      if(response.success==false){
        alert(response.errorMessage);
        this.setCurrentStatus(CurrentStatus.AlgorithmServiceIsBusyNow);
      }
      else{
        //console.info("solution : "+JSON.stringify(solution));
        this.currentSolution=response.solvedSolution;
        this.setChartItems(this.currentSolution);
        if(this.currentSolution.finalResult==false){
          this.setCurrentStatus(CurrentStatus.Calculating);
        }
        else{
          this.setCurrentStatus(CurrentStatus.CalculateComplete);
        }
      }
      
    })

    this.setCurrentStatus(CurrentStatus.None);

    this.demoSetting=new DemoSetting();
    this.demoSetting.jobNum=4;
    this.demoSetting.taskNumPerJob=4;
    this.demoSetting.representType=RepresentType.MixedIntegerModel;
    this.demoSetting.algorithmType=AlgorithmType.GoogleOrToolCpSolver;
  }

  ngOnInit() {

    let chartItems=this.solutionChartConvertService.generateInitChart();

    let rows =chartItems.rows;

    let items = chartItems.items;

    let columns=chartItems.columns;

    let timeLine=chartItems.timeline;

    this.config = {
      height: 500,
      headerHeight: 50,
      list: {
        rows:rows,
        columns:columns,
        toggle: {
          display: false,
        }
      },
      chart: {
        time:timeLine,
        items:items
      },
    };

    
  }


  onState(state) {
    this.gstcState = state;
    // YOU CAN SUBSCRIBE TO CHANGES

    this.gstcState.subscribe("config.list.rows", rows => {
      console.log("rows changed", rows);
    });

    this.gstcState.subscribe(
      "config.chart.items.:id",
      (bulk, eventInfo) => {
        if (eventInfo.type === "update" && eventInfo.params.id) {
          const itemId = eventInfo.params.id;
          console.log(
            `item ${itemId} changed`,
            this.gstcState.get("config.chart.items." + itemId)
          );
        }
      },
      { bulk: true }
    );
  }

  setChartItems(solution:Solution){
    let chartItems=this.solutionChartConvertService.getChartItemsFromTasks(solution);
    let rows =chartItems.rows;
    let items = chartItems.items;
    let columns=chartItems.columns;
    let timeLine=chartItems.timeline as any;
    
    if(this.gstcState!=undefined){
      this.gstcState.update("config.list.rows", rows);
      this.gstcState.update("config.chart.items", items);
      this.gstcState.update("config.chart.time.from", timeLine.from);
      this.gstcState.update("config.chart.time.to", timeLine.to);
      this.gstcState.update("config.list.columns", columns);
      
    }
  }

  onSampleData(){
    this.http.get(this.baseUrl+"/api/Solve/GetRandomTestData?jobNum="+this.demoSetting.jobNum+"&taskNum="+this.demoSetting.taskNumPerJob)
      .subscribe((data:object) => {
        this.currentSolution=data as Solution;
        this.setChartItems(this.currentSolution);
        this.setCurrentStatus(CurrentStatus.NewDataCreated);
      },
      err=>{console.error(err)});
  }

  async onCalculate(){
      if(this.currentSolution==null){
        return;
      }
      if(!this.testHub){
        return;
      }
      if(this.testHub.state!==signalR.HubConnectionState.Connected){
        await this.testHub.start().catch(err=>{console.error(err)});
      }
      let request=new CalculateRequest();
      request.unsolvedSolution=this.currentSolution;
      request.initialized=true;
      request.representType=RepresentType[this.demoSetting.representType];
      request.algorithmType=AlgorithmType[this.demoSetting.algorithmType];
      this.testHub.send("NewCalculateRequest",request);
      this.setCurrentStatus(CurrentStatus.ClickCalculateButton);
  }

  setCurrentStatus(status:CurrentStatus){
    this.currentStatus=status;

    if(this.currentStatus==CurrentStatus.None){
      this.disableGenerateTestData=false;
      this.disabledCalculate=true;
      this.currentStatusStr='';
      this.disabledSpinner=false;
      this.disabledSetting=false;
    }

    if(this.currentStatus==CurrentStatus.NewDataCreated){
      this.disableGenerateTestData=false;
      this.disabledCalculate=false;
      this.currentStatusStr='';
      this.disabledSpinner=false;
      this.disabledSetting=false;
    }

    if(this.currentStatus==CurrentStatus.ClickCalculateButton){
      this.disableGenerateTestData=true;
      this.disabledCalculate=true;
      this.currentStatusStr='';
      this.disabledSpinner=true;
      this.disabledSetting=true;
      this.timeCostStart();
    }

    if(this.currentStatus==CurrentStatus.Calculating){
      this.disableGenerateTestData=true;
      this.disabledCalculate=true;
      this.currentStatusStr='Calculating...';
      this.disabledSpinner=true;
      this.disabledSetting=true;
      
    }

    if(this.currentStatus==CurrentStatus.AlgorithmServiceIsBusyNow){
      this.disableGenerateTestData=false;
      this.disabledCalculate=false;
      this.currentStatusStr='';
      this.disabledSpinner=false;
      this.disabledSetting=false;
      this.timeCostReset();
    }

    if(this.currentStatus==CurrentStatus.CalculateComplete){
      this.disableGenerateTestData=false;
      this.disabledCalculate=false;
      this.currentStatusStr='Complete calculation!';
      this.disabledSpinner=false;
      this.disabledSetting=false;
      this.timeCostStop();
    }
  }

  changeDemoSetting(){
    const dialogRef = this.dialog.open(AlgorithmSettingComponent, {
      data: this.demoSetting,
      height:'100%',
      width:'80%'
    });

    dialogRef.afterClosed().subscribe(result => {
      if(result!=null)
      {
        console.info(result);
        this.demoSetting=result;
      }

    });
  }

  timeCostStart() { 
    this.timeCostReset();
    this.timeBegan = new Date(); 
    this.timer = setInterval(this.clockRunning.bind(this), 10); 
  }

  timeCostStop() {
    clearInterval(this.timer);
 }

 timeCostReset() {
    clearInterval(this.timer);
    this.time = this.blankTime;
  }

  zeroPrefix(num, digit) {
    let zero = '';
    for(let i = 0; i < digit; i++) {
      zero += '0';
    }
    return (zero + num).slice(-digit);
  }

  clockRunning(){
    let currentTime:any = new Date()
    let timeElapsed:any = new Date(currentTime - this.timeBegan)
    let hour = timeElapsed.getUTCHours()
    let min = timeElapsed.getUTCMinutes()
    let sec = timeElapsed.getUTCSeconds()
    let ms = timeElapsed.getUTCMilliseconds();    
    this.time =this.zeroPrefix(hour, 2) + ":" +
                this.zeroPrefix(min, 2) + ":" +
                this.zeroPrefix(sec, 2) + "." +
                this.zeroPrefix(ms, 3);
  };

}



