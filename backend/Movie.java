package backend;

import java.io.*;
import java.util.*;

import share.Color;
import share.Helper;

public class Movie extends ReadOneObject implements Serializable {

	private static final long serialVersionUID = 1L;
	private static RandomAccessFile file = null;
	static final String color = Color.ANSI + Color.Fore[2] + Color.ANSI + Color.Back[0] + Color.BOLD;
    public static final int smallSeatAmount = 144;
	public static final int largeSeatAmount = 407;
	public static final int maxSeatAmount = 407;
	public static final long BYTES = Film.BYTES +
                                    Theater.BYTES +
                                    Date.BYTES +
                                    Integer.BYTES +
                                    Byte.BYTES +
                                    Integer.BYTES;

	private static ArrayList<ArrayList<String>> largeTheater = new ArrayList<>();
	private static ArrayList<ArrayList<String>> smallTheater = new ArrayList<>();
	static {
		Seat testSeat = new Seat(Movie.largeSeatAmount);
		for(char row='A';row<='M';row++){
			largeTheater.add(new ArrayList<String>());
			for(int col=1;col<=39;col++){
				try {
					testSeat.setRow(row);
					testSeat.setCol(col);
					testSeat.setRegion();
					largeTheater.get(row - 'A').add(testSeat.getRegion());
				} catch ( IllegalArgumentException e ) {
					largeTheater.get(row - 'A').add(Color.Back[0]);
				}
			}
		}
	}
	static {
		for(char row='A';row<='I';row++){
			smallTheater.add(new ArrayList<String>());
			for(int col=1;col<=16;col++){
				smallTheater.get(row - 'A').add(Color.Back[7]);
			}
		}
	}

	static void open() {
		Movie.file = Filetool.open("movie", Movie.BYTES);
	}
	static void truncate() {
		Filetool.truncate(Movie.file);
	}
	static void close() {
		Filetool.close(Movie.file);
	}
	static ArrayList<Movie> readAll() {
		ArrayList<Movie> movies = Filetool.readAllObjects(Movie.file, Movie::new);
		return movies;
	}
    @Override
	void readOneObject() {
		Movie.readMovie(this, Movie.file);
	}
    static Movie readMovie() {
		Movie movie = new Movie();
		Movie.readMovie(movie, Movie.file);
		return movie;
	}
    static Movie readMovie(int uid) {
        Movie movie = new Movie();
        try {
            Movie.file.seek(uid * Movie.BYTES);
        } catch ( IOException e ) {
            System.out.println("Movie.file.seek error");
        }
		Movie.readMovie(movie, Movie.file);
		return movie;
    }
    static void readMovie(Movie movie, RandomAccessFile file) {
		try {
            movie.setFilm(Film.readFilm(file));
            movie.setTheater(Theater.readTheater(file));
            movie.setDate(Date.readDate(file));
            movie.setTime(file.readInt());
            movie.setValid(file.readBoolean());
            movie.setUid(file.readInt());
		} catch (IOException e) {
			e.printStackTrace();
			System.exit(-1);
		}
	}
    static void writeMovie(Movie movie) {
		Movie.writeMovie(movie, Movie.file);
	}
    static void writeMovie(Movie movie, RandomAccessFile file) {
		try {
			file.seek(movie.getUid() * Movie.BYTES );
            Film.writeFilm(movie.getFilm(), file);
            Theater.writeTheater(movie.getTheater(), file);
            Date.writeDate(file, movie.getDate());
            file.writeInt(movie.getTime());
            file.writeBoolean(movie.valid);
            file.writeInt(movie.uid);
		} catch (IOException e) {
			e.printStackTrace();
			System.out.println("Data written error.");
			System.exit(-1);
		}
	}
    static int findFirstValidPosition() {
        int uid = 0;
		try {
			Movie.file.seek(0);
			long cur = 0L;
			while( cur != Movie.file.length() ) {
				Movie movie = Movie.readMovie();
				cur = Movie.file.getFilePointer();
				if( !movie.getValid() ) {
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
    static boolean iscollidedTime(Movie movie) {
		ArrayList<Movie> movies = Movie.readAll();
        int movieStart = movie.getTime();
        int movieCease = movieStart + movie.getFilm().getLength();
		for(Movie existed: movies) {
            if( !existed.getValid() ) continue;
            if( existed.getUid() == movie.getUid() ) continue;
            if( existed.getTheater().getUid()!=movie.getTheater().getUid() ) continue;
			if( existed.getDate().equals(movie.getDate()) ){
                int otherStart = existed.getTime();
                int otherCease = otherStart + existed.getFilm().getLength();
                if( (otherCease>movieStart&&otherStart<movieCease) || (otherStart<movieCease&&otherCease>movieStart) ) {
                    return true;
                }
            }
		}
		return false;
	}
    public static int requireTime(String forWhat) {
        Movie movie = new Movie();
		Scanner scanner = new Scanner(System.in);
        while ( true ) {
			System.out.print("請輸入 " + forWhat + ": ");
			try {
				int time = scanner.nextInt();
				scanner.nextLine();
				movie.setTime(time);
                return time;
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
    private static Movie requireMovie() {
        ArrayList<Film> films = Film.readAll();
        ArrayList<Theater> theaters = Theater.readAll();
        Movie movie = Movie.requireMovie(films, theaters);
        return movie;
    }
    public static Movie requireMovie(List<Film> films, List<Theater> theaters) {
        Movie movie = new Movie();
		Scanner scanner = new Scanner(System.in);
        Film film = Film.chooseFilm(films);
        movie.setFilm(film);
        Theater theater = Theater.chooseTheater(theaters);
        movie.setTheater(theater);
        Date date = Date.requireDate("電影上映日期");
        movie.setDate(date);
        int time = Movie.requireTime("電影播放時間");
        movie.setTime(time);
        return movie;
    }
	public static Movie chooseMovie(List<Movie> movies) {
		Scanner scanner = new Scanner(System.in);
		for (Movie movie:movies) {
			if( movie.getValid() ) System.out.println(movie);
		}
		while ( true ) {
            System.out.print("choose one movie uid: ");
			try {
				int movieId = scanner.nextInt();
				scanner.nextLine();
				for(Movie movie:movies){
                    if( movie.getValid() && movie.getUid() == movieId ){
                        return new Movie(movie);
                    }
                }
			} catch ( InputMismatchException e1 ) {
				System.out.println("are you serious?");
				scanner.nextLine();
			} catch( NoSuchElementException e2 ) {
				System.out.println(e2.getMessage());
			} catch ( IllegalArgumentException e3 ) {
				System.out.println(e3.getMessage());
			}
			System.out.println("I think you type the wrong uid of movie: ");
			continue;
        }
	}

    private Film film;
    private Theater theater;
    private Date date;
    private int time;
    private boolean valid;
    private int uid;

    public Movie() {
        this.setValid(false);
        this.setUid(-1);
    }
    Movie(Film film, Theater theater, Date date, int time, boolean valid, int uid) throws IllegalArgumentException {
        this.setFilm(film);
        this.setTheater(theater);
        this.setDate(date);
        this.setTime(time);
        this.setValid(valid);
        this.setUid(uid);
    }
    public Movie(Movie movie) {
        this(movie.getFilm(), movie.getTheater(), movie.getDate(), movie.getTime(), movie.getValid(), movie.getUid());
    }

    public Film getFilm() {
        return new Film(this.film);
    }
    public Theater getTheater() {
        return new Theater(this.theater);
    }
    public Date getDate() {
        return new Date(this.date);
    }
    public int getTime() {
        return this.time;
    }
    public boolean getValid() {
        return this.valid;
    }
    public int getUid() {
        return this.uid;
    }

    void setFilm(Film film) {
        this.film = new Film(film);
    }
    void setTheater(Theater theater) {
        this.theater = new Theater(theater);
    }
    public void setDate(Date date) {
        this.date = new Date(date);
    }
    public void setTime(int time) throws IllegalArgumentException {
        if( time > 22 ) throw new IllegalArgumentException("這裡沒有 22:00 以後的電影");
        if( time < 3 ) throw new IllegalArgumentException("這裡沒有 03:00 以前的電影");
        this.time = time;
    }
    void setValid(boolean valid) {
        this.valid = valid;
    }
    void setUid(int uid) {
        this.uid = uid;
    }

    ArrayList<Ticket> getTicketByMovie() {
        ArrayList<Ticket> existed = Ticket.readAll();
        ArrayList<Ticket> tickets = new ArrayList<Ticket>();
        for( Ticket ticket:existed ){
            if( ticket.getMovieId() == this.uid && ticket.getValid() ) {
                tickets.add(ticket);
            }
        }
        return tickets;
    }
	public void showTheater() {
		this.showTheater(null, null);
	}
	public void showTheater(List<Ticket> tickets) {
		this.showTheater(null, tickets);
	}
	public void showTheater(User user, List<Ticket> tickets) {
		int rowAmount = this.getTheater().getRowAmount();
		int colAmount = this.getTheater().getColAmount();
		ArrayList<ArrayList<String>> specificTheater = (this.getTheater().getSeatAmount() == Movie.largeSeatAmount)? largeTheater : smallTheater;

		ArrayList<ArrayList<Character>> seats2D = new ArrayList<>();
		for (int i = 0; i < rowAmount; i++) {
			seats2D.add(new ArrayList<>(Collections.nCopies(colAmount, '.')));
		}
		if (tickets != null) {
			for (Ticket ticket : tickets) {
				if( ticket.getMovieId()!=this.getUid() ) continue;
				char mark = (user==null)? 'X':((ticket.getUserId()==user.getUid())?'O':'X');
				int row = ticket.getSeat().getRow() - 'A';
				int col = ticket.getSeat().getCol() - 1; // Assuming 1-based column
				seats2D.get(row).set(col, mark);
			}
		}

		// Display layout
		String indent = "  ";
		if (this.getTheater().getSeatAmount() == Movie.largeSeatAmount) {
			System.out.println(indent + "         111111111122222222223333333333");
			System.out.println(indent + "123456789012345678901234567890123456789");
		} else {
			System.out.println(indent + "            11  1111");
			System.out.println(indent + "1234  56789012  3456");
		}
		for (int i = 0; i < rowAmount; i++) {
			char rowChar = (char) ('A' + i);
			System.out.printf("%c ", rowChar);
			for (int j = 0; j < colAmount; j++) {
				String bg = specificTheater.get(i).get(j);
				char mark = seats2D.get(i).get(j);
				System.out.print(Color.ANSI + bg + Color.ANSI + Color.Fore[0] + mark + Color.RESET);
				if( specificTheater==smallTheater && (j==3||j==11) ) System.out.print("  ");
			}
			System.out.printf(" %c\n", rowChar);
		}
	}

    public String toString() {
		return this.toString(0);
	}
	public String toString(int level) {
		StringBuilder sb = new StringBuilder();
		String indent = "\t".repeat(level);

		sb.append(Movie.color)
		  .append(indent).append("Movie {\n")
		  .append(indent).append("\tuid: ").append(this.uid).append("\n")
		  .append(indent).append(this.film.toString(level+1)).append("\n")
		  .append(Theater.color)
		  .append(indent).append(this.theater.toString(level+1)).append("\n")
		  .append(Date.color)
		  .append(indent).append(this.date.toString(level+1)).append("\n")
		  .append(Movie.color)
		  .append(indent).append("\t播放時間: ").append(this.time+":00").append("\n")
		  .append(indent).append("\tvalid: ").append(this.valid).append("\n")
		  .append(indent).append("}")
		  .append(Color.RESET);

		return sb.toString();
	}
    public static void main(String[] args) {
        Film.open();
        Theater.open();
		Movie.open();
		Ticket.open();
        Movie.truncate();
		ArrayList<Film> films = Film.readAll();
		ArrayList<Theater> theaters = Theater.readAll();
		System.out.println("All data stored in film:");
		for(Film film: films) {
			System.out.println(film);
		}
		System.out.println("All data stored in theater:");
		for(Theater theater: theaters) {
			System.out.println(theater);
		}
		if( Helper.getOneCharInput("create movie by yourself or by default? [y/d]: ", "yd") == 'd' ){
			if( films.size() < 2 ){
				System.out.println("films data is not enough. please execute \"java backend.Film\"");
				System.exit(-1);
			}
			if( theaters.size() < 2 ){
				System.out.println("theaters data is not enough. please execute \"java backend.Theater\"");
				System.exit(-1);
			}
			Movie movie0 = new Movie(films.get(0), theaters.get(0), new Date(2025, 1, 1), 10, true, 0);
			Movie movie1 = new Movie(films.get(1), theaters.get(1), new Date(2025, 2, 2), 11, true, 1);
			Movie.writeMovie(movie0);
			Movie.writeMovie(movie1);
		} else {
			while ( true ) {
				if( Helper.getOneCharInput("create one movie? [y/n]: ", "yn") == 'n' ) break;
				Movie created = Movie.requireMovie();
				created.setUid( Movie.findFirstValidPosition() );
				created.setValid( true );
				System.out.println("created = " + created );
				Movie.writeMovie(created);
			}
		}
		System.out.println("All data stored in movie:");
		for(Movie movie:Movie.readAll()){
			System.out.println(movie);
			movie.showTheater(null, movie.getTicketByMovie());
		}
		if( Helper.getOneCharInput("truncate all movie? [y/n]: ", "yn") == 'y' ) Movie.truncate();
		Movie.close();
	}

}
