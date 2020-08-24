using WebAPI.Models;
using WebAPI.Models.Communication;
using Microsoft.AspNetCore.SignalR;
using Microsoft.Extensions.Configuration;
using Newtonsoft.Json;
using RabbitMQ.Client;
using RabbitMQ.Client.Events;
using System;
using System.Collections.Generic;
using System.Linq;
using System.Text;
using System.Threading.Tasks;
using WebAPI.AMQP;

namespace WebAPI.Hubs
{
    public class AlgorithmHub:Hub
    {
        private static IConfiguration _config;
        private static IHubContext<AlgorithmHub> _hubContext; 
        private AMQPHandler amqpHandler;
        private static HashSet<string> connectionIds;

        public AlgorithmHub(IConfiguration configuration, IHubContext<AlgorithmHub> ctx)
        {
            if (_config == null)
                _config = configuration;

            if (_hubContext == null)
                _hubContext = ctx;

            if (connectionIds == null)
                connectionIds = new HashSet<string>();

            if (this.amqpHandler == null)
            {
                this.amqpHandler = new AMQPHandler("rabbitmq");//if deployed into container, it should be the container name or service name
                this.amqpHandler.RegisterResponseHandle(
                        (model, ea) =>
                        {
                            var body = ea.Body.ToArray();
                            var message = Encoding.UTF8.GetString(body);
                            var response = JsonConvert.DeserializeObject<CalculateResponse>(message);
                            Console.WriteLine(" [x] Received {0}", message);
                            string clientId = null;
                            if (response.SolvedSolution != null)
                            {
                                clientId = response.SolvedSolution.ClientId;
                            }
                            if (string.IsNullOrEmpty(clientId))
                            {
                                _hubContext.Clients.All.SendAsync("PushSolutionResponse", response);
                            }
                            else
                            {
                                _hubContext.Clients.Client(clientId).SendAsync("PushSolutionResponse", response);
                            }
                        }
                    );
            }
        }

        public async void NewCalculateRequest(CalculateRequest calculateRequest)
        {
            AlgorithmHub.connectionIds.Add(Context.ConnectionId);
            bool busy = this.amqpHandler.IsServiceBusy();
            if (busy)
            {
                await _hubContext.Clients.Client(Context.ConnectionId).SendAsync("PushSolutionResponse",
                    new CalculateResponse { Success = false, ErrorMessage = "Algorithm service is busy now" });
            }
            else
            {
                calculateRequest.UnsolvedSolution.ClientId = Context.ConnectionId;
                this.amqpHandler.SendCalculateRequest(calculateRequest);
            }
        }

        public new void Dispose()
        {
            base.Dispose();
            this.amqpHandler.Dispose();
        }

        public async override System.Threading.Tasks.Task OnDisconnectedAsync(Exception exception)
        {
            AlgorithmHub.connectionIds.Remove(Context.ConnectionId);
            Console.WriteLine($"connection : {Context.ConnectionId} closed");
            await base.OnDisconnectedAsync(exception);
        }
    }

    
}
