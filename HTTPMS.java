import java.util.*;
import fi.iki.elonen.*;
import fi.iki.elonen.NanoHTTPD.IHTTPSession;

import javax.script.*;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.fasterxml.jackson.core.JsonProcessingException;
import java.io.*;
import java.net.*;

import java.lang.reflect.*;

// ----------------------------------------------------------------------------------------------------
interface Serializer {
  Object [] encode(String in);
  String decode(Object [] args);
}
// ----------------------------------------------------------------------------------------------------
interface FunctionResolver {
  public Function resolve(IHTTPSession session); 
  public void add(Matcher m );
};
// ----------------------------------------------------------------------------------------------------
interface Matcher {
  public Function match(IHTTPSession s);
};
// ----------------------------------------------------------------------------------------------------
class MicroserviceServer extends NanoHTTPD {
    private FunctionResolver impl;
    private Serializer serializer;
    int port;

    public MicroserviceServer(Serializer serializer, int port) {
        super(port);
        this.serializer = serializer;
        this.port = port;
    }
    
    public void setFunctionResolver(FunctionResolver impl) {
    	this.impl = impl;
    }
    
    @Override public Response serve(IHTTPSession session) {
        Method method = session.getMethod();
        String uri = session.getUri();
        String retV = "";
        //System.out.println(method + " '" + uri + "' ");

        try { 
          Map<String, String> files = new HashMap<String, String>();
          session.parseBody(files);

          String postBody = session.getQueryParameterString();
          Object [] args = this.serializer.encode(postBody);
       
          Function imp = this.impl.resolve(session);
          Object [] returnValues = imp.execute(args);
       
          //String fRet = (String)(returnValues[0]);
          Object fRet = (returnValues[0]);
          retV = this.serializer.decode(new Object[] { fRet });

          //System.out.println("POST:"+postBody);
          //System.out.println("-----------"+fRet+"]");
          //System.out.println("HTTP Return :"+retV);

        } catch (Exception e) {
          e.printStackTrace();
        }

        return new NanoHTTPD.Response(retV);
    }
}
// ----------------------------------------------------------------------------------------------------
class HTTPManager {
   
  public void init_receiver(FunctionResolver res) {
       System.out.println("Start Server");
       try { 
         MicroserviceServer server1 = null;
         int port = 8081;
         
         server1 = new MicroserviceServer(new BasicArraySerializer(), port);
         server1.setFunctionResolver(res);
         server1.start(); 
         System.out.println("Server Started port="+port);
       } catch ( Exception e ) {
         System.out.println("Server start error:");
         e.printStackTrace();
       }
  }
  
  public Invoker register(FunctionResolver res, Class cls, Function f, String host, String path) {
  
    Invoker i = new MicroserviceJsonOverHTTPInvoker();
    ((MicroserviceJsonOverHTTPInvoker)i).addInterface(f, host, path);
    res.add((IHTTPSession sess) -> {
      String uri = sess.getUri();
      if ( uri.equals(path) ) return f;

      return null;
    });

    return i;
  }
}
// ----------------------------------------------------------------------------------------------------
class HTTPJsonFunctionWrapper implements Function {
  Function base = null;
  Serializer serializer = null;
  String endpointURL = null;

  HTTPJsonFunctionWrapper(Function f, Serializer serializer, String endpointURL) {
    this.base = f;
    this.serializer = serializer;
    this.endpointURL = endpointURL;
  }

  public Object [] execute(Object... args) {
    try {  
      String jsonString = this.serializer.decode(args);
      //System.out.println("FunctionWrapper Args ->"+jsonString);

      URL url = new URL(this.endpointURL);
      HttpURLConnection connection = (HttpURLConnection) url.openConnection();
      connection.setRequestMethod("POST");
      connection.setDoOutput(true);

      PrintWriter out = new PrintWriter(connection.getOutputStream());
      out.println(jsonString);
      out.close();
      BufferedReader in = new BufferedReader(new InputStreamReader(connection.getInputStream()));

      String line;
      String all = "";
      while ((line = in.readLine()) != null) { // TODO: fix
         //System.out.println(line);
         all = all + line;
      }
      in.close();

      //System.out.println("HTTP Wrapper client :"+all);

      Object [] rets = this.serializer.encode(all);
      return rets;

    } catch (Exception ex) {
      ex.printStackTrace();
    }

    return null;
    //return this.base.execute(args);
  }

}
// ----------------------------------------------------------------------------------------------------
class MicroserviceJsonOverHTTPInvoker implements Invoker {
  Function fwrap;
  Function fx;
  String url;

  public MicroserviceJsonOverHTTPInvoker() {
    
  }

  public Object [] execute(Object... args) {
    //System.out.println("Exec start");
    return fwrap.execute(args);
  }

  public void init_sender() {} ;
 
  public Function addInterface(Function fx, String url, String path) {
    this.fx = fx;
    this.url = url+path;
    
    try { 
      Function wrapped = new HTTPJsonFunctionWrapper(fx, new BasicArraySerializer(),this.url);
      System.out.println("Wrapped to :"+this.url); 
      this.fwrap = wrapped;
    } catch ( Exception e ) {
      e.printStackTrace();
    }
  
    return this.fwrap;
  }
  
  public void init_receiver() {

  };

};
// ----------------------------------------------------------------------------------------------------
class BasicArraySerializer implements Serializer {
  public Object [] encode(String in) {
    Object [] args = null;
    try { 
      ObjectMapper mapper = new ObjectMapper();
      args = mapper.readValue(in, Object[].class);
    } catch (Exception e) {
    }

    return args;

  }
  public String decode(Object [] args) {
    String jsonString = null;

    try { 
      ObjectMapper mapper = new ObjectMapper();
      jsonString = mapper.writeValueAsString(args);
    } catch (Exception e) {
    }

    return jsonString;
  }
}
// ----------------------------------------------------------------------------------------------------
class BasicFunctionResolver implements FunctionResolver {

  ArrayList<Matcher> funcs = new ArrayList<Matcher>();
  
  public void add(Matcher m ) {
     funcs.add(m);
  }

  public Function resolve(IHTTPSession session) {
    for ( Matcher m : funcs ) {
      if ( m.match(session) != null )
        return m.match(session);
    }
    return null;
  } 
}
// ----------------------------------------------------------------------------------------------------