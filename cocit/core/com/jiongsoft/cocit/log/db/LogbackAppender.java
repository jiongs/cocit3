package com.jiongsoft.cocit.log.db;

import java.sql.SQLException;
import java.util.Date;

import javax.servlet.http.HttpServletRequest;

import ch.qos.logback.classic.spi.ILoggingEvent;
import ch.qos.logback.classic.spi.IThrowableProxy;
import ch.qos.logback.classic.spi.StackTraceElementProxy;
import ch.qos.logback.classic.spi.ThrowableProxyUtil;
import ch.qos.logback.core.AppenderBase;
import ch.qos.logback.core.CoreConstants;

import com.jiongsoft.cocit.lang.Str;
import com.kmjsoft.cocit.Demsy;
import com.kmjsoft.cocit.Demsy.Monitor;
import com.kmjsoft.cocit.entity.impl.log.RunningLog;

public class LogbackAppender extends AppenderBase<ILoggingEvent> {
	private final static String NA = "?";

	protected void execute(RunningLog log) throws SQLException {
		if (Demsy.bizSession != null)
			Demsy.bizSession.asynSave(log);
	}

	@Override
	protected void append(ILoggingEvent event) {
		try {
			RunningLog log = new RunningLog();

			log.setMessage(event.getFormattedMessage());
			Demsy me = Demsy.me();
			if (me != null) {
				log.setLoginuser(me.username());
				HttpServletRequest req = me.request();
				if (req != null) {
					log.setRemoteIp(req.getRemoteAddr());
					log.setRemoteAddress(Str.ipToName(log.getRemoteIp()));
					log.setRemoteUri(req.getRequestURI());
					log.setRemoteUrl(req.getRequestURL().toString());
				}
			}
			Monitor m = Demsy.monitor();
			log.setEslipse(m.getElepse());
			log.setMemEslipse(m.getMemElepse());
			log.setMonitor(m.toString());

			StringBuilder bud = new StringBuilder();
			IThrowableProxy tp = event.getThrowableProxy();
			if (tp != null) {
				ThrowableProxyUtil.printFirstLine(bud, tp);
				int commonFrames = tp.getCommonFrames();
				StackTraceElementProxy[] stepArray = tp.getStackTraceElementProxyArray();
				for (int i = 0; i < stepArray.length - commonFrames; i++) {
					bud.append(CoreConstants.TAB);
					ThrowableProxyUtil.printSTEP(bud, stepArray[i]);
				}
				log.setStackTrace(bud.toString());
			}
			log.setLoggername(event.getLoggerName());
			log.setThreadname(event.getThreadName());
			log.setLevel(event.getLevel().toString());
			log.setLogtime(new Date(event.getTimeStamp()));

			StackTraceElement callerData = event.getCallerData()[0];
			String fileName = callerData.getFileName();
			String className = callerData.getClassName();
			String methodName = callerData.getMethodName();
			String lineNumber = Integer.toString(callerData.getLineNumber());
			//
			StringBuffer buf = new StringBuffer();
			appendFragment(buf, className);
			buf.append(".");
			appendFragment(buf, methodName);
			buf.append("(");
			appendFragment(buf, fileName);
			buf.append(":");
			appendFragment(buf, lineNumber);
			buf.append(")");
			String fullInfo = buf.toString();
			log.setLocationinfo(fullInfo);

			execute(log);
		} catch (Throwable e) {
			// System.err.println("保存运行日志到数据库失败! 错误信息： " + e);
		}
	}

	private static final void appendFragment(final StringBuffer buf, final String fragment) {
		if (fragment == null) {
			buf.append(NA);
		} else {
			buf.append(fragment);
		}
	}

}
