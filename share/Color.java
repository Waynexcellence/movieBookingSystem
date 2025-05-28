package share;

public class Color {


	public static final String BOLD = "\u001B[1m";

	public static final String F_BLACK = "[30m";
	public static final String F_RED = "[31m";
	public static final String F_GREEN = "[32m";
	public static final String F_YELLOW = "[33m";
	public static final String F_BLUE = "[34m";
	public static final String F_MAGENTA = "[35m";
	public static final String F_CYAN = "[36m";
	public static final String F_WHITE = "[37m";

	public static final String B_BLACK = "[40m";
	public static final String B_RED = "[41m";
	public static final String B_GREEN = "[42m";
	public static final String B_YELLOW = "[43m";
	public static final String B_BLUE = "[44m";
	public static final String B_MAGENTA = "[45m";
	public static final String B_CYAN = "[46m";
	public static final String B_WHITE = "[47m";

	public static final String[] Fore = {F_BLACK, F_RED, F_GREEN, F_YELLOW, F_BLUE, F_MAGENTA, F_CYAN, F_WHITE};
	public static final String[] Back = {B_BLACK, B_RED, B_GREEN, B_YELLOW, B_BLUE, B_MAGENTA, B_CYAN, B_WHITE};

	public static final String RESET = "\u001B[0m";
	public static final String ANSI = "\u001B";

	public static void resetColor() {
		System.out.print(Color.RESET);
	}
	public static String setTextWithColor(String text, String... colors) {
		StringBuilder code = new StringBuilder();
		for (String color : colors) {
			code.append(Color.ANSI).append(color);
		}
		return code.toString() + Color.BOLD + text + Color.RESET;
	}

	static void printWithColor(String text, String... colors) {
		System.out.println(Color.setTextWithColor(text, colors));
	}

	public static void main(String[] args) {
		// 7 1 for red region
		// 7 3 for yellow region
		// 7 4 for blue region
		// 7 7 for gray region

		// 1 0 for Theater
		// 2 0 for Movie
		// 3 0 for User
		// 4 0 for Date
		// 4 0 for Film
		// 6 0 for Ticket
		for(int x=0;x<Fore.length;x++){
			for(int y=0;y<Back.length;y++){
				System.out.print(Color.BOLD);
				printWithColor(" "+x+" "+y, Color.Fore[x], Color.Back[y]);
			}
		}
		resetColor();
		System.out.println("This shoud be normal text.");
	}
}