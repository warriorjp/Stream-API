
public class SIngeltonClass {

	public static void main(String[] args) {
		Singleton obj1 = Singleton.getInstance();
		System.out.println("HashCode Value : " + obj1.hashCode());

		Singleton obj2 = Singleton.getInstance();
		System.out.println("HashCode Value : " + obj2.hashCode());

	}
}

class Singleton {
	private static Singleton sc = null;

	private Singleton() {
	}

	public static synchronized Singleton getInstance() {
		if (sc == null) {
			sc = new Singleton();
			return sc;
		} else {
			return sc;
		}

	}
}

To breack the singleton design pattern we can use serializable and clonable interface 
And to prevent it from breaking we use
Serializable - readResolver() method 
Clonable  - override clone methods and throw CloneNotSupportedException

// Protect against serialization
    protected Object readResolve() {
        return getInstance();
    }

    // Protect against cloning
    @Override
    protected Object clone() throws CloneNotSupportedException {
        throw new CloneNotSupportedException("Cloning not supported for singleton");
    }
