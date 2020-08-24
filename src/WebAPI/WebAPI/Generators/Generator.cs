using WebAPI.Models;
using System;
using System.Collections.Generic;
using System.Linq;
using System.Threading.Tasks;

namespace WebAPI.Generators
{
    public class Generator
    {
        public static Solution GenerateTestSolution()
        {
            Random rnd = new Random();

            Solution solution = new Solution();
            solution.FinalResult = true;
            solution.MinTime = "2020-07-15 00:00";
            solution.MaxTime = "2020-07-15 24:00";
            List<Job> jobs = new List<Job>();
            Job job0 = new Job { Id = "job0", Name = "Job0" };
            job0.Tasks = new List<Models.Task>();
            string startTime = "2020-07-15 00:00";
            string endTime = (DateTime.ParseExact(startTime, "yyyy-MM-dd HH:mm:ss", System.Globalization.CultureInfo.CurrentCulture))
                .AddHours(rnd.Next(2, 6)).ToString("yyyy-MM-dd HH:mm:ss");
            job0.Tasks.Add(
                new Models.Task { 
                    Id="Task_0_0", 
                    Name="Task_0_0",
                    StartTime= startTime,
                    EndTime = endTime,
                    Machine="Machine A"}
                );
            startTime = endTime;
            endTime = (DateTime.ParseExact(startTime, "yyyy-MM-dd HH:mm:ss", System.Globalization.CultureInfo.CurrentCulture))
                .AddHours(rnd.Next(2, 6)).ToString("yyyy-MM-dd HH:mm:ss");
            job0.Tasks.Add(
                new Models.Task
                {
                    Id = "Task_0_1",
                    Name = "Task_0_1",
                    StartTime = startTime,
                    EndTime = endTime,
                    Machine = "Machine B",
                }
                );
            startTime = endTime;
            endTime = (DateTime.ParseExact(startTime, "yyyy-MM-dd HH:mm:ss", System.Globalization.CultureInfo.CurrentCulture))
                .AddHours(rnd.Next(2, 6)).ToString("yyyy-MM-dd HH:mm:ss");
            job0.Tasks.Add(
                new Models.Task
                {
                    Id = "Task_0_2",
                    Name = "Task_0_2",
                    StartTime = startTime,
                    EndTime = endTime,
                    Machine = "Machine C",
                }
                );

            jobs.Add(job0);

            solution.Jobs = jobs;
            return solution;
        }

        public static Solution GenerateRandomUnSolvedSolution(int jobNum, int taskNumPerJob)
        {
            Random rnd = new Random();
            Solution solution = new Solution();
            solution.Id=System.Guid.NewGuid().ToString();
            solution.FinalResult = false;
            solution.MinTime = DateTime.Now.ToString("yyyy-MM-dd HH:mm:ss");
            solution.MaxTime = DateTime.Now.AddDays(jobNum).ToString("yyyy-MM-dd HH:mm:ss");
            List<string> Machines = new List<string> { "Machine A", "Machine B", "Machine C", "Machine D", "Machine E", "Machine F", };

            List<Job> jobs = new List<Job>();
            for(int i = 0; i < jobNum; i++)
            {
                Job job = new Job
                {
                    Id = System.Guid.NewGuid().ToString(),
                    Name = "Job_" + i,
                    Tasks = new List<Models.Task>()
                };
                
                for(int j = 0; j < taskNumPerJob; j++)
                {
                    Models.Task task = new Models.Task
                    {
                        Id = System.Guid.NewGuid().ToString(),
                        Name = "Task_" + i + "_" + j,
                        Machine= Machines[rnd.Next(0, Machines.Count)],
                    };
                    long duration = rnd.Next(180, 10 * 60 + 5);
                    duration = duration / 5 * 5;
                    task.Duration = duration;
                    job.Tasks.Add(task);
                }
                jobs.Add(job);
            }
            solution.Jobs = jobs;
            return solution;
        }

        public static Solution SolveWithSimpleHeuristic(Solution unSolvedSoltion)
        {
            Dictionary<String, string> machineTimeRecords = new Dictionary<string, string>();
            string minStartTime = unSolvedSoltion.MinTime;
            string maxStartTime = unSolvedSoltion.MaxTime;
            for (int i = 0; i < unSolvedSoltion.Jobs.Count; i++)
            {
                Job currentJob = unSolvedSoltion.Jobs[i];
                string predecessorEndTime = null; 
                for(int j = 0; j < currentJob.Tasks.Count; j++)
                {
                    Models.Task currentTask = currentJob.Tasks[j];
                    string machine = currentTask.Machine;
                    if (!machineTimeRecords.ContainsKey(machine))
                    {
                        if (j == 0)
                        {
                            currentTask.StartTime = minStartTime;
                            currentTask.EndTime = DateTime.ParseExact(currentTask.StartTime, "yyyy-MM-dd HH:mm:ss", System.Globalization.CultureInfo.CurrentCulture)
                                                    .AddMinutes(currentTask.Duration)
                                                    .ToString("yyyy-MM-dd HH:mm:ss");
                        }
                        else
                        {
                            currentTask.StartTime = predecessorEndTime;
                            currentTask.EndTime = DateTime.ParseExact(currentTask.StartTime, "yyyy-MM-dd HH:mm:ss", System.Globalization.CultureInfo.CurrentCulture)
                                                    .AddMinutes(currentTask.Duration)
                                                    .ToString("yyyy-MM-dd HH:mm:ss");
                        }
                        machineTimeRecords.Add(machine, currentTask.EndTime);
                    }
                    else
                    {
                        if (j == 0)
                        {
                            currentTask.StartTime = machineTimeRecords[machine];
                            currentTask.EndTime = DateTime.ParseExact(currentTask.StartTime, "yyyy-MM-dd HH:mm:ss", System.Globalization.CultureInfo.CurrentCulture)
                                                    .AddMinutes(currentTask.Duration)
                                                    .ToString("yyyy-MM-dd HH:mm:ss");
                        }
                        else
                        {
                           
                            DateTime d1 = DateTime.ParseExact(predecessorEndTime, "yyyy-MM-dd HH:mm:ss", System.Globalization.CultureInfo.CurrentCulture);
                            DateTime d2 = DateTime.ParseExact(machineTimeRecords[machine], "yyyy-MM-dd HH:mm:ss", System.Globalization.CultureInfo.CurrentCulture);
                            if (d1.CompareTo(d2) < 0)
                            {
                                currentTask.StartTime = d2.ToString("yyyy-MM-dd HH:mm:ss");
                            }
                            else
                            {
                                currentTask.StartTime = d1.ToString("yyyy-MM-dd HH:mm:ss");
                            }

                            currentTask.EndTime = DateTime.ParseExact(currentTask.StartTime, "yyyy-MM-dd HH:mm:ss", System.Globalization.CultureInfo.CurrentCulture)
                                                    .AddMinutes(currentTask.Duration)
                                                    .ToString("yyyy-MM-dd HH:mm:ss");
                        }
                        machineTimeRecords[machine] = currentTask.EndTime;
                    }
                    predecessorEndTime = currentTask.EndTime;
                }
            }

            return unSolvedSoltion;
        }

    }
}
