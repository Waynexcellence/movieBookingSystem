package share;

import java.io.*;
import java.util.*;

public class Conversation implements Serializable {

	public static int port = 16384;
	public static String localhost = "localhost";
	public static String remoteHost = "140.112.?.?";

	public Role role;       	// "customer" or "merchant"
	public Action action;		// "create", "update", "delete"
	public List<Object> data;	// The actual data

	
	public Conversation(Role role, Action action, List<Object> data) {
		this.data = data;
		this.role = role;
		this.action = action;
	}
	public Conversation(List<Object> data) {
		this.data = data;
	}

	// // this method currently doesn't be used
	// public <T> List<T> getDataAs(Class<T> clazz) {
	// 	if (this.data instanceof List<?>) {
	// 		List<?> dataList = (List<?>) this.data;
	// 		for (Object obj : dataList) {
	// 			if (!clazz.isInstance(obj)) {
	// 				throw new ClassCastException("Data contains an element that is not of type " + clazz.getName());
	// 			}
	// 		}
	// 		return (List<T>) this.data;
	// 	} else {
	// 		throw new ClassCastException("Data is not a List of " + clazz.getName());
	// 	}
	// }

	public String toString() {
		return this.toString(0);
	}
	public String toString(int level) {
		StringBuilder sb = new StringBuilder();
		String indent = "\t".repeat(level);

		sb.append(indent).append("Conversation {\n")
		  .append(indent).append("\trole: ").append(this.role).append("\n")
		  .append(indent).append("\taction: ").append(this.action).append("\n")
		  .append(indent).append("}\n");
		return sb.toString();
	}
}
