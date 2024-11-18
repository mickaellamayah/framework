package mg.itu.prom16;

import javax.servlet.http.HttpSession;
import java.io.IOException;

public class MySession {
    private HttpSession session;

    public MySession(HttpSession session) {
        this.session = session;
    }

    public void add(String key, Object object) throws Exception {
        if (session.getAttribute(key) == null) {
            session.setAttribute(key, object);
        } else {
            throw new Exception("Key existante dans la session.");
        }
    }

    public Object get(String key) {
        return session.getAttribute(key);
    }

    public void delete(String key) throws Exception {
        if (session.getAttribute(key) != null) {
            session.removeAttribute(key);
        } else {
            throw new Exception("Key introuvable dans la session.");
        }
    }
}