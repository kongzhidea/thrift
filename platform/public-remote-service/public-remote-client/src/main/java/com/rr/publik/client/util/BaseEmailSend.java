package com.rr.publik.client.util;

import java.util.ArrayList;
import java.util.List;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.commons.mail.DefaultAuthenticator;
import org.apache.commons.mail.EmailException;
import org.apache.commons.mail.SimpleEmail;

/**
 * 
 * http://commons.apache.org/email/index.html
 * 
 * 简单文本邮件发送
 * 
 * 注意：email.setHostName("smtp.163.com"); email.setAuthenticator(new
 * DefaultAuthenticator("yuaio@163.com", "yuaio"));
 * 
 * 还有email.setTLS(true); email.setSSL(false);
 * 这些都应该是对应的，使用不同的服务商邮箱，这里的HostName需要改一下，同时安全校验也是不同的，据我测试：只有google
 * gmail邮箱这两个校验都需要（google邮箱是我的最爱，好用，快速，最最重要的是安全，给你足够的隐私权。）设email.setTLS(true);
 * email.setSSL(true);
 * 
 * 163：两个都不需要校验就能通过；
 * 
 * sina：两个都不需要校验就能通过；
 * 
 * qq邮箱：需要需要校验tls；
 * 
 * email.setDebug(true); 开启debug模式，可以打印一些信息。
 * 
 * @author Administrator
 * 
 */
public class BaseEmailSend {
	private static final Log logger = LogFactory.getLog(BaseEmailSend.class);

	private static List<String> string2List(String adr) {
		List<String> adrs = new ArrayList<String>();
		String[] tmps = adr.split(",");
		for (String host : tmps) {
			adrs.add(host);
		}
		return adrs;
	}

	public static void send(String tos, String title, String body) {
		send(string2List(tos), title, body);
	}

	public static void send(List<String> tos, String title, String body) {
		SimpleEmail email = new SimpleEmail();
		// email.setTLS(true); //是否TLS校验，，某些邮箱需要TLS安全校验，同理有SSL校验
		email.setDebug(true);
		// email.setSSL(true);
		email.setHostName(Config.host);
		email.setAuthenticator(new DefaultAuthenticator(Config.account,
				Config.password));
		try {
			email.setFrom(Config.account); // 发送方,这里可以写多个
			for (String to : tos) {
				email.addTo(to);
			}
			email.setCharset("utf-8");
			email.setSubject(title); // 标题
			email.setMsg(body);
			email.send();
		} catch (Exception e) {
			logger.error(e.getMessage(), e);
		}
	}

	public static void main(String[] args) {
		send("563150913@qq.com,zhihui.kong@renren-inc.com", "服务挂了", "89挂了");
	}

}

class Config {
	public static final String host = "smtp.163.com";
	public static final String account = "bloghotwordrr@163.com";
	public static final String password = "bloghotword";
}
