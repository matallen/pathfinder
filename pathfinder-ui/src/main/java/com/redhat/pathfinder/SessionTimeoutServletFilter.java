package com.redhat.pathfinder;

import java.io.IOException;

import javax.servlet.FilterChain;
import javax.servlet.FilterConfig;
import javax.servlet.ServletContext;
import javax.servlet.ServletException;
import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

public class SessionTimeoutServletFilter implements javax.servlet.Filter{
	ServletContext ctx;
	
	@Override
	public void init(FilterConfig filterConfig) throws ServletException{
		ctx=filterConfig.getServletContext();
	}

	@Override
	public void doFilter(ServletRequest request, ServletResponse response, FilterChain chain) throws IOException, ServletException{
		HttpServletRequest req=(HttpServletRequest)request;
		HttpServletResponse resp=(HttpServletResponse)response;
		HttpSession session = req.getSession(false);// don't create if it doesn't exist
		
		String uri=((HttpServletRequest)request).getRequestURI();
		boolean sessionExists=session!=null && !session.isNew();
		boolean isLoginPage=((HttpServletRequest)request).getRequestURI().endsWith("/index.jsp");
		boolean hasJwt=sessionExists && session.getAttribute("x-access-token")!=null;
		boolean isAsset=uri.contains("/assets/") || uri.contains("/images/");
		boolean isExempt=uri.endsWith("/login") || uri.endsWith("/logout");
//		System.out.println("SessionTimeoutServletFilter:: doFilter() [sessionExists="+sessionExists+", isLoginPage="+isLoginPage+", hasJwt="+hasJwt+", isAsset="+isAsset+", isExempt="+isExempt+"] uri = "+((HttpServletRequest)request).getRequestURI());
		
		if (!isAsset && !hasJwt && !isLoginPage && !isExempt){
			resp.sendRedirect(ctx.getContextPath()+"/index.jsp");
		}
		
		chain.doFilter(request, response);
		
	}

	@Override
	public void destroy(){
		
	}

}
