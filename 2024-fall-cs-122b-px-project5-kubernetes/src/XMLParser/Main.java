package XMLParser;
import javax.xml.parsers.SAXParser;
import javax.xml.parsers.SAXParserFactory;
import java.sql.SQLException;

public class Main {
    public static void main(String[] args) {
        DatabaseHelper dbHelper = null;
        XMLParser xmlParser = null;

        try {
            // Step 1: Initialize DatabaseHelper for database connection
            dbHelper = new DatabaseHelper();

            // Step 2: Initialize XMLParser with dbHelper
            xmlParser = new XMLParser(dbHelper);

            // Step 3: Initialize SAX parser
            SAXParserFactory factory = SAXParserFactory.newInstance();
            SAXParser saxParser = factory.newSAXParser();

            // Step 4: Parse each XML file with respective handlers
            System.out.println("Parsing actors63.xml..."); // adds to stars
            // xmlParser.parseActorsFile("C:/Users/bounc/Desktop/stanford-movies/actors63.xml");
            xmlParser.parseMoviesFile("/Users/shinigami/Downloads/stanford-movies/mains243.xml");

            System.out.println("Parsing mains243.xml...");// adds to movies
           // xmlParser.parseMoviesFile("C:/Users/bounc/Desktop/stanford-movies/mains243.xml");
            xmlParser.parseMoviesFile("/Users/shinigami/Downloads/stanford-movies/mains243.xml");


            System.out.println("Parsing casts124.xml..."); // adds to stars in movies
            // xmlParser.parseCastsFile("C:/Users/bounc/Desktop/stanford-movies/casts124.xml");
            xmlParser.parseMoviesFile("/Users/shinigami/Downloads/stanford-movies/mains243.xml");

            // Step 5: Commit transaction if everything parsed successfully
            dbHelper.commit();
            System.out.println("Parsing completed successfully, and all data has been committed to the database.");

        } catch (Exception e) {
            e.printStackTrace();
            try {
                if (dbHelper != null) {
                    dbHelper.getConnection().rollback(); // Rollback transaction if any error occurs
                    System.out.println("Transaction has been rolled back due to an error.");
                }
            } catch (SQLException rollbackException) {
                rollbackException.printStackTrace();
            }
        } finally {
            // Step 6: Close database connection
            try {
                if (dbHelper != null) {
                    dbHelper.close();
                }
            } catch (SQLException closeException) {
                closeException.printStackTrace();
            }
        }
    }
}
