package share;

public enum Action {
	Create,		// buy ticket = create ticket, create user, movie, film, theater
	Update,		// update film, movie for merchant, update user for customer
	Delete,		// delete ticket, movie
	Browse		// login = Customer browse
}
