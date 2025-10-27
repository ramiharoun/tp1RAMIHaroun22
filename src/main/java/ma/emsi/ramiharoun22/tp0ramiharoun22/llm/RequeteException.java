package ma.emsi.ramiharoun22.tp0ramiharoun22.llm;




public class RequeteException extends Exception {


    private String jsonRequete;
    public RequeteException(String message) {
        super(message);
    }
    public RequeteException(String message, String jsonRequete) {
        super(message);
        this.jsonRequete = jsonRequete;
    }

    public String getJsonRequete() {
        return jsonRequete;
    }
}
