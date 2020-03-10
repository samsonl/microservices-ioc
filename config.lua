-- print("OK")
-- jframe = luajava.bindClass( "javax.swing.JFrame" )
-- frame = luajava.newInstance( "javax.swing.JFrame", "Texts" );
-- frame:setVisible(true)
-- sys = luajava.bindClass('java.lang.System')
-- print ( sys:currentTimeMillis())  

function register(class, impl, invoker) 
  container:register(luajava.bindClass( class ), luajava.newInstance( invoker, luajava.newInstance( impl )) );
end

register("NumberDoubler","NumberDoublerImpl","InMemoryInvoker")

container:finish()
