package il.ac.tau.cs.sw1.trivia;

/**
 * A general exception related to a formatting problem in the loaded trivia file
 */
public class TriviaFileFormatException extends Exception {

	private static final long serialVersionUID = 6640321377249474936L;

	/**
	 * @param message to display to the user
	 * @param rowNum the row number in which the error occurred
	 */
	public TriviaFileFormatException(String message, int rowNum) {
		this(message + " (row " + rowNum + ")");
	}

	/**
	 * @param message to display to the user
	 */
	public TriviaFileFormatException(String message) {
		super(message);
	}
}
