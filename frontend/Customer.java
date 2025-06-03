package frontend;

import java.io.*;
import java.util.*;
import java.net.*;

import backend.User;
import backend.Date;
import backend.Movie;
import backend.Ticket;
import backend.Seat;
import backend.Theater;
import backend.Server;

import share.Role;
import share.Action;
import share.Conversation;
import share.Helper;

public class Customer {
	private static Socket socket = null;
	private static Role role = Role.Customer;

	public static void connect() {
		try {
			Customer.socket = new Socket(Helper.getIPAddress(), Conversation.port);
		} catch ( Exception e ) {
			e.printStackTrace();
			System.exit(-1);
		}
	}
	public static void send(Action action, List<Object> dataList) {
		try {
			ObjectOutputStream outputStream = new ObjectOutputStream(Customer.socket.getOutputStream());
			Conversation conversation = new Conversation(Customer.role, action, (List<Object>) dataList);
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
			ObjectInputStream inputStream = new ObjectInputStream(Customer.socket.getInputStream());
			Conversation conversation = (Conversation) inputStream.readObject();
			objects.addAll(conversation.data);
		} catch (Exception e) {
			e.printStackTrace();
			System.exit(-1);
		}
		return objects;
	}

	private static void createUser() {									// create user = register user
		User user = User.requireUser();
		Customer.send(Action.Create, List.of(user));
		Object object = Customer.receive().get(0);
		if( object instanceof User ){
			System.out.println("Create success:\n" + object);
		} else if (object instanceof String ) {
			System.out.println(Helper.longLine);
			System.out.println("error: " + object);
			System.out.println(Helper.longLine);
		}
		else System.out.println("unknown class received from server.");
	}
	private static User browseUser() {									// browse user = login user, return invalid user if failed
		while ( true ) {
			System.out.println("Login: ");
			User attempt = User.requireLogin();
			Customer.send(Action.Browse, List.of(attempt));
			Object object= Customer.receive().get(0);
			if( object instanceof User ){
				return (User) object;
			} else if (object instanceof String ) {
				System.out.println(Helper.longLine);
				System.out.println("error: " + object);
				System.out.println(Helper.longLine);
				Scanner scanner = new Scanner(System.in);
				char c = Helper.getOneCharInput("try again? [y/n]: ", "yn");
				if( c == 'y' ) continue;
				else break;
			} else {
				System.out.println("unknown class received from server.");
				break;
			}
		}
		return new User();
	}
	private static User updateUser(User user) {							// return original one if failed or customer quit
		if( user==null || user.getUid()<0 || !user.getValid() ){
			System.out.println("You have not login.");
			return new User();
		}
		User origin = new User(user);
		Scanner scanner = new Scanner(System.in);
		while (true) {
			System.out.println("current User:\n" + user );
			char modify = Helper.getOneCharInput("which value you want to update? [password(p)/mail(m)/finish(f)/quit(q)]", "pmfq");
			try {
				if( modify == 'p' ) {
					String password = User.requirePassword();
					user.setPassword(password);
				} else if ( modify == 'm' ) {
					String mail = User.requireMail();
					user.setMail(mail);
				} else if ( modify == 'f' ) {
					break;
				} else if ( modify == 'q' ) {
					System.out.println("nothing happens");
					return origin;
				}
			} catch ( IllegalArgumentException e ) {
				System.out.println(e.getMessage());
			}
		}
		Customer.send(Action.Update, List.of(user));
		Object object = Customer.receive().get(0);
		if( object instanceof User ) {
			return (User) object;
		} else if ( object instanceof String ) {
			System.out.println(Helper.longLine);
			System.out.println("error: " + object);
			System.out.println(Helper.longLine);
		}
		user = origin;
		return user;
	}
	private static List<Movie> browseMovie(User user) {					// browse all valid movies
		if( user==null || user.getUid()<0 || !user.getValid() ){
			System.out.println("You have not login.");
			return new ArrayList<>();
		}
		Customer.send(Action.Browse, List.of(new Movie()));
		List<Object> response = Customer.receive();
		List<Movie> movies = new ArrayList<>();
		for(Object object: response) {
			if( object instanceof Movie ) movies.add((Movie) object);
			else if (object instanceof String ) {
				System.out.println(Helper.longLine);
				System.out.println("error: " + object);
				System.out.println(Helper.longLine);
		}
			else System.out.println("unknown class received from server.");
		}
		return movies;
	}
	private static void browseTheaterInMovie(User user) {				// display the specific movie status
		if( user==null || user.getUid()<0 || !user.getValid() ){
			System.out.println("You have not login.");
			return ;
		}
		List<Movie> movies = Customer.browseMovie(user);
		Movie movie = Movie.chooseMovie(movies);
		List<Ticket> tickets = Customer.browseTicket(user, movie);
		movie.showTheater(user, tickets);
	}
	private static Ticket createTicket(User user) {						// create ticket = buy ticket
		if( user==null || user.getUid()<0 || !user.getValid() ){
			System.out.println("You have not login.");
			return new Ticket();
		}
		List<Movie> movies = Customer.browseMovie(user);
		Movie movie = Movie.chooseMovie(movies);
		List<Ticket> existeds = Customer.browseTicket(user, movie);
		Ticket ticket = Ticket.requireTicket(user, movie, existeds);
		ticket.setUserId(user);
		Customer.send(Action.Create, List.of(ticket));
		Object object = Customer.receive().get(0);
		if( object instanceof Ticket ){
			System.out.println("create success:");
			return (Ticket) object;
		} else if (object instanceof String ) {
			System.out.println(Helper.longLine);
			System.out.println("error: " + object);
			System.out.println(Helper.longLine);
		}
		else System.out.println("unknown class received from server.");
		return new Ticket();
	}
	private static List<Ticket> browseTicket(User user) {				// get all tickets belonging to user
		if( user==null || user.getUid()<0 || !user.getValid() ){
			System.out.println("You have not login.");
			return new ArrayList<>();
		}
		List<Ticket> tickets = new ArrayList<>();
		Ticket ticket = new Ticket();
		ticket.setUserId(user);
		Customer.send(Action.Browse, List.of(ticket));
		List<Object> response = Customer.receive();
		for(Object object: response) {
			if( object instanceof Ticket ) tickets.add((Ticket) object);
			else if ( object instanceof String ) {
				System.out.println(Helper.longLine);
				System.out.println("error: " + object);
				System.out.println(Helper.longLine);
			}
			else System.out.println("unknown class received from server.");
		}
		return tickets;
	}
	private static List<Ticket> browseTicket(User user, Movie movie) {	// get all ticket belong to the specific movie, 
		if( user==null || user.getUid()<0 || !user.getValid() ){
			System.out.println("You have not login.");
			return new ArrayList<>();
		}
		if( movie==null || movie.getUid()<0 || !movie.getValid() ){
			System.out.println("you can't watch the invalid movie's ticket status.");
			return new ArrayList<>();
		}
		List<Ticket> tickets = new ArrayList<>();
		Ticket ticket = new Ticket();
		ticket.setUserId(user);
		ticket.setMovieId(movie);
		Customer.send(Action.Browse, List.of(movie, ticket));
		List<Object> response = Customer.receive();
		for(Object object: response) {
			if( object instanceof Ticket ) tickets.add((Ticket) object);
			else if ( object instanceof String ) {
				System.out.println(Helper.longLine);
				System.out.println("error: " + object);
				System.out.println(Helper.longLine);
			}
			else System.out.println("unknown class received from server.");
		}
		return tickets;
	}
	private static Ticket deleteTicket(User user) {						// return new Ticket() if failed
		if( user==null || user.getUid()<0 || !user.getValid() ) {
			System.out.println("You have not login.");
			return new Ticket();
		}
		List<Ticket> tickets = Customer.browseTicket(user);
		if( tickets.size() == 0 ) {
			System.out.println("you don't have any ticket that can be deleted.");
			return new Ticket();
		}
		Ticket ticket = Ticket.chooseTicket(tickets);
		System.out.println("make sure that you want to delete the Ticket:\n" + ticket );
		char confirm = Helper.getOneCharInput("Confirm? [y/n]: ", "yn");
		if( confirm == 'y' ) {
			Customer.send(Action.Delete, List.of(ticket));
			Object object = Customer.receive().get(0);
			if( object instanceof Ticket ) {
				return (Ticket) object;
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
		return new Ticket();
	}


	public static void main(String[] args) {

		Customer.connect();
		System.out.println("歡迎來到電影院訂票系統");
		User user = null;
		while (true) {
			if( user==null || user.getUid()<0 || !user.getValid() ) {
				StringBuilder sb = new StringBuilder();
				sb.append("You can\n")
					.append("\t(0) 註冊\n")
					.append("\t(1) 登入\n")
					.append("\t(e) 離開系統\n");
				char action = Helper.getOneCharInput(sb.toString()+"\t: ", "01e");
				if( action == '0' ){
					Customer.createUser();
				}
				if( action == '1' ){
					user = Customer.browseUser();
					if( user.getValid() ) {
						System.out.println(Helper.longLine);
						System.out.println("登入成功:\n" + user);
						System.out.println(Helper.longLine);
					}
				}
				if( action == 'e' ){
					System.exit(0);
				}
			}
			else {
				StringBuilder sb = new StringBuilder();
				sb.append("You can\n")
				  .append("\t(0) watch available movies\n")
				  .append("\t(1) display the available movie status\n")
				  .append("\t(2) watch tickets\n")
				  .append("\t(3) buy ticket\n")
				  .append("\t(4) refund ticket\n")
				  .append("\t(5) update user information\n")
				  .append("\t(6) logout\n");
				char action = Helper.getOneCharInput(sb.toString()+"\t: ", "0123456");
				if( action == '0' ) {
					List<Movie> movies = Customer.browseMovie(user);
					System.out.println(Helper.longLine);
					for( Movie movie:movies ) {
						System.out.println(movie);
					}
					if( movies.size() == 0 ) {
						System.out.println("沒有可以購買的電影.");
					}
					System.out.println(Helper.longLine);
				}
				if( action == '1' ) {
					Customer.browseTheaterInMovie(user);
				}
				if( action == '2' ) {
					List<Ticket> tickets = Customer.browseTicket(user);
					System.out.println(Helper.longLine);
					for( Ticket ticket:tickets ) {
						System.out.println(ticket);
					}
					if( tickets.size() == 0 ) {
						System.out.println("there is no any ticket you search about.");
					}
					System.out.println(Helper.longLine);
				}
				if( action == '3' ) {
					Ticket ticket = Customer.createTicket(user);
					if( ticket.getValid() ) System.out.println("This is your new ticket:\n" + ticket);
				}
				if( action == '4' ) {
					Ticket ticket = Customer.deleteTicket(user);
					if( ticket.getUserId()==user.getUid() ){
						System.out.println("This is your deleted ticket:\n" + ticket);
					}
				}
				if( action == '5' ) {
					user = Customer.updateUser(user);
					System.out.println("This is your updated user:\n" + user );
				}
				if( action == '6' ) {
					user = null;
				}
			}

		}
	}
}
