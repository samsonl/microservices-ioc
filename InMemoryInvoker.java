public class InMemoryInvoker implements Invoker {
  Function f;

  public InMemoryInvoker(Function f) {
    this.f = f;
  }

  public Object [] execute(Object... args) {
    return f.execute(args);
  }
  public void init_sender() {} ;
  public void init_receiver() {};
};