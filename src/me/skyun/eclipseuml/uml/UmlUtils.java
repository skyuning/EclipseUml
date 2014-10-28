package me.skyun.eclipseuml.uml;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.List;

import org.eclipse.jdt.core.Flags;
import org.eclipse.jdt.core.ICompilationUnit;
import org.eclipse.jdt.core.IMethod;
import org.eclipse.jdt.core.IType;
import org.eclipse.jdt.core.JavaModelException;
import org.eclipse.jdt.core.Signature;
import org.eclipse.jdt.internal.core.SourceRefElement;

import me.skyun.eclipseuml.Utils;
import net.sourceforge.plantuml.BlockUml;
import net.sourceforge.plantuml.FileFormat;
import net.sourceforge.plantuml.FileFormatOption;
import net.sourceforge.plantuml.SourceStringReader;
import net.sourceforge.plantuml.core.Diagram;

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

    public static String getMethodParamUml(IMethod method) throws JavaModelException {
        if (method.getNumberOfParameters() == 0)
            return "";
    
        String[] paramTypes = method.getParameterTypes();
        String[] paramNames = method.getParameterNames();
        String paramUml = Signature.getSignatureSimpleName(paramTypes[0]) + " " + paramNames[0];
        for (int i = 1; i < paramTypes.length; i++)
            paramUml += ", " + Signature.getSignatureSimpleName(paramTypes[i]) + " " + paramNames[i];
        return paramUml;
    }

    public static String getMethodModifier(IMethod method) {
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

    public static String getMethodUml(IMethod method) throws JavaModelException {
        String methodUml = String.format("%s %s %s(%s)\n", getMethodModifier(method),
                Signature.getSignatureSimpleName(method.getReturnType()), method.getElementName(),
                getMethodParamUml(method));
        return methodUml;
    }

    public static String getSuperTypeUml(IType type) throws JavaModelException {
        SourceRefElement superClass = (SourceRefElement) type.newSupertypeHierarchy(null).getSuperclass(type);
        if (superClass == null || !superClass.getPath().getFileExtension().equals("java"))
            return "";
    
        String link = "uml://" + Utils.getFullPath(superClass.getJavaProject().getProject(), superClass.getPath()).toString();
        String uml = String.format("class %s [[%s]] {\n}\n", superClass.getElementName(), link)
                + String.format("%s -u-|> %s\n", type.getElementName(), superClass.getElementName());
        return uml;
    }

    public static String getTypeUml(IType type) throws JavaModelException {
        String methodsUml = "";
        for (IMethod method : type.getMethods())
            methodsUml += getMethodUml(method);
    
        String typeUml = "";
        typeUml += String.format("class %s {\n%s}\n", type.getElementName(), methodsUml);
    
        typeUml += getSuperTypeUml(type);
        return typeUml;
    }

    public static String getCompilationUnitUml(ICompilationUnit compilationUnit) throws JavaModelException {
        String uml = "";
        for (IType type : compilationUnit.getAllTypes())
            uml += getTypeUml(type);
        uml = String.format("@startuml\n%s@enduml", uml);
        return uml;
    }

}
