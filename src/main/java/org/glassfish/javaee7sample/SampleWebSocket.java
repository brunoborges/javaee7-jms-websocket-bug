/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package org.glassfish.javaee7sample;

import java.io.IOException;
import javax.jms.Queue;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.annotation.Resource;
import javax.inject.Inject;
import javax.jms.JMSContext;
import javax.jms.JMSException;
import javax.jms.Message;
import javax.jms.MessageListener;
import javax.websocket.OnClose;
import javax.websocket.OnMessage;
import javax.websocket.OnOpen;
import javax.websocket.Session;
import javax.websocket.server.ServerEndpoint;

/**
 *
 * @author bruno
 */
@ServerEndpoint("/websocket")
public class SampleWebSocket implements MessageListener {

    @Resource(name = "jms/myQueue")
    private Queue myQueue;
    @Inject
    private JMSContext jmsContext;
    private Session session;

    @OnOpen
    public void onOpen(final Session session) {
        this.session = session;
        Logger.getLogger(SampleWebSocket.class.getName()).log(Level.INFO, "WebSocket session opened");
        try {
            session.getBasicRemote().sendText("websocket session opened");
        } catch (IOException ex) {
            Logger.getLogger(SampleWebSocket.class.getName()).log(Level.SEVERE, null, ex);
        }

        // create a queue consumer
        jmsContext.createConsumer(myQueue).setMessageListener(this);
    }

    @OnMessage
    public void onMessage(String message, Session client) {
        try {
            Logger.getLogger(SampleWebSocket.class.getName()).log(Level.INFO, "WebSocket message received: {0}", message);
            client.getBasicRemote().sendText("echo: " + message);
            jmsContext.createProducer().send(myQueue, message);
        } catch (IOException ex) {
            Logger.getLogger(SampleWebSocket.class.getName()).log(Level.SEVERE, null, ex);
        }
    }

    @OnClose
    public void onClose() {
        try {
            this.session.close();
            this.session = null;
            Logger.getLogger(SampleWebSocket.class.getName()).log(Level.INFO, "WebSocket session closed");
        } catch (IOException ex) {
            Logger.getLogger(SampleWebSocket.class.getName()).log(Level.SEVERE, null, ex);
        }
    }

    @Override
    public void onMessage(Message msg) {
        try {
            Logger.getLogger(SampleWebSocket.class.getName()).log(Level.INFO, "JMS payload receiced: {0}", msg.getJMSMessageID());
        } catch (JMSException ex) {
            Logger.getLogger(SampleWebSocket.class.getName()).log(Level.SEVERE, null, ex);
        }
    }
}
