package Models;

import java.util.ArrayList;

public class Movie {
    private String id;
    private String title;
    private int year;
    private String director;
    private ArrayList<Star> stars;
    private ArrayList<Genre> genres;
    private float rating;


    public Movie(String id, String title, int year, String director, ArrayList<Star> stars, ArrayList<Genre> genres, float rating) {
        this.id = id;
        this.title = title;
        this.year = year;
        this.director = director;
        this.stars = stars;
        this.genres = genres;
        this.rating = rating;
    }



    public Movie(String id, String title) {
        super();
        this.id = id;
        this.title = title;
    }



    public ArrayList<Star> getStars() {
        return stars;
    }

    public void setActors(ArrayList<Star> stars) {
        this.stars = stars;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public int getYear() {
        return year;
    }

    public void setYear(int year) {
        this.year = year;
    }

    public String getDirector() {
        return director;
    }

    public void setDirector(String director) {
        this.director = director;
    }
    public ArrayList<Genre> getGenres() {
        return genres;
    }
    public void setGenres(ArrayList<Genre> genres) {
        this.genres = genres;
    }
    public float getRating() {
        return rating;
    }
    public void setRating(float rating) {
        this.rating = rating;
    }

}
