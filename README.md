# JSP-Presentation
A web demo system to present solving basic Job Shop Schedule Problem by different algorithms or solvers.
It uses a third pard UI component in GPL license to show schedule results([angular-gantt-schedule-timeline-calendar](https://github.com/neuronetio/angular-gantt-schedule-timeline-calendar)), so it must also be in GPL.  

# Purpose
In production, Job Shop Schedule Problem is one of the most important optimization problems.I used to be responsible for algorithm interface development for schedule system, and than I was greatly interested in JSP and Operational research. So I develop this web demo to present the process of solving JSP with different algorithms or solvers. Personally and concretely, I want to achieve these purposes:
- Develop the simple and intuitive web UI to present the solving process of different algorithms not noly a final result, it may be helpful to distinguish some differences between algorithms or representation models, and makes it easy to explain the concept of JSP to others
- Try to implement different algorithms solving JSP, which helps me to better understanding them
- Offer a possible idea of schedule solution deployment. As a software engineer, what do I focus on more is the whole software system implement, and it's maintainability and scalability. This demo is a little complex system composed of several modules: web UI, web api, algorithm service and rabbitmq service. And I think using docker to package them is a good choice
