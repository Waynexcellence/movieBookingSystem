package backend;

import java.io.*;
import java.util.*;

import share.Color;
import share.Helper;

public class Ticket extends ReadOneObject implements Serializable {

	private static final long serialVersionUID = 1L;
	private static RandomAccessFile file = null;
	static final String color = Color.ANSI + Color.Fore[6] + Color.ANSI + Color.Back[0] + Color.BOLD;
	public static final long BYTES = Integer.BYTES +
								    Integer.BYTES +
									Seat.BYTES +
									Byte.BYTES +
									Integer.BYTES;
    static void open() {
		Ticket.file = Filetool.open("ticket", Ticket.BYTES);
	}
	static void truncate() {
		Filetool.truncate(Ticket.file);
	}
	static void close() {
		Filetool.close(Ticket.file);
	}
	static ArrayList<Ticket> readAll() {
		ArrayList<Ticket> tickets = Filetool.readAllObjects(Ticket.file, Ticket::new);
		return tickets;
	}
    @Override
	void readOneObject() {
		Ticket.readTicket(this, Ticket.file);
	}
	static Ticket readTicket() {
		Ticket ticket = new Ticket();
		Ticket.readTicket(ticket, Ticket.file);
		return ticket;
	}
    static Ticket readTicket(RandomAccessFile file) {
		Ticket ticket = new Ticket();
		Ticket.readTicket(ticket, file);
		return ticket;
	}
	static void readTicket(Ticket ticket, RandomAccessFile file) {
		try {
			ticket.setMovieId(file.readInt());
			ticket.setUserId(file.readInt());
			ticket.setSeat(Seat.readSeat(file));
			ticket.setValid(file.readBoolean());
			ticket.setUid(file.readInt());
		} catch (IOException e) {
			e.printStackTrace();
			System.exit(-1);
		}
	}
    static void writeTicket(Ticket ticket) {
		Ticket.writeTicket(ticket, Ticket.file);
	}
	static void writeTicket(Ticket ticket, RandomAccessFile file) {
		try {
			if( file == Ticket.file ) {
				file.seek( ticket.getUid() * Ticket.BYTES );
			} else {
				// file pointer no movement
			}
			file.writeInt(ticket.getMovieId());
			file.writeInt(ticket.getUserId());
			Seat.writeSeat(file, ticket.getSeat());
			file.writeBoolean(ticket.getValid());
			file.writeInt(ticket.getUid());
		} catch (IOException e) {
			e.printStackTrace();
			System.out.println("Data written error.");
			System.exit(-1);
		}
	}
    static int findFirstValidPosition() {
		int uid = 0;
		try {
			Ticket.file.seek(0);
			long cur = 0L;
			while( cur != Ticket.file.length() ) {
				Ticket ticket = Ticket.readTicket();
				cur = Ticket.file.getFilePointer();
				if( !ticket.getValid() ) {
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
	static boolean iscollidedTicket(Ticket ticket) {
		ArrayList<Ticket> tickets = Ticket.readAll();
		for( Ticket existed: tickets) {
			if( !existed.getValid() ) continue;
			if( existed.getMovieId() != ticket.getMovieId() ) continue;
			if( existed.getSeat().equals(ticket.getSeat()) ){
				return true;
			}
		}
		return false;
	}
	static User requireUser(List<User> users) {
		Ticket ticket = new Ticket();
		Scanner scanner = new Scanner(System.in);
		while ( true ) {
			for (User user:users) {
				System.out.println(user);
			}
            System.out.print("choose one user uid to buy: ");
			try {
				int userId = scanner.nextInt();
				scanner.nextLine();
				for(User user:users){
                    if(user.getUid() == userId){
                        ticket.setUserId(userId);
                        return new User(user);
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
			System.out.print("I think you type the wrong uid of user: ");
			continue;
        }
	} 
	static Ticket requireTicket() {
		ArrayList<Movie> movies = Movie.readAll();
		ArrayList<User> users = User.readAll();
		User user = Ticket.requireUser(users);
		Movie movie = Movie.chooseMovie(movies);
		Ticket ticket = Ticket.requireTicket(user, movie, Ticket.readAll());
		ticket.setUserId(user.getUid());
		return ticket;
	}
	public static Ticket requireTicket(User user, Movie movie, List<Ticket> tickets) {
		Ticket ticket = new Ticket();
		ticket.setMovieId(movie.getUid());
		movie.showTheater(user, tickets);
		// use for loop if need buying multiple ticket in one time
		Seat seat = Seat.requireSeat(movie.getTheater().getSeatAmount());
		ticket.setSeat(seat);
		return ticket;
	}
	public static Ticket chooseTicket(List<Ticket> tickets) {
		Scanner scanner = new Scanner(System.in);
		for (Ticket ticket:tickets) {
			System.out.println(ticket);
		}
		while ( true ) {
            System.out.print("choose one ticket uid: ");
			try {
				int ticketId = scanner.nextInt();
				scanner.nextLine();
				for(Ticket ticket:tickets){
                    if(ticket.getUid() == ticketId){
                        return new Ticket(ticket);
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
			System.out.println("I think you type the wrong uid of ticket: ");
			continue;
        }
	}

	private int movieId;
	private int userId;
	private Seat seat;
	private boolean valid;
	private int uid;

	public Ticket() {
		this.setMovieId(-1);
		this.setUserId(-1);
		this.seat = new Seat();
		this.setValid(false);
        this.setUid(-1);
	}
	Ticket (int movieId, int userId, Seat seat, boolean valid, int uid) {
		this.setMovieId(movieId);
		this.setUserId(userId);
		this.setSeat(seat);
		this.setValid(valid);
		this.setUid(uid);
	}
	public Ticket(Ticket ticket) {
		this(ticket.getMovieId(), ticket.getUserId(), ticket.getSeat(), ticket.getValid(), ticket.getUid());
	}

    public int getMovieId() {
        return this.movieId;
    }
    public int getUserId() {
        return this.userId;
    }
	public Seat getSeat() {
		return new Seat(this.seat);
	}
    public boolean getValid() {
        return this.valid;
    }
    public int getUid() {
        return this.uid;
    }
    void setMovieId(int movieId) {
        this.movieId = movieId;
    }
	public void setMovieId(Movie movie) {
		if( movie.getValid() ) {
			this.movieId = movie.getUid();
		}
	}
    void setUserId(int userId) {
        this.userId = userId;
    }
	public void setUserId(User user) {
		if( user.getValid() ) {
			this.userId = user.getUid();
		}
	}
	void setSeat(Seat seat) {
		this.seat = new Seat(seat);
	}
    void setValid(boolean valid) {
        this.valid = valid;
    }
    void setUid(int uid) {
        this.uid = uid;
    }


	@Override
    public boolean equals(Object obj) {
        if (obj == this) return true;
        if (obj == null || this.getClass() != obj.getClass()) {
            return false;
        }
        Ticket other = (Ticket) obj;
        return this.movieId == other.movieId && this.seat.equals(other.seat);
    }
	public String toString() {
		return this.toString(0);
	}
	public String toString(int level) {
		StringBuilder sb = new StringBuilder();
		String indent = "\t".repeat(level);

		sb.append(Ticket.color)
		  .append(indent).append("Ticket {\n")
		  .append(indent).append("\tuid: ").append(this.uid).append("\n")
		  .append(Movie.color)
		  .append(indent).append("\tmovieId: ").append(this.movieId).append("\n")
		  .append(User.color)
		  .append(indent).append("\tuserId: ").append(this.userId).append("\n")
		  .append(Color.RESET)
		  .append(indent).append("").append(this.seat.toString(level+1)).append("\n")
		  .append(Ticket.color)
		  .append(indent).append("\tvalid: ").append(this.valid).append("\n")
		  .append(indent).append("}")
		  .append(Color.RESET);

		return sb.toString();
	}
	public String toString(Movie movie) {
		return this.toString(0, movie);
	}
	public String toString(int level, Movie movie) {
		if( this.getMovieId()!=movie.getUid() ) {
			return "the ticket's movieId and movie's uid are different.";
		}
		StringBuilder sb = new StringBuilder();
		String indent = "\t".repeat(level);

		sb.append(Ticket.color)
		  .append(indent).append("Ticket {\n")
		  .append(indent).append("\tuid: ").append(this.uid).append("\n")
		  .append(Movie.color)
		  .append(indent).append("").append(movie.toString(level+1)).append("\n")
		  .append(User.color)
		  .append(indent).append("\tuserId: ").append(this.userId).append("\n")
		  .append(Color.RESET)
		  .append(indent).append("").append(this.seat.toString(level+1)).append("\n")
		  .append(Ticket.color)
		  .append(indent).append("\tvalid: ").append(this.valid).append("\n")
		  .append(indent).append("}")
		  .append(Color.RESET);

		return sb.toString();
	}
	public static void main(String[] args) {
		User.open();
		Movie.open();
		Ticket.open();
		Ticket.truncate();
		ArrayList<User> users = User.readAll();
		ArrayList<Movie> movies = Movie.readAll();
		System.out.println("All data stored in user:");
		for(User user: users) {
			System.out.println(user);
		}
		System.out.println("All data stored in movie:");
		for(Movie movie: movies) {
			System.out.println(movie);
		}
		if( Helper.getOneCharInput("create ticket by yourself or by default? [y/d]: ", "yd") == 'd' ){
			if( users.size() < 2 ){
				System.out.println("users data is not enough. please execute \"java backend.User\"");
				System.exit(-1);
			}
			if( movies.size() < 2 ){
				System.out.println("movies data is not enough. please execute \"java backend.Movie\"");
				System.exit(-1);
			}
			Ticket ticket0 = new Ticket(movies.get(0).getUid(), users.get(0).getUid(), new Seat('L', 12, Color.Back[3]), true, 0);
			Ticket ticket1 = new Ticket(movies.get(0).getUid(), users.get(1).getUid(), new Seat('L', 13, Color.Back[3]), true, 1);
			Ticket ticket2 = new Ticket(movies.get(1).getUid(), users.get(0).getUid(), new Seat('I', 15, Color.Back[7]), true, 2);
			Ticket ticket3 = new Ticket(movies.get(1).getUid(), users.get(1).getUid(), new Seat('I', 16, Color.Back[7]), true, 3);
			Ticket.writeTicket(ticket0);
			Ticket.writeTicket(ticket1);
			Ticket.writeTicket(ticket2);
			Ticket.writeTicket(ticket3);
		} else {
			while ( true ) {
				if( Helper.getOneCharInput("create one ticket? [y/n]: ", "yn") == 'n' ) break;
				Ticket created = Ticket.requireTicket();
				created.setUid( Ticket.findFirstValidPosition() );
				created.setValid( true );
				System.out.println("created = " + created );
				Ticket.writeTicket(created);
			}
		}
		System.out.println("All data stored in ticket:");
		for(Ticket ticket:Ticket.readAll()){
			System.out.println(ticket.toString(Movie.readMovie(ticket.getMovieId())));
		}
		if( Helper.getOneCharInput("truncate all ticket? [y/n]: ", "yn") == 'y' ) Ticket.truncate();
		Ticket.close();
	}
}