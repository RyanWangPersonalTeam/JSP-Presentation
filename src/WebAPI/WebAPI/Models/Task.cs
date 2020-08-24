using System;
using System.Collections.Generic;
using System.Linq;
using System.Threading.Tasks;

namespace WebAPI.Models
{
    public class Task
    {
        public string Id { get; set; }
        public string Name { get; set; }
        public string StartTime { get; set; }
        public string EndTime { get; set; }
        public string Machine { get; set; }

        public long Duration { get; set; } // Unit : Minute
    }
}
