package ma.emsi.ramiharoun22.tp0ramiharoun22.jsf;

import jakarta.faces.application.FacesMessage;
import jakarta.faces.context.FacesContext;
import jakarta.faces.model.SelectItem;
import jakarta.faces.view.ViewScoped;
import jakarta.inject.Inject;
import jakarta.inject.Named;
import ma.emsi.ramiharoun22.tp0ramiharoun22.llm.LlmInteraction;
import ma.emsi.ramiharoun22.tp0ramiharoun22.llm.JsonUtilPourGemini;


import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

@Named
@ViewScoped
public class Bb implements Serializable {

    private String roleSysteme;
    private boolean roleSystemeChangeable = true;
    private List<SelectItem> listeRolesSysteme;
    private String question;
    private String reponse;
    private StringBuilder conversation = new StringBuilder();

    @Inject
    private FacesContext facesContext;
    @Inject
    private JsonUtilPourGemini jsonUtil;

    // === Champs pour le mode Debug (affichage des JSON) ===
    private String texteRequeteJson;
    private String texteReponseJson;

    public String getTexteRequeteJson() {
        return texteRequeteJson;
    }

    public void setTexteRequeteJson(String texteRequeteJson) {
        this.texteRequeteJson = texteRequeteJson;
    }

    public String getTexteReponseJson() {
        return texteReponseJson;
    }

    public void setTexteReponseJson(String texteReponseJson) {
        this.texteReponseJson = texteReponseJson;
    }


    public Bb() {}

    public String getRoleSysteme() {
        return roleSysteme;
    }

    public void setRoleSysteme(String roleSysteme) {
        this.roleSysteme = roleSysteme;
    }

    public boolean isRoleSystemeChangeable() {
        return roleSystemeChangeable;
    }

    public String getQuestion() {
        return question;
    }

    public void setQuestion(String question) {
        this.question = question;
    }

    public String getReponse() {
        return reponse;
    }

    public void setReponse(String reponse) {
        this.reponse = reponse;
    }

    public String getConversation() {
        return conversation.toString();
    }

    public void setConversation(String conversation) {
        this.conversation = new StringBuilder(conversation);
    }

    public String envoyer() {
        if (question == null || question.isBlank()) {
            FacesMessage message = new FacesMessage(FacesMessage.SEVERITY_ERROR,
                    "Texte question vide", "Il manque le texte de la question");
            facesContext.addMessage(null, message);
            return null;
        }
        if (this.conversation.isEmpty()) {
            // Si l’utilisateur n’a rien choisi, mets une valeur par défaut
            if (this.roleSysteme == null || this.roleSysteme.isBlank()) {
                this.roleSysteme = "You are a helpful assistant. Answer clearly and concisely.";
            }
            jsonUtil.setSystemRole(this.roleSysteme);
            this.roleSystemeChangeable = false;
        }

        // === AJOUT DU TRY/CATCH ICI ===
        try {
            LlmInteraction interaction = jsonUtil.envoyerRequete(question);
            this.reponse = interaction.reponseExtraite();
            this.texteRequeteJson = interaction.questionJson();
            this.texteReponseJson = interaction.reponseJson();
        } catch (Exception e) {
            FacesMessage message =
                    new FacesMessage(FacesMessage.SEVERITY_ERROR,
                            "Problème de connexion avec l'API du LLM",
                            "Problème de connexion avec l'API du LLM" + e.getMessage());
            facesContext.addMessage(null, message);
        }
        // ================================

        afficherConversation();
        return null;
    }

    private String nettoyerTexte(String s) {
        if (s == null) return "";
        String normalise = java.text.Normalizer.normalize(s, java.text.Normalizer.Form.NFD);
        String sansAccents = normalise.replaceAll("\\p{InCombiningDiacriticalMarks}+", "");
        String sansPonctuation = sansAccents.replaceAll("[^a-zA-Z0-9\\s]", "");
        return sansPonctuation.toLowerCase(Locale.FRENCH).trim().replaceAll("\\s+", " ");
    }

    private String cesar(String s, int shift) {
        if (s == null) return "";
        int n = ((shift % 26) + 26) % 26;
        StringBuilder sb = new StringBuilder();
        for (char c : s.toCharArray()) {
            if (c >= 'A' && c <= 'Z') {
                sb.append((char) ('A' + (c - 'A' + n) % 26));
            } else if (c >= 'a' && c <= 'z') {
                sb.append((char) ('a' + (c - 'a' + n) % 26));
            } else {
                sb.append(c);
            }
        }
        return sb.toString();
    }

    public String nouveauChat() {
        return "index";
    }

    private void afficherConversation() {
        this.conversation.append("== User:\n").append(question)
                .append("\n== Serveur:\n").append(reponse).append("\n");
    }

    public List<SelectItem> getRolesSysteme() {
        if (this.listeRolesSysteme == null) {
            this.listeRolesSysteme = new ArrayList<>();
            String role = """
                    You are a helpful assistant. You help the user to find the information they need.
                    If the user type a question, you answer it.
                    """;
            this.listeRolesSysteme.add(new SelectItem(role, "Assistant"));

            role = """
                    You are an interpreter. You translate from English to French and from French to English.
                    If the user type a French text, you translate it into English.
                    If the user type an English text, you translate it into French.
                    If the text contains only one to three words, give some examples of usage of these words in English.
                    """;
            this.listeRolesSysteme.add(new SelectItem(role, "Traducteur Anglais-Français"));

            role = """
                    Your are a travel guide. If the user type the name of a country or of a town,
                    you tell them what are the main places to visit in the country or the town
                    are you tell them the average price of a meal.
                    """;
            this.listeRolesSysteme.add(new SelectItem(role, "Guide touristique"));

            role = """
                    You are an ancient and wise mage who speaks with mystical wisdom and poetic tone.
                    When the user asks a question, you answer as if you were casting words of magic,
                    mixing clarity with mystery, and using metaphors from arcane knowledge and the elements.
                    Always stay kind, wise, and slightly enigmatic, as if revealing secrets from another realm.
                    """;
            this.listeRolesSysteme.add(new SelectItem(role, "Mage"));

        }
        return this.listeRolesSysteme;
    }

    public void toggleDebug() {
        this.setDebug(!isDebug());
    }

    private boolean debug;

    public boolean isDebug() {
        return debug;
    }

    public void setDebug(boolean debug) {
        this.debug = debug;
    }
}
