<%@ page import="org.alfresco.web.site.*" %>
<%@ page import="org.springframework.extensions.surf.*" %>
<%@ page import="org.springframework.extensions.surf.site.*" %>
<%@ page import="org.springframework.extensions.surf.util.*" %>
<%@ page import="org.springframework.extensions.webscripts.connector.*" %>
<%@ page import="java.util.*" %>
<%@ page import="org.json.*" %>
<%
   // retrieve user name from the session
   String userid = (String)session.getAttribute(SlingshotUserFactory.SESSION_ATTRIBUTE_KEY_USER_ID);
   
   // test user dashboard page exists?
   RequestContext context = (RequestContext)request.getAttribute(RequestContext.ATTR_REQUEST_CONTEXT);
   if (context.getObjectService().getPage("user/" + userid + "/dashboard") == null)
   {
      // no user dashboard page found! create initial dashboard for this user...
      String userPreset = (String) context.getUser().getProperty("preset");
      if(userPreset == null) {
         userPreset = "user-dashboard";
      }
      context.getServiceRegistry().getPresetsManager().constructPreset(userPreset, Collections.singletonMap("userid", userid));
   }
   
   // redirect to site or user dashboard as appropriate
   String siteName = request.getParameter("site");
   if (siteName == null || siteName.length() == 0)
   {
      // forward to user specific dashboard page
      response.sendRedirect(request.getContextPath() + "/page/user/" + URLEncoder.encode(userid) + "/dashboard");
   }
   else
   {
      if (context.getObjectService().getPage("site/" + siteName + "/dashboard") == null)
      {
         Connector connector = context.getServiceRegistry().getConnectorService().getConnector("alfresco", userid, session);
         Response siteResponse = connector.call("/api/sites/" + siteName);
         int siteResponseCode = siteResponse.getStatus().getCode();
         if(siteResponseCode != ResponseStatus.STATUS_OK) {
            response.sendError(siteResponseCode, siteResponse.getStatus().getMessage());
         } else {
            JSONObject siteJSON = new JSONObject(siteResponse.getResponse());
            String sitePreset = siteJSON.has("sitePreset") ? siteJSON.getString("sitePreset") : "site-dashboard";
            context.getServiceRegistry().getPresetsManager().constructPreset(sitePreset, Collections.singletonMap("siteid", siteName));
         }
      }
       
      // forward to site specific dashboard page
      response.sendRedirect(request.getContextPath() + "/page/site/" + URLEncoder.encode(siteName) + "/dashboard");
   }
%>