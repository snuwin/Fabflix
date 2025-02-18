package XMLParser;
import org.xml.sax.Attributes;
import org.xml.sax.SAXException;
import org.xml.sax.InputSource;
import org.xml.sax.helpers.DefaultHandler;
import javax.xml.parsers.SAXParser;
import javax.xml.parsers.SAXParserFactory;
import java.io.FileInputStream;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.util.HashSet;
import java.util.Set;
import java.util.List;
import java.util.ArrayList;
import java.sql.ResultSet;

public class XMLParser extends DefaultHandler {

    private DatabaseHelper dbHelper;
    private PreparedStatement movieInsertStmt, genreInsertStmt, genreMovieLinkStmt, starInsertStmt, starMovieLinkStmt;
    private Set<String> uniqueMovies;
    private Set<String> uniqueActors;
    private Set<String> uniqueGenres;

    // Define handlers as instance variables
    private ActorsHandler actorsHandler;
    private CastsHandler castsHandler;

    public XMLParser(DatabaseHelper dbHelper) throws SQLException {
        this.dbHelper = dbHelper;
        initializeStatements();
        uniqueMovies = new HashSet<>();
        uniqueActors = new HashSet<>();
        uniqueGenres = new HashSet<>();

        // Initialize handlers
        this.actorsHandler = new ActorsHandler();
        this.castsHandler = new CastsHandler();
    }

    private void initializeStatements() throws SQLException {
        movieInsertStmt = dbHelper.getConnection().prepareStatement(
                "INSERT INTO movies (id, title, year, director) VALUES (?, ?, ?, ?)");
        genreInsertStmt = dbHelper.getConnection().prepareStatement(
                "INSERT IGNORE INTO genres (name) VALUES (?)"); // Ignores duplicates
        genreMovieLinkStmt = dbHelper.getConnection().prepareStatement(
                "INSERT INTO genres_in_movies (movie_id, genre_id) VALUES (?, ?)");
        starInsertStmt = dbHelper.getConnection().prepareStatement(
                "INSERT IGNORE INTO stars (name) VALUES (?)"); // Ignores duplicates
        starMovieLinkStmt = dbHelper.getConnection().prepareStatement(
                "INSERT INTO stars_in_movies (star_id, movie_id) VALUES (?, ?)");
    }

    public void parseMoviesFile(String filePath) {
        parseFile(filePath, new MainsHandler());
    }

    public void parseCastsFile(String filePath) {
        parseFile(filePath, castsHandler);
    }

    public void parseActorsFile(String filePath) {
        parseFile(filePath, actorsHandler);
    }

    private void parseFile(String filePath, DefaultHandler handler) {
        try {
            SAXParserFactory factory = SAXParserFactory.newInstance();
            SAXParser saxParser = factory.newSAXParser();
            InputSource inputSource = new InputSource(new FileInputStream(filePath));
            inputSource.setEncoding("ISO-8859-1");
            saxParser.parse(inputSource, handler);
            dbHelper.commit();
        } catch (Exception e) {
            System.out.println("Error parsing file: " + e.getMessage());
            e.printStackTrace();
        }
    }

    private class MainsHandler extends DefaultHandler {
        private String currentElement;
        private String movieId, title, directorName;
        private Integer year;
        private List<String> genres;

        private int batchSize = 1000; // declare and initialize batch ?
        private int count = 0; // add batchSize and count variables to control batch execution:

        @Override
        public void startElement(String uri, String localName, String qName, Attributes attributes) throws SAXException {
            currentElement = qName;
            if (qName.equals("film")) {
                movieId = attributes.getValue("fid");
                title = null;
                year = null;
                genres = new ArrayList<>();
            } else if (qName.equals("director")) {
                directorName = null;
            }
        }

        @Override
        public void characters(char[] ch, int start, int length) throws SAXException {
            String data = new String(ch, start, length).trim();
            if (data.isEmpty()) return;

            switch (currentElement) {
                case "dirname":
                    directorName = data;
                    break;
                case "t":
                    title = data.equals("NKT") ? null : data;
                    break;
                case "year":
                    try {
                        year = Integer.parseInt(data);
                    } catch (NumberFormatException e) {
                        System.out.println("Invalid year format: " + data);
                        year = null;
                    }
                    break;
                case "cat":
                    genres.add(data);
                    break;
            }
        }

        @Override
        public void endElement(String uri, String localName, String qName) throws SAXException {
            if (qName.equals("film")) {
                try {
                    movieInsertStmt.setString(1, movieId);
                    movieInsertStmt.setString(2, title);
                    if (year != null) {
                        movieInsertStmt.setInt(3, year);
                    } else {
                        movieInsertStmt.setNull(3, java.sql.Types.INTEGER);  // Insert null if year is invalid
                    }
                    movieInsertStmt.setString(4, directorName);
                    movieInsertStmt.addBatch();

                    for (String genre : genres) {
                        genreInsertStmt.setString(1, genre);
                        genreInsertStmt.addBatch();
                        genreMovieLinkStmt.setString(1, movieId);
                        genreMovieLinkStmt.setString(2, genre);
                        genreMovieLinkStmt.addBatch();
                    }
                } catch (SQLException e) {
                    e.printStackTrace();
                }
            }
        }
    }

    private class CastsHandler extends DefaultHandler {
        private String currentElement;
        private String movieId, actorName;
        private PreparedStatement lookupStarIdStmt;

        public CastsHandler() throws SQLException {
            lookupStarIdStmt = dbHelper.getConnection().prepareStatement(
                    "SELECT id FROM stars WHERE name = ?");
        }

        @Override
        public void startElement(String uri, String localName, String qName, Attributes attributes) throws SAXException {
            currentElement = qName;
            if (qName.equals("m")) {
                movieId = null;
                actorName = null;
            }
        }

        @Override
        public void characters(char[] ch, int start, int length) throws SAXException {
            String data = new String(ch, start, length).trim();
            if (data.isEmpty()) return;

            if ("f".equals(currentElement)) {
                movieId = data;
            } else if ("a".equals(currentElement)) {
                actorName = data;
            }
        }

        @Override
        public void endElement(String uri, String localName, String qName) throws SAXException {
            if (qName.equals("m") && movieId != null && actorName != null) {
                try {
                    lookupStarIdStmt.setString(1, actorName);
                    ResultSet rs = lookupStarIdStmt.executeQuery();

                    if (rs.next()) {
                        String starId = rs.getString("id");
                        String uniqueLink = starId + "_" + movieId;

                        if (!uniqueMovies.contains(uniqueLink)) {
                            starMovieLinkStmt.setString(1, starId);
                            starMovieLinkStmt.setString(2, movieId);
                            starMovieLinkStmt.addBatch();
                            uniqueMovies.add(uniqueLink);
                        }
                    } else {
                        System.out.println("Actor not found in stars table: " + actorName);
                    }
                    rs.close();
                } catch (SQLException e) {
                    e.printStackTrace();
                }
            }
        }

        public void closeStatements() {
            try {
                if (lookupStarIdStmt != null) {
                    lookupStarIdStmt.close();
                }
            } catch (SQLException e) {
                e.printStackTrace();
            }
        }
    }

    private class ActorsHandler extends DefaultHandler {
        private String currentElement;
        private String actorName;
        private Integer birthYear;
        private int nextStarId;
        private PreparedStatement getMaxStarIdStmt;

        public ActorsHandler() throws SQLException {
            getMaxStarIdStmt = dbHelper.getConnection().prepareStatement(
                    "SELECT MAX(CAST(SUBSTRING(id, 3) AS UNSIGNED)) AS maxId FROM stars");

            ResultSet rs = getMaxStarIdStmt.executeQuery();
            if (rs.next()) {
                nextStarId = rs.getInt("maxId") + 1;
            } else {
                nextStarId = 1;
            }
            rs.close();
        }

        @Override
        public void startElement(String uri, String localName, String qName, Attributes attributes) throws SAXException {
            currentElement = qName;
            if (qName.equals("actor")) {
                actorName = null;
                birthYear = null;
            }
        }

        @Override
        public void characters(char[] ch, int start, int length) throws SAXException {
            String data = new String(ch, start, length).trim();
            if (data.isEmpty()) return;

            switch (currentElement) {
                case "stagename":
                    actorName = data;
                    break;
                case "dob":
                    try {
                        birthYear = Integer.parseInt(data);
                    } catch (NumberFormatException e) {
                        System.out.println("Invalid birth year for actor: " + actorName + " - Data: " + data);
                        birthYear = null;
                    }
                    break;
            }
        }

        @Override
        public void endElement(String uri, String localName, String qName) throws SAXException {
            if (qName.equals("actor") && actorName != null) {
                try {
                    String starId = "s" + nextStarId++;
                    starInsertStmt.setString(0, starId);
                    starInsertStmt.setString(1, actorName);
                    if (birthYear != null) {
                        starInsertStmt.setInt(3, birthYear);
                    } else {
                        starInsertStmt.setNull(3, java.sql.Types.INTEGER);
                    }
                    starInsertStmt.addBatch();
                } catch (SQLException e) {
                    e.printStackTrace();
                }
            }
        }

        public void closeStatements() {
            try {
                if (getMaxStarIdStmt != null) {
                    getMaxStarIdStmt.close();
                }
            } catch (SQLException e) {
                e.printStackTrace();
            }
        }
    }

    @Override
    public void endDocument() throws SAXException {
        try {
            if (starInsertStmt != null) {
                starInsertStmt.executeBatch();
                System.out.println("All actor records have been successfully inserted into the stars table.");
            }

            if (starMovieLinkStmt != null) {
                starMovieLinkStmt.executeBatch();
                System.out.println("All star-movie relationships have been successfully inserted into the stars_in_movies table.");
            }

            dbHelper.getConnection().commit();

        } catch (SQLException e) {
            e.printStackTrace();
            throw new SAXException("Error executing batch insert operations", e);
        } finally {
            try {
                if (starInsertStmt != null) {
                    starInsertStmt.close();
                }
                if (movieInsertStmt != null) {
                    movieInsertStmt.close();
                }
                if (genreInsertStmt != null) {
                    genreInsertStmt.close();
                }
                if (genreMovieLinkStmt != null) {
                    genreMovieLinkStmt.close();
                }
                if (starMovieLinkStmt != null) {
                    starMovieLinkStmt.close();
                }

                // Close statements within handlers
                castsHandler.closeStatements();
                actorsHandler.closeStatements();

            } catch (SQLException e) {
                e.printStackTrace();
            }
        }
    }
}
