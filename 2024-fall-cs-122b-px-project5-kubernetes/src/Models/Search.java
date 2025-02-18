package Models;

public class Search {
    private String title;
    private String year;
    private String director;
    private String star;

    // Constructor
    public Search() {
        // Default constructor
    }

    // Getters
    public String getTitle() {
        return title;
    }

    public String getYear() {
        return year;
    }

    public String getDirector() {
        return director;
    }

    public String getStar() {
        return star;
    }

    // Setters
    public void setTitle(String title) {
        this.title = title;
    }

    public void setYear(String year) {
        this.year = year;
    }

    public void setDirector(String director) {
        this.director = director;
    }

    public void setStar(String star) {
        this.star = star;
    }
}
