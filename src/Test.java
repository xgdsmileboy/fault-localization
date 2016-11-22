import java.util.ArrayList;
import java.util.List;

public class Test {
	String field = "";

	public Test() {
	}

	public void testMethod() {
		Test test = new Test();
		Test test2;
		test2 = new Test();

		if (test == null)
			test = new Test();
		else {

		}

		for (int i = 0; i < 8; i++) {
			i++;
		}

		List<String> list = new ArrayList<>();
		for (String string : list) {

		}

		int a = 0;
		while (true) {
			if (a < 8) {
				break;
			}
		}

		do {
			a++;
		} while (a < 9);

		switch (a) {

		case 1:
			a++;
		case 2:
			break;
		default:
		}

		try {
			a++;
		} catch (Exception exception) {
			throw exception;
		}

	}

	public static void main(String[] args) {
		String clazz = "A$B$C";
		System.out.println(clazz.substring(0, clazz.lastIndexOf("$")));

	}

	class inner {

	}
}

class another {

}
