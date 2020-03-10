public class NumberDoublerImpl implements Function, NumberDoubler {
  public Object [] execute(Object... args) {
    int n1 = ((Integer)args[0]).intValue();

    return new Object[] {new Integer(doublef(n1))};
  }
  
  public int doublef(int i) {
    return i*2;
  }
  
}