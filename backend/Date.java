package backend;

import java.io.*;
import java.util.*;
import java.time.*;

import share.Color;

public class Date implements Serializable {

	private static final long serialVersionUID = 1L;
	static final String color = Color.ANSI + Color.Fore[5] + Color.ANSI + Color.Back[0] + Color.BOLD;
	public static final int BYTES = Integer.BYTES+
									Integer.BYTES+
									Integer.BYTES;

	static boolean isValidDate(int year, int month, int day) {
		try {
			LocalDate.of(year, month, day); // This will throw an exception if invalid
			return true;
		} catch (DateTimeException e) {
			return false;
		}
	}
	static Date readDate(RandomAccessFile file) {
		Date date = null;
		try {
			date = new Date(file.readInt(), file.readInt(), file.readInt());
		} catch (IllegalArgumentException | IOException e) {
			e.printStackTrace();
			System.out.println("Data stored error.");
			System.exit(-1);
		}
		return date;
	}
	static void writeDate(RandomAccessFile file, Date date) {
		try {
			if( date==null ){
				file.writeInt(-1);
				file.writeInt(-1);
				file.writeInt(-1);
			}
			else{
				file.writeInt(date.getYear());
				file.writeInt(date.getMonth());
				file.writeInt(date.getDay());
			}
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
	public static Date requireDate(String forWhat) {
		Date date = null;
		String[] question = {"year: ", "month: ", "day: "};
		Scanner scanner = new Scanner(System.in);
		while ( true ) {
			int[] input = new int[3];
			System.out.println("Enter your " + forWhat + ": ");
			for(int x=0;x<question.length;x++){
				while(true){
					System.out.print(question[x]);
					try {
						input[x] = scanner.nextInt();
						scanner.nextLine();
					} catch ( InputMismatchException e1 ) {
						System.out.println("are you serious?");
						scanner.nextLine();
						continue;
					} catch ( NoSuchElementException e2 ) {
						System.out.println(e2.getMessage());
						continue;
					}
					break;
				}
			}
			try {
				date = new Date(input[0], input[1], input[2]);
			} catch ( IllegalArgumentException e ) {
				System.out.println(e.getMessage());
				continue;
			}
			break;
		}
		return date;
	}

	private int year;
	private int month;
	private int day;

	public Date() {
		LocalDate now = LocalDate.now();
		this.year = now.getYear();
		this.month = now.getMonthValue();
		this.day = now.getDayOfMonth();
	}
	public Date(int year, int month, int day) throws IllegalArgumentException {
		if (!isValidDate(year, month, day)) throw new IllegalArgumentException("Invalid date: " + year + "/" + month + "/" + day);
		this.year = year;
		this.month = month;
		this.day = day;
	}
	Date(Date date) {
		this(date.getYear(), date.getMonth(), date.getDay());
	}

	public int getYear() { return this.year; }
	public int getMonth() { return this.month; }
	public int getDay() { return this.day; }
	public int getAge() {
        LocalDate birthDate = LocalDate.of(this.year, this.month, this.day);
        LocalDate currentDate = LocalDate.now();
        int age = currentDate.getYear() - birthDate.getYear();
        if (currentDate.getMonthValue() < this.month || 
            (currentDate.getMonthValue() == this.month && currentDate.getDayOfMonth() < this.day)) {
            age--;
        }
        return age;
    }

	public String toString() {
		return this.toString(0);
	}
	public String toString(int level) {
		StringBuilder sb = new StringBuilder();
		String indent = "\t".repeat(level);

		String date = String.format("%04d/%02d/%02d", this.year, this.month, this.day);
		sb.append(Date.color)
		  .append(indent).append("Date: {\n")
		  .append(indent).append("\t").append(date).append("\n")
		  .append(indent).append("}")
		  .append(Color.RESET);
		return sb.toString();
	}
	@Override
	public boolean equals(Object obj) {
		if (this == obj) {
			return true;
		}
		if (obj == null || getClass() != obj.getClass()) {
			return false;
		}
		Date otherDate = (Date) obj;
		return this.year == otherDate.year &&
			this.month == otherDate.month &&
			this.day == otherDate.day;
	}

	public static void main(String[] args) {
		Date date = Date.requireDate("birth date");
		System.out.println(date);
	}
}
