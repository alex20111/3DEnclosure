package enclosure.pi.monitor.websocket;

import java.io.IOException;
import javax.websocket.Session;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;


public class UserSession {

//	private static final Logger logger = LogManager.getLogger(UserSession.class);

	private Session session;
//	private String userName = "" ;


	public UserSession() {}
	public UserSession(Session session) {
		this.session = session;
	}


	public void SendData(String data) throws IOException {
		session.getBasicRemote().sendText(data);
	}
	public Session getSession() {
		return session;
	}
	public void setSession(Session session) {
		this.session = session;
	}

//	public String getUserName() {
//		return userName;
//	}
//	public void setUserName(String userName) {
//		this.userName = userName;
//	}
}