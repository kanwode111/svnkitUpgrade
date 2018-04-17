<%@ page language="java" contentType="text/html; charset=UTF-8"
	pageEncoding="UTF-8"%>  
<%-- <%    
String path = request.getContextPath();   
String basePath=request.getScheme()+"://"+ request.getServerName()+":"+request.getServerPort()
%>   --%>
<%
	// String basePath = request.getScheme()+"://"+ request.getServerName()+":"+request.getServerPort()+"/update/";
	String path = request.getContextPath();
	String basePath=request.getScheme()+"://"+ request.getServerName()+":"+request.getServerPort() + path + "/";
%>
    
<!DOCTYPE HTML PUBLIC "-//W3C//DTD HTML 4.01 Transitional//EN">    
<html>    
  <head>    
    <base href="<%=basePath%>">    
        
    <title>My JSP 'userinfo.jsp' starting page</title>    
        
    <meta http-equiv="pragma" content="no-cache">    
    <meta http-equiv="cache-control" content="no-cache">    
    <meta http-equiv="expires" content="0">        
    <meta http-equiv="keywords" content="keyword1,keyword2,keyword3">    
    <meta http-equiv="description" content="This is my page">    
    <!--  
    <link rel="stylesheet" type="text/css" href="styles.css">  
    -->    
    
  </head>    
      
  <body>    
    <form id="addUser" action="upgradeManager/connectSVN" method="get">     
        用户名: <input id="name" type="text" name="username"/><br/>     
        密码: <input id="password" type="password" name="password"/><br/>   
   SVNURL: <input id="URL" type="text" name="host"/><br/>  
        <input type="submit" value="登陆SVN"/>     
    </form>    
  </body>    
</html>     