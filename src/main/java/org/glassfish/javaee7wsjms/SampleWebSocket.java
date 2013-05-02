/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package org.glassfish.javaee7wsjms;

import java.util.Collections;
import java.util.HashSet;
import java.util.Set;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.annotation.Resource;
import javax.enterprise.event.Observes;
import javax.inject.Inject;
import javax.inject.Named;
import javax.jms.JMSContext;
import javax.jms.Message;
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
@Named
@ServerEndpoint("/websocket")
public class SampleWebSocket {

    @Resource(mappedName = "jms/myQueue")
    private Queue myQueue;
    private JMSContext jmsContext;
    private static final Set<Session> sessions = Collections.synchronizedSet(new HashSet<Session>());

    @Inject
    public SampleWebSocket(JMSContext jmsc) {
        this.jmsContext = jmsc;
    }

    @OnOpen
    public void onOpen(final Session session) {
        try {
            session.getBasicRemote().sendText("session opened");
            sessions.add(session);

            if (jmsContext == null) {
                Logger.getLogger(SampleWebSocket.class.getName()).log(Level.SEVERE, "JMSContext is null");
            } else if (myQueue == null) {
                Logger.getLogger(SampleWebSocket.class.getName()).log(Level.SEVERE, "Queue is null");
            }
        } catch (Exception ex) {
            Logger.getLogger(SampleWebSocket.class.getName()).log(Level.SEVERE, null, ex);
            ex.printStackTrace();
        }
    }

    @OnMessage
    public void onMessage(final String message, final Session client) {
        try {
            if (jmsContext != null && myQueue != null) {
                jmsContext.createProducer().send(myQueue, message);
            }
        } catch (Exception ex) {
            Logger.getLogger(SampleWebSocket.class.getName()).log(Level.SEVERE, null, ex);
            ex.printStackTrace();
        }
    }

    @OnClose
    public void onClose(final Session session) {
        try {
            session.getBasicRemote().sendText("WebSocket Session closed");
            sessions.remove(session);
        } catch (Exception ex) {
            Logger.getLogger(SampleWebSocket.class.getName()).log(Level.SEVERE, null, ex);
            ex.printStackTrace();
        }
    }

    public void onJMSMessage(@Observes @WebSocketJMSMessage Message msg) {
        Logger.getLogger(SampleWebSocket.class.getName()).log(Level.INFO, "Got JMS Message at WebSocket!");
    }
}
