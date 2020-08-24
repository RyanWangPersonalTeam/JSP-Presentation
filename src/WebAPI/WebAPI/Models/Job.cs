using System;
using System.Collections.Generic;
using System.Linq;
using System.Threading.Tasks;

namespace WebAPI.Models
{
    public class Job
    {
        public string Id { get; set; }
        public string Name { get; set; }
        public List<Task> Tasks { get; set; }
    }
}
