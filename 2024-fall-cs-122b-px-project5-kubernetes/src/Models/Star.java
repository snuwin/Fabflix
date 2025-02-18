package Models;

import java.util.ArrayList;

public class Star {
    private String id;
    private String birthYear;
    private String name;
    private ArrayList<Movie> movies;

    public Star(String id, String birthYear, String name) {
        this.id = id;
        this.birthYear = birthYear;
        this.name = name;
    }
    public Star(String id, String birthYear, String name, ArrayList<Movie> movies) {
        this.movies = movies;
        this.id = id;
        this.birthYear = birthYear;
        this.name = name;
    }
    public ArrayList<Movie> getMovies() {
        return movies;
    }
    public void setMovies(ArrayList<Movie> movies) {
        this.movies = movies;
    }
    public String getId() {
        return id;
    }
    public void setId(String id) {
        this.id = id;
    }
    public String getBirthYear() {
        return birthYear;
    }
    public void setBirthYear(String birthYear) {
        this.birthYear = birthYear;
    }
    public String getName() {
        return name;
    }
    public void setName(String name) {
        this.name = name;
    }


}