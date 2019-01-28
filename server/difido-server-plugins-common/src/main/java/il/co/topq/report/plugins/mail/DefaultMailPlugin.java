package il.co.topq.report.plugins.mail;

import java.io.StringWriter;
import java.util.List;

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

	protected MailSender sender;

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
	public void execute(List<ExecutionMetadata> metaDataList, String params) {
		if (!isEnabled()) {
			return;
		}

		if (null == metaDataList || metaDataList.size() == 0) {
			log.error("No execution was defined. Will not send mail");
			return;
		}

		if (metaDataList.size() > 1) {
			log.warn(
					"Plugin support only a single execution. Will send mail with details of only the first execution in the list");
		}
		sendExecutionMail(metaDataList.get(0));
	}

	@Override
	public void onExecutionEnded(ExecutionMetadata metadata) {
		sendExecutionMail(metadata);
	}

	protected void sendExecutionMail(ExecutionMetadata metadata) {
		if (!isEnabled()) {
			return;
		}
		if (null == metadata) {
			log.error("Can't find meta data for ended execution. Will not send mail");
			return;
		}

		setMetadata(metadata);
		String body = getMailBody();
		String subject = getMailSubject();
		configureMailSender();
		sendMail(subject, body);
	}

	protected void sendMail(final String subject, final String body) {
		if (!isEnabled()) {
			return;
		}
		if (null == sender) {
			// We already logged an appropriate log message in the
			// configureMailServer method, so there is no need in adding another
			// log message here.
			return;
		}

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
		return populateTemplate(getMailBodyTemplateName());
	}

	protected String getMailSubject() {
		return populateTemplate(getMailSubjectTemplateName());
	}

	private String populateTemplate(String templateName) {
		VelocityEngine ve = new VelocityEngine();
		ve.init();
		Template t = ve.getTemplate(templateName);
		/* create a context and add data */
		VelocityContext context = new VelocityContext();
		String host = System.getProperty("server.address");
		if (null == host) {
			host = "localhost";
		}
		String port = System.getProperty("server.port");
		if (null == port) {
			port = "8080";
		}
		context.put("host", host);
		context.put("port", port);
		context.put("meta", getMetadata());
		final StringWriter writer = new StringWriter();
		t.merge(context, writer);
		return writer.toString();
	}

	protected String getMailBodyTemplateName() {
		return Common.CONFIUGRATION_FOLDER_NAME + "/mail_body.vm";
	}

	protected String getMailSubjectTemplateName() {
		return Common.CONFIUGRATION_FOLDER_NAME + "/mail_subject.vm";
	}

	protected void configureMailSender() {
		if (!isEnabled() || null != sender) {
			return;
		}
		sender = new MailSender();
		sender.setMailMessageAsHtmlText(true);

		final String host = Configuration.INSTANCE.readString(ConfigProps.MAIL_SMTP_HOST);
		if (isEmpty(host)) {
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
		if (!isEmpty(userName)) {
			sender.setUserName(userName);
		} else {
			log.warn("SMTP User name is not configured.");
		}

		final String password = Configuration.INSTANCE.readString(ConfigProps.MAIL_PASSWORD);
		if (!isEmpty(password)) {
			sender.setPassword(password);
		} else {
			log.warn("SMTP Password is not configured.");
		}

		final boolean ssl = Configuration.INSTANCE.readBoolean(ConfigProps.MAIL_SSL);
		sender.setSsl(ssl);

		final String from = getFromAddress();
		if (isEmpty(from)) {
			log.warn("Mail from address is not configured. Can't send mail");
			setEnabled(false);
			return;
		}
		sender.setFromAddress(from);

		final String to = getToAddress();
		if (isEmpty(to)) {
			log.warn("Mail to address is not configured. Can't send mail");
			setEnabled(false);
			return;
		}
		sender.setSendTo(to.split(";"));

		final String cc = getCcAddresses();
		if (!isEmpty(cc)) {
			sender.setSendCc(cc.split(";"));
		}

		final String[] attachments = getAttachments();
		if (null != attachments && attachments.length != 0) {
			sender.setAttachments(attachments);
		}
	}
	
	private static boolean isEmpty(String value) {
		return null == value || value.isEmpty();
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

	protected String[] getAttachments() {
		return new String[] {};
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

	protected void setMetadata(ExecutionMetadata metadata) {
		this.metadata = metadata;
	}

}
