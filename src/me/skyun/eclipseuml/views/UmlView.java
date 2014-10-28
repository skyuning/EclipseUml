package me.skyun.eclipseuml.views;

import me.skyun.eclipseuml.uml.UmlLinkClickListener;

import org.eclipse.swt.SWT;
import org.eclipse.swt.browser.Browser;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.ui.part.ViewPart;

public class UmlView extends ViewPart {

    Browser mBrowser;

    public void createPartControl(final Composite parent) {
        mBrowser = new Browser(parent, SWT.NONE);
        mBrowser.addLocationListener(new UmlLinkClickListener());
    }

    @Override
    public void setFocus() {
        mBrowser.setFocus();
    }

    public void showUml(String uri) {
        mBrowser.setUrl(uri);
    }
}
