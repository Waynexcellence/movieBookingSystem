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

		if( Helper.getOneCharInput("要使用預設的資料嗎? [y/n]: ", "yn") == 'y' ) {
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

			Theater theater0 = new Theater("大影廳", Movie.largeSeatAmount, true, 0);
			Theater theater1 = new Theater("小影廳", Movie.smallSeatAmount, true, 1);
			Theater.writeTheater(theater0);
			Theater.writeTheater(theater1);

			Movie movie0 = new Movie(film0, theater0, new Date(2025, 10, 10), 10, true, 0);
			Movie movie1 = new Movie(film1, theater1, new Date(2025, 11, 11), 10, true, 1);
			Movie movie2 = new Movie(film2, theater0, new Date(2025, 10, 10), 15, true, 2);
			Movie movie3 = new Movie(film3, theater1, new Date(2025, 11, 11), 15, true, 3);
			Movie movie4 = new Movie(film0, theater1, new Date(2025, 11, 11), 20, true, 4);
			Movie movie5 = new Movie(film1, theater0, new Date(2025, 10, 10), 20, true, 5);
			Movie.writeMovie(movie0);
			Movie.writeMovie(movie1);
			Movie.writeMovie(movie2);
			Movie.writeMovie(movie3);
			Movie.writeMovie(movie4);
			Movie.writeMovie(movie5);

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
        	System.out.println("usr, film, theater, movie, ticket, 已全部清空.");
		}

        User.close();
        Film.close();
        Theater.close();
        Movie.close();
        Ticket.close();
    }
}
