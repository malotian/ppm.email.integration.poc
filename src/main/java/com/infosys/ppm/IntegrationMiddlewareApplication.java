package com.infosys.ppm;

import java.text.MessageFormat;
import java.util.Arrays;
import java.util.Properties;

import javax.mail.Folder;
import javax.mail.Message;
import javax.mail.MessagingException;
import javax.mail.PasswordAuthentication;
import javax.mail.Session;
import javax.mail.Store;
import javax.mail.internet.InternetAddress;
import javax.mail.internet.MimeMessage;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.http.ResponseEntity;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.web.client.RestTemplate;

import com.infosys.ppm.timesheet.Timesheet;

@EnableScheduling
@SpringBootApplication
public class IntegrationMiddlewareApplication {

	@Value("${pending.timesheet.ws.url}")
	private String pendingTimesheetWSUrl;

	@Value("${approve.timesheet.ws.url}")
	private String approveTimesheetWSUrl;

	@Value("${timesheet.approver.email}")
	private String timesheetApproverEmail;

	@Value("${mail.smtp.host}")
	private String smtpHost;

	@Value("${mail.smtp.port}")
	private int smtpPort;

	@Value("${mail.smtp.ssl.enable}")
	private boolean smtpSslEnabled;

	@Value("${mail.smtp.auth}")
	private boolean smtpAuthEnabled;

	@Value("${timesheet.service.account.email}")
	private String timesheetServiceAccountEmail;

	@Value("${timesheet.service.account.password}")
	private String timesheetServiceAccountPassword;

	private static final Logger LOGGER = LoggerFactory.getLogger(IntegrationMiddlewareApplication.class);

	public static void main(String[] args) {
		SpringApplication.run(IntegrationMiddlewareApplication.class, args);
	}

	public Session getSmtpSession() {
		return smtpSession;
	}

	public void setSmtpSession(Session smtpSession) {
		this.smtpSession = smtpSession;
	}

	public Folder getInbox() {
		return inbox;
	}

	public void setInbox(Folder inbox) {
		this.inbox = inbox;
	}

	Session smtpSession = null;
	Folder inbox = null;

	void ensureEmailInitilized() {

		if (null != smtpSession && null != inbox)
			return;

		Properties mailSettings = System.getProperties();
		mailSettings.put("mail.smtp.host", smtpHost);
		mailSettings.put("mail.smtp.port", smtpPort);
		mailSettings.put("mail.smtp.ssl.enable", smtpSslEnabled);
		mailSettings.put("mail.smtp.auth", smtpAuthEnabled);
		mailSettings.put("mail.smtp.socketFactory.port", 465);
		mailSettings.put("mail.smtp.socketFactory.class", "javax.net.ssl.SSLSocketFactory");

		smtpSession = Session.getDefaultInstance(mailSettings, new javax.mail.Authenticator() {
			protected PasswordAuthentication getPasswordAuthentication() {
				return new PasswordAuthentication(timesheetServiceAccountEmail, timesheetServiceAccountPassword);
			}
		});

		Store imaps;
		try {
			imaps = smtpSession.getStore("imaps");
			imaps.connect(smtpHost, timesheetServiceAccountEmail, timesheetServiceAccountPassword);
			inbox = imaps.getFolder("inbox");
			inbox.open(Folder.READ_ONLY);
		} catch (Exception e) {
			LOGGER.error("error, initializing email configuration", e);
		}
	}

	@Scheduled(fixedRate = 1000)
	void pollForTimesheetUpdates() {
		LOGGER.info("polling timesheets...");
		RestTemplate restTemplate = new RestTemplate();
		ResponseEntity<Timesheet[]> response = restTemplate.getForEntity(pendingTimesheetWSUrl, Timesheet[].class);
		Timesheet[] timesheets = response.getBody();

		ensureEmailInitilized();
		Arrays.asList(timesheets).forEach(t -> {
			LOGGER.info("sending email for approval request for: {}", t);
			getSmtpSession().setDebug(true);
			try {
				MimeMessage message = new MimeMessage(getSmtpSession());
				message.setFrom(new InternetAddress(timesheetServiceAccountEmail));
				message.addRecipient(Message.RecipientType.TO, new InternetAddress(timesheetApproverEmail));
				String emailMesage = MessageFormat.format("Approval Required, Timesheet: {0}, Name: {1}, Date: {2}, Hours: {3}", t.getId(), t.getName(), t.getDate(), t.getHours());
				message.setSubject(emailMesage);
				message.setText(emailMesage);
				// Transport.send(message);
				LOGGER.info("sending, {}", emailMesage);
			} catch (MessagingException e) {
				LOGGER.error("error, reading sending email", e);

				e.printStackTrace();
			}
		});
	}

	@Scheduled(fixedRate = 1500)
	void pollForTimesheetApprovals() {
		LOGGER.debug("polling...");
		ensureEmailInitilized();
		try {
			Folder inbox = getInbox();
			Message[] messages = inbox.getMessages(inbox.getMessageCount() - 10, inbox.getMessageCount());
			LOGGER.info("total emails read: {}", messages.length);
			Arrays.stream(messages).filter(m -> {
				try {
					LOGGER.info("reading, inbox-email: {}", m.getSubject());
					return null != m.getSubject() && m.getSubject().contains("Approval Required") && m.getSubject().toLowerCase().startsWith("approved");
				} catch (MessagingException e) {
					LOGGER.error("error, reading email", e);
					e.printStackTrace();
				}
				return false;
			}).forEach(m -> {
				try {
					LOGGER.info("processing, inbox-email: {}", m.getSubject());
					String[] tokens = m.getSubject().split(",");

					for (int i = 0; i < tokens.length; ++i)
						if (tokens[i].trim().startsWith("Timesheet: ")) {
							String url = MessageFormat.format(approveTimesheetWSUrl, tokens[1].split(" ")[1]);
							RestTemplate restTemplate = new RestTemplate();
							ResponseEntity<Boolean> response = restTemplate.getForEntity(url, Boolean.class);
							LOGGER.info("approval status: {}", response.getBody().booleanValue());
						}

				} catch (MessagingException e) {
					LOGGER.error("error, reading email", e);
					e.printStackTrace();
				}
			});
		} catch (Exception e) {
			LOGGER.error("error, reading reading email", e);
			e.printStackTrace();
		}
	}

}
