package me.skyun.eclipseuml.uml;

import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;

import me.skyun.eclipseuml.Utils;

import org.eclipse.core.resources.IFile;
import org.eclipse.core.runtime.IPath;
import org.eclipse.core.runtime.Path;
import org.eclipse.jdt.core.JavaModelException;
import org.eclipse.swt.browser.LocationEvent;
import org.eclipse.swt.browser.LocationListener;
import org.eclipse.ui.IEditorDescriptor;
import org.eclipse.ui.IEditorPart;
import org.eclipse.ui.IWorkbenchPage;
import org.eclipse.ui.PartInitException;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.part.FileEditorInput;

/**
 * intercept uml link
 * get the java file from the link
 * open the editor for the java file
 * @author linyun
 *
 */
public class UmlLinkClickListener implements LocationListener {
    @Override
    public void changing(final LocationEvent event) {
        if (event.location.startsWith("uml://")) {
            try {
                handleLocationEvent(new URI(event.location).getPath());
            } catch (URISyntaxException e) {
                e.printStackTrace();
            } catch (PartInitException e) {
                e.printStackTrace();
            } catch (JavaModelException e) {
                e.printStackTrace();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    @Override
    public void changed(LocationEvent event) {
    }

    private void handleLocationEvent(String fullPath) throws PartInitException, JavaModelException, IOException {
        // get target file
        IPath rootPath = Utils.getWorkspaceRoot().getLocation();
        IPath path = new Path(fullPath);
        if (rootPath.isPrefixOf(path))
            path = path.makeRelativeTo(rootPath);
        IFile file = Utils.getWorkspaceRoot().getFile(path);

        // open target editor
        IWorkbenchPage page = PlatformUI.getWorkbench().getActiveWorkbenchWindow().getActivePage();
        FileEditorInput newInput = new FileEditorInput(file);
        IEditorPart alreadyEditor = page.findEditor(newInput);
        if (alreadyEditor != null)
            page.activate(alreadyEditor);
        else {
            IEditorDescriptor desc = PlatformUI.getWorkbench().getEditorRegistry().getDefaultEditor(file.getName());
            page.openEditor(newInput, desc.getId());
        }
    }
}