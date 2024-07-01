package mg.itu.prom16;
import jakarta.servlet.http.HttpSession;

public class MySession
{
    HttpSession session;

    public HttpSession getSession() {
        return session;
    }

    public void setSession(HttpSession session) {
        this.session = session;
    }

    public Object getSession(String key)
    {
        return this.session.getAttribute(key);
    }

    public void addSession(String key,Object objet)
    {
        this.session.setAttribute(key,objet);
    }

    public void removeSession(String key)
    {
        this.session.removeAttribute(key);
    }

    // public void updateSession(String key,Object objet)
    // {
    //     this.session.setAttribute(key,objet);
    // }
}