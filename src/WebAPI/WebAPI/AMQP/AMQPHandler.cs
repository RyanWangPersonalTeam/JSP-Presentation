using System;
using System.Collections.Generic;
using System.Linq;
using System.Text;
using System.Threading.Tasks;
using WebAPI.Models.Communication;
using Newtonsoft.Json;
using RabbitMQ.Client;
using RabbitMQ.Client.Events;

namespace WebAPI.AMQP
{
    public class AMQPHandler : IDisposable
    {
        private string QUEUE_SERVICESTATUS = "queue_service_status";
        private string QUEUE_REQUEST = "queue_request";
        private string QUEUE_RESPONSE = "queue_response";

        private ConnectionFactory factory;
        private IConnection serviceStatusConnection;
        private IModel serviceStatusChannel;
        private IConnection calculateRequestConnection;
        private IModel calculateRequestChannel;
        private IConnection calculateResponseConnection;
        private IModel calculateResponseChannel;

        public AMQPHandler(string host)
        {
            this.factory= new ConnectionFactory() { HostName = host };
        }

        public bool IsServiceBusy()
        {
            if (this.serviceStatusConnection == null)
            {
                this.serviceStatusConnection=this.factory.CreateConnection();
            }
            if (this.serviceStatusChannel == null)
            {
                this.serviceStatusChannel = this.serviceStatusConnection.CreateModel();
            }
            bool busy = false;
            Dictionary<string, object> args = new Dictionary<string, object>();
            args.Add("x-max-length", 1);
            this.serviceStatusChannel.QueueDeclare(queue: QUEUE_SERVICESTATUS,
                                         durable: false,
                                         exclusive: false,
                                         autoDelete: false,
                                         arguments: args);

            var consumer = new EventingBasicConsumer(this.serviceStatusChannel);
            consumer.Received += (model, ea) =>
            {
                var body = ea.Body.ToArray();
                var message = Encoding.UTF8.GetString(body);
                var serviceStatus = JsonConvert.DeserializeObject<ServiceStatus>(message);
                Console.WriteLine(" [x] Received {0}", message);
                busy = serviceStatus.Busy;
                //this.serviceStatusChannel.BasicCancel(consumer.ConsumerTags[0]);
            };
            this.serviceStatusChannel.BasicConsume(queue: QUEUE_SERVICESTATUS,
                                         autoAck: false,
                                         consumer: consumer);
            System.Threading.Thread.Sleep(500);
            if (this.serviceStatusChannel != null)
            {
                this.serviceStatusChannel.Close();
            }
            if (this.serviceStatusConnection != null)
            {
                this.serviceStatusConnection.Close();
            }
            return busy;
        }

        public void RegisterResponseHandle(EventHandler<BasicDeliverEventArgs> eventHandler)
        {
            if (this.calculateResponseConnection == null)
            {
                this.calculateResponseConnection = this.factory.CreateConnection();
            }
            if (this.calculateResponseChannel == null)
            {
                this.calculateResponseChannel = this.calculateResponseConnection.CreateModel();
            }
            Dictionary<string, object> args = new Dictionary<string, object>();
            args.Add("x-message-ttl", 60000);
            args.Add("x-max-length", 5);
            this.calculateResponseChannel.QueueDeclare(queue: QUEUE_RESPONSE,
                                     durable: false,
                                     exclusive: false,
                                     autoDelete: false,
                                     arguments: args);

            var consumer = new EventingBasicConsumer(this.calculateResponseChannel);
            consumer.Received += eventHandler;
            this.calculateResponseChannel.BasicConsume(queue: QUEUE_RESPONSE,
                                             autoAck: true,
                                             consumer: consumer);
        }

        public void SendCalculateRequest(CalculateRequest calculateRequest)
        {
            if (this.calculateRequestConnection == null)
            {
                this.calculateRequestConnection = this.factory.CreateConnection();
            }
            if (this.calculateRequestChannel == null)
            {
                this.calculateRequestChannel = this.calculateRequestConnection.CreateModel();
            }
            Dictionary<string, object>  args = new Dictionary<string, object>();
            args.Add("x-max-length", 1);
            args.Add("x-message-ttl", 60000);
            this.calculateRequestChannel.QueueDeclare(queue: QUEUE_REQUEST,
                                         durable: false,
                                         exclusive: false,
                                         autoDelete: false,
                                         arguments: args);

            var message = JsonConvert.SerializeObject(calculateRequest);
            var body = Encoding.UTF8.GetBytes(message);

            this.calculateRequestChannel.BasicPublish(exchange: "",
                                             routingKey: QUEUE_REQUEST,
                                             basicProperties: null,
                                             body: body);
            Console.WriteLine(" [x] Sent {0}", message);
        }



        public void Dispose()
        {
            if (this.serviceStatusChannel != null)
            {
                this.serviceStatusChannel.Close();
            }
            if (this.serviceStatusConnection != null)
            {
                this.serviceStatusConnection.Close();
            }
            if (this.calculateRequestChannel != null)
            {
                this.calculateRequestChannel.Close();
            }
            if (this.calculateRequestConnection != null)
            {
                this.calculateRequestConnection.Close();
            }
            if (this.calculateResponseChannel != null)
            {
                this.calculateResponseChannel.Close();
            }
            if (this.calculateResponseConnection != null)
            {
                this.calculateResponseConnection.Close();
            }
        }
    }
}
