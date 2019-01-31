public class Movie {

    private String title;
    private String poster;
    private String overview;
    private String rating;
    private String releasedate;
    private double popularity;

    public Movie(String title, String poster, String overview, String rating, String releasedate, double popularity) {
        this.title = title;
        this.poster = poster;
        this.overview = overview;
        this.rating = rating;
        this.releasedate = releasedate;
        this.popularity = popularity;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getPoster() {
        return poster;
    }

    public void setPoster(String poster) {
        this.poster = poster;
    }

    public String getOverview() {
        return overview;
    }

    public void setOverview(String overview) {
        this.overview = overview;
    }

    public String getRating() {
        return rating;
    }

    public void setRating(String rating) {
        this.rating = rating;
    }

    public String getReleasedate() {
        return releasedate;
    }

    public void setReleasedate(String releasedate) {
        this.releasedate = releasedate;
    }

    public double getPopularity() {
        return popularity;
    }

    public void setPopularity(double popularity) {
        this.popularity = popularity;
    }
}
