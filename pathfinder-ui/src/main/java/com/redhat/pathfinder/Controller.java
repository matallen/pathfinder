package com.redhat.pathfinder;

import static io.restassured.RestAssured.given;

import java.io.IOException;
import java.net.HttpURLConnection;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;
import java.net.URLEncoder;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import javax.net.ssl.HttpsURLConnection;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;
import javax.ws.rs.DELETE;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.Response.ResponseBuilder;

import org.apache.commons.io.IOUtils;
import org.bson.Document;
import org.bson.codecs.BsonTypeClassMap;
import org.bson.codecs.DocumentCodec;
import org.bson.codecs.configuration.CodecRegistries;
import org.bson.codecs.configuration.CodecRegistry;

import com.google.common.base.Splitter;
import com.google.common.collect.Lists;
import com.mongodb.MongoClient;
import com.mongodb.MongoCredential;
import com.mongodb.ServerAddress;
import com.mongodb.client.MongoDatabase;

import io.restassured.http.Header;


@Path("/pathfinder/")
public class Controller{
  
  public static String getProperty(String name) throws IOException{
    if (name.equals("PATHFINDER_SERVER")) {
    	String result=null;
    	if (null!=System.getProperty("pathfinder.server")) result=System.getProperty("pathfinder.server");
    	if (null!=System.getProperty("PATHFINDER_SERVER")) result=System.getProperty("PATHFINDER_SERVER");
    	if (null!=System.getenv(name)) result=System.getenv(name);
    	if (null!=result) {
//    		System.out.println("Request for System.getenv("+name+")='"+result+"'");
    		return result;
    	}
      System.out.println("DEFAULTING SERVER TO: 'http://localhost:8080' because no environment variable '"+name+"' was found");
      return "http://localhost:8080";
    }
//    System.out.println("Request for System.getenv("+name+")='"+System.getenv(name)+"'");
    
    return System.getenv(name);
  }
  
  
  public static void mainx(String[] asd) throws Exception{
    
    List<ArrayList<String>> apps=Lists.newArrayList(
      Lists.newArrayList("G","G","G","G","G","G"),
      Lists.newArrayList("G","G","G","A","A","A"),
      Lists.newArrayList("A","A","A","A","A","A"),
      Lists.newArrayList("G","A","A","A","A","R"),
      Lists.newArrayList("G","A","A","R","R","R")
    );
    
    double increment=1-(1/apps.get(0).size());
    
    for(List<String> app:apps){
      double score=0;
      Collections.sort(app, new Comparator<String>(){
        public int compare(String o1, String o2){
          return "R".equals(o1)?-1:0;
        }
      });
      double spreadRatio=1;
      for(String r:app){
        if ("R".equals(r)) spreadRatio=spreadRatio*0.4;
        if ("A".equals(r)) spreadRatio=spreadRatio*0.8;
        score+=increment*(1*spreadRatio);
      }
      System.out.println(score);
    }
  }
  public Response getApps(){
    MongoCredential credential = MongoCredential.createCredential("userS1K", "pathfinder", "JBf2ibxFbqYAmAv0".toCharArray());
    MongoClient c = new MongoClient(new ServerAddress("localhost", 9191), Arrays.asList(credential));
    MongoDatabase db = c.getDatabase("pathfinder");
    
    CodecRegistry codecRegistry = CodecRegistries.fromRegistries(MongoClient.getDefaultCodecRegistry());
    final DocumentCodec codec = new DocumentCodec(codecRegistry, new BsonTypeClassMap());

    for (String name : db.listCollectionNames()) {
      System.out.println("CollectionName: "+name);
    }
    for(Document d:db.getCollection("assessments").find()){
      System.out.println(d.toJson(codec));
    }
    
//    for(Document d:db.getCollection("customer").find()){
//      System.out.println(d.toJson(codec));
////      Class.forName(className)
////      try{
////        Object x=Json.newObjectMapper(true).readValue(d.toJson(codec), Class.forName(d.getString("_class")));
////        System.out.println("X = "+x);
////      }catch (Exception e){
////        e.printStackTrace();
////      }
////      d.get("_class");
//    }
    
//    String mongoURI=String.format("mongodb://%s:%s@%s:%s/%s", "userS1K","JBf2ibxFbqYAmAv0","localhost","9191","pathfinder");
//    MongoClient c=new MongoClient(mongoURI);
//    MongoDatabase db=c.getDatabase("pathfinder");
    
    c.close();
    return null;
  }
  
  
  public static void main(String[] asd) throws Exception{
  	Controller c=new Controller();
  	
  }
  @GET
  @Path("{path:.+}")
  public Response getProxy(@PathParam("path") String path, @Context HttpServletRequest request, @Context HttpServletResponse response) throws URISyntaxException, IOException{
  	String proxyUrl=getProperty("PATHFINDER_SERVER")+request.getRequestURI().replaceAll(request.getContextPath(), "");
  	System.out.println("*****************************************************************");
  	System.out.println("PROXY::UI->SVR("+request.getMethod()+"): Request: url="+proxyUrl);
  	// Uncomment to add auth to querystring instead
//	proxyUrl+=(proxyUrl.contains("?")?"&":"?")+"_t="+request.getParameter("_t");
  	Map<String,String> headers=toMapBuilder(request).put("Authorization", request.getParameter("_t")).build();
  	for (Entry<String, String> e:headers.entrySet())
  		System.out.println("PROXY::UI->SVR("+request.getMethod()+"): Request: Headers: "+e.getKey()+"="+e.getValue());
  	
  	io.restassured.response.Response resp = given()
  			.headers(headers)
        .get(proxyUrl);
  	
  	System.out.println("PROXY::UI->SVR("+request.getMethod()+"): Response code="+resp.getStatusCode());
  	System.out.println("PROXY::UI->SVR("+request.getMethod()+"): Response asString="+resp.asString());
  	
//  	printRequestResponseInfo(request, proxyUrl, null, resp);
  	
  	System.out.println("=================================================================");
  	
  	ResponseBuilder rBuilder=Response.status(resp.getStatusCode());
  	System.out.println("PROXY::UI->BRWSR("+request.getMethod()+"): Response code="+resp.getStatusCode());
  	for(Header h:resp.getHeaders()){
  		rBuilder.header(h.getName(), h.getValue());
  		System.out.println("PROXY::UI->BRWSR("+request.getMethod()+"): Request Headers: "+h.getName()+"="+h.getValue());
  	}
  	String body=resp.getBody().asString();
  	System.out.println("PROXY::UI->BRWSR("+request.getMethod()+"): Response body="+body);
  	System.out.println("*****************************************************************");
  	return rBuilder.entity(body).build();
  }
  
  @POST
  @Path("{path:.+}")
  public Response postProxy(@PathParam("path") String path, @Context HttpServletRequest request, @Context HttpServletResponse response) throws URISyntaxException, IOException{
  	String proxyUrl=getProperty("PATHFINDER_SERVER")+request.getRequestURI().replaceAll(request.getContextPath(), "");
  	String body=IOUtils.toString(request.getInputStream());
  	
  	io.restassured.response.Response resp = given()
  			.headers(toMapBuilder(request).put("Authorization", request.getParameter("_t")).build())
  			.body(body)
        .post(proxyUrl);
  	
  	printRequestResponseInfo(request, proxyUrl, body, resp);
  	
  	ResponseBuilder rBuilder=Response.status(resp.getStatusCode());
  	for(Header h:resp.getHeaders())
  		rBuilder.header(h.getName(), h.getValue());
  	return rBuilder.entity(resp.getBody().asString()).build();
  }
  
  @DELETE
  @Path("{path:.+}")
  public Response deleteProxy(@PathParam("path") String path, @Context HttpServletRequest request, @Context HttpServletResponse response) throws URISyntaxException, IOException{
  	String proxyUrl=getProperty("PATHFINDER_SERVER")+request.getRequestURI().replaceAll(request.getContextPath(), "");
  	String body=IOUtils.toString(request.getInputStream());
  	
  	Map<String, String> headers=new HashMap<>();
  	for(Object key:Collections.list(request.getHeaderNames())){
  		headers.put((String)key, request.getHeader((String)key));
  		System.out.println("Browser->UI::request.header('"+key+"')="+request.getHeader((String)key));
  	}
  	headers.put("Authorization", request.getParameter("_t"));
  	headers.remove("Pragma");
  	headers.remove("Cache-Control");
  	headers.remove("Cookie");
  	headers.remove("Host");
  	headers.remove("Content-Length");
  	headers.put("Host", "localhost:8080");
  	for(Entry<String, String> e:headers.entrySet()){
  		System.out.println("UI->Server::request.header('"+e.getKey()+"')="+request.getHeader((String)e.getKey()));
  	}
  	
  	URL url = new URL(proxyUrl);
  	HttpURLConnection httpCon;
  	if (proxyUrl.startsWith("https://")){
  		httpCon = (HttpsURLConnection) url.openConnection();
  	}else{
  		httpCon = (HttpURLConnection) url.openConnection();
  	}
  	httpCon.setDoInput(true); // so we can send a batch list of id's to delete
  	httpCon.setDoOutput(true);
  	for(Entry<String, String> e:headers.entrySet())
  		httpCon.setRequestProperty(e.getKey(), e.getValue());
  	httpCon.setRequestMethod("DELETE");
  	httpCon.connect();
  	httpCon.getOutputStream().write(body.getBytes());
  	
  	String respAsString=IOUtils.toString(httpCon.getInputStream());
  	
//  	System.out.println("respCode="+httpCon.getResponseCode());
//  	System.out.println("respAsString="+respAsString);
  	
  	// Not sure why, but restassured's DELETE seems not to work with a payload
//  	io.restassured.response.Response resp = given()
////  			.headers(toMapBuilder(request).put("Authorization", request.getParameter("_t")).build())
////  			.headers(new MapBuilder<String, String>() /*toMapBuilder(request)*/.put("Authorization", request.getParameter("_t")).build())
////  			.headers(toMapBuilder(request).put("Authorization", request.getParameter("_t")).build())
////  			.header("Content-Type", "application/text")
//  			.headers(headers)
//  			.body(body)
//        .post(proxyUrl);
  	
//  	printRequestResponseInfo(request, proxyUrl, body, resp);
  	
  	return Response.status(httpCon.getResponseCode()).entity(respAsString).build();
  	
//  	ResponseBuilder rBuilder=Response.status(resp.getStatusCode());
//  	for(Header h:resp.getHeaders())
//  		rBuilder.header(h.getName(), h.getValue());
//  	return rBuilder.entity(resp.getBody().asString()).build();
  }
  
  private void printRequestResponseInfo(HttpServletRequest request, String proxyUrl, String body, io.restassured.response.Response resp) throws IOException{
  	System.out.println("Proxy:: "+request.getMethod()+":: "+request.getScheme()+"://"+request.getServerName()+":"+request.getServerPort()+""+request.getRequestURI() +" => "+ proxyUrl +" (status="+resp.getStatusCode()+")");
  	System.out.println("Proxy:: "+request.getMethod()+":: Headers:");
  	for (Object key : Collections.list(request.getHeaderNames()))
  	  System.out.println("Proxy:: "+request.getMethod()+"::   - "+key+"="+request.getHeader((String)key));
  	
  	for(Header header:resp.getHeaders().asList())
  		System.out.println("Proxy:: resp:: "+header);
  	
	  if (null!=body) System.out.println("Proxy:: "+request.getMethod()+":: Body = "+body);
	  System.out.println("Proxy:: "+request.getMethod()+":: Response String = "+resp.getBody().asString());
  }
  
  private MapBuilder<String,String> toMapBuilder(HttpServletRequest request){
  	MapBuilder<String,String> result=new MapBuilder<>();
  	for (Object key : Collections.list(request.getHeaderNames()))
//  		if (!"Content-Length".equals(key))// && !"Origin".equals(key))

  		if (key.equals("Authorization") || key.equals("host") || key.equals("accept-encoding") || key.equals("user-agent") || key.equals("accept"))
  			result.put((String)key, request.getHeader((String)key));
  	
  	
  	
//  	result.values.remove("Content-Length");
//  	result.values.remove("referer");
//  	result.values.remove("accept-language");
//  	result.values.remove("cookie");
//  	result.values.remove("x-forwarded-proto");
//  	result.values.remove("x-forwarded-port");
//  	result.values.remove("pragma");
//  	result.values.remove("x-forwarded-host");
//  	result.values.remove("x-requested-with");
//  	result.values.remove("referer");
//  	result.values.remove("referer");
//  	result.values.remove("referer");
  	
  	return result;
  }
  
  @GET
  @Path("/logout")
  public Response logout(@Context HttpServletRequest request, @Context HttpServletResponse response) throws URISyntaxException, IOException{
    request.getSession().invalidate();
    return Response.status(302).location(new URI("../index.jsp")).build();
  }
  
  private String maskPasswords(String input) {
  	return input.replaceAll("password=.+&", "password=****&");
  }
  
  void log(String s){
  	System.out.println(s);
  	System.err.println(s);
  }
  
  @POST
  @Path("/login")
  public Response login(@Context HttpServletRequest request, @Context HttpServletResponse response) throws URISyntaxException, IOException{
    
    System.out.println("Controller::login() called");
    
    String uri=IOUtils.toString(request.getInputStream());
    
//    log("Controller::login() payload = "+maskPasswords(uri)); //username=&password=
    
    final Map<String, String> keyValues=Splitter.on('&').trimResults().withKeyValueSeparator("=").split(uri);
    
//    System.out.println("Controller::login():: username="+keyValues.get("username") +", password="+keyValues.get("password"));
    log("Controller::login():: username="+keyValues.get("username") +", password=****");

    log("Controller::login():: Auth url (POST) = "+getProperty("PATHFINDER_SERVER")+"/auth");
    
    io.restassured.response.Response loginResp = given()
        .body("{\"username\":\""+keyValues.get("username")+"\",\"password\":\""+keyValues.get("password")+"\"}")
        .post(getProperty("PATHFINDER_SERVER")+"/auth");
    
    if (loginResp.statusCode()!=200){
//    	log("Controller:login():: ERROR1 loginResp(code="+loginResp.statusCode()+").asString() = "+loginResp.asString());
    	log("Controller:login():: 3 OUT/ERROR loginResp(code="+loginResp.statusCode()+").asString() = "+loginResp.asString());
      String error="Username and/or password is unknown or incorrect"; // would grab the text from server side but spring wraps some debug info in there so until we can strip that we cant give details of failure
      return Response.status(302).location(new URI("../index.jsp?error="+URLEncoder.encode(error, "UTF-8"))).build();
    }
    
    log("Controller:login():: 2 loginResp(code="+loginResp.statusCode()+").asString() = "+loginResp.asString());
    mjson.Json jsonResp=mjson.Json.read(loginResp.asString());
    String jwtToken=jsonResp.at("token").asString();
    String username=jsonResp.at("username").asString();
    String displayName=jsonResp.at("displayName").asString();
    
    log("Controller::login():: jwt json response="+jsonResp.toString(99999999));
//    System.out.println("Controller::login():: jwtToken="+jwtToken);
    
    
    request.getSession().setAttribute("x-access-token", jwtToken);
    request.getSession().setAttribute("x-username", username);
    request.getSession().setAttribute("x-displayName", displayName);
    
    return Response.status(302).location(new URI("../manageCustomers.jsp")).header("x-access-token", jwtToken).build();
  }
  
  @POST
  @Path("/logout")
  public Response logout(@Context HttpServletRequest request, @Context HttpServletResponse response, @Context HttpSession session) throws URISyntaxException{
    session.removeAttribute("username");
    session.invalidate();
    // TODO: and invalidate it on the server end too!
    return Response.status(302).location(new URI("/index.jsp")).build();
  }
  
  
}
