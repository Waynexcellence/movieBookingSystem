package backend;

import java.io.*;
import java.net.*;
import java.util.*;
import java.util.concurrent.*;
import java.util.concurrent.locks.*;

import share.Role;
import share.Action;
import share.Conversation;
import share.Helper;

public class Server {

	private static final ReentrantLock userLock = new ReentrantLock();
	private static final ReentrantLock filmLock = new ReentrantLock();
	private static final ReentrantLock movieLock = new ReentrantLock();
	private static final ReentrantLock theaterLock = new ReentrantLock();
	private static final ReentrantLock ticketLock = new ReentrantLock();

	private static final ConcurrentLinkedQueue<Socket> clientSockets = new ConcurrentLinkedQueue<>();

	public static Conversation receive(Socket socket) {
		Conversation conversation = null;
		try {
			ObjectInputStream inputStream = new ObjectInputStream(socket.getInputStream());
			conversation = (Conversation) inputStream.readObject();
		} catch ( SocketException e1 ) {
			System.out.println("Client disconnected: " + e1.getMessage());
			return null;
		}
		catch ( IOException e2 ) {
			e2.printStackTrace();
			System.out.println("IOException: " + e2.getMessage());
		} catch ( ClassNotFoundException e3 ) {
			e3.printStackTrace();
			System.out.println("Class not found: " + e3.getMessage());
		} catch ( Exception e4 ) {
			e4.printStackTrace();
			System.out.println("Unexpected error during receive: " + e4.getMessage());
		}
		return conversation;
	}
	public static void send(Socket socket, List<Object> dataList) {
		try {
			ObjectOutputStream outputStream = new ObjectOutputStream(socket.getOutputStream());
			Conversation conversation = new Conversation((List<Object>) dataList);
			outputStream.writeObject(conversation);
			outputStream.flush();
		} catch (Exception e) {
			e.printStackTrace();
			System.exit(-1);
		}
	}

	private static Object getData(Conversation task, int index) {
		if( task.data.size()<index+1 ) return null;
		return task.data.get(index);
	}

	private static void handleClient(Socket clientSocket) {
		while (true) {
			Conversation task = Server.receive(clientSocket);
			List<Object> response = new ArrayList<>();
			if (task == null) break;
			userLock.lock();
			filmLock.lock();
			movieLock.lock();
			theaterLock.lock();
			ticketLock.lock();
			try {
				if (task.role == Role.Customer) {
					response = Server.handleCustomer(task);
				} else if (task.role == Role.Merchant) {
					response = Server.handleMerchant(task);
				}
			} finally {
				userLock.unlock();
				filmLock.unlock();
				movieLock.unlock();
				theaterLock.unlock();
				ticketLock.unlock();
			}
			System.out.println("Server response:\n" + response);
			Server.send(clientSocket, response);
		}
		try {
			clientSocket.close();
			clientSockets.remove(clientSocket);
			System.out.println("Thread exit: " + Thread.currentThread().getName());
		} catch (IOException e) {
			System.out.println("Error closing client socket: " + e.getMessage());
		}
	}
	private static List<Object> handleCustomer(Conversation task) {
		List<Object> response = new ArrayList<>();
		Object obj0 = Server.getData(task, 0);
		Object obj1 = Server.getData(task, 1);
		if (task.action == Action.Create) {
			if 			( obj0 instanceof User ) 			response = Server.handleCreateUser((User) obj0);
			else if 	( obj0 instanceof Ticket ) 			response = Server.handleCreateTicket((Ticket) obj0);
			else 		response.add("Customer Create What?");
		} else if ( task.action == Action.Browse ) {
			if 			( obj0 instanceof User ) 			response = Server.handleBrowseUser((User) obj0);
			else if 	( obj0 instanceof Movie ) { 
				if 			( obj1 == null ) 				response = Server.handleBrowseMovie((Movie) obj0);
				else if 	( obj1 instanceof Ticket ) 		response = Server.handleBrowseMovie((Ticket) obj1);
				else 	response.add("Customer Browse Movie what?");
			}
			else if 	( obj0 instanceof Ticket ) 			response = Server.handleBrowseTicket((Ticket) obj0);
			else  		response.add("Customer Browse What?");
		} else if ( task.action == Action.Update ) {
			if 			( obj0 instanceof User ) 			response = Server.handleUpdateUser((User) obj0);
			else 		response.add("Customer Update What?");
		} else if ( task.action == Action.Delete ) {
			if 			( obj0 instanceof Ticket ) 			response = Server.handleDeleteTicket((Ticket) obj0);
			else 		response.add("Customer Update What?");
		} else 			response.add("Customer What?");
		return response;
	}
	private static List<Object> handleMerchant(Conversation task) {
		List<Object> response = new ArrayList<>();
		Object obj0 = Server.getData(task, 0);
		Object obj1 = Server.getData(task, 1);
		if (task.action == Action.Create) {
			if 		( obj0 instanceof Film ) 				response = Server.handleCreateFilm((Film) obj0);
			else if ( obj0 instanceof Theater ) 			response = Server.handleCreateTheater((Theater) obj0);
			else if ( obj0 instanceof Movie )  				response = Server.handleCreateMovie((Movie) obj0);
			else response.add("Merchant Create What?");
		} else if (task.action == Action.Browse) {
			if 		( obj0 instanceof Film ) 				response = Server.handleBrowseFilm((Film) obj0);
			else if ( obj0 instanceof Theater ) 			response = Server.handleBrowseTheater((Theater) obj0);
			else if ( obj0 instanceof Movie ) 				response = Server.handleBrowseMovie((Movie) obj0);
			else if ( obj0 instanceof Ticket ) 				response = Server.handleBrowseTicket((Ticket) obj0);
			else response.add("Merchant Browse What?");
		} else if (task.action == Action.Update) {
			if 		( obj0 instanceof Movie ) 				response = Server.handleUpdateMovie((Movie) obj0);
			else response.add("Merchant Update What?");
		} else if (task.action == Action.Delete) {
			if 		( obj0 instanceof Movie ) 				response = Server.handleDeleteMovie((Movie) obj0);
			else response.add("Merchant Delete What?");
		} else response.add("Merchant What?");
		return response;
	}

	// customer: return a valid user if successful creation
	private static List<Object> handleCreateUser(User user) {
		System.out.println("Receive User Create:\n" + user );
		List<Object> response = new ArrayList<>();
		if( User.isRepeatedMail(user) ){
			response.add("Repeated mail with others.");
			return response;
		}
		int uid = User.findFirstValidPosition();
		user.setUid(uid);
		response.add(user);
		user.setValid(true);
		User.writeUser(user);
		return response;
	}
	// customer: return a valid user if successful login
	private static List<Object> handleBrowseUser(User user) {
		System.out.println("Receive User Browse:\n" + user );
		List<Object> response = new ArrayList<>();
		for ( User existed : User.readAll() ) {
			if( !existed.getValid() ) continue;
			if( existed.getMail().equals(user.getMail()) ){
				if( existed.getPassword().equals(user.getPassword()) ){
					response.add(existed);
					return response;
				}
				response.add("Password Incorrect.");
				return response;
			}
		}
		response.add("User Not Found.");
		return response;
	}
	// customer: return a valid user if customer update it successfully by its uid
	private static List<Object> handleUpdateUser(User user) {
		System.out.println("Receive User Update:\n" + user);
		List<Object> response = new ArrayList<>();
		if( User.isRepeatedMail(user) ){
			response.add("Repeated mail with others.");
			return response;
		}
		User.writeUser(user);
		response.add(user);
		return response;
	}

	// merchant: return valid film if successful creation
	private static List<Object> handleCreateFilm(Film film) {
		System.out.println("Receive Film Create:\n" + film );
		List<Object> response = new ArrayList<>();
		int uid = Film.findFirstValidPosition();
		film.setUid(uid);
		film.setValid(true);
		Film.writeFilm(film);
		response.add(film);
		return response;
	}
	// merchant: return all valid film
	private static List<Object> handleBrowseFilm(Film film) {
		System.out.println("Receive Film Browse:");
		List<Object> response = new ArrayList<>();
		for( Film existed : Film.readAll() ) {
			if( existed.getValid() ) response.add(existed);
		}
		return response;
	}

	// merchant: return valid theater if successful creation
	private static List<Object> handleCreateTheater(Theater theater) {
		System.out.println("Receive Theater Create:\n" + theater );
		List<Object> response = new ArrayList<>();
		if ( Theater.isRepeatedName(theater) ) {
			response.add("Repeated name with others.");
			return response;
		}
		int uid = Theater.findFirstValidPosition();
		theater.setUid(uid);
		theater.setValid(true);
		Theater.writeTheater(theater);
		response.add(theater);
		return response;
	}
	// merchant: return all valid theater
	private static List<Object> handleBrowseTheater(Theater theater) {
		System.out.println("Receive Theater Browse:");
		List<Object> response = new ArrayList<>();
		List<Theater> theaters = Theater.readAll();
		for( Theater existed : theaters ) {
			if( existed.getValid() ) response.add(existed);
		}
		return response;
	}

	// merchant: return valid movie if successful creation
	private static List<Object> handleCreateMovie(Movie movie) {
		System.out.println("Receive Movie Create:\n" + movie );
		List<Object> response = new ArrayList<>();
		if( Movie.iscollidedTime(movie) ){
			response.add("Repeated playtime wit others.");
			return response;
		}
		int uid = Movie.findFirstValidPosition();
		movie.setUid(uid);
		movie.setValid(true);
		Movie.writeMovie(movie);
		response.add(movie);
		return response;
	}
	// merchant: return all valid movie
	// customer: return all valid movie
	private static List<Object> handleBrowseMovie(Movie movie) {			
		System.out.println("Receive Movie Browse:");
		List<Object> response = new ArrayList<>();
		for( Movie existed : Movie.readAll() ) {
			if( existed.getValid() ) response.add(existed);
		}
		return response;
	}
	// customer: return all valid ticket regarding the ticket's movieId
	private static List<Object> handleBrowseMovie(Ticket ticket) {
		System.out.println("Receive Movie Browse:\n" + ticket );
		List<Object> response = new ArrayList<>();
		for( Ticket existed : Ticket.readAll() ) {
			if( !existed.getValid() ) continue;
			if( existed.getMovieId()==ticket.getMovieId() ){
				if( existed.getUserId()!=ticket.getUserId() ) existed.setUserId(-1);
				response.add(existed);
			}
		}
		return response;
	}
	// merchant: return a valid movie if merchant update it successfully by its uid
	private static List<Object> handleUpdateMovie(Movie movie) {
		System.out.println("Receive Movie Update:\n" + movie );
		List<Object> response = new ArrayList<>();
		if( Movie.iscollidedTime(movie) ){
			response.add("Repeated playtime with others.");
			return response;
		}
		Movie.writeMovie(movie);
		response.add(movie);
		return response;
	}
	// merchant: return an invalid movie if merchant delete it successfully by its uid
	private static List<Object> handleDeleteMovie(Movie movie) {
		System.out.println("Receive Movie Delete:\n" + movie );
		List<Object> response = new ArrayList<>();
		if( !movie.getValid() ) {
			response.add("you want to delete the invalid movie.");
			return response;
		}
		for( Ticket ticket: Ticket.readAll() ) {
			if( ticket.getMovieId()==movie.getUid() ) {
				ticket.setValid(false);
				Ticket.writeTicket(ticket);
			}
		}
		movie.setValid(false);
		Movie.writeMovie(movie);
		response.add(movie);
		return response;
	}

	// customer: return a valid ticket if the ticket's all information is valid on the local file and all other conditions satisfy
	private static List<Object> handleCreateTicket(Ticket ticket) {
		System.out.println("Receive Ticket Create:\n" + ticket );
		List<Object> response = new ArrayList<>();
		Movie movie = Movie.readMovie(ticket.getMovieId());
		User user = User.readUser(ticket.getUserId());
		if( !movie.getValid() ) {
			response.add("the movie now is invalid.");
			return response;
		}
		if( !user.getValid() ) {
			response.add("the user now is invalid.");
			return response;
		}
		if( user.getDate().getAge()<movie.getFilm().getClassification() ) {
			response.add("your age is not enough.");
			return response;
		}
		if( Ticket.iscollidedTicket(ticket) ) {
			response.add("the seat has been bought already.");
			return response;
		}
		int uid = Ticket.findFirstValidPosition();
		ticket.setUid(uid);
		ticket.setValid(true);
		Ticket.writeTicket(ticket);
		response.add(ticket);
		return response;
	}
	// customer: return all valid ticket where userId = customer's
	// merchant: return all valid ticket
	private static List<Object> handleBrowseTicket(Ticket ticket) {
		System.out.println("Receive Ticket Browse:\n" + ticket );
		List<Object> response = new ArrayList<>();
		ArrayList<Ticket> existeds = Ticket.readAll();
		if( ticket.getUserId() == -1 ) {
			for( Ticket existed: existeds) {
				if( !existed.getValid() ) continue;
				response.add(existed);
			}
		} else {
			for( Ticket existed: existeds) {
				if( !existed.getValid() ) continue;
				if( existed.getUserId() == ticket.getUserId() ) {
					response.add(existed);
				}
			}
		}
		return response;
	}
	// customer: return an invalid ticket if customer delete it successfully
	private static List<Object> handleDeleteTicket(Ticket ticket) {
		System.out.println("Receive Ticket Delete:\n" + ticket );
		List<Object> response = new ArrayList<>();
		if( !ticket.getValid() ) {
			response.add("you can't delete the invalid ticket.");
			return response;
		}
		ticket.setValid(false);
		Ticket.writeTicket(ticket);
		response.add(ticket);
		return response;
	}

	public static void main(String[] args) {
		int listeningPort = Helper.getPort();
		try (ServerSocket serverSocket = new ServerSocket(listeningPort)) {
			URL url = URI.create("http://checkip.amazonaws.com").toURL();
			BufferedReader amazonawsReader = new BufferedReader(new InputStreamReader(url.openStream()));
			System.out.println("Server IP address is on: " + amazonawsReader.readLine());
			System.out.println("Server listening on port " + listeningPort);
			User.open();
			Film.open();
			Theater.open();
			Movie.open();
			Ticket.open();
			while (true) {
					Socket clientSocket = serverSocket.accept();
					clientSockets.add(clientSocket);
					new Thread(() -> handleClient(clientSocket), "ClientHandlerThread No." + clientSocket.getPort()).start();
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
}
