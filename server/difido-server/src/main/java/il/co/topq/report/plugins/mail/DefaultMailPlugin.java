package il.co.topq.report.plugins.mail;

import java.io.StringWriter;

import org.apache.commons.lang3.StringUtils;
import org.apache.velocity.Template;
import org.apache.velocity.VelocityContext;
import org.apache.velocity.app.VelocityEngine;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import il.co.topq.report.Common;
import il.co.topq.report.Configuration;
import il.co.topq.report.Configuration.ConfigProps;
import il.co.topq.report.business.execution.ExecutionMetadata;
import il.co.topq.report.plugins.ExecutionPlugin;

public class DefaultMailPlugin implements ExecutionPlugin {

	private final Logger log = LoggerFactory.getLogger(DefaultMailPlugin.class);

	private static final String DEFAULT_SUBJECT = "Test execution ended";

	private MailSender sender;

	private boolean enabled = true;

	private ExecutionMetadata metadata;

	public DefaultMailPlugin() {
		setEnabled(Configuration.INSTANCE.readBoolean(ConfigProps.ENABLE_MAIL));
	}

	@Override
	public String getName() {
		return "defaultMailPlugin";
	}

	@Override
	public void onExecutionEnded(ExecutionMetadata metadata) {
		if (!isEnabled()) {
			return;
		}
		if (null == metadata) {
			log.error("Can't find meta data for ended execution. Will not send mail");
			return;
		}

		this.metadata = metadata;
		sendMail();
	}

	protected void sendMail() {
		if (!isEnabled()) {
			return;
		}
		configureMailSender();
		if (null == sender) {
			// We already logged an appropriate log message in the
			// configureMailServer method, so there is no need in adding another
			// log message here.
			return;
		}

		final String subject = getMailSubject();
		final String body = getMailBody();
		
		if (!isEnabled()) {
			log.warn("The mail sender was disabled during the preperation of the mail body or header");
			// During the process of preparing the mail subject or the mail
			// body, bad things can happen and the plugin writer can decide
			// that he does not want to send mails. So we give him another
			// chance to disable the mail sender.
			return;
		}

		new Thread() {
			public void run() {
				try {
					sender.sendMail(subject, body);
				} catch (Exception e) {
					log.error("Failed sending mail", e);
				}
			}
		}.start();
	}

	protected String getMailBody() {
		return populateTemplate();
	}

	protected String getMailSubject() {
		String subject = StringUtils.isEmpty(Configuration.INSTANCE.readString(ConfigProps.MAIL_SUBJECT))
				? DEFAULT_SUBJECT : Configuration.INSTANCE.readString(ConfigProps.MAIL_SUBJECT);
		if (getMetadata().getNumOfFailedTests() > 0) {
			subject += " - Ended with " + getMetadata().getNumOfFailedTests() + " failures out of "
					+ getMetadata().getNumOfTests();
		} else if (getMetadata().getNumOfTestsWithWarnings() > 0) {
			subject += " - Ended with " + getMetadata().getNumOfTestsWithWarnings() + " warnings out of "
					+ getMetadata().getNumOfTests();
		} else {
			subject += " - Ended with " + getMetadata().getNumOfTests() + " successful tests";
		}
		return subject;
	}

	private String populateTemplate() {
		VelocityEngine ve = new VelocityEngine();
		ve.init();
		Template t = ve.getTemplate(getTemplateName());
		/* create a context and add data */
		VelocityContext context = new VelocityContext();
		context.put("meta", getMetadata());
		final StringWriter writer = new StringWriter();
		t.merge(context, writer);
		return writer.toString();
	}

	protected String getTemplateName() {
		return Common.CONFIUGRATION_FOLDER_NAME + "/mail.vm";
	}

	protected void configureMailSender() {
		if (!isEnabled() || null != sender) {
			return;
		}
		sender = new MailSender();
		sender.setMailMessageAsHtmlText(true);

		final String host = Configuration.INSTANCE.readString(ConfigProps.MAIL_SMTP_HOST);
		if (StringUtils.isEmpty(host)) {
			log.warn("SMTP host is not configured. Can't send mail");
			setEnabled(false);
			return;
		}
		sender.setSmtpHostName(host);

		final int port = Configuration.INSTANCE.readInt(ConfigProps.MAIL_SMTP_PORT);
		if (port == 0) {
			log.warn("SMTP port is not configured. Can't send mail");
			setEnabled(false);
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
			setEnabled(false);
			return;
		}
		sender.setFromAddress(from);

		final String to = getToAddress();
		if (StringUtils.isEmpty(to)) {
			log.warn("Mail to address is not configured. Can't send mail");
			setEnabled(false);
			return;
		}
		sender.setSendTo(to.split(";"));

		final String cc = getCcAddresses();
		if (!StringUtils.isEmpty(cc)) {
			sender.setSendCc(cc.split(";"));
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

	protected boolean isEnabled() {
		return enabled;
	}

	protected void setEnabled(boolean enabled) {
		this.enabled = enabled;
	}

	protected ExecutionMetadata getMetadata() {
		return metadata;
	}

	@Override
	public void execute(String params) {
		// TODO Auto-generated method stub
		
	}

}
