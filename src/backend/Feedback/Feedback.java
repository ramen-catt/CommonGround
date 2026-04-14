class FeedBack {
    private int feedbackId;
    private int buyerId;
    private int sellerId;
    private int listingId;
    private int rating;
    private String ratingDesc;
    private String reportDesc;
    private boolean report;

    public FeedBack(int buyerId, int sellerId, int listingId, int rating, String ratingDesc) {
        this.buyerId = buyerId;
        this.sellerId = sellerId;
        this.listingId = listingId;
        this.rating = rating; 
        this.ratingDesc = ratingDesc;
        this.report = false;
    }

    public FeedBack(int buyerId, int sellerId, int listingId, int rating, String ratingDesc, String reportDesc) {
        this.buyerId = buyerId;
        this.sellerId = sellerId;
        this.listingId = listingId;
        this.rating = rating; 
        this.ratingDesc = ratingDesc;
        this.reportDesc = reportDesc;
        this.report = true;
    }

    public int getFeedbackId(){
        return feedbackId;
    }

    public void setFeedbackId(int feedbackId){
        this.feedbackId = feedbackId;
    }

    public int getBuyerId() {
        return buyerId;
    }

    public int getSellerId(){
        return sellerId;
    }

    public int getListingId() {
        return listingId;
    }

    public int getRating() {
        return rating;
    }

    public String getRatingDesc(){
        return ratingDesc;
    }

    public String getReportDesc(){
        return  reportDesc;
    }

    public boolean isReport(){
        return report;
    }

    public void setReport(boolean report){
        this.report = report;
    }

    @Override
    public String toString(){
        String type = report ? "REPORT" : "REVIEW";
        String base = "FeedbackID: " + feedbackId +
               " | Type: " + type +
               " | Buyer: " + buyerId +
               " | Seller: " + sellerId +
               " | Listing: " + listingId +
               " | Rating: " + rating +
               " (" + (reportDesc != null ? ratingDesc : "N/A") + ")";
        if (report && reportDesc != null) {
            base += " | Report: " + reportDesc;
        }
        return base;
    }
}
