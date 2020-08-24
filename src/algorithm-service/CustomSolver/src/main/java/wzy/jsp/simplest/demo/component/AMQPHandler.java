package wzy.jsp.simplest.demo.component;

import com.google.gson.Gson;
import com.rabbitmq.client.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import wzy.jsp.simplest.demo.common.IHandleNewCalculateRequest;
import wzy.jsp.simplest.demo.domain.communication.Solution;
import wzy.jsp.simplest.demo.domain.communication.amqp.CalculateRequest;
import wzy.jsp.simplest.demo.domain.communication.amqp.CalculateResponse;
import wzy.jsp.simplest.demo.domain.communication.amqp.ServiceStatus;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.atomic.AtomicReference;

public class AMQPHandler implements AutoCloseable{
    final String QUEUE_SERVICESTATUS = "queue_service_status";
    final String QUEUE_REQUEST = "queue_request";
    final String QUEUE_RESPONSE = "queue_response";
    private ConnectionFactory factory;
    private Connection serviceStatusConnection;
    private Channel serviceStatusChannel;
    private Connection calculateRequestConnection;
    private Channel calculateRequestChannel;
    private Connection calculateResponseConnection;
    private Channel calculateResponseChannel;

    private Logger logger;

    private AtomicReference<Boolean> exit=new AtomicReference<>(false);

    public AMQPHandler(String amqpHost){
        this.factory = new ConnectionFactory();
        this.factory.setHost(amqpHost);

        this.logger= LoggerFactory.getLogger(AMQPHandler.class);
    }

    //Send current service status (busy or not busy)
    public void SendServiceStatus(boolean busy) throws Exception{
        if(this.serviceStatusConnection==null){
            this.serviceStatusConnection=this.factory.newConnection();
        }
        if(this.serviceStatusChannel==null){
            this.serviceStatusChannel=this.serviceStatusConnection.createChannel();
        }
        Map<String,Object> args=new HashMap<>();
        args.put("x-max-length",1);
        this.serviceStatusChannel.queueDeclare(QUEUE_SERVICESTATUS, false, false, false, args);
        Gson gson = new Gson();
        ServiceStatus notBusyStatus=new ServiceStatus();
        notBusyStatus.Busy=busy;
        String message = gson.toJson(notBusyStatus);
        this.serviceStatusChannel.basicPublish("", QUEUE_SERVICESTATUS, null, message.getBytes(StandardCharsets.UTF_8));
        this.logger.info("Algorithm service set the status to "+(busy?"'busy'":"'not busy'"));
    }

    //Check current calculate request queue, do callback work if exist
    public void RegisterRequestHandleCallback(IHandleNewCalculateRequest handleNewCalculateRequestCallback) throws Exception{
        if(this.calculateRequestConnection==null){
            this.calculateRequestConnection=this.factory.newConnection();
        }
        if(this.calculateRequestChannel==null){
            this.calculateRequestChannel=this.calculateRequestConnection.createChannel();
        }
        Map<String,Object> args=new HashMap<>();
        args.put("x-message-ttl", 60000);
        args.put("x-max-length",1);
        this.calculateRequestChannel.queueDeclare(QUEUE_REQUEST, false, false, false, args);
        this.logger.info("Algorithm is waiting for calculate request now...");
        this.calculateRequestChannel.basicConsume(QUEUE_REQUEST, true,
                new CustomDeliverCallback(this.calculateRequestChannel,handleNewCalculateRequestCallback,this),
                consumerTag -> { });
    }

    public void SendCalculateResponse(CalculateResponse calculateResponse) throws Exception{
        if(this.calculateResponseConnection==null){
            this.calculateResponseConnection=this.factory.newConnection();
        }
        if(this.calculateResponseChannel==null){
            this.calculateResponseChannel=this.calculateResponseConnection.createChannel();
        }
        Map<String,Object> args=new HashMap<>();
        args.put("x-message-ttl", 60000);
        args.put("x-max-length",5);
        this.calculateResponseChannel.queueDeclare(QUEUE_RESPONSE, false, false, false, args);
        Gson gson = new Gson();
        String message = gson.toJson(calculateResponse);
        this.calculateResponseChannel.basicPublish("", QUEUE_RESPONSE, null, message.getBytes(StandardCharsets.UTF_8));
        this.logger.info("Algorithm service send a calculate response back");
    }

    public void setExit(boolean exit) {
        this.exit.set(exit);
    }
    public boolean isExit() {
        return exit.get();
    }

    @Override
    public void close() throws Exception {
        if(this.serviceStatusChannel!=null){
            this.serviceStatusChannel.close();
        }
        if(this.serviceStatusConnection!=null){
            this.serviceStatusConnection.close();
        }
        if(this.calculateRequestChannel!=null){
            this.calculateRequestChannel.close();
        }
        if(this.calculateRequestConnection!=null){
            this.calculateRequestConnection.close();
        }
        if(this.calculateResponseChannel!=null){
            this.calculateResponseChannel.close();
        }
        if(this.calculateResponseConnection!=null){
            this.calculateResponseConnection.close();
        }
    }

    public class CustomDeliverCallback implements DeliverCallback{

        private CalculateRequest newRequest;
        private Channel calculateRequestChannel;
        private IHandleNewCalculateRequest handleNewCalculateRequestCallback;
        private AMQPHandler amqpHandler;

        public CustomDeliverCallback(Channel calculateRequestChannel,
                                     IHandleNewCalculateRequest handleNewCalculateRequestCallback,
                                     AMQPHandler amqpHandler){
            this.calculateRequestChannel=calculateRequestChannel;
            this.handleNewCalculateRequestCallback=handleNewCalculateRequestCallback;
            this.amqpHandler=amqpHandler;
        }

        public CalculateRequest getNewRequest() {
            return newRequest;
        }

        @Override
        public void handle(String s, Delivery delivery) throws IOException {
            String message = new String(delivery.getBody(), "UTF-8");
            CalculateRequest newRequest=new Gson().fromJson(message, CalculateRequest.class);
            try {
                this.handleNewCalculateRequestCallback.Handle(newRequest,this.amqpHandler);
            } catch (Exception e) {
                e.printStackTrace();
            }
            //this.calculateRequestChannel.basicCancel(s);
        }
    }
}
