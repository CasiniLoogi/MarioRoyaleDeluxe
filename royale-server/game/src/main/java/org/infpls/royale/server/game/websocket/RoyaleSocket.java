package org.infpls.royale.server.game.websocket;

import org.springframework.web.socket.*;
import org.springframework.web.socket.handler.TextWebSocketHandler;
import org.springframework.beans.factory.annotation.Autowired;

import org.infpls.royale.server.game.dao.DaoContainer;
import org.infpls.royale.server.game.session.RoyaleSession;
import org.infpls.royale.server.util.Oak;

import javax.servlet.http.HttpServletRequest;

public class RoyaleSocket extends TextWebSocketHandler {
  
    @Autowired
    private DaoContainer dao;
  
    @Override
    public void afterConnectionEstablished(WebSocketSession webSocket) {
      try {
        String remoteAddress = webSocket.getHandshakeHeaders().getFirst("X-Forwarded-For");
        System.out.println("Remote address: " + remoteAddress);

        RoyaleSession session = dao.getUserDao().createSession(webSocket, dao);
        session.start();
        webSocket.getAttributes().put("session", session);
      }
      catch(Exception ex) {
        Oak.log(Oak.Level.ERR, "Exception thrown at Websocket top level.", ex);
      }
    }

    @Override
    public void handleTextMessage(WebSocketSession webSocket, TextMessage data) {
      try {
        RoyaleSession session = (RoyaleSession)(webSocket.getAttributes().get("session"));
        session.handlePacket(data.getPayload());
      }
      catch(Exception ex) {
        Oak.log(Oak.Level.ERR, "Exception thrown at Websocket top level.", ex);
      }
    }
    
    @Override
    public void handleBinaryMessage(WebSocketSession webSocket, BinaryMessage data) {
      try {
        RoyaleSession session = (RoyaleSession)(webSocket.getAttributes().get("session"));
        session.handleBinary(data.getPayload());
      }
      catch(Exception ex) {
        Oak.log(Oak.Level.ERR, "Exception thrown at Websocket top level.", ex);
      }
    }
  
    @Override
    public void afterConnectionClosed(WebSocketSession webSocket, CloseStatus status) {
      try {
        dao.getLobbyDao().saveDatabase();
        dao.getUserDao().destroySession(webSocket);
      }
      catch(Exception ex) {
        Oak.log(Oak.Level.ERR, "Exception thrown at Websocket top level.", ex);
      }
    }
}