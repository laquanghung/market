package market.interceptor;

import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.servlet.handler.HandlerInterceptorAdapter;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

/**
 * Перехватчик сеансовой корзины.
 * <p>
 * При отсутствии корзины в сессии создаёт новую корзину.
 */
public class FakeAuthenInterceptor extends HandlerInterceptorAdapter {

	@Override
	public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) throws Exception {
		Authentication auth = new UsernamePasswordAuthenticationToken("admin", "password");
		SecurityContextHolder.getContext().setAuthentication(auth);
		return super.preHandle(request, response, handler);
	}
}
