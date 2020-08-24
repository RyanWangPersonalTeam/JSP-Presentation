package wzy.jsp.simplest.demo.generator;

import wzy.jsp.simplest.demo.domain.communication.Job;
import wzy.jsp.simplest.demo.domain.communication.Solution;
import wzy.jsp.simplest.demo.domain.communication.Task;

import java.util.ArrayList;
import java.util.UUID;

public class TestSolutionGenerator {
    public Solution GenerateTestSolution(){
        Solution solution=new Solution();
        solution.FinalResult=false;
        solution.Id= UUID.randomUUID().toString();
        solution.ClientId=UUID.randomUUID().toString();
        solution.MinTime="2020-07-20 00:00:00";
        solution.MaxTime="2020-07-22 00:00:00";
        solution.Jobs=new ArrayList<>();

        Job job0=new Job();
        job0.Id=UUID.randomUUID().toString();
        job0.Name="Job_0";
        job0.Tasks=new ArrayList<>();
        Task task00=new Task();
        task00.Id=UUID.randomUUID().toString();
        task00.Machine="M1";
        task00.Name="Task_0_0";
        task00.Duration=3*60;
        Task task01=new Task();
        task01.Id=UUID.randomUUID().toString();
        task01.Machine="M3";
        task01.Name="Task_0_1";
        task01.Duration=4*60;
        job0.Tasks.add(task00);
        job0.Tasks.add(task01);

        Job job1=new Job();
        job1.Id=UUID.randomUUID().toString();
        job1.Name="Job_1";
        job1.Tasks=new ArrayList<>();
        Task task10=new Task();
        task10.Id=UUID.randomUUID().toString();
        task10.Machine="M3";
        task10.Name="Task_1_0";
        task10.Duration=2*60;
        Task task11=new Task();
        task11.Id=UUID.randomUUID().toString();
        task11.Machine="M1";
        task11.Name="Task_1_1";
        task11.Duration=3*60;
        Task task12=new Task();
        task12.Id=UUID.randomUUID().toString();
        task12.Machine="M2";
        task12.Name="Task_1_2";
        task12.Duration=4*60;
        job1.Tasks.add(task10);
        job1.Tasks.add(task11);
        job1.Tasks.add(task12);

        Job job2=new Job();
        job2.Id=UUID.randomUUID().toString();
        job2.Name="Job_2";
        job2.Tasks=new ArrayList<>();
        Task task20=new Task();
        task20.Id=UUID.randomUUID().toString();
        task20.Machine="M1";
        task20.Name="Task_2_0";
        task20.Duration=4*60;
        Task task21=new Task();
        task21.Id=UUID.randomUUID().toString();
        task21.Machine="M2";
        task21.Name="Task_2_1";
        task21.Duration=5*60;
        Task task22=new Task();
        task22.Id=UUID.randomUUID().toString();
        task22.Machine="M3";
        task22.Name="Task_2_2";
        task22.Duration=2*60;
        job2.Tasks.add(task20);
        job2.Tasks.add(task21);
        job2.Tasks.add(task22);

        solution.Jobs.add(job0);
        solution.Jobs.add(job1);
        solution.Jobs.add(job2);

        return solution;
    }

    public Solution GenerateTestSolution2(){
        Solution solution=new Solution();
        solution.FinalResult=false;
        solution.Id= UUID.randomUUID().toString();
        solution.ClientId=UUID.randomUUID().toString();
        solution.MinTime="2020-07-20 00:00:00";
        solution.MaxTime="2020-07-22 00:00:00";
        solution.Jobs=new ArrayList<>();

        Job job0=new Job();
        job0.Id=UUID.randomUUID().toString();
        job0.Name="Job_0";
        job0.Tasks=new ArrayList<>();
        Task task00=new Task();
        task00.Id=UUID.randomUUID().toString();
        task00.Machine="M1";
        task00.Name="Task_0_0";
        task00.Duration=3*60;
        task00.StartTime="2020-07-20 00:00:00";
        Task task01=new Task();
        task01.Id=UUID.randomUUID().toString();
        task01.Machine="M2";
        task01.Name="Task_0_1";
        task01.Duration=2*60;
        task01.StartTime="2020-07-20 02:00:00";
        job0.Tasks.add(task00);
        job0.Tasks.add(task01);

        Job job1=new Job();
        job1.Id=UUID.randomUUID().toString();
        job1.Name="Job_1";
        job1.Tasks=new ArrayList<>();
        Task task10=new Task();
        task10.Id=UUID.randomUUID().toString();
        task10.Machine="M1";
        task10.Name="Task_1_0";
        task10.Duration=2*60;
        task10.StartTime="2020-07-20 00:10:00";
        job1.Tasks.add(task10);

        solution.Jobs.add(job0);
        solution.Jobs.add(job1);

        return solution;
    }
}
