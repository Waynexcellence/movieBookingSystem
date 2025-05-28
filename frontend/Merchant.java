package frontend;

import java.io.*;
import java.util.*;
import java.net.*;

import backend.Date;
import backend.User;
import backend.Film;
import backend.Movie;
import backend.Ticket;
import backend.Theater;
import backend.Server;

import share.Role;
import share.Action;
import share.Conversation;
import share.Helper;

public class Merchant {
	private static Socket socket = null;
	private static Role role = Role.Merchant;

	public static void connect() {
		try {
			Merchant.socket = new Socket(Conversation.localhost, Conversation.port);
		} catch ( Exception e ) {
			e.printStackTrace();
			System.exit(-1);
		}
	}
	public static void send(Action action, List<Object> dataList) {
		try {
			ObjectOutputStream outputStream = new ObjectOutputStream(Merchant.socket.getOutputStream());
			Conversation conversation = new Conversation(Merchant.role, action, (List<Object>) dataList);
			outputStream.writeObject(conversation);
			outputStream.flush();
		} catch (Exception e) {
			e.printStackTrace();
			System.exit(-1);
		}
	}
	public static List<Object> receive() {
		List<Object> objects = new ArrayList<>();
		try {
			ObjectInputStream inputStream = new ObjectInputStream(Merchant.socket.getInputStream());
			Conversation conversation = (Conversation) inputStream.readObject();
			objects.addAll(conversation.data);
		} catch (Exception e) {
			e.printStackTrace();
			System.exit(-1);
		}
		return objects;
	}
	
	private static List<Film> browseFilm() {
		Merchant.send(Action.Browse, List.of(new Film()));
		List<Object> response = Merchant.receive();
		List<Film> films = new ArrayList<>();
		for(Object object: response) {
			if( object instanceof Film ){
				Film film = (Film) object;
				films.add(film);
			}
			else if (object instanceof String ) {
				System.out.println(Helper.longLine);
				System.out.println("error: " + object);
				System.out.println(Helper.longLine);
			}
			else System.out.println("unknown class received from server.");
		}
		return films;
	}
	private static List<Theater> browseTheater() {
		Merchant.send(Action.Browse, List.of(new Theater()));
		List<Object> response = Merchant.receive();
		List<Theater> theaters = new ArrayList<>();
		for(Object object: response) {
			if( object instanceof Theater ){
				Theater theater = (Theater) object;
				theaters.add(theater);
			}
			else if (object instanceof String ) {
				System.out.println(Helper.longLine);
				System.out.println("error: " + object);
				System.out.println(Helper.longLine);
			}
			else System.out.println("unknown class received from server.");
		}
		return theaters;
	}
	private static List<Movie> browseMovie() {
		Merchant.send(Action.Browse, List.of(new Movie()));
		List<Object> response = Merchant.receive();
		List<Movie> movies = new ArrayList<>();
		for(Object object: response) {
			if( object instanceof Movie ){
				Movie movie = (Movie) object;
				movies.add(movie);
			}
			else if (object instanceof String ) {
				System.out.println(Helper.longLine);
				System.out.println("error: " + object);
				System.out.println(Helper.longLine);
			}
			else System.out.println("unknown class received from server.");
		}
		return movies;
	}
	private static List<Ticket> browseTicket() {
		Merchant.send(Action.Browse, List.of(new Ticket()));
		List<Object> response = Merchant.receive();
		List<Ticket> tickets = new ArrayList<>();
		for(Object object: response) {
			if( object instanceof Ticket ){
				Ticket ticket = (Ticket) object;
				tickets.add(ticket);
			}
			else if (object instanceof String ) {
				System.out.println(Helper.longLine);
				System.out.println("error: " + object);
				System.out.println(Helper.longLine);
			}
			else System.out.println("unknown class received from server.");
		}
		return tickets;
	}
	private static List<Ticket> browseTicket(Movie movie) {
		if( movie==null || movie.getUid()<0 || !movie.getValid() ){
			System.out.println("You can't see the tickets of invalid movie.");
			return new ArrayList<>();
		}
		List<Ticket> tickets = new ArrayList<>();
		for(Ticket ticket: Merchant.browseTicket()) {
			if( ticket.getMovieId()==movie.getUid() ){
				tickets.add(ticket);
			}
		}
		return tickets;
	}
	private static void browseTheaterInMovie() {						// display the specific movie status
		List<Movie> movies = Merchant.browseMovie();
		Movie movie = Movie.chooseMovie(movies);
		List<Ticket> tickets = Merchant.browseTicket(movie);
		movie.showTheater(tickets);
	}
	private static Film createFilm() {									// return null if fail
		Film film = Film.requireFilm();
		Merchant.send(Action.Create, List.of(film));
		Object object = Merchant.receive().get(0);
		if( object instanceof Film ){
			return (Film) object;
		}
		else if (object instanceof String ) {
			System.out.println(Helper.longLine);
			System.out.println("error: " + object);
			System.out.println(Helper.longLine);
		}
		else System.out.println("unknown class received from server.");
		return null;
	}
	private static Theater createTheater() {							// return null if fail
		Theater theater = Theater.requireTheater();
		Merchant.send(Action.Create, List.of(theater));
		Object object = Merchant.receive().get(0);
		if( object instanceof Theater ){
			return (Theater) object;
		}
		else if (object instanceof String ) {
			System.out.println(Helper.longLine);
			System.out.println("error: " + object);
			System.out.println(Helper.longLine);
		}
		else System.out.println("unknown class received from server.");
		return null;
	}
	private static Movie createMovie() {								// return null if failed
		List<Film> films = Merchant.browseFilm();
		List<Theater> theaters = Merchant.browseTheater();
		Movie movie = Movie.requireMovie(films, theaters);
		Merchant.send(Action.Create, List.of(movie));
		Object object = Merchant.receive().get(0);
		if( object instanceof Movie ){
			return (Movie) object;
		}
		else if (object instanceof String ) {
			System.out.println(Helper.longLine);
			System.out.println("error: " + object);
			System.out.println(Helper.longLine);
		}
		else System.out.println("unknown class received from server.");
		return null;
	}
	private static Movie updateMovie() {								// return original if failed
		List<Movie> movies = Merchant.browseMovie();
		Movie movie = Movie.chooseMovie(movies);
		Movie origin = new Movie(movie);
		while (true) {
			System.out.println("current Movie:\n" + movie );
			char modify = Helper.getOneCharInput("which value you want to update? [date(d)/time(t)/finish(f)/quit(q)]", "dtfq");
			try {
				if( modify == 'd' ) {
					Date date = Date.requireDate("new movie date");
					movie.setDate(date);
				} else if ( modify == 't' ) {
					int time = Movie.requireTime("new movie start play time");
					movie.setTime(time);
				} else if ( modify == 'f' ) {
					break;
				} else if ( modify == 'q' ) {
					System.out.println("nothing happens");
					movie = origin;
					return movie;
				}
			} catch ( IllegalArgumentException e ) {
				System.out.println(e.getMessage());
			}
		}
		Merchant.send(Action.Update, List.of(movie));
		Object object = Merchant.receive().get(0);
		if( object instanceof Movie ) {
			return (Movie) object;
		} else if ( object instanceof String ) {
			System.out.println(Helper.longLine);
			System.out.println("error: " + object);
			System.out.println(Helper.longLine);
		}
		movie = origin;
		return movie;
	}
	private static Movie deleteMovie() {								// return new Movie() if failed
		List<Movie> movies = Merchant.browseMovie();
		if( movies.size() == 0 ) {
			System.out.println("you don't have any movie that can be deleted.");
			return new Movie();
		}
		Movie movie = Movie.chooseMovie(movies);
		System.out.println("make sure that you want to delete the Movie:\n" + movie );
		char confirm = Helper.getOneCharInput("Confirm? [y/n]: ", "yn");
		if( confirm == 'y' ) {
			Merchant.send(Action.Delete, List.of(movie));
			Object object = Merchant.receive().get(0);
			if( object instanceof Movie ) {
				return (Movie) object;
			} else if ( object instanceof String ) {
				System.out.println(Helper.longLine);
				System.out.println("error: " + object);
				System.out.println(Helper.longLine);
			} else {
				System.out.println("unknown class received from server.");
			}
		} else {
			System.out.println("nothing happens.");			
		}
		return new Movie();
	}

	public static void main(String[] args) {

		Merchant.connect();
		System.out.println("歡迎來到電影院訂票系統");
		while (true) {
			StringBuilder sb = new StringBuilder();
			sb.append("You can\n")
				.append("\t(0) watch all valid films\n")
				.append("\t(1) watch all valid theaters\n")
				.append("\t(2) watch all valid movies\n")
				.append("\t(3) watch all valid tickets\n")
				.append("\t\n")
				.append("\t(4) update a valid movie\n")
				.append("\t(5) display a valid movie status\n")
				.append("\t(6) delete a valid movie\n")
				.append("\t\n")
				.append("\t(7) create a film\n")
				.append("\t(8) create a theater\n")
				.append("\t(9) create a movie\n")
				.append("\t(e) exit\n");
			char action = Helper.getOneCharInput(sb.toString()+"\t: ", "0123456789e");
			if( action == '0' ) {
				List<Film> films = Merchant.browseFilm();
				System.out.println(Helper.longLine);
				for( Film film:films ) {
					System.out.println(film);
				}
				if( films.size() == 0 ) {
					System.out.println("there is no any film you search about.");
				}
				System.out.println(Helper.longLine);
			}
			if( action == '1' ) {
				List<Theater> theaters = Merchant.browseTheater();
				System.out.println(Helper.longLine);
				for( Theater theater:theaters ) {
					System.out.println(theater);
				}
				if( theaters.size() == 0 ) {
					System.out.println("there is no any theater you search about.");
				}
				System.out.println(Helper.longLine);
			}
			if( action == '2' ) {
				List<Movie> movies = Merchant.browseMovie();
				System.out.println(Helper.longLine);
				for( Movie movie:movies ) {
					System.out.println(movie);
				}
				if( movies.size() == 0 ) {
					System.out.println("there is no any movie you search about.");
				}
				System.out.println(Helper.longLine);
			}
			if( action == '3' ) {
				List<Ticket> tickets = Merchant.browseTicket();
				System.out.println(Helper.longLine);
				for( Ticket ticket:tickets ) {
					System.out.println(ticket);
				}
				if( tickets.size() == 0 ) {
					System.out.println("there is no any ticket you search about.");
				}
				System.out.println(Helper.longLine);
			}
			if( action == '4' ) {
				Movie movie = Merchant.updateMovie();
				System.out.println("This is your updated movie:\n" + movie );
			}
			if( action == '5' ) {
				Merchant.browseTheaterInMovie();
			}
			if( action == '6' ) {
				Movie movie = Merchant.deleteMovie();
				if( movie.getUid()>=0 ){
					System.out.println("This is your deleted ticket:\n" + movie);
				}
			}
			if( action == '7' ) {
				Film film = Merchant.createFilm();
				if( film.getValid() ) System.out.println("This is your new film:\n" + film);
			}
			if( action == '8' ) {
				Theater theater = Merchant.createTheater();
				if( theater.getValid() ) System.out.println("This is your new theater:\n" + theater);
			}
			if( action == '9' ) {
				Movie movie = Merchant.createMovie();
				if( movie.getValid() ) System.out.println("This is your new movie:\n" + movie);
			}
			if( action == 'e' ) {
				break;
			}
		}
	}
}