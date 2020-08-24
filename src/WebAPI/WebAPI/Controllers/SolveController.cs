using System;
using System.Collections.Generic;
using System.Linq;
using System.Threading.Tasks;
using Microsoft.AspNetCore.Http;
using Microsoft.AspNetCore.Mvc;
using Microsoft.AspNetCore.SignalR;
using WebAPI.Generators;
using WebAPI.Hubs;
using WebAPI.Models;

namespace WebAPI.Controllers
{
    [Route("api/[controller]")]
    [ApiController]
    public class SolveController : ControllerBase
    {
        private IHubContext<AlgorithmHub> _hub;

        public SolveController(IHubContext<AlgorithmHub> hub)
        {
            _hub = hub;
        }

        [HttpGet("GetRandomTestData")]
        public Solution GetRandomTestData(int jobNum, int taskNum)
        {
            Solution unSolvedSoltion = Generator.GenerateRandomUnSolvedSolution(jobNum, taskNum);
            Solution solvedBasic = Generator.SolveWithSimpleHeuristic(unSolvedSoltion);

            return solvedBasic;
        }
    }
}