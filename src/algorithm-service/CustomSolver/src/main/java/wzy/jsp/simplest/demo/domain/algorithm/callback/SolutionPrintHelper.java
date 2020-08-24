package wzy.jsp.simplest.demo.domain.algorithm.callback;

import wzy.jsp.simplest.demo.component.DateTimeConverter;
import wzy.jsp.simplest.demo.domain.communication.Solution;
import wzy.jsp.simplest.demo.domain.communication.Task;

import java.util.*;

public class SolutionPrintHelper {
    public static void printScheduleSolution(Solution solution){
        Map<String, List<Task>> tasksGroupByMachine = new HashMap<>();
        for(int i=0;i<solution.Jobs.size();i++){
            for(int j=0;j<solution.Jobs.get(i).Tasks.size();j++){
                String machine=solution.Jobs.get(i).Tasks.get(j).Machine;
                List<Task> tempList=tasksGroupByMachine.get(machine);
                if(tempList==null){
                    tempList=new ArrayList<>();
                    tempList.add(solution.Jobs.get(i).Tasks.get(j));
                    tasksGroupByMachine.put(machine,tempList);
                }
                else{
                    tempList.add(solution.Jobs.get(i).Tasks.get(j));
                }
            }
        }

        for(String machineName:tasksGroupByMachine.keySet()){
            String str=String.format("Machine (%s) : ",machineName);
            List<Task> tasksThisMachine=tasksGroupByMachine.get(machineName);
            Collections.sort(tasksThisMachine);
            for(Task task:tasksThisMachine){
                str+=String.format("%s(S: %s D: %d E: %s) | ",task.Name,task.StartTime,task.Duration,task.EndTime);
            }
            str+="\r\n";
            System.out.printf(str);
            System.out.printf("-------------------------------------------------------------------------------------------\r\n");
        }

        System.out.printf("\r\n");
        System.out.printf("\r\n");
    }
}
