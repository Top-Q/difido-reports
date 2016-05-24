package il.co.topq.report.plugins.mail;

import java.io.StringWriter;

import org.apache.commons.lang3.StringUtils;
import org.apache.velocity.Template;
import org.apache.velocity.VelocityContext;
import org.apache.velocity.app.VelocityEngine;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import il.co.topq.report.Configuration;
import il.co.topq.report.Configuration.ConfigProps;
import il.co.topq.report.business.execution.ExecutionMetadata;
import il.co.topq.report.plugins.ExecutionPlugin;

public class DefaultMailPlugin implements ExecutionPlugin {

	private final Logger log = LoggerFactory.getLogger(DefaultMailPlugin.class);

	private static final String DEFAULT_SUBJECT = "Test automation execution was ended";

	private MailSender sender;

	private boolean enabled = true;

	public DefaultMailPlugin() {
		enabled = Configuration.INSTANCE.readBoolean(ConfigProps.ENABLE_MAIL);
	}

	@Override
	public String getName() {
		return "defaultMailPlugin";
	}

	@Override
	public void onExecutionEnded(ExecutionMetadata metadata) {
		if (!enabled) {
			return;
		}
		sendMail(metadata);
	}

	protected void sendMail(ExecutionMetadata metadata) {
		configureMailSender();
		if (null == sender) {
			// We already logged an appropriate log message in the
			// configureMailServer method, so there is no need in adding another
			// log message here.
			return;
		}
		final String subject = getMailSubject();
		if (null == metadata) {
			log.error("Can't find meta data for ended execution. Will not send mail");
			return;
		}

		final StringWriter writer = populateTemplate(metadata);
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

	protected String getMailSubject() {
		final String subject = StringUtils.isEmpty(Configuration.INSTANCE.readString(ConfigProps.MAIL_SUBJECT))
				? DEFAULT_SUBJECT : Configuration.INSTANCE.readString(ConfigProps.MAIL_SUBJECT);
		return subject;
	}

	private StringWriter populateTemplate(ExecutionMetadata metadata) {
		VelocityEngine ve = new VelocityEngine();
		ve.init();
		Template t = ve.getTemplate(getTemplateName());
		/* create a context and add data */
		VelocityContext context = new VelocityContext();
		context.put("meta", metadata);
		final StringWriter writer = new StringWriter();
		t.merge(context, writer);
		return writer;
	}

	protected String getTemplateName() {
		return "config/mail.vm";
	}

	protected void configureMailSender() {
		if (!enabled || null != sender) {
			return;
		}
		sender = new MailSender();
		sender.setMailMessageAsHtmlText(true);

		final String host = Configuration.INSTANCE.readString(ConfigProps.MAIL_SMTP_HOST);
		if (StringUtils.isEmpty(host)) {
			log.warn("SMTP host is not configured. Can't send mail");
			enabled = false;
			return;
		}
		sender.setSmtpHostName(host);

		final int port = Configuration.INSTANCE.readInt(ConfigProps.MAIL_SMTP_PORT);
		if (port == 0) {
			log.warn("SMTP port is not configured. Can't send mail");
			enabled = false;
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

		final String from = getFromAddress();
		if (StringUtils.isEmpty(from)) {
			log.warn("Mail from address is not configured. Can't send mail");
			enabled = false;
			return;
		}
		sender.setFromAddress(from);

		final String to = getToAddress();
		if (StringUtils.isEmpty(to)) {
			log.warn("Mail to address is not configured. Can't send mail");
			enabled = false;
			return;
		}
		sender.setSendTo(to);

		final String cc = getCcAddresses();
		if (!StringUtils.isEmpty(cc)) {
			sender.setSendCc(cc.split(","));
		}
	}

	protected String getCcAddresses() {
		return Configuration.INSTANCE.readString(ConfigProps.MAIL_CC_ADDRESS);
	}

	protected String getToAddress() {
		return Configuration.INSTANCE.readString(ConfigProps.MAIL_TO_ADDRESS);
	}

	protected String getFromAddress() {
		return Configuration.INSTANCE.readString(ConfigProps.MAIL_FROM_ADDRESS);
	}

}
