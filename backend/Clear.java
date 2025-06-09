package backend;

import share.Color;
import share.Helper;
import share.AESCrypto;

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
			User user0 = null;
			User user1 = null;
			try {
				user0 = new User(new Date(2000, 1, 1), AESCrypto.encrypt("pass0"), "mail0", true, 0);
				user1 = new User(new Date(2008, 8, 8), AESCrypto.encrypt("pass1"), "mail1", true, 1);
			} catch ( Exception e ) {
				System.out.println("encrypt error" + e );
			}
			User.writeUser(user0);
			User.writeUser(user1);

			Film film0 = new Film("美國隊長：無畏新世界", "Marvel Cinematic Universe", 2, 12, true, 0);
			Film film1 = new Film("機動戰士Gundam GquuuuuuX -Beginning-", "EVA, MSG", 1, 0, true, 1);
			Film film2 = new Film("夜校女生", "Adult's pain", 1, 0, true, 2);
			Film film3 = new Film("史蒂芬金之猴子", "Steven", 1, 18, true, 3);
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

			Ticket ticket00 = new Ticket(movie0.getUid(), user0.getUid(), new Seat('H', 18, Color.Back[3]), true,  0);
			Ticket ticket01 = new Ticket(movie0.getUid(), user0.getUid(), new Seat('I', 18, Color.Back[1]), true,  1);
			Ticket ticket02 = new Ticket(movie0.getUid(), user0.getUid(), new Seat('J', 18, Color.Back[1]), true,  2);
			Ticket ticket03 = new Ticket(movie1.getUid(), user0.getUid(), new Seat('B',  8, Color.Back[7]), true,  3);
			Ticket ticket04 = new Ticket(movie1.getUid(), user0.getUid(), new Seat('C',  8, Color.Back[7]), true,  4);
			Ticket ticket05 = new Ticket(movie1.getUid(), user0.getUid(), new Seat('D',  8, Color.Back[7]), true,  5);

			Ticket ticket10 = new Ticket(movie0.getUid(), user1.getUid(), new Seat('H', 20, Color.Back[3]), true,  6);
			Ticket ticket11 = new Ticket(movie0.getUid(), user1.getUid(), new Seat('I', 20, Color.Back[1]), true,  7);
			Ticket ticket12 = new Ticket(movie0.getUid(), user1.getUid(), new Seat('J', 20, Color.Back[1]), true,  8);
			Ticket ticket13 = new Ticket(movie1.getUid(), user1.getUid(), new Seat('B', 10, Color.Back[7]), true,  9);
			Ticket ticket14 = new Ticket(movie1.getUid(), user1.getUid(), new Seat('C', 10, Color.Back[7]), true, 10);
			Ticket ticket15 = new Ticket(movie1.getUid(), user1.getUid(), new Seat('D', 10, Color.Back[7]), true, 11);
			Ticket.writeTicket(ticket00);
			Ticket.writeTicket(ticket01);
			Ticket.writeTicket(ticket02);
			Ticket.writeTicket(ticket03);
			Ticket.writeTicket(ticket04);
			Ticket.writeTicket(ticket05);

			Ticket.writeTicket(ticket10);
			Ticket.writeTicket(ticket11);
			Ticket.writeTicket(ticket12);
			Ticket.writeTicket(ticket13);
			Ticket.writeTicket(ticket14);
			Ticket.writeTicket(ticket15);
			for(User item:User.readAll()){
				try {
					item.setPassword(AESCrypto.decrypt(item.getPassword()));
				} catch ( Exception e ) {
					System.out.println("decrypt error" + e );
				}
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
