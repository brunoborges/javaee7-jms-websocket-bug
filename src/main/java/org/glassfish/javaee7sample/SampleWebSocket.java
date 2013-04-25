/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package org.glassfish.javaee7sample;

import java.util.HashSet;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.annotation.Resource;
import javax.inject.Inject;
import javax.jms.JMSContext;
import javax.jms.Queue;
import javax.validation.constraints.NotNull;
import javax.websocket.OnClose;
import javax.websocket.OnMessage;
import javax.websocket.OnOpen;
import javax.websocket.Session;
import javax.websocket.server.ServerEndpoint;

/**
 *
 * @author bruno
 */
@ServerEndpoint("/websocket/{nickname}")
public class SampleWebSocket {

//    @Resource(mappedName = "jms/myQueue")
//    private Queue myQueue;
//    @Inject
//    private JMSContext jmsContext;
    private static HashSet<Session> sessions = new HashSet<>();

    @OnOpen
    public void onOpen(@NotNull Session session) {
        System.out.println("onOpen");
        String nickname = session.getPathParameters().get("nickname");
        Logger.getLogger(SampleWebSocket.class.getName()).log(Level.INFO, "WebSocket session opened for '{0}'", nickname);
        sessions.add(session);

//        System.out.println("queue exists? " + (myQueue != null));
    }

    @OnMessage
    public void onMessage(@NotNull String message, @NotNull Session client) {
        System.out.println("onMessage");
        Logger.getLogger(SampleWebSocket.class.getName()).log(Level.INFO, "WebSocket session onMessage()");
        Logger.getLogger(SampleWebSocket.class.getName()).log(Level.INFO, "WebSocket message received: {0}", message);
        for (Session s : sessions) {
            s.getAsyncRemote().sendText(client.getPathParameters().get("nickname") + "> " + message);
        }
    }

    @OnClose
    public void onClose(final Session session) {
        System.out.println("onClose");
        Logger.getLogger(SampleWebSocket.class.getName()).log(Level.INFO, "WebSocket session close()");
        sessions.remove(session);
    }
}
