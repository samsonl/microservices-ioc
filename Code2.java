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

interface NumberAdder {
  public int add(int i, int j);
};
interface PadToPlaces {
  public String pad(String s1, String s2, int p);
};
interface NumberOperator {
  public int doublen(int i);
  public int triplen(int i);
};
// ----------------------------------------------------------------------------------------------------

// ----------------------------------------------------------------------------------------------------
class NumberOperatorImpl implements Function, NumberOperator {
  public Object [] execute(Object... args) {
    int n1 = ((Integer)args[0]).intValue();

    return new Object[] {new Integer(triplen(n1))};
  }
  // TODO: How to handle this when interface has two+ worker methods
  public int doublen(int i) {  
    return i*2;
  }
  public int triplen(int i) {
    return i*3;
  }
}
// ----------------------------------------------------------------------------------------------------
class NumberAdderImpl implements Function, NumberAdder {
  public Object [] execute(Object... args) {
    int n1 = ((Integer)args[0]).intValue();
    int n2 = ((Integer)args[1]).intValue();

    return new Object[] {new Integer(add(n1,n2))};
  }
  
  public int add(int i, int j) {
    return i+j;
  }
  
}
// ----------------------------------------------------------------------------------------------------
class PadToPlacesImpl implements Function, PadToPlaces {
  public Object [] execute(Object... args) {
    String s1= (String)args[0];
    String s2= (String)args[1];
    int n3 = ((Integer)args[2]).intValue();

    return new Object[] { pad(s1,s2,n3)};
  }
  
  public String pad(String s1, String s2, int p) {
    String pval = (s1+"                    ").substring(0,p);

    return pval;
  }
  
}
// ----------------------------------------------------------------------------------------------------
// ----------------------------------------------------------------------------------------------------
// ----------------------------------------------------------------------------------------------------
public class Code2 {
  public static void main(String [] args) throws Exception {
  	
  	  
    System.out.println(">>"+new javax.script.ScriptEngineManager().getEngineFactories());
 
    boolean start_server = args.length>0 && args[0].equals("-server");
    Configuration c = new SimpleConfiguration();
    IOC container = c.configure(start_server);
    
    NumberDoubler f2 = (NumberDoubler)((container.resolve(NumberDoubler.class)));
    int db2 = f2.doublef(10);
    System.out.println("numberDoubler="+db2);

    NumberAdder f3 = (NumberAdder)((container.resolve(NumberAdder.class)));
    int db3 = f3.add(10,20);
    System.out.println("numberAdder="+db3);

    PadToPlaces f4 = (PadToPlaces)((container.resolve(PadToPlaces.class)));
    String db4 = f4.pad(db2+""," ",10);
    System.out.println("paddToPlaces=("+db4+")");

    System.out.println("Application Running");
    System.in.read();
    container.finish();
  }
}
// ----------------------------------------------------------------------------------------------------


