package bd;

import java.io.BufferedReader;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Date;
import java.util.GregorianCalendar;

import org.bson.Document;
import org.bson.types.ObjectId;

import com.mongodb.client.MongoCollection;
import com.mongodb.client.MongoDatabase;

public class LoadDataBase {

	private static String[] databaseQuery = { "DROP DATABASE if exists Gabriel_David",
			"CREATE DATABASE Gabriel_David" };

	private static String userQuery = "CREATE TABLE user (" + "nom varchar(30) not null,"
			+ "prenom varchar(30) not null," + "login varchar(30) not null," + "password blob not null,"
			+ "id int primary key not null auto_increment);";

	private static String sessionQuery = "CREATE TABLE session (" + "key_session varchar(32) not null primary key,"
			+ "id_user int not null," + "time timestamp not null,"
			+ "constraint fk_session foreign key (id_user) references user(id) on delete cascade);";

	private static String friendQuery = "CREATE TABLE friend (" + "id_1 int," + "id_2 int,"
			+ "constraint pk_friend primary key (id_1, id_2),"
			+ "constraint fk_friend1 foreign key (id_1) references user(id) on delete cascade,"
			+ "constraint fk_friend2 foreign key (id_2) references user(id) on delete cascade);";

	private static String insertUserQuery = "insert into user(nom, prenom, login, password) values(?, ?, ?, ?);";

	public static void loadSQLDataBase() {
		try {

			// create the database or drop it and recreate it
			Connection c = DriverManager.getConnection("jdbc:mysql://localhost/?user=root&password=root");
			PreparedStatement s = null;
			for (int i = 0; i < databaseQuery.length; i++) {

				s = c.prepareStatement(databaseQuery[i]);
				s.executeUpdate();
			}
			s.close();
			c.close();

			// create the structure of database with connection defined in DBStatic
			c = Database.getMySQLConnection();
			String[] queries = { userQuery, sessionQuery, friendQuery };
			for (int i = 0; i < queries.length; i++) {
				s = c.prepareStatement(queries[i]);
				s.executeUpdate();
			}

			s.close();

			// Inserting users
			s = c.prepareStatement(insertUserQuery);
			try (BufferedReader buff = new BufferedReader(new FileReader("donnee.txt"))) {
				String string;
				while ((string = buff.readLine()) != null) {
					String[] tab = string.split(";");
					for (int i = 1; i <= tab.length; i++) {
						s.setString(i, tab[i - 1]);
					}
					s.executeUpdate();

				}
			} catch (FileNotFoundException e) {
				e.printStackTrace();
			} catch (IOException e) {
				e.printStackTrace();
			}
			;

		} catch (SQLException e) {
			e.printStackTrace();
		}
	}

	public static String loadMongoDataBase() {
		MongoDatabase db = Database.getMongoDBConnection();
		//Deleting the old database
		db.drop();
		//creation of the table message
		MongoCollection<Document> coll = db.getCollection("message");
		Document doc = new Document();
		doc.append("author_id", 1);
		doc.append("author_name", "toto");
		doc.append("contenu", "Je suis en pleine forme aujourd'hui !!");
		GregorianCalendar calendar = new GregorianCalendar();
		Date date = calendar.getTime();
		doc.append("date", date);
		
		//the comments of the message
		ArrayList<Document> comments = new ArrayList<>();
		Document doc1 = new Document();
		doc1.append("author_id", 1);
		doc1.append("author_name", "toto");
		doc1.append("text", "Excellent ! Bonne journée!!");
		doc1.append("date", date);
		comments.add(doc1);
		
		Document doc2 = new Document();
		doc2.append("author_id", 1);
		doc2.append("author_name", "toto");
		doc2.append("text", "Excellent ! Bonne journée!!");
		doc2.append("date", date);
		comments.add(doc2);
		
		//inserting the comments in the message
		doc.append("comments", comments);
		coll.insertOne(doc);
		ObjectId id = doc.getObjectId("_id");
		
		
		Document query = new Document("author_id", 1);
		Document proj = new Document();
		proj.append("_id", 0);
		proj.append("message_id", 0);
		
		
		//Testing mongo cursor
		/*
		MongoCursor<Document> cursor = coll.find(query).projection(proj).iterator();
		while(cursor.hasNext()) {
			System.out.println(cursor.next().get("text"));
		}
		cursor.close();
		*/
		return id.toHexString();
	}
	public static void main(String[] args) throws ClassNotFoundException {
		loadSQLDataBase();
		loadMongoDataBase();
	}
}