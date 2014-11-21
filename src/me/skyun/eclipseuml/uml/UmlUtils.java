package me.skyun.eclipseuml.uml;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import me.skyun.eclipseuml.Utils;
import net.sourceforge.plantuml.BlockUml;
import net.sourceforge.plantuml.FileFormat;
import net.sourceforge.plantuml.FileFormatOption;
import net.sourceforge.plantuml.SourceStringReader;
import net.sourceforge.plantuml.core.Diagram;

import org.eclipse.core.resources.IProject;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.jdt.core.Flags;
import org.eclipse.jdt.core.ICompilationUnit;
import org.eclipse.jdt.core.IMethod;
import org.eclipse.jdt.core.IType;
import org.eclipse.jdt.core.JavaModelException;
import org.eclipse.jdt.core.Signature;
import org.eclipse.jdt.core.search.SearchEngine;
import org.eclipse.jdt.core.search.SearchMatch;
import org.eclipse.jdt.core.search.SearchRequestor;
import org.eclipse.jdt.internal.core.ResolvedSourceType;

@SuppressWarnings("restriction")
public class UmlUtils {

    public static String getSvgString(String uml) {
        try {
            ByteArrayOutputStream baos = new ByteArrayOutputStream();
            SourceStringReader reader = new SourceStringReader(uml);
            List<BlockUml> blocks = reader.getBlocks();
            int imageСounter = 0;
            for (BlockUml block : blocks) {
                Diagram diagram = block.getDiagram();
                int pages = diagram.getNbImages();
                for (int page = 0; page < pages; ++page) {
                    reader.generateImage(baos, imageСounter++, new FileFormatOption(FileFormat.SVG));
                    baos.close();
                }
            }
            return new String(baos.toByteArray());
        } catch (IOException e) {
            e.printStackTrace();
        }
        return "";
    }

    /**
     * search type reference first, and then get the compilation unit uml.
     * 
     * @param shell
     * @param type
     */
    public static String getCompilationUnitUml(final ICompilationUnit compilationUnit) {
        try {
            String uml = "";
            for (IType type : compilationUnit.getAllTypes()) {
                uml += getTypeUml(type) + "\n";
                uml += getSuperTypesUml(type) + "\n";

                Set<IType> referedTypes = new HashSet<IType>();
                SearchRequestor requestor = new ReferedTypeSearchRequestor(type, referedTypes);
                new SearchEngine().searchDeclarationsOfReferencedTypes(type, requestor, null);
                for (IType referedType : referedTypes)
                    uml += getReferedTypesUml(type, referedType);
                uml += "\n";
            }

            uml = String.format("@startuml\n%s@enduml", uml);
            return uml;
        } catch (JavaModelException e) {
            e.printStackTrace();
        }
        return "";
    }

    private static class ReferedTypeSearchRequestor extends SearchRequestor {

        private IType mType;
        private Set<IType> mReferedTypes;
        private IType[] mSuperTypes;

        public ReferedTypeSearchRequestor(IType type, Set<IType> referedTypes) throws JavaModelException {
            mType = type;
            mReferedTypes = referedTypes;
            mSuperTypes = mType.newSupertypeHierarchy(null).getSupertypes(mType);
        }

        @Override
        public void acceptSearchMatch(SearchMatch match) throws CoreException {
            if (!(match.getElement() instanceof ResolvedSourceType))
                return;

            IType referedType = (IType) match.getElement();
            if (referedType.equals(mType))
                return;

            if (isSuperType(referedType))
                return;

            mReferedTypes.add(referedType);
        }

        private boolean isSuperType(IType type) {
            for (IType superType : mSuperTypes) {
                if (type.equals(superType))
                    return true;
            }
            return false;
        }

        @Override
        public void endReporting() {
        }
    }

    private static String getMethodParamUml(IMethod method) throws JavaModelException {
        if (method.getNumberOfParameters() == 0)
            return "";

        String[] paramTypes = method.getParameterTypes();
        String[] paramNames = method.getParameterNames();
        String paramUml = Signature.getSignatureSimpleName(paramTypes[0]) + " " + paramNames[0];
        for (int i = 1; i < paramTypes.length; i++)
            paramUml += ", " + Signature.getSignatureSimpleName(paramTypes[i]) + " " + paramNames[i];
        return paramUml;
    }

    private static String getMethodModifierUml(IMethod method) {
        try {
            switch (method.getFlags()) {
            case Flags.AccPublic:
                return "+";
            case Flags.AccProtected:
                return "#";
            case Flags.AccPrivate:
                return "-";
            default:
                return "";
            }
        } catch (JavaModelException e) {
            e.printStackTrace();
        }
        return "";
    }

    private static String getMethodUml(IMethod method) throws JavaModelException {
        String methodUml = String.format("%s %s %s(%s)\n", getMethodModifierUml(method),
                Signature.getSignatureSimpleName(method.getReturnType()), method.getElementName(),
                getMethodParamUml(method));
        return methodUml;
    }

    private static String getSuperTypesUml(IType type) throws JavaModelException {
        String uml = "'----- super type -----\n";
        IType superClass = type.newSupertypeHierarchy(null).getSuperclass(type);
        uml += getSuperTypeUml(type, superClass, false);
        IType[] superInterfaces = type.newSupertypeHierarchy(null).getSuperInterfaces(type);
        for (IType superInterface : superInterfaces)
            uml += getSuperTypeUml(type, superInterface, true);
        return uml;
    }

    private static String getSuperTypeUml(IType type, IType superType, boolean isInterface) {
        if (type == null || superType == null || !superType.getPath().getFileExtension().equals("java"))
            return "";

        IProject project = superType.getJavaProject().getProject();
        String link = "uml://" + Utils.getFullPath(project, superType.getPath()).toString();
        String typeTag = "class";
        String arrow = "-u-|>";
        if (isInterface) {
            typeTag = "interface";
            arrow = ".r.|>";
        }
        String uml = String.format("%s %s [[%s]] {\n}\n", typeTag, superType.getElementName(), link)
                + String.format("%s %s %s\n", type.getElementName(), arrow, superType.getElementName());
        return uml;
    }

    private static String getTypeUml(IType type) throws JavaModelException {
        String methodsUml = "";
        for (IMethod method : type.getMethods())
            methodsUml += getMethodUml(method);

        String typeUml = String.format("'================== class %s ==================\n", type.getElementName());
        typeUml += String.format("class %s #cc8080 {\n%s}\n", type.getElementName(), addIndent(methodsUml));
        return typeUml;
    }

    private static String getReferedTypesUml(IType type, IType referedType) {
        String link = "uml://"
                + Utils.getFullPath(type.getJavaProject().getProject(), referedType.getPath()).toString();
        String uml = String.format("class %s [[%s]] {\n}\n", referedType.getElementName(), link)
                + String.format("%s --> %s\n", type.getElementName(), referedType.getElementName());
        return uml;
    }

    private static String addIndent(String content) {
        String s = "";
        for (String line : content.split("\n"))
            s += "    " + line + "\n";
        return s;
    }
}
