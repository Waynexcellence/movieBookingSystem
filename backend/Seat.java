package backend;

import java.io.*;
import java.util.*;

import share.Color;

public class Seat implements Serializable {
    private static final long serialVersionUID = 1L;
	public static final int colorTextLength = 4;
	public static final long BYTES = Character.BYTES +
									Integer.BYTES +
									Character.BYTES * Seat.colorTextLength;

    private int seatAmount;
    private char row;
    private int col;
	private String region;

	static Seat readSeat(RandomAccessFile file) {
		Seat seat = null;
		try {
			seat = new Seat(file.readChar(), file.readInt(), Filetool.readChars(file, colorTextLength));
		} catch (IllegalArgumentException | IOException e) {
			e.printStackTrace();
			System.out.println("Data stored error.");
			System.exit(-1);
		}
		return seat;
	}
	static void writeSeat(RandomAccessFile file, Seat seat) {
		try {
			if( seat==null ){
				file.writeChar('\0');
				file.writeInt(-1);
				file.writeChars("0000");
			}
			else{
				file.writeChar(seat.getRow());
				file.writeInt(seat.getCol());
				Filetool.writeChars(file, seat.getRegion(), Seat.colorTextLength);
			}
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
    private char requireRow() {
        Scanner scanner = new Scanner(System.in);
        while ( true ) {
            System.out.print("choose one row you want to sit: ");
			try {
				char row = scanner.nextLine().toUpperCase().charAt(0);
				this.setRow(row);
				return row;
			} catch( NoSuchElementException e2 ) {
				System.out.println(e2.getMessage());
			} catch ( IllegalArgumentException e3 ) {
				System.out.println(e3.getMessage());
			}
			continue;
        }
    }
    private int requireCol() {
        Scanner scanner = new Scanner(System.in);
        while ( true ) {
            System.out.print("choose one col you want to sit: ");
			try {
				int col = scanner.nextInt();
				scanner.nextLine();
				this.setCol(col);
                return col;
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
    static Seat requireSeat(int seatAmount) {
        Seat seat = new Seat(seatAmount);
		char row = seat.requireRow();
        seat.setRow(row);
        int col = seat.requireCol();
        seat.setCol(col);
		seat.setRegion();
        return seat;
    }

	public Seat () {
		this.row = '0';
		this.col = 0;
		this.region = " ";
	}
	public Seat (Seat seat) {
		this(seat.getRow(), seat.getCol(), seat.getRegion());
	}
    Seat (int seatAmount) {
        this.seatAmount = seatAmount;
    }
	Seat (char row, int col, String region) {
		this.row = row;
		this.col = col;
		this.region = region;
	}

	public char getRow() {
		return this.row;
	}
	public int getCol() {
		return this.col;
	}
	public String getRegion() {
		return this.region;
	}
    void setRow(char row) throws IllegalArgumentException {
        if( row < 'A' || row > 'M' ) throw new IllegalArgumentException("choose one correct row");
		if( this.seatAmount == 0 ) throw new IllegalArgumentException("Invalid calling to setter method.");
        if( this.seatAmount == Movie.smallSeatAmount && row>'I' ) throw new IllegalArgumentException("choose one correct row");
        this.row = row;
    }
    void setCol(int col) throws IllegalArgumentException {
        if( col<1 || col>39 ) throw new IllegalArgumentException("choose one correct col");
		if( this.seatAmount == 0 ) throw new IllegalArgumentException("Invalid calling to setter method.");
        if( this.seatAmount == Movie.smallSeatAmount ){
			if( col>16 ) throw new IllegalArgumentException("choose one correct col");
		}
        else {
            if( this.row!='L' && (col==12 || col==13 || col==26 || col==27 || col==39) ) throw new IllegalArgumentException("choose one correct col");
            if( this.row=='M' && (col>8 && col<31) ) throw new IllegalArgumentException("choose one correct col in row M");
            if( this.row=='B' && (col<5 || col>34) ) throw new IllegalArgumentException("choose one correct col in row B");
			if( this.row=='A' && (col<8 || col>31) ) throw new IllegalArgumentException("choose one correct col in row A");
        }
		this.col = col;
    }
	void setRegion() {
		if( this.seatAmount == 0 ) throw new IllegalArgumentException("Invalid calling to setter method.");
		if( this.seatAmount==Movie.smallSeatAmount ) this.region = Color.Back[7];
		else {
			if( ('I'==this.row||this.row=='J') && 14<=this.col && this.col<=25 ) this.region = Color.Back[1];
			else if( 'H'<=this.row && this.row<='K' && 10<=this.col && this.col<=29 ) this.region = Color.Back[3];
			else if( 'G'<=this.row && this.row<='K' &&  8<=this.col && this.col<=31 ) this.region = Color.Back[4];
			else if( this.row<='K'|| this.row=='M' ) this.region = Color.Back[7];
			else {
				if( 10<=this.col&&this.col<=31 ) this.region = Color.Back[3];
				else if( 6<=this.col&&this.col<=35 ) this.region = Color.Back[4];
				else this.region = Color.Back[7];
			}
		}
	}

	@Override
    public boolean equals(Object obj) {
        if (obj == this) return true;
        if (obj == null || this.getClass() != obj.getClass()) {
            return false;
        }
        Seat other = (Seat) obj;
        return this.row == other.row && this.col == other.col;
    }
	public String toString() {
		return this.toString(0);
	}
	public String toString(int level) {
		StringBuilder sb = new StringBuilder();
		String indent = "\t".repeat(level);
		String ticketColor = Color.ANSI+this.region + Color.ANSI+Color.Fore[0];
		sb.append(indent).append("Seat {").append("\n")
		  .append(indent).append(ticketColor).append("\trow: ").append(this.row).append(Color.RESET+"\n")
		  .append(indent).append(ticketColor).append("\tcol: ").append(this.col).append(Color.RESET+"\n")
		  .append(indent).append("}")
		  .append(Color.RESET);

		return sb.toString();
	}
}