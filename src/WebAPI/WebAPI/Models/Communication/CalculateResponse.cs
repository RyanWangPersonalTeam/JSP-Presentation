using System;
using System.Collections.Generic;
using System.Linq;
using System.Threading.Tasks;

namespace WebAPI.Models.Communication
{
    public class CalculateResponse
    {
        public bool Success { get; set; }
        public Solution SolvedSolution { get; set; }
        public string ErrorMessage { get; set; }

        public string LogInfo { get; set; }
    }
}
