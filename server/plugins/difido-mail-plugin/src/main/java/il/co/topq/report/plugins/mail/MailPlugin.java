package il.co.topq.report.plugins.mail;

import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import il.co.topq.report.Configuration;
import il.co.topq.report.Configuration.ConfigProps;
import il.co.topq.report.business.execution.ExecutionMetadata;

public class MailPlugin extends DefaultMailPlugin {

	private static final String MAIL_SEND_TO = "mail.sendTo";

	private static final String MAIL_FROM = "mail.from";

	private static final String MAIL_SUBJECT = "mail.subject";

	private final Logger log = LoggerFactory.getLogger(MailPlugin.class);

	@Override
	public String getName() {
		return "MailPlugin";
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
		String clientSubject = metadata.getProperties().get(MAIL_SUBJECT);
		String subject = clientSubject != null ? clientSubject : getMailSubject();
		configureMailSender(metadata);
		sendMail(subject, body);
	}
	protected void configureMailSender(){
		configureMailSender(null);	
	}
	
	private void configureMailSender(ExecutionMetadata metadata) {
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

		String clientMailFrom = metadata.getProperties().get(MAIL_FROM);
		final String userName = clientMailFrom != null ? clientMailFrom : Configuration.INSTANCE.readString(ConfigProps.MAIL_USER_NAME);
		if (!StringUtils.isEmpty(userName)) {
			sender.setUserName(userName);
		} else {
			log.warn("SMTP User name is not configured.");
		}

		final String password = Configuration.INSTANCE.readString(ConfigProps.MAIL_PASSWORD);
		if (!StringUtils.isEmpty(password)) {
			sender.setPassword(password);
		} else {
			log.warn("SMTP Password is not configured.");
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

		String clientRecipient = metadata.getProperties().get(MAIL_SEND_TO);
		final String to = clientRecipient != null ? clientRecipient : getToAddress();
		if (StringUtils.isEmpty(to)) {
			log.warn("Mail to address is not configured. Can't send mail");
			setEnabled(false);
			return;
		}
		sender.setSendTo(to.split(","));

		final String cc = getCcAddresses();
		if (!StringUtils.isEmpty(cc)) {
			sender.setSendCc(cc.split(";"));
		}

		final String[] attachments = getAttachments();
		if (null != attachments && attachments.length != 0) {
			sender.setAttachments(attachments);
		}
	}
}
