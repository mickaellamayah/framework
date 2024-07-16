package modelView;

import java.util.HashMap;

public class ModelView {
    String url;
    HashMap<String, Object> data = new HashMap<>();

    public String getUrl() {
        return url;
    }

    public void setUrl(String url) {
        this.url = url;
    }

    public HashMap<String, Object> getData() {
        return data;
    }

    public void setData(HashMap<String, Object> data) {
        this.data = data;
    }

    public void addObject(String cle, Object object) {
        this.data.put(cle, object);
    }

    public void addVariable(String nom, Object objet) {
        this.getData().put(nom, objet);
    }
}