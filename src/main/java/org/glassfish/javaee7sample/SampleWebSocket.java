/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package org.glassfish.javaee7sample;

import java.io.IOException;
import java.util.Collections;
import java.util.HashSet;
import java.util.Set;
import java.util.logging.Level;
import java.util.logging.Logger;
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
    private static Set<Session> sessions = Collections.synchronizedSet(new HashSet<Session>());

    @OnOpen
    public void onOpen(@NotNull Session session) {
        try {
            sessions.add(session);
            session.getBasicRemote().sendText("session opened.");
        } catch (IOException ex) {
            Logger.getLogger(SampleWebSocket.class.getName()).log(Level.SEVERE, null, ex);
        }
    }

    @OnMessage
    public void onMessage(final @NotNull String message, final @NotNull Session client) {
        for (Session s : sessions) {
            try {
                s.getBasicRemote().sendText(client.getPathParameters().get("nickname") + "> " + message);
            } catch (IOException ex) {
                Logger.getLogger(SampleWebSocket.class.getName()).log(Level.SEVERE, null, ex);
            }
        }
    }

    @OnClose
    public void onClose(final Session session) {
        try {
            sessions.remove(session);
            session.getBasicRemote().sendText("session closed.");
        } catch (IOException ex) {
            Logger.getLogger(SampleWebSocket.class.getName()).log(Level.SEVERE, null, ex);
        }
    }
}
