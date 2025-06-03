package backend;

import java.io.*;
import java.util.*;

import share.Color;
import share.Helper;

public class User extends ReadOneObject implements Serializable {

	private static final long serialVersionUID = 1L;
	private static RandomAccessFile file = null;
	static final String color = Color.ANSI + Color.Fore[3] + Color.ANSI + Color.Back[0] + Color.BOLD;
	public static final int passwordMaxLength = 32;
	public static final int mailMaxLength = 32;
	public static final long BYTES = Date.BYTES +
									Character.BYTES * User.mailMaxLength +
									Character.BYTES * User.passwordMaxLength +
									Byte.BYTES +
									Integer.BYTES;

	static void open() {
		User.file = Filetool.open("user", User.BYTES);
	}
	static void truncate() {
		Filetool.truncate(User.file);
	}
	static void close() {
		Filetool.close(User.file);
	}
	static ArrayList<User> readAll() {
		ArrayList<User> users = Filetool.readAllObjects(User.file, User::new);
		return users;
	}
	@Override
	void readOneObject() {
		User.readUser(this, User.file);
	}
	static User readUser() {
		User user = new User();
		User.readUser(user, User.file);
		return user;
	}
	static User readUser(int uid) {
		User user = new User();
		try {
			User.file.seek(uid * User.BYTES);
		} catch ( IOException e ) {
			System.out.println("User.file.seek error");
		}
		User.readUser(user, User.file);
		return user;
	}
	static void readUser(User user, RandomAccessFile file) {
		try {
			user.setDate(Date.readDate(file));
			user.setPassword(Filetool.readChars(file, User.passwordMaxLength));
			user.setMail(Filetool.readChars(file, User.mailMaxLength));
			user.setValid(file.readBoolean());
			user.setUid(file.readInt());
		} catch (IOException e) {
			e.printStackTrace();
			System.out.println("Data stored error.");
			System.exit(-1);
		}
	}
	static void writeUser(User user) {
		User.writeUser(user, User.file);
	}
	static void writeUser(User user, RandomAccessFile file) {
		try {
			file.seek( user.getUid() * User.BYTES );
			Date.writeDate(file, user.getDate());
			Filetool.writeChars(file, user.getPassword(), User.passwordMaxLength);
			Filetool.writeChars(file, user.getMail(), User.mailMaxLength);
			file.writeBoolean(user.getValid());
			file.writeInt(user.getUid());
		} catch (IOException e) {
			e.printStackTrace();
			System.out.println("Data written error.");
			System.exit(-1);
		}
	}
	static int findFirstValidPosition() {
		int uid = 0;
		try {
			User.file.seek(0);
			long cur = 0L;
			while( cur != User.file.length() ) {
				User user = User.readUser();
				cur = User.file.getFilePointer();
				if( !user.getValid() ) {
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
	static boolean isRepeatedMail(User user) {
		ArrayList<User> users = User.readAll();
		for(User existed: users) {
			if( !existed.getValid() ) continue;
			if( existed.getUid() == user.getUid() ) continue;
			if( existed.mail.equals(user.mail) ) return true;
		}
		return false;
	}
	public static String requireMail() {
		User user = new User();
		Scanner scanner = new Scanner(System.in);
		while(true){
			System.out.print("信箱: ");
			try {
				String mail = scanner.nextLine();
				user.setMail(mail);
				return mail;
			} catch( NoSuchElementException e1 ) {
				System.out.println(e1.getMessage());
			} catch ( IllegalArgumentException e2 ) {
				System.out.println(e2.getMessage());
			}
			continue;
		}
	}
	public static String requirePassword() {
		User user = new User();
		Scanner scanner = new Scanner(System.in);
		while(true){
			System.out.print("密碼: ");
			try {
				String password = scanner.nextLine();
				user.setPassword(password);
				return password;
			} catch ( NoSuchElementException e1 ) {
				System.out.println(e1.getMessage());
			} catch ( IllegalArgumentException e2 ) {
				System.out.println(e2.getMessage());
			}
			continue;
		}
	}
	public static User requireUser() {
		User user = new User();
		Date date = Date.requireDate("生日");
		user.setDate(date);
		String mail = User.requireMail();
		user.setMail(mail);
		String password = User.requirePassword();
		user.setPassword(password);
		return user;
	}
	public static User requireLogin() {
		User user = new User();
		String mail = User.requireMail();
		user.setMail(mail);
		String password = User.requirePassword();
		user.setPassword(password);
		return user;
	}


	private Date date;
	private String password;
	private String mail;
	private boolean valid;
	private int uid;

	public User() {
		this.setUid(-1);
		this.setValid(false);
		this.setDate(new Date());
	}
	User(Date date, String password, String mail, boolean valid, int uid) throws IllegalArgumentException {
		this.setDate(date);
		this.setPassword(password);
		this.setMail(mail);
		this.setValid(valid);
		this.setUid(uid);
	}
	public User(User user) {
		this(user.getDate(), user.getPassword(), user.getMail(), user.getValid(), user.getUid());
	}

	public Date getDate() {
		return new Date(this.date);
	}
	public String getPassword() {
		return this.password;
	}
	public String getMail() {
		return this.mail;
	}
	public boolean getValid() {
		return this.valid;
	}
	public int getUid() {
		return this.uid;
	}

	void setDate(Date date) {
		this.date = new Date(date);
	}
	public void setPassword(String password) throws IllegalArgumentException {
		if( password.length()<4 ) throw new IllegalArgumentException("password length should >= 4");
		if( password.length()>User.passwordMaxLength ) throw new IllegalArgumentException("password length should <= " + User.passwordMaxLength);
		this.password = password;
	}
	public void setMail(String mail) throws IllegalArgumentException {
		if( mail.length()>User.mailMaxLength ) throw new IllegalArgumentException("mail length should <= " + User.mailMaxLength);
		this.mail = mail;
	}
	void setValid(boolean valid) {
		this.valid = valid;
	}
	void setUid(int uid) {
		this.uid = uid;
	}

	public String toString() {
		return this.toString(0);
	}
	public String toString(int level) {
		StringBuilder sb = new StringBuilder();
		String indent = "\t".repeat(level);

		sb.append(User.color)
		  .append(indent).append("User {\n")
		  .append(indent).append("\tuid: ").append(this.uid).append("\n")
		  .append(Date.color)
		  .append(indent).append("").append(this.date.toString(level+1)).append("\n")
		  .append(User.color)
		  .append(indent).append("\t信箱: ").append(this.mail).append("\n")
		  .append(indent).append("\t密碼: ").append(this.password).append("\n")
		  .append(indent).append("\tvalid: ").append(this.valid).append("\n")
		  .append(indent).append("}")
		  .append(Color.RESET);

		return sb.toString();
	}
	public static void main(String[] args) {
		User.open();
		User.truncate();
		if( Helper.getOneCharInput("create user by yourself or by default? [y/d]: ", "yd") == 'd' ){
			User user0 = new User(new Date(2000, 1, 1), "pass0", "mail0", true, 0);
			User user1 = new User(new Date(2001, 2, 2), "pass1", "mail1", true, 1);
			User.writeUser(user0);
			User.writeUser(user1);
		} else {
			while ( true ) {
				if( Helper.getOneCharInput("create one user? [y/n]: ", "yn") == 'n' ) break;
				User created = User.requireUser();
				System.out.println("created = " + created );
				created.setUid( User.findFirstValidPosition() );
				created.setValid( true );
				User.writeUser(created);
			}
		}
		System.out.println("All data stored in user:");
		for(User user:User.readAll()){
			System.out.println(user);
		}
		if( Helper.getOneCharInput("truncate all user? [y/n]: ", "yn") == 'y' ) User.truncate();
		User.close();
	}
}
