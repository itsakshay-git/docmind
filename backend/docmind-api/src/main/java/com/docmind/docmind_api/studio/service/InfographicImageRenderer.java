package com.docmind.docmind_api.studio.service;

import com.fasterxml.jackson.databind.JsonNode;
import org.springframework.stereotype.Service;

import javax.imageio.ImageIO;
import java.awt.Color;
import java.awt.Font;
import java.awt.FontMetrics;
import java.awt.Graphics2D;
import java.awt.RenderingHints;
import java.awt.image.BufferedImage;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;

@Service
public class InfographicImageRenderer {

    private static final int WIDTH = 1200;
    private static final int HEIGHT = 1600;
    private static final int MARGIN = 72;

    public void renderPng(
            JsonNode data,
            Path outputPath
    ) {

        try {
            Files.createDirectories(
                    outputPath.getParent()
            );

            BufferedImage image =
                    new BufferedImage(
                            WIDTH,
                            HEIGHT,
                            BufferedImage.TYPE_INT_RGB
                    );

            Graphics2D graphics =
                    image.createGraphics();

            graphics.setRenderingHint(
                    RenderingHints.KEY_ANTIALIASING,
                    RenderingHints.VALUE_ANTIALIAS_ON
            );

            paintBackground(graphics);
            paintTitle(graphics, data.path("title").asText("DocMind Infographic"));
            paintSections(graphics, data.path("sections"));
            graphics.dispose();

            ImageIO.write(
                    image,
                    "png",
                    outputPath.toFile()
            );
        } catch (Exception e) {
            throw new RuntimeException(
                    "Failed to render infographic image",
                    e
            );
        }
    }

    private void paintBackground(
            Graphics2D graphics
    ) {

        graphics.setColor(
                new Color(18, 21, 27)
        );
        graphics.fillRect(
                0,
                0,
                WIDTH,
                HEIGHT
        );

        graphics.setColor(
                new Color(38, 44, 56)
        );
        graphics.fillRoundRect(
                32,
                32,
                WIDTH - 64,
                HEIGHT - 64,
                36,
                36
        );
    }

    private void paintTitle(
            Graphics2D graphics,
            String title
    ) {

        graphics.setColor(
                Color.WHITE
        );
        graphics.setFont(
                new Font(
                        Font.SANS_SERIF,
                        Font.BOLD,
                        54
                )
        );

        drawWrapped(
                graphics,
                title,
                MARGIN,
                116,
                WIDTH - (MARGIN * 2),
                62,
                2
        );
    }

    private void paintSections(
            Graphics2D graphics,
            JsonNode sections
    ) {

        int y = 250;
        int index = 0;

        for (JsonNode section : sections) {
            if (index >= 5) {
                break;
            }

            Color accent =
                    List.of(
                            new Color(132, 181, 255),
                            new Color(151, 222, 178),
                            new Color(245, 196, 104),
                            new Color(226, 159, 233),
                            new Color(126, 219, 214)
                    ).get(index);

            graphics.setColor(
                    new Color(25, 30, 39)
            );
            graphics.fillRoundRect(
                    MARGIN,
                    y,
                    WIDTH - (MARGIN * 2),
                    220,
                    28,
                    28
            );

            graphics.setColor(accent);
            graphics.fillRoundRect(
                    MARGIN,
                    y,
                    12,
                    220,
                    12,
                    12
            );

            graphics.setColor(accent);
            graphics.setFont(
                    new Font(
                            Font.SANS_SERIF,
                            Font.BOLD,
                            30
                    )
            );

            drawWrapped(
                    graphics,
                    section.path("heading").asText("Section"),
                    MARGIN + 34,
                    y + 48,
                    WIDTH - (MARGIN * 2) - 70,
                    36,
                    2
            );

            graphics.setColor(
                    new Color(232, 236, 242)
            );
            graphics.setFont(
                    new Font(
                            Font.SANS_SERIF,
                            Font.PLAIN,
                            24
                    )
            );

            int pointY =
                    y + 110;

            for (String point : points(section.path("points"))) {
                drawWrapped(
                        graphics,
                        "- " + point,
                        MARGIN + 42,
                        pointY,
                        WIDTH - (MARGIN * 2) - 84,
                        30,
                        2
                );
                pointY += 58;
            }

            y += 255;
            index++;
        }
    }

    private List<String> points(
            JsonNode points
    ) {

        List<String> values =
                new ArrayList<>();

        for (JsonNode point : points) {
            if (values.size() >= 2) {
                break;
            }

            values.add(
                    point.asText()
            );
        }

        return values;
    }

    private void drawWrapped(
            Graphics2D graphics,
            String text,
            int x,
            int y,
            int width,
            int lineHeight,
            int maxLines
    ) {

        FontMetrics metrics =
                graphics.getFontMetrics();

        List<String> lines =
                new ArrayList<>();

        StringBuilder line =
                new StringBuilder();

        for (String word : text.split("\\s+")) {
            String next =
                    line.isEmpty()
                            ? word
                            : line + " " + word;

            if (metrics.stringWidth(next) > width && !line.isEmpty()) {
                lines.add(line.toString());
                line =
                        new StringBuilder(word);
            } else {
                line =
                        new StringBuilder(next);
            }
        }

        if (!line.isEmpty()) {
            lines.add(
                    line.toString()
            );
        }

        for (int i = 0; i < Math.min(maxLines, lines.size()); i++) {
            graphics.drawString(
                    lines.get(i),
                    x,
                    y + (i * lineHeight)
            );
        }
    }
}
