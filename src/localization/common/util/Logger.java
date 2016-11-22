package localization.common.util;

public class Logger {

	public static final int DEBUG = 0;
	public static final int INFO = 5;
	public static final int WARNING = 10;
	public static final int ERROR = 15;

	private static Logger instance;

	// private PrintStream debugOS;
	// private PrintStream infoOS = System.out;
	// private PrintStream warningOS = System.out;
	// private PrintStream errorOS = System.err;

	private int fileLogLevel = Integer.MIN_VALUE;
	private String prefix;

	public static Logger getInstance() {
		if (instance == null) {
			instance = new Logger(null);
		}
		return instance;
	}

	public Logger(Object clazz) {
		if (clazz != null) {
			this.prefix = "[" + clazz.getClass().getName() + "] : "; //$NON-NLS-1$ //$NON-NLS-2$
		} else {
			this.prefix = "[ANONYMOUS] : "; //$NON-NLS-1$
		}
	}

	public final void log(Throwable e) {
		// CoveragePlugin cp = CoveragePlugin.getDefault();
		// if ( cp != null ) {
		// String message = new String();
		// if ( e.getMessage() != null ) {
		// message = e.getMessage();
		// }
		// cp.getLog().log(
		// new Status( IStatus.ERROR, CoveragePlugin.PLUGIN_ID,
		// IStatus.OK, message, e ) );
		// }
	}
}
