package net.codeocean.cheese.core.utils;

import android.accessibilityservice.AccessibilityService;
import android.graphics.Rect;
import android.icu.text.SimpleDateFormat;
import android.util.Log;
import android.view.accessibility.AccessibilityNodeInfo;
import android.view.accessibility.AccessibilityWindowInfo;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.io.Writer;
import java.util.Date;
import java.util.List;

public class Uix {
    private static final String TAG = "UiHierarchyUtil";


    public static void saveCurrentUiHierarchyToFile(AccessibilityService accessibilityService, String path) {
        List<AccessibilityWindowInfo> windows = accessibilityService.getWindows();
        Writer writer = null;
        try {
            File file = createOutputFile(path);
            if (file == null) {
                Log.e(TAG, "Failed to create output file");
                return;
            }

            writer = new OutputStreamWriter(new FileOutputStream(file), "UTF-8");
            writer.write("<hierarchy rotation=\"0\">\n");

            for (AccessibilityWindowInfo window : windows) {
                AccessibilityNodeInfo rootNode = window.getRoot();
                if (rootNode != null) {
                    dumpNodeToXml(rootNode, writer, 1);
                    rootNode.recycle();
                }
            }

            writer.write("</hierarchy>");

            Log.d(TAG, "UI hierarchy saved to file: " + file.getAbsolutePath());
        } catch (IOException e) {
            Log.e(TAG, "Failed to save UI hierarchy to file", e);
        } finally {
            if (writer != null) {
                try {
                    writer.close();
                } catch (IOException e) {
                    Log.e(TAG, "Failed to close file writer", e);
                }
            }
        }
    }

    private static File createOutputFile(String path) throws IOException {
        String fileName = "uix" + ".xml";
        File dir = new File(path);
        if (!dir.exists()) {
            if (!dir.mkdirs()) {
                return null;
            }
        }
        File file = new File(dir, fileName);
        if (!file.exists()) {
            if (!file.createNewFile()) {
                return null;
            }
        }
        return file;
    }

    private static void dumpNodeToXml(AccessibilityNodeInfo node, Writer writer, int indent)
            throws IOException {
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < indent; i++) {
            sb.append("\t");
        }
        sb.append("<node");
        sb.append(" drawingorder=\"" + node.getDrawingOrder() + "\"");
        sb.append(" index=\"" + "0" + "\"");
        sb.append(" layer=\"" + "0" + "\"");
        sb.append(" depth=\"" + "0" + "\"");
        sb.append(" text=\"" + node.getText() + "\"");
        sb.append(" clz=\"" + node.getClassName() + "\"");
        sb.append(" pkg=\"" + node.getPackageName() + "\"");
        sb.append(" desc=\"" + node.getContentDescription() + "\"");
        sb.append(" checkable=\"" + node.isCheckable() + "\"");
        sb.append(" checked=\"" + node.isChecked() + "\"");
        sb.append(" clickable=\"" + node.isClickable() + "\"");
        sb.append(" enabled=\"" + node.isEnabled() + "\"");
        sb.append(" focusable=\"" + node.isFocusable() + "\"");
        sb.append(" focused=\"" + node.isFocused() + "\"");
        sb.append(" scrollable=\"" + node.isScrollable() + "\"");
        sb.append(" longclickable=\"" + node.isLongClickable() + "\"");
        sb.append(" password=\"" + node.isPassword() + "\"");
        sb.append(" selected=\"" + node.isSelected() + "\"");
        sb.append(" nid=\"" + "node" + "\"");
        sb.append(" id=\"" + node.getViewIdResourceName() + "\"");
        sb.append(" visible=\"" + node.isVisibleToUser() + "\"");
        sb.append(" multiline=\"" + node.isMultiLine() + "\"");
        sb.append(" dismissable=\"" + node.isDismissable() + "\"");
        sb.append(" editable=\"" + node.isEditable() + "\"");
        Rect rect = new Rect();
        node.getBoundsInScreen(rect);
        sb.append(" bounds=\"" + rect.toShortString() + "\"");
        sb.append(" left=\"" + rect.left + "\"");
        sb.append(" top=\"" + rect.top + "\"");
        sb.append(" right=\"" + rect.right + "\"");
        sb.append(" bottom=\"" + rect.bottom + "\"");
        sb.append(" parentid=\"" + "node.getParent()" + "\"");
        sb.append(" childcount=\"" + node.getChildCount() + "\">");
        writer.write(sb.toString());
        writer.write("\n");
        int childCount = node.getChildCount();
        for (int i = 0; i < childCount; i++) {
            AccessibilityNodeInfo childNode = node.getChild(i);
            if (childNode != null) {
                dumpNodeToXml(childNode, writer, indent + 1);
                childNode.recycle();
            }
        }
        writer.write("</node>\n");
    }

}
