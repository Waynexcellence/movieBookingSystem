package backend;

import share.Color;
import share.Helper;

public class Clear {
    
    public static void main(String[] args) {
        User.open();
        Film.open();
        Theater.open();
        Movie.open();
        Ticket.open();

        User.truncate();
        Film.truncate();
        Theater.truncate();
        Movie.truncate();
        Ticket.truncate();

		if( Helper.getOneCharInput("create data by default? [y/n]: ", "yn") == 'y' ) {
			User user0 = new User(new Date(2000, 10, 10), "pass0", "mail0", true, 0);
			User user1 = new User(new Date(2001, 11, 11), "pass1", "mail1", true, 1);
			User.writeUser(user0);
			User.writeUser(user1);

			Film film0 = new Film("Captain America: Brave New World", "Marvel Cinematic Universe", 2, 12, true, 0);
			Film film1 = new Film("Mobile Suit Gundam GQuuuuuuX: Beginning", "EVA, MSG", 1, 0, true, 1);
			Film film2 = new Film("The Uniform", "Adult's pain", 1, 0, true, 2);
			Film film3 = new Film("The Monkey", "Steven", 1, 18, true, 3);
			Film.writeFilm(film0);
			Film.writeFilm(film1);
			Film.writeFilm(film2);
			Film.writeFilm(film3);

			Theater theater0 = new Theater("large", Movie.largeSeatAmount, true, 0);
			Theater theater1 = new Theater("small", Movie.smallSeatAmount, true, 1);
			Theater.writeTheater(theater0);
			Theater.writeTheater(theater1);

			Movie movie0 = new Movie(film0, theater0, new Date(2020, 10, 10), 10, true, 0);
			Movie movie1 = new Movie(film1, theater1, new Date(2021, 11, 11), 11, true, 1);
			Movie.writeMovie(movie0);
			Movie.writeMovie(movie1);

			Ticket ticket0 = new Ticket(movie0.getUid(), user0.getUid(), new Seat('L', 12, Color.Back[3]), true, 0);
			Ticket ticket1 = new Ticket(movie0.getUid(), user1.getUid(), new Seat('L', 13, Color.Back[3]), true, 1);
			Ticket ticket2 = new Ticket(movie1.getUid(), user0.getUid(), new Seat('I', 15, Color.Back[7]), true, 2);
			Ticket ticket3 = new Ticket(movie1.getUid(), user1.getUid(), new Seat('I', 16, Color.Back[7]), true, 3);
			Ticket.writeTicket(ticket0);
			Ticket.writeTicket(ticket1);
			Ticket.writeTicket(ticket2);
			Ticket.writeTicket(ticket3);
			for(User item:User.readAll()){
				System.out.println(item);
			}
			for(Film item:Film.readAll()){
				System.out.println(item);
			}
			for(Theater item:Theater.readAll()){
				System.out.println(item);
			}
			for(Movie item:Movie.readAll()){
				System.out.println(item);
			}
			for(Ticket item:Ticket.readAll()){
				System.out.println(item);
			}
		} else {
        	System.out.println("usr, film, theater, movie, ticket, all clear.");
		}

        User.close();
        Film.close();
        Theater.close();
        Movie.close();
        Ticket.close();
    }
}
