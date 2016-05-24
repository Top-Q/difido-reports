package il.co.topq.report.plugins.mail;

import java.io.File;
import java.security.Security;
import java.util.Properties;

import javax.activation.DataHandler;
import javax.activation.DataSource;
import javax.activation.FileDataSource;
import javax.mail.Message;
import javax.mail.Multipart;
import javax.mail.PasswordAuthentication;
import javax.mail.Session;
import javax.mail.Transport;
import javax.mail.internet.AddressException;
import javax.mail.internet.InternetAddress;
import javax.mail.internet.MimeBodyPart;
import javax.mail.internet.MimeMessage;
import javax.mail.internet.MimeMultipart;

import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * this class is used for sending\receiving mail<br>
 * <b>Usage:</b><br>
 * 
 * <b>For Sending:</b><br>
 * <UL>
 * 1) create a new object<br>
 * <br>
 * 2) set the following parameters:
 * <UL>
 * a) address to send from (user@domain.com)<br>
 * b) sender user name (user)<br>
 * c) sender password (password)<br>
 * d) mail host (for example: smtp.gmail.com for gmail)<br>
 * e) is host secured (ssl true)<br>
 * f) sending host port number (465 for gmail)<br>
 * g) mail to send to<br>
 * </UL>
 * 3) use <I>sendMail(Title,Message)</I> to send a message with a given title
 * </UL> <br>
 * <br>
 *
 */
public class MailSender {

	private final Logger log = LoggerFactory.getLogger(MailSender.class);

	/**
	 * secured host port number
	 */
	public final static int SSL_PORT = 465;

	/**
	 * unsecured host port number
	 */
	public final static int NOT_SSL_PORT = 25;

	/**
	 * Socket factory const
	 */
	protected final static String SSL_FACTORY = "javax.net.ssl.SSLSocketFactory";

	/**
	 * the sending account host
	 */
	protected String smtpHostName;

	/**
	 * the port the smtp host works on
	 */
	protected int smtpPort = -1;

	/**
	 * is the connection secured?
	 */
	protected boolean ssl = false;

	/**
	 * the address to send from
	 */
	protected String fromAddress = null;

	/**
	 * the addresses to send mail to
	 */
	private String[] sendTo = null;

	/**
	 * the addresses to Carbon copy to
	 */
	private String[] sendCc = null;

	/**
	 * the addresses to Blind carbon copy to
	 */
	private String[] sendBcc = null;

	/**
	 * if True will print debug info
	 */
	protected boolean isDebug = true;

	/**
	 * the account to login from user name
	 */
	protected String userName = null;

	/**
	 * the password of the account sending from
	 */
	protected String password = null;

	protected String popHost;

	protected int popPort;

	protected String[] attachments;

	protected boolean mailMessageAsHtmlText = false;


	/**
	 * used after configuring  all parameters<br>
	 * sends a message with a given title
	 * 
	 * @param title
	 *            the mail title
	 * @param msgContent
	 *            the msg content
	 * @throws Exception
	 */
	@SuppressWarnings("restriction")
	public void sendMail(String title, String msgContent) throws Exception {

		Properties props = new Properties();

		if (ssl) {
			props.put("mail.smtp.starttls.enable", "true");
			props.put("mail.smtp.socketFactory.port", "" + smtpPort);
			props.put("mail.smtp.socketFactory.class", SSL_FACTORY);
			props.put("mail.smtp.socketFactory.fallback", "false");
		}

		Security.addProvider(new com.sun.net.ssl.internal.ssl.Provider());

		props.put("mail.smtp.host", smtpHostName);

		props.put("mail.debug", isDebug + "");
		props.put("mail.smtp.port", "" + smtpPort);

		Session session = null;

		if (password != null) {
			props.put("mail.smtp.auth", "true");
			session = Session.getInstance(props, new javax.mail.Authenticator() {

				protected PasswordAuthentication getPasswordAuthentication() {
					return new PasswordAuthentication(userName, password);
				}
			});
		} else {
			session = Session.getInstance(props, null);
		}

		Message msg = new MimeMessage(session);
		InternetAddress addressFrom = null;
		if (userName != null) {
			log.info("Sending mail from " + fromAddress + " with user " + userName);
			addressFrom = new InternetAddress(/* from */userName, fromAddress);
		} else {
			log.info("Sending mail from " + fromAddress);
			addressFrom = new InternetAddress(/* from */fromAddress);
		}
		msg.setFrom(addressFrom);

		msg.setRecipients(Message.RecipientType.TO, getAddresses(sendTo));
		msg.setRecipients(Message.RecipientType.CC, getAddresses(sendCc));
		msg.setRecipients(Message.RecipientType.BCC, getAddresses(sendBcc));

		// Setting the Subject and Content Type
		msg.setSubject(title);

		/********* add attachments **********/
		Multipart multipart = new MimeMultipart();
		// create the message part
		MimeBodyPart messageBodyPart = new MimeBodyPart();

		// fill message
		if (mailMessageAsHtmlText) {
			messageBodyPart.setContent(msgContent, "text/html");
		} else {
			messageBodyPart.setText(msgContent);
		}

		multipart.addBodyPart(messageBodyPart);
		if (attachments != null) {
			for (String file : attachments) {
				if (!StringUtils.isEmpty(file)) {
					log.debug("Attaching " + file);
					// Part two is attachment
					messageBodyPart = new MimeBodyPart();
					DataSource source = new FileDataSource(file);
					messageBodyPart.setDataHandler(new DataHandler(source));
					messageBodyPart.setFileName(new File(file).getName());
					multipart.addBodyPart(messageBodyPart);
				}
			}
		}
		// Put parts in message
		msg.setContent(multipart);
		/************* attachments end ******/
		Transport.send(msg);
	}

	private InternetAddress[] getAddresses(String[] addresses) throws AddressException {
		if (addresses == null) {
			return new InternetAddress[0];
		}

		InternetAddress[] internetAaddresses = new InternetAddress[addresses.length];
		for (int i = 0; i < addresses.length; i++) {
			internetAaddresses[i] = new InternetAddress(addresses[i]);
		}
		return internetAaddresses;
	}

	/**
	 * the password of the sending mail
	 * 
	 * @param password
	 */
	public void setPassword(String password) {
		this.password = password;
	}

	/**
	 * the array of addresses to send to
	 * 
	 * @param sendTo
	 */
	public void setSendTo(String... sendTo) {
		this.sendTo = sendTo;
	}

	/**
	 * the smtp host name (smtp.gmail.com for example)
	 * 
	 * @param smtpHostName
	 */
	public void setSmtpHostName(String smtpHostName) {
		this.smtpHostName = smtpHostName;
	}

	/**
	 * the sending user name
	 * 
	 * @param userName
	 */
	public void setUserName(String userName) {
		this.userName = userName;
	}

	/**
	 * if True then the connection is secured
	 * 
	 * @param ssl
	 */
	public void setSsl(boolean ssl) {
		this.ssl = ssl;
	}

	/**
	 * the smtp port to send from
	 * 
	 * @param smtpPort
	 */
	public void setSmtpPort(int smtpPort) {
		this.smtpPort = smtpPort;
	}

	/**
	 * an array of all File attachments to be attached to mail
	 * 
	 * @param attachments
	 */
	public void setAttachments(String... attachments) {
		this.attachments = attachments;
	}

	/**
	 * set mail message as html text - this option enable to format mail message
	 * with html code.
	 * 
	 * @param mailMessageAsHtmlText
	 */
	public void setMailMessageAsHtmlText(boolean mailMessageAsHtmlText) {
		this.mailMessageAsHtmlText = mailMessageAsHtmlText;
	}

	/**
	 * the array of addresses to send Carbon copy
	 * 
	 * @param sendCc
	 */
	public void setSendCc(String[] sendCc) {
		this.sendCc = sendCc;
	}

	/**
	 * the array of addresses to send Blind carbon copy
	 * 
	 * @param sendBcc
	 */
	public void setSendBcc(String[] sendBcc) {
		this.sendBcc = sendBcc;
	}

	public String getFromAddress() {
		return fromAddress;
	}

	public void setFromAddress(String fromAddress) {
		this.fromAddress = fromAddress;
	}

}