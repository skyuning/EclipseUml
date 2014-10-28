package me.skyun.eclipseuml.views;

import java.io.IOException;

import me.skyun.eclipseuml.Utils;
import me.skyun.eclipseuml.uml.UmlLinkClickListener;
import me.skyun.eclipseuml.uml.UmlUtils;

import org.eclipse.core.resources.IFile;
import org.eclipse.jdt.core.ICompilationUnit;
import org.eclipse.jdt.core.IJavaElement;
import org.eclipse.jdt.core.JavaCore;
import org.eclipse.jdt.core.JavaModelException;
import org.eclipse.jdt.internal.ui.javaeditor.CompilationUnitEditor;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.swt.SWT;
import org.eclipse.swt.browser.Browser;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.ui.IEditorPart;
import org.eclipse.ui.IFileEditorInput;
import org.eclipse.ui.ISelectionListener;
import org.eclipse.ui.IWorkbenchPart;
import org.eclipse.ui.part.ViewPart;

@SuppressWarnings("restriction")
public class UmlView extends ViewPart {

    private Browser mBrowser;
    private String mTempUmlFilePath;

    public UmlView() {
        mTempUmlFilePath = Utils.getWorkspaceRoot().getLocation() + "/uml.svg";
    }

    public void createPartControl(final Composite parent) {
        mBrowser = new Browser(parent, SWT.NONE);
        mBrowser.addLocationListener(new UmlLinkClickListener());
        Utils.getActivePage().addSelectionListener(new FileSelectedListener());
    }

    private class FileSelectedListener implements ISelectionListener {
        @Override
        public void selectionChanged(final IWorkbenchPart part, ISelection selection) {
            if (!(part instanceof CompilationUnitEditor))
                return;

            IEditorPart editor = (IEditorPart) part;
            if (!(editor.getEditorInput() instanceof IFileEditorInput))
                return;

            IFile file = ((IFileEditorInput) editor.getEditorInput()).getFile();
            try {
                String umlFilePath = refreshUmlFile(JavaCore.create(file));
                mBrowser.setUrl(umlFilePath + "?" + file.getLocation().toString());
            } catch (JavaModelException e) {
                e.printStackTrace();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }

        private String refreshUmlFile(IJavaElement javaElement) throws JavaModelException, IOException {
            // Only support for IComilationUnit currently
            if (!(javaElement instanceof ICompilationUnit))
                return "";

            String uml = UmlUtils.getCompilationUnitUml((ICompilationUnit) javaElement);
            String svg = UmlUtils.getSvgString(uml);
            Utils.saveFile(mTempUmlFilePath, svg);
            return mTempUmlFilePath;
        }
    }

    @Override
    public void setFocus() {
        mBrowser.setFocus();
    }
}