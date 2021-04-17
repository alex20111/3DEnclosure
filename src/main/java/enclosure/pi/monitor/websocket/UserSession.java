package enclosure.pi.monitor.websocket;

import java.io.IOException;
import javax.websocket.Session;


public class UserSession {

//	private static final Logger logger = LogManager.getLogger(UserSession.class);

	private Session session;
	
	private SessionType type;
	private boolean serialConsoleMaster = false;


	public UserSession() {}
	public UserSession(Session session) {
		this.session = session;
	}
	
	public UserSession(Session session, boolean serialConsoleMaster) {
		this.session = session;
		this.serialConsoleMaster = serialConsoleMaster;
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
	public boolean isSerialConsoleMaster() {
		return serialConsoleMaster;
	}
	public void setSerialConsoleMaster(boolean serialConsoleMaster) {
		this.serialConsoleMaster = serialConsoleMaster;
	}

	public SessionType getType() {
		return type;
	}
	public void setType(SessionType type) {
		this.type = type;
	}

	enum SessionType{
		DASHBOARD, TERMINAL;
	}

}