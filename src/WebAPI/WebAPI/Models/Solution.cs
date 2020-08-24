using System;
using System.Collections.Generic;
using System.Linq;
using System.Threading.Tasks;

namespace WebAPI.Models
{
    public class Solution
    {
        public string Id { get; set; }
        public string ClientId { get; set; }
        public bool FinalResult { get; set; }
        public string ErrMessage { get; set; }
        public string MinTime { get; set; }
        public string MaxTime { get; set; }
        public List<Job> Jobs { get; set; }
    }
}
