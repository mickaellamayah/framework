package mg.itu.prom16;

public class VerbAction {
    String url;
    String verb;

    public VerbAction() {
    }

    public VerbAction(String url, String verb) {
        this.url = url;
        this.verb = verb;
    }

    public String getUrl() {
        return url;
    }

    public void setUrl(String url) {
        this.url = url;
    }

    public String getVerb() {
        return verb;
    }

    public void setVerb(String verb) {
        this.verb = verb;
    }

    public boolean verifFormulaire(String url, String verb) {
        if (url.equals(this.getUrl()) && verb.equals(getVerb())) {
            return true;
        } else {
            return false;
        }
    }
}