package com.kmjsoft.cocit.actionplugin.security;

import static com.kmjsoft.cocit.Demsy.security;

import com.jiongsoft.cocit.lang.DemsyException;
import com.kmjsoft.cocit.Demsy;
import com.kmjsoft.cocit.entity.EntityConst;
import com.kmjsoft.cocit.entity.actionplugin.ActionEvent;
import com.kmjsoft.cocit.entity.actionplugin.ActionPlugin;
import com.kmjsoft.cocit.entity.impl.security.User;
import com.kmjsoft.cocit.entityengine.service.SecurityManager;
import com.kmjsoft.cocit.orm.ExtOrm;
import com.kmjsoft.cocit.orm.expr.Expr;
import com.kmjsoft.cocit.util.StringUtil;

public class SaveUser extends ActionPlugin {

	@Override
	public void before(ActionEvent event) {
		synchronized (SaveUser.class) {

			/*
			 * 获取用户信息
			 */
			User user = (User) event.getEntity();
			ExtOrm orm = (ExtOrm) event.getOrm();

			boolean isAdministrator = false;
			try {
				security.checkLogin(SecurityManager.ROLE_ADMIN_USER);
				isAdministrator = true;
			} catch (Throwable e) {
			}
			// String cocit_verify_code = null;
			// ActionContext ctx = Cocit.getActionContext();

			/*
			 * 检查图片验证码：以避免因其他校验错误导致验证码失效，避免防止重复提交！但验证码必须在最后检查，这里只检查验证码是否存在？
			 */
			// if (user instanceof WebUser) {
			// if (!isAdministrator) {
			// cocit_verify_code = ctx.getRequest().getParameter("cocit_verify_code");
			// if (StringUtil.isNil(cocit_verify_code)) {
			// throw new DemsyException("必须输入图片验证码!");
			// }
			// }
			// }

			/*
			 * 检查登录帐号是否被占用：。
			 */
			String code = user.getUsername();
			User oldUser = (User) orm.load(user.getClass(), Expr.eq(EntityConst.F_TENANT_OWNER_GUID, Demsy.me().getTenant()).and(Expr.eq(EntityConst.F_CODE, code)));
			if (oldUser != null && !oldUser.getId().equals(user.getId())) {
				throw new DemsyException("登录帐号已被使用，请重新填写登录帐号!");
			}

			// if (user instanceof WebUser) {
			// WebUser webUser = (WebUser) user;
			//
			// /*
			// * 验证手机号码是否合法：手机号码不能为空且手机号码不能重复注册。
			// */
			// if (
			// // !StringUtil.isNil(webUser.getTel()) &&//
			// !StringUtil.isMobile(webUser.getTel())//
			// ) {
			// throw new DemsyException("非法手机号码！");
			// }
			// oldUser = (User) orm.load(user.getClass(), Expr.eq(EntityConst.F_SOFT_ID, Demsy.me().getSoft()).and(Expr.eq("tel", webUser.getTel())));
			// if (oldUser != null && !oldUser.getId().equals(user.getId())) {
			// throw new DemsyException("手机号码已被使用，请重新填写手机号码!");
			// }
			//
			// /*
			// * 检查QQ号码是否合法
			// */
			// if (//
			// // !StringUtil.isNil(webUser.getQq()) &&//
			// (!StringUtil.isNumber(webUser.getQq()) || webUser.getQq().trim().length() < 5)//
			// ) {
			// throw new DemsyException("非法QQ号码！");
			// }
			// oldUser = (User) orm.load(user.getClass(), Expr.eq(EntityConst.F_SOFT_ID, Demsy.me().getSoft()).and(Expr.eq("qq", webUser.getQq())));
			// if (oldUser != null && !oldUser.getId().equals(user.getId())) {
			// throw new DemsyException("QQ号码已被使用，请重新填写QQ号码!");
			// }
			// }

			/*
			 * 验证登录帐号：登录帐号只能由数字、字母、下划线组成，且只能以字母开头。
			 */
			char c = code.charAt(0);
			if (!(c >= 'a' && c <= 'z') && !(c >= 'A' && c <= 'Z')) {
				throw new DemsyException("登录帐号必须以字母开头！且只能由数字、字母、下划线组成！");
			}
			for (int i = code.length() - 1; i > 0; i--) {
				c = code.charAt(i);
				if (!(c >= '0' && c <= '9')// is not number
						&& !(c >= 'a' && c <= 'z')// is not lower case
						&& !(c >= 'A' && c <= 'Z')// is not lower case
						&& c != '_'//
				) {
					throw new DemsyException("登录帐号必须以字母开头！且只能由数字、字母、下划线组成！");
				}
			}

			if (!isAdministrator) {

				/*
				 * 验证密码：密码最小长度为8
				 */
				if (user.getId() == null || user.getId() == 0) {// 针对新增帐号
					StringUtil.validatePassword(user.getRawPassword());
				}

				/*
				 * 检查图片验证码：验证码必须在最后检查，以避免因其他校验错误导致验证码失效，避免防止重复提交！
				 */
				// if (user instanceof WebUser) {
				// HttpUtil.checkImgVerifyCode(ctx.getRequest(), cocit_verify_code, "验证码不正确！");
				// }
			}
		}
	}

	@Override
	public void after(ActionEvent event) {
	}

	@Override
	public void loaded(ActionEvent event) {
		// TODO Auto-generated method stub

	}

}
