package net.mcose.friendsapi.skins;

import java.awt.Graphics;
import java.awt.image.BufferedImage;

/**
 * Modern skin canonicalization for old renderers.
 *
 * This is the part that makes beta 1.7.3 render modern skins correctly:
 * 64x32 skins are expanded into 64x64 using the same copy rectangles as modern
 * Minecraft, opaque body regions are normalized, and legacy hat-layer
 * transparency gets the old Notch compatibility pass.
 */
public final class SkinImageProcessor {
    private SkinImageProcessor() {
    }

    public static BufferedImage processSkinImage(BufferedImage src, PlayerTextureInfo info) {
        if (src == null) {
            return null;
        }
        int width = src.getWidth();
        int height = src.getHeight();
        if (width != 64 || (height != 32 && height != 64)) {
            throw new IllegalArgumentException("Expected 64x32 or 64x64 skin, got " + width + "x" + height);
        }

        boolean legacy = height == 32;
        BufferedImage image = src;
        if (legacy) {
            if (info != null) {
                info.setSkinVariant("classic");
            }
            BufferedImage upgraded = new BufferedImage(64, 64, BufferedImage.TYPE_INT_ARGB);
            Graphics g = upgraded.getGraphics();
            try {
                g.drawImage(src, 0, 0, null);
            } finally {
                g.dispose();
            }
            image = upgraded;
            fillRect(image, 0, 32, 64, 32, 0x00000000);

            // Copy map parity with modern SkinTextureDownloader.processLegacySkin.
            copyRect(image, 4, 16, 16, 32, 4, 4, true, false);
            copyRect(image, 8, 16, 16, 32, 4, 4, true, false);
            copyRect(image, 0, 20, 24, 32, 4, 12, true, false);
            copyRect(image, 4, 20, 16, 32, 4, 12, true, false);
            copyRect(image, 8, 20, 8, 32, 4, 12, true, false);
            copyRect(image, 12, 20, 16, 32, 4, 12, true, false);

            copyRect(image, 44, 16, -8, 32, 4, 4, true, false);
            copyRect(image, 48, 16, -8, 32, 4, 4, true, false);
            copyRect(image, 40, 20, 0, 32, 4, 12, true, false);
            copyRect(image, 44, 20, -8, 32, 4, 12, true, false);
            copyRect(image, 48, 20, -16, 32, 4, 12, true, false);
            copyRect(image, 52, 20, -8, 32, 4, 12, true, false);
        }

        setNoAlpha(image, 0, 0, 32, 16);
        if (legacy) {
            doNotchTransparencyHack(image, 32, 0, 64, 32);
        }
        setNoAlpha(image, 0, 16, 64, 32);
        setNoAlpha(image, 16, 48, 48, 64);
        return image;
    }

    private static void fillRect(BufferedImage image, int x, int y, int width, int height, int color) {
        for (int py = y; py < y + height; py++) {
            for (int px = x; px < x + width; px++) {
                image.setRGB(px, py, color);
            }
        }
    }

    private static void copyRect(BufferedImage image, int srcX, int srcY, int xOffset, int yOffset, int width, int height, boolean mirrorX, boolean mirrorY) {
        int[] src = new int[width * height];
        for (int row = 0; row < height; row++) {
            for (int col = 0; col < width; col++) {
                src[row * width + col] = image.getRGB(srcX + col, srcY + row);
            }
        }
        int dstX = srcX + xOffset;
        int dstY = srcY + yOffset;
        for (int row = 0; row < height; row++) {
            for (int col = 0; col < width; col++) {
                int outX = mirrorX ? width - 1 - col : col;
                int outY = mirrorY ? height - 1 - row : row;
                image.setRGB(dstX + outX, dstY + outY, src[row * width + col]);
            }
        }
    }

    private static void setNoAlpha(BufferedImage image, int x0, int y0, int x1, int y1) {
        for (int x = x0; x < x1; x++) {
            for (int y = y0; y < y1; y++) {
                image.setRGB(x, y, image.getRGB(x, y) | 0xFF000000);
            }
        }
    }

    private static void doNotchTransparencyHack(BufferedImage image, int x0, int y0, int x1, int y1) {
        for (int x = x0; x < x1; x++) {
            for (int y = y0; y < y1; y++) {
                int alpha = (image.getRGB(x, y) >>> 24) & 255;
                if (alpha < 128) {
                    return;
                }
            }
        }
        for (int x = x0; x < x1; x++) {
            for (int y = y0; y < y1; y++) {
                image.setRGB(x, y, image.getRGB(x, y) & 0x00FFFFFF);
            }
        }
    }
}
