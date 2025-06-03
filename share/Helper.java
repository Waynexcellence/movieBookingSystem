package share;

import java.io.*;
import java.util.*;
import java.util.regex.*;

public class Helper implements Serializable {

    public static String longLine = "-----------------------------------------------";

	public static char getOneCharInput(String hint, String chars) {
        Scanner scanner = new Scanner(System.in);
        while (true) {
            System.out.print(hint);
            try {
                String input = scanner.nextLine().toLowerCase();
                if (input.length() == 1 && chars.contains(input)) {
                    return input.charAt(0);
                } else {
                    System.out.println("Invalid input. Please enter again.");
                }
            } catch (NoSuchElementException e) {
                System.out.println("Error reading input.");
                break;
            }
        }
        return '\0';
    }
	public static String getIPAddress() {
        Scanner scanner = new Scanner(System.in);
        String input;
        Pattern ipPattern = Pattern.compile("^140\\.112\\.(\\d{1,3})\\.(\\d{1,3})$");

        while (true) {
            System.out.print("請輸入 IP 位址, 格式為 140.112.xxx.xxx, 或直接按 Enter 使用 localhost: ");
            input = scanner.nextLine().trim();
            if (input.isEmpty()) {
                return "localhost";
            }
            if ( ipPattern.matcher(input).matches() ) {
                return input;
            } else {
                System.out.println("格式錯誤, 請重新輸入, 或按 Enter 使用 localhost: ");
            }
        }
    }

}
