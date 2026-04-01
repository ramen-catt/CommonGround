class FeedBack{
    private String name;
    private String email;
    private String message;
    private int rating;

    public FeedBack(String name, String email, String message, int rating){
        this.name = name;
        this.email = email;
        this.message = message;
        this.rating = rating;
    }
    public String toString(){
        return "Name:" + name +
                "\nEmail" + email +
                "\nMessage" + message + "\n";
    }
}
