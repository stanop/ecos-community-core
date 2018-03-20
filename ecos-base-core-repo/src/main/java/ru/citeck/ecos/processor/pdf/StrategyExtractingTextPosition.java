package ru.citeck.ecos.processor.pdf;

import com.itextpdf.text.pdf.parser.SimpleTextExtractionStrategy;
import com.itextpdf.text.pdf.parser.TextRenderInfo;
import com.itextpdf.text.pdf.parser.Vector;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class StrategyExtractingTextPosition extends SimpleTextExtractionStrategy {
    private List<Float> coordX;
    private List<Float> coordY;
    private String findString;
    private String positionFindString;

    public StrategyExtractingTextPosition(String findString, String positionFindString) {
        this.findString = findString;
        this.positionFindString = positionFindString;
        coordX = new ArrayList<>();
        coordY = new ArrayList<>();
    }

    @Override
    public void renderText(TextRenderInfo renderInfo) {
        float startPosX = renderInfo.getBaseline().getStartPoint().get(Vector.I1);
        float endPosX = renderInfo.getBaseline().getEndPoint().get(Vector.I1);
        float startPosY = renderInfo.getBaseline().getStartPoint().get(Vector.I2);
        float endPosY = renderInfo.getBaseline().getEndPoint().get(Vector.I2);
        float x;
        float y;

        if (positionFindString.equals(PositionFindString.START_POSITION.getValue())) {
            x = startPosX;
            y = startPosY;
        } else if(positionFindString.equals(PositionFindString.END_POSITION.getValue())) {
            x = endPosX;
            y = endPosY;
        } else if(positionFindString.equals(PositionFindString.MIDDLE_POSITION.getValue())) {
            x = startPosX + (endPosX - startPosX);
            y = startPosY + (endPosY - startPosY);
        } else {
            x = 0f;
            y = 0f;
        }

        if (renderInfo.getText().contains(findString)) {
            coordX.add(x);
            coordY.add(y);
        }
    }

    public float getX() {
        return Collections.min(coordX);
    }

    public float getY() {
        return Collections.max(coordY);
    }

    private enum PositionFindString {
        START_POSITION("start"),
        END_POSITION("end"),
        MIDDLE_POSITION("middle");

        final String value;

        PositionFindString(String value) {
            this.value = value;
        }

        String getValue() {
            return value;
        }
    }

    public boolean isFoundX() {
        return !coordX.isEmpty();
    }

    public boolean isFoundY() {
        return !coordY.isEmpty();
    }
}
