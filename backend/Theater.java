package backend;

import java.io.*;
import java.util.*;

import share.Color;
import share.Helper;

public class Theater extends ReadOneObject implements Serializable {

	private static final long serialVersionUID = 1L;
	private static RandomAccessFile file = null;
	static final String color = Color.ANSI + Color.Fore[1] + Color.ANSI + Color.Back[0] + Color.BOLD;
    public static final int nameMaxLength = 32;
	public static final long BYTES = Character.BYTES * Theater.nameMaxLength +
                                    Integer.BYTES +
                                    Byte.BYTES +
                                    Integer.BYTES;

	static void open() {
		Theater.file = Filetool.open("theater", Theater.BYTES);
	}
	static void truncate() {
		Filetool.truncate(Theater.file);
	}
	static void close() {
		Filetool.close(Theater.file);
	}
	static ArrayList<Theater> readAll() {
		ArrayList<Theater> theaters = Filetool.readAllObjects(Theater.file, Theater::new);
		return theaters;
	}
	@Override
	void readOneObject() {
		Theater.readTheater(this, Theater.file);
	}
	static Theater readTheater() {
		Theater theater = new Theater();
		Theater.readTheater(theater, Theater.file);
		return theater;
	}
    static Theater readTheater(RandomAccessFile file) {
        Theater theater = new Theater();
        Theater.readTheater(theater, file);
        return theater;
    }
	static void readTheater(Theater theater, RandomAccessFile file) {
		try {
            theater.setName(Filetool.readChars(file, Theater.nameMaxLength));
            theater.setSeatAmount(file.readInt());
            theater.setValid(file.readBoolean());
            theater.setUid(file.readInt());
		} catch (IOException e) {
			e.printStackTrace();
			System.exit(-1);
		}
	}
	static void writeTheater(Theater theater) {
		Theater.writeTheater(theater, Theater.file);
	}
	static void writeTheater(Theater theater, RandomAccessFile file) {
		try {
            if( file == Theater.file ) {
                Theater.file.seek(theater.getUid() * Theater.BYTES);
            } else {
                // file pointer no movement
            }
            Filetool.writeChars(file, theater.getName(), Theater.nameMaxLength);
			file.writeInt(theater.getSeatAmount());
            file.writeBoolean(theater.getValid());
            file.writeInt(theater.getUid());
		} catch (IOException e) {
			e.printStackTrace();
			System.out.println("Data written error.");
			System.exit(-1);
		}
	}
	static int findFirstValidPosition() {
		int uid = 0;
		try {
			Theater.file.seek(0);
			long cur = 0L;
			while( cur != Theater.file.length() ) {
				Theater theater = Theater.readTheater();
				cur = Theater.file.getFilePointer();
				if( !theater.getValid() ) {
					break;
				}
				uid ++;
			}
		} catch (IOException e) {
			e.printStackTrace();
			System.out.println("Data read error.");
			System.exit(-1);
		}
		return uid;
	}
	static boolean isRepeatedName(Theater theater) {
		ArrayList<Theater> theaters = Theater.readAll();
		for(Theater existed: theaters) {
			if( !existed.getValid() ) continue;
			if( existed.getName().equals(theater.getName()) ){
				return true;
            }
		}
		return false;
	}
	public static String requireName() {
		Theater theater = new Theater();
		Scanner scanner = new Scanner(System.in);
        while ( true ) {
			System.out.print("name: ");
			try {
				String name = scanner.nextLine();
				theater.setName(name);
				return name;
			} catch( NoSuchElementException e1 ) {
				System.out.println(e1.getMessage());
			} catch ( IllegalArgumentException e2 ) {
				System.out.println(e2.getMessage());
			}
			continue;
		}
	}
	public static int requireSeatAmount() {
		Theater theater = new Theater();
		Scanner scanner = new Scanner(System.in);
		while ( true ) {
            System.out.print("seatAmount ( " + Movie.smallSeatAmount + " or " + Movie.largeSeatAmount + " ) : ");
			try {
				int seatAmount = scanner.nextInt();
				scanner.nextLine();
				theater.setSeatAmount(seatAmount);
				return seatAmount;
			} catch ( InputMismatchException e1 ) {
				System.out.println("are you serious?");
				scanner.nextLine();
			} catch( NoSuchElementException e2 ) {
				System.out.println(e2.getMessage());
			} catch ( IllegalArgumentException e3 ) {
				System.out.println(e3.getMessage());
			}
			continue;
        }
	}
	public static Theater requireTheater() {
		Theater theater = new Theater();
		String name = Theater.requireName();
		theater.setName(name);
        int seatAmount = Theater.requireSeatAmount();
		theater.setSeatAmount(seatAmount);
        return theater;
	}
	static Theater chooseTheater(List<Theater> theaters) {
		Scanner scanner = new Scanner(System.in);
		for(Theater theater:theaters){
			System.out.println(theater);
		}
        while(true) {
            System.out.print("Choose one theater name: ");
            try {
				String name = scanner.nextLine();
                for(Theater theater:theaters){
                    if(theater.getName().equals(name)){
                        return new Theater(theater);
                    }
                }
			} catch( NoSuchElementException e1 ) {
				System.out.println(e1.getMessage());
			}
            System.out.println("I think you type the wrong name of theater: ");
            continue;
        }
    }

	private int rowAmount;
	private int colAmount;
    private String name;
    private int seatAmount;
    private boolean valid;
    private int uid;

    public Theater() {
        this.setValid(false);
        this.setUid(-1);
    }
    Theater(String name, int seatAmount, boolean valid, int uid) throws IllegalArgumentException {
        this.setName(name);
        this.setSeatAmount(seatAmount);
        this.setValid(valid);
        this.setUid(uid);
    }
    Theater(Theater theater) {
        this(theater.getName(), theater.getSeatAmount(), theater.getValid(), theater.getUid());
    }

	public int getRowAmount() {
		return this.rowAmount;
	}
	public int getColAmount() {
		return this.colAmount;
	}
    public String getName() {
        return this.name;
    }
    public int getSeatAmount() {
        return this.seatAmount;
    }
    public boolean getValid() {
        return this.valid;
    }
    public int getUid() {
        return this.uid;
    }

    public void setName(String name) throws IllegalArgumentException {
        if( name.length()<1 ) throw new IllegalArgumentException("password length should >= 1");
        if( name.length()>Theater.nameMaxLength ) throw new IllegalArgumentException("password length should <= " + Theater.nameMaxLength);
        this.name = name;
    }
    public void setSeatAmount(int seatAmount) throws IllegalArgumentException {
        if( seatAmount != Movie.smallSeatAmount && seatAmount != Movie.largeSeatAmount ){
			throw new IllegalArgumentException("only " + Movie.smallSeatAmount + " or " + Movie.largeSeatAmount);
		}
		this.rowAmount = (seatAmount==Movie.smallSeatAmount)?  9 : 13;
		this.colAmount = (seatAmount==Movie.smallSeatAmount)? 16 : 39;
        this.seatAmount = seatAmount;
    }
    void setValid(boolean valid) {
        this.valid = valid;
    }
    void setUid(int uid) {
        this.uid = uid;
    }

    public String toString() {
		return this.toString(0);
	}
	public String toString(int level) {
		StringBuilder sb = new StringBuilder();
		String indent = "\t".repeat(level);

		sb.append(Theater.color)
		  .append(indent).append("Theater {\n")
		  .append(indent).append("\tuid: ").append(this.uid).append("\n")
		  .append(indent).append("\t影廳名稱: ").append(this.name).append("\n")
		  .append(indent).append("\t座位數量: ").append(this.seatAmount).append("\n")
		  .append(indent).append("\tvalid: ").append(this.valid).append("\n")
		  .append(indent).append("}")
		  .append(Color.RESET);

		return sb.toString();
	}
    public static void main(String[] args) {
        Theater.open();
		Theater.truncate();
		if( Helper.getOneCharInput("create theater by yourself or by default? [y/d]: ", "yd") == 'd' ){
			Theater theater0 = new Theater("large", Movie.largeSeatAmount, true, 0);
			Theater theater1 = new Theater("small", Movie.smallSeatAmount, true, 1);
			Theater.writeTheater(theater0);
			Theater.writeTheater(theater1);
		} else {
			while ( true ) {
				if( Helper.getOneCharInput("create one theater? [y/n]: ", "yn") == 'n' ) break;
				Theater created = Theater.requireTheater();
				created.setUid( Theater.findFirstValidPosition() );
				created.setValid( true );
				System.out.println("created = " + created );
				Theater.writeTheater(created);
			}
		}
		System.out.println("All data stored in theater:");
		for(Theater theater:Theater.readAll()){
			System.out.println(theater);
		}
		if( Helper.getOneCharInput("truncate all theater? [y/n]: ", "yn") == 'y' ) Theater.truncate();
		Theater.close();
    }
}