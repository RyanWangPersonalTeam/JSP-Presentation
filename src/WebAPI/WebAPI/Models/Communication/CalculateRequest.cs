using System;
using System.Collections.Generic;
using System.Linq;
using System.Threading.Tasks;

namespace WebAPI.Models.Communication
{
    public class CalculateRequest
    {
        public bool Initialized { get; set; }
        public string RepresentType { get; set; }
        public string AlgorithmType { get; set; }
        public Solution UnsolvedSolution { get; set; }
    }
}
