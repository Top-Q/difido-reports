package il.co.topq.report.business.mail;

import java.io.StringWriter;

import org.apache.commons.lang3.StringUtils;
import org.apache.velocity.Template;
import org.apache.velocity.VelocityContext;
import org.apache.velocity.app.VelocityEngine;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Component;

import il.co.topq.report.Configuration;
import il.co.topq.report.Configuration.ConfigProps;
import il.co.topq.report.business.execution.ExecutionMetadata;
import il.co.topq.report.events.ExecutionEndedEvent;

@Component
public class MailController {

	private static final String DEFAULT_SUBJECT = "Test automation execution was ended";

	private final Logger log = LoggerFactory.getLogger(MailController.class);

	private MailSender sender;

	private boolean enable = true;
	
	private boolean enabled;
	
	public MailController() {
		enabled = Configuration.INSTANCE.readBoolean(ConfigProps.ENABLE_MAIL);
	}

	@EventListener
	public void onExecutionEndedEvent(ExecutionEndedEvent executionEndedEvent) {
		if (!enabled){
			return;
		}
		configureMailSender();
		if (null == sender) {
			// We already logged an appropriate log message in the
			// configureMailServer method, so there is no need in adding another
			// log message here.
			return;
		}
		final String subject = StringUtils.isEmpty(Configuration.INSTANCE.readString(ConfigProps.MAIL_SUBJECT)) ? DEFAULT_SUBJECT
				: Configuration.INSTANCE.readString(ConfigProps.MAIL_SUBJECT);
		final ExecutionMetadata metaData = executionEndedEvent.getMetadata();
		if (null == metaData) {
			log.error("Can't find meta data for ended execution with id " + executionEndedEvent.getExecutionId() + ". Will not send mail");
			return;
		}

		VelocityEngine ve = new VelocityEngine();
		ve.init();
		Template t = ve.getTemplate("config/mail.vm");
		/* create a context and add data */
		VelocityContext context = new VelocityContext();
		context.put("meta", metaData);
		final StringWriter writer = new StringWriter();
		t.merge(context, writer);
		new Thread() {
			public void run() {
				try {
					sender.sendMail(subject, writer.toString());
				} catch (Exception e) {
					log.error("Failed sending mail", e);
				}
			}
		}.start();

	}

	private void configureMailSender() {
		if (!enable || null != sender) {
			return;
		}
		sender = new MailSender();
		sender.setMailMessageAsHtmlText(true);

		final String host = Configuration.INSTANCE.readString(ConfigProps.MAIL_SMTP_HOST);
		if (StringUtils.isEmpty(host)) {
			log.warn("SMTP host is not configured. Can't send mail");
			enable = false;
			return;
		}
		sender.setSmtpHostName(host);

		final int port = Configuration.INSTANCE.readInt(ConfigProps.MAIL_SMTP_PORT);
		if (port == 0) {
			log.warn("SMTP port is not configured. Can't send mail");
			enable = false;
			return;
		}
		sender.setSmtpPort(port);

		final String userName = Configuration.INSTANCE.readString(ConfigProps.MAIL_USER_NAME);
		if (!StringUtils.isEmpty(userName)) {
			sender.setUserName(userName);
		} else {
			log.warn("SMTP User name is not configured.");
		}

		final String password = Configuration.INSTANCE.readString(ConfigProps.MAIL_PASSWORD);
		if (!StringUtils.isEmpty(password)) {
			sender.setPassword(password);
		} else {
			log.warn("SMTP User name is not configured.");
		}

		final boolean ssl = Configuration.INSTANCE.readBoolean(ConfigProps.MAIL_SSL);
		sender.setSsl(ssl);

		final String from = Configuration.INSTANCE.readString(ConfigProps.MAIL_FROM_ADDRESS);
		if (StringUtils.isEmpty(from)) {
			log.warn("Mail from address is not configured. Can't send mail");
			enable = false;
			return;
		}
		sender.setFromAddress(from);

		final String to = Configuration.INSTANCE.readString(ConfigProps.MAIL_TO_ADDRESS);
		if (StringUtils.isEmpty(to)) {
			log.warn("Mail to address is not configured. Can't send mail");
			enable = false;
			return;
		}
		sender.setSendTo(to);

		final String cc = Configuration.INSTANCE.readString(ConfigProps.MAIL_CC_ADDRESS);
		if (!StringUtils.isEmpty(cc)) {
			sender.setSendCc(cc.split(","));
		}

	}

}
