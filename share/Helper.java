package share;

import java.io.*;
import java.util.*;

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

}
