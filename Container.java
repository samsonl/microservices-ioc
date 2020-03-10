import java.util.*;


import java.lang.reflect.*;


import javax.script.*;

import java.io.File;
import java.io.Reader;
import java.io.FileReader;
import java.io.FileNotFoundException;


// ----------------------------------------------------------------------------------------------------
interface IOCInit {
  public void init();
};

interface Function {
  public Object [] execute(Object... args);
};

interface Invoker {
  public Object [] execute(Object... args);
  public void init_sender();
  public void init_receiver();

};


interface Configuration {
  public IOC configure(boolean start_server);
}


// ----------------------------------------------------------------------------------------------------

// ----------------------------------------------------------------------------------------------------

class IOC {
  HashMap<Class,Object> IOC = new HashMap<Class, Object>();

  public void register (Class Int, Invoker i) {
  	 System.out.println("Class="+Int+" Invoker="+i);
  	 
  	 Object f2 = proxyWrap(i, Int);
   
     IOC.put(Int, f2);
  }
  public Object resolve(Class Int) {
    try { 
	  
      return IOC.get(Int);
    } catch (Exception e ) {
    }
    return null;
  }

  public void begin(IOCInit init) {
    init.init();
    System.out.println("Initialisation complete");
  }
  public void finish() {
  	System.out.println("Container.finish");
    //if ( server1 != null ) server1.stop();
  }
  
  public static Object proxyWrap(Invoker inv, Class cla) {
  
    InvocationHandler handler = new InvocationHandler() {
      public Object invoke(Object proxy, Method method, Object[] args) throws Throwable {
        //System.out.println("Proxy invoke");
      
        Object [] returnValues = inv.execute(args);

        if ( returnValues.length == 1 ) // TODO: Why?
          return returnValues[0];
        else 
          return returnValues;
      }
    };
    Object f = java.lang.reflect.Proxy.newProxyInstance(cla.getClassLoader(), new Class[] { cla }, handler);
 
    return f;
  }
}
// ----------------------------------------------------------------------------------------------------

class SimpleConfiguration implements Configuration {
  public void script_begin(IOC container) {
    try {

      ScriptEngineManager mgr = new ScriptEngineManager();
  	  ScriptEngine e = mgr.getEngineByName("luaj");
      e.put("container",org.luaj.vm2.lib.jse.CoerceJavaToLua.coerce(container));
	  Reader reader = new FileReader(new File("config.lua"));
      e.eval(reader);

	  //e.put("x", 25);
	  //e.put("f", new  org.luaj.vm2.lib.OneArgFunction() {
	  //			public org.luaj.vm2.LuaValue call(org.luaj.vm2.LuaValue arg) {
	  //			System.out.println("arg "+arg.tojstring());
	  //			return org.luaj.vm2.LuaValue.valueOf(123);
	  //		}
      //    });
	  //e.eval("y = math.sqrt(x)");
	  //System.out.println( "y="+e.get("y") ); 
	  //e.eval("print('>'..f('aaa'))"); 
      
	} catch ( ScriptException e) { 
	} catch ( FileNotFoundException e) { 
	}

  }
  
  public IOC configure(boolean start_server) {
    IOC container = new IOC();
 
    script_begin(container);
	
    /*
    container.begin(
      () -> {
      	FunctionResolver res = new BasicFunctionResolver();
        HTTPManager man = new HTTPManager();

        Invoker i1 = man.register(res, NumberDoubler.class, new NumberDoublerImpl(), "http://localhost:8081","/a/b/c1");
      	container.register(NumberDoubler.class, i1 );   
      	
        Invoker i2 = man.register(res, NumberAdder.class, new NumberAdderImpl(), "http://localhost:8081","/a/b/c2");
      	container.register(NumberAdder.class, i2 );   

        Invoker i3 = man.register(res, PadToPlaces.class, new PadToPlacesImpl(), "http://localhost:8081","/a/b/c3");
      	container.register(PadToPlaces.class, i3 );   

        if (start_server ) man.init_receiver(res);

      });
      */
      
    container.begin(
      () -> {
        //container.register(NumberDoubler.class, new InMemoryInvoker(new NumberDoublerImpl()) );
        container.register(NumberAdder.class, new InMemoryInvoker(new NumberAdderImpl()) );
        container.register(PadToPlaces.class, new InMemoryInvoker(new PadToPlacesImpl()) );
      });
  
   

    return container;
  }
  
}
