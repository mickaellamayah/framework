package mg.itu.prom16;

import java.io.File;
import java.io.IOException;
import java.sql.Connection;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Set;
import modelView.*;

import javax.sql.rowset.serial.SerialException;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;

import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

import java.io.PrintWriter;
import java.lang.annotation.Annotation;
import java.lang.reflect.Method;
import java.lang.reflect.Parameter;
import java.net.http.HttpRequest;

import jakarta.servlet.RequestDispatcher;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.ServletContext;
import mesAnnotations.AnnotationControleur;
import mesAnnotations.AnnotationGet;
import mesAnnotations.Param;
import mg.itu.prom16.Mapping;
import mg.itu.prom16.MySession;

@AnnotationControleur(value="Annotation sur ma classe")
public class FrontServlet extends HttpServlet
{
    List<String> liste;
    HashMap<String,Mapping> mappings;

    public HashMap<String, Mapping> getMappings() {
        return mappings;
    }

    public void setMappings(HashMap<String, Mapping> mappings) {
        this.mappings = mappings;
    }

    public List<String> getListe() {
        return liste;
    }

    public void setListe(List<String> liste) {
        this.liste = liste;
    }

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response)throws ServletException, IOException 
    {
        processRequest(request, response);
    }

    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response)throws ServletException, IOException 
    {
        processRequest(request, response);
    }

    protected void processRequest(HttpServletRequest request,HttpServletResponse response)throws ServletException,IOException
    {
            response.setContentType("text/html");
            PrintWriter out = response.getWriter();
            try
            {
                String contextPath = request.getContextPath();
                String servletPath = request.getServletPath();
                String pathInfo = request.getPathInfo();
                String queryString = request.getQueryString();
        
                StringBuilder fullUrl = new StringBuilder();
                if (servletPath != null) {
                    fullUrl.append(servletPath);
                }
                if (pathInfo != null) {
                    fullUrl.append(pathInfo);
                }
                if (queryString != null) {
                    fullUrl.append("?").append(queryString);
                }
                this.verifDoublant(fullUrl.toString());
        
                Mapping mapping = this.getMappings().get(fullUrl.toString());
                Object result=this.minvoke(mapping,request);

                if (result instanceof String) 
                {
                    String resultString = (String) result;
                    out.print(resultString);
                } 
                else if (result instanceof ModelView) 
                {
                    ModelView modelView = (ModelView) result;
                    String url=modelView.getUrl();
                    HashMap<String,Object> data=modelView.getData();

                    Set<String> keys = data.keySet();
                    for (int i = 0; i < keys.size(); i++) 
                    {
                        String key = (String) keys.toArray()[i];
                        Object value = data.get(key);
                        request.setAttribute(key,value);
                    }
                    RequestDispatcher dispatch=request.getRequestDispatcher(url);
                    dispatch.forward(request,response);
                } 
                else 
                {
                    System.out.println("La méthode a renvoyé un type inattendu : " + result.getClass().getName());
                }
            } 
            catch (Exception e) 
            {
                out.println(e.getMessage());
                e.printStackTrace();
            }
    }

    @Override
    public void init() throws ServletException
    {
        try
        {
           HashMap<String,Mapping> map=this.scanneMapping();
           this.setMappings(map);
        }
        catch(Exception e)
        {
            e.printStackTrace();
        }
    }

    public Object minvoke(Mapping map,HttpServletRequest request)throws Exception
    {
        if(map==null)
        {
            throw new Exception("Url introuvable");
        }

        Class<?> clazz=Class.forName(map.getClassName());
        Object instance=clazz.getDeclaredConstructor().newInstance();
        Method[] allmethods=instance.getClass().getDeclaredMethods();
        Method method = null;

        for (int i = 0; i < allmethods.length; i++) 
        {
            if(allmethods[i].getName().equals(map.getMethodName()))
            {
                method=allmethods[i];
            }
        }
        Parameter[] params=method.getParameters();
        Object[] arguments=new Object[params.length];

        for (int i = 0; i < params.length; i++) 
        {
            if(params[i].isAnnotationPresent(Param.class))
            {
                Param param=params[i].getAnnotation(Param.class);
                String nom=param.nom();
                String value=request.getParameter(nom);
                arguments[i]=value;
            }
            else if(params[i].getType() == MySession.class)
            {
                MySession session=new MySession();
                session.setSession(request.getSession());
                arguments[i]=session;
            }
        }
        method.setAccessible(true);
        Object result = method.invoke(instance,arguments);
        if (result instanceof String || result instanceof ModelView) 
        {
            return result;
        } 
        else
        {
            throw new Exception("Le type de retour doit sêtre String ou ModelView");
        }

    }

    public void verifDoublant(String url)throws Exception
    {
        HashMap<String,Mapping> mapping=this.getMappings();
        if(mapping==null)
        {
            throw new Exception("URL en doublant");
        }
    }

    public static List<File>  scaner(File directory)
    {
        File[] classes=directory.listFiles();
        List <File> all=new ArrayList<>();
        for (int i = 0; i < classes.length; i++) 
        {
            if(classes[i].isDirectory() && !classes[i].getName().contains("mg"))
            {
                all.addAll(scaner(classes[i]));
            }
            else if(classes[i].getName().endsWith(".class"))
            {
                all.add(classes[i]);
            }
        }

        return all;
    }

    public static  List<String> getListeControleur(File file)throws Exception
    {
        List<File> allClass=scaner(file);
        List<String> ClassAnnoter=new ArrayList<>();
        for (int i = 0; i < allClass.size(); i++) 
        {
            String pathClass=allClass.get(i).getName().split("[.]")[0];
            try
            {
                System.out.println(getPackage(file));
                pathClass=getPackage(file)+"."+pathClass;
                Class<?> clazz=null;
                clazz=Class.forName(pathClass);
                if(clazz.isAnnotationPresent(AnnotationControleur.class))
                {
                    ClassAnnoter.add(pathClass);
                }
            }
            catch(Exception e)
            {
                e.printStackTrace();
            }

        }
        return ClassAnnoter;
    }

    private static String removeEndClass(String pathclass)
    {
        pathclass=pathclass.replace("/",".");
        pathclass=pathclass.replace("/",".");
        char[] ch=pathclass.toCharArray();
        ch[ch.length-1]='\0';
        ch[0]='\0';
        ch[1]='\0';
        pathclass=String.valueOf(ch);
        return pathclass;
    }

    public static String getPackage(File path)throws Exception
    {
        // DocumentBuilderFactory factory=DocumentBuilderFactory.newInstance();
        // DocumentBuilder builder=factory.newDocumentBuilder();
        // Document document=builder.parse(path.getPath()+"/"+"web.xml");
        // NodeList balise=document.getElementsByTagName("packageCtrl");
        // Element element=(Element)balise.item(0);
        // String value=((Node) element).getTextContent();

        DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
        DocumentBuilder builder = factory.newDocumentBuilder();
        Document document = builder.parse(path.getPath() + "/web.xml");

        NodeList nodeList = document.getElementsByTagName("packageCtrl");
        if (nodeList.getLength() == 0) {
            throw new Exception("Le tag 'packageCtrl' n'a pas été trouvé dans le fichier web.xml.");
        }

        Element element = (Element) nodeList.item(0);

        Node node = element.getFirstChild(); 
        if (node!= null &&!node.getTextContent().trim().isEmpty()) 
        {
            return node.getTextContent().trim();
        } 
        else 
        {
            throw new Exception("La valeur du package est vide ou n'existe pas.");
        }

    }


    public HashMap<String,Mapping> scanneMapping()throws ServletException,Exception
    {
        List<String> liste=new ArrayList<>();
        HashMap<String,Mapping> map=new HashMap<>();
        ServletContext context=getServletContext();
        String path=context.getRealPath("/");
        File all=new File(path+"WEB-INF");
        try
        {
            List<String> ctrl=FrontServlet.getListeControleur(all);
            for (int i = 0; i < ctrl.size(); i++) 
            {
                String clazz=ctrl.get(i);
                Class<?> clazzs=null;
                clazzs=Class.forName(clazz);
                Object instance = clazzs.getDeclaredConstructor().newInstance();
                Method[] tabmethode=instance.getClass().getDeclaredMethods();

                for (int j = 0; j < tabmethode.length; j++) 
                {
                    if(tabmethode[j].isAnnotationPresent(AnnotationGet.class))
                    {
                        AnnotationGet annot=tabmethode[j].getAnnotation(AnnotationGet.class);
                        String url=annot.url();
                        liste.add(url);
                        Mapping mapping=new Mapping();
                        mapping.setMethodName(tabmethode[j].getName());
                        mapping.setClassName(ctrl.get(i));
                        map.put(url, mapping);
                    }   
                }
            }
        }
        catch(Exception e)
        {
            e.printStackTrace();
        }
        for (int i = 0; i < liste.size(); i++) 
        {
            for (int j = 0; j < liste.size(); j++) 
            {
                if(liste.get(i).equals(liste.get(j)) && i!=j)
                {
                    throw new Exception("URL en doublant"+liste.get(j));
                }
            }    
        }
        return map;
    }
}