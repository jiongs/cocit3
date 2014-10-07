package com.jiongsoft.cocit.mvc.nutz;

import java.io.IOException;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import com.jiongsoft.cocit.Demsy;
import com.jiongsoft.cocit.lang.Ex;
import com.jiongsoft.cocit.security.SecurityException;

@SuppressWarnings("serial")
public class DemsyNutServlet extends HttpServlet {

	@Override
	protected void service(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
		Demsy me = null;
		try {
			me = Demsy.initMe(req, resp);

			if (!Demsy.actionHandler.execute(req, resp)) {
				resp.setStatus(404);
			}

		} catch (SecurityException e) {
			if (e.getCode() > 0) {
				resp.setStatus(e.getCode());
			} else {
				throw new ServletException(Ex.msg(e));
			}
		} finally {
			if (me != null) {
				me.release();
			}
		}
	}
}