package org.intellij.plugins.ui.common;

import javax.swing.plaf.basic.BasicGraphicsUtils;
import javax.swing.plaf.basic.BasicLabelUI;
import javax.swing.*;
import java.awt.Graphics;
import java.awt.Rectangle;
import java.awt.FontMetrics;
import java.awt.Dimension;

public class ElipsisLabelUI extends BasicLabelUI {

   public ElipsisLabelUI() {
   }

   protected String layoutCL(JLabel label,
                             FontMetrics metrics,
                             String text,
                             Icon icon,
                             Rectangle viewRect,
                             Rectangle iconRect,
                             Rectangle textRect) {
      String abbreviatedText = computeCorrectlyOrientedAbbreviatedText(label,
                                                                       metrics,
                                                                       text,
                                                                       icon,
                                                                       label.getVerticalAlignment(),
                                                                       label.getHorizontalAlignment(),
                                                                       label.getVerticalTextPosition(),
                                                                       label.getHorizontalTextPosition(),
                                                                       viewRect,
                                                                       iconRect,
                                                                       textRect,
                                                                       label.getIconTextGap());
      if (abbreviatedText.equals(""))
         return text;
      else
         return abbreviatedText;
   }

   public static String computeCorrectlyOrientedAbbreviatedText(JComponent component,
                                                                FontMetrics metrics,
                                                                String text,
                                                                Icon icon,
                                                                int vAlign,
                                                                int hAlign,
                                                                int vTextPos,
                                                                int hTextPos,
                                                                Rectangle viewRect,
                                                                Rectangle iconRect,
                                                                Rectangle textRect,
                                                                int iconTextGap) {
      boolean isLeftToRight = true;
      int newHAlign = hAlign;
      int newHTextPos = hTextPos;
      if (component != null && !component.getComponentOrientation().isLeftToRight())
         isLeftToRight = false;
      switch (hAlign) {
         case SwingConstants.LEADING:
            newHAlign = isLeftToRight ? SwingConstants.LEFT : SwingConstants.RIGHT;
            break;

         case SwingConstants.TRAILING:
            newHAlign = isLeftToRight ? SwingConstants.RIGHT : SwingConstants.LEFT;
            break;
      }
      switch (hTextPos) {
         case SwingConstants.LEADING:
            newHTextPos = isLeftToRight ? SwingConstants.LEFT : SwingConstants.RIGHT;
            break;

         case SwingConstants.TRAILING:
            newHTextPos = isLeftToRight ? SwingConstants.RIGHT : SwingConstants.LEFT;
            break;
      }
      return  computeAbbreviatedText(metrics,
               text,
               icon,
               vAlign,
               newHAlign,
               vTextPos,
               newHTextPos,
               viewRect,
               iconRect,
               textRect,
               iconTextGap);
   }

   public static String computeAbbreviatedText(FontMetrics metrics,
                          String text,
                          Icon icon,
                          int vAlign,
                          int hAlign,
                          int vTextPos,
                          int hTextPos,
                          Rectangle viewRect,
                          Rectangle iconRect,
                          Rectangle textRect,
                          int iconTextGap) {
      if (icon != null) {
         iconRect.width = icon.getIconWidth();
         iconRect.height = icon.getIconHeight();
      } else {
         iconRect.width = iconRect.height = 0;
      }
      boolean isTextEmply = text == null || text.equals("");
      if (isTextEmply) {
         textRect.width = textRect.height = 0;
      } else {
         Dimension dimension = new Dimension(SwingUtilities.computeStringWidth(metrics, text),
                                             metrics.getHeight());
         textRect.width = dimension.width;
         textRect.height = dimension.height;
      }
      int gap = !isTextEmply && icon != null ? iconTextGap : 0;
      String computedText = "";
      if (!isTextEmply) {
         int width;
         if (hTextPos == 0)
            width = viewRect.width;
         else
            width = viewRect.width - (iconRect.width + gap);
         if (textRect.width > width) {
            String elipsis = "...";
            int elipsisWidth = SwingUtilities.computeStringWidth(metrics, elipsis);
            for (int i = text.length() - 1; i >= 0; i--) {
               elipsisWidth += metrics.charWidth(text.charAt(i));
               if (elipsisWidth > width)
                  break;
               computedText = text.charAt(i) + computedText;
            }

            computedText = elipsis + computedText;
            textRect.width = SwingUtilities.computeStringWidth(metrics, computedText);
         }
      }
      if (vTextPos == 1) {
         if (hTextPos != 0)
            textRect.y = 0;
         else
            textRect.y = -(textRect.height + gap);
      } else if (vTextPos == 0)
         textRect.y = iconRect.height / 2 - textRect.height / 2;
      else if (hTextPos != 0)
         textRect.y = iconRect.height - textRect.height;
      else
         textRect.y = iconRect.height + gap;
      if (hTextPos == 2)
         textRect.x = -(textRect.width + gap);
      else if (hTextPos == 0)
         textRect.x = iconRect.width / 2 - textRect.width / 2;
      else
         textRect.x = iconRect.width + gap;
      int minX = Math.min(iconRect.x, textRect.x);
      int maxX = Math.max(iconRect.x + iconRect.width, textRect.x + textRect.width) - minX;
      int minY = Math.min(iconRect.y, textRect.y);
      int maxY = Math.max(iconRect.y + iconRect.height, textRect.y + textRect.height) - minY;
      int y;
      if (vAlign == 1)
         y = viewRect.y - minY;
      else if (vAlign == 0)
         y = (viewRect.y + viewRect.height / 2) - (minY + maxY / 2);
      else
         y = (viewRect.y + viewRect.height) - (minY + maxY);
      int x;
      if (hAlign == 2)
         x = viewRect.x - minX;
      else if (hAlign == 4)
         x = (viewRect.x + viewRect.width) - (minX + maxX);
      else
         x = (viewRect.x + viewRect.width / 2) - (minX + maxX / 2);
      textRect.x += x;
      textRect.y += y;
      iconRect.x += x;
      iconRect.y += y;
      return computedText;
   }

   protected void paintEnabledText(JLabel label, Graphics g, String text, int x, int y) {
      int underlinedChar = label.getDisplayedMnemonic();
      g.setColor(label.getForeground());
      BasicGraphicsUtils.drawString(g, text, underlinedChar, x, y);
   }

   protected void paintDisabledText(JLabel label, Graphics g, String text, int x, int y) {
      int underlinedChar = label.getDisplayedMnemonic();
      g.setColor(label.getBackground());
      BasicGraphicsUtils.drawString(g, text, underlinedChar, x, y);
   }

}
