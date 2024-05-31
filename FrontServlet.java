package mg.itu.prom16;

import java.io.File;
import java.io.IOException;
import java.sql.Connection;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

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
import mg.itu.prom16.Mapping;

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
            try (PrintWriter out = response.getWriter()) 
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
        
                Mapping mapping = this.getMappings().get(fullUrl.toString());
                Class<?>clazz=Class.forName(mapping.getClassName());
                Object controleur = clazz.getDeclaredConstructor().newInstance();
                Method methode=controleur.getClass().getMethod(mapping.getMethodName());
                methode.setAccessible(true);
                String retour=(String)methode.invoke(controleur);
                out.print(retour);
            } 
            catch (Exception e) 
            {
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
        DocumentBuilderFactory factory=DocumentBuilderFactory.newInstance();
        DocumentBuilder builder=factory.newDocumentBuilder();
        Document document=builder.parse(path.getPath()+"/"+"web.xml");
        NodeList balise=document.getElementsByTagName("packageCtrl");
        Element element=(Element)balise.item(0);
        String value=((Node) element).getTextContent();

        return value;
    }

    public HashMap<String,Mapping> scanneMapping()throws ServletException
    {
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
        return map;
    }
}