package me.skyun.eclipseuml;

import java.io.FileOutputStream;
import java.io.IOException;

import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IWorkspaceRoot;
import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.core.runtime.IPath;
import org.eclipse.ui.IViewPart;
import org.eclipse.ui.IWorkbenchPage;
import org.eclipse.ui.PartInitException;
import org.eclipse.ui.PlatformUI;

public class Utils {

    public static IPath getFullPath(IProject project, IPath path) {
        IPath relativePath = path.makeRelativeTo(project.getLocation());
        IPath fullPath = project.getFile(relativePath).getLocation();
        return fullPath;
    }

    public static IWorkspaceRoot getWorkspaceRoot() {
        return ResourcesPlugin.getWorkspace().getRoot();
    }

    public static boolean isJavaFile(IPath path) {
        return path.getFileExtension().equals("java");
    }

    public static IWorkbenchPage getActivePage() {
        return PlatformUI.getWorkbench().getActiveWorkbenchWindow().getActivePage();
    }

    public static IViewPart openView(String viewId) {
        try {
            return PlatformUI.getWorkbench().getActiveWorkbenchWindow().getActivePage().showView(viewId);
        } catch (PartInitException e) {
            e.printStackTrace();
        }
        return null;
    }

    public static void saveFile(String filename, String content) throws IOException {
        FileOutputStream os = new FileOutputStream(filename);
        os.write(content.getBytes());
        os.flush();
        os.close();
    }
}
