package org.intellij.plugins;

import com.intellij.openapi.components.ProjectComponent;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.projectRoots.ProjectRootManager;
import com.intellij.openapi.projectRoots.ProjectRootType;
import com.intellij.openapi.util.DefaultJDOMExternalizer;
import com.intellij.openapi.util.InvalidDataException;
import com.intellij.openapi.util.JDOMExternalizable;
import com.intellij.openapi.util.WriteExternalException;
import com.intellij.openapi.vfs.VfsUtil;
import com.intellij.openapi.vfs.VirtualFile;
import org.intellij.plugins.ui.pathlisteditor.PathListElement;
import org.jdom.Element;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.beans.PropertyChangeListener;
import java.beans.PropertyChangeSupport;

//TODO: Clean up this class from decompiled items and comments

public class ExcludedPathsFromVcsConfiguration
      implements ListenerNotifier, JDOMExternalizable, ProjectComponent {
   public static final String EXCLUDE_FROM_VCS       = "excludeFromVCS";
   public static final String FILE                   = "file";
   public static final String DIRECTORY              = "directory";
   public static final String URL                    = "url";
   public static final String INCLUDE_SUBDIRECTORIES = "includeSubdirectories";
   private PropertyChangeSupport listenerSupport = new PropertyChangeSupport(this);

//    private List a()
//    {
//       try {
//          return Arrays.asList(new Pattern[] {
//             a(".+\\.(properties|xml|html)"), a(".+\\.(gif|png|jpeg|jpg)")
//          });
//       } catch (MalformedPatternException e) {
//          a.error(e);
//          return Collections.EMPTY_LIST;
//       }
//    }
//
//    private Pattern a(String s1)
//        throws MalformedPatternException
//    {
//        Pattern pattern;
//        if(SystemInfo.isFileSystemCaseSensitive)
//            pattern = b.compile(s1);
//        else
//            pattern = b.compile(s1, 1);
//        return pattern;
//    }

   public ExcludedPathsFromVcsConfiguration(Project project) {
//        DEFAULT_COMPILER = "Javac";
//        SYNCHRONIZE_OUTPUT_DIRECTORY = false;
//        OUTPUT_MODE = "single";
      excludedPathsFromVcs = new ArrayList();
//        resourcePatterns = new ArrayList(a());
//        e = new HashMap();
      this.project = project;
   }

   public void disposeComponent() {
   }

   public void initComponent() {
   }

   public void projectClosed() {
   }

   public void projectOpened() {
   }

//    public String sourcePaths_b(String s1)
//    {
//        if("single".equals(j()))
//            return i();
//        if("source".equals(j()))
//            return s1;
//        if(!SystemInfo.isFileSystemCaseSensitive)
//            s1 = CompilerUtil.a(s1, '/');
//        if(!e.containsKey(s1))
//            return i();
//        else
//            return (String)e.get(s1);
//    }

//    public String[] b()
//    {
//        ArrayList arraylist = new ArrayList();
//        if("multiple".equals(j()))
//        {
//            arraylist.addAll(e.values());
//            if(i() != null)
//                arraylist.add(i());
//        } else
//        if("source".equals(j()))
//            ApplicationManager.getApplication().runReadAction(new w(this, arraylist));
//        else
//        if("single".equals(j()) && i() != null)
//            arraylist.add(i());
//        return (String[])arraylist.toArray(new String[arraylist.size()]);
//    }

//    public boolean c()
//    {
//        return "multiple".equals(j()) || "source".equals(j());
//    }
//
//    public void a(String s1, String s2)
//    {
//        if(!SystemInfo.isFileSystemCaseSensitive)
//            s1 = CompilerUtil.a(s1, '/');
//        e.put(s1, s2);
//    }
//
//    public void d()
//    {
//        e.clear();
//    }
//
//    public String[] e()
//    {
//        String as[] = new String[resourcePatterns.size()];
//        int i1 = 0;
//        for(Iterator iterator = resourcePatterns.iterator(); iterator.hasNext();)
//            as[i1++] = ((Pattern)iterator.next()).getPattern();
//
//        return as;
//    }
//
//    public void c(String s1)
//        throws MalformedPatternException
//    {
//        Pattern pattern = a(s1);
//        if(pattern != null)
//            resourcePatterns.add(pattern);
//    }
//
//    public void f()
//    {
//        resourcePatterns.clear();
//    }
//
//    public boolean d(String s1)
//    {
//        Perl5Matcher perl5matcher = new Perl5Matcher();
//        for(Iterator iterator = resourcePatterns.iterator(); iterator.hasNext();)
//        {
//            Pattern pattern = (Pattern)iterator.next();
//            if(perl5matcher.matches(s1, pattern))
//                return true;
//        }
//
//        return false;
//    }

   public List getExcludedPaths() {
      return excludedPathsFromVcs;
   }

   public void addExcludedPath(PathListElement element) {
      excludedPathsFromVcs.add(element);
   }

   public void resetExcludedPaths(List excludedPaths) {
      excludedPathsFromVcs = new ArrayList(excludedPaths);
      notifyListenersOfChange();
   }

   public void readExternal(Element element)
         throws InvalidDataException {
      DefaultJDOMExternalizer.readExternal(this, element);
      Element element1 = element.getChild(EXCLUDE_FROM_VCS);
      if (element1 != null) {
         for (Iterator iterator = element1.getChildren().iterator(); iterator.hasNext();) {
            Element element2 = (Element) iterator.next();
            String  s1       = element2.getAttributeValue(URL);
            if (s1 != null) {
               if (FILE.equals(element2.getName())) {
                  PathListElement cf1 = new PathListElement(s1, false, true);
                  excludedPathsFromVcs.add(cf1);
               }
               if (DIRECTORY.equals(element2.getName())) {
                  boolean flag = true;
                  if ("false".equals(element2.getAttributeValue(INCLUDE_SUBDIRECTORIES)))
                  flag = false;
                  PathListElement cf2 = new PathListElement(s1, flag, false);
                  excludedPathsFromVcs.add(cf2);
               }
            }
         }

      }
//        element1 = element.getChild("sourceToOutputPathMap");
//        if(element1 != null)
//        {
//            for(Iterator iterator1 = element1.getChildren("mapEntry").iterator(); iterator1.hasNext();)
//            {
//                Element element3 = (Element)iterator1.next();
//                String s2 = element3.getAttributeValue("sourcePath");
//                String s4 = element3.getAttributeValue("outputPath");
//                if(s2 != null && !"".equals(s2) && s4 != null && !"".equals(s4))
//                    a(s2, s4);
//            }
//
//        }
//        f();
//        element1 = element.getChild("resourceExtensions");
//        if(element1 != null)
//            try
//            {
//                for(Iterator iterator2 = element1.getChildren("entry").iterator(); iterator2.hasNext();)
//                {
//                    Element element4 = (Element)iterator2.next();
//                    String s3 = element4.getAttributeValue("name");
//                    if(s3 != null && !"".equals(s3))
//                        c(s3);
//                }
//
//            }
//            catch(MalformedPatternException malformedpatternexception)
//            {
//                a.error(malformedpatternexception);
//            }
   }

   public void writeExternal(Element element)
         throws WriteExternalException {
      DefaultJDOMExternalizer.writeExternal(this, element);
      if (excludedPathsFromVcs.size() > 0) {
         Element element1 = new Element(EXCLUDE_FROM_VCS);
         for (Iterator iterator = excludedPathsFromVcs.iterator(); iterator.hasNext();) {
            PathListElement cf1 = (PathListElement) iterator.next();
            if (cf1.isFile()) {
               Element element4 = new Element(FILE);
               element4.setAttribute(URL, cf1.getUrl());
               element1.addContent(element4);
            } else {
               Element element5 = new Element(DIRECTORY);
               element5.setAttribute(URL, cf1.getUrl());
               element5.setAttribute(INCLUDE_SUBDIRECTORIES, cf1.isIncludeSubDirectories() ? "true" : "false");
               element1.addContent(element5);
            }
         }

         element.addContent(element1);
      }
//        if(e.size() > 0)
//        {
//            Element element2 = new Element("sourceToOutputPathMap");
//            Element element6;
//            for(Iterator iterator1 = e.keySet().iterator(); iterator1.hasNext(); element2.addContent(element6))
//            {
//                String s1 = (String)iterator1.next();
//                String s2 = (String)e.get(s1);
//                element6 = new Element("mapEntry");
//                element6.setAttribute("sourcePath", s1);
//                element6.setAttribute("outputPath", s2);
//            }
//
//            element.addContent(element2);
//        }
//        String as[] = e();
//        Element element3 = new Element("resourceExtensions");
//        for(int i1 = 0; i1 < as.length; i1++)
//        {
//            String s3 = as[i1];
//            Element element7 = new Element("entry");
//            element7.setAttribute("name", s3);
//            element3.addContent(element7);
//        }
//
//        element.addContent(element3);
   }

   public static ExcludedPathsFromVcsConfiguration getInstance(Project project) {
      ExcludedPathsFromVcsConfiguration compilerConfiguration =
            (ExcludedPathsFromVcsConfiguration) project.getComponent(ExcludedPathsFromVcsConfiguration.class);
      if (compilerConfiguration == null)
         throw new RuntimeException(
               ExcludedPathsFromVcsConfiguration.class.getName() + " not configured as a component");
      return compilerConfiguration;
   }

   public boolean reject(VirtualFile file, boolean flag) {
      for (Iterator iterator = excludedPathsFromVcs.iterator(); iterator.hasNext();) {
         PathListElement excludedPath = (PathListElement) iterator.next();
         VirtualFile     excludedFile = excludedPath.getVirtualFile();
         if (excludedFile != null)
            if (excludedPath.isFile()) {
               if (excludedFile.equals(file))
                  return true;
            } else if (excludedPath.isIncludeSubDirectories()) {
               if (VfsUtil.isAncestor(excludedFile, file, false))
                  return true;
            } else if (!file.isDirectory() && excludedFile.equals(file.getParent()))
               return true;
      }

      if (flag) {
         VirtualFile avirtualfile[] = ProjectRootManager.getInstance(project).getRootFiles(ProjectRootType.EXCLUDE);
         for (int i1 = 0; i1 < avirtualfile.length; i1++) {
            VirtualFile virtualfile2 = avirtualfile[i1];
            if (VfsUtil.isAncestor(virtualfile2, file, false))
               return true;
         }

      }
      return false;
   }

//    public void e(String s1)
//    {
//        DEFAULT_OUTPUT_PATH = s1;
//    }

//    public String i()
//    {
//        return DEFAULT_OUTPUT_PATH;
//    }

//    public String j()
//    {
//        return OUTPUT_MODE;
//    }
//
//    public void f(String s1)
//    {
//        a.assertTrue("single".equals(s1) || "multiple".equals(s1) || "source".equals(s1));
//        OUTPUT_MODE = s1;
//    }

   public String getComponentName() {
      return getClass().getName();
   }

//    public String k()
//    {
//        return DEFAULT_COMPILER;
//    }
//
//    public void g(String s1)
//    {
//        a.assertTrue(s1.equals("Javac") || s1.equals("Jikes"), "Unsupported compiler");
//        DEFAULT_COMPILER = s1;
//    }

//    public boolean l()
//    {
//        return SYNCHRONIZE_OUTPUT_DIRECTORY;
//    }
//
//    public void a(boolean flag)
//    {
//        SYNCHRONIZE_OUTPUT_DIRECTORY = flag;
//    }
//
   static Project a(ExcludedPathsFromVcsConfiguration v1) {
      return v1.project;
   }

//    private static final Logger a = Logger.getInstance("#com.intellij.compiler.CompilerConfiguration");
//    private static final PatternCompiler b = new Perl5Compiler();
   public static final int DEPENDENCY_FORMAT_VERSION = 18;
//    public static final String JAVAC = "Javac";
//    public static final String JIKES = "Jikes";
//    public String DEFAULT_COMPILER;
//    public boolean SYNCHRONIZE_OUTPUT_DIRECTORY;
//    public String DEFAULT_OUTPUT_PATH;
//    public static final String SINGLE = "single";
//    public static final String MULTIPLE = "multiple";
//    public static final String SOURCES = "source";
//    public String OUTPUT_MODE;
   private List excludedPathsFromVcs;
//    private List resourcePatterns;
//    private Map e;
   private Project project;

   public PropertyChangeListener[] getListeners() {
      return listenerSupport.getPropertyChangeListeners();
   }

   public void addListener(PropertyChangeListener listener) {
      listenerSupport.addPropertyChangeListener(listener);
   }

   public void notifyListenersOfChange() {
      listenerSupport.firePropertyChange("configuration",null,this);
   }

   public void removeListener(PropertyChangeListener listener) {
      listenerSupport.removePropertyChangeListener(listener);
   }
//    private static final String h = "resourceExtensions";
//    private static final String o = "sourceToOutputPathMap";
//    private static final String p = "mapEntry";
//    private static final String q = "sourcePath";
//    private static final String r = "outputPath";

}
