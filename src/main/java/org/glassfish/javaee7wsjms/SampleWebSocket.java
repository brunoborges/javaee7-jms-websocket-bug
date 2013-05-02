/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package org.glassfish.javaee7wsjms;

import java.io.IOException;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.annotation.Resource;
import javax.inject.Inject;
import javax.jms.JMSConsumer;
import javax.jms.JMSContext;
import javax.jms.JMSException;
import javax.jms.Message;
import javax.jms.MessageListener;
import javax.jms.Queue;
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
public class SampleWebSocket {

    @Resource(mappedName = "jms/myQueue")
    private Queue myQueue;
    @Inject
    private JMSContext jmsContext;

    @OnOpen
    public void onOpen(final Session session) {
        try {
            session.getBasicRemote().sendText("session opened");

            // create a consumer for this session
            session.getBasicRemote().sendText("going to create a consumer on top of JMSContext and destination myQueue");

            JMSConsumer consumer = jmsContext.createConsumer(myQueue);
            consumer.setMessageListener(new WebSocketSessionJMSListener(session));

            session.getUserProperties().put("consumer", consumer);

            // store this consumer on this session
            session.getBasicRemote().sendText("consumer created and started");
        } catch (Exception ex) {
            Logger.getLogger(SampleWebSocket.class.getName()).log(Level.SEVERE, null, ex);
            ex.printStackTrace();
        }

    }

    @OnMessage
    public void onMessage(final String message, final Session client) {
        try {
            jmsContext.createProducer().send(myQueue, message);
        } catch (Exception ex) {
            Logger.getLogger(SampleWebSocket.class.getName()).log(Level.SEVERE, null, ex);
            ex.printStackTrace();
        }
    }

    @OnClose
    public void onClose(final Session session) {
        try {
            ((JMSConsumer) session.getUserProperties().get("consumer")).close();
            session.getBasicRemote().sendText("WebSocket Session and JMS Consumer closed");
        } catch (Exception ex) {
            Logger.getLogger(SampleWebSocket.class.getName()).log(Level.SEVERE, null, ex);
            ex.printStackTrace();
        }
    }

    private static class WebSocketSessionJMSListener implements MessageListener {

        private Session webSocketSession;

        public WebSocketSessionJMSListener() {
        }

        public WebSocketSessionJMSListener(Session wsSession) {
            this.webSocketSession = wsSession;
        }

        @Override
        public void onMessage(Message msg) {
            try {
                Logger.getLogger(WebSocketSessionJMSListener.class.getName()).log(Level.INFO, "Message received [id={0}] [payload={1}]", new Object[]{msg.getJMSMessageID(), msg.getBody(String.class)});
                webSocketSession.getBasicRemote().sendText("[Echoing from JMS]: " + msg.getBody(String.class));
            } catch (Exception ex) {
                Logger.getLogger(WebSocketSessionJMSListener.class.getName()).log(Level.SEVERE, null, ex);
                ex.printStackTrace();
            }
        }
    }
}
