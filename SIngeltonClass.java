
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
