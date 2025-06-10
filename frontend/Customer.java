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
import share.AESCrypto;

public class Customer {
	private static Socket socket = null;
	private static Role role = Role.Customer;

	public static void connect() {
		try {
			Customer.socket = new Socket(Helper.getIPAddress(), Helper.getPort());
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
		try {
			user.setPassword(AESCrypto.encrypt(user.getPassword()));
		} catch ( Exception e ) {
			System.out.println("encrypt error" + e );
		}
		Customer.send(Action.Create, List.of(user));
		Object object = Customer.receive().get(0);
		if( object instanceof User ){
			User success = (User) object;
			try {
				success.setPassword(AESCrypto.decrypt(user.getPassword()));
			} catch ( Exception e ) {
				System.out.println("decrypt error" + e );
			}
			System.out.println("註冊成功:\n" + object);
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
			try {
				attempt.setPassword(AESCrypto.encrypt(attempt.getPassword()));
			} catch ( Exception e ) {
				System.out.println("encrypt error" + e );
			}
			Customer.send(Action.Browse, List.of(attempt));
			Object object= Customer.receive().get(0);
			if( object instanceof User ){
				User success = (User) object;
				try {
					success.setPassword(AESCrypto.decrypt(success.getPassword()));
				} catch ( Exception e ) {
					System.out.println("decrypt error" + e );
				}
				return success;
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
			System.out.println("尚未登入.");
			return new User();
		}
		User origin = new User(user);
		Scanner scanner = new Scanner(System.in);
		while (true) {
			System.out.println("更改中的使用者資訊:\n" + user );
			char modify = Helper.getOneCharInput("想更改什麼值? [password(p)/mail(m)/finish(f)/quit(q)]", "pmfq");
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
		try {
			user.setPassword(AESCrypto.encrypt(user.getPassword()));
		} catch ( Exception e ) {
			System.out.println("encrypt error" + e );
		}
		Customer.send(Action.Update, List.of(user));
		Object object = Customer.receive().get(0);
		if( object instanceof User ) {
			User success = (User) object;
			try {
				success.setPassword(AESCrypto.decrypt(success.getPassword()));
			} catch ( Exception e ) {
				System.out.println("decrypt error" + e );
			}
			return success;
		} else if ( object instanceof String ) {
			System.out.println(Helper.longLine);
			System.out.println("error: " + object);
			System.out.println(Helper.longLine);
		}
		user = origin;
		return user;
	}
	private static User deleteUser(User user) {							// delete user = logout user
		if( user==null || user.getUid()<0 || !user.getValid() ){
			System.out.println("尚未登入.");
			return new User();
		}
		Customer.send(Action.Delete, List.of(user));
		Object object = Customer.receive().get(0);
		if( object instanceof User ) {
			return (User) object;
		} else if ( object instanceof String ) {
			System.out.println(Helper.longLine);
			System.out.println("error: " + object);
			System.out.println(Helper.longLine);
			return user;
		}
		return (User) object;
	}
	private static List<Movie> browseMovie(User user) {					// browse all valid movies
		if( user==null || user.getUid()<0 || !user.getValid() ){
			System.out.println("尚未登入.");
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
			System.out.println("尚未登入.");
			return ;
		}
		List<Movie> movies = Customer.browseMovie(user);
		Movie movie = Movie.chooseMovie(movies);
		List<Ticket> tickets = Customer.browseTicket(user, movie);
		movie.showTheater(user, tickets);
	}
	private static Ticket createTicket(User user) {						// create ticket = buy ticket
		if( user==null || user.getUid()<0 || !user.getValid() ){
			System.out.println("尚未登入.");
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
			System.out.println("訂票成功:");
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
			System.out.println("尚未登入.");
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
			System.out.println("尚未登入.");
			return new ArrayList<>();
		}
		if( movie==null || movie.getUid()<0 || !movie.getValid() ){
			System.out.println("無法觀看非法電影的狀態.");
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
			System.out.println("尚未登入.");
			return new Ticket();
		}
		List<Ticket> tickets = Customer.browseTicket(user);
		if( tickets.size() == 0 ) {
			System.out.println("你沒有可以退的票.");
			return new Ticket();
		}
		Ticket ticket = Ticket.chooseTicket(tickets);
		System.out.println("請確定你想退這張票:\n" + ticket );
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
		final User[] currentUser = new User[1];

		Runtime.getRuntime().addShutdownHook(new Thread(() -> {
			if( currentUser[0]!=null && currentUser[0].getValid() ) {
				User logout = Customer.deleteUser(currentUser[0]);		// 呼叫前端的 logout
				System.out.println("已自動登出使用者 : " + logout.getMail());
			}
		}));

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
						currentUser[0] = user;
					}
				}
				if( action == 'e' ){
					System.exit(0);
				}
			}
			else {
				StringBuilder sb = new StringBuilder();
				sb.append("You can\n")
				  .append("\t(0) 查詢上映中的電影\n")
				  .append("\t(1) 查詢上映中電影的座位\n")
				  .append("\t(2) 查詢擁有的票\n")
				  .append("\t(3) 買票\n")
				  .append("\t(4) 退票\n")
				  .append("\t(5) 更新使用者資訊\n")
				  .append("\t(6) 登出\n");
				char action = Helper.getOneCharInput(sb.toString()+"\t: ", "0123456");
				if( action == '0' ) {
					List<Movie> movies = Customer.browseMovie(user);
					System.out.println(Helper.longLine);
					for( Movie movie:movies ) {
						System.out.println(movie);
					}
					if( movies.size() == 0 ) {
						System.out.println("無上映中的電影，故無法訂票.");
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
						System.out.println("沒有符合的票.");
					}
					System.out.println(Helper.longLine);
				}
				if( action == '3' ) {
					Ticket ticket = Customer.createTicket(user);
					if( ticket.getValid() ) System.out.println("以下是你查詢到的票:\n" + ticket);
				}
				if( action == '4' ) {
					Ticket ticket = Customer.deleteTicket(user);
					if( ticket.getUserId()==user.getUid() ){
						System.out.println("以下是你退成功的票:\n" + ticket);
					}
				}
				if( action == '5' ) {
					user = Customer.updateUser(user);
					System.out.println("以下是你成功更新的使用者資訊:\n" + user );
					currentUser[0] = user;
				}
				if( action == '6' ) {
					user = Customer.deleteUser(user);
					currentUser[0] = user;
				}
			}

		}
	}
}
