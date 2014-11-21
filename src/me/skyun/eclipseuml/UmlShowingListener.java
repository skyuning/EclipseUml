package me.skyun.eclipseuml;

import java.io.IOException;

import me.skyun.eclipseuml.uml.UmlUtils;
import me.skyun.eclipseuml.views.UmlView;

import org.eclipse.core.resources.IFile;
import org.eclipse.jdt.core.ICompilationUnit;
import org.eclipse.jdt.core.IJavaElement;
import org.eclipse.jdt.core.JavaCore;
import org.eclipse.jdt.internal.ui.javaeditor.CompilationUnitEditor;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.ui.IEditorPart;
import org.eclipse.ui.IFileEditorInput;
import org.eclipse.ui.ISelectionListener;
import org.eclipse.ui.IWorkbenchPart;

/**
 * @author linyun Listen the WorkBenchPart change event, if change to
 *         EditorPart, the showing the uml image.
 */
@SuppressWarnings("restriction")
public class UmlShowingListener implements ISelectionListener {

    private String mTempUmlFilePath = Utils.getWorkspaceRoot().getLocation() + "/uml.svg";

    @Override
    public void selectionChanged(final IWorkbenchPart part, ISelection selection) {
        if (!(part instanceof CompilationUnitEditor))
            return;

        IEditorPart editor = (IEditorPart) part;

        if (!(editor.getEditorInput() instanceof IFileEditorInput))
            return;

        IFile file = ((IFileEditorInput) editor.getEditorInput()).getFile();
        IJavaElement javaElement = JavaCore.create(file);

        // Only support for IComilationUnit currently
        if (!(javaElement instanceof ICompilationUnit))
            return;

        String uml = UmlUtils.getCompilationUnitUml((ICompilationUnit) javaElement);
        showUmlImage(uml, file.getLocation().toString());
    }

    private void showUmlImage(String uml, String fileLocation) {
        try {
            String svg = UmlUtils.getSvgString(uml);
            Utils.saveFile(mTempUmlFilePath, svg);
            UmlView umlView = (UmlView) Utils.getActivePage().findView(UmlView.class.getName());
            if (umlView != null)
                umlView.showUml(mTempUmlFilePath + "?" + fileLocation);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}