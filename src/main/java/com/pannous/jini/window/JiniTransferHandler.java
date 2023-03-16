package com.pannous.jini.window;

import java.awt.datatransfer.DataFlavor;

public class JiniTransferHandler extends javax.swing.TransferHandler {
    private final MyToolWindow window;

    public JiniTransferHandler(MyToolWindow myToolWindow) {
        super();
        window = myToolWindow;
    }

    @Override
    public boolean importData(TransferSupport support) {
        try {
            Object data = support.getTransferable().getTransferData(DataFlavor.stringFlavor);
            window.addResponse(data.toString());
        } catch (Exception e) {
        }
        return true;
    }
}
