package enclosure.pi.monitor.thread;

import javax.mail.MessagingException;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import enclosure.pi.monitor.common.Constants;
import enclosure.pi.monitor.common.SharedData;
import enclosure.pi.monitor.db.entity.Config;
import home.email.EmailMessage;
import home.email.EmailType;
import home.email.GoogleMail;

public class SendSMSThread implements Runnable{
	private static final Logger logger = LogManager.getLogger(SendSMSThread.class);

	private String message = "";
	private String subject = "";

	public SendSMSThread(String subject, String message) {
		this.message = message;
		this.subject = subject;
	}

	@Override
	public void run() {

		Config cfg = (Config)SharedData.getInstance().getSharedObject(Constants.CONFIG);


		if (cfg.getSmsPhoneNumber().length() > 0) {
			logger.info("Sending sms text to " + cfg.getSmsPhoneNumber() + " \nof: " + subject + "\nBody: " + message );

			EmailMessage em = new EmailMessage();
			em.setSubject(subject);
			em.setMessageBody(message);
			em.setTo(cfg.getSmsPhoneNumber() + "@pcs.rogers.com");
			em.setFrom("3dEnclosure");

			GoogleMail g = new GoogleMail();
			try {
				g.SendTLSSecure("alex.mailservice1@gmail.com", "90opklm,)", em, EmailType.text);
			} catch (MessagingException e) {
				logger.error("Error sending SMS message" , e);
			}
		}else {
			logger.info("No SMS number defined.. Not sendind a message");
		}


	}

}
