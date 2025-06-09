package backend;

import java.io.*;
import java.util.*;

import share.Color;
import share.Helper;

public class Film extends ReadOneObject implements Serializable {

	private static final long serialVersionUID = 1L;
	private static RandomAccessFile file = null;
	static final String color = Color.ANSI + Color.Fore[4] + Color.ANSI + Color.Back[0] + Color.BOLD;
	public static final int titleMaxLength = 128;
	public static final int summaryMaxLength = 128;
	public static final long BYTES = Character.BYTES * Film.titleMaxLength +
									Character.BYTES * Film.summaryMaxLength +
									Integer.BYTES +
									Integer.BYTES +
									Byte.BYTES +
									Integer.BYTES;

	static void open() {
		Film.file = Filetool.open("film", Film.BYTES);
	}
	static void truncate() {
		Filetool.truncate(Film.file);
	}
	static void close() {
		Filetool.close(Film.file);
	}
	static ArrayList<Film> readAll() {
		ArrayList<Film> films = Filetool.readAllObjects(Film.file, Film::new);
		return films;
	}
	@Override
	void readOneObject() {
		Film.readFilm(this, Film.file);
	}
	static Film readFilm() {
		Film film = new Film();
		Film.readFilm(film, Film.file);
		return film;
	}
	static Film readFilm(RandomAccessFile file) {
		Film film = new Film();
		Film.readFilm(film, file);
		return film;
	}
	static void readFilm(Film film, RandomAccessFile file) {
		try {
			film.setTitle(Filetool.readChars(file, Film.titleMaxLength));
			film.setSummary(Filetool.readChars(file, Film.summaryMaxLength));
			film.setLength(file.readInt());
			film.setClassification(file.readInt());
			film.setValid(file.readBoolean());
			film.setUid(file.readInt());
		} catch (IOException e) {
			e.printStackTrace();
			System.exit(-1);
		}
	}
	static void writeFilm(Film film) {
		Film.writeFilm(film, Film.file);
	}
	static void writeFilm(Film film, RandomAccessFile file) {
		try {
			if( file == Film.file ) {
				file.seek( film.getUid() * Film.BYTES );
			} else {
				// file pointer no movement
			}
			Filetool.writeChars(file, film.getTitle(), Film.titleMaxLength);
			Filetool.writeChars(file, film.getSummary(), Film.summaryMaxLength);
			file.writeInt(film.getLength());
			file.writeInt(film.getClassification());
			file.writeBoolean(film.getValid());
			file.writeInt(film.getUid());
		} catch (IOException e) {
			e.printStackTrace();
			System.out.println("Data written error.");
			System.exit(-1);
		}
	}
	static int findFirstValidPosition() {
		int uid = 0;
		try {
			Film.file.seek(0);
			long cur = 0L;
			while( cur != Film.file.length() ) {
				Film film = Film.readFilm();
				cur = Film.file.getFilePointer();
				if( !film.getValid() ) {
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
	public static Film requireFilm() {
		Film film = new Film();
		Scanner scanner = new Scanner(System.in);
		while ( true ) {
			System.out.print("標題: ");
			try {
				String title = scanner.nextLine();
				film.setTitle(title);
			} catch( NoSuchElementException e1 ) {
				System.out.println(e1.getMessage());
				continue;
			} catch ( IllegalArgumentException e2 ) {
				System.out.println(e2.getMessage());
				continue;
			}
			break;
		}
		while ( true ) {
			System.out.print("大綱: ");
			try {
				String summary = scanner.nextLine();
				film.setSummary(summary);
			} catch( NoSuchElementException e1 ) {
				System.out.println(e1.getMessage());
				continue;
			} catch ( IllegalArgumentException e2 ) {
				System.out.println(e2.getMessage());
				continue;
			}
			break;
		}
		while ( true ) {
			System.out.print("時長 (1~3小時): ");
			try {
				int length = scanner.nextInt();
				scanner.nextLine();
				film.setLength(length);
			} catch ( InputMismatchException e1 ) {
				System.out.println("are you serious?");
				scanner.nextLine();
				continue;
			} catch( NoSuchElementException e2 ) {
				System.out.println(e2.getMessage());
				continue;
			} catch ( IllegalArgumentException e3 ) {
				System.out.println(e3.getMessage());
				continue;
			}
			break;
		}
		while ( true ) {
			System.out.print("分級 ( 0, 9, 12, 15, 18) : ");
			try {
				int classification = scanner.nextInt();
				scanner.nextLine();
				film.setClassification(classification);
			} catch ( InputMismatchException e1 ) {
				System.out.println("are you serious?");
				scanner.nextLine();
				continue;
			} catch( NoSuchElementException e2 ) {
				System.out.println(e2.getMessage());
				continue;
			} catch ( IllegalArgumentException e3 ) {
				System.out.println(e3.getMessage());
				continue;
			}
			break;
		}
		return film;
	}
	public static Film chooseFilm(List<Film> films) {
		Scanner scanner = new Scanner(System.in);
		for(Film film:films){
			System.out.println(film);
		}
        while(true) {
            System.out.print("Choose one film uid: ");
            try {
				int uid = scanner.nextInt();
                scanner.nextLine();
                for(Film film:films){
                    if(film.getUid() == uid){
                        return new Film(film);
                    }
                }
			} catch ( InputMismatchException e1 ) {
				System.out.println("are you serious?");
				scanner.nextLine();
			} catch( NoSuchElementException e1 ) {
				System.out.println(e1.getMessage());
			}
            System.out.print("I think you type the wrobg uid of film: ");
            continue;
        }
    }


	private String title;
	private String summary;
	private int length;
	private int classification;
	private boolean valid;
	private int uid;
	
	public Film() {
		this.setValid(false);
		this.setUid(-1);
	}
	Film(String title, String summary, int length, int classification, boolean valid, int uid)  throws IllegalArgumentException {
		this.setTitle(title);
		this.setSummary(summary);
		this.setLength(length);
		this.setClassification(classification);
		this.setValid(valid);
		this.setUid(uid);
	}
	Film(Film film) {
		this(film.getTitle(), film.getSummary(), film.getLength(), film.getClassification(), film.getValid(), film.getUid());
	}

	public String getTitle() {
		return this.title;
	}
	public String getSummary() {
		return this.summary;
	}
	public int getLength() {
		return this.length;
	}
	public int getClassification() {
		return this.classification;
	}
	public boolean getValid() {
		return this.valid;
	}
	public int getUid() {
		return this.uid;
	}

	void setTitle(String title) throws IllegalArgumentException {
		if( title.length()>Film.titleMaxLength ) throw new IllegalArgumentException("title length should < " + Film.titleMaxLength);
		this.title = title;
	}
	void setSummary(String summary) throws IllegalArgumentException {
		if( summary.length()>Film.summaryMaxLength ) throw new IllegalArgumentException("summary length should < " + Film.summaryMaxLength);
		this.summary = summary;
	}
	void setLength(int length) throws IllegalArgumentException {
		if( length >= 4 ) throw new IllegalArgumentException("Invalid length>=4: " + length);
		if( length <= 0 ) throw new IllegalArgumentException("Invalid length<=0: " + length);
		this.length = length;
	}
	void setClassification(int classification) throws IllegalArgumentException {
		if( classification<0 ) throw new IllegalArgumentException("Invalid classification: " + classification);
		if( classification>18 ) throw new IllegalArgumentException("Invalid classification: " + classification);
		if( classification == 3 ) throw new IllegalArgumentException("Invalid classification: " + classification);
		if( classification == 6 ) throw new IllegalArgumentException("Invalid classification: " + classification);
		if( classification%3>0 ) throw new IllegalArgumentException("Invalid classification: " + classification);
		this.classification = classification;
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

		sb.append(Film.color)
		  .append(indent).append("Film {\n")
		  .append(indent).append("\tuid: ").append(this.uid).append("\n")
		  .append(indent).append("\t標題: ").append(this.title).append("\n")
		  .append(indent).append("\t大綱: ").append(this.summary).append("\n")
		  .append(indent).append("\t時長: ").append(this.length).append("\n")
		  .append(indent).append("\t分級: ").append(this.classification).append("\n")
		  .append(indent).append("\tvalid: ").append(this.valid).append("\n")
		  .append(indent).append("}")
		  .append(Color.RESET);;

		return sb.toString();
	}
	public static void main(String[] args) {
		Film.open();
		Film.truncate();
		if( Helper.getOneCharInput("create film by yourself or by default? [y/d]: ", "yd") == 'd' ){
			Film film0 = new Film("美國隊長：無畏新世界", "Marvel Cinematic Universe", 2, 12, true, 0);
			Film film1 = new Film("機動戰士Gundam GquuuuuuX -Beginning-", "EVA, MSG", 1, 0, true, 1);
			Film film2 = new Film("夜校女生", "Adult's pain", 1, 0, true, 2);
			Film film3 = new Film("史蒂芬金之猴子", "Steven", 1, 18, true, 3);
			Film.writeFilm(film0);
			Film.writeFilm(film1);
			Film.writeFilm(film2);
			Film.writeFilm(film3);
		} else {
			while ( true ) {
				if( Helper.getOneCharInput("create one film? [y/n]: ", "yn") == 'n' ) break;
				Film created = Film.requireFilm();
				created.setUid( Film.findFirstValidPosition() );
				created.setValid( true );
				System.out.println("created = " + created );
				Film.writeFilm(created);
			}
		}
		System.out.println("All data stored in film:");
		for(Film film:Film.readAll()){
			System.out.println(film);
		}
		if( Helper.getOneCharInput("truncate all film? [y/n]: ", "yn") == 'y' ) Film.truncate();
		Film.close();
	}
}